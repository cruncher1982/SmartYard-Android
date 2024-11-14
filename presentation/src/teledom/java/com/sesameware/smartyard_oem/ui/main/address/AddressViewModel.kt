package com.sesameware.smartyard_oem.ui.main.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sesameware.data.DataModule
import com.sesameware.data.prefs.PreferenceStorage
import com.sesameware.domain.interactors.AddressInteractor
import com.sesameware.domain.interactors.AuthInteractor
import com.sesameware.domain.interactors.DatabaseInteractor
import com.sesameware.domain.interactors.IssueInteractor
import com.sesameware.domain.model.AddressItem
import com.sesameware.domain.model.StateButton
import com.sesameware.domain.model.response.Address
import com.sesameware.smartyard_oem.Event
import com.sesameware.smartyard_oem.GenericViewModel
import com.sesameware.smartyard_oem.R
import com.sesameware.smartyard_oem.ui.main.address.event_log.Flat
import com.sesameware.smartyard_oem.ui.main.address.models.AddressListItem
import com.sesameware.smartyard_oem.ui.main.address.models.HouseState
import com.sesameware.smartyard_oem.ui.main.address.models.EntranceId
import com.sesameware.smartyard_oem.ui.main.address.models.EntranceState
import com.sesameware.smartyard_oem.ui.main.address.models.IssueModel
import timber.log.Timber

class AddressViewModel(
    private val addressInteractor: AddressInteractor,
    override val mPreferenceStorage: PreferenceStorage,
    override val mAuthInteractor: AuthInteractor,
    private val issueInteractor: IssueInteractor,
    override val mDatabaseInteractor: DatabaseInteractor
) : GenericViewModel() {

    private val _dataList = MutableLiveData<List<AddressListItem>>()
    val dataList: LiveData<List<AddressListItem>>
        get() = _dataList

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean>
        get() = _progress
    private val _navigationToAuth = MutableLiveData<Event<Unit>>()
    val navigationToAuth: LiveData<Event<Unit>>
        get() = _navigationToAuth

    var houseIdFlats: HashMap<Int, List<Flat>> = hashMapOf()
        private set

    fun openDoor(id: EntranceId) {
        viewModelScope.withProgress {
            mAuthInteractor.openDoor(id.domophoneId, id.doorId)
        }
    }

    //учитывать ли кэш при следующем запросе списка адресов
    var nextListNoCache = true

    init {
        getDataList()
    }

    fun setAddressItemExpanded(position: Int, isExpanded: Boolean) {
        val addressList = _dataList.value?.toMutableList() ?: return
        val listItem = try {
            addressList[position]
        } catch (e: Exception) {
            return
        }
        if (listItem !is HouseState) return

        val newState = listItem.copy(isExpanded = isExpanded)
        addressList[position] = newState
        _dataList.postValue(addressList)
    }

    private suspend fun getHouseIdFlats(): HashMap<Int, List<Flat>> {
        val res = addressInteractor.getSettingsList()
        val houseFlats = hashMapOf<Int, MutableSet<Int>>()
        val flatToNumber = hashMapOf<Int, String>()
        res?.data?.forEach { settingItem ->
            flatToNumber[settingItem.flatId] = settingItem.flatNumber
            if (settingItem.hasPlog) {
                (houseFlats.getOrPut(settingItem.houseId) {mutableSetOf()}).add(settingItem.flatId)
            }
        }

        val houseIdFlats = hashMapOf<Int, List<Flat>>()  // идентификатор дома с квартирами пользователя
        houseFlats.keys.forEach { houseId ->
            houseIdFlats[houseId] = houseFlats[houseId]!!.map { flatId ->
                val resIntercom = addressInteractor.getIntercom(flatId)
                val frsEnabled = (resIntercom.data.frsDisabled == false)
                Flat(flatId, flatToNumber[flatId]!!, frsEnabled)
            }
            houseIdFlats[houseId]?.sortedBy {
                it.flatNumber
            }
        }

        Timber.d("debug_dmm houseIdFlats = $houseIdFlats")
        return houseIdFlats
    }

    fun getDataList(forceRefresh: Boolean = false) {
        val noCache = nextListNoCache || forceRefresh
        nextListNoCache = false
        viewModelScope.withProgress(
            handleError = {
                true
            },
            progress = _progress
        ) {
            populateData(noCache)
        }
    }

    private suspend fun populateData(noCache: Boolean) {
        if (noCache) {
            mPreferenceStorage.xDmApiRefresh = true
        }
        houseIdFlats = getHouseIdFlats()
        if (noCache) {
            mPreferenceStorage.xDmApiRefresh = true
        }
        val response = addressInteractor.getAddressList()
        if (response?.data == null) {
            _dataList.value = listOf()
            if (!mPreferenceStorage.whereIsContractWarningSeen) {
                _navigationToAuth.value = (Event(Unit))
            }
            return
        }

        Timber.d(this.javaClass.simpleName, response.data.size)
        mDatabaseInteractor.deleteAll()

        val expandedHouseIds = dataList.value?.asSequence()
            ?.filterIsInstance<HouseState>()
            ?.filter { it.isExpanded }
            ?.map { it.houseId }
            ?.toSet()
            ?: setOf()

        val addressListDto = response.data
        val houseStateList = addressListDto.map { addressDto ->
            val entranceList = addressDto.doors.map { entranceDto ->
                addToWidgetDatabase(entranceDto, addressDto)
                EntranceState(
                    iconRes = when (entranceDto.icon) {
                        "barrier" -> R.drawable.ic_barrier
                        "gate" -> R.drawable.ic_gates
                        "wicket" -> R.drawable.ic_wicket
                        "entrance" -> R.drawable.ic_porch
                        else -> R.drawable.ic_barrier
                    },
                    name = entranceDto.name,
                    entranceId = EntranceId(
                        domophoneId = entranceDto.domophoneId,
                        doorId = entranceDto.doorId
                    )
                )
            }
            val houseHasEntrances = entranceList.isNotEmpty()
            val houseHasFlats = houseIdFlats[addressDto.houseId]?.isNotEmpty() ?: false
            val isExpanded = expandedHouseIds.contains(addressDto.houseId)
            HouseState(
                houseId = addressDto.houseId,
                address = addressDto.address,
                entranceList = entranceList,
                cameraCount = addressDto.cctv,
                hasEventLog = addressDto.hasPlog && houseHasEntrances && houseHasFlats,
                isExpanded = isExpanded
            )
        }.toMutableList()

        houseStateList.sortWith(
            compareBy(
                { it.entranceList.isEmpty() },
                { it.address }
            )
        )

        if (expandedHouseIds.isEmpty()) {
            val newState = houseStateList[0].copy(isExpanded = true)
            houseStateList[0] = newState
        }

        val issuesList = if (DataModule.providerConfig.issuesVersion != "2") {
            issueInteractor.listConnectIssue()?.data
        } else {
            issueInteractor.listConnectIssueV2()?.data
        }
        _dataList.value = houseStateList.plus(
            issuesList?.map {
                IssueModel(
                    it.address ?: "",
                    it.key ?: "",
                    it.courier ?: ""
                )
            } ?: emptyList()
        )
    }

    private suspend fun addToWidgetDatabase(
        entranceDto: Address.Door,
        addressDto: Address
    ) {
        mDatabaseInteractor
            .createItem(
                AddressItem(
                    name = entranceDto.name,
                    address = addressDto.address,
                    icon = entranceDto.icon,
                    domophoneId = entranceDto.domophoneId,
                    doorId = entranceDto.doorId,
                    state = StateButton.CLOSE
                )
            )
    }
}

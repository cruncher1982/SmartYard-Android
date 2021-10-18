package ru.madbrains.smartyard.ui.main.pay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ru.madbrains.domain.interactors.PayInteractor
import ru.madbrains.smartyard.Event
import ru.madbrains.smartyard.GenericViewModel

class PayAddressViewModel(
    private val payInteractor: PayInteractor
) : GenericViewModel() {

    private val _addressList = MutableLiveData<Event<List<PayAddressModel>>>()
    val addressList: LiveData<Event<List<PayAddressModel>>>
        get() = _addressList

    private val _progress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean>
        get() = _progress

    private val _navigateToСontractFragment = MutableLiveData<Event<Unit>>()
    val navigateToСontractFragment: LiveData<Event<Unit>>
        get() = _navigateToСontractFragment

    private val _selectedItemIndex = MutableLiveData<Int>()
    val selectedItemIndex: LiveData<Int>
        get() = _selectedItemIndex

    fun navigateToPayContractFragment(position: Int) {
        setIndex(position)
        _navigateToСontractFragment.value = Event(Unit)
    }

    fun setIndex(index: Int) {
        _selectedItemIndex.value = index
    }

    fun updateIndex() {
        setIndex(selectedItemIndex.value ?: 0)
    }

    fun getPaymentsList() {
        viewModelScope.withProgress(
            handleError = {
                true
            },
            progress = _progress
        ) {
            val res =
                payInteractor.getPaymentsList()
            if (res?.data != null) {
                res.data.let {
                    val listPayAddress = it.map {
                        PayAddressModel(
                            address = it.address,
                            accounts = it.accounts.map {
                                PayAddressModel.Account(
                                    balance = it.balance,
                                    blocked = it.blocked,
                                    bonus = it.bonus,
                                    clientId = it.clientId,
                                    clientName = it.clientName,
                                    contractName = it.contractName,
                                    contractPayName = it.contractPayName,
                                    services = it.services,
                                    payAdvice = it.payAdvice,
                                    lcab = it.lcab,
                                    lcabPay = it.lcabPay
                                )
                            }
                        )
                    }
                    // listPayAddress = listOf(listPayAddress[0])
                    _addressList.value = Event(listPayAddress)
                    updateIndex()
                }
            }
        }
    }
}

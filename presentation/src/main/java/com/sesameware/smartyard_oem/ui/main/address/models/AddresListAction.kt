package com.sesameware.smartyard_oem.ui.main.address.models

import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.VideoCameraModelP

sealed interface AddressAction

data class OnOpenEntranceClick(val entranceId: EntranceId) : AddressAction
data class OnExpandClick(val position: Int, val isExpanded: Boolean) : AddressAction
data class OnItemFullyExpanded(val position: Int) : AddressAction
data class OnCameraClick(val model: VideoCameraModelP) : AddressAction
data class OnEventLogClick(val title: String, val houseId: Int) : AddressAction
package com.sesameware.smartyard_oem.ui.main.address.models

import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.VideoCameraModelP

sealed interface AddressAction

data class OnOpenEntranceClick(val id: EntranceId) : AddressAction
data class OnExpandClick(val pos: Int, val isExpanded: Boolean) : AddressAction
data class OnCameraClick(val model: VideoCameraModelP) : AddressAction
data object OnEventLogClick : AddressAction
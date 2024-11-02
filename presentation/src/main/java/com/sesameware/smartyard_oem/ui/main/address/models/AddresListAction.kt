package com.sesameware.smartyard_oem.ui.main.address.models

import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.EventLogModel
import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.VideoCameraModel

sealed interface AddressListAction

data class OpenEntranceAction(val domophoneId: Int, val doorId: Int) : AddressListAction
data class ExpandAddressAction(val pos: Int, val isExpanded: Boolean) : AddressListAction
data class IssueAction(val issueModel: IssueModel) : AddressListAction
data object QrCodeAction : AddressListAction
data class CameraAction(val model: VideoCameraModel) : AddressListAction
data class EventLogAction(val model: EventLogModel) : AddressListAction
package com.sesameware.smartyard_oem.ui.main.address.models

data class AddressState(
    val houseId: Int,
    val title: String,
    val entranceList: List<EntranceState>,
    val cameraCount: Int,
    val hasEventLog: Boolean,
    val isExpanded: Boolean
) : AddressListItem
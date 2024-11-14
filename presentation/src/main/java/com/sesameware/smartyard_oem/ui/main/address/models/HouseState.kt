package com.sesameware.smartyard_oem.ui.main.address.models

data class HouseState(
    val houseId: Int,
    val address: String,
    val entranceList: List<EntranceState>,
    val cameraCount: Int,
    val hasEventLog: Boolean,
    val isExpanded: Boolean = false,
    val savedPosition: Int = Int.MAX_VALUE
) : AddressListItem
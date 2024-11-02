package com.sesameware.smartyard_oem.ui.main.address.models

import com.sesameware.smartyard_oem.ui.main.address.models.interfaces.AddressListItem

data class AddressState(
    val houseId: Int,
    val title: String,
    val entranceList: List<String>,
    val cameraCount: Int,
    val hasEventLog: Boolean,
    val isExpanded: Boolean
) : AddressListItem
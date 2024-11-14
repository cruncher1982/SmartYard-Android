package com.sesameware.smartyard_oem.ui.main.address.models

import androidx.annotation.DrawableRes

data class EntranceState(
    @DrawableRes
    val iconRes: Int,
    val name: String,
    val entranceId: EntranceId
)

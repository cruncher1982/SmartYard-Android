package com.sesameware.data

import com.huawei.agconnect.crash.AGConnectCrash

object Crashlytics {
    fun getInstance(): AGConnectCrash {
        return AGConnectCrash.getInstance()
    }
}

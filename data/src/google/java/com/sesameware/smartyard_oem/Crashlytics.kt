package com.sesameware.data

import com.google.firebase.crashlytics.FirebaseCrashlytics

object Crashlytics {
    fun getInstance(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }
}

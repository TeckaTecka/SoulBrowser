package com.teckatecka.netadmin

import android.app.Application

class NetAdminApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NetAdminApp
            private set
    }
}

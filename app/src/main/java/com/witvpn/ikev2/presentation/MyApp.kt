package com.witvpn.ikev2.presentation

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Process
import com.witvpn.ikev2.BuildConfig
import com.witvpn.ikev2.presentation.utils.connectivity.ConnectivityProvider
import dagger.hilt.android.HiltAndroidApp
import org.strongswan.android.logic.StrongSwanApplication
import timber.log.Timber

@HiltAndroidApp
open class MyApp : StrongSwanApplication() {
    companion object {
        lateinit var self: MyApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        self = this
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        if (isMainProcess()) {
            ConnectivityProvider.createProvider(this).subscribe()
        }
    }

    // your package name is the same with your main process name
    private fun isMainProcess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageName == getProcessName()
        } else packageName == getProcessNameLegacy()
    }

    // you can use this method to get current process name, you will get
    private fun getProcessNameLegacy(): String? {
        val mypid = Process.myPid()
        val manager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.runningAppProcesses
        for (info in infos) {
            if (info.pid == mypid) {
                return info.processName
            }
        }
        // may never return null
        return null
    }
}
package com.duke.wechathelper

import android.app.Application
import com.orhanobut.hawk.Hawk
import kotlin.properties.Delegates

/**
 * 描述：
 *
 */
class HelperApp : Application() {
    companion object {
        var instance by Delegates.notNull<HelperApp>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(this).build()
    }
}
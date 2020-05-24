package com.catnemo.camerastudy

import android.app.Application
import android.content.Context
import com.catnemo.zfilter.FilterDrawer
import java.util.logging.Filter

/**
 *
 * @author matrixJoy
 * @version V1.0
 * @since 2019/03/23
 *
 */
class ZApplication : Application() {

    companion object {
        lateinit var sApp: ZApplication
        lateinit var sContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        sApp = this
        sContext = applicationContext
        FilterDrawer.sContext = sContext
    }
}
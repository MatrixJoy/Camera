package com.catnemo.zfilter.util

import android.util.Log
import com.catnemo.zfilter.BuildConfig

/**
 *
 * @author matrixJoy
 * @description
 * @date 2019-11-12   10:57
 */
object Logger {

    private val isDebug = BuildConfig.DEBUG

    fun v(tag: String, msg: String) {
        if (isDebug) {
            Log.v(tag, msg)
        }
    }

    fun d(tag: String, msg: String) {
        if (isDebug) {
            Log.d(tag, msg)
        }
    }


    fun w(tag: String, msg: String) {
        if (isDebug) {
            Log.w(tag, msg)
        }
    }

    fun e(tag: String, msg: String) {
        if (isDebug) {
            Log.e(tag, msg)
        }
    }

    fun e(tag: String, msg: String, throwable: Throwable) {
        if (isDebug) {
            Log.e(tag, msg, throwable)
        }
    }

}

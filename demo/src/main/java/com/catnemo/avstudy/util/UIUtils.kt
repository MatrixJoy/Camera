package com.catnemo.avstudy.util

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import com.catnemo.avstudy.ZApplication

/**
 *
 * @author matrixJoy
 * @version V1.0
 * @since 2019/05/09
 *
 */
object UIUtils {
    private var mDisplay: Display = (ZApplication.sContext.getSystemService(Context.WINDOW_SERVICE)
            as WindowManager).defaultDisplay

    var rotation = mDisplay.rotation

    var deviceScreenWidth = 0
        private set
    var deviceScreenHeight = 0
        private set

    init {
        val point = Point()
        mDisplay.getSize(point)
        deviceScreenWidth = point.x
        deviceScreenHeight = point.y
    }

    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    fun dip2px(context: Context?, dpValue: Float): Int {
        if (context == null) {
            return 0
        }
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}
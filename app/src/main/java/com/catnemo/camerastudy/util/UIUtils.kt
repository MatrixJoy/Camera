package com.catnemo.camerastudy.util

import android.content.Context
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import com.catnemo.camerastudy.ZApplication

/**
 *
 * @author zhoujunjiang
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

}
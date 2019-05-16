package com.catnemo.camerastudy.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker
import android.widget.Toast

/**
 *权限管理器
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/03/19
 *
 */
object PermissionHelper {
    const val REQUES_CAMERA_PERMISSION = 0x1

    fun hasCameraPermission(context: Context) =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PermissionChecker.PERMISSION_GRANTED

    fun requestCameraPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.CAMERA)) {
            Toast.makeText(activity, "需要相机权限，开启预览与录制", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUES_CAMERA_PERMISSION)
        }
    }
}
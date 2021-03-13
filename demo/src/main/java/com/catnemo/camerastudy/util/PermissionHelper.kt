package com.catnemo.camerastudy.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.DialogCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker

/**
 *权限管理器
 * @author matrixJoy
 * @version V1.0
 * @since 2019/03/19
 *
 */
object PermissionHelper {
    const val REQUES_CAMERA_PERMISSION = 0x1
    const val REQUES_WRITE_EXTERNAL_STORAGE_PERMISSION = 0x2
    const val REQUES_RECORD_AUDIO_STORAGE_PERMISSION = 0x3
    const val REQUES_RECORD_PERMISSION = 0x4

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


    fun hasWriteStoragePermission(context: Context) =
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PermissionChecker.PERMISSION_GRANTED


    fun requestWriteStoragePermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val builder = AlertDialog.Builder(activity).setMessage("需要写入sdcard权限保存照片")
                    .setPositiveButton("授权") { dialog, which ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUES_WRITE_EXTERNAL_STORAGE_PERMISSION)
                    }
                    .setNegativeButton("拒绝") { dialog, which -> dialog?.dismiss() }
            builder.create().show()
        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUES_WRITE_EXTERNAL_STORAGE_PERMISSION)

        }
    }

    fun hasAudioRecordPermission(context: Context) = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED


    fun requestAudioRecordPermission(activity: Activity) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.RECORD_AUDIO)) {
            val builder = AlertDialog.Builder(activity).setMessage("需要麦克风权限，录制音频")
                    .setPositiveButton("授权") { dialog, which ->
                        dialog.dismiss()
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUES_RECORD_AUDIO_STORAGE_PERMISSION)
                    }
                    .setNegativeButton("拒绝") { dialog, which -> dialog?.dismiss() }
            builder.create().show()

        } else {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUES_RECORD_AUDIO_STORAGE_PERMISSION)
        }
    }

    fun requestAllRecordPermission(activity: Activity) {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUES_RECORD_PERMISSION)
    }
}
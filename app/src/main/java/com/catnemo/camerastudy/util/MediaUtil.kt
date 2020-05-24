package com.catnemo.camerastudy.util

import android.net.Uri
import android.os.Environment
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/16
 *
 */
object MediaUtil {
    const val MEDIA_TYPE_IMAGE = 0
    const val MEDIA_TYPE_VIDEO = 1

    fun getOutputMediaFileUri(type: Int): Uri {
        return Uri.fromFile(getOutputMediaFile(type))
    }

    fun getOutputMediaFile(type: Int): File? {
        val mediaDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ZCamera")
        if (!mediaDir.exists()) {
            if (!mediaDir.mkdirs()) {
                return null
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        return when (type) {
            MEDIA_TYPE_IMAGE -> File("${mediaDir.path}${File.separator}IMG_$timeStamp.jpg")
            MEDIA_TYPE_VIDEO -> File("${mediaDir.path}${File.separator}VID_$timeStamp.mp4")
            else -> null
        }
    }
}
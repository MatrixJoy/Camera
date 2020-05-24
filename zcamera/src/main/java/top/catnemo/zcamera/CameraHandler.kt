package top.catnemo.zcamera

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import java.lang.ref.WeakReference

/**
 * @description 操作相机的Handler
 * @author  MatrixJoy
 * @date    2019-11-07   21:02
 */
internal class CameraHandler(cameraHandlerThread: CameraHandlerThread, looper: Looper) : Handler(looper) {
    private val mWeakRef: WeakReference<CameraHandlerThread> = WeakReference(cameraHandlerThread)

    companion object {
        const val MSG_OPEN_CAMERA = 0
        const val MSG_START_PREVIEW = 1
        const val MSG_STOP_PREVIEW = 2
        const val MSG_SWITCH_CAMERA = 3
        const val MSG_OPEN_FLASH = 4
        const val MSG_DO_FOCUS = 5
        const val MSG_SET_CAMERA_ID = 6
        const val MSG_UPDATE_EXPOSURE_COMPENSATION = 7
        const val MSG_QUIT = 8
    }

    fun openCamera(width: Int, height: Int, fps: Int) {
        sendMessage(obtainMessage(MSG_OPEN_CAMERA, width, height, fps))
    }

    fun startPreview() {
        sendMessage(obtainMessage(MSG_START_PREVIEW))
    }

    fun stopPreview() {
        sendMessage(obtainMessage(MSG_STOP_PREVIEW))
    }

    fun switchCamera() {
        sendMessage(obtainMessage(MSG_SWITCH_CAMERA))
    }

    fun openFlash() {
        sendMessage(obtainMessage(MSG_OPEN_FLASH))
    }

    fun doFocus(x: Float, y: Float, width: Int, height: Int) {
        val msg = Message()
        msg.what = MSG_DO_FOCUS
        val bundle = Bundle()
        bundle.putFloat("x", x)
        bundle.putFloat("y", y)
        bundle.putInt("width", width)
        bundle.putInt("height", height)
        msg.data = bundle
        sendMessage(msg)
    }

    fun setCameraId(id: Int) {
        sendMessage(obtainMessage(MSG_SET_CAMERA_ID, id))
    }

    fun updateExposureCompensation(exposureCompensation: Int) {
        sendMessage(obtainMessage(MSG_UPDATE_EXPOSURE_COMPENSATION, exposureCompensation, 0, null))
    }

    fun quit() {
        sendMessage(obtainMessage(MSG_QUIT))
    }

    override fun handleMessage(msg: Message?) {
        val cameraThread = mWeakRef.get()
        cameraThread?.apply {
            when (msg?.what) {
                MSG_OPEN_CAMERA -> {
                    openCamera(msg.arg1, msg.arg2, msg.obj as Int)
                }
                MSG_START_PREVIEW -> {
                    startPreview()
                }
                MSG_STOP_PREVIEW -> {
                    stopPreview()
                }
                MSG_SWITCH_CAMERA -> {
                    switchCamera()
                }
                MSG_OPEN_FLASH -> {
                    openFlash()
                }
                MSG_DO_FOCUS -> {
                    doFocus(msg.data.getFloat("x"), msg.data.getFloat("y"), msg.data.getInt("width"), msg.data.getInt("height"))
                }
                MSG_SET_CAMERA_ID -> {
                    setCameraId(msg.obj as Int)
                }
                MSG_UPDATE_EXPOSURE_COMPENSATION -> {
                    updateExposureCompensation(msg.arg1)
                }
                MSG_QUIT -> {
                    quitSafely()
                }
            }
        }
    }
}
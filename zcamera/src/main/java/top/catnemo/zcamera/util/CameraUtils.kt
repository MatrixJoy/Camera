package top.catnemo.zcamera.util

import android.graphics.Rect
import android.hardware.Camera
import android.util.Log
import android.view.Surface
import top.catnemo.zcamera.Constant

/**
 *
 * @author matrixJoy
 * @version V1.0
 * @since 2019/03/18
 *
 */
object CameraUtils {

    private const val TAG = Constant.TAG

    /**
     * open mCamera
     */
    fun openCamera(): Camera? {
        return openCameraById(Camera.CameraInfo.CAMERA_FACING_FRONT)
    }

    fun openCameraById(id: Int): Camera? {
        val cameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0..cameras) {
            try {
                Camera.getCameraInfo(i, cameraInfo)
                if (cameraInfo.facing == id) {
                    return Camera.open(id)
                }
            } catch (e: Exception) {

            }

        }
        return null

    }

    fun choosePreviewSize(params: Camera.Parameters, width: Int, height: Int) {
        val ppsfv = params.preferredPreviewSizeForVideo
        if (ppsfv != null) {
            Log.d(TAG, "Camera preferred preview size for video is ${ppsfv.width}x${ppsfv.height} ")
        }
        for (size in params.supportedPreviewSizes) {
            if (size.width == width && size.height == height) {
                params.setPreviewSize(width, height)
                return
            }
        }
        Log.w(TAG, "Unable to set preview size to ${width}x$height")
        ppsfv?.let {
            params.setPreviewSize(it.width, it.height)
        }
    }

    fun chooseFixedPreviewFps(parameters: Camera.Parameters, desiredThousandFps: Int): Int {
        val supported = parameters.supportedPreviewFpsRange
        for (entry in supported) {
            if ((entry[0] == entry[1]) && (entry[1] == desiredThousandFps)) {
                parameters.setPreviewFpsRange(entry[0], entry[1])
                return entry[0]
            }
        }
        val temp = IntArray(2)
        parameters.getPreviewFpsRange(temp)
        return if (temp[0] == temp[1]) {
            temp[0]
        } else {
            temp[0] / 2
        }
    }

    fun chooseRightRotation(cameraId: Int, deviceRotation: Int): Int {
        var degrees = 0
        when (deviceRotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }
        var result = 0
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        if (cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360
        } else {
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }


    /**
     * 计算点击区域，转换成相机坐标
     */
    fun calculateTapArea(x: Float, y: Float, coefficient: Float, width: Int, height: Int): Rect {
        val fAreaSize = (300 * coefficient).toInt()
        val centerX = (x / width * 2000 - 1000).toInt()
        val centerY = (y / height * 2000 - 1000).toInt()
        val left = clamp(centerX - fAreaSize / 2, -1000, 1000)
        val top = clamp(centerY - fAreaSize / 2, -1000, 1000)
        val right = clamp(centerX + fAreaSize / 2, -1000, 1000)
        val bottom = clamp(centerY + fAreaSize / 2, -1000, 1000)
        val rect = Rect(left, top, right, bottom)
        return rect
    }

    private fun clamp(x: Int, min: Int, max: Int): Int {
        if (x > max) {
            return max
        }
        if (x < min) {
            return min
        }
        return x
    }

}
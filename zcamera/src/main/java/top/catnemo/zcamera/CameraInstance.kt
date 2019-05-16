package top.catnemo.zcamera

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.Display
import top.catnemo.zcamera.interfazz.OnCameraOperateCallback
import top.catnemo.zcamera.util.CameraUtils
import java.io.IOException
import java.lang.RuntimeException

/**
 * 相机实例，负责实例化相机
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/09
 *
 */
internal object CameraInstance : Camera.AutoFocusCallback {

    var mCamera: Camera? = null
        private set

    private const val TAG = Constant.TAG
    var mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
    var mPreViewWidth = 0
        private set
    var mPreViewHeight = 0
        private set
    var mPreViewFps = 0
        private set

    val isFront: Boolean
        get() = this.mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT

    var mRotation = 0
        private set
    var mDisplay: Display? = null

    var mDesiredWidth = 0
        private set
    var mDesiredHeight = 0
        private set
    var mDesiredFps = 0
        private set
    var sMCameraOperateCallback: OnCameraOperateCallback? = null

    private fun openCamera(id: Int): Camera? {
        val cameras = Camera.getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0..cameras) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == id) {
                return try {
                    mRotation = cameraInfo.orientation
                    Camera.open(id)
                } catch (e: RuntimeException) {
                    Log.e(TAG, "Open mCamera fail")
                    null
                }
            }
        }
        return null
    }

    fun openCamera(desiredWidth: Int, desiredHeight: Int, desiredFps: Int) {
        releaseCamera()
        if (mCamera != null) {
            Log.w(TAG, "do not open camera again")
            return
        }
        mCamera = openCamera(mCameraId)
        if (mCamera == null) {
            Log.e(TAG, "OPEN Camera Failed")
            sMCameraOperateCallback?.onOpenCameraFail()
            return
        }

        mDesiredWidth = desiredWidth
        mDesiredHeight = desiredHeight
        mDesiredFps = desiredFps

        mCamera?.apply {
            val params = parameters
            params.setRecordingHint(true)
            CameraUtils.choosePreviewSize(params, desiredWidth, desiredHeight)
            mPreViewWidth = params.previewSize.width
            mPreViewHeight = params.previewSize.height
            mPreViewFps = CameraUtils.chooseFixedPreviewFps(params, desiredFps * 1000) / 1000
            Log.d(TAG, "$mPreViewWidth x $mPreViewHeight @${mPreViewFps}fps")
            if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            }
            parameters = params
        }
        setRotation(mDisplay?.rotation!!)
        sMCameraOperateCallback?.onOpenCameraSuccess(mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK)
    }

    private fun setRotation(deviceRotation: Int) {
        if (mCamera == null) {
            return
        }
        mCamera?.setDisplayOrientation(CameraUtils.chooseRightRotation(mCameraId, deviceRotation))
    }

    fun doFocus(x: Float, y: Float, deviceScreenWidth: Int, deviceScreenHeight: Int) {
        val focusRect = CameraUtils.calculateTapArea(x, y, 1.0f,
                deviceScreenWidth, deviceScreenHeight)
        val meteringRect = CameraUtils.calculateTapArea(x, y, 1.5f,
                deviceScreenWidth, deviceScreenHeight)
        mCamera?.cancelAutoFocus()
        val parameters = mCamera?.parameters
        parameters?.apply {
            if (maxNumFocusAreas > 0) {
                focusAreas = arrayListOf(Camera.Area(focusRect, 1000))
            }

            if (maxNumMeteringAreas > 0) {
                meteringAreas = arrayListOf(Camera.Area(meteringRect, 800))
            }

            focusMode = Camera.Parameters.FOCUS_MODE_MACRO

            try {
                mCamera?.parameters = this
            } catch (e: Exception) {

            }
        }
        mCamera?.autoFocus(this)
    }

    override fun onAutoFocus(success: Boolean, camera: Camera?) {
        camera?.parameters?.apply {
            focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            try {
                camera.parameters = this
            } catch (e: Exception) {
            }
        }
        sMCameraOperateCallback?.onFocusDone(success)
    }

    fun startPreview(st: SurfaceTexture?) {
        if (mCamera == null || st == null) {
            Log.w(TAG, "can not  start preview Camera $mCamera,SurfaceTexture $st")
            sMCameraOperateCallback?.onStartPreviewFail("can not  start preview Camera $mCamera,SurfaceTexture $st")
            return
        }
        mCamera?.setPreviewTexture(st)
        mCamera?.startPreview()
        sMCameraOperateCallback?.onStartPreviewSuccess()
    }

    fun switchCamera() {
        if (mCamera == null) {
            Log.w(TAG, "Camera still not opened")
            return
        }
//        releaseCamera()
        mCameraId = if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            Camera.CameraInfo.CAMERA_FACING_BACK
        } else {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        }
    }

    fun openFlash(context: Context) {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.w(TAG, "current device not flash")
            sMCameraOperateCallback?.onOpenFlashFail("current device not flash")
            return
        }
        val params: Camera.Parameters? = mCamera?.parameters
        params?.apply {
            if (flashMode == Camera.Parameters.FLASH_MODE_OFF) {
                flashMode = Camera.Parameters.FLASH_MODE_TORCH
            } else if (flashMode == Camera.Parameters.FLASH_MODE_TORCH) {
                flashMode = Camera.Parameters.FLASH_MODE_OFF
            }
            mCamera?.parameters = this
        }
    }

    fun releaseCamera() {
        mCamera?.apply {
            stopPreview()
            try {
                setPreviewCallback(null)
                setPreviewTexture(null)
            } catch (e: IOException) {

            }
            release()
            mCamera = null
        }
    }
}

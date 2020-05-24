package top.catnemo.zcamera

import android.os.HandlerThread
import top.catnemo.zcamera.interfazz.OnCameraOperateCallback
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * @description CameraHandlerThread
 * @author  MatrixJoy
 * @date    2019-11-07   20:49
 */
internal class CameraHandlerThread(cameraview: ZCameraView) : HandlerThread("ZCameraThread"), OnCameraOperateCallback {

    private val mWeakRef: WeakReference<ZCameraView> = WeakReference(cameraview)
    private val mCameraView = mWeakRef.get()

    init {
        CameraInstance.mDisplay = mCameraView?.mDisplay
        CameraInstance.sMCameraOperateCallback = this
    }


    fun startPreview() {
        mCameraView?.apply {
            setRotation(CameraInstance.mRotation)
        }
        CameraInstance.startPreview(mCameraView?.mSurfaceTexture)
    }


    fun stopPreview() {
        CameraInstance.releaseCamera()
    }

    fun switchCamera() {
        CameraInstance.switchCamera()
        openCamera(CameraInstance.mDesiredWidth, CameraInstance.mDesiredHeight, CameraInstance.mDesiredFps)
    }

    fun openCamera(width: Int, height: Int, fps: Int) {
        CameraInstance.openCamera(width, height, fps)
    }

    fun openFlash() {
        mCameraView?.apply {
            CameraInstance.openFlash(context)
        }
    }

    fun doFocus(x: Float, y: Float, width: Int, height: Int) {
        mCameraView?.apply {
            CameraInstance.doFocus(x, y, width, height)
        }
    }

    fun setCameraId(id: Int) {
        CameraInstance.mCameraId = id
    }

    /**
     * 0-100
     */
    fun updateExposureCompensation(exposureCompensation: Int) {
        CameraInstance.updateExposureCompensation(exposureCompensation)
    }

    override fun onOpenCameraSuccess(flashIsEnable: Boolean) {
        try {
            CameraInstance.mCamera?.setPreviewCallback { data, _ ->
                mCameraView?.handlePreviewCallBackData(data)
            }
        } catch (e: Exception) {

        }
        mCameraView?.isFront = CameraInstance.isFront
        mCameraView?.handleOpenCameraSuccess(flashIsEnable)
    }

    override fun onOpenCameraFail() {
        mCameraView?.handleOpenCameraFail()
    }

    override fun onOpenFlashFail(msg: String) {
        mCameraView?.handleOpenFlashFail(msg)
    }

    override fun onStartPreviewSuccess() {
        mCameraView?.handleStartPreviewSuccess()
    }

    override fun onStartPreviewFail(msg: String) {
        mCameraView?.handleStartPreviewFail(msg)
    }

    override fun onFocusDone(success: Boolean?) {
        mCameraView?.handleFocusDone(success)
    }
}
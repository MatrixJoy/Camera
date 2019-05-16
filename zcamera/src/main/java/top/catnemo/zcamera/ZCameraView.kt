package top.catnemo.zcamera

import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.*
import android.util.AttributeSet
import android.view.Display
import android.view.WindowManager
import top.catnemo.zcamera.interfazz.ICapturePhoto
import top.catnemo.zcamera.interfazz.IZCameraDrawer
import top.catnemo.zcamera.interfazz.IZCamera
import top.catnemo.zcamera.interfazz.OnCameraOperateCallback
import top.catnemo.zcamera.render.ZCameraRender
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 *  基于OpenGL ES 3 的相机预览view
 *  逻辑控制可以交由业务层自定义UI
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/10
 *
 */
class ZCameraView : GLSurfaceView, IZCamera, ZCameraRender.OnSurfaceTextureCreatedListener, SurfaceTexture.OnFrameAvailableListener {

    private lateinit var mRenderer: ZCameraRender
    private var mCameraHandler: CameraHandler? = null
    private lateinit var mDisplay: Display

    private val DEFUALT__PREVIEW_WIDTH = 1080
    private val DEFUALT__PREVIEW_HEIGHT = 720
    private val DEFUALT__PREVIEW_FPS = 30
    private val mDeviceSizePoint: Point = Point()
    private var mSurfaceTexture: SurfaceTexture? = null
    var isFront = false
        private set

    var mCameraOperateCallback: OnCameraOperateCallback? = null


    constructor(context: Context?) : this(context, null)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setEGLContextClientVersion(3)
        context?.apply {

            mDisplay = (getSystemService(Context.WINDOW_SERVICE)
                    as WindowManager).defaultDisplay
            mDisplay.getSize(mDeviceSizePoint)
        }
        initRender()

    }

    private fun initRender() {
        mRenderer = ZCameraRender()
        setRenderer(mRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        mRenderer.mOnSurfaceTextureCreatedListener = this
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onPause() {
        releaseCamera()
        queueEvent {
            mRenderer.release()
        }
        super.onPause()
    }


    override fun setPreviewParams(previewWidth: Int, previewHeight: Int, previewFps: Int) {
        // todo 传参
    }


    override fun switchCamera() {
        queueEvent {
            mRenderer.mWaitDraw = true
        }
        mCameraHandler?.switchCamera()
        queueEvent {
            mRenderer.mWaitDraw = false
        }
    }

    override fun releaseCamera() {
        mCameraHandler?.stopPreview()
    }

    override fun setCameraId(cameraId: Int) {
        mCameraHandler?.setCameraId(cameraId)
    }

    override fun openFlash() {
        mCameraHandler?.openFlash()
    }

    override fun doFocus(x: Float, y: Float) {
        mCameraHandler?.doFocus(x, y)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    override fun onSurfaceTextureCreated(st: SurfaceTexture?) {
        mSurfaceTexture = st
        st?.setOnFrameAvailableListener(this)
        startPreview()
    }

    private fun startPreview() {
        if (mCameraHandler == null) {
            val cameraHandlerThread = CameraHandlerThread(this)
            cameraHandlerThread.start()
            mCameraHandler = CameraHandler(cameraHandlerThread, cameraHandlerThread.looper)
        }
        mCameraHandler?.startPreview(DEFUALT__PREVIEW_WIDTH, DEFUALT__PREVIEW_HEIGHT, DEFUALT__PREVIEW_FPS)
    }

    override fun setIDrawer(iDrawer: IZCameraDrawer?) {
        queueEvent {
            mRenderer.mIDrawer = iDrawer
        }
    }

    override fun takePicture(iCapturePhoto: ICapturePhoto) {
        queueEvent {
            mRenderer.captureFrame(object : ICapturePhoto {
                override fun onCapturePhoto(byteArray: ByteArray) {
                    post {
                        iCapturePhoto.onCapturePhoto(byteArray)
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        mCameraHandler?.quit()
        mCameraHandler?.removeCallbacksAndMessages(null)
        mCameraHandler = null
    }

    private fun handlePreviewCallBackData(data: ByteArray) {
    }

    private fun handleOpenCameraSuccess(flashIsEnable: Boolean) {
        post {
            mCameraOperateCallback?.onOpenCameraSuccess(flashIsEnable)
        }
    }

    private fun handleOpenCameraFail() {
        post {
            mCameraOperateCallback?.onOpenCameraFail()
        }
    }

    private fun handleOpenFlashFail(msg: String) {
        post {
            mCameraOperateCallback?.onOpenFlashFail(msg)
        }
    }

    private fun handleStartPreviewSuccess() {
        post {
            mCameraOperateCallback?.onStartPreviewSuccess()
        }
    }

    private fun handleStartPreviewFail(msg: String) {
        post {
            mCameraOperateCallback?.onStartPreviewFail(msg)
        }
    }

    private fun handleFocusDone(success: Boolean?) {
        post {
            mCameraOperateCallback?.onFocusDone(success)
        }
    }

    private fun setRotation(cameraOration: Int) {
        queueEvent {
            mRenderer.mRotation = cameraOration.toFloat()
        }
    }

    private class CameraHandler(cameraHandlerThread: CameraHandlerThread, looper: Looper) : Handler(looper) {
        private val mWeakRef: WeakReference<CameraHandlerThread> = WeakReference(cameraHandlerThread)

        companion object {
            const val MSG_START_PREVIEW = 1
            const val MSG_STOP_PREVIEW = 2
            const val MSG_SWITCH_CAMERA = 3
            const val MSG_OPEN_FLASH = 4
            const val MSG_DO_FOCUS = 5
            const val MSG_SET_CAMERA_ID = 6
            const val MSG_QUIT = 7
        }

        fun startPreview(width: Int, height: Int, fps: Int) {
            sendMessage(obtainMessage(MSG_START_PREVIEW, width, height, fps))
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

        fun doFocus(x: Float, y: Float) {
            val msg = Message()
            msg.what = MSG_DO_FOCUS
            val bundle = Bundle()
            bundle.putFloat("x", x)
            bundle.putFloat("y", y)
            msg.data = bundle
            sendMessage(msg)
        }

        fun setCameraId(id: Int) {
            sendMessage(obtainMessage(MSG_SET_CAMERA_ID, id))
        }

        fun quit() {
            sendMessage(obtainMessage(MSG_QUIT))
        }

        override fun handleMessage(msg: Message?) {
            val cameraThread = mWeakRef.get()
            cameraThread?.apply {
                when (msg?.what) {
                    MSG_START_PREVIEW -> {
                        startPreview(msg.arg1, msg.arg2, msg.obj as Int)
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
                        doFocus(msg.data.getFloat("x"), msg.data.getFloat("y"))
                    }
                    MSG_SET_CAMERA_ID -> {
                        setCameraId(msg.obj as Int)
                    }
                    MSG_QUIT -> {
                        quitSafely()
                    }
                }
            }
        }
    }

    private class CameraHandlerThread(cameraview: ZCameraView) : HandlerThread("ZCameraThread"), OnCameraOperateCallback {

        private val mWeakRef: WeakReference<ZCameraView> = WeakReference(cameraview)
        private val mCameraView = mWeakRef.get()

        init {
            CameraInstance.mDisplay = mCameraView?.mDisplay
            CameraInstance.sMCameraOperateCallback = this
        }

        fun startPreview(width: Int, height: Int, fps: Int) {
            openCamera(width, height, fps)
        }


        fun stopPreview() {
            CameraInstance.releaseCamera()
        }

        fun switchCamera() {
            CameraInstance.switchCamera()
            openCamera(CameraInstance.mDesiredWidth, CameraInstance.mDesiredHeight, CameraInstance.mDesiredFps)
        }

        private fun openCamera(width: Int, height: Int, fps: Int) {
            CameraInstance.openCamera(width, height, fps)
            CameraInstance.startPreview(mCameraView?.mSurfaceTexture)
            mCameraView?.apply {
                setRotation(CameraInstance.mRotation)
            }
        }

        fun openFlash() {
            mCameraView?.apply {
                CameraInstance.openFlash(context)
            }
        }

        fun doFocus(x: Float, y: Float) {
            mCameraView?.apply {
                CameraInstance.doFocus(x, y, mDeviceSizePoint.x, mDeviceSizePoint.y)
            }
        }

        fun setCameraId(id: Int) {
            CameraInstance.mCameraId = id
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
}
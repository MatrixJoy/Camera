package top.catnemo.zcamera

import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Display
import android.view.WindowManager
import top.catnemo.zcamera.interfazz.*
import top.catnemo.zcamera.record.IRecorder
import top.catnemo.zcamera.record.ZCameraRecorder
import top.catnemo.zcamera.render.ZCameraRender
import java.nio.ByteBuffer

/**
 * 基于OpenGL ES 3 的相机预览view 逻辑控制可以交由业务层自定义UI
 *
 * @author MatrixJoy
 * @since 2019/05/10
 *
 */
class ZCameraView : GLSurfaceView, IZCamera, ZCameraRender.OnSurfaceTextureCreatedListener, SurfaceTexture.OnFrameAvailableListener {
    private lateinit var mRenderer: ZCameraRender

    private var mCameraHandler: CameraHandler? = null
    internal lateinit var mDisplay: Display

    private val mDeviceSizePoint: Point = Point()
    var mSurfaceTexture: SurfaceTexture? = null
        private set
    var isFront = false
        internal set
    var mCameraOperateCallback: OnCameraOperateCallback? = null

    private var mZCameraRecorder: IRecorder? = null

    constructor(context: Context?) : this(context, null)


    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {

        setEGLContextClientVersion(3)
        context?.apply {
            mDisplay = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
            mDisplay.getSize(mDeviceSizePoint)
        }
        initRender()

    }

    private fun initRender() {
        mRenderer = ZCameraRender()
        mZCameraRecorder = ZCameraRecorder()
        mRenderer.mIRecorder = mZCameraRecorder
        setRenderer(mRenderer)
        renderMode = RENDERMODE_WHEN_DIRTY
        mRenderer.mOnSurfaceTextureCreatedListener = this
    }

    override fun onPause() {
        releaseCamera()
        queueEvent {
            mRenderer.release()
        }
        super.onPause()
    }


    override fun openCamera(previewWidth: Int, previewHeight: Int, previewFps: Int) {
        if (mCameraHandler == null) {
            val cameraHandlerThread = CameraHandlerThread(this)
            cameraHandlerThread.start()
            mCameraHandler = CameraHandler(cameraHandlerThread, cameraHandlerThread.looper)
        }
        mCameraHandler?.openCamera(previewWidth, previewHeight, previewFps)
    }

    override fun openCamera() {
        openCamera(Constant.DEFAULT__PREVIEW_WIDTH, Constant.DEFAULT__PREVIEW_HEIGHT, Constant.DEFAULT__PREVIEW_FPS)
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
        mCameraHandler?.doFocus(x, y, mDeviceSizePoint.x, mDeviceSizePoint.y)
    }

    override fun updateExposureCompensation(exposureCompensation: Int) {
        mCameraHandler?.updateExposureCompensation(exposureCompensation)
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        requestRender()
    }

    override fun onSurfaceTextureCreated(st: SurfaceTexture?) {
        mSurfaceTexture = st
        st?.setOnFrameAvailableListener(this)
    }

    override fun startPreview() {
        mCameraHandler?.startPreview()
    }

    override fun setIDrawer(iDrawer: IZCameraDrawer?) {
        queueEvent {
            mRenderer.mIDrawer = iDrawer
        }
    }

    override fun takePicture(iCapturePhoto: ICapturePhoto) {
        queueEvent {
            mRenderer.captureFrame(object : ICapturePhoto {
                override fun onCapturePhoto(buffer: ByteBuffer) {
                    post {
                        iCapturePhoto.onCapturePhoto(buffer)
                    }
                }
            })
        }
    }

    override fun startRecord(iRecodingCallback: IRecodingCallback) {
        queueEvent {
            mZCameraRecorder?.setRecodingCallback(iRecodingCallback)
            mZCameraRecorder?.startRecording(EGL14.eglGetCurrentContext())
        }
    }

    override fun stopRecord() {
        queueEvent {
            mZCameraRecorder?.stopRecording()
        }
    }

    override fun onDestroy() {
        mCameraHandler?.quit()
        mCameraHandler?.removeCallbacksAndMessages(null)
        mCameraHandler = null
    }

    internal fun handlePreviewCallBackData(data: ByteArray) {
        // TODO 相机的nav21数据做处理
    }

    internal fun handleOpenCameraSuccess(flashIsEnable: Boolean) {
        post {
            mCameraOperateCallback?.onOpenCameraSuccess(flashIsEnable)
        }
    }

    internal fun handleOpenCameraFail() {
        post {
            mCameraOperateCallback?.onOpenCameraFail()
        }
    }

    internal fun handleOpenFlashFail(msg: String) {
        post {
            mCameraOperateCallback?.onOpenFlashFail(msg)
        }
    }

    internal fun handleStartPreviewSuccess() {
        post {
            mCameraOperateCallback?.onStartPreviewSuccess()
        }
    }

    internal fun handleStartPreviewFail(msg: String) {
        post {
            mCameraOperateCallback?.onStartPreviewFail(msg)
        }
    }

    internal fun handleFocusDone(success: Boolean?) {
        post {
            mCameraOperateCallback?.onFocusDone(success)
        }
    }

    internal fun setRotation(cameraOration: Int) {
        queueEvent {
            mRenderer.mRotation = cameraOration.toFloat()
        }
    }
}
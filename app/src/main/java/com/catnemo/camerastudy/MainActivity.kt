package com.catnemo.camerastudy

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES30
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.catnemo.camerastudy.glesutil.FullFrameRect
import com.catnemo.camerastudy.util.PermissionHelper
import kotlinx.android.synthetic.main.activity_main.*
import top.catnemo.zcamera.interfazz.ICapturePhoto
import top.catnemo.zcamera.interfazz.IZCameraDrawer
import top.catnemo.zcamera.interfazz.OnCameraOperateCallback
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity(), OnCameraOperateCallback, IZCameraDrawer, Camera.PreviewCallback {

    // todo 开发特效类 获取保存媒体文件地址
    companion object {
        const val TAG = "ZCamera"
    }

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        z_camera_view.mCameraOperateCallback = this
        z_camera_view.setIDrawer(this)
        btn_switch_camera.setOnClickListener {
            z_camera_view.switchCamera()
        }
        btn_flash.setOnClickListener {
            z_camera_view.openFlash()
        }

        gl_surface_view_container.setOnTouchListener { v, event ->
            doFocus(event)
            return@setOnTouchListener true
        }

        beauty.setOnClickListener {

        }

        btn_record.setOnClickListener {
            //            startRecord()
            z_camera_view.takePicture(object : ICapturePhoto {
                override fun onCapturePhoto(byteArray: ByteArray) {
                    val buf = ByteBuffer.allocateDirect(byteArray.size).order(ByteOrder.LITTLE_ENDIAN)
                    buf.put(byteArray)
                    buf.position(0)
                    val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                    bitmap.copyPixelsFromBuffer(buf)
                    val mtx = Matrix()
                    mtx.postScale(-1f, 1f)
                    mtx.postRotate(180f)
                    MediaStore.Images.Media.insertImage(contentResolver, Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mtx, false), "Zcamera", "zjj")
                }

            })
        }
    }

//    private fun startRecord() {
//        gl_surface_view.queueEvent {
//            if (mEncoder.isRecording) {
//                runOnUiThread {
//                    btn_record.text = "录制"
//                }
//                mEncoder.stopRecording()
//            } else {
//                val outfile = File("sdcard/${System.currentTimeMillis()}.mp4")
//                mEncoder.startRecording(TextureMovieEncoder.EncoderConfig(outfile, CameraInstance.mPreViewWidth, CameraInstance.mPreViewHeight, 1000000, EGL14.eglGetCurrentContext()))
//                runOnUiThread {
//                    btn_record.text = "停止"
//                }
//            }
//            mRender.isRecoding = mEncoder.isRecording
//        }
//    }

    /**
     * 聚焦
     */
    private fun doFocus(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                showFocusView(event.x, event.y)
                z_camera_view.doFocus(event.x, event.y)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (PermissionHelper.hasCameraPermission(this)) {
            z_camera_view.onResume()
        } else {
            PermissionHelper.requestCameraPermission(this)
        }
    }

    override fun onPause() {
        super.onPause()
        z_camera_view.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        z_camera_view.onDestroy()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!PermissionHelper.hasCameraPermission(this)) {
            Toast.makeText(this, "需要相机权限，开启应用", Toast.LENGTH_SHORT).show()
        } else {
            z_camera_view.onResume()
        }
    }

    private var focusView: ImageView? = null

    @SuppressLint("ObjectAnimatorBinding")
    private fun showFocusView(x: Float, y: Float) {
        if (focusView == null) {
            focusView = ImageView(this)
            gl_surface_view_container.addView(focusView, 100, 100)
        }
        focusView?.setImageResource(R.drawable.icon_focus_start)
        focusView?.x = x
        focusView?.y = y
        focusView?.visibility = View.VISIBLE
        val scaleXAni = ObjectAnimator.ofFloat(focusView, "scaleX", 1.5f, 1f)
        val scaleYAni = ObjectAnimator.ofFloat(focusView, "scaleY", 1.5f, 1f)
        val animatorSet = AnimatorSet()
        animatorSet.play(scaleXAni).with(scaleYAni)
        animatorSet.startDelay = 300
        animatorSet.start()
    }

    override fun onOpenCameraSuccess(flashIsEnable: Boolean) {
        btn_flash.isEnabled = flashIsEnable
    }

    override fun onPreviewFrame(nv21Byte: ByteArray?, camera: Camera?) {
        // todo 拿到 nv21 数据去识别人像
    }

    override fun onOpenCameraFail() {
        // todo fix open fail
    }

    override fun onOpenFlashFail(msg: String) {
        // todo fix open fail
    }

    override fun onStartPreviewSuccess() {

    }

    override fun onStartPreviewFail(msg: String) {
        // todo fix open fail
    }

    override fun onFocusDone(success: Boolean?) {
        focusView?.visibility = View.GONE
    }

    private var mWidth = 0
    private var mHeight = 0
    override fun onDrawerInit(width: Int, height: Int) {
        mWidth = width
        mHeight = height
    }

    override fun afterDrawFrame(texture: SurfaceTexture?, textureId: Int) {
        // todo 处理 处理后的 texture id 可以用来编码
    }

    private val mFbos = IntArray(1)
    private var mFb = 0
    private var mTextureID = 0
    private var mFullFrameRect: FullFrameRect? = null
    private val mStMtx = FloatArray(16)
    override fun beforeOnDrawFrame(texture: SurfaceTexture?, textureId: Int): Int {
//        if (mFb == 0) {
//            GLES30.glGenFramebuffers(1, mFbos, 0)
//            mFullFrameRect = FullFrameRect(TwoScreenProgram())
//            mTextureID = mFullFrameRect?.createOESTextureObject()!!
//            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, mWidth, mHeight, 0, GLES30.GL_RGBA,
//                    GLES30.GL_UNSIGNED_BYTE, null)
//            mFb = mFbos[0]
//        }
//        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFb)
//        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, mTextureID, 0)
//        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
//        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
//        GLES30.glViewport(0, 0, mWidth, mHeight)
//        texture?.getTransformMatrix(mStMtx)
//        mFullFrameRect?.drawFrame(textureId, mStMtx)
//        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
//        return mTextureID
        return textureId
    }

    override fun onDrawerRelease() {
        if (mFb != 0) {
            GLES30.glDeleteFramebuffers(1, mFbos, 0)
            mFb = 0
        }
        if (mTextureID != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(mTextureID), 0)
            mTextureID = 0
        }
    }
}

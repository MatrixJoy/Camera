package com.catnemo.camerastudy

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.catnemo.camerastudy.util.MediaUtil
import com.catnemo.camerastudy.util.PermissionHelper
import com.catnemo.zfilter.FilterDrawer
import com.catnemo.zfilter.TextureInfo
import com.catnemo.zfilter.filter.BlurScreenFilter
import kotlinx.android.synthetic.main.activity_main.*
import top.catnemo.zcamera.RecordConfig
import top.catnemo.zcamera.interfazz.ICapturePhoto
import top.catnemo.zcamera.interfazz.IRecodingCallback
import top.catnemo.zcamera.interfazz.IZCameraDrawer
import top.catnemo.zcamera.interfazz.OnCameraOperateCallback
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.*

class MainActivity : AppCompatActivity(), OnCameraOperateCallback, IZCameraDrawer {

    companion object {
        const val TAG = "ZCamera"
    }

    private var record = false

    private var speed = 1.0f

    private val list = ArrayList<String>()
    private var currentEffec: Effect? = null
    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_main)
        z_camera_view.mCameraOperateCallback = this
        z_camera_view.setIDrawer(this)

        view_effect.init(object : EffectAdapter.OnEffectClick {
            override fun onEffectClick(effect: Effect) {
                view_effect.visibility = View.INVISIBLE
                z_camera_view.queueEvent {
                    if (effect.name == "NONE") {
                        currentEffec = null
                        FilterDrawer.sInstance?.updateEffectFilter(null)
                        return@queueEvent
                    }
                    currentEffec = effect
                    FilterDrawer.sInstance?.updateEffectFilter(effect.filter)
                }
            }
        })

        view_filter.init(object : FilterAdapter.OnFilterClick {
            override fun onFilterClick(effect: Filter) {
                view_filter.visibility = View.INVISIBLE
                z_camera_view.queueEvent {
                    FilterDrawer.sInstance?.updateLutFilter(effect.lutPath)
                }
            }
        })

        view_filter.onFilterProgressChanged = object : FilterView.OnFilterProgressChanged {
            override fun onProgess(process: Float) {
                FilterDrawer.sInstance?.updateLutProgess(process)
            }
        }

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
            if (view_effect.visibility == View.INVISIBLE) {
                view_effect.visibility = View.VISIBLE
                view_filter.visibility = View.INVISIBLE
            } else {
                view_effect.visibility = View.INVISIBLE
            }
        }

        filter.setOnClickListener {
            if (view_filter.visibility == View.INVISIBLE) {
                view_filter.visibility = View.VISIBLE
                view_effect.visibility = View.INVISIBLE
            } else {
                view_filter.visibility = View.INVISIBLE
            }
        }

        btn_record.setOnClickListener {
            if (record) {
                z_camera_view.stopRecord()
            } else {
                if (!PermissionHelper.hasWriteStoragePermission(this)) {
                    PermissionHelper.requestWriteStoragePermission(this)
                    return@setOnClickListener
                }
                if (!PermissionHelper.hasAudioRecordPermission(this)) {
                    PermissionHelper.requestAudioRecordPermission(this)
                    return@setOnClickListener
                }
                startRecording()
            }
        }
        btn_capture.setOnClickListener { takePicture() }

        btn_speed.setOnClickListener {
            if (speed_layout.visibility == View.GONE) {
                speed_layout.visibility = View.VISIBLE
            } else {
                speed_layout.visibility = View.GONE
            }
        }
        speed_layout.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.normal -> {
                    speed = 1.0f
                }
                R.id.slow -> {
                    speed = 0.8f
                }
                R.id.slowly -> {
                    speed = 0.4f
                }
                R.id.fast -> {
                    speed = 1.5f
                }
                R.id.fastly -> {
                    speed = 2.0f
                }
            }
        }

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    z_camera_view.updateExposureCompensation(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
    }

    private fun startRecording() {
        if (speed_layout.visibility == View.VISIBLE) {
            speed_layout.visibility = View.GONE
        }
        val outputFile = MediaUtil.getOutputMediaFile(MediaUtil.MEDIA_TYPE_VIDEO)
        outputFile?.let { file ->
            z_camera_view.startRecord(object : IRecodingCallback {
                override fun onRecordingStart() {
                    runOnUiThread {
                        btn_record.text = "录制中"
                        record = true
                    }

                }

                override fun onRecordingStop(outputPath: String) {
                    runOnUiThread {
                        record = false
                        btn_record.text = "录制"
                        Log.d("zjj", "out $outputPath")
                        list.add(outputPath)
                    }
                }

                override val recordConfig: RecordConfig
                    get() {
                        val config = RecordConfig(720, 1280, file.path)
                        config.isMute = false
                        config.speed = speed
                        Log.d("zjj", config.toString())
                        return config
                    }


                override fun onRecodingError(errorMsg: String) {

                }
            })
        }
    }

    private fun takePicture() {
        z_camera_view.takePicture(object : ICapturePhoto {
            override fun onCapturePhoto(buffer: ByteBuffer) {
                val bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888)
                bitmap.copyPixelsFromBuffer(buffer)
                buffer.rewind()
                val mtx = Matrix()
                mtx.postScale(-1f, 1f)
                mtx.postRotate(180f)
                try {
                    val fos = FileOutputStream(MediaUtil.getOutputMediaFile(MediaUtil.MEDIA_TYPE_IMAGE))
                    val newBitMap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, mtx, false)
                    newBitMap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.close()
                    newBitMap.recycle()
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    bitmap.recycle()
                }
            }
        })
    }

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
        z_camera_view.onResume()
        if (PermissionHelper.hasCameraPermission(this)) {
            z_camera_view?.openCamera()
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
        if (requestCode == PermissionHelper.REQUES_CAMERA_PERMISSION) {
            if (PermissionHelper.hasCameraPermission(this)) {
                z_camera_view.openCamera()
            }
        } else {
            if (PermissionHelper.hasAudioRecordPermission(this) && PermissionHelper.hasWriteStoragePermission(this)) {
                startRecording()
            }
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
        z_camera_view.startPreview()
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
        FilterDrawer.sInstance?.renderSize(width, height)
    }

    override fun afterDrawFrame(texture: SurfaceTexture?, textureId: Int) {
        // todo 处理 处理后的 texture id 可以用来编码
    }


    private val mStMtx = FloatArray(16)

    override fun beforeOnDrawFrame(texture: SurfaceTexture?, textureId: Int): Int {
        texture?.getTransformMatrix(mStMtx)
        val textureInfo = TextureInfo(textureId)
        textureInfo.textMatrix = mStMtx
        val result = FilterDrawer.sInstance?.process(textureInfo)
        result?.let {
            return it
        }
        return textureId
    }

    override fun onDrawerRelease() {
        FilterDrawer.sInstance?.destroy()
    }
}

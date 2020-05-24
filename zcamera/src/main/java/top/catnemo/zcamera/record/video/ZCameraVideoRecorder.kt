package top.catnemo.zcamera.record.video

import android.opengl.EGLContext
import android.os.Handler
import android.os.Looper
import android.os.Message
import top.catnemo.zcamera.frame.ZCameraFrame
import top.catnemo.zcamera.render.ZCameraDrawer
import java.lang.ref.WeakReference


/**
 * @description 录制实现类
 * @date   2019-09-27   17:00
 * @author MatrixJoy
 */

class ZCameraVideoRecorder(private val iVideoRecorder: IVideoRecorder) : Runnable {

    private companion object {
        const val MSG_START_RECORDING = 1
        const val MSG_PROCESS_FRAME = 2
        const val MSG_STOP_RECORDING = 3
        const val MSG_QUIT = 4
    }

    private val mReadyLock = Object()
    private var mReady = false
    private var mRecording = false

    private lateinit var mEncoderHandler: EncoderHandler

    private var mInputSurface: ZCodecInputSurface? = null
    private var mDrawer: ZCameraDrawer? = null

    private var mStartTime = 0L


    override fun run() {
        Looper.prepare()
        synchronized(mReadyLock) {
            mEncoderHandler = EncoderHandler(this@ZCameraVideoRecorder)
            mReady = true
            mReadyLock.notifyAll()
        }
        Looper.loop()

        synchronized(mReadyLock) {
            mReady = false
            mRecording = false
        }
    }


    /**
     * 开始录制
     * @eglContext 当前egl环境上下文
     */
    fun startRecord(eglContext: EGLContext) {

        synchronized(mReadyLock) {
            if (mRecording) {
                return
            }
            mRecording = true
            Thread(this, "VideoRecordThread").start()
            while (!mReady) {
                mReadyLock.wait()
            }
        }

        mEncoderHandler.sendMessage(mEncoderHandler.obtainMessage(MSG_START_RECORDING, eglContext))
    }

    /**
     * 停止视频录制
     */
    fun stopRecord() {
        mEncoderHandler.sendMessage(mEncoderHandler.obtainMessage(MSG_STOP_RECORDING))
        mEncoderHandler.sendMessage(mEncoderHandler.obtainMessage(MSG_QUIT))
    }

    /**
     * 开始编码
     * @zCameraFrame 需要编码的帧参数
     */
    fun recordFrame(zCameraFrame: ZCameraFrame) {
        synchronized(mReadyLock) {
            if (!mReady) {
                return
            }
        }
        mEncoderHandler.sendMessage(mEncoderHandler.obtainMessage(MSG_PROCESS_FRAME, zCameraFrame))
    }

    private fun handleStartRecord(eglContext: EGLContext) {

        iVideoRecorder.onVideoRecordStart()
        mInputSurface = ZCodecInputSurface(iVideoRecorder.getInputSurface(), eglContext)
        mInputSurface?.makeCurrent()
        mDrawer = ZCameraDrawer(ZCameraDrawer.TEXTURE_2D)
        mStartTime = System.currentTimeMillis()
    }

    private fun handleRecordFrame(zCameraFrame: ZCameraFrame) {
        iVideoRecorder.encodeVideoFrame()

        mDrawer?.mMVPMatrix = zCameraFrame.modeMatrix
        mDrawer?.draw(zCameraFrame.textureId, zCameraFrame.stMatrix)
        val time = zCameraFrame.presentTime
        mInputSurface?.setPresentationTime(time)
        mInputSurface?.swapBuffers()
    }

    private fun handleStopRecording() {
        iVideoRecorder.onVideoRecordStop()
        release()
    }

    private fun release() {
        mDrawer?.release()
        mInputSurface?.release()
    }

    /**
     * 编码所需要的Handler
     */
    class EncoderHandler(zCameraVideoRecorder: ZCameraVideoRecorder) : Handler() {
        private val weakReference = WeakReference(zCameraVideoRecorder)

        override fun handleMessage(msg: Message?) {
            val videoRecord = weakReference.get()
            when (msg?.what) {
                MSG_START_RECORDING -> {
                    videoRecord?.handleStartRecord(msg.obj as EGLContext)
                }
                MSG_PROCESS_FRAME -> {
                    if (msg.obj is ZCameraFrame) {
                        videoRecord?.handleRecordFrame(msg.obj as ZCameraFrame)
                    }
                }
                MSG_STOP_RECORDING -> {
                    videoRecord?.handleStopRecording()
                }

                MSG_QUIT -> {
                    Looper.myLooper()?.quitSafely()
                }
            }
        }
    }

}
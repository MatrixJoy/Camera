package top.catnemo.zcamera.record

import android.opengl.EGLContext
import android.view.Surface
import top.catnemo.zcamera.Constant
import top.catnemo.zcamera.frame.ZCameraFrame
import top.catnemo.zcamera.interfazz.IRecodingCallback
import top.catnemo.zcamera.record.audio.AudioProcessor
import top.catnemo.zcamera.record.audio.IAudioRecorder
import top.catnemo.zcamera.record.audio.SonicAudioProcessor
import top.catnemo.zcamera.record.audio.ZCameraAudioRecorder
import top.catnemo.zcamera.record.video.IVideoRecorder
import top.catnemo.zcamera.record.video.ZCameraVideoRecorder
import top.catnemo.zmedaicodec.IMediaEnCoder
import top.catnemo.zmedaicodec.IMediaEncoderCallBack
import top.catnemo.zmedaicodec.ZHardMediaEncoder
import top.catnemo.zmedaicodec.util.Logger
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @description 录制实现类
 * @date  2019-11-12   15:41
 * @author MatrixJoy
 */
class ZCameraRecorder : IRecorder {

    private val TAG = "ZCameraRecorder"
    private var mZCameraVideoRecorder: ZCameraVideoRecorder? = null
    private var mZCameraAudioRecorder: ZCameraAudioRecorder? = null

    private var mIMediaEnCoder: IMediaEnCoder? = null

    var mRecording = false
        private set
    private var mFrame: Int = 0

    private var mIRecodingCallback: IRecodingCallback? = null

    private var mIAudioProcessor: AudioProcessor? = null

    private var mSpeed = 1.0f


    /**
     * 视频录制接口回调
     */
    private var mIVideoRecord = object : IVideoRecorder {
        override fun onVideoRecordStart() {
            try {
                mIRecodingCallback?.apply {
                    mIMediaEnCoder?.prepareVideoEncoder(recordConfig.recordWith, recordConfig.recordHeight, 2000000)
                }

            } catch (e: IOException) {
                onError(e.localizedMessage)
            }
        }

        override fun getInputSurface(): Surface {
            return mIMediaEnCoder?.inputSurface!!
        }

        override fun encodeVideoFrame() {
            mIMediaEnCoder?.encodeVideoFrame()
        }

        override fun onVideoRecordStop() {
            mIMediaEnCoder?.stopEncodeVideoFrame()
        }

    }

    /**
     * 音频录制接口回调
     */
    private val mIAudioRecorder = object : IAudioRecorder {

        override fun onAudiRecordError(errorMsg: String) {
            onError(errorMsg)
        }

        override fun onAudioRecord(byteArray: ByteArray, size: Int) {
            if (mRecording) {
                if (mIAudioProcessor != null) {
                    mIAudioProcessor?.apply {
                        val inputBuffer = ByteBuffer.wrap(byteArray, 0, size).order(ByteOrder.LITTLE_ENDIAN)
                        queueInput(inputBuffer)
                        val outPut = output
                        if (outPut.hasRemaining()) {
                            val outPutArray = ByteArray(outPut.remaining())
                            Logger.d(TAG, "original size $size after process size${outPutArray.size} ")
                            outPut.get(outPutArray)
                            outPut.clear()
                            mIMediaEnCoder?.encodeAudioFrame(outPutArray, outPutArray.size)
                        }
                    }

                } else {
                    mIMediaEnCoder?.encodeAudioFrame(byteArray, byteArray.size)
                }

            }
        }

        override fun onAudioRecordStart(sampleRate: Int, channelCont: Int, bitRate: Int, minBufferSize: Int) {
            try {
                mIMediaEnCoder?.prepareAudioEncoder(sampleRate, channelCont, bitRate, minBufferSize.div(mSpeed).plus(10).toInt())
                if (mSpeed != 1.0f) {
                    initAudioProcessor(sampleRate, channelCont)
                }

            } catch (e: IOException) {
                onError(e.localizedMessage)
            }
        }

        override fun onAudioRecordStop() {
            mIAudioProcessor?.apply {
                queueEndOfStream()
                val outPut = output
                if (outPut.hasRemaining()) {
                    val outPutArray = ByteArray(outPut.remaining())
                    outPut.get(outPutArray)
                    outPut.clear()
                    mIMediaEnCoder?.encodeAudioFrame(outPutArray, outPutArray.size)
                }
            }
            mIMediaEnCoder?.stopEncodeAudioFrame()

        }
    }

    private fun initAudioProcessor(sampleRate: Int, channelCont: Int) {
        mIAudioProcessor = SonicAudioProcessor()
        (mIAudioProcessor as SonicAudioProcessor).setSpeed(mSpeed)
        (mIAudioProcessor as SonicAudioProcessor).setPitch(mSpeed)
        mIAudioProcessor?.configure(sampleRate, channelCont, Constant.Audio.ENCODING_PCM_16BIT)
        (mIAudioProcessor as SonicAudioProcessor).outputSampleRateHz = sampleRate
        mIAudioProcessor?.flush()
    }

    private fun onError(errorMsg: String) {
        mIRecodingCallback?.onRecodingError(errorMsg)
    }

    private var mstar = 0L
    override fun recordFrame(zCameraFrame: ZCameraFrame) {
        mIMediaEnCoder?.apply {
            zCameraFrame.presentTime = getVideoPtUS(mFrame++)
            mZCameraVideoRecorder?.recordFrame(zCameraFrame)
        }
        Logger.d(TAG, " fps ${mFrame / ((System.currentTimeMillis() - mstar) / 1000f)}")
    }

    private val iMediaEncoderCallBack = object : IMediaEncoderCallBack {
        override fun onEncodrStart() {

            mIRecodingCallback?.onRecordingStart()
        }

        override fun onEncoderDone(resultPath: String) {
            mIRecodingCallback?.onRecordingStop(resultPath)
        }
    }

    /**
     * 开始录制 | start recording
     * @param eglContext 当前gl上下文
     */
    override fun startRecording(eglContext: EGLContext) {
        mIRecodingCallback?.apply {
            try {
                mSpeed = recordConfig.speed
                mIMediaEnCoder = ZHardMediaEncoder(recordConfig.outPutPath)
                mIMediaEnCoder?.setSpeed(mSpeed)
                if (recordConfig.isMute) {
                    mIMediaEnCoder?.setEncoderCont(1)
                }
            } catch (e: IOException) {
                onError(e.localizedMessage)
            }
            mIMediaEnCoder?.setIMediaEncoderCallBack(iMediaEncoderCallBack)
            mZCameraVideoRecorder = ZCameraVideoRecorder(mIVideoRecord)
            mZCameraVideoRecorder?.startRecord(eglContext)

            if (!recordConfig.isMute) {
                mZCameraAudioRecorder = ZCameraAudioRecorder(mIAudioRecorder)
                mZCameraAudioRecorder?.startRecordAudio()
            }
            mRecording = true
            mstar = System.currentTimeMillis()
        }

    }

    /**
     * 拍摄事件回调 | recoding callback
     */
    override fun setRecodingCallback(iRecodingCallback: IRecodingCallback) {
        mIRecodingCallback = iRecodingCallback
    }

    /**
     * 停止录制 | stop recoding
     */
    override fun stopRecording() {
        mZCameraVideoRecorder?.stopRecord()
        mZCameraAudioRecorder?.stopRecordAudio()

        mRecording = false
        mFrame = 0
    }

    /**
     * 是否正在录制
     */
    override fun isRecording(): Boolean {
        return mRecording
    }

}
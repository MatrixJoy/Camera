package top.catnemo.zcamera.record.audio

import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import top.catnemo.zcamera.Constant
import top.catnemo.codec.util.Logger

/**
 *
 * @author  MatrixJoy
 * @description 音频录制器
 * @date    2019-11-11   14:30
 */
class ZCameraAudioRecorder(private val mIAudioRecorder: IAudioRecorder) {


    companion object {
        private const val TAG = "ZCameraAudioRecorder"
    }


    private var mAudioRecord: AudioRecord? = null
    private var mIsLoopExit = false
    private var mIsRecoding = false
    private var mRecordThread: Thread? = null
    /**
     * 默认采样数据大小
     */
    private var mMinBufferSize = 1024 * 4
    private var mBuffer: ByteArray = ByteArray(mMinBufferSize)


    private val mAudioRunnable = Runnable {
        while (!mIsLoopExit) {
            when (val size = mAudioRecord?.read(mBuffer, 0, mBuffer.size)) {
                AudioRecord.ERROR_INVALID_OPERATION -> {
                    Log.d(TAG, "ERROR_INVALID_OPERATION")
                    mIAudioRecorder.onAudiRecordError("ERROR_INVALID_OPERATION")
                }
                AudioRecord.ERROR_BAD_VALUE -> {
                    Logger.d(TAG, "ERROR_BAD_VALUE")
                    mIAudioRecorder.onAudiRecordError("ERROR_BAD_VALUE")
                }
                else -> {
                    size?.let {
                        mIAudioRecorder.onAudioRecord(mBuffer, it)

                    }
                }
            }
        }
        mIAudioRecorder.onAudioRecordStop()
    }


    fun startRecordAudio(): Boolean {
        mMinBufferSize = AudioRecord.getMinBufferSize(Constant.Audio.SAMPLE_RATE, Constant.Audio.CHANNEL_IN_MONO, Constant.Audio.ENCODING_PCM_16BIT)
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            return false
        }
        try {
            mAudioRecord = AudioRecord(MediaRecorder.AudioSource.MIC, Constant.Audio.SAMPLE_RATE, Constant.Audio.CHANNEL_IN_MONO, Constant.Audio.ENCODING_PCM_16BIT, mMinBufferSize)
            if (mAudioRecord?.state == AudioRecord.STATE_UNINITIALIZED) {
                return false
            }
        } catch (e: IllegalArgumentException) {
            Logger.e(TAG, "error", e)
            return false
        }
        mBuffer = ByteArray(mMinBufferSize)

        mAudioRecord?.startRecording()

        mIsLoopExit = false

        mRecordThread = Thread(mAudioRunnable, "AudioRecordThread")
        mRecordThread?.start()

        mIAudioRecorder.onAudioRecordStart(Constant.Audio.SAMPLE_RATE, 1, Constant.Audio.ENCODING_BIT_RATE, mMinBufferSize)
        mIsRecoding = true
        return true
    }

    fun stopRecordAudio() {
        if (!mIsRecoding) {
            return
        }
        mIsLoopExit = true
        try {
            mRecordThread?.interrupt()
            mRecordThread?.join(1000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (mAudioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord?.stop()
        }
        mAudioRecord?.release()
        mIsRecoding = false
    }
}
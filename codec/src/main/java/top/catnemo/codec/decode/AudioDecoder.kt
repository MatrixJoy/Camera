package top.catnemo.codec.decode

import android.media.AudioFormat
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

/**
 * 音频解码器
 */
class AudioDecoder {

    companion object {
        private const val TAG = "AudioDecoder"
    }

    private var audioDecodeThread: AudioDecodeThread? = null

    fun initAudioDecode(path: String, audioDecodeCallBack: AudioDecodeCallBack) {
        audioDecodeThread = AudioDecodeThread(path, audioDecodeCallBack)
    }

    fun startDecode() {
        audioDecodeThread?.start()
    }

    fun stop() {
        audioDecodeThread?.stopDecode()
    }

    private class AudioDecodeThread(val path: String, val audioDecodeCallBack: AudioDecodeCallBack) : Thread() {

        private var stop = false
        private var isStart = false

        companion object {
            private const val AUDIO_PREFIX = "audio/"
        }

        override fun run() {
            startDecode(path, audioDecodeCallBack)
        }

        fun startDecode(path: String, callback: AudioDecodeCallBack) {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(path)
            val trackCount = mediaExtractor.trackCount
            var format: MediaFormat? = null
            var mime = ""
            var index = 0
            for (i in 0 until trackCount) {
                format = mediaExtractor.getTrackFormat(i)
                mime = format.getString(MediaFormat.KEY_MIME)
                if (mime.startsWith(AUDIO_PREFIX)) {
                    index = i
                    break
                }
            }
            format ?: return

            isStart = true

            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            callback.startDecode(sampleRate, audioFormat, channelCount)

            val codec = MediaCodec.createDecoderByType(mime)
            codec.configure(format, null, null, 0)
            codec.start()
            val codecInputBuffers = codec.inputBuffers
            var codecOutputBuffers = codec.outputBuffers

            mediaExtractor.selectTrack(index)

            val timeOutUs = 5000L
            val info = MediaCodec.BufferInfo()

            var inputEOS = false
            var outputEOS = false

            val startTime = System.currentTimeMillis()
            while (!outputEOS && !stop) {
                if (!inputEOS) {
                    val inputBufferIndex = codec.dequeueInputBuffer(timeOutUs)
                    if (inputBufferIndex >= 0) {
                        val dstBuf = codecInputBuffers[inputBufferIndex]
                        var sampleSize = mediaExtractor.readSampleData(dstBuf, 0)
                        var presentationTimeUs = 0L
                        if (sampleSize < 0) {
                            inputEOS = true
                            sampleSize = 0
                        } else {
                            presentationTimeUs = mediaExtractor.sampleTime
                        }
                        codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, presentationTimeUs,
                                if (inputEOS) MediaCodec.BUFFER_FLAG_END_OF_STREAM else 0)
                        if (!inputEOS) {
                            Log.d(TAG, "presentationTimeUs $presentationTimeUs")
                            mediaExtractor.advance()
                        }
                    }
                }

                val res = codec.dequeueOutputBuffer(info, timeOutUs)
                if (res > 0) {
                    val buf = codecOutputBuffers[res]
                    callback.decoding(buf)
                    codec.releaseOutputBuffer(res, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        outputEOS = true
                    }
                } else if (res == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    codecOutputBuffers = codec.outputBuffers
                } else if (res == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val format = codec.outputFormat
                    Log.d(TAG, "output format changed $format")
                } else {
                    Log.d(TAG, "unknown error $res")
                }
            }
            codec.stop()
            codec.release()
            mediaExtractor.release()
            callback.decodeEnd()
            Log.d(TAG, "costTime ${System.currentTimeMillis() - startTime}")
        }

        fun stopDecode() {
            if (!isStart) {
                return
            }
            stop = true
        }
    }

    interface AudioDecodeCallBack {
        fun startDecode(sampleRate: Int, enCoding: Int, channelCount: Int)

        fun decoding(shortBuffer: ByteBuffer)

        fun decodeEnd()
    }
}
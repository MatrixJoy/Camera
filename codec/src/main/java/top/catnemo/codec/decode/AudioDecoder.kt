package top.catnemo.codec.decode

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.nio.ByteBuffer

/**
 * 音频解码器
 */
class AudioDecoder {

    private var stop = false

    companion object {
        private const val TAG = "AudioDecoder"
    }

    fun startDecode(path: String, callback: (ByteBuffer) -> Unit) {
        val mediaExtractor = MediaExtractor()
        mediaExtractor.setDataSource(path)
        val trackCount = mediaExtractor.trackCount
        var format: MediaFormat? = null
        var mime: String = ""
        var index = 0
        for (i in 0 until trackCount) {
            format = mediaExtractor.getTrackFormat(i)
            mime = format.getString(MediaFormat.KEY_MIME)
            if (mime.startsWith("audio/")) {
                index = i
                break
            }
        }

        format ?: return

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

        var startTime = System.currentTimeMillis()
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
                        mediaExtractor.seekTo(presentationTimeUs + 5 * 1000 * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        mediaExtractor.advance()
                    }
                }
            }

            var res = codec.dequeueOutputBuffer(info, timeOutUs)
            if (res > 0) {
                val outputBufIndex = res
                val buf = codecOutputBuffers[outputBufIndex]
                callback(buf)
                codec.releaseOutputBuffer(outputBufIndex, false)
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
        Log.d(TAG, "costTime ${System.currentTimeMillis() - startTime}")
    }

    fun stop() {
        stop = true
    }
}
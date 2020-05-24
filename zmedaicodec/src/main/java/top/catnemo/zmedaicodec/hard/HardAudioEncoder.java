package top.catnemo.zmedaicodec.hard;

import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import java.io.IOException;
import java.nio.ByteBuffer;
import top.catnemo.zmedaicodec.IMuxerListener;
import top.catnemo.zmedaicodec.util.Logger;

/**
 * hard encoder for audio
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/20
 */
public class HardAudioEncoder {

    private static final String TAG = "HardAudioEncoder";

    private static final String MIME_TYPE = "audio/mp4a-latm";
    private static final int TIME_OUT = 10000;

    private MediaCodec mAudioEncoder;

    private MediaCodec.BufferInfo mBufferInfo;

    private int mTrackIndex = -1;
    private boolean mMuxerStarted = false;

    private long mPrePtUs = 0;

    private IMuxerListener mIMuxerListener;
    private long mStartTime = 0L;

    private float mSpeed = 1.0f;

    public void setIMuxerListener(IMuxerListener IMuxerListener) {
        mIMuxerListener = IMuxerListener;
    }

    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    public void prepareAudioEncoder(int sampleRate, int channelCount, int bitRate, int maxBufferSize)
        throws IOException {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(MIME_TYPE, sampleRate, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_MASK, AudioFormat.CHANNEL_IN_MONO);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, channelCount);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, maxBufferSize);

        mBufferInfo = new BufferInfo();

        mAudioEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mAudioEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mAudioEncoder.start();

        mTrackIndex = -1;
        mMuxerStarted = false;

        mStartTime = System.nanoTime();
    }

    public void encodeAudio(byte[] inputs, int length) {
        ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();

        int encoderStatus = mAudioEncoder.dequeueInputBuffer(TIME_OUT);
        if (encoderStatus >= 0) {
            ByteBuffer inputBuffer = inputBuffers[encoderStatus];
            inputBuffer.clear();
            Logger.d(TAG, "inputBuffer size " + inputBuffer.remaining());
            if (inputs != null) {
                inputBuffer.put(inputs);
            }
            if (length <= 0) {
                mAudioEncoder.queueInputBuffer(encoderStatus, 0, 0, getPtUS(), MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                if (inputs != null) {
                    mAudioEncoder.queueInputBuffer(encoderStatus, 0, inputs.length, getPtUS(), 0);
                }
            }
        } else {
            Logger.w(TAG, "encoderStatus " + encoderStatus);
        }

        ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        while (true) {
            int encodeOutStatus = mAudioEncoder.dequeueOutputBuffer(mBufferInfo, TIME_OUT);
            if (encodeOutStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                break;
            } else if (encodeOutStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    Logger.d(TAG, "audio encoder output format changed twice");
                    return;
                }
                if (mIMuxerListener != null) {
                    MediaFormat mediaFormat = mAudioEncoder.getOutputFormat();
                    mTrackIndex = mIMuxerListener.addTrack(mediaFormat);
                    Logger.d(TAG, "audio encoder output format changed " + mediaFormat);
                    if (!mIMuxerListener.start()) {
                        synchronized (mIMuxerListener) {
                            while (!mIMuxerListener.isStart()) {
                                try {
                                    mIMuxerListener.wait(100);
                                } catch (InterruptedException e) {
                                    Logger.e(TAG, "error ", e);
                                }
                            }
                        }
                    }
                }
                mMuxerStarted = true;
            } else if (encodeOutStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                outputBuffers = mAudioEncoder.getOutputBuffers();
            } else if (encodeOutStatus < 0) {
                Logger.w(TAG, "audio encoder unknown status " + encodeOutStatus);
            } else {
                ByteBuffer encodeData = outputBuffers[encodeOutStatus];
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    mBufferInfo.size = 0;
                }
                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        return;
                    }
                    if (encodeData != null) {
                        encodeData.position(mBufferInfo.offset);
                        encodeData.limit(mBufferInfo.size + mBufferInfo.offset);
                        if (mIMuxerListener != null) {
                            mIMuxerListener.writeSamples(mTrackIndex, encodeData, mBufferInfo);
                            Logger.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                                mBufferInfo.presentationTimeUs);
                            mPrePtUs = mBufferInfo.presentationTimeUs;
                        }
                    }
                }

                mAudioEncoder.releaseOutputBuffer(encodeOutStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Logger.d(TAG, "编码结束");
                    break;
                }
            }
        }
    }

    public void release() {
        mAudioEncoder.stop();
        mAudioEncoder.release();
        mTrackIndex = -1;
        mMuxerStarted = false;
    }

    private long getPtUS() {
        long result = (long) ((System.nanoTime() - mStartTime) / 1000L / mSpeed);
        if (result < mPrePtUs) {
            result = (mPrePtUs - result) + result;
        }
        return result;
    }

}

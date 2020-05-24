package top.catnemo.zmedaicodec.hard;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import top.catnemo.zmedaicodec.IMuxerListener;
import top.catnemo.zmedaicodec.util.Logger;


/**
 * hard encoder for video
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/20
 */
public class HardVideoEncoder {

    private static final String TAG = "HardVideoEncoder";

    private static final String MIME_TYPE = "video/avc";
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int I_FRAME_INTERVAL = 5;           // 5 seconds between I-frames
    private static final int TIMEOUT_USEC = 10000;

    private MediaCodec mEncoder;
    private MediaCodec.BufferInfo mBufferInfo;
    private Surface mInputSurface;
    private IMuxerListener mIMuxerListener;

    private int mTrackIndex = -1;

    private boolean mMuxerStarted = false;


    public void prepareVideoEncoder(int width, int height, int bitRate) throws IOException {
        mBufferInfo = new MediaCodec.BufferInfo();
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, I_FRAME_INTERVAL);
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);

        Logger.d(TAG, "media format prepared " + format);

        mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();

        mTrackIndex = -1;
        mMuxerStarted = false;

        Logger.d(TAG, "MediaCodec started");
    }

    public void setIMuxerListener(IMuxerListener IMuxerListener) {
        mIMuxerListener = IMuxerListener;
    }

    public  Surface getInputSurface() {
        return mInputSurface;
    }


    public void drainEncoder(boolean eos) {

        if (eos) {
            Logger.d(TAG, "send eos to stream ");

            mEncoder.signalEndOfInputStream();
        }
        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();

        while (true) {

            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!eos) {
                    break;      // out of whilse
                } else {
                    Logger.d(TAG, "no output available, spinning to await EOS");
                }
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                encoderOutputBuffers = mEncoder.getOutputBuffers();
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (mMuxerStarted) {
                    Logger.d(TAG, "muxer is start but ouput format changed");
                    return;
                }
                if (mIMuxerListener != null) {
                    MediaFormat newFormat = mEncoder.getOutputFormat();
                    mTrackIndex = mIMuxerListener.addTrack(newFormat);
                    Logger.d(TAG, "encode video output format changed " + newFormat);
                    if (!mIMuxerListener.start()) {
                        synchronized (mIMuxerListener) {
                            while (!mIMuxerListener.isStart()) {
                                try {
                                    mIMuxerListener.wait(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    mMuxerStarted = true;
                }
            } else if (encoderStatus < 0) {
                Logger.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                    encoderStatus);
            } else {
                ByteBuffer encoderData = encoderOutputBuffers[encoderStatus];
                if (encoderData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                        " was null");
                }
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Logger.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }
                if (mBufferInfo.size != 0) {
                    encoderData.position(mBufferInfo.offset);
                    encoderData.limit(mBufferInfo.offset + mBufferInfo.size);
                    if (mIMuxerListener != null) {
                        mIMuxerListener.writeSamples(mTrackIndex, encoderData, mBufferInfo);
                    }
                    Logger.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer, ts=" +
                        mBufferInfo.presentationTimeUs);
                }
                mEncoder.releaseOutputBuffer(encoderStatus, false);
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!eos) {
                        Logger.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        Logger.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }

    }

    public void release() {
        mEncoder.stop();
        mEncoder.release();
        mTrackIndex = -1;
        mMuxerStarted = false;
    }
}

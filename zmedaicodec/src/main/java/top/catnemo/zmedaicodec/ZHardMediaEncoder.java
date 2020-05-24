package top.catnemo.zmedaicodec;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaMuxer.OutputFormat;
import android.view.Surface;
import java.io.IOException;
import java.nio.ByteBuffer;
import top.catnemo.zmedaicodec.hard.HardAudioEncoder;
import top.catnemo.zmedaicodec.hard.HardVideoEncoder;
import top.catnemo.zmedaicodec.util.Logger;


/**
 * hard encoder
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/20
 */
public class ZHardMediaEncoder implements IMediaEnCoder, IMuxerListener {

    public static final String TAG = "ZHardMediaEncoder";

    private MediaMuxer mMediaMuxer;

    private HardVideoEncoder mHardVideoEncoder;

    private HardAudioEncoder mHardAudioEncoder;

    private volatile boolean mIsStartMuxer;

    /**
     * 编码器启动的个数
     */
    private int mStartEncoderCount;

    private static final long ONE_BILLION = 1000000000L;

    /**
     * 编码器数量  默认是两个
     * 音频编码器
     * 视频编码器
     */
    private int mEncoderCont = 2;

    private IMediaEncoderCallBack mIMediaEncoderCallBack;

    private String mOutputPath;

    private float mSpeed;


    public ZHardMediaEncoder(String outputPath) throws IOException {
        mOutputPath = outputPath;
        mMediaMuxer = new MediaMuxer(outputPath, OutputFormat.MUXER_OUTPUT_MPEG_4);
        Logger.d(TAG, "1 startEncodeFrame");
    }

    @Override
    public void setIMediaEncoderCallBack(IMediaEncoderCallBack IMediaEncoderCallBack) {
        mIMediaEncoderCallBack = IMediaEncoderCallBack;
    }

    @Override
    public void setEncoderCont(int count) {
        mEncoderCont = count;
    }

    @Override
    public void setSpeed(float speed) {
        mSpeed = speed;
    }

    @Override
    public void prepareVideoEncoder(int outWidth, int outHeight, int bitrate) throws IOException {
        mHardVideoEncoder = new HardVideoEncoder();
        mHardVideoEncoder.setIMuxerListener(this);
        mHardVideoEncoder.prepareVideoEncoder(outWidth, outHeight, bitrate);
        Logger.d(TAG, "prepareVideoEncoder Thread name " + Thread.currentThread().getName());
    }

    @Override
    public void prepareAudioEncoder(int sampleRate, int channelCount, int bitRate, int maxBufferSize)
        throws IOException {
        mHardAudioEncoder = new HardAudioEncoder();
        mHardAudioEncoder.setSpeed(mSpeed);
        mHardAudioEncoder.setIMuxerListener(this);
        mHardAudioEncoder.prepareAudioEncoder(sampleRate, channelCount, bitRate, maxBufferSize);

        Logger.d(TAG, "prepareAudioEncoder Thread name " + Thread.currentThread().getName());
    }


    @Override
    public void encodeVideoFrame() {
        mHardVideoEncoder.drainEncoder(false);
    }

    @Override
    public void encodeAudioFrame(byte[] inputPcm, int length) {
        Logger.d(TAG, "receiveSamples length " + length);
        mHardAudioEncoder.encodeAudio(inputPcm, length);
    }

    @Override
    public void stopEncodeAudioFrame() {
        mHardAudioEncoder.encodeAudio(null, 0);
        releaseEncoder();
    }

    @Override
    public void stopEncodeVideoFrame() {
        mHardVideoEncoder.drainEncoder(true);
        releaseEncoder();
    }

    private void releaseEncoder() {
        mStartEncoderCount--;
        Logger.d(TAG, "releaseEncoder mStartEncoderCount " + mStartEncoderCount);
        if (mStartEncoderCount <= 0) {
            release();
            Logger.d(TAG, "release");
            if (mIMediaEncoderCallBack != null) {
                mIMediaEncoderCallBack.onEncoderDone(mOutputPath);
            }
        }
    }

    private void release() {
        if (mHardVideoEncoder != null) {
            mHardVideoEncoder.release();
        }
        if (mHardAudioEncoder != null) {
            mHardAudioEncoder.release();
        }
        if (mMediaMuxer != null) {
            mMediaMuxer.stop();
            mMediaMuxer.release();
            mMediaMuxer = null;
        }
    }

    @Override
    public Surface getInputSurface() {
        return mHardVideoEncoder.getInputSurface();
    }

    @Override
    public synchronized boolean start() {
        mStartEncoderCount++;
        Logger.d(TAG, "start");
        if (mStartEncoderCount == mEncoderCont) {
            mIsStartMuxer = true;
            mMediaMuxer.start();
            notifyAll();
            Logger.d(TAG, "MediaMuxer started");
            if (mIMediaEncoderCallBack != null) {
                mIMediaEncoderCallBack.onEncodrStart();
            }
        }
        return mIsStartMuxer;
    }


    @Override
    public synchronized int addTrack(MediaFormat format) {
        int index = mMediaMuxer.addTrack(format);
        Logger.d(TAG, "addTrack");
        return index;
    }

    @Override
    public synchronized void writeSamples(int trackIndex, ByteBuffer encoderData, BufferInfo bufferInfo) {
        if (!mIsStartMuxer) {
            Logger.w(TAG, "writeSamples but muxer not start");
            return;
        }
        mMediaMuxer.writeSampleData(trackIndex, encoderData, bufferInfo);
    }

    @Override
    public synchronized boolean isStart() {
        return mIsStartMuxer;
    }

    @Override
    public long getVideoPtUS(int frame) {
        return (long) (frame * ONE_BILLION / 30 / mSpeed);
    }
}

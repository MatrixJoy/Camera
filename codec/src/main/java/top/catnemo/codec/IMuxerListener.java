package top.catnemo.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * MediaMuxer interface
 *
 * @author matrixJoy
 * @version V1.0
 * @since 2019/05/21
 */
public interface IMuxerListener {


    boolean start();

    int addTrack(MediaFormat format);

    void writeSamples(int statues, ByteBuffer encoderData, MediaCodec.BufferInfo bufferInfo);

    boolean isStart();

}

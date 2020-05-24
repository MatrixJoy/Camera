package top.catnemo.zmedaicodec;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * MediaMuxer interface
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/21
 */
public interface IMuxerListener {


    boolean start();

    int addTrack(MediaFormat format);

    void writeSamples(int statues, ByteBuffer encoderData, MediaCodec.BufferInfo bufferInfo);

    boolean isStart();

}

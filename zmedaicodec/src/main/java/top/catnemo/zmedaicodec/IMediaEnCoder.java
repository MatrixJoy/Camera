package top.catnemo.zmedaicodec;

import android.view.Surface;
import java.io.IOException;

/**
 *
 * @author franticzhou
 * @description
 * @date 2019-11-12   14:35 
 */
public interface IMediaEnCoder {

    /**
     * 配置视频编码器
     * @param outWidth 输出视频宽
     * @param outHeight 输出视频高
     * @param bitrate bit率
     */
    void prepareVideoEncoder(int outWidth, int outHeight, int bitrate) throws IOException;

    /**
     * 配置音频编码器
     * @param sampleRate 采样率
     * @param channelCount 声道数
     * @param bitRate 带宽
     */
    void prepareAudioEncoder(int sampleRate, int channelCount, int bitRate, int maxBufferSize) throws IOException;


    /**
     * 编码视频
     */
    void encodeVideoFrame();

    /**
     * 编码音频
     */
    void encodeAudioFrame(byte[] inputPcm, int length);

    /**
     * 结束视频编码
     */
    void stopEncodeVideoFrame();

    /**
     * 结束音频编码
     */
    void stopEncodeAudioFrame();

    /**
     * 获取编码所需的 surface
     * @return 编码器吐出来的surface
     */
    Surface getInputSurface();

    /**
     * 根据当前视频帧获取对应时间戳
     * @param frame 当前帧
     * @return 时间戳 US
     */
    long getVideoPtUS(int frame);

    void setIMediaEncoderCallBack(IMediaEncoderCallBack iMediaEncoderCallBack);

    void setEncoderCont(int count);

    /**
     * 设置变速
     * @param speed 速度
     */
    void setSpeed(float speed);

}

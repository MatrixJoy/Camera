package top.catnemo.zmedaicodec;

/**
 *
 * @author franticzhou
 * @description
 * @date 2019-11-14   13:36 
 */
public interface IMediaEncoderCallBack {

    void onEncodrStart();

    void onEncoderDone(String resultPath);

}

package top.catnemo.zcamera.interfazz

/**
 * camera 接口
 * @author matrixJoy
 * @version V1.0
 * @since 2019/05/10
 *
 */
interface IZCamera {

    /**
     * 不带参数
     */
    fun openCamera()

    /**
     * 打开相机
     */
    fun openCamera(previewWidth: Int, previewHeight: Int, previewFps: Int)


    /**
     * 开始预览
     */

    fun startPreview()

    /**
     * 切换摄像头
     */
    fun switchCamera()

    /**
     * 释放资源
     */
    fun releaseCamera()

    /**
     * 设置摄像头id
     */
    fun setCameraId(cameraId: Int)

    /**
     * 开启闪光灯
     */
    fun openFlash()

    /**
     * 聚焦
     */
    fun doFocus(x: Float, y: Float)

    fun updateExposureCompensation(exposureCompensation:Int)

    /**
     * 实现自己的OpenGl Es 纹理绘制
     */
    fun setIDrawer(iDrawer: IZCameraDrawer?)

    /**
     * 注销
     */
    fun onDestroy()

    /**
     * 拍照
     * @iCapturePhoto 拍照回调接口
     */
    fun takePicture(iCapturePhoto: ICapturePhoto)

    /**
     * 开始录制视频
     * @recordConfig 录制配置类
     */
    fun startRecord(iRecodingCallback: IRecodingCallback)

    /**
     * 停止录制
     */
    fun stopRecord()

}
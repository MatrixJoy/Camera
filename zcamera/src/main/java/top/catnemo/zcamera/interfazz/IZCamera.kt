package top.catnemo.zcamera.interfazz

/**
 * camera 接口
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/10
 *
 */
interface IZCamera {

    /**
     * 开启预览 带参数
     */
    fun setPreviewParams(previewWidth: Int, previewHeight: Int, previewFps: Int)

    fun switchCamera()

    fun releaseCamera()

    fun setCameraId(cameraId: Int)

    fun openFlash()

    fun doFocus(x: Float, y: Float)

    fun setIDrawer(iDrawer: IZCameraDrawer?)

    fun onDestroy()

    fun takePicture(iCapturePhoto: ICapturePhoto)

}
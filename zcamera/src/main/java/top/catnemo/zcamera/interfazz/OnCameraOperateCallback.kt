package top.catnemo.zcamera.interfazz

/**
 * 相机操作结果回调
 * @author MatrixJoy
 * @version V1.0
 * @since 2019/05/10
 *
 */
interface OnCameraOperateCallback {
    fun onOpenCameraSuccess(flashIsEnable: Boolean)
    fun onOpenCameraFail()
    fun onOpenFlashFail(msg: String)
    fun onStartPreviewSuccess()
    fun onStartPreviewFail(msg: String)
    fun onFocusDone(success: Boolean?)
}
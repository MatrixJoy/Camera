package top.catnemo.zcamera.interfazz

/**
 * 拍照接口
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/15
 *
 */
interface ICapturePhoto {
    fun onCapturePhoto(byteArray: ByteArray)
}
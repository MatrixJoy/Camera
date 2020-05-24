package top.catnemo.zcamera.interfazz

import java.nio.ByteBuffer

/**
 * 拍照接口
 * @author MatrixJoy
 * @version V1.0
 * @since 2019/05/15
 *
 */
interface ICapturePhoto {
    fun onCapturePhoto(buffer: ByteBuffer)
}
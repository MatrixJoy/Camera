package top.catnemo.zcamera.record

import android.opengl.EGLContext
import top.catnemo.zcamera.frame.ZCameraFrame
import top.catnemo.zcamera.interfazz.IRecodingCallback

/**
 * @description 录制接口，外部可以接管录制事件
 *
 * @author  MatrixJoy
 *
 * @date    2019-11-13   21:36
 */
interface IRecorder {

    fun setRecodingCallback(iRecodingCallback: IRecodingCallback)

    fun startRecording(eglContext: EGLContext)

    fun stopRecording()

    fun recordFrame(zCameraFrame: ZCameraFrame)

    fun isRecording(): Boolean

}
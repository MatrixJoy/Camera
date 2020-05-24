package top.catnemo.zcamera.interfazz

import top.catnemo.zcamera.RecordConfig

/**
 *
 * @author  MatrixJoy
 * @description 录制接口回调
 * @date    2019-11-07   19:55
 */
interface IRecodingCallback {
    val recordConfig: RecordConfig
    fun onRecordingStart()
    fun onRecordingStop(outputPath: String)

    fun onRecodingError(errorMsg: String)
}
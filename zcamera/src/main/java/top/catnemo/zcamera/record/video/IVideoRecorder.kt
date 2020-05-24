package top.catnemo.zcamera.record.video

import android.view.Surface

/**
 * @description 视频录制接口
 *
 * @author  MatrixJoy
 *
 * @date    2019-11-12   15:26
 */
interface IVideoRecorder {
    fun onVideoRecordStart()
    fun getInputSurface(): Surface
    fun encodeVideoFrame()
    fun onVideoRecordStop()
}
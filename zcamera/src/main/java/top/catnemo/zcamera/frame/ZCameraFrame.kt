package top.catnemo.zcamera.frame

import top.catnemo.zcamera.util.GlUtil

/**
 * @description 录制时候的一帧
 * @author  MatrixJoy
 * @date    2019-11-07   17:10
 */
data class ZCameraFrame(var textureId: Int = 0,
                        var stMatrix: FloatArray = GlUtil.IDENTITY_MATRIX,
                        var modeMatrix: FloatArray = GlUtil.IDENTITY_MATRIX,
                        var presentTime: Long = 0L)
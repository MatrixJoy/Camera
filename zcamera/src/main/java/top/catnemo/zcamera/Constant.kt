package top.catnemo.zcamera

import android.media.AudioFormat

/**
 * 常量配置类
 * @author MatrixJoy
 * @version V1.0
 * @since 2019/05/10
 *
 */
internal object Constant {
    const val TAG = "ZCamera"
    const val isDebug = true


    /**
     * 默认预览宽
     */
    const val DEFAULT__PREVIEW_WIDTH = 1080
    /**
     * 默认预览的高
     */
    const val DEFAULT__PREVIEW_HEIGHT = 720
    /**
     * 默认预览 fps
     */
    const val DEFAULT__PREVIEW_FPS = 30


    /**
     * opengl es android 录制配置特殊标识
     */
    const val EGL_RECORDABLE_ANDROID = 0x3142


    object Audio {
        const val SAMPLE_RATE = 44100

        const val CHANNEL_IN_MONO = AudioFormat.CHANNEL_IN_MONO

        const val ENCODING_PCM_16BIT = AudioFormat.ENCODING_PCM_16BIT

        const val ENCODING_BIT_RATE = 64000
    }

}
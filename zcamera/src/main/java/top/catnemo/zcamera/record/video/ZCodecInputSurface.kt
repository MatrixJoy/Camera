package top.catnemo.zcamera.record.video

import android.opengl.*
import android.util.Log
import android.view.Surface
import top.catnemo.zcamera.Constant


/**
 * @description gl环境初始化和surface创建，用于实现 mediacodec编码
 * @date 2019-09-27   15:40
 * @author MatrixJoy
 */
class ZCodecInputSurface(var surface: Surface?, var shareContext: EGLContext = EGL14.EGL_NO_CONTEXT) {
    private val TAG = "ZCodecInputSurface"

    private var mEglDisplay: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var mEGLContext: EGLContext = EGL14.EGL_NO_CONTEXT
    private var mEGlSurface: EGLSurface = EGL14.EGL_NO_SURFACE

    init {
        surface?.let {
            eglSetup()
        }
    }

    private fun eglSetup() {
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            Log.d(TAG, "this device no display")
            return
        }
        val version = IntArray(2)
        if (!EGL14.eglInitialize(mEglDisplay, version, 0, version, 1)) {
            mEglDisplay = EGL14.EGL_NO_DISPLAY
            Log.d(TAG, "egl initialize fail")
            return
        }
        val attribList = intArrayOf(EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                Constant.EGL_RECORDABLE_ANDROID, 1,
                EGL14.EGL_NONE // placeholder
        )
        val eglConfigs = arrayOfNulls<EGLConfig>(1)
        val numConfigs = IntArray(1)
        if (!EGL14.eglChooseConfig(mEglDisplay, attribList, 0, eglConfigs, 0, eglConfigs.size, numConfigs, 0)) {
            Log.d(TAG, "egl eglChooseConfig fail")
            return
        }
        if (eglConfigs[0] != null) {
            val contextAttribus = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE)
            val context = EGL14.eglCreateContext(mEglDisplay, eglConfigs[0], shareContext, contextAttribus, 0)
            if (EGL14.eglGetError() == EGL14.EGL_SUCCESS) {
                mEGLContext = context
            }
        }
        val surfaceArrtib = intArrayOf(EGL14.EGL_NONE)
        mEGlSurface = EGL14.eglCreateWindowSurface(mEglDisplay, eglConfigs[0], surface, surfaceArrtib, 0)
    }

    fun release() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(mEglDisplay, mEGlSurface)
            EGL14.eglDestroyContext(mEglDisplay, mEGLContext)
            EGL14.eglReleaseThread()
            EGL14.eglTerminate(mEglDisplay)
        }

        surface?.release()
        mEglDisplay = EGL14.EGL_NO_DISPLAY
        mEGLContext = EGL14.EGL_NO_CONTEXT
        mEGlSurface = EGL14.EGL_NO_SURFACE

        surface = null
    }

    fun makeCurrent(): Boolean {
        return EGL14.eglMakeCurrent(mEglDisplay, mEGlSurface, mEGlSurface, mEGLContext)
    }

    fun swapBuffers(): Boolean {
        return EGL14.eglSwapBuffers(mEglDisplay, mEGlSurface)
    }

    fun setPresentationTime(nsecs: Long) {
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEGlSurface, nsecs)
        checkError("setPresentationTime")
    }

    fun checkError(msg: String) {
        val error = EGL14.eglGetError()
        if (error != EGL14.EGL_SUCCESS) {
            Log.e(TAG, "$msg EGL error: 0x + ${Integer.toHexString(error)}")
        }
    }
}
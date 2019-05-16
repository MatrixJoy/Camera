package top.catnemo.zcamera.render

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import top.catnemo.zcamera.interfazz.ICapturePhoto
import top.catnemo.zcamera.interfazz.IZCameraDrawer
import top.catnemo.zcamera.util.GlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 相机数据渲染
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/10
 *
 */
internal class ZCameraRender : GLSurfaceView.Renderer {

    private var mZDrawerOES: ZCameraDrawer? = null
    private var mZDrawer2D: ZCameraDrawer? = null

    private var mSurfaceTexture: SurfaceTexture? = null
    private var mTextureOESId: Int = 0
    private val mStMatrix = FloatArray(16)
    private var mWidth = 0
    private var mHeight = 0
    private var mTexture2DId: Int = 0
    private var mFrameBuffer = 0
    private var mFrameBuffers = IntArray(1)
    private val mModeMtx = FloatArray(16)
    var mRotation: Float = 0F
        set(value) {
            if (value != 0f) {
                field = value
                mChangeMtx = true
            }
        }
    private var mChangeMtx = false
    var mWaitDraw = false
    var mOnSurfaceTextureCreatedListener: OnSurfaceTextureCreatedListener? = null
    var mIDrawer: IZCameraDrawer? = null
    var onSurfaceCreated = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mZDrawerOES = ZCameraDrawer(ZCameraDrawer.TEXTURE_OES)
        mZDrawer2D = ZCameraDrawer(ZCameraDrawer.TEXTURE_2D)
        mZDrawerOES?.apply {
            mTextureOESId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
            mSurfaceTexture = SurfaceTexture(mTextureOESId)
            mOnSurfaceTextureCreatedListener?.onSurfaceTextureCreated(mSurfaceTexture)
        }
        onSurfaceCreated = true
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
        mIDrawer?.onDrawerInit(width, height)
        mWidth = width
        mHeight = height
    }

    override fun onDrawFrame(gl: GL10?) {
        if (mWaitDraw) {
            return
        }
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        mSurfaceTexture?.updateTexImage()

        drawToFbo()
        var processId = mTexture2DId
        mIDrawer?.apply {
            processId = beforeOnDrawFrame(mSurfaceTexture, mTexture2DId)
        }
        if (mChangeMtx) {
            changeRotation()
            mChangeMtx = false
            mZDrawer2D?.mMVPMatrix = mModeMtx
        }
        mSurfaceTexture?.getTransformMatrix(mStMatrix)
        var mtx = mStMatrix
        if (processId != mTexture2DId) {
            mtx = GlUtil.IDENTITY_MATRIX
        }
        mZDrawer2D?.draw(processId, mtx)
        mIDrawer?.afterDrawFrame(mSurfaceTexture, processId)

    }

    private fun changeRotation() {
        Matrix.setIdentityM(mModeMtx, 0)
        val viewMtx = FloatArray(16)
        Matrix.setIdentityM(viewMtx, 0)
        Matrix.rotateM(viewMtx, 0, mRotation, 0f, 0f, 1f)
        if (mRotation == 90f) {
            Matrix.scaleM(viewMtx, 0, -1f, 1f, 1f)
        }
        Matrix.multiplyMM(mModeMtx, 0, viewMtx, 0, mModeMtx, 0)
    }

    private fun drawToFbo() {
        if (mFrameBuffer == 0) {
            GLES30.glGenFramebuffers(mFrameBuffers.size, mFrameBuffers, 0)
            mTexture2DId = GlUtil.createTextureObject(GLES30.GL_TEXTURE_2D)
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA,
                    mWidth, mHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, null)
            mFrameBuffer = mFrameBuffers[0]
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mFrameBuffer)
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D,
                mTexture2DId, 0)
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glViewport(0, 0, mWidth, mHeight)
        mZDrawerOES?.draw(mTextureOESId, mStMatrix)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
    }

    fun captureFrame(iCapturePhoto: ICapturePhoto) {
        mWaitDraw = true
        val bb = ByteBuffer.allocateDirect(mWidth * mHeight * 4)
                .order(ByteOrder.LITTLE_ENDIAN)
        GLES30.glReadPixels(0, 0, mWidth, mHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, bb)
        GlUtil.checkGlError("glReadPixels")
        bb.rewind()
        val byteArray = ByteArray(bb.remaining())
        bb.get(byteArray)
        mWaitDraw = false
        iCapturePhoto.onCapturePhoto(byteArray)
    }

    fun release() {
        deleteFrameBuffer()
        deleteTexture()
        mIDrawer?.onDrawerRelease()
        mZDrawerOES?.release()

    }

    private fun deleteTexture() {
        if (mTextureOESId != 0) {
            val textureArray = intArrayOf(mTextureOESId)
            GLES30.glDeleteTextures(1, textureArray, 0)
            mTextureOESId = 0
        }
        if (mTexture2DId != 0) {
            val textureArray = intArrayOf(mTexture2DId)
            GLES30.glDeleteTextures(1, textureArray, 0)
            mTexture2DId = 0
        }
    }

    private fun deleteFrameBuffer() {
        if (mFrameBuffer != 0) {
            GLES30.glDeleteFramebuffers(1, mFrameBuffers, 0)
            mFrameBuffer = 0
        }
    }

    interface OnSurfaceTextureCreatedListener {
        fun onSurfaceTextureCreated(st: SurfaceTexture?)
    }
}
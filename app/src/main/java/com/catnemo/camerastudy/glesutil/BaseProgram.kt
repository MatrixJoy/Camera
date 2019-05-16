package com.catnemo.camerastudy.glesutil

import android.opengl.GLES11Ext
import android.opengl.GLES30
import android.util.Log
import catnemo.top.airhockey.util.TextResourceReader
import com.catnemo.camerastudy.R
import com.catnemo.camerastudy.ZApplication

/**
 * 处理着色器的基类
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/03/23
 *
 */
open class BaseProgram
/**
 * 默认构造函数
 */(vertexShader: Int, fragmentShader: Int) {
    protected var mProgramHandle = 0
    protected var mAPositionLocation = 0
    protected var mATextureCoordLocation = 0
    protected var mUOESTextureLocation = 0
    protected var mUMVPMatrixLocation = 0
    protected var mUTextMatrixLocation = 0

    protected var mTextureTarget = GLES30.GL_TEXTURE_2D

    companion object {
        const val TAG = "BaseProgram"
        const val A_POSITION = "aPosition"
        const val A_TEXTURECOORD = "aTextureCoord"
        const val U_MVPMATRIX = "uMVPMatrix"
        const val U_TEXTMATRIX = "uTexMatrix"
        const val U_TEXTURE_OES = "sTexture" // 纹理
    }

    init {
        mProgramHandle = GlUtil.createdProgram(TextResourceReader.readTextFileFromResource(ZApplication.sContext, vertexShader),
                TextResourceReader.readTextFileFromResource(ZApplication.sContext, fragmentShader))
        if (mProgramHandle == 0) {
            throw RuntimeException("Unable to create mProgram")
        }
        Log.d(TAG, "Created Program $mProgramHandle")
        mAPositionLocation = GLES30.glGetAttribLocation(mProgramHandle, A_POSITION)
        GlUtil.checkLocation(mAPositionLocation, A_POSITION)
        mATextureCoordLocation = GLES30.glGetAttribLocation(mProgramHandle, A_TEXTURECOORD)
        GlUtil.checkLocation(mATextureCoordLocation, A_TEXTURECOORD)
        mUMVPMatrixLocation = GLES30.glGetUniformLocation(mProgramHandle, U_MVPMATRIX)
        GlUtil.checkLocation(mUMVPMatrixLocation, U_MVPMATRIX)
        mUTextMatrixLocation = GLES30.glGetUniformLocation(mProgramHandle, U_TEXTMATRIX)
        GlUtil.checkLocation(mUMVPMatrixLocation, U_TEXTMATRIX)
        mUOESTextureLocation = GLES30.glGetUniformLocation(mProgramHandle, U_TEXTURE_OES)
        GlUtil.checkLocation(mUOESTextureLocation, U_TEXTURE_OES)
    }

    fun release() {
        Log.d(TAG, "deleting mProgram $mProgramHandle")
        GLES30.glDeleteProgram(mProgramHandle)
        mProgramHandle = -1
    }

    fun createTextureObject(): Int {
        val textureIds = IntArray(1)
        GLES30.glGenTextures(1, textureIds, 0)
        GlUtil.checkGlError("glGenTextures")
        val textureId = textureIds[0]
        GLES30.glBindTexture(mTextureTarget, textureId)
        GlUtil.checkGlError("glBindTexture $textureId")
        GLES30.glTexParameteri(mTextureTarget, GLES30.GL_TEXTURE_MIN_FILTER,
                GLES30.GL_NEAREST)
        GLES30.glTexParameteri(mTextureTarget, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR)
        GLES30.glTexParameteri(mTextureTarget, GLES30.GL_TEXTURE_WRAP_S,
                GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(mTextureTarget, GLES30.GL_TEXTURE_WRAP_T,
                GLES30.GL_CLAMP_TO_EDGE)
        GlUtil.checkGlError("glTexParameter")
        return textureId
    }

    open fun draw(mvpMatrix: FloatArray, textureId: Int, texMatrix: FloatArray, vertexArray: VertexArray) {
        GlUtil.checkGlError("draw start")

        // USE mProgram
        GLES30.glUseProgram(mProgramHandle)
        GlUtil.checkGlError("glUseProgram")

        // set texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(mTextureTarget, textureId)
        GLES30.glUniform1i(mUOESTextureLocation, 0)

        // Copy the model / view / projection matrix over.
        GLES30.glUniformMatrix4fv(mUMVPMatrixLocation, 1, false, mvpMatrix, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")

        // Copy the texture transformation matrix over.
        GLES30.glUniformMatrix4fv(mUTextMatrixLocation, 1, false, texMatrix, 0)
        GlUtil.checkGlError("glUniformMatrix4fv")

        // Enable the "aPosition" vertex attribute.
        GLES30.glEnableVertexAttribArray(mAPositionLocation)
        GlUtil.checkGlError("glEnableVertexAttribArray")

        // Connect vertexBuffer to "aPosition".
        GLES30.glVertexAttribPointer(mAPositionLocation, vertexArray.mCoordsPerVertex, GLES30.GL_FLOAT, false,
                vertexArray.mVertexStride, vertexArray.vertexFb)
        GlUtil.checkGlError("glVertexAttribPointer")

        // Enable the "aTextureCoord" vertex attribute.
        GLES30.glEnableVertexAttribArray(mATextureCoordLocation)
        GlUtil.checkGlError("glEnableVertexAttribArray")

        // Connect texBuffer to "aTextureCoord".
        GLES30.glVertexAttribPointer(mATextureCoordLocation, vertexArray.mCoordsPerVertex, GLES30.GL_FLOAT, false,
                vertexArray.mTexCoordStride, vertexArray.texVertexFb)
        GlUtil.checkGlError("glVertexAttribPointer")

        // Draw the rect.
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, vertexArray.mVertexCount)
        GlUtil.checkGlError("glDrawArrays")

        // Done -- disable vertex array, texture, and mProgram.
        GLES30.glDisableVertexAttribArray(mAPositionLocation)
        GLES30.glDisableVertexAttribArray(mATextureCoordLocation)
        GLES30.glBindTexture(mTextureTarget, 0)
        GLES30.glUseProgram(0)
    }

}
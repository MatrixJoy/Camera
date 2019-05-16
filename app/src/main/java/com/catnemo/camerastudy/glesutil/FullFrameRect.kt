package com.catnemo.camerastudy.glesutil

import android.opengl.Matrix


/**
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/03/28
 *
 */
class FullFrameRect(var mProgram: BaseProgram?) {
    private val mVertexArray = VertexArray()
    var mOritation: Int = 0
    fun changeProgram(program: BaseProgram) {
        mProgram?.release()
        mProgram = program
    }

    fun changeOration(oration: Int) {
        mOritation = oration
    }

    fun createOESTextureObject(): Int? {
        return mProgram?.createTextureObject()
    }

     var mModeMtx = FloatArray(16)

    init {
        Matrix.setIdentityM(mModeMtx, 0)
    }

    fun drawFrame(textueId: Int, texturMatrix: FloatArray) {
        mProgram?.draw(mModeMtx, textueId, texturMatrix, mVertexArray)
    }

    fun release(doEglCleanup: Boolean) {
        if (doEglCleanup) {
            mProgram?.release()
        }
        mProgram = null
    }

}
package com.catnemo.zfilter.filter

import android.graphics.BitmapFactory
import android.opengl.GLES30
import android.opengl.GLUtils
import com.catnemo.zfilter.FilterDrawer
import com.catnemo.zfilter.R
import com.catnemo.zfilter.TextureInfo
import com.catnemo.zfilter.util.GlUtil

/**
 * @description
 * @author  matrixJoy
 * @date    2019-12-16   13:25
 */
class LutFilter : TwoInputTextureFilter {

    companion object {
        const val U_INTENSITY = "intensity"
    }

    private var intensityLocation = 0

    var intensity = 1.0f

    constructor() : super(readShaderFromRaw(R.raw.fragment_lut))

    private var resId = 0
    override fun onInit() {
        super.onInit()
        intensityLocation = GLES30.glGetUniformLocation(program, U_INTENSITY)
        if (this.resId != 0) {
            val tempId = resId
            resId = 0
            initLut(tempId)
        }
    }


    fun initLut(resId: Int) {
        if (!isInit) {
            this.resId = resId
            return
        }

        if (resId == this.resId) {
            return
        }
        this.resId = resId
        val lutBitmap = BitmapFactory.decodeResource(FilterDrawer.sContext?.resources, resId)
        if (textureInfo == null) {
            textureInfo = TextureInfo(GlUtil.createTextureObject(GLES30.GL_TEXTURE_2D))
            GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, lutBitmap, 0)
        } else {
            textureInfo?.let {
                GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, it.textureId)
                GLUtils.texSubImage2D(GLES30.GL_TEXTURE_2D, 0, 0, 0, lutBitmap)
            }
        }
        lutBitmap.recycle()

    }

    override fun onDestroy() {
        destroy()
    }

    private fun destroy() {
        textureInfo?.apply {
            GLES30.glDeleteTextures(1, intArrayOf(textureId), 0)
        }
        textureInfo = null
    }

    override fun preOnDraw() {
        super.preOnDraw()
        GLES30.glUniform1f(intensityLocation, intensity)
    }

}
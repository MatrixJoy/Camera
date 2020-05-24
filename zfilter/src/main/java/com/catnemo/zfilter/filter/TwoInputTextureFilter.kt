package com.catnemo.zfilter.filter

import android.opengl.GLES30
import com.catnemo.zfilter.R
import com.catnemo.zfilter.TextureInfo
import com.catnemo.zfilter.VertexArray
import com.catnemo.zfilter.util.GlUtil

/**
 * @description 双输入纹理的filter
 * @author  franticzhou
 * @date    2019-12-16   12:54
 */
open class TwoInputTextureFilter : BaseFilter {
    companion object {
        const val U_TEXTURE_2 = "sTexture2"
        const val A_TEXTURE_CORDS_2 = "aTextureCords2"

    }

    protected var uTexture2Location = 0
    protected var aTextureCords2Location = 0

    var textureInfo: TextureInfo? = null

    protected open var vertexArray: VertexArray = VertexArray()

    constructor(fragmentShader: String) : super(readShaderFromRaw(R.raw.two_input_vertex_shader), fragmentShader)

    override fun onInit() {
        uTexture2Location = GLES30.glGetUniformLocation(program, U_TEXTURE_2)
        GlUtil.checkLocation(uTexture2Location, "onInit uTexture2Location")

        aTextureCords2Location = GLES30.glGetAttribLocation(program, A_TEXTURE_CORDS_2)
        GlUtil.checkLocation(aTextureCords2Location, "onInit aTextureCords2Location")
    }

    override fun preOnDraw() {

        if (textureInfo == null) {
            return
        }

        textureInfo?.apply {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE1)
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureId)
            GLES30.glUniform1i(uTexture2Location, 1)
            GlUtil.checkGlError("use texture1")

        }

        GLES30.glEnableVertexAttribArray(aTextureCords2Location)
        GLES30.glVertexAttribPointer(aTextureCords2Location, vertexArray.perCordsCount, GLES30.GL_FLOAT, false, vertexArray.textCubeStride, vertexArray.textureCubeBuffer)
        GlUtil.checkGlError("use glVertexAttribPointer2")

    }

    override fun afterOnDraw() {
        GLES30.glDisableVertexAttribArray(aTextureCords2Location)
    }
}
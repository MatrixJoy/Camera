package com.catnemo.zfilter

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLES30
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.core.util.set
import com.catnemo.zfilter.filter.BaseFilter
import com.catnemo.zfilter.filter.BlurScreenFilter
import com.catnemo.zfilter.filter.LutFilter
import com.catnemo.zfilter.util.GlUtil
import java.lang.RuntimeException

/**
 * @description 滤镜入口
 * @author  franticzhou
 * @date    2019-12-12   13:59
 */
@SuppressLint("StaticFieldLeak")
class FilterDrawer private constructor() {


    companion object {
        var sContext: Context? = null
        var sInstance: FilterDrawer? = null
            private set
            get() {
                if (sContext == null) {
                    throw RuntimeException("must init context first")
                }
                if (field == null) {
                    field = FilterDrawer()
                }
                return field
            }
    }

    /**
     * 顶点坐标
     */
    private val vertexArray = VertexArray()


    private var width: Int = 0
    private var height: Int = 0

    private val filterLayers = ArrayList<BaseFilter?>()

    private val fboInfos = SparseArray<FboInfo?>()


    fun renderSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }


    fun updateEffectFilter(baseFilter: BaseFilter?) {
        if (filterLayers.isEmpty() || filterLayers[0] is LutFilter) {
            if (baseFilter != null) {
                filterLayers.add(0, baseFilter)
            }
        } else {
            filterLayers[0]?.release()
            if (baseFilter == null) {
                filterLayers.removeAt(0)
            } else {
                filterLayers[0] = baseFilter
            }
        }
    }

    fun updateLutFilter(lut: Int) {
        var lutFilter: LutFilter? = null
        for (filter in filterLayers) {
            if (filter is LutFilter) {
                lutFilter = filter
                break
            }
        }

        if (lut == 0 && lutFilter != null) {
            lutFilter.release()
            filterLayers.remove(lutFilter)
            return
        }

        if (lutFilter == null && lut != 0) {
            lutFilter = LutFilter()
            filterLayers.add(lutFilter)
        }
        lutFilter?.initLut(lut)
    }

    fun updateLutProgess(process: Float) {
        var lutFilter: LutFilter? = null
        for (filter in filterLayers) {
            if (filter is LutFilter) {
                lutFilter = filter
                break
            }
        }
        lutFilter?.intensity = process
    }

    fun process(textureInfo: TextureInfo): Int {
        if (filterLayers.isEmpty()) {
            return textureInfo.textureId
        }
        for (filter in filterLayers) {
            filter?.initIfNeed()

            filter?.onSurfaceSizeChanged(width, height)
        }
        for (i in 0 until filterLayers.size) {
            if (fboInfos[i] == null) {
                fboInfos[i] = initFboInfo()
            }
            fboInfos[i]?.apply {
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fboId)
                GlUtil.checkGlError("process glGenFramebuffers")
                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, fboTextureId, 0)
                GlUtil.checkGlError("process glFramebufferTexture2D")

                GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
                GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
                GLES30.glViewport(0, 0, width, height)

                filterLayers[i]?.draw(textureInfo, vertexArray)

                GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_COLOR_ATTACHMENT0, GLES30.GL_TEXTURE_2D, 0, 0)
                GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
                textureInfo.textureId = fboTextureId
                textureInfo.textMatrix = GlUtil.IDENTITY_MATRIX
            }
        }

        return textureInfo.textureId
    }


    private fun initFboInfo(): FboInfo {
        val fboArray = IntArray(1)
        GLES30.glGenFramebuffers(1, fboArray, 0)
        val fbTextureId = GlUtil.createTextureObject(GLES30.GL_TEXTURE_2D)
        GlUtil.checkGlError("initFboInfo glGenFramebuffers")
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, width, height, 0, GLES30.GL_RGBA,
                GLES30.GL_UNSIGNED_BYTE, null)
        GlUtil.checkGlError("initFboInfo glTexImage2D")

        return FboInfo(fboArray[0], fbTextureId)
    }

    fun destroy() {
        for (i in 0 until filterLayers.size) {
            filterLayers[i]?.release()
        }
        fboInfos.forEach { key, value ->
            value?.apply {
                GLES30.glDeleteFramebuffers(1, intArrayOf(fboId), 0)
                GLES30.glDeleteTextures(1, intArrayOf(fboTextureId), 0)
            }
        }
        fboInfos.clear()
    }
}
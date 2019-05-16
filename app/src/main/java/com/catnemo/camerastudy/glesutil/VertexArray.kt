package com.catnemo.camerastudy.glesutil

import android.opengl.GLES11Ext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/03/28
 *
 */
class VertexArray {

    private val SIZEOF_FLOAT = 4 // float 的byte大小

    /**
     * 顶点坐标
     */
    private val FULL_RECTANGLE_COORDS = floatArrayOf(
            -1.0f, -1.0f, // 0 bottom left
            1.0f, -1.0f,  // 1 bottom right
            -1.0f, 1.0f, // 2 top left
            1.0f, 1.0f  // 3 top right
    )

    /**
     * 纹理坐标
     */
    private val FULL_RECTANGLE_TEX_COORDS = floatArrayOf(
            0.0f, 0.0f, // 0 bottom left
            1.0f, 0.0f, // 1 bottom right
            0.0f, 1.0f, // 2 top left
            1.0f, 1.0f // 3 top right

    )



    // 顶点 fb
    private val FULL_RECTANGLE_BUF = createFloatBuffer(FULL_RECTANGLE_COORDS)
    // 纹理 fb
    private val FULL_RECTANGLE_TEX_BUF = createFloatBuffer(FULL_RECTANGLE_TEX_COORDS)

    var vertexFb: FloatBuffer = FULL_RECTANGLE_BUF

    var texVertexFb: FloatBuffer = FULL_RECTANGLE_TEX_BUF

    var mCoordsPerVertex = 2 // 每一个坐标个数 x,y

    var mVertexCount = FULL_RECTANGLE_COORDS.size / mCoordsPerVertex // 总顶点个数

    var mVertexStride = mCoordsPerVertex * SIZEOF_FLOAT // 顶点坐标位宽 可以是 0 或者 坐标个数的size

    var mTexCoordStride = mCoordsPerVertex * SIZEOF_FLOAT // 纹理坐标的位宽

    private fun createFloatBuffer(cords: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(cords.size * SIZEOF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fb.put(cords)
        fb.position(0)
        return fb
    }
}
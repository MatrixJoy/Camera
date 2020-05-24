package com.catnemo.zfilter

import com.catnemo.zfilter.util.GlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @description 顶点坐标处理类
 * @author  matrixJoy
 * @date    2019-12-13   13:18
 */
class VertexArray {

    companion object {
        // 每个浮点数所占字节
        private const val SIZE_OF_FLOAT = 4

        /**
         * 顶点坐标
         */
        private val FULL_RECTANGLE_CORDS = floatArrayOf(
                -1.0f, -1.0f, // 0 bottom left
                1.0f, -1.0f,  // 1 bottom right
                -1.0f, 1.0f, // 2 top left
                1.0f, 1.0f  // 3 top right
        )

        /**
         * 纹理坐标
         */
        private val FULL_RECTANGLE_TEX_CORDS = floatArrayOf(
                0.0f, 0.0f, // 0 bottom left
                1.0f, 0.0f, // 1 bottom right
                0.0f, 1.0f, // 2 top left
                1.0f, 1.0f // 3 top right

        )
    }

    /**
     * 顶点坐标buffer
     */
    var vertexCubeBuffer = createFloatBuffer(FULL_RECTANGLE_CORDS)

    /**
     * 纹理坐标buffer
     */
    var textureCubeBuffer = createFloatBuffer(FULL_RECTANGLE_TEX_CORDS)


    /**
     * 顶点坐标个数
     */
    var perCordsCount = 2
        private set
    /**
     * 纹理顶点个数
     */
    var cubeCount = FULL_RECTANGLE_CORDS.size / perCordsCount
        private set

    /**
     * 步长
     */
    var vertexCubeStride = perCordsCount * SIZE_OF_FLOAT

    /**
     * 步长
     */
    var textCubeStride = perCordsCount * SIZE_OF_FLOAT

    var mvpMatrix = GlUtil.IDENTITY_MATRIX

    private fun createFloatBuffer(cords: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(cords.size * SIZE_OF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fb.put(cords)
        fb.position(0)
        return fb
    }
}
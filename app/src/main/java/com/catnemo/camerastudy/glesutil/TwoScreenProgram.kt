package com.catnemo.camerastudy.glesutil

import com.catnemo.camerastudy.R

/**
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/04/02
 *
 */
class TwoScreenProgram : BaseProgram(R.raw.vertex_shader, R.raw.fragment_shader_two_screen_oes) {

    override fun draw(mvpMatrix: FloatArray, textureId: Int, texMatrix: FloatArray, vertexArray: VertexArray) {
        super.draw(mvpMatrix, textureId, texMatrix, vertexArray)
    }
}
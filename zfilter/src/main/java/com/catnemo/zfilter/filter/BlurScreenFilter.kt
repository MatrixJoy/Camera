package com.catnemo.zfilter.filter

import android.opengl.GLES30
import android.util.Log
import com.catnemo.zfilter.R

/**
 * @description 三屏特效
 * @author  franticzhou
 * @date    2019-12-13   19:32
 */
class BlurScreenFilter : BaseFilter {
    private var transformLocation = 0
    private var aspectRatioLocation = 0
    private var angleLocation = 0

    constructor() : super(readShaderFromRaw(R.raw.defualt_vertex_shader), readShaderFromRaw(R.raw.fragment_blur))

    override fun onInit() {
        super.onInit()
        transformLocation = GLES30.glGetUniformLocation(program, "transform")
        aspectRatioLocation = GLES30.glGetUniformLocation(program, "aspectRatio")
        angleLocation = GLES30.glGetUniformLocation(program, "angle")
    }

    override fun preOnDraw() {
        setUniform1f(aspectRatioLocation, surfaceWidth * 1.0f / surfaceHeight)
    }

    fun setAngle(angle: Float) {
        setUniform1f(angleLocation, angle)
    }

    fun transForm(x: Float, y: Float) {
        setUniform2fv(transformLocation, floatArrayOf(x, y))
    }
}
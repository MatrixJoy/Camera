package com.catnemo.zfilter.filter

import android.opengl.GLES30
import com.catnemo.zfilter.FilterDrawer
import com.catnemo.zfilter.R
import com.catnemo.zfilter.TextureInfo
import com.catnemo.zfilter.VertexArray
import com.catnemo.zfilter.util.GlUtil
import com.catnemo.zfilter.util.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.Exception
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.util.*

/**
 * @description 滤镜基础类
 * @author  matrixJoy
 * @date    2019-12-12   19:13
 */
open class BaseFilter {
    companion object {
        val TAG = BaseFilter::class.java.simpleName
        const val U_MVP_MATRIX = "uMVPMatrix"
        const val U_TEX_MATRIX = "uTexMatrix"
        const val A_POSITION = "aPosition"
        const val A_TEXTURE_COORD = "aTextureCoord"
        const val U_TEXTURE = "sTexture"

        /**
         * 从raw文件里读取shader文件
         */
        fun readShaderFromRaw(resId: Int): String {
            FilterDrawer.sContext?.apply {
                return try {
                    val inputStream = resources.openRawResource(resId)
                    val inputReader = BufferedReader(InputStreamReader(inputStream))
                    var line = inputReader.readLine()
                    val sb = StringBuilder()
                    while (line != null) {
                        sb.append(line)
                                .append("\n")
                        line = inputReader.readLine()
                    }
                    sb.toString()
                } catch (e: Exception) {
                    ""
                }
            }
            return ""
        }
    }

    protected var program: Int = 0
    protected var uMvpMatrixLocation = 0
    protected var uTexMatrixLocation = 0
    protected var aPositionLocation = 0
    protected var aTextureCordLocation = 0
    protected var uTextureLocation = 0

    protected var vertexShader = ""
    protected var fragmentShader = ""


    var isInit = false
        private set
    protected var runOnDraw: LinkedList<Runnable> = LinkedList()

    protected var surfaceWidth = 0
    protected var surfaceHeight = 0

    constructor() : this(readShaderFromRaw(R.raw.defualt_vertex_shader), readShaderFromRaw(R.raw.defualt_fragment_shader))


    constructor(vertexShader: String, fragmentShader: String) {
        this.vertexShader = vertexShader
        this.fragmentShader = fragmentShader
    }

    private fun doInit() {

        onInit()
        onInitialized()
        Logger.d(TAG, "init filter success")
    }

    open fun onInit() {
        program = GlUtil.createdProgram(vertexShader, fragmentShader)
        if (program == 0) {
            throw RuntimeException("unable to create program")
        }
        uMvpMatrixLocation = GLES30.glGetUniformLocation(program, U_MVP_MATRIX)
        GlUtil.checkLocation(uMvpMatrixLocation, U_MVP_MATRIX)

        uTexMatrixLocation = GLES30.glGetUniformLocation(program, U_TEX_MATRIX)
        GlUtil.checkLocation(uTexMatrixLocation, U_TEX_MATRIX)

        aPositionLocation = GLES30.glGetAttribLocation(program, A_POSITION)
        GlUtil.checkLocation(aPositionLocation, A_POSITION)

        aTextureCordLocation = GLES30.glGetAttribLocation(program, A_TEXTURE_COORD)
        GlUtil.checkLocation(aTextureCordLocation, A_TEXTURE_COORD)

        uTextureLocation = GLES30.glGetUniformLocation(program, U_TEXTURE)
        GlUtil.checkLocation(uTextureLocation, U_TEXTURE)
        isInit = true

    }

    open fun onInitialized() {

    }

    fun initIfNeed() {
        if (!isInit) {
            doInit()
        }
    }

    open fun onDestroy() {

    }

    fun onSurfaceSizeChanged(width: Int, height: Int) {
        surfaceHeight = height
        surfaceWidth = width
    }

    fun release() {
        isInit = false
        GLES30.glDeleteProgram(program)
        onDestroy()
    }

    fun draw(textureInfo: TextureInfo, vertexArray: VertexArray) {
        GlUtil.checkGlError("draw start")

        GLES30.glUseProgram(program)
        GlUtil.checkGlError("glUseProgram")
        runPendingOnDrawTasks()
        if (!isInit) {
            return
        }

        // 激活绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, textureInfo.textureId)
        GLES30.glUniform1i(uTextureLocation, 0)
        GlUtil.checkGlError("use texture")


        // 传参顶点转换矩阵
        GLES30.glUniformMatrix4fv(uMvpMatrixLocation, 1, false, vertexArray.mvpMatrix, 0)
        GlUtil.checkGlError("use uMvpMatrixLocation")

        // 传参纹理转换矩阵
        GLES30.glUniformMatrix4fv(uTexMatrixLocation, 1, false, textureInfo.textMatrix, 0)
        GlUtil.checkGlError("use uTexMatrixLocation")
        // 顶点坐标
        GLES30.glEnableVertexAttribArray(aPositionLocation)
        GLES30.glVertexAttribPointer(aPositionLocation, vertexArray.perCordsCount, GLES30.GL_FLOAT, false,
                vertexArray.vertexCubeStride, vertexArray.vertexCubeBuffer)
        GlUtil.checkGlError("aPositionLocation glEnableVertexAttribArray ")
        // 纹理坐标
        GLES30.glEnableVertexAttribArray(aTextureCordLocation)
        GLES30.glVertexAttribPointer(aTextureCordLocation, vertexArray.perCordsCount, GLES30.GL_FLOAT, false,
                vertexArray.textCubeStride, vertexArray.textureCubeBuffer)
        GlUtil.checkGlError("aTextureCordLocation glEnableVertexAttribArray ")

        preOnDraw()
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, vertexArray.cubeCount)

        GLES30.glDisableVertexAttribArray(aPositionLocation)
        GLES30.glDisableVertexAttribArray(aTextureCordLocation)
        afterOnDraw()
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0)

        GLES30.glUseProgram(0)
    }

    open fun afterOnDraw() {

    }

    open fun preOnDraw() {

    }

    open fun runPendingOnDrawTasks() {
        synchronized(runOnDraw) {
            while (!runOnDraw.isEmpty()) {
                runOnDraw.removeFirst().run()
            }
        }
    }

    open fun setUniform2fv(location: Int, vec2: FloatArray) {
        runOnDraw.add(Runnable {
            initIfNeed()
            GLES30.glUniform2fv(location, 1, vec2, 0)
        })
    }

    open fun setUniform1f(location: Int, f: Float) {
        runOnDraw.add(Runnable {
            initIfNeed()
            GLES30.glUniform1f(location, f)
        })
    }

}
package com.catnemo.camerastudy.glesutil

import android.opengl.GLES30
import android.opengl.Matrix
import android.util.Log

/**
 *
 * @author zhoujunjiang
 * @version V1.0
 * @since 2019/05/08
 *
 */
object GlUtil {
    private const val TAG = "GlUtil"
     val IDENTITY_MATRIX = FloatArray(16)

    init {
        Matrix.setIdentityM(IDENTITY_MATRIX, 0)
    }

    fun createdProgram(vertexSource: String, frgamentSource: String): Int {
        val vertexShader = createdVertexShader(vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val fragmentShader = createdFragmentShader(frgamentSource)
        if (fragmentShader == 0) {
            return 0
        }
        val program = linkProgram(vertexShader, fragmentShader)
        if (program == 0) {
            return 0
        }

        return program
    }

    fun createdFragmentShader(source: String): Int {
        return createShader(GLES30.GL_FRAGMENT_SHADER, source)
    }

    fun createdVertexShader(source: String): Int {
        return createShader(GLES30.GL_VERTEX_SHADER, source)
    }


    private fun createShader(type: Int, source: String): Int {
        val shaderId = GLES30.glCreateShader(type)
        checkGlError("glCreateShader ${type}=")
        if (shaderId == 0) {
            Log.e(TAG, "create shader fail")
            return 0
        }
        GLES30.glShaderSource(shaderId, source)
        GLES30.glCompileShader(shaderId)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shaderId, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "Compiled shader $type fail")
            Log.e(TAG, GLES30.glGetShaderInfoLog(shaderId))
            GLES30.glDeleteShader(shaderId)
            return 0
        }
        return shaderId
    }


    fun linkProgram(vertexShader: Int, fragmentShader: Int): Int {
        val programId = GLES30.glCreateProgram()
        checkGlError("created program")
        if (programId == 0) {
            Log.e(TAG, "create program fail")
            return 0
        }
        GLES30.glAttachShader(programId, vertexShader)
        checkGlError("glAttachShader")
        GLES30.glAttachShader(programId, fragmentShader)
        checkGlError("glAttachShader")
        GLES30.glLinkProgram(programId)
        val linkStatus = IntArray(1)
        GLES30.glGetProgramiv(programId, GLES30.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == GLES30.GL_FALSE) {
            Log.e(TAG, "Linked program fail")
            Log.e(TAG, GLES30.glGetProgramInfoLog(programId))
            GLES30.glDeleteProgram(programId)
            return 0
        }
        return programId
    }

    /**
     * check error
     * because opengl not throw any exception，so we also can not throw any exception
     */
    fun checkGlError(msg: String) {
        val error = GLES30.glGetError()
        if (error != GLES30.GL_NO_ERROR) {
            Log.e(TAG, "$msg :glError 0x${Integer.toHexString(error)}")
        }
    }

    fun checkLocation(location: Int, label: String) {
        if (location < 0) {
            Log.e(TAG, "Unable to locate $label in mProgram")
        }
    }
}
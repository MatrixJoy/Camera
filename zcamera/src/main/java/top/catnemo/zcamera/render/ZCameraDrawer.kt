package top.catnemo.zcamera.render

import android.opengl.GLES11Ext
import android.opengl.GLES30
import top.catnemo.zcamera.util.GlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 2D 纹理 绘制面板
 * @author MatrixJoy
 * @version V1.0
 * @since 2019/05/10
 *
 */
class ZCameraDrawer(textureTarget: Int) {
    private val SIZEOF_FLOAT = 4
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

    // 默认 OES 类型
    var mTextureTarget = 0

    var mMVPMatrix = GlUtil.IDENTITY_MATRIX

    private fun createFloatBuffer(cords: FloatArray): FloatBuffer {
        val fb = ByteBuffer.allocateDirect(cords.size * SIZEOF_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        fb.put(cords)
        fb.position(0)
        return fb
    }

    private val VERTEX_SHADER = "#version 300 es\n" +
            "uniform mat4 uMVPMatrix; // 投影矩阵\n" +
            "uniform mat4 uTexMatrix; // 纹理矩阵\n" +
            "\n" +
            "in vec4 aPosition; // 顶点坐标\n" +
            "in vec4 aTextureCord; // 纹理坐标\n" +
            "\n" +
            "out vec2 vTextureCord;\n" +
            "\n" +
            "void main() {\n" +
            "     gl_Position = uMVPMatrix * aPosition;\n" +
            "     vTextureCord = (uTexMatrix * aTextureCord).xy;\n" +
            "}"

    private val FRAGMENT_SHADER = "#version 300 es\n" +
            "#extension GL_OES_EGL_image_external_essl3 : require // 扩展类型\n" +
            "precision mediump float; // 精度\n" +
            "in vec2 vTextureCord;\n" +
            "uniform samplerExternalOES sTexture;\n" +
            "out vec4 fragColor;\n" +
            "void main(){\n" +
            "    fragColor = texture(sTexture, vTextureCord);\n" +
            "}"

    private val FRAGMENT_SHADER_2D = "#version 300 es\n" +
            "precision mediump float; // 精度\n" +
            "in vec2 vTextureCord;\n" +
            "uniform sampler2D sTexture;\n" +
            "out vec4 fragColor;\n" +
            "void main(){\n" +
            "    fragColor = texture(sTexture, vTextureCord);\n" +
            "}"


    private var mProgram = 0

    private var mUMVPMatrixLocation = 0
    private var mUTexMatrixLocation = 0
    private var mAPositionLocation = 0
    private var mATextureCordLocation = 0
    private var mSTextureLocation = 0

    companion object {
        const val TEXTURE_OES = 0
        const val TEXTURE_2D = 1
    }

    init {
        if (textureTarget == TEXTURE_OES) {
            mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
            mProgram = GlUtil.createdProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        } else {
            mTextureTarget = GLES30.GL_TEXTURE_2D
            mProgram = GlUtil.createdProgram(VERTEX_SHADER, FRAGMENT_SHADER_2D)
        }
        mUMVPMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uMVPMatrix")
        mUTexMatrixLocation = GLES30.glGetUniformLocation(mProgram, "uTexMatrix")
        mAPositionLocation = GLES30.glGetAttribLocation(mProgram, "aPosition")
        mATextureCordLocation = GLES30.glGetAttribLocation(mProgram, "aTextureCord")
        mSTextureLocation = GLES30.glGetUniformLocation(mProgram, "sTexture")
    }

    fun draw(textureId: Int, stMtx: FloatArray) {
        GLES30.glUseProgram(mProgram)

        // set texture
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(mTextureTarget, textureId)
        GLES30.glUniform1i(mSTextureLocation, 0)

        GLES30.glVertexAttribPointer(mAPositionLocation, 2, GLES30.GL_FLOAT,
                false, 0, FULL_RECTANGLE_BUF)
        GLES30.glEnableVertexAttribArray(mAPositionLocation)


        GLES30.glVertexAttribPointer(mATextureCordLocation, 2, GLES30.GL_FLOAT,
                false, 0, FULL_RECTANGLE_TEX_BUF)
        GLES30.glEnableVertexAttribArray(mATextureCordLocation)
        GLES30.glUniformMatrix4fv(mUMVPMatrixLocation, 1, false, mMVPMatrix, 0)
        GLES30.glUniformMatrix4fv(mUTexMatrixLocation, 1, false, stMtx, 0)

        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, FULL_RECTANGLE_COORDS.size / 2)

        GLES30.glDisableVertexAttribArray(mAPositionLocation)
        GLES30.glDisableVertexAttribArray(mATextureCordLocation)
        GLES30.glBindTexture(mTextureTarget, 0)
        GLES30.glUseProgram(0)
    }

    fun release() {
        if (mProgram != 0) {
            GLES30.glDeleteProgram(mProgram)
            mProgram = 0
        }

    }

}
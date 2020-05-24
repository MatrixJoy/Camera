package top.catnemo.zcamera.interfazz

import android.graphics.SurfaceTexture

/**
 * interface for outer to access the texture
 * @author MatrixJoy
 * @version V1.0
 * @since 2019/05/11
 *
 */
interface IZCameraDrawer {
    /**
     * after onDrawFrame
     * we can handle this texture by we want,maybe encoder or others
     * @notice this method call in GLThread
     */
    fun afterDrawFrame(texture: SurfaceTexture?, textureId: Int)

    /**
     * before onDrawFrame
     * we can process the texture id by we want
     * @return process after texture id maybe oes or 2d
     * @notice this method call in GLThread
     */
    fun beforeOnDrawFrame(texture: SurfaceTexture?, textureId: Int): Int

    fun onDrawerInit(width: Int, height: Int)

    fun onDrawerRelease()
}
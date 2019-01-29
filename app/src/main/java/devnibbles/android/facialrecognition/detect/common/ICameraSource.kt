package devnibbles.android.facialrecognition.detect.common

import android.view.SurfaceHolder

import com.google.android.gms.common.images.Size

import java.io.IOException

interface ICameraSource {

    fun previewSize(): Size?

    fun cameraFacing(): Int

    fun release()

    @Throws(IOException::class)
    fun start(surfaceHolder: SurfaceHolder)

    fun stop()
}

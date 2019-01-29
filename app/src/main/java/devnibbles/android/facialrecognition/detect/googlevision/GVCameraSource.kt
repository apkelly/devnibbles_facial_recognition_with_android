package devnibbles.android.facialrecognition.detect.googlevision

import android.content.Context
import android.view.SurfaceHolder

import com.google.android.gms.common.images.Size
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import devnibbles.android.facialrecognition.detect.common.GraphicOverlay
import devnibbles.android.facialrecognition.detect.common.ICameraSource

import java.io.IOException

class GVCameraSource(context: Context, detector: Detector<*>) :
    ICameraSource {

    private val delegate = CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(GraphicOverlay.CAMERA_FACING_FRONT)
            .setRequestedFps(15.0f)
            .build()

    override fun previewSize(): Size? {
        return delegate.previewSize
    }

    override fun cameraFacing(): Int {
        return delegate.cameraFacing
    }

    override fun release() {
        delegate.release()
    }

    @Throws(IOException::class)
    override fun start(surfaceHolder: SurfaceHolder) {
        delegate.start(surfaceHolder)
    }

    override fun stop() {
        delegate.stop()
    }

}

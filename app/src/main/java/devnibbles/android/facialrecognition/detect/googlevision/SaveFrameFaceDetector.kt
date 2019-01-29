package devnibbles.android.facialrecognition.detect.googlevision

import android.util.SparseArray

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face

class SaveFrameFaceDetector(private val delegateDetector: Detector<Face>) : Detector<Face>() {
    var lastFrame: Frame? = null

    override fun detect(frame: Frame): SparseArray<Face> {
        lastFrame = frame

        return delegateDetector.detect(frame)
    }

    override fun isOperational(): Boolean {
        return delegateDetector.isOperational
    }

    override fun setFocus(id: Int): Boolean {
        return delegateDetector.setFocus(id)
    }
}


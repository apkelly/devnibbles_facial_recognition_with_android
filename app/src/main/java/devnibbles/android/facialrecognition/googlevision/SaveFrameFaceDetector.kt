package devnibbles.android.facialrecognition.googlevision

import android.util.SparseArray

import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face

class SaveFrameFaceDetector(private val mDelegate: Detector<Face>) : Detector<Face>() {
    var lastFrame: Frame? = null

    override fun detect(frame: Frame): SparseArray<Face> {
        lastFrame = frame

        return mDelegate.detect(frame)
    }

    override fun isOperational(): Boolean {
        return mDelegate.isOperational
    }

    override fun setFocus(id: Int): Boolean {
        return mDelegate.setFocus(id)
    }
}


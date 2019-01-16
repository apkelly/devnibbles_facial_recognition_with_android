package devnibbles.android.facialrecognition.googlevision

import android.graphics.PointF

import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.Landmark
import devnibbles.android.facialrecognition.common.AbstractFaceGraphic
import devnibbles.android.facialrecognition.common.GraphicOverlay


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(graphicOverlay: GraphicOverlay) : AbstractFaceGraphic(graphicOverlay) {

    private var face: Face? = null

    override fun rightEyePosition() : PointF? {
        return face?.landmarks?.firstOrNull { it.type == Landmark.RIGHT_EYE }?.position
    }

    override fun leftEyePosition() : PointF? {
        return face?.landmarks?.firstOrNull { it.type == Landmark.LEFT_EYE }?.position
    }

    fun updateFace(face: Face) {
        this.face = face
        postInvalidate()
    }
}

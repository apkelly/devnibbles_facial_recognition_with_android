package devnibbles.android.facialrecognition.detect.googlevision

import android.graphics.PointF

import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.Landmark
import devnibbles.android.facialrecognition.detect.common.AbstractFaceGraphic
import devnibbles.android.facialrecognition.detect.common.GraphicOverlay


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(faceId: Int, graphicOverlay: GraphicOverlay) : AbstractFaceGraphic(faceId, graphicOverlay) {

    private var face: Face? = null

    override fun rightEyePosition() : PointF? {
        return face?.landmarks?.firstOrNull { it.type == Landmark.RIGHT_EYE }?.position
    }

    override fun leftEyePosition() : PointF? {
        return face?.landmarks?.firstOrNull { it.type == Landmark.LEFT_EYE }?.position
    }

    override fun namePosition() : PointF? {
        return face?.landmarks?.firstOrNull { it.type == Landmark.NOSE_BASE }?.position
    }

    fun updateFace(face: Face) {
        this.face = face
        postInvalidate()
    }
}

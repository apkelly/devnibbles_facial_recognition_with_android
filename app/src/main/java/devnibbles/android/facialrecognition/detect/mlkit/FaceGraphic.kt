package devnibbles.android.facialrecognition.detect.mlkit

import android.graphics.PointF

import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import devnibbles.android.facialrecognition.detect.common.AbstractFaceGraphic
import devnibbles.android.facialrecognition.detect.common.GraphicOverlay


/**
 * Graphic instance for rendering face position, orientation, and landmarks within an associated
 * graphic overlay view.
 */
class FaceGraphic(faceId: Int, graphicOverlay: GraphicOverlay) :
    AbstractFaceGraphic(faceId, graphicOverlay) {

    private var face: FirebaseVisionFace? = null

    override fun leftEyePosition(): PointF? {
        return PointF(
            face?.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)?.position?.x ?: 0f,
            face?.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)?.position?.y ?: 0f
        )
    }

    override fun rightEyePosition(): PointF? {
        return PointF(
            face?.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)?.position?.x ?: 0f,
            face?.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)?.position?.y ?: 0f
        )
    }

    override fun namePosition(): PointF? {
        return PointF(
            face?.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)?.position?.x ?: 0f,
            face?.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)?.position?.y ?: 0f
        )
    }

    fun updateFace(face: FirebaseVisionFace) {
        this.face = face
        postInvalidate()
    }
}


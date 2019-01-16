package devnibbles.android.facialrecognition.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF


abstract class AbstractFaceGraphic(graphicOverlay: GraphicOverlay) : GraphicOverlay.Graphic(graphicOverlay) {

    companion object {
        private const val FACE_POSITION_RADIUS = 10.0f
        private const val ID_TEXT_SIZE = 40.0f
    }

    private val mFacePositionPaint = Paint()

    abstract fun leftEyePosition(): PointF?
    abstract fun rightEyePosition(): PointF?

    var name: String? = null

    init {
        mFacePositionPaint.color = Color.WHITE
        mFacePositionPaint.textSize = ID_TEXT_SIZE
        mFacePositionPaint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        System.out.println("draw : " + (leftEyePosition() != null) + " : " + (rightEyePosition() != null))
//        val face = mFace ?: return

        // Draws a circle at the position of the detected face, with the face's track id below.
//        val x = translateX(face.position.x + face.width / 2)
//        val y = translateY(face.position.y + face.height / 2)


        leftEyePosition()?.let { position ->
            canvas.drawCircle(
                translateX(position.x),
                translateY(position.y),
                FACE_POSITION_RADIUS,
                mFacePositionPaint
            )
        }

        rightEyePosition()?.let { position ->
            canvas.drawCircle(
                translateX(position.x),
                translateY(position.y),
                FACE_POSITION_RADIUS,
                mFacePositionPaint
            )
        }

//        canvas.drawText(mName!!, x, y - 100, mFacePositionPaint)
    }
}

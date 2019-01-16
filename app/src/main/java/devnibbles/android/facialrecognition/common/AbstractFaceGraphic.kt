package devnibbles.android.facialrecognition.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF


abstract class AbstractFaceGraphic(graphicOverlay: GraphicOverlay) : GraphicOverlay.Graphic(graphicOverlay) {

    companion object {
        private const val DOT_RADIUS = 10.0f
        private const val TEXT_SIZE = 40.0f
    }

    private val mFacePositionPaint = Paint()

    abstract fun leftEyePosition(): PointF?
    abstract fun rightEyePosition(): PointF?

    init {
        mFacePositionPaint.color = Color.WHITE
        mFacePositionPaint.textSize = TEXT_SIZE
        mFacePositionPaint.textAlign = Paint.Align.CENTER
    }

    override fun draw(canvas: Canvas) {
        leftEyePosition()?.let { position ->
            canvas.drawCircle(
                translateX(position.x),
                translateY(position.y),
                DOT_RADIUS,
                mFacePositionPaint
            )
        }

        rightEyePosition()?.let { position ->
            canvas.drawCircle(
                translateX(position.x),
                translateY(position.y),
                DOT_RADIUS,
                mFacePositionPaint
            )
        }
    }
}

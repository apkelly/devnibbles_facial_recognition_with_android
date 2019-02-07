package devnibbles.android.facialrecognition.detect.common

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF


abstract class AbstractFaceGraphic(faceId: Int, graphicOverlay: GraphicOverlay) :
    GraphicOverlay.Graphic(faceId, graphicOverlay) {

    companion object {
        private const val DOT_RADIUS = 10.0f
        private const val TEXT_SIZE = 40.0f
    }

    private val mFacePositionPaint = Paint()

    abstract fun leftEyePosition(): PointF?
    abstract fun rightEyePosition(): PointF?
    abstract fun namePosition(): PointF?

    private var name: String? = null

    init {
        mFacePositionPaint.color = Color.WHITE
        mFacePositionPaint.textSize = TEXT_SIZE
        mFacePositionPaint.textAlign = Paint.Align.CENTER
    }

    fun setName(name: String) {
        this.name = name
        postInvalidate()
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

        namePosition()?.let { position ->
            if (name != null) {
                canvas.drawText(
                    name!!,
                    translateX(position.x),
                    translateY(position.y),
                    mFacePositionPaint
                )
            }
        }
    }
}

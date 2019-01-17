package devnibbles.android.facialrecognition

import android.util.Log
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import devnibbles.android.facialrecognition.common.AbstractActivity
import devnibbles.android.facialrecognition.mlkit.FaceDetector
import devnibbles.android.facialrecognition.mlkit.FaceGraphic
import devnibbles.android.facialrecognition.mlkit.FrameMetadata
import devnibbles.android.facialrecognition.mlkit.MLCameraSource
import java.io.IOException
import java.nio.ByteBuffer


class MLKitActivity : AbstractActivity() {

    companion object {
        private const val TAG = "MLKitActivity"
    }

    private var mCameraSource: MLCameraSource? = null

    /**
     * Creates and starts the camera.
     */
    override fun createCameraSource() {
        mCameraSource = MLCameraSource(this, mGraphicOverlay)
        mCameraSource!!.setMachineLearningFrameProcessor(FaceDetector(object:FaceDetector.DetectorCallback {
            override fun onSuccess(frameData: ByteBuffer, results: List<FirebaseVisionFace>, frameMetadata: FrameMetadata) {
                mGraphicOverlay.clear()

                results.forEach {face ->
                    face.trackingId
                    val faceGraphic = FaceGraphic(face, mGraphicOverlay)
                    mGraphicOverlay.add(faceGraphic)
                }

                mGraphicOverlay.postInvalidate()
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }
        }))
    }

    /**
     * Starts or restarts the camera source, if it exists.
     */
    override fun startCameraSource() {
        checkGooglePlayServices()

        if (mCameraSource != null) {
            try {
                mCameraPreview.start(mCameraSource!!, mGraphicOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to start camera source.", e)
                mCameraSource!!.release()
                mCameraSource = null
            }
        }
    }

    /**
     * Releases the resources associated with the camera source.
     */
    override fun releaseCameraSource() {
        if (mCameraSource != null) {
            mCameraSource!!.release()
            mCameraSource = null
        }
    }
}

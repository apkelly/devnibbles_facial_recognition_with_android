package devnibbles.android.facialrecognition.mlkit

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions

import java.io.IOException
import java.nio.ByteBuffer

/** Face Detector Demo.  */
class FaceDetectionProcessor(private val callback : MyCallback?) : AbstractVisionProcessor<List<FirebaseVisionFace>>() {

    private val detector: FirebaseVisionFaceDetector

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build()

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
    }

    interface MyCallback {
        fun onSuccess(frameData: ByteBuffer, results: List<FirebaseVisionFace>, frameMetadata: FrameMetadata)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
            frameData: ByteBuffer,
        results: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata
    ) {
        callback?.onSuccess(frameData, results, frameMetadata)
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceDetectionProcessor"
    }
}
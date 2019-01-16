package devnibbles.android.facialrecognition.mlkit

import android.util.Log
import androidx.annotation.GuardedBy
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import java.io.IOException

import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
class FaceDetector(private val callback: DetectorCallback?):IFrameProcessor {

    interface DetectorCallback {
        fun onSuccess(frameData: ByteBuffer, results: List<FirebaseVisionFace>, frameMetadata: FrameMetadata)
        fun onFailure(exception: Exception)
    }

    companion object {
        private const val TAG = "FaceDetector"
    }

    private val delegateDetector = FirebaseVision.getInstance()
        .getVisionFaceDetector(
            FirebaseVisionFaceDetectorOptions.Builder()
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .build()
        )

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null

    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null

    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null

    @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage()
        }
    }

    @Synchronized
    private fun processLatestImage() {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage!!, processingMetaData!!)
        }
    }

    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setWidth(frameMetadata.width)
                .setHeight(frameMetadata.height)
                .setRotation(frameMetadata.rotation)
                .build()

        detectInVisionImage(
                data,
                FirebaseVisionImage.fromByteBuffer(data, metadata), frameMetadata)
    }

    private fun detectInVisionImage(
            frameData: ByteBuffer,
        image: FirebaseVisionImage,
        metadata: FrameMetadata?
    ) {
        delegateDetector.detectInImage(image)
                .addOnSuccessListener { results ->
                    callback?.onSuccess(frameData, results, metadata!!)
                    processLatestImage()
                }
                .addOnFailureListener { e -> callback?.onFailure(e) }
    }

    override fun stop() {
        try {
            delegateDetector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Detector: $e")
        }
    }

}

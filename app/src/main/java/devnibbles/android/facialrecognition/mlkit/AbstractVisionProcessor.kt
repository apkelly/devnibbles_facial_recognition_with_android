package devnibbles.android.facialrecognition.mlkit

import androidx.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata

import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class AbstractVisionProcessor<T> {

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
    fun process(
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
        detectInImage(image)
                .addOnSuccessListener { results ->
                    onSuccess(frameData, results,
                            metadata!!)
                    processLatestImage()
                }
                .addOnFailureListener { e -> onFailure(e) }
    }

    abstract fun stop()

    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>

    /**
     * Callback that executes with a successful detection result.
     *
     * image.
     */
    protected abstract fun onSuccess(
            frameData: ByteBuffer,
        results: T,
        frameMetadata: FrameMetadata
    )

    protected abstract fun onFailure(e: Exception)
}

package devnibbles.android.facialrecognition

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import devnibbles.android.facialrecognition.classify.CloudAutoMLViewModel
import devnibbles.android.facialrecognition.classify.common.*
import devnibbles.android.facialrecognition.detect.googlevision.FaceGraphic
import devnibbles.android.facialrecognition.detect.googlevision.GVCameraSource
import devnibbles.android.facialrecognition.detect.googlevision.SaveFrameFaceDetector
import java.io.IOException

class GoogleVisionActivity : AbstractActivity() {

    companion object {
        private const val TAG = "GoogleVisionActivity"
    }

    private var mCameraSource: GVCameraSource? = null
    private lateinit var mDetector: SaveFrameFaceDetector
    private lateinit var mViewModel: CloudAutoMLViewModel

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)

        mViewModel = ViewModelProviders.of(this).get(CloudAutoMLViewModel::class.java)
        mViewModel.subscribeClassifications()
            .observe(this, Observer<Resource<FaceClassification, Throwable>> { resource ->
                when (resource) {
                    is LoadingResource -> {
                        System.out.println("Classifying...")
                    }
                    is SuccessResource -> {
                        System.out.println("SuccessResource : " + resource.data)
                        val faceId = resource.data.faceId
                        val name = resource.data.name
                        val score = resource.data.confidence
                        (mGraphicOverlay.find(faceId) as? FaceGraphic)?.setName("$name ($score)")
                    }
                    is ErrorResource -> {
                        System.out.println("ErrorResource : " + resource.data)
                        resource.errorData?.printStackTrace()
                    }
                }
            })
    }

    /**
     * Creates and starts the camera.
     */
    override fun createCameraSource() {
        val context = applicationContext
        val detector = FaceDetector.Builder(context)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .build()

        mDetector = SaveFrameFaceDetector(detector)
        mDetector.setProcessor(
            MultiProcessor.Builder<Face>(GraphicFaceTrackerFactory())
                .build()
        )

        if (!mDetector.isOperational) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.")
        }

        mCameraSource = GVCameraSource(context, mDetector)
    }

    /**
     * Starts or restarts the camera source, if it exists.
     */
    override fun startCameraSource() {
        checkGooglePlayServices()

        mCameraSource?.let {
            try {
                mCameraPreview.start(it, mGraphicOverlay)
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

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private inner class GraphicFaceTrackerFactory : MultiProcessor.Factory<Face> {
        override fun create(face: Face): Tracker<Face> {
            return GraphicFaceTracker()
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private inner class GraphicFaceTracker internal constructor() : Tracker<Face>() {
        private var mFaceGraphic: FaceGraphic? = null

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face) {
            mFaceGraphic = FaceGraphic(faceId, mGraphicOverlay)
            mDetector.lastFrame?.let { frame ->
                // Lets try and find out who this face belongs to
                mViewModel.classify(faceId, frame.convertToByteArray())
            }
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        override fun onUpdate(detectionResults: Detector.Detections<Face>, face: Face) {
            mFaceGraphic?.updateFace(face)
            mGraphicOverlay.add(mFaceGraphic)
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        override fun onMissing(detectionResults: Detector.Detections<Face>) {
            mGraphicOverlay.remove(mFaceGraphic)
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        override fun onDone() {
            mGraphicOverlay.remove(mFaceGraphic)
        }
    }

}

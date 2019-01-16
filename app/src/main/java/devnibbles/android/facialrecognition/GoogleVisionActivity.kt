package devnibbles.android.facialrecognition

import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.MultiProcessor
import com.google.android.gms.vision.Tracker
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector
import devnibbles.android.facialrecognition.common.AbstractActivity
import devnibbles.android.facialrecognition.googlevision.FaceGraphic
import devnibbles.android.facialrecognition.googlevision.GVCameraSource
import devnibbles.android.facialrecognition.googlevision.SaveFrameFaceDetector
import java.io.IOException

class GoogleVisionActivity : AbstractActivity() {

    companion object {
        private const val TAG = "GoogleVisionActivity"
    }

    private var mCameraSource: GVCameraSource? = null
    private lateinit var mDetector: SaveFrameFaceDetector


    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
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
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    override fun onDestroy() {
        super.onDestroy()

        if (mCameraSource != null) {
            mCameraSource!!.release()
        }
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    override fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            applicationContext
        )
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

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

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

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
        private var mFaceGraphic:FaceGraphic? = null

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        override fun onNewItem(faceId: Int, item: Face) {
            mFaceGraphic = FaceGraphic(mGraphicOverlay)
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
//            mOverlay.remove(mFaceGraphic)
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

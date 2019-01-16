package devnibbles.android.facialrecognition

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import devnibbles.android.facialrecognition.common.AbstractActivity
import devnibbles.android.facialrecognition.common.CameraSourcePreview
import devnibbles.android.facialrecognition.common.GraphicOverlay
import devnibbles.android.facialrecognition.mlkit.FaceDetectionProcessor
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
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    override fun createCameraSource() {
        mCameraSource = MLCameraSource(this, mGraphicOverlay)
        mCameraSource!!.setMachineLearningFrameProcessor(FaceDetectionProcessor(object:FaceDetectionProcessor.MyCallback {
            override fun onSuccess(frameData: ByteBuffer, results: List<FirebaseVisionFace>, frameMetadata: FrameMetadata) {
                mGraphicOverlay.clear()

                results.forEach {face ->
                    face.trackingId
                    val faceGraphic = FaceGraphic(face, mGraphicOverlay)
                    mGraphicOverlay.add(faceGraphic)
                }

                mGraphicOverlay.postInvalidate()
            }
        }))
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

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    override fun startCameraSource() {

        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                applicationContext)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS)
            dlg.show()
        }

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

}

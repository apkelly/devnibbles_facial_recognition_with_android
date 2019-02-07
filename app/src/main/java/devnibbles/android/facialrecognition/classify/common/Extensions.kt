package devnibbles.android.facialrecognition.classify.common

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import com.google.android.gms.vision.Frame
import devnibbles.android.facialrecognition.detect.mlkit.FrameMetadata
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


fun Bitmap.convertToByteArray(): ByteArray {
    //minimum number of bytes that can be used to store this bitmap's pixels
    val size = this.byteCount

    //allocate new instances which will hold bitmap
    val buffer = ByteBuffer.allocate(size)
    val bytes = ByteArray(size)

    //copy the bitmap's pixels into the specified buffer
    this.copyPixelsToBuffer(buffer)

    //rewinds buffer (buffer position is set to zero and the mark is discarded)
    buffer.rewind()

    //transfer bytes from buffer into the given destination array
    buffer.get(bytes)

    //return bitmap's pixels
    return bytes
}

fun Frame.convertToByteArray(quality: Int = 100): ByteArray {
    val bytes = this.bitmap?.convertToByteArray() ?: this.grayscaleImageData.array()

    val yuvImage = YuvImage(bytes, this.metadata.format, this.metadata.width, this.metadata.height, null)
    val byteArrayOutputStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, this.metadata.width, this.metadata.height), quality, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun ByteBuffer.convertToByteArray(metadata: FrameMetadata): ByteArray {
    this.rewind()
    val imageInBuffer = ByteArray(this.limit())
    this.get(imageInBuffer, 0, imageInBuffer.size)

    val baos = ByteArrayOutputStream()
    baos.use { stream ->
        val image = YuvImage(imageInBuffer, ImageFormat.NV21, metadata.width, metadata.height, null)
        image.compressToJpeg(Rect(0, 0, metadata.width, metadata.height), 80, stream)
        return stream.toByteArray()
    }
}
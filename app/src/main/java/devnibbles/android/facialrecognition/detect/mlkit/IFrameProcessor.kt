package devnibbles.android.facialrecognition.detect.mlkit

import java.nio.ByteBuffer

interface IFrameProcessor {

    fun process(data: ByteBuffer, frameMetadata: FrameMetadata)

    fun stop()

}
package devnibbles.android.facialrecognition.mlkit

import java.nio.ByteBuffer

interface IFrameProcessor {

    fun process(data: ByteBuffer, frameMetadata: FrameMetadata)

    fun stop()

}
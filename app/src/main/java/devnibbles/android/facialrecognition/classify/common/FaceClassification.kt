package devnibbles.android.facialrecognition.classify.common

data class FaceClassification(
    val faceId: Int,
    val name: String,
    val confidence: Double
)
package devnibbles.android.facialrecognition.classify

import kotlinx.coroutines.Deferred
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// curl -X POST -H "Content-Type: application/json" \
// -H "Authorization: Bearer <access_token>" \
// https://automl.googleapis.com/v1beta1/projects/devnibbles/locations/us-central1/models/ICN3704829353327390855:predict -d @request.json

// Expected json response from webservice
//{
//  "payload": [
//    {
//      "classification": {
//        "score": 0.87991875
//      },
//      "displayName": "Andy"
//    }
//  ]
//}

interface CloudAutoMLService {

    @POST("/v1beta1/projects/{project}/locations/{location}/models/{model}:predict")
    fun classify(
        @Header("Authorization") authorization: String,
        @Path("project") project: String,
        @Path("location") location: String,
        @Path("model") model: String,
        @Body body: CloudAutoMLModel
    ): Deferred<CloudResponse>

    data class Score(val score: Double)
    data class Classification(val classification: Score?, val displayName: String?)
    data class CloudResponse(val payload: List<Classification>?)

}
package devnibbles.android.facialrecognition.classify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.api.client.util.Base64
import com.google.api.gax.core.FixedCredentialsProvider
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.automl.v1beta1.*
import com.google.gson.GsonBuilder
import com.google.protobuf.ByteString
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import devnibbles.android.facialrecognition.classify.common.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream
import java.nio.charset.Charset


class MainViewModel : AbstractViewModel() {

    companion object {
        private const val REST_CLASSIFIER =
            false // flag to decide if we should use REST (true) or SDK (false) classifier.

        private const val PROJECT = "devnibbles"
        private const val LOCATION = "us-central1"
        private const val MODEL = "ICN3704829353327390855"
        private const val ACCESS_TOKEN = "<insert token here>"
        private const val SERVICE_ACCOUNT_JSON = "<insert json here>"
    }

    private val mServiceCredentials = ServiceAccountCredentials
        .fromStream(ByteArrayInputStream(SERVICE_ACCOUNT_JSON.toByteArray(Charset.defaultCharset())))
        .createScoped(mutableListOf("https://www.googleapis.com/auth/cloud-platform"))

    private val errorHandler = CoroutineExceptionHandler { _, throwable ->
        mResult.postValue(ErrorResource(throwable))
    }

    private val mResult = MutableLiveData<Resource<Pair<Int, String>, Throwable>>()

    fun subscribeClassifications(): LiveData<Resource<Pair<Int, String>, Throwable>> {
        return mResult
    }

    fun classify(faceId: Int, imageBytes: ByteArray) {
        if (REST_CLASSIFIER) {
            classifyUsingRetrofit(faceId, imageBytes)

        } else {
            classifyUsingCloudSDK(faceId, imageBytes)

        }
    }

    private fun classifyUsingRetrofit(faceId: Int, imageBytes: ByteArray) {
        launch(errorHandler) {
            mResult.value = LoadingResource(null)

            val body = CloudAutoMLModel(
                Payload(
                    MlImage(
                        String(
                            Base64.encodeBase64(imageBytes)
                        )
                    )
                )
            )
            val response = getRESTService().classify(
                "Bearer $ACCESS_TOKEN",
                PROJECT, LOCATION, MODEL, body
            ).await()

            System.out.println("Response : " + response.payload?.size + " : " + response.payload?.firstOrNull()?.displayName)

            if (response.payload?.isNotEmpty() == true) {
                // We have a prediction!
                var predictedName: String? = null

                response.payload.forEach { entry ->
                    // TODO: Check that score is within a valid threshold.
                    if (entry.displayName != null) {
                        predictedName = entry.displayName
                    }
                }

                if (predictedName != null) {
                    mResult.postValue(SuccessResource(Pair(faceId, predictedName!!)))
                } else {
                    mResult.postValue(ErrorResource(null, Pair(-1, "Not recognised (001)")))
                }
            } else {
                mResult.postValue(ErrorResource(null, Pair(-1, "Not recognised (002)")))
            }
        }
    }

    private fun classifyUsingCloudSDK(faceId: Int, imageBytes: ByteArray) {
        launch(errorHandler) {
            mResult.value = LoadingResource(null)

            withContext(Dispatchers.IO) {
                val image = Image.newBuilder().setImageBytes(ByteString.copyFrom(imageBytes)).build()

                val settings = PredictionServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(mServiceCredentials)).build()
                val predictionServiceClient = PredictionServiceClient.create(settings)
                predictionServiceClient.use { client ->
                    val name = ModelName.of(PROJECT, LOCATION, MODEL)
                    val payload = ExamplePayload.newBuilder().setImage(image).build()
                    val params = HashMap<String, String>()
                    val response = client.predict(name, payload, params)

                    System.out.println("response : $response")

                    if (response.payloadCount > 0) {
                        // We have a prediction!
                        var predictedName: String? = null
                        response.getPayload(0).allFields.entries.forEach { entry ->
                            System.out.println("Entry : ${entry.key.jsonName} = ${entry.value}")

                            // TODO: Check that score is within a valid threshold.
                            if (entry.key.jsonName == "displayName") {
                                predictedName = entry.value as String
                            }
                        }

                        if (predictedName != null) {
                            mResult.postValue(SuccessResource(Pair(faceId, predictedName!!)))
                        } else {
                            mResult.postValue(ErrorResource(null, Pair(-1, "Not recognised (001)")))
                        }
                    } else {
                        mResult.postValue(ErrorResource(null, Pair(-1, "Not recognised (002)")))
                    }
                }
            }
        }
    }

    private fun getRESTService(): CloudAutoMLService {
        val gsonFactory = GsonConverterFactory
            .create(GsonBuilder().create())

        val networkClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        return Retrofit.Builder()
            .baseUrl("https://automl.googleapis.com/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(gsonFactory)
            .client(networkClient)
            .build()
            .create(CloudAutoMLService::class.java)
    }

}




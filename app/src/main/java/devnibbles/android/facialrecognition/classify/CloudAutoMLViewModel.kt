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
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayInputStream
import java.nio.charset.Charset


class MainViewModel : AbstractViewModel() {

    companion object {
        private const val PROJECT = "devnibbles"
        private const val LOCATION = "us-central1"
        private const val MODEL = "ICN3704829353327390855"
        private const val ACCESS_TOKEN = "<insert token here>"
        private const val SERVICE_ACCOUNT_JSON = "<insert json here>"
    }

    private val mServiceCredentials = ServiceAccountCredentials
        .fromStream(ByteArrayInputStream(SERVICE_ACCOUNT_JSON.toByteArray(Charset.defaultCharset())))
        .createScoped(mutableListOf("https://www.googleapis.com/auth/cloud-platform"))


    private val mResult = MutableLiveData<Resource<String, Throwable>>()

    fun subscribeClassifications(): LiveData<Resource<String, Throwable>> {
        return mResult
    }

    fun classifyUsingRetrofit(imageBytes: ByteArray) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            mResult.postValue(ErrorResource(throwable))
        }
        launch(handler) {
            mResult.value = LoadingResource("Classifying...")

            val body = CloudAutoMLModel(Payload(devnibbles.android.facialrecognition.classify.MlImage(String(Base64.encodeBase64(imageBytes)))))
            val response = provideService(provideGsonConverter(), provideNetworkClient()).classify(
                "Bearer $ACCESS_TOKEN",
                PROJECT, LOCATION, MODEL, body
            ).await()

            System.out.println("Response : " + response)

            mResult.value = SuccessResource("Andrew Kelly")
        }
    }


    fun classifyUsingCloudSDK(imageBytes: ByteArray) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            mResult.postValue(ErrorResource(throwable))
        }
        launch(handler) {
            mResult.value = LoadingResource("Classifying...")

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
                            mResult.value = SuccessResource(predictedName!!)
                        } else {
                            mResult.value = ErrorResource(null, "Not recognised (001)")
                        }
                    } else {
                        mResult.value = ErrorResource(null, "Not recognised (002)")
                    }
                }
            }
        }
    }

    private fun provideService(gsonFactory: GsonConverterFactory, networkClient: OkHttpClient): CloudAutoMLService {
        return Retrofit.Builder()
            .baseUrl("https://automl.googleapis.com/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(gsonFactory)
            .client(networkClient)
            .build()
            .create(CloudAutoMLService::class.java)
    }

    private fun provideGsonConverter(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder().create())
    }

    private fun provideNetworkClient(): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY

        return OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()
    }

}




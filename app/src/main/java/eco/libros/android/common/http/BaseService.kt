package eco.libros.android.common.http

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class BaseService {
    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    fun getClient(baseUri: String): Retrofit? = Retrofit.Builder()
            .baseUrl(baseUri)
            .client(OkHttpClient.Builder()
                 .connectTimeout(4,TimeUnit.SECONDS)


                .writeTimeout(4,TimeUnit.SECONDS)
                .readTimeout(4,TimeUnit.SECONDS)
//                .addInterceptor(TimeoutInterceptorImpl())
                .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

}
package eco.libros.android.common.http

import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.unity3d.ads.api.Request.post
import eco.libros.android.App
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.internal.http2.Http2Reader
import retrofit2.HttpException
import java.net.SocketTimeoutException

class TimeoutInterceptorImpl : TimeoutInterceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        try {
            response = chain.proceed(request)
        }catch (e: SocketTimeoutException){
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(App.appContext,"에러",Toast.LENGTH_LONG).show()
            }
            return error("에러")
        }
       return chain.proceed(request)
    }

    private fun isConnectionTimedOut(chain: Interceptor.Chain): Boolean {
        try {
            val response = chain.proceed(chain.request())
            val content = response.toString()
            response.close()
        } catch (e: SocketTimeoutException) {
            return true
        }
        return false
    }
}
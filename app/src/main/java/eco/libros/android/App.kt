package eco.libros.android

import android.app.Application
import android.webkit.WebView
import androidx.databinding.library.BuildConfig

class App : Application() {

    companion object {
        private const val BASE_URL = "http://m.libropia.co.kr/LibropiaWebService/request/"
    }


    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}
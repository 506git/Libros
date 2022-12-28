package eco.libros.android

import android.app.Application
import android.util.Log
import android.webkit.WebView
import androidx.databinding.library.BuildConfig
import eco.libros.android.common.LibrosExceptionHandler
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

class App : Application() {

    companion object {
        private const val BASE_URL = "http://m.libropia.co.kr/LibropiaWebService/request/"
    }

    private fun setCrashHandler(){
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, _ ->

        }

        Thread.setDefaultUncaughtExceptionHandler(

            LibrosExceptionHandler(this, defaultExceptionHandler)
        )
    }

    override fun onCreate() {
        super.onCreate()


    }
}
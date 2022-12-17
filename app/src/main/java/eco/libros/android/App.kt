package eco.libros.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.webkit.WebView
import eco.libros.android.common.LibrosExceptionHandler
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {

    companion object {
        lateinit var appContext: Context
        private const val channelIdNotice = "N"
        private const val channelIdLib = "L"
        private const val channelIdMark = "M"

        private const val channelNameNotice = "공지사항 알림"
        private const val channelNameLib = "도서관 알림"
        private const val channelNameAppMark = "청구기호 알림"

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
//        setCrashHandler()
        appContext = this
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel = NotificationChannel(
                channelIdNotice,
                channelNameNotice,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setShowBadge(true)
            notificationChannel.description = "도서관 소식을 받을 수 있습니다."
            manager.createNotificationChannel(notificationChannel)

            notificationChannel = NotificationChannel(
                channelIdLib,
                channelNameLib,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setShowBadge(true)
            notificationChannel.description = "도서 반납일 안내, 예약정보 안내 등의 알림을 받습니다."
            manager.createNotificationChannel(notificationChannel)

            notificationChannel = NotificationChannel(
                channelIdMark,
                channelNameAppMark,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.setShowBadge(true)
            notificationChannel.description = "청구기호 알림을 받습니다."
            manager.createNotificationChannel(notificationChannel)
        }
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(retrofitModule)
        }
    }

    val retrofitModule = module {
//        single { Cache(androidApplication().cacheDir, 10L * 1024 * 1024) }
//        single {
//            OkHttpClient.Builder()
//                .addInterceptor(get<Interceptor>())
//                .addInterceptor(get<HttpLoggingInterceptor>())
//                .build()
//        }

//        single {
//            Interceptor { chain: Interceptor.Chain ->
//                val original = chain.request()
//                chain.proceed(original.newBuilder().apply {
//                    addHeader("Content-Type", "application/json; charset=utf-8")
//                }.build())
//            }
//        }
//
//        single {
//            HttpLoggingInterceptor().apply {
//                level = HttpLoggingInterceptor.Level.NONE
//            }
//        }

        single {
            Retrofit.Builder()
                .baseUrl(Companion.BASE_URL)
                .client(get())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

    }
}
package eco.libros.android.common

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.content.Intent.EXTRA_INTENT
import android.os.Bundle
import android.os.Process.killProcess
import android.os.Process.myPid
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class LibrosExceptionHandler(application: Application, private val defaultExceptionHandler: Thread.UncaughtExceptionHandler): Thread.UncaughtExceptionHandler {
    private var lastActivity: Activity? = null
    private var activityCount = 0

    init {
        application.registerActivityLifecycleCallbacks(
            object : SimpleActivityLifecycleCallbacks(){
                override fun onActivityStarted(actiivty: Activity) {
                    if(isSkipActivity(actiivty)){
                        return
                    }
                    lastActivity = actiivty
                }

                override fun onActivityStopped(actiivty: Activity) {
                    if (isSkipActivity(actiivty)){
                        return
                    }
                    activityCount++
                    lastActivity = actiivty
                }

                override fun onActivityCreated(actiivty: Activity, savedInstanceState: Bundle?) {
                    if (isSkipActivity(actiivty)){
                        return
                    }
                    activityCount--
                    if (activityCount < 0){
                        lastActivity = null
                    }
                }
            }
        )
    }

    private fun isSkipActivity(activity: Activity) = activity is ErrorActivity

    override fun uncaughtException(thread: Thread, throwable : Throwable) {
        lastActivity?.run{
            val stringWriter = StringWriter()
            throwable.printStackTrace(PrintWriter(stringWriter))

            startErrorActivity(this, stringWriter.toString())
        } ?: defaultExceptionHandler.uncaughtException(thread,throwable)

        killProcess(android.os.Process.myPid())
        exitProcess(-1)
    }

    private fun startErrorActivity(activity: Activity, errorText: String) = activity.run {
        val errorActivityIntent = Intent(this, ErrorActivity::class.java).apply {
            putExtra(ErrorActivity.EXTRA_INTENT, intent)
            putExtra(ErrorActivity.EXTRA_ERROR_TEXT, errorText)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(errorActivityIntent)
        finish()
    }
}
package eco.libros.android.common

import android.app.Activity
import android.app.Application
import android.os.Bundle

abstract class SimpleActivityLifecycleCallbacks: Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(actiivty: Activity) {

    }

    override fun onActivityResumed(actiivty: Activity) {

    }

    override fun onActivityStarted(actiivty: Activity) {

    }

    override fun onActivityDestroyed(actiivty: Activity) {

    }

    override fun onActivitySaveInstanceState(actiivty: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(actiivty: Activity) {

    }

    override fun onActivityCreated(actiivty: Activity, savedInstanceState: Bundle?) {

    }
}
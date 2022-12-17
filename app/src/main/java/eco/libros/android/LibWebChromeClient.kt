package eco.libros.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Build
import android.os.Message
import android.util.Log
import android.view.*
import android.webkit.*
import android.webkit.WebSettings.ZoomDensity
import android.webkit.WebView.WebViewTransport
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContentProviderCompat.requireContext
import eco.libros.android.common.utill.LibrosLog
import kotlinx.android.synthetic.main.fragment_login.*

class LibWebChromeClient(activity: Activity, mainLayout: ConstraintLayout) : WebChromeClient() {
    var activity:Activity? = null
    var mainLayout: ConstraintLayout? = null
    init {
        this.activity = activity
        this.mainLayout = mainLayout
    }
    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        try{
            AlertDialog.Builder(activity).apply {
                setMessage(message)
                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    result?.confirm()
                })
                setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which -> result?.cancel() })
                setCancelable(false)
                create().show()
            }
        } catch (e: WindowManager.BadTokenException){
            LibrosLog.print(e.toString())
        }
        return true
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        try{
            AlertDialog.Builder(activity).apply {
                setMessage(message)
                setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    result?.confirm()
                })
                setCancelable(false)
                create().show()
            }
        } catch (e: WindowManager.BadTokenException){
            LibrosLog.print(e.toString())
        }
        return true
    }

    @SuppressLint("JavascriptInterface")
    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
        if (mainLayout != null){
            Log.d("TEST11","TEST");
            val childView = WebView(this.activity!!)
            val settings = childView.settings
            settings.javaScriptEnabled = true
            settings.setSupportMultipleWindows(true)
            settings.javaScriptCanOpenWindowsAutomatically = true
            settings.useWideViewPort = true
            settings.setSupportZoom(true) //Modify this

            settings.defaultZoom = ZoomDensity.FAR
            settings.loadWithOverviewMode = true
            childView.setInitialScale(100)
            childView.webChromeClient = this
            settings.domStorageEnabled = true
            childView.webViewClient = LocalWebViewClient()
            childView.addJavascriptInterface(SmartLibBridge(childView), "Android")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }

            val dialog = Dialog(activity!!, R.style.FullscreenTheme).apply{
                window!!.requestFeature(Window.FEATURE_NO_TITLE)
                setContentView(childView)
                window!!.attributes.width = ViewGroup.LayoutParams.MATCH_PARENT
                window!!.attributes.height = ViewGroup.LayoutParams.MATCH_PARENT
                show()
            }
            dialog.setOnKeyListener { _, keyCode, event ->
                Log.d("test111","close")
                if(event.keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_DOWN ) {
                    Log.d("test1121","close")
                    if (childView.canGoBack()) {
                        childView.goBack()
                        true
                    } else {
                        dialog.dismiss()
                        childView.destroy()
                        true
                    }
                } else
                    false

            }

            val transport = resultMsg!!.obj as WebViewTransport
            transport.webView = childView
            resultMsg.sendToTarget()


//            mainLayout!!.addView(childView, 0, ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
            return true
        }
        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    override fun onExceededDatabaseQuota(url: String?, databaseIdentifier: String?, quota: Long, estimatedDatabaseSize: Long, totalQuota: Long, quotaUpdater: WebStorage.QuotaUpdater?) {
        super.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater)
    }

    override fun onCloseWindow(window: WebView?) {
        Log.d("testClose","onCloseWindow")
        if (mainLayout != null){
            mainLayout!!.removeAllViews()
            window?.destroy()
        }
        window?.destroy()
        super.onCloseWindow(window)
    }

    public class SmartLibBridge(view: WebView) {
        private var view: WebView? = null;
         init {
            this.view = view
        }

         //@JavascriptInterface
         fun onBack() {
             if (view!!.canGoBack()) {
                 view!!.goBack()
             }
         }
    }
    private class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            Log.d("testdd2",url.toString())
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            Log.d("testdd3",view?.url.toString())
            return super.shouldOverrideUrlLoading(view, request)
        }
    }

}


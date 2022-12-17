package eco.libros.android.common.web

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Message
import android.util.Log
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.utill.LibrosLog
import java.net.URISyntaxException
import java.util.regex.Pattern

class LibWebChromeClient(activity: Activity, layoutPopup: LinearLayout?) : WebChromeClient() {
    private val mActivity = activity
    private val mLayoutPopup = layoutPopup
    private val basketCnt = 0

    init {

    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
        return super.onConsoleMessage(consoleMessage)
    }

    override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        try {
            AlertDialog.Builder(mActivity).apply {
                setMessage(message)
                setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    result?.confirm()
                })
                setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which ->
                    result?.cancel()
                })
                setCancelable(false)
                create().show()
            }
        } catch (e: WindowManager.BadTokenException) {
            LibrosLog.print(e.toString())
        }
        return true
    }

    override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
        try {
            AlertDialog.Builder(mActivity).apply {
                setMessage(message)
                setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    result?.confirm()
                })
                setCancelable(false)
                create().show()
            }
        } catch (e: WindowManager.BadTokenException) {
            LibrosLog.print(e.toString())
        }
        return true
    }

//    override fun onProgressChanged(view: WebView?, newProgress: Int) {
//
//        if (view != null) {
//            var progressFragment = ProgressFragment()
//            try {
//                progressFragment = ProgressFragment.newInstance("로딩 중입니다.")
//                progressFragment.show((mActivity as FragmentActivity).supportFragmentManager,"progressbar")
//            } catch (e: WindowManager.BadTokenException) {
//                Log.e("error", e.message.toString())
//                return
//            }
//            if (newProgress >= 100) {
//                if (progressFragment.isAdded) {
//                    try {
//                        progressFragment.dismissAllowingStateLoss()
//                    } catch (e: WindowManager.BadTokenException) {
//                        // TODO: handle exception
//                        Log.e("error", e.message.toString())
//                    } catch (e: IllegalArgumentException) {
//                        // TODO: handle exception
//                        Log.e("error", e.message.toString())
//                    }
//                }
//            }
//        }
//    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
        if (mLayoutPopup != null) {
            val childView = WebView(mActivity)
            childView.settings.apply {
                javaScriptEnabled = true
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
                useWideViewPort = true
                setSupportZoom(true)
                loadWithOverviewMode = true
                domStorageEnabled = true
            }
            childView.apply {
                webChromeClient = this@LibWebChromeClient
                webViewClient = LocalWebViewClient()
            }
            mLayoutPopup.addView(childView, 0, ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
            return true
        }

        return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg)
    }

    private inner class LocalWebViewClient : WebViewClient() {
        private val ACCEPTED_URI_SCHEMA = Pattern.compile(
                "(?i)" +
                        "(" +
                        "(?:http|https|file):\\/\\/" +
                        "|(?:inline|data|about|javascript):" +
                        ")" +
                        "(.*)")

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            view!!.loadUrl("javascript:basketCntRefresh($basketCnt)")
        }

        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
            val m = ACCEPTED_URI_SCHEMA.matcher(url!!)
            if (m.matches() == false) {
                val lowerUrl = url.toLowerCase()
                if (lowerUrl.startsWith("intent://")) {
                    lateinit var intent: Intent
                    try {
                        intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME).apply {
                            mActivity.startActivity(this)
                        }
                    } catch (e: URISyntaxException) {
                        LibrosLog.print(e.toString())
                    } catch (e: ActivityNotFoundException) {
                        if (intent.`package` != null)
                            mActivity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + intent.`package`)))
                    }
                }
            }
            return super.shouldOverrideUrlLoading(view, url)
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            AlertDialog.Builder(mActivity).apply {
                setPositiveButton("확인", DialogInterface.OnClickListener { dialog, which ->
                    handler?.proceed()
                })
                setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                    handler?.cancel()
                })
                setMessage(
                        when (error?.primaryError) {
                            SslError.SSL_EXPIRED, SslError.SSL_IDMISMATCH, SslError.SSL_NOTYETVALID, SslError.SSL_UNTRUSTED -> "이 사이트의 보안 인증서는 신뢰할 수 없습니다.\n"
                            else -> "보안 인증서에 오류가 있습니다.\n"
                        } + "계속 진행하시겠습니까?"
                )
                setTitle("SSL 인증 오류")
                create().show()
            }

            super.onReceivedSslError(view, handler, error)
        }
    }

//    override fun onProgressChanged(view: WebView?, newProgress: Int) {
//        try {
//            var progressFragment = ProgressFragment()
//            progressFragment = ProgressFragment.newInstance("정보 조회 중입니다.")
//            progressFragment.show(, "progress")
//        } catch (e: WindowManager.BadTokenException) {
//            Log.e("error", e.message)
//            return
//        }
//    }

//    override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: Message?): Boolean {
//
//    }
}
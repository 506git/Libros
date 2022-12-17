package eco.libros.android.home.fragment

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import eco.libros.android.BuildConfig
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibropiaNoticeDTO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.component.LibrosDialog
import eco.libros.android.data.LibData

import eco.libros.android.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.regex.Matcher
import java.util.regex.Pattern

class HomeFragment : BaseWebViewFragment() {
    // TODO: Rename and change types of parameters
    private var contentsKey: String? = null
    private var libCode: String? = null

    private lateinit var webView: WebView

    private val ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" +
                    "(" +
                    "(?:http|https|file):\\/\\/" +
                    "|(?:inline|data|about|javascript):" +
                    ")" +
                    "(.*)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contentsKey = it.getString("contentsKey")
            libCode = it.getString("libCode")
        }

        Log.d("testsssss","param1 : $contentsKey param2 $libCode")

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById(R.id.webView)
        initWebViewSetting(webView)
        initWebView(savedInstanceState, view.findViewById(R.id.mainLayout))
//        webView.loadUrl("http://192.168.7.122:8998/home.html")
        if (contentsKey != null && libCode != null){
            webView.loadUrl("http://211.253.36.163:3005/qr_info.html?libCode=$libCode&contentsKey=$contentsKey")
//            webView.loadUrl("http://192.168.7.99:5000/qr_info.html?libCode=$libCode&contentsKey=$contentsKey")
        } else webView.loadUrl("http://211.253.36.163:3005/index.html")
//        } else webView.loadUrl("http://192.168.7.99:5000/index.html")

//        webView.loadUrl("http://220.72.184.140:3006/main_libros.html")
//        if (savedInstanceState != null || webViewBundle != null) {
//            webView.restoreState(Companion.webViewBundle!!)
//            bundleClear()
//        }
    }

    override fun onPause() {
        super.onPause()
        webViewBundle = Bundle()
    }

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun initWebView(savedInstanceState: Bundle?) {
//        webView.webViewClient = object : WebViewClient() {
//            private var mWebView: WebView? = null
//
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onRenderProcessGone(
//                    view: WebView?,
//                    detail: RenderProcessGoneDetail?
//            ): Boolean {
//                if (!detail?.didCrash()!!) {
//                    view?.also { it ->
//                        val webViewContainer: ViewGroup = view.findViewById(R.id.fragment_container)
//                        webViewContainer.removeView(webView)
//                        mWebView?.destroy()
//                        mWebView = null
//                    }
//                    return true
//                }
//                return false
//            }
//
//            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                super.onPageStarted(view, url, favicon)
//                if (isAdded) {
//                    (requireActivity() as MainActivity).progressBarStart()
//                }
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                if (isAdded) {
//                    (requireActivity() as MainActivity).progressBarEnd()
//                }
//                if (url!!.contains("smartLibrary/loginOk")) {
//                    webView.clearHistory()
//                }
//            }
//
//            @RequiresApi(Build.VERSION_CODES.N)
//            override fun shouldOverrideUrlLoading(
//                    view: WebView?,
//                    request: WebResourceRequest?
//            ): Boolean {
//                val url: String = request?.url.toString()
//                val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url)
//                return super.shouldOverrideUrlLoading(view, request)
//
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                val intent = getParsedIntent(url)
//                val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url.toString())
//
//                return super.shouldOverrideUrlLoading(view, url)
//            }
//
//            private fun getParsedIntent(url: String?): Intent? {
//                if (url == null) {
//                    return null
//                }
//
//                return Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//            }
//
//        }
//
//        webView.webChromeClient = object : WebChromeClient() {
//            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
//                super.onGeolocationPermissionsShowPrompt(origin, callback)
//                callback?.invoke(origin, true, false)
//            }
//
//            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
//                if (message != null) {
//                    LibrosDialog.LibrosBuilder(requireActivity())
//                            .setMessage(message)
//                            .setCancelable(false)
//                            .setPositiveButton(R.string.confirm) { _, _ ->
//                                result?.confirm()
//                            }
//                            .show()
//                }
//                return true
//            }
//
//            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?
//            ): Boolean {
//                if (message != null) {
//                    LibrosDialog.LibrosBuilder(requireActivity())
//                            .setMessage(message)
//                            .setCancelable(false)
//                            .setPositiveButton(R.string.confirm) { _, _ ->
//                                result?.confirm()
//                            }
//                            .setNegativeButton(R.string.cancel) { _, _ ->
//                                result?.cancel()
//                            }
//                            .show()
//                }
//                return true
//            }
//
//        }
//
//        webView.clearCache(true)
//        webView.loadUrl("http://192.168.7.122:8998/home.html")
//        if (savedInstanceState != null || webViewBundle != null) {
//            webView.restoreState(Companion.webViewBundle!!)
//            bundleClear()
//        }
//
//    }

    private fun bundleClear() {
        webViewBundle = null
    }

    companion object {
        var webViewBundle: Bundle? = null
    }
}




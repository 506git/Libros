package eco.libros.android.myContents

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.*
import androidx.fragment.app.Fragment
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.utill.LibrosWebMenu
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ebook.EBookFragment
import eco.libros.android.home.fragment.HomeFragment
import kotlinx.android.synthetic.main.fragment_e_book.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_my_content.*

/**
 * A simple [Fragment] subclass.
 * Use the [MyContentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyContentFragment : BaseWebViewFragment() {
    // TODO: Rename and change types of parameters
    private var receiverRegistered: Boolean = false

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent : Intent?) {
            Log.d("testlent", intent?.getStringExtra("lent_key").toString())
            LibrosWebMenu(context!!, webView!!).webViewExec("downloadEBook",LibrosUtil.getSimpleJson(mapOf<String, String?>(
                "resultCode" to "Y",
                "resultMessage" to "",
                "lent_key" to intent?.getStringExtra("lent_key")
            )))
        }
    }


    override fun onResume() {
        super.onResume()
        val filter = IntentFilter()
        filter.addAction(GlobalVariable.DOWNLOAD_RESULT)
        activity?.registerReceiver(receiver, filter)
        receiverRegistered = true
    }

    override fun onPause() {
        super.onPause()
        webViewBundle = Bundle()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        webView = view.findViewById<WebView>(R.id.webView)
        initWebViewSetting(webView)
        initWebView(savedInstanceState, view.findViewById(R.id.mainLayout))
//        webView.loadUrl("http://192.168.7.122:8998/myContents.html")
        webView.loadUrl("http://220.72.184.140:3006/main_mylib.html")
        if (savedInstanceState != null || HomeFragment.webViewBundle != null) {
            webView.restoreState(HomeFragment.webViewBundle!!)
            bundleClear()
        }
    }

    override fun onStop() {
        super.onStop()
        if (receiverRegistered){
            activity?.unregisterReceiver(receiver)
            receiverRegistered = false
        }
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    private fun bundleClear() {
        EBookFragment.webViewBundle = null
    }

//    @SuppressLint("SetJavaScriptEnabled")
//    private fun initWebView() {
//        activity?.window?.setFlags(
//            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//        )
//        webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//        webView?.settings?.apply {
//            javaScriptEnabled = true
//            saveFormData = false
//            userAgentString = getString(R.string.user_agent)
//            domStorageEnabled = true
//            databaseEnabled = true
//
//        }
//        webView?.addJavascriptInterface(
//            WebViewCallback(),
//            WEB_VIEW_CALLBACK
//        )
//        webView?.setOnKeyListener { v, keyCode, event ->
//            if (event.keyCode == KeyEvent.KEYCODE_BACK && !webView?.canGoBack()!!) {
//                false
//            } else if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP) {
//                webView?.goBack()
//                true
//            } else true
//        }
//        webView?.webViewClient = object : WebViewClient() {
//            private var mWebView: WebView? = null
//
//            @RequiresApi(Build.VERSION_CODES.O)
//            override fun onRenderProcessGone(
//                view: WebView?,
//                detail: RenderProcessGoneDetail?
//            ): Boolean {
//                if (!detail?.didCrash()!!) {
//                    view?.also { it ->
//                        val webViewContainer: ViewGroup = view.findViewById(R.id.webView)
//
//                        webViewContainer.removeView(webView)
//                        mWebView?.destroy()
//                        mWebView = null
//                    }
//                    return true
//                }
//                return false
////                return super.onRenderProcessGone(view, detail)
//
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
//            }
//
//            @RequiresApi(Build.VERSION_CODES.N)
//            override fun shouldOverrideUrlLoading(
//                view: WebView?,
//                request: WebResourceRequest?
//            ): Boolean {
//                val intent = getParsedIntent(request?.url.toString())
//
//                return if (intent != null) {
//                    try {
//                        startActivity(intent)
//                        true
//                    } catch (ignored: ActivityNotFoundException) {
//                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT)
//                            .show()
//                        super.shouldOverrideUrlLoading(view, request)
//                    }
//                } else {
//                    super.shouldOverrideUrlLoading(view, request)
//                }
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
//                val intent = getParsedIntent(url)
//
//                return if (intent != null) {
//                    try {
//                        startActivity(intent)
//                        true
//                    } catch (ignored: ActivityNotFoundException) {
//                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT)
//                            .show()
//                        super.shouldOverrideUrlLoading(view, url)
//                    }
//                } else {
//                    super.shouldOverrideUrlLoading(view, url)
//                }
//            }
//
//            private fun getParsedIntent(url: String?): Intent? {
//                if (url == null) {
//                    return null
//                }
//
//                return Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//            }
//        }
//
//        webView?.webChromeClient = object : WebChromeClient() {
//            override fun onJsAlert(
//                view: WebView?,
//                url: String?,
//                message: String?,
//                result: JsResult?
//            ): Boolean {
//                if (message != null) {
//                    LibrosDialog.LibrosBuilder(requireActivity())
//                        .setMessage(message)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.confirm) { _, _ ->
//                            result?.confirm()
//                        }
//                        .show()
//                }
//                return true
//            }
//
//            override fun onJsConfirm(
//                view: WebView?,
//                url: String?,
//                message: String?,
//                result: JsResult?
//            ): Boolean {
//                if (message != null) {
//                    LibrosDialog.LibrosBuilder(requireActivity())
//                        .setMessage(message)
//                        .setCancelable(false)
//                        .setPositiveButton(R.string.confirm) { _, _ ->
//                            result?.confirm()
//                        }
//                        .setNegativeButton(R.string.cancel) { _, _ ->
//                            result?.cancel()
//                        }
//                        .show()
//                }
//                return true
//            }
//
//        }
//
//        webView?.loadUrl("http://192.168.7.122:8998/myContents.html")
//    }

    companion object {
        var webViewBundle: Bundle? = null
    }
}
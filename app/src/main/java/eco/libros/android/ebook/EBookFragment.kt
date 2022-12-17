package eco.libros.android.ebook

import android.os.Bundle
import android.view.*
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import eco.libros.android.home.fragment.HomeFragment
import kotlinx.android.synthetic.main.fragment_e_book.webView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class EBookFragment : BaseWebViewFragment() {
    // TODO: Rename and change types of parameters

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_e_book, container, false)
    }

    override fun onPause() {
        super.onPause()
        webViewBundle = Bundle()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebViewSetting(view.findViewById(R.id.webView))
        initWebView(savedInstanceState, view.findViewById(R.id.mainLayout))
//        webView.loadUrl("http://192.168.7.122:8998/ebook.html")
        webView.loadUrl("http://220.72.184.140:3006/ebook.html")
        if (savedInstanceState != null || HomeFragment.webViewBundle != null) {
            webView.restoreState(HomeFragment.webViewBundle!!)
            bundleClear()
        }
    }
//    private fun initWebView() {
//        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
//
//        webView.settings.apply {
//            javaScriptEnabled = true
//            saveFormData = false
//            userAgentString = getString(R.string.user_agent)
//            domStorageEnabled = true
//            databaseEnabled = true
//        }
//        webView.addJavascriptInterface(
//            WebViewCallback(),
//            WEB_VIEW_CALLBACK
//        )
//
//        webView.setOnKeyListener { v, keyCode, event ->
//            if(event.keyCode == KeyEvent.KEYCODE_BACK && !webView.canGoBack()){
//                false
//            } else if(event.keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP){
//                webView.goBack()
//                true
//            } else true
//        }

//        webView.webViewClient = object : WebViewClient() {
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
//            @RequiresApi(Build.VERSION_CODES.N)
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                val intent = getParsedIntent(request?.url.toString())
//
//                return if (intent != null) {
//                    try {
//                        startActivity(intent)
//                        true
//                    } catch (ignored: ActivityNotFoundException) {
//                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
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
//                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
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
//        webView.webChromeClient = object : WebChromeClient() {
//
//            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
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
//            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
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
//    }
    private fun bundleClear() {
        webViewBundle = null
    }
    companion object {
        var webViewBundle: Bundle? = null
    }
}
package eco.libros.android.newBook

import android.os.Bundle
import android.view.*
import android.webkit.*
import androidx.constraintlayout.widget.ConstraintLayout
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import kotlinx.android.synthetic.main.fragment_e_book.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class NewBookFragment : BaseWebViewFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initWebViewSetting(view.findViewById(R.id.webView))
        initWebView(savedInstanceState, view.findViewById(R.id.mainLayout))
//        webView.loadUrl("http://192.168.7.122:8998/newbook.html")
        webView.loadUrl("http://220.72.184.140:3006/newbook.html")
    }
//    @SuppressLint("SetJavaScriptEnabled")
//    private fun initWebView() {
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
////                webView.loadUrl(intent?.url)
////                return if (intent != null) {
////                    try {
////                        startActivity(intent)
////                        true
////                    } catch (ignored: ActivityNotFoundException) {
////                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
////                        super.shouldOverrideUrlLoading(view, request)
////                    }
////                } else {
//                   return super.shouldOverrideUrlLoading(view, request)
////                }
////                return true
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
////                val intent = getParsedIntent(url)
////                if (url?.contains("smartLibrary/loginOk?")!!){
////                    webView.loadUrl(url)
////                    return false
////                }
////                return true
////                return if (intent != null) {
////                    try {
////                        startActivity(intent)
////                        true
////                    } catch (ignored: ActivityNotFoundException) {
////                        Toast.makeText(activity, R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
////                        super.shouldOverrideUrlLoading(view, url)
////                    }
////                } else {
//                    return super.shouldOverrideUrlLoading(view, url)
////                }
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
//        webView.loadUrl("http://192.168.7.122:8998/newbook.html")
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_book, container, false)
    }


    companion object {
        const val WEB_VIEW_CALLBACK = "LibrosApp"
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment newBookFragment.
         */
        // TODO: Rename and change types and number of parameters

    }
}



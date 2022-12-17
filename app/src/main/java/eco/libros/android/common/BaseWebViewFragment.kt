package eco.libros.android.common

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import eco.libros.android.LibWebChromeClient
import eco.libros.android.R
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.utill.LibrosWebMenu
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.component.LibrosDialog
import eco.libros.android.ui.BaseActivity
import eco.libros.android.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_e_book.*
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern

open class BaseWebViewFragment : Fragment() {
    private var mRecognizer: SpeechRecognizer? = null
    var progressFragment = ProgressFragment()
    private val WEB_VIEW_CALLBACK = "LibrosApp"
    var webViewCanGoBack = false

    companion object{
        private val ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" +
                    "(" +
                    "(?:http|https|file):\\/\\/" +
                    "|(?:inline|data|about|javascript):" +
                    ")" +
                    "(.*)"
        )
    }

    fun init(view: View) {
        view.findViewById<TextView>(R.id.toolbar_text)?.text =
                activity?.supportFragmentManager?.getBackStackEntryAt(activity?.supportFragmentManager?.backStackEntryCount!! - 1)?.name
        view.findViewById<ImageButton>(R.id.close_btn)?.setOnClickListener {
            activity?.let { it1 -> CustomFragmentManager.removeCurrentFragment(it1) }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebView(savedInstanceState: Bundle?, mainLayout: View) {
        webView.webViewClient = object : WebViewClient() {
            private var mWebView: WebView? = null

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onRenderProcessGone(
                view: WebView?,
                detail: RenderProcessGoneDetail?
            ): Boolean {
                if (!detail?.didCrash()!!) {
                    view?.also { it ->
                        val webViewContainer: ViewGroup = view.findViewById(R.id.fragment_container)
                        webViewContainer.removeView(webView)
                        mWebView?.destroy()
                        mWebView = null
                    }
                    return true
                }
                return false
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                if (isAdded) {
                    (requireActivity() as MainActivity).progressBarStart()
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (isAdded) {
                    (requireActivity() as MainActivity).progressBarEnd()
                }
                if (url!!.contains("smartLibrary/loginOk")) {
                    webView.clearHistory()
                }
                if(url.contains("main_libros.html")) {
                    val loginInfoMap = mapOf<String, String?>(
                            "librosId" to LibrosUtil.getUserId(
                                    activity!!,
                                    needEncoding = true,
                                    needEncrypt = true
                            ),
                            "libCode" to LibrosUtil.getLibCode(activity!!),
                            "deviceType" to context?.resources?.getString(R.string.device_type),
                            "deviceId" to LibrosUtil.getOriginDeviceId(activity!!),
                            "startUrl" to "http://220.72.184.140:3006/library.html"
                    )
                    view?.evaluateJavascript(
                            "javascript:window.\$appInterfaceObject.APP_TO_WEBVIEW('getLoginInfo', 'test')",
                            null
                    )
                }

            }

            @RequiresApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url: String = request?.url.toString()
                val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url)
                Log.d("testdd", url.toString())
                Log.d("testdd", view?.url.toString())
                return super.shouldOverrideUrlLoading(view, request)

            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val intent = getParsedIntent(url)
                val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url.toString())
                Log.d("testdd", url.toString())
                Log.d("testdd", view?.url.toString())
                return super.shouldOverrideUrlLoading(view, url)
            }

            private fun getParsedIntent(url: String?): Intent? {
                if (url == null) {
                    return null
                }

                return Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            }
        }

        webView.webChromeClient = LibWebChromeClient(requireActivity(), mainLayout as ConstraintLayout)

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
//        }
    }

    fun initWebViewSetting(webView: WebView) {
        webView.apply {
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            addJavascriptInterface(WebViewCallback(), WEB_VIEW_CALLBACK)
            settings.apply {
                javaScriptEnabled = true
                userAgentString = getString(R.string.user_agent)
                domStorageEnabled = true
                databaseEnabled = true
                useWideViewPort = true
                displayZoomControls = false
                loadWithOverviewMode = true

                displayZoomControls = false
                setSupportMultipleWindows(true)
                javaScriptCanOpenWindowsAutomatically = true
            }
        }
    }


    fun hideProgress() {
        if (progressFragment.isAdded) {
            try {
                progressFragment.dismissAllowingStateLoss()
            } catch (e: WindowManager.BadTokenException) {
                // TODO: handle exception
                Log.e("error", e.message.toString())
            } catch (e: IllegalArgumentException) {
                // TODO: handle exception
                Log.e("error", e.message.toString())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (webView != null) {
            webView.onResume()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (webView != null) {
            webView.destroy()
        }
    }

    override fun onPause() {
        super.onPause()
        if (webView != null) {
            webView.onPause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mRecognizer != null) {
            mRecognizer?.cancel()
            mRecognizer?.destroy()
        }
        if (webView != null) {
            webView.destroy()
        }

    }

    protected fun hideKeyboard() {
        AppUtils.hideSoftKeyBoard(requireActivity())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView != null) {
                    if (webView.canGoBack()) {
                        Log.d("TESTGOBACK", "TRUE")
                        webView.goBack()
                        webViewCanGoBack = true
                    } else {
                        Log.d("TESTGOBACK", "FALSE")
                        isEnabled = true
                        webViewCanGoBack = false
                        (activity as BaseActivity).backNav()
                    }
                } else {
                    Log.d("TESTGOBACK", "FALSE2")
                    isEnabled = false
                    requireActivity().onBackPressed()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO Auto-generated method stub
        retainInstance = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    inner class WebViewCallback {
        @JavascriptInterface
        fun APP_EXEC(alias: String, data: String?) {
            Log.d("testExec", alias)
            LibrosWebMenu(context!!, webView).exec(activity!!, alias, data)
        }

        @JavascriptInterface
        fun APP_SETDATA(alias: String, data: String?) {
            Log.d("testSetData", "$alias data : $data")
            LibrosWebMenu(context!!, webView).setData(activity!!, alias, data!!)
        }

        @JavascriptInterface
        fun APP_GETDATA(alias: String, data: String?) {
            Log.d("testGetData", alias)
            LibrosWebMenu(context!!, webView).getData(activity!!, alias, data!!)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TESTURLF", requestCode.toString())
        Log.d("TESTURLF", resultCode.toString())
        when (requestCode) {
            GlobalVariable.BARCODE_SCAN -> {
                val result: IntentResult? =
                    IntentIntegrator.parseActivityResult(
                        IntentIntegrator.REQUEST_CODE,
                        resultCode,
                        data
                    )
                val contents = result?.contents

                if (contents != null) {
                    try {
                        val jobj = JSONObject()
                        jobj.put("searchData", contents)
                        Log.d("TESTOBJ", jobj.toString())
                        webView.post {
                            webView.evaluateJavascript(
                                "javascript:window.\$appInterfaceObject.RETURN_APP_EXEC(\"scanBarcode\", $jobj)",
                                null
                            )
                        }
                    } catch (ignored: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_barcode_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            GlobalVariable.BARCODE_SMARTPHONE_AUTH -> {
                val result: IntentResult? =
                    IntentIntegrator.parseActivityResult(
                        IntentIntegrator.REQUEST_CODE,
                        resultCode,
                        data
                    )
                val contents = result?.contents
                if (contents != null) {
                    try {
                        Toast.makeText(requireContext(), contents, Toast.LENGTH_SHORT)
                            .show()
                    } catch (ignored: Throwable) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_barcode_error_msg),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}
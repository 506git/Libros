package eco.libros.android.home.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.http.SslError
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.*
import androidx.annotation.RequiresApi
import eco.libros.android.R
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.web.LibWebChromeClient
import eco.libros.android.ui.BaseActivity
import eco.libros.android.ui.MainActivity
import java.util.regex.Matcher
import java.util.regex.Pattern

class HomeWebViewActivity : AppCompatActivity() {
    lateinit var homeWebView: WebView
    lateinit var progressBar: ProgressBar
    lateinit var webViewUrl: String
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
        setContentView(R.layout.activity_home_web_view)
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }

        findViewById<TextView>(R.id.toolbar_text).text = intent.getStringExtra("libName")!!
        findViewById<ImageView>(R.id.btn_close).apply {
            setOnClickListener {
                finish()
            }
//            setColorFilter(Color.parseColor("#b2b2b2"),PorterDuff.Mode.SRC_IN)
        }
        findViewById<ImageView>(R.id.btn_close).setColorFilter(Color.parseColor("#ff0000"),PorterDuff.Mode.SRC_IN)
        homeWebView = findViewById<WebView>(R.id.home_webView)
        progressBar = findViewById(R.id.progressBar)
        webViewUrl = intent.getStringExtra("webViewURL")!!
        Log.d("TESTURLF",webViewUrl)
        init()
        homeWebView.loadUrl(webViewUrl)
    }

    override fun onBackPressed() {
        if(homeWebView.canGoBack()){
            homeWebView.goBack()
        } else
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if(homeWebView.canGoBack()){
                    homeWebView.goBack()
                } else onBackPressed()
                true
            }
            else -> true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("JavascriptInterface")
    private fun init() {
        homeWebView.apply {
            webChromeClient =
                LibWebChromeClient(activity = this@HomeWebViewActivity, layoutPopup = null)
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                userAgentString = getString(R.string.user_agent)
                defaultTextEncodingName = "euc-kr"
                useWideViewPort = true
                setSupportMultipleWindows(true)
                addJavascriptInterface(WebViewCallBack(), "android")
            }
            webViewClient = object : WebViewClient() {
                private var mWebView: WebView? = null

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onRenderProcessGone(
                    view: WebView?,
                    detail: RenderProcessGoneDetail?
                ): Boolean {
                    if (!detail?.didCrash()!!) {
                        view?.also { it ->
                            val webViewContainer: ViewGroup =
                                view.findViewById(R.id.fragment_container)
                            webViewContainer.removeView(homeWebView)
                            mWebView?.destroy()
                            mWebView = null
                        }
                        return true
                    }
                    return false
//                return super.onRenderProcessGone(view, detail)

                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    progressBar.visibility = View.VISIBLE
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    if(view?.canGoBack() == true){
                        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                    } else {
                        supportActionBar?.setDisplayHomeAsUpEnabled(false)
                    }
                    if (url!!.contains("smartLibrary/loginOk")) {
                        homeWebView.clearHistory()
                    }
                }


                @RequiresApi(Build.VERSION_CODES.N)
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url: String = request?.url.toString()
                    val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url)
                    return super.shouldOverrideUrlLoading(view, request)

                }

                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val intent = getParsedIntent(url)
                    val m: Matcher = ACCEPTED_URI_SCHEMA.matcher(url.toString())
                    return super.shouldOverrideUrlLoading(view, url)
                }

                private fun getParsedIntent(url: String?): Intent? {
                    if (url == null) {
                        return null
                    }

                    return Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                }

                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    AlertDialog.Builder(this@HomeWebViewActivity).apply {
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
        }
    }

    inner class WebViewCallBack {

    }

}
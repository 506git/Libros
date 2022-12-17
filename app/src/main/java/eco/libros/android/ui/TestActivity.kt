package eco.libros.android.ui

import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.*
import eco.libros.android.R
import eco.libros.android.newBook.NewBookFragment
import kotlinx.android.synthetic.main.activity_test.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class TestActivity : BaseActivity() {
    var tokenTask: authTokenAsyncTask? = null
    var token: String? = null
    var cookieManager: CookieManager? = null
    private var count : Int = 1

    override fun onStart() {
        super.onStart()
        tokenTask = authTokenAsyncTask()
        tokenTask?.execute()

    }

    override fun onPause() {
        super.onPause()
        CookieSyncManager.getInstance().stopSync()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        CookieSyncManager.createInstance(this)
        CookieManager.getInstance().flush();
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.title = "test"
        changeView(count);
        fab.setOnClickListener{
            finish()
        }
        imgBtnLeft.setOnClickListener{
            if(count>1){
                changeView(--count)
            }
        }
        imgBtnRight.setOnClickListener{
            if(count<4) {
                changeView(++count)
            }
        }
        webView.settings.apply {
            javaScriptEnabled = true
            saveFormData = false
            userAgentString = "Mozilla"
            domStorageEnabled = true
            databaseEnabled = true
            defaultTextEncodingName = "euc-kr"

        }
        webView.addJavascriptInterface(
            WebViewCallbackInterface(),
            NewBookFragment.WEB_VIEW_CALLBACK
        )
        webView.webViewClient = object : WebViewClient() {

            private fun getExtraHeaders(): MutableMap<String, String>? {
                val headers: MutableMap<String, String> = HashMap()
                headers["x-auth-token"] = token.toString()
                return headers
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val extraHeaders: MutableMap<String, String> =
                    HashMap()
                extraHeaders["X-Auth-Token"] = token.toString()
                webView.post(Runnable {
                    webView.loadUrl(
                        request?.url.toString(),
                        extraHeaders
                    )
                })
                Log.d("testurld", view?.url.toString())
                return true
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
//                if (!request!!.requestHeaders.containsKey("x-auth-token")) return null
//                val headers: MutableMap<String, String> = HashMap()
//                headers.putAll(request.requestHeaders)
//                headers["X-Auth-Token"] = token.toString();
                var url: URL = URL(request!!.url.toString())
                val httpClient: OkHttpClient = OkHttpClient()
                val request2: Request = Request.Builder()
                    .url(request.url.toString().trim())
                    .addHeader("X-Auth-Token", token.toString()) // Example header
                    .build()

                val response: Response = httpClient.newCall(request2).execute()
                Log.d("testddd", response.headers["content-type"]!!)
                return WebResourceResponse(
                    "",
                    "UTF-8",
                    response.body?.byteStream()
                )
            }


            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)

                progressBarStart()

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                progressBarEnd()

            }

            //            @RequiresApi(Build.VERSION_CODES.N)
//            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
//                val intent = getParsedIntent(request?.url.toString())
//
//                return if (intent != null) {
//                    try {
//                        startActivity(intent)
//                        true
//                    } catch (ignored: ActivityNotFoundException) {
//                        Toast.makeText(activity,
//                            R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
//                        super.shouldOverrideUrlLoading(view, request)
//                    }
//                } else {
//                    super.shouldOverrideUrlLoading(view, request)
//                }
//            }
//
//            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
////    if (request!!.requestHeaders.containsKey("X-Auth-Token")) return null
//
//                // Add here your headers (could be good to import original request header here!!!)
//
//                // Add here your headers (could be good to import original request header here!!!)
//                val customHeaders: MutableMap<String, String> =
//                    HashMap<String, String>()
//                customHeaders["X-Auth-Token"] = token.toString();
//                view!!.loadUrl(resources.getString(R.string.lifeBooks_main), customHeaders)
//                return false
//
//            }
//                val intent = getParsedIntent(url)
//
//                return if (intent != null) {
//                    try {
//                        startActivity(intent)
//                        true
//                    } catch (ignored: ActivityNotFoundException) {
//                        Toast.makeText(activity,
//                            R.string.toast_app_not_found, Toast.LENGTH_SHORT).show()
//                        super.shouldOverrideUrlLoading(view, url)
//                    }
//                } else {
//                    super.shouldOverrideUrlLoading(view, url)
//                }

//
//            private fun getParsedIntent(url: String?): Intent? {
//                if (url == null) {
//                    return null
//                }
//
////                return Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
//            }
        }
        Log.d("testuDf", intent.getStringExtra("webViewURL")!!)
//        webView.loadUrl(intent.getStringExtra("webViewURL"))
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        if(webView.canGoBack()) {
            webView.goBack()
        } else{
            finish()
        }
    }

    inner class WebViewCallbackInterface {
        @JavascriptInterface
        fun webViewToApp(num1: Int, num2: Int): String {
            return "계산결과 : " + (num1 + num2)
        }

        @JavascriptInterface
        fun appToWebViewNative(): String {
            return "접근불가"
        }
    }

    private fun changeView(count : Int){
        Log.d("testCount",count.toString())
        when(count){
            1 -> {fab.visibility = View.VISIBLE
                appbar.visibility = View.GONE
                bottom_nav_view.visibility = View.GONE}

            2 -> {fab.visibility = View.GONE
                appbar.visibility = View.VISIBLE
                bottom_nav_view.visibility = View.GONE}

            3 -> {fab.visibility = View.GONE
                appbar.visibility = View.GONE
                bottom_nav_view.visibility = View.VISIBLE}

            4 -> {fab.visibility = View.GONE
                appbar.visibility = View.VISIBLE
                bottom_nav_view.visibility = View.VISIBLE}
        }
    }

    inner class authTokenAsyncTask : AsyncTask<Void, String, String>() {

        override fun onPostExecute(result: String?) {
//            super.onPostExecute(result)
            token = result
            val extraHeaders: MutableMap<String, String> =
                HashMap()
            extraHeaders["X-Auth-Token"] = token.toString()
            Log.d("testToken", extraHeaders.toString())
            webView.post(Runnable {
                webView.loadUrl(
                    resources.getString(R.string.lifeBooks_main),
                    extraHeaders
                )
            })
//            webView.loadUrl(resources.getString(R.string.lifeBooks_main),extraHeaders)
        }

        override fun onPreExecute() {
            Log.d("TestURDF", "result")

//            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): String {
            var json: String? = null
            var jsonObject: JSONObject = JSONObject()
            jsonObject.accumulate("highOrgCode", "ORG0000")
            jsonObject.accumulate("lowOrgCode", "ORG0000")
            jsonObject.accumulate("userId", "ecotest35")
            jsonObject.accumulate("userPw", "1234")
            json = jsonObject.toString()
//            val sb = StringBuffer()
//            sb.append("&highOrgCode="+ URLEncoder.encode("ORG0000", "UTF-8"))
//            sb.append("&lowOrgCode="+ URLEncoder.encode("ORG0000", "UTF-8"))
//            sb.append("&userId="+ URLEncoder.encode("ecotest35", "UTF-8"))
//            sb.append("&userPw="+ URLEncoder.encode("1234", "UTF-8"))

            var host: String = resources.getString(R.string.lifeBooks_host)
            val url = URL(host)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Content-type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.requestMethod = "POST"
            connection.readTimeout = 3000
            connection.connectTimeout = 3000
            connection.doOutput = true
            connection.useCaches = true
//            connection.doInput = true

            connection.setFixedLengthStreamingMode(json.toByteArray().size)

            val os: OutputStream = connection.outputStream
            val writer = BufferedWriter(
                OutputStreamWriter(os, "UTF-8")
            )
            writer.write(json)
            writer.flush()
            val responseCode: Int = connection.responseCode
            var line: String = ""
            var stream: InputStream? = null

            if (responseCode == 200) {
                val results = StringBuilder()
                BufferedReader(InputStreamReader(connection.inputStream)).forEachLine {
                    results.append(
                        it
                    )
                }
                val resultsAsString = results.toString()
                val JSONObject = JSONObject(resultsAsString)
                val token: String = JSONObject.getString("accessToken")

                return token
            } else return ""
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> true
        }
        return super.onOptionsItemSelected(item)
    }

    fun progressBarStart() {

        progressBar.visibility = View.VISIBLE
    }

    fun progressBarEnd() {
        progressBar.visibility = View.GONE
    }



}



class HttpResultHelper {
    var statusCode = 0
    var response: InputStream? = null

}
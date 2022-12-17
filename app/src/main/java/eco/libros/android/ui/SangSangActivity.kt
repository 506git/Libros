package eco.libros.android.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import android.webkit.*
import androidx.appcompat.app.AlertDialog
import eco.libros.android.R
import kotlinx.android.synthetic.main.activity_sang_sang.*
import org.altbeacon.beacon.*

class SangSangActivity : BaseActivity(), BeaconConsumer {
    companion object {
        private const val TAG = "SANG_SANG"
        private const val WEB_VIEW_CALLBACK_NAME = "Android"

        private const val PERMISSIONS_REQUEST_CODE = 100

        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private lateinit var beaconManager: BeaconManager

    // NOTE; 웹에서 비콘 대출버튼을 누르면 Android.beaconScanStart()룰 호츌해 이 값을 true로 초기화 시킨다고 함. 그래서 비콘 정보를 알려줄 때 대출버튼을 눌렀는지 확인을 하는 듯
    private var isFirstBeaconDetection = true
    // NOTE; 책바구니에 담긴 도서 수를 웹에서 관리하기 어려워 웹에서 Android.basketCnt()로 앱에 현재 책바구니를 알려주면 페이지 로딩마다 basketCntRefresh()를 호출해서 웹에 다시 알려줘야 한다고 함
    private var basketCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sang_sang)

        beaconManager = BeaconManager.getInstanceForApplication(this).apply {
            beaconParsers.clear()
            beaconParsers.add(BeaconParser().setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"))
        }

        initWebView()
    }

    override fun onResume() {
        super.onResume()

        webView.onResume()
        startScan()
    }

    override fun onPause() {
        stopScan()
        webView.onPause()

        super.onPause()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        webView.settings.javaScriptEnabled = true
        webView.addJavascriptInterface(WebViewCallback(), WEB_VIEW_CALLBACK_NAME)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                view?.evaluateJavascript("basketCntRefresh($basketCount)", null)
            }
        }
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        result?.confirm()
                    }
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                AlertDialog.Builder(activity)
                    .setMessage(message)
                    .setPositiveButton(R.string.confirm) { _, _ ->
                        result?.confirm()
                    }
                    .setNegativeButton(R.string.cancel) { _, _ ->
                        result?.cancel()
                    }
                    .show()
                return true
            }
        }

        goHome()
    }

    @SuppressLint("DefaultLocale")
    override fun onBeaconServiceConnect() {
        try {
            val rangeNotifier = RangeNotifier { beacons, region ->
                beacons?.forEach { beacon ->
                    Log.i(TAG, "UUID: ${beacon.id1}, Distance: ${"%.2f".format(beacon.distance)}m")
                    val uuidString = beacon.id1.toUuid().toString()
                    val beaconId = "${uuidString}_${beacon.id2}_${beacon.id3}".toUpperCase()

                    webView.evaluateJavascript(
                        "beaconDetect('$beaconId', ${beacon.rssi}, '$isFirstBeaconDetection')",
                        null
                    )

                    isFirstBeaconDetection = false
                }
            }
            // TEST;
            beaconManager.startRangingBeaconsInRegion(
                Region("eco-sangsang", null, null, null)
            )
            beaconManager.addRangeNotifier(rangeNotifier)
        } catch (e: RemoteException) {
        }

    }

    private fun startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && PERMISSIONS.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        ) {
            requestPermissions(PERMISSIONS, PERMISSIONS_REQUEST_CODE)
        } else {
            beaconManager.bind(this)
        }
    }

    private fun stopScan() {
        beaconManager.unbind(this)
    }

    private fun goHome() {
        webView.loadUrl("http://211.43.12.249:7777")
    }

    inner class WebViewCallback {
        @JavascriptInterface
        fun beaconScanStart() {
            isFirstBeaconDetection = true
        }

        @JavascriptInterface
        fun basketCnt(newCount: Int) {
            basketCount = newCount
        }

        @JavascriptInterface
        fun onBack() {
            handler.post {
                webView.goBack()
            }
        }

        @JavascriptInterface
        fun pageRefresh(option: Int) {
            handler.post {
                webView.reload()
            }
        }

        @JavascriptInterface
        fun home() {
            handler.post {
                goHome()
            }
        }
    }
}

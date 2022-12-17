package eco.libros.android.ui

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewStub
import android.widget.Toast
import androidx.fragment.app.FragmentContainerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.PendingDynamicLinkData
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.gun0912.tedpermission.PermissionListener
import eco.libros.android.R
import eco.libros.android.common.CustomFragmentManager
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ebook.EBookFragment
import eco.libros.android.home.fragment.HomeFragment
import eco.libros.android.myContents.MyContentFragment
import eco.libros.android.newBook.NewBookFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_e_book.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.webView
import org.json.JSONObject


class MainActivity : BaseActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val AUDIO_REQUEST_CODE = 3002
    }
    var context: Context? = null

    enum class Permission {
        LOCATION, CAMERA, BARCODE, WRITE_STORAGE, RECORD_AUDIO
    }

    private var originalBrightness: Float = 0.5f
    private var alert: View? = null
    private var progress: View? = null

    private var fragmentContainer: FragmentContainerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        context = this@MainActivity
        activity = this
        originalBrightness = window.attributes.screenBrightness
        alert = View.inflate(activity, R.layout.dialog, null)
        progress = findViewById<ViewStub>(R.id.progressBarTest).inflate()
        bottom_nav_view.setOnNavigationItemSelectedListener(this)
        bottom_nav_view.selectedItemId = R.id.btn_bottom_home

        fragmentContainer = findViewById<FragmentContainerView>(R.id.fragment_container)

        handleDeepLink()

    }

    private fun handleDeepLink() {
        FirebaseDynamicLinks.getInstance()
            .getDynamicLink(intent)
            .addOnSuccessListener(this,
                OnSuccessListener<PendingDynamicLinkData?> { pendingDynamicLinkData ->
                    if (pendingDynamicLinkData == null) {
                        Log.d("testNull", "No have dynamic link")
                        return@OnSuccessListener
                    }
                    val deepLink: Uri = pendingDynamicLinkData.link!!
                    val segment = deepLink.lastPathSegment
                    if (segment == "ebook"){
                        val deepLinkQuery = deepLink.getQueryParameter("contentsKey").toString().split("?")
                        val contentKey = deepLinkQuery[0]
                        val libCode = deepLinkQuery[1].split("libCode=")[1]
                        Log.d("testKey", "contentsKey = $contentKey libCode = $libCode")
                        val homeFragment = HomeFragment().apply {
                            arguments = Bundle().apply {
                                putString("contentsKey", contentKey)
                                putString("libCode", libCode)
                            }
                        }
                        CustomFragmentManager.addFragmentOnMain(this, homeFragment, "home");
                    }
                })
            .addOnFailureListener(this,
                OnFailureListener { e -> Log.w("testFailed", "getDynamicLink:onFailure", e) })
    }

    var permissionListener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            LibrosUtil.showCustomMsgWindow(this@MainActivity,
                "알림",
                "권한을 허용하지 않으면 서비스를 이용 할 수 없습니다.",
                "확인", false,
                DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
            )
        }
    }

//    override fun onBackPressed() {
//        when (supportFragmentManager.backStackEntryCount - 1) {
//            0 -> when{
////                fragmentContainer !=null &&
////                        fragmentContainer!!.childCount > 1-> {
////                        val childWebView = fragmentContainer!!.getChildAt(fragmentContainer?.childCount?.minus(1)!!)
////                    if (childWebView is WebView){
////                        childWebView.loadUrl("javascript:window.close();")
////                    }
////                }
//                System.currentTimeMillis() - lastBackPressedTime < HomeFragment.EXIT_APP_BACK_BUTTON_PERIOD -> (this as MainActivity).finish()
//                else -> {Toast.makeText(this, getString(R.string.toast_back_button_msg), Toast.LENGTH_SHORT).show()
//                    lastBackPressedTime = System.currentTimeMillis()}
//            }
//            else -> {
//                supportFragmentManager.popBackStackImmediate()
//                selectedFragment()
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GlobalVariable.AUDIO_REQUEST_CODE -> {
                if (data != null && resultCode == RESULT_OK) {
                    val result: String = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!![0]
                    val jobj = JSONObject()
                    jobj.put("searchData", result)
                    Log.d("TESTURLF", jobj.toString())
                    if (!TextUtils.isEmpty(result)) {
                        webView.evaluateJavascript(
                                "javascript:window.\$appInterfaceObject.RETURN_APP_EXEC(\"voiceSearch\", $jobj)",
                                null
                        )
                    }
                } else Toast.makeText(context, "오류", Toast.LENGTH_LONG).show()
            }
            GlobalVariable.NFC_REQUEST_CODE -> {
                if (data != null && resultCode == RESULT_OK) {
                    val jobj = JSONObject()
                    jobj.put("regNo", data?.extras?.getString("nfcTag"))
                    Log.d("TESTOBJ", jobj.toString())
                    webView.post {
                        webView.evaluateJavascript(
                            "javascript:window.\$appInterfaceObject.RETURN_APP_EXEC(\"nfcInfo\", $jobj)",
                            null
                        )
                    }
                }
            }
            GlobalVariable.BARCODE_EBOOK_ACTIVITY -> {
                if (data != null && resultCode == RESULT_OK) {
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
                                this,
                                getString(R.string.toast_barcode_error_msg),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.btn_bottom_home -> {
                val homeFragment = HomeFragment()
                CustomFragmentManager.addSettingFragmentOnMain(this, homeFragment, "home");
                return true
            }
            R.id.btn_bottom_newBook -> {
                val newBookFragment = NewBookFragment()
                CustomFragmentManager.addSettingFragmentOnMain(this, newBookFragment, "newBook")
                return true
            }
            R.id.btn_bottom_mobileCard -> {
                LibrosUtil.showMobileCard(this@MainActivity, this)
            }
            R.id.btn_bottom_ebook -> {
                val eBookFragment = EBookFragment()
                CustomFragmentManager.addSettingFragmentOnMain(this, eBookFragment, "eBook")
                return true
            }
            R.id.btn_bottom_myContents -> {
                val myContentFragment = MyContentFragment()
                CustomFragmentManager.addSettingFragmentOnMain(this, myContentFragment, "myContents")
                return true
            }
        }


        return false;
    }

    fun progressBarStart() {
        progress!!.visibility = View.VISIBLE
    }

    fun progressBarEnd() {
        progress!!.visibility = View.GONE

    }
}

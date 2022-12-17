package eco.libros.android.common.utill

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import android.webkit.WebView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.journeyapps.barcodescanner.CaptureActivity
import eco.libros.android.common.database.UserLibListDBFacade
import eco.libros.android.common.model.LibraryMenuListDataVo
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.home.fragment.HomeWebViewActivity
import eco.libros.android.nfc.NFCActivity
import org.json.JSONException
import org.json.JSONObject
import java.net.URISyntaxException

class MenuEvent(private val mWebView: WebView, mActivity: Activity) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var activity: Activity
        lateinit var appLinkType: String
    }

    init {
        activity = mActivity
    }

    fun doIconService(libCode: String?, menuId: String, authYn: String, linkType: String): String? {
        appLinkType = linkType
        val menu: LibraryMenuListDataVo? = UserLibListDBFacade(activity).getMenuInfo(
            libCode = libCode.toString(),
            menuId = menuId
        )

        if (menu == null && libCode == null) {
            return null
        }

        if (authYn.isNotEmpty() && authYn == "필요") {
            Log.d("TESTURL", libCode.toString())
            val objs: Array<Any?> = LibrosUtil.canUse(activity, libCode, "login")
            if (objs != null && objs[0] != null && objs[1] != null) {
                val canUse = objs[0] as Boolean

                if (!canUse) {
                    showLoginWindow(activity, libCode)
                    return null
                }
            } else {
//                canBtnClick = true
                LibrosUtil.showMsgWindow(activity, "알림", "아이콘 정보를 얻을 수 없습니다.", "확인")
                return null
            }
        }

        when (appLinkType) {
            "APP_VIEW", "앱화면" -> {
//                if (menuId.equals("NFC_LOAN")) {
//                    return menuId
//                }
                goAppView(menuId, menu?.pageLinkPath)
            }
            "APP_LINK" -> {
                showApplinkMsg(activity, menuId, menu?.pageLinkPath, libCode)
            }
            "WEB_HOME" -> {
                goWebHome(menu?.pageLinkPath)
            }
            "WEB_IFRAME" -> {
//                goWebIframe()
            }
            "WEB_BROWSER" -> {
                goWebBrowser(activity, menu?.pageLinkPath)
            }
            "WEB_POPUP", "웹화면" -> {
                goWebPopup(menu?.pageLinkPath)
            }
            "앱바코드", "APP_BARCODE" -> {
                openBarcode(activity, menu?.pageLinkPath)
            }
        }

        return null
//        canBtnClick = true
    }

    private fun goWebPopup(_pageLinkPath: String?) {
        var pageLinkPath = _pageLinkPath
        val libCode = LibrosUtil.getLibCode(activity)
        val libName = LibrosUtil.getLibName(activity,libCode)
        Log.d("TESTURLF", pageLinkPath!!)
        pageLinkPath = pageLinkPath.replace(
            "##portal_id##", LibrosUtil.getUserId(
                activity,
                true,
                needEncrypt = false
            ).toString()
        )
        pageLinkPath = pageLinkPath.replace(
            "##user_no##", LibrosUtil.getUserLibNo(
                activity,
                libCode
            )
        )
        pageLinkPath = pageLinkPath.replace("##user_id##", LibrosUtil.getLibId(activity, libCode))
        activity.startActivity(Intent(activity, HomeWebViewActivity::class.java).apply {
            putExtra("webViewURL", pageLinkPath)
            putExtra("libName",libName)
        })
    }

    private fun goAppView(menuId: String, pageLinkPath: String?) {
        when (menuId) {
            "MOBILE_CARD_NEW" -> {
                LibrosUtil.showMobileCard(activity as FragmentActivity, activity)
            }
            "NFC_LOAN" ->{
                activity.startActivityForResult(Intent(activity, NFCActivity::class.java).apply {
                    putExtra("nfcType",appLinkType)
                }, GlobalVariable.NFC_REQUEST_CODE)
                Toast.makeText(activity,"NFC",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openBarcode(activity: Activity, pageLinkPath: String?) {
        val pm = activity.packageManager

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            TedPermission.with(activity)
                .setPermissions(Manifest.permission.CAMERA)
                .setPermissionListener(object : PermissionListener {
                    override fun onPermissionGranted() {
                        val barcodeIntent = Intent(activity, CaptureActivity::class.java)
                        barcodeIntent.putExtra("pageLink", pageLinkPath)
                        activity.startActivityForResult(
                            barcodeIntent,
                            GlobalVariable.BARCODE_EBOOK_ACTIVITY
                        )
                    }

                    override fun onPermissionDenied(deniedPermissions: List<String>) {}
                })
                .check()
        } else {
            LibrosUtil.showMsgWindow(activity, "알림", "카메라 기능을 사용할 수 없습니다.", "확인")
        }
    }

    private fun goWebHome(pageLinkPath: String?) {
        val resultMap = mapOf<String, String?>(
            "pageLink" to pageLinkPath
        )
        mWebView.post {
            mWebView.evaluateJavascript(
                "javascript:window.\$appInterfaceObject.RETURN_APP_EXEC('menuEvent', ${
                    LibrosUtil.getSimpleJson(
                        resultMap
                    )
                })",
                null
            )
        }
    }

    private fun goWebBrowser(activity: Activity, urlPath: String?) {
        var urlPath = urlPath
        val libCode: String? = LibrosUtil.getLibCode(activity)

        urlPath = urlPath?.replace(
            "##portal_id##",
            LibrosUtil.getUserId(activity, needEncoding = true, needEncrypt = false).toString()
        )
        urlPath = urlPath?.replace("##user_no##", LibrosUtil.getUserLibNo(activity, libCode))
        urlPath = urlPath?.replace("##user_id##", LibrosUtil.getLibId(activity, libCode))
        activity.startActivity(Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(urlPath)
        })
    }

    private fun showLoginWindow(activity: Activity, libCode: String?) {
        val myActivity = activity as FragmentActivity
        val string: String = UserLibListDBFacade(activity).getCertifyKind(libCode.toString())
        LibrosUtil.showLoginWindow(myActivity.supportFragmentManager, libCode, string)
    }

    private fun showApplinkMsg(
        activity: Activity,
        menuId: String,
        linkPath: String?,
        libCode: String?
    ) {
        try {
            AlertDialog.Builder(activity).apply {
                setTitle("알림")
                setMessage("앱을 연동 하시겠습니까?")
                setPositiveButton("확인", DialogInterface.OnClickListener { dialogInterface, i ->
                    dialogInterface.dismiss()
                    openOtherApp(activity, linkPath, libCode, menuId)
                })
                setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
//                    canBtnClick = true
                    dialogInterface.dismiss()
                })
                setCancelable(false)
                create().show()
            }
        } catch (e: WindowManager.BadTokenException) {
            LibrosLog.print(e.toString())
        }
    }

    private fun openOtherApp(
        activity: Activity,
        linkPath: String?,
        libCode: String?,
        menuId: String
    ) {
        if (linkPath == null) {
            return
        }

        var appLink: String? = null

        try {
            val jObj = JSONObject(linkPath)
            if (jObj != null && jObj.has("android")) {
                appLink = jObj.getString("android")
            }
        } catch (e: JSONException) {
            LibrosLog.print(e.toString())
        }

        var intent: Intent? = null

        try {
            intent = Intent.parseUri(appLink, Intent.URI_INTENT_SCHEME)
            activity.startActivity(Intent.parseUri(appLink, 0))
        } catch (e: URISyntaxException) {
            LibrosLog.print(e.toString())
        } catch (e: ActivityNotFoundException) {
            if (intent != null && intent.`package` != null) {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + intent.`package`)
                    )
                )
                LibrosLog.print(e.toString())
            }
        }
    }
}
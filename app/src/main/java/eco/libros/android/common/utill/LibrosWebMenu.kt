package eco.libros.android.common.utill

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.gson.Gson
import com.google.zxing.integration.android.IntentIntegrator
import eco.libros.android.R
import eco.libros.android.common.database.*
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibropiaNoticeDTO
import eco.libros.android.common.model.UserLibListDataVO
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ebook.MyLibManager
import eco.libros.android.gps.GpsTracker
import eco.libros.android.myContents.MyEbookListModel
import eco.libros.android.nfc.NFCActivity
import eco.libros.android.settings.SettingsMainActivity
import eco.libros.android.ui.MainActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kr.co.smartandwise.eco_epub3_module.Drm.markany.FileManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

class LibrosWebMenu constructor(context: Context, webView: WebView) {
    private val mContext: Context by lazy { context }
    private val mWebView: WebView by lazy { webView }
    private lateinit var mActivity : Activity

    fun exec(mActivity: FragmentActivity, menu: String, data: String?) {
        this.mActivity = mActivity
        when (menu) {
            "scanBarcode" -> {
                if (checkPermission(MainActivity.Permission.BARCODE, mActivity)) {
                    openBarcodeView(mActivity)
                }
            }
            "openSettings" -> {
                Intent(mActivity, SettingsMainActivity::class.java).apply {
                    startActivity(mActivity, this, null)
                }
            }

            "voiceSearch" -> {
                if (checkPermission(MainActivity.Permission.RECORD_AUDIO, mActivity)) {
                    mWebView.post {
                        val mRecognizer = SpeechRecognizer.createSpeechRecognizer(mActivity)
                        openVoiceSearch(mRecognizer, mActivity)
                    }
                }
            }
            "nfcInfo" -> {
                MenuEvent.activity.startActivityForResult(Intent(MenuEvent.activity, NFCActivity::class.java).apply {
                    putExtra("nfcType", MenuEvent.appLinkType)
                }, GlobalVariable.NFC_REQUEST_CODE)
                Toast.makeText(MenuEvent.activity, "NFC", Toast.LENGTH_LONG).show()
            }
            "nfcLoan" -> {

            }
            "goMyContents" -> {
                CoroutineScope(Main).launch {
                    (mActivity as MainActivity).bottom_nav_view.menu.findItem(R.id.btn_bottom_myContents).isChecked = true
                    (mActivity as MainActivity).bottom_nav_view.selectedItemId = R.id.btn_bottom_myContents
                }
            }
            "downloadEBook" -> {
                CoroutineScope(Main).launch {
                    val gson = Gson()
                    val list = gson.fromJson(data, MyEbookListModel::class.java)
                    EBookDownloadTask(
                            _activity = mActivity,
                            _ebookData = list,
                            _downloadPlace = "detail"
                    ).downloadEBook()
                }
            }
            "readEBook" -> {
                CoroutineScope(Main).launch {
                    val lentKey = JSONObject(data).getString("lent_key")
                    Log.d("TESTLENTK", lentKey)
                    MyLibManager().showEBook(
                            activity = mActivity,
                            lentKey = lentKey,
                            myLibraryMainUrl = ""
                    )
                }
            }
            "menuEvent" -> {
                val menuId = JSONObject(data).getString("menuId")
                val authYn = JSONObject(data).getString("authYnDesc")
                val appLinkType = JSONObject(data).getString("appLinkType")
                LibrosUtil.getLibCode(mActivity)
                val resultMenu = MenuEvent(mWebView, mActivity).doIconService(LibrosUtil.getLibCode(mActivity), menuId, authYn, appLinkType)
                if (resultMenu != null) {
                    when (resultMenu) {
                        "NFC_LOAN" -> {
                            val menuMap = mapOf<String, String?>("menuId" to GpsTracker(mActivity).getLatitude().toString())
                            webViewExec(alias = "menuId", obj = LibrosUtil.getSimpleJson(menuMap))
                        }
                    }
                }
            }
        }
    }

    fun setData(activity: FragmentActivity, menu: String, data: String) {
        this.mActivity = activity
        when (menu) {
            "setLibrary" -> {
//                val libCode = JSONObject(data).getString("libCode")
                val libCode = "111042"
                LibrosUtil.setLastLib(activity, libCode)
            }
            "addLibrary" -> {
                addLibrary(activity, data)
            }
            "deleteLibrary" -> {
                val libCode = JSONObject(data).getString("libCode")
                if (LibrosUtil.getLibCode(activity).equals(libCode)) {
                    LibrosUtil.setLastLib(activity, "000000")
                }
                val result = UserLibListDBFacade(activity).deleteLib(libCode)
                val resultMap =
                        if (result == "Y") {
                            mapOf<String, String?>("resultCode" to result, "resultMessage" to "")
                        } else {
                            mapOf<String, String?>("resultCode" to "N", "resultMessage" to result)
                        }
                webViewSetData(alias = menu, obj = LibrosUtil.getSimpleJson(resultMap))
            }
            "returnEBook" -> {
                CoroutineScope(IO).launch {
                    val lentKey = JSONObject(data).getString("lent_key")
                    val libCode = JSONObject(data).getString("lib_code")
                    var deleteResult = "N"
                    try {
                        if (libCode.isNotEmpty() && lentKey.isNotEmpty()) {
                            val eBook = EbookDownloadDBFacade(activity).getDownloadFileName(lentKey)
                            EbookDownloadDBFacade(activity).deleteBook(libCode, lentKey)
                            ViewerDBFacade(activity).deleteBook(lentKey)

                            if (eBook.downloadYn == "Y" && eBook.fileName.isNotEmpty()) {
                                val path: String =
                                        if (eBook.fileType.isNotEmpty() && eBook.fileType.equals("PDF", true))
                                            "${activity.getExternalFilesDir(null)?.absolutePath}/"
                                        else
                                            "${LibrosUtil.getEPUBRootPath(activity)}/${mContext.resources.getString(R.string.sdcard_dir_name)}/"

                                val file = File(path, eBook.fileName)

                                if (file.exists()) {
                                    FileManager.deleteFolder(file)
                                }
                                deleteResult = "Y"
                            }
                        }
                    } catch (e: Exception) {
                        deleteResult = e.toString()
                    } finally {
                        val resultMap =
                                if (deleteResult == "Y") {
                                    mapOf<String, String?>("resultCode" to deleteResult, "resultMessage" to "")
                                } else {
                                    mapOf<String, String?>("resultCode" to "N", "resultMessage" to deleteResult)
                                }
                        webViewSetData(alias = "returnEBook", obj = LibrosUtil.getSimpleJson(resultMap))
                    }
                }
            }
        }
    }

    fun getData(activity: FragmentActivity, menu: String, data: String) {
        this.mActivity = activity
        when (menu) {
            "getLibList" -> {
                webViewGetData(alias = menu, obj = LibrosUtil.getLibList(mActivity))
            }

            "getLibMenuList" -> {
                CoroutineScope(IO).launch {
                    webViewGetData(alias = menu, obj = LibrosUtil.getLibMenuList(mActivity, data))
                    noticeTask(activity)
                }
            }

            "getLoginInfo" -> {
                mWebView.post {
                    val loginInfoMap = mapOf<String, String?>(
                            "librosId" to LibrosUtil.getUserId(
                                    mActivity,
                                    needEncoding = true,
                                    needEncrypt = true
                            ),
                            "libCode" to LibrosUtil.getLibCode(mActivity),
                            "deviceType" to mContext.resources.getString(R.string.device_type),
                            "deviceId" to LibrosUtil.getOriginDeviceId(activity),
                            "startUrl" to "http://220.72.184.140:3006/library.html"
                    )
                    webViewGetData(
                            alias = menu, obj = LibrosUtil.getSimpleJson(
                            loginInfoMap
                    )
                    )
                }
            }

            "getCurrentPosition" -> {
                if (checkPermission(MainActivity.Permission.LOCATION, activity)) {
                    val locationMap = mapOf<String, String?>(
                            "latitude" to GpsTracker(mActivity).getLatitude().toString(),
                            "longitude" to GpsTracker(mActivity).getLongitude().toString()
                    )
                    webViewGetData(
                            alias = menu, obj = LibrosUtil.getSimpleJson(
                            locationMap
                    )
                    )
                }
            }
            "getEBookList" -> {
                CoroutineScope(IO).launch {
                    webViewGetData(alias = menu, obj = downloadEBookList())
                }
            }
        }
    }

    private fun addLibrary(activity: FragmentActivity, data: String) {
        val gson = Gson()
        val list = gson.fromJson(data, UserLibListDataVO::class.java)
        UserLibListDBFacade(activity).insertData(list)
    }

    private suspend fun noticeTask(activity: FragmentActivity) {
        var noticeResult: LibropiaNoticeDTO? = null
        val libCode = LibrosUtil.getLibCode(activity)!!

        withContext(Dispatchers.IO) {
            val librosRepository = LibrosRepository()
            librosRepository.getNoticeRepo(libCode, "003")?.let { response ->
                if (response.isSuccessful) {
                    noticeResult = response.body()
                }
            }
        }

        if (noticeResult != null && noticeResult!!.contents != null) {
            val libNotice = noticeResult!!.contents
            if (libNotice?.noticeContent != null) {
                NoticeDBFacade(activity).isShowMsg(libCode)
                withContext(Main) {
                    showNoticeMsg(libNotice.noticeTitle, libNotice.noticeContent, libCode, NoticeDBFacade(activity).isShowMsg(libCode))
                }
            } else {
                NoticeDBFacade(activity).removeLib(libCode)
            }
        } else {
            NoticeDBFacade(activity).removeLib(libCode)
        }


        if (noticeResult != null) {
            Log.d("testRESULT", noticeResult.toString())
        }
    }

    fun showNoticeMsg(title: String?, content: String?, libCode: String?, isShowMsg: Boolean) {
        if (isShowMsg) {
            AlertDialog.Builder(mActivity)
                    .setTitle(title)
                    .setMessage(content)
                    .setPositiveButton("하루동안 보지 않기", DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                        if (libCode != null) {
                            NoticeDBFacade(mContext).insertLib(libCode)
                        }
                    })
                    .setNegativeButton("닫기", DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                    })
                    .create().show()
        }
    }

    fun webViewExec(alias: String, obj: JSONObject) {
        mWebView.post {
            mWebView.evaluateJavascript(
                    "javascript:window.\$appInterfaceObject.RETURN_APP_EXEC('$alias', $obj)",
                    null
            )
        }
    }

    private fun webViewGetData(alias: String, obj: JSONObject) {
        mWebView.post {
            mWebView.evaluateJavascript(
                    "javascript:window.\$appInterfaceObject.RETURN_APP_GETDATA('$alias', $obj)",
                    null
            )
        }
    }

    fun webViewSetData(alias: String, obj: JSONObject) {
        mWebView.post {
            mWebView.evaluateJavascript(
                    "javascript:window.\$appInterfaceObject.RETURN_APP_SETDATA('$alias', $obj)",
                    null
            )
        }
    }

    private suspend fun downloadEBookList(): JSONObject {
        val eBookList = withContext(IO) {
            //전자책 기간 체크
            EbookDownloadDBFacade(mActivity).getDownloadEBookCheck()
            checkSqlite()
            EbookDownloadDBFacade(mActivity).getDownloadEBookYn()
        }

        val jsonArray = JSONArray()
        for (i in eBookList) {
            val obj = JSONObject()
            obj.put("lent_key", i.lentKey)
            obj.put("downloadYn", i.downloadYn)
            jsonArray.put(obj)
        }
        val ebookObj = JSONObject()
        ebookObj.put("eBookList", jsonArray)

        return ebookObj
    }

    //SQLite로 파일 검색해서 없으면 삭제
    private fun checkSqlite() {

    }

    private fun checkExpireBook() {

    }

    fun openVoiceSearch(mRecognizer: SpeechRecognizer?, activity: FragmentActivity) {
        mRecognizer?.startListening(
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, mActivity.packageName)
                    putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "책 이름을 말해주세요.")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    activity.startActivityForResult(this, GlobalVariable.AUDIO_REQUEST_CODE)
                }
        )
        mRecognizer?.stopListening()
    }

    private fun openBarcodeView(activity: FragmentActivity) {
        IntentIntegrator.forSupportFragment(activity.getCurrentFragment())
                .setRequestCode(GlobalVariable.BARCODE_SCAN)
                .setBeepEnabled(false)
                .setOrientationLocked(true)
                .initiateScan()
    }

    private fun FragmentActivity.getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentById(R.id.fragment_container)!!
    }

    private fun checkPermission(permission: MainActivity.Permission, activity: FragmentActivity): Boolean {
        val code: String
        val requestCode: Int

        when (permission) {
            MainActivity.Permission.CAMERA -> {
                code = Manifest.permission.CAMERA
                requestCode = GlobalVariable.PERMISSION_REQUEST_CAMERA_CODE
            }
            MainActivity.Permission.LOCATION -> {
                code = Manifest.permission.ACCESS_FINE_LOCATION
                requestCode = GlobalVariable.PERMISSION_REQUEST_LOCATION_CODE
            }
            MainActivity.Permission.BARCODE -> {
                code = Manifest.permission.CAMERA
                requestCode = GlobalVariable.PERMISSION_REQUEST_BARCODE_CODE
            }
            MainActivity.Permission.WRITE_STORAGE -> {
                code = Manifest.permission.WRITE_EXTERNAL_STORAGE
                requestCode = GlobalVariable.PERMISSION_REQUEST_WRITE_CODE
            }
            MainActivity.Permission.RECORD_AUDIO -> {
                code = Manifest.permission.RECORD_AUDIO
                requestCode = GlobalVariable.PERMISSION_REQUEST_AUDIO
            }

        }

        if (ContextCompat.checkSelfPermission(mActivity, code) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(code), requestCode)
        } else {
            return true
        }
        return false
    }

    private fun getFilePath(fileType: String?): String? {
        return if (fileType != null && fileType.equals("PDF", ignoreCase = true)) {
            "${mActivity.getExternalFilesDir(null)?.absolutePath}/"
        } else {
            "${LibrosUtil.getEPUBRootPath(mActivity)}/${mActivity.resources.getString(R.string.sdcard_dir_name)}/"
        }
    }
}
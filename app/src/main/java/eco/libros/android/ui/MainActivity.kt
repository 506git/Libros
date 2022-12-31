package eco.libros.android.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import eco.libros.android.R
import eco.libros.android.common.utill.EBookDownloadTask
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.myContents.MyEbookListModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_CONNECT_ERROR
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(){
    lateinit var mSocket: Socket
    val LIBORS_PERMISSION_BASIC = 1
    var BASE_URL = "http://121.130.28.115:8000/ebook_worker"

    private val PERMISSIONS = arrayOf(
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private val permissionList = mutableListOf<String>()

    override fun onResume() {
        super.onResume()
        if (mSocket.connected()){
            sendStatus("ready")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermission()
        mSocket = IO.socket(BASE_URL).connect()
//        val yesData = "{\"thumbnail\":\"https://epbook.eplib.or.kr/resources/images/yes24/Msize/112361381M.jpg\",\"author\":\"심윤경 저\",\"lent_key\":\"1506740\",\"return_key\":\"238298\",\"epubID\":\"1506740\",\"title\":\"나의 아름다운 할머니\",\"return_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=112361381&proc_mode=return&lent_key=238298&udid=\",\"book_info_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_010_ebook_detail_json?version=v20&lib_code=111042&id=112361381&udid=&user_id=ecotest&portal_id=\",\"comcode\":\"YES24\",\"cover\":\"https://epbook.eplib.or.kr/resources/images/yes24/Msize/112361381M.jpg\",\"ebook_lib_name\":\"은평구립공공도서관\",\"extending_count\":\"0\",\"lending_date\":\"2022-12-20\",\"lending_expired_date\":\"2022-12-26\",\"drm_url_info\":\"http://epbook.eplib.or.kr:8088/\",\"ISBN\":\"9791160949674\",\"platform_type\":\"\",\"lib_code\":\"111042\",\"file_type\":\"EPUB\",\"publisher\":\"사계절\",\"extension_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=112361381&proc_mode=extension&lent_key=238298&udid=\",\"id\":\"112361381\",\"drm_key\":\"1506740\"}"
        mSocket.on(io.socket.client.Socket.EVENT_CONNECT) {
            Log.d("test2", "connect")
            sendStatus("connect")
            sendStatus("ready")
        }.on(Socket.EVENT_DISCONNECT) { args ->
            Log.d("test2", "disconnect" + args[0])
        }.on(EVENT_CONNECT_ERROR) { args ->
            Log.d("test2", "err" + args[0])
        }

        mSocket.on("request_download", Emitter.Listener { args ->
            val data = args[0].toString()
            CoroutineScope(Dispatchers.Main).launch {
                sendStatus("working")
                val gson = Gson()
                val list = gson.fromJson(data, MyEbookListModel::class.java)
//                Log.d("TESTWORKINGdata", data.toString())
                Log.d("TESTWORKINGlist", list.toString())
                EBookDownloadTask(
                    _activity = this@MainActivity,
                    _ebookData = list,
                    _downloadPlace = "detail",
                    mSocket
                ).downloadEBook()
            }
        })
    }

    fun sendStatus(message : String){
        if(mSocket.connected()){
            mSocket.emit("status", message)
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 26) {
            val pm: PackageManager = packageManager
            Log.e("package Name", packageName)
            if (!pm.canRequestPackageInstalls()) {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            }
        }

        for (p in PERMISSIONS){
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(p)
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, p)){
                    LibrosUtil.showMsgWindow(this, "알림", resources.getString(R.string.deny_message_essential_permission), "확인")
                }
            }
        }
        Log.d("TESTPERMISIION", permissionList.toString())
        if(permissionList.isNotEmpty() && permissionList.size != 0){
            requestPermission()
        } else{

        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), LIBORS_PERMISSION_BASIC)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == LIBORS_PERMISSION_BASIC && (grantResults.isNotEmpty())){
            val list = mutableListOf<String>()
            grantResults.forEach { i ->
                if (i == PackageManager.PERMISSION_DENIED){
                    AlertDialog.Builder(this).setTitle("권한 설정").setMessage("앱에서 요구하는 권한 설정이 필요합니다\n" +
                            " [설정] -> [권한]에서 사용으로 활성화 해주세요.")
                        .setPositiveButton("설정") { dialog, which ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                dialog.dismiss()
                                startActivityForResult(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:$packageName")), 2001)
                            }
                        }
                        .setNegativeButton("취소", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.dismiss()
                            finish()
                        }).create().show()
                    return
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}

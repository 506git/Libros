package eco.libros.android.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import eco.libros.android.R
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.crypt.RoutingCheck
import eco.libros.android.common.database.DefaultDBHelper
import eco.libros.android.common.database.UserLibListDBFacade
import eco.libros.android.common.http.LibrosJsonNetwork
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibListModelVO
import eco.libros.android.common.model.LibrosDataVO
import eco.libros.android.common.model.UserLibListDataVO
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.databinding.ActivityIntroBinding
import eco.libros.android.login.LogInSettingActivity
import kotlinx.android.synthetic.main.activity_intro.*
import kotlinx.android.synthetic.main.fragment_pw_find.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.IOException
import java.util.*

class IntroActivity : BaseActivity() {
    companion object{
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        private var userId: String? = null
        private var userPw: String? = null
        private val PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
    private val permissionList = mutableListOf<String>()
    private lateinit var binding: ActivityIntroBinding

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GlobalVariable.LIBROS_LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                CoroutineScope(Main).launch {
                    libListTask()
                }
            } else {
                finish()
            }
        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == GlobalVariable.LIBORS_PERMISSION_BASIC && (grantResults.isNotEmpty())){
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
            connectionCheck(this)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)
        binding.permissionConfirm.setOnClickListener {
            requestPermission()
        }
        context = this@IntroActivity
        if (RoutingCheck.checkUser()) {
            LibrosUtil.showCustomMsgWindow(this, "경고", "루팅 기기는 이용할 수 없습니다.", "확인", false,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    })
        } else {
            checkPermission()
            CoroutineScope(IO).launch {
                firebasePush()
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 26) {
            val pm: PackageManager = context.packageManager
            Log.e("package Name", packageName)
            if (!pm.canRequestPackageInstalls()) {
                startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName")))
            }
        }

        for (p in PERMISSIONS){
            if (ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED){
                permissionList.add(p)
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, p)){
                    LibrosUtil.showMsgWindow(activity, "알림", resources.getString(R.string.deny_message_essential_permission), "확인")
                }
            }
        }
        Log.d("TESTPERMISIION", permissionList.toString())
        if(permissionList.isNotEmpty() && permissionList.size != 0){
            binding.permissionContainer.visibility = View.VISIBLE
        } else{
            binding.permissionContainer.visibility = View.GONE
            connectionCheck(this)
        }
    }

    private fun requestPermission(){
        ActivityCompat.requestPermissions(this, permissionList.toTypedArray(), GlobalVariable.LIBORS_PERMISSION_BASIC)
    }

//    private suspend fun sqlVersionCheck() {
//        val result = withContext(IO) {
//            var sb = "${resources.getString(R.string.url_libropia_web)}serviceName=MB_22_01_01_SERVICE&deviceType=003"
//            LibrosData().getSqliteVersion(sb)
//        }
////        Log.d("TESTRESULT",result?.getContent() as String)
//        val result2 = result?.getContent() as Array<*>
//        var storedVersion = LibrosUtil.getSharedData(this, resources.getString(R.string.version_default_db))
//
//        if (storedVersion?.isEmpty() == true) {
//            DefaultDBHelper(context).initCreateDataBase()
//            LibrosUtil.setSharedData(this, resources.getString(R.string.version_default_db), "0")
//            storedVersion = "0"
//        }
//
//
//        var userLastLibCode = LibrosUtil.getLibCode(context)
//
//
//        if (userLastLibCode?.isEmpty() == true) {
//            userLastLibCode = "000000"
//        }
//
//        val userId = LibrosUtil.getUserId(context, true, needEncrypt = true)
//
//        val str = "${resources.getString(R.string.url_libropia_web)}serviceName=MB_12_01_01_SERVICE&deviceType=003&encryptFromId=Y" +
//                "&userId=${userId}&libCode=${userLastLibCode}"
//        LibrosData().getSimpleJsonResult(str)
//
//        val serverVersion = Integer.parseInt(result2[1].toString())
//        if (storedVersion?.trim()?.isNotEmpty() == true){
//            val version = Integer.parseInt(storedVersion)
//            if (serverVersion >= version){
//                sqliteUpdateTask(result2[0].toString(), result2[1].toString())
//            }
//        }
//
//    }

    private fun sqliteUpdateTask(dbUrl: String, serverVersion: String) {
        showProgress("업데이트 중입니다")

        val network = LibrosJsonNetwork()
        val newSqlFile: ByteArray? = network.getSqlFile(resources.getString(R.string.url_libropia) + dbUrl)

        if (newSqlFile != null && newSqlFile.isNotEmpty()) {
            DefaultDBHelper(context).updateDataBase(newSqlFile)
            LibrosUtil.setSharedData(context, resources.getString(R.string.version_default_db), serverVersion)
        }

        hideProgress()
    }

    private suspend fun firebasePush() {
        withContext(Dispatchers.IO) {
            Firebase.messaging.token.addOnCompleteListener(OnCompleteListener { task ->
                kotlin.runCatching {
                    if (!task.isSuccessful) {
                        throw Error("Fetching FCM registration token failed")
                    }
                }.onSuccess {
                    LibrosUtil.setSharedData(context, "pushToken", task.result)
                }.onFailure {
                    Log.e("TestToken", "Fetching FCM registration token failed", task.exception)
                }
            })
        }
    }

    private fun connectionCheck(context: Context) {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    result = when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            2
                        }
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            1
                        }
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                            3
                        }
                        else -> throw Error("Network failed")
                    }
                }
            }
        } else {
            cm?.run {
                cm.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> {
                            2
                        }
                        ConnectivityManager.TYPE_MOBILE -> {
                            1
                        }
                        ConnectivityManager.TYPE_VPN -> {
                            3
                        }
                        else -> throw Error("Network failed")
                    }
                }
            }
        }
        if (result != 0) {
            CoroutineScope(Main).launch {
                version()
            }
        } else {
            LibrosUtil.showCustomMsgWindow(this, "알림", "네트워크 연결이 안될 시 앱 사용에 지장이 있습니다.\n네트워크 연결을 확인해주세요.", "확인", false,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    })
        }
    }

    private suspend fun version() {
        showProgress("앱 버전 확인 중입니다.")
        val result = withContext(Dispatchers.IO) {
            try {
                val version = Jsoup.connect("https://play.google.com/store/apps/details?id=eco.libros.android").get().select(".htlgb").eq(6)
                for (mElement: Element in version) {
                    mElement.text().trim()
                }
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
        val version = packageManager.getPackageInfo(packageName, 0).versionName
        hideProgress()
        when {
            result.toString() == "null" -> {
                appInit()
            }
            TextUtils.isEmpty(result.toString()) -> {
                LibrosUtil.showCustomMsgWindow(this@IntroActivity, "오류", "현재 버전을 가져올수 없습니다.", "확인", true,
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                            appInit()
                        })
            }
            version != result.toString() -> {
                LibrosUtil.showCustomWindowBtn(this@IntroActivity,"업데이트 알림","최신버전의 앱이 존재합니다. \n 마켓으로 이동 하시겠습니까?",  "확인",
                    DialogInterface.OnClickListener { dialog, i ->
                        dialog.dismiss()
                        Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("market://details?id=" + application.packageName)
                            startActivity(this)
                            finish()
                        }
                    }, DialogInterface.OnClickListener { dialogInterface, i ->
                        dialogInterface.dismiss()
                        appInit()
                    })
            }
            else -> {
                appInit()
            }
        }
    }

    private fun appInit() {
        moveToMain()
    }

    private fun moveLoginActivity() {
        startActivityForResult(Intent(this, LogInSettingActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION) },GlobalVariable.LIBROS_LOGIN_ACTIVITY)
    }

    inner class NoticeTask {
        private var progressFragment = ProgressFragment()
        private var result: LibrosDataVO? = null
        fun task() {
            try {

            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
                return
            }
        }
    }

    private suspend fun logInTask(userId: String, userPw: String) {
        showProgress("로그인 중입니다")
        val result = withContext(IO) {
            if (userId.isEmpty() && userPw.isEmpty()) {
                hideProgress()
            }

            val crypt = PasswordCrypt(context).apply {init() }

            var tempId = LibrosUtil.encryptedTxt(userId, crypt)
            var tempPw = LibrosUtil.encryptedTxt(userPw, crypt)
            var encryptYn = "Y"

            if (tempId  == "N" || tempPw == "N"){
                tempId = userId
                tempPw = userPw
                encryptYn = "N"
            }

            tempId = LibrosUtil.getEncodedStr(tempId).toString()
            tempPw = LibrosUtil.getEncodedStr(tempPw).toString()


            val librosRepository = LibrosRepository()
//            librosRepository.getLoginRepo(tempId, tempPw, encryptYn, LibrosUtil.getSharedData(context, "pushToken")!!,
//                LibrosUtil.getOriginDeviceId(context)!!, resources.getString(R.string.device_type))?.let { response ->
//                if (response.isSuccessful) {
//                    return@withContext response.body()
//                } else
//                    return@withContext null
//            }

            librosRepository.getLibLoginRepo(tempId, userPw, "Y", LibrosUtil.getOriginDeviceId(context)!!, resources.getString(R.string.device_type))?.let { response ->
                if (response.isSuccessful) {
                    return@withContext response.body()
                } else
                    return@withContext null
            }
        } as LibListModelVO
        hideProgress()

        if (result.result != null) {
            if (result.result.resultCode == "N") {
                LibrosUtil.delUserId(context, context.resources.getString(R.string.libros_lib_user_pw))
                LibrosUtil.delSharedData(context, context.resources.getString(R.string.libros_lib_user_pw))
                moveLoginActivity()
            } else if (result.result.resultCode == "Y") {
                libListTask()
            }
        } else if (result == null) {
            moveLoginActivity()
        } else {
            LibrosUtil.showCustomMsgWindow(this@IntroActivity, "오류", result.result.resultMessage, "확인", true,
                DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        moveLoginActivity()
                    })
        }

    }

    private suspend fun libListTask() {
        showProgress("도서관 정보\n 불러오는 중입니다.")
//        var serviceResult: LibListModelVO? = null
        val serviceResult = withContext(IO) {
            val librosRepository = LibrosRepository()
            librosRepository.getRepositories(LibrosUtil.getUserId(context, needEncoding = false, needEncrypt = true)!!, "Y", "003"
            )?.let { response ->
                if (response.isSuccessful) {
                    return@withContext response.body()
                }
            }
        } as LibListModelVO

        hideProgress()
        if (serviceResult.result != null && serviceResult.result.resultCode == "Y") {
            withContext(IO) {
                val userList = serviceResult.contents.userLibraryInfoList as List<UserLibListDataVO>
                for (i in userList) {
                    UserLibListDBFacade(context).insertData(i)
                }
            }
            moveToMain()
        } else if (serviceResult == null) {
            moveToMain()
        } else {
            LibrosUtil.showCustomMsgWindow(this@IntroActivity, "오류", serviceResult.result.resultMessage, "확인", true,
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.dismiss()
                        moveToMain()
                    })
        }
    }

    private fun moveToMain() {
//        UserLibListDBFacade(context).libListCheck(LibrosUtil.getLibCode(context)!!)
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            finish()
        })
    }

    fun getAppContext(): Context {
        return context
    }
}
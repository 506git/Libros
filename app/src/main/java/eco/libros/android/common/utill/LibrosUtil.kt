package eco.libros.android.common.utill

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager.BadTokenException
import androidx.fragment.app.FragmentManager
import eco.libros.android.R
import eco.libros.android.common.crypt.Cryptchar
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.database.UserLibListDBFacade
import eco.libros.android.common.model.LibraryMenuListDataVo
import eco.libros.android.common.model.UserLibListDataVO
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.lang.reflect.InvocationTargetException
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

object LibrosUtil {

    fun getOriginDeviceId(context: Context): String? {
        val tmDevice: String
        val tmSerial: String
        val androidId: String
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceUuid: UUID
        var deviceId: String? = null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Secure.getString(
                context.contentResolver, Secure.ANDROID_ID
            )
            Log.d("testsddddf", deviceId)
            deviceId
        } else {
            tmDevice = "" + tm.deviceId
            tmSerial = "" + tm.simSerialNumber
            androidId = "" + Secure.getString(context.contentResolver, Secure.ANDROID_ID)
            deviceUuid = UUID(
                androidId.hashCode().toLong(),
                tmDevice.hashCode().toLong() shl 32 or tmSerial.hashCode().toLong()
            )
            deviceId = deviceUuid.toString()
            Log.d("testsddd22df", deviceId)
            deviceId.replace("-".toRegex(), "")
        }
//        return deviceId.replaceAll("-", "");
    }

    fun getDecodeImage(imageArray: ByteArray): ByteArray? {
        var byteArray: ByteArray? = null
        try {
            val base64 = Class.forName("org.apache.commons.codec.binary.Base64")
            val parameterTypes = arrayOf<Class<*>>(ByteArray::class.java)
            byteArray =  base64.getMethod("decodeBase64", *parameterTypes).invoke(base64, imageArray) as ByteArray
        } catch (e: ClassNotFoundException) {
            // TODO Auto-generated catch block
            return imageArray
        } catch (e: NoSuchMethodException) {
            // TODO Auto-generated catch block
            return imageArray
        } catch (e: java.lang.IllegalArgumentException) {
            // TODO Auto-generated catch block
            return imageArray
        } catch (e: IllegalAccessException) {
            // TODO Auto-generated catch block
            return imageArray
        } catch (e: InvocationTargetException) {
            // TODO Auto-generated catch block
            return imageArray
        } catch (e: java.lang.NullPointerException) {
            return imageArray
        }
        return byteArray
    }
//
//    fun getOriginDeviceIdOLD(context: Context): String? {
//        var deviceID: String?
//        deviceID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Secure.getString(
//                    context.contentResolver, Secure.ANDROID_ID) // UUID 대체코드
//        } else {
//            val telManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            "" + telManager.deviceId
//        }
//
////        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        if (deviceID == null || deviceID.trim { it <= ' ' }.length == 0) {
//            deviceID = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
//        }
//        if (deviceID == null || deviceID.trim { it <= ' ' }.length == 0) {
//            val accounts = AccountManager.get(context).accounts
//            for (account in accounts) {
//                deviceID = account.name
//            }
//        }
//        return deviceID
//    }

    fun showMsgWindow(activity: Activity?, title: String?, msg: String?, btnMsg: String?) {
        try {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(title)
            alertDialog.setMessage(msg)
            alertDialog.setPositiveButton(
                btnMsg
            ) { dialog, which -> dialog.dismiss() }
//            alertDialog.create()
//            alertDialog.show()
        } catch (e: BadTokenException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        } catch (e: IllegalArgumentException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        }
    }

    fun showCustomMsgWindow(activity: Activity?, title: String?, msg: String?, btnMsg: String?, cancel: Boolean, listener: DialogInterface.OnClickListener) {
        try {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(btnMsg, listener)
                    .setCancelable(cancel)
                    .create().show()
        } catch (e: BadTokenException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        } catch (e: IllegalArgumentException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        }
    }
    fun showCustomWindowBtn(activity: Activity?, title: String?, msg: String?, btnMsg: String?, positiveListener: DialogInterface.OnClickListener, negativeListener: DialogInterface.OnClickListener) {
        try {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(title)
                .setMessage(msg)
                .setPositiveButton(btnMsg, positiveListener)
                .setNegativeButton("취소",negativeListener)
                .setCancelable(false)
                .create().show()
        } catch (e: BadTokenException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        } catch (e: IllegalArgumentException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        }
    }

    fun showMsgWindowFinish(activity: Activity, title: String?, msg: String?, btnMsg: String?) {
        try {
            val alertDialog = AlertDialog.Builder(activity)
            alertDialog.setTitle(title)
            alertDialog.setMessage(msg)
            alertDialog.setCancelable(false)
            alertDialog.setPositiveButton(
                btnMsg
            ) { dialog, which ->
                dialog.dismiss()
                activity.finish()
            }
            alertDialog.create()
            alertDialog.show()
        } catch (e: BadTokenException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        } catch (e: java.lang.IllegalArgumentException) {
            // TODO: handle exception
            Log.e("error", e.message.toString())
        }
    }

    fun getEncodedStr(keyword: String?): String? {
        var keyword: String? = keyword ?: return ""
        try {
            keyword = URLEncoder.encode(keyword, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            // TODO Auto-generated catch block
            Log.e("error", e.message.toString())
        }
        return keyword
    }

    fun setUserId(context: Context, userId: String?) {
        setSharedData(
            context, context.resources.getString(R.string.libros_login_id),
            if (userId != null && userId.trim().isNotEmpty()) {
                Cryptchar.encrypt(getOriginDeviceId(context), userId)
            } else userId
        )
    }


    fun getSharedData(ctx: Context, key: String?): String? {
        val prefs = ctx.getSharedPreferences("eco.app.librose", Context.MODE_PRIVATE)
        return prefs.getString(key, "")
    }

    fun setSharedData(ctx: Context, key: String?, value: String?) {
        val prefs = ctx.getSharedPreferences("eco.app.librose", Context.MODE_PRIVATE)
        val ed = prefs.edit()
        ed.putString(key, value).apply()
    }

    fun delSharedData(ctx: Context, key: String?) {
        try {
            val prefs = ctx.getSharedPreferences("eco.app.librose", Context.MODE_PRIVATE)
            val ed = prefs.edit()
            ed.clear().apply()
//            ed.remove(key).apply()
        } catch (e: Exception) {
            Log.d("testee", e.toString())
        }

    }

    fun setUserPw(context: Context, userPw: String?) {
        var userPw = userPw
        if (userPw != null && userPw.toString().trim().isNotEmpty()) {
            userPw = Cryptchar.encrypt(getOriginDeviceId(context), userPw)
        }
        setSharedData(context, context.resources.getString(R.string.libros_login_pw), userPw)
    }

    fun setLastLib(context: Context, userLib: String?) {
        setSharedData(context, context.resources.getString(R.string.libros_last_lib_code), userLib)
    }


    fun getLibCode(context: Context): String? {
        var libCode: String? = null
        try {
            libCode = getSharedData(context, context.resources.getString(R.string.libros_last_lib_code))
            if (TextUtils.isEmpty(libCode))
                libCode = "111042"
        } catch (e: NullPointerException) {
            libCode = "111042"
        }

        return libCode
    }


    fun getUserId(context: Context, needEncoding: Boolean, needEncrypt: Boolean): String? {
        var userId: String? =
                getSharedData(context, context.resources.getString(R.string.libros_login_id))
        if (userId != null && userId.trim().isNotEmpty()) {

            userId = Cryptchar.decrypt(getOriginDeviceId(context), userId)

            if (!needEncoding) {
                userId = try {
                    URLDecoder.decode(userId, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    // TODO Auto-generated catch block
                    return userId
                } catch (e: Exception) {
                    // TODO Auto-generated catch block
                    return userId
                }
            }
            if (needEncrypt) {
                try {
                    val crypt = PasswordCrypt(context)
                    crypt.init()
                    userId = URLDecoder.decode(userId, "utf-8")
                    userId = crypt.encodePw(userId)
                    userId = URLEncoder.encode(userId, "utf-8")

                } catch (e: Exception) {
                    // TODO Auto-generated catch block
                    return userId
                }
            }
        }
        return userId
    }

    fun delUserId(context: Context, key: String) {
        delSharedData(context, key)
    }

    fun getUserPw(context: Context): String? {
        var userPw: String? =
                getSharedData(context, context.resources.getString(R.string.libros_login_pw))
        if (userPw != null && userPw.trim { it <= ' ' }.length != 0) {
            userPw = Cryptchar.decrypt(getOriginDeviceId(context), userPw)
        }
        return userPw
    }

//    fun showLogoutMsg(activity: Activity?, cancel: Boolean) {
//        val context: Context = activity!!.applicationContext
//        try {
//            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(activity)
//            alertDialog.setTitle("알림")
//                    .setMessage("로그아웃 하시겠습니까?")
//                    .setPositiveButton("확인", DialogInterface.OnClickListener { dialog, _ ->
//                        dialog.dismiss()
//                        Log.d("TEST!!", "TEST!!!")
////                        delUserId(context, context.resources.getString(R.string.libros_login_id))
//                        delUserId(context, context.resources.getString(R.string.libros_login_pw))
//                        SettingsMainActivity().finish()
//                        Intent(activity, IntroActivity::class.java).apply {
//                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                            startActivity(context, this, null)
//                        }
//                    })
//                    .setCancelable(cancel)
//                    .create().show()
//
//        } catch (e: BadTokenException) {
//            LibrosLog.print(e.message.toString())
//        } catch (e: java.lang.IllegalArgumentException) {
//            LibrosLog.print(e.message.toString())
//        }
//    }
//
//    fun logoutTask(activity: Activity?) {
//        val context: Context = activity!!.applicationContext
//        try {
//            delUserId(context, context.resources.getString(R.string.libros_login_id))
//            delUserId(context, context.resources.getString(R.string.libros_login_pw))
//            EbookDownloadDBFacade(context).deleteEBookDB()
//            UserLibListDBFacade(context).deleteLibDB()
//            activity.finish()
//            Intent(activity, LogInSettingActivity::class.java).apply {
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                startActivity(context, this, null)
//            }
//        } catch (e: java.lang.IllegalArgumentException) {
//            LibrosLog.print(e.message.toString())
//        }
//    }

//    fun showMsgDialog(context: FragmentManager, title: String, msg: String) {
//        val customMsgDialogFragment: CustomMsgDialogFragment =
//                CustomMsgDialogFragment.newInstance(title, msg)
//        customMsgDialogFragment.show(context, "customDialog")
//    }
//
//    fun showMsgCustomDialog(context: FragmentManager, title: String, msg: String, button: String) {
//        val customMsgDialogFragment: CustomMsgDialogFragment =
//                CustomMsgDialogFragment.newInstance(title, msg, button)
//        customMsgDialogFragment.show(context, "customDialog")
//    }

//    fun showAuthDialog(
//        context: FragmentManager,
//        userName: String?,
//        userNo: String?,
//        userPw: String?,
//        certifyKind: String?
//    ) {
//        val customAuthDialog: CustomAuthDialog =
//                if (certifyKind.equals("LOANNOCARD")) {
//                    CustomAuthDialog.newInstance(
//                        userName!!,
//                        userNo!!,
//                        userPw!!,
//                        certifyKind.toString()
//                    )
//                } else {
//                    CustomAuthDialog.newInstance(
//                        userName!!,
//                        userNo!!,
//                        certifyKind.toString()
//                    )
//                }
//        customAuthDialog.show(context, "auth_dialog")
//    }

    fun getLibName(context: Context, libCode: String?): String {
        val userLastLibCode = LibrosUtil.getLibCode(context).toString()
        val userInfo = UserLibListDBFacade(context).getLibYnInfo(userLastLibCode)
        return userInfo?.libraryName.toString()
    }

    fun getSimpleJson(map: Map<String, String?>): JSONObject {
        val obj = JSONObject()
        for (i in map) {
            obj.put(i.key, i.value)
        }
        Log.d("testobj", obj.toString())
        return obj
    }

    fun getLibList(mContext: Context): JSONObject {
        val libList =
                UserLibListDBFacade(context = mContext).getUserLibList() as List<UserLibListDataVO>
        val jsonArray = JSONArray()
        for (i in libList) {
            val obj = JSONObject()
            obj.put("LibraryName", i.libraryName)
            obj.put("LibraryCode", i.libraryCode)
            obj.put("LibraryUserId", i.libraryUserId)
            obj.put("LibraryUserPw", i.libraryUserPw)
            obj.put("LibraryUserNo", i.libraryUserNo)
            obj.put("CertifyKindId", i.certifyKindId)
            obj.put("IsUserCertifyYn", i.isUserCertifyYn)
            obj.put("IsEbookServiceYn", i.isEBookServiceYn)
            obj.put("IsJoinYn", i.isJoinYn)
            obj.put("IsSearchYn", i.isSearchYn)
            obj.put("IsSeatServiceYn", i.isSeatServiceYn)
            obj.put("EbookId", i.eBookId)
            obj.put("EbookPw", i.eBookPw)
            jsonArray.put(obj)
        }
        val libListObj = JSONObject()
        libListObj.put("UserLibraryInfoList", jsonArray)
        Log.d("TESTLIBLIST", libListObj.toString())
        return libListObj
    }
    fun getLibId(context: Context, libCode: String?): String {
        var userInfo = ""

        val userData = UserLibListDBFacade(context).getCertifyInfo(libCode!!)
        if(userData?.libraryUserId != null){
            userInfo = userData.libraryUserId
        }
        return userInfo
    }


//    fun showMobileCard(activity: FragmentActivity, mContext: Context){
//        var libCode = getLibCode(mContext).toString()
//        if (TextUtils.isEmpty(libCode.trim())){
//            libCode = "128040"
//        }
//        libCode = "128040"
//        Log.d("TESTUSERINFO",libCode)
//        val certifyKind : String = UserLibListDBFacade(mContext).getCertifyKind(libCode)
//
//        val userInfo = UserLibListDBFacade(mContext).getCertifyInfo(libCode)
//
////        Log.d("TESTUSERINFO",userInfo!!.libraryUserId)
//        if(!TextUtils.isEmpty(certifyKind)) {
//            val mobileCardFragment: BottomSheetDialogFragment =
//                if (TextUtils.isEmpty(userInfo?.libraryUserId)) {
//                    MobileLoginCardFragment().apply {
//                        arguments = Bundle().apply {
//                            putString("certifyKind", certifyKind)
//                        }
//                    }
//                    //                mobileCardFragment.show(supportFragmentManager,"mobile_card")
//                } else {
//                    MobileCardFragment().apply {
//                        arguments = Bundle().apply {
//                            putString("userId", userInfo?.libraryUserId)
//                            putString("libCode", libCode)
//                            putString("userLibNo", userInfo?.libraryUserNo)
//                            putString("certifyKind", certifyKind)
//                        }
//                    }
//                }
//            mobileCardFragment.show(activity.supportFragmentManager, "mobile_card")
//        }else{
//            LibrosUtil.showMsgCustomDialog(activity.supportFragmentManager,"알림","해당 도서관에서는 스마트 회원증을 이용할 수 없습니다.","확인")
//        }
//    }
    suspend fun getLibMenuList(mContext: Context, data: String): JSONObject {
        return withContext(IO) {
            val libCode = JSONObject(data).getString("libCode")
            val libList =
                    UserLibListDBFacade(context = mContext).getLibMenuList(libCode) as List<LibraryMenuListDataVo>
            val jsonArray = JSONArray()
            for (i in libList) {
                val obj = JSONObject()
                obj.put("LibraryMenuId", i.libraryMenuId)
                obj.put("LibraryMenuSeq", i.libraryMenuSeq)
                obj.put("LibraryMenuIconId", i.libraryMenuIconId)
                obj.put("PageLinkPath", i.pageLinkPath)
                jsonArray.put(obj)
            }
            val libListObj = JSONObject()
            libListObj.put("LibraryMenuInfoList", jsonArray)
            libListObj
        }
    }

    fun getEPUBRootPath(context: Context): String{
        return context.filesDir.absolutePath
    }

    fun canUse(activity: Activity, libCode: String?, function: String): Array<Any?> {
        val obj = arrayOfNulls<Any>(2)

        var canUse = false
        var msg = ""

        val lib = UserLibListDBFacade(activity).getLibYnInfo(libCode.toString())
        Log.d("TESTLIB", lib.toString())
        if (libCode == null){
            msg = "도서관 데이터를 찾을 수 없습니다."
            obj[0] = canUse
            obj[1] = msg

            return obj
        }
        if (function.equals("login", true)){
            if (lib?.isUserCertifyYn.equals("Y", true)){
                canUse = true
            } else{
                msg = "인증이 필요한 서비스 입니다."
            }
        }
        obj[0] = canUse
        obj[1] = msg
        Log.d("TESTOBJ", obj.toString())
        return obj
    }

//    fun showLoginWindow(activity: FragmentManager, libCode: String?, certifyKind: String) {
//        val customAuthDialog: CustomAuthDialog =CustomAuthDialog.newInstance(
//            "",
//            "",
//            certifyKind.toString()
//        )
//        customAuthDialog.show(activity, "auth_dialog")
//    }

    fun getUserLibNo(context: Context, libCode: String?): String {
        var userInfo = ""

        val userData = UserLibListDBFacade(context).getCertifyInfo(libCode!!)
        if(userData?.libraryUserId != null){
            userInfo = userData.libraryUserNo
        }
        return userInfo
    }

    val encryptedTxt = {
        txt: String, crypt : PasswordCrypt ->
        var tempTxt = ""
        try {
            tempTxt = crypt.encodePw(txt)
        } catch (e: Exception) {
            tempTxt = when (e) {
                is NoSuchAlgorithmException,
                is UnsupportedEncodingException,
                is NoSuchPaddingException,
                is InvalidAlgorithmParameterException,
                is IllegalBlockSizeException,
                is BadPaddingException,
                is IOException -> {
                    "N"
                }
                else -> {
                    "N"
                }
            }
        }

        tempTxt
    }


}
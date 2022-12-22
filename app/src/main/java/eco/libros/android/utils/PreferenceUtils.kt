package eco.libros.android.utils

import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import eco.libros.android.App
import eco.libros.android.R
import eco.libros.android.common.crypt.Cryptchar
import eco.libros.android.common.crypt.PasswordCrypt
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder

class PreferenceUtils (mContext: Context) {

    private var preferences: SharedPreferences? = null

    init {
        context = mContext
    }
    companion object{
        @SuppressLint("StaticFieldLeak")
        private lateinit var context:Context
    }

    private fun getPreferences(): SharedPreferences? {
        preferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(context)
        }

        return preferences
    }

    fun putBoolean(key: String, value: Boolean) {
        getPreferences()
            ?.edit()
            ?.putBoolean(key, value)
            ?.apply()
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean? {
        return getPreferences()?.getBoolean(key, defValue)
    }

    fun getSharedData(ctx: Context, key: String?): String? {
        val prefs = ctx.getSharedPreferences("eco.app.libropia", Context.MODE_PRIVATE)
        return prefs.getString(key, "")
    }

    fun setSharedData(ctx: Context, key: String?, value: String?) {
        val prefs = ctx.getSharedPreferences("eco.app.libropia", Context.MODE_PRIVATE)
        val ed = prefs.edit()
        ed.putString(key, value)
        ed.apply()
    }

    fun setUserId(context: Context, userId: String?) {
        var userId = userId
        if (userId != null && userId.trim { it <= ' ' }.length != 0) {
            userId = Cryptchar.encrypt(
                PreferenceUtils(context).getOriginDeviceIdOLD(context), userId
            )
        }
        PreferenceUtils(context).setSharedData(
            context,
            context.resources.getString(R.string.libros_login_id),
            userId
        )
    }

    fun getUserId(context: Context, needEncoding: Boolean, needEncrypt: Boolean): String? {
        var userId: String? =
            getSharedData(context, context.resources.getString(R.string.libros_login_id))
        if (userId != null && userId.trim { it <= ' ' }.isNotEmpty()) {
            userId = Cryptchar.decrypt(PreferenceUtils(context).getOriginDeviceIdOLD(context), userId)
            if (!needEncoding) {
                userId = try {
                    URLDecoder.decode(userId, "utf-8")
                } catch (e: UnsupportedEncodingException) {
                    // TODO Auto-generated catch block
                    Log.d("teste", e.toString())
                    return userId
                } catch (e: Exception) {
                    Log.d("teste2", e.toString())
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

    fun getOriginDeviceIdOLD(context: Context): String? {
        var deviceID: String?
        deviceID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Secure.getString(
                context.contentResolver, Secure.ANDROID_ID
            ) // UUID 대체코드
        } else {
            val telManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_PHONE_STATE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return ""
            }
            "" + telManager.deviceId
        }


//        TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (deviceID == null || deviceID.trim { it <= ' ' }.isEmpty()) {
            deviceID = Secure.getString(context.contentResolver, Secure.ANDROID_ID)
        }
        if (deviceID == null || deviceID.trim { it <= ' ' }.isEmpty()) {
            val accounts =
                AccountManager.get(context).accounts
            for (account in accounts) {
                deviceID = account.name
            }
        }
        return deviceID
    }

}
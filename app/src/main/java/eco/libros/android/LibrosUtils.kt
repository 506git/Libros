package eco.libros.android

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import java.util.*

object LibrosUtils {
    fun getOriginDeviceId(context: Context): String? {
        val tmDevice: String
        val tmSerial: String
        val androidId: String
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val deviceUuid: UUID
        var deviceId: String? = null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            deviceId = Settings.Secure.getString(
                context.contentResolver, Settings.Secure.ANDROID_ID
            )
            Log.d("testsddddf", deviceId)
            deviceId // UUID 대체코드 }
        } else {
            tmDevice = "" + tm.deviceId
            tmSerial = "" + tm.simSerialNumber
            androidId = "" + Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
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

    fun getEPUBRootPath(context: Context): String{
        return context.filesDir.absolutePath
    }
}
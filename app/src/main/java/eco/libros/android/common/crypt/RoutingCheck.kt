package eco.libros.android.common.crypt

import android.os.Build
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

object RoutingCheck {

    fun checkUser(): Boolean {
        return checkSuperUserCommand() || checkSuperUserCommand2() || checkTags() || routingCheck()
    }

    private fun routingCheck(): Boolean {
        val files = arrayOf(
            "/sbin/su",
            "/system/su",
            "system/bin/su",
            "/system/xbin/su",
            "/system/xbin/mu",
            "/system/bin/.ext/.su",
            "/system/usr/su-backup",
            "/data/data/com.noshufou.android.su",
            "/system/app/Superuser.apk",
            "/system/app/su.apk",
            "/system/bin/.ext",
            "/system/xbin/.ext",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su",
            "/su/bin/su"
        )
        for (s in files) {
            val file = File(s)
            if (null != file && file.exists()) {
                return true
            }
        }
        return false
    }

    private fun checkTags(): Boolean {
        val buildTags = Build.TAGS

        return buildTags.isNotEmpty() && buildTags.contains("test-keys")
    }

    private fun checkSuperUserCommand(): Boolean {
        return try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: IOException) {
            false
        }
    }

    private fun checkSuperUserCommand2(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime()
                .exec(arrayOf("/system/xbin/which", "su"))
            val `in` =
                BufferedReader(InputStreamReader(process.inputStream))
            if (`in`.readLine() != null) true else false
        } catch (e: IOException) {
            false
        } finally {
            process?.destroy()
        }
    }
}
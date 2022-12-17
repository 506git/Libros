package eco.libros.android.ebook.download

import android.app.Activity
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class EBookDownloadBOOJAM {

    fun down(activity: Activity, _url: String, task: CustomProgressFragment, comCode: String?): String? {
        var url: String = _url
        if(url.contains("#UDID")){
            url = url.replace("#UDID", LibrosUtil.getOriginDeviceId(activity).toString())
        }

        val uid = UUID.randomUUID().toString()

        val filePath = "${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}"
        val fileName: String? = "$uid.BXP"

        val dir = File(filePath)

        if(!dir.exists()){
            dir.mkdir()
        }

        val file = File(filePath, fileName!!)

        if (file.exists()){
            file.delete()
        }

        if (url != null && url.isNotEmpty()){
            var inputStream: InputStream? = null

            val fileOutputStream: FileOutputStream? = null

            try{
                val uo = URL(url)

                val connection = uo.openConnection().apply {
                    readTimeout = GlobalVariable.EBOOK_TIMEOUT_SOCKET
                    connectTimeout = GlobalVariable.EBOOK_TIMEOUT_CONNECTION
                } as HttpURLConnection

                var lengthOfFile = connection.contentLength

                if(lengthOfFile < 1000 && lengthOfFile != -1){
                    return null
                }

                if(lengthOfFile == -1){
                    lengthOfFile = 10 * 1024 * 1024
                }

                inputStream = connection.inputStream

                var size = inputStream.available()

                if(size <= 0){
                    size = 1024
                }

                val buf = ByteArray(size)

                val fileResult = file.createNewFile()

                if (fileResult){
                    FileOutputStream(file).use { fos ->
                        var cnt = 0
                        var readData: Long = 0
                        var total: Long = 0

                        while (inputStream.read(buf, 0, buf.size).also { cnt = it } != -1) {
                            total += cnt.toLong()
                            val i = (total * 80 / lengthOfFile).toInt()
                            if (i > 79) {
                                task.progressTask(79)
                            } else {
                                task.progressTask(i)
                            }
                            fos.write(buf, 0, cnt)
                            fos.flush()
                            readData += cnt.toLong()
                        }
                    }
                }
                return fileName

            }catch (e: Exception){
                LibrosLog.print(e.toString())
            }
        }
        return null
    }
}
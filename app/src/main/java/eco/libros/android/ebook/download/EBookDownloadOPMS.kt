package eco.libros.android.ebook.download

import android.app.Activity
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.utill.EBookDownloadTask
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ui.MainActivity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.*

class EBookDownloadOPMS {
    suspend fun down(activity: Activity, _url: String, task: CustomProgressFragment, comCode: String): String? {
        var url: String = _url
        if (url.contains("#UDID")) {
            url = url.replace("#UDID", LibrosUtil.getOriginDeviceId(activity)!!)
        }

        val uid = UUID.randomUUID().toString()

        val filePath = "${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}"
        val fileName = "$uid.epub"

        val dir = File(filePath)

        if (dir.exists()) {
            dir.mkdir()
        }

        val file = File(filePath, fileName)
        if (file.exists()) {
            file.delete()
        }
        task.progressTask(20)
        if (url != null && url.isNotEmpty()) {
            var inputStream: InputStream? = null
            var isComplete = true
            try {
                val uo = URL(url)

                val conn = uo.openConnection().apply {
                    readTimeout = GlobalVariable.EBOOK_TIMEOUT_SOCKET
                    connectTimeout = GlobalVariable.EBOOK_TIMEOUT_CONNECTION
                }

                var lengthOfFile = conn.contentLength

                if (lengthOfFile < 1000 && lengthOfFile != -1) {
                    return null
                }

                if (lengthOfFile == -1) {
                    lengthOfFile = 10 * 1024 * 1024
                }

                inputStream = conn.getInputStream()

                var size = inputStream.available()

                if (size <= 0) {
                    size = 1024
                }

                val buf = ByteArray(size)
                val fileResult = file.createNewFile()
                task.progressTask(30)
                if (fileResult) {
                    FileOutputStream(file).use { fos ->
                        var cnt: Int? = 0
                        var total: Long = 0
                        var readData: Long = 0
                        while (inputStream.read(buf, 0, buf.size).also { cnt = it } != -1) {
                            total += cnt?.toLong()!!
                            val i = (total * 80 / lengthOfFile).toInt()
//                            if (i > 79) {
//                                task.progressTask(79)
//                            } else {
//                                task.progressTask(i)
//                            }
                            fos.write(buf, 0, cnt!!)
                            fos.flush()
                            readData += cnt!!.toLong()
                        }
                    }
                }
                task.progressTask(80)
            } catch (e: Exception) {
                isComplete = false
                LibrosLog.print(e.toString())
                val mSocket = (activity as MainActivity).mSocket
                mSocket.emit("working_error", e.toString())
            } finally {
                try {
                    inputStream?.close()
                } catch (e: IOException) {
                    if (isComplete) {
                        var returnFileName: String? = null
                        if (fileName != null) {
                            try {
                                if (comCode != null && comCode.equals("OPMS_MARKANY", true)) {
                                    returnFileName = FileManager().unzipOpmsMarkAny(activity, file, fileName)
                                } else {
                                    returnFileName = FileManager().unzip(activity, file, fileName, uid, task)
                                }
                            } catch (e: IOException) {
                                LibrosLog.print(e.toString())
                            }
                        }
                        return returnFileName
                    } else
                        LibrosLog.print(e.toString())
                }
            }
        }

        var returnFileName: String? = null

        if (fileName != null){
            try{
                if (comCode != null && comCode.equals("OPMS_MARKANY",true)){
                    returnFileName = FileManager().unzipOpmsMarkAny(activity,file,fileName)
                } else{
                    returnFileName = FileManager().unzip(activity, file, fileName, uid, task)
                }
            } catch (e: IOException){
                LibrosLog.print(e.toString())
            }
        }
        return returnFileName
    }
}
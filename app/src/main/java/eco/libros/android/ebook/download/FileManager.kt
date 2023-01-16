package eco.libros.android.ebook.download

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.markany.xsync.XSyncException
import com.markany.xsync.core.XSyncCipher
import com.markany.xsync.core.XSyncContent
import com.markany.xsync.core.XSyncZipFile
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.utill.EBookDownloadTask
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ui.MainActivity
import java.io.*
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManager {
    private val BUFFER = 80000
    private val BUFFER_SIZE = 1024 * 2
    private val COMPRESSION_LEVEL = 8
    init {
        //load key gen, load first needed
        try{
            System.loadLibrary("xsync-keygen")
        } catch (e:Exception){
            Log.d("test", "err$e")
        }
//        private static final int BUFFER = 80000;
    }

    fun unzip(activity: Activity, zipFile: File, url: String?, uuid: String, task: CustomProgressFragment): String? {
        val outputDirPath = "${LibrosUtil.getEPUBRootPath(activity)}/${
            activity.resources.getString(
                R.string.sdcard_dir_name
            )
        }/$uuid/$uuid"
        task.progressTask(85)
        try {
            FileInputStream(zipFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var ze: ZipEntry?

                    val parents = File(
                        "${LibrosUtil.getEPUBRootPath(activity)}/${
                            activity.resources.getString(
                                R.string.sdcard_dir_name
                            )
                        }/$uuid"
                    )

                    parents.mkdir()

                    val dir = File(outputDirPath)
                    if (dir.exists()) {
                        dir.delete()
                    }

                    val mkdir = dir.mkdir()

                    if (mkdir) {
                        val noMediaFile = File(outputDirPath, ".nomedia")
                        if (!noMediaFile.exists()) {
                            noMediaFile.createNewFile()
                        }
                    } else {
                        return null
                    }

                    task.progressTask(88)

                    while (zis.nextEntry.apply { ze = this } != null) {
                        val fileNameToUnZip = ze?.name

                        val targetFile = File(outputDirPath, fileNameToUnZip)
                        if (ze?.isDirectory == true) {
                            targetFile.mkdir()
                        } else {
                            var path = targetFile.absolutePath

                            val filePoint: Int = path.lastIndexOf("/")

                            if (filePoint != -1) {
                                path = path.substring(0, filePoint)
                                val directory = File(path)

                                if (!directory.exists() && directory == null) {
                                    directory.mkdir()
                                }
                            }

                            unZipEntry(zis, targetFile)

                        }
                    }
                    if (zis != null) {
                        zis.close()
                    }
                }
                if (fis != null) {
                    fis.close()
                }
            }
        } catch (e: Exception) {
            LibrosLog.print("$e*file*")
            return null
        }
        task.progressTask(95)

        val isDelete = true

        // Delete File Name
        //isDelete = deleteZipFile(zipFile);

        return if (isDelete) {
            uuid
        } else {
            null.toString()
        }
    }

    fun unZipPdf(
        activity: Activity,
        zipFile: File,
        url: String,
        task: CustomProgressFragment
    ): String {
        val uid = ".${UUID.randomUUID().toString()}"

        val inputDirPath = activity.getExternalFilesDir(null)?.absolutePath
        var outputDirPath = "$inputDirPath/$uid/$uid"

        task.progressTask(85)

        try {
            val inFile = File(inputDirPath, url)
            FileInputStream(inFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var ze: ZipEntry?

                    var parents = File("$inputDirPath/$uid")
                    if (parents.exists()) {
                        parents.delete()
                    }

                    parents.mkdir()

                    var dir = File(outputDirPath)
                    if (dir.exists()) {
                        dir.delete()
                    }
                    dir.mkdir()
                    var nomediaFile = File(outputDirPath, ".nomedia")
                    if (!nomediaFile.exists()) {
                        nomediaFile.createNewFile()
                    }

                    task.progressTask(88)

                    while (zis.nextEntry.apply { ze = this } != null) {
                        val fileNameToUnZip = ze?.name

                        val targetFile = File(outputDirPath, fileNameToUnZip)
                        if (ze?.isDirectory == true) {
                            targetFile.mkdir()
                        } else {
                            var path = targetFile.absolutePath

                            var filePoint: Int = path.lastIndexOf("/")

                            if (filePoint != -1) {
                                path = path.substring(0, filePoint)
                                var directory = File(path)

                                if (!directory.exists() && directory == null) {
                                    directory.mkdir()
                                }
                            }

                            unZipEntry(zis, targetFile)

                        }
                    }
                    if (zis != null) {
                        zis.close()
                    }

                }
            }
        } catch (e: IOException) {
            LibrosLog.print(e.toString())
        }

        var isDelete = true

        // Delete File Name
        //isDelete = deleteZipFile(zipFile);

        if (isDelete) {
            return uid
        } else {
            return null.toString()
        }

    }
    private fun unZipEntry(zis: ZipInputStream, targetFile: File): File? {
            try {
                FileOutputStream(targetFile).use { fos ->
                        val buffer = ByteArray(1024)
                        var len: Int? = 0
                        while (zis.read(buffer).apply { len = this } != -1) {
                            fos.write(buffer, 0, len!!)
                        }

                        if (targetFile.name.endsWith(".html")) {
                            FileInputStream(targetFile).use { fis ->
                                val data = ByteArray(targetFile.length().toInt())
                                val temp = ByteArray(1024)
                                var size: Int? = 0
                                var totalSize = 0

                                while (fis.read(temp).apply { size = this } != -1) {
                                    System.arraycopy(temp, 0, data, totalSize, size!!)
                                    totalSize += size!!
                                }
                                FileOutputStream(targetFile, false).use { os ->
                                    os.write(data)
                                    os.flush()
                                }
                            }
                        }

                }
                return targetFile
            } catch (e: Exception) {
                LibrosLog.print(e.toString()+"ddf")
                return targetFile
            }
    }

//    @Throws(IOException::class)
//    private fun unZipEntry(zis: ZipInputStream, targetFile: File): File? {
//        var fos: FileOutputStream? = null
//        var `is`: FileInputStream? = null
//        var os: FileOutputStream? = null
//        try {
//            fos = FileOutputStream(targetFile)
//            val buffer = ByteArray(1024)
//            var len = 0
//            while (zis.read(buffer).also { len = it } != -1) {
//                fos.write(buffer, 0, len)
//            }
//            if (targetFile.name.endsWith(".html")) {
//                `is` = FileInputStream(targetFile)
//                val data = ByteArray(targetFile.length().toInt())
//                val temp = ByteArray(1024)
//                var size = 0
//                var totalsize = 0
//                while (`is`.read(temp).also { size = it } != -1) {
//                    System.arraycopy(temp, 0, data, totalsize, size)
//                    totalsize += size
//                }
//                os = FileOutputStream(targetFile, false)
//                os.write(data)
//                os.flush()
//            }
//        } catch (e: IOException) {
//            //not handle
//            LibrosLog.print(e.message.toString())
//        } finally {
//            fos?.close()
//            `is`?.close()
//            os?.close()
//        }
//        return targetFile
//    }

    @Throws(java.lang.Exception::class)
    fun unzipEpub(activity: Activity, zipFile: File?, url: String?, uuid: String): String? {
        val outputDirPath: String = LibrosUtil.getEPUBRootPath(context = activity)
            .toString() + "/" + activity.resources.getString(R.string.sdcard_dir_name) + "/" + uuid + "/" + uuid
        val inputDirPath: String = LibrosUtil.getEPUBRootPath(context = activity)
            .toString() + "/" + activity.resources.getString(R.string.sdcard_dir_name)
        // File UnZip
        var fis: FileInputStream? = null
        var zis: ZipInputStream? = null
        try {
            val inFile = File("$inputDirPath/$uuid", url)
            fis = FileInputStream(inFile)
            zis = ZipInputStream(fis)
            var ze: ZipEntry

            val dir = File(outputDirPath)
            if (dir.exists()) {
                dir.delete()
            }
            dir.mkdir()
            val nomediaFile = File(outputDirPath, ".nomedia")
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile()
            }
            while (zis.nextEntry.also { ze = it } != null) {
                val fileNameToUnzip = ze.name
                val targetFile = File(outputDirPath, fileNameToUnzip)
                if (ze.isDirectory) { // Directory 인 경우
                    targetFile.mkdir() // 디렉토리 생성
                } else { // File 인 경우
                    var path = targetFile.absolutePath
                    val filePoint = path.lastIndexOf("/")
                    if (filePoint != -1) {
                        path = path.substring(0, filePoint)
                        val directory = File(path)
                        if (directory == null || !directory.exists()) {
                            directory.mkdir()
                        }
                    }
                    unZipEntry(zis, targetFile)
                }
            }
        } catch (e: java.lang.Exception) {
            //not handle
            LibrosLog.print(e.message.toString())
        } finally {
            zis?.close()
            fis?.close()
        }

        var isDelete = false

        // Delete File Name
        try {
            //isDelete = deleteZipFile(zipFile);
            isDelete = true
        } catch (e: java.lang.Exception) {
            //not handle
            LibrosLog.print(e.message.toString())
        }
        return if (isDelete) {
            uuid
        } else {
            null
        }
    }

    @Throws(IOException::class)
    fun writeFile(data: ByteArray?, path: String?) {
        val fos = FileOutputStream(path)
        try {
            fos.write(data)
        } finally {
            if (fos != null) {
                fos.close()
            }
        }
    }

    @Throws(IOException::class)
    fun writePdfFile(context: Context?, data: ByteArray?, path: String?) {
        val fos = FileOutputStream(path)
        try {
            fos.write(data)
        } finally {
            if (fos != null) {
                fos.close()
            }
        }
    }


    fun readPrivateFile(ctxWrapper: ContextWrapper, name: String): ByteArray? {
        try {
            ctxWrapper.openFileInput(name).use { fis ->
                val buf = ByteArray(fis.available())
                var idx = 0
                while (idx < buf.size) {
                    val read = fis.read(buf, idx, buf.size - idx)
                    if (read < 0) throw EOFException("illegal eof reached in readPrivateFile() : $name")
                    idx += read
                }
                return buf
            }
        } catch (e: java.lang.Exception) {
            LibrosLog.print(e.toString())
        }
        return null
    }

    @Throws(IOException::class)
    fun deleteFiles(tbdFile: File) {
        if (tbdFile.isDirectory) {
            val files = tbdFile.listFiles()
            if (files != null) {
                for (i in files.indices) {
                    deleteFiles(files[i])
                }
            }
        }
        tbdFile.delete()
    }

    private fun deleteZipFile(targetZipFile: File): Boolean {
        return if (targetZipFile.exists()) {
            targetZipFile.delete()
        } else
            false
    }

    fun deleteFolder(targetFolder: File?): Boolean {
        return if (targetFolder != null && targetFolder.exists()) {
            if (targetFolder.isDirectory) {
                val childFile = targetFolder.listFiles()
                if (childFile != null) {
                    val size = childFile.size
                    if (size > 0) {
                        for (i in 0 until size) {
                            if (childFile[i].isFile) {
                                childFile[i].delete()
                            } else {
                                deleteFolder(childFile[i])
                            }
                        }
                    }
                }
                targetFolder.delete()
            } else {
                targetFolder.delete()
            }
        } else {
            true
        }
    }

    fun unzipOpmsMarkAny(activity: Activity, zipFile: File, fileName: String): String? {
        var retval = -1
        var parents: File? = null
        val uid = UUID.randomUUID().toString()
        val outputDirPath = "${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}/${uid}/${uid}"
        val inputDirPath: String = "${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}/$fileName"

        // File UnZip
        try {
            parents = File("${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}/$uid")

            if (parents.exists()) {
                parents.delete()
            }

            parents.mkdir()

            val dir = File(outputDirPath)
            if (dir.exists()) {
                dir.delete()
            }

            dir.mkdirs()

            val nomediaFile = File(outputDirPath, ".nomedia")
            if (!nomediaFile.exists()) {
                nomediaFile.createNewFile()
            }

            Thread.sleep(500)

            retval = FileManager().zipExtract(activity, inputDirPath, outputDirPath,
                LibrosUtil.getOriginDeviceId(activity).toString(), XSyncCipher.RUNNING_JAVA)
        } catch (e: IOException) {
            //not handle
            LibrosLog.print(e.toString())
        } catch (e: InterruptedException) {
            // TODO Auto-generated catch block
            LibrosLog.print(e.toString())
        } finally {
            if (retval != 0) {
                FileManager().deleteFolder(parents)
                deleteZipFile(zipFile)
                return null
            }
        }

        var isDelete = false

        // Delete File Name
        isDelete = deleteZipFile(zipFile)
        return if (isDelete) {
            uid
        } else {
            null
        }
    }

    fun zipExtractTest(
        activity: Activity,
        srcContentPath: String,
        dexOutPath: String,
        deviceKey: String?
    ): Int {
        return zipExtract(activity, srcContentPath, dexOutPath, deviceKey.toString(), XSyncCipher.RUNNING_JAVA)
    }

    private fun zipExtract(activity: Activity, srcContentPath: String, decOutPath: String, deviceKey: String, cipherMode: Int): Int {
        var retval: Int = 0
        try {
            if (XSyncCipher.RUNNING_NATIVE == cipherMode)  // no native mode supported
                return -1

            // test options
            val fullDecompression = true

            val drmFile = File(srcContentPath)

            if (!drmFile.exists()) {
                return -1
            }

            val content = XSyncContent(drmFile, deviceKey, cipherMode)
            val zipContent = XSyncZipFile(content, activity)

            var crrEntry: com.markany.xsync.core.ZipEntry? = null
            var crrEntryFileName: String? = null

            var entriesCount = 0 //from map

            var crrEntryStream: InputStream? = null
            val buf = ByteArray(1024)
            //check start time : getting whole entries

            //check start time : getting whole entries
            val xsyncContentEntryMap: HashMap<String, com.markany.xsync.core.ZipEntry> =
                zipContent.entries

            //making entries finished

            //making entries finished
            entriesCount = xsyncContentEntryMap.size.toLong().toInt()

            var readBytes = 0
            var totalBytes: Int = 0

            //full decompression test using entryMap

            if (fullDecompression) {
                val xsyncEntryKeySet: Set<String> = xsyncContentEntryMap.keys

                for (crrKey in xsyncEntryKeySet) {
                    totalBytes = 0
                    //get entry
                    crrEntry = xsyncContentEntryMap.get(crrKey)
                    //get decoded stream
                    try {
                        crrEntryStream = zipContent.getInputStream(crrEntry, activity)
                    } catch (e: IOException) {
                        LibrosLog.print(e.toString())
                        continue
                    } catch (e: XSyncException) {
                        val mSocket = (activity as MainActivity).mSocket
                        mSocket.emit("working_error", e.toString())
                        LibrosLog.print("tt ${e.toString()}")
                        continue
                    }

                    crrEntryFileName = crrEntry?.name

                    var parentDir: File? = null
                    val decOutFile = File("$decOutPath/$crrEntryFileName")

                    parentDir = decOutFile.parentFile
                    while (null != parentDir) {
                        if (!parentDir.exists()) {
                            parentDir.mkdirs()
                        } else {
                            break
                        }
                        parentDir = parentDir.parentFile
                    }

                    // if target entry is directory
                    if (crrEntry?.isDirectory!!){
                        decOutFile.mkdir()
                    } else{
                        decOutFile.createNewFile()
                        FileOutputStream(decOutFile).use { fos->
                            while (-1 != crrEntryStream.read(buf).also { readBytes = it }) {
                                fos.write(buf, 0, readBytes)
                                totalBytes += readBytes
                            }
                        }
                    }

                }
            }


        } catch (e: java.lang.Exception){
            val mSocket = (activity as MainActivity).mSocket
            mSocket.emit("working_error", e.toString())
            LibrosLog.print(e.toString())
            retval = -1
        } catch (e: IOException){
            LibrosLog.print(e.toString())
            retval = -1
        } finally {
          if (XSyncCipher.RUNNING_NATIVE == cipherMode) return -1
        }
        return retval
    }

    fun readFile(path: String): ByteArray? {
        val file = File(path)
        val buf = ByteArray(file.length().toInt())
        try {
            FileInputStream(file).use { fis ->
                var idx = 0
                while (idx < buf.size) {
                    val read = fis.read(buf, idx, buf.size - idx)
                    if (read < 0) {
                        throw EOFException("illegal eof reached in readFile() : $path")
                    }
                    idx += read
                }
            }

        } catch (e: java.lang.Exception) {
            LibrosLog.print(e.toString())
        }
        return buf
    }
}
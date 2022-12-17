package eco.libros.android.ebook.download

import android.content.Context
import android.content.ContextWrapper
import java.io.IOException

class FileManager2 {


    fun writeXmlFile(ctxWrapper: ContextWrapper, data: ByteArray?, name: String?) {
        val fos = ctxWrapper.openFileOutput(name, Context.MODE_PRIVATE).use {
            it.write(data)
        }
//        fos.write(data)
//        fos.close()
    }
}
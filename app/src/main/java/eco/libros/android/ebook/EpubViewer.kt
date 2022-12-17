package eco.libros.android.ebook

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import eco.libros.android.common.database.ViewerDBFacade
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ebook.download.FileManager
import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity
import java.io.File
import java.util.*

class EpubViewer : EpubViewerActivity() {

    private var drmType: String? = null
    private var filePath: String? = "libros"
    private var id: String? = null
    private var pw: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        drmType = intent.getStringExtra("drmType")
        if (intent.hasExtra("filePath") && intent.getStringExtra("filePath") != null) {
            filePath = intent.getStringExtra("filePath")
        }
        id = if (intent.hasExtra("id")) {
            intent.getStringExtra("id")
        } else
            LibrosUtil.getUserId(this, true, needEncrypt = false)
        if (intent.hasExtra("pw")) {
            pw = intent.getStringExtra("pw")
        }
    }

//    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
//        super.onCreate(savedInstanceState, persistentState)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        Log.d("testEpubViewerAct", "test")
//
//        drmType = intent.getStringExtra("drmType")
//        Log.d("testEpubViewerAct1", drmType.toString())
//        if (intent.hasExtra("filePath") && intent.getStringExtra("filePath") != null) {
//            filePath = intent.getStringExtra("filePath")
//        }
//        Log.d("testEpubViewerAct2", filePath.toString())
//        id = if (intent.hasExtra("id")) {
//            intent.getStringExtra("id")
//        } else
//            LibrosUtil.getUserId(this, true, needEncrypt = false)
//        if (intent.hasExtra("pw")) {
//            pw = intent.getStringExtra("pw")
//        }
//
//    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onExitBackPressed() {

    }

    override fun onErrorActivityFinish(debugMessage: String?) {
        Log.d("TESTException", debugMessage.toString() )
    }

    override fun onDataUpdate(contentId: String, epubBase64: String) {
        ViewerDBFacade(this).updateBookById(contentId, epubBase64)
    }

    override fun onObserver_AddBookMark(context: Context?, bookmarkId: Int, strIdref: String?, strCfi: String?, strSelectedText: String?, dateCurr: Date?) {

    }

    override fun onObserver_DeleteBookMark(context: Context?, bookmarkId: Int) {

    }

    override fun onObserver_AddHighlight(
        context: Context?,
        highlightId: Int,
        strIdref: String?,
        strCfi: String?,
        strSelectedText: String?,
        strMemo: String?,
        color: String?,
        dateCurr: Date?
    ) {
    }

    override fun onObserver_ModifyHighlight(
        context: Context?,
        highlightId: Int,
        strMemo: String?,
        color: String?,
        dateCurr: Date?
    ) {
    }

    override fun onObserver_DeleteHighlight(context: Context, highlightId: Int) {
        super.onObserver_DeleteHighlight(context, highlightId)
    }

    override fun onPrepareSpine(idref: String) {
        super.onPrepareSpine(idref)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (drmType != null && drmType!!.trim()
                .isNotEmpty() && (drmType.equals("Yes24") || drmType.equals("ECO_MOA") || drmType.equals(
                "ALADIN"
            ))
        ) {
            val fileDir =
                File(
                    "${LibrosUtil.getEPUBRootPath(this)}/$filePath/${this.intent.getStringExtra("epubFileName")!!}/${
                        this.intent.getStringExtra(
                            "epubFileName"
                        )!!.split("\\.")[0]
                    }"
                )
            if (fileDir.exists()) {
                FileManager().deleteFolder(fileDir)
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }
}
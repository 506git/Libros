package eco.libros.android.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ViewerDBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "viewerDataBase"

        private const val SQL_BOOK_CREATE_ENTRIES = "create table ${ContactViewerContract.Entry.BOOK_TABLE_NAME} ("+
                "${ContactViewerContract.Entry._id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
                "${ContactViewerContract.Entry.content_id} TEXT, " +
                "${ContactViewerContract.Entry.root_path} TEXT, " +
                "${ContactViewerContract.Entry.book_data} TEXT);"

        private const val SQL_BOOKMARK_CREATE_ENTRIES = "create table ${ContactViewerContract.Entry.BOOKMARK_TABLE_NAME} (" +
                "${ContactViewerContract.Entry._id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${ContactViewerContract.Entry.bookId} INTEGER, " +
                "${ContactViewerContract.Entry.create_time} INTEGER, " +
                "${ContactViewerContract.Entry.idref} TEXT, " +
                "${ContactViewerContract.Entry.cfi} TEXT, " +
                "${ContactViewerContract.Entry.content} TEXT);"

        private const val SQL_HIGHLIGHT_CREATE_ENTRIES = "create table ${ContactViewerContract.Entry.HIGHLIGHT_TABLE_NAME} (" +
                "${ContactViewerContract.Entry._id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "${ContactViewerContract.Entry.bookId} INTEGER, " +
                "${ContactViewerContract.Entry.create_time} INTEGER, " +
                "${ContactViewerContract.Entry.idref} TEXT, " +
                "${ContactViewerContract.Entry.cfi} TEXT, " +
                "${ContactViewerContract.Entry.content} TEXT, " +
                "${ContactViewerContract.Entry.memo} TEXT, " +
                "${ContactViewerContract.Entry.color} TEXT);"

        private val SQL_BOOK_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactViewerContract.Entry.BOOK_TABLE_NAME}"
        private val SQL_BOOKMARK_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactViewerContract.Entry.BOOKMARK_TABLE_NAME}"
        private val SQL_HIGHLIGHT_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactViewerContract.Entry.HIGHLIGHT_TABLE_NAME}"

        private var sInstance: ViewerDBHelper? = null

        @Synchronized
        fun getInstance(context: Context): ViewerDBHelper?{
            if(sInstance == null){
                sInstance = ViewerDBHelper(context)
            }
            return sInstance
        }
    }



    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_BOOK_CREATE_ENTRIES)
        db?.execSQL(SQL_BOOKMARK_CREATE_ENTRIES)
        db?.execSQL(SQL_HIGHLIGHT_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        onCreate(db)
    }
}
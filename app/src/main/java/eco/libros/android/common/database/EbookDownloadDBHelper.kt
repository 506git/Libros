package eco.libros.android.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class EbookDownloadDBHelper private constructor(context: Context) :SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "EBookDownloadDatabase"

        private val SQL_CREATE_ENTRIES = "create table ${ContactEbookContract.Entry.TABLE_NAME} ("+
                "${ContactEbookContract.Entry.id} INTEGER PRIMARY KEY AUTOINCREMENT not null, " +
                "${ContactEbookContract.Entry.title} TEXT, " +
                "${ContactEbookContract.Entry.fileName} TEXT, " +
                "${ContactEbookContract.Entry.libCode} TEXT, " +
                "${ContactEbookContract.Entry.libName} TEXT, " +
                "${ContactEbookContract.Entry.eBookCoverImage} TEXT, " +
                "${ContactEbookContract.Entry.lentKey} TEXT, " +
                "${ContactEbookContract.Entry.bookId} TEXT not null, " +
                "${ContactEbookContract.Entry.comkey} TEXT not null, " +
                "${ContactEbookContract.Entry.ISBN} TEXT, " +
                "${ContactEbookContract.Entry.fileType} TEXT NOT NULL, " +
                "${ContactEbookContract.Entry.drm_info} TEXT, " +
                "${ContactEbookContract.Entry.drm} TEXT NOT NULL, " +
                "${ContactEbookContract.Entry.downloadYn} TEXT NOT NULL default 'Y', " +
                "${ContactEbookContract.Entry.useStartTime} TEXT, " +
                "${ContactEbookContract.Entry.useEndTime} TEXT, " +
                "${ContactEbookContract.Entry.returnDate} TEXT);"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactContract.Entry.TABLE_NAME}"

        private var sInstance: EbookDownloadDBHelper? = null

        @Synchronized
        fun getInstance(context: Context): EbookDownloadDBHelper?{
            if (sInstance == null){
                sInstance = EbookDownloadDBHelper(context)
            }
            return sInstance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if(oldVersion < 2) {
            db?.execSQL(SQL_DELETE_ENTRIES)
            onCreate(db)
        }
    }
}
package eco.libros.android.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class NoticeDBHelper private constructor(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "noticeDBHelper"

        private const val SQL_LIB_NOTICE_CREATE_ENTRIES = "create table ${ContactNoticeContract.Entry.TABLE_NAME} ("+
                "${ContactNoticeContract.Entry._id} INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "+
                "${ContactNoticeContract.Entry.libCode} TEXT, " +
                "${ContactNoticeContract.Entry.time} TEXT);"

        private val SQL_LIB_NOTICE_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactNoticeContract.Entry.TABLE_NAME}"

        private var sInstance: NoticeDBHelper? = null

        @Synchronized
        fun getInstance(context: Context): NoticeDBHelper?{
            if(sInstance == null){
                sInstance = NoticeDBHelper(context)
            }
            return sInstance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_LIB_NOTICE_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        onCreate(db)
    }
}

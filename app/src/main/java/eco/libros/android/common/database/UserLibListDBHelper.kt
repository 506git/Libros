package eco.libros.android.common.database

import android.content.Context
import android.database.sqlite.SQLiteCursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.ContactsContract

class UserLibListDBHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "userLibListDatabase"

        private const val SQL_LIB_CREATE_ENTRIES = "create table ${ContactContract.Entry.TABLE_NAME} ("+
                "${ContactContract.Entry._id} INTEGER PRIMARY KEY AUTOINCREMENT not null," +
                "${ContactContract.Entry.library} TEXT not null," +
                "${ContactContract.Entry.libraryCode} TEXT not null," +
                "${ContactContract.Entry.libraryUserId} TEXT not null," +
                "${ContactContract.Entry.libraryUserPw} TEXT not null," +
                "${ContactContract.Entry.libraryUserNo} TEXT not null," +
                "${ContactContract.Entry.certifyKindId} TEXT not null," +
                "${ContactContract.Entry.isUserCertifyYn} TEXT not null," +
                "${ContactContract.Entry.isEBookCertifyYn} TEXT not null," +
                "${ContactContract.Entry.isEBookServiceYn} TEXT not null," +
                "${ContactContract.Entry.isJoinYn} TEXT not null," +
                "${ContactContract.Entry.isSearchYn} TEXT not null," +
                "${ContactContract.Entry.isSeatServiceYn} TEXT not null," +
                "${ContactContract.Entry.eBookId} TEXT," +
                "${ContactContract.Entry.eBookPw} TEXT);"

        private const val SQL_MENU_CREATE_ENTRIES = "create table ${ContactContract.MenuEntry.TABLE_NAME} ("+
                "${ContactContract.MenuEntry._id} INTEGER PRIMARY KEY AUTOINCREMENT," +
                "${ContactContract.MenuEntry.libraryCode} TEXT not null," +
                "${ContactContract.MenuEntry.libraryMenuId} TEXT not null," +
                "${ContactContract.MenuEntry.libraryMenuSeq} INTEGER not null," +
                "${ContactContract.MenuEntry.libraryMenuIconId} TEXT," +
                "${ContactContract.MenuEntry.pageLinkPath} TEXT);"

        private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactContract.Entry.TABLE_NAME}"
        private val SQL_MENU_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactContract.MenuEntry.TABLE_NAME}"

        private var sInstance: UserLibListDBHelper? = null

        @Synchronized
        fun getInstance(context: Context): UserLibListDBHelper?{
            if (sInstance == null){
                sInstance = UserLibListDBHelper(context)
            }
            return sInstance
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_LIB_CREATE_ENTRIES)
        db?.execSQL(SQL_MENU_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

            db?.execSQL(SQL_DELETE_ENTRIES)
            db?.execSQL(SQL_MENU_DELETE_ENTRIES)

        onCreate(db)
    }
}
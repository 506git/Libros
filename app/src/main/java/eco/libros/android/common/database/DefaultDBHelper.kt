package eco.libros.android.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import eco.libros.android.R
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import java.io.*

class DefaultDBHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
)  {

    override fun onCreate(p0: SQLiteDatabase?) {

    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    init {
        mContext = context
        SEARCH_DB_PATH = "/data/data/${context.packageName}/databases/"
    }

    companion object{
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "defaultInfo.sqlite";
        private lateinit var SEARCH_DB_PATH: String
        private lateinit var mContext: Context

        private var sInstance: DefaultDBHelper? = null

        @Synchronized
        fun getInstance(context: Context): DefaultDBHelper?{
            if(sInstance == null){
                sInstance = DefaultDBHelper(context)
            }
//            SEARCH_DB_PATH = "/data/data/${context.packageName}/databases/"
            return sInstance
        }
    }

    fun updateDataBase(sqlFile: ByteArray?) {
        deleteDataBaseFile()
        val outFileName =
            SEARCH_DB_PATH + DATABASE_NAME
        var myOutput: OutputStream? = null
        try {
            myOutput = FileOutputStream(outFileName)
            myOutput.write(sqlFile)
            myOutput.flush()
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            LibrosLog.print(e.toString())
        } finally {
            try {
                myOutput?.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                LibrosLog.print(e.toString())
            }
        }
    }

    fun deleteDataBaseFile() {
        val dbExist: Boolean = checkDataBase()
        if (dbExist) {
            //do nothing - database already exist
        } else {
            this.readableDatabase
            val deleteFileName =
                SEARCH_DB_PATH + DATABASE_NAME
            val file = File(deleteFileName)
            file.delete()
        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        val myPath =
            SEARCH_DB_PATH +DATABASE_NAME
        checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS)
        checkDB?.close()
        return checkDB != null
    }

    fun initCreateDataBase() {
        try {
            var file = File(
                SEARCH_DB_PATH,
                DATABASE_NAME
            )
            if (!file.exists()) {
                val db_Read = this.readableDatabase
                db_Read.close()
                copyDataBase()
                LibrosUtil.setSharedData(
                    mContext,
                    mContext.resources.getString(R.string.version_default_db),
                    "0"
                )
            }
            file = File(SEARCH_DB_PATH, "libList.sqlite")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: IOException) {
            // TODO: handle exception
            LibrosLog.print(e.toString())
        }
    }

    private fun copyDataBase() {
        val myInput: InputStream = mContext.assets.open(DATABASE_NAME)
        val outFileName =
            SEARCH_DB_PATH + DATABASE_NAME
        val myOutput: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(1024)
        var length: Int
        try {
            while (myInput.read(buffer).also { length = it } != -1) {
                myOutput.write(buffer, 0, length)
            }
        } finally {
            myOutput.flush()
            myOutput.close()
            myInput.close()
        }
    }


}
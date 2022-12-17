package eco.libros.android.common.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class NoticeDBFacade(context: Context) {
    private val mHelpers: NoticeDBHelper = NoticeDBHelper.getInstance(context)!!
    var mContext : Context? = null

    private val DATE_FORMAT = "yyyyMMdd"

    fun insertLib(libCode: String){
        var count = 0
        val db = mHelpers.writableDatabase
        db.beginTransaction()

        val query = "SELECT count(*) as count from ${ContactNoticeContract.Entry.TABLE_NAME} WHERE ${ContactNoticeContract.Entry.libCode}='"+libCode+"';"

        val cursor = db.rawQuery(query, null)
        if (cursor != null && cursor.count != 0){
            cursor.moveToFirst()
            count = cursor.getInt(cursor.getColumnIndex("count"))
        }

        db.setTransactionSuccessful()
        db.endTransaction()
        if (count >0)
            update(libCode)
         else
            insert(libCode)
    }

    fun update(libCode: String){
        val db = mHelpers.writableDatabase

        val strTime = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

        val cvTime = ContentValues()
        cvTime.put(ContactNoticeContract.Entry.time, strTime)

        db.update(ContactNoticeContract.Entry.TABLE_NAME,cvTime,"${ContactNoticeContract.Entry.libCode}=?", arrayOf(libCode))
        db.close()
    }

    fun insert(libCode: String){
        val db = mHelpers.writableDatabase

        val strTime = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

        val cvNotice = ContentValues()
        cvNotice.put(ContactNoticeContract.Entry.time, strTime)
        cvNotice.put(ContactNoticeContract.Entry.libCode, libCode)
        try {
            db.insert(ContactNoticeContract.Entry.TABLE_NAME, null, cvNotice)
            db.close()
        }catch (e: Exception){
            Log.d("TEST",e.toString())
        }
    }

    fun isShowMsg(libCode: String): Boolean{
        var showMsg: Boolean = true
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val query = "SELECT " + ContactNoticeContract.Entry.libCode + ", " + ContactNoticeContract.Entry.time + " FROM ${ContactNoticeContract.Entry.TABLE_NAME} " +
                "WHERE ${ContactNoticeContract.Entry.libCode}='"+libCode+"';"

        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.count != 0){
            cursor.moveToFirst()
            val lastTime = cursor.getString(cursor.getColumnIndex("TIME"))
            cursor.close()

            if (lastTime != null && lastTime.trim().isNotEmpty()){
                val nowTime = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())

                if (nowTime != null && lastTime.equals(nowTime)){
                    showMsg = false
                }
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        return showMsg
    }


    fun removeLib(libCode: String){
        val db = mHelpers.writableDatabase

        db.delete(ContactNoticeContract.Entry.TABLE_NAME,"${ContactNoticeContract.Entry.libCode}=?", arrayOf(libCode))
        db.close()
    }
}
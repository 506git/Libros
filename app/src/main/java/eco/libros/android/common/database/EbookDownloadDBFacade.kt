package eco.libros.android.common.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import eco.libros.android.common.model.EbookDownloadListVO
import eco.libros.android.common.model.EbookListVO
import eco.libros.android.common.utill.LibrosLog
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault
import kotlin.collections.ArrayList

class EbookDownloadDBFacade(context: Context) {
    private val mHelpers: EbookDownloadDBHelper = EbookDownloadDBHelper.getInstance(context)!!
    var mContext: Context? = null

    init {
        mContext = context
    }

    fun insertData(ebook: EbookListVO) {
        val db = mHelpers.writableDatabase

        val query =
                ("select " + ContactEbookContract.Entry.id + " from " + ContactEbookContract.Entry.TABLE_NAME + " Where " + ContactEbookContract.Entry.comkey + "='" + ebook.comKey + "' ")
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        if (cursor.count == 0) {
            val cv = ContentValues()
            cv.put(ContactEbookContract.Entry.fileName, ebook.fileName)
            cv.put(ContactEbookContract.Entry.libCode, ebook.libCode)
            cv.put(ContactEbookContract.Entry.libName, ebook.libName)
            cv.put(ContactEbookContract.Entry.eBookCoverImage, ebook.thumbnail)
            cv.put(ContactEbookContract.Entry.bookId, ebook.bookId)
            cv.put(ContactEbookContract.Entry.comkey, ebook.comKey)
            cv.put(ContactEbookContract.Entry.lentKey, ebook.lentKey)
            cv.put(ContactEbookContract.Entry.ISBN, ebook.isbn)
            cv.put(ContactEbookContract.Entry.fileType, ebook.fileType)
            cv.put(ContactEbookContract.Entry.drm_info, ebook.drmInfo)
            cv.put(ContactEbookContract.Entry.drm, ebook.drm)
            cv.put(ContactEbookContract.Entry.downloadYn, ebook.downloadYn)
            cv.put(ContactEbookContract.Entry.returnDate, ebook.returnDate)
            cv.put(ContactEbookContract.Entry.title, ebook.title)
            cv.put(ContactEbookContract.Entry.useStartTime, ebook.useStartTime)
            cv.put(ContactEbookContract.Entry.useEndTime, ebook.useEndTime)
            db.beginTransaction()
            try {
                val rowId = db.insert(ContactEbookContract.Entry.TABLE_NAME, null, cv)
                if (rowId < 0) {
                    throw SQLException("Fail to Insert")
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                LibrosLog.print(e.toString())
            } finally {
                db.endTransaction()
                LibrosLog.print("DB Inserted $ebook.libraryName idx =$ebook.libraryCode")
            }
        }
        cursor.close()
        db.close()
    }

    fun getDownloadEBookCheck(){
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val selectQuery = ("SELECT * FROM " + ContactEbookContract.Entry.TABLE_NAME)
        var cursor: Cursor? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                do {
                    val strExpiredDate = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.returnDate))

                    if(strExpiredDate.isNotEmpty()){
                        val date : Date? = SimpleDateFormat("yyyy-MM-dd").parse(strExpiredDate)

                        if (date != null){
                            val expireDate = date.time
                            val nowTime: Long = System.currentTimeMillis()
                            if (expireDate <= nowTime){

                                val bookId = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.lentKey))
                                val libCode = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.libCode))
                                deleteBook(libCode, bookId)
                                ViewerDBFacade(mContext!!).deleteBook(bookId)
                            }
                        }
                    }
                } while (cursor.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
    }

    fun getDownloadEBookYn(): ArrayList<EbookDownloadListVO> {
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val selectQuery =
                ("SELECT " + ContactEbookContract.Entry.lentKey + ", " + ContactEbookContract.Entry.downloadYn + " FROM " + ContactEbookContract.Entry.TABLE_NAME)
        var cursor: Cursor? = null
        val ebookDownloadList = ArrayList<EbookDownloadListVO>()
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                do {
                    val lib = EbookDownloadListVO(
                            cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.lentKey)),
                            cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.downloadYn))
                    )
                    ebookDownloadList.add(lib)
                } while (cursor.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return ebookDownloadList
    }

    fun getDownloadFileName(bookId: String): EbookListVO {
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val selectQuery =
                ("SELECT * FROM " + ContactEbookContract.Entry.TABLE_NAME +
                        " WHERE " + ContactEbookContract.Entry.lentKey + " = '" + bookId + "'")
        var cursor: Cursor? = null
        var ebookFileName: EbookListVO? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                ebookFileName = EbookListVO(
                        fileName = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.fileName)),
                        drm = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.drm)),
                        drmInfo = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.drm_info)),
                        returnDate = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.returnDate)),
                        fileType = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.fileType)),
                        lentKey = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.lentKey)),
                        title = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.title)),
                        isbn = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.ISBN)),
                        bookId = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.bookId)),
                        libName = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.libName)),
                        downloadYn = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.downloadYn)),
                        libCode = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.libCode)),
                        comKey = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.comkey)),
                        thumbnail = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.eBookCoverImage)),
                        useStartTime = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.useStartTime)),
                        useEndTime = cursor.getString(cursor.getColumnIndex(ContactEbookContract.Entry.useEndTime))
                )
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return ebookFileName!!
    }

    fun deleteEBookDB() {
        val db = mHelpers.writableDatabase
        db.delete(ContactEbookContract.Entry.TABLE_NAME, null, null)
    }

    fun deleteBook(libCode: String, bookId: String) {
        val db = mHelpers.writableDatabase
        db.delete(ContactEbookContract.Entry.TABLE_NAME, ContactEbookContract.Entry.lentKey + " = '$bookId'" + "and " + ContactEbookContract.Entry.libCode + " = '$libCode'", null)
    }
}
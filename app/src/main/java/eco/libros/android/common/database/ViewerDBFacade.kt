package eco.libros.android.common.database

import android.content.ContentValues
import android.content.Context
import android.util.Log
import eco.libros.android.common.model.BookVO
import eco.libros.android.common.utill.LibrosLog
import java.lang.Exception
import java.sql.SQLException

class ViewerDBFacade(context: Context) {
    private val mHelpers: ViewerDBHelper = ViewerDBHelper.getInstance(context)!!
    var mContext : Context? = null

    fun insertOrUpdateBook(_contentId: String?, rootPath: String, bookData: String?){
        var contentId = _contentId
        val db = mHelpers.writableDatabase
        db.beginTransaction()
        val query = "SELECT * from ${ContactViewerContract.Entry.BOOK_TABLE_NAME} WHERE ${ContactViewerContract.Entry.root_path}='"+rootPath+"';"

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        try {
            if (cursor.count == 0) {
                val cv = ContentValues()
                cv.put("content_id", contentId)
                cv.put("root_path", rootPath)
                cv.put("book_data", bookData)

               val rowMenuId = db.insert(ContactViewerContract.Entry.BOOK_TABLE_NAME, null, cv)
                if (rowMenuId < 0){
                    throw SQLException("Fail to Insert")
                }
                LibrosLog.print("DB Inserted $contentId idx =$rootPath")
                db.setTransactionSuccessful()
            } else {
                val cBook = db.query(
                    ContactViewerContract.Entry.BOOK_TABLE_NAME,
                    arrayOf(ContactViewerContract.Entry.content_id),
                    "${ContactViewerContract.Entry.root_path}=?",
                    arrayOf(rootPath),
                    null,
                    null,
                    null
                )
                if (cBook != null && cBook.moveToFirst()) {
                    contentId = cBook.getString(0)
                }
                updateBookById(contentId, bookData)
                cBook.close()
            }
        } catch (e: Exception){
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        db.close()
        cursor.close()
    }

    fun updateBookById(contentId:String?, bookData: String?){
        val db = mHelpers.writableDatabase

        val cvBook = ContentValues()
        cvBook.put(ContactViewerContract.Entry.book_data, bookData)
        db.update(ContactViewerContract.Entry.BOOK_TABLE_NAME,cvBook,"${ContactViewerContract.Entry.content_id}=?", arrayOf(contentId))
        db.close()
    }

    fun getBookById(contentId: String?): BookVO?{
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        var book: BookVO? = null
        try {
            val cBook = db.query(
                ContactViewerContract.Entry.BOOK_TABLE_NAME,
                arrayOf("${ContactViewerContract.Entry.content_id}, ${ContactViewerContract.Entry.root_path}, ${ContactViewerContract.Entry.book_data}"),
                "${ContactViewerContract.Entry.content_id}=?",
                arrayOf(contentId),
                null, null, null
            )
            if (cBook != null && cBook.moveToFirst()) {
                book = BookVO(
                    contentId = cBook.getString(0),
                    bookName = "",
                    rootPath = cBook.getString(1),
                    bookData = cBook.getString(2)
                )
            }

            db.setTransactionSuccessful()
        }catch (e: Exception){
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return book
    }

    fun deleteBook(contentId: String?){
        val db = mHelpers.writableDatabase

        db.delete(ContactViewerContract.Entry.BOOK_TABLE_NAME, "${ContactViewerContract.Entry.content_id}=?", arrayOf(contentId))
        db.close()
    }

}
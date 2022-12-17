package eco.libros.android.common.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import androidx.core.content.contentValuesOf
import eco.libros.android.common.crypt.Cryptchar
import eco.libros.android.common.model.LibraryMenuListDataVo
import eco.libros.android.common.model.UserLibInfVO
import eco.libros.android.common.model.UserLibListDataVO
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import java.sql.SQLException

class UserLibListDBFacade(context: Context) {
    private val mHelpers: UserLibListDBHelper = UserLibListDBHelper.getInstance(context)!!
    var mContext: Context? = null

    fun insertData(user: UserLibListDataVO) {
        val db = mHelpers.writableDatabase

        val query =
                ("select " + ContactContract.Entry.libraryCode + " from " + ContactContract.Entry.TABLE_NAME + " Where " + ContactContract.Entry.libraryCode + "='" + user.libraryCode + "' ")
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        if (cursor.count == 0) {
            val cv = ContentValues()
            cv.put(ContactContract.Entry.library, user.libraryName)
            cv.put(ContactContract.Entry.libraryCode, user.libraryCode)
            cv.put(ContactContract.Entry.libraryUserId, user.libraryUserId)
            cv.put(ContactContract.Entry.libraryUserPw, user.libraryUserPw)
            cv.put(ContactContract.Entry.libraryUserNo, user.libraryUserNo)
            cv.put(ContactContract.Entry.certifyKindId, user.certifyKindId)
            cv.put(ContactContract.Entry.isUserCertifyYn, user.isUserCertifyYn)
            cv.put(ContactContract.Entry.isEBookCertifyYn, user.isEBookCertifyYn)
            cv.put(ContactContract.Entry.isEBookServiceYn, user.isEBookServiceYn)
            cv.put(ContactContract.Entry.isJoinYn, user.isJoinYn)
            cv.put(ContactContract.Entry.isSearchYn, user.isSearchYn)
            cv.put(ContactContract.Entry.isSeatServiceYn, user.isSeatServiceYn)
            cv.put(ContactContract.Entry.eBookId, user.eBookId)
            cv.put(ContactContract.Entry.eBookPw, user.eBookPw)

            db.beginTransaction()
            try {
                val rowId = db.insert(ContactContract.Entry.TABLE_NAME, null, cv)
                for (i in user.UserLibraryInfoList) {
                    val cvm = ContentValues()
                    cvm.put(ContactContract.MenuEntry.libraryCode, user.libraryCode)
                    cvm.put(ContactContract.MenuEntry.libraryMenuId, i.libraryMenuId)
                    cvm.put(ContactContract.MenuEntry.libraryMenuSeq, i.libraryMenuSeq)
                    cvm.put(ContactContract.MenuEntry.libraryMenuIconId, i.libraryMenuIconId)
                    cvm.put(ContactContract.MenuEntry.pageLinkPath, i.pageLinkPath)
                    val rowMenuId = db.insert(ContactContract.MenuEntry.TABLE_NAME, null, cvm)
                    if (rowMenuId < 0) {
                        throw SQLException("Fail to Insert")
                    }
                }
                if (rowId < 0) {
                    throw SQLException("Fail to Insert")
                }
                db.setTransactionSuccessful()
            } catch (e: Exception) {
                LibrosLog.print(e.toString())
            } finally {
                db.endTransaction()
                LibrosLog.print("DB Inserted $user.libraryName idx =$user.libraryCode")
            }
        }
        cursor.close()
        db.close()
    }

    fun getCertify(libCode: String): String {
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val selectQuery =
                ("SELECT " + ContactContract.Entry.isUserCertifyYn + " FROM " + ContactContract.Entry.TABLE_NAME +
                        " WHERE " + ContactContract.Entry.libraryCode + "='" + libCode + "' ")
        var cursor: Cursor? = null

        lateinit var certify: String
        try {
            cursor = db.rawQuery(selectQuery, null)
            if (cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                certify =
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isUserCertifyYn))
            } else {
                certify = "N"
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            certify = "N"
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return certify
    }

    fun getCertifyInfo(libCode: String): UserLibInfVO?{
        val db = mHelpers.readableDatabase

        val query = "SELECT ${ContactContract.Entry.libraryUserId}, ${ContactContract.Entry.libraryUserPw}, ${ContactContract.Entry.libraryUserNo}, " +
                "${ContactContract.Entry.eBookId}, ${ContactContract.Entry.eBookPw}, ${ContactContract.Entry.isEBookCertifyYn}, ${ContactContract.Entry.isUserCertifyYn} " +
                "FROM ${ContactContract.Entry.TABLE_NAME} WHERE ${ContactContract.Entry.libraryCode} = '" + libCode+ "'"

        val cursor = db.rawQuery(query,null)

        if (cursor != null && cursor.count != 0){
            cursor.moveToFirst()

            var libPw = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserPw))
            var ebookPw = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookPw))
            var userNo = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserNo))

            if (libPw.trim().isNotEmpty()){
                libPw = Cryptchar.decrypt(LibrosUtil.getUserId(context = mContext!!, needEncoding = true, needEncrypt = false), libPw)
            }

            if (ebookPw.trim().isNotEmpty()){
                ebookPw = Cryptchar.decrypt(LibrosUtil.getUserId(context = mContext!!, needEncoding = true, needEncrypt = false), ebookPw)
            }

            if (userNo.trim().isNotEmpty()){
                userNo = Cryptchar.decrypt(LibrosUtil.getUserId(context = mContext!!, needEncoding = true, needEncrypt = false), userNo)
            }
//            cursor.close()
//            db.close()
           return UserLibInfVO(
                    libraryUserId = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserId)),
                    libraryUserPw = libPw,
                    libraryUserNo = userNo,
                    eBookId = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookId)),
                    eBookPw = ebookPw,
                    isUserCertifyYn = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isUserCertifyYn)),
                    isEBookCertifyYn = cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isEBookCertifyYn))
            )
        } else
        return null

    }

    fun getCertifyKind(libCode: String): String {
        val db = mHelpers.readableDatabase
        db.beginTransaction()

        val selectQuery =
                ("SELECT " + ContactContract.Entry.certifyKindId + " FROM " + ContactContract.Entry.TABLE_NAME +
                        " WHERE " + ContactContract.Entry.libraryCode + "='" + libCode + "' ")
        var cursor: Cursor? = null

        lateinit var certify: String
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                certify =
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.certifyKindId))
            } else {
                certify = "N"
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            certify = "N"
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return certify
    }

    fun libListCheck(libCodeCheck: String) {
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        var libCode = libCodeCheck
        val selectQuery =
                ("SELECT " + ContactContract.Entry.libraryCode + " FROM " + ContactContract.Entry.TABLE_NAME +
                        " WHERE " + ContactContract.Entry.libraryCode + "='" + libCode + "' ")
        var cursor: Cursor? = null

        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor == null && cursor?.count == 0) {
//                libCode = "000000"
                libCode = "111042"
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
//            libCode = "000000"
            libCode = "111042"
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
            LibrosUtil.setLastLib(mContext!!, libCode)
        }
    }

    fun getUserLibList(): ArrayList<UserLibListDataVO> {
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        val selectQuery =
                ("SELECT * FROM " + ContactContract.Entry.TABLE_NAME)
        var cursor: Cursor? = null
        val libList = ArrayList<UserLibListDataVO>()
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                do {
                    val lib = UserLibListDataVO(
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isEBookCertifyYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.library)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryCode)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserId)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserPw)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserNo)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.certifyKindId)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isUserCertifyYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isEBookServiceYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isJoinYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isSearchYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isSeatServiceYn)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookId)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookPw)),
                            ArrayList<LibraryMenuListDataVo>()
                    )
                    libList.add(lib)
                } while (cursor.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
//            libList = "N"
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return libList
    }

    fun getLibYnInfo(libCode: String): UserLibListDataVO? {
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        val selectQuery =
            ("SELECT * FROM " + ContactContract.Entry.TABLE_NAME + " WHERE " + ContactContract.Entry.libraryCode + " = '" + libCode + "' ")
        Log.d("TESTLIB",libCode)
        var cursor: Cursor? = null
        var libInfo : UserLibListDataVO? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                libInfo = UserLibListDataVO(
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isEBookCertifyYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.library)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryCode)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserId)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserPw)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.libraryUserNo)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.certifyKindId)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isUserCertifyYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isEBookServiceYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isJoinYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isSearchYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.isSeatServiceYn)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookId)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.Entry.eBookPw)),
                        ArrayList<LibraryMenuListDataVo>()
                    )
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return libInfo
    }

    fun getLibMenuList(libCode: String): ArrayList<LibraryMenuListDataVo> {
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        val selectQuery =
                ("SELECT * FROM " + ContactContract.MenuEntry.TABLE_NAME + " WHERE " + ContactContract.MenuEntry.libraryCode + " = '" + libCode + "' ")
        var cursor: Cursor? = null
        val libList = ArrayList<LibraryMenuListDataVo>()
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                do {
                    val lib = LibraryMenuListDataVo(
                            cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryCode)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuId)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuSeq)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuIconId)),
                            cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.pageLinkPath))
                    )
                    libList.add(lib)
                } while (cursor.moveToNext())
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return libList
    }

    fun getMenuInfo(libCode: String, menuId: String): LibraryMenuListDataVo? {
        val db = mHelpers.readableDatabase
        db.beginTransaction()
        val selectQuery =
            ("SELECT * FROM " + ContactContract.MenuEntry.TABLE_NAME + " WHERE " + ContactContract.MenuEntry.libraryCode + " = '" + libCode + "'AND "
                    + ContactContract.MenuEntry.libraryMenuId + " = '" + menuId + "' ")
        var cursor: Cursor? = null
        var libList : LibraryMenuListDataVo? = null
        try {
            cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            if (cursor != null && cursor.count > 0) {
                    libList = LibraryMenuListDataVo(
                        cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryCode)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuId)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuSeq)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.libraryMenuIconId)),
                        cursor.getString(cursor.getColumnIndex(ContactContract.MenuEntry.pageLinkPath))
                    )
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        } finally {
            db.endTransaction()
        }
        return libList
    }

    fun deleteLib(libCode: String): String {
        val db = mHelpers.writableDatabase
        return try {
            db.delete(
                ContactContract.Entry.TABLE_NAME,
                ContactContract.Entry.libraryCode + " = '$libCode'",
                null
            )
            db.delete(
                ContactContract.MenuEntry.TABLE_NAME,
                ContactContract.Entry.libraryCode + " = '$libCode'",
                null
            )
            "Y"
        } catch (e: java.lang.Exception) {
            e.toString()
        }
    }

    fun deleteLibDB() {
        val db = mHelpers.writableDatabase
        db.delete(ContactContract.Entry.TABLE_NAME, null, null)
        db.delete(ContactContract.MenuEntry.TABLE_NAME, null, null)
    }

    init {
        mContext = context
    }
}
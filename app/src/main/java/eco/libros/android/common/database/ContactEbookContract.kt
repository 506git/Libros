package eco.libros.android.common.database

import android.provider.BaseColumns

object ContactEbookContract {
    const val _RESULTS = "result"

    object Entry : BaseColumns {
        const val TABLE_NAME = "EBook_Download_inf"
        const val id = "_ID"
        const val fileName = "EPUB_FILE_NAME"
        const val libCode = "LIB_CODE"
        const val title = "TITLE"
        const val libName = "LIB_NAME"
        const val eBookCoverImage = "EBOOK_COVER_IMAGE"
        const val lentKey = "LENT_KEY"
        const val bookId = "BOOK_ID"
        const val comkey = "COMKEY"
        const val ISBN = "ISBN"
        const val fileType = "FILE_TYPE"
        const val drm_info = "DRM_INFO"
        const val drm = "DRM"
        const val downloadYn = "DOWNLOAD_YN"
        const val returnDate = "RETURN_DATE"
        const val useStartTime = "USE_START_TIME"
        const val useEndTime = "USE_END_TIME"
    }

}
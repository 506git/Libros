package eco.libros.android.common.database

import android.provider.BaseColumns

object ContactViewerContract {
    const val _RESULTS = "result"

    object Entry : BaseColumns{
        const val BOOK_TABLE_NAME = "book_TB"

        const val _id = "_id"
        const val content_id = "content_id"
        const val root_path = "root_path"
        const val book_data = "book_data"

        const val BOOKMARK_TABLE_NAME = "bookmark_TB"
        const val bookId = "book_id"
        const val create_time = "create_time"
        const val idref = "idref"
        const val cfi = "cfi"
        const val content = "content"

        const val HIGHLIGHT_TABLE_NAME = "highlight_TB"
        const val memo = "memo"
        const val color = "color"
    }
}

package eco.libros.android.common.database

import android.provider.BaseColumns

object ContactNoticeContract {
    const val _RESULTS = "result"

    object Entry : BaseColumns {
        const val TABLE_NAME = "LIB_NOTICE_TBL"

        const val _id = "_id"
        const val libCode = "LIB_CODE"
        const val time = "TIME"
    }
}
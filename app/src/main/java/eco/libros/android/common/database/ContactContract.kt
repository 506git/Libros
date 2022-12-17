package eco.libros.android.common.database

import android.provider.BaseColumns

object ContactContract {
    const val _RESULTS = "result"

    object Entry : BaseColumns{
        const val TABLE_NAME = "USER_LIB_LIST"

        const val _id = "ID"
        const val library = "LIBRARY_NAME"
        const val libraryCode = "LIBRARY_CODE"
        const val libraryUserId = "LIBRARY_USER_ID"
        const val libraryUserPw = "LIBRARY_USER_PW"
        const val libraryUserNo = "LIBRARY_USER_NO"
        const val certifyKindId = "CERTIFY_KIND_ID"
        const val isUserCertifyYn = "IS_USER_CERTIFY_YN"
        const val isEBookCertifyYn = "IS_EBOOK_CERTIFY_YN"
        const val isEBookServiceYn = "IS_EBOOK_SERVICE_YN"
        const val isJoinYn = "IS_JOIN_YN"
        const val isSearchYn = "IS_SEARCH_YN"
        const val isSeatServiceYn = "IS_SEAT_SERVICE_YN"
        const val eBookId = "EBOOK_ID"
        const val eBookPw = "EBOOK_PW"
    }

    object MenuEntry : BaseColumns{
        const val TABLE_NAME = "USER_LIB_MENU_LIST"

        const val _id = "ID"
        const val libraryCode = "LIBRARY_CODE"
        const val libraryMenuId = "LIBRARY_MENU_ID"
        const val libraryMenuSeq = "LIBRARY_MENU_SEQ"
        const val libraryMenuIconId = "LIBRARY_MENU_ICON_ID"
        const val pageLinkPath = "PAGE_LINK_PATH"
    }

}
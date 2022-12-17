package eco.libros.android.common.variable

class GlobalVariable{
    companion object {
        const val LIBROS_LOGIN_ACTIVITY = 25

        const val LIBORS_PERMISSION_BASIC = 1001
        const val LIBORS_PERMISSION_SELECT = 1002
        const val LIBORS_PERMISSION_CAMERA = 1001

        const val SHOW_LOG = true
        const val TIMEOUT_CONNECTION = 30000
        const val TIMEOUT_SOCKET = 60000
        const val LOGIN_SUCCESS = 100

        const val EBOOK_TIMEOUT_CONNECTION = 60000
        const val EBOOK_TIMEOUT_SOCKET = 100000

        const val NOTIFICATION_NO = 101

        const val PERMISSION_REQUEST_LOCATION_CODE = 2000
        const val PERMISSION_REQUEST_CAMERA_CODE = 2001
        const val PERMISSION_REQUEST_BARCODE_CODE = 2002
        const val PERMISSION_REQUEST_WRITE_CODE = 2003
        const val PERMISSION_REQUEST_AUDIO = 2004
        const val BARCODE_EBOOK_ACTIVITY = 2005
        const val CAPTURE_IMAGE_REQUEST_CODE = 2006
        const val CAPTURE_ALBUM_REQUEST_CODE = 2007
        const val AUDIO_REQUEST_CODE = 2008
        const val NFC_REQUEST_CODE = 2009

        const val BARCODE_SMARTPHONE_AUTH = 3003
        const val BARCODE_SCAN = 3004
        const val EXIT_APP_BACK_BUTTON_PERIOD = 3000

        const val REQUEST_CODE_UPDATE = 1000

        var mode: Int = GlobalVariable.GET_SYSTEM_INFORMATION
        private const val GET_SYSTEM_INFORMATION = 0
        const val WRITE_AFI = 1
        const val RESET_EAS = 3
        const val INVENTORY = 9

        const val DOWNLOAD_RESULT = "co.kr.eco.app.libros.downloadResult";

        const val PRIVATE_TERMS = "PrivateInfoPolicy"
        const val SERVICE_USE_TERMS = "ServiceUseAgreement"
    }
}
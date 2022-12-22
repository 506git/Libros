package eco.libros.android.common.variable

class GlobalVariable{
    companion object {
        const val SHOW_LOG = true
        const val TIMEOUT_CONNECTION = 30000
        const val TIMEOUT_SOCKET = 60000

        const val EBOOK_TIMEOUT_CONNECTION = 60000
        const val EBOOK_TIMEOUT_SOCKET = 100000

        const val NOTIFICATION_NO = 101

        const val AUDIO_REQUEST_CODE = 2008

        const val BARCODE_SMARTPHONE_AUTH = 3003
        const val BARCODE_SCAN = 3004

        const val REQUEST_CODE_UPDATE = 1000

        var mode: Int = GlobalVariable.GET_SYSTEM_INFORMATION
        private const val GET_SYSTEM_INFORMATION = 0
        const val WRITE_AFI = 1
        const val INVENTORY = 9

        const val DOWNLOAD_RESULT = "co.kr.eco.app.libros.downloadResult";

        const val SERVICE_USE_TERMS = "ServiceUseAgreement"
    }
}
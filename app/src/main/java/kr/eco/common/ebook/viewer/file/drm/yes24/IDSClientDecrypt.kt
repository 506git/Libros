package kr.eco.common.ebook.viewer.file.drm.yes24

import android.annotation.SuppressLint
import android.app.Activity
import androidx.fragment.app.FragmentActivity
import btworks.drm.client.IDSClientApi
import btworks.drm.client.XMLUtil
import btworks.drm.context.DRMConstants
import btworks.drm.util.TimeUtil
import btworks.drm.util.TimeUtil.getTimeStamp_0900
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ebook.download.FileManager
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IDSClientApiImpl
import org.dom4j.Document
import org.dom4j.Element
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

class IDSClientDecrypt(_activity: Activity, _sdCardDirName: String, _eBookFolderName: String, _userId: String, _epubLibName: String) {

    init {
        activity = _activity
        libName = _epubLibName
        g_userId = _userId

        fileName = if (_eBookFolderName.contains(".epub")){
            val index = _eBookFolderName.indexOf(".epub")
            _eBookFolderName.substring(0, index)
        } else{
            _eBookFolderName
        }

        eBookFileFolderName = "${LibrosUtil.getEPUBRootPath(activity)}/$_sdCardDirName/$_eBookFolderName/"
        try{
            startDownload()
        } catch (e: Exception){
            LibrosLog.print(e.toString())
        }
    }

    private fun startDownload() {
        // ---------------------------------------------------------------------------------------
        //  (*) 초기변수 설정
        // ---------------------------------------------------------------------------------------

        // ---------------------------------------------------------------------------------------
        //  (*) 초기변수 설정
        // ---------------------------------------------------------------------------------------

        val deviceType = DRMConstants.SYSTYPE_PORTABLE_ANDROID

        val userId = g_userId

        val PROFILE_FILENAME: String = libName + "_" + userId + ".xml"

        idsc = IDSClientApiImpl(activity)
        // ---------------------------------------------------------------------------------------
        //  1. 프로파일 복호화/로딩
        // ---------------------------------------------------------------------------------------

        // ---------------------------------------------------------------------------------------
        //  1. 프로파일 복호화/로딩
        // ---------------------------------------------------------------------------------------
        val encProfile: ByteArray? = FileManager().readPrivateFile(activity, PROFILE_FILENAME)

        val profileBytes = idsc?.decryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, encProfile)
                ?: return

        val profileDoc = XMLUtil.decodeDocument(profileBytes)
        val profileElm = profileDoc.rootElement // <ViewerProfile> element


        val p_serviceName = profileElm.element("ServiceName").text
        val p_userId = profileElm.element("UserID").text
        val p_passwdHash = profileElm.element("PasswdHash").text
        val p_signPubKid = profileElm.element("SignPubKid").text
        val p_kekPub = profileElm.element("KekPub").text
        val p_kekPriv = profileElm.element("KekPriv").text

        if (p_userId != userId) {
            return
        }

        if (p_serviceName != g_serviceName) {
            return
        }
        g_b64PasswdMac = p_passwdHash
        g_b64UserPub = p_kekPub
        g_b64UserPbePriv = p_kekPriv
        g_b64SrvPubKid = p_signPubKid

        // ---------------------------------------------------------------------------------------
        //  2. 전자책 DRM 정보 추출
        // ---------------------------------------------------------------------------------------

        var rightsXmlBytes: ByteArray? = null
        var encryptionXmlBytes: ByteArray? = null

        val rightsFile: File = File(eBookFileFolderName, "$fileName/META-INF/rights.xml")
        val encryptionFile: File = File(eBookFileFolderName, "$fileName/META-INF/encryption.xml")

        rightsXmlBytes = readByteFromFile(rightsFile)
        encryptionXmlBytes = readByteFromFile(encryptionFile)

        if (rightsXmlBytes == null || rightsXmlBytes.size == 0) {
        }
        if (encryptionXmlBytes == null || encryptionXmlBytes.size == 0) {
        }
        // ---------------------------------------------------------------------------------------
        //  3. 전자책 라이센스 검증
        // ---------------------------------------------------------------------------------------

        // ---------------------------------------------------------------------------------------
        //  3. 전자책 라이센스 검증
        // ---------------------------------------------------------------------------------------
        val verifyInfo = idsc?.parseAndVerifyXRML(deviceType, rightsXmlBytes, g_b64SrvPubKid)

        // ####################################################################
        //  [필독.1] 라이센스 검증 실패 시, 절대로 전자책 복호화-보기를 실행하지 않아야 함!
        // ####################################################################

        // ####################################################################
        //  [필독.1] 라이센스 검증 실패 시, 절대로 전자책 복호화-보기를 실행하지 않아야 함!
        // ####################################################################
        val failCode = verifyInfo?.get("fail.code") as String?
        if (failCode != null) {
            return
        }
        // ####################################################################
        //  [필독.2-a] 라이센스 파싱/검증 결과로부터, 유효기간을 획득하여 현재시간과 비교한다.
        //           유효기간이 아닌 경우, 절대로 전자책 복호화/보기를 실행하지 않아야 함!
        // ####################################################################

        // ####################################################################
        //  [필독.2-a] 라이센스 파싱/검증 결과로부터, 유효기간을 획득하여 현재시간과 비교한다.
        //           유효기간이 아닌 경우, 절대로 전자책 복호화/보기를 실행하지 않아야 함!
        // ####################################################################
        val notBefore = verifyInfo?.get("ids-xrml-notBefore") as String?
        val notAfter = verifyInfo?.get("ids-xrml-notAfter") as String?

        var curTime: String? = null

        // modified by powerway, 20101202
        // if(!g_serviceName.equalsIgnoreCase("IDS-KT")) {
        if (!g_serviceName.equals("IDS-KT", ignoreCase = true) && !g_serviceName.equals("IDS-IN3", ignoreCase = true)) {
            curTime = getTimeStamp_0900() //  "2010-05-04T16:29:20+0900";
        } else {
            curTime = TimeUtil.getTimeStamp_utc() //  "2010-05-04T07:29:20Z";
        }
        if (curTime < notBefore!!) {
            return
        }

        if (notAfter != "UNLIMITED" && curTime > notAfter!!) {
            return
        }
        // ####################################################################
        //  [필독.2-b] 라이센스 내 eBookId가 현재의 전자책 eBookId와 같은지 비교하는 로직이 필요함
        //           eBookId가 다른 경우, 절대로 전자책 복호화-보기를 허용하지 않아야 함!
        // ####################################################################

        // ####################################################################
        //  [필독.2-b] 라이센스 내 eBookId가 현재의 전자책 eBookId와 같은지 비교하는 로직이 필요함
        //           eBookId가 다른 경우, 절대로 전자책 복호화-보기를 허용하지 않아야 함!
        // ####################################################################
        val eBookId_from_rights = verifyInfo?.get("ids-xrml-eBookId") as String?

        val eBookId_from_ebook = eBookId_from_rights + ""

        if (eBookId_from_rights != eBookId_from_ebook) {
            return
        }
        // ####################################################################
        //  [선택.3-a] <운영 이슈>
        //             라이센스에서 사용자 ID를 획득하여,
        //             현 프로파일 또는 로그인 사용자가 아닌경우 전자책 보기를 허용하지 않음.
        // ####################################################################

        // ####################################################################
        //  [선택.3-a] <운영 이슈>
        //             라이센스에서 사용자 ID를 획득하여,
        //             현 프로파일 또는 로그인 사용자가 아닌경우 전자책 보기를 허용하지 않음.
        // ####################################################################
        val keyName = verifyInfo?.get("ids-xrml-keyname") as String?
        val userId_from_rights = keyName!!.substring(0, keyName.indexOf(":"))

        // TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함

        // TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함
        val userId_from_profile = g_userId

        if (userId_from_rights != userId_from_profile) {
            return
        }
        // ####################################################################
        //  [선택.3-b] <운영 이슈>
        //             단말기 뷰어에서 DRM 서비스/방식을 혼용하여 사용하는 경우,
        //             라이센스 내에 서비스 이름을 획득하여 구분하도록 함
        // ####################################################################

        // ####################################################################
        //  [선택.3-b] <운영 이슈>
        //             단말기 뷰어에서 DRM 서비스/방식을 혼용하여 사용하는 경우,
        //             라이센스 내에 서비스 이름을 획득하여 구분하도록 함
        // ####################################################################
        val serviceName_from_rights = verifyInfo?.get("ids-xrml-serviceName") as String?

        // TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함

        // TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함
        val serviceName_from_profile: String = g_serviceName

        if (serviceName_from_rights != serviceName_from_profile) {
            return
        }
        // ---------------------------------------------------------------------------------------
        //  4. 전자책 컨텐츠 암호키 획득
        // ---------------------------------------------------------------------------------------

        // ---------------------------------------------------------------------------------------
        //  4. 전자책 컨텐츠 암호키 획득
        // ---------------------------------------------------------------------------------------
        var encXmlElm: Element? = null
        var encKeyElm: Element? = null

        encXmlDoc = XMLUtil.decodeDocument(encryptionXmlBytes)
        encXmlElm = encXmlDoc?.rootElement
        encKeyElm = encXmlElm?.element("EncryptedKey")

        val b64EK = encKeyElm?.element("CipherData")?.element("CipherValue")?.text

        val resCode = idsc?.decryptCEK(
                DRMConstants.ALG_IDS_PBE,
                deviceType, userId, g_b64PasswdMac, g_b64UserPbePriv, b64EK
        )

        if (resCode != 0) {
            return
        }
    }

    private fun readByteFromFile(f: File): ByteArray? {
        try{
            var resultBynary: ByteArray? = null
            ByteArrayOutputStream().use { baos ->
                FileInputStream(f).use { fis ->
                    var readCnt: Int? = 0
                    val buf = ByteArray(1024)
                    while (fis.read(buf).also { readCnt = it } != -1) {
                        baos.write(buf, 0, readCnt!!)
                    }
                    baos.flush()
                    resultBynary = baos.toByteArray()
                }
            }
            return resultBynary
        }catch (e: java.lang.Exception){
            LibrosLog.print(e.toString())
        }
        return null
    }

    companion object{
        @SuppressLint("StaticFieldLeak")
        private lateinit var activity: Activity
        lateinit var myActivity: FragmentActivity
        lateinit var g_userId: String
        lateinit var libName: String
        private var fileName: String? = null
        private var idsc: IDSClientApi? = null
        var g_serviceName = "IDS-demo"
        var g_b64PasswdMac: String? = null
        var g_b64UserPub: String? = null
        var g_b64UserPbePriv: String? = null
        var g_b64SrvPubKid: String? = null
        var eBookFileFolderName: String? = null
        private var encXmlDoc: Document? = null
    }

    //type == download, type == drm
    @Throws(java.lang.Exception::class)
    fun getDecryptedContent(type: String?, t_entryPath: String): ByteArray? {
        // ----------------------------------------------------------------------------------------
        //  5. 전자책 컨텐츠 복호화
        // ----------------------------------------------------------------------------------------
        /* */
        // alt.1 - 특정파일 복호화 예제

        // TODO: 본 예제에서는 자체 함수로 epub 내 특정 엔트리를 꺼내서 복호화하는 데모를 구성함
        //       실제 적용 시에는 단말기/뷰어 환경에 맞게 압축된 epub 암호화 컨텐트를 복호화 처리하도록 함

        //=============changed
        //=============
        var t_entryPath = t_entryPath
        if (type != null && type == "drm" && t_entryPath.contains(fileName!!)) {
            val index = t_entryPath.lastIndexOf(fileName!!)
            t_entryPath = t_entryPath.substring(index + fileName!!.length + 1)
        }
        //=============
        //=============
        val ceAlg: Int = getAlgorithmWithEntryPath(encXmlDoc!!, t_entryPath)
        var decData: ByteArray? = null
        if (ceAlg != 0) {

            // alt.1-b : 대용량 파일 복호화 (1 MB 이상)
            // TODO: 본 예제에서는 단지 테스트/화면출력을 위해 ByteArray I/O 스트림을 사용하였으나,
            //       실제 구현에서는 File I/O 스트림 등을 사용하도록 한다.
            /* */
            val drmFile: File = File(eBookFileFolderName, "$fileName/$t_entryPath")
            val contentLength = drmFile.length().toInt()
            val fis2 = FileInputStream(drmFile)
            val os: OutputStream = ByteArrayOutputStream()

            // 아래는 내부 버퍼 크기의 default 값임 (필요 시 조정하여 사용)
            // IDSClientApi.BUFFER_SIZE = 8192;

            //			int writeLen = idsc.decryptContent(ceAlg, is, encData.length, os);
            val writeLen = idsc!!.decryptContent(ceAlg, fis2, contentLength, os)
            decData = (os as ByteArrayOutputStream).toByteArray()
            fis2.close()
        }
        return decData
    }

    // -----------------------------------------------------------------------------
    //  epub의 encryption.xml 내 해당 리소스의 ZipEntry path 를 입력하여
    //  암호화 여부 및 암호화 알고리즘을 획득하는 예제 함수임
    //   : 0 - 암호화 되어 있지 않음
    //   : 1 - AGOLITHM_SEED_CBC
    //   : 2 - AGOLITHM_AES128_CBC
    //   : 4 - AGOLITHM_AES256_CBC
    // -----------------------------------------------------------------------------
    fun getAlgorithmWithEntryPath(encXmlDoc: Document, entryPath: String): Int {
        var ceAlg = 0
        val encXmlElm = encXmlDoc.rootElement
        val encDataElmList = encXmlElm.elements("EncryptedData")
        val it: Iterator<*> = encDataElmList.iterator()
        while (it.hasNext()) {
            val encDataElm = it.next() as Element
            val cipherDataElm = encDataElm.element("CipherData") ?: continue
            val cipherRefElm = cipherDataElm.element("CipherReference") ?: continue
            val uri = cipherRefElm.attributeValue("URI")
            if (uri != null && uri == entryPath) {
                val encMthElm = encDataElm.element("EncryptionMethod") ?: continue
                val algName = encMthElm.attributeValue("Algorithm") ?: continue
                val algAlias = algName.substring(algName.lastIndexOf("#") + 1)
                ceAlg = if (algAlias == "seed-cbc") {
                    DRMConstants.ALG_SEED_CBC_PKCS5Padding
                } else if (algAlias == "aes128-cbc") {
                    DRMConstants.ALG_AES128_CBC_PKCS5Padding
                } else if (algAlias == "aes256-cbc") {
                    DRMConstants.ALG_AES256_CBC_PKCS5Padding
                } else {
                    // default
                    DRMConstants.ALG_SEED_CBC_PKCS5Padding
                }
                break
            }
        }
        return ceAlg
    }
}
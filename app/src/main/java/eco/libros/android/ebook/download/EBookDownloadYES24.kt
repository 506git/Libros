package eco.libros.android.ebook.download

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import btworks.drm.client.XMLUtil
import btworks.drm.context.DRMConstants
import btworks.drm.message.ReqMessage
import btworks.drm.message.RespMessage
import btworks.drm.util.SecurityHelper
import btworks.util.Base64
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.AndroidZipUtil4IDS
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IDSClientApiImpl

import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IOUtil4Demo
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.MetaXMLHandler
import kr.eco.common.ebook.viewer.file.drm.yes24.IDSClientDecrypt
import org.dom4j.Element
import org.xml.sax.InputSource
import java.io.*
import java.net.Socket
import java.util.*
import javax.xml.parsers.SAXParserFactory

class EBookDownloadYES24(_activity: Activity, _sdCardDirName: String, _userId: String?, _passwd: String?, _epubId: String?, _ids_host: String, _epubLibName: String) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var activity: Activity
        lateinit var sdCardDirName: String
        var g_userId: String? = null
        var g_passwd: String? = null
        var epubId: String? = null
        lateinit var ids_host: String
        lateinit var epubLibName: String
        private var ids_port = 0
        private var ebookFileFolderName: String = ""
        private var socket: Socket? = null
        private var input: InputStream? = null
        private var os: OutputStream? = null
        var service_uri = "/license/service.jsp"
        const val CRLF = "\r\n"
        private var m_sessionId: String? = null
        private var m_userId: String? = null
        private var m_sKeyInfo: ByteArray? = null

        var g_serviceVersion = "1.5"
        var g_serviceName = "IDS-demo"
        lateinit var task: CustomProgressFragment

        @SuppressLint("StaticFieldLeak")
        private var apiTest: IDSClientDecrypt? = null

        private var totalFileCount = 0f
        private var nowFileCount = 0
    }

    init {
        var ids_host_init = _ids_host
        if (ids_host_init.contains("http://")) {
            val index = ids_host_init.indexOf("http://")
            ids_host_init = ids_host_init.substring(index + "http://".length)
        }

        if (ids_host_init.contains(":")) {
            val index = ids_host_init.indexOf(":")

            var sPort = ids_host_init.substring(index + 1)
            ids_host_init = ids_host_init.substring(0, index)

            if (sPort.contains("/")) {
                val slashPoint = sPort.indexOf("/")
                sPort = sPort.substring(0, slashPoint)
            }

            if (sPort != null) {
                ids_port = sPort.toInt()
            }
        }

        activity = _activity
        sdCardDirName = _sdCardDirName
        g_userId = _userId
        g_passwd = _passwd
        epubId = _epubId
        ids_host = ids_host_init
        epubLibName = _epubLibName
        ebookFileFolderName = "${LibrosUtil.getEPUBRootPath(activity)}/${sdCardDirName}/"

        val file = File(ebookFileFolderName)

        if (!file.exists()) {
            file.mkdir()
        }
    }

    fun epubFileDownload(_task: CustomProgressFragment): String? {
        // ---------------------------------------------------------------------------------------
        //  (*) 초기변수 설정
        // ---------------------------------------------------------------------------------------
        // TODO: check this !!
        task = _task
        task.progressTask(1)
        Log.d("test","stepp2")
        val deviceType: Int = DRMConstants.SYSTYPE_PORTABLE_ANDROID
        val deviceInfo: String

        if (deviceType == DRMConstants.SYSTYPE_PORTABLE_TEST) {
            deviceInfo = DRMConstants.SYSINFO_PORTABLE_TEST
        } else if (deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID
                || deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_CP
                || deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALS2
                || deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALTAB
                || deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALTAB2) {
            deviceInfo = IDSClientApiImpl(activity)._getAndroidDeviceInfo()
        } else {
            throw IllegalArgumentException("unspported device-type : $deviceType")
        }

        val userId = g_userId
        val passwd = g_passwd

        val PROFILE_FILENAME = "${epubLibName}_${userId}.xml"

        // ---------------------------------------------------------------------------------------
        //  1. HandShake 수행 - 클라이언트와 서버간 채널보안용 세션키 교환 과정
        // ---------------------------------------------------------------------------------------

        task.progressTask(2)
        var resObjs = handshakeInit()
        task.progressTask(3)

        var resCode = resObjs[0] as String
        var resMsg = resObjs[1] as String
        var resParams = resObjs[2] as Array<String>
        if (!resCode.equals("000")) {
            return null
        }

        val b64_idsKmCert: String = resParams[0] as String
        val nonce = resParams[1] as String
        resObjs = handshakeKex(b64_idsKmCert, nonce)

        task.progressTask(4)

        resCode = resObjs[0] as String

        if (resCode != "000") {
            return null
        }

        task.progressTask(5)

        // ---------------------------------------------------------------------------------------
        //  2. 사용자 로그인 수행 - 사용자 키/서버공개키식별자 수신
        // ---------------------------------------------------------------------------------------


        // ---------------------------------------------------------------------------------------
        //  2. 사용자 로그인 수행 - 사용자 키/서버공개키식별자 수신
        // ---------------------------------------------------------------------------------------
        resObjs = _userLogin_v1_5(userId, passwd, deviceType, deviceInfo)

        task.progressTask(7)

        resCode = resObjs[0] as String
        resParams = resObjs[2] as Array<String>

        if (resCode != "000" && resCode != "001") {
            return null
        }

        val b64PasswdMac: String = resParams[0] as String
        val b64KekPub: String = resParams[1] as String
        val b64KekPbePriv: String = resParams[2] as String
        val b64SrvPubKid: String = resParams[3] as String

        task.progressTask(10)


        // ---------------------------------------------------------------------------------------
        //  3. 프로파일 생성/수정
        // ---------------------------------------------------------------------------------------
        val profileStr: String = _makeProfile(
                g_serviceVersion, g_serviceName,
                userId, b64PasswdMac,
                b64SrvPubKid,
                b64KekPub, b64KekPbePriv
        )

        val profile = profileStr.toByteArray(charset("UTF-8"))
        // modified by powerway, 20100909 -- to FIX bug
        // byte[] encProfileData = IDSClientApi.encryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, profile);

        // modified by powerway, 20100909 -- to FIX bug
        // byte[] encProfileData = IDSClientApi.encryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, profile);
        val encProfileData = IDSClientApiImpl(activity).encryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, profile)

        FileManager2().writeXmlFile(activity, encProfileData, PROFILE_FILENAME)

        task.progressTask(15)

        // ---------------------------------------------------------------------------------------
        //  4. 전자책 라이센스 목록 조회
        // ---------------------------------------------------------------------------------------


        // ---------------------------------------------------------------------------------------
        //  4. 전자책 라이센스 목록 조회
        // ---------------------------------------------------------------------------------------
        val condStr = "STATUS in (2, 3, 4)"

        resObjs = selectLicenseInfo(condStr)

        resCode = resObjs[0] as String
        resMsg = resObjs[1] as String
        val licenseInfoElms = resObjs[2] as Array<Element>

        if (resCode != "000") {
            return null
        }

        if (licenseInfoElms.size > 0) {
            val sbuf = StringBuffer()
            for (i in licenseInfoElms.indices) {
                sbuf.append(" -> licenseInfo[$i] ${String(XMLUtil.encodeXmlObject(licenseInfoElms[i]), Charsets.UTF_8)}\n\n")
            }
        } else {
            return null
        }

        task.progressTask(20)

        // ----------------------------------------------------------------------------------------
        //  5. 전자책 다운로드
        // ----------------------------------------------------------------------------------------

        // 설치(재설치)할 전자책의 라이센스ID 선택

        var bookNum = -1
        for (i in licenseInfoElms.indices) {
            if (epubId == licenseInfoElms[i].element("eBookId").text) {
                bookNum = i
                break
            }
        }

        if (bookNum == -1) {
            return null
        }

        val t_licenseId = licenseInfoElms[bookNum].element("licenseId").text
        resObjs = getDownloadUri(t_licenseId)
        resCode = resObjs[0] as String
        resMsg = resObjs[1] as String
        resParams = resObjs[2] as Array<String>
        if (resCode != "000") {
            return null
        }

        task.progressTask(25)

        val fileName = UUID.randomUUID().toString() + ".epub"
        val downloadUri = resParams[1] as String

        // TODO: downloadUri로 부터 전자책 다운로드를 수행함  <커스터마이징>
        _downloadEBook(downloadUri, fileName)

        // TODO: 라이센스 다운로드
        resObjs = getDownloadInfo(t_licenseId)

        resCode = resObjs[0] as String
        resMsg = resObjs[1] as String
        resParams = resObjs[2] as Array<String>

        if (resCode != "000") {
            return null
        }

        task.progressTask(85)

        val b64_encKeyData: String = resParams[0] as String
        val b64_rightsData: String = resParams[1] as String

        val encKeyData = Base64.decode(b64_encKeyData)
        val rightsData = Base64.decode(b64_rightsData)

        val folderName: String = fileName
        val extractName = fileName + "__tmp"

        val extractDir = File(ebookFileFolderName, "$folderName/$extractName")

        if (!extractDir.exists()) {
            val res: Boolean = extractDir.mkdir()
            if (!res) throw RuntimeException("mkdir failed : " + extractDir.path)
        }

        AndroidZipUtil4IDS.decompressFile(File(ebookFileFolderName, "$folderName/$fileName"), extractDir)
        val encXmlData: ByteArray? = FileManager().readFile(extractDir.path + "/META-INF/encryption.xml")
        val encXmlDoc = XMLUtil.decodeDocument(encXmlData)

        val encKeyElm = XMLUtil.decodeDocument(encKeyData).rootElement
        encXmlDoc.rootElement.add(encKeyElm)
        val encXmlData2 = XMLUtil.encodeXmlObject(encXmlDoc)
        FileManager().writeFile(encXmlData2, extractDir.path + "/META-INF/encryption.xml")
        FileManager().writeFile(rightsData, extractDir.path + "/META-INF/rights.xml")
        val cfg = HashMap<Any?, Any?>()
        cfg["/mimetype"] = "METHOD.STORED"
        val oriFile = File(ebookFileFolderName, "$folderName/$fileName")
        task.progressTask(88)

        AndroidZipUtil4IDS.compressFile(cfg, extractDir, FileOutputStream(oriFile))

        FileManager().deleteFiles(extractDir)

        task.progressTask(90)

        // 설치(재설치) 완료 시 서버에 confirm 메세지 전송

        // 설치(재설치) 완료 시 서버에 confirm 메세지 전송
        resObjs = setLicenseAsActivated(t_licenseId)
        resCode = resObjs[0] as String
        resMsg = resObjs[1] as String

        if (resCode != "000") {
            return null
        }

        task.progressTask(10)

        // ----------------------------------------------------------------------------------------
        //  *. 끝
        // ----------------------------------------------------------------------------------------

        return fileName
    }

    private fun setLicenseAsActivated(linceseId: String?): Array<Any?> {
        val OP_NAME = "set-license-as-activated"
        val PARAM_LIST = ArrayList<Any?>()
        PARAM_LIST.add(arrayOf("ids.license.id", linceseId!!))

        val reqMsg = ReqMessage(m_userId, OP_NAME, PARAM_LIST)
        reqMsg.applyEncrypt(m_sKeyInfo)
        _sendMessage(reqMsg)

        val respMsg = _receiveMessage()
        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        return arrayOf(resultCode, resultMsg, null)
    }

    private fun _downloadEBook(downloadUri: String, _fileName: String): Int? {
        var fileName = _fileName
        require(downloadUri.startsWith("http://")) { "invalid protocol: $downloadUri" }

        val tmp = downloadUri.substring("http://".length)
        val delimIdx = tmp.indexOf("/")
        var hostName = tmp.substring(0, delimIdx)
        var port = 80
        val requestURI = tmp.substring(delimIdx)

        val delimIdx2 = hostName.indexOf(":")
        if (delimIdx2 > 0) {
            port = hostName.substring(delimIdx2 + 1).toInt()
            hostName = hostName.substring(0, delimIdx2)
        }

        val reqHeader = "GET $requestURI HTTP/1.1${CRLF}Host: $hostName" + (if (port != 80) ":$port" else "") + CRLF +
                "Connection: Close$CRLF$CRLF"
        try {
            val socket = Socket(hostName, port)
            BufferedOutputStream(socket.getOutputStream()).use { os ->
                BufferedInputStream(socket.getInputStream()).use { ins ->
                    os.write(reqHeader.toByteArray())
                    os.flush()
                    val headerInfo = IOUtil4Demo.readHeader(ins)
                    val contentLengthObj = headerInfo.get("content.length") as Int?
                            ?: throw java.lang.IllegalArgumentException("content-length required")

                    val contentLength = contentLengthObj.toInt()

                    val folderName = fileName
                    val folderFile = File(ebookFileFolderName + folderName)
                    var result = false

                    if (!folderFile.exists()) {
                        result = folderFile.mkdir()
                    }

                    if (result) {
                        //final int BUFFER_SIZE = 23 * 1024;
                        val BUFFER_SIZE = ins.available()
                        fileName = "$ebookFileFolderName$folderName/$fileName"
                        val downFile = File(fileName)
                        BufferedInputStream(ins, BUFFER_SIZE).use { bis ->
                            FileOutputStream(downFile).use { fos ->
                                val baf = ByteArray(BUFFER_SIZE)
                                var actual = 0
                                var i = 0.0
                                while (actual != -1) {
                                    fos.write(baf, 0, actual)
                                    actual = bis.read(baf, 0, BUFFER_SIZE)
                                    i += actual.toDouble()
                                    if (actual != -1) {
                                        task.progressTask((i * 55 / contentLength).toInt() + 25)
                                    }
                                }
                            }
                        }
                    }
                    return contentLengthObj
                }
            }
        } catch (e: Exception) {
            LibrosLog.print(e.toString())
        }

        return null
    }

    fun getDownloadInfo(licenseId: String): Array<Any?> {
        val OP_NAME = "get-download-info"
        val PARAM_LIST = ArrayList<Any?>()
        PARAM_LIST.add(arrayOf("ids.license.id", licenseId))

        val reqMsg = ReqMessage(m_userId, OP_NAME, PARAM_LIST)

        reqMsg.applyEncrypt(m_sKeyInfo)
        _sendMessage(reqMsg)

        val respMsg = _receiveMessage()

        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        if (resultCode != "000") {
            return arrayOf(resultCode, resultMsg, null)
        }

        respMsg.applyDecrypt(m_sKeyInfo)

        val i = 0
        val encKeyData = respMsg.getParamValue("ids.ebook.enckey-data", i.toString() + "")
        val rightData = respMsg.getParamValue("ids.ebook.rights-data", i.toString() + "")
        val eBookId = respMsg.getParamValue("ids.ebook.id", i.toString() + "")
        return arrayOf(
                resultCode,
                resultMsg,
                arrayOf(encKeyData, rightData, eBookId, licenseId))
    }

    private fun getDownloadUri(licenseId: String?): Array<Any?> {
        // String OP_NAME = "get-download-uri";
        val OP_NAME = "get-download-uri-v1_5" // For ECO

        val PARAM_LIST = ArrayList<Any?>()
        PARAM_LIST.add(arrayOf("ids.license.id", licenseId!!))

        val reqMsg = ReqMessage(m_userId, OP_NAME, PARAM_LIST)
        reqMsg.applyEncrypt(m_sKeyInfo)
        _sendMessage(reqMsg)

        val respMsg = _receiveMessage()
        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        if (resultCode != "000") {
            return arrayOf(resultCode, resultMsg, null)
        }

        respMsg.applyDecrypt(m_sKeyInfo)

        val i = 0
        val fileName = respMsg.getParamValue("ids.ebook.filename", i.toString() + "")
        val downloadUri = respMsg.getParamValue("ids.ebook.download-uri", i.toString() + "")
        val eBookId = respMsg.getParamValue("ids.ebook.id", i.toString() + "")
        return arrayOf<Any?>(
                resultCode,
                resultMsg, arrayOf(fileName, downloadUri, eBookId, licenseId))
    }

    fun selectLicenseInfo(cond: String?): Array<Any?> {
        val OP_NAME = "select-license-info"
        val PARAM_LIST = ArrayList<Any?>()

        // alternative
        if (cond == null) {
            PARAM_LIST.add(arrayOf("ids.license-cond.status", "ALL"))
        } else {
            PARAM_LIST.add(arrayOf("ids.license-cond.status", cond))
        }

        val reqMsg = ReqMessage(m_userId, OP_NAME, PARAM_LIST)
        reqMsg.applyEncrypt(m_sKeyInfo)
        _sendMessage(reqMsg)

        val respMsg = _receiveMessage()
        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        if (resultCode != "000") {
            return arrayOf(resultCode, resultMsg, null)
        }

        respMsg.applyDecrypt(m_sKeyInfo)

        val tray = ArrayList<Any?>()
        var i = 0
        while (true) {
            var licenseInfo_i: String? = respMsg.getParamValue("ids.license.info", i.toString() + "")
                    ?: break

            licenseInfo_i = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n$licenseInfo_i"
            val licenseInfoElm : Element = XMLUtil.decodeDocument(licenseInfo_i.toByteArray(charset("UTF-8"))).rootElement
            tray.add(licenseInfoElm)
            i++
        }

        val licenseInfoElms : Array<Element> = tray.toArray(arrayOfNulls<Element>(tray.size)) as Array<Element>

        return arrayOf(
                resultCode,
                resultMsg,
                licenseInfoElms
        )
    }

    fun _makeProfile(serviceVersion: String, serviceName: String, userId: String?, b64PasswdMac: String, b64SrvPubKid: String, b64KekPub: String, b64KekPbePriv: String): String {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "\r\n" +
                "<ViewerProfile version=\"${serviceVersion}\">\r\n" +
                "  <ServiceName>${serviceName}</ServiceName>\r\n" +
                "  <UserID>${userId}</UserID>\r\n" +
                "  <PasswdHash>${b64PasswdMac}</PasswdHash>\r\n" +
                "  <SignPubKid>${b64SrvPubKid}</SignPubKid>\r\n" +
                "  <KekPub>${b64KekPub}</KekPub>\r\n" +
                "  <KekPriv>${b64KekPbePriv}</KekPriv>\r\n" +
                "</ViewerProfile>\r\n";
    }

    private fun _userLogin_v1_5(userId: String?, passwd: String?, deviceType: Int, deviceInfo: String?): Array<Any?> {
        val OP_NAME = "user-login-v1_5"

        // String b64PasswdMac = SecurityHelper.makeHMACPasswd(userId, passwd);
        val PARAM_LIST = ArrayList<Any?>()
        PARAM_LIST.add(arrayOf("ids.user.id", userId!!))
        PARAM_LIST.add(arrayOf("ids.user.passwd-ext", passwd!!))
        PARAM_LIST.add(arrayOf("ids.user.sys-type", deviceType.toString() + ""))
        PARAM_LIST.add(arrayOf("ids.user.sys-info", deviceInfo!!))

        val reqMsg = ReqMessage(userId, OP_NAME, PARAM_LIST)
        reqMsg.applyEncrypt(m_sKeyInfo)

        _sendMessage(reqMsg)
        val respMsg = _receiveMessage()

        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        if (!resultCode.equals("000") && !resultCode.equals("001")) {
            return arrayOf(resultCode, resultMsg, null)
        }

        m_userId = userId

        respMsg.applyDecrypt(m_sKeyInfo)
        val b64PasswdMac = SecurityHelper.makeHMACPasswd(g_userId, g_passwd)

        val b64KekPub = respMsg.getParamValue("ids.user.kek-pub")
        val b64KekPbePriv = respMsg.getParamValue("ids.user.kek-pbe-priv")
        val b64SrvPubKid = respMsg.getParamValue("ids.server.sign-pub.kid")

        // XPC: 1.2.2 - drm refactoring
        val uniqueId = respMsg.getParamValue("ids.user.unique-id ")

        return arrayOf<Any?>(
                resultCode,
                resultMsg, arrayOf(b64PasswdMac, b64KekPub, b64KekPbePriv, b64SrvPubKid, uniqueId))
    }

    private fun handshakeKex(b64_idsKmCert: String?, nonce: String?): Array<Any?> {
        val OP_NAME = "handshake-kex"
        val PARAM_LIST = ArrayList<Any?>()

        PARAM_LIST.add(arrayOf("ids.secure.nonce", nonce!!))

        val reqMsg = ReqMessage(null, OP_NAME, PARAM_LIST)
        m_sKeyInfo = reqMsg.applyEnvelop(b64_idsKmCert)

        _sendMessage(reqMsg)
        val respMsg = _receiveMessage()

        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage

        if (!resultCode.equals("000")) {
            return arrayOf(resultCode, resultMsg, null)
        }

        respMsg.applyDecrypt(m_sKeyInfo)
        val hnd_finished = respMsg.getParamValue("ids.hnd.finished")

        return arrayOf(resultCode, resultMsg, arrayOf<String>(hnd_finished))

    }


    private fun handshakeInit(): Array<Any?> {
        val OP_NAME = "handshake-init"
        val PARAM_LIST: List<*>? = null

        val reqMsg = ReqMessage(null, OP_NAME, PARAM_LIST)

        _sendMessage(reqMsg)
        val respMsg = _receiveMessage()
        val resultCode = respMsg.resultCode
        val resultMsg = respMsg.resultMessage
        if (!resultCode.equals("000")) {
            return arrayOf(resultCode, resultMsg, null)
        }

        val b64_idsKmCert = respMsg.getParamValue("ids.km.cert")
        val nonce = respMsg.getParamValue("ids.secure.nonce")

        return arrayOf(resultCode, resultMsg, arrayOf<String>(b64_idsKmCert, nonce))
    }

    private fun _receiveMessage(): RespMessage {
        val headerInfo = IOUtil4Demo.readHeader(input)

        val contentLengthObj = headerInfo.get("content.length") as Int
        if (headerInfo.containsKey("session.id")) {
            m_sessionId = headerInfo.get("session.id") as String
        }
        val buf = ByteArray(contentLengthObj)

        var i = 0
        while (i < buf.size) {
            val read: Int? = input?.read(buf, i, buf.size - i)
            if (read!! < 0) throw EOFException("illegal eof reached")
            i += read
        }
        input?.close()
        socket?.close()

        return RespMessage(buf)

    }

    private fun _sendMessage(reqMsg: ReqMessage) {
        socket = Socket(ids_host, ids_port)
        os = BufferedOutputStream(socket?.getOutputStream())
        input = BufferedInputStream(socket?.getInputStream())
        var port = if (ids_port != 80) {
            ":${ids_port}"
        } else {
            ""
        }
        val session = if (m_sessionId != null) {
            "Cookie: $m_sessionId$CRLF"
        } else ""
        val reqBody = reqMsg.encode()
        val reqHeader = "post $service_uri HTTP/1.1${CRLF}Host: $ids_host$port${CRLF}Conncetion: Close$CRLF" +
                "Content-Type: text/xml$CRLF" +
                "Content-Length: ${reqBody.size}$CRLF" +
                "$session$CRLF"
        (os as BufferedOutputStream).write(reqHeader.toByteArray())
        (os as BufferedOutputStream).write(reqBody)
        (os as BufferedOutputStream).flush()

    }

    fun setImageWithoutDrm(fileName: String): Boolean {
        //===================================
        //image 미리 복호화
        //===================================

        var isSuccessDownLoad = false

        val folderName = fileName

        var extractFileName = fileName

        if (fileName.contains(".epub")) {
            val index = fileName.indexOf(".epub")
            extractFileName = fileName.substring(0, index)
        }

        val extractFileDir = File(ebookFileFolderName, "$folderName/$extractFileName")

        if (!extractFileDir.exists()) {
            val res = extractFileDir.mkdir()
            if(!res) throw RuntimeException("mkdir failed : ${extractFileDir.path}")
        }

        val nomediaFile = File(extractFileDir.absolutePath, ".nomedia")
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile()
        }

        val oriFile = File(ebookFileFolderName, "$folderName/$fileName")

        AndroidZipUtil4IDS.decompressFile(oriFile, extractFileDir)

        apiTest = IDSClientDecrypt(activity, sdCardDirName, fileName, g_userId.toString(), epubLibName)

        val parserFactory = SAXParserFactory.newInstance()
        val saxParser = parserFactory.newSAXParser()
        val reader = saxParser.xmlReader

        val rootFile = File(extractFileDir.absolutePath + "/META-INF", "container.xml")

        if(rootFile.exists()){
            val metaXmlHandler = MetaXMLHandler()
            reader.contentHandler = metaXmlHandler
            reader.parse(InputSource(InputStreamReader(FileInputStream(rootFile))))
            val opfPath = metaXmlHandler.opfPath

            if(opfPath != null){
                totalFileCount = 0F
                getFileCount(extractFileDir)

                releaseImage(extractFileDir, extractFileName)
                isSuccessDownLoad = true
            }
        }

        return isSuccessDownLoad
    }

    private fun releaseImage(file: File, fileName: String) {
        if(file.isDirectory){
            val files = file.listFiles()
            if (files != null){
                for ( i in files.indices){
                    releaseImage(files[i], fileName)
                }
            }
        } else{
            var extension = getExtension(file.name)

            val fileAbsolutePath = file.absolutePath

            if (fileAbsolutePath != null && fileName != null && fileAbsolutePath.contains(fileName)){
                val index = fileAbsolutePath.lastIndexOf(fileName) + fileName.length
                var imageNamePath = fileAbsolutePath.substring(index)

                if (imageNamePath != null && imageNamePath.isNotEmpty() && imageNamePath.substring(0, 1) == "/") {
                    imageNamePath = imageNamePath.substring(1)
                }

                val decryptedImage = apiTest!!.getDecryptedContent(null, imageNamePath)

                if (decryptedImage != null && decryptedImage.isNotEmpty()) {
                    FileManager().writeFile(decryptedImage, file.absolutePath)
                }
            }

            nowFileCount++

//            Log.d("TESTCOUNT", nowFileCount.toString())
        }
    }


    private fun getExtension(fileStr: String): String? {
        return fileStr.substring(fileStr.lastIndexOf(".") + 1)
    }
    private fun getFileCount(f: File) {
        for (file: File in f.listFiles()){
            if(file.isFile){
                totalFileCount = totalFileCount +1
            } else{
                getFileCount(file)
            }
        }
    }


}
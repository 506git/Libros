package kr.co.smartandwise.eco_epub3_module.Drm;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;
import android.util.Log;

import btworks.drm.client.XMLUtil;
import btworks.drm.context.DRMConstants;
import btworks.drm.message.ReqMessage;
import btworks.drm.message.RespMessage;
import btworks.drm.util.SecurityHelper;
import btworks.util.Base64;
import kr.co.smartandwise.eco_epub3_module.R;
import kr.co.smartandwise.eco_epub3_module.Drm.markany.FileManager;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.AndroidZipUtil4IDS;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IDSClientApiImpl;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IDSClientDecrypt;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IOUtil4Demo;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.ImageXMLHandler;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.MetaXMLHandler;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;

public class EBookDownloadYES24 {

	private String ebookFileFolderName = null;

	public static final String CRLF = "\r\n";

	public static String ids_host = null;  
	public static int ids_port;
	
	public static String service_uri = "/license/service.jsp"; 

	public String g_userId = null;
	public String g_passwd = null;

	private String epubId = null;

	public static String g_serviceVersion = "1.5";
	public static String g_serviceName = "IDS-demo";

	private Context context = null;
	private Socket socket = null;
	private InputStream is = null;
	private OutputStream os = null;

	private String m_sessionId = null;
	private String m_userId = null;
	private byte[] m_sKeyInfo = null;

	private EBookDownloadAsyncTask task = null;
	
	public boolean isCacheFile = false;
	
	public EBookDownloadYES24(Context context, String userId, String passwd, String epubId) {
		String yes24Url = context.getString(R.string.yes24_url);
		int seperator		= yes24Url.indexOf(":");
		ids_host			= yes24Url.substring(0, seperator);
		ids_port			= Integer.valueOf(yes24Url.substring(seperator + 1));
		
		this.context 		= context;
		this.g_userId 		= userId;
		this.g_passwd 		= passwd;
		this.epubId 		= epubId;
		ebookFileFolderName = EBookFileUtil.getEBookStoragePath(context) + "/";

		File file = new File(ebookFileFolderName);

		if(file.exists() == false){
			file.mkdir();
		}
	}

	public String epubFileDownload(EBookDownloadAsyncTask task) throws Exception {
		// ---------------------------------------------------------------------------------------
		//  (*) 초기변수 설정
		// ---------------------------------------------------------------------------------------
		// TODO: check this !!
		
		this.task = task;

		task.onProgressUpdate(1);
		
		int deviceType = DRMConstants.SYSTYPE_PORTABLE_ANDROID;
		String deviceInfo = null;

		if(deviceType == DRMConstants.SYSTYPE_PORTABLE_TEST) {
			deviceInfo = DRMConstants.SYSINFO_PORTABLE_TEST;

		} else if(deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID ||
				deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_CP ||
				deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALS2 ||
				deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALTAB ||
				deviceType == DRMConstants.SYSTYPE_PORTABLE_ANDROID_GALTAB2
		) {
			deviceInfo = new IDSClientApiImpl(context)._getAndroidDeviceInfo();

		} else {
			throw new IllegalArgumentException("unsuppoted device-type: " + deviceType);
		}

		String userId = g_userId;
		String passwd = g_passwd;

		String PROFILE_FILENAME = userId + ".xml";

		// ---------------------------------------------------------------------------------------
		//  1. HandShake 수행 - 클라이언트와 서버간 채널보안용 세션키 교환 과정
		// ---------------------------------------------------------------------------------------

		task.onProgressUpdate(2);
		
		Object[] resObjs = handshakeInit();

		task.onProgressUpdate(3);
		
		String resCode = (String)resObjs[0];
		String resMsg = (String)resObjs[1];
		String[] resParams = (String[])resObjs[2];

		if(!resCode.equals("000")) {
			return null;
		}

		String b64_idsKmCert = resParams[0];
		String nonce = resParams[1];

		resObjs = handshakeKex(b64_idsKmCert, nonce);

		task.onProgressUpdate(4);
		
		resCode = (String)resObjs[0];
		resMsg = (String)resObjs[1];
		resParams = (String[])resObjs[2];

		if(!resCode.equals("000")) {
			return null;
		}

		task.onProgressUpdate(5);
		
		// ---------------------------------------------------------------------------------------
		//  2. 사용자 로그인 수행 - 사용자 키/서버공개키식별자 수신
		// ---------------------------------------------------------------------------------------

		resObjs = _userLogin_v1_5(userId, passwd, deviceType, deviceInfo);

		task.onProgressUpdate(7);
		
		resCode = (String)resObjs[0];
		resMsg = (String)resObjs[1];
		resParams = (String[])resObjs[2];

		if(!resCode.equals("000") && !resCode.equals("001")) {
			return null;
		}

		String b64PasswdMac = resParams[0];
		String b64KekPub = resParams[1];
		String b64KekPbePriv = resParams[2];
		String b64SrvPubKid = resParams[3];

		task.onProgressUpdate(10);
		
		// ---------------------------------------------------------------------------------------
		//  3. 프로파일 생성/수정
		// ---------------------------------------------------------------------------------------
		String profileStr = _makeProfile(
				g_serviceVersion, g_serviceName,
				userId, b64PasswdMac, 
				b64SrvPubKid, 
				b64KekPub, b64KekPbePriv
		);

		byte[] profile = profileStr.getBytes("UTF-8"); 

		// modified by powerway, 20100909 -- to FIX bug
		// byte[] encProfileData = IDSClientApi.encryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, profile);
		byte[] encProfileData = new IDSClientApiImpl(context).encryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, profile);

		FileManager.writeXmlFile(context, encProfileData, PROFILE_FILENAME);
		
		task.onProgressUpdate(15);
		
		// ---------------------------------------------------------------------------------------
		//  4. 전자책 라이센스 목록 조회
		// ---------------------------------------------------------------------------------------
		String condStr = "STATUS in (2, 3, 4)"; 

		resObjs = selectLicenseInfo(condStr);

		resCode = (String)resObjs[0];
		resMsg = (String)resObjs[1];
		Element[] licenseInfoElms = (Element[])resObjs[2];

		if(!resCode.equals("000")) {
			return null;
		}
		//task.onProgressUpdate(licenseInfoElms.length);
		if(licenseInfoElms.length > 0) {
			StringBuffer sbuf = new StringBuffer();
			for(int i = 0; i < licenseInfoElms.length; i++) {
				sbuf.append(" -> licenseInfo[" + i + "] " +
						new String(XMLUtil.encodeXmlObject(licenseInfoElms[i]), "UTF-8") + "\n\n");
			}
		} else {
			return null;
		}

		task.onProgressUpdate(20);
		
		// ----------------------------------------------------------------------------------------
		//  5. 전자책 다운로드
		// ----------------------------------------------------------------------------------------

		// 설치(재설치)할 전자책의 라이센스ID 선택

		int bookNum = -1;

		for(int i = 0; i < licenseInfoElms.length; i++){

			if(epubId.equals(licenseInfoElms[i].element("eBookId").getText())){
				bookNum = i;
				break;
			}
		}
		//task.onProgressUpdate(bookNum);

		if(bookNum == -1){
			return null;
		}

		String t_licenseId = licenseInfoElms[bookNum].element("licenseId").getText();

		resObjs = getDownloadUri(t_licenseId);

		resCode = (String)resObjs[0];
		resMsg = (String)resObjs[1];
		resParams = (String[])resObjs[2];

		if(!resCode.equals("000")) {
			return null;
		}

		task.onProgressUpdate(25);
		
//		String fileName = resParams[0];
		String fileName = epubId + ".epub";
		String downloadUri = resParams[1];

		// TODO: downloadUri로 부터 전자책 다운로드를 수행함  <커스터마이징>
		String filePath = EBookFileUtil.getEBookStoragePath(context) + "/" + fileName;
		File dirtyCacheDir = new File(filePath + "/" + epubId);
		FileManager.deleteFolder(dirtyCacheDir);
		File dirtyCacheExtractDir = new File(filePath + "/" + epubId + ".epub__tmp");
		FileManager.deleteFolder(dirtyCacheExtractDir);
		File cacheFile = new File(filePath, fileName);
		// if cache does not exist
		if (!cacheFile.exists()) {
			_downloadEBook(downloadUri, fileName);

			// TODO: 라이센스 다운로드
			resObjs = getDownloadInfo(t_licenseId);
	
			resCode = (String)resObjs[0];
			resMsg = (String)resObjs[1];
			resParams = (String[])resObjs[2];
	
			if(!resCode.equals("000")) {
				return null;
			}
	
			task.onProgressUpdate(85);
			
			String b64_encKeyData = resParams[0];
			String b64_rightsData = resParams[1];
	
			byte[] encKeyData = Base64.decode(b64_encKeyData);
			byte[] rightsData = Base64.decode(b64_rightsData);
	
			String folderName = fileName;
			String extractName = fileName + "__tmp";
			
			File extractDir = new File(ebookFileFolderName, folderName + "/" + extractName);
	
			if(!extractDir.exists()) {
				boolean res = extractDir.mkdir();
				if(!res) throw new RuntimeException("mkdir failed : " + extractDir.getPath());
			}
	
			AndroidZipUtil4IDS.decompressFile(new File(ebookFileFolderName, folderName + "/" + fileName), extractDir);
	
			byte[] encXmlData = FileManager.readFile(extractDir.getPath() + "/META-INF/encryption.xml");
			Document encXmlDoc = XMLUtil.decodeDocument(encXmlData);
	
			Element encKeyElm = XMLUtil.decodeDocument(encKeyData).getRootElement();
			encXmlDoc.getRootElement().add(encKeyElm);
			byte[] encXmlData2 = XMLUtil.encodeXmlObject(encXmlDoc);
	
			FileManager.writeFile(encXmlData2, extractDir.getPath() + "/META-INF/encryption.xml");
			FileManager.writeFile(rightsData, extractDir.getPath() + "/META-INF/rights.xml");
	
			Map cfg = new HashMap();
			cfg.put("/mimetype", "METHOD.STORED");
	
			File oriFile = new File(ebookFileFolderName, folderName + "/" + fileName);
	
			AndroidZipUtil4IDS.compressFile(cfg, extractDir, new FileOutputStream(oriFile));  
	
			FileManager.deleteFiles(extractDir);
		} else {
			isCacheFile = true;
		}

		task.onProgressUpdate(90);

		// 설치(재설치) 완료 시 서버에 confirm 메세지 전송
		resObjs = setLicenseAsActivated(t_licenseId);

		resCode = (String)resObjs[0];
		resMsg = (String)resObjs[1];

		if(!resCode.equals("000")) {
			return null;
		}

		task.onProgressUpdate(100);
		
		// ----------------------------------------------------------------------------------------
		//  *. 끝
		// ----------------------------------------------------------------------------------------
		
		return fileName;
	}

	public Object[] handshakeInit() throws Exception {

		String OP_NAME = "handshake-init"; 
		List PARAM_LIST = null;

		ReqMessage reqMsg = new ReqMessage(null, OP_NAME, PARAM_LIST);

		_sendMessage(reqMsg);
		RespMessage respMsg = _receiveMessage();

		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();
		if(!resultCode.equals("000")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		String b64_idsKmCert = respMsg.getParamValue("ids.km.cert");
		String nonce = respMsg.getParamValue("ids.secure.nonce");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { b64_idsKmCert, nonce }
		};
	}

	public Object[] handshakeKex(String b64_idsKmCert, String nonce) throws Exception {

		String OP_NAME = "handshake-kex"; 
		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] {"ids.secure.nonce", nonce } );

		ReqMessage reqMsg = new ReqMessage(null, OP_NAME, PARAM_LIST);
		m_sKeyInfo = reqMsg.applyEnvelop(b64_idsKmCert);

		_sendMessage(reqMsg);
		RespMessage respMsg = _receiveMessage();

		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();
		if(!resultCode.equals("000")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		respMsg.applyDecrypt(m_sKeyInfo);
		String hnd_finished = respMsg.getParamValue("ids.hnd.finished");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { hnd_finished }
		};
	}

	public Object[] userLogin(String userId, String passwd, int deviceType, String deviceInfo) throws Exception {

		String OP_NAME = "user-login"; 

		String b64PasswdMac = SecurityHelper.makeHMACPasswd(userId, passwd);

		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] { "ids.user.id", userId });
		PARAM_LIST.add(new String[] {"ids.user.passwd-hash", b64PasswdMac });
		PARAM_LIST.add(new String[] {"ids.user.sys-type", deviceType + ""} );
		PARAM_LIST.add(new String[] {"ids.user.sys-info", deviceInfo } );

		ReqMessage reqMsg = new ReqMessage(userId, OP_NAME, PARAM_LIST);
		reqMsg.applyEncrypt(m_sKeyInfo);

		_sendMessage(reqMsg);
		RespMessage respMsg = _receiveMessage();

		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();
		if(!resultCode.equals("000") && !resultCode.equals("001")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		m_userId = userId;

		respMsg.applyDecrypt(m_sKeyInfo);
		String b64KekPub = respMsg.getParamValue("ids.user.kek-pub");
		String b64KekPbePriv = respMsg.getParamValue("ids.user.kek-pbe-priv");
		String b64SrvPubKid = respMsg.getParamValue("ids.server.sign-pub.kid");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { b64PasswdMac, b64KekPub, b64KekPbePriv, b64SrvPubKid }
		};
	}

	Object[] _userLogin_v1_5(String userId, String passwd, int deviceType, String deviceInfo) throws Exception {

		String OP_NAME = "user-login-v1_5";

		// String b64PasswdMac = SecurityHelper.makeHMACPasswd(userId, passwd);

		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] { "ids.user.id", userId });
		PARAM_LIST.add(new String[] {"ids.user.passwd-ext", passwd });
		PARAM_LIST.add(new String[] {"ids.user.sys-type", deviceType + ""} );
		PARAM_LIST.add(new String[] {"ids.user.sys-info", deviceInfo } );

		ReqMessage reqMsg = new ReqMessage(userId, OP_NAME, PARAM_LIST);
		reqMsg.applyEncrypt(this.m_sKeyInfo);


		_sendMessage(reqMsg);
		RespMessage respMsg = _receiveMessage();

		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();

		if(!resultCode.equals("000") && !resultCode.equals("001")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		m_userId = userId;

		respMsg.applyDecrypt(this.m_sKeyInfo);

		String b64PasswdMac = SecurityHelper.makeHMACPasswd(g_userId, g_passwd);

		String b64KekPub = respMsg.getParamValue("ids.user.kek-pub");
		String b64KekPbePriv = respMsg.getParamValue("ids.user.kek-pbe-priv");
		String b64SrvPubKid = respMsg.getParamValue("ids.server.sign-pub.kid");

		// XPC: 1.2.2 - drm refactoring
		String uniqueId = respMsg.getParamValue("ids.user.unique-id ");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { b64PasswdMac, b64KekPub, b64KekPbePriv, b64SrvPubKid, uniqueId }
		};
	}

	public Object[] selectLicenseInfo(String cond) throws Exception {

		String OP_NAME = "select-license-info"; 
		List PARAM_LIST = new ArrayList();

		// alternative
		if(cond == null) {
			PARAM_LIST.add(new String[] {"ids.license-cond.status", "ALL" });
		} else {
			PARAM_LIST.add(new String[] {"ids.license-cond.status", cond });
		}

		ReqMessage reqMsg = new ReqMessage(m_userId, OP_NAME, PARAM_LIST);
		reqMsg.applyEncrypt(m_sKeyInfo);
		_sendMessage(reqMsg);

		RespMessage respMsg = _receiveMessage();
		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();

		if(!resultCode.equals("000")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		respMsg.applyDecrypt(m_sKeyInfo);

		List tray = new ArrayList();
		for(int i=0; ; i++) {
			String licenseInfo_i = respMsg.getParamValue("ids.license.info", i + "");
			if(licenseInfo_i == null) break;

			licenseInfo_i = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n\r\n" + licenseInfo_i;

			Element licenseInfoElm = XMLUtil.decodeDocument(licenseInfo_i.getBytes("UTF-8")).getRootElement();

			tray.add(licenseInfoElm);
		}

		Element[] licenseInfoElms = (Element[])tray.toArray(new Element[tray.size()]);

		return new Object[] { 
				resultCode,
				resultMsg,
				licenseInfoElms
		};
	}

	public Object[] getDownloadUri(String licenseId) throws Exception {

		// String OP_NAME = "get-download-uri"; 
		String OP_NAME = "get-download-uri-v1_5";	// For ECO
		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] { "ids.license.id", licenseId });

		ReqMessage reqMsg = new ReqMessage(m_userId, OP_NAME, PARAM_LIST);
		reqMsg.applyEncrypt(m_sKeyInfo);
		_sendMessage(reqMsg);

		RespMessage respMsg = _receiveMessage();
		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();

		if(!resultCode.equals("000")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		respMsg.applyDecrypt(m_sKeyInfo);

		int i = 0;
		String fileName = respMsg.getParamValue("ids.ebook.filename", i + "");
		String downloadUri = respMsg.getParamValue("ids.ebook.download-uri", i + "");
		String eBookId = respMsg.getParamValue("ids.ebook.id", i + "");
		String licenseId_same = respMsg.getParamValue("ids.license.id", i + "");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { fileName, downloadUri, eBookId, licenseId }
		};
	}

	public Object[] getDownloadInfo(String licenseId) throws Exception {

		String OP_NAME = "get-download-info"; 
		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] { "ids.license.id", licenseId });

		ReqMessage reqMsg = new ReqMessage(m_userId, OP_NAME, PARAM_LIST);

		reqMsg.applyEncrypt(m_sKeyInfo);
		_sendMessage(reqMsg);

		RespMessage respMsg = _receiveMessage();

		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();

		if(!resultCode.equals("000")) {
			return new Object[] { resultCode, resultMsg, null };
		}

		respMsg.applyDecrypt(m_sKeyInfo);

		int i = 0;
		String encKeyData = respMsg.getParamValue("ids.ebook.enckey-data", i + "");
		String rightData = respMsg.getParamValue("ids.ebook.rights-data", i + "");
		String eBookId = respMsg.getParamValue("ids.ebook.id", i + "");
		String licenseId_same = respMsg.getParamValue("ids.license.id", i + "");

		return new Object[] { 
				resultCode,
				resultMsg,
				new String[] { encKeyData, rightData, eBookId, licenseId }
		};
	}

	public Object[] setLicenseAsActivated(String linceseId) throws Exception {

		String OP_NAME = "set-license-as-activated"; 
		List PARAM_LIST = new ArrayList();
		PARAM_LIST.add(new String[] { "ids.license.id", linceseId });

		ReqMessage reqMsg = new ReqMessage(m_userId, OP_NAME, PARAM_LIST);
		reqMsg.applyEncrypt(m_sKeyInfo);
		_sendMessage(reqMsg);

		RespMessage respMsg = _receiveMessage();
		String resultCode = respMsg.getResultCode();
		String resultMsg = respMsg.getResultMessage();

		return new Object[] { resultCode, resultMsg, null };
	}

	private void _sendMessage(ReqMessage reqMsg) throws Exception {

		socket = new Socket(ids_host, ids_port);
		//socket.setSoTimeout(60000);  // 
		os = new BufferedOutputStream(socket.getOutputStream());
		is = new BufferedInputStream(socket.getInputStream());

		byte[] reqBody = reqMsg.encode();
		String reqHeader =
			"POST " + service_uri + " HTTP/1.1" + CRLF +
			"Host: " + ids_host + (ids_port != 80 ? ":" + ids_port : "") + CRLF +
			"Connection: Close" + CRLF +
			"Content-Type: text/xml" + CRLF + 
			"Content-Length: " + reqBody.length + CRLF +
			(m_sessionId != null ? "Cookie: " + m_sessionId + CRLF : "") +
			CRLF;

		os.write(reqHeader.getBytes());
		os.write(reqBody);
		os.flush();
	}

	private RespMessage _receiveMessage() throws Exception {

		Map headerInfo = IOUtil4Demo.readHeader(is);

		String respHeader = (String)headerInfo.get("resp.header");
		Integer contentLengthObj = (Integer)headerInfo.get("content.length");
		if(headerInfo.containsKey("session.id")) {
			m_sessionId = (String)headerInfo.get("session.id");
		}

		int contentLength = contentLengthObj.intValue();

		byte[] buf = new byte[contentLength];
		for(int i = 0; i < buf.length; ) {
			int read = is.read(buf, i, buf.length - i);
			if(read < 0) throw new EOFException("illegal eof reached");
			i += read;
		}

		is.close();
		socket.close();
		
		RespMessage respMsg = new RespMessage(buf);

		return respMsg;
	}

	private int _downloadEBook(String downloadUri, String fileName) throws IOException {

		if(!downloadUri.startsWith("http://")) {
			throw new IllegalArgumentException("invalid protocol: " + downloadUri);
		}

		String tmp = downloadUri.substring("http://".length());
		int delimIdx = tmp.indexOf("/");
		String hostName = tmp.substring(0, delimIdx);
		int port = 80;
		String requestURI = tmp.substring(delimIdx);

		int delimIdx2 = hostName.indexOf(":");
		if(delimIdx2 > 0) {
			port = Integer.parseInt(hostName.substring(delimIdx2 + 1));
			hostName = hostName.substring(0, delimIdx2);
		}

		Socket socket = new Socket(hostName, port);
		OutputStream os = new BufferedOutputStream(socket.getOutputStream());
		InputStream is = new BufferedInputStream(socket.getInputStream());

		String reqHeader =
			"GET " + requestURI + " HTTP/1.1" + CRLF +
			"Host: " + hostName + (port != 80 ? ":" + port : "") + CRLF +
			"Connection: Close" + CRLF +
			CRLF;

		os.write(reqHeader.getBytes());
		os.flush();

		Map headerInfo = IOUtil4Demo.readHeader(is);

		Integer contentLengthObj = (Integer)headerInfo.get("content.length");
		if(contentLengthObj == null) {
			throw new IllegalArgumentException("content-length required");
		}

		int contentLength = contentLengthObj.intValue();
		byte[] buf = new byte[contentLength];
		boolean assumeFileExists = false;

		File existingFile = new File(ebookFileFolderName + fileName + "/"+ fileName);
		if (existingFile.exists()) {
			long existingFileLength = existingFile.length();
			assumeFileExists = Math.abs(existingFileLength - contentLength) < 15000;
		}
		
		if (!assumeFileExists) { 
		
			for(int i = 0; i < buf.length; ) {
				int read = is.read(buf, i, buf.length - i);
				if(read < 0) throw new EOFException("illegal eof reached");
				i += read;
				task.onProgressUpdate((int)(i*55/contentLength)+25);
			}
		}
		task.onProgressUpdate(80);
		
		is.close();
		socket.close();
		
		String folderName = fileName;
		File folderFile = new File(ebookFileFolderName + folderName);
		
		boolean result = false;
		
		if(folderFile.exists() == false){
			result = folderFile.mkdir();
		}
		
		if(result == true && !assumeFileExists){
			fileName = ebookFileFolderName + folderName + "/" + fileName;
			FileManager.writeEbookFile(context, buf, fileName);
		}

		return buf.length;
	}

	String _makeProfile(
			String serviceVersion, String serviceName,
			String userId, String b64PasswdMac,
			String b64SrvPubKid, String b64KekPub, String b64KekPbePriv)
	{
		String profile = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
			"\r\n" +
			"<ViewerProfile version=\"" + serviceVersion + "\">\r\n" +
			"  <ServiceName>" + serviceName + "</ServiceName>\r\n" +
			"  <UserID>" + userId + "</UserID>\r\n" +
			"  <PasswdHash>" + b64PasswdMac + "</PasswdHash>\r\n" +
			"  <SignPubKid>" + b64SrvPubKid + "</SignPubKid>\r\n" +
			"  <KekPub>" + b64KekPub + "</KekPub>\r\n" +
			"  <KekPriv>" + b64KekPbePriv + "</KekPriv>\r\n" +
			"</ViewerProfile>\r\n";

		return profile;
	}
	
	public boolean setImageWithoutDrm(EBookImageDrmAsyncTask task, String fileName) throws Exception{
		//===================================
		//image 미리 복호화
		//===================================

		boolean isSuccessDownLoad = false;

		task.onProgressUpdate(0);
		
		String folderName = fileName;
		
		int index = fileName.indexOf(".epub");
		String extractFileName = fileName.substring(0, index);

		File extractFileDir = new File(ebookFileFolderName, folderName + "/" + extractFileName);

		if(!extractFileDir.exists()) {
			boolean res = extractFileDir.mkdir();
			if(!res) throw new RuntimeException("mkdir failed : " + extractFileDir.getPath());
		}
		
		File nomediaFile = new File(extractFileDir.getAbsolutePath(), ".nomedia");
		if(nomediaFile.exists() == false){
			nomediaFile.createNewFile();
		}
		
		AndroidZipUtil4IDS.decompressFile(new File(ebookFileFolderName, folderName + "/" + fileName), extractFileDir);
		
		task.onProgressUpdate(10);
		
		IDSClientDecrypt apiTest = new IDSClientDecrypt(context, fileName, g_userId, g_passwd);
		
		task.onProgressUpdate(15);

		SAXParserFactory 	parserFactory	= SAXParserFactory.newInstance();
		SAXParser 			saxParser		= parserFactory.newSAXParser();
		XMLReader 			reader			= saxParser.getXMLReader();

		File rootFile = new File(extractFileDir.getAbsolutePath() + "/META-INF", "container.xml");

		if (isCacheFile) {
			isSuccessDownLoad = true;
		} else {
			if (rootFile.exists() == true) {

				MetaXMLHandler metaXmlHandler = new MetaXMLHandler();
				reader.setContentHandler(metaXmlHandler);
				reader.parse(new InputSource(new InputStreamReader(new FileInputStream(rootFile))));
				String opfPath = metaXmlHandler.getOpfPath();

				if (opfPath != null) {

					int folderIndex = opfPath.indexOf("/");
					String mainFolderName = opfPath.substring(0, folderIndex);

					File opfFile = new File(extractFileDir.getAbsolutePath(), opfPath);

					if (opfFile.exists() == true) {

						ImageXMLHandler imageXmlHandler = new ImageXMLHandler();
						reader.setContentHandler(imageXmlHandler);
						reader.parse(new InputSource(new InputStreamReader(new FileInputStream(opfFile))));
						ArrayList<String> imageFilesName = imageXmlHandler.getImagesPath();

						double x = 75.0 / imageFilesName.size();
						double totalCount = 20;

						task.onProgressUpdate(20);

						for (int j = 0; j < imageFilesName.size(); j++) {
							try {
								byte[] decryptedImage = apiTest.getDecryptedContent("drm",
										mainFolderName + "/" + imageFilesName.get(j));
								if (decryptedImage != null)
									FileManager.writeFile(decryptedImage, extractFileDir.getAbsolutePath() + "/"
											+ mainFolderName + "/" + imageFilesName.get(j));
							} catch (Exception e) {
								Log.w("Decrypt", imageFilesName.get(j) + ":: " + e.getMessage());
							}

							totalCount = totalCount + x;
							task.onProgressUpdate((int) totalCount);
						}
						isSuccessDownLoad = true;
					}
				}
			}

			Map cfg = new HashMap();
			cfg.put("/mimetype", "METHOD.STORED");

			File oriImageFile = new File(ebookFileFolderName, folderName + "/" + fileName);
			AndroidZipUtil4IDS.compressFile(cfg, extractFileDir, new FileOutputStream(oriImageFile));
		}
		
		task.onProgressUpdate(100);
		
		return isSuccessDownLoad;
	}
}

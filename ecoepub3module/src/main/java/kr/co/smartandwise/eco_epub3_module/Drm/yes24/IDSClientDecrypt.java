/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */

package kr.co.smartandwise.eco_epub3_module.Drm.yes24;

import android.content.Context;

import org.dom4j.Document;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import btworks.drm.client.IDSClientApi;
import btworks.drm.client.XMLUtil;
import btworks.drm.context.DRMConstants;
import btworks.drm.util.TimeUtil;
import kr.co.smartandwise.eco_epub3_module.Drm.markany.FileManager;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;

public class IDSClientDecrypt {

	public String ebookFileFolderName = null;

	private String libName = null;
	private String g_userId = null;
	private String g_passwd = null;

	static String g_b64PasswdMac = null;
	static String g_b64UserPub = null;
	static String g_b64UserPbePriv = null;
	static String g_b64SrvPubKid = null;

	// KEPH
	static String g_serviceName = "IDS-demo";
	static String g_serviceVersion = "1.5";

	private Context context = null;
	private IDSClientApi idsc = null;

	private String fileName = null;

	private Document encXmlDoc = null;

	// 뷰어가 별도 관리해야하는 부분 (암호키/라이센스)
	static byte[] g_encKeyData = null;
	static byte[] g_rightsData = null;

	public IDSClientDecrypt(Context context, String ebookFolderName, String userId, String epubLibName) {
		this.context = context;
		this.libName = epubLibName;
		this.g_userId = userId;

		if(ebookFolderName.contains(".epub")){
			int index = ebookFolderName.indexOf(".epub");
			fileName = ebookFolderName.substring(0, index);
		}else{
			fileName = ebookFolderName;
		}

		ebookFileFolderName = EBookFileUtil.getEBookStoragePath(context) + "/" + ebookFolderName + "/";

		try {
			startDownload();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void startDownload() throws Exception {
		// ---------------------------------------------------------------------------------------
		//  (*) 초기변수 설정
		// ---------------------------------------------------------------------------------------

		int deviceType = DRMConstants.SYSTYPE_PORTABLE_ANDROID;

		String userId = g_userId;

		String PROFILE_FILENAME = userId + ".xml";

		this.idsc = new IDSClientApiImpl(context);

		// ---------------------------------------------------------------------------------------
		//  1. 프로파일 복호화/로딩
		// ---------------------------------------------------------------------------------------

		byte[] encProfile = FileManager.readPrivateFile(context, PROFILE_FILENAME);

		byte[] profileBytes = idsc.decryptProfile(DRMConstants.ALG_IDS_PKDF, deviceType, userId, encProfile);

		if(profileBytes == null) {
			return;
		}

		Document profileDoc = XMLUtil.decodeDocument(profileBytes);
		Element profileElm = profileDoc.getRootElement(); // <ViewerProfile> element

		String p_serviceName = profileElm.element("ServiceName").getText();
		String p_userId = profileElm.element("UserID").getText();
		String p_passwdHash = profileElm.element("PasswdHash").getText();
		String p_signPubKid = profileElm.element("SignPubKid").getText();
		String p_kekPub = profileElm.element("KekPub").getText();
		String p_kekPriv = profileElm.element("KekPriv").getText();

		if(!p_userId.equals(userId)) {
			return;
		}

		if(!p_serviceName.equals(g_serviceName)) {
			return;
		}

		g_b64PasswdMac = p_passwdHash;
		g_b64UserPub = p_kekPub;
		g_b64UserPbePriv = p_kekPriv;
		g_b64SrvPubKid = p_signPubKid;

		// ---------------------------------------------------------------------------------------
		//  2. 전자책 DRM 정보 추출
		// ---------------------------------------------------------------------------------------

		byte[] rightsXmlBytes = null;
		byte[] encryptionXmlBytes = null;

		File rightsFile 	=  new File(ebookFileFolderName, fileName + "/META-INF/rights.xml");
		File encryptionFile	=  new File(ebookFileFolderName, fileName + "/META-INF/encryption.xml");

		rightsXmlBytes 		= readByteFromFile(rightsFile);
		encryptionXmlBytes 	= readByteFromFile(encryptionFile);

		if(rightsXmlBytes == null || rightsXmlBytes.length == 0) {
		}
		if(encryptionXmlBytes == null || encryptionXmlBytes.length == 0) {
		}

		// ---------------------------------------------------------------------------------------
		//  3. 전자책 라이센스 검증
		// ---------------------------------------------------------------------------------------
		Map verifyInfo = idsc.parseAndVerifyXRML(deviceType, rightsXmlBytes, g_b64SrvPubKid);

		// ####################################################################
		//  [필독.1] 라이센스 검증 실패 시, 절대로 전자책 복호화-보기를 실행하지 않아야 함!
		// ####################################################################
		String failCode = (String)verifyInfo.get("fail.code");
		if(failCode != null) {
			return;
		}

		// ####################################################################
		//  [필독.2-a] 라이센스 파싱/검증 결과로부터, 유효기간을 획득하여 현재시간과 비교한다.
		//           유효기간이 아닌 경우, 절대로 전자책 복호화/보기를 실행하지 않아야 함!
		// ####################################################################
		String notBefore  = (String)verifyInfo.get("ids-xrml-notBefore");
		String notAfter = (String)verifyInfo.get("ids-xrml-notAfter");

		String curTime = null;

		// modified by powerway, 20101202
		// if(!g_serviceName.equalsIgnoreCase("IDS-KT")) {
		if(!g_serviceName.equalsIgnoreCase("IDS-KT") && !g_serviceName.equalsIgnoreCase("IDS-IN3")) {
			curTime = getTimeStamp_0900();			//  "2010-05-04T16:29:20+0900";
		} else {
			curTime = TimeUtil.getTimeStamp_utc();	//  "2010-05-04T07:29:20Z";
		}

		if(curTime.compareTo(notBefore) < 0) {
			return;
		}

		if(!notAfter.equals("UNLIMITED") && curTime.compareTo(notAfter) > 0) {
			return;
		}

		// ####################################################################
		//  [필독.2-b] 라이센스 내 eBookId가 현재의 전자책 eBookId와 같은지 비교하는 로직이 필요함
		//           eBookId가 다른 경우, 절대로 전자책 복호화-보기를 허용하지 않아야 함!
		// ####################################################################

		String eBookId_from_rights = (String)verifyInfo.get("ids-xrml-eBookId");

		String eBookId_from_ebook = eBookId_from_rights + "";

		if(!eBookId_from_rights.equals(eBookId_from_ebook)) {
			return;
		}

		// ####################################################################
		//  [선택.3-a] <운영 이슈>
		//             라이센스에서 사용자 ID를 획득하여,
		//             현 프로파일 또는 로그인 사용자가 아닌경우 전자책 보기를 허용하지 않음.
		// ####################################################################
		String keyName = (String)verifyInfo.get("ids-xrml-keyname");
		String userId_from_rights = keyName.substring(0, keyName.indexOf(":"));

		// TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함
		String userId_from_profile = g_userId;

		if(!userId_from_rights.equalsIgnoreCase(userId_from_profile)) {
			return;
		}

		// ####################################################################
		//  [선택.3-b] <운영 이슈>
		//             단말기 뷰어에서 DRM 서비스/방식을 혼용하여 사용하는 경우,
		//             라이센스 내에 서비스 이름을 획득하여 구분하도록 함
		// ####################################################################
		String serviceName_from_rights = (String)verifyInfo.get("ids-xrml-serviceName");

		// TODO: 본 예제에서는 프로파일에서 로딩한 글로벌 변수와 비교함
		String serviceName_from_profile = g_serviceName;

		if(!serviceName_from_rights.equals(serviceName_from_profile)) {
			return;
		}

		// ---------------------------------------------------------------------------------------
		//  4. 전자책 컨텐츠 암호키 획득
		// ---------------------------------------------------------------------------------------
		Element encXmlElm = null;
		Element encKeyElm = null;

		encXmlDoc = XMLUtil.decodeDocument(encryptionXmlBytes);
		encXmlElm = encXmlDoc.getRootElement();
		encKeyElm = encXmlElm.element("EncryptedKey");

		String b64EK = encKeyElm.element("CipherData").element("CipherValue").getText();

		int resCode = idsc.decryptCEK(
				DRMConstants.ALG_IDS_PBE,
				deviceType, userId, g_b64PasswdMac, g_b64UserPbePriv, b64EK
				);

		if(resCode != 0) {
			return;
		}
	}

	//type == download, type == drm
	public byte[] getDecryptedContent(String type, String t_entryPath) throws Exception {
		// ----------------------------------------------------------------------------------------
		// 5. 전자책 컨텐츠 복호화
		// ----------------------------------------------------------------------------------------
		/* */
		// alt.1 - 특정파일 복호화 예제

		// TODO: 본 예제에서는 자체 함수로 epub 내 특정 엔트리를 꺼내서 복호화하는 데모를 구성함
		// 실제 적용 시에는 단말기/뷰어 환경에 맞게 압축된 epub 암호화 컨텐트를 복호화 처리하도록 함

		// =============changed
		// =============

		if (type != null && type.equals("drm") && t_entryPath.contains(fileName)) {

			int index = t_entryPath.lastIndexOf(fileName);

			t_entryPath = t_entryPath.substring(index + fileName.length() + 1);
		}
		// =============
		// =============

		int ceAlg = getAlgorithmWithEntryPath(encXmlDoc, t_entryPath);

		byte[] decData;

		// non-DRM file
		if (ceAlg == 0) {
			File nonDrmFile = new File(ebookFileFolderName, fileName + "/" + t_entryPath);
			FileInputStream fis = new FileInputStream(nonDrmFile);
			decData = new byte[(int)nonDrmFile.length()];
			fis.read(decData);
			fis.close();
		} else {

			File drmFile = new File(ebookFileFolderName, fileName + "/" + t_entryPath);

			int contentLength = (int) drmFile.length();
			FileInputStream fis2 = new FileInputStream(drmFile);
			OutputStream os = new ByteArrayOutputStream();

			idsc.decryptContent(ceAlg, fis2, contentLength, os);

			decData = ((ByteArrayOutputStream) os).toByteArray();

			fis2.close();
		}

		return decData;
	}


	// -----------------------------------------------------------------------------
	//  epub의 encryption.xml 내 해당 리소스의 ZipEntry path 를 입력하여
	//  암호화 여부 및 암호화 알고리즘을 획득하는 예제 함수임
	//   : 0 - 암호화 되어 있지 않음
	//   : 1 - AGOLITHM_SEED_CBC
	//   : 2 - AGOLITHM_AES128_CBC
	//   : 4 - AGOLITHM_AES256_CBC
	// -----------------------------------------------------------------------------
	public int getAlgorithmWithEntryPath(Document encXmlDoc, String entryPath) {
		int ceAlg = 0;

		Element encXmlElm = encXmlDoc.getRootElement();
		List encDataElmList = encXmlElm.elements("EncryptedData");

		for(Iterator it = encDataElmList.iterator(); it.hasNext(); ) {
			Element encDataElm = (Element)it.next();

			Element cipherDataElm = encDataElm.element("CipherData");
			if(cipherDataElm == null) continue;

			Element cipherRefElm = cipherDataElm.element("CipherReference");
			if(cipherRefElm == null) continue;

			String uri = cipherRefElm.attributeValue("URI");
			if(uri != null && uri.equals(entryPath)) {
				Element encMthElm = encDataElm.element("EncryptionMethod");
				if(encMthElm == null) continue;

				String algName = encMthElm.attributeValue("Algorithm");
				if(algName == null) continue;

				String algAlias = algName.substring(algName.lastIndexOf("#") + 1);
				if(algAlias.equals("seed-cbc")) {
					ceAlg = DRMConstants.ALG_SEED_CBC_PKCS5Padding;
				} else if(algAlias.equals("aes128-cbc")) {
					ceAlg = DRMConstants.ALG_AES128_CBC_PKCS5Padding;
				} else if(algAlias.equals("aes256-cbc")) {
					ceAlg = DRMConstants.ALG_AES256_CBC_PKCS5Padding;
				} else {
					// default
					ceAlg = DRMConstants.ALG_SEED_CBC_PKCS5Padding;
				}

				break;
			}
		}

		return ceAlg;
	}

	// -----------------------------------------------------------------------------
	//  ANDROID 에서 TimeZone 출력이 정상적으로 안되어서 다음 함수를 이용하여 별도 처리함
	// -----------------------------------------------------------------------------
	public String getTimeStamp_0900() {
		return TimeUtil.getTimeStamp_0900().substring(0, 19) + "+0900";
	}

	private byte[] readByteFromFile(File f){

		byte[] resultBynary = null;
		ByteArrayOutputStream baos = null;
		FileInputStream fis =null;
		try {
			baos = new ByteArrayOutputStream();
			fis = new FileInputStream(f);
			int readCnt=0;

			byte[] buf = new byte[1024];
			while((readCnt = fis.read(buf)) != -1){
				baos.write(buf, 0, readCnt);
			}
			baos.flush();
			resultBynary = baos.toByteArray();

		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}finally{
			try {
				baos.close();
				fis.close();
			} catch (IOException e) {
			}
		}

		return resultBynary ;
	}
}

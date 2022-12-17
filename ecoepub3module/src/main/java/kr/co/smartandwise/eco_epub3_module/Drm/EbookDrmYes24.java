package kr.co.smartandwise.eco_epub3_module.Drm;


import android.content.Context;

import kr.co.smartandwise.eco_epub3_module.Drm.yes24.IDSClientDecrypt;


public class EbookDrmYes24 extends EbookFile {

	private Context 	context 			= null;
	private String 		epubFileName 		= null;
	private String 		userId 				= null;
	private String 		passwd 				= null;
	
	private IDSClientDecrypt idsDecrypt 	= null;
	
	public EbookDrmYes24(Context context, String epubFileName, String libSeq, String userId, String passwd) {
		super(context, epubFileName, libSeq);
		// TODO Auto-generated constructor stub
		this.context 		= context;
		this.epubFileName 	= epubFileName;
		this.userId 		= userId;
		this.passwd 		= passwd;
	}

	@Override
	public void initEbookData() {
		// TODO Auto-generated method stub

		idsDecrypt = new IDSClientDecrypt(context, epubFileName, userId, passwd);
		
	}

	@Override
	public byte[] getEbookHtml(String deviceId, String href) {
		// TODO Auto-generated method stub
		
		byte[] returnData = null;
		
		try {
			returnData = idsDecrypt.getDecryptedContent("drm", super.oebpsFolderName + "/" + href);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnData;
	}
}

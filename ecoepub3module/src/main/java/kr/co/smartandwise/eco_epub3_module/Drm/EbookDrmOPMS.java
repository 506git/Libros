package kr.co.smartandwise.eco_epub3_module.Drm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Context;
import kr.co.smartandwise.eco_epub3_module.Drm.markany.DrmOPMSRelease;

public class EbookDrmOPMS extends EbookFile{

	public EbookDrmOPMS(Context context, String fileName, String libSeq) {
		super(context, fileName, libSeq);
	}

	
	@Override
	public void initEbookData() {
		// TODO Auto-generated method stub
	}

	@Override
	public byte[] getEbookHtml(String deviceId, String href) {
		// TODO Auto-generated method stub
		
		File inFile = new File (this.webViewPath, href);

		byte[] bytes = null;

		if(inFile.exists() == true){

			FileInputStream fis = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int readCount = 0;

			try {
				fis = new FileInputStream(inFile);

				while (( readCount = fis.read(buffer)) != -1) {
					bos.write(buffer, 0, readCount);
				}

				bytes = bos.toByteArray();

			} catch (Exception e) {
				//not handle
			} finally{
				if(bos != null){
					try {
						bos.close();
					} catch (IOException e) {
						//not handle
					}	
				}
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e1) {
						//not handle
					}
				}
			}
		}
		
		byte[] returnBytes = null;

		try {
			returnBytes = DrmOPMSRelease.releaseDrm(bytes, deviceId, libSeq);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			

		return returnBytes;
	}
}

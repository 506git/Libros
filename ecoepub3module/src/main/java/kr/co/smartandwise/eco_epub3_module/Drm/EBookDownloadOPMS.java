package kr.co.smartandwise.eco_epub3_module.Drm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;

import com.markany.xsync.core.XSyncCipher;
import com.markany.xsync.core.XSyncContent;

import kr.co.smartandwise.eco_epub3_module.Drm.markany.FileManager;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;

public class EBookDownloadOPMS {

	public String down(Context context, String url, EBookDownloadAsyncTask task, MarkanyBook markanyBook){
		if(url.contains("#UDID")){
			url = url.replace("#UDID", Util.getDeviceId(context));
		}

		String filePath = EBookFileUtil.getEBookStoragePath(context);
		String fileName = "OPMS_" + url.substring(url.lastIndexOf("CID=") + 4, url.indexOf("&DEVCODE=")) + ".epub";

		File dir = new File(filePath);

		if(dir.exists() == false){
			dir.mkdir();
		}

		File file = new File(filePath, fileName);
//		if(file.exists()) {
//			file.delete();
//		}

		if (url != null && url.length() > 0 && !file.exists())	{
			InputStream inputStream = null;
			FileOutputStream fileOutputStream = null; 
			byte[] buf = new byte[1024];

			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.connect();

				int lenghtOfFile = connection.getContentLength();
				if(lenghtOfFile < 1000 && lenghtOfFile != -1){
					return null;
				}
				
				if(lenghtOfFile == -1){
					lenghtOfFile = 10 * 1024 * 1024;
				}
				
				inputStream = connection.getInputStream();

				boolean fileResult = file.createNewFile();

				if(fileResult == true) {

					fileOutputStream = new FileOutputStream(file);

					int cnt = 0;
					long readData = 0;
					long total = 0;
					
					while ((cnt = inputStream.read(buf)) != -1)	{

						total += cnt;
						// publishing the progress....
						task.onProgressUpdate((int)(total*80/lenghtOfFile));
						
						fileOutputStream.write(buf, 0, cnt);
						fileOutputStream.flush();
						readData += cnt;
					}
				}
			} catch (Exception e){
				//not handle
			}finally{
				if (fileOutputStream != null){
					try {
						fileOutputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (inputStream != null){
					try {
						inputStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		String returnFileName = null;

		try {
			if (markanyBook.getFileType().equalsIgnoreCase("epub")) {
				returnFileName = FileManager.unzipOpmsMarkAny(context, file, fileName, task);
			} else if (markanyBook.getFileType().equalsIgnoreCase("pdf")) {
                java.lang.System.loadLibrary("xsync-keygen");
                XSyncContent testContent = new XSyncContent(file, Util.getDeviceId(context), XSyncCipher.RUNNING_JAVA);
                InputStream is = testContent.getInputStream(context);
                File outputFile = new File(filePath, fileName.replace(".epub", ".pdf"));
                FileOutputStream fos = new FileOutputStream(outputFile);
                byte[] buf = new byte[1024];
                int readBytes;
                int totalBytes = 0;
                while( -1 != (readBytes = is.read(buf)) )
                {
                    fos.write(buf, 0, readBytes);
                    totalBytes += readBytes;
                }
                fos.close();

				returnFileName = outputFile.getName();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnFileName;
	}
}

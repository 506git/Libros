/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */

package kr.co.smartandwise.eco_epub3_module.Drm.markany;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import com.markany.xsync.XSyncException;
import com.markany.xsync.core.XSyncCipher;
import com.markany.xsync.core.XSyncContent;
import com.markany.xsync.core.XSyncZipFile;

import android.content.Context;
import kr.co.smartandwise.eco_epub3_module.Drm.EBookDownloadAsyncTask;
import kr.co.smartandwise.eco_epub3_module.Drm.Util;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;

public class FileManager {

	static
	   {
	      try {
	      //load key gen, load first needed
	      java.lang.System.loadLibrary("xsync-keygen");
	      }
	      catch(Exception e) {
	    	  e.printStackTrace();
	      }
	   }
	

	private static boolean deleteZipFile(File targetZipFile){

		boolean isDelete = false;

		if(targetZipFile.exists() == true){

			isDelete = targetZipFile.delete();
		}

		return isDelete;
	}

	public static byte[] readFile(String path) throws IOException {
		File file = new File(path);
		FileInputStream fis = new FileInputStream(file);
		byte[] buf = new byte[(int)file.length()];

		for(int idx = 0; idx < buf.length; ) {
			int read = fis.read(buf, idx, buf.length - idx);
			if(read < 0) {
				fis.close();
				throw new EOFException("illegal eof reached in readFile() : " + path);
			}

			idx += read;
		}
		fis.close();

		return buf;
	}

	public static byte[] readFile(File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);
		byte[] buf = new byte[(int)file.length()];

		for(int idx = 0; idx < buf.length; ) {
			int read = fis.read(buf, idx, buf.length - idx);
			if(read < 0) {
				fis.close();
				throw new EOFException("illegal eof reached in readFile() : ");
			}

			idx += read;
		}
		fis.close();

		return buf;
	}
	
	public static void writeFile(byte[] data, String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(data);
		fos.close();
	}

	public static void writePdfFile(Context context, byte[] data, String path) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		fos.write(data);
		fos.close();
	}
	
	public static void writeXmlFile(Context context, byte[] data, String name) throws IOException {
		FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
		fos.write(data);
		fos.close();
	}
	
	public static void writeEbookFile(Context context, byte[] data, String name) throws IOException {
		File file = new File(name);
		
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.close();
	}
	
	public static byte[] readPrivateFile(Context context, String name) throws IOException {
		FileInputStream fis = context.openFileInput(name);
		byte[] buf = new byte[fis.available()];
		
		for(int idx = 0; idx < buf.length; ) {
			int read = fis.read(buf, idx, buf.length - idx);
			if(read < 0) throw new EOFException("illegal eof reached in readPrivateFile() : " + name);
			
			idx += read;
		}
		fis.close();
		
		return buf;
	}

	public static void deleteFiles(File tbdFile) throws IOException {
		if(tbdFile.isDirectory()) {
			File[] files = tbdFile.listFiles();
			for(int i = 0 ; i < files.length ; i++) {
				deleteFiles(files[i]);
			}
		} 

		tbdFile.delete();
	}
	
	public static boolean deleteFolder(File targetFolder) {
		
		if(targetFolder != null && targetFolder.exists() == true){
			if(targetFolder.isDirectory() == true){
				File[] childFile = targetFolder.listFiles();
				
				if(childFile != null){
					int size = childFile.length;
					if (size > 0) {
						for (int i = 0; i < size; i++) {
							if (childFile[i].isFile()) {
								childFile[i].delete();
							} else {
								deleteFolder(childFile[i]);
							}
						}
					}
				}
				boolean result = targetFolder.delete();
				
				return result;
			}else{
				
				boolean result = targetFolder.delete();
				return result;
			}
			
		}else{
			return true;
		}
	}

	public static String unzipOpmsMarkAny(Context context, File zipFile, String fileName, EBookDownloadAsyncTask task){
		
		int retval = -1;
		File parents = null;
		
		String uid = UUID.randomUUID().toString();
		
		String outputDirPath = EBookFileUtil.getEBookStoragePath(context) + "/" + uid + "/" + uid;
		String inputDirPath = EBookFileUtil.getEBookStoragePath(context) + "/" + fileName;

		task.onProgressUpdate(85);
		
		// File UnZip
		
		try {

			parents = new File(EBookFileUtil.getEBookStoragePath(context) + "/" + uid);
			if(parents.exists()){
				parents.delete();
			}
			
			parents.mkdir();
			
			File dir = new File(outputDirPath);
			if(dir.exists()){
				dir.delete();
			}
				
			dir.mkdirs();

			File nomediaFile = new File(outputDirPath, ".nomedia");
			if(nomediaFile.exists() == false){
				nomediaFile.createNewFile();
			}
			
			task.onProgressUpdate(88);
			
			Thread.sleep(500);
			String deviceId = Util.getDeviceId(context);
			retval = zipExtractTest(context, inputDirPath, outputDirPath, deviceId);

		} catch (Exception e){
			//not handle
			e.printStackTrace();
		}finally{
			
			if(retval != 0){
			
				deleteFolder(parents);
//				deleteZipFile(zipFile);
				
				return null;
			}
		}

		task.onProgressUpdate(95);
		
		boolean isDelete = false;

		// Delete File Name
		try {
			// leave cache
//			isDelete = deleteZipFile(zipFile);
			isDelete = true;
		} catch (Exception e) {
			//not handle
			e.printStackTrace();
		}

		if(isDelete == true){
			return uid;
		}else{
			return null;
		}
	}
	
	private static int zipExtractTest(Context context, String srcContentPath, String decOutPath, String deviceKey)
	{
		return zipExtract(context, srcContentPath, decOutPath, deviceKey, XSyncCipher.RUNNING_JAVA);
	}

	private static int zipExtract(Context context, String srcContentPath, String decOutPath, String deviceKey, int cipherMode)
	{
		int retval = 0;

		try
		{
			if(XSyncCipher.RUNNING_NATIVE == cipherMode) // no native mode supported
				return -1;

			// test options 
			boolean fullDecompression = true;

			File drmFile = new File(srcContentPath); 

			if(drmFile.exists() == false){
				return -1;
			}
			
			XSyncContent testContent = new XSyncContent(drmFile, deviceKey, cipherMode);
			XSyncZipFile zipTestContent = new XSyncZipFile(testContent, context);

			com.markany.xsync.core.ZipEntry crrEntry = null;
			String crrEntryFileName = null;

			long entriesCount = 0;   //from map

			InputStream crrEntryStream = null;
			FileOutputStream fos = null;
			byte[] buf = new byte[1024];

			//check start time : getting whole entries

			HashMap<String, com.markany.xsync.core.ZipEntry> xsyncContentEntryMap = zipTestContent.getEntries();

			//making entries finished
			entriesCount = xsyncContentEntryMap.size();

			int readBytes = 0;
			int totalBytes = 0;

			//full decompression test using entryMap
			if(fullDecompression)
			{
				Set<String> xsyncEntryKeySet = xsyncContentEntryMap.keySet();

				for(String crrKey:  xsyncEntryKeySet)
				{
					totalBytes =0;

					//get entry
					crrEntry = xsyncContentEntryMap.get(crrKey); 

					//get decoded stream
					
					try {
						crrEntryStream = zipTestContent.getInputStream(crrEntry, context);						
					} catch (Exception e) {
						continue;
					}
					
					crrEntryFileName = crrEntry.getName();

					File parentDir = null;
					File decOutFile = new File(decOutPath + "/" + crrEntryFileName);

					for( parentDir = decOutFile.getParentFile(); 
							null != parentDir; 
							parentDir = parentDir.getParentFile() )
					{
						if( false == parentDir.exists())
						{
							parentDir.mkdirs();
						}
						else {
							break;
						}
					}

					// if target entry is directory
					if(crrEntry.isDirectory())
					{
						decOutFile.mkdir();
					}
					else
					{

						decOutFile.createNewFile();
						fos = new FileOutputStream(decOutFile);

						while( -1 != (readBytes = crrEntryStream.read(buf)) )
						{

							fos.write(buf, 0, readBytes);

							totalBytes += readBytes;

						}
					}
				}
			}
		}
		catch (XSyncException e) 
		{
			e.printStackTrace();
			retval = -1;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			retval = -1;
		}
		finally {
			if(XSyncCipher.RUNNING_NATIVE == cipherMode)
				return -1;
		}

		return retval;

	}
}


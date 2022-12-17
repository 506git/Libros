package kr.co.smartandwise.eco_epub3_module.Drm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.Context;

import kr.co.smartandwise.eco_epub3_module.Drm.markany.DrmOPMSRelease;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.MetaXMLHandler;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.OpfData;
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.OpfXMLHandler;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;

public abstract class EbookFile {

	public Context						context			= null;
	
	public HashMap<String, OpfData> 	opfDataMap 			= null;
	public ArrayList<String> 			pageOrder 			= null;
	public String 						defaultPath 		= null;
	public String						webViewPath			= null;
	public String 						libSeq				= null;

//폴더 순 : sdCardDirName - 개별EpubFileDirName - file.epub(EpubFileDirName) 
//											  - file(fileName) 				- oebpsFolderName
	
	private String						epubFileDirName		= null;
	public String						oebpsFolderName		= null;
	
	private String 						epubFileName		= null;

	public EbookFile(Context context, String epubFileName, String libSeq){
		this.context			= context;
		
		this.libSeq				= libSeq;
		this.epubFileDirName	= epubFileName;

		this.epubFileName		= epubFileName;
		
		this.defaultPath 		= EBookFileUtil.getEBookStoragePath(context) + "/" + this.epubFileDirName + "/";
		if(epubFileName.contains(".epub")){
			int index 			= epubFileName.indexOf(".epub");
			this.epubFileName	= epubFileName.substring(0, index);			
		}

		Object[] returnObj = null;
		try {
			returnObj = getInitData(this.defaultPath + this.epubFileName, context);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(returnObj != null){
			String startFolder = (String)returnObj[0];
			opfDataMap = (HashMap<String, OpfData>)returnObj[1];
			pageOrder = (ArrayList<String>)returnObj[2];
			
			if(startFolder != null && startFolder.contains("/")){
				int index = startFolder.indexOf("/");
				oebpsFolderName 	= startFolder.substring(0, index);
				this.webViewPath	= defaultPath + this.epubFileName + "/" + oebpsFolderName;
			}else{
				this.webViewPath	= defaultPath + this.epubFileName;
			}			
		}
	}

	/**
	 * 1. epub파일 풀기전 필요한 사항들을 넣음
	 */
	public abstract void initEbookData();

	/**
	 * 1. epub viewer의 내용
	 * 2. DRM이 풀린 epub파일의 개별 HTML을 반환
	 */
	public abstract byte[] getEbookHtml(String deviceId, String href);

	public String getTemplateHtml(){

		byte[] bytes = null;

		InputStream is = null;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int readCount = 0;

		try {

			is = context.getAssets().open("eco_ebook_template.html");

			while (( readCount = is.read(buffer)) != -1) {
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
			if(is != null){
				try {
					is.close();

				} catch (IOException e1) {
					//not handle
				}
			}
		}
		String templateHtml = null;
		try {
			templateHtml = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return templateHtml;
	}

	public void storeCurrentFileInfo(int fileNum, int pageNum, int fontSize, String backGroundColor){

		String filePath = defaultPath + epubFileName + "/info";

		String fileInfo = fileNum + "||" + pageNum+ "||" + fontSize+ "||" + backGroundColor;

		File dir = new File(filePath);

		if(dir.exists() == false){
			dir.mkdir();
		}

		File file = new File(filePath, "info.txt");
		if(file.exists()) {
			file.delete();
		}

		FileOutputStream fileOutputStream = null; 

		try {
			boolean result = file.createNewFile();

			if(result == true) {

				fileOutputStream = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
				writer.write(fileInfo);
				writer.flush();
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
		}
	}

	public String getStoredCurrentFileInfo(){

		String filePath = defaultPath + epubFileName + "/info";
		String returnInfo = null;

		File dir = new File(filePath);

		if(dir.exists() == false){
			return null;
		}

		File file = new File(filePath, "info.txt");

		if(file.exists() == false){
			return null;
		}

		FileInputStream fileInputStream = null;
		InputStreamReader reader = null;
		BufferedReader bufferReader = null;

		try {
			fileInputStream = new FileInputStream(file);
			reader = new InputStreamReader(fileInputStream);
			bufferReader 	= new BufferedReader(reader);
			returnInfo = bufferReader.readLine();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//not handle
		}finally{

			if(bufferReader != null){
				try {
					bufferReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fileInputStream != null){
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return returnInfo;
	}

	public void storeTotalPageInfo(int fontSize, String content){
		String filePath = defaultPath + epubFileName + "/page";

		File dir = new File(filePath);

		if(dir.exists() == false){
			dir.mkdir();
		}

		File file = new File(filePath, "font_"+fontSize+".txt");
		if(file.exists()) {
			return;
		}

		FileOutputStream fileOutputStream = null; 

		try {
			boolean result = file.createNewFile();

			if(result == true) {

				fileOutputStream = new FileOutputStream(file);
				OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
				writer.write(content);
				writer.flush();
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
		}
	}

	public String getStoredTotalPageInfo(int fontSize){

		String filePath 	= defaultPath + epubFileName + "/page";
		String returnInfo 	= null;

		File dir = new File(filePath);

		if(dir.exists() == false){
			return null;
		}

		File file = new File(filePath, "font_"+fontSize+".txt");

		if(file.exists() == false){
			return null;
		}

		FileInputStream fileInputStream = null;
		InputStreamReader reader = null;
		BufferedReader bufferReader = null;

		try {
			fileInputStream = new FileInputStream(file);
			reader = new InputStreamReader(fileInputStream);
			bufferReader 	= new BufferedReader(reader);
			returnInfo = bufferReader.readLine();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			//not handle
		}finally{

			if(bufferReader != null){
				try {
					bufferReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(reader != null){
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fileInputStream != null){
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return returnInfo;
	}

	private Object[] getInitData(String path, Context context) throws Exception{

		Object[] objs = new Object[3];

		String metaXml = null;
		
		File file = new File(path, "/META-INF/container.xml");
		
		if(file != null && file.exists() == true){
			if(libSeq != null){
				byte[] htmlBytes = DrmOPMSRelease.releaseDrm(getXmlFile(file), Util.getDeviceId(context), libSeq);
				
				try {
					metaXml = new String(htmlBytes, "utf-8");
				}catch (Exception e) {
				}
			}else{
				metaXml = getInitFile(file);
			}
			
			String flag = "</container>";
			
			if(metaXml != null && metaXml.contains(flag)){
				
				int idx = metaXml.indexOf(flag);
				
				metaXml = metaXml.substring(0, idx + flag.length());
			}
			
			SAXParserFactory 	parserFactory	= SAXParserFactory.newInstance();
			SAXParser 			saxParser		= parserFactory.newSAXParser();
			XMLReader 			reader			= saxParser.getXMLReader();
			MetaXMLHandler 		metaXmlHandler 	= new MetaXMLHandler();
			reader.setContentHandler(metaXmlHandler);
			reader.parse(new InputSource(new StringReader(metaXml)));
			
			String opfPath 	= metaXmlHandler.getOpfPath();

			String opfData 	= getInitFile(new File(path, opfPath));
			
			OpfXMLHandler opfXmlHandler = new OpfXMLHandler();
			reader.setContentHandler(opfXmlHandler);
			reader.parse(new InputSource(new StringReader(opfData)));

			objs[0] = opfPath;
			objs[1] = opfXmlHandler.getOpfData(); 
			objs[2] = opfXmlHandler.getPageOrder(); 
		}
		
		return objs;
	}

	private String getInitFile(File file){

		if(file.exists() == false){
			return null;
		}

		byte[] bytes = null;

		FileInputStream fis = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int readCount = 0;

		try {

			fis = new FileInputStream(file);
			
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

		String xmlFile = null;
		try {
			xmlFile = new String(bytes, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return xmlFile;
	}

	private byte[] getXmlFile(File file){

		if(file.exists() == false){
			return null;
		}

		byte[] bytes = null;

		FileInputStream fis = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		byte[] buffer = new byte[1024];
		int readCount = 0;

		try {

			fis = new FileInputStream(file);
			
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

		return bytes;
	}
	
	public HashMap<String, OpfData> getOpfDataMap(){
		return this.opfDataMap;
	}

	public ArrayList<String> getPageOrder(){
		return this.pageOrder;
	}
	public String getFileFolderName(){
		return this.epubFileDirName;
	}
	public String getDefaultPath(){
		return this.defaultPath;
	}
	public String getWebViewPath(){
		return this.webViewPath + "/";
	}
}
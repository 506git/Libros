/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */

package kr.co.smartandwise.eco_epub3_module.Drm.yes24;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.ContextWrapper;
import btworks.drm.util.ZipUtil4IDS;

/**
 * TODO: fill desc. of <code>AndroidZipUtil4IDS</code>
 *  
 * @version $Revision: 1.2 $ $Date: 2011-05-06 17:30:34 $
 * @author Ahn, Myung Hoon <powerway@incube.co.kr>
 */

public class AndroidZipUtil4IDS extends ZipUtil4IDS {
	
	static ContextWrapper ctxWrapper = null;
	
	public AndroidZipUtil4IDS(){
		
	}
	
	public static void decompressFile(File zipFile, File destPath)
		throws IOException
	{
		FileInputStream fis = getFileInputStream(zipFile);
		decompressStream(new BufferedInputStream(fis, R_BUFFER_SIZE), destPath);
		fis.close();
	}
	
	protected static FileInputStream getFileInputStream(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		
		return fis;
	}

	protected static FileOutputStream getFileOutputStream(File f) throws IOException
	{
		FileOutputStream fos = new FileOutputStream(f);
		return fos;
	}
}

/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */

package kr.co.smartandwise.eco_epub3_module.Drm.yes24;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: fill desc. of <code>FileUtil</code>
 *  
 * @version $Revision: 1.2 $ $Date: 2011-06-14 02:20:07 $
 * @author Ahn, Myung Hoon <powerway@incube.co.kr>
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class IOUtil4Demo {

	public static final String CRLF = "\r\n";
	public static final int CR = (int)'\r';
	public static final int LF = (int)'\n';
	public static int LINE_BUFFER_SIZE = 2048; 

	

	public static Map readHeader(InputStream is) throws IOException {

		StringBuffer sbuf = new StringBuffer();
		String sessionId = null;
		int contentLength = -1;

		String line = null;
		while((line = readLine(is)) != null) {
			sbuf.append(line + CRLF);
			if(line.length() == 0) break;

			int delimIdx = line.indexOf(":");
			if(delimIdx < 0) continue;

			String headerName = line.substring(0, delimIdx);
			String headerValue = line.substring(delimIdx+1).trim();

			if(headerName.equalsIgnoreCase("Content-Length")) {
				contentLength = Integer.parseInt(headerValue);

			} else if(headerName.equalsIgnoreCase("Set-Cookie")) {
				int s_idx = headerValue.indexOf("JSESSIONID=");
				if(s_idx < 0) continue;
				sessionId = headerValue.substring(s_idx);
			}
		}

		if(contentLength < 0) throw new IOException("invalid content-length");

		Map headerInfo = new HashMap();
		headerInfo.put("resp.header", sbuf.toString());
		headerInfo.put("content.length", new Integer(contentLength));

		if(sessionId != null) {
			headerInfo.put("session.id", sessionId);
		}

		return headerInfo;
	}

	public static String readLine(InputStream is) throws IOException {
		byte[] lineBuf = new byte[LINE_BUFFER_SIZE];
		int idx = 0;

		int read = is.read();
		if(read < 0) return null;

		while(true) {
			if(read == CR) {
				read = is.read();
				if(read < 0) break;
				if(read == LF) break;
			}
			lineBuf[idx++] = (byte)read;

			read = is.read();
			if(read < 0) break;
		}

		return new String(lineBuf, 0, idx);
	}
}

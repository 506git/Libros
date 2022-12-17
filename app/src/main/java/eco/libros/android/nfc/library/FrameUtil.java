/*==============================================================*
 *@author   [SC사업팀] 
 *@Copyright(c) 2012 by (주)이씨오. All rights reserved. 
 *==============================================================*/
package eco.libros.android.nfc.library;

import java.util.ArrayList;

public class FrameUtil
{
	/**
	 * byte 배열을 공백으로 구분된 16진수 문자열로 변
	 * @param bytes 원본 byte배
	 * @return 16진수로 변환된 문자열 
	 */
	public static String byteArrayToHexString(byte[] bytes)
	{
		if (bytes == null || bytes.length == 0) {
			return null;
		}

		//StringBuffer sb = new StringBuffer(bytes.length * 2);
		StringBuffer sb = new StringBuffer();
		for (int x = 0; x < bytes.length; x++) {
			sb.append(leftPaddingHexString(bytes[x]));
		}
		return sb.toString().trim();
	}

	/**
	 * byte를 16진수 문자열과 공백으로 변환 
	 * @param src
	 * @return
	 */
	public static String leftPaddingHexString(byte src)
	{
		String hexaNum;
		hexaNum = "0" + Integer.toHexString(0xff & src);
		return hexaNum.substring(hexaNum.length() - 2) + " ";
	}

	/**
	 * 
	 * @param rawUID
	 * @return
	 */
	public static String getFormattedUID4ByteArray(byte[] rawUID)
	{
		String temp1 = "";
		String temp2 = "";
		String formattedUID = "";
		if(rawUID != null && rawUID.length > 0) {
			for(int i = rawUID.length-1; i >= 0; i = i-2) {
				temp1 = "";
				temp2 = "";
				temp1 = FrameUtil.leftPaddingHexString(rawUID[i]);
				temp2 = FrameUtil.leftPaddingHexString(rawUID[i-1]);
				formattedUID = formattedUID + temp1 + temp2;
			}
		}
		String returnVal = "";
		for(int i = 0; i < formattedUID.length(); i++) {
			if(formattedUID.charAt(i) == ' ') {
				continue;
			}
			returnVal += formattedUID.charAt(i);
		}
		//return formattedUID.trim();
		return returnVal.toUpperCase();
	}

	public static char getHexToAscii(String hex) {
		int decimal = Integer.parseInt(hex, 16);  
		return (char)decimal;
	}

	/**
	 * 
	 * @param rawUID
	 * @return
	 */

	public static ArrayList<String> getReposeDataList(byte[] bytes){

		String responseStr = byteArrayToHexString(bytes);
		
		if(responseStr == null){
			return null;
		}
		
		ArrayList<String> returnList = new ArrayList<String>();

		String [] datas = responseStr.split(" ");

		for(int i = 0; i < datas.length; i++){
			returnList.add(datas[i]);
		}

		return returnList;
	}

	//	public static String getFormattedUID4String(String rawUID)
	//	{
	//		StringTokenizer stk;
	//		ArrayList<String> als = new ArrayList<String>();
	//		String formattedUID = "";
	//		if(rawUID != null && rawUID.length() == 23) {
	//			stk = new StringTokenizer(rawUID, " "); // 생성 
	//			while(stk.hasMoreTokens()){
	//				als.add(stk.nextToken()); // 토큰값 빼서 넣기 
	//			}
	//			String temp = "";
	//			for(int i = als.size(); i >= 0 ; i = i-2) {
	//				formattedUID = formattedUID + als.get(i) + als.get(i-1) + " ";
	//			}
	//		}
	//		return formattedUID.trim();
	//	}
}

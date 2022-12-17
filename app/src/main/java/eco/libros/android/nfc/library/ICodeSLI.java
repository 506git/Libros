/*==============================================================*
 *@author   [SC사업팀] 
 *@Copyright(c) 2012 by (주)이씨오. All rights reserved. 
 *==============================================================*/
package eco.libros.android.nfc.library;


public class ICodeSLI extends ISO15693
{
	//public static final byte FLAGS               = (byte) 0x20;
	public static final byte IC_MANUFACTURE_CODE = (byte) 0x04;  // NXP
	public static final int  BYTES_PER_BLOCK     = 4;            // 블럭 당 바이트수 
	
	public static final byte INVENTORY_READ      = (byte) 0xA0;
	public static final byte FAST_INVENTORY_READ = (byte) 0xA1;
	public static final byte SET_EAS             = (byte) 0xA2;
	public static final byte RESET_EAS           = (byte) 0xA3;
	public static final byte LOCK_EAS            = (byte) 0xA4;
	public static final byte EAS_ALARM           = (byte) 0xA5;

	/**
	 * 
	 * @param flags
	 * @param afi
	 * @param masklength
	 * @param maskvalue
	 * @param start
	 * @param count
	 * @return
	 */
	public static byte[] InventoryRead(byte flags, byte afi, byte masklength, byte[] maskvalue, byte start, byte count)
	{
		byte[]  request = new byte[5+maskvalue.length+2];
		if(maskvalue.length > 0) {
			request[0] = flags;
			request[1] = INVENTORY_READ;
			request[2] = IC_MANUFACTURE_CODE;
			request[3] = afi;
			request[4] = masklength;
			for(int i = 0; i < maskvalue.length; i++) {
				request[5+i] = maskvalue[i];
			}
			request[5+maskvalue.length] = start;
			request[5+maskvalue.length+1] = count;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param afi
	 * @param masklength
	 * @param maskvalue
	 * @param start
	 * @param count
	 * @return
	 */
	public static byte[] FastInventoryRead(byte flags, byte afi, byte masklength, byte[] maskvalue, byte start, byte count)
	{
		byte[]  request = new byte[5+maskvalue.length+2];
		if(maskvalue.length > 0) {
			request[0] = flags;
			request[1] = FAST_INVENTORY_READ;
			request[2] = IC_MANUFACTURE_CODE;
			request[3] = afi;
			request[4] = masklength;
			for(int i = 0; i < maskvalue.length; i++) {
				request[5+i] = maskvalue[i];
			}
			request[5+maskvalue.length] = start;
			request[5+maskvalue.length+1] = count;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
	public static byte[] SetEAS(byte flags, byte[] uid)
	{
		byte[]  request = new byte[3+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = SET_EAS;
			request[2] = IC_MANUFACTURE_CODE;
			for(int i = 0; i < uid.length; i++) {
				request[3+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
	public static byte[] ResetEAS(byte flags, byte[] uid)
	{
		byte[]  request = new byte[3+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = RESET_EAS;
			request[2] = IC_MANUFACTURE_CODE;
			for(int i = 0; i < uid.length; i++) {
				request[3+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
//	public static byte[] LockEAS(byte flags, byte[] uid)
//	{
//		byte[]  request = new byte[3+uid.length];
//		if(uid.length > 0) {
//			request[0] = flags;
//			request[1] = LOCK_EAS;
//			request[2] = IC_MANUFACTURE_CODE;
//			for(int i = 0; i < uid.length; i++) {
//				request[3+i] = uid[i];
//			}
//		}
//		return request;
//	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
//	public static byte[] EASAlarm(byte flags, byte[] uid)
//	{
//		byte[]  request = new byte[3+uid.length];
//		if(uid.length > 0) {
//			request[0] = flags;
//			request[1] = EAS_ALARM;
//			request[2] = IC_MANUFACTURE_CODE;
//			for(int i = 0; i < uid.length; i++) {
//				request[3+i] = uid[i];
//			}
//		}
//		return request;
//	}
	/**
	 * 
	 * @param command
	 * @param response
	 * @return
	 */
//	public static HashMap<String, String> parseResponse(byte command, byte[] response)
//	{
//		HashMap<String, String> contents = new HashMap<String, String>();
//		String temp, temp2;
//		String flags;
//		switch(command)
//		{
//		case INVENTORY_READ :
//			
//			break;
//			
//		case FAST_INVENTORY_READ :
//			
//			break;
//			
//		case SET_EAS :
//			flags = FrameUtil.leftPaddingHexString(response[0]);
//			contents.put("FLAGS", flags);
//			break;
//			
//		case RESET_EAS :
//			flags = FrameUtil.leftPaddingHexString(response[0]);
//			contents.put("FLAGS", flags);
//			break;
//			
//		case LOCK_EAS :
//			flags = FrameUtil.leftPaddingHexString(response[0]);
//			contents.put("FLAGS", flags);
//			break;
//			
//		case EAS_ALARM :
//			flags = FrameUtil.leftPaddingHexString(response[0]);
//			contents.put("FLAGS", flags);
//			String EASSequence = "";
//			for(int i = 1; i < 33; i++) {  // EAS sequence = 256 bit
//				temp = FrameUtil.leftPaddingHexString(response[i]);
//				EASSequence = EASSequence + temp;
//			}
//			contents.put("EAS_SEQUENCE", EASSequence);
//			break;
//		}
//		return contents;
//	}
}

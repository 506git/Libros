/*==============================================================*
 *@author   [SC사업팀] 
 *@Copyright(c) 2012 by (주)이씨오. All rights reserved. 
 *==============================================================*/
package eco.libros.android.nfc.library;


public class ISO15693
{
	//public static final byte FLAGS               = (byte) 0x20;
	public static final byte IC_MANUFACTURE_CODE = (byte) 0x04;  // NXP
	public static final int  BYTES_PER_BLOCK     = 4;            // 블럭 당 바이트수 
	
    public static final byte INVENTORY                           = (byte) 0x01;
    public static final byte STAY_QUIET                          = (byte) 0x02;
    public static final byte READ_SINGLE_BLOCK                   = (byte) 0x20;
    public static final byte WRITE_SINGLE_BLOCK                  = (byte) 0x21;
    public static final byte LOCK_SINGLE_BLOCK                   = (byte) 0x22;
    public static final byte READ_MULTIPLE_BLOCKS                = (byte) 0x23;
    public static final byte WRITE_MULTIPLE_BLOCKS               = (byte) 0x24;
    public static final byte SELECT                              = (byte) 0x25;
    public static final byte RESET_TO_READY                      = (byte) 0x26;
    public static final byte WRITE_AFI                           = (byte) 0x27;
    public static final byte LOCK_AFI                            = (byte) 0x28;
    public static final byte WRITE_DSFID                         = (byte) 0x29;
    public static final byte LOCK_DSFID                          = (byte) 0x2A;
    public static final byte GET_SYSTEM_INFORMATION              = (byte) 0x2B;
    public static final byte GET_MULTIPLE_BLOCKS_SECURITY_STATUS = (byte) 0x2C;
    /**
     * 
     * @param sub_carrier
     * @param data_rate
     * @param inventory
     * @param protocol_extension
     * @param select_afi
     * @param address_nbslots
     * @param option
     * @param rfu
     * @return
     */
	public static byte makeFlags( boolean sub_carrier, boolean data_rate
						 , boolean inventory  , boolean protocol_extension
						 , boolean select_afi , boolean address_nbslots
						 , boolean option     , boolean rfu )
	{
		byte flags = 0x00;
		// 0: A Single sub-carrier frequency shall be used by the VICC, 
		// 1: Two sub-carrier shall be used by the VICC
		flags = sub_carrier ? (byte)128 : 0;
		// 0: Low data rate shall be used, 1: High data rate shall be used.
		flags += data_rate ? (byte)64 : 0;
		// 0: Flags 5 to 8(select, address, option, rfu)
		// 1: Flags 5 to 8(afi, nb_slots, option, rfu)
		flags += inventory ? (byte)32 : 0;
		// 0: No protocol format extension, 1: Protocol format is extended. Reserved for future use
		flags += protocol_extension ? (byte)16 : 0;
		// Reference to ISO15693-3 page 10.
		flags += select_afi ? (byte)8 : 0;
		// Reference to ISO15693-3 page 10.
		flags += address_nbslots ? (byte)4 : 0;
		// Reference to ISO15693-3 page 10.
		flags += option ? (byte)2 : 0;
		// Reference to ISO15693-3 page 10.
		flags += rfu ? (byte)1 : 0;
		
		return flags;
	}
	/**
	 * 
	 * @param flags
	 * @param afi
	 * @param masklenth
	 * @param maskvalue
	 * @return
	 */
	public static byte[] Inventory(byte flags, byte afi, byte masklenth, byte[] maskvalue)
	{
		byte[]  request = new byte[4+maskvalue.length];
		if(maskvalue.length > 0) {
			request[0] = flags;
			request[1] = INVENTORY;
			request[2] = afi;
			request[3] = masklenth;
			for(int i = 0; i < maskvalue.length; i++) {
				request[4+i] = maskvalue[i];
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
	public static byte[] StayQuiet(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = STAY_QUIET;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param addr
	 * @return
	 */
	public static byte[] ReadSingleBlock(byte flags, byte[] uid, byte addr)
	{
		byte[]  request = new byte[2+uid.length+1];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = READ_SINGLE_BLOCK;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = addr;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param addr
	 * @param data
	 * @return
	 */
	public static byte[] WriteSingleBlock(byte flags, byte[] uid, byte addr, byte[] data)
	{
		byte[]  request = new byte[2+uid.length+1+data.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = WRITE_SINGLE_BLOCK;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = addr;
			for(int i = 0; i < data.length; i++) {
				request[2+uid.length+1+i] = data[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param addr
	 * @return
	 */
	public static byte[] LockSingleBlock(byte flags, byte[] uid, byte addr)
	{
		byte[]  request = new byte[2+uid.length+1];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = LOCK_SINGLE_BLOCK;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = addr;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param start
	 * @param count
	 * @return
	 */
	public static byte[] ReadMultipleBlocks(byte flags, byte[] uid, byte start, byte count)
	{
		byte[]  request = new byte[2+uid.length+2];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = READ_MULTIPLE_BLOCKS;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = start;
			request[2+uid.length+1] = count;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param start
	 * @param count
	 * @param data
	 * @return
	 */
	public static byte[] WriteMultipleBlocks(byte flags, byte[] uid, byte start, byte count, byte[] data)
	{
		byte[]  request;
		// uid is option
		if(uid != null && uid.length > 0) {
			request = new byte[2+uid.length+2+data.length];
			request[0] = flags;
			request[1] = WRITE_MULTIPLE_BLOCKS;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = start;
			request[2+uid.length+1] = count;
			for(int i = 0; i < data.length; i++) {
				request[2+uid.length+2+i] = data[i];
			}
		} else {
			request = new byte[4+data.length];
			request[0] = flags;
			request[1] = WRITE_MULTIPLE_BLOCKS;
			request[2] = start;
			request[3] = count;
			for(int i = 0; i < data.length; i++) {
				request[4+i] = data[i];
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
	public static byte[] Select(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = SELECT;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
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
	public static byte[] ResetToReady(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = RESET_TO_READY;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param afi
	 * @return
	 */
	public static byte[] WriteAFI(byte flags, byte[] uid, byte afi)
	{
		byte[]  request = new byte[2+uid.length+1];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = WRITE_AFI;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = afi;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
	public static byte[] LockAFI(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = LOCK_AFI;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param dsfid
	 * @return
	 */
	public static byte[] WriteDSFID(byte flags, byte[] uid, byte dsfid)
	{
		byte[]  request = new byte[2+uid.length+1];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = WRITE_DSFID;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = dsfid;
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @return
	 */
	public static byte[] LockDSFID(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = LOCK_DSFID;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
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
	public static byte[] GetSystemInformation(byte flags, byte[] uid)
	{
		byte[]  request = new byte[2+uid.length];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = GET_SYSTEM_INFORMATION;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
		}
		return request;
	}
	/**
	 * 
	 * @param flags
	 * @param uid
	 * @param start
	 * @param count
	 * @return
	 */
	public static byte[] GetMultipleBlocksSecurityStatus(byte flags, byte[] uid, byte start, byte count)
	{
		byte[]  request = new byte[2+uid.length+2];
		if(uid.length > 0) {
			request[0] = flags;
			request[1] = GET_MULTIPLE_BLOCKS_SECURITY_STATUS;
			for(int i = 0; i < uid.length; i++) {
				request[2+i] = uid[i];
			}
			request[2+uid.length] = start;
			request[2+uid.length+1] = count;
		}
		return request;
	}
}

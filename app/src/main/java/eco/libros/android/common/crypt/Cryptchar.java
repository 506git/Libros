/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */

package eco.libros.android.common.crypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Cryptchar
{
	public static String encrypt(String seed, String cleartext) 
	{
		byte[] rawKey;
		byte[] result;
		try {
			rawKey = getRawKey(seed.getBytes());
			result = encrypt(rawKey, cleartext.getBytes());
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return cleartext;
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			return cleartext;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return cleartext;
		}
		return toHex(result);
	}
	
	public static String decrypt(String seed, String encrypted)
	{
		byte[] rawKey;
		byte[] result;
		try {
			rawKey = getRawKey(seed.getBytes());
			byte[] enc = toByte(encrypted);
			result = decrypt(rawKey, enc);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return encrypted;
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			return encrypted;
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			return encrypted;
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			return encrypted;
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			return encrypted;
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			return encrypted;
		}
		
		return new String(result);
	}

	private static byte[] getRawKey(byte[] seed) throws NoSuchAlgorithmException, NoSuchProviderException
	{
		KeyGenerator kgen = KeyGenerator.getInstance("AES");
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", new CryptoProvider());
		sr.setSeed(seed);
	    kgen.init(128, sr);
	    SecretKey skey = kgen.generateKey();
	    byte[] raw = skey.getEncoded();
	    return raw;
	}
	
	private static byte[] encrypt(byte[] raw, byte[] clear) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
	    byte[] encrypted = cipher.doFinal(clear);
		return encrypted;
	}

	private static byte[] decrypt(byte[] raw, byte[] encrypted) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException
	{
	    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
		Cipher cipher = Cipher.getInstance("AES");
	    cipher.init(Cipher.DECRYPT_MODE, skeySpec);
	    byte[] decrypted = cipher.doFinal(encrypted);
		return decrypted;
	}

	public static String toHex(String txt)
	{
		return toHex(txt.getBytes());
	}
	
	public static String fromHex(String hex)
	{
		return new String(toByte(hex));
	}
	
	public static byte[] toByte(String hexString)
	{
		int len = hexString.length()/2;
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++)
			result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
		return result;
	}

	public static String toHex(byte[] buf)
	{
		if (buf == null)
			return "";
		StringBuffer result = new StringBuffer(2*buf.length);
		for (int i = 0; i < buf.length; i++) {
			appendHex(result, buf[i]);
		}
		return result.toString();
	}
	
	private final static String HEX = "0123456789ABCDEF";
	private static void appendHex(StringBuffer sb, byte b)
	{
		sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
	}
}

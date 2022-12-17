package kr.co.smartandwise.eco_epub3_module.Drm.markany;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class DrmOPMSRelease {
	
	public static byte[] releaseDrm(byte[] data, String deviceId, String libSeq) throws Exception {
		byte[] keysbyte = (deviceId+libSeq).getBytes(); 
		byte[] szKey = {0x31, 0x32, 0x33, 0x34, 0x61, 0x62, 0x43, 0x44, 0x41, 0x40, 0x7B, 0x3E, 0x05, 0x06, 0x07, 0x08 };
		byte[] szIV  = {0x22, (byte)0xda, (byte)0x80, 0x2c, (byte)0x9f, (byte)0xac, 0x40, 0x36, (byte)0xb8, 0x3d, (byte)0xaf, (byte)0xba, 0x42, (byte)0x9d, (byte)0x9e, (byte)0xb4};

		for (int n4ByteIdx = 0; n4ByteIdx < keysbyte.length; n4ByteIdx++) {
			szKey[n4ByteIdx % 16] ^= keysbyte[n4ByteIdx];
		}

		SecretKeySpec skeySpec = new SecretKeySpec(szKey, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec(szIV);
		Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);
		byte[] decrypted = cipher.doFinal(data);

		return decrypted;
	}
}

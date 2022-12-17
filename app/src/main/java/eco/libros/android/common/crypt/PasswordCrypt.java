package eco.libros.android.common.crypt;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import common.util.encryption.aria.AriaCipher;

/**
 * 
 * @author BTC111110_07
 * 
 * 보안검증시 통신 암호화
 *
 */
public class PasswordCrypt {

	private Context context = null;
	private String key = "";
	
	public PasswordCrypt(Context context)
	{
		this.context = context;
	}
	
	public void init() throws IOException
	{
		BufferedReader bf = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open("key")));
		String temp = "";
		while((temp = bf.readLine()) != null)
		{
			key += temp;
		}
		
		try {
			key = AriaCipher.decrypt(key);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			return;
		}
	}
	
	private byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };

	public String encodePw(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException,	IllegalBlockSizeException, BadPaddingException {
		
		byte[] textBytes = str.getBytes("UTF-8");
		AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
		     SecretKeySpec newKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
		     Cipher cipher = null;
		cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);

		return Base64.encodeToString(cipher.doFinal(textBytes), 0);
	}
}

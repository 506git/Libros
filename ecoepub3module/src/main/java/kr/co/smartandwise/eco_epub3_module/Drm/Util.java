package kr.co.smartandwise.eco_epub3_module.Drm;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import kr.co.smartandwise.eco_epub3_module.R;

public class Util {
	
	public static Document makeXmlDocument(String xml) {
		xml = refineXml(xml);
//		Log.i("Util", "Recv : " + xml);

		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes("UTF-8"));

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = factory.newDocumentBuilder();

			return docBuilder.parse(is);
		} catch (ParserConfigurationException e) {
//			Log.e("Util", "makeXmlDocument : " + e.getMessage());
		} catch (SAXException e) {
//			Log.e("Util", "makeXmlDocument : " + e.getMessage());
		} catch (IOException e) {
//			Log.e("Util", "makeXmlDocument : " + e.getMessage());
		}
		return null;
	}

	public static Document makeXmlDocument(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);

		char[] buf = new char[1024];
		String response = "";
		int read;

		try {
			while ((read = isr.read(buf, 0, 1024)) != -1) {
				response = response.concat(String.valueOf(buf).substring(0,
						read));
			}
		} catch (IOException e) {
//			Log.e("Util", "makeXmlString : " + e.getMessage());
		}

		return makeXmlDocument(response);
	}

	private static String refineXml(String xml) {
		// remove \t, \r, \n
		xml = xml.replaceAll("\t", "");
		xml = xml.replaceAll("\r", "");
		xml = xml.replaceAll("\n", "");

		return xml;
	}

	public static String getValueFromXmlString(String xml, String name) {
		int start = xml.indexOf("<" + name + ">") + name.length() + 2;
		int end = xml.indexOf("</" + name + ">");

		if (start != -1 && end != -1)
			return xml.substring(start, end);
		else
			return "";
	}

	public static int getIntValueFromXmlString(String xml, String name) {
		String str = getValueFromXmlString(xml, name);

		if (str.equals(""))
			return -1;
		else {
			try {
				return Integer.valueOf(str);
			} catch (Exception e) {
				return -1;
			}
		}
	}
	
	public static void startFragment(FragmentActivity activity, int res, Fragment fragment) {
		FragmentManager fm = activity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		if (!fragment.isAdded())
			ft = ft.add(res, fragment, null).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	public static void removeFragment(FragmentActivity activity, Fragment fragment) {
		FragmentManager fm = activity.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();

		if (fragment.isAdded()) {
			ft = ft.remove(fragment);
		}
		ft.commit();
	}	

	public static void enableBackButtonForActivity(final Activity activity, TextView back) {
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				activity.finish();
			}
		});
	}
	
	public static void enableBackButtonForFragment(final FragmentActivity activity, final Fragment fragment, TextView back) {
		back.setVisibility(View.VISIBLE);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentManager fm = activity.getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();

				if (fragment.isAdded())
					ft.remove(fragment).commit();
			}
		});
	}

	public static void setCoverImage(final Activity activity, final ImageView iv, final String src) {
		iv.setImageResource(R.drawable.bookcover_default);
		new Thread() {
			@Override
			public void run() {
				downloadBookCoverImage(activity, src);
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						iv.setImageDrawable(getBookCoverImage(activity, src));
					}
				});
			}
		}.start();
	}

	private static Drawable getBookCoverImage(Context context, String src) {
		downloadBookCoverImage(context, src);

		ImageView ivBookCover = new ImageView(context);
		ivBookCover.setImageURI(Uri.parse(context.getFilesDir().getAbsolutePath()
				+ imgUrlToFilename(src)));

		if (ivBookCover.getDrawable() == null)
			ivBookCover.setImageResource(R.drawable.bookcover_default);
		return ivBookCover.getDrawable();
	}

	private static void downloadBookCoverImage(Context context, String src) {
		File fileImage = new File(context.getFilesDir().getAbsolutePath() + imgUrlToFilename(src));

		if (fileImage.exists() == false) {
			URL url;
			try {
//				Log.i("Util", "downloadBookCoverImage : connection occured");
				url = new URL(src);
				URLConnection urlCon = url.openConnection();

				InputStream is = urlCon.getInputStream();
				OutputStream os = context.openFileOutput(imgUrlToFilename(src),
						Context.MODE_PRIVATE);
				byte[] buf = new byte[10240];
				int read;
				while ((read = is.read(buf)) != -1)
					os.write(buf, 0, read);

				os.flush();
				os.close();
				is.close();
			} catch (IOException e) {
//				Log.e("Util", "downloadBookCoverImage : " + e.getMessage());
			}
		}
	}

	private static String imgUrlToFilename(String url) {
		return url.substring(url.lastIndexOf("/") + 1);
	}
	
	public static String readOneLine(Context context, int raw) {
		Resources res = context.getResources();
		InputStream is = res.openRawResource(raw);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader reader = new BufferedReader(isr);
		
		try {
			return reader.readLine();
		} catch (IOException e) {
//			Log.e("Util", "on readOneLine : " + e.getMessage());
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
//				Log.e("Util", "on readOneLine : " + e.getMessage());
			}
		}
		return null;
	}
	
	public static String getDeviceId(Context context){
		
		String deviceId = null;
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            deviceId = android.provider.Settings.Secure.getString(
                    context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        } else {
            TelephonyManager telManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = "" + telManager.getDeviceId();
        }

//        deviceId = UUID.randomUUID().toString();
		if(deviceId == null || deviceId.trim().length() == 0){
			Account[] accounts = AccountManager.get(context).getAccounts();

			for (Account account : accounts) {   
				deviceId = account.name;
			}
		}
		return deviceId;
	}
	
	public static boolean deleteFileAndDirectory(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			
			for (int i = 0; i < files.length; i++) {
				boolean succ = deleteFileAndDirectory(files[i]);
				if (!succ)
					return false;
			}
		}
		return file.delete();
	}
}
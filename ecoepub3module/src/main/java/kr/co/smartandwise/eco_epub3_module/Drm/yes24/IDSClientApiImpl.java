/**
 *@author   [정보시스템 사업 2팀] 
 *@Copyright(c) 2010 by (주)이씨오. All rights reserved.
 *
 *이씨오의 비밀자료임 절대 외부 유출을 금지합니다. 
 */
package kr.co.smartandwise.eco_epub3_module.Drm.yes24;

import android.content.Context;
import android.provider.Settings;
import btworks.drm.client.IDSClientApi;
import btworks.xcrypto.CryptoContext;

public class IDSClientApiImpl extends IDSClientApi {

	Context context = null;

	// added by powerway, 20100517
	public IDSClientApiImpl(Context context) {
		super();
		this.context = context;
	}

	// added by powerway, 20100517
	// [override method]
	public String _getAndroidDeviceInfo() {
		String androidId = Settings.Secure.getString(this.context.getContentResolver(), Settings.Secure.ANDROID_ID);

		// in case of emulator
		if (androidId == null) {
			androidId = "1111-TEST-ANDROID";
		}

		return androidId;
	}

	// get by powerway, 20110506
	public static void ensurePowerUpBTWCrypto(boolean asyncMode) throws Exception {
		final String lhead = "IDSClientApiImpl";

		final CryptoContext ctx = CryptoContext.getInstance();

		if (ctx.getStatus() == CryptoContext.INITIALIZED) {
			if (!asyncMode) {
				boolean result = ctx.powerUpSelfTest();
				if (result) {
					// 암호검증 승인모드 설정
					// CryptoContext.getInstance().setApprovedMode(true);

				} else {
					throw new IllegalStateException(ctx.getMessage());
				}
			} else {
				new Thread() {
					public void run() {
						String lhead_a = lhead + "-async";

						boolean result = ctx.powerUpSelfTest();
						if (result) {
							// 암호검증 승인모드 설정
							// CryptoContext.getInstance().setApprovedMode(true);

						} else {
							// throw new
							// IllegalStateException(ctx.getMessage());
						}
					}
				}.start();
			}
		}
	}
}

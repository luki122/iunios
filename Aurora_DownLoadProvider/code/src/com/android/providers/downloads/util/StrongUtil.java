package com.android.providers.downloads.util;

import android.os.storage.IMountService;
import android.os.ServiceManager;

public class StrongUtil {

	private static IMountService mountService = IMountService.Stub
			.asInterface(ServiceManager.getService("mount"));

	public static boolean sdIsMounted(String mount) {
		try {
			if (mountService.getVolumeState(mount).equals(
					android.os.Environment.MEDIA_MOUNTED)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

}


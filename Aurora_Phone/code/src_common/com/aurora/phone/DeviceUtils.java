package com.android.phone;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.ContactsContract.Data;
import android.util.Log;
import android.view.ViewConfiguration;

import android.os.SystemProperties;
import android.os.Build;

public class DeviceUtils {
	private static final String TAG = "DeviceUtils";

	public static boolean isOnePlus() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("ONEPLUS") || prop.contains("OnePlus");
	}

	public static boolean isHMNOTE1LTETD() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("HMNOTE1LTETD");
	}

	public static boolean isMI4() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("MI4");
	}

	public static boolean isUseAuroraPhoneService() {
		return isFind7() || isOnePlus();
	}

	public static boolean isIUNI() {
		return is8902() || is8905() || is8910();
	}

	public static boolean isFind7() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("FIND");
	}

	public static boolean isIndia() {
//		String prop = SystemProperties.get("persist.sys.country");
//		String prop = SystemProperties.get("ro.product.locale.region");
		String prop = SystemProperties.get("ro.iuni.country.option");
		return prop != null && prop.equalsIgnoreCase("INDIA");
	}
	
	//u5
	public static boolean is7503() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("CBL7503");
	}

	// U3m
	public static boolean is8905() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("8905");
	}

	// U3
	public static boolean is8910() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("8910");
	}

	// U2
	public static boolean is8902() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("8902");
	}

	public static boolean isSupportDualData() {
		return is8905() || PhoneUtils.isMtk() || is7503();
	}

	public static boolean isDsds() {
		String multiSimConfig = SystemProperties
				.get("persist.radio.multisim.config");
		return multiSimConfig.equals("dsds");
	}

	public static boolean isHonor6() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("H60L01");
	}

	public static boolean is9008v() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("G9008V");
	}

	public static boolean hasKey() {
		return ViewConfiguration.get(PhoneGlobals.getInstance())
				.hasPermanentMenuKey();
	}

	// U3m
	public static boolean isNexus5() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("Nexus5");
	}
	
	public static boolean isLG() {
		String prop = SystemProperties.get("ro.gn.gnprojectid");
		return prop.contains("Nexus5");
	}
	
	public static boolean isI2() {
		String prop = SystemProperties.get("ro.product.model");
		return prop.contains("N1") && (Build.VERSION.SDK_INT == 21);
	}

}
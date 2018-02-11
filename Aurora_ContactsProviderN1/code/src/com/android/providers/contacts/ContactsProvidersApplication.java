package com.android.providers.contacts;

import java.util.Locale;

import com.privacymanage.service.AuroraPrivacyUtils;

import android.app.Application;
import android.os.SystemProperties;

public class ContactsProvidersApplication extends Application {
	public static boolean sIsGnContactsSupport = true;//SystemProperties.get("ro.gn.contacts.newfeature").equals("yes");
	//Gionee:huangzy 20120615 add for CR00624875 start
	public static final boolean sIsGnZoomClipSupport = SystemProperties.get("ro.gn.zoomclipview.prop").equals("yes") && sIsGnContactsSupport;
	//Gionee:huangzy 20120615 add for CR00624875 end
	
	//Gionee:huangzy 20120906 add for CR00688166 start
	public static final int GN_MATCH_CONTACTS_NUMBER_LENGTH = SystemProperties.getInt("ro.gn.match.numberlength", 11);//qiaohu 20141114
	//Gionee:huangzy 20120906 add for CR00688166 end
	
    //Gionee:huangzy 20121128 add for CR00736966 start
	public static final boolean sIsGnSyncSupport = true;
    //Gionee:huangzy 20121128 add for CR00736966 end
	
	public static final boolean sIsAuroraRejectSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
	
	public static final boolean sIsAuroraPrivacySupport = true;
	
    //Gionee:huangzy 20121011 add for CR00710695 start
	public static final boolean sIsGnDialerSearchSupport = true;//SystemProperties.get("ro.gn.contacts.gndialersearch").equals("yes") || true;	
	public static final boolean sIsGnFlySupport = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
	
	public static final boolean sIsIndiaProduct = SystemProperties.get("ro.iuni.country.option").equals("INDIA");
	
	public static final boolean sIsCtsSupport = SystemProperties.get("ro.build.cts").equals("yes");
	
	private static ContactsProvidersApplication mInstance;
	public static ContactsProvidersApplication getInstance() {
		return mInstance;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		
		if (sIsAuroraPrivacySupport) {
            AuroraPrivacyUtils.bindService(getApplicationContext());
        }
	}
    //Gionee:huangzy 20121011 add for CR00710695 end 
}

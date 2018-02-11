package com.android.providers.contacts;

import java.util.Locale;

import com.privacymanage.service.AuroraPrivacyUtils;

import android.app.Application;
import android.os.SystemProperties;

public class ContactsProvidersApplication extends Application {
	public static boolean sIsGnContactsSupport = true;//SystemProperties.get("ro.gn.contacts.newfeature").equals("yes");
	//Gionee:huangzy 20120615 add for CR00624875 start
//	public static final boolean sIsGnZoomClipSupport = SystemProperties.get("ro.gn.zoomclipview.prop").equals("yes") && sIsGnContactsSupport;
	public static final boolean sIsGnZoomClipSupport = true;
	//Gionee:huangzy 20120615 add for CR00624875 end
	
	//Gionee:huangzy 20120906 add for CR00688166 start
	public static final int GN_MATCH_CONTACTS_NUMBER_LENGTH = SystemProperties.getInt("ro.gn.match.numberlength", 11);//qiaohu 20141114
	//Gionee:huangzy 20120906 add for CR00688166 end
	
    //Gionee:huangzy 20121128 add for CR00736966 start
	public static final boolean sIsGnSyncSupport = true;
    //Gionee:huangzy 20121128 add for CR00736966 end
	
	public static final boolean sIsAuroraRejectSupport = SystemProperties.get("ro.aurora.reject.support").equals("yes");
	
	public static final boolean sIsAuroraPrivacySupport = true;
	
	public static final boolean sIsAuroraWhitelistSupport = true;
	
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
	
	public static boolean isCts() {
		return !isChinaSetting();
	}
	
	public static boolean isCtsForContactAggregator() {
		return true;//20160114 by liyang
	}
	
	private static boolean isChinaSetting() {  
        String language = getLanguageEnv();  
  
        if (language != null  
                && (language.trim().equals("zh-CN") || language.trim().equals("zh-TW")))  
            return true;  
        else  
            return false;  
    }  
	
	private static  String getLanguageEnv() {  
	       Locale l = Locale.getDefault();  
	       String language = l.getLanguage();  
	       String country = l.getCountry().toLowerCase();  
	       if ("zh".equals(language)) {  
	           if ("cn".equals(country)) {  
	               language = "zh-CN";  
	           } else if ("tw".equals(country)) {  
	               language = "zh-TW";  
	           }  
	       } else if ("pt".equals(language)) {  
	           if ("br".equals(country)) {  
	               language = "pt-BR";  
	           } else if ("pt".equals(country)) {  
	               language = "pt-PT";  
	           }  
	       }  
	       return language;  
	   }  
}

package com.mediatek.providers.contacts;

public class ContactsFeatureConstants {

	public interface FeatureOption {
        public static boolean MTK_SEARCH_DB_SUPPORT = com.aurora.featureoption.FeatureOption.MTK_SEARCH_DB_SUPPORT;
        public static boolean MTK_DIALER_SEARCH_SUPPORT = com.aurora.featureoption.FeatureOption.MTK_DIALER_SEARCH_SUPPORT;
        public static boolean MTK_GEMINI_SUPPORT = com.aurora.featureoption.FeatureOption.MTK_GEMINI_SUPPORT;
        
        public static boolean mIsQCContactsSupport = com.aurora.featureoption.FeatureOption.GN_QC_CONTACTS_PRODUCT;
	}
	
	public static boolean DBG_DIALER_SEARCH = true;
}

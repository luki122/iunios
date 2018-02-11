/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.preference;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.ResConstant.ContactPhotoStyle;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.SimCardUtils;
//import com.mediatek.audioprofile.AudioProfileManager;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import aurora.preference.AuroraCheckBoxPreference; // import android.preference.CheckBoxPreference;
import aurora.preference.AuroraListPreference; // import android.preference.ListPreference;
import aurora.preference.AuroraPreference; // import android.preference.Preference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener; //import android.preference.Preference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference.OnPreferenceClickListener; // import android.preference.Preference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceFragment;// import android.preference.PreferenceFragment;
import aurora.preference.AuroraPreferenceManager.OnActivityStopListener;// import android.preference.PreferenceManager.OnActivityStopListener;
import android.provider.Settings;

/**
 * This fragment shows the preferences for the first header.
 */
public class DisplayOptionsPreferenceFragment2 extends AuroraPreferenceFragment implements
	OnPreferenceChangeListener, OnPreferenceClickListener {

	public static final String GEMINI_SIM_MANAGEMENT = "gemini_sim_management";
	public static final String CALL_SETTING_KEY = "call_settings";
	public static final String LISTITEM_ONCLICK_STYLE_KEY = "listItemOnclickAction";
	
	//Gionee <huangzy> <2013-05-08> add for CR00809854 begin
	public static final String TONE_DIALING_KEY = "toneDialing";
	//Gionee <huangzy> <2013-05-08> add for CR00809854 end
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        
//        //Gionee <wangth><2013-04-25> modify for CR00801922 begin
//        /*
//        addPreferencesFromResource(R.xml.gn_preference_call_setting_options);
//        */
//        if ((GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.isMultiSimEnabled())
//                || (!GNContactsUtils.isOnlyQcContactsSupport() && FeatureOption.MTK_GEMINI_SUPPORT)) {
//            addPreferencesFromResource(R.xml.gn_preference_call_setting_options);
//        } else {
//            addPreferencesFromResource(R.xml.gn_preference_call_setting_options_single_sim);
//        }
//        //Gionee <wangth><2013-04-25> modify for CR00801922 end
//    	addPreferencesFromResource(R.xml.gn_preference_listitem_onclick_options);
//    	
//    	//Gionee <huangzy> <2013-05-08> add for CR00809854 start
//    	if (com.gionee.featureoption.FeatureOption.GN_FEATUTE_TONE_DAILING) {
//    		addPreferencesFromResource(R.xml.gn_preference_tactile_options);
//    	}
//    	//Gionee <huangzy> <2013-05-08> add for CR00809854 end
//    	
//    	AuroraPreference preference = null;
//    	
//    	preference = findPreference(GEMINI_SIM_MANAGEMENT);
//    	if (null != preference) {
//    	    //Gionee <wangth><2013-04-26> modify for CR00801922 begin
//    		if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
//            	int sim1Slotid = SimCardUtils.SimSlot.SLOT_ID1;
//                int sim2Slotid = SimCardUtils.SimSlot.SLOT_ID2;
//                if (!SimCardUtils.isSimInserted(sim1Slotid) 
//                        && !SimCardUtils.isSimInserted(sim2Slotid)) {
//                	preference.setEnabled(false);
//                } else {
//                    if (GNContactsUtils.isMultiSimEnabled()) {
//                        Intent intent = new Intent();
//                        intent.setClassName("com.android.settings", "com.android.settings.multisimsettings.MultiSimSettings");
//                        preference.setIntent(intent);
//                    }
//                    //Gionee <wangth><2013-04-26> modify for CR00801922 end
//                	boolean isWifiOnly = isWifiOnly();
//                	preference.setEnabled(!isWifiOnly);    	
//                }
//            } else {
//            	preference.setEnabled(false);
//            }	
//    	}
//    	
//    	preference = findPreference(LISTITEM_ONCLICK_STYLE_KEY);
//    	if (null != preference && preference instanceof AuroraListPreference) {
//    		preference.setSummary(((AuroraListPreference)preference).getEntry());
//    		preference.setOnPreferenceChangeListener(this);
//    	}
//    	
//    	AuroraPreference callSettingPreference = findPreference(CALL_SETTING_KEY);
//    	if (null != callSettingPreference) {
//    		callSettingPreference.setOnPreferenceClickListener(this);
//    	}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		AuroraPreference preference = null;
		
		//Gionee <huangzy> <2013-05-08> add for CR00809854 start
//    	if (com.gionee.featureoption.FeatureOption.GN_FEATUTE_TONE_DAILING) {
//    		preference = findPreference(TONE_DIALING_KEY);
//    		AudioProfileManager profileManager = getAudioProfileManager();
//    		boolean enabled = profileManager.getDtmfToneEnabled(
//    				profileManager.getActiveProfileKey());
//
//        	if (null != preference && preference instanceof AuroraCheckBoxPreference) {
//        		((AuroraCheckBoxPreference)preference).setChecked(enabled);        		
//        		preference.setOnPreferenceChangeListener(this);
//        	}	
//    	}
    	//Gionee <huangzy> <2013-05-08> add for CR00809854 end
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
//		if (LISTITEM_ONCLICK_STYLE_KEY.equals(preference.getKey())) {
//			String[] listItemOnclickOptionsEntries = getResources().getStringArray(
//	        		R.array.gn_entries_listitem_onclick_options);
//			String[] listItemOnclickOptionsEntrieValues = getResources().getStringArray(
//	        		R.array.gn_entryvalues_listitem_onclick_options);
//			
//			if (null != listItemOnclickOptionsEntries &&
//					null != listItemOnclickOptionsEntrieValues &&
//						listItemOnclickOptionsEntrieValues.length == listItemOnclickOptionsEntries.length) {
//				for (int i = 0; i < listItemOnclickOptionsEntrieValues.length; i++) {
//					if (listItemOnclickOptionsEntrieValues[i].equals(newValue)) {
//						preference.setSummary(listItemOnclickOptionsEntries[i]);
//						ResConstant.setCallLogListItemPrimaryAction(getActivity(), Integer.valueOf(newValue.toString()));
//						break;
//					}
//				}
//			}
//			
//			return true;
//    	}
//		
//		//Gionee <huangzy> <2013-05-08> add for CR00809854 begin
//		if (TONE_DIALING_KEY.equals(preference.getKey()) && newValue instanceof Boolean) {
//			boolean checked = (Boolean) newValue;
//			((AuroraCheckBoxPreference)preference).setChecked(checked);			
//			AudioProfileManager profileManager = getAudioProfileManager();
//			profileManager.setDtmfToneEnabled(
//					profileManager.getActiveProfileKey(), checked);
//		}
//		//Gionee <huangzy> <2013-05-08> add for CR00809854 end
		
		return false;
	}

	@Override
	public boolean onPreferenceClick(AuroraPreference preference) {
//		if (CALL_SETTING_KEY.equals(preference.getKey())) {
//			startActivity(ContactsUtils.getCallSettingsIntent());
//			return true;
//		}
		
		return false;
	}
	
	protected boolean isWifiOnly() {
    	ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
	}
	
	//Gionee <huangzy> <2013-05-08> add for CR00809854 begin
//	private AudioProfileManager getAudioProfileManager() {
//		return (AudioProfileManager) ContactsApplication.getInstance().
//				getSystemService(Context.AUDIOPROFILE_SERVICE);
//	}
	//Gionee <huangzy> <2013-05-08> add for CR00809854 end
}

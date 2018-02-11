/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.settings;

import aurora.app.AuroraActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceGroup;
import aurora.preference.AuroraPreferenceScreen;
import android.util.Log;
import android.widget.Toast;
import java.util.Date;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Gionee <wangguojing> 2013-06-29 add for CR00831786 begin
import com.gionee.settings.utils.GnUtils;
//Gionee <wangguojing> 2013-06-29 add for CR00831786 end

public class DeviceInfoSettings extends SettingsPreferenceFragment {

    private static final String LOG_TAG = "DeviceInfoSettings";

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MSV = "/sys/board_properties/soc/msv";

    private static final String KEY_CONTAINER = "container";
    private static final String KEY_TEAM = "team";
    private static final String KEY_CONTRIBUTORS = "contributors";
    private static final String KEY_REGULATORY_INFO = "regulatory_info";
    private static final String KEY_TERMS = "terms";
    private static final String KEY_LICENSE = "license";
    private static final String KEY_COPYRIGHT = "copyright";
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String PROPERTY_URL_SAFETYLEGAL = "ro.url.safetylegal";
    private static final String PROPERTY_SELINUX_STATUS = "ro.build.selinux";
    private static final String KEY_KERNEL_VERSION = "kernel_version";
    private static final String KEY_BUILD_NUMBER = "build_number";
    private static final String KEY_DEVICE_MODEL = "device_model";
    private static final String KEY_SELINUX_STATUS = "selinux_status";
    private static final String KEY_BASEBAND_VERSION = "baseband_version";
    private static final String KEY_FIRMWARE_VERSION = "firmware_version";
    private static final String KEY_UPDATE_SETTING = "additional_system_update_settings";
    private static final String KEY_EQUIPMENT_ID = "fcc_equipment_id";
    private static final String PROPERTY_EQUIPMENT_ID = "ro.ril.fccid";
    private static final String KEY_HWINFO = "gn_devices_info";
    //status info key
    private static final String KEY_STATUS_INFO = "status_info";
    private static final String KEY_STATUS_INFO_GEMINI = "status_info_gemini";

    //Gionee zengxuanhui 2012-09-24 add for CR00688800 begin
    private static final String KEY_CUSTOM_BUILD_VERSION = "custom_build_version";
    //Gionee zengxuanhui 2012-09-24 add for CR00688800 end
    //Gionee zengxuanhui 20121123 add for CR00724044 begin
    private static final String KEY_GIONEE_ROM_VERSION = "gn_rom_version";
    //Gionee zengxuanhui 20121123 add for CR00724044 end

    static final int TAPS_TO_BE_A_DEVELOPER = 7;
    private Date mOldDate;
    private int mClickNum = 0;

    long[] mHits = new long[3];
    int mDevHitCountdown;
    Toast mDevHitToast;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.device_info_settings);

        setStringSummary(KEY_FIRMWARE_VERSION, Build.VERSION.RELEASE);
        findPreference(KEY_FIRMWARE_VERSION).setEnabled(true);
        setValueSummary(KEY_BASEBAND_VERSION, "gsm.version.baseband");
        setStringSummary(KEY_DEVICE_MODEL, Build.MODEL + getMsvSuffix());
        setValueSummary(KEY_EQUIPMENT_ID, PROPERTY_EQUIPMENT_ID);

        // Gionee xiaolin 20120503 modify for CR00588728 start
        // setStringSummary(KEY_DEVICE_MODEL, Build.MODEL);
        String productName = 
	        SystemProperties.get("ro.product.brand") == null ? "" : SystemProperties.get("ro.product.brand") + " ";
		if(Build.MODEL.contains("IUNI")) {
			setStringSummary(KEY_DEVICE_MODEL, Build.MODEL + getMsvSuffix());
		} else {
			setStringSummary(KEY_DEVICE_MODEL, productName + Build.MODEL + getMsvSuffix());
		}
	    // Gionee xiaolin 20120503 modify for CR00588728 end

        // Aurora <likai> modify begin
        //setStringSummary(KEY_BUILD_NUMBER, Build.DISPLAY);
        //setValueSummary(KEY_BUILD_NUMBER, "ro.gn.iuniznvernumber");
        String iuniBuildNumber = SystemProperties.get("ro.gn.iuniznvernumber",
                getResources().getString(R.string.device_info_default));
        if (iuniBuildNumber.indexOf("_") != -1) {
            iuniBuildNumber = iuniBuildNumber.substring(iuniBuildNumber.indexOf("_") + 3);
        }
        //delete time
        if (iuniBuildNumber.length() > 4) {
            iuniBuildNumber = iuniBuildNumber.substring(0, iuniBuildNumber.length() - 4);
        }
        //delete "IUNI-"
        if (iuniBuildNumber.length() > 5) {
            iuniBuildNumber = iuniBuildNumber.substring(5, iuniBuildNumber.length());
        }
        findPreference(KEY_BUILD_NUMBER).setSummary(iuniBuildNumber);
        // Aurora <likai> modify end
        //findPreference(KEY_BUILD_NUMBER).setEnabled(true);
        findPreference(KEY_KERNEL_VERSION).setSummary(getFormattedKernelVersion());

        //Gionee: lvxp 20120425 add for setting ota update flag start
        String gnOTAUpdateSuport = 
                SystemProperties.get("ro.gn.gnotaupdate.support") == null ? " " : SystemProperties.get("ro.gn.gnotaupdate.support");
        if (!gnOTAUpdateSuport.equalsIgnoreCase("yes")) {
            AuroraPreference pf_gn_ota_system_update = null;
            pf_gn_ota_system_update = findPreference("gn_ota_system_update_settings");
            if (pf_gn_ota_system_update != null) {
                getPreferenceScreen().removePreference(pf_gn_ota_system_update);
           }
        }
        //Gionee: lvxp 20120425 add for setting ota update flag end

        if (!SELinux.isSELinuxEnabled()) {
            String status = getResources().getString(R.string.selinux_status_disabled);
            setStringSummary(KEY_SELINUX_STATUS, status);
        } else if (!SELinux.isSELinuxEnforced()) {
            String status = getResources().getString(R.string.selinux_status_permissive);
            setStringSummary(KEY_SELINUX_STATUS, status);
        }

        // Remove selinux information if property is not present
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_SELINUX_STATUS,
                PROPERTY_SELINUX_STATUS);

        //Gionee zengxuanhui 2012-09-24 add for CR00688800 begin
        if((getPreferenceScreen() != null) && (findPreference(KEY_CUSTOM_BUILD_VERSION) != null)) {
            getPreferenceScreen().removePreference(findPreference(KEY_CUSTOM_BUILD_VERSION));
        }
        //Gionee zengxuanhui 2012-09-24 add for CR00688800 end
        
        //aurora 2013-11-02 hide hwinfo
        if((getPreferenceScreen() != null) && (findPreference(KEY_HWINFO) != null)) {
             getPreferenceScreen().removePreference(findPreference(KEY_HWINFO));
        }

        //Gionee zengxuanhui 20121123 add for CR00724044 begin
        if((getPreferenceScreen() != null) && (findPreference(KEY_GIONEE_ROM_VERSION) != null)) {
            String romVer = SystemProperties.get("ro.gn.gnromvernumber", "GiONEE ROM4.0.1");
            String Ver = romVer.substring(romVer.indexOf("M") == -1 ? 0 : (romVer.indexOf( "M" ) + 1));
            setStringSummary(KEY_GIONEE_ROM_VERSION, "GN" + Ver);
            getPreferenceScreen().removePreference(findPreference(KEY_GIONEE_ROM_VERSION));
        }
        //Gionee zengxuanhui 20121123 add for CR00724044 end

        // Remove Safety information preference if PROPERTY_URL_SAFETYLEGAL is not set
        removePreferenceIfPropertyMissing(getPreferenceScreen(), "safetylegal",
                PROPERTY_URL_SAFETYLEGAL);

        // Remove Equipment id preference if FCC ID is not set by RIL
        removePreferenceIfPropertyMissing(getPreferenceScreen(), KEY_EQUIPMENT_ID,
                PROPERTY_EQUIPMENT_ID);

        // Remove Baseband version if wifi-only device
        if (Utils.isWifiOnly(getActivity())) {
//            getPreferenceScreen().removePreference(findPreference(KEY_BASEBAND_VERSION));
        }

        /*
         * Settings is a generic app and should not contain any device-specific
         * info.
         */
        final AuroraActivity act = (AuroraActivity)getActivity();
        // These are contained in the "container" preference group
        AuroraPreferenceGroup parentPreference = (AuroraPreferenceGroup) findPreference(KEY_CONTAINER);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TERMS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_LICENSE,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_COPYRIGHT,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_TEAM,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // These are contained by the root preference screen
        parentPreference = getPreferenceScreen();
        if (UserHandle.myUserId() == UserHandle.USER_OWNER) {
            Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference,
                    KEY_SYSTEM_UPDATE_SETTINGS,
                    Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);
        } else {
            // Remove for secondary users
            removePreference(KEY_SYSTEM_UPDATE_SETTINGS);
        }
        Utils.updatePreferenceToSpecificActivityOrRemove(act, parentPreference, KEY_CONTRIBUTORS,
                Utils.UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY);

        // Read platform settings for additional system update setting
        removePreferenceIfBoolFalse(KEY_UPDATE_SETTING,
                R.bool.config_additional_system_update_setting_enable);

        // Remove regulatory information if not enabled.
        removePreferenceIfBoolFalse(KEY_REGULATORY_INFO,
                R.bool.config_show_regulatory_info);

        if ((getPreferenceScreen() != null) && (findPreference(KEY_SELINUX_STATUS) != null)) {
            parentPreference.removePreference(findPreference(KEY_SELINUX_STATUS));
        }
        if ((getPreferenceScreen() != null) && (findPreference(KEY_STATUS_INFO) != null)) {
            parentPreference.removePreference(findPreference(KEY_STATUS_INFO));
        }
        if((getPreferenceScreen() != null) && (findPreference(KEY_STATUS_INFO_GEMINI) != null)) {
            parentPreference.removePreference(findPreference(KEY_STATUS_INFO_GEMINI));
        }
        //Gionee <wangguojing> 2013-06-29 modify for CR00831786 begin
//        if (GnUtils.isDoubleCard()) {
//            if((getPreferenceScreen() != null) && (findPreference(KEY_STATUS_INFO) != null)) {
//                parentPreference.removePreference(findPreference(KEY_STATUS_INFO));
//            }
//        } else {
//            if((getPreferenceScreen() != null) && (findPreference(KEY_STATUS_INFO_GEMINI) != null)) {
//                parentPreference.removePreference(findPreference(KEY_STATUS_INFO_GEMINI));
//            }
//        }
        //Gionee <wangguojing> 2013-06-29 modify for CR00831786 end

	// Gionee wangyaohui 20120914 modify for CR00689629 begin
        if((getPreferenceScreen() != null) && (findPreference(KEY_SYSTEM_UPDATE_SETTINGS) != null)) {
           getPreferenceScreen().removePreference(findPreference(KEY_SYSTEM_UPDATE_SETTINGS));
        }
//        if((getPreferenceScreen() != null) && (findPreference(KEY_BASEBAND_VERSION) != null)) {
//           getPreferenceScreen().removePreference(findPreference(KEY_BASEBAND_VERSION));
//            if (findPreference(KEY_BASEBAND_VERSION) != null) {
//                Log.v("xiaoyong", "baseband_version not null");
//            } else {
//                Log.v("xiaoyong", "baseband_version is null");
//            }
//        }
        // Gionee wangyaohui 20120914 modify for CR00689629 end
        mOldDate = new Date();
    }

    @Override
    public void onResume() {
        super.onResume();
        mDevHitCountdown = getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
                Context.MODE_PRIVATE).getBoolean(DevelopmentSettings.PREF_SHOW,
                        android.os.Build.TYPE.equals("eng")) ? -1 : TAPS_TO_BE_A_DEVELOPER;
        mDevHitToast = null;
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference.getKey().equals(KEY_FIRMWARE_VERSION)) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
            mHits[mHits.length-1] = SystemClock.uptimeMillis();
            if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClassName("android",
                        com.android.internal.app.PlatLogoActivity.class.getName());
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
                }
            }
        }else if (preference.getKey().equals(KEY_BUILD_NUMBER)) {
        	if(GnUtils.isAbroadVersion()){
	            if (mDevHitCountdown > 0) {
	                mDevHitCountdown--;
	                if (mDevHitCountdown == 0) {
	                    getActivity().getSharedPreferences(DevelopmentSettings.PREF_FILE,
	                            Context.MODE_PRIVATE).edit().putBoolean(
	                                    DevelopmentSettings.PREF_SHOW, true).apply();
	                    if (mDevHitToast != null) {
	                        mDevHitToast.cancel();
	                    }
	                    mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_on,
	                            Toast.LENGTH_LONG);
	                    mDevHitToast.show();
	                } else if (mDevHitCountdown > 0
	                        && mDevHitCountdown < (TAPS_TO_BE_A_DEVELOPER-2)) {
	                    if (mDevHitToast != null) {
	                        mDevHitToast.cancel();
	                    }
	                    mDevHitToast = Toast.makeText(getActivity(), getResources().getString(
	                            R.string.show_dev_countdown, mDevHitCountdown),
	                            Toast.LENGTH_SHORT);
	                    mDevHitToast.show();
	                }
	            } else if (mDevHitCountdown < 0) {
	                if (mDevHitToast != null) {
	                    mDevHitToast.cancel();
	                }
	                mDevHitToast = Toast.makeText(getActivity(), R.string.show_dev_already,
	                        Toast.LENGTH_LONG);
	                mDevHitToast.show();
	            }
        }else{
        		Date curDate = new Date();
	            long gapDate = curDate.getTime() - mOldDate.getTime();
	            if (gapDate <= 3000) {
	                mClickNum += 1;
	                if (mClickNum == 5) {
	                    Date newYearEve = new Date(2014, 0, 31, 0, 0, 0);
	                    int year = curDate.getYear();
	                    curDate.setYear(curDate.getYear() + 1900);
	                    Log.v("xiaoyong", "year = " + curDate.getYear() + " newyear = " + newYearEve.getYear());
	                    Log.v("xiaoyong", "month = " + curDate.getMonth() + " newyear month = " + newYearEve.getMonth());
	                    Log.v("xiaoyong", "curDate = " + curDate.getTime() + " newYearEve = " + newYearEve.getTime());
	                    if (curDate.after(newYearEve)) {
	//                    if (curDate.getTime() >= newYearEve.getTime()) {
	                        //showPic();
	                        Log.v("xiaoyong", "after newYearEve");
	                    } else {
	                        Log.v("xiaoyong", "early newYearEve");
	                    }
	    				scanImage();
	                   //Toast.makeText(getActivity(), "Happy New Year!", Toast.LENGTH_LONG).show(); 
	                    //showPic();
	                } else if (mClickNum > 5) {
	                    mClickNum = 0;
	                }
	//                mClickNum += 1;
	                Log.v("xiaoyong", "mClickNum = " + mClickNum);
	            } else {
	                mClickNum = 1;
	            }
	            mOldDate = curDate;
        
        	}
        } 
//        Date curDate = new Date(System.currentTimeMillis());
      /*  else if (preference.getKey().equals(KEY_BUILD_NUMBER)) {
        	Date curDate = new Date();
            long gapDate = curDate.getTime() - mOldDate.getTime();
//            Log.v("xiaoyong", "gapDate = " + gapDate);
            if (gapDate <= 3000) {
                mClickNum += 1;
                if (mClickNum == 5) {
//                    Log.v("xiaoyong", "showPic() Toast");
                    Date newYearEve = new Date(2014, 0, 31, 0, 0, 0);
                    int year = curDate.getYear();
                    curDate.setYear(curDate.getYear() + 1900);
                    Log.v("xiaoyong", "year = " + curDate.getYear() + " newyear = " + newYearEve.getYear());
                    Log.v("xiaoyong", "month = " + curDate.getMonth() + " newyear month = " + newYearEve.getMonth());
                    Log.v("xiaoyong", "curDate = " + curDate.getTime() + " newYearEve = " + newYearEve.getTime());
                    if (curDate.after(newYearEve)) {
//                    if (curDate.getTime() >= newYearEve.getTime()) {
                        //showPic();
                        Log.v("xiaoyong", "after newYearEve");
                    } else {
                        Log.v("xiaoyong", "early newYearEve");
                    }
    				scanImage();
                   //Toast.makeText(getActivity(), "Happy New Year!", Toast.LENGTH_LONG).show(); 
                    //showPic();
                } else if (mClickNum > 5) {
                    mClickNum = 0;
                }
//                mClickNum += 1;
                Log.v("xiaoyong", "mClickNum = " + mClickNum);
            } else {
                mClickNum = 1;
            }
            mOldDate = curDate;
        }*/
        
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

	/**
	 *预览动画
     *
	 */
	public void scanImage() {
		 try {
				Intent intent = new Intent(); 
				intent.setClass(getActivity(), ScanEggImage.class); 
                startActivity(intent);  
				getActivity().overridePendingTransition(R.anim.aurora_zoom_in, 0);
            } catch (Exception e) {
				e.printStackTrace();  
            }  
	}

    private void removePreferenceIfPropertyMissing(AuroraPreferenceGroup preferenceGroup,
            String preference, String property ) {
        if (SystemProperties.get(property).equals("")) {
            // Property is missing so remove preference from group
            try {
                preferenceGroup.removePreference(findPreference(preference));
            } catch (RuntimeException e) {
                Log.d(LOG_TAG, "Property '" + property + "' missing and no '"
                        + preference + "' preference");
            }
        }
    }

    private void removePreferenceIfBoolFalse(String preference, int resId) {
        if (!getResources().getBoolean(resId)) {
            AuroraPreference pref = findPreference(preference);
            if (pref != null) {
                getPreferenceScreen().removePreference(pref);
            }
        }
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }

    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            Log.e(LOG_TAG,
                "IO Exception when getting kernel version for Device Info screen",
                e);

            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012
      //gionee wangyaohui 20120612 modify for CR00623199 begin
//        final String PROC_VERSION_REGEX =
//            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
//            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
//            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
//            "(#\\d+) " +              /* group 3: "#1" */
//            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
//            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */
        final String PROC_VERSION_REGEX =
                "\\w+\\s+" + /* ignore: Linux */
                "\\w+\\s+" + /* ignore: version */
                "([^\\s]+)\\s.+" ; /* group 1: 2.6.22-omap1 */
//gionee wangyaohui 20120612 modify for CR00623199 end
        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        
      //gionee wangyaohui 20120612 modify for CR00623199 begin      
//        if (!m.matches()) {
//            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
//            return "Unavailable";
//        } else if (m.groupCount() < 4) {
//            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
//                    + " groups");
//            return "Unavailable";
//        }
//        return m.group(1) + "\n" +                 // 3.0.31-g6fb96c9
//            m.group(2) + " " + m.group(3) + "\n" + // x@y.com #1
//            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
//    }
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 1) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        } else {
            return (new StringBuilder(m.group(1))).toString();
        }
    //gionee wangyaohui 20120612 modify for CR00623199 end
    }

    /**
     * Returns " (ENGINEERING)" if the msv file has a zero value, else returns "".
     * @return a string to append to the model number description.
     */
    private String getMsvSuffix() {
        // Production devices should have a non-zero value. If we can't read it, assume it's a
        // production device so that we don't accidentally show that it's an ENGINEERING device.
        try {
            String msv = readLine(FILENAME_MSV);
            // Parse as a hex number. If it evaluates to a zero, then it's an engineering build.
            if (Long.parseLong(msv, 16) == 0) {
                return " (ENGINEERING)";
            }
        } catch (IOException ioe) {
            // Fail quietly, as the file may not exist on some devices.
        } catch (NumberFormatException nfe) {
            // Fail quietly, returning empty string should be sufficient
        }
        return "";
    }
}

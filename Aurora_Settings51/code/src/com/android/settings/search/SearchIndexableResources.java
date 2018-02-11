/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2014 The Android Open Source Project
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

package com.android.settings.search;

import android.provider.SearchIndexableResource;

import com.android.settings.DataUsageSummary;
import com.android.settings.DateTimeSettings;
import com.android.settings.DevelopmentSettings;
import com.android.settings.DeviceInfoSettings;
import com.android.settings.DisplaySettings;
import com.android.settings.HomeSettings;
import com.android.settings.ScreenPinningSettings;
import com.android.settings.PrivacySettings;
import com.android.settings.R;
import com.android.settings.SecuritySettings;
import com.android.settings.TetherSettings;
import com.android.settings.WallpaperTypeSettings;
import com.android.settings.WirelessSettings;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.FingerAndBodySettings;
import com.android.settings.accounts.AccountSettings;
import com.android.settings.bluetooth.AuroraBluetoothSettings;
import com.android.settings.bluetooth.BluetoothSettings;
import com.android.settings.deviceinfo.Memory;
import com.android.settings.deviceinfo.UsbSettings;
import com.android.settings.fuelgauge.BatterySaverSettings;
import com.android.settings.fuelgauge.PowerUsageSummary;
import com.android.settings.inputmethod.InputMethodAndLanguageSettings;
import com.android.settings.location.LocationSettings;
import com.android.settings.net.DataUsageMeteredSettings;
import com.android.settings.notification.AuroraAppNotificationSettings;
import com.android.settings.notification.AuroraStatusNotifyPushSettings;
import com.android.settings.notification.NotificationSettings;
import com.android.settings.notification.OtherSoundSettings;
import com.android.settings.notification.ZenModeSettings;
import com.android.settings.print.PrintSettingsFragment;
import com.android.settings.sim.SimSettings;
import com.android.settings.theme.ThemeFragment;
import com.android.settings.users.UserSettings;
import com.android.settings.voice.VoiceInputSettings;
import com.android.settings.vpn2.VpnSettings;
import com.android.settings.wifi.AdvancedWifiSettings;
import com.android.settings.wifi.SavedAccessPointsWifiSettings;
import com.android.settings.wifi.WifiSettings;
import com.aurora.advancedsettings.AdvancedSettings;
import com.mediatek.audioprofile.AudioProfileSettings;
import com.mediatek.audioprofile.SoundEnhancement;
import com.mediatek.search.SearchExt;
import com.mediatek.settings.hotknot.HotKnotSettings;
import com.mediatek.hotknot.MoreNetworkSettings;
import com.mediatek.nfc.NfcSettings;

import java.util.Collection;
import java.util.HashMap;

public final class SearchIndexableResources {

    public static int NO_DATA_RES_ID = 0;

    private static HashMap<String, SearchIndexableResource> sResMap =
            new HashMap<String, SearchIndexableResource>();

    static {
    	/*
    	 *Wlan 网络设置
    	 */
        sResMap.put(WifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(WifiSettings.class.getName()),
                        NO_DATA_RES_ID,
                        WifiSettings.class.getName(),
                        R.drawable.aurora_settings_wifi));

        sResMap.put(AdvancedWifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AdvancedWifiSettings.class.getName()),
                        R.xml.wifi_advanced_settings,
                        AdvancedWifiSettings.class.getName(),
                        R.drawable.aurora_settings_wifi));

        sResMap.put(SavedAccessPointsWifiSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SavedAccessPointsWifiSettings.class.getName()),
                        NO_DATA_RES_ID,
                        SavedAccessPointsWifiSettings.class.getName(),
                        R.drawable.aurora_settings_wifi));

        /*
         * 蓝牙
         */
        sResMap.put(AuroraBluetoothSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AuroraBluetoothSettings.class.getName()),
                        NO_DATA_RES_ID,
                        AuroraBluetoothSettings.class.getName(),
                        R.drawable.aurora_settings_bluetooth));
        /*
         * 其他网络设置
         */
        sResMap.put(MoreNetworkSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(MoreNetworkSettings.class.getName()),
                        R.xml.aurora_more_network_settings,
                        MoreNetworkSettings.class.getName(),
                        R.drawable.ic_settings_more));
        
        /*
         * 热点设置
         */
        sResMap.put(TetherSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(TetherSettings.class.getName()),
                        NO_DATA_RES_ID,
                        TetherSettings.class.getName(),
                        R.drawable.ic_settings_tether));
        
        /*
         * VPN设置
         */
        sResMap.put(VpnSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(VpnSettings.class.getName()),
                        R.xml.vpn_settings2,
                        VpnSettings.class.getName(),
                        R.drawable.ic_settings_vpn));
        
        
        
        
        
        //该功能不在设置内部，是一个独立的app
        sResMap.put(AccountSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AccountSettings.class.getName()),
                        R.xml.account_settings,
                        AccountSettings.class.getName(),
                        R.drawable.ic_settings_accounts));

/*        sResMap.put(SimSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SimSettings.class.getName()),
                        NO_DATA_RES_ID,
                        SimSettings.class.getName(),
                        R.drawable.ic_sim_sd));*/

   /*     sResMap.put(DataUsageSummary.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DataUsageSummary.class.getName()),
                        NO_DATA_RES_ID,
                        DataUsageSummary.class.getName(),
                        R.drawable.ic_settings_data_usage));*/
/*
        sResMap.put(DataUsageMeteredSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DataUsageMeteredSettings.class.getName()),
                        NO_DATA_RES_ID,
                        DataUsageMeteredSettings.class.getName(),
                        R.drawable.ic_settings_data_usage));*/
/*
        sResMap.put(WirelessSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(WirelessSettings.class.getName()),
                        NO_DATA_RES_ID,
                        WirelessSettings.class.getName(),
                        R.drawable.ic_settings_more));*/

/*        sResMap.put(HomeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(HomeSettings.class.getName()),
                        NO_DATA_RES_ID,
                        HomeSettings.class.getName(),
                        R.drawable.ic_settings_home));*/

        /*
         * 显示
         */
        sResMap.put(DisplaySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DisplaySettings.class.getName()),
                        NO_DATA_RES_ID,
                        DisplaySettings.class.getName(),
                        R.drawable.aurora_settings_display));
        
        /*
         * 通知
         */
        sResMap.put(AuroraStatusNotifyPushSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AuroraStatusNotifyPushSettings.class.getName()),
                        R.xml.aurora_status_notify_push_settings,
                        AuroraStatusNotifyPushSettings.class.getName(),
                        R.drawable.ic_settings_notifications));

/*        sResMap.put(WallpaperTypeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(WallpaperTypeSettings.class.getName()),
                        NO_DATA_RES_ID,
                        WallpaperTypeSettings.class.getName(),
                        R.drawable.ic_settings_display));*/

        /*sResMap.put(NotificationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(NotificationSettings.class.getName()),
                        NO_DATA_RES_ID,
                        NotificationSettings.class.getName(),
                        R.drawable.ic_settings_notifications));

        sResMap.put(OtherSoundSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(OtherSoundSettings.class.getName()),
                        NO_DATA_RES_ID,
                        OtherSoundSettings.class.getName(),
                        R.drawable.ic_settings_notifications));

        sResMap.put(ZenModeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ZenModeSettings.class.getName()),
                        NO_DATA_RES_ID,
                        ZenModeSettings.class.getName(),
                        R.drawable.ic_settings_notifications));*/

        /*
         * 存储
         */
        sResMap.put(Memory.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(Memory.class.getName()),
                        NO_DATA_RES_ID,
                        Memory.class.getName(),
                        R.drawable.ic_settings_storage));

        /*
         * USB
         */
        sResMap.put(UsbSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(UsbSettings.class.getName()),
                        R.xml.aurora_usb_settings,
                        UsbSettings.class.getName(),
                        R.drawable.ic_settings_usb));
        

        /*
         * 定位
         */
        sResMap.put(LocationSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(LocationSettings.class.getName()),
                        R.xml.location_settings,
                        LocationSettings.class.getName(),
                        R.drawable.ic_settings_location));
        
        
        
        /*
         * 更多设置
         */
        sResMap.put(AdvancedSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AdvancedSettings.class.getName()),
                        R.xml.aurora_advanced_settings,
                        AdvancedSettings.class.getName(),
                        R.drawable.aurora_settings_advanced));

        
        
      /*  sResMap.put(BatterySaverSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(BatterySaverSettings.class.getName()),
                        R.xml.battery_saver_settings,
                        BatterySaverSettings.class.getName(),
                        R.drawable.aurora_settings_advanced));

        sResMap.put(UserSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(UserSettings.class.getName()),
                        R.xml.user_settings,
                        UserSettings.class.getName(),
                        R.drawable.ic_settings_multiuser));*/



        /*
         * 安全
         */
        sResMap.put(SecuritySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SecuritySettings.class.getName()),
                        NO_DATA_RES_ID,
                        SecuritySettings.class.getName(),
                        R.drawable.ic_settings_security));

       /* sResMap.put(ScreenPinningSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ScreenPinningSettings.class.getName()),
                        NO_DATA_RES_ID,
                        ScreenPinningSettings.class.getName(),
                        R.drawable.ic_settings_security));*/

        /*
         * 语言和输入法
         */
        sResMap.put(InputMethodAndLanguageSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(InputMethodAndLanguageSettings.class.getName()),
                        NO_DATA_RES_ID,
                        InputMethodAndLanguageSettings.class.getName(),
                        R.drawable.ic_settings_language));

     /*   sResMap.put(VoiceInputSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(VoiceInputSettings.class.getName()),
                        NO_DATA_RES_ID,
                        VoiceInputSettings.class.getName(),
                        R.drawable.ic_settings_language));
        */

/*        sResMap.put(PrivacySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrivacySettings.class.getName()),
                        NO_DATA_RES_ID,
                        PrivacySettings.class.getName(),
                        R.drawable.ic_settings_backup));*/

        /*
         * 日期和时间
         */
        sResMap.put(DateTimeSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DateTimeSettings.class.getName()),
                        R.xml.aurora_date_time_prefs,
                        DateTimeSettings.class.getName(),
                        R.drawable.ic_settings_date_time));

        /*
         * 辅助功能
         */
        sResMap.put(AccessibilitySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AccessibilitySettings.class.getName()),
                        NO_DATA_RES_ID,
                        AccessibilitySettings.class.getName(),
                        R.drawable.ic_settings_accessibility));
        /*
         * 手势体感
         */
        sResMap.put(FingerAndBodySettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(FingerAndBodySettings.class.getName()),
                        NO_DATA_RES_ID,
                        FingerAndBodySettings.class.getName(),
                        R.drawable.ic_settings_finger_body));
        

/*        sResMap.put(PrintSettingsFragment.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(PrintSettingsFragment.class.getName()),
                        NO_DATA_RES_ID,
                        PrintSettingsFragment.class.getName(),
                        R.drawable.ic_settings_print));*/

        /*
         * 开发者选项
         */
        sResMap.put(DevelopmentSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DevelopmentSettings.class.getName()),
                        NO_DATA_RES_ID,
                        DevelopmentSettings.class.getName(),
                        R.drawable.ic_settings_development));

        /*
         * 关于手机
         */
        sResMap.put(DeviceInfoSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(DeviceInfoSettings.class.getName()),
                        NO_DATA_RES_ID,
                        DeviceInfoSettings.class.getName(),
                        R.drawable.aurora_settings_about_normal));
        
        
        /*
         *个性化
         */
        sResMap.put(ThemeFragment.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(ThemeFragment.class.getName()),
                        R.xml.aurora_theme_settings,
                        ThemeFragment.class.getName(),
                        R.drawable.aurora_settings_wallpaper));

        // add hotknot NFC {
/*        sResMap.put(HotKnotSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(HotKnotSettings.class.getName()),
                        NO_DATA_RES_ID,
                        HotKnotSettings.class.getName(),
                        R.drawable.ic_settings_hotknot));*/

/*        sResMap.put(NfcSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(NfcSettings.class.getName()),
                        NO_DATA_RES_ID,
                        NfcSettings.class.getName(),
                        R.drawable.ic_settings_home));*/
        // }

        /// M: add for mtk feature(Settings is an entrance , has its separate apk,
        /// such as schedule power on/off) search function {@
        sResMap.put(SearchExt.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SearchExt.class.getName()),
                        NO_DATA_RES_ID,
                        SearchExt.class.getName(),
                        R.mipmap.aurora_ic_launcher_settings));
        /// @}
        
       /* sResMap.put(AudioProfileSettings.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(AudioProfileSettings.class.getName()),
                        NO_DATA_RES_ID,
                        AudioProfileSettings.class.getName(),
                        R.drawable.ic_settings_notifications));*/
        
        /*sResMap.put(SoundEnhancement.class.getName(),
                new SearchIndexableResource(
                        Ranking.getRankForClassName(SoundEnhancement.class.getName()),
                        NO_DATA_RES_ID,
                        SoundEnhancement.class.getName(),
                        R.drawable.ic_settings_notifications));*/
        
        
        
        
    }

    private SearchIndexableResources() {
    }

    public static int size() {
        return sResMap.size();
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}

package com.aurora.puremanager.utils;

import android.content.pm.ApplicationInfo;

/**
 * 
 File Description: Filter class to filter third applications and can be installed in SD card applications
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-16 Change List:
 */
public class AppFilterUtil {
    /**
     * Application Filter interface, to filter application by defined condition.
     */
    public static interface AppFilter {
        public void init();

        public boolean filterApp(ApplicationInfo info);
    }

    /**
     * Filter Third Party Applications.
     */
    public static final AppFilter THIRD_PARTY_FILTER = new AppFilter() {
        public void init() {
        }

        @Override
        public boolean filterApp(ApplicationInfo info) {
            if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return true;
            }
            return false;
        }
    };
    /**
     * Filter Applications that can be moved to SDCARD.
     */
    public static final AppFilter ON_SD_CARD_FILTER = new AppFilter() {
        final CanBeOnSdCardChecker mCanBeOnSdCardChecker = new CanBeOnSdCardChecker();

        public void init() {
            mCanBeOnSdCardChecker.init();
        }

        @Override
        public boolean filterApp(ApplicationInfo info) {
            return mCanBeOnSdCardChecker.check(info);
        }
    };
}

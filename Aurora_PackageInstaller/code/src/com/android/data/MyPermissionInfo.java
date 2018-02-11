package com.android.data;

import android.content.pm.PermissionInfo;

public class MyPermissionInfo extends PermissionInfo {
        public CharSequence mLabel;

        /**
         * PackageInfo.requestedPermissionsFlags for the new package being installed.
         */
        public int mNewReqFlags;

        /**
         * PackageInfo.requestedPermissionsFlags for the currently installed
         * package, if it is installed.
         */
        public int mExistingReqFlags;

        /**
         * True if this should be considered a new permission.
         */
        public boolean mNew;

        public MyPermissionInfo() {
        }

        public MyPermissionInfo(PermissionInfo info) {
            super(info);
        }

        public MyPermissionInfo(MyPermissionInfo info) {
            super(info);
            mNewReqFlags = info.mNewReqFlags;
            mExistingReqFlags = info.mExistingReqFlags;
            mNew = info.mNew;
        }
    }

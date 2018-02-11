/*
**
** Copyright 2007, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**     http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package com.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.android.data.MyPermissionInfo;

/**
 * This class contains the SecurityPermissions view implementation.
 * Initially the package's advanced or dangerous security permissions
 * are displayed under categorized
 * groups. Clicking on the additional permissions presents
 * extended information consisting of all groups and permissions.
 * To use this view define a LinearLayout or any ViewGroup and add this
 * view by instantiating AppSecurityPermissions and invoking getPermissionsView.
 * 
 * {@hide}
 */
public class AppSecurityPermissions {
    private final static String TAG = "AppSecurityPermissions";
    private final static boolean localLOGV = false;
    private Context mContext;
    private PackageManager mPm;
    private PackageInfo mInstalledPackageInfo;
    private final PermissionInfoComparator mPermComparator;
    private List<MyPermissionInfo> mPermsList;
    
    final ArrayList<MyPermissionInfo> mNewPermissions = new ArrayList<MyPermissionInfo>();
    final ArrayList<MyPermissionInfo> mAllPermissions = new ArrayList<MyPermissionInfo>();

    public AppSecurityPermissions(Context context, PackageInfo info) {
        mContext = context;
        mPm = mContext.getPackageManager();
        mPermComparator = new PermissionInfoComparator();
        mPermsList = new ArrayList<MyPermissionInfo>();
        Set<MyPermissionInfo> permSet = new HashSet<MyPermissionInfo>();
        if(info == null) {
            return;
        }

        PackageInfo installedPkgInfo = null;
        if (info.requestedPermissions != null) {
            try {
                installedPkgInfo = mPm.getPackageInfo(info.packageName,PackageManager.GET_PERMISSIONS);
            } catch (NameNotFoundException e) {
            }
            extractPerms(info, permSet, installedPkgInfo);
        }
        // Get permissions related to  shared user if any
        if (info.sharedUserId != null) {
            int sharedUid;
            try {
                sharedUid = mPm.getUidForSharedUser(info.sharedUserId);
                getAllUsedPermissions(sharedUid, permSet);
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Could'nt retrieve shared user id for:"+info.packageName);
            }
        }
        // Retrieve list of permissions
        for (MyPermissionInfo tmpInfo : permSet) {
            mPermsList.add(tmpInfo);
        }
        setPermissions(mPermsList);
    }
    
    public PackageInfo getInstalledPackageInfo() {
        return mInstalledPackageInfo;
    }

    private void getAllUsedPermissions(int sharedUid, Set<MyPermissionInfo> permSet) {
        String sharedPkgList[] = mPm.getPackagesForUid(sharedUid);
        if(sharedPkgList == null || (sharedPkgList.length == 0)) {
            return;
        }
        for(String sharedPkg : sharedPkgList) {
            getPermissionsForPackage(sharedPkg, permSet);
        }
    }
    
    private void getPermissionsForPackage(String packageName, 
            Set<MyPermissionInfo> permSet) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = mPm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Couldn't retrieve permissions for package:"+packageName);
            return;
        }
        if ((pkgInfo != null) && (pkgInfo.requestedPermissions != null)) {
            extractPerms(pkgInfo, permSet, pkgInfo);
        }
    }

    private void extractPerms(PackageInfo info, Set<MyPermissionInfo> permSet,
            PackageInfo installedPkgInfo) {
        String[] strList = info.requestedPermissions;
        int[] flagsList = info.requestedPermissionsFlags;
        if ((strList == null) || (strList.length == 0)) {
            return;
        }
        mInstalledPackageInfo = installedPkgInfo;
        for (int i=0; i<strList.length; i++) {
            String permName = strList[i];
            // If we are only looking at an existing app, then we only
            // care about permissions that have actually been granted to it.
            if (installedPkgInfo != null && info == installedPkgInfo) {
                if ((flagsList[i]&PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0) {
                    continue;
                }
            }
            try {
                PermissionInfo tmpPermInfo = mPm.getPermissionInfo(permName, 0);
                if (tmpPermInfo == null) {
                    continue;
                }
                int existingIndex = -1;
                if (installedPkgInfo != null
                        && installedPkgInfo.requestedPermissions != null) {
                    for (int j=0; j<installedPkgInfo.requestedPermissions.length; j++) {
                        if (permName.equals(installedPkgInfo.requestedPermissions[j])) {
                            existingIndex = j;
                            break;
                        }
                    }
                }
                final int existingFlags = existingIndex >= 0 ?
                        installedPkgInfo.requestedPermissionsFlags[existingIndex] : 0;
                if (!isDisplayablePermission(tmpPermInfo, flagsList[i], existingFlags)) {
                    // This is not a permission that is interesting for the user
                    // to see, so skip it.
                    continue;
                }

                final boolean newPerm = installedPkgInfo != null
                        && (existingFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) == 0;
                MyPermissionInfo myPerm = new MyPermissionInfo(tmpPermInfo);
                myPerm.mNewReqFlags = flagsList[i];
                myPerm.mExistingReqFlags = existingFlags;
                // This is a new permission if the app is already installed and
                // doesn't currently hold this permission.
                myPerm.mNew = newPerm;
                permSet.add(myPerm);
            } catch (NameNotFoundException e) {
                Log.i(TAG, "Ignoring unknown permission:"+permName);
            }
        }
    }

    public ArrayList<MyPermissionInfo> getPermissionList() {	
    	return mAllPermissions;
    }
    
    public ArrayList<MyPermissionInfo> getNewPermissionList() {
    	return mNewPermissions;
    }

    private boolean isDisplayablePermission(PermissionInfo pInfo, int newReqFlags,
            int existingReqFlags) {
        final int base = pInfo.protectionLevel & PermissionInfo.PROTECTION_MASK_BASE;
        // Dangerous and normal permissions are always shown to the user.
        if (base == PermissionInfo.PROTECTION_DANGEROUS ||
                base == PermissionInfo.PROTECTION_NORMAL) {
            return true;
        }
        // Development permissions are only shown to the user if they are already
        // granted to the app -- if we are installing an app and they are not
        // already granted, they will not be granted as part of the install.
        if ((existingReqFlags&PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                && (pInfo.protectionLevel & PermissionInfo.PROTECTION_FLAG_DEVELOPMENT) != 0) {
            if (localLOGV) Log.i(TAG, "Special perm " + pInfo.name
                    + ": protlevel=0x" + Integer.toHexString(pInfo.protectionLevel));
            return true;
        }
        return false;
    }
    
    private static class PermissionInfoComparator implements Comparator<MyPermissionInfo> {
        private final Collator sCollator = Collator.getInstance();
        PermissionInfoComparator() {
        }
        public final int compare(MyPermissionInfo a, MyPermissionInfo b) {
            return sCollator.compare(a.mLabel, b.mLabel);
        }
    }

    private void addPermToList(ArrayList<MyPermissionInfo> permList,
    		MyPermissionInfo pInfo) {
        if (pInfo.mLabel == null) {
            pInfo.mLabel = pInfo.loadLabel(mPm);
        }
        int idx = Collections.binarySearch(permList, pInfo, mPermComparator);
        if(localLOGV) Log.i(TAG, "idx="+idx+", list.size="+permList.size());
        if (idx < 0) {
            idx = -idx-1;
            permList.add(idx, pInfo);
        }
    }

    private void setPermissions(List<MyPermissionInfo> permList) {
        if (permList != null) {
            // First pass to group permissions
            for (MyPermissionInfo pInfo : permList) {
                if(localLOGV) Log.i(TAG, "Processing permission:"+pInfo.name);
                if(!isDisplayablePermission(pInfo, pInfo.mNewReqFlags, pInfo.mExistingReqFlags)) {
                    if(localLOGV) Log.i(TAG, "Permission:"+pInfo.name+" is not displayable");
                    continue;
                }
                
                pInfo.mLabel = pInfo.loadLabel(mPm);
	              if (pInfo.mNew) {
	                  addPermToList(mNewPermissions, pInfo);
	              }else{
	            	  addPermToList(mAllPermissions, pInfo);	            	  
	              }
            }
        }
    }
}

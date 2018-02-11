package com.aurora.puremanager.permission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.R.integer;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

import com.aurora.puremanager.R;

/**
 * 
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-4-27 Change List:
 */
public class PermissionsInfo {
    private static final Object LOCK = new Object();
    private static PermissionsInfo sInstance;
    public static int sCurrentId = -1;
    static boolean isPermissionApp = true;
    static int mTrust = 0;
    public final List<GNPermissionInfo> mPermissions = new ArrayList<GNPermissionInfo>();

    public static final String URI = "content://com.amigo.settings.PermissionProvider/permissions";
    public static final String TRUST_URI = "content://com.amigo.settings.PermissionProvider/whitelist";
    public static final int STATUS_PERMIT = 1;
    public static final int STATUS_REQUEST = 0;
    public static final int STATUS_PROHIBITION = -1;
    private String mDefaultGrpName = "DefaultGrp";
    static final String[] PERMISSIONS = new String[] {"android.permission.CALL_PHONE",
            "android.permission.SEND_SMS", "android.permission.SEND_SMS_MMS",
            "android.permission.READ_CALL_LOG", "android.permission.READ_SMS",
            "android.permission.READ_SMS_MMS", "android.permission.READ_CONTACTS",
            "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION",
            "android.permission.READ_PHONE_STATE", "android.permission.WRITE_SMS",
            "android.permission.WRITE_SMS_MMS", "android.permission.WRITE_CONTACTS",
            "android.permission.WRITE_CALL_LOG", "android.permission.RECORD_AUDIO",
            "android.permission.CAMERA", "android.permission.CHANGE_NETWORK_STATE",
            "android.permission.CHANGE_WIFI_STATE", "android.permission.BLUETOOTH_ADMIN",
            "android.permission.BLUETOOTH", "android.permission.NFC"};

    private static final String[] PERMISSION_GROUPS = new String[] {"android.permission-group.COST_MONEY",
            "android.permission-group.COST_MONEY", "android.permission-group.MESSAGES",
            "android.permission-group.PERSONAL_INFO", "android.permission-group.MESSAGES",
            "android.permission-group.MESSAGES", "android.permission-group.PERSONAL_INFO",
            "android.permission-group.LOCATION", "android.permission-group.LOCATION",
            "android.permission-group.PHONE_CALLS", "android.permission-group.MESSAGES",
            "android.permission-group.MESSAGES", "android.permission-group.PHONE_CALLS",
            "android.permission-group.PERSONAL_INFO", "android.permission-group.HARDWARE_CONTROLS",
            "android.permission-group.HARDWARE_CONTROLS", "android.permission-group.COST_MONEY",
            "android.permission-group.HARDWARE_CONTROLS", "android.permission-group.BLUETOOTH_NETWORK",
            "android.permission-group.BLUETOOTH_NETWORK", "android.permission-group.HARDWARE_CONTROLS"};

    /*static final int[] DRAWABLE_ID = new int[] {R.drawable.icon_call_phone, R.drawable.icon_send_mms,
            R.drawable.icon_send_sms_mms, R.drawable.icon_read_calllog, R.drawable.icon_read_mms,
            R.drawable.icon_read_sms_mms, R.drawable.icon_read_contacts, R.drawable.icon_read_location,
            R.drawable.icon_def_brower, R.drawable.icon_read_phone, R.drawable.icon_del_sms,
            R.drawable.icon_del_mms, R.drawable.icon_del_contacts, R.drawable.icon_del_calllog,
            R.drawable.icon_read_soundrecord, R.drawable.icon_camera, R.drawable.icon_use_net,
            R.drawable.icon_use_wlan, R.drawable.icon_use_bluetooth, R.drawable.icon_use_bluetooth_pairing,
            R.drawable.icon_use_nfc};*/

    static final int[] IMPORTANT_PERMISSION_STATE = new int[] {0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0,
            0, 1, 0, 0, 0, 0, 0, 0};

    public final HashMap<String, Integer> mPerMissionMap = new HashMap<String, Integer>();

    static String sSavedPackageName;

    public static PermissionsInfo getInstance() {
        synchronized (LOCK) {
            if (sInstance == null) {
                sInstance = new PermissionsInfo();
            }
            return sInstance;
        }
    }

    private PermissionsInfo() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            GNPermissionInfo pmisInfo = new GNPermissionInfo(PERMISSIONS[i], PERMISSION_GROUPS[i]);
            mPermissions.add(pmisInfo);
            mPerMissionMap.put(PERMISSIONS[i], i);
        }
    }


    public void init(Context context) {
        ApplicationsInfo applications = ApplicationsInfo.getInstance();
        applications.loadPermissionsAppEntries(context);
        // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
        ContentResolver cr = context.getContentResolver();
        initPermStatusHashSet(cr);
        for (int i = 0; i < mPermissions.size(); i++) {
            reloadPmisById(context, i, false);
        }
        // Gionee <liuyb> <2014-5-6> modify for CR01237582 end
    }

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 begiin
    public void reloadPmisById(Context context, int id, boolean needInit) {
        DebugUtil.v("reloadPmisById: " + id);
        // Gionee <lihq> <2013-7-19> modify for CR00837269 begin
        if (id <= -1) {
            return;
        }
        // Gionee <lihq> <2013-7-19> modify for CR00837269 end
        ApplicationsInfo applications = ApplicationsInfo.getInstance();
        PackageManager pm = context.getPackageManager();
        ContentResolver cr = context.getContentResolver();
        GNPermissionInfo pmisInfo = mPermissions.get(id);
        pmisInfo.getList().clear();
        // Gionee <lihq> <2013-6-19> modify for CR00826735 begin
        DebugUtil.v("ApplicationsInfo is ready : " + applications.isReady());
        if (!applications.isReady()) {
            applications.loadPermissionsAppEntries(context);
        }

        if (needInit) {
            initPermStatusHashSet(cr);
        }
        // Gionee <lihq> <2013-6-19> modify for CR00826735 end
        for (int j = 0; j < applications.mPermissionsAppEntries.size(); j++) {
            ItemInfo appInfo = applications.mPermissionsAppEntries.get(j);
            Integer status = getPermStatus(cr, pmisInfo.getPermissionName(),
                    appInfo.mApplicationInfo.packageName);
            if (status == null) {
                continue;
            } else {
                appInfo.setStauts(status.intValue());
                pmisInfo.addList(appInfo); // add app that own this permission to list
            }
        }
        // Gionee <liuyb> <2014-08-08> add for CR01348048 begin
        if (!"android.permission.SEND_SMS".equals(pmisInfo.getPermissionName())) {
            return;
        }
        // Gionee <liuyb> <2014-08-08> add for CR01348048 end
        
        // Gionee <liuyb> <2013-10-18> add for CR00905594 begin
        List<ItemInfo> lsAppInfo = pmisInfo.getList();
        for (ItemInfo appinfo : lsAppInfo) {
            if ("com.mediatek.smsreg".equals(appinfo.mApplicationInfo.packageName)) {
                return;
            }
        }
        ApplicationInfo info;
        try {
            info = pm.getApplicationInfo("com.mediatek.smsreg", 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        ItemInfo appInfo = new ItemInfo(info);
        Integer status = getPermStatus(cr, pmisInfo.getPermissionName(), appInfo.mApplicationInfo.packageName);
        // Gionee <jianghuan> <2014-04-17> modify for CR01200613 begin
        // appInfo.setIcon(HelperUtils.loadIcon(context, info));
        appInfo.setPackageName(info.packageName);
        // Gionee <jianghuan> <2014-04-17> modify for CR01200613 end
        appInfo.setName(HelperUtils_per.loadLabel(context, info));
        if (status != null) {
            appInfo.setStauts(status.intValue());
        } else {
            appInfo.setStauts(0);
        }
        pmisInfo.addList(appInfo);
        // Gionee <liuyb> <2013-10-18> add for CR00905594 end
    }

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 end

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    public void setPermission(final Context context, final String pkgName, int pos, final int status,
            ItemInfo appInfo) {
        final GNPermissionInfo pmisInfo = mPermissions.get(pos);
        DebugUtil.v("set Permission: " + pmisInfo.getPermissionName() + " for package " + pkgName + " to "
                + status);
        int value = status;
        if (updatePermission(context, pmisInfo.getPermissionName(), pkgName, status)) {
            DebugUtil.v("update permission successed");
        } else {
            DebugUtil.v("db has not this permission for this package...will to add");
            new Thread(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    insertPermissinDB(context, pkgName, pmisInfo, status);
                }
            }).start();
        }
//        pmisInfo.swap(appInfo);  
    }

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
    public void addPackage(Context context, String pkgName) {
        ContentResolver cr = context.getContentResolver();
        ItemInfo appInfo = ApplicationsInfo.getInstance().mPermissionsMapEntries.get(pkgName);
        if (appInfo == null) {
            return;
        }
        initPermStatusHashSet(cr);
        for (int i = 0; i < mPermissions.size(); i++) {
            GNPermissionInfo pmisInfo = mPermissions.get(i);
            Integer status = getPermStatus(cr, pmisInfo.getPermissionName(), pkgName);
            if (status != null) {
                appInfo.setStauts(status.intValue());
                pmisInfo.addList(appInfo);
            }
        }
    }

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 end

    public void removePackage(String pkgName) {
        ItemInfo appInfo = ApplicationsInfo.getInstance().mMapEntries.get(pkgName);
        for (int i = 0; i < mPermissions.size(); i++) {
            GNPermissionInfo pmisInfo = mPermissions.get(i);
            pmisInfo.removePackage(appInfo);
        }
    }

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin
    private void insertPermissinDB(Context context, String packageName, GNPermissionInfo pmisInfo, int status) {
        PackageManager mPm = context.getPackageManager();
        List<PermissionInfo> mPermsList = new ArrayList<PermissionInfo>();
        Set<PermissionInfo> permSet = new HashSet<PermissionInfo>();
        PackageInfo pkgInfo;
        try {
            pkgInfo = mPm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            DebugUtil.v("Could'nt retrieve permissions for package:" + packageName);
            return;
        }
        // Extract all user permissions
        if ((pkgInfo.applicationInfo != null) && (pkgInfo.applicationInfo.uid != -1)) {
            getAllUsedPermissions(context, pkgInfo.applicationInfo.uid, permSet);
        }
        for (PermissionInfo tmpInfo : permSet) {
            mPermsList.add(tmpInfo);
        }
        ContentResolver cr = context.getContentResolver();
        for (PermissionInfo pInfo : mPermsList) {
            if (!HelperUtils_per.mCheckPermissions.contains(pInfo.name)) {
                continue;
            }
            String grpName = (pInfo.group == null) ? mDefaultGrpName : pInfo.group;
            ContentValues cv = new ContentValues();
            cv.put("packagename", packageName);
            cv.put("permission", pInfo.name);
            cv.put("permissiongroup", grpName);
            if (pInfo.name.equals(pmisInfo.getPermissionName())) {
                cv.put("status", status);
            } else {
                cv.put("status", STATUS_PERMIT); // default value = 1
            }
            DebugUtil.v(" PermissionsInfo insertPermissinDB cv : "+cv);
            cr.insert(Uri.parse(URI), cv);
        }
        Intent intent = new Intent();
        intent.setAction("com.gionee.action.UPDATE_PERM_DB");
        context.sendBroadcast(intent);
    }

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end

    private void getAllUsedPermissions(Context context, int sharedUid, Set<PermissionInfo> permSet) {
        String[] sharedPkgList = context.getPackageManager().getPackagesForUid(sharedUid);
        if (sharedPkgList == null || (sharedPkgList.length == 0)) {
            return;
        }
        for (String sharedPkg : sharedPkgList) {
            getPermissionsForPackage(context, sharedPkg, permSet);
        }
    }

    private void getPermissionsForPackage(Context context, String packageName, Set<PermissionInfo> permSet) {
        PackageInfo pkgInfo;
        try {
            pkgInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (NameNotFoundException e) {
            DebugUtil.v("Could'nt retrieve permissions for package:" + packageName);
            return;
        }
        if ((pkgInfo != null) && (pkgInfo.requestedPermissions != null)) {
            extractPerms(context, pkgInfo.requestedPermissions, permSet);
        }
    }

    private void extractPerms(Context context, String[] strList, Set<PermissionInfo> permSet) {
        if ((strList == null) || (strList.length == 0)) {
            return;
        }
        for (String permName : strList) {
            try {
                PermissionInfo tmpPermInfo = context.getPackageManager().getPermissionInfo(permName, 0);
                if (tmpPermInfo != null) {
                    permSet.add(tmpPermInfo);
                }
            } catch (NameNotFoundException e) {
                DebugUtil.v("Ignoring unknown permission:" + permName);
            }
        }
    }

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 begin

    public boolean updatePermission(Context context, String permission, String pkgName, int status) {
        ContentValues cv = new ContentValues();
        cv.put("status", status);
        int result = context.getContentResolver().update(Uri.parse(URI), cv,
                "packagename = ? and permission = ?", new String[] {pkgName, permission});
        if (result > 0) {
            return true;
        }
        return false;
    }

    public boolean updatePermission(Context context, ContentValues cv, String pkgName) {
        int result = context.getContentResolver().update(Uri.parse(URI), cv, " packagename =? ",
                new String[] {pkgName});
        if (result > 0) {
            return true;
        }
        return false;
    }

    public boolean updatePermissionInPartialTrust(Context context, ContentValues cv, String pkgName) {
        String temp = HelperUtils_per.mImportantPermissions.toString();
        temp = temp.replace('[', '(');
        temp = temp.replace(']', ')');
        temp = temp.replaceAll(",", "','").replaceAll(" ", "");

        int result = context.getContentResolver().update(
                Uri.parse(URI),
                cv,
                "permission not in ('" + temp.substring(1, temp.length() - 1) + "') and  packagename = '"
                        + pkgName + "'", null);
        if (result > 0) {
            return true;
        }
        return false;
    }

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 begin
    private Integer getPermStatus(ContentResolver cr, String permission, String pkgName) {
        return mPermStatusHashMap.get(pkgName + "_" + permission);
    }

    private Map<String, Integer> mPermStatusHashMap = new HashMap<String, Integer>();

    void initPermStatusHashSet(ContentResolver cr) {

        mPermStatusHashMap.clear();
        Cursor c = null;
        try {
            c = cr.query(Uri.parse(URI), new String[] {"packagename", "permission", "status"}, null, null,
                    null);
            if (c != null && c.moveToFirst()) {
                do {
                    String packagename = c.getString(0);
                    String permission = c.getString(1);
                    String status = c.getString(2);
                    mPermStatusHashMap.put(packagename + "_" + permission, Integer.parseInt(status));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null && c.isClosed()) {
                c.close();
                c = null;
            }
        }
    }

    // Gionee <liuyb> <2014-5-6> modify for CR01237582 end

    public boolean updateTrust(Context context, String pkgName, int status) {

        ContentValues cv = new ContentValues();
        cv.put("status", status);
        int result = context.getContentResolver().update(Uri.parse(TRUST_URI), cv, "packagename = ?",
                new String[] {pkgName});
        if (result > 0) {
            return true;
        }
        return false;
    }

    /*    private boolean updatePermission(Context context, String permission, String pkgName, boolean status) {
            ContentValues cv = new ContentValues();
            cv.put("status", status ? 1 : 0);
            int result = context.getContentResolver().update(Uri.parse(URI), cv,
                    "packagename = ? and permission = ?",
                    new String[]{pkgName, permission });
            if (result > 0) {
                return true;
            }
            return false; 
        }
        
        private boolean isEnable(ContentResolver cr, String permission, String pkgName) {
            boolean result = false;
            Cursor queryResult = cr.query(Uri.parse(URI), new String[]{"status" },
                    "packagename = ? and permission = ? and status = ?", new String[]{pkgName,
                        permission, "0" }, null);
            result = queryResult.getCount() <= 0;
            queryResult.close();
            DebugUtil.e(result, "permission: " + permission + " allow for package: " + pkgName);
            return result;
        }*/

    // Gionee <jingjc> <2013-9-17> modify for CR00904574 end

    // Gionee <liuyb> <2014-3-3> add for CR01078882 begin
    public void releaseRes() {
        if (mPermissions != null) {
            mPermissions.clear();
        }
        sInstance = null;
    }
    // Gionee <liuyb> <2014-3-3> add for CR01078882 end
}

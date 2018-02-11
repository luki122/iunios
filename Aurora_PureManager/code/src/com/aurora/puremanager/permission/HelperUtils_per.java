package com.aurora.puremanager.permission;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.aurora.puremanager.R;

//import com.android.settings.R;
// Gionee <liuyb> <2014-7-11> modify for CR01316210 begin
//import com.example.test.permission.AsyncIconImageLoader;
// Gionee <liuyb> <2014-7-11> modify for CR01316210 end

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
//import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;

//import com.android.internal.app.IMediaContainerService;

import android.graphics.Bitmap; 
import android.graphics.drawable.BitmapDrawable; 
import android.graphics.drawable.Drawable; 
import android.view.View; 
import android.view.ViewGroup; 
import android.widget.AdapterView; 
import android.widget.ImageView;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
//import android.os.SystemProperties;
import android.text.format.Formatter;
import android.util.AndroidRuntimeException;
import android.util.Log;
/**
 *   
 File Description:
 * 
 * @author: Gionee-lihq
 * @see: 2013-1-17 Change List:
 */
public class HelperUtils_per {
    private static final String DEFAULT_CONTAINER_PACKAGE = "com.android.defcontainer";

    public static final ComponentName DEFAULT_CONTAINER_COMPONENT = new ComponentName(
            DEFAULT_CONTAINER_PACKAGE, "com.android.defcontainer.DefaultContainerService");

    public  static boolean mCheckFlag = false; 
    
    private final static String TAG = "HelperUtils";
    
    
    /**
     * To get application info by package name
     * 
     * @param context
     * @param pkgName
     * @return
     */


  public static final boolean filterThirdPartyApp(ApplicationInfo info) {
            if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true;
            } else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                return true;
            }
            return false;
        }

  public static void unbindDrawables(View view) {
        try {
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Drawable drawable = imageView.getDrawable();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                drawable = imageView.getBackground();
                if (drawable != null && drawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        // bitmap.recycle();
                        bitmap = null;
                    }
                    drawable = null;
                }
                imageView.setImageDrawable(null);
                imageView.setBackgroundDrawable(null);
            }

            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
                view.setBackgroundResource(0);
            }

            if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                ((ViewGroup) view).removeAllViews();
            }
        } catch (Exception e) {
            Log.e("Util", "unbindDrawables exception --->", e);
        }
    }
  
    public static ApplicationInfo getApplicationInfo(Context context, String pkgName) {
        ApplicationInfo result = null;
        try {
            result = context.getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    /**
     * To get application of current installed application
     * 
     * @param context
     * @return
     */
    public static List<ApplicationInfo> getThirdApplicationInfo(Context context) {
        List<ApplicationInfo> mApplications = new ArrayList<ApplicationInfo>();
        try {
            mApplications = context.getPackageManager().getInstalledApplications(
                    PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
        } catch (AndroidRuntimeException ar) {
            Log.i(TAG, "AndroidRuntimeException", ar);
        } catch (Exception e) {
            Log.i(TAG, "Exception", e);
        }
        for (int i = 0; i < mApplications.size(); i++) {
            final ApplicationInfo info = mApplications.get(i);

            if (!filterThirdPartyApp(info) || loadLabel(context, info).equalsIgnoreCase("com.android.nfc.tests")) {
                mApplications.remove(i);
                i--;
                continue;
            }
        }
        return mApplications;
    }

    public static String getSizeStr(Context context, long size) {
        if (size >= 0) {
            return Formatter.formatFileSize(context, size);
        }
        return null;
    }

    public static String loadLabel(Context context, ApplicationInfo info) {
        String result = info.loadLabel(context.getPackageManager()).toString();
//        if (result == null) {
        if (result.equals("")) {
            result = info.packageName;
        }
        return result;
    }

    /**
     * To convert String {@value src} to "src(num)"
     * 
     * @param src
     * @param num
     * @return
     */
    
    public static Drawable loadIcon(Context context, ApplicationInfo info) {
        // Gionee <liuyb> <2014-7-11> modify for CR01316210 begin
        Drawable result = null;
        try {
            result = new AsyncIconImageLoader().getIconFromSDCard(info.packageName);
            if (result == null) {
                result = info.loadIcon(context.getPackageManager());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Gionee <liuyb> <2014-7-11> modify for CR01316210 end
        if (result == null) {
            result = context.getResources().getDrawable(
                    R.drawable.sym_app_on_sd_unavailable_icon);
        }
        return result;
    }

    
    public static final HashSet<String> mCheckPermissions = new HashSet<String>();
    static {
        mCheckPermissions.add("android.permission.CALL_PHONE");
        mCheckPermissions.add("android.permission.SEND_SMS");
        mCheckPermissions.add("android.permission.SEND_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_SMS");
        mCheckPermissions.add("android.permission.READ_SMS_MMS");
        mCheckPermissions.add("android.permission.READ_CONTACTS");
        mCheckPermissions.add("android.permission.READ_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.READ_CALL_LOG");
        mCheckPermissions.add("android.permission.WRITE_SMS");
        mCheckPermissions.add("android.permission.WRITE_SMS_MMS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS");
        mCheckPermissions.add("android.permission.WRITE_CONTACTS_CALLS");
        mCheckPermissions.add("android.permission.WRITE_CALL_LOG");
        mCheckPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        mCheckPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        mCheckPermissions.add("android.permission.RECORD_AUDIO");
        mCheckPermissions.add("android.permission.READ_PHONE_STATE");
        mCheckPermissions.add("android.permission.CAMERA");
        mCheckPermissions.add("android.permission.BLUETOOTH_ADMIN");
        mCheckPermissions.add("android.permission.BLUETOOTH");
        mCheckPermissions.add("android.permission.CHANGE_WIFI_STATE");
        mCheckPermissions.add("android.permission.CHANGE_NETWORK_STATE");
        mCheckPermissions.add("android.permission.NFC");
    }
    
    public static final HashSet<String> mImportantPermissions = new HashSet<String>();
    static {
        mImportantPermissions.add("android.permission.READ_SMS");
        mImportantPermissions.add("android.permission.READ_SMS_MMS");
        mImportantPermissions.add("android.permission.READ_CONTACTS");
        mImportantPermissions.add("android.permission.READ_CONTACTS_CALLS");
        mImportantPermissions.add("android.permission.READ_CALL_LOG");
        mImportantPermissions.add("android.permission.ACCESS_FINE_LOCATION");
        mImportantPermissions.add("android.permission.ACCESS_COARSE_LOCATION");
        mImportantPermissions.add("android.permission.RECORD_AUDIO");
    }
    
    
   public static String getStringDate(long currentTime ) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(new Date(currentTime));
        return dateString;
    }
	 
}

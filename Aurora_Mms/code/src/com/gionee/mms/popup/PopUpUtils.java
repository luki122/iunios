/*
 *
 * Copyright (C) 2012 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.popup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import aurora.preference.AuroraPreferenceManager;
import android.text.SpannableStringBuilder;
import android.util.Log;

import com.android.mms.util.SmileyParser;
import gionee.provider.GnTelephony;
//Gionee <zhouyj> <2013-06-25> add for CR00829094 begin
import java.util.Iterator;
import android.content.Intent;
import android.content.pm.ResolveInfo;
//Gionee <zhouyj> <2013-06-25> add for CR00829094 end

public class PopUpUtils {
    public static boolean mPopUpShowing = false;
    public static final String POPUP_ACTION = "android.intent.action.PopUpMmsActivity";
    public static final String POPUP_INFO_ADDRESS = "address";
    public static final String POPUP_INFO_DATE = "date";
    public static final String POPUP_INFO_BODY = "body";
    public static final String POPUP_INFO_SIM_ID = GnTelephony.GN_SIM_ID;//"sim_id";
    public static final String POPUP_INFO_MSG_TYPE = "msg_type";
    public static final String POPUP_INFO_MSG_URI = "msg_uri";
    public static final String POPUP_INFO_THREAD_ID = "thread_id";

    public static final int POPUP_TYPE_SMS = 1;
    public static final int POPUP_TYPE_MMS = 2;
    public static final int POPUP_TYPE_PUSH = 3;

    public static final Uri TABLE_THREADS_URI = Uri.parse("content://cb/threads");
    public static final Uri TABLE_CANONICAL_ADDRESS_URI = Uri.parse("content://mms-sms/canonical-addresses");
    public static final Uri TABLE_MMS_SMS_PART_URI = Uri.parse("content://mms-sms/conversations");

    private static ActivityManager mActivityManager = null;
    private static SharedPreferences mSharedPreferences = null;
    private static KeyguardManager mKeyguardManager = null;

    public static final String MSG_INFO_RECEIVER_ACTION = "android.intent.action.PopUpMsgActivity.MsgInfoReceiver";

    public static final String DATE_SECOND_TYPE = "yyyy-MM-dd HH:mm:ss";
    //Gionee <zhouyj> <2013-06-25> modify for CR00829094 begin
    public static boolean isLauncherView(Context context) {
        if (null == mActivityManager) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        List<RunningTaskInfo> list = mActivityManager.getRunningTasks(1);
        String topApp = list.get(0).baseActivity.getPackageName();
        List<ResolveInfo> launcherApps = getLauncherApps(context);
        Iterator it = launcherApps.iterator();
        ResolveInfo info;
        while(it.hasNext()) {
            info = (ResolveInfo) it.next();
            if (topApp.equals(info.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }
    
    private static List<ResolveInfo> getLauncherApps(Context context) {
        Intent i = new Intent(Intent.ACTION_MAIN, null);
        i.addCategory(Intent.CATEGORY_HOME);
        return context.getPackageManager().queryIntentActivities(i, 0);
    }
    //Gionee <zhouyj> <2013-06-25> modify for CR00829094 end

    public static CharSequence formatMessage(String body) {
        SpannableStringBuilder bufMessageBody = new SpannableStringBuilder("");
        SmileyParser parser = SmileyParser.getInstance();
        bufMessageBody.append(parser.addSmileySpans(body));
        bufMessageBody.setSpan(null, 0, bufMessageBody.length(), 0);
        return bufMessageBody;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height= drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float)w / width);
        float scaleHeight = ((float)h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height, matrix, true);
        return new BitmapDrawable(newbmp);
    }

    public static boolean getPopNotfiSetting(Context context) {
        if (null == mSharedPreferences) {
            mSharedPreferences = AuroraPreferenceManager.getDefaultSharedPreferences(context);
        }
        boolean flag = mSharedPreferences.getBoolean("pref_key_enable_pop_notifications", false);
        Log.e("TEST", "default notifi setting : " + flag + " ....msg ring: " + mSharedPreferences.getBoolean("pref_key_enable_notifications", false));
        return flag;
    }

    public static boolean isLockScreen(Context context) {
        if (null == mKeyguardManager) {
            mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
        return mKeyguardManager.inKeyguardRestrictedInputMode();
    }

    public static boolean isMmsView(Context context) {
        if (null == mActivityManager) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        List<RunningTaskInfo> list = mActivityManager.getRunningTasks(1);
        return list.get(0).baseActivity.getPackageName().contains("mms");
    }
    

    public static String formatDate2String(String type, long seconds) {
        String mTime = null;
        SimpleDateFormat mFormat = new SimpleDateFormat(type);
        Date mDate = new Date(seconds);
        mTime = mFormat.format(mDate);
        return mTime;
    }

}

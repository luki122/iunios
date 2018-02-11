package com.aurora.puremanager.utils;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class AppStateUtil {
    /**
     * 是否在播放音乐
     * @param context
     * @return
     */
    public static boolean isPlayMusic(Context context) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return am.isMusicActive();
    }

    /**
     * 获取所有音乐类应用
     * @param context
     * @return
     */
    public static List<String> getMusicApps(Context context) {
        List<String> ls = new ArrayList<String>();
        try {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setDataAndType(Uri.parse("file:///android_asset/"), "audio/mpeg");
            List<ResolveInfo> riLists = context.getPackageManager().queryIntentActivities(intent,
                    PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
            for (ResolveInfo ri : riLists) {
                if (ri != null && ri.activityInfo != null) {
                    ls.add(ri.activityInfo.packageName);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ls;
    }

    /**
     * FM是否打开
     * @param context
     * @return
     */
    public static boolean isFmOn(Context context) {
        boolean flag = false;
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
//            flag = am.isFmActive();
//            Class cpClass = am.getClass();
//            Method method = cpClass.getDeclaredMethod("isFmActive");
//            flag = ((Boolean) method.invoke(am)).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 获取FM应用
     * @param context
     * @return
     */
    public static List<String> getFMApps(Context context) {
        List<String> ls = new ArrayList<String>();
        ls.add("com.caf.fmradio");
        ls.add("com.mediatek.FMRadio");
        return ls;
    }

    /**
     * 获取默认输入法信息
     *
     * @return InputMethodInfo
     */
    public static InputMethodInfo getDefInputMethod(Context context) {
        String defInput = android.provider.Settings.Secure.getString(context.getContentResolver(),
                "default_input_method");
        InputMethodManager mImm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mImis = mImm.getEnabledInputMethodList();
        for (int i = 0; i < mImis.size(); i++) {
            InputMethodInfo info = mImis.get(i);
            if (info.getId().equals(defInput)) {
                return info;
            }
        }
        return null;
    }
}

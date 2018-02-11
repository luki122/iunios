package com.aurora.account.util;

import android.content.Context;
import android.widget.Toast;

import com.aurora.account.AccountApp;

/**
 * Toast工具类
 * @author JimXia
 * @date 2014-4-22 上午10:23:01
 */
public class ToastUtil {
    private static final Context sContext = AccountApp.getInstance();
    
    public static void shortToast(String msg) {
        Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
    }
    
    public static void shortToast(int msgId) {
        Toast.makeText(sContext, msgId, Toast.LENGTH_SHORT).show();
    }
    
    public static void longToast(String msg) {
        Toast.makeText(sContext, msg, Toast.LENGTH_LONG).show();
    }
    
    public static void longToast(int msgId) {
        Toast.makeText(sContext, msgId, Toast.LENGTH_LONG).show();
    }
}

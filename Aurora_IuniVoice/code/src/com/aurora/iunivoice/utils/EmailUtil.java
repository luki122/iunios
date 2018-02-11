/**
 * 
 */
package com.aurora.iunivoice.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 根据邮箱地址用浏览器打开邮箱对应的登录页面
 * 
 * @author JimXia
 * @date 2015年1月5日 下午5:17:22
 */
public class EmailUtil {
    private static final String TAG = "EmailUtil";
    
    private static final Pattern sEmailHostPattern =
            Pattern.compile(".*@(.+?)\\..+");
    private static final HashMap<String, String> sEmail2EmailLoginPageMap = new HashMap<String, String>(7);
    static {
        sEmail2EmailLoginPageMap.put("163", "http://mail.163.com");
        sEmail2EmailLoginPageMap.put("126", "http://mail.126.com");
        sEmail2EmailLoginPageMap.put("qq", "http://mail.qq.com");
        sEmail2EmailLoginPageMap.put("139", "http://mail.10086.cn");
        sEmail2EmailLoginPageMap.put("189", "http://webmail30.189.cn");
        sEmail2EmailLoginPageMap.put("sina", "http://mail.sina.com");
        sEmail2EmailLoginPageMap.put("hotmail", "http://login.live.com");
    }
    
    private static String extractEmailHost(String emailAddress) {
        if (!TextUtils.isEmpty(emailAddress)) {
            Matcher m = sEmailHostPattern.matcher(emailAddress);
            if (m.find()) {
                return m.group(1);
            }
        }
        
        return null;
    }
    
    public static boolean openEmailLoginPageByBrowser(Context context, String emailAddress, String errorMsg) {
        Intent openEmailLoginPage = new Intent(Intent.ACTION_VIEW);
        openEmailLoginPage.addCategory(Intent.CATEGORY_BROWSABLE);
        
        String emailHost = extractEmailHost(emailAddress);
        Log.d(TAG, "Jim, email host: " + emailHost);
        
        if (!TextUtils.isEmpty(emailHost)) {
            String loginPage = sEmail2EmailLoginPageMap.get(emailHost);
            if (!TextUtils.isEmpty(loginPage)) {
                openEmailLoginPage.setData(Uri.parse(loginPage));
            }
        }
        if (openEmailLoginPage.getData() == null) {
            // 邮箱的登录界面URL没有解析出来，设置一个默认的data，不然不会打开浏览器
            openEmailLoginPage.setData(Uri.fromParts("http", "", ""));
        }
        boolean error = false;
        try {
            context.startActivity(openEmailLoginPage);
        } catch (Throwable t) {
            Log.e(TAG, "Open browser error.", t);
            error = true;
        }
        if (error && !TextUtils.isEmpty(errorMsg)) {
            ToastUtil.longToast(errorMsg);
        }
        
        return !error;
    }
}

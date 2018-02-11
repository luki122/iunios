package cn.com.xy.sms.sdk.ui.popu.web;

import android.app.Activity;
import android.webkit.WebView;

public interface IActivityParamForJS {

    public WebView getWebView();

    /**
     * get value by key
     * 
     * @param key
     * @return
     */
    public String getParamData(String key);

    public Activity getActivity();

    /**
     * Somehow the screen gets the current screen
     * 
     * @return
     */
    public int checkOrientation();
}

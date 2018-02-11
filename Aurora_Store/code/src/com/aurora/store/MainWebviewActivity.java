package com.aurora.store;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.aurora.store.pay.AliPay;
import com.aurora.store.util.Constants;
import com.aurora.store.util.SystemUtils;


public class MainWebviewActivity extends Activity {

    private static final String SCHEME_WTAI_MC = "wtai://wp/mc;";
    private WebView mainWebView = null;
    private static final String APP_CACAHE_DIRNAME = "/app_webview";

    private Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TestPay.pay(MainWebviewActivity.this);
                }
            }).start();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_webview);

        com.aurora.utils.SystemUtils.setStatusBarBackgroundTransparent(this);

        mainWebView = (WebView) findViewById(R.id.main_view);

        initWebView();

        //mainWebView.loadUrl("file:///android_asset/pay.html");
        mainWebView.loadUrl("http://m.iuni.com");
        //mainWebView.loadUrl("http://m.baidu.com");
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
    }

    private void initWebView() {
        WebSettings settings = mainWebView.getSettings();
        //fix CN error code
        settings.setDefaultTextEncodingName("GBK");
        //enable js
        settings.setJavaScriptEnabled(true);
        //设置 缓存模式
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        //settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 自适应屏幕
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        // 开启 DOM storage API 功能
        settings.setDomStorageEnabled(true);
        //开启 database storage API 功能  
        settings.setDatabaseEnabled(true);
        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
        //设置  Application Caches 缓存目录
        settings.setAppCachePath(cacheDirPath);
        //开启 Application Caches 功能
        //开启 database storage API 功能
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);

        //url jump inside webView
        mainWebView.setWebViewClient(new StoreWebViewClient());
        //enable dialog.alert etc.
        mainWebView.setWebChromeClient(new StoreWebChromeClient());
        //interf for js of alipay
        mainWebView.addJavascriptInterface(new AliPay(this, UIHandler), Constants.ALI_PAY);

        SharedPreferences sp = getSharedPreferences(Constants.SETTING_SP, 0);
        String spVersion = sp.getString(Constants.VERSION, "0");
        String currentVersion = SystemUtils.getVersionName(this);
        if (!spVersion.equals(currentVersion)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(Constants.VERSION, currentVersion);
            editor.commit();
            clearWebViewCache();
        }
    }

    /**
     * 清除WebView缓存
     */
    public void clearWebViewCache() {

        /** 清理Webview缓存数据库
         try {
         deleteDatabase("webview.db");
         deleteDatabase("webviewCache.db");
         } catch (Exception e) {
         e.printStackTrace();
         } */

        /**WebView 缓存目录
         File webviewCacheDir = new File(getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME);
         //删除webview 缓存目录
         if(webviewCacheDir.exists()){
         FileUtils.deleteFile(webviewCacheDir);
         } */
        mainWebView.clearCache(true);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mainWebView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            mainWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private class StoreWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.toLowerCase().startsWith("http:")
                    || url.toLowerCase().startsWith("https:")
                    || url.toLowerCase().startsWith("file:")) {
                //view.loadUrl(url);
                //return true;
            } else if (url.startsWith(SCHEME_WTAI_MC)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(WebView.SCHEME_TEL +
                                    url.substring(SCHEME_WTAI_MC.length())));
                    startActivity(intent);
                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        }

    }

    private class StoreWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message,
                                 JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

    }

    public void testPay(View view) {
        TestPay.pay(this);
    }
}

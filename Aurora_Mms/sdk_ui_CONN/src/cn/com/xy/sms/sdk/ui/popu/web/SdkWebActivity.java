package cn.com.xy.sms.sdk.ui.popu.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.util.Xml.Encoding;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebSettings.TextSize;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.net.NetWebUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.KeyManager;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.ParseManager;

public class SdkWebActivity extends Activity implements IActivityParamForJS {

    public static String endUrl = "";
    private CommonWebView mWebView = null;
    private RelativeLayout mWebViewLy = null;
    private TextView mTitleNameView = null;
    private ImageView mHeadBackView = null;
    private ImageView mMenuView = null;
//    private RelativeLayout mDuoquBar;
    private ProgressBar mDuoquProgressBar;
    private JSONObject mJsObj = null;
    private Context mContext = null;
    private RelativeLayout mErrorPage = null;
    private RelativeLayout mNetworkSetting = null;
    private String mDuoquText = "";
    
    private String mChannelId = "";
    private String mSdkVersion = "";
    

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.duoqu_sdk_web_main); 
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayShowCustomEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(false);
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        LayoutInflater inflater = getLayoutInflater();
//        RelativeLayout actionBarLayout = (RelativeLayout)inflater.inflate(R.layout.duoqu_web_action_bar, null);
//        actionBar.setCustomView(actionBarLayout);
        
        mContext = this;
        
        mWebViewLy = (RelativeLayout) findViewById(R.id.duoqu_webview);
        mWebView = new CommonWebView(this);
        

        mWebViewLy.addView(mWebView);
        mWebView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));

        mTitleNameView = (TextView) findViewById(R.id.duoqu_title_name);

        mHeadBackView = (ImageView) findViewById(R.id.duoqu_header_back);

        mMenuView = (ImageView) findViewById(R.id.duoqu_header_menu);

        mErrorPage = (RelativeLayout) findViewById(R.id.duoqu_error_page);
        mNetworkSetting = (RelativeLayout) findViewById(R.id.duoqu_network_setting);
//        mDuoquBar = (RelativeLayout) findViewById(R.id.duoqu_progressbar);
        mDuoquProgressBar = (ProgressBar) findViewById(R.id.duoqu_progressbar);
        
        mDuoquText = getResources().getString(R.string.duoqu_tip_duoqu_name);

        // When version greater than 4.4 enable immersive status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setImmersion();
       }

        initParams();
        initWebView();
        loadWebViewUrl();
        initListener();
    }

    @Override
    protected void onDestroy() {
    	
        // TODO Auto-generated method stub
        super.onDestroy();
        mWebViewLy.removeAllViews();
        mWebView.destroy();
//        
//        //如果该Activity实例是当前进程内最后一个实例，则结束进程
//        if(mCreateCount < 1){
//        	System.exit(0);
//        }
    }

    /**
     * Immersive setting
     */
    private void setImmersion() {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    }
    /**
     * set title from menu name
     * @param title
     * @param menuName
     */
    public void setTitle(String title,String menuName){
        if(StringUtils.isNull(menuName)){
            SdkWebActivity.this.setTitle(title);
            mTitleNameView.setText(title);
        }else{
            SdkWebActivity.this.setTitle(menuName);
            mTitleNameView.setText(menuName);
        }
    }
    
    /**
     * init some params from sdk
     */
    void initParams(){
    	try {
//			KeyManager.initAppKey();
//			Constant.initContext(this);
			mChannelId = KeyManager.getAppKey();
			mSdkVersion = ParseManager.getSdkVersion();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    /**
     * OnclickListener method
     */
    void initListener() {

        mMenuView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                MenuWindow mLifeHallWindow = new MenuWindow(
                        SdkWebActivity.this, getWebView(), new OnClickListener() {
                            public void onClick(View v) {
                            }
                        });
                Rect frame = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                int yOffset = frame.top
                        + SdkWebActivity.this.findViewById(R.id.duoqu_header)
                                .getHeight();
                int xOffset = ViewUtil.dp2px(SdkWebActivity.this, 5);
                // mLifeHallWindow.showAsDropDown(SdkWebActivity.this.findViewById(R.id.duoqu_header));
                mLifeHallWindow.setBackgroundDrawable(getResources()
                        .getDrawable(R.drawable.duoqu_popupwindow_menu));
                mLifeHallWindow.showAtLocation(
                        SdkWebActivity.this.findViewById(R.id.duoqu_webview),
                        Gravity.TOP | Gravity.RIGHT, xOffset, yOffset);
            }
        });
        mHeadBackView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                finish();
            }
        });

        mNetworkSetting.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = null;
                try {

                    String sdkVersion = android.os.Build.VERSION.SDK;
                    if (Integer.valueOf(sdkVersion) > 10) {
                        intent = new Intent(
                                android.provider.Settings.ACTION_SETTINGS);
                    } else {
                        intent = new Intent();
                        ComponentName comp = new ComponentName(
                                "com.android.settings",
                                "com.android.settings.WirelessSettings");
                        intent.setComponent(comp);
                        intent.setAction("android.intent.action.VIEW");
                    }
                    SdkWebActivity.this.startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mWebView.canGoBack()) {
                mWebView.goBack();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Load the URL
     */
    void loadWebViewUrl() {
        String aType = super.getIntent().getStringExtra("actionType");
        String pageViewUrl = null;
        if (aType != null && "WEB_URL".equals(aType)) {
            pageViewUrl = getParamData("url");
        } else {
            String host = getParamData("HOST");

            if (StringUtils.isNull(host)) {
                host = NetWebUtil.WEB_SERVER_URL;
            }
            pageViewUrl = getParamData("PAGEVIEW");
            if (StringUtils.isNull(pageViewUrl)) {
                String actionType = getParamData("type");

                if ("WEB_MAP_SITE".equals(actionType)) {
                    String address = getParamData("address");
                    pageViewUrl = "http://api.map.baidu.com/geocoder?address="
                            + address + "&output=html&src=xiaoyuan|"+mDuoquText;

                    if (!StringUtils.isNull(address)) {
                        mDuoquProgressBar.setVisibility(View.GONE);
                        mWebView.loadUrl(pageViewUrl);
                    } else {
                        errorPage();
                    }
                    return;
                } else {
                    pageViewUrl = "h5service?action_type=" + actionType
                            + "&xy_channel=" + mChannelId
                            + "&xy_sdkver=" + mSdkVersion;
                }

				if ("WEB_ABOUT".equals(actionType)
						|| "WEB_HELP".equals(actionType)
						|| "WEB_FEEDBACK".equals(actionType)) {
					mMenuView.setVisibility(View.INVISIBLE);
				} else {
					mMenuView.setVisibility(View.VISIBLE);
				}
            }
            if (!StringUtils.isNull(pageViewUrl)) {
                pageViewUrl = host + "/" + pageViewUrl;
            }
        }
        if (!StringUtils.isNull(pageViewUrl)) {
            int isNetWork = XyUtil.checkNetWork(mContext);
            if (isNetWork == 0 || isNetWork == 1) {
            	
            	Map<String, String> header = new HashMap<String, String>();
//            	header.put("xy-url", pageViewUrl);
//            	header.put("xy-channel", mChannelId);
//            	header.put("xy-sdk-ver", mSdkVersion);
//            	header.put("xy-req-time",String.valueOf(new Date().getTime()));
//            	header.put("xy-x",DuoquUtils.getXid());
//            	header.put("xy-p",DuoquUtils.getPid());
//            	if(getParamData("menuName")!=null||"".equals(getParamData("menuName"))){
//            		try {
//    					String encodeMenuName = URLEncoder.encode(getParamData("menuName"), "utf-8");
//                		header.put("xy-menu-name",encodeMenuName);
//    				} catch (UnsupportedEncodingException e) {
//    					// TODO Auto-generated catch block
//    					e.printStackTrace();
//    				}
//            	}
//            	if(getParamData("publicId")!=null||"".equals(getParamData("publicId"))){
//            		try {
//    					String encodePublicId = URLEncoder.encode(getParamData("publicId"), "utf-8");
//                		header.put("xy-public-id",encodePublicId);
//    				} catch (UnsupportedEncodingException e) {
//    					// TODO Auto-generated catch block
//    					e.printStackTrace();
//    				}
//            	}
//            	mWebView.loadUrl(NetWebUtil.WEBACTIVITY_URL,header);
            	mWebView.loadUrl(pageViewUrl);
            } else {
                errorPage();
                endUrl = pageViewUrl;
            }

        } else {
            errorPage();
        }
    }

    /**
     * Error page
     */
    void errorPage() {
        mDuoquProgressBar.setVisibility(View.GONE);
        mWebView.setVisibility(View.GONE);
        mTitleNameView.setText(R.string.duoqu_web_not_find_page);
        mErrorPage.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize webview
     */
    @SuppressLint("SetJavaScriptEnabled")
    void initWebView() {

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDomStorageEnabled(true); // open DOM storage
        
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);// support h5 view port
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setTextSize(TextSize.NORMAL);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);  //设置 缓存模式 
        mWebView.getSettings().setRenderPriority(RenderPriority.HIGH); //提高渲染优先级
        mWebView.getSettings().setBlockNetworkImage(true);//图片放在最后加载
        
        String dir = this.getApplicationContext()
                .getDir("database", Context.MODE_PRIVATE).getPath();

        mWebView.getSettings().setGeolocationDatabasePath(dir);

        mWebView.getSettings().setGeolocationEnabled(true);
       
        // API
        // function
        mWebView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent,
                    String contentDisposition, String mimetype,
                    long contentLength) {
                // TODO Auto-generated method stub
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }

        });
        mWebView.setWebViewClient(new CommonWebViewClientEx() {
            public void onReceivedSslError(WebView view,
                    SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            // Response to a hyperlink
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Block identification number for telephone connection
                if (url != null
                        && !url.toLowerCase(Locale.getDefault()).startsWith(
                                "http")) {
                    try {
                        if (url.indexOf("tel:") >= 0) {
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri
                                    .parse(url));
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url));
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
//                view.loadUrl(url);
                return false;
            }

            // end loading
            @Override
            public void onPageFinished(WebView view, String url) {


//                String menuName = getParamData("menuName");
//                String title = view.getTitle();
//                setTitle(title,menuName);
            	mWebView.getSettings().setBlockNetworkImage(false);
                super.onPageFinished(view, url);
            }

            // start loading
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                // TODO Auto-generated method stub
                mErrorPage.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }

            // Handle the abnormal case
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {

                if (errorCode != -10) {
                    view.stopLoading();
                    view.clearView();
                    errorPage();
                    endUrl = failingUrl;
                }
            }
        });

        mWebView.setWebChromeClient(new CommonWebChromeClientEx() {
            @Override
            @Deprecated
            public void onConsoleMessage(String message, int lineNumber,
                    String sourceID) {
                // TODO Auto-generated method stub
                super.onConsoleMessage(message, lineNumber, sourceID);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                    Callback callback) {
                callback.invoke(origin, true, false);  
                // TODO Auto-generated method stub
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                return true;
            }

            public boolean onJsAlert(WebView view, String url, String message,
                    final JsResult result) {
                Builder builder = new Builder(SdkWebActivity.this);
                builder.setTitle("Alert");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                result.confirm();
                            }
                        });
                builder.setCancelable(false);
                builder.create();
                builder.show();
                return true;
            };

            public boolean onJsConfirm(WebView view, String url,
                    String message, final JsResult result) {
                Builder builder = new Builder(SdkWebActivity.this);
                builder.setTitle("confirm");
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                result.confirm();
                            }
                        });
                builder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                result.cancel();
                            }
                        });
                builder.setCancelable(false);
                builder.create();
                builder.show();
                return true;
            };

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

//                if(newProgress < 90){
//                    if(mDuoquBar.getVisibility() != View.VISIBLE
//                            ||mWebViewLy.getVisibility() != View.GONE){
//                        mDuoquBar.setVisibility(View.VISIBLE);
//                        mWebViewLy.setVisibility(View.GONE);
//                    }
//                }else{
//                    if(mDuoquBar.getVisibility() != View.GONE
//                            ||mWebViewLy.getVisibility() != View.VISIBLE){
//                        mDuoquBar.setVisibility(View.GONE);
//                        mWebViewLy.setVisibility(View.VISIBLE);
//                    }
//                }
            	 if (newProgress == 100) {
                     mDuoquProgressBar.setVisibility(View.GONE);
                 } else {
                     if (View.GONE == mDuoquProgressBar.getVisibility()) {
                         mDuoquProgressBar.setVisibility(View.VISIBLE);
                     }
                     mDuoquProgressBar.setProgress(newProgress);
                 }
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }

            /**
             * set web view title
             */
            public void onReceivedTitle(WebView view, String title) {
            	String menuName = getParamData("menuName");
                setTitle(title,menuName);
                super.onReceivedTitle(view, title);
            }

        });

        SdkWebJavaScript sdkJs = new SdkWebJavaScript(SdkWebActivity.this);
        mWebView.addJavascriptInterface(sdkJs, "injs");
    }

    @Override
    public WebView getWebView() {
        return mWebView;
    }

    @Override
    public String getParamData(String key) {
        String res = null;
        if (key != null) {
            try {
                if (mJsObj == null) {
                    String jsonData = getIntent().getStringExtra("JSONDATA");
                    mJsObj = new JSONObject(jsonData);
                }
                if (mJsObj != null && mJsObj.has(key)) {
                    res = mJsObj.getString(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (res == null)
            res = "";
        return res;
    }

    @Override
    public Activity getActivity() {
        return SdkWebActivity.this;
    }

    @Override
    public int checkOrientation() {
        int currentOrientation = mContext.getResources().getConfiguration().orientation;
        return currentOrientation;
    }

}

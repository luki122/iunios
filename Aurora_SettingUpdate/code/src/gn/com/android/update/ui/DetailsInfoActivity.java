package gn.com.android.update.ui;

import gn.com.android.update.business.OtaUpgradeInfo;
import gn.com.android.update.business.OtaUpgradeManager;
import gn.com.android.update.settings.OtaSettings;
import gn.com.android.update.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import gn.com.android.update.R;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DetailsInfoActivity extends AuroraActivity {
    private String TAG = "DetailsInfoActivity";
    // Aurora <likai> add begin
    private AuroraActivity mActivity;
    private View mDetailAndDownloadView;
    private Button mDownloadButton;
    // Aurora <likai> add end
    private WebView mMoreWebView = null;
    private ImageView mErrorImageView = null;
    private TextView mHasNoInfoView;
    private ProgressBar mProgressBar = null;
    private TextView mTextView = null;
    private View mView;
    private String mUrl;
    private boolean mFlag = false;
    private final static int UPDATE_UI_MESSAGE = 1;
    private final static int ERROE_MESSAGE = 2;
    private OtaUpgradeManager mOtaUpgradeManager = null;
    private OtaUpgradeInfo mOtaUpgradeInfo = null;
    private final static int TIME_DELAY = 1000;
    private final static long TEXT_TIME_OUT = 10 * 1000;
    private final static long POTO_TIME_OUT = 60 * 1000;
    public static final String FILENAME = "cache.html";
    public static final String MROE_INFO_URL = "moreInfoURL";
    public static final String IS_CURRENT_VERSION_INFO = "isCurrentVersionInfo";
    public static final String IS_LOCAL_VERSION_INFO = "isLocalVersionInfo";
    public static final String HAS_NO_VERSION_INFO = "has_no_info";
    public static boolean HAS_NEW_VERSION = false;
    private boolean mIsCurrentVersionInfo = false;
    private boolean mLightTheme = false;
    private boolean isLocalVersion = false;
    private boolean mHasNoInfo = false;
    private LinearLayout mDownloadButtonLay;
    
    //Begin add by gary.gou
    ScrollView mVersionDetail;
    LinearLayout mVersionLayout;
	HashMap<String,ArrayList<String>> mVersion;
    ArrayList<String>mTitle;
    ArrayList<Integer>mBlockIndex;
    boolean mHasHead = false;
    String mVersionDetailStr;
    boolean mParseError = false;
    //End add by gary.gou
    
    
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_UI_MESSAGE:
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mTextView.setVisibility(View.INVISIBLE);
                    // Aurora <likai> modify begin
                    //mMoreWebView.setVisibility(View.VISIBLE);
                    mDetailAndDownloadView.setVisibility(View.VISIBLE);
                    // Aurora <likai> modify end
                    mErrorImageView.setVisibility(View.INVISIBLE);
                    break;
                case ERROE_MESSAGE:
                	// Aurora <likai> modify begin
                    //if (mMoreWebView.getVisibility() == View.INVISIBLE) {
                	if (mDetailAndDownloadView.getVisibility() == View.INVISIBLE) {
                    // Aurora <likai> modify end
                        mProgressBar.setVisibility(View.INVISIBLE);
                        mTextView.setVisibility(View.INVISIBLE);
                        // Aurora <likai> modify begin
                        //mMoreWebView.setVisibility(View.INVISIBLE);
                        mDetailAndDownloadView.setVisibility(View.INVISIBLE);
                        // Aurora <likai> modify begin
                        mErrorImageView.setVisibility(View.VISIBLE);
                        mHandler.removeMessages(UPDATE_UI_MESSAGE);
                    }
                	
                	if(mParseError){
                		mMoreWebView.stopLoading();
                    }
                	
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean noInfoRelease = true;
        mOtaUpgradeManager = OtaUpgradeManager.getInstance(this);
        if (mOtaUpgradeManager != null) {
            mOtaUpgradeInfo = mOtaUpgradeManager.getOtaUpgradeInfo();
        }
        if(TextUtils.isEmpty(mOtaUpgradeInfo.getReleaseNote())){
            noInfoRelease = true;
        }else{
            noInfoRelease = false;
        }
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(MROE_INFO_URL);
        mIsCurrentVersionInfo = intent.getBooleanExtra(IS_CURRENT_VERSION_INFO, false);
        isLocalVersion = intent.getBooleanExtra(IS_LOCAL_VERSION_INFO, false);
        
        mHasNoInfo = intent.getBooleanExtra(HAS_NO_VERSION_INFO, false) || noInfoRelease;
        // Aurora <likai> modify begin
        /*
        if (GnSettingUpdateThemeUtils.getThemeType(getApplicationContext()).equals(
                GnSettingUpdateThemeUtils.TYPE_DARK_THEME)) {
            setTheme(R.style.GnSettingUpdateDarkTheme);
            mLightTheme = false;
            StringBuilder stringBuilder = new StringBuilder();
            if (mUrl != null && !mUrl.equals("null") && !mUrl.equals("")) {
                mUrl = stringBuilder.append(url).append("&color=1").toString();
            }

        } else if (GnSettingUpdateThemeUtils.getThemeType(getApplicationContext()).equals(
                GnSettingUpdateThemeUtils.TYPE_LIGHT_THEME)) {
            setTheme(R.style.GnSettingUpdateLightTheme);
            mLightTheme = true;
        }
        setContentView(R.layout.detail_info_layout);
        */
        setTheme(R.style.SettingUpdateLightTheme);
        mActivity = this;
        setAuroraContentView(R.layout.aurora_detail_layout, AuroraActionBar.Type.Normal);
        initialLayout();
        if(mHasNoInfo){
            getAuroraActionBar().setTitle(R.string.kown_more_no_info);
        }else{
            if (mIsCurrentVersionInfo||isLocalVersion) {
                getAuroraActionBar().setTitle(R.string.current_version_detail);
            } else {
                getAuroraActionBar().setTitle(R.string.new_version_detail);
            }
            if(!TextUtils.isEmpty(mUrl)){
                if(isParseError(mIsCurrentVersionInfo)){
                	//版本详情里面以webview形式显示
                	Log.v(TAG, "isParseError------true");
                	setWebView(mUrl);
                	mParseError = true;
                	mVersionDetail.setVisibility(View.GONE);
                	mMoreWebView.setVisibility(View.VISIBLE);
                }else{
                	//版本详情里面以IUNI定义的排版显示
                	Log.v(TAG, "isParseError------false");
                	setVersionDetailView();
                	mParseError = false;
                	mVersionDetail.setVisibility(View.VISIBLE);
                	mMoreWebView.setVisibility(View.GONE);
                }
            }else{
                mHasNoInfoView.setVisibility(View.VISIBLE);
            }
            
        }
        
        
        /*
        AuroraActionBar actionBar = getAuroraActionBar();
        if (actionBar != null) {
            actionBar.setDisplayOptions(
                    AuroraActionBar.DISPLAY_HOME_AS_UP | AuroraActionBar.DISPLAY_SHOW_TITLE,
                    AuroraActionBar.DISPLAY_HOME_AS_UP | AuroraActionBar.DISPLAY_SHOW_TITLE
                            | AuroraActionBar.DISPLAY_SHOW_HOME);
        }
        */

        LogUtils.logd(TAG, "onCreate() mUrl = " + mUrl + " mIsCurrentVersionInfo " + mIsCurrentVersionInfo);
        
    }

    public void initialLayout() {
        LogUtils.logd(TAG, "initialLayout");
        mView = (View) findViewById(R.id.info_more);
        // Aurora <likai> delete begin
        /*
        if (mLightTheme) {
            mView.setBackgroundResource(R.drawable.gn_background_light);
        } else {
            mView.setBackgroundColor(Color.rgb(27, 27, 27));
        }
        */
        // Aurora <likai> delete end
        mHasNoInfoView = (TextView)findViewById(R.id.no_detail);
        
        mMoreWebView = (WebView) findViewById(R.id.ready_webview);  
        //Begin modify by gary.gou
        mVersionDetail = (ScrollView)findViewById(R.id.version_detail);
		mVersion = new HashMap<String,ArrayList<String>>();
		mTitle = new ArrayList<String>();
		mBlockIndex = new ArrayList<Integer>();
		
		mVersionLayout = new LinearLayout(this);
		mVersionLayout.setOrientation(LinearLayout.VERTICAL);
        //End modify by gary.gou
		
        mErrorImageView = (ImageView) findViewById(R.id.network_error);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_webview);
        mTextView = (TextView) findViewById(R.id.progress_textview);
        mErrorImageView.setVisibility(View.INVISIBLE);
        mProgressBar.setMax(100);
        Log.e("lf", "mHasNoInfo:"+mHasNoInfo);
        mProgressBar.setVisibility(!mHasNoInfo?View.VISIBLE:View.GONE);
        mTextView.setVisibility(!mHasNoInfo?View.VISIBLE:View.GONE);
        // Aurora <likai> modify begin
        //mMoreWebView.setVisibility(View.INVISIBLE);
        //mMoreWebView.setBackgroundColor(Color.TRANSPARENT);
        mDetailAndDownloadView = (View) findViewById(R.id.detail_and_download_view);
        mDetailAndDownloadView.setVisibility(View.VISIBLE);
        mDownloadButton = (Button) findViewById(R.id.download_button);
        mDownloadButtonLay = (LinearLayout)findViewById(R.id.download_button_layout);
        mDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.setResult(Activity.RESULT_OK);
                mActivity.finish();
            }
        });
        if(mHasNoInfoView != null){
            mHasNoInfoView.setVisibility(mHasNoInfo?View.VISIBLE:View.GONE);
        }
        
        if (mIsCurrentVersionInfo||isLocalVersion||(mHasNoInfo && HAS_NEW_VERSION)){
            mDownloadButtonLay.setVisibility(View.GONE);
        }
        Log.e("button", "has new version:"+HAS_NEW_VERSION);
        if(HAS_NEW_VERSION){
            mDownloadButtonLay.setVisibility(View.VISIBLE);
        }else{
            mDownloadButtonLay.setVisibility(View.GONE);
        }
        // Aurora <likai> modify end
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setWebViewFromCache(String url) {
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = openFileInput(FILENAME);
            LogUtils.logd(TAG, "available = " + fis.available());
            if (fis.available() > 0) {
                bos = new ByteArrayOutputStream();
                int len = 0;
                byte[] buffer = new byte[1024];
                while ((len = fis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                }
                byte[] content_byte = bos.toByteArray();
                String content = new String(content_byte);
                mHandler.sendEmptyMessageDelayed(ERROE_MESSAGE, TEXT_TIME_OUT);
                LogUtils.logd(TAG, "setWebViewFromCache  ERROE_MESSAGE");
                mMoreWebView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
            } else {
                cacherNotFound(url);
            }
        } catch (FileNotFoundException e) {
            LogUtils.loge(TAG, FILENAME + "not found");
            cacherNotFound(url);
        } catch (IOException e) {
            LogUtils.loge(TAG, e.getMessage());
            cacherNotFound(url);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (bos != null) {
                    bos.close();
                }
            } catch (IOException io) {
                LogUtils.loge(TAG, io.getMessage());
            }
        }
    }

    private WebChromeClient mWebChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            LogUtils.logd(TAG, "progress = "+progress);
            if (progress == 100) {
                if (mFlag) {
                    mHandler.sendEmptyMessageDelayed(UPDATE_UI_MESSAGE, TIME_DELAY);
                } else {
                    mHandler.sendEmptyMessage(UPDATE_UI_MESSAGE);
                }
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
                mTextView.setVisibility(View.VISIBLE);
                // Aurora <likai> modify begin
                //mMoreWebView.setVisibility(View.INVISIBLE);
                mDetailAndDownloadView.setVisibility(View.INVISIBLE);
                // Aurora <likai> modify begin
                mErrorImageView.setVisibility(View.INVISIBLE);
            }
        }
    };

    private void setWebView(String url) {
        Log.e("lf", "setWebView");
        WebSettings webSettings = mMoreWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mMoreWebView.setHorizontalScrollBarEnabled(false);
        mMoreWebView.setVerticalScrollBarEnabled(false);
        mMoreWebView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        if ("null".equals(url) || "".equals(url)) {
            loadTextImproveInfoFromCacher(mIsCurrentVersionInfo);
        } else {
            setWebViewFromCache(url);
        }

        mMoreWebView.setWebChromeClient(mWebChromeClient);
        mMoreWebView.setWebViewClient(new WebViewClient() {
            boolean onError = false;

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                LogUtils.logd(TAG, "WebViewClient onReceivedError() errorCode = " + errorCode + " description = "
                        + description + "failingUrl = " + failingUrl);
                mErrorImageView.setVisibility(View.VISIBLE);
                mMoreWebView.setWebChromeClient(null);
                // Aurora <likai> modify begin
                //mMoreWebView.setVisibility(View.INVISIBLE);
                mDetailAndDownloadView.setVisibility(View.INVISIBLE);
                // Aurora <likai> modify begin
                mProgressBar.setVisibility(View.GONE);
                mTextView.setVisibility(View.GONE);
                onError = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (!onError) {
                    view.loadUrl("javascript:window.handler.cacher(document.getElementsByTagName('html')[0].innerHTML);");
                }
            }
        });
    }

    private void cacherNotFound(String url) {
        mMoreWebView.addJavascriptInterface(new CacherHandler(), "handler");
        mMoreWebView.loadUrl(url);
        mFlag = false;
        mHandler.sendEmptyMessageDelayed(ERROE_MESSAGE, POTO_TIME_OUT);
        LogUtils.logd(TAG, "cacherNotFound  ERROE_MESSAGE");
    }

    private void loadTextImproveInfoFromCacher(boolean isCurrentVersionInfo) {
        String improveInfo = "";
        if (isCurrentVersionInfo) {
            improveInfo = OtaSettings.getCurrVerReleasenote(this, "");
        } else {
            improveInfo = mOtaUpgradeInfo.getReleaseNote();
        }
        mHandler.sendEmptyMessageDelayed(ERROE_MESSAGE, TEXT_TIME_OUT);
        LogUtils.logd(TAG, "loadTextImproveInfoFromCacher  ERROE_MESSAGE");
        mMoreWebView.loadDataWithBaseURL(null, improveInfo, "text/html", "utf-8", null);
        mFlag = true;
    }

    class CacherHandler {
        public void cacher(String data) {
            FileOutputStream fos = null;
            try {
                fos = DetailsInfoActivity.this.openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(data.getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                        fos = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    //Begin add by gary.gou
    
    /*
     * 能正确解析版本说明，返回false
     * 不能正确解析版本说明，返回true
     */
    private boolean isParseError(boolean isCurrentVersion){
    	 if (isCurrentVersion) {
    		 mVersionDetailStr = OtaSettings.getCurrVerReleasenote(this, "");
         } else {
        	 mVersionDetailStr = mOtaUpgradeInfo.getReleaseNote();
         }
    	 if(mVersionDetailStr.equals("")){
    		 return true;
    	 }
    	 
    	 int indexBR = mVersionDetailStr.indexOf("<br />");
    	 if(indexBR == -1){
    		 return true;
    	 }
    	 
		 return false;
    }
    
	private void setVersionDetailView() {
		LayoutInflater inflater = LayoutInflater.from(DetailsInfoActivity.this);
		mVersionLayout.removeAllViews();
		mBlockIndex.clear();
		mTitle.clear();
		
		mVersionDetailStr = mVersionDetailStr.replaceAll("\\s*", "");//替换所以空格、制表符、换页符等空白字符,<br /> 变成<br/>
		String[] Blocks = mVersionDetailStr.split("<br/>");	
		 
		//记录第一个不是“”字符串的位置。
			int firstNotEmptyStrPos=0;
			for(int i=0;i<Blocks.length;i++){
	               if(!Blocks[i].equals("")){
	            	   firstNotEmptyStrPos = i;
	            	   break;
	               }
		    	}
			
			
			//保存所有块结束位置
			for(int i=firstNotEmptyStrPos ; i+1<Blocks.length;i++){
               if(Blocks[i].equals("") && !Blocks[i+1].equals("")){
            	   mBlockIndex.add(i);
               }
	    	}

			for(int a=0;a<mBlockIndex.size();a++){
				Log.v(TAG, "mBlockIndex-----------"+mBlockIndex.get(a));
			}
			
			//判断是否包含head信息，如果有head时,添加
			if(mBlockIndex.size() > 0){
				if(mBlockIndex.get(0) == firstNotEmptyStrPos+1){
					TextView head = (TextView)inflater.inflate(R.layout.head_item, null);
					head.setText(Blocks[firstNotEmptyStrPos]);
	    			mVersionLayout.addView(head);
	    			mHasHead = true;
	    			Log.v(TAG, "has head-----------");
				}
			}
			
			int index;
			int j=0;
			int y = firstNotEmptyStrPos;
			if(mHasHead){
				//firstNotEmptyStrPos + 1 is empty
				y = firstNotEmptyStrPos + 2;
				mHasHead = false;
			}
			//保存title,content
			for(;y<Blocks.length && j<mBlockIndex.size();){
				ArrayList<String> list = new ArrayList<String>();
				mTitle.add(Blocks[y]);
				Log.v(TAG, "mTitle--------" +Blocks[y]);
				index = mBlockIndex.get(j);
				for(int n=y+1;n<index;n++){
					  if(!Blocks[n].equals("")){
						  list.add(Blocks[n]);
					  }
					  Log.v(TAG, "content--------" + Blocks[n]);
				}
				mVersion.put(Blocks[y], list);
				y = index+1;
				j++;
			}
			
			//保存 最后的title,content
			int indexSize = mBlockIndex.size();
			ArrayList<String> endList = new ArrayList<String>();
			if(indexSize>0 && indexSize<Blocks.length){
				int m= mBlockIndex.get(indexSize-1)+1;
				mTitle.add(Blocks[m]);
				
				for(int n=m+1;n<Blocks.length;n++){
					endList.add(Blocks[n]);
				}
				
				mVersion.put(Blocks[m], endList);
			}
			
			//排版
    		for(int k=0;k<mTitle.size();k++){
    			ArrayList<String> list = new ArrayList<String>();
    			list = mVersion.get(mTitle.get(k));
    			TextView title = (TextView)inflater.inflate(R.layout.title_item, null);
    			title.setText(mTitle.get(k));
    			mVersionLayout.addView(title);
    			
    			for(int m = 0;m <list.size();m++){
    				RelativeLayout relativeLayout = (RelativeLayout)inflater.inflate(R.layout.content_item, null);
    				TextView content = (TextView)relativeLayout.findViewById(R.id.content);
    				content.setText(list.get(m));
    				mVersionLayout.addView(relativeLayout);
    			}
    		}
    		//排版完，添加空白view
    		TextView blankView = (TextView)inflater.inflate(R.layout.blank_item, null);
    		mVersionLayout.addView(blankView);
    		mVersionDetail.addView(mVersionLayout);
    		
    		mHandler.sendEmptyMessage(UPDATE_UI_MESSAGE);
    }
    
    //End add by gary.gou
    
    
}
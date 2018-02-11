package com.aurora.setupwizard;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;
import com.aurora.setupwizard.adapter.LanguageAdapter;
import com.aurora.setupwizard.adapter.LanguageAdapter.RBListener;
import com.aurora.setupwizard.domain.LanguageInfo;
import com.aurora.setupwizard.utils.Constants;
import com.aurora.setupwizard.view.LocalePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class LanguageSetupWizard extends BaseActivity implements OnClickListener, OnItemClickListener {

    //private static final int MSG_CHANGE_STATUS_BAR_COLOR = 0x01;
    //private boolean isFirst = true;

    private LocalePicker mLanguagePicker;
    private ImageButton imgBtnNext;
    private LinearLayout ll;
    private static final String ACTION_EMERGENCY_DIAL = "com.android.phone.EmergencyDialer.DIAL";
    private String mCurrentLanguage = "中文";
    private Locale mCurrentLocale;
    private int mSelectedIndex;
    private int zuIndex = -1;
    private Locale[] mAvailableLocales;

    private ArrayAdapter<com.android.internal.app.LocalePicker.LocaleInfo> mLocaleAdapter;
    private int[] mAdapterIndices;
    private Locale mInitialLocale;
    private String[] labels;
    //
    private List<LanguageInfo> mLanguageInfos = null;
    private String[] mLanguages = new String[]{"中文 (简体)","English (United States)"};
    
    private ListView mLvLanguage = null;
    private LanguageAdapter mAdapter = null;
    private TextView tv_next;
    private TextView tv_pre;
    private ImageView iv_header;


    public static final String ACTION_SETUP_WIFI = "com.android.net.wifi.SETUP_WIFI_NETWORK";
    public static final String EXTRA_FIRST_RUN = "firstRun";
    public static final String EXTRA_ALLOW_SKIP = "allowSkip";
    public static final String EXTRA_AUTO_FINISH = "wifi_auto_finish_on_connect";
    public static final String EXTRA_USE_IMMERSIVE = "useImmersiveMode";

    private static final int SWITCH_ACTIONBAR_COLOR = 0;
    
    private Handler animHandler = new Handler(){
    	public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case SWITCH_ACTIONBAR_COLOR:
				com.aurora.utils.SystemUtils.switchStatusBarColorMode(
						com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, LanguageSetupWizard.this);
				break;

			default:
				break;
			}
    	};
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int provision = Settings.Global.getInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);
        if (provision == 1) {

            // remove this activity from the package manager.
            PackageManager pm = getPackageManager();
            ComponentName name = new ComponentName(this, LanguageSetupWizard.class);
            pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);

            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            am.forceStopPackage(getPackageName());
        }

        setAuroraContentView(R.layout.activity_language, AuroraActionBar.Type.Empty);
        
        getAuroraActionBar().setVisibility(View.GONE);

        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
				com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
        
        initView();
        initData();
        initEvent();
        initEnv();
        
    }

    //Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0);

    @Override
    protected void onStart() {
    	super.onStart();
        Intent intent = new Intent(Constants.HANDLER_DISABLE);
        sendBroadcast(intent);
    }
       @Override
    public void onResume(){
        super.onResume();
    	 com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                 com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, LanguageSetupWizard.this);
    	 tv_next.setClickable(true);
    }


    private void initEnv() {
        ((WifiManager) getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
    }

    private void initEvent() {

//        imgBtnNext.setOnClickListener(this);
//        ll.setOnClickListener(this);
    	tv_next.setOnClickListener(this);

    }

    private void initData() {
        load();
        mAdapter = new LanguageAdapter(getApplicationContext(), mLanguageInfos);
        mAdapter.setRBListener(new RBListener() {
			
			@Override
			public void changer(int position) {
				setLocaleFromPicker(position);
			}
		});
        mLvLanguage.setAdapter(mAdapter);
        mLvLanguage.setOnItemClickListener(this);
        
    }

    // 加载语言
    private ArrayList<String> loadLanguages() {
        // Locale default1 = Locale.getDefault();
        // System.out.println(default1.getDisplayName());
        mAvailableLocales = Locale.getAvailableLocales();
        ArrayList<String> locales = new ArrayList<String>();
        for (int i = 0; i < mAvailableLocales.length; i++) {
            if (mAvailableLocales[i].getDisplayName().equals(mCurrentLanguage)) {
                zuIndex = i;
            }
            locales.add(mAvailableLocales[i].getDisplayName());
        }
        return locales;
    }

    private void load() {
        //语言适配器
        mLocaleAdapter = com.android.internal.app.LocalePicker
                .constructAdapter(this, R.layout.locale_picker_item,
                        R.id.locale);
        mInitialLocale = Locale.getDefault();
        mCurrentLocale = mInitialLocale;
        mAdapterIndices = new int[mLocaleAdapter.getCount()];
        mCurrentLocaleIndex = 0;
        //用于存储语言
        labels = new String[mLocaleAdapter.getCount()];
        mLanguageInfos = new ArrayList<LanguageInfo>();
        
        for (int i = 0; i < mAdapterIndices.length; i++) {
            com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo = mLocaleAdapter.getItem(i);
            Locale localLocale = localLocaleInfo.getLocale();
            
           boolean isSelect = false;
            
            if (localLocale.equals(mCurrentLocale)) {
                mCurrentLocaleIndex = i;
                isSelect = true;
            }
            mAdapterIndices[i] = i;
            labels[i] = localLocaleInfo.getLabel();
            //locale.getCountry().equals("CN")
            if(!(localLocaleInfo.getLocale().getCountry().equals("CN")||localLocaleInfo.getLocale().getCountry().equals("US"))){
            	continue;
            }
//            if(!(localLocaleInfo.getLabel().trim().equals(mLanguages[0].trim())||localLocaleInfo.getLabel().trim().equals(mLanguages[1].trim()))){
//            	continue;
//            }
            //语言封装类
            LanguageInfo info = new LanguageInfo();
            info.setId(i);
            info.setSelect(isSelect);
            info.setName( localLocaleInfo.getLabel());
            mLanguageInfos.add(info);
        }

    }

    // 初始化view
    private void initView() {

    	mLvLanguage = (ListView)findViewById(R.id.lv_language);
    	tv_next = (TextView)findViewById(R.id.tv_next);
    	tv_next.setClickable(true);
    	tv_pre = (TextView)findViewById(R.id.tv_pre);
    	tv_pre.setVisibility(View.INVISIBLE);
    	iv_header= (ImageView)findViewById(R.id.iv_header_img);

    }

	

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.tv_next:
                // 下一步
                callNext();
                break;

            default:
                break;
        }

    }

    // 跳到下一步
    private void callNext() {
    	
        if (SetupWizardUtils.hasTelephony(this)) {
            if (SetupWizardUtils.isSimInserted(this)) {

                Intent intent = new Intent(ACTION_SETUP_WIFI);
                intent.putExtra(EXTRA_FIRST_RUN, true);
                intent.putExtra(EXTRA_ALLOW_SKIP, true);
                intent.putExtra(EXTRA_USE_IMMERSIVE, true);
                intent.putExtra(EXTRA_AUTO_FINISH, false);
                startActivity(intent);
            } else {
                Intent i = new Intent(this, SimCardMissingActivity.class);
                startActivity(i);
            }
        }
    }

    // 紧急呼叫
    private void callExigency() {

        Intent intent = new Intent(ACTION_EMERGENCY_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivity(intent);
    }

    // 设置语言
    private final Handler mHandler = new Handler();

    private final Runnable mUpdateLocale = new Runnable() {
        public void run() {
            if (mCurrentLocale != null) {
                com.android.internal.app.LocalePicker.updateLocale(mCurrentLocale);
            }
        }
    };
    private int mCurrentLocaleIndex;


    private void setLocaleFromPicker(int position) {
    	  //int i = mAdapterIndices[mLanguagePicker.getValue()];
    	int i = position;
          final com.android.internal.app.LocalePicker.LocaleInfo localLocaleInfo = mLocaleAdapter.getItem(i);
          onLocaleChanged(localLocaleInfo.getLocale());

    }

    private void onLocaleChanged(Locale paramLocale) {
        Resources localResources = getResources();
        Configuration localConfiguration1 =
                localResources.getConfiguration();
        Configuration localConfiguration2 = new Configuration();
        localConfiguration2.locale = paramLocale;
        localResources.updateConfiguration(localConfiguration2, null);
        localResources.updateConfiguration(localConfiguration1, null);
        mHandler.removeCallbacks(mUpdateLocale);
        mCurrentLocale = paramLocale;
        mCurrentLocale.getDisplayName();
        mHandler.postDelayed(mUpdateLocale, 200);
    }

    @Override
    public boolean onKeyDown(int arg0, KeyEvent arg1) {
        if (arg0 == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(arg0, arg1);
    }
    
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		 //setLocaleFromPicker(arg2);
	}

    @Override
    public void onAttachedToWindow () {
        super.onAttachedToWindow();
        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                   com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, LanguageSetupWizard.this);
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	super.onWindowFocusChanged(hasFocus);
    	 com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                 com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, LanguageSetupWizard.this);
    }

}

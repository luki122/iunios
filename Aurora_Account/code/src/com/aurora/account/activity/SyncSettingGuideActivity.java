package com.aurora.account.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraSwitch;
import aurora.widget.AuroraUtil;

import com.aurora.account.R;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.widget.PageIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导页界面
 * @author JimXia
 *
 * @date 2014年11月14日 下午3:15:44
 */
public class SyncSettingGuideActivity extends AuroraActivity implements
		OnClickListener, OnCheckedChangeListener, OnPageChangeListener {

    private static final String TAG = "SyncSettingGuideActivity";
    
	public static final String EXTRA_KEY_LOGIN_STATUS = "loginStatus";
	private int mLoginStatus;
	
	private static final int POS_CONTACTS = 0; // 联系人索引
    private static final int POS_SMS = 1; // 短信索引
    
	private static final int MODE_CONTACTS = 1; // 联系人
	private static final int MODE_SMS = 2; // 短信

	private ViewPager mModuleVp;
	private PageIndicator mPi;
	private RelativeLayout mSyncOrNotRl;
	private AuroraSwitch mSyncOrNotAs;
	private Button mActionBtn;
	
	private PagerAdapter mAdapter;
	private int mMode = MODE_CONTACTS;
	private final int mTotalPage = 2;
	private int mCurrentPage = 0;
	private final SparseIntArray mPos2Mode = new SparseIntArray(2);
	private List<AppConfigInfo> mAppConfigInfoList;
	
	private Typeface mModuleTitleTypeface;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sync_setting_guide_activity);
		mLoginStatus = getIntent().getIntExtra(EXTRA_KEY_LOGIN_STATUS, SyncAccountActivity.LOGIN_STATUS_NORMAL);
		
		initViews();
		setListeners();
		setAdapter();
		initData();
		handlePageChanged(POS_CONTACTS);
	}
	
	private void initData() {
	    mPi.setTotalPages(2);
	    mPi.setCurrentPage(0);
	    
	    mPos2Mode.put(POS_CONTACTS, MODE_CONTACTS);
	    mPos2Mode.put(POS_SMS, MODE_SMS);
	}
	
	private void initViews() {
	    mModuleVp = (ViewPager) findViewById(R.id.module_vp);
	    mPi = (PageIndicator) findViewById(R.id.page_pi);
	    mSyncOrNotRl = (RelativeLayout) findViewById(R.id.sync_or_not_rl);
	    mSyncOrNotAs = (AuroraSwitch) findViewById(R.id.sync_or_not_as);
	    mActionBtn = (Button) findViewById(R.id.action_btn);
    }
	
	private void setListeners() {
	    mSyncOrNotRl.setOnClickListener(this);
	    mSyncOrNotAs.setOnCheckedChangeListener(this);
	    mModuleVp.setOnPageChangeListener(this);
	    mActionBtn.setOnClickListener(this);
    }
	
	private void setAdapter() {
	    mAdapter = new ModulePagerAdapter(getPageViewList());
	    mModuleVp.setAdapter(mAdapter);
//	    mModuleVp.setCurrentItem(POS_CONTACTS);
    }

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		    case R.id.action_btn:
		        next();
		        break;
		}
	}
	
	private void next() {
	    if (!isLastPage()) {
	        mModuleVp.setCurrentItem(mCurrentPage + 1, true);
	    } else {
	        openMainActivity(mLoginStatus);
	    }
	}
	
	private void prev() {
        if (!isFirstPage()) {
            mModuleVp.setCurrentItem(mCurrentPage - 1, true);
        }
    }
	
	private boolean isLastPage() {
	    return mCurrentPage >= mTotalPage - 1;
	}
	
	private boolean isFirstPage() {
	    return mCurrentPage == 0;
	}
	
	private void openMainActivity(int loginStatus) {
        Log.d(TAG, "Jim, loginStatus: " + loginStatus);
        Intent intent = new Intent(this, SyncAccountActivity.class);
        intent.putExtra(SyncAccountActivity.EXTRA_KEY_LOGIN_STATUS, loginStatus);
        startActivity(intent);
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (mMode) {
            case MODE_CONTACTS:
                SystemUtils.updateAppConfigInfo(this, getAppConfigInfoList().get(POS_CONTACTS).getApp_packagename(), isChecked);
//                setSyncEnabled(getAppConfigInfoList().get(POS_CONTACTS).getApp_packagename(), isChecked);
                break;
            case MODE_SMS:
                SystemUtils.updateAppConfigInfo(this, getAppConfigInfoList().get(POS_SMS).getApp_packagename(), isChecked);
//                setSyncEnabled(getAppConfigInfoList().get(POS_SMS).getApp_packagename(), isChecked);
                break;
        }
    }
    
    private List<AppConfigInfo> getAppConfigInfoList() {
        if (mAppConfigInfoList == null) {
            mAppConfigInfoList = SystemUtils.getAppConfigInfo(this);
        }
        
        return mAppConfigInfoList;
    }
    
    /*private void setSyncEnabled(String packageName, boolean syncEnabled) {
        getSharedPreferences(Globals.SHARED_MODULE_SYNC, Context.MODE_PRIVATE).edit()
        .putBoolean(packageName, syncEnabled).commit();
    }*/
    
    private boolean isSyncEnabled(String packageName) {
        SharedPreferences sts = getSharedPreferences(
                Globals.SHARED_MODULE_SYNC, Context.MODE_PRIVATE);
        return sts.getBoolean(packageName, true);
    }
    
    @SuppressLint("InflateParams")
    private ArrayList<View> getPageViewList() {
        ArrayList<View> pageViewList = new ArrayList<View>(mTotalPage);
        
        View view = LayoutInflater.from(this).inflate(R.layout.sync_setting_guide_item, null);
        ImageView logoIv = (ImageView) view.findViewById(R.id.logo_iv);
        logoIv.setImageResource(R.drawable.sync_setting_contact_logo);
        TextView moduleNameTv = (TextView) view.findViewById(R.id.module_name_tv);
        moduleNameTv.setTypeface(getModuleTitleTypeface());
        moduleNameTv.setText(R.string.contact_sync_title);
        TextView moduleDescTv = (TextView) view.findViewById(R.id.module_desc_tv);
        moduleDescTv.setText(R.string.contact_sync_desc);
        pageViewList.add(view);
        
        view = LayoutInflater.from(this).inflate(R.layout.sync_setting_guide_item, null);
        logoIv = (ImageView) view.findViewById(R.id.logo_iv);
        logoIv.setImageResource(R.drawable.sync_setting_sms_logo);
        moduleNameTv = (TextView) view.findViewById(R.id.module_name_tv);
        moduleNameTv.setTypeface(getModuleTitleTypeface());
        moduleNameTv.setText(R.string.sms_sync_title);
        moduleDescTv = (TextView) view.findViewById(R.id.module_desc_tv);
        moduleDescTv.setText(R.string.sms_sync_desc);
        pageViewList.add(view);
        
        return pageViewList;
    }
    
    private Typeface getModuleTitleTypeface() {
        if (mModuleTitleTypeface == null) {
            mModuleTitleTypeface = Typeface.createFromFile(AuroraUtil.ACTION_BAR_TITLE_FONT);
        }
        
        return mModuleTitleTypeface;
    }
    
    private class ModulePagerAdapter extends PagerAdapter {
        private ArrayList<View> mPageViewList;
        
        public ModulePagerAdapter(ArrayList<View> pageViewList) {
            mPageViewList = pageViewList;
        }
        
        @Override
        public int getCount() {
            return mPageViewList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        
        // 删除页卡
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object)   {
            Log.d(TAG, "Jim, destroyItem, position: " + position);
            container.removeView(mPageViewList.get(position));  
        }  
  
        // 这个方法用来实例化页卡
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {         
             container.addView(mPageViewList.get(position), 0); // 添加页卡  
             return mPageViewList.get(position);  
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        handlePageChanged(position);
    }
    
    private void handlePageChanged(int selectedPage) {
        Log.d(TAG, "Jim, onPageSelected, selectedPage: " + selectedPage);
        mPi.setCurrentPage(selectedPage);
        mCurrentPage = selectedPage;
        mMode = mPos2Mode.get(selectedPage);
        
        mSyncOrNotAs.setChecked(isSyncEnabled(getAppConfigInfoList().get(selectedPage).getApp_packagename()));
        
        if (!isLastPage()) {
            mActionBtn.setText(R.string.register_next);
        } else {
            mActionBtn.setText(R.string.open_cloun);
        }
    }

    @Override
    public void onBackPressed() {
        prev();
    }
}
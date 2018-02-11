package com.aurora.change.activities;

import java.util.ArrayList;

import android.R.integer;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;

import com.aurora.thememanager.ThemeManagerApplication;
import com.aurora.thememanager.R;
import com.aurora.change.data.AdapterDataFactory;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureGroupInfo;
import com.aurora.change.utils.Consts;
import com.aurora.change.utils.WallpaperConfigUtil;
import com.aurora.change.utils.WallpaperUtil;
import android.os.SystemProperties;

public class WallpaperManagerActivity extends AuroraActivity implements OnClickListener {

    private ImageView mManagerLockpaper = null;
    private ImageView mManagerWallpaper = null;
    private ImageView mManagerPreLockpaper = null;
    private TextView mLockPaperTv = null;
    private TextView mWallpaperTv = null;
    private Context mContext;
    //private AdapterDataFactory mDataFactory;
    private AuroraActionBar mAuroraActionBar;
    private boolean mFromApp = false;
    
    //shigq add start
    private ImageView mManagerWallpaperBase;
    private boolean isNeedExitAnimation = false;
    //shigq add end
        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_wallpaper_manager);
        setAuroraContentView(R.layout.activity_wallpaper_manager, AuroraActionBar.Type.Normal);
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setTitle(R.string.app_name);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setupViews();
		 // Aurora liugj 2014-09-24 deleted for bug-8151 start
        //initWallpaperData();
		 // Aurora liugj 2014-09-24 deleted for bug-8151 end
        
        //shigq fix bug #14962 start
        Intent mIntent = getIntent();
        if (mIntent != null) {
            Bundle bundle = mIntent.getExtras();
            if (bundle != null) {
				isNeedExitAnimation = bundle.getBoolean("needSetExitAnimation", false);
				Log.d("Wallpaper_DEBUG", "isNeedExitAnimation = "+isNeedExitAnimation);
			}
        }
        //shigq fix bug #14962 end
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWallpaperPreview();
        if (getIntent().getExtras() != null) {
          mFromApp = "settings".equals(getIntent().getExtras().getString("fromApp", "launcher"));
      }
    }
    
    //shigq fix bug #14962 start
    @Override
    protected void onPause() {
    	if (isNeedExitAnimation) {
			overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
		}
    	super.onPause();
	}
    //shigq fix bug #14962 end

    private void setupViews() {
        mContext = this;
        mManagerLockpaper = ( ImageView ) findViewById(R.id.manager_lockpaper);
        mLockPaperTv = ( TextView ) findViewById(R.id.lockpaper_pre_tv);
        mManagerWallpaper = ( ImageView ) findViewById(R.id.manager_wallpaper);
        
        //shigq add start
        mManagerWallpaperBase = ( ImageView ) findViewById(R.id.manager_wallpaper_base);
        if ("true".equals(SystemProperties.get("phone.type.oversea"))) {
        	mManagerWallpaperBase.setImageResource(R.drawable.manager_wallpaper_india);
        }
        //shigq add end
        
        mWallpaperTv = ( TextView ) findViewById(R.id.wallpaper_pre_tv);
        mManagerPreLockpaper = ( ImageView ) findViewById(R.id.manager_prelockpaper);
        mManagerLockpaper.setOnClickListener(this);
        mManagerWallpaper.setOnClickListener(this);
        mAuroraActionBar = getAuroraActionBar();
        mAuroraActionBar.setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {

            @Override
            public void onAuroraActionBarBackItemClicked(int arg0) {
                finish();
                if (mFromApp) {
                    overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.manager_lockpaper:
                startNextActivity(Consts.WALLPAPER_LOCKSCREEN_TYPE);
                break;

            case R.id.manager_wallpaper:
//                startNextActivity(Consts.WALLPAPER_DESKTOP_TYPE);
                // Intent intent = new Intent(mContext, DesktopWallpaperActivity.class);
                Intent intent = new Intent(mContext, DesktopWallpaperLocalActivity.class);
                ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext,
                        com.aurora.R.anim.aurora_activity_open_enter, com.aurora.R.anim.aurora_activity_open_exit);
                startActivity(intent, opts.toBundle());
                break;

            default:
                break;
        }
    }

    private void startNextActivity(String flag) {
        Intent intent = new Intent(mContext, WallpaperLocalActivity.class);
        intent.putExtra(Consts.WALLPAPER_TYPE_KEY, flag);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityOptions opts = ActivityOptions.makeCustomAnimation(mContext, com.aurora.R.anim.aurora_activity_open_enter,
                com.aurora.R.anim.aurora_activity_open_exit);
//        startActivity(intent);
        startActivity(intent, opts.toBundle());
    }

    private void setWallpaperPreview() {
    	//shigq add start
    	/*String currentGroup = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, Consts.DEFAULT_LOCKPAPER_GROUP);
    	if (currentGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || currentGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
    		Log.d("Wallpaper_DEBUG", "WallpaperManagerActivity-------setWallpaperPreview----manager_lockpaper_black ");
			mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper_black);
		} else {
			Log.d("Wallpaper_DEBUG", "WallpaperManagerActivity-------setWallpaperPreview----------manager_lockpaper ");
			mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper);
		}*/
    	
    	boolean timeIsBlack = false;
    	DbControl mDbControl = new DbControl(mContext);
    	PictureGroupInfo groupInfo = null;
    	String currentGroup = DataOperation.getStringPreference(mContext, Consts.CURRENT_LOCKPAPER_GROUP, null);
    	if (currentGroup == null) {
    		groupInfo = mDbControl.queryDefaultGroup();
		} else {
			groupInfo = mDbControl.queryGroupByName(currentGroup);
		}
    	if (groupInfo != null) {
    		Log.d("Wallpaper_DEBUG", "WallpaperManagerActivity-------setWallpaperPreview------currentGroup = "+currentGroup+" timeIsBlack = "+timeIsBlack);
    		if (mContext.getResources().getString(R.string.nextday_wallpaper_name).equals(currentGroup)) {
    			ArrayList<NextDayPictureInfo> mPictureList = ((ThemeManagerApplication) mContext.getApplicationContext()).getNextDayPictureInfoList();
    			if (mPictureList != null && mPictureList.size() != 0) {
    				NextDayPictureInfo pictureInfo = mPictureList.get(0);
    				
    				if ("white".equals(pictureInfo.getPictureTimeColor())) {
    					mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper);
    				} else {
    					mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper_black);
    				}
    			}
    			
			} else {
				timeIsBlack = (groupInfo.getIsTimeBlack() == 0)? false : true;
				if (timeIsBlack) {
					mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper_black);
		    	} else {
		    		mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper);
				}
			}
    		
		} else {
			currentGroup = Consts.DEFAULT_LOCKPAPER_GROUP;
			if (currentGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_1) || currentGroup.equals(Consts.BLACKSTAYLE_LOCKPAPER_GROUP_2)) {
	    		Log.d("Wallpaper_DEBUG", "WallpaperManagerActivity-------setWallpaperPreview----manager_lockpaper_black ");
				mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper_black);
			} else {
				Log.d("Wallpaper_DEBUG", "WallpaperManagerActivity-------setWallpaperPreview----------manager_lockpaper ");
				mManagerPreLockpaper.setImageResource(R.drawable.manager_lockpaper);
			}
		}
    	
    	mDbControl.close();
    	//shigq add end
    	
    	mManagerWallpaper.setImageDrawable(getWallpaper());
        mManagerLockpaper.setImageDrawable(WallpaperUtil.getLockScreenPre(mContext));
    }

	 // Aurora liugj 2014-09-24 deleted for bug-8151 start
    /*private void initWallpaperData() {
        mDataFactory = new AdapterDataFactory(mContext, Consts.WALLPAPER_LOCKSCREEN_TYPE);
        mDataFactory.initWallpaperItems();
        mDataFactory.clearData();
        mDataFactory = null;
    }*/
	 // Aurora liugj 2014-09-24 deleted for bug-8151 end

    @Override
    protected void onDestroy() {
        super.onDestroy();
		  // Aurora liugj 2014-11-13 add for bug-9260 start
        System.exit(0);
		  // Aurora liugj 2014-11-13 add for bug-9260 end
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mFromApp) {
            finish();
            overridePendingTransition(com.aurora.R.anim.aurora_activity_close_enter, com.aurora.R.anim.aurora_activity_close_exit);
        }
        return super.onKeyDown(keyCode, event);
    }

}

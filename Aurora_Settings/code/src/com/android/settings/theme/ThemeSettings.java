package com.android.settings.theme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.os.Bundle;
import android.os.PowerManager;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import com.android.settings.R;

import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBar.Type;
import android.provider.Settings;
import android.os.SystemProperties;
import android.provider.Settings;

public class ThemeSettings extends AuroraActivity {
	public static final String TAG = "ThemeSettings";
	public static final String Properties = "persist.sys.aurora.overlay";
	public static final String SYSTEM_THEME_WOMEN = "/system/theme/woman/";
	public static final String SYSTEM_THEME_KITTY = "/system/theme/kity/";
	public static final String SYSTEM_NORMAL_DESKTOP = "/system/iuni/aurora/change/desktop/normal";
	public static final String SYSTEM_WOMAN_DESKTOP = "/system/iuni/aurora/change/desktop/woman";
	public static final String SCHEMA = "file://";
	public static final String DEFAULT_WALLPAPER_NAME = "wallpaper_01.jpg";
	public static final String CLEAR_FILE_PATH = "/data/aurora/icons";
    public static final String THEME_DB = "iuni_theme";
    public static final String DEFAULT_THEME_DB = "kitty";
    
    public static final String THEME_KITTY = "kitty";
    public static final String THEME_HER = "her";
	
	private String mChoice;
	private ThemeItem mKittyItem;
	private ThemeItem mHerItem;

	private View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ThemeSettings.this,ThemePreview.class);
			
			if (v.getId() == R.id.kitty_image) {
				Log.v(TAG, "-------------kitty-------");
				intent.putExtra("theme_tpye", THEME_KITTY);
			} else if (v.getId() == R.id.her_image) {
				Log.v(TAG, "-------------her-------");
				intent.putExtra("theme_tpye", THEME_HER);
			}

			startActivity(intent);
		}
	};

	private CompoundButton.OnCheckedChangeListener mCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			// TODO Auto-generated method stub
			
			if (buttonView.getId() == R.id.kitty_checkbox) {
				Log.v(TAG, "------check-------kitty-----isChecked-----=="+isChecked);
				mKittyItem.setOnCheckedChangeListener(null);
				mKittyItem.setCheckBoxChecked(true);
				mKittyItem.setOnCheckedChangeListener(mCheckedChangeListener);
				if(isChecked){
					Settings.System.putString(getContentResolver(), THEME_DB ,SYSTEM_THEME_KITTY);
					mHerItem.setOnCheckedChangeListener(null);
					mHerItem.setCheckBoxChecked(false);
					mHerItem.setOnCheckedChangeListener(mCheckedChangeListener);
				}
			} else if (buttonView.getId() == R.id.her_checkbox) {
				Log.v(TAG, "------check-------her------isChecked----====="+isChecked);
				mHerItem.setOnCheckedChangeListener(null);
				mHerItem.setCheckBoxChecked(true);
				mHerItem.setOnCheckedChangeListener(mCheckedChangeListener);
				if(isChecked){
					Settings.System.putString(getContentResolver(), THEME_DB ,SYSTEM_THEME_WOMEN);
					mKittyItem.setOnCheckedChangeListener(null);
					mKittyItem.setCheckBoxChecked(!isChecked);
					mKittyItem.setOnCheckedChangeListener(mCheckedChangeListener);
				}
			}
		}

	};

	private OnAuroraActionBarBackItemClickListener actionBarItemClickListener = new OnAuroraActionBarBackItemClickListener() {
		@Override
		public void onAuroraActionBarBackItemClicked(int id) {
			// TODO Auto-generated method stub
			switch (id) {
			case OnAuroraActionBarBackItemClickListener.HOME_ITEM:
				if (isShowDialog()) {
					showAuroraDialog();
				}else{
					ThemeSettings.this.finish();
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
		setAuroraContentView(R.layout.theme_settings, Type.Normal);
		getAuroraActionBar().setTitle(R.string.theme_settings_title);
		getAuroraActionBar().setmOnActionBarBackItemListener(
				actionBarItemClickListener);

		mKittyItem = (ThemeItem) findViewById(R.id.kitty);
		mKittyItem.setOnClickListener(mOnClickListener);
		mKittyItem.setOnCheckedChangeListener(mCheckedChangeListener);

		mHerItem = (ThemeItem) findViewById(R.id.her);
		mHerItem.setOnClickListener(mOnClickListener);
		mHerItem.setOnCheckedChangeListener(mCheckedChangeListener);

		mChoice = SystemProperties.get(Properties,SYSTEM_THEME_KITTY);
		if(mChoice.equals(SYSTEM_THEME_KITTY)){
			mKittyItem.setCheckBoxChecked(true);
			mChoice = THEME_KITTY;
		}else if(mChoice.equals(SYSTEM_THEME_WOMEN)){
			mHerItem.setCheckBoxChecked(true);
			mChoice = THEME_HER;
		}
		
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_UP) {
			if (isShowDialog()) {
				showAuroraDialog();
				return true;
			}
		}

		return super.dispatchKeyEvent(event);
	}

	public boolean isShowDialog() {
		if ((mChoice.equals(THEME_KITTY) && mHerItem.isChecked())
				|| (mChoice.equals(THEME_HER) && mKittyItem.isChecked())) {
			return true;
		}
		return false;
	}

	public void setProp(){
		if(mChoice.equals(THEME_KITTY) && mHerItem.isChecked()){
			SystemProperties.set(Properties,SYSTEM_THEME_WOMEN);
		}else if (mChoice.equals(THEME_HER) && mKittyItem.isChecked()){
			SystemProperties.set(Properties,SYSTEM_THEME_KITTY);
		}
	}
	
	public void setWallpaperAndLockScreen(){
		String themeExtra = SYSTEM_THEME_KITTY;
		WallpaperManager wpm = (WallpaperManager)getSystemService(Context.WALLPAPER_SERVICE);
		try{
			String url = SCHEMA+"/system/iuni/aurora/change/desktop/"+DEFAULT_WALLPAPER_NAME;
			
			if(mChoice.equals(THEME_KITTY) && mHerItem.isChecked()){
				url = SCHEMA + SYSTEM_WOMAN_DESKTOP + DEFAULT_WALLPAPER_NAME;
				themeExtra = SYSTEM_THEME_WOMEN;
				
				Settings.System.putString(getContentResolver(), THEME_DB,SYSTEM_THEME_WOMEN);
			}else if(mChoice.equals(THEME_HER) && mKittyItem.isChecked()){
				url = SCHEMA + SYSTEM_NORMAL_DESKTOP + DEFAULT_WALLPAPER_NAME;
				themeExtra = SYSTEM_THEME_KITTY;
				
				Settings.System.putString(getContentResolver(), THEME_DB,SYSTEM_THEME_KITTY);
			}
			Log.v(TAG, "------setWallpaperAndLockScreen-----url-----===="+url);
			InputStream ins = new URL(url).openStream();
			wpm.setStream(ins);
		}catch(IOException e){
			Log.v(TAG, "------setWallpaperAndLockScreen----IOException--");
			e.printStackTrace();
		}
		
		Intent intent  = new Intent("com.aurora.loackscreen.wallpaperchanger");
		intent.putExtra("theme", themeExtra);
		sendBroadcast(intent);
	}
	
	public void cleanIcons(File file){
		try {
			if(file.isFile()){
	            file.delete();
	            return;
	        }
			
			if (file.isDirectory()) {
				File[] childFile = file.listFiles();
	            if(childFile == null || childFile.length == 0){
	                file.delete();
	                return;
	            }
	            for(File f : childFile){
	            	cleanIcons(f);
	            }
	            
	            Log.v(TAG, "---cleanIcons---");
	            file.delete();
			} 
		} catch (Exception e) {
			Log.v(TAG, "---cleanIcons----Exception-");
			e.printStackTrace();
		}
	}
	
	
	public void showAuroraDialog() {
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
				ThemeSettings.this);
		builder.setTitle(R.string.theme_dialog_title);
		builder.setMessage(R.string.theme_dialog_msg);
		builder.setNegativeButton(R.string.theme_dialog_negative,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						ThemeSettings.this.finish();
					}
				});
		builder.setPositiveButton(R.string.theme_dialog_positive,
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						setProp();
						setWallpaperAndLockScreen();
						cleanIcons(new File(CLEAR_FILE_PATH));
						PowerManager pManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
						pManager.reboot("");
					}
				});
		builder.show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v(TAG, "--------onDestroy--------");
	}

}

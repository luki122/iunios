package com.android.gallery3d.setting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import aurora.preference.AuroraMultiSelectListPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraSwitchPreference;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.Log;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.setting.tools.SettingLocalUtils;
import com.android.gallery3d.setting.widget.SeletUpdatePopupWindowUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity;
import com.baidu.xcloud.pluginAlbum.AccountProxy;

// 得到我们的存储Preferences值的对象，然后对其进行相应操作  
//        shp = PreferenceManager.getDefaultSharedPreferences(this);  
//        boolean apply_wifiChecked = shp.getBoolean("auto_upload", false); 

public class SettingsActivity extends AuroraPreferenceActivity implements OnPreferenceChangeListener, OnSharedPreferenceChangeListener,OnPreferenceClickListener{

	
	private ContentResolver cr;
	private static String TAG = "HelloPreference";            
    private AuroraPreference keySettingBlockManagement;       
    private AuroraPreference keySettingMyInfo;  
//    private AuroraSwitchPreference keySettingAutoUpload;         
    private AuroraPreference keySettingSelectUpdateAlbum;     
    private AuroraPreference keySettingUploadList;           
    private AuroraPreferenceCategory keySettingLoginInfo;     
	
    /**帐号中心返回的昵称*/
    public static final String ACCOUNT_INFO_NICK = "nick";
    /**帐号中心返回的图像URL*/
//    public static final String ACCOUNT_INFO_ICON_URL = "iconUrl";
    /**帐号中心返回的图像本地Path*/
    public static final String ACCOUNT_INFO_ICON_PATH = "iconPath";
	
    private final String AUTHORITY = "com.aurora.account.accountprovider";
	private final String ACCOUNT_CONTENT_URI = "content://" + AUTHORITY + "/account_info";
	
    public static final String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String dcimPath = sdPath + "/" + Environment.DIRECTORY_DCIM + "/";
	public static final String cameraPath = dcimPath + "Camera";
	public static final String screenShotsPath = sdPath + "/Screenshots";
	public static final String videoPath = "videoPath";
	public static final String collectionPath = "collectionPath";
	
	@Override
	protected void onCreate(Bundle arg0) {
		//TODO Auto-generated method stub
		super.onCreate(arg0);
		addPreferencesFromResource(R.xml.aurora_gallery_setting);
		initdata();
		cr = this.getContentResolver();
		update();
		
		SelectRoundDrawable mRoundDrawable = null;
		Drawable mDrawable =null;
		
		Bitmap bitmap=BitmapFactory.decodeFile(path);
		
		if(  bitmap!=null){
			mRoundDrawable = new SelectRoundDrawable(bitmap);
			keySettingMyInfo.setIcon(mRoundDrawable);
		}else{
			mDrawable = getResources().getDrawable(R.drawable.head_icon);
			keySettingMyInfo.setIcon(mDrawable);
		}
	
		keySettingMyInfo.setTitle(nick);
		
		if(mLoginStatus){
			keySettingLoginInfo.setEnabled(true);
		}else{
			getPreferenceScreen().removePreference(findPreference("login_info"));
		}
		
//		new AutoLoadTask().execute();
	}

	SharedPreferences shp;
	private void initdata() {
		keySettingBlockManagement = (AuroraPreference) findPreference("block_management"); 
		keySettingMyInfo = (AuroraPreference) findPreference("my_info"); 
//		keySettingAutoUpload = (AuroraSwitchPreference) findPreference("auto_upload"); 
		keySettingSelectUpdateAlbum = (AuroraPreference) findPreference("select_update_album"); 
		keySettingUploadList = (AuroraPreference) findPreference("upload_list"); 
		keySettingLoginInfo = (AuroraPreferenceCategory) findPreference("login_info"); 
		
		keySettingBlockManagement.setOnPreferenceClickListener(this); 
		keySettingMyInfo.setOnPreferenceClickListener(this); 
//		keySettingAutoUpload.setOnPreferenceClickListener(this); 
		keySettingSelectUpdateAlbum.setOnPreferenceClickListener(this); 
		keySettingUploadList.setOnPreferenceClickListener(this); 
	}

	String nick;
	String path;
	Boolean mLoginStatus =false;
	public void update() {
		try {
			Cursor cursor = cr.query(Uri.parse(ACCOUNT_CONTENT_URI), null, null,null, null);
			if (cursor != null && cursor.moveToFirst()) {
				mLoginStatus = cursor.getInt(cursor
						.getColumnIndex("hasLogin")) == 1 ? true : false;
				if(mLoginStatus){
					nick = cursor.getString(cursor.getColumnIndex(ACCOUNT_INFO_NICK));
					path = cursor.getString(cursor.getColumnIndex(ACCOUNT_INFO_ICON_PATH));
				}
			}
			if (cursor != null){
				cursor.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	@Override
	public boolean onPreferenceChange(AuroraPreference arg0, Object arg1) {
		//TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		//TODO Auto-generated method stub
		
	}

	@Override
	protected void onResume() {
		//TODO Auto-generated method stub
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
		.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onPause() {
		//TODO Auto-generated method stub
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
		.unregisterOnSharedPreferenceChangeListener(this);


	}

//	public class AutoLoadTask extends AsyncTask<Void, Void, Void> {
//		@Override
//		protected Void doInBackground(Void... arg0) {
//			update();
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void result) {
//			//TODO Auto-generated method stub
//			super.onPostExecute(result);
//			
//			SelectRoundDrawable mRoundDrawable = null;
//			Drawable mDrawable =null;
//			
//			Bitmap bitmap=BitmapFactory.decodeFile(path);
//			
//			if(  bitmap!=null){
//				mRoundDrawable = new SelectRoundDrawable(bitmap);
//				keySettingMyInfo.setIcon(mRoundDrawable);
//			}else{
//				mDrawable = getResources().getDrawable(R.drawable.head_icon);
//				keySettingMyInfo.setIcon(mDrawable);
//			}
//		
//			keySettingMyInfo.setTitle(nick);
//			
//			if(mLoginStatus){
//				keySettingLoginInfo.setEnabled(true);
//			}else{
//				getPreferenceScreen().removePreference(findPreference("login_info"));
//			}
//		}
//		
//	}
	
	@Override
	public boolean onPreferenceClick(AuroraPreference arg0) {
		//TODO Auto-generated method stub
		if(arg0.getKey().equals("block_management")){
			Intent it = new Intent("aurora.intent.action.shield");
			startActivity(it);
		}
		if(arg0.getKey().equals("my_info")){
			Uri uri = Uri.parse("openaccount://com.aurora.account.login");
			Intent intent = new Intent();
			intent.setData(uri);
			startActivity(intent);
		}
		if(arg0.getKey().equals("select_update_album")){
			SeletUpdatePopupWindowUtil mSeletUpdatePopupWindowUtil = SeletUpdatePopupWindowUtil.getInstance(this);
			mSeletUpdatePopupWindowUtil.showSelectPopupWindow(getResources().getString(R.string.aurora_selet_auto_updata_album), this.getCurrentFocus());
		}
		if(arg0.getKey().equals("upload_list")){
			Intent it = new Intent(this,UploadDownloadListActivity.class);
			startActivity(it);
		}
		
		return false;
	}
	

 
}

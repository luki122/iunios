package com.android.gallery3d.xcloudalbum;

import java.util.List;

import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.R;
import com.baidu.xcloud.account.AuthResponse;
import com.baidu.xcloud.account.IAuth;
import com.baidu.xcloud.account.IBindDetailListener;
import com.baidu.xcloud.pluginAlbum.AccountProxy;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

/**
 * 
 * @author wenyongzhe
 *2015.12.8 bind_account
 */
public class BIndAccountActivity extends Activity implements IBaiduinterface{

	private ContentResolver cr;
	private BaiduAlbumUtils mBaiduAlbumUtils;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aurora_cloud_bind_account);
		mBaiduAlbumUtils = BaiduAlbumUtils.getInstance(this);
		cr = this.getContentResolver();
		mBaiduAlbumUtils.setBaiduinterface(this);
		update();
	}

	@Override
	protected void onDestroy() {
		//TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath,
			CommonFileInfo info) {
		//TODO Auto-generated method stub
		
	}

	@Override
	public void loginComplete(boolean success) {
		//TODO Auto-generated method stub
		if(success){
			PrefUtil.setBoolean(this, "ACCOUNT_BIND", true);
		}
		
		finish();
	}

	private final String AUTHORITY = "com.aurora.account.accountprovider";
	private final String ACCOUNT_CONTENT_URI = "content://" + AUTHORITY
			+ "/account_info";
	
	public void update() {
		try {
			Cursor cursor = cr.query(Uri.parse(ACCOUNT_CONTENT_URI), null, null,
					null, null);
			if (cursor != null && cursor.moveToFirst()) {
				Boolean mLoginStatus = cursor.getInt(cursor
						.getColumnIndex("hasLogin")) == 1 ? true : false;
				if(mLoginStatus){
					String user_id = cursor.getString(cursor.getColumnIndex("user_id"));
					PrefUtil.setString(this, AlbumPage.PREF_KEY_IUNI_ACCOUNT_TOKEN, user_id);
					if(TextUtils.isEmpty(user_id)){
						if(!AccountProxy.getInstance().hasLogout()){
							BaiduAlbumUtils.getInstance(this).loginOutBaidu();
						}
					}else{
						mBaiduAlbumUtils.loginBaidu(user_id, false);
					}
				}
			}
			if (cursor != null){
				cursor.close();
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
}

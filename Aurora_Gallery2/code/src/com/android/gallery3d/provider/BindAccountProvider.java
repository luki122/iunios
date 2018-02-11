package com.android.gallery3d.provider;

import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.BIndAccountActivity;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadBroadcastReceiver;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadService;
import com.baidu.xcloud.account.AuthResponse;
import com.baidu.xcloud.account.IAuth;
import com.baidu.xcloud.account.IBindDetailListener;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

public class BindAccountProvider extends ContentProvider {
	
	private BaiduAlbumUtils mBaiduAlbumUtils;
	private Context mContext;
	 
	@Override
	public boolean onCreate() {
		//TODO Auto-generated method stub
		return false;
	}
   
    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
    		Bundle mBundle = new Bundle();
    		if(PrefUtil.getBoolean(getContext(), "ACCOUNT_BIND", false)){
    			mBundle.putBoolean("ACCOUNT_BIND", true);
    		}else{
    			mBundle.putBoolean("ACCOUNT_BIND", false);
    		}
    		 return mBundle;
    }

}

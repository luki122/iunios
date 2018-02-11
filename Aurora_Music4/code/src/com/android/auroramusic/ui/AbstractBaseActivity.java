package com.android.auroramusic.ui;



//import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.auroramusic.model.AuroraLoadingListener;
import com.android.music.MusicUtils;
import com.android.music.MusicUtils.DbChangeListener;



abstract public class AbstractBaseActivity extends BasicActivity implements DbChangeListener{

	private static final String TAG = "AbstractBaseActivity";
	private static final int AURORA_MSG_UPDATE_DB = 150;
	private static final int AURORA_MSG_NEEDUPDATE_DB = 151;
	
	abstract public void onMediaDbChange(boolean selfChange);
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		boolean flag = MusicUtils.registerDbObserver(getApplicationContext(), this);
		if (MusicUtils.mSongDb != null) {
			MusicUtils.mSongDb.setLoadingListener(mLoadingListener);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (MusicUtils.mSongDb != null) {
			MusicUtils.mSongDb.removesetLoadingListener(mLoadingListener);
		}
		mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
	}
	
	
	private final AuroraLoadingListener mLoadingListener = new AuroraLoadingListener() {
		
		@Override
		public void onNotNeedLoading() {
		}
		
		@Override
		public void onNeedLoading() {
			//Log.i(TAG, "zll ----- onNeedLoading xxxx 1");
			onMediaDbChange(false);
		}
	};
	
	@Override
	public void onDbChanged(boolean selfChange) {
		if (mUiHandler != null) {
			mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
			mUiHandler.sendMessage(mUiHandler.obtainMessage(AURORA_MSG_UPDATE_DB, selfChange) );
		}
	}

	private final Handler mUiHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AURORA_MSG_UPDATE_DB:
				boolean selfChange = ((Boolean)msg.obj).booleanValue();
				//Log.i(TAG, "zll ----- AbstractBaseActivity handleMessage selfChange:"+selfChange);
				if (MusicUtils.mSongDb != null) {
					MusicUtils.mSongDb.onContentDirty();
				}
				
				break;
				
			case AURORA_MSG_NEEDUPDATE_DB:
				break;

			default:
				break;
			}
		}
		
	};
	
	/*private final ContentObserver mAuroraObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange)
		{
			if (mUiHandler != null) {
				mUiHandler.removeMessages(AURORA_MSG_UPDATE_DB);
				mUiHandler.sendMessage(mUiHandler.obtainMessage(AURORA_MSG_UPDATE_DB, selfChange) );
			}
			
		}
	};
*/
}

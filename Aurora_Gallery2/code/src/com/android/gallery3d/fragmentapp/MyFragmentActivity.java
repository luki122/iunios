package com.android.gallery3d.fragmentapp;

import java.io.File;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.Type;
import com.android.gallery3d.R;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.AlbumSetPage;
import com.android.gallery3d.app.PhotoPage;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.fragmentutil.MySelfBuildConfig;
import com.android.gallery3d.fragmentutil.MyUtils;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.common.Utils;
import android.os.SystemProperties;


public class MyFragmentActivity extends AuroraActivity{

	private static final String 	TAG = "MyFragmentActivity";
	private GridViewFragment 		mFragment;
	public static final String KEY_GET_CONTENT = "get-content";
	public static final String KEY_TYPE_BITS = "type-bits";
	private boolean m_RegisterReceiver = false;
	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
	
	//home key 
	final String SYSTEM_DIALOG_REASON_KEY = "reason";  
	final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";  
	final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";  
	final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
	
	public static final int FRAGMENT_REQUEST_PHOTO = 1;
	private static final String STRFILE_SHARED = "myfragment";
	private static final String MYSDK_KEY = "iuni_sdk";

	
	private void getSystemVerson() {
		SharedPreferences settingSharedPreferences = getSharedPreferences(STRFILE_SHARED, Context.MODE_PRIVATE);
		Log.i(TAG, "zll getPacketVerson 1");
		if (!settingSharedPreferences.getString(MYSDK_KEY, "0.1").equals(getInternalVersion())) {
			Log.i(TAG, "zll getPacketVerson 3");
			SharedPreferences.Editor editor = settingSharedPreferences.edit();
			editor.putString(MYSDK_KEY, getInternalVersion());
			editor.commit();
			
			if (mFragment != null) {
				mFragment.clearCacheData();
			}
		} 
		
		return;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/*if (MySelfBuildConfig.DEBUG) {
			MyUtils.enableStrictMode();
        }*/
		
		super.onCreate(savedInstanceState);
		//Log.i(TAG, "zll ---- MyFragmentActivity Product Model: " + android.os.Build.MODEL + "," + android.os.Build.VERSION.SDK_INT + "," + android.os.Build.VERSION.RELEASE);
		//Log.i(TAG, "zll --- getInternalVersion:"+getInternalVersion());
		//getSystemVerson();
		
		MediaSetUtils.getCameraBucketID(this);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setAuroraContentView(R.layout.fragmentactivity, AuroraActionBar.Type.Empty, true);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
		if (savedInstanceState != null) {
			Log.i(TAG, "zll --- MyFragmentActivity onCreate from WidgetConfigure");
			initFragmentFromWidgetConfigure();
			return;
		}
		initdata();
	}
	
	private void initFragmentFromWidgetConfigure() {
		Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Bundle data = extras == null ? new Bundle() : new Bundle(extras);
        
        int typeBits = DataManager.INCLUDE_IMAGE;
        data.putBoolean(KEY_GET_CONTENT, true);
        data.putInt(KEY_TYPE_BITS, typeBits);
        
        if (getFragmentManager().findFragmentByTag(TAG) == null) {
    		
    		FragmentTransaction ft = getFragmentManager().beginTransaction();
    		mFragment = new GridViewFragment();
    		mFragment.setArguments(data);
    		View fragmentview = findViewById(R.id.fragment_container);
    		if (fragmentview != null) {
    			ft.add(R.id.fragment_container, mFragment, TAG);
        		ft.commit();
			}
        }
        
		return;
	}
	
	public static String getInternalVersion() {
		return SystemProperties.get("ro.gn.gnznvernumber");
	}
	
	private void initdata() {
		Intent intent = getIntent();
        String action = intent.getAction();
        
        Log.i(TAG, "zll --- initdata 1 action:"+action);
        if (Intent.ACTION_GET_CONTENT.equalsIgnoreCase(action)) {
        	Log.i(TAG, "zll --- initializeByIntent 2");
        	startFragmentGetContent(intent);	
        }else if ("android.intent.action.GN_GET_CONTENT".equalsIgnoreCase(action)) {
        	Log.i(TAG, "zll --- initializeByIntent 3");
        	startFragmentGetContent(intent);
        } else if (Intent.ACTION_PICK.equalsIgnoreCase(action)) {
            Log.i(TAG, "zllaction PICK is not supported");
            String type = Utils.ensureNotNull(intent.getType());
            if (type.startsWith("vnd.android.cursor.dir/")) {
                if (type.endsWith("/image")) intent.setType("image/*");
                if (type.endsWith("/video")) intent.setType("video/*");
            }
            startFragmentGetContent(intent);
        } else {
        	startFragmentDefaultPage();
		}
        
		return;
	}
	
	public void startFragmentDefaultPage() {
		if (getFragmentManager().findFragmentByTag(TAG) == null) {
    		FragmentTransaction ft = getFragmentManager().beginTransaction();
    		mFragment = new GridViewFragment();
    		View fragmentview = findViewById(R.id.fragment_container);
    		if (fragmentview != null) {
    			ft.add(R.id.fragment_container, mFragment, TAG);
        		ft.commit();
			}
        }
		
		return;
    }
	
	private void startFragmentGetContent(Intent intent) {
        Bundle data = intent.getExtras() != null
                ? new Bundle(intent.getExtras())
                : new Bundle();
                
        data.putBoolean(KEY_GET_CONTENT, true);
        int typeBits = GalleryUtils.determineTypeBits(this, intent);
        data.putInt(KEY_TYPE_BITS, typeBits);
        
        if (getFragmentManager().findFragmentByTag(TAG) == null) {
    		
    		FragmentTransaction ft = getFragmentManager().beginTransaction();
    		mFragment = new GridViewFragment();
    		mFragment.setArguments(data);
    		View fragmentview = findViewById(R.id.fragment_container);
    		if (fragmentview != null) {
    			ft.add(R.id.fragment_container, mFragment, TAG);
        		ft.commit();
			}
        }
        
        return;
    }
	
	private void InitSdIntentFilter() {
		IntentFilter mFilter = new IntentFilter();
		mFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		mFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		mFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		mFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		mFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		mFilter.addDataScheme("file");
		
		mFilter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
		//Log.i(TAG, "zll ---- InitSdIntentFilter");
		this.registerReceiver(m_SdBroadCastRec, mFilter);
		m_RegisterReceiver = true;
		
		return;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!m_RegisterReceiver) {
			InitSdIntentFilter();
		}
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
	}
	
	
	@Override
	protected void onPause() {
		super.onPause();
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		
		//Log.i(TAG, "zll --- MyFragmentActivity onPause");
		/*if (m_SdBroadCastRec != null && m_RegisterReceiver) {
			this.unregisterReceiver(m_SdBroadCastRec);
			m_RegisterReceiver = false;
		}*/
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (m_SdBroadCastRec != null && m_RegisterReceiver) {
			this.unregisterReceiver(m_SdBroadCastRec);
			m_RegisterReceiver = false;
		}
		m_RegisterReceiver = false;
	}
	
	private final BroadcastReceiver m_SdBroadCastRec = new BroadcastReceiver(){
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "zll --- onReceive 0 action:"+action);
			if(action.equals(Intent.ACTION_MEDIA_MOUNTED))
			{
				//Log.i(TAG, "zll --- onReceive 1");
				
			} else if(action.equals(Intent.ACTION_MEDIA_EJECT)
					/*||action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
					||action.equals(Intent.ACTION_MEDIA_BAD_REMOVAL)
					||action.equals(Intent.ACTION_MEDIA_REMOVED)*/)
			{
				//Log.i(TAG, "zll --- onReceive 2");
				if (mFragment != null) {
					mFragment.setDirtyContentObserver(0);
				}
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				//Log.i(TAG, "zll --- onReceive 3");
				
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
				
			} 
			
			if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
				String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
				Log.i(TAG, "zll action:" + action + ",reason:" + reason); 
				if (reason != null) {  
					//reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY) short press;
					//reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS) long press
				} 
			}
			
		}
	};
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//Log.i(TAG, "zll ----onKeyDown onBackPressed 1");
    	if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	        try {
	        	//Log.i(TAG, "zll ----onKeyDown onBackPressed 2");
	        	if (mFragment != null && mFragment.bDelteMenuShow) {
					mFragment.onKeyBackPressed();
					return true;
				}
	        } finally {
	        	
	        }
		} 

		//Log.i(TAG, "zll ----onKeyDown onBackPressed 4");
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
    protected void onSaveInstanceState(Bundle outState) {
		//Log.i(TAG, "zll ----onSaveInstanceState 1");
        try {
        	
        } finally {
        	
        }
    }
	
	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		//Log.i(TAG, "zll ----onUserLeaveHint 1");
		if (mFragment != null) {
			mFragment.setBackGroud(true);
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//Log.i(TAG, "zll -- onActivityResult requestCode:"+requestCode+",resultCode:"+resultCode);
		
        switch (requestCode) {
		case FRAGMENT_REQUEST_PHOTO:
			if (data == null) return;
			int mFocusIndex = data.getIntExtra(PhotoPage.KEY_RETURN_INDEX_HINT, 0);
			//Log.i(TAG, "zll -- onActivityResult mFocusIndex:"+mFocusIndex);
			if(mFragment != null)
			{
				mFragment.setFoucsIndex(mFocusIndex);//Iuni <lory><2014-01-07> add begin
			}
			
			break;

		default:
			break;
		}
        
    }
}

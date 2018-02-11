/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;


import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;
import android.Manifest.permission;
import aurora.app.AuroraActivity;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.os.Build;
import android.os.Process;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreference.OnPreferenceClickListener;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.app.INotificationManager;
import android.app.ProgressDialog;
import android.os.ServiceManager;
import android.os.SystemProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.android.settings.lscreen.ls.LSOperator;
import com.android.settings.widget.GnSwitchPreference;
import com.gionee.settings.push.PushApp;

import static com.gionee.settings.push.NotifyPushReceiver.GN_PUSH_APP_ADDED;
import static com.gionee.settings.push.NotifyPushReceiver.GN_PUSH_APP_REMOVED;

import com.aurora.utils.DensityUtil;

import aurora.widget.AuroraListView;
import android.app.Dialog;


// Gionee <liuran> <2013-3-11> add for CR00814184 begin
import static com.gionee.settings.push.NotifyPushReceiver.GN_PUSH_REFRESH;
// Gionee <liuran> <2013-3-11> add for CR00814184 end




















import com.android.settings.Utils;

import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.android.settings.AuroraIconUtils;

public class NotifyPushSettings extends SettingsPreferenceFragment 
		implements OnPreferenceChangeListener, DialogInterface.OnClickListener{

    private static final String TAG = "NotifyPush";

    private final String PREF_APP_ON_BOX = "pref_app_on_box";
    private final String SHARED_PREF_FILE = "notify_push_settings";
    private final String GIONEE_PUSH_PERMISSION = "com.gionee.cloud.permission.RECEIVE";
    
    
    private static final String DISPLAY="isdisplay";
    private static boolean isOpen=false;
    private final String DISPLAY_NETWORK_SPEED= "display_network_speed";
    private static final String TABLE_NETWORK_DISPLAY="isdisplay_network_speed";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String ACTION_NETWORKS_SPEED = "action_isdisplay_network_speed";
    private AuroraPreferenceCategory mAppOnBox;
    private PackageManager mPm;
    private PackageInfo mPackageInfo;
    private AuroraSwitchPreference mDisplay_net_Speed;
    public static final String ACTION_GPE_REG = "com.gionee.cloud.intent.REGISTER";
    public static final String ACTION_GPE_UNREG = "com.gionee.cloud.intent.UNREGISTER";
    public static final String EXTRA_PKG_NAME = "packagename";
    public static final String EXTRA_NEW_VALUE = "switch";
    
    private final String [] AURORA_PUSH_APP_LIST = new String [] {
        "com.sec.android.app.music",
        "com.android.chrome",
        "com.android.email",
        "com.samsung.everglades.video",
        "eu.chainfire.supersu",
    };
    private  List<PushPkgInfo> mPushPkgInfoList;
    private ExecutorService mExecutorService;
    private ListView mTempListView;
    private int listViemItemTop;
    private int position;
    private final String NOTIFYPUSH_PREFERENCES = "notifypushsettings_preferences";
    
    private LSOperator lsOperator;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       
       
        addPreferencesFromResource(R.xml.notify_push_settings);
        sharedPreferences=getActivity().getSharedPreferences(TABLE_NETWORK_DISPLAY, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
        editor=sharedPreferences.edit();
        isOpen=sharedPreferences.getBoolean(DISPLAY, false);
        imageCache = new HashMap<String, WeakReference<Drawable>>();
        
        mAppOnBox = (AuroraPreferenceCategory) findPreference(PREF_APP_ON_BOX);
//        mAppOnBox.setOrderingAsAdded(true);
        mAppOnBox.setOnPreferenceChangeListener(this);
        mDisplay_net_Speed=(AuroraSwitchPreference)findPreference(DISPLAY_NETWORK_SPEED);
        mDisplay_net_Speed.setChecked(isOpen);
        mDisplay_net_Speed.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(AuroraPreference preference, Object check) {
				boolean checked=((Boolean)check).booleanValue();
				Log.i("pgd", "onPreferenceChange ischecked : "+ checked);
                                // MonkeyTest 
                                if(getActivity()==null)
                                {
                                return false;
                                }
                                Intent intent = new Intent(ACTION_NETWORKS_SPEED);
				intent.putExtra(DISPLAY, checked);
				editor.putBoolean(DISPLAY, checked);
				editor.commit();                    
				getActivity().sendBroadcast(intent);
				return true;
			}
		});
        
        
        //keep this listener in whole lifetime,so when some dialog shows up which causes us 
        // to resume ,we won't reload app pref,so previous app list won't be changed
        IntentFilter pkgFilter = new IntentFilter();
        
        pkgFilter.addAction(GN_PUSH_APP_ADDED);
        pkgFilter.addAction(GN_PUSH_APP_REMOVED);
        // Gionee <liuran> <2013-3-11> add for CR00814184 begin
        pkgFilter.addAction(GN_PUSH_REFRESH);
		// Gionee <liuran> <2013-3-11> add for CR00814184 end

        //getActivity().registerReceiver(mPushReceiver, pkgFilter);
        
        mPm = getPackageManager();
        
        mExecutorService = Executors.newFixedThreadPool(20);
        if (mPm != null) {
            loadAppPrefs();
        }
        if(false)
        {
            if(Build.VERSION.RELEASE.contains("4.3") || Build.VERSION.RELEASE.contains("4.4"))
            {
            	lsOperator=new LSOperator(this);
            }else
            {
            	getPreferenceScreen().removePreference((AuroraPreferenceCategory)findPreference("pref_lscreen"));
            }
        }
        getPreferenceScreen().removePreference((AuroraPreferenceCategory)findPreference("pref_lscreen"));
    }
    
 // qy add 2014 06 17 begin
    private AuroraListView mListView;
    private ImageCallback imageCallback = new ImageCallback(){

		@Override
		public void imageLoaded(Drawable imageDrawable,
				String packageName) {
			// TODO Auto-generated method stub
			AuroraSwitchPreference sptemp = (AuroraSwitchPreference)findPreference(packageName);
			sptemp.setIcon(imageDrawable);
		}
    	
    };
    
   
    
    private int mfirstItem;
    private int mVisibleItemCount;
    private Drawable appIcon;
	private INotificationManager nm;
	private boolean isRunning = true;	
	
	
	
	Handler mNewprefHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			AuroraProgressDialogCategory prefDialog = (AuroraProgressDialogCategory)findPreference("progress_dialog");
			prefDialog.setVisible(false);
			getPreferenceScreen().removePreference(prefDialog);
			mPushPkgInfoList = (ArrayList<PushPkgInfo>)msg.obj;
			
			if (mPushPkgInfoList.size() <= 0)
	        {   
	            mAppOnBox.setTitle(R.string.pref_push_noapp_summary);
	        }else{   
	            mAppOnBox.setTitle(R.string.pref_push_summary);
	        }

			//整体加载		
						 
			boolean checked = false;
			for (int i = 0;i < mPushPkgInfoList.size();i++)
			{
				PackageInfo info = mPushPkgInfoList.get(i).pkgInfo;


				AuroraSwitchPreference sp = new AuroraSwitchPreference(getActivity());
		        AuroraPreferenceCategory boxToJoin = mAppOnBox;
	            sp.setOrder(boxToJoin.getPreferenceCount());
	            boxToJoin.addPreference(sp);
	            checked = false;
	            checked = android.app.GnINotificationManager.areNotificationsEnabledForPackage(info.applicationInfo.packageName, info.applicationInfo.uid, nm);
	                    
	            
	            sp.setChecked(checked);
	            sp.setKey(info.applicationInfo.packageName);
	            int px = DensityUtil.dip2px(getActivity(),48);
	            sp.setIconSize(px,px);
	            sp.setIcon(appIcon);
	            sp.setTitle(info.applicationInfo.loadLabel(mPm).toString());
	            sp.setOnPreferenceChangeListener(NotifyPushSettings.this);            
			
			}
			
			
			int initNum = mPushPkgInfoList.size()>20 ? 20 : mPushPkgInfoList.size();
			for (int i = 0;i < initNum;i++)
			{
				PackageInfo info = mPushPkgInfoList.get(i).pkgInfo;
				
	            Drawable icon = loadDrawable(getActivity(),info.applicationInfo.packageName,imageCallback);
	            if(icon !=null){
	            	 AuroraSwitchPreference sp = (AuroraSwitchPreference)findPreference(info.applicationInfo.packageName);

						sp.setIcon(icon);
	            }
	           
				
			}
			
			
		}
	};
	
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setOnListScrollChangeListener(new aurora.preference.AuroraPreferenceFragment.ListScrollChangeListener (){

			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				Log.i("qy----" +TAG, "scrollState = "+scrollState);				
				
				if(scrollState == 0){
					Log.i("qy----" +TAG, "keySet =  "+imageCache.keySet().size());
										
					int cacheNum = 15;
					int num = mfirstItem+mVisibleItemCount +cacheNum < mPushPkgInfoList.size() ? mfirstItem+mVisibleItemCount :mPushPkgInfoList.size(); // add cacheNum
					int first = mfirstItem > cacheNum ? mfirstItem-cacheNum :mfirstItem;
					for (int i =first;i < num;i++)
					{
						PackageInfo info = mPushPkgInfoList.get(i).pkgInfo;
						
						
			            Drawable icon = loadDrawable(getActivity(),info.applicationInfo.packageName,imageCallback);
			            if(icon !=null){
			            	 AuroraSwitchPreference sp = (AuroraSwitchPreference)findPreference(info.applicationInfo.packageName);

								sp.setIcon(icon);							

			            }
			           
						
					}
					
					if(mfirstItem > cacheNum){
						for(int j = 0;j< first;j++){
							PackageInfo ifo = mPushPkgInfoList.get(j).pkgInfo;
							AuroraSwitchPreference sptemp = (AuroraSwitchPreference)findPreference(ifo.applicationInfo.packageName);

							sptemp.setIcon(appIcon);

							if (imageCache.containsKey(ifo.applicationInfo.packageName)) {
								imageCache.remove(ifo.applicationInfo.packageName);
							}
							
						}
					}
					
					
					for(int k = num;k< mPushPkgInfoList.size();k++){
						PackageInfo ifo = mPushPkgInfoList.get(k).pkgInfo;
						AuroraSwitchPreference sptemp = (AuroraSwitchPreference)findPreference(ifo.applicationInfo.packageName);

						sptemp.setIcon(appIcon);

						if (imageCache.containsKey(ifo.applicationInfo.packageName)) {
							imageCache.remove(ifo.applicationInfo.packageName);
						}
						
					}
					
					System.gc();
				}
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				Log.i("qy----" +TAG, "firstVisibleItem = "+firstVisibleItem +"\n"+
						"visibleItemCount = "+visibleItemCount+"\n"+
						"totalItemCount = "+totalItemCount);
				mfirstItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
								
			}
		
		});
    

		/*mListView = getListView();
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mListView.setOnScrollListener(new OnScrollListener() {
			
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub
				Log.i("qy----" +TAG, "scrollState = "+scrollState);				
				
				if(scrollState == 0){
					Log.i("qy----" +TAG, "keySet =  "+imageCache.keySet().size());
										
					int cacheNum = 15;
					int num = mfirstItem+mVisibleItemCount +cacheNum < mPushPkgInfoList.size() ? mfirstItem+mVisibleItemCount :mPushPkgInfoList.size(); // add cacheNum
					int first = mfirstItem > cacheNum ? mfirstItem-cacheNum :mfirstItem;
					for (int i =first;i < num;i++)
					{
						PackageInfo info = mPushPkgInfoList.get(i).pkgInfo;
						
						
			            Drawable icon = loadDrawable(getActivity(),info.applicationInfo.packageName,imageCallback);
			            if(icon !=null){
			            	 AuroraSwitchPreference sp = (AuroraSwitchPreference)findPreference(info.applicationInfo.packageName);

								sp.setIcon(icon);							

			            }
			           
						
					}
					
					if(mfirstItem > cacheNum){
						for(int j = 0;j< first;j++){
							PackageInfo ifo = mPushPkgInfoList.get(j).pkgInfo;
							AuroraSwitchPreference sptemp = (AuroraSwitchPreference)findPreference(ifo.applicationInfo.packageName);

							sptemp.setIcon(appIcon);

							if (imageCache.containsKey(ifo.applicationInfo.packageName)) {
								imageCache.remove(ifo.applicationInfo.packageName);
							}
							
						}
					}
					
					
					for(int k = num;k< mPushPkgInfoList.size();k++){
						PackageInfo ifo = mPushPkgInfoList.get(k).pkgInfo;
						AuroraSwitchPreference sptemp = (AuroraSwitchPreference)findPreference(ifo.applicationInfo.packageName);

						sptemp.setIcon(appIcon);

						if (imageCache.containsKey(ifo.applicationInfo.packageName)) {
							imageCache.remove(ifo.applicationInfo.packageName);
						}
						
					}
					
					System.gc();
				}
			}
			
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				Log.i("qy----" +TAG, "firstVisibleItem = "+firstVisibleItem +"\n"+
						"visibleItemCount = "+visibleItemCount+"\n"+
						"totalItemCount = "+totalItemCount);
				mfirstItem = firstVisibleItem;
				mVisibleItemCount = visibleItemCount;
								
			}
		});*/
		
	}
   // qy add 2014 06 17 end

   
    
	@Override
    public void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    }
	
	@Override
	public void onResume()
	{
		super.onResume();
		if(lsOperator!=null)
		{
			lsOperator.onResume();
		}
	}

    @Override
    public void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	imageCache.clear();
    	isRunning = false;
    	
    	if(lsOperator!=null)
    	{
    		lsOperator.onDestory();
    	}
    	//getActivity().unregisterReceiver(mPushReceiver);
    }
   
	class PushPkgInfo 
	{
		PackageInfo pkgInfo;
		String label;
	}
		
	
	
    private void loadAppPrefs()
    {
    	mAppOnBox.removeAll();
    	
		mPushPkgInfoList = new ArrayList<PushPkgInfo>();
    	/*List<PackageInfo> pkgList = mPm.getInstalledPackages(0);

        for (int i = 0;i < pkgList.size();i++)
        {
            PackageInfo info = pkgList.get(i);
            if (((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                    || isAuroraPushApp(info))//3rd or aurora_push apps
            //if (!isThisASystemPackage(info)) //all app
            {
                PushPkgInfo pushInfo = new PushPkgInfo();
                pushInfo.pkgInfo = info;
                pushInfo.label = info.applicationInfo.loadLabel(mPm).toString();
                mPushPkgInfoList.add(pushInfo);
            }
        }
        Collections.sort(mPushPkgInfoList, sDisplayNameComparator);*/
        
        // qy 20 14 06 17 begin 
		
        new Thread() {
		@Override
		public void run() {
			List<PackageInfo> pkgList = mPm.getInstalledPackages(0);
			List<PushPkgInfo> pushPkgList = new ArrayList<PushPkgInfo>();
			for (int i = 0;i < pkgList.size();i++)
	        {
				if(!isRunning){
					break;
				}
	            PackageInfo info = pkgList.get(i);
	            if ((((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
	                    || isAuroraPushApp(info)) && info.applicationInfo.enabled)	           
	            {
	                PushPkgInfo pushInfo = new PushPkgInfo();
	                pushInfo.pkgInfo = info;
	                pushInfo.label = info.applicationInfo.loadLabel(mPm).toString();
	                pushPkgList.add(pushInfo);
	            }
	        }
			Collections.sort(pushPkgList, sDisplayNameComparator);
			if(isRunning){				
				mNewprefHandler.sendMessage(mNewprefHandler.obtainMessage(10, pushPkgList));
			}
			
		}
        }.start();
       
        // qy 20 14 06 17 end
        


		
		boolean checked = false; 
		// 2014 06 16 begin
		nm = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		appIcon = getActivity().getResources().getDrawable(R.drawable.aurora_def_app_icon);
		/*for (int i = 0;i < mPushPkgInfoList.size();i++)
		{
			PackageInfo info = mPushPkgInfoList.get(i).pkgInfo;

//			GnSwitchPreference sp = new GnSwitchPreference(getActivity());
			AuroraSwitchPreference sp = new AuroraSwitchPreference(getActivity());
	        AuroraPreferenceCategory boxToJoin = mAppOnBox;
            sp.setOrder(boxToJoin.getPreferenceCount());
            boxToJoin.addPreference(sp);
            checked = false;
            checked = android.app.GnINotificationManager.areNotificationsEnabledForPackage(info.applicationInfo.packageName, info.applicationInfo.uid, nm);
        
 */
		// 2014 06 16 end
            //always set state after sp has been added to preference category
			
//            try
//            {
//                checked = android.app.GnINotificationManager.areNotificationsEnabledForPackage(info.applicationInfo.packageName, info.applicationInfo.uid, nm);
//            	checked = nm.areNotificationsEnabledForPackage(info.applicationInfo.packageName);
//                checked = nm.areNotificationsEnabledForPackage(info.applicationInfo.packageName, info.uid);
//            }
//            catch (android.os.RemoteException ex)
//            {
            	// this does not
//         	}
       

    }
    
    private final static Comparator<PushPkgInfo> sDisplayNameComparator
            = new Comparator<PushPkgInfo>() {
         public final int
         compare(PushPkgInfo a, PushPkgInfo b) {
             return collator.compare(Utils.getSpell(a.label), Utils.getSpell(b.label));
         }
 
         private final Collator collator = Collator.getInstance();
     };

	private boolean queryPushState(PackageInfo pi)
	{
		boolean checked = false;

		return checked;
	}

    private boolean isAuroraPushApp(PackageInfo pi)
    {
        //Log.v("xiaoyong", "isAuroraPushApp packageName = " + pi.applicationInfo.packageName);
        //Log.v("xiaoyong", "label = " + pi.applicationInfo.loadLabel(mPm).toString());
        //Log.v("xiaoyong", "isAuroraPushApp className = " + pi.applicationInfo.className);
        for (String appPkgName : AURORA_PUSH_APP_LIST)
        {
            if (pi.applicationInfo.packageName.equals(appPkgName))
                return true;
        }

        return false;
    }

    private boolean isThisASystemPackage(PackageInfo pkginfo) {
        PackageInfo info = null;
        try {
            info = mPm.getPackageInfo(pkginfo.applicationInfo.packageName,
                    PackageManager.GET_DISABLED_COMPONENTS |
                    PackageManager.GET_UNINSTALLED_PACKAGES |
                    PackageManager.GET_SIGNATURES);
       } catch (NameNotFoundException e) {
          Log.e(TAG, "Exception when retrieving package:" + "pkginfo.applicationInfo.packageName");
       }

        try {
            PackageInfo sys = mPm.getPackageInfo("android", PackageManager.GET_SIGNATURES);
            if (sys != null && info != null) {
                //Log.v(TAG, "info = " + info + "pkginfo.signatures[0] = " + info.signatures[0] + "sys.signatures[0] = " + sys.signatures[0]);// + "pkginfo.signatures[0] = " + pkginfo.signatures[0]);
            }
            return (pkginfo != null && info.signatures != null &&
                sys.signatures[0].equals(info.signatures[0]));
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference)
	{
		boolean checked = false;
		checked = ((AuroraSwitchPreference)preference).isChecked();

 		//Log.v(TAG, "onPreferenceTreeClick checked = " + checked);

		return true;
	}

	public boolean onPreferenceClick(AuroraPreference preference)
	{
		//Log.v(TAG, "onPreferenceClick");

		return false;
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
		// TODO Auto-generated method stub
		if (Utils.isMonkeyRunning()) {
			Log.d(TAG, "ignoring monkey's attempt to onPreferenceChange");
			return true;
		}
		boolean checked = ((Boolean)newValue).booleanValue();
		// test
		/*if(preference instanceof AuroraSwitchPreference ){
			if((((AuroraSwitchPreference)preference).isChecked()) == checked){
				return false;
			}
		}*/
		
//		final ContentResolver cr = getContentResolver();
	
		//Log.v(TAG, "onPreferenceChange");

//		boolean checked = false;
//		checked = ((AuroraSwitchPreference)preference).isChecked();
		//Log.v(TAG, "onPreferenceChange checked = " + checked + "  getKey() = " + preference.getKey());

		INotificationManager nm = INotificationManager.Stub.asInterface(ServiceManager.getService(Context.NOTIFICATION_SERVICE));
		try
		{
//			nm.setNotificationsEnabledForPackage(preference.getKey(), checked);	
            //getOrder replace uid
//            android.app.GnINotificationManager.setNotificationsEnabledForPackage(preference.getKey(), 
//                    ((AuroraSwitchPreference)preference).getPreferenceUid(), checked, nm);
		
		ApplicationInfo applicationInfo = getApplicationInfo(getActivity(),preference.getKey());
		if(applicationInfo != null){
			Log.i("qy", "checked ==" +checked);
			android.app.GnINotificationManager.setNotificationsEnabledForPackage(preference.getKey(), 
	        		applicationInfo.uid, checked, nm);
			}
        
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return true;
	}

	public void onClick(DialogInterface dialog, int which) 
	{
	}

    private ResolveInfo getPackageFirstResolveInfo(String packageName){
        Intent it = new Intent(Intent.ACTION_MAIN);
        it.addCategory(Intent.CATEGORY_LAUNCHER);
        it.setPackage(packageName);
        List<ResolveInfo> list = mPm.queryIntentActivities(it, 0);
        ResolveInfo resolveInfo = null;
        if (!list.isEmpty()) {
            resolveInfo = list.get(0);
        }
        return resolveInfo;
    }
    
    // qy add start 2014 04 15
    private HashMap<String, WeakReference<Drawable>> imageCache;
    /**
     * 获取应用的icon
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized Drawable getApkIcon(Context context ,String packageName){
	
		Drawable icon = GetLauncherAppIconUtils.getSystemIconDrawableByPackage(context,packageName);
    	if(icon != null){        		
    		return icon;
    	}
    	
    	
    	ApplicationInfo applicationInfo = getApplicationInfo(context,packageName);
    	if(applicationInfo == null){
			return null;
		}
    		
        PackageManager pm = context.getPackageManager();   
        //下面这行代码，在E6机器上如果快速多次调用，会出现程序异常卡死退出（即使放在子线程也同样如此），让人蛋疼
        return pm.getApplicationIcon(applicationInfo);
    }
   
    /**
     * 根据包名获取ApplicationInfo
     * @param context
     * @param packageName
     * @return
     */
    public static synchronized ApplicationInfo getApplicationInfo(Context context,String packageName){
    	if(context == null || packageName == null){
    		return null;
    	}
    	ApplicationInfo appinfo = null;
		try {
			appinfo = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return appinfo;
    } 
    
    public Drawable loadDrawable(final Context context,final String packageName,
			
			final ImageCallback imageCallback) {

		if (imageCache.containsKey(packageName)) {
			WeakReference<Drawable> softReference = imageCache.get(packageName);
			if (softReference != null) {
				Drawable drawable = softReference.get();
				if (drawable != null) {
					return drawable;
				}
			}
		}
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				Drawable icon =(Drawable)message.obj;
				if(icon == null || imageCache == null){
					return ;
				}
				
				imageCache.put(packageName, new WeakReference<Drawable>(icon));
				if(imageCallback != null){
					imageCallback.imageLoaded(icon, packageName);	
				}  
			}
		};
		
		mExecutorService.submit(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Drawable drawable = getApkIcon(context, packageName);
				Message message = handler.obtainMessage(0, drawable);				
				handler.sendMessage(message);
				
			}
			
		});
		
		
		return null;
	}
	
	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable,String packageName);
	}
	// qy add end 2014 04 15


}

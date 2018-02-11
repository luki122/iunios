package com.aurora.voiceassistant.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.LiveFolders;
import android.util.Log;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreferenceScreen;
import aurora.preference.AuroraSwitchPreference;

import com.aurora.voiceassistant.R;
import com.aurora.voiceassistant.account.TotalCount;
import com.aurora.voiceassistant.model.CFG;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;

//shigq add the implements OnSharedPreferenceChangeListener
public class SettingActivity extends AuroraPreferenceActivity implements OnSharedPreferenceChangeListener
{
	private String path = "";
	
	private static final String VOPEN_KEY = "vopen";
	private static final String WAKEUP_KEY = "wakeup";
	
	private Map<String, Boolean> key_values = new HashMap<String, Boolean>();
	
	private static final String pckage = "com.qualcomm.listen.voicewakeup";
	private PackageInfo pacinfo;
	
	//定时器
	private static AtomicInteger WATI = new AtomicInteger();
	private static boolean isChanging = false;
	private static String ikey = "";
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// 所的的值将会自动保存到SharePreferences
		addPreferencesFromResource(R.xml.vs_preference);
		
		//M:shigq set status bar background as transparent for its flashing on Android5.0 platform begin
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
		getWindow().setStatusBarColor(Color.TRANSPARENT);
		//M:shigq set status bar background as transparent for its flashing on Android5.0 platform end
		
		key_values.clear();
		path = "/data/data/"+this.getPackageName()+"/switch.txt";
		
		//初始化
		key_values.put("vopen", PreferenceManager.getDefaultSharedPreferences(this).getBoolean("vopen", false));
		key_values.put("wakeup", PreferenceManager.getDefaultSharedPreferences(this).getBoolean("wakeup", false));
		
		//shigq
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		
		try {
			pacinfo = SettingActivity.this.getPackageManager().getPackageInfo(pckage, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(pacinfo != null){
		
			//U3
			AuroraPreferenceScreen pre = (AuroraPreferenceScreen) findPreference("to_wakeup");
			pre.setOnPreferenceClickListener(new AuroraPreference.OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(AuroraPreference arg0) {
					// TODO Auto-generated method stub
					try {
						if(pacinfo != null){
							Intent intent = new Intent();
							ComponentName cpone = new ComponentName(pckage, pckage+".ux10.HomeActivity");
							intent.setComponent(cpone);
							intent.setAction("android.intent.action.VIEW");
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
							SettingActivity.this.startActivity(intent);
							
							//Intent intent = SettingActivity.this.getPackageManager().getLaunchIntentForPackage(pckage);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}
			});
			
			
		}else{
		
			//U2
			AuroraPreferenceScreen ss =	(AuroraPreferenceScreen) findPreference("groups");
			AuroraPreferenceCategory cate = (AuroraPreferenceCategory) findPreference("wakeup_group");
			ss.removePreference(cate);
		}
	}

	//shigq
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		//account start
		if ("vopen".equals(key)) {
			TotalCount mTotalCount = new TotalCount(this, CFG.ACCOUNT_MODULE_ID, CFG.ACCOUNT_ACTION_VOICE_SWITCH, 1);
			mTotalCount.CountData();
		}
		//account end
		
		//将值保存
		key_values.put(key, sharedPreferences.getBoolean(key, false));
		ikey = key;
		synchronized (SettingActivity.this) {
			//第一次肯定是false
			if(SettingActivity.isChanging){
				SettingActivity.WATI.set(2);
			}else{
				SettingActivity.isChanging = true;
				SettingActivity.WATI.set(2);
				new Thread(){
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						while(SettingActivity.WATI.get() > 0){
							try {
								sleep(500);
							} catch (Exception e) {
								// TODO: handle exception
							}
							SettingActivity.WATI.decrementAndGet();
						}
						//等待执行
						set(ikey);
						SettingActivity.isChanging = false;
					}
				}.start();
			}
		}
	
	}

	private void set(String key){
		/*if(path != null && !path.equals("")){
			createFile(path, key_values);
		}*/
		
		ActivityManager am = (ActivityManager) SettingActivity.this.getSystemService(SettingActivity.this.ACTIVITY_SERVICE);
		List<RunningServiceInfo> slist = am.getRunningServices(Integer.MAX_VALUE);
		//由开到关的状态
		if(key.equals("wakeup") && !key_values.get(key) ){
			for(RunningServiceInfo rs : slist){
				//com.qualcomm.listen.voicewakeup:::true
				Log.v("iht", rs.process + "------>"+rs.started);
				if(rs.process.equals("com.qualcomm.listen.voicewakeup") && rs.started){
					Intent intent = new Intent("com.aurora.voiceassistant.UNSERVICE"); 
					SettingActivity.this.sendBroadcast(intent);
				}
			}
		}
		
		//启动
		if(key.equals("wakeup") && key_values.get(key)){
			Intent intent = new Intent("com.aurora.voiceassistant.STSERVICE"); 
			SettingActivity.this.sendBroadcast(intent);
		}
	}
	
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		if(path != null && !path.equals("")){
			createFile(path, key_values);
		}
		//System.exit(0);
	}
	
	private void createFile(String path, Map<String, Boolean> key_values){
		BufferedWriter bwrite = null;
		try {	
			File file = new File(path);
			if(!file.exists()){
				file.createNewFile();
			}
			bwrite = new BufferedWriter(new FileWriter(file));
			if(!key_values.isEmpty()){
				Iterator ite = key_values.entrySet().iterator();
				while(ite.hasNext()){
					Entry entry = (Entry) ite.next();
					bwrite.write(entry.getKey() + ":"+ entry.getValue()+"\n");
				}
			}
			bwrite.flush();
			bwrite.close();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			try {
				if(bwrite != null){
					bwrite.flush();
					bwrite.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}


}
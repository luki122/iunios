/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.Dialog;
import aurora.app.AuroraProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.preference.*;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import  com.android.settings.AuroraApnPreference;

import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import android.view.KeyEvent;

import java.util.ArrayList;



// add by steve.tang 2014-06-03, start.
import android.os.SystemProperties;
import gionee.telephony.GnTelephonyManager;

import com.codeaurora.telephony.msim.GnMSimPhoneFactory;
// add by steve.tang 2014-06-03, end.

public class ApnSettings extends AuroraPreferenceActivity implements
        AuroraPreference.OnPreferenceChangeListener {
    static final String TAG = "ApnSettings";

    public static final String EXTRA_POSITION = "position";
    public static final String RESTORE_CARRIERS_URI =
        "content://telephony/carriers/restore";
    public static final String PREFERRED_APN_URI =
        "content://telephony/carriers/preferapn";

    // add by pgd 2015-01-06, start.
    public static final String MTK_PREFERRED_APN_URI_SIM1="content://telephony/carriers_sim1/preferapn";
    public static final String MTK_PREFERRED_APN_URI_SIM2="content://telephony/carriers_sim2/preferapn";

    // add by pgd 2015-01-06, end.
	// add by steve.tang 2014-06-03, start.

	private Uri mPreferApnUri;
	private Uri mRestoreCarriersUri;
	private int mSubscription = 0; // slot id
    public static final String OPERATOR_NUMERIC_EXTRA = "operator"; 
	private static final boolean MULTI_SIM_SUPPORT = GnTelephonyManager.isMultiSimEnabled();

	// add by steve.tang 2014-06-03, end.


    public static final String APN_ID = "apn_id";

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int TYPES_INDEX = 3;

    private static final int MENU_NEW = Menu.FIRST;
    private static final int MENU_RESTORE = Menu.FIRST + 1;

    private static final int EVENT_RESTORE_DEFAULTAPN_START = 1;
    private static final int EVENT_RESTORE_DEFAULTAPN_COMPLETE = 2;

    private static final int DIALOG_RESTORE_DEFAULTAPN = 1001;

    private static final Uri DEFAULTAPN_URI = Uri.parse(RESTORE_CARRIERS_URI);
    private static final Uri PREFERAPN_URI = Uri.parse(PREFERRED_APN_URI);
	

	private static final int MENU_ID_ADD_APN = Menu.FIRST + 3;

	public static final String ACTION_UPDATE_APN_SELECTED = "com.android.settings.ACTION_UPDATE_APN_SELECTED";
	

    private static boolean mRestoreDefaultApnMode;

    private RestoreApnUiHandler mRestoreApnUiHandler;
    private RestoreApnProcessHandler mRestoreApnProcessHandler;
    private HandlerThread mRestoreDefaultApnThread;
    

    private String mSelectedKey;
    private boolean isFindLastApn;
    private IntentFilter mMobileStateFilter;

    private final BroadcastReceiver mMobileStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED)) {
                PhoneConstants.DataState state = getMobileDataState(intent);
                switch (state) {
                case CONNECTED:
                    if (!mRestoreDefaultApnMode) {
                        fillList();
                    } else {
                        showDialog(DIALOG_RESTORE_DEFAULTAPN);
                    }
                    break;
                }
            }
            if(intent.getAction().equals(ACTION_UPDATE_APN_SELECTED)){
            	AuroraApnPreference.setSelectedKey( getSelectedApnKey());
            	boolean isFindLastApn = false;
            	for (int i = 0, count = getPreferenceScreen().getPreferenceCount(); i < count; i++) {
                    
                    if(getPreferenceScreen().getPreference(i) instanceof AuroraApnPreference){
                    	AuroraApnPreference pref = (AuroraApnPreference)(getPreferenceScreen().getPreference(i));
                    	
                    	if (pref.getcurrentKey().equals(getSelectedApnKey()) ){
                            pref.setChecked(true);
                            isFindLastApn = true;
                            break;
                        }
                    }
                    
	            }
            	// not find set this 
            	if(!isFindLastApn){
            		AuroraApnPreference pref = (AuroraApnPreference)(getPreferenceScreen().getPreference(1));
            		pref.setChecked(true);
            	}
            	
	        }
	    }
	};
    
    private OnAuroraActionBarBackItemClickListener auroActionBarItemBackListener = new OnAuroraActionBarBackItemClickListener() {
		public void onAuroraActionBarBackItemClicked(int itemId) {
			switch (itemId) {
			case -1:
				// setMenuEnable(true);
//				Toast.makeText(AuroraRingPickerActivity.this, "getCancelBtn",
//						 Toast.LENGTH_SHORT).show();
				
				finish();
				overridePendingTransition(R.anim.aurora_close_enter,R.anim.aurora_close_exit);
				break;
			
			default:
				break;
			}
		}
	};
	
	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case MENU_ID_ADD_APN:
				addNewApn();
				break;
			default:
				break;
			}
		}
	};
    

    private static PhoneConstants.DataState getMobileDataState(Intent intent) {
        String str = intent.getStringExtra(PhoneConstants.STATE_KEY);
        if (str != null) {
            return Enum.valueOf(PhoneConstants.DataState.class, str);
        } else {
            return PhoneConstants.DataState.DISCONNECTED;
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        // Gionee fangbin 20120619 added for CR00622030 start
        /*if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }*/
        // Gionee fangbin 20120619 added for CR00622030 end
//    	setTheme(R.style.AuroraApnTheme);
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.apn_settings);
        
        //Gionee:zhang_xin 20121215 add for CR00746521 start
	//AURORA-START::delete temporarily for compile::waynelin::2013-9-14 
        /*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setTitle(R.string.apn_settings);
        //AURORA-END::delete temporarily for compile::waynelin::2013-9-14
        getAuroraActionBar().addItem(R.drawable.aurora_wifi_add, MENU_ID_ADD_APN, null);
        getAuroraActionBar().setmOnActionBarBackItemListener(auroActionBarItemBackListener);
        getAuroraActionBar().setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        //Gionee:zhang_xin 20121215 add for CR00746521 end
        
        getListView().setItemsCanFocus(true);

		//steve.tang 2014-06-05 add for slot2 apn settings, start.
        
        /*
         *   1 is card 2;
         *   0 is card 1; 
         */
	mSubscription = getIntent().getIntExtra("subscription", GnMSimPhoneFactory.getDefaultSubscription());
        Log.d(TAG, "onCreate received sub :" + mSubscription + "  MULTI_SIM_SUPPORT="+MULTI_SIM_SUPPORT);
        
        if (MULTI_SIM_SUPPORT) 
        {
	//  mPreferApnUri = Uri.parse(PREFERRED_APN_URI + "/" + mSubscription);	
            if(mSubscription==0)
            {
                mPreferApnUri=Uri.parse(MTK_PREFERRED_APN_URI_SIM1);
            }else if(mSubscription==1)
            {
                mPreferApnUri=Uri.parse(MTK_PREFERRED_APN_URI_SIM2);
            }
            AuroraApnPreference.setCurrentSubid(mSubscription);
        } else {
            mPreferApnUri = Uri.parse(PREFERRED_APN_URI);
        }
		//steve.tang 2014-06-05 add for slot2 apn settings, start.
        Log.d(TAG, "Preferred APN Uri is set to '" + mPreferApnUri.toString() + "'");
        mMobileStateFilter = new IntentFilter();
        mMobileStateFilter.addAction(TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);//TelephonyIntents.ACTION_ANY_DATA_CONNECTION_STATE_CHANGED);
        mMobileStateFilter.addAction(ACTION_UPDATE_APN_SELECTED);
        registerReceiver(mMobileStateReceiver, mMobileStateFilter);
        // qy add  
        AuroraApnPreference.setSelectedKey( getSelectedApnKey());
		
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "Restore Default Apn Mode '" + mRestoreDefaultApnMode + "'");

        if (!mRestoreDefaultApnMode) {
            fillList();
        } else {
            showDialog(DIALOG_RESTORE_DEFAULTAPN);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mMobileStateReceiver);

        if (mRestoreDefaultApnThread != null) {
            mRestoreDefaultApnThread.quit();
        }
        // release resources
        for (int i = 0, count = getPreferenceScreen().getPreferenceCount(); i < count; i++) {
            AuroraPreference pref = getPreferenceScreen().getPreference(i);
            if (pref instanceof AuroraApnPreference) {
            	((AuroraApnPreference)pref).releaseResource();
            }
        }

        
    }

    private void fillList() {
        String where = getOperatorNumericSelection();
		if(where == null || TextUtils.isEmpty(where)) {
			android.util.Log.e(TAG, "Where is mull: " + (where == null));
			return ;
		}
        Cursor cursor = getContentResolver().query(Telephony.Carriers.CONTENT_URI, new String[] {
                "_id", "name", "apn", "type"}, where, null,
                Telephony.Carriers.DEFAULT_SORT_ORDER);

		if (cursor == null) return;
		AuroraPreferenceGroup apnList = (AuroraPreferenceGroup) findPreference("apn_list");
        apnList.removeAll();
        
        ArrayList<AuroraPreference> mmsApnList = new ArrayList<AuroraPreference>();

        mSelectedKey = getSelectedApnKey();
        // qy add  
//        AuroraApnPreference.setSelectedKey(mSelectedKey);
        
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String name = cursor.getString(NAME_INDEX);
            String apn = cursor.getString(APN_INDEX);
            String key = cursor.getString(ID_INDEX);
            String type = cursor.getString(TYPES_INDEX);

//            ApnPreference pref = new ApnPreference(this);
            AuroraApnPreference pref = new AuroraApnPreference(this);
            

            pref.setKey(key);
            pref.setcurrentKey(key);
            pref.setTitle(name);
            pref.setSummary(apn);
            pref.setPersistent(false);
            pref.setOnPreferenceChangeListener(this);           


            boolean selectable = ((type == null) || !type.equals("mms"));
//            pref.setSelectable(selectable);  //modify
            if (selectable) {
//                if ((mSelectedKey != null) && mSelectedKey.equals(key)) {
//                    pref.setChecked(true);
//                }else{
//                	pref.setChecked(false);
//                }
                apnList.addPreference(pref);
            } else {
            	// qy add 
            	AuroraPreference prefTemp = new  AuroraPreference(this);
            	prefTemp.setKey(key);
            	prefTemp.setTitle(name);
            	prefTemp.setSummary(apn);
            	prefTemp.setPersistent(false);
            	prefTemp.setOnPreferenceChangeListener(this); // end 
            	
                mmsApnList.add(prefTemp);
            }
            cursor.moveToNext();
        }
        cursor.close();
        
        for (AuroraPreference preference : mmsApnList) {
            apnList.addPreference(preference);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //Gionee <chenml> <2013-08-19> modify for CR00857897 begin
        menu.add(0, MENU_NEW, 0,getResources().getString(R.string.menu_new))
                .setIcon(android.R.drawable.ic_menu_add)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, MENU_RESTORE, 0,
                getResources().getString(R.string.menu_restore))
                .setIcon(android.R.drawable.ic_menu_upload)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        //Gionee <chenml> <2013-08-19> modify for CR00857897 end
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_NEW:
            addNewApn();
            return true;

        case MENU_RESTORE:
            restoreDefaultApn();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addNewApn() {
        
		// Modify by steve.tang 2014-06-02, make is support single/dual sim. start. 
		//startActivity(new Intent(Intent.ACTION_INSERT, Telephony.Carriers.CONTENT_URI));

		Intent intent = new Intent(Intent.ACTION_INSERT, Telephony.Carriers.CONTENT_URI);
		String numeric = null ;
		if(MULTI_SIM_SUPPORT){
		    numeric = getOperatorNumeric()[0] ;
		} else {
			numeric = SystemProperties.get(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC);
		}
		if(numeric == null) return ;
		intent.putExtra(OPERATOR_NUMERIC_EXTRA, numeric);
	    startActivity(intent);

		// Modify by steve.tang 2014-06-02, make is support single/dual sim. end. 
    }

    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
    	
        int pos = Integer.parseInt(preference.getKey());
        Log.d(TAG,"onclick preference is ="+preference.toString() + "  pos="+pos);
        Uri url = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI, pos);
        
//        startActivity(new Intent(Intent.ACTION_EDIT, url));
        // qy add 
        
        if(preference instanceof AuroraApnPreference){
        	if(((AuroraApnPreference)preference).getcurrentKey().equals(AuroraApnPreference.mSelectedKey)){
        		((AuroraApnPreference)preference).setChecked(true);
            }else{
            	((AuroraApnPreference)preference).setChecked(false);
            }
        } else{
        	startActivity(new Intent(Intent.ACTION_EDIT, url));
        }
      //end 
        
        return true;
    }

    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {

// remove by steve.tang 2014-06-03, wrong newvalue. start.
/*        if (preference == null || newValue == null) return true;
        Log.d(TAG, "onPreferenceChange(): AuroraPreference - " + preference
                + ", newValue - " + newValue + ", newValue type - "
                + newValue.getClass());
        if (newValue instanceof String) {
            setSelectedApnKey((String) newValue);
        }
*/
// remove by steve.tang 2014-06-03, end.
        return true;
    }
    
    /*
     * 选择APN
     */
    private void setSelectedApnKey(String key) {
		Log.e(TAG,"setSelectedApnKey:mPreferApnUri is: " + mPreferApnUri);
        mSelectedKey = key;
        ContentResolver resolver = getContentResolver();

        ContentValues values = new ContentValues();
        values.put(APN_ID, mSelectedKey);
        resolver.update(mPreferApnUri, values, null, null);
    }

    private String getSelectedApnKey() {
        String key = null;
		Log.e(TAG,"getSelectedApnKey:mPreferApnUri is: " + mPreferApnUri);
        Cursor cursor = getContentResolver().query(mPreferApnUri, new String[] {"_id"},
                null, null, Telephony.Carriers.DEFAULT_SORT_ORDER);
		if(cursor == null) return key;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            key = cursor.getString(ID_INDEX);
        }
        cursor.close();
		Log.e(TAG,"getSelectedApnKey:key is: " + key);
        return key;
    }

    private boolean restoreDefaultApn() {
        showDialog(DIALOG_RESTORE_DEFAULTAPN);
        mRestoreDefaultApnMode = true;

        if (mRestoreApnUiHandler == null) {
            mRestoreApnUiHandler = new RestoreApnUiHandler();
        }

        if (mRestoreApnProcessHandler == null ||
            mRestoreDefaultApnThread == null) {
            mRestoreDefaultApnThread = new HandlerThread(
                    "Restore default APN Handler: Process Thread");
            mRestoreDefaultApnThread.start();
            mRestoreApnProcessHandler = new RestoreApnProcessHandler(
                    mRestoreDefaultApnThread.getLooper(), mRestoreApnUiHandler);
        }

        mRestoreApnProcessHandler
                .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_START);
        return true;
    }

    private class RestoreApnUiHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_COMPLETE:
                    fillList();
                    getPreferenceScreen().setEnabled(true);
                    mRestoreDefaultApnMode = false;
                    dismissDialog(DIALOG_RESTORE_DEFAULTAPN);
                    Toast.makeText(
                        ApnSettings.this,
                        getResources().getString(
                                R.string.restore_default_apn_completed),
                        Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    private class RestoreApnProcessHandler extends Handler {
        private Handler mRestoreApnUiHandler;

        public RestoreApnProcessHandler(Looper looper, Handler restoreApnUiHandler) {
            super(looper);
            this.mRestoreApnUiHandler = restoreApnUiHandler;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_RESTORE_DEFAULTAPN_START:
                    ContentResolver resolver = getContentResolver();
                    resolver.delete(mRestoreCarriersUri, null, null);                    
                    mRestoreApnUiHandler
                        .sendEmptyMessage(EVENT_RESTORE_DEFAULTAPN_COMPLETE);
                    break;
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            AuroraProgressDialog dialog = new AuroraProgressDialog(this);
            dialog.setMessage(getResources().getString(R.string.restore_default_apn));
            dialog.setCancelable(false);
            return dialog;
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == DIALOG_RESTORE_DEFAULTAPN) {
            getPreferenceScreen().setEnabled(false);
        }
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_BACK:
    		
    		finish();
    		overridePendingTransition(R.anim.aurora_close_enter,R.anim.aurora_close_exit);
    		return true;
    	default:
    			
			return super.onKeyDown(keyCode, event);
    	}
    }
	

	//add by steve.tang 2014-06-02 get selection. start.

	private String getOperatorNumericSelection() {
        String[] mccmncs = getOperatorNumeric();
        String where;
        where = (mccmncs[0] != null) ? "numeric=\"" + mccmncs[0] + "\"" : "";
        where += (mccmncs[1] != null) ? " or numeric=\"" + mccmncs[1] + "\"" : "";
        Log.d(TAG, "getOperatorNumericSelection: " + where);
        return where;
    } 
    //  TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC ==gsm.sim.operator.numeric;
    private String[] getOperatorNumeric() {
        ArrayList<String> result = new ArrayList<String>();
        String mccMncFromSim =null;
        Log.d(TAG, " mSubscription ="+mSubscription);
        if(mSubscription==0)
        {
        	 mccMncFromSim = getTelephonyProperty(TelephonyProperties.PROPERTY_ICC_OPERATOR_NUMERIC, mSubscription, null);
        }else
        {
        	 mccMncFromSim = getTelephonyProperty("gsm.sim.operator.numeric.2", mSubscription, null);
        }
        if (mccMncFromSim != null && mccMncFromSim.length() > 0) {
            result.add(mccMncFromSim);
        }
        return result.toArray(new String[2]);
    }

    public static String getTelephonyProperty(String property, int index, String defaultVal) {
        String propVal = null;
        String prop = SystemProperties.get(property);

		Log.e(TAG, "the prop is: " + prop);

        if ((prop != null) && (prop.length() > 0)) {
            String values[] = prop.split(",");
/*            if ((index >= 0) && (index < values.length) && (values[index] != null)) {
                propVal = values[index];
            }*/
            propVal=values[0];
        }
		Log.e(TAG, "the propVal is: " + propVal);
        return propVal == null ? defaultVal : propVal;
    }
 	//add by steve.tang 2014-06-02 get selection. end.   
}

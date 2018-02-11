/*
 * Copyright (C) 2006 The Android Open Source Project
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


import aurora.app.AuroraAlertDialog;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import aurora.preference.AuroraEditTextPreference;
import aurora.preference.AuroraListPreference;
import aurora.preference.AuroraCheckBoxPreference;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceActivity;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import aurora.widget.CustomAuroraActionBarItem;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.RILConstants;
import com.android.internal.telephony.TelephonyProperties;

import aurora.widget.CustomAuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;


public class ApnEditor extends AuroraPreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener,
                    AuroraPreference.OnPreferenceChangeListener ,View.OnClickListener{

    private final static String TAG = ApnEditor.class.getSimpleName();

    private final static String SAVED_POS = "pos";
    private final static String KEY_AUTH_TYPE = "auth_type";
    private final static String KEY_PROTOCOL = "apn_protocol";
    private final static String KEY_ROAMING_PROTOCOL = "apn_roaming_protocol";
    private final static String KEY_CARRIER_ENABLED = "carrier_enabled";
    private final static String KEY_BEARER = "bearer";

    private static final int MENU_DELETE = Menu.FIRST;
    private static final int MENU_SAVE = Menu.FIRST + 1;
    private static final int MENU_CANCEL = Menu.FIRST + 2;
    private static final int ERROR_DIALOG_ID = 0;

    private static String sNotSet;
    private AuroraApnEditTextPreference mName;
    private AuroraApnEditTextPreference mApn;
    private AuroraApnEditTextPreference mProxy;
    private AuroraApnEditTextPreference mPort;
    private AuroraApnEditTextPreference mUser;
    private AuroraApnEditTextPreference mServer;
    private AuroraApnEditTextPreference mPassword;
    private AuroraApnEditTextPreference mMmsc;
    private AuroraApnEditTextPreference mMcc;
    private AuroraApnEditTextPreference mMnc;
    private AuroraApnEditTextPreference mMmsProxy;
    private AuroraApnEditTextPreference mMmsPort;
    private AuroraListPreference mAuthType;
    private AuroraApnEditTextPreference mApnType;
    private AuroraListPreference mProtocol;
    private String mProtocolLastValue;
    private boolean mIsAuthTypeModify;
    private boolean mIsProtocolModify;
    private boolean mIsRoamingProtocolModify;
    private boolean mIsBearerModify;
    private String mAuthTypeLastValue;
    private String mRoamingProtocolLastValue;
    private String mBearerLastValue;
    private AuroraListPreference mRoamingProtocol;
    private AuroraCheckBoxPreference mCarrierEnabled;
    private AuroraListPreference mBearer;
    private boolean mIsAdd;
    private String mCurMnc;
    private String mCurMcc;

    private Uri mUri;
    private Cursor mCursor;
    private boolean mNewApn;
    private boolean mFirstTime;
    private Resources mRes;
	private View mSaveBtn;
    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] sProjection = new String[] {
            Telephony.Carriers._ID,     // 0
            Telephony.Carriers.NAME,    // 1
            Telephony.Carriers.APN,     // 2
            Telephony.Carriers.PROXY,   // 3
            Telephony.Carriers.PORT,    // 4
            Telephony.Carriers.USER,    // 5
            Telephony.Carriers.SERVER,  // 6
            Telephony.Carriers.PASSWORD, // 7
            Telephony.Carriers.MMSC, // 8
            Telephony.Carriers.MCC, // 9
            Telephony.Carriers.MNC, // 10
            Telephony.Carriers.NUMERIC, // 11
            Telephony.Carriers.MMSPROXY,// 12
            Telephony.Carriers.MMSPORT, // 13
            Telephony.Carriers.AUTH_TYPE, // 14
            Telephony.Carriers.TYPE, // 15
            Telephony.Carriers.PROTOCOL, // 16
            Telephony.Carriers.CARRIER_ENABLED, // 17
            Telephony.Carriers.BEARER, // 18
            Telephony.Carriers.ROAMING_PROTOCOL // 19
    };

    private static final int ID_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int APN_INDEX = 2;
    private static final int PROXY_INDEX = 3;
    private static final int PORT_INDEX = 4;
    private static final int USER_INDEX = 5;
    private static final int SERVER_INDEX = 6;
    private static final int PASSWORD_INDEX = 7;
    private static final int MMSC_INDEX = 8;
    private static final int MCC_INDEX = 9;
    private static final int MNC_INDEX = 10;
    private static final int MMSPROXY_INDEX = 12;
    private static final int MMSPORT_INDEX = 13;
    private static final int AUTH_TYPE_INDEX = 14;
    private static final int TYPE_INDEX = 15;
    private static final int PROTOCOL_INDEX = 16;
    private static final int CARRIER_ENABLED_INDEX = 17;
    private static final int BEARER_INDEX = 18;
    private static final int ROAMING_PROTOCOL_INDEX = 19;
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {
			switch (itemId) {
			case MENU_SAVE:
				if (validateAndSave(false)) {
	                finish();
	            }
				break;
			default:
				break;
			}
		}
	};

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {
		//
				@Override
				public void auroraMenuItemClick(int itemId) {
					Log.i("qy", "onKeyDown()");
					switch (itemId) {
					case R.id.remove_apn:
						sendBroadcast(new Intent(ApnSettings.ACTION_UPDATE_APN_SELECTED));
						deleteApn();
					}
				}
			};

	@Override
    protected void onCreate(Bundle icicle) {
        // Gionee fangbin 20120619 added for CR00622030 start
        if (GnSettingsUtils.getThemeType(getApplicationContext()).equals(GnSettingsUtils.TYPE_LIGHT_THEME)){
            setTheme(R.style.GnSettingsLightTheme);
        } else {
            setTheme(R.style.GnSettingsDarkTheme);
        }
        // Gionee fangbin 20120619 added for CR00622030 end
        super.onCreate(icicle);
      
        /*
        getAuroraActionBar().setDisplayShowHomeEnabled(false);
	*/
        getAuroraActionBar().setDisplayHomeAsUpEnabled(true);
        
        AuroraApnEditTextPreference.mIsModify =false;
        addPreferencesFromResource(R.xml.apn_editor);

        sNotSet = getResources().getString(R.string.apn_not_set);
        mName = (AuroraApnEditTextPreference) findPreference("apn_name");        
        
        mApn = (AuroraApnEditTextPreference) findPreference("apn_apn");
        mProxy = (AuroraApnEditTextPreference) findPreference("apn_http_proxy");
        mPort = (AuroraApnEditTextPreference) findPreference("apn_http_port");
        mUser = (AuroraApnEditTextPreference) findPreference("apn_user");
        mServer = (AuroraApnEditTextPreference) findPreference("apn_server");
        mPassword = (AuroraApnEditTextPreference) findPreference("apn_password");
        mMmsProxy = (AuroraApnEditTextPreference) findPreference("apn_mms_proxy");
        mMmsPort = (AuroraApnEditTextPreference) findPreference("apn_mms_port");
        mMmsc = (AuroraApnEditTextPreference) findPreference("apn_mmsc");
        mMcc = (AuroraApnEditTextPreference) findPreference("apn_mcc");
        mMnc = (AuroraApnEditTextPreference) findPreference("apn_mnc");
        mApnType = (AuroraApnEditTextPreference) findPreference("apn_type");

        mAuthType = (AuroraListPreference) findPreference(KEY_AUTH_TYPE);
        mAuthType.setOnPreferenceChangeListener(this);

        mProtocol = (AuroraListPreference) findPreference(KEY_PROTOCOL);
        mProtocol.setOnPreferenceChangeListener(this);

        mRoamingProtocol = (AuroraListPreference) findPreference(KEY_ROAMING_PROTOCOL);
        mRoamingProtocol.setOnPreferenceChangeListener(this);

        mCarrierEnabled = (AuroraCheckBoxPreference) findPreference(KEY_CARRIER_ENABLED);

        mBearer = (AuroraListPreference) findPreference(KEY_BEARER);
        mBearer.setOnPreferenceChangeListener(this);

        mRes = getResources();

        final Intent intent = getIntent();
        final String action = intent.getAction();

        mFirstTime = icicle == null;
        // qy add
//        getAuroraActionBar().addItem(R.drawable.aurora_wifi_save_pressed,MENU_SAVE,null);
//        getAuroraActionBar().setOnAuroraActionBarListener(auroraActionBarItemClickListener);
        getAuroraActionBar().addItem(R.layout.aurora_actionbar_apn_save, 0);
        CustomAuroraActionBarItem item = (CustomAuroraActionBarItem)getAuroraActionBar().getItem(0);
        View view = item.getItemView();
        mSaveBtn =  view.findViewById(R.id.btn_save);
        mSaveBtn.setOnClickListener(ApnEditor.this);

        if (action.equals(Intent.ACTION_EDIT)) {
        	// qy start
        	getAuroraActionBar().setTitle(R.string.apn_edit); 
        	setAuroraMenuCallBack(auroraMenuCallBack);
        	setAuroraMenuItems(R.menu.apn);
        	
        	// end
        	 
            mUri = intent.getData();
        } else if (action.equals(Intent.ACTION_INSERT)) {
        	getAuroraActionBar().setTitle(R.string.apn_add);
        	mIsAdd = true;
        	
            if (mFirstTime || icicle.getInt(SAVED_POS) == 0) {
                mUri = getContentResolver().insert(intent.getData(), new ContentValues());
            } else {
                mUri = ContentUris.withAppendedId(Telephony.Carriers.CONTENT_URI,
                        icicle.getInt(SAVED_POS));
            }
            mNewApn = true;
            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.w(TAG, "Failed to insert new telephony provider into "
                        + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            finish();
            return;
        }

        mCursor = managedQuery(mUri, sProjection, null, null);
        mCursor.moveToFirst();

        //fillUi();
        fillUi(intent.getStringExtra(ApnSettings.OPERATOR_NUMERIC_EXTRA));
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    private void fillUi(String numeric) {
        if (mFirstTime) {
            mFirstTime = false;
            // Fill in all the values from the db in both text editor and summary
            mName.setText(mCursor.getString(NAME_INDEX));
            mApn.setText(mCursor.getString(APN_INDEX));
            mProxy.setText(mCursor.getString(PROXY_INDEX));
            mPort.setText(mCursor.getString(PORT_INDEX));
            mUser.setText(mCursor.getString(USER_INDEX));
            mServer.setText(mCursor.getString(SERVER_INDEX));
            mPassword.setText(mCursor.getString(PASSWORD_INDEX));
            mMmsProxy.setText(mCursor.getString(MMSPROXY_INDEX));
            mMmsPort.setText(mCursor.getString(MMSPORT_INDEX));
            mMmsc.setText(mCursor.getString(MMSC_INDEX));
            mMcc.setText(mCursor.getString(MCC_INDEX));
            mMnc.setText(mCursor.getString(MNC_INDEX));
            mApnType.setText(mCursor.getString(TYPE_INDEX));
            if (mNewApn) {
                // MCC is first 3 chars and then in 2 - 3 chars of MNC
                if (numeric != null && numeric.length() > 4) {
                    // Country code
                    String mcc = numeric.substring(0, 3);
                    // Network code
                    String mnc = numeric.substring(3);
                    // Auto populate MNC and MCC for new entries, based on what SIM reports
                    mMcc.setText(mcc);
                    mMnc.setText(mnc);
                    mCurMnc = mnc;
                    mCurMcc = mcc;
                }
            }
            int authVal = mCursor.getInt(AUTH_TYPE_INDEX);
            if (authVal != -1) {
                mAuthType.setValueIndex(authVal);
            } else {
                mAuthType.setValue(null);
            }

            mProtocol.setValue(mCursor.getString(PROTOCOL_INDEX));
            mRoamingProtocol.setValue(mCursor.getString(ROAMING_PROTOCOL_INDEX));
            mCarrierEnabled.setChecked(mCursor.getInt(CARRIER_ENABLED_INDEX)==1);
            mBearer.setValue(mCursor.getString(BEARER_INDEX));
            
            
        }

        mName.setSummary(checkNull(mName.getText()));
        mApn.setSummary(checkNull(mApn.getText()));
        mProxy.setSummary(checkNull(mProxy.getText()));
        mPort.setSummary(checkNull(mPort.getText()));
        mUser.setSummary(checkNull(mUser.getText()));
        mServer.setSummary(checkNull(mServer.getText()));
        mPassword.setSummary(starify(mPassword.getText()));
        mMmsProxy.setSummary(checkNull(mMmsProxy.getText()));
        mMmsPort.setSummary(checkNull(mMmsPort.getText()));
        mMmsc.setSummary(checkNull(mMmsc.getText()));
        mMcc.setSummary(checkNull(mMcc.getText()));
        mMnc.setSummary(checkNull(mMnc.getText()));
        mApnType.setSummary(checkNull(mApnType.getText()));

        String authVal = mAuthType.getValue();
        if (authVal != null) {
            int authValIndex = Integer.parseInt(authVal);
            mAuthType.setValueIndex(authValIndex);

            String []values = mRes.getStringArray(R.array.apn_auth_entries);
            mAuthType.setSummary(values[authValIndex]);
            mAuthTypeLastValue = values[authValIndex];
        } else {
            mAuthType.setSummary(sNotSet);
            mAuthTypeLastValue = sNotSet;
        }

        mProtocol.setSummary(
                checkNull(protocolDescription(mProtocol.getValue(), mProtocol)));
        mRoamingProtocol.setSummary(
                checkNull(protocolDescription(mRoamingProtocol.getValue(), mRoamingProtocol)));
        mBearer.setSummary(
                checkNull(bearerDescription(mBearer.getValue())));
        // qy
        mProtocolLastValue = checkNull(protocolDescription(mProtocol.getValue(), mProtocol));
        mRoamingProtocolLastValue = checkNull(protocolDescription(mRoamingProtocol.getValue(), mRoamingProtocol));
        mBearerLastValue = checkNull(bearerDescription(mBearer.getValue()));
        
    }

    /**
     * Returns the UI choice (e.g., "IPv4/IPv6") corresponding to the given
     * raw value of the protocol preference (e.g., "IPV4V6"). If unknown,
     * return null.
     */
    private String protocolDescription(String raw, AuroraListPreference protocol) {
        int protocolIndex = protocol.findIndexOfValue(raw);
        if (protocolIndex == -1) {
            return null;
        } else {
            String[] values = mRes.getStringArray(R.array.apn_protocol_entries);
            try {
                return values[protocolIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    private String bearerDescription(String raw) {
        int mBearerIndex = mBearer.findIndexOfValue(raw);
        if (mBearerIndex == -1) {
            return null;
        } else {
            String[] values = mRes.getStringArray(R.array.bearer_entries);
            try {
                return values[mBearerIndex];
            } catch (ArrayIndexOutOfBoundsException e) {
                return null;
            }
        }
    }

    public boolean onPreferenceChange(AuroraPreference preference, Object newValue) {
        String key = preference.getKey();
        if (KEY_AUTH_TYPE.equals(key)) {
            try {
                int index = Integer.parseInt((String) newValue);
                mAuthType.setValueIndex(index);

                String []values = mRes.getStringArray(R.array.apn_auth_entries);
                mAuthType.setSummary(values[index]);
                
                if(mAuthTypeLastValue.equals(values[index])){
                	mIsAuthTypeModify = false;
                }else{
                	mIsAuthTypeModify = true;
                }
                
            } catch (NumberFormatException e) {
                return false;
            }
        } else if (KEY_PROTOCOL.equals(key)) {
            String protocol = protocolDescription((String) newValue, mProtocol);
            if (protocol == null) {
                return false;
            }
            mProtocol.setSummary(protocol);
            mProtocol.setValue((String) newValue);
            //qy
            
            if(mProtocolLastValue.equals(protocol)){
            	mIsProtocolModify = false;
            }else{
            	mIsProtocolModify = true;
            }
            
        } else if (KEY_ROAMING_PROTOCOL.equals(key)) {
            String protocol = protocolDescription((String) newValue, mRoamingProtocol);
            if (protocol == null) {
                return false;
            }
            mRoamingProtocol.setSummary(protocol);
            mRoamingProtocol.setValue((String) newValue);
            
            if(mRoamingProtocolLastValue.equals(protocol)){
            	mIsRoamingProtocolModify = false;
            }else{
            	mIsRoamingProtocolModify = true;
            }
            
        } else if (KEY_BEARER.equals(key)) {
            String bearer = bearerDescription((String) newValue);
            if (bearer == null) {
                return false;
            }
            mBearer.setValue((String) newValue);
            mBearer.setSummary(bearer);
            
            if(mBearerLastValue.equals(bearer)){
            	mIsBearerModify = false;
            }else{
            	mIsBearerModify = true;
            }
        }

        return true;
    }

  /*  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // If it's a new APN, then cancel will delete the new entry in onPause
        if (!mNewApn) {
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete)
                .setIcon(R.drawable.ic_menu_delete_holo_dark);
        }
        menu.add(0, MENU_SAVE, 0, R.string.menu_save)
            .setIcon(android.R.drawable.ic_menu_save);
        menu.add(0, MENU_CANCEL, 0, R.string.menu_cancel)
            .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }*/

   /* @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_DELETE:
            deleteApn();
            return true;
        case MENU_SAVE:
            if (validateAndSave(false)) {
                finish();
            }
            return true;
        case MENU_CANCEL:
            if (mNewApn) {
                getContentResolver().delete(mUri, null, null);
            }
            finish();
            return true;
        //Gionee <chenml> <2013-04-10> add for CR00795611 begin
        case android.R.id.home:
            onBackPressed();
            break;
        //Gionee <chenml> <2013-04-10> add for CR00795611 end
        default: break;
        }
        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	
        switch (keyCode) {
        
            case KeyEvent.KEYCODE_BACK: {
                /*if (validateAndSave(false)) {
                    finish();
                } */ // qy 
            	
            	if( AuroraApnEditTextPreference.mIsModify || mIsProtocolModify || mIsRoamingProtocolModify
            		|| mIsBearerModify || mIsAuthTypeModify	){
            		if(mIsAdd){
            			//.setNegativeButton(textId, listener).setPositiveButton(textId, listener).setMessage(messageId)
            			new AlertDialog.Builder(this).setTitle(R.string.apn_add).setMessage(R.string.apn_add_message)
            			.setPositiveButton(R.string.apn_cancel, new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								
				                
							}
            				
            			})
            			.setNegativeButton(R.string.apn_ok, new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								onBackPressed();
				                
							}
            				
            			}).create().show();
            			
            		}else{
            			
            			new AlertDialog.Builder(this).setTitle(R.string.apn_edit).setMessage(R.string.apn_edit_message)
            			.setPositiveButton(R.string.apn_cancel, new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								
				                
							}
            				
            			})
            			.setNegativeButton(R.string.apn_ok, new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								onBackPressed();
				                
							}
            				
            			}).create().show();
            		}
        		// if no modify
            	}else{
            		onBackPressed();
            	}
            	
                return true;
            }
        }
//        return super.onKeyDown(keyCode, event);
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (validateAndSave(true)) {
            icicle.putInt(SAVED_POS, mCursor.getInt(ID_INDEX));
        }
    }

    /**
     * Check the key fields' validity and save if valid.
     * @param force save even if the fields are not valid, if the app is
     *        being suspended
     * @return true if the data was saved
     */
    private boolean validateAndSave(boolean force) {
        String name = checkNotSet(mName.getText());
        String apn = checkNotSet(mApn.getText());
        String mcc = checkNotSet(mMcc.getText());
        String mnc = checkNotSet(mMnc.getText());

        if (getErrorMsg() != null && !force) {
            showDialog(ERROR_DIALOG_ID);
            return false;
        }

        if (!mCursor.moveToFirst()) {
            Log.w(TAG,
                    "Could not go to the first row in the Cursor when saving data.");
            return false;
        }

        // If it's a new APN and a name or apn haven't been entered, then erase the entry
        if (force && mNewApn && name.length() < 1 && apn.length() < 1) {
            getContentResolver().delete(mUri, null, null);
            return false;
        }

        ContentValues values = new ContentValues();

        // Add a dummy name "Untitled", if the user exits the screen without adding a name but 
        // entered other information worth keeping.
        values.put(Telephony.Carriers.NAME,
                name.length() < 1 ? getResources().getString(R.string.untitled_apn) : name);
        values.put(Telephony.Carriers.APN, apn);
        values.put(Telephony.Carriers.PROXY, checkNotSet(mProxy.getText()));
        values.put(Telephony.Carriers.PORT, checkNotSet(mPort.getText()));
        values.put(Telephony.Carriers.MMSPROXY, checkNotSet(mMmsProxy.getText()));
        values.put(Telephony.Carriers.MMSPORT, checkNotSet(mMmsPort.getText()));
        values.put(Telephony.Carriers.USER, checkNotSet(mUser.getText()));
        values.put(Telephony.Carriers.SERVER, checkNotSet(mServer.getText()));
        values.put(Telephony.Carriers.PASSWORD, checkNotSet(mPassword.getText()));
        values.put(Telephony.Carriers.MMSC, checkNotSet(mMmsc.getText()));

        String authVal = mAuthType.getValue();
        if (authVal != null) {
            values.put(Telephony.Carriers.AUTH_TYPE, Integer.parseInt(authVal));
        }

        values.put(Telephony.Carriers.PROTOCOL, checkNotSet(mProtocol.getValue()));
        values.put(Telephony.Carriers.ROAMING_PROTOCOL, checkNotSet(mRoamingProtocol.getValue()));

        values.put(Telephony.Carriers.TYPE, checkNotSet(mApnType.getText()));

        values.put(Telephony.Carriers.MCC, mcc);
        values.put(Telephony.Carriers.MNC, mnc);

        values.put(Telephony.Carriers.NUMERIC, mcc + mnc);

        if (mCurMnc != null && mCurMcc != null) {
            if (mCurMnc.equals(mnc) && mCurMcc.equals(mcc)) {
                values.put(Telephony.Carriers.CURRENT, 1);
            }
        }

        String bearerVal = mBearer.getValue();
        if (bearerVal != null) {
            values.put(Telephony.Carriers.BEARER, Integer.parseInt(bearerVal));
        }

        getContentResolver().update(mUri, values, null, null);
        // qy test
        Log.i("qy", "mUri == " +mUri.toString());
        Log.i("qy", "Id == " +ContentUris.parseId(mUri)+"");
        SharedPreferences apnSh = getSharedPreferences("apn",MODE_PRIVATE);
        SharedPreferences.Editor editor = apnSh.edit();
        editor.putString(mUri.toString(), ContentUris.parseId(mUri)+"");
        editor.commit();
        return true;
    }

    private String getErrorMsg() {
        String errorMsg = null;

        String name = checkNotSet(mName.getText());
        String apn = checkNotSet(mApn.getText());
        String mcc = checkNotSet(mMcc.getText());
        String mnc = checkNotSet(mMnc.getText());

        if (name.length() < 1) {
            errorMsg = mRes.getString(R.string.error_name_empty);
        } else if (apn.length() < 1) {
            errorMsg = mRes.getString(R.string.error_apn_empty);
        } else if (mcc.length() != 3) {
            errorMsg = mRes.getString(R.string.error_mcc_not3);
        } else if ((mnc.length() & 0xFFFE) != 2) {
            errorMsg = mRes.getString(R.string.error_mnc_not23);
        }

        return errorMsg;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();

            return new AuroraAlertDialog.Builder(this)
                    .setTitle(R.string.error_title)
                    .setPositiveButton(android.R.string.ok, null)
                    .setMessage(msg)
                    .create();
        }

        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (id == ERROR_DIALOG_ID) {
            String msg = getErrorMsg();

            if (msg != null) {
                ((AuroraAlertDialog)dialog).setMessage(msg);
            }
        }
    }

    private void deleteApn() {
        getContentResolver().delete(mUri, null, null);
        // qy
        SharedPreferences apnSh = getSharedPreferences("apn",MODE_PRIVATE);
        SharedPreferences.Editor editor = apnSh.edit();
        editor.putString(mUri.toString(), null);
        editor.commit();
        ContentResolver resolver = getContentResolver();

        ContentValues values = new ContentValues();
        values.put(ApnSettings.APN_ID, AuroraApnPreference.mLastSelectedKey);
        resolver.update(Uri.parse("content://telephony/carriers/preferapn"), values, null, null);
        
        finish();
    }

    private String starify(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            char[] password = new char[value.length()];
            for (int i = 0; i < password.length; i++) {
                password[i] = '*';
            }
            return new String(password);
        }
    }

    private String checkNull(String value) {
        if (value == null || value.length() == 0) {
            return sNotSet;
        } else {
            return value;
        }
    }

    private String checkNotSet(String value) {
        if (value == null || value.equals(sNotSet)) {
            return "";
        } else {
            return value;
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        AuroraPreference pref = findPreference(key);
        if (pref != null) {
            if (pref.equals(mPassword)){
                pref.setSummary(starify(sharedPreferences.getString(key, "")));
            } else {
                pref.setSummary(checkNull(sharedPreferences.getString(key, "")));
            }
        }
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (validateAndSave(false)) {
            finish();
        }
	}


}

/*
 * Copyright (C) 2008 The Android Open Source Project
 * Copyright (c) 2011-2013 The Linux Foundation. All rights reserved.
 *
 * Not a Contribution.
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

package com.android.phone;

import com.android.internal.telephony.CallForwardInfo;
import com.android.internal.telephony.CommandsInterface;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import java.util.ArrayList;

import aurora.app.*;
import aurora.preference.*;
import aurora.widget.*;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;

import android.os.SystemProperties;
import static com.android.internal.telephony.PhoneConstants.SUBSCRIPTION_KEY;

import com.android.internal.telephony.Phone;
/**
 * Top level "Call settings" UI; see res/xml/call_feature_setting.xml
 *
 * This preference screen is the root of the "MSim Call settings" hierarchy
 * available from the Phone app; the settings here let you control various
 * features related to phone calls (including voicemail settings, SIP
 * settings, the "Respond via SMS" feature, and others.)  It's used only
 * on voice-capable phone devices.
 *
 * Note that this activity is part of the package com.android.phone, even
 * though you reach it from the "Phone" app (i.e. DialtactsActivity) which
 * is from the package com.android.contacts.
 *
 * For the "MSim Mobile network settings" screen under the main Settings app,
 * See {@link MSimMobileNetworkSettings}.
 *
 * @see com.android.phone.MSimMobileNetworkSettings
 */
public class MsimCallForwardSettings extends TimeConsumingPreferenceActivity {
    private static final String LOG_TAG = "MSimCallForwardSettings";
    private final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final String NUM_PROJECTION[] = {android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER};

    private static final String BUTTON_CFU_KEY   = "button_cfu_key_";
    private static final String BUTTON_CFB_KEY   = "button_cfb_key_";
    private static final String BUTTON_CFNRY_KEY = "button_cfnry_key_";
    private static final String BUTTON_CFNRC_KEY = "button_cfnrc_key_";
    
    private static final String CDMA_BUTTON_CFU_KEY   = "button_cfu_key";
    private static final String CDMA_BUTTON_CFB_KEY   = "button_cfb_key";
    private static final String CDMA_BUTTON_CFNRY_KEY = "button_cfnry_key";
    private static final String CDMA_BUTTON_CFNRC_KEY = "button_cfnrc_key";
    private static final String CDMA_BUTTON_CFC_KEY = "button_cfc_key";

    private static final String KEY_TOGGLE = "toggle";
    private static final String KEY_STATUS = "status";
    private static final String KEY_NUMBER = "number";

    private CallForwardEditPreference[] mButtonCFU;
    private CallForwardEditPreference[] mButtonCFB;
    private CallForwardEditPreference[] mButtonCFNRy;
    private CallForwardEditPreference[] mButtonCFNRc;
    private Phone[] phoneList;
    
    private AuroraPreference mCdmaButtonCFU;
    private AuroraPreference mCdmaButtonCFB;
    private AuroraPreference mCdmaButtonCFNRy;
    private AuroraPreference mCdmaButtonCFNRc;
    private AuroraPreference mCdmaButtonCFC;
    
    private AuroraEditText mEditNumber = null;
    private Bundle mBundle;
    private int mSubId = -1;
    
    private static final int DIALOG_CFU = 0;
    private static final int DIALOG_CFB = 1;
    private static final int DIALOG_CFNRY = 2;
    private static final int DIALOG_CFNRC = 3;
    private static final int DIALOG_CFC = 4;
    
    private static final int GET_CONTACTS = 100;
    
    private static final String[] CF_HEADERS = {
        SystemProperties.get("ro.cdma.cfu.enable"), SystemProperties.get("ro.cdma.cfu.disable"),
        SystemProperties.get("ro.cdma.cfb.enable"), SystemProperties.get("ro.cdma.cfb.disable"),
        SystemProperties.get("ro.cdma.cfnr.enable"), SystemProperties.get("ro.cdma.cfnr.disable"),
        SystemProperties.get("ro.cdma.cfdf.enable"), SystemProperties.get("ro.cdma.cfdf.disable"),
        SystemProperties.get("ro.cdma.cfall.disable")
    };
    
	private static final int mNumPhones = PhoneUtils.getPhoneCount();

    private final ArrayList<CallForwardEditPreference> mPreferences =
            new ArrayList<CallForwardEditPreference> ();
    private ArrayList<AuroraPreference> mCdmaPreferences = null;
    private int mInitIndex= 0;

    private boolean mFirstResume;
    private Bundle mIcicle;
    private AuroraPreference mSimCategory1, mSimCategory2;
    private AuroraPreference mCdmaSimCategory;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.msim_callforward_options);
        getAuroraActionBar().setTitle(R.string.aurora_cf_title);

		phoneList = new Phone[2];
		phoneList[0] = PhoneGlobals.getInstance().getPhone(0);
		phoneList[1] = PhoneGlobals.getInstance().getPhone(1);

        boolean iscard1Enable = PhoneUtils.getSimState(0) != TelephonyManager.SIM_STATE_ABSENT;
		boolean iscard2Enable = PhoneUtils.getSimState(1) != TelephonyManager.SIM_STATE_ABSENT;
        mSimCategory1= findPreference("sim1_category_key");
        mSimCategory2= findPreference("sim2_category_key");
 	    mSimCategory1.setEnabled(iscard1Enable);
 	    mSimCategory2.setEnabled(iscard2Enable);	    
 	   mCdmaSimCategory= findPreference("cdma_category_key");

        AuroraPreferenceScreen prefSet = (AuroraPreferenceScreen)findPreference("main");
        mButtonCFU = new CallForwardEditPreference[mNumPhones];
        mButtonCFB = new CallForwardEditPreference[mNumPhones];
        mButtonCFNRy = new CallForwardEditPreference[mNumPhones];
        mButtonCFNRc = new CallForwardEditPreference[mNumPhones];       
        for (int i = 0; i < mNumPhones; ++i) {
        	mButtonCFU[i]   = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFU_KEY + i);
            mButtonCFB[i]    = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFB_KEY + i);
            mButtonCFNRy[i]  = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRY_KEY + i);
            mButtonCFNRc[i]  = (CallForwardEditPreference) prefSet.findPreference(BUTTON_CFNRC_KEY + i);
            mButtonCFU[i].setParentActivity(this, mButtonCFU[i].reason + i*4);
            mButtonCFB[i].setParentActivity(this, mButtonCFB[i].reason + i*4);
            mButtonCFNRy[i].setParentActivity(this, mButtonCFNRy[i].reason + i*4);
            mButtonCFNRc[i].setParentActivity(this, mButtonCFNRc[i].reason + i*4);          
        }
        
        int phonetype1 = phoneList[0].getPhoneType();
        int phonetype2 = phoneList[1].getPhoneType();
 
        if(iscard1Enable && phonetype1 != PhoneConstants.PHONE_TYPE_CDMA) {
        	  mPreferences.add(mButtonCFU[0]);
              mPreferences.add(mButtonCFB[0]);
              mPreferences.add(mButtonCFNRy[0]);
              mPreferences.add(mButtonCFNRc[0]); 	
        } else {
        	prefSet.removePreference(mSimCategory1);
        }
        
        if(iscard2Enable && phonetype2 != PhoneConstants.PHONE_TYPE_CDMA) {
        	  mPreferences.add(mButtonCFU[1]);
              mPreferences.add(mButtonCFB[1]);
              mPreferences.add(mButtonCFNRy[1]);
              mPreferences.add(mButtonCFNRc[1]);
      } else {
    	  prefSet.removePreference(mSimCategory2);
      }
        // we wait to do the initialization until onResume so that the
        // TimeConsumingPreferenceActivity dialog can display as it
        // relies on onResume / onPause to maintain its foreground state.
        mCdmaButtonCFU   = prefSet.findPreference(CDMA_BUTTON_CFU_KEY);
        mCdmaButtonCFB   = prefSet.findPreference(CDMA_BUTTON_CFB_KEY);
        mCdmaButtonCFNRy = prefSet.findPreference(CDMA_BUTTON_CFNRY_KEY);
        mCdmaButtonCFNRc = prefSet.findPreference(CDMA_BUTTON_CFNRC_KEY);
        mCdmaButtonCFC = prefSet.findPreference(CDMA_BUTTON_CFC_KEY);
        mCdmaPreferences = new ArrayList<AuroraPreference>();
        mCdmaPreferences.add(mCdmaButtonCFU);
        mCdmaPreferences.add(mCdmaButtonCFB);
        mCdmaPreferences.add(mCdmaButtonCFNRy);
        mCdmaPreferences.add(mCdmaButtonCFNRc);
        mCdmaPreferences.add(mCdmaButtonCFC);
        
        if(phonetype1 == PhoneConstants.PHONE_TYPE_CDMA && iscard1Enable) {
        	mCdmaSimCategory.setTitle(R.string.sub_1);
        	mSubId =  AuroraSubUtils.getSubIdbySlot(this, 0);
        }else if(phonetype2 == PhoneConstants.PHONE_TYPE_CDMA && iscard2Enable) {
        	mCdmaSimCategory.setTitle(R.string.sub_2);
        	mSubId =  AuroraSubUtils.getSubIdbySlot(this, 1);
        } else {
        	prefSet.removePreference(mCdmaSimCategory);
        }
        

        mFirstResume = true;
        mIcicle = icicle;

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            // android.R.id.home will be triggered in onOptionsItemSelected()
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mFirstResume) {       
        	if(mPreferences.size() == 0) {
        		
        	} else  if (mIcicle == null) {
                if (DBG) Log.d(LOG_TAG, "start to init ");
                mPreferences.get(mInitIndex).init(this, false, 0);
            } else {
                mInitIndex = mPreferences.size();

                int count = 0;
                for (CallForwardEditPreference pref : mPreferences) {
  
                    Bundle bundle = mIcicle.getParcelable(pref.getKey());
                    pref.setToggled(bundle.getBoolean(KEY_TOGGLE));
                    CallForwardInfo cf = new CallForwardInfo();
                    cf.number = bundle.getString(KEY_NUMBER);
                    cf.status = bundle.getInt(KEY_STATUS);
                    pref.handleCallForwardResult(cf);
                    pref.init(this, true, 0);
                  	count++;
                }
            }
            mFirstResume = false;
            mIcicle=null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        for (CallForwardEditPreference pref : mPreferences) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(KEY_TOGGLE, pref.isToggled());
            if (pref.callForwardInfo != null) {
                bundle.putString(KEY_NUMBER, pref.callForwardInfo.number);
                bundle.putInt(KEY_STATUS, pref.callForwardInfo.status);
            }
            outState.putParcelable(pref.getKey(), bundle);
        }
    }

    @Override
    public void onFinished(AuroraPreference preference, boolean reading) {
        if (mInitIndex < mPreferences.size()-1 && !isFinishing()) {
            mInitIndex++;
            mPreferences.get(mInitIndex).init(this, false, 0);
        }

        super.onFinished(preference, reading);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	

      
    	
    	
        if (DBG) Log.d(LOG_TAG, "onActivityResult: done");
        if (resultCode != RESULT_OK) {
            if (DBG) Log.d(LOG_TAG, "onActivityResult: contact picker result not OK.");
            return;
        }
        
        if(requestCode == GET_CONTACTS) {
        	  Cursor c = null;
              try {
                  c = getContentResolver().query(data.getData(),
                          NUM_PROJECTION, null, null, null);
                  if ((c != null) && (c.moveToFirst()) && mEditNumber != null) {
                      mEditNumber.setText(c.getString(0));
                  }
              } finally {
                  if (c != null) {
                      c.close();
                  }
              }
              return;
        }        
        
        
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(data.getData(),
                NUM_PROJECTION, null, null, null);
            if ((cursor == null) || (!cursor.moveToFirst())) {
                if (DBG) Log.d(LOG_TAG, "onActivityResult: bad contact data, no results found.");
                return;
            }

            switch (requestCode) {
                case CommandsInterface.CF_REASON_UNCONDITIONAL:
                    mButtonCFU[0].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_BUSY:
                    mButtonCFB[0].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NO_REPLY:
                    mButtonCFNRy[0].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NOT_REACHABLE:
                    mButtonCFNRc[0].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_UNCONDITIONAL + 4 :
                    mButtonCFU[1].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_BUSY  + 4 :
                    mButtonCFB[1].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NO_REPLY  + 4 :
                    mButtonCFNRy[1].onPickActivityResult(cursor.getString(0));
                    break;
                case CommandsInterface.CF_REASON_NOT_REACHABLE  + 4 :
                    mButtonCFNRc[1].onPickActivityResult(cursor.getString(0));
                    break;
                default:
                    // TODO: may need exception here.
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {  // See ActionBar#setDisplayHomeAsUpEnabled()
            CallFeaturesSetting.goUpToTopLevelSetting(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onPreferenceTreeClick(AuroraPreferenceScreen preferenceScreen, AuroraPreference preference) {
        if (preference == mCdmaButtonCFU) {
            showDialog(DIALOG_CFU);
        } else if (preference == mCdmaButtonCFB) {
            showDialog(DIALOG_CFB);
        } else if (preference == mCdmaButtonCFNRy) {
            showDialog(DIALOG_CFNRY);
        } else if (preference == mCdmaButtonCFNRc) {
            showDialog(DIALOG_CFNRC);
        } else if (preference == mCdmaButtonCFC) {
            setCallForward(CF_HEADERS[8]);
        }
        return true;
    }
    
    @Override
    protected Dialog onCreateDialog(final int id) {
    	
    	if(id > 4 ) {
    		return super.onCreateDialog(id);
    	}
    	
    	View main = LayoutInflater.from(this).inflate(R.layout.pref_dialog_editphonenumber_v2, null);
    	
        ImageButton addContactBtn = (ImageButton) main.findViewById(R.id.select_contact);
        if (addContactBtn != null) {
            addContactBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startContacts();
                }
            });
        }
        
        
        
      mEditNumber = (AuroraEditText) main.findViewById(R.id.EditNumber);
    	
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this,
				AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
				.setView(main)
				.setTitle(mCdmaPreferences.get(id).getTitle())
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						})
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {

								String cf;
								int cfType = id * 2;
								cf = CF_HEADERS[cfType] + mEditNumber.getText();
								dialog.dismiss();
								setCallForward(cf);
							}
						})
				.setNeutralButton(R.string.disable,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								String cf;
								int cfType = id * 2 + 1;
								cf = CF_HEADERS[cfType];
								dialog.dismiss();
								setCallForward(cf);
							}
						}).create();
		
		
		requestInputMethod(dialog);
    	
//        final AuroraDialog dialog = new AuroraDialog(this);
//        dialog.setContentView(R.layout.mtk_cdma_cf_dialog);
//        dialog.setTitle(mCdmaPreferences.get(id).getTitle());
//
//        final RadioGroup radioGroup = (RadioGroup) dialog.findViewById(R.id.group);

//        ImageButton addContactBtn = (ImageButton) dialog.findViewById(R.id.select_contact);
//        if (addContactBtn != null) {
//            addContactBtn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    startContacts();
//                }
//            });
//        }

//        AuroraButton dialogSaveBtn = (AuroraButton) dialog.findViewById(R.id.save);
//        if (dialogSaveBtn != null) {
//            dialogSaveBtn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (radioGroup.getCheckedRadioButtonId() == -1) {
//                        return;
//                    }
//                    String cf;
//                    if (radioGroup.getCheckedRadioButtonId() == R.id.enable) {
//                        int cfType = id * 2;
//                        cf = CF_HEADERS[cfType] + mEditNumber.getText();
//                    } else {
//                        int cfType = id * 2 + 1;
//                        cf = CF_HEADERS[cfType];
//                    }
//                    dialog.dismiss();
//                    setCallForward(cf);
//                }
//            });
//        }
//
//        AuroraButton dialogCancelBtn = (AuroraButton) dialog.findViewById(R.id.cancel);
//        if (dialogCancelBtn != null) {
//                dialogCancelBtn.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.dismiss();
//                }
//            });
//        }
        return dialog;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        // Do not initialize mEditNumber in onCreateDialog, it is only called
        // when Dialog is created.
//        mEditNumber = (AuroraEditText) dialog.findViewById(R.id.EditNumber);
    }

    private void setCallForward(String cf) {
        if (mSubId == -1 || cf == null || cf.isEmpty()) {
            Log.d(LOG_TAG, "setCallForward null return");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + cf));
//        ComponentName pstnConnectionServiceName =
//            new ComponentName(this, TelephonyConnectionService.class);
//        String id = "" + String.valueOf(mSubId);
//        Log.d(LOG_TAG, "setCallForward: " + id);
//        PhoneAccountHandle phoneAccountHandle =
//            new PhoneAccountHandle(pstnConnectionServiceName, id);
//        intent.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, phoneAccountHandle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SUBSCRIPTION_KEY, AuroraSubUtils.getSlotBySubId(this, mSubId));
        
        startActivity(intent);
       
    }
    
    private void startContacts() {
        Intent intent = new Intent("android.intent.action.PICK");
        intent.setType(android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        intent.addCategory("android.intent.category.GIONEE");
        startActivityForResult(intent, GET_CONTACTS);
    }
    
    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }
}

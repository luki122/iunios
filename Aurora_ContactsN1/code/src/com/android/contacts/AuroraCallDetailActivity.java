/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.contacts;

import com.android.contacts.BackScrollManager.ScrollableHeader;
import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.activities.AuroraMarkActivity;
import com.android.contacts.calllog.AuroraCallDetailHistoryAdapter;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.YuloreUtils;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.android.contacts.R;

import android.app.StatusBarManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import android.provider.ContactsContract.RawContacts;

import com.aurora.android.contacts.AuroraStorageManager;





import com.aurora.android.contacts.AuroraSubInfoNotifier;
import com.android.internal.telephony.ITelephony;

import android.app.ActionBar;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.provider.CallLog.Calls;
import android.provider.Contacts.Intents.Insert;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore.Audio.Media;
//import android.provider.VoicemailContract.Voicemails;
import android.telephony.*;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.android.contacts.util.Constants;




// Gionee:wangth 20120710 add for CR00633799 begin
import android.provider.ContactsContract.Data;
import android.graphics.drawable.Drawable;
// Gionee:wangth 20120710 add for CR00633799 end
// Gionee:xuhz 20130328 add for CR00790874 start
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
// Gionee:xuhz 20130328 add for CR00790874 end

import aurora.app.AuroraActivity;
import aurora.provider.AuroraSettings;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraCheckBox;

import com.android.contacts.calllog.AuroraCallLogAdapterV2;

import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenu;

import com.privacymanage.service.AuroraPrivacyUtils;

/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class AuroraCallDetailActivity extends AuroraActivity implements ProximitySensorAware, AuroraSubInfoNotifier.SubInfoListener {
    private static final String TAG = "AuroraCallDetailActivity";

    /** The time to wait before enabling the blank the screen due to the proximity sensor. */
    private static final long PROXIMITY_BLANK_DELAY_MILLIS = 100;
    /** The time to wait before disabling the blank the screen due to the proximity sensor. */
    private static final long PROXIMITY_UNBLANK_DELAY_MILLIS = 500;

    /** The enumeration of {@link AsyncTask} objects used in this class. */
    public enum Tasks {
        MARK_VOICEMAIL_READ,
        DELETE_VOICEMAIL_AND_FINISH,
        REMOVE_FROM_CALL_LOG_AND_FINISH,
        UPDATE_PHONE_CALL_DETAILS,
    }

    /** A long array extra containing ids of call log entries to display. */
    public static final String EXTRA_CALL_LOG_IDS = "EXTRA_CALL_LOG_IDS";
    /** If we are started with a voicemail, we'll find the uri to play with this extra. */
    public static final String EXTRA_VOICEMAIL_URI = "EXTRA_VOICEMAIL_URI";
    /** If we should immediately start playback of the voicemail, this extra will be set to true. */
    public static final String EXTRA_VOICEMAIL_START_PLAYBACK = "EXTRA_VOICEMAIL_START_PLAYBACK";

    private CallTypeHelper mCallTypeHelper;
    private PhoneNumberHelper mPhoneNumberHelper;
    private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
    private TextView mHeaderTextView;
    private TextView mNameTextView;
    private TextView mNoteTextView;
    private View mHeaderOverlayView;
    private ImageView mMainActionView;
    private ImageView mContactBackgroundView;
    private AsyncTaskExecutor mAsyncTaskExecutor;
    private ContactInfoHelper mContactInfoHelper;

    private String mNumber = null;
    private String mName = null;
    private String mNote = null;
    private String mDefaultCountryIso;
    private Uri mContactUri;
    
    private AuroraActionBar mActionBar;
    private Context mContext;
    private int mNumberType = 0;

    /* package */ LayoutInflater mInflater;
    /* package */ Resources mResources;
    /** Helper to load contact photos. */
    private ContactPhotoManager mContactPhotoManager;
    /** Helper to make async queries to content resolver. */
    private CallDetailActivityQueryHandler mAsyncQueryHandler;
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.

    /** Helper to get voicemail status messages. */
    // private VoicemailStatusHelper mVoicemailStatusHelper;
    // Views related to voicemail status message.
    // private View mStatusMessageView;
    // private TextView mStatusMessageText;
    // private TextView mStatusMessageAction;
    
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /** Whether we should show "edit number before call" in the options menu. */
    private boolean mHasEditNumberBeforeCallOption;
    /** Whether we should show "trash" in the options menu. */
    //private boolean mHasTrashOption;
    /** Whether we should show "remove from call log" in the options menu. */
    //private boolean mHasRemoveFromCallLogOption;

    private ProximitySensorManager mProximitySensorManager;
    private final ProximitySensorListener mProximitySensorListener = new ProximitySensorListener();
    
    private static boolean mIsRejectedDetail = false;// aurora wangth 20140618 add for reject detail
    private String mBlackName = null;
    private boolean mIsShowRejectFlag = false;
    private String mUserMark = null;
    private boolean mIsNoUserMark = false;
    private String mMarkContent = null;
    private int mMarkCount = 0;
    private String mNameOrig = null;
    private RelativeLayout header_bg;
    private int mContactSimId = -1;
    private static final int ADD_MARK = 1;
    private static final int EDIT_MARK = 2;
    private String mNumberArea = null;
    private boolean mNeedReQuery = false;    

    /** Listener to changes in the proximity sensor state. */
    private class ProximitySensorListener implements ProximitySensorManager.Listener {
        /** Used to show a blank view and hide the action bar. */
        private final Runnable mBlankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.VISIBLE);
//                getActionBar().hide();
            }
        };
        /** Used to remove the blank view and show the action bar. */
        private final Runnable mUnblankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.GONE);
//                getActionBar().show();
            }
        };

        @Override
        public synchronized void onNear() {
            clearPendingRequests();
            postDelayed(mBlankRunnable, PROXIMITY_BLANK_DELAY_MILLIS);
        }

        @Override
        public synchronized void onFar() {
            clearPendingRequests();
            postDelayed(mUnblankRunnable, PROXIMITY_UNBLANK_DELAY_MILLIS);
        }

        /** Removed any delayed requests that may be pending. */
        public synchronized void clearPendingRequests() {
            View blankView = findViewById(R.id.blank);
            blankView.removeCallbacks(mBlankRunnable);
            blankView.removeCallbacks(mUnblankRunnable);
        }

        /** Post a {@link Runnable} with a delay on the main thread. */
        private synchronized void postDelayed(Runnable runnable, long delayMillis) {
            // Post these instead of executing immediately so that:
            // - They are guaranteed to be executed on the main thread.
            // - If the sensor values changes rapidly for some time, the UI will not be
            //   updated immediately.
            View blankView = findViewById(R.id.blank);
            blankView.postDelayed(runnable, delayMillis);
        }
    }

    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
    		if(GNContactsUtils.isMultiSimEnabled()) {
    		    boolean showDouble = ContactsUtils.isShowDoubleButton();
    			if (showDouble && SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
					try {
						removeMenuById(AuroraMenu.FIRST);
						removeMenuById(AuroraMenu.FIRST + 1);
						removeMenuById(AuroraMenu.FIRST + 2);
						removeMenuById(AuroraMenu.FIRST + 3);
						removeMenuById(AuroraMenu.FIRST + 4);
					} catch (Exception e) {
						e.printStackTrace();
					}
    				
    			    int lastCallSlotId = ContactsUtils.getLastCallSlotId(mContext, mCallNumber);
    			    String recentCall = getString(R.string.aurora_recent_call);
    			    String menuSlot0 = getString(R.string.aurora_slot_0) +  getString(R.string.gn_dial_desciption);
    			    String menuSlot1 = getString(R.string.aurora_slot_1) +  getString(R.string.gn_dial_desciption);
    			    
    			    if (lastCallSlotId == 0) {
    			        menuSlot0 = menuSlot0 + recentCall;
    			    } else if (lastCallSlotId == 1) {
    			        menuSlot1 = menuSlot1 + recentCall;
    			    }
    			    
					addMenu(AuroraMenu.FIRST, menuSlot1,
							new OnMenuItemClickLisener() {
								public void onItemClick(View menu) {
									Intent intent = ContactsUtils.getCallNumberIntent(mCallNumber, 1);
									intent.putExtra("contactUri", mContactUri);
									startActivity(intent);
									mNeedReQuery = true;
								}
							});
					addMenu(AuroraMenu.FIRST + 1, menuSlot0,
							new OnMenuItemClickLisener() {
								public void onItemClick(View menu) {
									Intent intent = ContactsUtils.getCallNumberIntent(mCallNumber, 0);
									intent.putExtra("contactUri", mContactUri);
									startActivity(intent);
									mNeedReQuery = true;
								}
							});
					showCustomMenu();
    			} else {
    	            startActivity(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
    	          	mNeedReQuery = true;
    			}
    		} else {
                startActivity(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
            	mNeedReQuery = true;
    		}

        }
    };

    private final View.OnClickListener mSecondaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
            	startActivity(((ViewEntry) view.getTag()).secondaryIntent);
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "ActivityNotFoundException for secondaryIntent");
            }
        }
    };

    @Override
    protected void onCreate(Bundle icicle) {

        setTheme(R.style.GN_CallDetailActivityTheme_light);
        
        log("AuroraCallDetailActivity  onCreat()");
        super.onCreate(icicle);
        
        sendSimContactBroad();
        
        setAuroraContentView(R.layout.aurora_call_detail_without_voicemail,
                AuroraActionBar.Type.Normal); 
        mContext = AuroraCallDetailActivity.this;
        mActionBar = getAuroraActionBar();
        mActionBar.setTitle(R.string.callDetailTitle);

        mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();

        mCallTypeHelper = new CallTypeHelper(getResources());
        mPhoneNumberHelper = new PhoneNumberHelper(mResources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
                                                             mPhoneNumberHelper, null, this);
        mAsyncQueryHandler = new CallDetailActivityQueryHandler(this);
        mHeaderTextView = (TextView) findViewById(R.id.header_text);
        mNameTextView = (TextView) findViewById(R.id.header_name);
        mNoteTextView = (TextView) findViewById(R.id.note);
        mHeaderOverlayView = findViewById(R.id.photo_text_bar);
        
        mMainActionView = (ImageView) findViewById(R.id.main_action);
        mMainActionView.setVisibility(View.GONE);
        mContactBackgroundView = (ImageView) findViewById(R.id.contact_background);
        mContactBackgroundView.setEnabled(false);
        mContactBackgroundView.setClickable(false);
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(this);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);
        mProximitySensorManager = new ProximitySensorManager(this, mProximitySensorListener);
        mContactInfoHelper = new ContactInfoHelper(this, mDefaultCountryIso);
        // The following lines are provided and maintained by Mediatek Inc.
//        mSimName = new TextView(this);// just a stub, uesless
        AuroraSubInfoNotifier.getInstance().addListener(this);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // The previous lines are provided and maintained by Mediatek Inc.
        //optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.

		
        Intent intent = getIntent();  
        ids=intent.getIntArrayExtra("ids");
        mCallNumber = intent.getStringExtra(Calls.NUMBER);
        mCallsCount = intent.getIntExtra(Calls._COUNT, 0);
        mCallId = intent.getIntExtra(Calls._ID, 0);
        mCallType = intent.getIntExtra(Calls.TYPE, 0);
        mContactSimId = intent.getIntExtra("contact_sim_id", -1);
        header_bg = (RelativeLayout)findViewById(R.id.header_con);
        mIsRejectedDetail = intent.getBooleanExtra("reject_detail", false);
        if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
        	setMenuEnable(false);
        	header_bg.setBackgroundColor(mResources.getColor(R.color.aurora_reject_call_detail_header_bg));
        	mActionBar.setTitle(R.string.aurora_reject_call_detail_title);
        	mBlackName = intent.getStringExtra("black_name");
        	mUserMark = intent.getStringExtra("user-mark");
        	mMarkContent = intent.getStringExtra("mark-content");
        	mMarkCount = intent.getIntExtra("mark-count", 0);
        } else {
        	addAuroraActionBarItem(AuroraActionBarItem.Type.More, AURORA_CALL_DETAIL_MORE);
            mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
            setAuroraMenuCallBack(auroraMenuCallBack);
        }
        

        
//        mIsPrivate = intent.getBooleanExtra("is_privacy_contact", false);
//        if(mIsPrivate) {
//        	mPrivateContactUri =  (Uri) intent.getParcelableExtra("contactUri");
//            mRawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), Long.parseLong(mPrivateContactUri.getLastPathSegment()));
//        	header_bg.setBackgroundColor(mResources.getColor(R.color.aurora_privacy_contact_detail_header_color));
//            ContactsApplication.mPrivacyActivityList.add(this);            
//        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        hasSms = PhoneCapabilityTester.isSmsIntentRegistered(getApplicationContext());
        
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Uri[] callEntryUris = getCallLogEntryUris();
				if (callEntryUris != null) {
					mContactSimId = isSimCardPhoneNumber(mContext, mNumber);
    				if (callEntryUris.length > 1) {
    					updateData(callEntryUris[0]);
    					updateHistoryList(callEntryUris);
    				} else {
    					updateData(callEntryUris);
    				}
				}
			}
		}, mIsFristResume ? 0 : 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        
        
        if (mNeedReQuery) {
        	mNeedReQuery = false;
        	mHandler.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				Uri[] callEntryUris = getCallLogEntryUris();
    				if (callEntryUris != null) {
    					mContactSimId = isSimCardPhoneNumber(mContext, mNumber);
        				if (callEntryUris.length > 1) {
        					updateData(callEntryUris[0]);
        					updateHistoryList(callEntryUris);
        				} else {
        					updateData(callEntryUris);
        				}
    				}
    			}
    		}, 0);
        }
        
    }    

    
    /**
     * Returns the list of URIs to show.
     * <p>
     * There are two ways the URIs can be provided to the activity: as the data on the intent, or as
     * a list of ids in the call log added as an extra on the URI.
     * <p>
     * If both are available, the data on the intent takes precedence.
     */
    private Uri[] getCallLogEntryUris() {
//    	if(mIsPrivate) {
//    		return getPrivateCallLogEntryUris();    	
//    	}
    	
		return gnGetCallLogEntryUris();    		

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_CALL: {
                // Make sure phone isn't already busy before starting direct call
                TelephonyManager tm = (TelephonyManager)
                        getSystemService(Context.TELEPHONY_SERVICE);
                if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
                	Intent callIntent;
          
                		callIntent = IntentFactory.newDialNumberIntent(mNumber);
                
                	// Gionee <fenglp> <2013-08-19> add for CR00861033 begin
                    if (callIntent != null) {
//                        callIntent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                        startActivity(callIntent);
                        return true;
                    }
                    // Gionee <fenglp> <2013-08-19> add for CR00861033 end
                }
            }
            case  KeyEvent.KEYCODE_MENU: {
            	if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
            		return false;
            	}
            	
            	if (GNContactsUtils.isMultiSimEnabled()) {
            		gnSwitchUi(mNumberType);
            	}
            	
            	showCustomMenu();
            	return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * Update user interface with details of given call.
     *
     * @param callUris URIs into {@link CallLog.Calls} of the calls to be displayed
     */
    private void updateData(final Uri... callUris) {
        class UpdateContactDetailsTask extends AsyncTask<Void, Void, PhoneCallDetails[]> {
        	
        	@Override
            public PhoneCallDetails[] doInBackground(Void... params) {
                // TODO: All phone calls correspond to the same person, so we can make a single
                // lookup.
                // Gionee:wangth 20120615 add for CR00624246 begin
                if (callUris == null) {
                    Log.w(TAG, "callUris is null");
                    return null;
                }
                // Gionee:wangth 20120615 add for CR00624246 end
                final int numCalls = callUris.length;
                PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
                try {
                    for (int index = 0; index < numCalls; ++index) {
                        details[index] = getPhoneCallDetailsForUri(callUris[index]);
                    }
                    return details;
                } catch (IllegalArgumentException e) {
                    // Something went wrong reading in our primary data.
                    Log.w(TAG, "invalid URI starting call details", e);
                    return null;
                }
            }

            @Override
            public void onPostExecute(PhoneCallDetails[] details) {
                if (details == null) {
                    // Somewhere went wrong: we're going to bail out and show error to users.
                    Toast.makeText(AuroraCallDetailActivity.this, R.string.toast_call_detail_error,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                
                // aurora <wangth> <2014-03-20> add for aurora ui begin
                if (isFinishing()) {
                    return;
                }
                // aurora <wangth> <2014-03-20> add for aurora ui end

                // We know that all calls are from the same number and the same contact, so pick the
                // first.
                PhoneCallDetails firstDetails = details[0];
                mNumber = firstDetails.number.toString();
                if (!TextUtils.isEmpty(firstDetails.name)) {
                    mName = firstDetails.name.toString();
                } else {
                    mName = null;
                }
                
                
                if(firstDetails.pirvateId > 0) {
                	mIsPrivateUri = true;
                	header_bg.setBackgroundColor(mResources.getColor(R.color.aurora_privacy_contact_detail_header_color));
                    ContactsApplication.mPrivacyActivityList.add(AuroraCallDetailActivity.this);                    
                }
                
                
                
                mNumberArea = firstDetails.numberArea;
                mNameOrig = mName;
                long mSimId = firstDetails.simId;
                
                if(GNContactsUtils.isMultiSimEnabled()) {
                	mSlot = ContactsUtils.getSlotBySubId(firstDetails.simId);
	                Log.d(TAG, "mSlot = " +  mSlot + " firstDetails.simId" + firstDetails.simId);
                }
                
                if (!TextUtils.isEmpty(firstDetails.numberLabel)) {
                	phoneType = firstDetails.numberLabel.toString();
                }
                final Uri contactUri = firstDetails.contactUri;
                final Uri photoUri = firstDetails.photoUri;
                // Set the details header, based on the first phone call.
                mPhoneCallDetailsHelper.setCallDetailsHeader(mHeaderTextView, firstDetails);
                
                //Gionee:huangzy 20120612 modify for CR00614326 start
                //Gionee:huangzy 20120528 add for CR00611149 start

                Log.d(TAG, "contactUri = " +  contactUri);
                
                if ((null != mNumber && mNumber.equals("-1"))) {
                	mNumberType = 2;
                } else if (null == contactUri) {
                	mNumberType = 1;
                } else {
                   	mNumberType = 0;
                }                
                //Gionee:huangzy 20120528 add for CR00611149 end
                //Gionee:huangzy 20120612 modify for CR00614326 end

                // Cache the details about the phone number.
                final Uri numberCallUri = mPhoneNumberHelper.getCallUri(mNumber,firstDetails.simId);
                final boolean canPlaceCallsTo = mPhoneNumberHelper.canPlaceCallsTo(mNumber);
//                final boolean isVoicemailNumber = mPhoneNumberHelper.isVoicemailNumber(mNumber);
                final boolean isVoicemailNumber = false;
                final boolean isSipNumber = mPhoneNumberHelper.isSipNumber(mNumber);

                // Let user view contact details if they exist, otherwise add option to create new
                // contact from this number.
                final Intent mainActionIntent;
                final int mainActionIcon;
                final String mainActionDescription;

                final CharSequence nameOrNumber;
                //aurora change liguangyu 20131218 for bug #1280 start
                if (!TextUtils.isEmpty(firstDetails.name)) {
                    nameOrNumber = firstDetails.name;
                    mName = nameOrNumber.toString();
                } else {
                    nameOrNumber = firstDetails.number;
                    CharSequence c = mPhoneNumberHelper.getDisplayNumber(
                            firstDetails.number, firstDetails.formattedNumber);
                    mName = c != null ? c.toString() : "";
                }
                //aurora change liguangyu 20131218 for bug #1280 end
                
                mNameTextView.setText(mName);
                if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail && mBlackName != null && !mBlackName.isEmpty()) {
                	mNameTextView.setText(mBlackName);
                }
                
                if (ContactsApplication.sIsAuroraRejectSupport && !mIsRejectedDetail) {
                	if (mUserMark == null) {
                		mMarkCount = firstDetails.markCount;
                        if (mMarkCount >= 0) {
                        	mMarkContent = firstDetails.userMark;
                        } else {
                        	if (!mIsNoUserMark) {
                        		mUserMark = firstDetails.userMark;
                        	}
                        }
                	}
                	
                    Log.d(TAG, "mMarkCount = " + mMarkCount + "  mMarkContent = " + mMarkContent + " mUserMark = " + mUserMark);
                }

                if (contactUri != null) {
                	//Gionee:huangzy 20130401 modify for CR00792013 start
                    /*mainActionIntent = new Intent(Intent.ACTION_VIEW, contactUri);*/
                	mContactUri = contactUri;
                	mainActionIntent = IntentFactory.newViewContactIntent(contactUri);
                    //Gionee:huangzy 20130401 modify for CR00792013 end
                    mainActionIcon = R.drawable.ic_contacts_holo_dark;
                    mainActionDescription =
                            getString(R.string.description_view_contact, nameOrNumber);
                } else if (isVoicemailNumber) {
                    mainActionIntent = null;
                    mainActionIcon = 0;
                    mainActionDescription = null;
                    // } else if (isSipNumber) {
                    // // TODO: This item is currently disabled for SIP
                    // addresses, because
                    // // the Insert.PHONE extra only works correctly for PSTN
                    // numbers.
                    // //
                    // // To fix this for SIP addresses, we need to:
                    // // - define ContactsContract.Intents.Insert.SIP_ADDRESS,
                    // and use it here if
                    // // the current number is a SIP address
                    // // - update the contacts UI code to handle
                    // Insert.SIP_ADDRESS by
                    // // updating the SipAddress field
                    // // and then we can remove the "!isSipNumber" check above.
                    // mainActionIntent = null;
                    // mainActionIcon = 0;
                    // mainActionDescription = null;
                } else if (canPlaceCallsTo) {
                    mainActionIntent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    mainActionIntent.setType(Contacts.CONTENT_ITEM_TYPE);
                    /**
                    * Change Feature by Mediatek Begin.
                    * Original Android's Code:
                       mainActionIntent.putExtra(Insert.PHONE, mNumber);
                    * Descriptions:
                    */
                    if (isSipNumber) {
                    	mainActionIntent.putExtra(
                    			ContactsContract.Intents.Insert.SIP_ADDRESS,
                    			mNumber);
                    } else {
                    	mainActionIntent.putExtra(Insert.PHONE, mNumber);
                    }

                    /**
                    * Change Feature by Mediatek End.
                    */
                    mainActionIcon = R.drawable.ic_add_contact_holo_dark;
                    mainActionDescription = getString(R.string.description_add_contact);
                } else {
                    // If we cannot call the number, when we probably cannot add it as a contact either.
                    // This is usually the case of private, unknown, or payphone numbers.
                    mainActionIntent = null;
                    mainActionIcon = 0;
                    mainActionDescription = null;
                }

                if (mainActionIntent == null) {
                    mHeaderTextView.setVisibility(View.INVISIBLE);
                    mHeaderOverlayView.setVisibility(View.INVISIBLE);
                } else {
                	//Gionee:huangzy 20120528 modify for CR00611149 start
                	if (null == contactUri) {                    	
                    } else {
//                    	mMainActionView.setImageResource(mainActionIcon);                        
                        mHeaderOverlayView.setVisibility(View.VISIBLE);
                    }
                    //Gionee:huangzy 20120528 modify for CR00611149 end
                }
                // The previous lines are provided and maintained by Mediatek
                // Inc.
                // This action allows to call the number that places the call.
                if (canPlaceCallsTo) {
                    boolean isVoicemailUri = PhoneNumberHelper.isVoicemailUri(numberCallUri);
                    int slotId = ContactsUtils.getSlotBySubId((int)firstDetails.simId);
                    
                    final CharSequence displayNumber =
                            mPhoneNumberHelper.getDisplayNumber(
                                    firstDetails.number, firstDetails.formattedNumber);

                    Intent it;
                    if (ContactsApplication.sIsGnAreoNumAreaSupport) {
                    	// gionee xuhz 20121215 modify for Dual Sim Select start
                 
                		it = IntentFactory.newDialNumberIntent(numberCallUri);
    	            	//aurora add liguangyu 20131206 start
                		it.putExtra("contactUri", contactUri);
    	            	//aurora add liguangyu 20131206 end
                    	
                        // gionee xuhz 20121215 modify for Dual Sim Select end
                    	
                    } else {
                    	it = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri)
                        .putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long)firstDetails.simId);
                    }
                    
//                    it.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    if (isVoicemailUri && slotId != -1) {
                        it.putExtra("simId", slotId);
                    }
                	
                    //GIONEE: lujian 2012.10.09 modify for "explay CR00709996" begin-->
                    int gnResId = R.string.menu_callNumber;
                    
                    if (mContactUri != null) {
                    	long contactId = -1;
                        long rawContactId = -1;
                        Cursor c = null;
                    	if (mContactUri.getLastPathSegment() != null) {
                    		contactId = Long.parseLong(mContactUri.getLastPathSegment());
                            
                            if (contactId != -1) {
                                rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), contactId);
                                mNote = ContactsUtils.getNote(mContext, rawContactId);
                                Log.i(TAG, "Call detail note:" + mNote);
                                
                                if (mNameOrig == null) {
                                	mNote = null;
                                }
                            }
                            
                            try {
                                final String number = firstDetails.number.toString();
                                String[] projection = new String[]{"data2"};
                                
                                c = getContentResolver().query(Data.CONTENT_URI, 
                                        projection, 
                                        Data.RAW_CONTACT_ID + " = ?" + " and data1 = ? " + " and " + Data.MIMETYPE + " = ?", 
                                        new String[] {String.valueOf(rawContactId), number, Phone.CONTENT_ITEM_TYPE}, 
                                        null);
                                
                                if (c != null) {
                                    if (c.moveToFirst()) {
                                    	int i = c.getInt(0);
                                    	Log.e(TAG, "phone type:"+Phone.getTypeLabelResource(i));
                                    	//aurora change liguangyu 20131112 for BUG #781 start
//                                    	phoneType = mContext.getResources().getString(Phone.getTypeLabelResource(i));
                                    	phoneType = mContext.getResources().getString(getTypeLabelResource(i));
                                    	//aurora change liguangyu 20131112 for BUG #781 end                                    	
                                    	Log.e(TAG, "phone type:"+phoneType);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                if (c != null) {
                                    c.close();
                                }
                            }
                    	}
                    }
                    
                    ViewEntry entry = new ViewEntry(getString(gnResId,
                            displayNumber),
                            it,
                            getString(R.string.description_call, nameOrNumber));
                    //GIONEE: lujian 2012.10.09 modify for "explay CR00709996"   end-->
                    // Only show a label if the number is shown and it is not a SIP address.
                    if (!TextUtils.isEmpty(firstDetails.name)
                            && !TextUtils.isEmpty(firstDetails.number)
                            && !PhoneNumberUtils.isUriNumber(firstDetails.number.toString())) {
                        if (mNumberType == 0) {
                            // Gionee:wangth 20120710 modify for CR00633799 begin
                            /*
                            entry.label = mResources.getString(R.string.list_filter_custom);
                            */
                      
                                long contactId = -1;
                                long rawContactId = -1;
                                Cursor c = null;
                                String nLabel = null;
                                
                                if (contactUri != null && contactUri.getLastPathSegment() != null) {
                                    contactId = Long.parseLong(contactUri.getLastPathSegment());
                                    
                                    if (contactId != -1) {
                                        rawContactId = ContactsUtils.queryForRawContactId(getContentResolver(), contactId);
                                    }
                                }
                                try {
                                    final String number = firstDetails.number.toString();
                                    String[] projection = new String[]{"data3"};
                                    
                                    c = getContentResolver().query(Data.CONTENT_URI, 
                                            projection, 
                                            Data.RAW_CONTACT_ID + " = ?" + " and data1 = ? " + " and " + Data.MIMETYPE + " = ?", 
                                            new String[] {String.valueOf(rawContactId), number, Phone.CONTENT_ITEM_TYPE}, 
                                            null);
                                    
                                    if (c != null) {
                                        if (c.moveToFirst()) {
                                            nLabel = c.getString(0);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    if (c != null) {
                                        c.close();
                                    }
                                }
                                
                                // gionee xuhz 20120820 modify for CR00678736 start
                                if (nLabel != null && !nLabel.isEmpty()) {
                                // gionee xuhz 20120820 modify for CR00678736 end
                                    entry.label = nLabel;
                                    phoneType = entry.label.toString(); // wangth 20140303 added for aurora
                                } else {
                                    entry.label = mResources.getString(R.string.list_filter_custom);
                                }
                          
                            // Gionee:wangth 20120710 modify for CR00633799 end
                        } else {
                            entry.label = Phone.getTypeLabel(mResources, firstDetails.numberType,
                                    firstDetails.numberLabel);
                        }
                    }

                    if (!TextUtils.isEmpty(mNote)) {
                    	mNoteTextView.setText(mNote);
                    	//aurora add liguangyu 20131118 for BUG #956 start
                    	mNoteTextView.setVisibility(View.VISIBLE);
                    	//aurora add liguangyu 20131118 for BUG #956 end
                    } else {
                    	mNoteTextView.setVisibility(View.GONE);
                    }
                    // The secondary action allows to send an SMS to the number that placed the
                    // call.
                    if (mPhoneNumberHelper.canSendSmsTo(mNumber) && hasSms) {
                        //Gionee:huangzy 20130401 modify for CR00792013 start
                        /*Intent itSecond = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts("sms", mNumber, null));*/
                    	Intent itSecond = IntentFactory.newCreateSmsIntent(mNumber);
                        //Gionee:huangzy 20130401 modify for CR00792013 end
                    	if(mIsPrivate || mIsPrivateUri) {
                    		itSecond.putExtra("is_privacy", AuroraPrivacyUtils.mCurrentAccountId);
                        }
                        
                        // gionee xuhz 20120528 add for gn theme start
                            entry.setSecondaryAction(
                                    R.drawable.aurora_detail_send_msg,
                                    itSecond,
                                    getString(R.string.description_send_text_message, nameOrNumber));

                        // gionee xuhz 20120528 add for gn theme end
                    }
                    
                    // The following lines are provided and maintained by
                    // Mediatek Inc.
                    // For Video Call.     

                    // The previous lines are provided and maintained by
                    // Mediatek Inc.
                    
                    if (ContactsApplication.sIsAuroraRejectSupport) {
                    	checkForBlack(mNumber); // aurora wangth 20140620 add for black
                    }

                    configureCallButton(entry);
                    
                    // gionee xuhz 20121126 modify for GIUI2.0 start
                    setPhoneNumberAndArea(displayNumber);
                    // gionee xuhz 20121126 modify for GIUI2.0 end
                } else {
                    disableCallButton();
                }
                
                //aurora delete liguangyu 20140616 for BUG #5783 start
            	if (!GNContactsUtils.isMultiSimEnabled()) {
            		gnSwitchUi(mNumberType);
            	}
                //aurora delete liguangyu 20140616 for BUG #5783 end

                mHasEditNumberBeforeCallOption =
                        canPlaceCallsTo && !isSipNumber;
                //mHasTrashOption = hasVoicemail();
                //mHasRemoveFromCallLogOption = !hasVoicemail();

                AuroraListView historyList = (AuroraListView) findViewById(R.id.history);
          
                if (mIsRejectedDetail) {
                	historyList.setAdapter(
                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivity.this, mInflater,
                                    mCallTypeHelper, details, false, canPlaceCallsTo,
                                    findViewById(R.id.controls), true));
                } else {
                	historyList.setAdapter(
                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivity.this, mInflater,
                                    mCallTypeHelper, details, false, canPlaceCallsTo,
                                    findViewById(R.id.controls), false));
                }
                
            	showPhoneRecords(details);

                /**
                * Change Feature by Mediatek End.
                */

                BackScrollManager.bind(
                        new ScrollableHeader() {
                            private View mControls = findViewById(R.id.controls);
                            private View mPhoto = findViewById(R.id.contact_background_sizer);
                            private View mHeader = findViewById(R.id.photo_text_bar);
                            private View mSeparator = findViewById(R.id.blue_separator);
                            private View mCallAndSmsView = findViewById(R.id.aurora_call_and_sms_container);

                            @Override
                            public void setOffset(int offset) {
                                mControls.setY(-offset);
                            }

                            @Override
                            public int getMaximumScrollableHeaderOffset() {
                                // We can scroll the photo out, but we should keep the header if
                                // present.
                            	return 0;
//                                if (mHeader.getVisibility() == View.VISIBLE) {
//                                    return mPhoto.getHeight() - mHeader.getHeight();
//                                } else {
//                                	if (mCallAndSmsView.getVisibility() == View.VISIBLE) {
//                                		return mPhoto.getHeight() + mCallAndSmsView.getHeight();
//                                	}
//                                    // If the header is not present, we should also scroll out the
//                                    // separator line.
//                                    return mPhoto.getHeight() + mSeparator.getHeight();
//                                }
                            }
                        },
                        historyList);
                // gionee xuhz 20121126 modify for GIUI2.0 start
                if (mNumberType == 1 && !mIsRejectedDetail) {
                    if(!mIsPrivate && !mIsPrivateUri) {
                    	firstDetails.photoId = ContactPhotoManager.DEFAULT_UNKOWN_CONTACT_PHOTO;
                    }
            	}
                loadContactPhotosByPhotoId(firstDetails.photoId);
                // gionee xuhz 20121126 modify for GIUI2.0 end

                findViewById(R.id.call_detail).setVisibility(View.VISIBLE);
            }
        }
        mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateContactDetailsTask());
    }

    /** Return the phone call details for a given call log URI. */
    private PhoneCallDetails getPhoneCallDetailsForUri(Uri callUri) {
        ContentResolver resolver = getContentResolver();
//        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
        String selection = null;
        if(ContactsApplication.sIsAuroraPrivacySupport) {
        	selection = "privacy_id > -1 ";
        }
        Cursor callCursor = resolver.query(callUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, selection, null, null);
        
        try {
            if (callCursor == null || !callCursor.moveToFirst()) {
                throw new IllegalArgumentException("Cannot find content: " + callUri);
            }

            ContactInfo contactInfo = ContactInfo.fromCursor(callCursor);
			String photo = callCursor
					.getString(CallLogQuery.CALLS_JOIN_DATA_VIEW_PHOTO_URI);

			Uri photoUri = null;
			if (null != photo) {
				photoUri = Uri.parse(photo);
			}else {
				photoUri=null;
			}
			
			// Gionee:huangzy 20120528 modify for CR00600650 start
			boolean isSpecialNumber = false;
	
				isSpecialNumber = !PhoneNumberHelper.canPlaceCallsTo(contactInfo.number);
				
		
			
            if (isSpecialNumber){
            	contactInfo.formattedNumber = mPhoneNumberHelper.getDisplayNumber(contactInfo.number, null).toString();
            	contactInfo.name = "";
            	contactInfo.nNumberTypeId = 0;
            	contactInfo.label = "";
            	photoUri = null;
            	contactInfo.lookupUri = null;
            }
            
            	return new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                        contactInfo.countryIso, contactInfo.geocode,
                        contactInfo.type, contactInfo.date,
                        contactInfo.duration, contactInfo.name,
                        contactInfo.nNumberTypeId, contactInfo.label,
                        contactInfo.lookupUri, contactInfo.photoId, photoUri, contactInfo.simId,
                        0, contactInfo.numberArea, contactInfo.userMark, contactInfo.markCount, contactInfo.private_id);

            
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    /** Load the contact photos and places them in the corresponding views. */
    // gionee xuhz 20121126 add for GIUI2.0 start
    private void loadContactPhotosByPhotoId(long photoId) {
        // Place photo when discovered in data, otherwise show generic avatar
    	log("loadContactPhotos photoId="+photoId);
        if (photoId != 0) {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
        
                mContactPhotoManager.loadPhoto(mContactBackgroundView, photoId, false, false);
        	
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        } else {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
       
       //aurora add zhouxiaobing 20140419 start
        	  int simId = isSimCardPhoneNumber(this,mNumber);
        	  if(simId > 0) { 
        	      if (FeatureOption.MTK_GEMINI_SUPPORT) {
        	          int iconId = ContactsUtils.getSimBigIcon(mContext, simId);
        	          mContactBackgroundView.setImageResource(iconId);
        	      } else {
        	          mContactBackgroundView.setImageResource(R.drawable.aurora_sim_contact);
        	      }
        	  } else
       //aurora add zhouxiaobing 20140419 end   		  
                mContactBackgroundView.setImageResource(ContactPhotoManager.getDefaultAvatarResId(false, false));
        	
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        	
        	if (mIsRejectedDetail) {
            	mContactBackgroundView.setImageResource(R.drawable.aurora_reject_contact_default);
            } else if(mIsPrivate || mIsPrivateUri) {
            	mContactBackgroundView.setImageResource(R.drawable.aurora_privacy_contact_default_header);
            }
        }
    }
    
    private void setPhoneNumberAndArea(CharSequence displayNumber) {
        mPhoneNumberText.setText(displayNumber);
        updateAreaAndMarkUi ();
    }
    // gionee xuhz 20121126 add for GIUI2.0 end
    
    private void updateAreaAndMarkUi () {
	   	if (!ContactsApplication.sIsGnAreoNumAreaSupport) {
    		return;
	   	}
    	   	    
//    	String area = NumberAreaUtil.getNumAreaFromAora(mNumber);
    	String area = mNumberArea;
    	
    	if (ContactsApplication.sIsAuroraRejectSupport && mNameOrig == null) {
            if (mIsRejectedDetail) {
            	if (mUserMark != null) {
            	    area = mUserMark;
            	} else {
            	    area = mMarkContent;
            	}
            	
            	if (TextUtils.isEmpty(area)) {
            		area = mNumberArea;
            	}
            } else {
//                mUserMark = SogouUtil.getUserMark(mContext, mNumber);
//                mMarkContent = SogouUtil.getMarkContent(mContext, mNumber);
//                mMarkCount = SogouUtil.getMarkNumber(mContext, mNumber);
                String mark = mUserMark;
                Log.d(TAG, "mNameOrig =  = " + mNameOrig + "  mNumber = " + mNumber + "  mUserMark = " + mUserMark + "  mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);
                
                if (mark != null) {
                    mSogouMark1.setText(mContext.getString(R.string.aurora_reject_user_mark));
                    mSogouMark1.setVisibility(View.VISIBLE);
                    mMarkVerDri1.setVisibility(View.VISIBLE);
                    mSogouMark2.setVisibility(View.GONE);
                    mMarkVerDri2.setVisibility(View.GONE);
                    mSogouLogo.setVisibility(View.GONE);
                } else {
                    if (mMarkContent != null) {
                    	mark = mMarkContent;
                    	if (mMarkCount > 0) {
                    		mSogouMark1.setText(String.valueOf(mMarkCount) + mContext.getResources().getString(R.string.aurora_reject_marks));
                    		mSogouMark2.setText(mContext.getResources().getString(R.string.aurora_reject_from_sogou));
                    		mSogouMark1.setVisibility(View.VISIBLE);
                    		mMarkVerDri1.setVisibility(View.VISIBLE);
                    		mSogouMark2.setVisibility(View.VISIBLE);
                    		mMarkVerDri2.setVisibility(View.VISIBLE);
                    		mSogouLogo.setVisibility(View.VISIBLE);
                    	} else {
                    		mSogouMark2.setText(mContext.getResources().getString(R.string.aurora_reject_from_sogou));
                    		mSogouMark1.setVisibility(View.GONE);
                    		mMarkVerDri1.setVisibility(View.GONE);
                    		mSogouMark2.setVisibility(View.VISIBLE);
                    		mMarkVerDri2.setVisibility(View.VISIBLE);
                    		mSogouLogo.setVisibility(View.VISIBLE);
                    	}
                    } else {
                    	mSogouMark.setVisibility(View.GONE);
                    	mSogouMark1.setVisibility(View.GONE);
                    	mSogouMark2.setVisibility(View.GONE);
                    	mMarkVerDri1.setVisibility(View.GONE);
                    	mMarkVerDri2.setVisibility(View.GONE);
                    	mSogouLogo.setVisibility(View.GONE);
                    }
                }
                
                if (mark != null) {
                	mSogouMark.setText(mark);
                	mSogouMark.setVisibility(View.VISIBLE);
                } else {
                	mSogouMark.setVisibility(View.GONE);
                }
            }
        }
    	
    	if (!mIsRejectedDetail) {
    		if (checkHotline() && ContactsApplication.sIsAuroraRejectSupport) {
    			area = mContext.getResources().getString(R.string.aurora_service_number);
    		} else {
    			area = mNumberArea;
    		}
    	}
    	
        if (TextUtils.isEmpty(area)) {
        	area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAora(mContext, mNumber, true);
        	if(area != null){
    			setArea(area);
        	} else {
        		mNumberAreaText.setVisibility(View.GONE);
            	new Thread(new Runnable() {
					public void run() {
		                final String area = NumberAreaUtil.getInstance(mContext).getNumAreaFromAoraLock(mContext, mNumber, true);
		                Activity activity = AuroraCallDetailActivity.this;
		                if(activity != null){
		                	activity.runOnUiThread(new Runnable() {
								public void run() {
		                			setArea(area);
								}
							});
		                }
					}
				}).start();
        	}
    	} else {
			setArea(area);
    	}
    }
    
    private void setArea(String area){
    	if (TextUtils.isEmpty(area)) {
        	mNumberAreaText.setText(mContext.getResources().getString(R.string.aurora_unknow_source_calllog));
        	mNumberAreaText.setVisibility(View.VISIBLE);
        } else {
        	mNumberAreaText.setText(area);
        	mNumberAreaText.setVisibility(View.VISIBLE);
        }
    }

    static final class ViewEntry {
        public final String text;
        public final Intent primaryIntent;
        /** The description for accessibility of the primary action. */
        public final String primaryDescription;

        public CharSequence label = null;
        /** Icon for the secondary action. */
        public int secondaryIcon = 0;
        /** Intent for the secondary action. If not null, an icon must be defined. */
        public Intent secondaryIntent = null;
        /** The description for accessibility of the secondary action. */
        public String secondaryDescription = null;

        // The following lines are provided and maintained by Mediatek Inc.
        /**
         * Intent for the third action-vtCall. If not null, an icon must be
         * defined.
         */
        public Intent thirdIntent = null;
        public String thirdDescription = null;
        public String videoText ;
        
    	public Intent fourthIntent = null;
		public String fourthDescription = null;
		public String ipText;
        /** The description for accessibility of the third action. */


        // The previous lines are provided and maintained by Mediatek Inc.
        
        public ViewEntry(String text, Intent intent, String description) {
            this.text = text;
            primaryIntent = intent;
            primaryDescription = description;
        }

        public void setSecondaryAction(int icon, Intent intent, String description) {
            secondaryIcon = icon;
            secondaryIntent = intent;
            secondaryDescription = description;
        }
    }

    /** Disables the call button area, e.g., for private numbers. */
    private void disableCallButton() {
        //aurora change liguangyu 20131218 for bug #1280 start
        findViewById(R.id.aurora_call_and_sms_container).setVisibility(View.GONE);
        //aurora change liguangyu 20131218 for bug #1280 end
    }

    /** Configures the call button area using the given entry. */
    private void configureCallButton(ViewEntry entry) {
    	View convertView = findViewById(R.id.call_and_sms);
        convertView.setVisibility(View.VISIBLE);
        
        mPhoneTypeText = (TextView)convertView.findViewById(R.id.type);
        mPhoneNumberText = (TextView)convertView.findViewById(R.id.phone_number);
        mNumberAreaText = (TextView)convertView.findViewById(R.id.number_area);
        mSogouMark = (TextView)convertView.findViewById(R.id.sogou_mark);
        mSogouMark1 = (TextView)convertView.findViewById(R.id.sogou_mark1);
        mSogouMark2 = (TextView)convertView.findViewById(R.id.sogou_mark2);
        mMarkVerDri1 = (ImageView)convertView.findViewById(R.id.aurora_ver_drv_1);
        mMarkVerDri2 = (ImageView)convertView.findViewById(R.id.aurora_ver_drv_2);
        mSogouLogo = (ImageView)convertView.findViewById(R.id.aurora_logo);
        
        Drawable right = mContext.getResources().getDrawable(R.drawable.aurora_black_flag);
    	right.setBounds(0, 0, right.getMinimumWidth(), right.getMinimumHeight());
        if (ContactsApplication.sIsAuroraRejectSupport && !mIsRejectedDetail && mIsShowRejectFlag) {
        	mPhoneNumberText.setCompoundDrawables(null, null, right, null);
        } else {
        	mPhoneNumberText.setCompoundDrawables(null, null, null, null);
        }
        
        if (phoneType != null) {
        	mPhoneTypeText.setText(phoneType);
        } else {
        	mPhoneTypeText.setVisibility(View.GONE);
        }

        View primaryActionView = convertView.findViewById(R.id.primary_action_view);
        convertView.setOnClickListener(mPrimaryActionListener);
        convertView.setTag(entry);
        convertView.setContentDescription(entry.primaryDescription);
        convertView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
	    		AuroraAlertDialog dialog = (AuroraAlertDialog) createLongClickDialog();
	    		dialog.show();
	    		return false;
			}        	
        });
        
        ImageView secondaryActionButton = (ImageView) convertView.findViewById(R.id.secondary_action_button);
        
        if (entry.secondaryIntent != null) {
        	secondaryActionButton.setOnClickListener(mSecondaryActionListener);
            secondaryActionButton.setImageResource(entry.secondaryIcon);
        	secondaryActionButton.setVisibility(View.VISIBLE);
        	secondaryActionButton.setTag(entry);
        	secondaryActionButton.setContentDescription(entry.secondaryDescription);
        } else {
        	secondaryActionButton.setVisibility(View.GONE);
        }

    }
    
	private AuroraAlertDialog createLongClickDialog() {
		ArrayList<String> itemList = new ArrayList<String>();		        
        itemList.add(getResources().getString(R.string.copy_text));
        itemList.add(getResources().getString(R.string.gn_edit_number_before_call));        
        CharSequence[] items = itemList.toArray(new CharSequence[itemList.size()]);        
		return new AuroraAlertDialog.Builder(this.mContext)
        .setTitle(mNumber)
        .setItems(items, new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
		        switch (which) {
		            case 0:
		            	gnCopyToClipboard();
		                break;
		            case 1:
		            	startActivity(ContactsUtils.getEditNumberBeforeCallIntent(mNumber));
		                break;
		            default:
		                break;
		        }
			}
		})
		.setTitleDividerVisible(true)
        .setCancelIcon(true)
        .create();
	}
	
	 private void gnCopyToClipboard() {
	        if (TextUtils.isEmpty(mNumber)) return;	        
			ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(ClipData.newPlainText(null, mNumber));
	        String toastText = getString(R.string.toast_text_copied);
	        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	    }
    
    
    public void onMenuCleanCallLog() {
    	new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearSingleCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		.setMessage(R.string.gn_clearSingleCallLogConfirmation)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final StringBuilder callIds = new StringBuilder();
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 begin
				if (ids != null) {
			        for (int id : ids) {
			            if (callIds.length() != 0) {
			                callIds.append(",");
			            }
			            callIds.append(id);
			        }
				}
		        //Gionee <xuhz> <2013-08-01> modify for CR00844946 end

		        mAsyncTaskExecutor.submit(Tasks.REMOVE_FROM_CALL_LOG_AND_FINISH,
		                new AsyncTask<Void, Void, Void>() {
		                    @Override
		                    public Void doInBackground(Void... params) {
		                        getContentResolver().delete(Calls.CONTENT_URI_WITH_VOICEMAIL,
		                                Calls._ID + " IN (" + callIds + ")" + " AND privacy_id > -1", null);
		                        return null;
		                    }

		                    @Override
		                    public void onPostExecute(Void result) {
		                        finish();
		                    }
		                });
			}
		})
		.setNegativeButton(android.R.string.no, null).show();        
    }

    private void configureActionBar(boolean known) {
        Log.d(TAG, "configureActionBar = " +  known);
        //aurora change liguangyu 20131115 for BUG #728 start
//    	if (known) {
//    		setAuroraMenuItems(R.menu.aurora_call_detail);
//    	} else {
//    		setAuroraMenuItems(R.menu.aurora_call_detail_unknow);
//    	}
        
        if (mIsRejectedDetail) {
            return;
        }
        
        try {
			removeMenuById(AuroraMenu.FIRST);
			removeMenuById(AuroraMenu.FIRST + 1);
			removeMenuById(AuroraMenu.FIRST + 2);
			removeMenuById(AuroraMenu.FIRST + 3);
			removeMenuById(AuroraMenu.FIRST + 4);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	if (known) {
    		if (ContactsApplication.sIsAuroraRejectSupport) {
    			if (mIsShowRejectFlag) {
        			addMenu(AuroraMenu.FIRST, getString(R.string.aurora_remove_from_black), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	removeFromBlack();
                        }
                    });
        		} else {
        			addMenu(AuroraMenu.FIRST, getString(R.string.aurora_add_to_black), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	addToBlack();
                        }
                    });
        		}
    		}
    		
            addMenu(AuroraMenu.FIRST + 1, getString(R.string.aurora_menu_delete_calllog), new OnMenuItemClickLisener() {
                public void onItemClick(View menu) {
                	onMenuCleanCallLog();
                }
            });
            addMenu(AuroraMenu.FIRST + 2, getString(R.string.menu_viewContact), new OnMenuItemClickLisener() {
                public void onItemClick(View menu) {
                	if (mContactUri != null) {
                		Intent intent = IntentFactory.newViewContactIntent(mContactUri);
                		if(mIsPrivate || mIsPrivateUri) {
                			intent.putExtra("is_privacy_contact", true);
                		}
                    	startActivity(intent);
                    	mNeedReQuery = true;
                    }
                }
            });
            
    	} else {
    		if (ContactsApplication.sIsAuroraRejectSupport) {
    			if (mIsShowRejectFlag) {
        			addMenu(AuroraMenu.FIRST, getString(R.string.aurora_remove_from_black), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	removeFromBlack();
                        }
                    });
        		} else {
        			addMenu(AuroraMenu.FIRST, getString(R.string.aurora_add_to_black), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	addToBlack();
                        }
                    });
        		}
    			
    			if (mUserMark == null) {
    				addMenu(AuroraMenu.FIRST + 1, getString(R.string.add_number_mark), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	Intent intent = new Intent(AuroraCallDetailActivity.this, AuroraMarkActivity.class);
                        	startActivityForResult(intent, ADD_MARK);
                        }
                    });
    			} else {
    				addMenu(AuroraMenu.FIRST + 1, getString(R.string.edit_number_mark), new OnMenuItemClickLisener() {
                        public void onItemClick(View menu) {
                        	Intent intent = new Intent(AuroraCallDetailActivity.this, AuroraMarkActivity.class);
                        	intent.putExtra("user_mark", mUserMark);
                        	startActivityForResult(intent, EDIT_MARK);
                        }
                    });
    			}
    		}
    		
            addMenu(AuroraMenu.FIRST + 2, getString(R.string.aurora_menu_delete_calllog), new OnMenuItemClickLisener() {
                public void onItemClick(View menu) {
                	onMenuCleanCallLog();
                }
            });
            addMenu(AuroraMenu.FIRST + 3, getString(R.string.menu_newContact), new OnMenuItemClickLisener() {
                public void onItemClick(View menu) {
                	startActivity(IntentFactory.newCreateContactIntent(mNumber));
                	mNeedReQuery = true;
                }
            });
            addMenu(AuroraMenu.FIRST + 4, getString(R.string.aurora_menu_add_exist_contact), new OnMenuItemClickLisener() {
                public void onItemClick(View menu) {
            		startActivity(IntentFactory.newInsert2ExistContactIntent(mNumber));
            		mNeedReQuery = true;
                }
            });
    	}
    	//aurora change liguangyu 20131115 for BUG #728 end
    }

    /** Invoked when the user presses the home button in the action bar. */
    private void onHomeSelected() {
        //Gionee:huangzy 20130401 modify for CR00792013 start
        /*Intent intent = new Intent(Intent.ACTION_VIEW, Calls.CONTENT_URI);*/
    	Intent intent = IntentFactory.newViewContactIntent(Calls.CONTENT_URI);
        //Gionee:huangzy 20130401 modify for CR00792013 end
        // This will open the call log even if the detail view has been opened directly.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        // Immediately stop the proximity sensor.
        disableProximitySensor(false);
        mProximitySensorListener.clearPendingRequests();
        
        
        super.onPause();
    }

    @Override
    public void enableProximitySensor() {
        mProximitySensorManager.enable();
    }

    @Override
    public void disableProximitySensor(boolean waitForFarState) {
        mProximitySensorManager.disable(waitForFarState);
    }
    
    @Override
    protected void onDestroy() {
    	log("onDestroy"); 
        super.onDestroy();
        AuroraSubInfoNotifier.getInstance().removeListener(this);
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        }
    }

    // The following lines are provided and maintained by Mediatek Inc.
//    private TextView mSimName;
    private boolean hasSms = true;

    static final int CALL_SIMID_COLUMN_INDEX = 6;
    static final int CALL_VT_COLUMN_INDEX = 7;
    
	private final View.OnClickListener mThirdActionListener = new View.OnClickListener() {
		@Override
		public void onClick(View view) {
			startActivity(((ViewEntry) view.getTag()).thirdIntent);
		}
	};
    
    private final View.OnClickListener mFourthActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivity(((ViewEntry) view.getTag()).fourthIntent);
        }
    };

    private Handler mHandler = new Handler() {};
    private StatusBarManager mStatusBarMgr;
    
    
    private void log(final String msg) {
        if (true) {
            Log.d(TAG, msg);
        }
    }
    
    public static final String EXTRA_CALL_LOG_NAME = "EXTRA_CALL_LOG_NAME";
    public static final String EXTRA_CALL_LOG_NUMBER_TYPE = "EXTRA_CALL_LOG_NUMBER_TYPE";
    public static final String EXTRA_CALL_LOG_PHOTO_URI = "EXTRA_CALL_LOG_PHOTO_URI";
    public static final String EXTRA_CALL_LOG_LOOKUP_URI = "EXTRA_CALL_LOG_LOOKUP_URI";
    // The previous lines are provided and maintained by Mediatek Inc.
    
    private int[] ids;
    private String mCallNumber;
    private int mCallsCount;
    private int mCallId;
    private int mCallType;
    
    private String phoneType = null;
    // gionee xuhz 20121126 add for GIUI2.0 start
    private TextView mPhoneNumberText;
    private TextView mNumberAreaText;
    private TextView mPhoneTypeText;
    // gionee xuhz 20121126 add for GIUI2.0 end
    private TextView mSogouMark;
    private TextView mSogouMark1;
    private TextView mSogouMark2;
    private ImageView mMarkVerDri1;
    private ImageView mMarkVerDri2;
    private ImageView mSogouLogo;
    
    private static final int AURORA_CALL_DETAIL_MORE = 1;
    
    private boolean mIsFristResume = true;
    private Uri[] gnGetCallLogEntryUris() {
        log("AuroraCallDetailActivity gnGetCallLogEntryUris()");
        
        Uri queryUri = Uri.parse("content://call_log/callsjoindataview");

        if (0 == mCallsCount && !mIsRejectedDetail) {
            // gionee xuhz 20120530 add for CR00611487 start
            Uri uri = getIntent().getData();
            if (uri != null) {
            	try {
            		long id = ContentUris.parseId(uri);
            		uri = ContentUris.withAppendedId(queryUri, id);
            	} catch (Exception ex) { 
            		ex.printStackTrace();
            		return null;
            	}
                return new Uri[]{ uri };
            } 
            // gionee xuhz 20120530 add for CR00611487 end
            
        	return null;
        }
        
        if (1 == mCallsCount && mCallId != 0 && mIsFristResume  && !mIsRejectedDetail) {
        	mIsFristResume = false;        
        	Uri uri = ContentUris.withAppendedId(queryUri, mCallId);
            return new Uri[]{ uri };
        } 
        
        //aurora changes zhouxiaobing 20130925 start               
        // long[] ids = getCallIdsByNumber(mCallNumber, mCallType);
        int[] ids=getIntent().getIntArrayExtra("ids");
        
        if (mIsRejectedDetail) {
        	ids = getCallIdsByNumber(mCallNumber, 1);
        }
        //aurora changes zhouxiaobing 20130925 end
        
        //Gionee:huangzy 20120705 add for CR00632668 start
        if (null == ids) {
            return null;
        }
        //Gionee:huangzy 20120705 add for CR00632668 end
        
		//aurora add liguangyu 20131112 for BUG #639 start    
        if(AuroraCallLogFragmentV2.contactInfos!= null && AuroraCallLogFragmentV2.contactInfos.ids.length > ids.length&& AuroraCallLogFragmentV2.contactInfos.number.equals(mCallNumber)) {
        	
                mCallsCount =AuroraCallLogFragmentV2.contactInfos.ids.length;
                Uri[] uris = new Uri[mCallsCount];
                this.ids = new int[mCallsCount];
                for (int index = 0; index < mCallsCount; ++index) {
                	uris[index] = ContentUris.withAppendedId(queryUri, AuroraCallLogFragmentV2.contactInfos.ids[index]);
                	this.ids[index] = AuroraCallLogFragmentV2.contactInfos.ids[index];
                }
                return uris;
        } 
		//aurora add liguangyu 20131112 for BUG #639 end
        mCallsCount = ids.length;
        Uri[] uris = new Uri[ids.length];
        for (int index = 0; index < ids.length; ++index) {
        	uris[index] = ContentUris.withAppendedId(queryUri, ids[index]);
        }
        return uris;
        
    }
    
    private int[] getCallIdsByNumber(String number, int callType) {
    	String srotOrder = Calls.DATE + " DESC ";
    	
    	//Gionee:huangzy 20120906 modify for CR00688166 start
    	/*String selection = Calls.NUMBER + "='" + number + "'";*/
    	String selection = null;
    	if (ContactsApplication.sIsGnCombineCalllogMatchNumber) {
    		int numLen = number.length();
    		final int MATCH_LEN = ContactsApplication.GN_MATCH_CONTACTS_NUMBER_LENGTH;
    		if (MATCH_LEN <= numLen) {
    			selection = Calls.NUMBER + "='" + number + "' OR " + Calls.NUMBER +
    						" LIKE '%" + number.substring(numLen - MATCH_LEN, numLen) + "'";
    		} else {
    			selection = Calls.NUMBER + "='" + number + "'";
    		}
    	} else {
    		selection = Calls.NUMBER + "='" + number + "'";
    	}
    	//Gionee:huangzy 20120906 modify for CR00688166 end
    	
    	if (mIsRejectedDetail) {
    		selection = selection + " AND reject=1";
    	}
    	
    	if (callType > 0) {
    	    // Gionee:wangth 20130409 modify for CR00793864 begin
    	    /*
    		selection += (" AND " + Calls.TYPE + "=" + callType);
    		*/
    		if (GNContactsUtils.isOnlyQcContactsSupport()) {
    		    if (1 == callType) {
    		        selection += (" AND (" + Calls.TYPE + "=" + callType
    		                + " OR "  + Calls.TYPE + "=" + GNContactsUtils.INCOMMING_VIDEO_TYPE + " OR "  + Calls.TYPE + "=3" + ")");
    		    } else if (2 == callType) {
    		        selection += (" AND (" + Calls.TYPE + "=" + callType
    		                + " OR "  + Calls.TYPE + "=" + GNContactsUtils.OUTGOING_VIDEO_TYPE + ")");
    		    } else if (3 == callType) {
    		        selection += (" AND (" + Calls.TYPE + "=" + callType
    		                + " OR "  + Calls.TYPE + "=" + GNContactsUtils.MISSED_VIDEO_TYPE + ")");
    		    }
    		} else {
    		    selection += (" AND " + Calls.TYPE + "=" + callType);
    		}
    		// Gionee:wangth 20130409 modify for CR00793864 end
    	}
    	
    	Cursor c = getContentResolver().query(Calls.CONTENT_URI, new String[]{Calls._ID}, 
    			selection, null, srotOrder);
    	
    	if (null == c)
    		return null;
    	    	
    	final int count = c.getCount();
    	int[] ids = null;
    	if (count > 0) {
    		c.moveToFirst();
    		ids = new int[count];
    		for (int i = 0; i < count; ++i) {
    			ids[i] = c.getInt(0);
    			c.moveToNext();
    		}    		    	
    	}
    	
    	c.close();
    	return ids;
    }
    
    private boolean foundAndSetPhoneRecords(PhoneCallDetails[] phoneCallDetails) {
    	if (null == phoneCallDetails) {
    		return false;
    	}
    	
    	String path = AuroraStorageManager.getInstance(mContext).getInternalStoragePath();
    	if (path == null) {
    		return false;
    	}
    	
    	String historyPath = path + "/" + mContext.getString(R.string.aurora_call_record_history_path);
    	ArrayList<PhoneCallRecord> records = new ArrayList<PhoneCallRecord>();
    	int found = 0;
    	
    	parseRecording(records, historyPath, false);
    	
    	if (ContactsApplication.sIsAuroraPrivacySupport && AuroraPrivacyUtils.mCurrentAccountId > 0) {
    		historyPath = AuroraPrivacyUtils.mCurrentAccountHomePath
                    + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
    		historyPath = ContactsUtils.replaceBlank(historyPath);
    		parseRecording(records, historyPath, true);
    	}
    	
    	for (PhoneCallDetails detail : phoneCallDetails) {
			for (int i = 0; i < records.size() && found < records.size(); ++i) {
				if (null != records.get(i) && detail.betweenCall(records.get(i).getEndTime())) {
					detail.addPhoneRecords(records.get(i));
					++found;
				}
			}
		}
    	
		return found > 0;
    }
    
    private void parseRecording(ArrayList<PhoneCallRecord> records, String path, boolean isPrivacyPath) {
    	try {
    		synchronized(this) {
    			File file = new File(path);
        		if (file.isDirectory()) {
        			String[] filesArr = file.list();
        			File[] files = file.listFiles();
        			String origName = null;
        			
        			if (filesArr != null) {
        				int fileLen = filesArr.length;
        				
        				if (fileLen > 0) {
        					for (int i = 0; i < fileLen; i++) {
                				String name = filesArr[i];
                				origName = name;
                				String startTime = "";
                				String duration = "";
                				Log.d(TAG, "name = " + name);
                				String postfix = ".3gpp";
	            				if(!TextUtils.isEmpty(name) && name.endsWith(".amr")) {
	            					 postfix = ".amr";
	            				}
                				
                				if (isPrivacyPath && !name.contains(postfix)) {
	            					boolean change = ContactsUtils.auroraChangeFile(files[i].getPath());
	            					Log.i(TAG, "files[i].getPath():" + files[i].getPath() + "  change:" + change);
	            					if (!change) {
	            						continue;
	            					} else {
	            						name = new String(Base64.decode(name, Base64.URL_SAFE), "UTF-8");
	            						try {
	            							boolean rename = files[i].renameTo(new File(path, name));
	            							Log.i(TAG, "rename:" + rename + "  path:" + path + "  name:" + name);
	            						} catch (Exception ex) {
	            							ex.printStackTrace();
	            						}
	            					}
	            				}
                				
                				if (name != null) {
                					if (name.length() > 20) {
                						startTime = name.substring(0, 13);
                    					if (!TextUtils.isEmpty(startTime)) {
                    						long endTime = 0;
                    						long durationTime = 0;
                    						try {
                    							int durEnd = (name.substring(15, name.length())).indexOf("_");
                    							durEnd += 15;
                    							duration = name.substring(14, durEnd);
                    							if (!TextUtils.isEmpty(duration)) {
                    								durationTime = Long.valueOf(duration);
                        							endTime = Long.valueOf(startTime) + durationTime;
                        							PhoneCallRecord record = new PhoneCallRecord();
                            						record.setPath(path + "/" + name);
                            						record.setEndTime(endTime);
                            						record.setDruation(durationTime);
                            	        			record.setMimeType("audio/amr");
                            	        			records.add(record);
                    							}
                    						} catch (Exception e) {
                    							e.printStackTrace();
                    						}
                    					}
                    				}
                				}
                				Log.d(TAG, "name = " + name + "  startTime = " + startTime + " duration = " + duration + "  records.size = " + records.size());
                        	}
        				}
        			}
        		}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    private void showPhoneRecords(final PhoneCallDetails[] phoneCallDetails) {
    	new SimpleAsynTask() {
			@Override
			protected Integer doInBackground(Integer... params) {
				boolean founded = foundAndSetPhoneRecords(phoneCallDetails);
				return founded ? 1 : 0;
			}
			
			@Override
			protected void onPostExecute(Integer result) {
				if (0 == result)
					return;
				
				AuroraListView historyList = (AuroraListView) findViewById(R.id.history);
				if (null != historyList && null != historyList.getAdapter()) {
					((BaseAdapter)(historyList.getAdapter())).notifyDataSetChanged();
				}
			}
			
		}.execute();
    }
    
    private void gnSwitchUi(int numberType) {
    	
    	    switch (mNumberType) {
            case 0://Saved Number
            	configureActionBar(true);
                mHeaderTextView.setVisibility(View.INVISIBLE);                      
                mHeaderOverlayView.setVisibility(View.VISIBLE);
//                mMainActionView.setVisibility(View.VISIBLE);
                break;
            case 1://not saved Number
            	configureActionBar(false);
                mHeaderTextView.setVisibility(View.INVISIBLE);
//                mMainActionView.setVisibility(View.INVISIBLE);
                break;
            case 2://Number Unshow                
                mHeaderTextView.setVisibility(View.INVISIBLE);                    
                mHeaderOverlayView.setVisibility(View.VISIBLE);
//                mMainActionView.setVisibility(View.VISIBLE);
                break;
            default:
                break;
            }
           	
	}
	
    /**
     * Update user interface with details of given call.
     *
     * @param callUris URIs into {@link CallLog.Calls} of the calls to be displayed
     */
    private void updateHistoryList(final Uri... callUris) {
        class UpdateHistoryListTask extends AsyncTask<Void, Void, PhoneCallDetails[]> {
            @Override
            public PhoneCallDetails[] doInBackground(Void... params) {
                // TODO: All phone calls correspond to the same person, so we can make a single
                // lookup.
                if (callUris == null) {
                    Log.w(TAG, "callUris is null");
                    return null;
                }
                
                final int numCalls = callUris.length;
                PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
                try {
                    for (int index = 0; index < numCalls; ++index) {
                        details[index] = getPhoneCallDetailsForUri(callUris[index]);
                    }
                    return details;
                } catch (IllegalArgumentException e) {
                    // Something went wrong reading in our primary data.
                    Log.w(TAG, "invalid URI starting call details", e);
                    return null;
                }
            }

            @Override
            public void onPostExecute(PhoneCallDetails[] details) {
                if (details == null) {
                    // Somewhere went wrong: we're going to bail out and show error to users.
                    Toast.makeText(AuroraCallDetailActivity.this, R.string.toast_call_detail_error,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // We know that all calls are from the same number and the same contact, so pick the
                // first.
                PhoneCallDetails firstDetails = details[0];
                mNumber = firstDetails.number.toString();
                if (!TextUtils.isEmpty(firstDetails.name)) {
                    mName = firstDetails.name.toString();
                } else {
                    mName = null;
                }
                
                // Cache the details about the phone number.
                final boolean canPlaceCallsTo = mPhoneNumberHelper.canPlaceCallsTo(mNumber);

                AuroraListView historyList = (AuroraListView) findViewById(R.id.history);
       
                if (mIsRejectedDetail) {
                	historyList.setAdapter(
                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivity.this, mInflater,
                                    mCallTypeHelper, details, false, canPlaceCallsTo,
                                    findViewById(R.id.controls), true));
                } else {
                	historyList.setAdapter(
                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivity.this, mInflater,
                                    mCallTypeHelper, details, false, canPlaceCallsTo,
                                    findViewById(R.id.controls), false));
                }
                
             
                	showPhoneRecords(details);
                
            }
        }
        mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateHistoryListTask());
    }

    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_CALL_DETAIL_MORE:
            	if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
            		return;
            	}
            	
//                showAuroraMenu();
            	if (GNContactsUtils.isMultiSimEnabled()) {
            		gnSwitchUi(mNumberType);
            	}
            	showCustomMenu();
                break;
            default:
                break;
            }
        }
    };
    
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            switch (itemId) {
            
            case R.id.menu_add_new_contact: {
            	startActivity(IntentFactory.newCreateContactIntent(mNumber));
    			break;
            }
    			
    		case R.id.menu_add_exist_contact: {
    			startActivity(IntentFactory.newInsert2ExistContactIntent(mNumber));
    			break;
    		}

            default:
                break;
            }
        }
    };
    
    private static final int getTypeLabelResource(int type) {
			switch (type) {
			case Phone.TYPE_MOBILE: return R.string.telType_Moblie;
			case Phone.TYPE_HOME: return R.string.telType_Home;
			case Phone.TYPE_WORK: return R.string.telType_Work;
			case Phone.TYPE_FAX_WORK: return R.string.telType_Fax;
			case Phone.TYPE_OTHER: return R.string.othertype;
			default: break;
		}
			return R.string.telType_Moblie;
    }
    
    //aurora add zhouxiaobing 20140419 start
	private int isSimCardPhoneNumber(Context context,String number)
	{
		Cursor cursor=context.getContentResolver().query(Data.CONTENT_URI, new String[]{Data.RAW_CONTACT_ID},
				Data.DATA1 + " = '" + number + "'", null, null);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				long contact_id=cursor.getLong(0);
				cursor.close();
				Cursor cursor2=context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{RawContacts.INDICATE_PHONE_SIM},
						android.provider.ContactsContract.RawContacts._ID+" = " +contact_id+ " AND "+ "deleted"+" < 1" + " and is_privacy > -1", null, null);
				Log.v("CallCard", "cursor2 count="+cursor2.getCount()+"contact_id="+contact_id);
				if(cursor2!=null)
				{
					if(cursor2.moveToFirst())
					{
						int simid=cursor2.getInt(0);
						Log.v("CallCard", "simid="+simid);
						cursor2.close();
						return simid;
					}
					cursor2.close();
				}
			}
			else
			{
			 cursor.close();
			}
		}
		return 0;
		
	}
    
    //aurora add zhouxiaobing 20140419 end
	
	private int mSlot = -1;
	
	private void checkForBlack(String number) {
		if (!ContactsApplication.sIsAuroraRejectSupport) {
		    return;
		}
		
		Cursor cursor = mContext.getContentResolver().query(
				Uri.parse("content://com.android.contacts/black"), new String[]{"reject"},
				GNContactsUtils.getPhoneNumberEqualString(number) + " and isblack=1", null, null);
		if (cursor != null) {
		    if (cursor.moveToFirst()) {
		    	int reject = cursor.getInt(0);
		        if (reject > 0) {
		        	mIsShowRejectFlag = true;
		        } else {
		        	mIsShowRejectFlag = false;
		        }
		    }  else {
	        	mIsShowRejectFlag = false;
	        }
		    cursor.close();
		} else {
        	mIsShowRejectFlag = false;
        }
		
		if (!mIsShowRejectFlag) {
			Cursor c = mContext.getContentResolver().query(
					Uri.parse("content://com.android.contacts/black"), new String[]{"reject"},
					"number='" + number + "' and isblack=1", null, null);
			if (c != null) {
			    if (c.moveToFirst()) {
			    	int reject = c.getInt(0);
			        if (reject > 0) {
			        	mIsShowRejectFlag = true;
			        } else {
			        	mIsShowRejectFlag = false;
			        }
			    }  else {
		        	mIsShowRejectFlag = false;
		        }
			    c.close();
			} else {
	        	mIsShowRejectFlag = false;
	        }
		}
	}
	
	private void addToBlack() {
	    Intent intent = new Intent();
	    intent.setClassName("com.aurora.reject", "com.aurora.reject.AuroraManuallyAddActivity");
	    try {
	    	Bundle bundle = new Bundle();
            bundle.putString("add_name", mNameOrig);
            bundle.putString("add_number", mNumber);
            bundle.putString("user_mark", mUserMark);
            intent.putExtras(bundle);
            intent.putExtra("add", true);
	    	mContext.startActivity(intent);
	    } catch (ActivityNotFoundException e) {
	        e.printStackTrace();
	    }
	}
	
	private void removeFromBlack() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
		final AuroraCheckBox checkBox = (AuroraCheckBox)view.findViewById(R.id.check_box);
		checkBox.setChecked(true);

		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
				AuroraCallDetailActivity.this)
				.setTitle(mContext.getResources().getString(R.string.black_remove))
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								boolean recoveryLogs = checkBox.isChecked();
								int isblack = 0;
								if (!recoveryLogs) {
									isblack = -1;
								}
								
								ContentValues values = new ContentValues();
								values.put("isblack", isblack);
								values.put("number", mNumber);
								values.put("reject", 0);
				            	mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values, GNContactsUtils.getPhoneNumberEqualString(mNumber), null);
				                values.clear();
				                
				                mIsShowRejectFlag = false;
				                mPhoneNumberText.setCompoundDrawables(null, null, null, null);
				                gnSwitchUi(mNumberType);
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.dismiss();
							}
						}).show();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_MARK: {
            	if (data != null) {
            		mUserMark = data.getStringExtra("user_mark");
            		YuloreUtils.getInstance(mContext).insertUserMark(mContext, mNumber, mUserMark);
            		Log.d(TAG, "ADD_MARK mUserMark = " + mUserMark + "  mNumber = " + mNumber + " mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);
                	
                	configureActionBar(false);
                	updateAreaAndMarkUi();
                	updateCallsAndBlackMark();
            	}
            	break;
            }
            
            case EDIT_MARK: {
            	if (data != null) {
            		mUserMark = data.getStringExtra("user_mark");
            		
            		if (mUserMark == null) {
            			YuloreUtils.getInstance(mContext).deleteUserMark(mContext, mNumber);
            			mIsNoUserMark = true;
            			
            			new Thread() {
                			public void run() {
                				mMarkContent = YuloreUtils.getInstance(mContext).getMarkContent( mNumber,mContext);
                    			mMarkCount = YuloreUtils.getInstance(mContext).getMarkNumber(mContext, mNumber);
                        		Log.d(TAG, "EDIT_MARK mUserMark = " + mUserMark + "  mNumber = " + mNumber + " mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);
                            	updateCallsAndBlackMark();
                			}
                		}.start();
            		} else {
            			YuloreUtils.getInstance(mContext).insertUserMark(mContext, mNumber, mUserMark);
            			updateCallsAndBlackMark();
            		}
            		
            		configureActionBar(false);
                	updateAreaAndMarkUi();
            	}
            	break;
            }
        }
	}
	
	private void updateCallsAndBlackMark () {
		new Thread() {
    		public void run() {
    			try{
    				ContentValues cv = new ContentValues();
					int userMark = -1;
					String mark = mUserMark;
					if (mark == null) {
						mark = mMarkContent;
						userMark = mMarkCount;
					}
					
					cv.put("mark", mark);
					cv.put("user_mark", userMark);
					mContext.getContentResolver().update(Calls.CONTENT_URI, cv,
							GNContactsUtils.getPhoneNumberEqualString(mNumber) + " and reject in(0, 1)", null);
					
					cv.clear();
					Uri blackUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black");
					cv.put("lable", mark);
					cv.put("user_mark", userMark);
					mContext.getContentResolver().update(blackUri, cv,
							GNContactsUtils.getPhoneNumberEqualString(mNumber), null);
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
		}.start();
	}
	
	private boolean checkHotline() {	
	    
	    return false;
	}
	
	private boolean mIsPrivate = false;
	private boolean mIsPrivateUri = false;
	
//    private Uri mPrivateContactUri = null; 
//    private long mRawContactId = 0;
	
//    private Uri[] getPrivateCallLogEntryUris() {
//        log("getPrivateCallLogEntryUris()");
//        
//        Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
//        
//        int[] ids = getPrivateCallIds();       
//        
//        if (null == ids) {
//            return null;
//        }
//        
//        mCallsCount = ids.length;
//        Uri[] uris = new Uri[ids.length];
//        for (int index = 0; index < ids.length; ++index) {
//        	uris[index] = ContentUris.withAppendedId(queryUri, ids[index]);
//        }
//        return uris;        
//    }
//    
//    private int[] getPrivateCallIds() {
//    	String srotOrder = Calls.DATE + " DESC ";
//    	
//    	String selection = null;
//    	selection = " raw_contact_id =" + mRawContactId;
//    	
//    	
//        selection = selection + " AND privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();   	
//
//    	Cursor c = getContentResolver().query(Calls.CONTENT_URI, new String[]{Calls._ID}, 
//    			selection, null, srotOrder);
//    	
//    	if (null == c) {
//    		return null;
//    	}
//    	    	
//    	final int count = c.getCount();
//    	int[] ids = null;
//    	if (count > 0) {
//    		c.moveToFirst();
//    		ids = new int[count];
//    		for (int i = 0; i < count; ++i) {
//    			ids[i] = c.getInt(0);
//    			c.moveToNext();
//    		}    		    	
//    	}
//    	
//    	c.close();
//    	return ids;
//    }
	
	private void sendSimContactBroad() {
        Intent intent = new Intent("com.android.action.LAUNCH_CONTACTS_LIST");
        sendBroadcast(intent);
	}
	
	public void onSubscriptionsChanged() {
		Uri[] callEntryUris = getCallLogEntryUris();
		if (callEntryUris != null) {
			if (callEntryUris.length > 1) {
				updateData(callEntryUris[0]);
				updateHistoryList(callEntryUris);
			} else {
				updateData(callEntryUris);
			}
		}	
	}
	
	
}

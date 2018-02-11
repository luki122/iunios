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
import com.android.contacts.calllog.CallDetailHistoryAdapter;
import com.android.contacts.calllog.CallLogQuery;
import com.android.contacts.calllog.CallTypeHelper;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.calllog.IntentProvider;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.GnCallForSelectSim;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;

import android.R.integer;
import android.app.StatusBarManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
// import gionee.provider.GnSettings;

/**
 * import com.android.contacts.voicemail.VoicemailPlaybackFragment;
 * import com.android.contacts.voicemail.VoicemailStatusHelper;
 * import com.android.contacts.voicemail.VoicemailStatusHelper.StatusMessage;
 * import com.android.contacts.voicemail.VoicemailStatusHelperImpl;
 * 
 */
import com.android.internal.telephony.ITelephony;

import android.app.ActionBar;
import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
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
import gionee.provider.GnContactsContract;
import android.provider.MediaStore;
import gionee.provider.GnCallLog.Calls;
import android.provider.Contacts.Intents.Insert;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.Contacts;
import android.provider.MediaStore.Audio.Media;
import gionee.provider.GnTelephony.SIMInfo;
//import android.provider.VoicemailContract.Voicemails;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.util.Constants;
import gionee.app.GnStatusBarManager;

// Gionee:wangth 20120710 add for CR00633799 begin
import gionee.provider.GnContactsContract.Data;
// Gionee:wangth 20120710 add for CR00633799 end
// Gionee:xuhz 20130328 add for CR00790874 start
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
// Gionee:xuhz 20130328 add for CR00790874 end

import aurora.app.AuroraActivity;

import aurora.provider.AuroraSettings;


/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class CallDetailActivity extends AuroraActivity implements ProximitySensorAware, OnClickListener {
    private static final String TAG = "CallDetail";

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
    private View mHeaderOverlayView;
    private ImageView mMainActionView;
    private ImageButton mMainActionPushLayerView;
    private View mMainActionArrowRightView;   //4.9.0
    private ImageView mContactBackgroundView;
    private AsyncTaskExecutor mAsyncTaskExecutor;
    private ContactInfoHelper mContactInfoHelper;

    private String mNumber = null;
    private String mDefaultCountryIso;

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

    /** Listener to changes in the proximity sensor state. */
    private class ProximitySensorListener implements ProximitySensorManager.Listener {
        /** Used to show a blank view and hide the action bar. */
        private final Runnable mBlankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.VISIBLE);
                getActionBar().hide();
            }
        };
        /** Used to remove the blank view and show the action bar. */
        private final Runnable mUnblankRunnable = new Runnable() {
            @Override
            public void run() {
                View blankView = findViewById(R.id.blank);
                blankView.setVisibility(View.GONE);
                getActionBar().show();
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

    static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls.DATE,
        Calls.DURATION,
        Calls.NUMBER,
        Calls.TYPE,
        Calls.COUNTRY_ISO,
        Calls.GEOCODED_LOCATION,
        //The following lines are provided and maintained by Mediatek Inc.
        Calls.SIM_ID,
        Calls.VTCALL,
        //The previous lines are provided and maintained by Mediatek Inc.
    };

    static final int DATE_COLUMN_INDEX = 0;
    static final int DURATION_COLUMN_INDEX = 1;
    static final int NUMBER_COLUMN_INDEX = 2;
    static final int CALL_TYPE_COLUMN_INDEX = 3;
    static final int COUNTRY_ISO_COLUMN_INDEX = 4;
    static final int GEOCODED_LOCATION_COLUMN_INDEX = 5;

    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        	// gionee xuhz 20121224 add for CR00752005 start
    		if (ContactsApplication.sIsGnDualSimSelectSupport) {
				String actionString = ((ViewEntry) view.getTag()).primaryIntent.getAction();
				if ("com.android.contacts.action.GNSELECTSIM".equalsIgnoreCase(actionString)) {
                    int[] location = new int[2] ;
                    view.getLocationOnScreen(location);
                    DisplayMetrics dm = getResources().getDisplayMetrics();
                    int y = location[1] - dm.heightPixels/2;
                    y = y + 60;
                	
					((ViewEntry) view.getTag()).primaryIntent.putExtra("y", y);
				}
    		}
            // gionee xuhz 20121224 add for CR00752005 end
            startActivity(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
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
        // gionee xuhz add for support gn theme start
        if (ContactsApplication.sIsGnTransparentTheme) {
            setTheme(R.style.GN_CallDetailActivityTheme);
        } else if (ContactsApplication.sIsGnDarkTheme) {
            setTheme(R.style.GN_CallDetailActivityTheme_dark);
        } else if (ContactsApplication.sIsGnLightTheme) {
            setTheme(R.style.GN_CallDetailActivityTheme_light);
        } 
        // gionee xuhz add for support gn theme start
        
        log("CallDetailActivity  onCreat()");
        super.onCreate(icicle);
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	gnOnCreate(icicle);
        	return;
        }
        
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
          setContentView(R.layout.call_detail);
        * Descriptions:
        */
        setContentView(R.layout.call_detail_without_voicemail);
        /**
        * Change Feature by Mediatek End.
        */

        mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();

        mCallTypeHelper = new CallTypeHelper(getResources());
        mPhoneNumberHelper = new PhoneNumberHelper(mResources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
                                                             mPhoneNumberHelper, null, this);
        mAsyncQueryHandler = null;//new CallDetailActivityQueryHandler(this);
        mHeaderTextView = (TextView) findViewById(R.id.header_text);
        mHeaderOverlayView = findViewById(R.id.photo_text_bar);
        
        // The following lines are deleted by Mediatek Inc to close Google default
        // Voicemail function.
        /**
         mVoicemailStatusHelper = new VoicemailStatusHelperImpl();
         mStatusMessageView = findViewById(R.id.voicemail_status);
         mStatusMessageText = (TextView)
         findViewById(R.id.voicemail_status_message);
         mStatusMessageAction = (TextView)
         findViewById(R.id.voicemail_status_action);
        */
        // The previous lines are deleted by Mediatek Inc to close Google default
        // Voicemail function.
        mMainActionView = (ImageView) findViewById(R.id.main_action);
        mMainActionPushLayerView = (ImageButton) findViewById(R.id.main_action_push_layer);
        mContactBackgroundView = (ImageView) findViewById(R.id.contact_background);
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(this);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);
        mProximitySensorManager = new ProximitySensorManager(this, mProximitySensorListener);
        mContactInfoHelper = new ContactInfoHelper(this, ContactsUtils.getCurrentCountryIso(this));
        // The following lines are provided and maintained by Mediatek Inc.
        mSimName = (TextView) findViewById(R.id.sim_name);
        SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // The previous lines are provided and maintained by Mediatek Inc.
        configureActionBar();
        //optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.
    }

    @Override
    public void onResume() {
        super.onResume();

        hasSms = PhoneCapabilityTester.isSmsIntentRegistered(getApplicationContext());
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	mHandler.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				// gionee xuhz 20120912 modify for CR00692279 start
    				Uri[] callEntryUris = getCallLogEntryUris();
    				if (callEntryUris != null) {
        				if (callEntryUris.length > 1) {
        					updateData(callEntryUris[0]);
        					updateHistoryList(callEntryUris);
        				} else {
        					updateData(callEntryUris);
        				}
    				}
    				// gionee xuhz 20120912 modify for CR00692279 end
    			}
    		}, mIsFristResume ? 0 : 1000);
        } else {
        	// gionee xuhz 20120912 modify for CR00692279 start
			Uri[] callEntryUris = getCallLogEntryUris();
			if (callEntryUris != null) {
				if (callEntryUris.length > 1) {
					updateData(callEntryUris[0]);
					updateHistoryList(callEntryUris);
				} else {
					updateData(callEntryUris);
				}
			}
			// gionee xuhz 20120912 modify for CR00692279 end
        }
      //The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            setSimIndicatorVisibility(true);
			// gionee tianliang 20120925 modify for CR00692598 start
            mShowSimIndicator = true;
			// gionee tianliang 20120925 modify for CR00692598 end
        }
        //The previous lines are provided and maintained by Mediatek Inc.
        
        // Gionee:xuhz 20130328 add for CR00790874 start
        if (ContactsApplication.sIsHandSensorDial) {
            mSensorMgr.registerListener(mGnHandSensorEventListener, mGnHandSensor, 12000);
        }
        // Gionee:xuhz 20130328 add for CR00790874 end
    }
    
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /**
     * Handle voicemail playback or hide voicemail ui.
     * <p>
     * If the Intent used to start this Activity contains the suitable extras, then start voicemail
     * playback.  If it doesn't, then hide the voicemail ui.
     */
    /**
    private void optionallyHandleVoicemail() {
        View voicemailContainer = findViewById(R.id.voicemail_container);
        if (hasVoicemail()) {
            // Has voicemail: add the voicemail fragment.  Add suitable arguments to set the uri
            // to play and optionally start the playback.
            // Do a query to fetch the voicemail status messages.
            VoicemailPlaybackFragment playbackFragment = new VoicemailPlaybackFragment();
            Bundle fragmentArguments = new Bundle();
            fragmentArguments.putParcelable(EXTRA_VOICEMAIL_URI, getVoicemailUri());
            if (getIntent().getBooleanExtra(EXTRA_VOICEMAIL_START_PLAYBACK, false)) {
                fragmentArguments.putBoolean(EXTRA_VOICEMAIL_START_PLAYBACK, true);
            }
            playbackFragment.setArguments(fragmentArguments);
            voicemailContainer.setVisibility(View.VISIBLE);
            getFragmentManager().beginTransaction()
                    .add(R.id.voicemail_container, playbackFragment).commitAllowingStateLoss();
            mAsyncQueryHandler.startVoicemailStatusQuery(getVoicemailUri());
            markVoicemailAsRead(getVoicemailUri());
        } else {
            // No voicemail uri: hide the status view.
            mStatusMessageView.setVisibility(View.GONE);
            voicemailContainer.setVisibility(View.GONE);
        }
    }

    private boolean hasVoicemail() {
        return getVoicemailUri() != null;
    }

    private Uri getVoicemailUri() {
        return getIntent().getParcelableExtra(EXTRA_VOICEMAIL_URI);
    }

    private void markVoicemailAsRead(final Uri voicemailUri) {
        log("CallDetailActivity  markVoicemailAsRead()");
        mAsyncTaskExecutor.submit(Tasks.MARK_VOICEMAIL_READ, new AsyncTask<Void, Void, Void>() {
            @Override
            public Void doInBackground(Void... params) {
                ContentValues values = new ContentValues();
                values.put(Voicemails.IS_READ, true);
                getContentResolver().update(voicemailUri, values,
                        Voicemails.IS_READ + " = 0", null);
                return null;
            }
        });
    }
    */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    /**
     * Returns the list of URIs to show.
     * <p>
     * There are two ways the URIs can be provided to the activity: as the data on the intent, or as
     * a list of ids in the call log added as an extra on the URI.
     * <p>
     * If both are available, the data on the intent takes precedence.
     */
    private Uri[] getCallLogEntryUris() {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		return gnGetCallLogEntryUris();    		
    	}
    	
        log("CallDetailActivity getCallLogEntryUris()");
        Uri uri = getIntent().getData();
        if (uri != null) {
            // If there is a data on the intent, it takes precedence over the extra.
         	Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
            //uris[index] = ContentUris.withAppendedId(Calls.CONTENT_URI_WITH_VOICEMAIL, ids[index]);
        	long id = ContentUris.parseId(uri);
        	uri = ContentUris.withAppendedId(queryUri, id);
            return new Uri[]{ uri };
        } 
        long[] ids = getIntent().getLongArrayExtra(EXTRA_CALL_LOG_IDS);
        Uri[] uris = new Uri[ids.length];
        for (int index = 0; index < ids.length; ++index) {
        	Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
            //uris[index] = ContentUris.withAppendedId(Calls.CONTENT_URI_WITH_VOICEMAIL, ids[index]);
        	uris[index] = ContentUris.withAppendedId(queryUri, ids[index]);
        }
        return uris;
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
                	if (ContactsApplication.sIsGnContactsSupport) {
                		callIntent = IntentFactory.newDialNumberIntent(mNumber);
                	} else {
                		callIntent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                Uri.fromParts("tel", mNumber, null));
                	}
                	// Gionee <fenglp> <2013-08-19> add for CR00861033 begin
                    if (callIntent != null) {
                        callIntent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                        startActivity(callIntent);
                        return true;
                    }
                    // Gionee <fenglp> <2013-08-19> add for CR00861033 end
                }
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
        	//GIONEE: lujian 2012.10.09 modify for "explay CR00709996" begin-->
        	private final boolean gnFlyFlag = SystemProperties.get("ro.gn.oversea.custom").equals("RUSSIA_FLY");
        	//GIONEE: lujian 2012.10.09 modify for "explay CR00709996"   end-->
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
                    Toast.makeText(CallDetailActivity.this, R.string.toast_call_detail_error,
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
                
                final Uri contactUri = firstDetails.contactUri;
                final Uri photoUri = firstDetails.photoUri;

                // Set the details header, based on the first phone call.
                mPhoneCallDetailsHelper.setCallDetailsHeader(mHeaderTextView, firstDetails);
                
                //Gionee:huangzy 20120612 modify for CR00614326 start
                //Gionee:huangzy 20120528 add for CR00611149 start
                int numberType = 0;
                if ((null != mNumber && mNumber.equals("-1"))) {
                    numberType = 2;
                } else if (null == contactUri) {
                    numberType = 1;
                }                
                gnSwitchUi(numberType, firstDetails);
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
                if (!TextUtils.isEmpty(firstDetails.name)) {
                    nameOrNumber = firstDetails.name;
                } else {
                    nameOrNumber = firstDetails.number;
                }

                if (contactUri != null) {
                	//Gionee:huangzy 20130401 modify for CR00792013 start
                    /*mainActionIntent = new Intent(Intent.ACTION_VIEW, contactUri);*/
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
                    // // - define GnContactsContract.Intents.Insert.SIP_ADDRESS,
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
                    			GnContactsContract.Intents.Insert.SIP_ADDRESS,
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
                    mMainActionView.setVisibility(View.INVISIBLE);
                    mMainActionPushLayerView.setVisibility(View.GONE);
                    //4.9.0 begin
                    if (null != mMainActionArrowRightView) {
                    	mMainActionArrowRightView.setVisibility(View.GONE);	
                    }
                    //4.9.0 end
                    mHeaderTextView.setVisibility(View.INVISIBLE);
                    mHeaderOverlayView.setVisibility(View.INVISIBLE);
                } else {
                	//Gionee:huangzy 20120528 modify for CR00611149 start
                	if (ContactsApplication.sIsGnContactsSupport && null == contactUri) {                    	
                    } else {
                    	mMainActionView.setVisibility(View.VISIBLE);
                    	mMainActionView.setImageResource(mainActionIcon);
                    	mMainActionPushLayerView.setVisibility(View.VISIBLE);
                      //4.9.0 begin
                    	if (null != mMainActionArrowRightView) {
                    		mMainActionArrowRightView.setVisibility(View.VISIBLE);
                    	}
                       //4.9.0 end
                    	mMainActionPushLayerView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startActivity(mainActionIntent);
                            }
                        });
                        mMainActionPushLayerView.setContentDescription(mainActionDescription);
                        
                        mHeaderOverlayView.setVisibility(View.VISIBLE);
                    }
                    //Gionee:huangzy 20120528 modify for CR00611149 end
                }
                // The following lines are provided and maintained by Mediatek
                // Inc.
                if (FeatureOption.MTK_GEMINI_SUPPORT ) {
                	 mSimName.setPadding(4, 2, 4, 2);
                    if (firstDetails.simId == ContactsUtils.CALL_TYPE_SIP) {
                        mSimName
                                .setBackgroundResource(R.drawable.sim_background_sip);
                        mSimName.setText(R.string.call_sipcall);
                        mSimName.setPadding(4, 2, 4, 2);
                    } else if (ContactsUtils.CALL_TYPE_NONE == firstDetails.simId) {
                        mSimName.setVisibility(View.INVISIBLE);
                    } else {
                        String simName = SIMInfoWrapper.getDefault().getSimDisplayNameById(
                                firstDetails.simId);
                        if (null != simName) {
                            // Gionee:lihuafang 20120504 add for CR00588600 begin
                            /*
                            mSimName.setText(simName);
                            */
                            int slot = SIMInfoWrapper.getDefault().getSimSlotById(
                                    firstDetails.simId);
                            if (ContactsUtils.mIsGnContactsSupport
                                    && ContactsUtils.mIsGnShowSlotSupport) {
                                if (slot == 0) {
                                    mSimName.setText(getString(R.string.slot_a) + simName);
                                } else if (slot == 1) {
                                    mSimName.setText(getString(R.string.slot_b) + simName);
                                } else {
                                    mSimName.setText(simName);
                                }
                            } else {
                                mSimName.setText(simName);
                            }
                            // Gionee:lihuafang 20120504 add for CR00588600 end
                            mSimName.setPadding(4, 2, 4, 2);
                        } else {
                            mSimName.setVisibility(View.INVISIBLE);
                        }
                        int color = SIMInfoWrapper.getDefault().getInsertedSimColorById(
                                firstDetails.simId);
                        int simColorResId = SIMInfoWrapper.getDefault().getSimBackgroundResByColorId(color);
                        if (-1 != color) {
                            mSimName.setBackgroundResource(simColorResId);
                            mSimName.setPadding(4, 2, 4, 2);
                        } else {
                            mSimName
                                    .setBackgroundResource(R.drawable.sim_background_locked);
                            mSimName.setPadding(4, 2, 4, 2);
                        }
                    }
                } else {
                    mSimName.setVisibility(View.GONE);
                }
                // The previous lines are provided and maintained by Mediatek
                // Inc.
                // This action allows to call the number that places the call.
                if (canPlaceCallsTo) {
                    boolean isVoicemailUri = PhoneNumberHelper.isVoicemailUri(numberCallUri);
                    int slotId = SIMInfoWrapper.getDefault().getSimSlotById((int)firstDetails.simId);
                    
                    final CharSequence displayNumber =
                            mPhoneNumberHelper.getDisplayNumber(
                                    firstDetails.number, firstDetails.formattedNumber);

                    Intent it;
                    if (ContactsApplication.sIsGnAreoNumAreaSupport) {
                    	// gionee xuhz 20121215 modify for Dual Sim Select start
                    	if (ContactsApplication.sIsGnDualSimSelectSupport) {
                        	it = new Intent("com.android.contacts.action.GNSELECTSIM");
                        	it.putExtra("callUri", numberCallUri.toString());
                    	} else {
                    		it = IntentFactory.newDialNumberIntent(numberCallUri);
                    	}
                        // gionee xuhz 20121215 modify for Dual Sim Select end
                    	
                    } else {
                    	it = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri)
                        .putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long)firstDetails.simId);
                    }
                    
                	// gionee xuhz 20121215 modify for Dual Sim Select start
                	if (!ContactsApplication.sIsGnDualSimSelectSupport) {
	                    it.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
	                    if (isVoicemailUri && slotId != -1) {
	                        it.putExtra("simId", slotId);
	                    }
                	}
                    // gionee xuhz 20121215 modify for Dual Sim Select end
                    //GIONEE: lujian 2012.10.09 modify for "explay CR00709996" begin-->
                    int gnResId = R.string.menu_callNumber;
                    if(gnFlyFlag) {
                    	gnResId = R.string.zzzzz_gn_menu_callNumber;
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
                        if (0 == firstDetails.numberType) {
                            // Gionee:wangth 20120710 modify for CR00633799 begin
                            /*
                            entry.label = mResources.getString(R.string.list_filter_custom);
                            */
                            if (ContactsUtils.mIsGnContactsSupport) {
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
                                if (nLabel != null && !nLabel.toUpperCase().isEmpty()) {
                                // gionee xuhz 20120820 modify for CR00678736 end
                                    entry.label = nLabel.toUpperCase();
                                } else {
                                    entry.label = mResources.getString(R.string.list_filter_custom);
                                }
                            } else {
                                entry.label = mResources.getString(R.string.list_filter_custom);
                            }
                            // Gionee:wangth 20120710 modify for CR00633799 end
                        } else {
                            entry.label = Phone.getTypeLabel(mResources, firstDetails.numberType,
                                    firstDetails.numberLabel);
                        }
                    }

                    // The secondary action allows to send an SMS to the number that placed the
                    // call.
                    if (mPhoneNumberHelper.canSendSmsTo(mNumber) && hasSms) {
                        //Gionee:huangzy 20130401 modify for CR00792013 start
                        /*Intent itSecond = new Intent(Intent.ACTION_SENDTO,
                                Uri.fromParts("sms", mNumber, null));*/
                    	Intent itSecond = IntentFactory.newCreateSmsIntent(mNumber);
                        //Gionee:huangzy 20130401 modify for CR00792013 end
                        
                        // gionee xuhz 20120528 add for gn theme start
                        if (ContactsApplication.sIsGnDarkStyle) {
                            entry.setSecondaryAction(
                                    R.drawable.ic_text_holo_dark,
                                    itSecond,
                                    getString(R.string.description_send_text_message, nameOrNumber));
                        } else {
                            entry.setSecondaryAction(
                                    R.drawable.ic_text_holo_light,
                                    itSecond,
                                    getString(R.string.description_send_text_message, nameOrNumber));
                        }
                        // gionee xuhz 20120528 add for gn theme end
                    }
                    
                    // The following lines are provided and maintained by
                    // Mediatek Inc.
                    // For Video Call.
                    
                    if (true == FeatureOption.MTK_VT3G324M_SUPPORT) {
                    	Intent itThird;
                    	if (ContactsApplication.sIsGnContactsSupport) {
                    		itThird = IntentFactory.newDialNumberIntent(numberCallUri);
                    	} else {
                    		itThird = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri);
                    	}
                        itThird.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true)
                               .putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, (long) firstDetails.simId)
                               .setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                        
                        if (isVoicemailUri && slotId != -1) {
                            itThird.putExtra("simId", slotId);
                        }
                        entry.setThirdAction(
                                getString(R.string.menu_videocallNumber, displayNumber), itThird,
                                getString(R.string.description_call, nameOrNumber));
                    } else {
                        entry.thirdIntent = null;
                    }

                    Intent itFourth;
                    if (ContactsApplication.sIsGnContactsSupport) {
                    	itFourth = IntentFactory.newDialNumberIntent(numberCallUri);
                    } else {
                    	itFourth = new Intent(Intent.ACTION_CALL_PRIVILEGED, numberCallUri);
                    }
                    
                    itFourth.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID,(long) firstDetails.simId)
                    .putExtra(Constants.EXTRA_IS_IP_DIAL, true)
                    .putExtra(Constants.EXTRA_ORIGINAL_SIM_ID,(long) firstDetails.simId);
                    
                    itFourth.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    if (isVoicemailUri && slotId != -1) {
                        itFourth.putExtra("simId", slotId);
                    }
                    
                    entry.setFourthAction(getString(R.string.menu_ipcallNumber,
                            displayNumber), itFourth, getString(
                                    R.string.description_call, nameOrNumber));

                    // The previous lines are provided and maintained by
                    // Mediatek Inc.

                    configureCallButton(entry);
                    
                    // gionee xuhz 20121126 modify for GIUI2.0 start
                    if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                        setPhoneNumberAndArea(displayNumber);
                    }
                    // gionee xuhz 20121126 modify for GIUI2.0 end
                } else {
                    disableCallButton();
                }

                mHasEditNumberBeforeCallOption =
                        canPlaceCallsTo && !isSipNumber;
                //mHasTrashOption = hasVoicemail();
                //mHasRemoveFromCallLogOption = !hasVoicemail();
                invalidateOptionsMenu();

                AuroraListView historyList = (AuroraListView) findViewById(R.id.history);
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                historyList.setAdapter(
                        new CallDetailHistoryAdapter(CallDetailActivity.this, mInflater,
                                mCallTypeHelper, details, hasVoicemail(), canPlaceCallsTo,
                                findViewById(R.id.controls)));

                * Descriptions:
                */
                historyList.setAdapter(
                        new CallDetailHistoryAdapter(CallDetailActivity.this, mInflater,
                                mCallTypeHelper, details, false, canPlaceCallsTo,
                                findViewById(R.id.controls)));
                
                if (ContactsApplication.sIsGnContactsSupport) {
                	showPhoneRecords(details);
                }

                /**
                * Change Feature by Mediatek End.
                */

                BackScrollManager.bind(
                        new ScrollableHeader() {
                            private View mControls = findViewById(R.id.controls);
                            private View mPhoto = findViewById(R.id.contact_background_sizer);
                            private View mHeader = findViewById(R.id.photo_text_bar);
                            private View mSeparator = findViewById(R.id.blue_separator);

                            @Override
                            public void setOffset(int offset) {
                                mControls.setY(-offset);
                            }

                            @Override
                            public int getMaximumScrollableHeaderOffset() {
                                // We can scroll the photo out, but we should keep the header if
                                // present.
                                if (mHeader.getVisibility() == View.VISIBLE) {
                                    return mPhoto.getHeight() - mHeader.getHeight();
                                } else {
                                    // If the header is not present, we should also scroll out the
                                    // separator line.
                                    return mPhoto.getHeight() + mSeparator.getHeight();
                                }
                            }
                        },
                        historyList);
                // gionee xuhz 20121126 modify for GIUI2.0 start
                if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                	if (numberType == 1) {
                		firstDetails.photoId = ContactPhotoManager.DEFAULT_UNKOWN_CONTACT_PHOTO;
                	}
                    loadContactPhotosByPhotoId(firstDetails.photoId);
                } else {
                    loadContactPhotos(photoUri);
                }
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
        Cursor callCursor = resolver.query(callUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, null, null, null);
        
        try {
            if (callCursor == null || !callCursor.moveToFirst()) {
                throw new IllegalArgumentException("Cannot find content: " + callUri);
            }
            /**
             * Change Feature by Mediatek Begin.
             * Original Android's Code:
            // Read call log specifics.
            String number = callCursor.getString(NUMBER_COLUMN_INDEX);
            long date = callCursor.getLong(DATE_COLUMN_INDEX);
            long duration = callCursor.getLong(DURATION_COLUMN_INDEX);
            int callType = callCursor.getInt(CALL_TYPE_COLUMN_INDEX);
            String countryIso = callCursor.getString(COUNTRY_ISO_COLUMN_INDEX);
            final String geocode = callCursor.getString(GEOCODED_LOCATION_COLUMN_INDEX);
            // The following lines are provided and maintained by Mediatek Inc.
            int simId = callCursor.getInt(CALL_SIMID_COLUMN_INDEX);
            int vtCall = callCursor.getInt(CALL_VT_COLUMN_INDEX);
            log("number= " + number);
            log("date= " + date);
            log("duration= " + duration);
            log("callType= " + callType);
            log("countryIso= " + countryIso);
            log("geocode= " + geocode);
            log("simId= " + simId);
            log("vtCall= " + vtCall);
            // The previous lines are provided and maintained by Mediatek Inc.
            
            if (TextUtils.isEmpty(countryIso)) {
                countryIso = mDefaultCountryIso;
            }

            // Formatted phone number.
            final CharSequence formattedNumber;
            // Read contact specifics.
            final CharSequence nameText;
            final int numberType;
            final CharSequence numberLabel;
            final Uri photoUri;
            final Uri lookupUri;
            // If this is not a regular number, there is no point in looking it up in the contacts.
            ContactInfo info =
                    mPhoneNumberHelper.canPlaceCallsTo(number)
                    && !mPhoneNumberHelper.isVoiceMailNumberForMtk(number, simId)
                    && !mPhoneNumberHelper.isEmergencyNumber(number)
                            ? mContactInfoHelper.lookupNumber(number, countryIso)
                            : null;
            if (info == null) {
                formattedNumber = mPhoneNumberHelper.getDisplayNumber(number, null);
                nameText = "";
                numberType = 0;
                numberLabel = "";
                photoUri = null;
                lookupUri = null;
            } else {
                formattedNumber = info.formattedNumber;
                nameText = info.name;
                numberType = info.type;
                numberLabel = info.label;
                photoUri = info.photoUri;
                lookupUri = info.lookupUri;
            }
            
            * return new PhoneCallDetails(number, formattedNumber, countryIso, geocode,
                  *       new int[]{ callType }, date, duration,
                  *        nameText, numberType, numberLabel, lookupUri, photoUri);
             * Descriptions:
             */

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
			if (ContactsApplication.sIsGnContactsSupport) {
				isSpecialNumber = !PhoneNumberHelper.canPlaceCallsTo(contactInfo.number);
				// Gionee:tianliang 20120925 modify for CR00692528 start
				//|| mPhoneNumberHelper.isVoiceMailNumberForMtk(contactInfo.number, contactInfo.simId);
				// Gionee:tianliang 20120925 modify for CR00692528 end
			} else {
				isSpecialNumber = !PhoneNumberHelper.canPlaceCallsTo(contactInfo.number)
				|| mPhoneNumberHelper.isVoiceMailNumberForMtk(contactInfo.number, contactInfo.simId)
	            || mPhoneNumberHelper.isEmergencyNumber(contactInfo.number);
			}
			// Gionee:huangzy 20120528 modify for CR00600650 end
			
            if (isSpecialNumber){
            	contactInfo.formattedNumber = mPhoneNumberHelper.getDisplayNumber(contactInfo.number, null).toString();
            	contactInfo.name = "";
            	contactInfo.nNumberTypeId = 0;
            	contactInfo.label = "";
            	photoUri = null;
            	contactInfo.lookupUri = null;
            }
            
            // gionee xuhz 20121126 modify for GIUI2.0 start
            if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
                return new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                        contactInfo.countryIso, contactInfo.geocode,
                        contactInfo.type, contactInfo.date,
                        contactInfo.duration, contactInfo.name,
                        contactInfo.nNumberTypeId, contactInfo.label,
                        contactInfo.lookupUri, contactInfo.photoId, photoUri, contactInfo.simId,
                        contactInfo.vtCall, 0);
            } else {
                return new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
                        contactInfo.countryIso, contactInfo.geocode,
                        contactInfo.type, contactInfo.date,
                        contactInfo.duration, contactInfo.name,
                        contactInfo.nNumberTypeId, contactInfo.label,
                        contactInfo.lookupUri, photoUri, contactInfo.simId,
                        contactInfo.vtCall, 0);
            }
            // gionee xuhz 20121126 modify for GIUI2.0 end
			/**
			 * Change Feature by Mediatek End.
			 */
        } finally {
            if (callCursor != null) {
                callCursor.close();
            }
        }
    }

    /** Load the contact photos and places them in the corresponding views. */
    private void loadContactPhotos(Uri photoUri) {
        // gionee xuhz 20120518 modify for CR00601639 start
        if (ContactsApplication.sIsGnContactsSupport) {

            // Place photo when discovered in data, otherwise show generic avatar
            if (photoUri != null) {
                mContactPhotoManager.loadPhoto(mContactBackgroundView, photoUri, true, false);
            } else {
                mContactBackgroundView.setImageResource(ContactPhotoManager.getDefaultAvatarResId(true, false));
            }
        } else {
            mContactPhotoManager.loadPhoto(mContactBackgroundView, photoUri, true, true);
        }
        // gionee xuhz 20120518 modify for CR00601639 start
    }
    
    // gionee xuhz 20121126 add for GIUI2.0 start
    private void loadContactPhotosByPhotoId(long photoId) {
        // Place photo when discovered in data, otherwise show generic avatar
        if (photoId != 0) {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
        	if (ContactsApplication.sIsGnDarkStyle) {
                mContactPhotoManager.loadPhoto(mContactBackgroundView, photoId, false, true);
        	} else {
                mContactPhotoManager.loadPhoto(mContactBackgroundView, photoId, false, false);
        	}
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        } else {
        	// gionee xuhz 20121208 modify for GIUI2.0 start
        	if (ContactsApplication.sIsGnDarkStyle) {
                mContactBackgroundView.setImageResource(ContactPhotoManager.getDefaultAvatarResId(false, true));
        	} else {
                mContactBackgroundView.setImageResource(ContactPhotoManager.getDefaultAvatarResId(false, false));
        	}
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        }
    }
    
    private void setPhoneNumberAndArea(CharSequence displayNumber) {
        mPhoneNumberText.setText(displayNumber);
        
        String area = NumberAreaUtil.getInstance(this).getNumAreaFromAora(this, mNumber, true);
        if (TextUtils.isEmpty(area)) {
        	mNumberAreaText.setVisibility(View.GONE);
        } else {
        	mNumberAreaText.setText(area);
        	mNumberAreaText.setVisibility(View.VISIBLE);
        }
    }
    // gionee xuhz 20121126 add for GIUI2.0 end

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
        public int thirdIcon = 0;
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

		public void setThirdAction(String text, Intent intent,
				String description) {
			videoText = text;
			thirdIntent = intent;
			thirdDescription = description;
		}

		/** The description for accessibility of the fourth action. */

		public void setFourthAction(String text, Intent intent,
				String description) {
			ipText = text;
			fourthIntent = intent;
			fourthDescription = description;
		}

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
        // gionee xuhz 20120507 add for CR00589238 start
        if (ContactsApplication.sIsGnContactsSupport) {
            gnDisableCallButton();
            return;
        }
        // gionee xuhz 20120507 add for CR00589238 end
        
        findViewById(R.id.call_and_sms).setVisibility(View.GONE);
        findViewById(R.id.separator01).setVisibility(View.GONE);
        findViewById(R.id.separator02).setVisibility(View.GONE);
        findViewById(R.id.video_call).setVisibility(View.GONE);
        findViewById(R.id.ip_call).setVisibility(View.GONE);
    }

    /** Configures the call button area using the given entry. */
    private void configureCallButton(ViewEntry entry) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnConfigureCallButton(entry);
    		return;
    	}
        View convertView = findViewById(R.id.call_and_sms);
        convertView.setVisibility(View.VISIBLE);

        ImageView icon = (ImageView) convertView.findViewById(R.id.call_and_sms_icon);
        View divider = convertView.findViewById(R.id.call_and_sms_divider);
        TextView text = (TextView) convertView.findViewById(R.id.call_and_sms_text);

        View mainAction = convertView.findViewById(R.id.call_and_sms_main_action);
        mainAction.setOnClickListener(mPrimaryActionListener);
        mainAction.setTag(entry);
        mainAction.setContentDescription(entry.primaryDescription);

        if (entry.secondaryIntent != null) {
            icon.setOnClickListener(mSecondaryActionListener);
            icon.setImageResource(entry.secondaryIcon);
            icon.setVisibility(View.VISIBLE);
            icon.setTag(entry);
            icon.setContentDescription(entry.secondaryDescription);
            divider.setVisibility(View.VISIBLE);
        } else {
            icon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        
        
        text.setText(entry.text);

        TextView label = (TextView) convertView.findViewById(R.id.call_and_sms_label);
        if (TextUtils.isEmpty(entry.label)) {
            label.setVisibility(View.GONE);
        } else {
            label.setText(entry.label);
            label.setVisibility(View.VISIBLE);
        }
        // The following lines are provided and maintained by Mediatek Inc.
        //For video call 
        
        View separator01 = findViewById(R.id.separator01);
        separator01.setVisibility(View.VISIBLE);
        View separator02 = findViewById(R.id.separator02);
        separator02.setVisibility(View.VISIBLE);
        
        View convertView1 = findViewById(R.id.video_call);
        View videoAction = convertView1.findViewById(R.id.video_call_action);
        if (entry.thirdIntent != null) {
            videoAction.setOnClickListener(mThirdActionListener);
            videoAction.setTag(entry);
            videoAction.setContentDescription(entry.thirdDescription);
            videoAction.setVisibility(View.VISIBLE);
            TextView videoText = (TextView) convertView1.findViewById(R.id.video_call_text);

            videoText.setText(entry.videoText);

            TextView videoLabel = (TextView) convertView1.findViewById(R.id.video_call_label);
            if (TextUtils.isEmpty(entry.label)) {
                videoLabel.setVisibility(View.GONE);
            } else {
                videoLabel.setText(entry.label);
                videoLabel.setVisibility(View.VISIBLE);
            }
        } else {
            separator01.setVisibility(View.GONE);
            convertView1.setVisibility(View.GONE);
        }
        
        //For IP call
        View convertView2 = findViewById(R.id.ip_call);
        View ipAction = convertView2.findViewById(R.id.ip_call_action);
        ipAction.setOnClickListener(mFourthActionListener);
        ipAction.setTag(entry);
        ipAction.setContentDescription(entry.fourthDescription);
        TextView ipText = (TextView) convertView2.findViewById(R.id.ip_call_text);

        ipText.setText(entry.ipText);

        TextView ipLabel = (TextView) convertView2.findViewById(R.id.ip_call_label);
        if (TextUtils.isEmpty(entry.label)) {
        	ipLabel.setVisibility(View.GONE);
        } else {
        	ipLabel.setText(entry.label);
        	ipLabel.setVisibility(View.VISIBLE);
        }
        
        // The previous lines are provided and maintained by Mediatek Inc.
    }
    
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
 /**
  * 
    protected void updateVoicemailStatusMessage(Cursor statusCursor) {
        if (statusCursor == null) {
            mStatusMessageView.setVisibility(View.GONE);
            return;
        }
        final StatusMessage message = getStatusMessage(statusCursor);
        if (message == null || !message.showInCallDetails()) {
            mStatusMessageView.setVisibility(View.GONE);
            return;
        }

        mStatusMessageView.setVisibility(View.VISIBLE);
        mStatusMessageText.setText(message.callDetailsMessageId);
        if (message.actionMessageId != -1) {
            mStatusMessageAction.setText(message.actionMessageId);
        }
        if (message.actionUri != null) {
            mStatusMessageAction.setClickable(true);
            mStatusMessageAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, message.actionUri));
                }
            });
        } else {
            mStatusMessageAction.setClickable(false);
        }
    }

    private StatusMessage getStatusMessage(Cursor statusCursor) {
        List<StatusMessage> messages = mVoicemailStatusHelper.getStatusMessages(statusCursor);
        if (messages.size() == 0) {
            return null;
        }
        // There can only be a single status message per source package, so num of messages can
        // at most be 1.
        if (messages.size() > 1) {
            Log.w(TAG, String.format("Expected 1, found (%d) num of status messages." +
                    " Will use the first one.", messages.size()));
        }
        return messages.get(0);
    }
 */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.call_details_options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
        // This action deletes all elements in the group from the call log.
        // We don't have this action for voicemails, because you can just use the trash button.
        menu.findItem(R.id.menu_remove_from_call_log).setVisible(mHasRemoveFromCallLogOption);
        menu.findItem(R.id.menu_edit_number_before_call).setVisible(mHasEditNumberBeforeCallOption);
        menu.findItem(R.id.menu_trash).setVisible(mHasTrashOption);
        * Descriptions:
        */
    	
        menu.findItem(R.id.menu_remove_from_call_log).setVisible(true);
        menu.findItem(R.id.menu_edit_number_before_call).setVisible(mHasEditNumberBeforeCallOption);
        
        final MenuItem add2BlacklistMenu = menu.findItem(R.id.gn_menu_add_blacklist);
        add2BlacklistMenu.setVisible(false);
        /**
        * Change Feature by Mediatek End.
        */
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onHomeSelected();
                return true;
            }
            // All the options menu items are handled by onMenu... methods.
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case R.id.menu_remove_from_call_log: {
	        	onMenuRemoveFromCallLog(item);
	            break;
	        }
	        case R.id.menu_edit_number_before_call: {
	        	onMenuEditNumberBeforeCall(item);
	            break;
	        }
	        case R.id.gn_menu_add_blacklist: {
	            break;
	        }
	    }
	    super.onOptionsItemSelected(item);
	    return true;
    }
    
    public void onMenuRemoveFromCallLog(MenuItem menuItem) {
    	new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearSingleCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		.setMessage(R.string.gn_clearSingleCallLogConfirmation)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final StringBuilder callIds = new StringBuilder();
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 begin
				Uri[] uris = getCallLogEntryUris();
				if (uris != null) {
			        for (Uri callUri : uris) {
			            if (callIds.length() != 0) {
			                callIds.append(",");
			            }
			            callIds.append(ContentUris.parseId(callUri));
			        }
				}
		        //Gionee <xuhz> <2013-08-01> modify for CR00844946 end

		        mAsyncTaskExecutor.submit(Tasks.REMOVE_FROM_CALL_LOG_AND_FINISH,
		                new AsyncTask<Void, Void, Void>() {
		                    @Override
		                    public Void doInBackground(Void... params) {
		                        getContentResolver().delete(Calls.CONTENT_URI_WITH_VOICEMAIL,
		                                Calls._ID + " IN (" + callIds + ")", null);
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

    public void onMenuEditNumberBeforeCall(MenuItem menuItem) {
        startActivity(new Intent(Intent.ACTION_DIAL, mPhoneNumberHelper.getCallUri(mNumber)));
    }
    // The following lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.
  /**
   * 
    public void onMenuTrashVoicemail(MenuItem menuItem) {
        final Uri voicemailUri = getVoicemailUri();
        mAsyncTaskExecutor.submit(Tasks.DELETE_VOICEMAIL_AND_FINISH,
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    public Void doInBackground(Void... params) {
                        getContentResolver().delete(voicemailUri, null, null);
                        return null;
                    }
                    @Override
                    public void onPostExecute(Void result) {
                        finish();
                    }
                });
    }
  */
    // The previous lines are deleted by Mediatek Inc to close Google default
    // Voicemail function.

    private void configureActionBar() {
        ActionBar actionBar = getActionBar();
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE);
        	actionBar.setTitle("");
        	return;
        }
        
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME);
        }
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
        
      //The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            setSimIndicatorVisibility(false);        
			// gionee tianliang 20120925 modify for CR00692598 start
            mShowSimIndicator = false;
			// gionee tianliang 20120925 modify for CR00692598 end
        }
        //The previous lines are provided and maintained by Mediatek Inc.
        
        // Gionee:xuhz 20130328 add for CR00790874 start
        if (ContactsApplication.sIsHandSensorDial) {
        	mSensorMgr.unregisterListener(mGnHandSensorEventListener);
        }
        // Gionee:xuhz 20130328 add for CR00790874 end
        
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
        super.onDestroy();
        SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
        if(FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
		// gionee tianliang 20120925 modify for CR00692598 start
            unregisterReceiver(mReceiver);
		// gionee tianliang 20120925 modify for CR00692598 end
        }
    }

    // The following lines are provided and maintained by Mediatek Inc.
    private TextView mSimName;
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

    private static final int SIM_INFO_UPDATE_MESSAGE = 100;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SIM_INFO_UPDATE_MESSAGE:
                	// gionee xuhz 20120912 modify for CR00692279 start
    				Uri[] callEntryUris = getCallLogEntryUris();
    				if (callEntryUris != null) {
        				if (callEntryUris.length > 1) {
        					updateData(callEntryUris[0]);
        					updateHistoryList(callEntryUris);
        				} else {
        					updateData(callEntryUris);
        				}
    				}
    				// gionee xuhz 20120912 modify for CR00692279 end
                    break;
                default:
                    break;
            }
        }
    };
    public StatusBarManager mStatusBarMgr;
    
    void setSimIndicatorVisibility(boolean visible) {
        if(visible)
            GnStatusBarManager.showSIMIndicator(mStatusBarMgr, getComponentName(), ContactsFeatureConstants.VOICE_CALL_SIM_SETTING);
        else
            GnStatusBarManager.hideSIMIndicator(mStatusBarMgr, getComponentName());
    }
    
    private void log(final String msg) {
        if (false) {
            Log.d(TAG, msg);
        }
    }
    
    public static final String EXTRA_CALL_LOG_NAME = "EXTRA_CALL_LOG_NAME";
    public static final String EXTRA_CALL_LOG_NUMBER_TYPE = "EXTRA_CALL_LOG_NUMBER_TYPE";
    public static final String EXTRA_CALL_LOG_PHOTO_URI = "EXTRA_CALL_LOG_PHOTO_URI";
    public static final String EXTRA_CALL_LOG_LOOKUP_URI = "EXTRA_CALL_LOG_LOOKUP_URI";
    // The previous lines are provided and maintained by Mediatek Inc.
    
    
    private String mCallNumber;
    private int mCallsCount;
    private int mCallId;
    private int mCallType;
    
    // gionee xuhz 20121126 add for GIUI2.0 start
    private TextView mPhoneNumberText;
    private TextView mNumberAreaText;
    // gionee xuhz 20121126 add for GIUI2.0 end
    
    protected void gnOnCreate(Bundle icicle) {
        log("CallDetailActivity  gnOnCreat()");
        /**
        * Change Feature by Mediatek Begin.
        * Original Android's Code:
          setContentView(R.layout.call_detail);
        * Descriptions:
        */
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            setContentView(R.layout.gn_call_detail_without_voicemail_v2);            
            mPhoneNumberText = (TextView) findViewById(R.id.phone_number);
            mNumberAreaText = (TextView) findViewById(R.id.number_area);
        } else {
            setContentView(R.layout.gn_call_detail_without_voicemail);
        }

        /**
        * Change Feature by Mediatek End.
        */

        mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();

        mCallTypeHelper = new CallTypeHelper(getResources());
        mPhoneNumberHelper = new PhoneNumberHelper(mResources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
                                                             mPhoneNumberHelper, null, this);
        mAsyncQueryHandler = null;//new CallDetailActivityQueryHandler(this);
        mHeaderTextView = (TextView) findViewById(R.id.header_text);
        mHeaderOverlayView = findViewById(R.id.photo_text_bar);
        
        mMainActionView = (ImageView) findViewById(R.id.main_action);
        mMainActionPushLayerView = (ImageButton) findViewById(R.id.main_action_push_layer);
        mMainActionArrowRightView = findViewById(R.id.iv_arrow_right); //4.9.0
        mContactBackgroundView = (ImageView) findViewById(R.id.contact_background);
        mContactBackgroundView.setEnabled(false);
        mContactBackgroundView.setClickable(false);
        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(this);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);
        mProximitySensorManager = new ProximitySensorManager(this, mProximitySensorListener);
        mContactInfoHelper = new ContactInfoHelper(this, ContactsUtils.getCurrentCountryIso(this));
        // The following lines are provided and maintained by Mediatek Inc.
        mSimName = new TextView(this);// just a stub, uesless
        SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // The previous lines are provided and maintained by Mediatek Inc.
        configureActionBar();
        //optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.

		// gionee tianliang 20120925 modify for CR00692598 start
        if(FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }
		// gionee tianliang 20120925 modify for CR00692598 end

		
        Intent intent = getIntent();        
        mCallNumber = intent.getStringExtra(Calls.NUMBER);
        mCallsCount = intent.getIntExtra(Calls._COUNT, 0);
        mCallId = intent.getIntExtra(Calls._ID, 0);
        mCallType = intent.getIntExtra(Calls.TYPE, 0);
        
        // Gionee:xuhz 20130328 add for CR00790874 start
        if (ContactsApplication.sIsHandSensorDial) {
        	mSensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
            mGnHandSensor = mSensorMgr.getDefaultSensor(TYPE_HAND_ANSWER);
        }
        // Gionee:xuhz 20130328 add for CR00790874 end
    }
    
    private void gnConfigureCallButton(ViewEntry entry) {
        View convertView = findViewById(R.id.call_and_sms);
        convertView.setVisibility(View.VISIBLE);

        ImageView icon = (ImageView) convertView.findViewById(R.id.call_and_sms_sms_icon);
        View divider = convertView.findViewById(R.id.call_and_sms_divider);
        TextView text = (TextView) convertView.findViewById(R.id.call_and_sms_text);

        // gionee xuhz 20120530 add for gn theme start
        ImageView gnPhoneNumIcon = (ImageView) convertView.findViewById(R.id.gn_phone_num_icon);
        if (gnPhoneNumIcon != null) {
        	gnPhoneNumIcon.setImageResource(ResConstant.getIconRes(ResConstant.IconTpye.Call));
        }
        // gionee xuhz 20120530 add for gn theme end

        View mainAction = convertView.findViewById(R.id.call_and_sms_main_action);
        mainAction.setOnClickListener(mPrimaryActionListener);
        mainAction.setTag(entry);
        mainAction.setContentDescription(entry.primaryDescription);
        
        if (entry.secondaryIntent != null) {
        	icon.setOnClickListener(mSecondaryActionListener);
            icon.setImageResource(entry.secondaryIcon);
            icon.setVisibility(View.VISIBLE);
            icon.setTag(entry);
            icon.setContentDescription(entry.secondaryDescription);
            divider.setVisibility(View.VISIBLE);
        } else {
        	icon.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        }
        // gionee xuhz 20121126 modify for GIUI2.0 start
         if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
            text.setText(R.string.gn_call_this_number);
        } else {
            text.setText(entry.text);
        }
        // gionee xuhz 20121126 modify for GIUI2.0 end

        TextView label = (TextView) convertView.findViewById(R.id.call_and_sms_label);
        // gionee xuhz 20121126 modify for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        	label.setVisibility(View.GONE);
        } else {
            if (TextUtils.isEmpty(entry.label)) {
                label.setVisibility(View.GONE);
            } else {
                label.setText(entry.label);
                label.setVisibility(View.VISIBLE);
            }
        }
        // gionee xuhz 20121126 modify for GIUI2.0 end
        
        //Gionee:huangzy 20120904 add for CR00682212 start
        ImageView vedioCallIcon = (ImageView) convertView.findViewById(R.id.call_and_sms_vedio_call_icon);
    	if (!FeatureOption.MTK_VT3G324M_SUPPORT) {
    		vedioCallIcon.setVisibility(View.GONE);
    		divider.setVisibility(View.GONE);
    	} else {
            // gionee xuhz 20120528 add for gn theme start
    		vedioCallIcon.setImageResource(ResConstant.getIconRes(ResConstant.IconTpye.VedioCall));
            // gionee xuhz 20120528 add for gn theme end
            {
                vedioCallIcon.setOnClickListener(new View.OnClickListener() {			
        			@Override
        			public void onClick(View v) {
        			    // Gionee:wangth 20130304 modify for CR00771431 begin
        			    /*
        			    startActivity(IntentFactory.newDialNumberIntent(mNumber, IntentFactory.DIAL_NUMBER_INTENT_VIDEO));
        			    */
        			    if (GNContactsUtils.isOnlyQcContactsSupport()) {
        			        Intent intent = GNContactsUtils.startQcVideoCallIntent(mNumber);
        			        try {
        			            startActivity(intent);
        			        } catch (ActivityNotFoundException a) {
        			            a.printStackTrace();
        			        }
        			    } else {
        			        startActivity(IntentFactory.newDialNumberIntent(mNumber, IntentFactory.DIAL_NUMBER_INTENT_VIDEO));
        			    }
        			    // Gionee:wangth 20130304 modify for CR00771431 end
        			}
                });
            }
        }
        //Gionee:huangzy 20120904 add for CR00682212 end
        
        TextView areaTv = (TextView) convertView.findViewById(R.id.call_and_sms_area);
        // gionee xuhz 20121126 modify for GIUI2.0 start
        if (ContactsApplication.sIsGnGGKJ_V2_0Support) {
        	areaTv.setVisibility(View.GONE);
        } else {
            String area = NumberAreaUtil.getInstance(this).getNumAreaFromAora(this, mNumber, true);
            if (TextUtils.isEmpty(area)) {
            	areaTv.setVisibility(View.GONE);
            } else {
            	if (null != label && label.getVisibility() == View.VISIBLE) {
            		area = "-" + area;
            	}
            	areaTv.setText(area);
            	areaTv.setVisibility(View.VISIBLE);
            }        
        }
        // gionee xuhz 20121126 modify for GIUI2.0 end
    }
    
    private boolean mIsFristResume = true;
    private Uri[] gnGetCallLogEntryUris() {
        log("CallDetailActivity gnGetCallLogEntryUris()");
        
        Uri queryUri = Uri.parse("content://call_log/callsjoindataview");

        if (0 == mCallsCount) {
            // gionee xuhz 20120530 add for CR00611487 start
            Uri uri = getIntent().getData();
            if (uri != null) {
                long id = ContentUris.parseId(uri);
                uri = ContentUris.withAppendedId(queryUri, id);
                return new Uri[]{ uri };
            } 
            // gionee xuhz 20120530 add for CR00611487 end
            
        	return null;
        }
        
        if (1 == mCallsCount && mCallId != 0 && mIsFristResume) {
        	mIsFristResume = false;        
        	Uri uri = ContentUris.withAppendedId(queryUri, mCallId);
            return new Uri[]{ uri };
        } 
 //aurora changes zhouxiaobing 20130925 start               
       // long[] ids = getCallIdsByNumber(mCallNumber, mCallType);
        int[] ids=getIntent().getIntArrayExtra("ids");

//aurora changes zhouxiaobing 20130925 end          
        //Gionee:huangzy 20120705 add for CR00632668 start
        if (null == ids) {
            return null;
        }
        //Gionee:huangzy 20120705 add for CR00632668 end
        
        mCallsCount = ids.length;
        Uri[] uris = new Uri[ids.length];
        for (int index = 0; index < ids.length; ++index) {
        	uris[index] = ContentUris.withAppendedId(queryUri, ids[index]);
        }
        return uris;
    }
    
    private long[] getCallIdsByNumber(String number, int callType) {
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
    	
    	if (callType > 0) {
    	    // Gionee:wangth 20130409 modify for CR00793864 begin
    	    /*
    		selection += (" AND " + Calls.TYPE + "=" + callType);
    		*/
    		if (GNContactsUtils.isOnlyQcContactsSupport()) {
    		    if (1 == callType) {
    		        selection += (" AND (" + Calls.TYPE + "=" + callType
    		                + " OR "  + Calls.TYPE + "=" + GNContactsUtils.INCOMMING_VIDEO_TYPE + ")");
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
    	long[] ids = null;
    	if (count > 0) {
    		c.moveToFirst();
    		ids = new long[count];
    		for (int i = 0; i < count; ++i) {
    			ids[i] = c.getInt(0);
    			c.moveToNext();
    		}    		    	
    	}
    	
    	c.close();
    	return ids;
    }
    
    private boolean gnFoundAndSetPhoneRecords(PhoneCallDetails[] phoneCallDetails) {
    	if (null == phoneCallDetails) {
    		return false;
    	}
    	
    	long earliestCallTime = Long.MAX_VALUE;
    	long lastEndCallTime = Long.MIN_VALUE;
    	{
	    	long end;
	    	for (PhoneCallDetails detail : phoneCallDetails) {
	    		if (detail.date < earliestCallTime) {
	    			earliestCallTime = detail.date; 
	    		}
	    		//Gionee:huangzy 20121017 modify for CR00710756, CR00710636 start
	    		/*end = detail.date + detail.duration*1000;*/
	    		end = detail.date + detail.duration*1000 + 999;
	    		//Gionee:huangzy 20121017 modify for CR00710756, CR00710636 end
	    		if (end > lastEndCallTime) {
	    			lastEndCallTime = end; 
	    		}
	    	}
    	}
    	
    	// Gionee:wangth 20130320 modify for CR00782676 begin
    	/*
    	final String recordEndTime = "drm_dataLen";
    	String selection = Media.IS_MUSIC + "=2 AND " +
    		recordEndTime + "<=" + lastEndCallTime + " AND " +
    		recordEndTime + ">=" + earliestCallTime;
    	*/
    	String recordEndTime = "drm_dataLen";
    	String selection = Media.IS_MUSIC + "=2 AND " + 
    	        recordEndTime + "<=" + lastEndCallTime + " AND " + 
    	        recordEndTime + ">=" + earliestCallTime;
    	
    	if (GNContactsUtils.isOnlyQcContactsSupport()) {
    	    recordEndTime = Media.IS_MUSIC;
    	    selection = Media.IS_MUSIC + ">10000 AND " + 
                    recordEndTime + "<=" + lastEndCallTime + " AND " + 
                    recordEndTime + ">=" + earliestCallTime;
    	}
    	// Gionee:wangth 20130320 modify for CR00782676 end
    	
    	Cursor c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
    			null, selection, null, null);
    	
    	if (null == c) {
    		return false;
		}

    	int found = 0;
    	if (c.moveToFirst()) {    		
    		final int count = c.getCount();
        	PhoneCallRecord[] records = new PhoneCallRecord[count];
        	{
        		int _dataIndex = c.getColumnIndex(Media.DATA);
        		int recordEndTimeIndex = c.getColumnIndex(recordEndTime);
        		int druationIndex = c.getColumnIndex(Media.DURATION);
        		int mimeTypeIndex = c.getColumnIndex(Media.MIME_TYPE);
        		for (int i = 0; i < count; i++) {
        			records[i] = new PhoneCallRecord();
        			records[i].setPath(c.getString(_dataIndex));
        			records[i].setEndTime(c.getLong(recordEndTimeIndex));
        			records[i].setDruation(c.getLong(druationIndex));
        			records[i].setMimeType(c.getString(mimeTypeIndex));
        			
        			if(!c.moveToNext()) {
        				break;
        			}
        		}
        	}
        	        	
    		for (PhoneCallDetails detail : phoneCallDetails) {
    			for (int i = 0; i < count && found < count; ++i) {
    				if (null != records[i]&& detail.betweenCall(records[i].getEndTime())) {
    					detail.addPhoneRecords(records[i]);
    					records[i] = null;
    					++found;    					
    				}
    			}			
    		}
		}
    	
    	c.close();
		
		return found > 0;
    }

    private void showPhoneRecords(final PhoneCallDetails[] phoneCallDetails) {
    	new SimpleAsynTask() {
			@Override
			protected Integer doInBackground(Integer... params) {
				boolean founded = gnFoundAndSetPhoneRecords(phoneCallDetails);
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
    
    // gionee xuhz 20120507 add for CR00589238 start
    private void gnDisableCallButton() {
        findViewById(R.id.call_and_sms).setVisibility(View.GONE);
    }
    // gionee xuhz 20120507 add for CR00589238 end
    
    //Gionee:huangzy 20120612 modify for CR00614326 start
    //Gionee:huangzy 20120528 add for CR00611149 start
    protected void gnSwitchUi(int numberType, PhoneCallDetails firstDetails) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    	    switch (numberType) {
            case 0://Saved Number
                getActionBar().setTitle(firstDetails.name);
                mHeaderTextView.setVisibility(View.INVISIBLE);                      
                mHeaderOverlayView.setVisibility(View.VISIBLE);
                mMainActionView.setVisibility(View.VISIBLE);
                mMainActionPushLayerView.setVisibility(View.VISIBLE);
                //4.9.0 begin
                if (null != mMainActionArrowRightView) {
                	mMainActionArrowRightView.setVisibility(View.VISIBLE);	
                }
                //4.9.0 end
                findViewById(R.id.gn_strange_num_handler).setVisibility(View.GONE);
                break;
            case 1://not saved Number
                getActionBar().setTitle(getString(R.string.gn_unknow_contact));
                mHeaderTextView.setVisibility(View.INVISIBLE);
                mMainActionView.setVisibility(View.INVISIBLE);
                mMainActionPushLayerView.setVisibility(View.GONE);
                //4.9.0 begin
                if (null != mMainActionArrowRightView) {
                	mMainActionArrowRightView.setVisibility(View.GONE);	
                }
                //4.9.0 end
                findViewById(R.id.gn_strange_num_handler).setVisibility(View.VISIBLE);
                findViewById(R.id.gn_strange_num_add_new_contact).setOnClickListener(this);
                findViewById(R.id.gn_strange_num_add_to_contact).setOnClickListener(this);
                break;
            case 2://Number Unshow                
                getActionBar().setTitle(R.string.gn_hidden_number);
                mHeaderTextView.setVisibility(View.INVISIBLE);                    
                mHeaderOverlayView.setVisibility(View.VISIBLE);
                mMainActionView.setVisibility(View.VISIBLE);
                mMainActionPushLayerView.setVisibility(View.VISIBLE);
                //4.9.0 begin
                if (null != mMainActionArrowRightView) {
                	mMainActionArrowRightView.setVisibility(View.VISIBLE);	
                }
                //4.9.0 end
                findViewById(R.id.gn_strange_num_handler).setVisibility(View.GONE);
                break;
            default:
                break;
            }
        }    	
	}
    //Gionee:huangzy 20120612 modify for CR00614326 end

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.gn_strange_num_add_new_contact:
			startActivity(IntentFactory.newCreateContactIntent(mNumber));
			break;
		case R.id.gn_strange_num_add_to_contact:
			startActivity(IntentFactory.newInsert2ExistContactIntent(mNumber));
			break;

		default:
			break;
		}
		
	}
	//Gionee:huangzy 20120528 add for CR00611149 end
	
    private String mName = null;
	
	// gionee xuhz 20120912 add for CR00692279 start
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
                    Toast.makeText(CallDetailActivity.this, R.string.toast_call_detail_error,
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
                /**
                * Change Feature by Mediatek Begin.
                * Original Android's Code:
                historyList.setAdapter(
                        new CallDetailHistoryAdapter(CallDetailActivity.this, mInflater,
                                mCallTypeHelper, details, hasVoicemail(), canPlaceCallsTo,
                                findViewById(R.id.controls)));

                * Descriptions:
                */
                historyList.setAdapter(
                        new CallDetailHistoryAdapter(CallDetailActivity.this, mInflater,
                                mCallTypeHelper, details, false, canPlaceCallsTo,
                                findViewById(R.id.controls)));
                
                if (ContactsApplication.sIsGnContactsSupport) {
                	showPhoneRecords(details);
                }
            }
        }
        mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateHistoryListTask());
    }
    // gionee xuhz 20120912 modify for CR00692279 end



    // gionee tianliang 20120925 modify for CR00692598 start
    private BroadcastReceiver mReceiver = new DialtactsBroadcastReceiver();
    private boolean mShowSimIndicator = false;
    private class DialtactsBroadcastReceiver extends BroadcastReceiver {
    @Override
   		public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        log("DialtactsBroadcastReceiver, onReceive action = " + action);
        if(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
            if(FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
                if (mShowSimIndicator) {
                    setSimIndicatorVisibility(true);
                }
            }
        }
        }
    }
    // gionee tianliang 20120925 modify for CR00692598 end
    
    // Gionee:xuhz 20130328 add for CR00790874 start
    private Sensor mGnHandSensor;
    private SensorManager mSensorMgr;
    private final static int TYPE_HAND_ANSWER = 14; 
    
    private final SensorEventListener mGnHandSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
        	log("event.sensor.getType() == Sensor.TYPE_HAND_ANSWER");
        	Log.v("Test", "mNumber = " + mNumber);
        	// Gionee <fenglp> <2013-08-21> add for CR00868131 begin
//        	int ssgAutoDial = Settings.System.getInt(getContentResolver(), "ssg_auto_dial", 0);
        	int ssgAutoDial = AuroraSettings.getInt(getContentResolver(),AuroraSettings.SSG_AUTO_DIAL,0); 
        	// Gionee <fenglp> <2013-08-21> add for CR00868131 end
        	
            if (ssgAutoDial == 1) {
                if (event.values[0] == 1) {
                	if (!PhoneNumberHelper.canPlaceCallsTo(mNumber)) {
                		return;
                	}
                	Intent callIntent = IntentFactory.newDialNumberIntent(mNumber);
                	callIntent.putExtra("sensoryDial", true);
                    callIntent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
                    startActivity(callIntent);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    // Gionee:xuhz 20130328 add for CR00790874 end
}

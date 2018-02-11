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

import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
import com.android.contacts.activities.AuroraDialActivity;
import com.android.contacts.activities.AuroraMarkActivity;
import com.android.contacts.activities.SystemUtils;
import com.android.contacts.calllog.AuroraCallDetailHistoryAdapter;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
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
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.YuloreUtils;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.android.contacts.R;

import android.R.integer;
import android.provider.Settings;
import android.content.ActivityNotFoundException;
import gionee.provider.GnContactsContract.RawContacts;
import gionee.os.storage.GnStorageManager;
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
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.util.Constants;

import gionee.app.GnStatusBarManager;

// Gionee:wangth 20120710 add for CR00633799 begin
import gionee.provider.GnContactsContract.Data;
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
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;

import com.privacymanage.service.AuroraPrivacyUtils;

/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class FullCallDetailActivity extends AuroraActivity implements OnItemLongClickListener, OnItemClickListener {
	private static final String TAG = "liyang-FullCallDetailActivity";

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

	private CallTypeHelper mCallTypeHelper;
	private PhoneNumberHelper mPhoneNumberHelper;
	private PhoneCallDetailsHelper mPhoneCallDetailsHelper;
	private AsyncTaskExecutor mAsyncTaskExecutor;


	private String mNumber = null;
	private String mName = null;
	private String mNote = null;
	private Uri mContactUri;


	private AuroraActionBar mActionBar;
	private Activity mActivity;
	private int mNumberType = 0;

	/* package */ LayoutInflater mInflater;
	/* package */ Resources mResources;
	/** Whether we should show "edit number before call" in the options menu. */
	private boolean mHasEditNumberBeforeCallOption;
	/** Whether we should show "trash" in the options menu. */
	//private boolean mHasTrashOption;
	/** Whether we should show "remove from call log" in the options menu. */
	//private boolean mHasRemoveFromCallLogOption;

	private Uri[] mCallEntryUris;
	private ProximitySensorManager mProximitySensorManager;
	private boolean editMode = false; // 编辑模式
	private boolean isSelectAll = false;
	private static boolean mIsRejectedDetail = false;// aurora wangth 20140618 add for reject detail
	private boolean mIsShowRejectFlag = false;
	private String mNameOrig = null;

	private String mNumberArea = null;
	private boolean mNeedReQuery = false;    

	private boolean mIsContactDetial = false;
	private long mRawContactId;

	private AuroraListView historyList;
	private TextView leftView;
	private TextView rightView;
	private AuroraCallDetailHistoryAdapter mAdapter;
	public AuroraListView getAuroraListView() {
		return historyList;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		ContactsApplication.sendSimContactBroad();
		log("CallDetailActivity  onCreat()");
		super.onCreate(icicle);

		Intent intent = getIntent();  
	      mIsPrivate = intent.getBooleanExtra("is_privacy_contact", false);
	      if(mIsPrivate) {
	          ContactsApplication.mPrivacyActivityList.add(this);            
	      }

		setAuroraContentView(R.layout.full_call_detail_list,
				AuroraActionBar.Type.Normal);

		mActivity = FullCallDetailActivity.this;

		mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
		mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		mResources = getResources();

		mCallTypeHelper = new CallTypeHelper(getResources());
		mPhoneNumberHelper = new PhoneNumberHelper(mResources);
		mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
				mPhoneNumberHelper, null, this);



		// The following lines are provided and maintained by Mediatek Inc.
		SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
		// The previous lines are provided and maintained by Mediatek Inc.
		//optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.
	
		ids = intent.getIntArrayExtra("ids");
		mCallNumber = intent.getStringExtra(Calls.NUMBER);
		mCallsCount = intent.getIntExtra(Calls._COUNT, 0);
		mCallId = intent.getIntExtra(Calls._ID, 0);

		mIsRejectedDetail = intent.getBooleanExtra("reject_detail", false);

		initActionBar();

		mIsContactDetial =  intent.getBooleanExtra("contact_detail", false);
		mRawContactId = intent.getLongExtra("rawcontactid", -1);
		if (mRawContactId > 0) {
			mIsContactDetial =  true;        	
			ids = getCallIdsByContactUri(mRawContactId);       	

		} else {
			ids = getCallIdsByNumber(mCallNumber,0);
			mIsContactDetial =  false;
		}

		historyList = (AuroraListView) findViewById(R.id.history);
		historyList.setOnItemLongClickListener(this);
		historyList.auroraSetNeedSlideDelete(false);
		historyList.setOnItemClickListener(this);
		
	}

	@Override
	public void onStart() {
		super.onStart();        

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mCallEntryUris = getCallLogEntryUris();
				if (mCallEntryUris != null) {

					//    				if (mCallEntryUris.length > 1) {
					//    					updateData(mCallEntryUris[0]);
					//    					updateHistoryList(mCallEntryUris);
					//    				} else {
					updateData(mCallEntryUris);
					//    				}
				}
			}
		}, mIsFristResume ? 0 : 1000);
	}

	@Override
	protected void onPause() {
		historyList.auroraOnPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		historyList.auroraOnResume();
		super.onResume();
		if (editMode) {
//			closeEditMode();
		}        

		SystemUtils.setStatusBarBackgroundTransparent(this);
	}

	private Uri[] getCallLogEntryUris() {
		return gnGetCallLogEntryUris();    		    
	}


	/**
	 * Update user interface with details of given call.
	 *
	 * @param callUris URIs into {@link CallLog.Calls} of the calls to be displayed
	 */
	private void updateData(final Uri... callUris) {
		if(editMode) return;
		
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
				log("updateData numCalls = " + numCalls);
				//                PhoneCallDetails[] details = new PhoneCallDetails[numCalls];
				try {
					//                    for (int index = 0; index < numCalls; ++index) {
					//                        details[index] = getPhoneCallDetailsForUri(callUris[index]);
					//                    }
					//                    return details;
					return getPhoneCallDetailsFromCursor(gnGetCallLogCursor());
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
					Toast.makeText(mActivity, R.string.toast_call_detail_error,
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
//					ContactsApplication.mPrivacyActivityList.add(FullCallDetailActivity.this);                    
				}


				mNumberArea = firstDetails.numberArea;
				mNameOrig = mName;
				long mSimId = firstDetails.simId;


				final Uri contactUri = firstDetails.contactUri;
				final Uri photoUri = firstDetails.photoUri;


				Log.d(TAG, "contactUri = " +  contactUri);

				if ((null != mNumber && mNumber.equals("-1"))) {
					mNumberType = 2;
				} else if (null == contactUri) {
					mNumberType = 1;
				} else {
					mNumberType = 0;
				}                

				mContactUri = contactUri;

				if (mIsRejectedDetail) {
					mAdapter = new AuroraCallDetailHistoryAdapter(mActivity, mInflater,
							mCallTypeHelper, details, false, true,
							findViewById(R.id.controls), true);
					historyList.setAdapter(mAdapter);
				} else {
					mAdapter = new AuroraCallDetailHistoryAdapter(mActivity, mInflater,
							mCallTypeHelper, details, false, true,
							findViewById(R.id.controls), false);
					historyList.setAdapter(mAdapter);
				}
				mAdapter.setCallEntryUris(mCallEntryUris);

				if(mAdapter.getCount() > 0) {

					TextView clearButton  = (TextView)mActionBar.findViewById(R.id.action_clear);
					if(clearButton == null) {
						mActionBar.addItem(R.layout.aurora_right_actionbar_item, AURORA_CALL_DETAIL_MORE);
						clearButton  = (TextView)mActionBar.findViewById(R.id.action_clear);
					}
					if(clearButton!=null){
						clearButton.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								onMenuCleanCallLog();
							}
						});
					}
				} 

				if (ContactsApplication.sIsGnContactsSupport) {
					showPhoneRecords(details);
				}
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

			return new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
					contactInfo.countryIso, contactInfo.geocode,
					contactInfo.type, contactInfo.date,
					contactInfo.duration, contactInfo.name,
					contactInfo.nNumberTypeId, contactInfo.label,
					contactInfo.lookupUri, contactInfo.photoId, photoUri, contactInfo.simId,
					contactInfo.vtCall, 0, contactInfo.numberArea, contactInfo.userMark, contactInfo.markCount, contactInfo.private_id);


		} finally {
			if (callCursor != null) {
				callCursor.close();
			}
		}
	}


	public void onMenuCleanCallLog() {
		new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(mIsContactDetial?R.string.gn_clearCallLogConfirmation_title:R.string.gn_clearCallLogConfirmation_title_for_number)  // gionee xuhz 20120728 modify for CR00658189
		//		.setMessage(mIsContactDetial ? R.string.gn_clearSingleCallLogConfirmation :R.string.gn_clearAllCallLogConfirmation)
		.setPositiveButton(R.string.clear_all, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final StringBuilder callIds = new StringBuilder();
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 begin舍弃
				if (ids != null) {
					for (int id : ids) {
						if (callIds.length() != 0) {
							callIds.append(",");
						}
						callIds.append(id);
					}
				}
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 end

				Log.d(TAG,"ids:"+callIds);
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

						Log.d(TAG,"mIsContactDetial:"+mIsContactDetial);

						if(mIsContactDetial){						
							Intent intent=new Intent(FullCallDetailActivity.this,FullCallDetailActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("rawcontactid", mRawContactId);
							intent.putExtra("contact_detail", true);
							startActivity(intent);		     
						} else if (mIsRejectedDetail) {
							Intent intent=new Intent(FullCallDetailActivity.this,FullCallDetailActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							intent.putExtra("reject_detail", true);
							intent.putExtra(Calls.NUMBER, mCallNumber);
							startActivity(intent);		
						}else{
							Intent intent=new Intent(FullCallDetailActivity.this,AuroraDialActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);		
						}


					}
				});
			}
		})
		.setNegativeButton(android.R.string.no, null).show();        
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
		if (ContactsApplication.sIsAuroraPrivacySupport && mIsPrivate) {
			ContactsApplication.mPrivacyActivityList.remove(this);
		}
	}

	private static final int SIM_INFO_UPDATE_MESSAGE = 100;

	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SIM_INFO_UPDATE_MESSAGE:
				// gionee xuhz 20120912 modify for CR00692279 start
				Uri[] callEntryUris = getCallLogEntryUris();
				if (callEntryUris != null) {
					//        				if (callEntryUris.length > 1) {
					//        					updateData(callEntryUris[0]);
					//        					updateHistoryList(callEntryUris);
					//        				} else {
					updateData(callEntryUris);
					//        				}
				}else{
					Intent intent=new Intent(FullCallDetailActivity.this,FullCallDetailActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.putExtra("rawcontactid", mRawContactId);
					intent.putExtra("contact_detail", true);
					startActivity(intent);	
				}
				// gionee xuhz 20120912 modify for CR00692279 end
				break;
			default:
				break;
			}
		}
	};


	private void log(final String msg) {
		if (true) {
			Log.d(TAG, msg);
		}
	}


	private int[] ids;
	private String mCallNumber;
	private int mCallsCount;
	private int mCallId;

	private static final int AURORA_CALL_DETAIL_MORE = 1;

	private boolean mIsFristResume = true;
	private Uri[] gnGetCallLogEntryUris() {
		log("CallDetailActivity gnGetCallLogEntryUris()");

		Uri queryUri = Uri.parse("content://call_log/callsjoindataview");

		//aurora changes zhouxiaobing 20130925 start               
//		int[] ids;
//
//
//		if (mIsContactDetial) {
//			ids = getCallIdsByContactUri(mRawContactId);
//		} else {
//			ids = getCallIdsByNumber(mCallNumber, 0);
//		}

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

	private int[] getCallIdsByNumber(String number, int callType) {
		String srotOrder = Calls.DATE + " DESC ";

		//Gionee:huangzy 20120906 modify for CR00688166 start
		/*String selection = Calls.NUMBER + "='" + number + "'";*/
		String selection = null;
		if (ContactsApplication.sIsGnCombineCalllogMatchNumber) {
			int numLen = number.length();
			final int MATCH_LEN = ContactsApplication.GN_MATCH_CONTACTS_NUMBER_LENGTH;
			if (MATCH_LEN <= numLen) {
				selection = "( " +Calls.NUMBER + "='" + number + "' OR " + Calls.NUMBER +
						" LIKE '%" + number.substring(numLen - MATCH_LEN, numLen) + "' )";
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
		
		if(mIsPrivate) {
			selection +=  " and privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();
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

	private int[] getCallIdsByContactUri(long rawcontactId) {
		if(rawcontactId == -1) return null;

		String srotOrder = Calls.DATE + " DESC ";

		String selection = null;
		selection =  " raw_contact_id = '" + rawcontactId + "'";  
		if(mIsPrivate) {
			selection +=  " and privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();
		}

		Cursor c = getContentResolver().query(Calls.CONTENT_URI, new String[]{Calls._ID}, 
				selection, null, srotOrder);

		if (null == c) {
			return null;
		}

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

		String path = GnStorageManager.getInstance(ContactsApplication.getInstance()).getInternalStoragePath();
		if (path == null) {
			return false;
		}

		String historyPath = path + "/" + mActivity.getString(R.string.aurora_call_record_history_path);
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

				//				historyList = (AuroraListView) findViewById(R.id.history);
				if (null != historyList && null != historyList.getAdapter()) {
					((BaseAdapter)(historyList.getAdapter())).notifyDataSetChanged();
				}
			}

		}.execute();
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
					Toast.makeText(mActivity, R.string.toast_call_detail_error,
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

				//                historyList = (AuroraListView) findViewById(R.id.history);

				if (mIsRejectedDetail) {
					mAdapter = new AuroraCallDetailHistoryAdapter(mActivity, mInflater,
							mCallTypeHelper, details, false, canPlaceCallsTo,
							findViewById(R.id.controls), true);
					historyList.setAdapter(mAdapter
							);
				} else {
					mAdapter = new AuroraCallDetailHistoryAdapter(mActivity, mInflater,
							mCallTypeHelper, details, false, canPlaceCallsTo,
							findViewById(R.id.controls), false);
					historyList.setAdapter(mAdapter
							);
				}
				mAdapter.setCallEntryUris(mCallEntryUris);

				if (ContactsApplication.sIsGnContactsSupport) {
					showPhoneRecords(details);
				}
			}
		}
		mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateHistoryListTask());
	}

	private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
		public void onAuroraActionBarItemClicked(int itemId) {

			switch (itemId) {
			case AURORA_CALL_DETAIL_MORE:
				onMenuCleanCallLog();
				break;
			default:
				break;
			}
		}
	};    

	private boolean mIsPrivate = false;
	private boolean mIsPrivateUri = false;





	@Override
	public boolean onItemLongClick(AdapterView<?> parent,
			View view, int position, long id) {
		Log.d(TAG,"onItemLongClick view:"+view+" getId():"+view.getId()+" position:"+position);

		AuroraCheckBox cb = (AuroraCheckBox) view
				.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		cb.setChecked(true);
		if (!editMode) {
			if (mCallEntryUris != null && position < mCallEntryUris.length) {
				mAdapter.getSelectSet().add(mCallEntryUris[position]);
			}
			openEditMode();
		}
		return false;
	}


	/**
	 * @Title: openEditMode
	 * @Description: 打开编辑模式
	 * @param
	 * @return void
	 * @throws
	 */
	private void openEditMode() {
		editMode = true;

		historyList.setLongClickable(false);
		historyList.auroraEnableSelector(false);

		mActionBar.setShowBottomBarMenu(true);
		mActionBar.showActionBarDashBoard();

		leftView = (TextView) mActionBar.getSelectLeftButton();
		rightView = (TextView) mActionBar.getSelectRightButton();

		rightView.setText(getString(R.string.selectAll));
		leftView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editMode) {					
					closeEditMode();
				}
			}
		});

		rightView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isSelectAll) {
					mAdapter.getSelectSet().clear();
					mAdapter.notifyDataSetChanged();
				} else {
					for (Uri uri: mCallEntryUris) {
						Log.e(TAG, "uri: " +uri);
						mAdapter.getSelectSet().add(uri);
					}
					mAdapter.notifyDataSetChanged();
				}
				checkSelect();
			}
		});

		mAdapter.setEditMode(true);
		mAdapter.setNeedAnim(true);
		mAdapter.notifyDataSetChanged();

		//		mListView.auroraEnableSelector(false);
	}

	/**
	 * @Title: checkSelect
	 * @Description: 检查是否全选状态
	 * @param
	 * @return void
	 * @throws
	 */
	private void checkSelect() {
		int allCount = mCallEntryUris.length;
		if (mAdapter.getSelectSet().size() == allCount) {
			isSelectAll = true;
		} else {
			isSelectAll = false;
		}
		if (isSelectAll) {
			rightView.setText(getString(R.string.menu_select_none));
		} else {
			rightView.setText(getString(R.string.menu_select_all));
		}
		if (mAdapter.getSelectSet().size() == 0) {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
					1, false);
		} else {
			mActionBar.getAuroraActionBottomBarMenu().setBottomMenuItemEnable(
					1, true);
		}
	}

	/**
	 * @Title: closeEditMode
	 * @Description: 关闭编辑模式
	 * @param
	 * @return void
	 * @throws
	 */
	private void closeEditMode() {
		editMode = false;
		historyList.setLongClickable(true);
		historyList.auroraEnableSelector(true);

		mActionBar.setShowBottomBarMenu(false);
		mActionBar.showActionBarDashBoard();

		for (int i = 0; i < historyList.getChildCount(); i++) {
			AuroraCheckBox cb = (AuroraCheckBox) historyList.getChildAt(i)
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
			cb.setChecked(false);
			cb.postInvalidate();
		}
		mAdapter.getSelectSet().clear();

		mAdapter.setEditMode(false);
		mAdapter.setNeedAnim(true);
		mAdapter.notifyDataSetChanged();
	}

	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
			mActionBar.setTitle(R.string.aurora_reject_call_detail_title);
		} else {        	
			mActionBar.setTitle(R.string.callDetailTitle);
		}
		setAuroraBottomBarMenuCallBack(new OnAuroraMenuItemClickListener() {
			@Override
			public void auroraMenuItemClick(int paramInt) {
				switch (paramInt) {
				case R.id.menu_delete:
					showSelectDeleteDialog();
					break;
				}
			}
		});
		mActionBar.initActionBottomBarMenu(R.menu.aurora_delete, 1);
		mActionBar.setOnAuroraActionBarListener(auroraActionBarItemClickListener);
	}
	private boolean showDialog = false;

	private OnDismissListener dismissListener = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface arg0) {
			showDialog = false;
		}
	};

	private void showSelectDeleteDialog() {
		//		if (showDialog) {
		//			return;
		//		}
		showDialog = true;
		if (ids == null || mAdapter.getSelectSet().size() == 0) {
			return;
		}

		new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearSelectedCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		.setMessage(R.string.gn_clearSelectedCallLogConfirmation)
		.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final StringBuilder callIds = new StringBuilder();
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 begin
				Set<Uri> selectIdSet = mAdapter.getSelectSet();
				int count = selectIdSet.size();
				int[] selectIds = new int[count];
				int i = 0;
				for (Uri uri : selectIdSet) {
					selectIds[i] = Integer.valueOf(uri.getLastPathSegment());
					i++;
				}

				if (selectIds != null) {
					for (int id : selectIds) {
						if (callIds.length() != 0) {
							callIds.append(",");
						}
						callIds.append(id);
					}
				}
				//Gionee <xuhz> <2013-08-01> modify for CR00844946 end

				Log.d(TAG,"ids:"+callIds);
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
						//		                        finish();
						if (editMode) {					
							closeEditMode();
						}
						mCallEntryUris = getCallLogEntryUris();
						mHandler.sendEmptyMessageDelayed(SIM_INFO_UPDATE_MESSAGE, 700);
					}
				});
			}
		})
		.setNegativeButton(android.R.string.no, null).show();      


	}

	@Override
	public void onItemClick(AdapterView<?> paramAdapterView, View view,
			int position, long id) {
		Log.e(TAG, "[onItemClick]editMode:"+editMode+";mCallEntryUris.length:"+mCallEntryUris.length+";position:"+position);
		if (editMode) {
			if (!mAdapter.getSelectSet().contains(
					mCallEntryUris[position])) {
				mAdapter.getSelectSet().add(mCallEntryUris[position]);
				Log.e(TAG, "mCallEntryUris:" + mCallEntryUris[position]);
				AuroraCheckBox cb = (AuroraCheckBox) view
						.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				cb.auroraSetChecked(true, true);
			} else {
				mAdapter.getSelectSet().remove(mCallEntryUris[position]);

				AuroraCheckBox cb = (AuroraCheckBox) view
						.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
				cb.auroraSetChecked(false, true);
			}
			checkSelect();
			//			getContentResolver().delete(Calls.CONTENT_URI_WITH_VOICEMAIL, Calls._ID, selectionArgs);
		}else{
			mAdapter.playRecord(position);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mActionBar.auroraIsExitEditModeAnimRunning()
					|| mActionBar.auroraIsEntryEditModeAnimRunning()) {
				return true;
			}
			if (editMode) {
				closeEditMode();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private Cursor gnGetCallLogCursor() {
		log("CallDetailActivity gnGetCallLogCursor()");

		if (mIsContactDetial) {
			return getCallCursorByContactUri(mRawContactId);
		}	else {
			return getCallCursorByNumber(mCallNumber, 0);
		}       

	}

	private Cursor getCallCursorByNumber(String number, int callType) {
		String srotOrder = Calls.DATE + " DESC ";

		Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
		//Gionee:huangzy 20120906 modify for CR00688166 start
		/*String selection = Calls.NUMBER + "='" + number + "'";*/
		String selection = null;
		if (ContactsApplication.sIsGnCombineCalllogMatchNumber) {
			int numLen = number.length();
			final int MATCH_LEN = ContactsApplication.GN_MATCH_CONTACTS_NUMBER_LENGTH;
			if (MATCH_LEN <= numLen) {
				selection = "( " + Calls.NUMBER + "='" + number + "' OR " + Calls.NUMBER +
						" LIKE '%" + number.substring(numLen - MATCH_LEN, numLen) + "' )";
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
		
		if(mIsPrivate) {
			selection +=  " and privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();
		}

		Cursor c = getContentResolver().query(queryUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, 
				selection, null, srotOrder);


		return c;
	}

	private Cursor getCallCursorByContactUri(long rawcontactId) {
		if(rawcontactId == -1) return null;

		Uri queryUri = Uri.parse("content://call_log/callsjoindataview");

		String srotOrder = Calls.DATE + " DESC ";

		String selection = null;
		selection =  " calls.raw_contact_id = '" + rawcontactId + "'";    	
		if(mIsPrivate) {
			selection +=  " and privacy_id = " + AuroraPrivacyUtils.getCurrentAccountId();
		}

		Cursor c = getContentResolver().query(queryUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, 
				selection, null, srotOrder);


		return c;
	}

	private PhoneCallDetails[] getPhoneCallDetailsFromCursor(Cursor callCursor) {
		if(callCursor == null) {
			return null;
		}

		if(callCursor.getCount() == 0) {
			callCursor.close();
			return null;
		}
		ContentResolver resolver = getContentResolver();
		//	        Cursor callCursor = resolver.query(callUri, CALL_LOG_PROJECTION, null, null, null);
//		String selection = null;
//		if(ContactsApplication.sIsAuroraPrivacySupport) {
//			selection = "privacy_id > -1 ";
//		}
		//	        Cursor callCursor = resolver.query(callUri, CallLogQuery.PROJECTION_CALLS_JOIN_DATAVIEW, selection, null, null);        
		PhoneCallDetails[] result = new PhoneCallDetails[callCursor.getCount()];

		callCursor.moveToFirst();
		int count = 0;
		do {
			if(!callCursor.isAfterLast()) {	    		

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

				result[count] = new PhoneCallDetails(contactInfo.number, contactInfo.formattedNumber, 
						contactInfo.countryIso, contactInfo.geocode,
						contactInfo.type, contactInfo.date,
						contactInfo.duration, contactInfo.name,
						contactInfo.nNumberTypeId, contactInfo.label,
						contactInfo.lookupUri, contactInfo.photoId, photoUri, contactInfo.simId,
						contactInfo.vtCall, 0, contactInfo.numberArea, contactInfo.userMark, contactInfo.markCount, contactInfo.private_id);

				count ++;

			}
		} while(callCursor.moveToNext());
		callCursor.close();

		return result;
	}
}

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

import android.app.StatusBarManager;

import com.android.internal.telephony.ITelephony;
import com.android.contacts.BackScrollManager.ScrollableHeader;
import com.android.contacts.PhoneCallDetails.PhoneCallRecord;
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
// Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature start
import com.android.contacts.common.util.BitmapUtil;
// Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature end
import com.android.contacts.preference.ContactsPreferenceActivity;
import com.android.contacts.preference.DisplayOptionsPreferenceFragment;
import com.android.contacts.util.AccountFilterUtil;
import com.android.contacts.util.AsyncTaskExecutor;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.Blur;
import com.android.contacts.util.GnCallForSelectSim;
import com.android.contacts.util.GnHotLinesUtil;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.SchedulingUtils;
import com.android.contacts.util.StructuredPostalUtils;
import com.android.contacts.util.YuloreUtils;
import com.android.contacts.util.AuroraDatabaseUtils;;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SubContactsUtils;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.calllog.CallLogSimInfoHelper;
import com.android.contacts.R;

import android.os.SystemProperties;
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
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import gionee.provider.GnContactsContract;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import gionee.provider.GnCallLog.Calls;
import android.provider.Contacts.Intents.Insert;
import gionee.provider.GnContactsContract.CommonDataKinds.Phone;
import gionee.provider.GnContactsContract.Contacts;
import android.provider.MediaStore.Audio.Media;
import gionee.provider.GnTelephony.SIMInfo;
//import android.provider.VoicemailContract.Voicemails;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import aurora.widget.AuroraListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.mediatek.contacts.list.service.MultiChoiceService;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.android.contacts.util.Constants;
import com.android.contacts.widget.MultiShrinkScroller;
import com.android.contacts.widget.QuickContactImageView;
import com.android.contacts.widget.MultiShrinkScroller.MultiShrinkScrollerListener;

import gionee.app.GnStatusBarManager;

// Gionee:wangth 20120710 add for CR00633799 begin
import gionee.provider.GnContactsContract.Data;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
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
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import aurora.widget.AuroraCheckBox;

import com.android.contacts.calllog.AuroraCallLogAdapterV2;
import com.android.contacts.detail.ContactDetailDisplayUtils;
import com.android.contacts.detail.ContactDisplayUtils;
import com.android.contacts.editor.ContactEditorFragment;
import com.android.contacts.interactions.CallLogInteraction;
import com.android.contacts.interactions.ContactInteraction;

import aurora.widget.AuroraCustomMenu.OnMenuItemClickLisener;
import aurora.widget.AuroraMenu;
import gionee.provider.GnTelephony.SIMInfo;
import gionee.telephony.AuroraTelephoneManager;

import com.privacymanage.service.AuroraPrivacyUtils;

import android.support.v7.app.ActionBarActivity;
import android.animation.ObjectAnimator;
import android.provider.ContactsContract.DataUsageFeedback;
import android.provider.ContactsContract.QuickContact;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.*;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.util.Collections;

import android.database.DatabaseUtils;

import com.android.contacts.quickcontact.ExpandingEntryCardView;
import com.android.contacts.quickcontact.QuickContactActivity;
import com.android.contacts.quickcontact.ExpandingEntryCardView.*;

import android.view.MenuInflater;
/**
 * Displays the details of a specific call log entry.
 * <p>
 * This activity can be either started with the URI of a single call log entry, or with the
 * {@link #EXTRA_CALL_LOG_IDS} extra to specify a group of call log entries.
 */
public class AuroraCallDetailActivityV2 extends ActionBarActivity  {
    private static final String TAG = "AuroraCallDetailActivityV2";

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


 

    private TextView mLargeTextView;

    private AsyncTaskExecutor mAsyncTaskExecutor;
    private ContactInfoHelper mContactInfoHelper;

    private String mNumber = null;
    private String mName = null;
    private String mNote = null;
    private String mDefaultCountryIso;
    private Uri mContactUri;
    
//    private ActionBar mActionBar;
    private Context mContext;
    private int mNumberType = 0;

    /* package */ LayoutInflater mInflater;
    /* package */ Resources mResources;
    /** Helper to load contact photos. */
    private ContactPhotoManager mContactPhotoManager;
    /** Helper to make async queries to content resolver. */
//    private CallDetailActivityQueryHandler mAsyncQueryHandler;
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

    
    private static boolean mIsRejectedDetail = false;// aurora wangth 20140618 add for reject detail
    private String mBlackName = null;
    private boolean mIsShowRejectFlag = false;
    private String mUserMark = null;
    private boolean mIsNoUserMark = false;
    private String mMarkContent = null;
    private int mMarkCount = 0;
    private String mNameOrig = null;
   
    private int mContactSimId = -1;
    private static final int ADD_MARK = 1;
    private static final int EDIT_MARK = 2;
    private String mNumberArea = null;
    private boolean mNeedReQuery = false;    

  

    private final View.OnClickListener mPrimaryActionListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//        	showCallDialog(view);
            // Aurora xuyong 2016-01-22 modified for bug #18556 start
           	//showCallPopupWindow(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
            Intent dial = IntentFactory.newDialNumberIntent(mCallNumber);
            startActivity(dial);
            // Aurora xuyong 2016-01-22 modified for bug #18556 end
        }
    };
    
	AuroraAlertDialog mCallDialog = null;  
	private int mSelectSlot = 0;
    protected void showCallDialog(View view) {  
    	    	
	     if(mCallDialog != null) {
	    	 mCallDialog.dismiss();
	    	 mCallDialog = null;
	     }
	     
    	if(ContactsApplication.isMultiSimEnabled) {
    		boolean showDouble = ContactsUtils.isShowDoubleButton();
			if (showDouble && SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
				  int lastCallSlotId = ContactsUtils.getLastCallSlotId(mContext, mCallNumber);
  			    String recentCall = getString(R.string.aurora_recent_call);
  			    String menuSlot0 = getString(R.string.aurora_slot_0) +  getString(R.string.gn_dial_desciption);
  			    String menuSlot1 = getString(R.string.aurora_slot_1) +  getString(R.string.gn_dial_desciption);
  			    if (lastCallSlotId == 0) {
			        menuSlot0 = menuSlot0 + recentCall;
			    } else if (lastCallSlotId == 1) {
			        menuSlot1 = menuSlot1 + recentCall;
			    }
  			    AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);  
  		        builder.setTitle(mContext.getString(R.string.menu_callNumber, mCallNumber));

  	           DialogInterface.OnClickListener singleListener =   
  	               new DialogInterface.OnClickListener() {  
  	                     
  	                   @Override  
  	                   public void onClick(DialogInterface dialogInterface,   
  	                           int which) {  
  	                	      mSelectSlot = which ;  
  	                   }  
  	               };           
  	            
  	           builder.setSingleChoiceItems(new String[]{menuSlot0, menuSlot1}, 0, singleListener);  
  	           DialogInterface.OnClickListener btnListener =   
  	               new DialogInterface.OnClickListener() {  
  	                   @Override  
  	                   public void onClick(DialogInterface dialogInterface, int which) {
  	                		Intent intent = AuroraTelephoneManager.getCallNumberIntent(mCallNumber, mSelectSlot);
							intent.putExtra("contactUri", mContactUri);
							startActivity(intent);
							mNeedReQuery = true;
  	                   }  
  	               };  
  	           builder.setPositiveButton(android.R.string.ok, btnListener);  
  	           mCallDialog = builder.create();   
  	           mCallDialog.show();
  	           return;
			}
    	} 
    	
        startActivity(((ViewEntry) view.getTag()).primaryIntent.putExtra(Constants.EXTRA_FOLLOW_SIM_MANAGEMENT, true));
    	mNeedReQuery = true;
	 
}     
    
    public class MenuAdapter extends ArrayAdapter<String> {
        private int mResourceId;
  

        public MenuAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId, objects);
            this.mResourceId = textViewResourceId;
      
        }
        
        public MenuAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
            this.mResourceId = textViewResourceId;
      
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {            
            LayoutInflater inflater = getLayoutInflater();
            View view = inflater.inflate(mResourceId, null);
            TextView text = (TextView) view.findViewById(R.id.popup_menu_item);
            text.setText(getItem(position));     
            return view;
        }
    }
    
    private void showCallPopupWindow(Intent callIntent) {
	  	if(ContactsApplication.isMultiSimEnabled) {
	  		   boolean showDouble = com.android.contacts.ContactsUtils.isShowDoubleButton();
				if (showDouble && SubContactsUtils.simStateReady(0) && SubContactsUtils.simStateReady(1)) {
				       View contentView = LayoutInflater.from(mContext).inflate(
				               R.layout.aurora_popup_menu, null);
				       
				       AuroraListView menulist = (AuroraListView)contentView.findViewById(R.id.menu_list);
				 
				       int lastCallSlotId = com.android.contacts.ContactsUtils.getLastCallSlotId(mContext, mCallNumber);
				       String recentCall = getString(R.string.aurora_recent_call);
					    String menuSlot0 = getString(R.string.aurora_slot_0) +  getString(R.string.gn_dial_desciption);
					    String menuSlot1 = getString(R.string.aurora_slot_1) +  getString(R.string.gn_dial_desciption);
					    if (lastCallSlotId == 0) {
					        menuSlot0 = menuSlot0 + recentCall;
					    } else if (lastCallSlotId == 1) {
					        menuSlot1 = menuSlot1 + recentCall;
					    }
				       
				       menulist.setAdapter(new MenuAdapter(AuroraCallDetailActivityV2.this,R.layout.aurora_popup_menu_item, new String[]{menuSlot0, menuSlot1}));
				       
				       final PopupWindow popupWindow = new PopupWindow(contentView,
				               LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
				       
				       menulist.setOnItemClickListener(new OnItemClickListener() {
				       	@Override
				           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				       		Intent intent = AuroraTelephoneManager.getCallNumberIntent(mCallNumber, position);
							intent.putExtra("contactUri", mContactUri);
							startActivity(intent);
				       	 	popupWindow.dismiss();
				       		mNeedReQuery = true;
				       	}
						});

				       popupWindow.setTouchable(true);
				       
				       popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
				     
				
				       popupWindow.setBackgroundDrawable(new BitmapDrawable());

				       popupWindow.showAtLocation (AuroraCallDetailActivityV2.this.findViewById(R.id.multiscroller), Gravity.BOTTOM, 0, 0 );
				       return;
				}
	  	}
	    startActivity(callIntent);
    	mNeedReQuery = true;
   }
    
    

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
    	ContactsApplication.sendSimContactBroad();
        
        Log.d(TAG,"CallDetailActivity  onCreat()");
        super.onCreate(icicle);
        
        
        
        setContentView(R.layout.quickcalldetail_activity); 
        mContext = AuroraCallDetailActivityV2.this;
//        mActionBar = getSupportActionBar();
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.callDetailTitle);
        
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.addView(getLayoutInflater().inflate(R.layout.quickcontact_title_placeholder, null));

        mAsyncTaskExecutor = AsyncTaskExecutors.createThreadPoolExecutor();
        mInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mResources = getResources();

        mCallTypeHelper = new CallTypeHelper(getResources());
        mPhoneNumberHelper = new PhoneNumberHelper(mResources);
        mPhoneCallDetailsHelper = new PhoneCallDetailsHelper(mResources, mCallTypeHelper,
                                                             mPhoneNumberHelper, null, this);
//        mAsyncQueryHandler = new CallDetailActivityQueryHandler(this);
     
      


        mDefaultCountryIso = ContactsUtils.getCurrentCountryIso(this);
        mContactPhotoManager = ContactPhotoManager.getInstance(this);
        mContactInfoHelper = new ContactInfoHelper(this, mDefaultCountryIso);
        // The following lines are provided and maintained by Mediatek Inc.
//        mSimName = new TextView(this);// just a stub, uesless
        SIMInfoWrapper.getDefault().registerForSimInfoUpdate(mHandler, SIM_INFO_UPDATE_MESSAGE, null);
        mStatusBarMgr = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        // The previous lines are provided and maintained by Mediatek Inc.
        //optionallyHandleVoicemail(); deleted by Mediatek Inc to close Google default Voicemail function.

        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
            this.registerReceiver(mReceiver, intentFilter);
        }

		
        Intent intent = getIntent();  
//        ids=intent.getIntArrayExtra("ids");
        
        mCallNumber = intent.getStringExtra(Calls.NUMBER);
        mCallsCount = intent.getIntExtra(Calls._COUNT, 0);
        mCallId = intent.getIntExtra(Calls._ID, 0);
        mCallType = intent.getIntExtra(Calls.TYPE, 0);
        mContactSimId = intent.getIntExtra("contact_sim_id", -1);

        mIsRejectedDetail = intent.getBooleanExtra("reject_detail", false);
        if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
//        	setMenuEnable(false);
//        	header_bg.setBackgroundColor(mResources.getColor(R.color.aurora_reject_call_detail_header_bg));
        	getSupportActionBar().setTitle(R.string.aurora_reject_call_detail_title);
        	mBlackName = intent.getStringExtra("black_name");
        	mUserMark = intent.getStringExtra("user-mark");
        	mMarkContent = intent.getStringExtra("mark-content");
        	mMarkCount = intent.getIntExtra("mark-count", 0);
        } 
        
        
//        if(ids!=null){
//        	for(int i:ids){
//        		Log.d(TAG,"ids:"+i);
//        	}
//        }
        

        
        mPhotoView = (QuickContactImageView) findViewById(R.id.photo);
//        mBlurPhotoView = (QuickContactImageView) findViewById(R.id.blur_photo);
        mSmallPhotoView = (ImageView) findViewById(R.id.small_photo);
        mScroller = (MultiShrinkScroller) findViewById(R.id.multiscroller);
        mLargeTextView = (TextView) findViewById(R.id.large_title);
        mLargeTextView.setVisibility(View.INVISIBLE);
        mTransparentView = findViewById(R.id.transparent_view);
        mTransparentView.setEnabled(false);
        if (mScroller != null) {
        	mTransparentView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTransparentView.setEnabled(false);
                    mScroller.scrollOffBottom();
                }
            });
        }
        mHasAlreadyBeenOpened = icicle != null;
        mIsEntranceAnimationFinished = mHasAlreadyBeenOpened;
        mWindowScrim = new ColorDrawable(SCRIM_COLOR);
        mWindowScrim.setAlpha(0);
        getWindow().setBackgroundDrawable(mWindowScrim);
        mExtraMode = getIntent().getIntExtra(QuickContact.EXTRA_MODE,
                QuickContact.MODE_LARGE);
        mScroller.initialize(mMultiShrinkScrollerListener, mExtraMode == MODE_FULLY_EXPANDED);
        mScroller.setVisibility(View.INVISIBLE);
//        SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ true,
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        if (!mHasAlreadyBeenOpened) {
//                            // The initial scrim opacity must match the scrim opacity that would be
//                            // achieved by scrolling to the starting position.
//                            final float alphaRatio = mExtraMode == MODE_FULLY_EXPANDED ?
//                                    1 : mScroller.getStartingTransparentHeightRatio();
//                            final int duration = getResources().getInteger(
//                                    android.R.integer.config_shortAnimTime);
//                            final int desiredAlpha = (int) (0xFF * alphaRatio);
//                            ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", 0,
//                                    desiredAlpha).setDuration(duration);
//
//                            o.start();
//                        }
//                    }
//                });

        if (icicle != null) {
            final int color = icicle.getInt(KEY_THEME_COLOR, 0);
            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
                    new Runnable() {
                        @Override
                        public void run() {
                            // Need to wait for the pre draw before setting the initial scroll
                            // value. Prior to pre draw all scroll values are invalid.
                            if (mHasAlreadyBeenOpened) {
                                mScroller.setVisibility(View.VISIBLE);
                                mScroller.setScroll(mScroller.getScrollNeededToBeFullScreen());
                            }
                            // Need to wait for pre draw for setting the theme color. Setting the
                            // header tint before the MultiShrinkScroller has been measured will
                            // cause incorrect tinting calculations.
//                            if (color != 0) {
//                                setThemeColor(mMaterialColorMapUtils
//                                        .calculatePrimaryAndSecondaryColor(color));
//                            }
                        }
                    });
        }
        // Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature start
       mIsCallHistoryEmpty = getIntent().getBooleanExtra("isCallHistoryEmpty", false);
        if (mIsCallHistoryEmpty) {
            updateEmptyCallData(intent.getStringExtra(Calls.NUMBER));
        }
        // Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature end


    }
    
 
    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onstart");
       hasSms = PhoneCapabilityTester.isSmsIntentRegistered(getApplicationContext());
        
        mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Uri[] callEntryUris = getCallLogEntryUris();
                if (callEntryUris != null) {
                    Log.d(TAG, "callEntryUris:" + callEntryUris.length);
                }
				if (callEntryUris != null) {
					mContactSimId = isSimCardPhoneNumber(mContext, mNumber);
//    				if (callEntryUris.length > 1) {
    					updateData(callEntryUris[0]);
//    					updateHistoryList(callEntryUris);
//    				} else {
//    					updateData(callEntryUris);
//    				}
				}
			}
		}, 0);
        
    }

    @Override
    public void onResume() {
        super.onResume();
        
        Log.d(TAG, "onresume");

        
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            setSimIndicatorVisibility(true);
            mShowSimIndicator = true;
        }
        
        Log.d(TAG,"mNeedReQuery:"+mNeedReQuery);
        if (mNeedReQuery) {
        	mNeedReQuery = false;
        	mHandler.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				Uri[] callEntryUris = getCallLogEntryUris();
    				if (callEntryUris != null) {
    					mContactSimId = isSimCardPhoneNumber(mContext, mNumber);
//        				if (callEntryUris.length > 1) {
        					updateData(callEntryUris[0]);
//        					updateHistoryList(callEntryUris);
//        				} else {
//        					updateData(callEntryUris);
//        				}
    				}
    			}
    		}, 0);
        }
        
        SystemUtils.setStatusBarBackgroundTransparent(this);
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

        log("CallDetailActivity gnGetCallLogEntryUris()");
        
        Uri queryUri = Uri.parse("content://call_log/callsjoindataview");
        ids=getCallIdsByNumber(mCallNumber, 0);
        Log.d(TAG,"ids:"+ids);
        /*if (0 == mCallsCount && !mIsRejectedDetail) {
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
        }*/
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
        	Log.d(TAG,"uri:"+uris[index].toString());
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
                		mNeedReQuery=true;
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
                        if (ContactsApplication.sIsAuroraRejectSupport) {
                        	checkForBlack(details[index].number.toString()); // aurora wangth 20140620 add for black
                        }
                        mCalllogEntries = buildCalllogEntries(); 
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
            	  Log.w(TAG, "onPostExecute");
                if (details == null) {
                    // Somewhere went wrong: we're going to bail out and show error to users.
//                    Toast.makeText(AuroraCallDetailActivityV2.this, R.string.toast_call_detail_error,
//                            Toast.LENGTH_SHORT).show();
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
//                	header_bg.setBackgroundColor(mResources.getColor(R.color.aurora_privacy_contact_detail_header_color));
                    ContactsApplication.mPrivacyActivityList.add(AuroraCallDetailActivityV2.this);                    
                }
                
                
                
                mNumberArea = firstDetails.numberArea;
                mNameOrig = mName;
                long mSimId = firstDetails.simId;
                
                if(ContactsApplication.isMultiSimEnabled) {
                	SIMInfo siminfo = SIMInfoWrapper.getDefault().getSimInfoById(firstDetails.simId);
                	if(siminfo != null) {
                		mSlot = siminfo.mSlot;
                	}
	                Log.d(TAG, "mSlot = " +  mSlot + " firstDetails.simId" + firstDetails.simId);
                }
                
                if (!TextUtils.isEmpty(firstDetails.numberLabel)) {
                	phoneType = firstDetails.numberLabel.toString();
                }
                final Uri contactUri = firstDetails.contactUri;
                final Uri photoUri = firstDetails.photoUri;
        
                
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

               if (isVoicemailNumber) {
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
                    		mNeedReQuery=true;
                    		it = IntentFactory.newDialNumberIntent(numberCallUri);
        	            	//aurora add liguangyu 20131206 start
                    		it.putExtra("contactUri", contactUri);
        	            	//aurora add liguangyu 20131206 end
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
                                      
                    
                    ViewEntry entry = new ViewEntry(getString(gnResId,
                            displayNumber),
                            it,
                            getString(R.string.description_call, nameOrNumber));               
                    
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
                        
                            entry.setSecondaryAction(
                                    R.drawable.ic_message_24dp,
                                    itSecond,
                                    getString(R.string.description_send_text_message, nameOrNumber));

                    }
                                        

                    configureCallButton(entry);
                    
                    // gionee xuhz 20121126 modify for GIUI2.0 start
                    setPhoneNumberAndArea(displayNumber);
                    // gionee xuhz 20121126 modify for GIUI2.0 end
                } else {
                    disableCallButton();
                }
                
                //aurora delete liguangyu 20140616 for BUG #5783 start
            	if (!ContactsApplication.isMultiSimEnabled) {
            		gnSwitchUi(mNumberType);
            	}
                //aurora delete liguangyu 20140616 for BUG #5783 end

                mHasEditNumberBeforeCallOption =
                        canPlaceCallsTo && !isSipNumber;
                //mHasTrashOption = hasVoicemail();
                //mHasRemoveFromCallLogOption = !hasVoicemail();

                ExpandingEntryCardView historyList = (ExpandingEntryCardView) findViewById(R.id.history);
                // Aurora xuyong 2016-01-13 added fora aurora 2.0 new feature start
                historyList.setTitle(getResources().getString(R.string.recent_card_title));
                // Aurora xuyong 2016-01-13 added fora aurora 2.0 new feature end
                historyList.setExpandButtonText(
                        mContext.getResources().getString(R.string.expanding_entry_card_view_see_all));
                historyList.setExpandButtonOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    	Intent i = getIntent();
                    	i.setClassName(getApplicationContext(), "com.android.contacts.FullCallDetailActivity");
                    	i.putExtra(Calls.NUMBER,mCallNumber);
                    	startActivity(i);
                    }
                });
      
              	historyList.setOnClickListener(mEntryClickHandler);
            	historyList.initialize(mCalllogEntries ,
                        /* numInitialVisibleEntries = */ 3,
                        /* isExpanded = */ false,
                        /* isAlwaysExpanded = */ false,
                        mExpandingEntryCardViewListener,
                        mScroller);      
                
                
//                if (mIsRejectedDetail) {
//                	historyList.setAdapter(
//                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivityV2.this, mInflater,
//                                    mCallTypeHelper, details, false, canPlaceCallsTo,
//                                    findViewById(R.id.controls), true));
//                } else {
//                	historyList.setAdapter(
//                            new AuroraCallDetailHistoryAdapter(AuroraCallDetailActivityV2.this, mInflater,
//                                    mCallTypeHelper, details, false, canPlaceCallsTo,
//                                    findViewById(R.id.controls), false));
//                }
                
                if (ContactsApplication.sIsGnContactsSupport) {
//                	showPhoneRecords(details);
                }

                // gionee xuhz 20121126 modify for GIUI2.0 start
                if (mNumberType == 1 && !mIsRejectedDetail) {
                    if(!mIsPrivate && !mIsPrivateUri) {
                    	firstDetails.photoId = ContactPhotoManager.DEFAULT_UNKOWN_CONTACT_PHOTO;
                    }
            	}
                loadContactPhotosByPhotoId(firstDetails.photoId);
                // gionee xuhz 20121126 modify for GIUI2.0 end

                findViewById(R.id.call_detail).setVisibility(View.VISIBLE);
//                invalidateOptionsMenu();
                if(!mIsCallHistoryEmpty) {
                	showActivity();
                }
            }
        }
        mAsyncTaskExecutor.submit(Tasks.UPDATE_PHONE_CALL_DETAILS, new UpdateContactDetailsTask());
    }
    // Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature start
    private void updateEmptyCallData(final String number) {
        mNumber = number;
        // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
//        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(
//                        R.drawable.large_svg_dial_default_photo1);
        
        VectorDrawable drawable = (VectorDrawable) getResources().getDrawable(
                R.drawable.large_svg_dial_default_photo1);
        
        // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature end
        mPhotoView.setImageDrawable(drawable);
        // Aurora xuyong 2016-01-30 deleted for aurora 2.0 new feature start
        //Bitmap bm = drawable.getBitmap();
        //Bitmap newbitmap = Blur.fastblur(mContext, bm, 12);
        //mBlurPhotoView.setImageBitmap(newbitmap);
        //mSmallPhotoView.setImageBitmap(ContactDetailDisplayUtils.toRoundBitmap(bm));
        // Aurora xuyong 2016-01-30 deleted for aurora 2.0 new feature end

        findViewById(R.id.call_detail).setVisibility(View.VISIBLE);
        View convertView = findViewById(R.id.call_and_sms);
        convertView.setVisibility(View.VISIBLE);
        convertView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            	mNeedReQuery=true;
                Intent dial = IntentFactory.newDialNumberIntent(number);
                startActivity(dial);
            }
        });
        mPhoneTypeText = (TextView)convertView.findViewById(R.id.type);
        mPhoneTypeText.setVisibility(View.GONE);
        mPhoneNumberText = (TextView)convertView.findViewById(R.id.phone_number);
        mNumberAreaText = (TextView)convertView.findViewById(R.id.number_area);

        mPhoneNumberText.setText(number);

        if (mPhoneNumberHelper.canSendSmsTo(number)) {
            ImageView secondaryActionButton = (ImageView) convertView.findViewById(R.id.secondary_action_button);
            secondaryActionButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent itSecond = IntentFactory.newCreateSmsIntent(number);
                    itSecond.putExtra("is_privacy", AuroraPrivacyUtils.mCurrentAccountId);
                    startActivity(itSecond);
                }
            });
            secondaryActionButton.setImageResource(R.drawable.ic_message_24dp);
            secondaryActionButton.setVisibility(View.VISIBLE);
        }
        checkForBlack(number);
        mNumberType = 1;
        // Aurora xuyong 2016-01-15 added for aurora 2.0 new feature start
        updateAreaAndMarkUi();
        // Aurora xuyong 2016-01-15 added for aurora 2.0 new feature end
        // Aurora xuyong 2016-01-30 added for aurora 2.0 new feature start
        showActivity();
        // Aurora xuyong 2016-01-30 added for aurora 2.0 new feature end
    }
    // Aurora xuyogn 2016-01-14 added for aurora 2.0 new feature end
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

    /** Load the contact photos and places them in the corresponding views. */
    // gionee xuhz 20121126 add for GIUI2.0 start
    private void loadContactPhotosByPhotoId(long photoId) {
        // Place photo when discovered in data, otherwise show generic avatar
    	log("loadContactPhotos photoId="+photoId);
//        if (photoId != 0) {
//                mContactPhotoManager.loadPhoto(mPhotoView, photoId, false, false);
//        } else {
        	// gionee xuhz 20121208 modify for GIUI2.0 start

//        	  int simId = isSimCardPhoneNumber(this,mNumber);
//        	  if(simId > 0) { 
//        	      if (FeatureOption.MTK_GEMINI_SUPPORT) {
//        	          int iconId = ContactsUtils.getSimBigIcon(mContext, simId);
//        	          mPhotoView.setImageResource(iconId);
//        	      } else {
//        	    	  mPhotoView.setImageResource(R.drawable.aurora_sim_contact);
//        	      }
//        	  } else {  
//        		  mPhotoView.setImageResource(ContactPhotoManager.getDefaultAvatarResId(false, false));
                  // Aurora xuyong 2016-01-13 modified for aurora 2.0 new feature start
                  // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature start
//        		  mPhotoView.setImageResource(R.drawable.large_svg_dial_default_photo1);
                  // Aurora xuyong 2016-01-23 modified for aurora 2.0 new feature end
                  // Aurora xuyong 2016-01-13 modified for aurora 2.0 new feature end
//        	  }
        	
        	// gionee xuhz 20121208 modify for GIUI2.0 end
        	
//        	if (mIsRejectedDetail) {
//        		mPhotoView.setImageResource(R.drawable.aurora_reject_contact_default);
//            } else if(mIsPrivate || mIsPrivateUri) {
//            	mPhotoView.setImageResource(R.drawable.aurora_privacy_contact_default_header);
//            }
//        }       
    	
    	if(mCalllogEntries.size() > 0) {
    		Drawable d = mCalllogEntries.get(0).get(0).getThirdIcon();
    		if(d != null) {
    			mPhotoView.setImageDrawable(d);
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
       	String name = mName;
        if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail && mBlackName != null && !mBlackName.isEmpty()) {
        	name = mBlackName;                	
        } else  if (mContactUri == null) {        	
        	if(mCalllogEntries.size() > 0) {
        		name = mCalllogEntries.get(0).get(0).getAlternateContentDescription();        		
        	}      	
        	if(TextUtils.isEmpty(name)) {
        		name = mContext.getResources().getString(R.string.unknown_contact);
        	}
        }
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
	                if(!TextUtils.isEmpty(mUserMark)) {
	                	 name +=  "(" + mUserMark +")";
	                }
            }
        }
    	
    	if (!mIsRejectedDetail) {
    			area = mNumberArea;    		
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
		                Activity activity = AuroraCallDetailActivityV2.this;
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
        getSupportActionBar().setTitle(name);
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
      
        
        Drawable right = mContext.getResources().getDrawable(R.drawable.svg_dial_reject);
        int w = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_reject_icon_width);
		int h = mContext.getResources().getDimensionPixelSize(
                R.dimen.aurora_reject_icon_height);
		right.setBounds(0, 0, w, h);
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
	            Window window = dialog.getWindow();  
	            window.setWindowAnimations(R.style.aurora_dialog_anim); 
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
		AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this.mContext)
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
		
        Window window = dialog.getWindow();  
        window.setWindowAnimations(R.style.aurora_dialog_anim); 
        return dialog;
	}
	
	 private void gnCopyToClipboard() {
	        if (TextUtils.isEmpty(mNumber)) return;	        
			ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
			clipboard.setPrimaryClip(ClipData.newPlainText(null, mNumber));
	        String toastText = getString(R.string.toast_text_copied);
	        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
	    }
    
    
    public void onMenuCleanCallLog() {
    	AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
		.setTitle(R.string.gn_clearCallLogConfirmation_title)  // gionee xuhz 20120728 modify for CR00658189
		.setMessage(R.string.gn_clearAllCallLogConfirmation)
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
		.setNegativeButton(android.R.string.no, null).create();        
        Window window = dialog.getWindow();  
        window.setWindowAnimations(R.style.aurora_dialog_anim);
        dialog.show();
    }

    public void onMenuEditNumberBeforeCall(MenuItem menuItem) {
        startActivity(new Intent(Intent.ACTION_DIAL, mPhoneNumberHelper.getCallUri(mNumber)));
    }

    private void configureActionBar(boolean known) {
        Log.d(TAG, "configureActionBar = " +  known);
        //aurora change liguangyu 20131115 for BUG #728 start
        if (mIsRejectedDetail) {
            return;
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
        
      //The following lines are provided and maintained by Mediatek Inc.
        if (FeatureOption.MTK_GEMINI_SUPPORT || ContactsApplication.isMultiSimEnabled) {
            setSimIndicatorVisibility(false);        
			// gionee tianliang 20120925 modify for CR00692598 start
            mShowSimIndicator = false;
			// gionee tianliang 20120925 modify for CR00692598 end
        }
        //The previous lines are provided and maintained by Mediatek Inc.
        

        
        super.onPause();
    }

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SIMInfoWrapper.getDefault().unregisterForSimInfoUpdate(mHandler);
        if(FeatureOption.MTK_GEMINI_SUPPORT || ContactsApplication.isMultiSimEnabled) {
		// gionee tianliang 20120925 modify for CR00692598 start
            unregisterReceiver(mReceiver);
		// gionee tianliang 20120925 modify for CR00692598 end
        }
        if (ContactsApplication.sIsAuroraPrivacySupport) {
        	ContactsApplication.mPrivacyActivityList.remove(this);
        }
	     if(mBlackDialog != null) {
	    	 mBlackDialog.dismiss();
	    	 mBlackDialog = null;
	     }
	     
        if(mCallDialog != null) {
        	mCallDialog.dismiss();
        	mCallDialog = null;
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

    private static final int SIM_INFO_UPDATE_MESSAGE = 100;

    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SIM_INFO_UPDATE_MESSAGE:
                	// gionee xuhz 20120912 modify for CR00692279 start
    				Uri[] callEntryUris = getCallLogEntryUris();
    				if (callEntryUris != null) {
//        				if (callEntryUris.length > 1) {
        					updateData(callEntryUris[0]);
//        					updateHistoryList(callEntryUris);
//        				} else {
//        					updateData(callEntryUris);
//        				}
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

    
    private static final int AURORA_CALL_DETAIL_MORE = 1;
    
    private boolean mIsFristResume = true;
    
    private int[] getCallIdsByNumber(String number, int callType) {
    	String srotOrder = Calls.DATE + " DESC limit 1";
    	
    	//Gionee:huangzy 20120906 modify for CR00688166 start
    	/*String selection = Calls.NUMBER + "='" + number + "'";*/
    	number = number.replaceAll(" ", "");
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
    	Log.d(TAG,"phoneCallDetails:"+phoneCallDetails.length);
    	if (null == phoneCallDetails) {
    		return false;
    	}
    	
    	String path = GnStorageManager.getInstance(ContactsApplication.getInstance()).getInternalStoragePath();
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
				
//				ExpandingEntryCardView historyList = (AuroraListView) findViewById(R.id.history);
//				if (null != historyList && null != historyList.getAdapter()) {
//					((BaseAdapter)(historyList.getAdapter())).notifyDataSetChanged();
//				}
			}
			
		}.execute();
    }
    
    protected void gnSwitchUi(int numberType) {
    
	    switch (mNumberType) {
        case 0://Saved Number
        	configureActionBar(true);
                    


            break;
        case 1://not saved Number
        	configureActionBar(false);
           

            break;
        case 2://Number Unshow                
                   
    

            break;
        default:
            break;
        }
         	
	}
	

    private BroadcastReceiver mReceiver = new DialtactsBroadcastReceiver();
    private boolean mShowSimIndicator = false;
    private class DialtactsBroadcastReceiver extends BroadcastReceiver {
    @Override
   		public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        log("DialtactsBroadcastReceiver, onReceive action = " + action);
        if(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
            if(FeatureOption.MTK_GEMINI_SUPPORT || ContactsApplication.isMultiSimEnabled) {
                if (mShowSimIndicator) {
                    setSimIndicatorVisibility(true);
                }
            }
        }
        }
    }    
    
    
    private OnAuroraActionBarItemClickListener auroraActionBarItemClickListener = new OnAuroraActionBarItemClickListener() {
        public void onAuroraActionBarItemClicked(int itemId) {
            switch (itemId) {
            case AURORA_CALL_DETAIL_MORE:
//            	if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
//            		return;
//            	}
//            	
////                showAuroraMenu();
//            	if (GNContactsUtils.isMultiSimEnabled()) {
//            		gnSwitchUi(mNumberType);
//            	}
//            	showCustomMenu();
                break;
            default:
                break;
            }
        }
    };
    

    
    public static final int getTypeLabelResource(int type) {
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
	public int isSimCardPhoneNumber(Context context,String number)
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
	
	AuroraAlertDialog mBlackDialog = null;  

	private void addToBlack() {
		if (mBlackDialog != null) {
			mBlackDialog.dismiss();
			mBlackDialog = null;
		}

		final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(
				this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
				.setTitle(R.string.add_contact_to_black_title)
				.setMessage(R.string.add_black_message)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(R.string.add_black_action,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int whichButton) {
								com.android.contacts.ContactsUtils.addblack(AuroraCallDetailActivityV2.this, mNumber, "");		
				            	mHandler.sendMessage(mHandler.obtainMessage(SIM_INFO_UPDATE_MESSAGE));
							}
						});
		mBlackDialog = builder.create();
	    Window window = mBlackDialog.getWindow();  
        window.setWindowAnimations(R.style.aurora_dialog_anim);
		mBlackDialog.show();
	}
	
	private void removeFromBlack() {
//		View view = LayoutInflater.from(mContext).inflate(R.layout.black_remove, null);
//		final AuroraCheckBox checkBox = (AuroraCheckBox)view.findViewById(R.id.check_box);
//		checkBox.setChecked(true);
//
//		AuroraAlertDialog dialogs = new AuroraAlertDialog.Builder(
//				AuroraCallDetailActivityV2.this)
//				.setTitle(mContext.getResources().getString(R.string.black_remove))
//				.setView(view)
//				.setPositiveButton(android.R.string.ok,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int whichButton) {
//								boolean recoveryLogs = checkBox.isChecked();
//								int isblack = 0;
//								if (!recoveryLogs) {
//									isblack = -1;
//								}
//								
//								ContentValues values = new ContentValues();
//								values.put("isblack", isblack);
//								values.put("number", mNumber);
//								values.put("reject", 0);
//				            	mContext.getContentResolver().update(Uri.withAppendedPath(GnContactsContract.AUTHORITY_URI, "black"), values, GNContactsUtils.getPhoneNumberEqualString(mNumber), null);
//				                values.clear();
//				                
//				                mIsShowRejectFlag = false;
//				                mPhoneNumberText.setCompoundDrawables(null, null, null, null);
//				                gnSwitchUi(mNumberType);
//							}
//						})
//				.setNegativeButton(android.R.string.cancel,
//						new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog,
//									int whichButton) {
//								dialog.dismiss();
//							}
//						}).show();
		
		ContentValues values = new ContentValues();
		values.put("isblack", 0);
		values.put("number", mNumber);
		values.put("reject", 0);
    	mContext.getContentResolver().update(Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "black"), values, GNContactsUtils.getPhoneNumberEqualString(mNumber), null);
        values.clear();
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ADD_MARK: {
            	if (data != null) {
            		mUserMark = data.getStringExtra("user_mark");
            		YuloreUtils.getInstance(mContext).insertUserMark(mContext, mNumber, mUserMark);
            		Log.d(TAG, "ADD_MARK mUserMark = " + mUserMark + "  mNumber = " + mNumber + " mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);
                	
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
					Uri blackUri = Uri.withAppendedPath(GnContactsContract.AUTHORITY_URI, "black");
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
	
	
	private boolean mIsPrivate = false;
	private boolean mIsPrivateUri = false;
	
	
	
    final ExpandingEntryCardViewListener mExpandingEntryCardViewListener
    		= new ExpandingEntryCardViewListener() {
					@Override
					public void onCollapse(int heightDelta) {
					    mScroller.prepareForShrinkingScrollChild(heightDelta);
					}
					
					@Override
					public void onExpand(int heightDelta) {
					    mScroller.prepareForExpandingScrollChild();
					}
};
	
	final MultiShrinkScrollerListener mMultiShrinkScrollerListener = new MultiShrinkScrollerListener() {
		@Override
		public void onScrolledOffBottom() {
			finish();
		}

		@Override
		public void onEnterFullscreen() {
			updateStatusBarColor();
		}

		@Override
		public void onExitFullscreen() {
			updateStatusBarColor();
		}

		@Override
		public void onStartScrollOffBottom() {
			mIsExitAnimationInProgress = true;
		}

		@Override
		public void onEntranceAnimationDone() {
		     mTransparentView.setEnabled(true);
			mIsEntranceAnimationFinished = true;
			  invalidateOptionsMenu();
		}

		@Override
		public void onTransparentViewHeightChange(float ratio) {
	          if (mIsEntranceAnimationFinished && mIsExitAnimationFinished) {
	                mWindowScrim.setAlpha((int) (0xFF * ratio));
	            }
		}
		
		  @Override
	        public void onStartEnterDimAnimator(int duration) {
	            final Interpolator interpolator = AnimationUtils.loadInterpolator(mContext,
	          		  android.R.interpolator.ease_cubic);
		        ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", 0, (int)(255*0.3f));
		        o.setDuration(duration);
		        o.setInterpolator(interpolator);
			    o.start();
	        }
	        
	        @Override
	        public void onStartExitDimAnimator(int duration) {
	        	mIsExitAnimationFinished = false;
	            final Interpolator interpolator = AnimationUtils.loadInterpolator(mContext,
	          		  android.R.interpolator.ease_cubic_hide);
		        ObjectAnimator o = ObjectAnimator.ofInt(mWindowScrim, "alpha", mWindowScrim.getAlpha(), 0);
		        o.setDuration(duration);
		        o.setInterpolator(interpolator);
			    o.start();
	        }
		  
	};
    private ColorDrawable mWindowScrim;
    private boolean mIsEntranceAnimationFinished;
    private boolean mIsExitAnimationFinished = true;
	private boolean mIsExitAnimationInProgress;
    private boolean mHasAlreadyBeenOpened;
    private int mExtraMode;
    public static final int MODE_FULLY_EXPANDED = 4;
    private MultiShrinkScroller mScroller;
	  @Override
	    public void onBackPressed() {
	        if (mScroller != null) {
	            if (!mIsExitAnimationInProgress) {
	                mScroller.scrollOffBottom();
	            }
	        } else {
	            super.onBackPressed();
	        }
	    }
	  
	    private void runEntranceAnimation() {
	        if (mHasAlreadyBeenOpened) {
	            return;
	        }
	        mHasAlreadyBeenOpened = true;
	        mScroller.scrollUpForEntranceAnimation(mExtraMode != MODE_FULLY_EXPANDED);
	    }
	    
	    private void showActivity() {
	    	   Log.e(TAG, "showActivity");   
	        if (mScroller != null) {
	            mScroller.setVisibility(View.VISIBLE);
	            SchedulingUtils.doOnPreDraw(mScroller, /* drawNextFrame = */ false,
	                    new Runnable() {
	                        @Override
	                        public void run() {
	                            runEntranceAnimation();
	                        }
	                    });
	        }
	    }
	    private static final String KEY_THEME_COLOR = "theme_color";
	    private static final int DEFAULT_SCRIM_ALPHA = 0xFF;
	    private static final int SCRIM_COLOR = Color.argb(DEFAULT_SCRIM_ALPHA, 0, 0, 0);
	    
	    private View mTransparentView;
	    private QuickContactImageView mPhotoView, mBlurPhotoView;
	    private ImageView mSmallPhotoView;
	    private List<List<PhoneCallRecord>> mPhoneRecords = new ArrayList<>();
	    private List<List<Entry>> mCalllogEntries = new ArrayList<>();
	    
	    private List<List<Entry>> buildCalllogEntries() {
	    	   final List<List<Entry>> interactionsWrapper = new ArrayList<>();
	    	   foundAllPhoneRecords() ;
	    	  final List<CallLogInteraction> allInteractions = getCallLogInteractions();
	        for (Entry CallLogInteraction : contactInteractionsToEntries(allInteractions)) {
                List<Entry> entryListWrapper = new ArrayList<>(1);
                entryListWrapper.add(CallLogInteraction);
                interactionsWrapper.add(entryListWrapper);
            }
	        return interactionsWrapper;
	    }
	    
	    private List<CallLogInteraction> getCallLogInteractions() {
	    	String phoneNumber = mCallNumber;
	    	
	        final String normalizedNumber = PhoneNumberUtils.normalizeNumber(phoneNumber);
	        // If the number contains only symbols, we can skip it
	        if (TextUtils.isEmpty(normalizedNumber)) {
	            return Collections.emptyList();
	        }
	        final Uri uri = Uri.withAppendedPath(Calls.CONTENT_FILTER_URI,
	                Uri.encode(normalizedNumber));
	        // Append the LIMIT clause onto the ORDER BY clause. This won't cause crashes as long
	        // as we don't also set the {@link android.provider.CallLog.Calls.LIMIT_PARAM_KEY} that
	        // becomes available in KK.

//	       StringBuilder selection = new StringBuilder();
//	        int[] ids=getIntent().getIntArrayExtra("ids");
//        	selection.append("_id in (");
//	        if(1 == mCallsCount && mCallId != 0) {
//			    selection.append("'").append(mCallId).append("'");
//	        } else {
//				for (int id : ids) {
//					selection.append("'").append(id).append("',");
//				}
//				selection.setLength(selection.length() - 1);
//	        }
//			selection.append(")");
	        final String orderByAndLimit = Calls.DATE + " DESC LIMIT 4";
	        final Cursor cursor = mContext.getContentResolver().query(uri, null, null, null,
	                orderByAndLimit);
	        try {
	            if (cursor == null || cursor.getCount() < 1) {
	                return Collections.emptyList();
	            }
	            cursor.moveToPosition(-1);
	            List<CallLogInteraction> interactions = new ArrayList<>();
	            while (cursor.moveToNext()) {
	                final ContentValues values = new ContentValues();
	                AuroraDatabaseUtils.cursorRowToContentValues(cursor, values);
	                interactions.add(new CallLogInteraction(values, mAllRecords));
	            }
	            return interactions;
	        } finally {
	            if (cursor != null) {
	                cursor.close();
	            }
	        }
	    }
	    
	    private List<Entry> contactInteractionsToEntries(List<CallLogInteraction> interactions) {
	        final List<Entry> entries = new ArrayList<>();
	        int recordIndex = 0;
	        for (CallLogInteraction interaction : interactions) {
	        	mPhoneRecords.add(recordIndex, interaction.getPhoneRecords());
	            entries.add(new Entry(/* id = */ recordIndex,
	                    null,  //Drawable icon,
	                    interaction.getViewHeader(this), //String header
	                    interaction.getViewBody(this), // String subHeader
	                    interaction.getBodyIcon(this), //Drawable subHeaderIcon
	                    interaction.getViewFooter(this), //String text
	                    interaction.getFooterIcon(this), //Drawable textIcon
	                    null, //Drawable alternateIcon
	                    interaction.getRecordIntent(), //Intent alternateIntent
	                    /* alternateIcon = */ getResources().getDrawable(R.drawable.gn_play_call_record_light),
	                    /* alternateIntent = */ interaction.getRecordIntent(),
	                    /* alternateContentDescription = */ interaction.getCachedName(),
	                    /* shouldApplyColor = */interaction.isMissedCall() ? true : false ,
	                    /* isEditable = */ false,
	                    /* EntryContextMenuInfo = */ null,
	                    /* thirdIcon = */ interaction.getPhoto(),
	                    /* thirdIntent = */ null,
	                    /* thirdContentDescription = */ null,
	                    interaction.getIconResourceId()));
	            recordIndex ++;
//	            Entry(int id, Drawable mainIcon, String header, String subHeader,
//	                    Drawable subHeaderIcon, String text, Drawable textIcon,
//	                    String primaryContentDescription, Intent intent,
//	                    Drawable alternateIcon, Intent alternateIntent, String alternateContentDescription,
//	                    boolean shouldApplyColor, boolean isEditable,
//	                    EntryContextMenuInfo entryContextMenuInfo, Drawable thirdIcon, Intent thirdIntent,
//	                    String thirdContentDescription, int iconResourceId)
	        }
	        return entries;
	    }
	    
	    
	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	
	      	if (ContactsApplication.sIsAuroraRejectSupport && mIsRejectedDetail) {
        		return false;
        	}
	        final MenuInflater inflater = getMenuInflater();
  	       inflater.inflate(R.menu.aurora_call_detail, menu);     
	
	        return true;
	    }

	    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		boolean known = false;
//		switch (mNumberType) {
//		case 0:// Saved Number
//			known = true;
//			break;
//		case 1:// not saved Number
//			known = false;
//			break;
//		default:
//			return false;
//		}

		Log.i(TAG, "onCreateOptionsMenu known = " + known);
		final MenuItem contactInfoMenuItem = menu
				.findItem(R.id.menu_check_contact_info);
		final MenuItem removeBlackMenuItem = menu
				.findItem(R.id.menu_remove_black);
		final MenuItem addBlackMenuItem = menu.findItem(R.id.menu_add_black);
		
		checkForBlack(mCallNumber);
		
		if (ContactsApplication.sIsAuroraRejectSupport) {
			if (mIsShowRejectFlag) {
				removeBlackMenuItem.setVisible(true);
				addBlackMenuItem.setVisible(false);
			} else {
				addBlackMenuItem.setVisible(true);
				removeBlackMenuItem.setVisible(false);
			}
		}
		final MenuItem editMarkMenuItem = menu.findItem(R.id.menu_edit_mark);
		final MenuItem addMarkMenuItem = menu.findItem(R.id.menu_add_mark);
		final MenuItem addNewContactMenuItem = menu.findItem(R.id.menu_add_new_contact);
		final MenuItem addExistContactMenuItem = menu.findItem(R.id.menu_add_exist_contact);
		addNewContactMenuItem.setIcon(R.drawable.aurora_add_contact);
		addNewContactMenuItem.setTitle(R.string.menu_addToContact);
		if (known) {			
			addNewContactMenuItem.setVisible(false);
			addExistContactMenuItem.setVisible(false);
			editMarkMenuItem.setVisible(false);
			addMarkMenuItem.setVisible(false);
			contactInfoMenuItem.setVisible(true);
		} else {
			addNewContactMenuItem.setVisible(true);
			addExistContactMenuItem.setVisible(true);
			if (ContactsApplication.sIsAuroraRejectSupport) {
				if (mUserMark == null) {
					addMarkMenuItem.setVisible(true);
					editMarkMenuItem.setVisible(false);
				} else {
					editMarkMenuItem.setVisible(true);
					addMarkMenuItem.setVisible(false);
				}
			}
			contactInfoMenuItem.setVisible(false);
		}
		final MenuItem removeCallLogMenuItem = menu
				.findItem(R.id.menu_clean_call_log);
		removeCallLogMenuItem.setVisible(false);
		addExistContactMenuItem.setVisible(false);

		return true;
	}

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.menu_check_contact_info:	

                	if (mContactUri != null) {
                		Intent intent = IntentFactory.newViewContactIntent(mContactUri);
                		if(mIsPrivate || mIsPrivateUri) {
                			intent.putExtra("is_privacy_contact", true);
                		}
                    	startActivity(intent);
                    	mNeedReQuery = true;
                    }
                
	                return true;
	            case R.id.menu_add_new_contact:	 
	               	startActivity(IntentFactory.newCreateContactIntent(mNumber));
	               	isNoFinishAnim = false;
	               	finish();
                	mNeedReQuery = true;
	                return true;
	            case R.id.menu_add_exist_contact:	 
	            	startActivity(IntentFactory.newInsert2ExistContactIntent(mNumber));
            		mNeedReQuery = true;
	                return true;
	            case R.id.menu_clean_call_log:
	            	mNeedReQuery = true;
	             	onMenuCleanCallLog();
	                return true;
	            case R.id.menu_add_black:	 
	            	addToBlack();
	                return true;
	            case R.id.menu_remove_black:
	            	removeFromBlack();
	            	mHandler.sendMessage(mHandler.obtainMessage(SIM_INFO_UPDATE_MESSAGE));
	                return true;
	            case R.id.menu_edit_mark:	{

//                	Intent intent = new Intent(AuroraCallDetailActivityV2.this, AuroraMarkActivity.class);
//                	intent.putExtra("user_mark", mUserMark);
//                	startActivityForResult(intent, EDIT_MARK);
//	            	showAddMarkPopupWindow(AuroraCallDetailActivityV2.this.findViewById(R.id.multiscroller));	            	
	            	AuroraAlertDialog dialog = (AuroraAlertDialog) createAddMarkDialog();
		    		dialog.show();
	            }
                
	                return true;
	            case R.id.menu_add_mark:	{

//                	Intent intent = new Intent(AuroraCallDetailActivityV2.this, AuroraMarkActivity.class);
//                	startActivityForResult(intent, ADD_MARK);
                	
//                	showAddMarkPopupWindow(AuroraCallDetailActivityV2.this.findViewById(R.id.multiscroller));
	             	AuroraAlertDialog dialog = (AuroraAlertDialog) createAddMarkDialog();
	    		    		dialog.show();
	            }
                
	                return true;
	            case android.R.id.home:           
	                onBackPressed();
	                return true;
	            default:
	                return super.onOptionsItemSelected(item);
	        }
	    }  
	    
	    public boolean dispatchKeyEvent(KeyEvent event) {
	  		Log.v(TAG, "dispatchKeyEvent dispatchKeyEvent  "); 
	        if(event.getKeyCode() == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_UP) {
	      	   Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
	       	   if(toolbar.isOverflowMenuShowing()) {
	       		   toolbar.hideOverflowMenu();
	       	   } else {
	       		   toolbar.showOverflowMenu();
	       	   }
	      	   return true;
	      	   
	        }
	    return super.dispatchKeyEvent(event);
	}
	    
	    private ArrayList<String> mMarkItemList;
	    private AuroraAlertDialog createAddMarkDialog() {
			mMarkItemList = new ArrayList<String>();		 
			mMarkItemList.add(getResources().getString(R.string.aurora_no_mark));
	        Cursor cursor = getContentResolver() .query(mMarkUri, null, "lable is not null and number is null", null, null);
	        int count = 0;
	        if(cursor != null) {
	        	if(cursor.moveToFirst()) {
		        	while(cursor.moveToNext()) {
		        		count ++;
		        		mMarkItemList.add(cursor.getString(1));
		        	}
	        	}
	        	cursor.close();
	        }
	        mMarkItemList.add(getResources().getString(R.string.new_mark));
   
	        final int last = count + 1;
	        log("last = " + last);
	        CharSequence[] items = mMarkItemList.toArray(new CharSequence[mMarkItemList.size()]);        
	        AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this.mContext)
	        .setTitle(getResources().getString(R.string.add_number_mark))
	        .setItems(items, new DialogInterface.OnClickListener() {			
				@Override
				public void onClick(DialogInterface dialog, int which) {			
				    log("which = " + which);
					if(which == 0) {
						onDeleteMark();
					} else if(which == last) {
						createNewMark();
					} else {
						mSelectedMark = mMarkItemList.get(which);
			    		onEditOrAddMark(mSelectedMark);
					}
				}
			})
			.setTitleDividerVisible(true)
	        .setCancelIcon(true)
	        .create();
			Window window = dialog.getWindow();  
	        window.setWindowAnimations(R.style.aurora_dialog_anim);
	        return dialog;
		}
	    
	    private ArrayList<String> mMarkList = new ArrayList<String>();
		private String mSelectedMark = null;
		Uri mMarkUri = Uri.parse("content://com.android.contacts/mark");
	    private void showAddMarkPopupWindow(View view) {
	    	LinearLayout mNewMarkLayout;
	    	LinearLayout mNoMarkLayout;
	  
	        View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.aurora_popup_menu, null);	        
	        
	        AuroraListView menulist = (AuroraListView)contentView.findViewById(R.id.menu_list);
		    final MarkAdapter adapter = new MarkAdapter(this);
	        final PopupWindow popupWindow = new PopupWindow(contentView,
	                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
	        
	        mNoMarkLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.aurora_popup_menu_item, null);
		    TextView mNoMarkCTV = (TextView) mNoMarkLayout.findViewById(R.id.popup_menu_item);
		    String mNoMarkStr = mContext.getResources().getString(R.string.aurora_no_mark);
		    mNoMarkCTV.setText(mNoMarkStr);
			mNoMarkLayout.setOnClickListener(new View.OnClickListener() {
			    	@Override
		            public void onClick(View v) {
			    		onDeleteMark();
			     		adapter.changeCursor(null);
		        	 	popupWindow.dismiss();
			    	}
			    });
			menulist.addHeaderView(mNoMarkLayout);
			
			
			   mNewMarkLayout = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.aurora_popup_menu_item, null);
			   TextView createMarkCTV = (TextView) mNewMarkLayout.findViewById(R.id.popup_menu_item);
			    String createMarkStr = mContext.getResources().getString(R.string.new_mark);
			    createMarkCTV.setText(createMarkStr);
			    mNewMarkLayout.setOnClickListener(new View.OnClickListener() {
			    	@Override
		            public void onClick(View v) {
			    		createNewMark();
			      		adapter.changeCursor(null);
		        	 	popupWindow.dismiss();
			    	}
			    });
			    menulist.addFooterView(mNewMarkLayout);
		
	 
			 
	        menulist.setAdapter(adapter);
	    	
	        Cursor cursor = getContentResolver() .query(mMarkUri, null, "lable is not null and number is null", null, null);
//	        if(cursor == null) return;
//		    if(cursor.getCount() == 0) {
//		    	cursor.close();
//		    	return;
//		   }
	
        	adapter.changeCursor(cursor);       	      
	        
	        menulist.setOnItemClickListener(new OnItemClickListener() {
	        	@Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        		mSelectedMark = adapter.getMark(position -1);
//	        		setCheckedPosition(position);
		    		onEditOrAddMark(mSelectedMark);
	        		adapter.changeCursor(null);
	        	 	popupWindow.dismiss();
	        	}
			});

	        popupWindow.setTouchable(true);
	        
	        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);
	      
	  
	        popupWindow.setBackgroundDrawable(new BitmapDrawable());

	        popupWindow.showAtLocation (view, Gravity.BOTTOM, 0, 0 );

	    }
	    
	    private void createNewMark() {
			View view = LayoutInflater.from(mContext).inflate(R.layout.aurora_new_mark_dia, null);
			  final AuroraEditText mark_content=(AuroraEditText) view.findViewById(R.id.mark_content);
			  mark_content.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
			  final AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(mContext)
	          .setTitle(mContext.getResources().getString(R.string.new_mark))
	          .setView(view)
	          .setPositiveButton(android.R.string.ok,
	              new DialogInterface.OnClickListener() {
	                  @Override
	                  public void onClick(DialogInterface dialog, int whichButton) {
	                  	 String content = mark_content.getText().toString();
	                  	 mSelectedMark = content;
	                  	 if (!content.equals("")) {
	                  		 if (content != null && content.replaceAll(" ", "").equals("")) {
	                  			Toast.makeText(mContext, mContext.getResources().getString(R.string.aurora_mark_error), Toast.LENGTH_SHORT).show();
	                  			return;
	                  		 }
	                  		 
	                  		 if (!mMarkList.contains(content)) {
	                       		ContentResolver cr = getContentResolver();
	                    			ContentValues cv = new ContentValues();
	                    			cv.put("lable", content);
	                    			Uri uri2 = cr.insert(mMarkUri, cv);
	                    			onEditOrAddMark(mSelectedMark);
	                    			try {
	                    				Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing" );
		                  			    field.setAccessible( true );
	                 			    	field.set(dialog, true );
	                 			    } catch(Exception e) {
	                 			    	e.printStackTrace();
	                 			    }	                    			
	                          		
	                  		 } else {
	                  		    Toast.makeText(mContext, mContext.getResources().getString(R.string.mark_content_exist), Toast.LENGTH_SHORT).show();
	                  			try {
	                  				Field field = dialog.getClass()
		                  			            .getSuperclass().getDeclaredField(
		                  			                     "mShowing" );
		                  			    field.setAccessible( true );
	                 			    	field.set(dialog, false );
	                 			} catch(Exception e) {
	                 			    	
	                 			}
	                  		 }
	                  	 }
	                  	 dialog.dismiss();
	                  }
	              }
	          )
	          .setNegativeButton(android.R.string.cancel,   
	          		new DialogInterface.OnClickListener() {
	              @Override
	              public void onClick(DialogInterface dialog, int whichButton) {
	              	try{
	              	 Field field = dialog.getClass()
	    			            .getSuperclass().getDeclaredField(
	    			                     "mShowing" );
	    			    field.setAccessible( true );
	    			    
	    			    	field.set(dialog, true );
	    			    }catch(Exception e){
	    			    	
	    			    }
	              	
	              	dialog.dismiss();
	              }
	          }).create();
		    	Window window = dialog.getWindow();  
		        window.setWindowAnimations(R.style.aurora_dialog_anim);
		        dialog.show();
			  dialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE).setEnabled(false);
			  
			  mark_content.addTextChangedListener(new TextWatcher() {
					
					@Override
					public void onTextChanged(CharSequence s, int start, int before, int count) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void beforeTextChanged(CharSequence s, int start, int count,
							int after) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						if (s != null && s.length() > 0) {
							dialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE).setEnabled(true);
						} else {
							dialog.getButton(AuroraAlertDialog.BUTTON_POSITIVE).setEnabled(false);
						}
					}
				  });
			  
			  dialog.setCanceledOnTouchOutside(false);
			  dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE) ;
		}
	    
	    private class MarkAdapter extends CursorAdapter {
			private LayoutInflater mInflater;
			private Context context;
			
			public MarkAdapter(Context context) {
				super(context, null, false);
				this.context=context;
				mInflater = LayoutInflater.from(context);
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (convertView == null) {
	            	convertView = mInflater.inflate(R.layout.aurora_popup_menu_item, parent, false);
	            }
				
				Cursor cursor = getCursor();
				if (position <= cursor.getCount()) {
					cursor.moveToPosition(position);
				}
				
				bindView(convertView, mContext, cursor);
				
				return convertView;
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				if (null == view) {
					return;
				}
				
				 TextView text = (TextView) view.findViewById(R.id.popup_menu_item);
				
				int position = cursor.getPosition();
				String mark = cursor.getString(1);
				
				if (mark != null && !mMarkList.contains(mark)) {
					mMarkList.add(mark);
				}
				
				text.setText(mark);
				
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				CheckedTextView view;
				view = (CheckedTextView) mInflater.inflate(R.layout.aurora_popup_menu_item, parent, false);
				return view;
			}
			
			public String getMark(int position) {
				Cursor cursor = (Cursor) getItem(position);
		        if (cursor == null) {
		            return null;
		        }
		        
		        String result = null;
		        try {
		        	result = cursor.getString(1);
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
		        
		        return result;
			}

		}
	    
	    
	    private void onEditOrAddMark(String mark){
	    	mUserMark = mark;
			YuloreUtils.getInstance(mContext).insertUserMark(mContext, mNumber, mUserMark);
			updateCallsAndBlackMark();
		 	updateAreaAndMarkUi();
	    }	    
	    
	    private void onDeleteMark() {
	    	mUserMark = null;
	    	
			YuloreUtils.getInstance(mContext).deleteUserMark(mContext, mNumber);
			mIsNoUserMark = true;
			
			new Thread() {
    			public void run() {
    				mMarkContent = YuloreUtils.getInstance(mContext).getMarkContent(mNumber,mContext);
        			mMarkCount = YuloreUtils.getInstance(mContext).getMarkNumber(mContext, mNumber);
            		Log.d(TAG, "EDIT_MARK mUserMark = " + mUserMark + "  mNumber = " + mNumber + " mMarkContent = " + mMarkContent + "  mMarkCount = " + mMarkCount);
                	updateCallsAndBlackMark();
    			}
    		}.start();
         	updateAreaAndMarkUi();
    		
	    }
	    
	    private boolean isNoFinishAnim = true;
	    @Override
	    public void finish() {
	        super.finish();

	        // override transitions to skip the standard window animations
	        if(isNoFinishAnim) {
	        	overridePendingTransition(0, 0);
	        }
	    }
	    
	    private boolean mIsCallHistoryEmpty =  false;
	    final OnClickListener mEntryClickHandler = new OnClickListener() {
	        @Override
	        public void onClick(View v) {
	            Log.w(TAG, "mEntryClickHandler onClick ");
	            final Object entryTagObject = v.getTag();
	            if (entryTagObject == null || !(entryTagObject instanceof EntryTag)) {
	                Log.w(TAG, "EntryTag was not used correctly");
	                return;
	            }
	            final EntryTag entryTag = (EntryTag) entryTagObject;
	            final Intent intent = entryTag.getIntent();
	            final int dataId = entryTag.getId();
	            
	  		  Log.d(TAG, "dataId = " + dataId);
	            if("PLAY_RECORD_ACTION".equals(intent.getAction())) {
	            	try {
	            		playRecord(mPhoneRecords.get(dataId));
	            	} catch (Exception e) {
	            		e.printStackTrace();
	            	}
	            	return;
	            }
	            
	        }
	    };
	    
	    private void playRecord(final List<PhoneCallRecord> records) {
		    Log.d(TAG, "playRecord ");
	    	if (null == records) {
	    		return;
	    	}
	    	
	    	final int size = records.size();
	    	
	    	if (size == 1) {
	    		playRecord(records.get(0));
	    		return;
	    	}
	    	
	    	CharSequence[] items = new CharSequence[size];
	    	for (int i = 0; i < items.length; i++) {
	    		items[i] = new File(records.get(i).getPath()).getName().substring(0, 13) + ".3gpp";
	    	}
	    	AuroraAlertDialog dialog = new AuroraAlertDialog.Builder(this)
		    	.setTitle(R.string.gn_phone_call_record_tile).setTitleDividerVisible(true)
		    	.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						playRecord(records.get(which));
					}
				})
	    	.create();
	    	Window window = dialog.getWindow();  
	        window.setWindowAnimations(R.style.aurora_dialog_anim);
	        dialog.show();
	    }
		
	    private void playRecord(PhoneCallRecord record) {
	        Log.d(TAG, "playRecord single");
	    	if (null == record) {
	    		return;
	    	}
	    	
	    	Uri data = Uri.fromFile(new File(record.getPath()));  
	        Intent intent = new Intent(Intent.ACTION_VIEW);  
	        intent.setClassName("com.android.music", "com.android.music.AudioPreview");
	        intent.setDataAndType(data, record.getMimeType());  
	        startActivity(intent);
	    }
	    
	    private  ArrayList<PhoneCallRecord> mAllRecords = new ArrayList<PhoneCallRecord>();
	    
	    private void foundAllPhoneRecords() {    	
	     	String path = GnStorageManager.getInstance(ContactsApplication.getInstance()).getInternalStoragePath();
	     	if (path == null) {
	     		return;
	     	}
	     	
	     	mAllRecords = new ArrayList<PhoneCallRecord>();
	     	String historyPath = path + "/" + ContactsApplication.getInstance().getString(R.string.aurora_call_record_history_path);    	
	     	int found = 0;    	
	     	parseRecording(mAllRecords, historyPath, false);
	     	
	     	if (ContactsApplication.sIsAuroraPrivacySupport && AuroraPrivacyUtils.mCurrentAccountId > 0) {
	     		historyPath = AuroraPrivacyUtils.mCurrentAccountHomePath
	                     + Base64.encodeToString(("audio").getBytes(), Base64.URL_SAFE);
	     		historyPath = ContactsUtils.replaceBlank(historyPath);
	     		parseRecording(mAllRecords, historyPath, true);
	     	}
	     	
	     }
	    
	    private void updateStatusBarColor() {
		      if (mScroller.getScrollNeededToBeFullScreen() <= 0) {
		      		com.aurora.utils.SystemUtils.switchStatusBarColorMode(com.aurora.utils.SystemUtils.STATUS_BAR_MODE_WHITE, this);
		      } else {
		      		com.aurora.utils.SystemUtils.switchStatusBarColorMode(com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
		      }
	    }

}

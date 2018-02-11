/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.contacts.dialpad;


import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import aurora.app.AuroraAlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.StatusBarManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.CallLog.Calls;
import android.provider.Contacts.Intents.Insert;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import android.provider.ContactsContract.Contacts;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.text.method.NumberKeyListener;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import aurora.widget.AuroraListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.ResConstant;
import com.android.contacts.SimpleAsynTask;
import com.android.contacts.activities.AuroraCallRecordActivity;
import com.android.contacts.activities.AuroraDialtactsActivityV2;
import com.android.contacts.activities.ContactsLog;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.android.contacts.list.ProviderStatusLoader;
import com.android.contacts.list.ProviderStatusLoader.ProviderStatusListener;
import com.android.contacts.util.ChangeStatusBar;
import com.android.contacts.util.Constants;
//Gionee:huangzy 20120710 add for CR00614809 start
//import com.android.contacts.util.EditTextControler.EditTextDeleter;
//import com.android.contacts.util.EditTextControler.EditTextSwitchAdder;
import com.android.contacts.util.EditTextSwitchAdder;
//Gionee:huangzy 20120710 add for CR00614809 end
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneCapabilityTester;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.internal.telephony.ITelephony;
import com.android.contacts.util.CallLogAsync;
import com.android.contacts.util.HapticFeedback;
import com.mediatek.contacts.SpecialCharSequenceMgrProxy;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.activities.SpeedDialManageActivity;
import com.mediatek.contacts.dialpad.AuroraDialerSearchController;
import com.mediatek.contacts.dialpad.AuroraDialerSearchController.DialerSearchResult;
import com.mediatek.contacts.dialpad.AuroraDialerSearchController.OnDialerSearchResult;
//Gionee:huangzy 20121011 add for CR00710695 start
import com.mediatek.contacts.dialpad.AuroraDialerSearchController;
import com.mediatek.contacts.dialpad.IDialerSearchController;
import com.mediatek.contacts.dialpad.IDialerSearchController.OnDialerSearchListener;
//Gionee:huangzy 20121011 add for CR00710695 end
import com.mediatek.contacts.dialpad.SpeedDial;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.SimAssociateHandler;

import android.provider.ContactsContract.PhoneLookup;



//Gionee zhanglina 2012-07-10 add for CR00640388 begin
import android.os.SystemProperties;
//Gionee zhanglina 2012-07-10 add for CR00640388 end
//GIONEE:liuying 2012-7-5 modify for CR00637517 start



import java.util.List;

import org.json.JSONObject;







//GIONEE:liuying 2012-7-5 modify for CR00637517 end
//Gionee:xuhz 20121014 add for CR00686812 start
import android.widget.ViewFlipper;
//Gionee:xuhz 20121014 add for CR00686812 end
import android.util.DisplayMetrics;



import com.aurora.android.contacts.AuroraSubInfoNotifier;
//Gionee:huangzy 20121226 add for CR00667771 begin
import com.aurora.android.contacts.GnAutoScaleTextSizeWatcher;
//Gionee:huangzy 20121226 add for CR00667771 end







// gionee xuhz 20121225 add for show popup window when key down start
import android.view.Gravity;
import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;
import android.graphics.Typeface;

//gionee xuhz 20121225 add for show popup window when key down end
import com.aurora.android.contacts.AuroraTelephonyManager;

import aurora.widget.AuroraEditText;
import 	android.content.res.AssetFileDescriptor;

import java.io.IOException;

import android.media.SoundPool;
import android.os.Build;

import com.yulore.framework.fm.HomeFragment;

import android.telephony.*;
/**
 * Fragment that displays a twelve-key phone dialpad.
 */
public class AuroraDialpadFragmentV2 extends Fragment
        implements View.OnClickListener,
        View.OnLongClickListener, View.OnKeyListener,
        AdapterView.OnItemClickListener, TextWatcher,
        ProviderStatusListener,
        OnScrollListener, OnDialerSearchResult, OnDialerSearchListener, OnTouchListener, AuroraSubInfoNotifier.SubInfoListener {

    private static final String TAG = AuroraDialpadFragmentV2.class.getSimpleName();

    private static final String EMPTY_NUMBER = "";

    /** The length of DTMF tones in milliseconds */
    private static final int TONE_LENGTH_MS = 150;

    /** The DTMF tone volume relative to other sounds in the stream */
    //aurora change liguangyu 20131212 start
    private static final int TONE_RELATIVE_VOLUME = 60;
    //aurora change liguangyu 20131212 end

    /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys */
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_DTMF;

    private View fragmentView = null;
    private static final boolean DBG = true;
    private AuroraEditText mDigits;

    private View mDelete, mDeleteMsim;
//    private ToneGenerator mToneGenerator;
//    private Object mToneGeneratorLock = new Object();
    private View mDigitKeyboard;
    private View mYellowPages;

    private View mSingleDialLine, mDoubleDialLine;
    private View mDialButton, mDialButton0, mDialButton1;
    private AuroraListView mDialpadChooser;
    private DialpadChooserAdapter mDialpadChooserAdapter;
    private View mDialpadBg;

    //Gionee:xuhz 20121014 add for CR00686812 start
    //Gionee <huangzy> <2013-04-13> add for CR00786343 start
    private int mDialpadFlipperHeight;
    //Gionee <huangzy> <2013-04-13> add for CR00786343 end
    private View mQwertKeyboard;
    //Gionee:xuhz 20121014 add for CR00686812 end
    /**
     * Regular expression prohibiting manual phone call. Can be empty, which means "no rule".
     */
    private String mProhibitedPhoneNumberRegexp;

    // Last number dialed, retrieved asynchronously from the call DB
    // in onCreate. This number is displayed when the user hits the
    // send key and cleared in onPause.
     
    private CallLogAsync mCallLog = new CallLogAsync();
    private String mLastNumberDialed = EMPTY_NUMBER;

    // determines if we want to playback local DTMF tones.
    private boolean mDTMFToneEnabled;

    // Vibration (haptic feedback) for dialer key presses.
    private HapticFeedback mHaptic = new HapticFeedback();
    
    /** Identifier for the "Add Call" intent extra. */
    private static final String ADD_CALL_MODE_KEY = "add_call_mode";

    /**
     * Identifier for intent extra for sending an empty Flash message for
     * CDMA networks. This message is used by the network to simulate a
     * press/depress of the "hookswitch" of a landline phone. Aka "empty flash".
     *
     * TODO: Using an intent extra to tell the phone to send this flash is a
     * temporary measure. To be replaced with an ITelephony call in the future.
     * TODO: Keep in sync with the string defined in OutgoingCallBroadcaster.java
     * in Phone app until this is replaced with the ITelephony API.
     */
    private static final String EXTRA_SEND_EMPTY_FLASH
            = "com.android.phone.extra.SEND_EMPTY_FLASH";

    private String mCurrentCountryIso;
    
    private String mClipboardTextCache;
    
    private boolean mClearDigitsOnStop = false;
    
    private boolean mShowSimIndicator = false;
    private StatusBarManager mStatusBarMgr;
    
    private ProviderStatusLoader mProviderStatusLoader;
    
 
    private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {


		/**
         * Listen for phone state changes so that we can take down the
         * "dialpad chooser" if the phone becomes idle while the
         * chooser UI is visible.
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // Log.i(TAG, "PhoneStateListener.onCallStateChanged: "
            //       + state + ", '" + incomingNumber + "'");
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                // Log.i(TAG, "Call ended with dialpad chooser visible!  Taking it down...");
                // Note there's a race condition in the UI here: the
                // dialpad chooser could conceivably disappear (on its
                // own) at the exact moment the user was trying to select
                // one of the choices, which would be confusing.  (But at
                // least that's better than leaving the dialpad chooser
                // onscreen, but useless...)
                //On gemini platform, the phone state need to check SIM1 & SIM2
                //for current state, we only check if the phone is IDLE
                final boolean phoneIsInUse = ContactsUtils.phoneIsInUse();
                if(dialpadChooserVisible()) {
                    if (!phoneIsInUse) {
                        showDialpadChooser(false);
                    }
                }

                if(!phoneIsInUse) {
                    if(mDigits != null)
                        mDigits.setHint(null);
                }
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            log("onServiceStateChanged, serviceState = " + serviceState);
            if(getActivity() == null) 
                return;
            if(serviceState.getState() == ServiceState.STATE_IN_SERVICE) {
                String newIso = ContactsUtils.getCurrentCountryIso(getActivity());
                if(mCurrentCountryIso != null && !mCurrentCountryIso.equals(newIso)) {
                    mCurrentCountryIso = newIso;
                    if(mTextWatcher == null)
                        return;

                    mDigits.removeTextChangedListener(mTextWatcher);
                    log("re-set phone number formatting text watcher, mCurrentCountryIso = " + mCurrentCountryIso + " newIso = " + newIso);
                    mDigits.setTag(mHandler.obtainMessage(MSG_GET_TEXT_WATCHER));
                    PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), mDigits);
                }
            }
        }
    };

    private boolean mWasEmptyBeforeTextChange;
    
    //Gionee <huangzy> <2013-05-29> add for CR00820736 begin
    private final boolean ONE_HAND_HANDLER_SUPPORT =
    		SystemProperties.get("ro.gn.operation.support").equals("yes");
    private boolean mIsOneHandHandlerState;
    private final String ONE_HAND_STATE_SETTING_KEY = "gn_phone_keyboard";/*Settings.System.GN_PHONE_KEYBOARD*/
    private final String ONE_HAND_DIAL_PLACE_SETTING_KEY = "gn_phone_keyboard_place";
    private final int DIALPAD_PLACE_LEFT = 1;
    private final int DIALPAD_PLACE_RIGHT = 2;
    //Gionee <huangzy> <2013-05-29> add for CR00820736 end

    private Handler mBackgroundHandler = null;
    private final int DIALPAD_GET_AREA = 0;
    
    private void performBackgroundTask(int what, Object msgObj){
    	switch(what){
    	case DIALPAD_GET_AREA:
    		final String number = (String) msgObj;
            final String area = NumberAreaUtil.getInstance(getActivity()).getNumAreaFromAoraLock(getActivity(), number, false);
    		
            if (getActivity() != null) { //aurora add by ukiliu 2015-07-20
    			getActivity().runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    		            if (mDigits.getText().toString().equals(number)) {
    			            mNumArea.setVisibility(TextUtils.isEmpty(area) ? View.GONE : View.VISIBLE);
    		            	if(!mNumArea.getText().toString().equals(area)){
    				            mNumArea.setText(area);
    		            	}
    		            }
    				}
    			});
    		}
            
    		break;
    	}
    }
    
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mWasEmptyBeforeTextChange = TextUtils.isEmpty(s);
    }

    public void onTextChanged(CharSequence input, int start, int before, int changeCount) {
    	boolean isCurEmpty = TextUtils.isEmpty(input);
        if ((mWasEmptyBeforeTextChange && !isCurEmpty) || isCurEmpty) {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
        

        // DTMF Tones do not need to be played here any longer -
        // the DTMF dialer handles that functionality now.
    }

    //Gionee:huangzy 20121012 add start
    private String mDigitsPreviousText;
    //Gionee:huangzy 20121012 add end
    
    public void afterTextChanged(Editable input) {
    	if (input.toString().equals(mDigitsPreviousText)) {
    		return;
    	}

    	mDigitsPreviousText = input.toString();
    	
    	//Gionee:huangzy 20120821 modify for CR00673082 start
        if (null != input && input.length() > 1 &&
        		SpecialCharSequenceMgrProxy.handleChars(getActivity(), input.toString(), mDigits)) {
            clearDigits();
            return;
        }
    	//Gionee:huangzy 20120821 modify for CR00673082 end

        updateDialAndDeleteButtonEnabledState();
        final int digitsLen = getDigitsTextLen();
        
        
        if (null != mNumArea && digitsLen > 1) {
            if (mDigits != null && !mDigits.isShown()) {
                mDigits.setVisibility(View.VISIBLE);
                mDigits.requestFocus();
            }
            
            String area = NumberAreaUtil.getInstance(getActivity()).getNumAreaFromAora(getActivity(), mDigits.getText().toString(), false);
            if(area != null){
                if (!mNumArea.getText().toString().equals(area)) {
    	            mNumArea.setVisibility(TextUtils.isEmpty(area) ? View.GONE : View.VISIBLE);            
    	            mNumArea.setText(area);
                }
            } else {
            	if(mBackgroundHandler.hasMessages(DIALPAD_GET_AREA)){
            		mBackgroundHandler.removeMessages(DIALPAD_GET_AREA);
            	}
            	Message msg = new Message();
            	msg.what = DIALPAD_GET_AREA;
            	msg.obj = mDigits.getText().toString();
            	mBackgroundHandler.sendMessageDelayed(msg, 150);
            }
        } else if(null != mNumArea) {
        	 mNumArea.setVisibility(View.GONE);     
        }
        
        String lastclickNumber = ((AuroraDialerSearchController)mDialerSearchController).getDialNumber();
        if(lastclickNumber == null || !lastclickNumber.equalsIgnoreCase(mDigits.getText().toString())) {
        	((AuroraDialerSearchController)mDialerSearchController).resetLastClickResult();
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        if(DBG)log("onCreate start...");
        

        mProviderStatusLoader = new ProviderStatusLoader(getActivity());
        try {
            mHaptic.init(getActivity(), true);
        } catch (Resources.NotFoundException nfe) {
             Log.e(TAG, "Vibrate control bool missing.", nfe);
        }

        mProhibitedPhoneNumberRegexp = getResources().getString(
                R.string.config_prohibited_phone_number_regexp);

        /**
         * Change Feature by mediatek .inc
         * description : initialize speed dial
         */
        if(ContactsApplication.sSpeedDial)
            mSpeedDial = new SpeedDial(getActivity());
        mFragmentState = FragmentState.CREATED;
        /**
         * Change Feature by mediatek end
         */
//        mIsUseSoundPool = Build.VERSION.SDK_INT < 21;        
        if(mIsUseSoundPool) {
        	new InitSoundTask().execute("");
        }

        if(DBG)log("onCreate end...");
        Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onCreate1 time="+System.currentTimeMillis());  

        HandlerThread mBackgroundThread = new HandlerThread("DialpadGetArea",
                Process.THREAD_PRIORITY_BACKGROUND);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                performBackgroundTask(msg.what, msg.obj);
            }
        };

        if(getActivity() instanceof AuroraActivity){
        	AuroraActivity auroraActivity = (AuroraActivity)getActivity();
        	auroraActivity.getAuroraActionBar().setVisibility(View.GONE);
        	auroraActivity.setAuroraMenuCallBack(auroraMenuCallBack);
        	auroraActivity.setAuroraMenuItems(R.menu.aurora_dialtacts_options);
        }
              

       IntentFilter phbLoadIntentFilter =
           new IntentFilter((AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED));
       if(FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
//           IntentFilter intentFilter = new IntentFilter();
    	   phbLoadIntentFilter.addAction(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED);
//           getActivity().registerReceiver(mReceiver, intentFilter);
       }
       getActivity().registerReceiver(mReceiver, phbLoadIntentFilter);
    }
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();

            if(ContactsFeatureConstants.ACTION_VOICE_CALL_DEFAULT_SIM_CHANGED.equals(action)) {
                if(FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
                    if (mShowSimIndicator) {
                    }
                }
            }
            
            if (action.equals(AbstractStartSIMService.ACTION_PHB_LOAD_FINISHED)) {
                updateDialerSearch();
            }
		}
	};

    
    //aurora add liguangyu 201311125 start
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

        @Override
        public void auroraMenuItemClick(int itemId) {
            Log.e("AuroraDialtactsActivityV2", "auroraMenuItemClick");
            switch (itemId) {
            case R.id.menu_call_settings: {
            	startActivity(GNContactsUtils.getCallSettingsIntent());
                break; 
            }
            case R.id.menu_add_new_contact: {
            	startActivity(IntentFactory.newCreateContactIntent(getDigitsText()));
    			break;
            }
    		case R.id.menu_add_exist_contact: {
    			startActivity(IntentFactory.newInsert2ExistContactIntent(getDigitsText()));
    			break;
    		}
    		
    		case R.id.menu_call_record: {
                startActivity(new Intent(ContactsApplication.getInstance().getApplicationContext(), AuroraCallRecordActivity.class));
                break;
            }
            
            default:
                break;
            }
        }
    };
    //aurora add liguangyu 201311125 end

    /**
     * chagne feature by mediatek .inc
     * description : add this method to do clean up
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        log("onDestroyView");

        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null)
                mDialerSearchController.onDestroy();
        }

        
    	if(GNContactsUtils.isMultiSimEnabled()) {
    		getActivity().unregisterReceiver(mPhoneStateReceiver);
    		AuroraSubInfoNotifier.getInstance().removeListener(this);
    		getActivity().getContentResolver().unregisterContentObserver(mSimModeObserver);    	
    	}
    }

    @Override
    public void onDestroy() {
        if(mBackgroundHandler != null){
            mBackgroundHandler.getLooper().quit();
            mBackgroundHandler = null;
        }
        log("onDestroy");

        getActivity().unregisterReceiver(mReceiver);
        mFragmentState = FragmentState.DESTROYED;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
    	Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onCreateView1 time="+System.currentTimeMillis());
        fragmentView = inflater.inflate(R.layout.aurora_dialpad_fragment_v2, container, false);
        Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onCreateView11 time="+System.currentTimeMillis());  
        mLaunch = true;
        // Load up the resources for the text field.
        Resources r = getResources();

        mDigits = (AuroraEditText) fragmentView.findViewById(R.id.digits);
//        mDigits.setKeyListener(DialerKeyListener.getInstance());//aurora add zhouxiaobing 20131028
        mDigits.setKeyListener(new NumberKeyListener(){
        	protected char[] getAcceptedChars()
        	{
        	  char[] numberChars={'0','1','2','3','4','5','6','7','8','9','*','#','p','w','P','W','+', ' '};
        	  return numberChars;
        	}
        	public int getInputType() {
        	    return android.text.InputType.TYPE_CLASS_PHONE;  
        	   }
        	});
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);
        mDigits.setOnLongClickListener(this);
        mDigits.addTextChangedListener(this);
//        try {
//            mDigits.setTypeface(Typeface.createFromFile("system/fonts/number.ttf"));//aurora add zhouxiaobing 20130927 
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        mDigits.addTextChangedListener(new GnAutoScaleTextSizeWatcher(mDigits, 
        		r.getDimensionPixelSize(R.dimen.gn_dialpad_digits_text_size_min),
        		r.getDimensionPixelSize(R.dimen.gn_dialpad_digits_text_size), 
        		r.getDimensionPixelSize(R.dimen.gn_dialpad_digits_text_size_delta)));
        
        mNumArea = (TextView)fragmentView.findViewById(R.id.num_area);

        if(ContactsApplication.sDialerSearchSupport) {
            mListView = (AuroraListView) fragmentView.findViewById(R.id.list_view);           
            mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            
            if(mListView != null) {
//                mListView.setOnScrollListener(this);
                mDialerSearchController = new AuroraDialerSearchController(getActivity(), mListView);
                
                if (null != mDialerSearchController) {
                	mDialerSearchController.setDialerSearchTextWatcher(mDigits);
                    mDialerSearchController.setOnDialerSearchResult(this);
                    mDialerSearchController.setOnDialerSearchListener(this);
                }
                
                mNoMatchView = (TextView)fragmentView.findViewById(R.id.no_match_view);
                mDivider = fragmentView.findViewById(R.id.divider);
            }
        }
        
        mAddContactView = fragmentView.findViewById(R.id.aurora_addcontacts);
        if (mAddContactView != null) {
            mAddContactView.setOnClickListener(this);
            mAddContactView.setEnabled(false);
        }
        
        mAddContactViewMsim = fragmentView.findViewById(R.id.aurora_addcontacts_msim);
        if (mAddContactViewMsim != null) {
        	mAddContactViewMsim.setOnClickListener(this);
        	mAddContactViewMsim.setEnabled(false);
        }
        /**
         * Change Feature by mediatek .inc end
         */

        mDigits.setTag(mHandler.obtainMessage(MSG_GET_TEXT_WATCHER));
        
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), mDigits);

        // Check whether we should show the onscreen "Dial" button.
        mDialButton0 = fragmentView.findViewById(R.id.aurora_dialButton_0);  
        mDialButton1 = fragmentView.findViewById(R.id.aurora_dialButton_1);  
        mDialButton = fragmentView.findViewById(R.id.aurora_dialButton);        

        if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
            mDialButton.setOnClickListener(this);
            mDialButton.setOnTouchListener(this);
            mDialButton0.setOnClickListener(this);
            mDialButton0.setOnTouchListener(this);
            mDialButton1.setOnClickListener(this);
            mDialButton1.setOnTouchListener(this);
        } else {
            mDialButton.setVisibility(View.GONE); // It's VISIBLE by default
            mDialButton = null;
            mDialButton0.setVisibility(View.GONE); 
            mDialButton0 = null;
            mDialButton1.setVisibility(View.GONE); 
            mDialButton1 = null;
        }
		
        /**
         * Change Feature by mediatek .inc
         * original android code:
         * mDelete = mAdditionalButtonsRow.findViewById(R.id.deleteButton);
         * description : delete button is moved to the right of digits, nolonger
         * in the additional buttons row
         */
        mDelete = fragmentView.findViewById(R.id.aurora_deleteButton);
        /**
         * Change Feature by mediatek.inc end
         */
        if(mDelete != null){
            mDelete.setOnClickListener(this);
            mDelete.setOnLongClickListener(this);
            mDelete.setOnTouchListener(this);
        }
        
        mDeleteMsim = fragmentView.findViewById(R.id.aurora_deleteButton_msim);
        if(mDeleteMsim != null){
        	mDeleteMsim.setOnClickListener(this);
        	mDeleteMsim.setOnLongClickListener(this);
        	mDeleteMsim.setOnTouchListener(this);
        }
        
        mDialpadButton = (ImageView) fragmentView.findViewById(R.id.dialpadButton);
        if (null != mDialpadButton) {
            mDialpadButton.setOnClickListener(this);
        }

        mDigitKeyboard = fragmentView.findViewById(R.id.aurora_dial_digit_keyboard);  // This is null in landscape mode.
        
        // In landscape we put the keyboard in phone mode.
        if (null == mDigitKeyboard) {
 //           mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            mDigits.setCursorVisible(false);
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);//aurora change zhouxiaobing 20131028
        }
        

        // Set up the "dialpad chooser" UI; see showDialpadChooser().
        mDialpadChooser = (AuroraListView) fragmentView.findViewById(R.id.dialpadChooser);
        mDialpadChooser.setOnItemClickListener(this);

        configureScreenFromIntent(getActivity().getIntent());
        setKeyboardClickListener();

        //Gionee <huangzy> <2013-04-13> add for CR00786343 start        
//        mDigitKeyboard.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//        	@Override
//			public void onLayoutChange(View v, int left, int top, int right,
//					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//        		mDialpadFlipperHeight = bottom - top;  
//        		if (null != mListView) {
//        			mListView.setPadding(0, 0, 0, mDialpadFlipperHeight);
//        		}
//			}
//		});
        //Gionee <huangzy> <2013-04-13> add for CR00786343 end
        Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onCreateView2 time="+System.currentTimeMillis());   
        //aurora add zhouxiaobing 20131122 start
        mHandler.sendMessageDelayed(mHandler.obtainMessage(2), 100);
        //aurora add zhouxiaobing 20131122 end
        mSingleDialLine = (View) fragmentView.findViewById(R.id.single_dial_line);
        mDoubleDialLine = (View) fragmentView.findViewById(R.id.msim_dial_line);
    	mDialpadBg = (View) fragmentView.findViewById(R.id.aurora_dialpad_bg);
    	if(GNContactsUtils.isMultiSimEnabled()) {
    		IntentFilter recordFilter = new IntentFilter(AURORA_STATE_CHANGED_ACTION);      
    		getActivity().registerReceiver(mPhoneStateReceiver, recordFilter);	
    		getActivity().getContentResolver().registerContentObserver(Settings.System.CONTENT_URI, true, mSimModeObserver);	
    		AuroraSubInfoNotifier.getInstance().addListener(this);
    		updateCallButtonVisbilityAndWidth();
    	}    
    	
        if(Build.VERSION.SDK_INT == 19
				|| Build.VERSION.SDK_INT >= 21) {
            int height = getResources().getDimensionPixelOffset(R.dimen.aurora_dialpad_digits_height);
          	int paddingTop = getResources().getDimensionPixelOffset(com.aurora.R.dimen.status_bar_height);
        	FrameLayout mNumLayout = (FrameLayout)fragmentView.findViewById(R.id.digits_area);     	
            ViewGroup.LayoutParams params = mNumLayout.getLayoutParams();
            params.height = height + paddingTop;
            mNumLayout.setLayoutParams(params);                  
          	mNumLayout.setPadding(0, paddingTop, 0, 0);
        }
    	
        return fragmentView;
    }

     
    private boolean isInitYellowPages = false;
    public void initYellowPages(){
    	if(!isInitYellowPages){
    		isInitYellowPages = true;
            mYellowPages = fragmentView.findViewById(R.id.aurora_dial_yellow_pages);
    		if(ContactsApplication.sIsAuroraYuloreSupport){
    	        FragmentManager fm = getFragmentManager();
    	        HomeFragment yellowPagesBody = (HomeFragment)HomeFragment.instantiate(getActivity(), HomeFragment.class.getName());
    	        Bundle bundle = new Bundle();
    	        int height;
    			if (Build.VERSION.SDK_INT == 19
    					|| Build.VERSION.SDK_INT >= 21) {
    				height = (int) getResources().getDimension(com.aurora.R.dimen.aurora_action_bar_height_trasparent_status);
    			} else {
    				height = (int) getResources().getDimension(com.aurora.R.dimen.aurora_action_bar_height);
    			}
    	    	bundle.putInt("top_height", height);
    	    	yellowPagesBody.setArguments(bundle);
//    	    	FragmentTransaction ft = fm.beginTransaction();
//    	    	ft.replace(R.id.yellow_pages_body, yellowPagesBody);
//    	    	ft.commit();
    		} else {
    			mYellowPages.setVisibility(View.GONE);
    		}
    	}
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDialpadBg.setVisibility(View.GONE);
    }

    private boolean isLayoutReady() {
        return mDigits != null;
    }

    public AuroraEditText getDigitsWidget() {
        return mDigits;
    }

    /**
     * @return true when {@link #mDigits} is actually filled by the Intent.
     */
    private boolean fillDigitsIfNecessary(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri != null) {
            	
            	//Gionee:huangzy 20130122 add for CR00763149 start
            	if (null != mDialerSearchController) {
            		mDialerSearchController.onResume();
            		Log.i("James", "mDialerSearchController.onResume()");
            	}
            	//Gionee:huangzy 20130122 add for CR00763149 end
            	
                if ("tel".equals(uri.getScheme())) {
                    // Put the requested number into the input area
                    String data = uri.getSchemeSpecificPart();
                    setFormattedDigits(data, null);
                    //Gionee:huangzy 20120620 add for CR00625717 start
                    showDialpad(true, true);
                    //Gionee:huangzy 20120620 add for CR00625717 end
                    // clear the data
                    intent.setData(null);
                    
                    // gionee xuhz 20120508 add for CR00594040 start
                    if (data != null && !data.isEmpty()) {
                        // gionee xuhz 20120713 add for CR00626592 start
                        mDigits.requestFocus();
                        // gionee xuhz 20120713 add for CR00626592 start
//                        mDigits.setSelection(0);
                        mDigits.setVisibility(View.VISIBLE);
                        mDigits.setCursorVisible(false);
                    }
                    // gionee xuhz 20120508 add for CR00594040 end
                    
                    return true;
                } else if ("voicemail".equals(uri.getScheme())) {
                    String data = uri.getSchemeSpecificPart();
                    setFormattedDigits(data, null);
                    // clear the data
                    intent.setData(null);
                    if (data != null && !data.isEmpty()) {
                        mDigits.setVisibility(View.VISIBLE);
                    }
                    return true;
                } else {
                    String type = intent.getType();
                    if (People.CONTENT_ITEM_TYPE.equals(type)
                            || Phones.CONTENT_ITEM_TYPE.equals(type)) {
                        // Query the phone number
                        Cursor c = getActivity().getContentResolver().query(intent.getData(),
                                new String[] {PhonesColumns.NUMBER, PhonesColumns.NUMBER_KEY},
                                null, null, null);
                        if (c != null) {
                            try {
                                if (c.moveToFirst()) {
                                    // Put the number into the input area
                                    setFormattedDigits(c.getString(0), c.getString(1));
                                    // clear the data
                                    intent.setData(null);
                                    return true;
                                }
                            } finally {
                                c.close();
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * @see #showDialpadChooser(boolean)
     */
    private static boolean needToShowDialpadChooser(Intent intent, boolean isAddCallMode) {
        final String action = intent.getAction();

        boolean needToShowDialpadChooser = false;

        if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            Uri uri = intent.getData();
            if (uri == null) {
                // ACTION_DIAL or ACTION_VIEW with no data.
                // This behaves basically like ACTION_MAIN: If there's
                // already an active call, bring up an intermediate UI to
                // make the user confirm what they really want to do.
                // Be sure *not* to show the dialpad chooser if this is an
                // explicit "Add call" action, though.
                if (!isAddCallMode && ContactsUtils.phoneIsInUse()) {
                    needToShowDialpadChooser = true;
                }
            }
        } else if (Intent.ACTION_MAIN.equals(action)) {
            // The MAIN action means we're bringing up a blank dialer
            // (e.g. by selecting the Home shortcut, or tabbing over from
            // Contacts or Call log.)
            //
            // At this point, IF there's already an active call, there's a
            // good chance that the user got here accidentally (but really
            // wanted the in-call dialpad instead).  So we bring up an
            // intermediate UI to make the user confirm what they really
            // want to do.
            if (ContactsUtils.phoneIsInUse()) {
                // Log.i(TAG, "resolveIntent(): phone is in use; showing dialpad chooser!");
                needToShowDialpadChooser = true;
            }
        }

        return needToShowDialpadChooser;
    }

    private static boolean isAddCallMode(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_DIAL.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            // see if we are "adding a call" from the InCallScreen; false by default.
            return intent.getBooleanExtra(ADD_CALL_MODE_KEY, false);
        } else {
            return false;
        }
    }

    /**
     * Checks the given Intent and changes dialpad's UI state. For example, if the Intent requires
     * the screen to enter "Add Call" mode, this method will show correct UI for the mode.
     */
    public void configureScreenFromIntent(Intent intent) {
        if (!isLayoutReady()) {
            // This happens typically when parent's Activity#onNewIntent() is called while
            // Fragment#onCreateView() isn't called yet, and thus we cannot configure Views at
            // this point. onViewCreate() should call this method after preparing layouts, so
            // just ignore this call now.
            Log.i(TAG,
                    "Screen configuration is requested before onCreateView() is called. Ignored");
            return;
        }

        boolean needToShowDialpadChooser = false;

        final boolean isAddCallMode = isAddCallMode(intent);
        if (!isAddCallMode) {
            final boolean digitsFilled = fillDigitsIfNecessary(intent);
            if (!digitsFilled) {
                needToShowDialpadChooser = needToShowDialpadChooser(intent, isAddCallMode);
            }
        }
        showDialpadChooser(needToShowDialpadChooser);
    }

    private void setFormattedDigits(String data, String normalizedNumber) {
        // strip the non-dialable numbers out of the data string.
        String dialString = PhoneNumberUtils.extractNetworkPortion(data);
        dialString =
                PhoneNumberUtils.formatNumber(dialString, normalizedNumber, mCurrentCountryIso);
        if (!TextUtils.isEmpty(dialString)) {
            Editable digits = mDigits.getText();
            digits.replace(0, digits.length(), dialString);
            // for some reason this isn't getting called in the digits.replace call above..
            // but in any case, this will make sure the background drawable looks right
            afterTextChanged(digits);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mProviderStatusLoader.setProviderStatusListener(this);
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            mShowSimIndicator = true;
        }
        Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onResume1 time="+System.currentTimeMillis());
        if(getActivity() instanceof AuroraActivity){
    		if(Build.VERSION.SDK_INT == 19
    				|| Build.VERSION.SDK_INT >= 21) {
    			if(isYellowPagesShowing()){

    				ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), true);
    			} else {
    				if(SystemProperties.get("persist.sys.aurora.overlay").equals("/system/theme/lady/")){

    					ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), true);
    				}else{

    					ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), false);
    				}
    				
    			}
    		}
        }
        mCurrentCountryIso = ContactsUtils.getCurrentCountryIso();
        log("current country iso : " + mCurrentCountryIso);
        
        Log.i("James", "GnDialpadV2 onResume");
        
        log("onResume, mFragmentState = " + mFragmentState);
        if(mFragmentState == FragmentState.RESUMED) {
            log("duplicate resumed state, bial out...");
            return;
        }
        mFragmentState = FragmentState.RESUMED;
        

    
        /**
         * add by mediatek .inc
         * description : start query SIM association
         */
        SimAssociateHandler.getInstance().load();
        /**
         * add by mediatek end
         */

        // Query the last dialed number. Do it first because hitting
        // the DB is 'slow'. This call is asynchronous.
        queryLastOutgoingCall();

        // retrieve the DTMF tone play back setting.
        mDTMFToneEnabled = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;

        // Retrieve the haptic feedback setting.
        mHaptic.checkSystemSetting();
        
        Activity parent = getActivity();
        if(mIAuroraDialpadFragment != null){
            // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
            // digits in the dialer field.
            Uri uri = mIAuroraDialpadFragment.getReallyUri();
            boolean isDialIntent = false;
            Intent intent = new Intent();
            if (uri != null && uri.toString().startsWith("tel:")) {
                isDialIntent = true;
                intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(uri);
            }
            
            if (isDialIntent) {
                fillDigitsIfNecessary(intent);
            } else {
                fillDigitsIfNecessary(parent.getIntent());
            }
        }

        // While we're in the foreground, listen for phone state changes,
        // purely so that we can take down the "dialpad chooser" if the
        // phone becomes idle while the chooser UI is visible.
        TelephonyManager telephonyManager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        /**
         * change by mediatek .inc
         * description : add gemini support
         */
//        if(FeatureOption.MTK_GEMINI_SUPPORT) {
//            AuroraTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_1);
//            AuroraTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_2);
//        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE);
//        }
        /**
         * change by mediatek .inc end
         */

        // Potentially show hint text in the mDigits field when the user
        // hasn't typed any digits yet.  (If there's already an active call,
        // this hint text will remind the user that he's about to add a new
        // call.)
        //
        // TODO: consider adding better UI for the case where *both* lines
        // are currently in use.  (Right now we let the user try to add
        // another call, but that call is guaranteed to fail.  Perhaps the
        // entire dialer UI should be disabled instead.)
        if (ContactsUtils.phoneIsInUse()) {
            //Gionee <wangth><2013-05-02> modify for CR00803973 begin
            /*
            mDigits.setHint(R.string.dialerDialpadHintText);
            */
            mDigits.setHint(null);
            //Gionee <wangth><2013-05-02> modify for CR00803973 end
        } else {
            // Common case; no hint necessary.
            mDigits.setHint(null);

            // Also, a sanity-check: the "dialpad chooser" UI should NEVER
            // be visible if the phone is idle!
            showDialpadChooser(false);
            
            /*updateDialAndDeleteButtonEnabledState();*/
        }

     
    		if (null != mHandler) {
        		mHandler.sendEmptyMessageDelayed(
        				MSG_DIALER_SEARCH_CONTROLLER_ON_RESUME, 200);
        	}
               

        if(mfirstShow) { 
	        boolean isDigitsEmpty = isDigitsEmpty();
	        if (isDigitsEmpty) {        	
//	            showDialpad(true, true);
//	            //aurora add liguangyu 20131113 for #864 start
	        	resetDialpadButton();
//	            //aurora add liguangyu 20131113 for #864 end
//	        	updateViewOnResumeAnimation();
	        }
        }


        Log.v("SHIJIAN", "AuroraDialpadFragmentV2 onResume2 time="+System.currentTimeMillis()); 
        if (mListView != null) {
            mListView.auroraOnResume();
        }
    }
    
	public interface IAuroraDialpadFragment{
		public Uri getReallyUri();
	}
	private IAuroraDialpadFragment mIAuroraDialpadFragment;
	public void setIAuroraDialpadFragment(IAuroraDialpadFragment iAuroraDialpadFragment){
		mIAuroraDialpadFragment = iAuroraDialpadFragment;
	}
    
    private void resetDialpadButton(){
		if (mUpdateDialpadButtonlistener != null) {
			if(ContactsApplication.sIsAuroraYuloreSupport){
    			mUpdateDialpadButtonlistener.updateDialpadButton(false);
			} else {
    			mUpdateDialpadButtonlistener.updateDialpadButton(true);
			}
		}
    }

    @Override
    public void onPause() {
        /**
         * add by mediatek .inc
         * description : mark the fragment state
         */
        log("onPause");
        mFragmentState = FragmentState.PAUSED;
        /**
         * add by mediatek .inc end
         */

        // Stop listening for phone state changes.
        TelephonyManager telephonyManager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

//        synchronized (mToneGeneratorLock) {
//            if (mToneGenerator != null) {
//                mToneGenerator.release();
//                mToneGenerator = null;
//            }
//        }
        // TODO: I wonder if we should not check if the AsyncTask that
        // lookup the last dialed number has completed.
        //aurora delete liguangyu 20140923 for #8441 start
//        mLastNumberDialed = EMPTY_NUMBER;  // Since we are going to query again, free stale number.
        //aurora delete liguangyu 20140923 for #8441 end
        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null)
                mDialerSearchController.onPause();
        }
        
        
        if (mListView != null) {
            mListView.auroraOnPause();
        }
        
        if (FeatureOption.MTK_GEMINI_SUPPORT || GNContactsUtils.isMultiSimEnabled()) {
            mShowSimIndicator = false;
        }

        mProviderStatusLoader.setProviderStatusListener(null);
        super.onPause();
    }
    
    @Override
    public void onProviderStatusChange() {
    }

    @Override
    public void onStop() {
        super.onStop();
        log("onStop");
        /**
         * Change Feature by mediatek .inc
         * description : clear digits when onPause
         */
        mFragmentState = FragmentState.STOPPED;        

        /**
         * Change Feature by mediatek end
         */
        
        if (null != mCallLogObserver) {
            getActivity().getContentResolver().unregisterContentObserver(mCallLogObserver);
        }
    }

    private static Intent getAddToContactIntent(CharSequence digits) {
        final Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(Insert.PHONE, digits);
        // add by mediatek 
        intent.putExtra("fromWhere", "CALL_LOG");
        intent.setType(People.CONTENT_ITEM_TYPE);
        return intent;
    }

    private void keyPressed(int keyCode) {
        //Gionee:huangzy 20121226 add for CR00667771 begin
    	/*mHaptic.vibrate();*/
        vibrate();
        //Gionee:huangzy 20121226 add for CR00667771 end        
        
        if(DBG)log("keyPressed keyCode: "+ keyCode);		
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);

        // If the cursor is at the end of the text we hide it.
        final int length = mDigits.length();
        if (length == mDigits.getSelectionStart() && length == mDigits.getSelectionEnd()) {
            mDigits.setCursorVisible(false);//aurora change zhouxiaobing 20131028
        }
    }

    public boolean onKey(View view, int keyCode, KeyEvent event) {
        if(DBG)log("onKey: "+ keyCode + "event: " + event);
        switch (view.getId()) {
            case R.id.digits:
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    dialButtonPressed();
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        /**
         * Change Feature by mediatek .Inc
         * description : button event handler
         */
        final boolean handled = onClickInternal(view);
        if(DBG) log("onClick: "+ view.getId() + "handled: " + handled);
        if(handled)
            return;
        /**
         * change Feature by mediatek .Inc end
         */
        int viewId = view.getId();

        //Gionee <huangzy> <2013-05-08> modify for CR00809854 begin
        int eventKey = DIGIT_ID_TO_EVENT_KEY.get(viewId, -1);
        //aurora modify liguangyu 20140723 for #6768 start
    	if (-1 != eventKey) {
        //aurora modify liguangyu 20140723 for #6768 end
    		Log.v(TAG, "onTouch music start");
    		if(!ActivityManager.isUserAMonkey()) {
    			playMusic(viewId);
    		}
    		Log.v(TAG, "onTouch music end");
    		keyPressed(eventKey);
//    		return;
    	}
    	//Gionee <huangzy> <2013-05-08> modify for CR00809854 end

        
        switch (view.getId()) {
//            case R.id.aurora_deleteButton: {
//                keyPressed(KeyEvent.KEYCODE_DEL);
//                return;
//            }
        	case R.id.aurora_dialButton_0:
        	case R.id.aurora_dialButton_1:
            case R.id.aurora_dialButton: {
                vibrate();  // Vibrate here too, just like we do for the regular keys
               
                if(GNContactsUtils.isMultiSimEnabled() /*&& ContactsUtils.isShowDoubleButton()*/) {//aurora change zhouxiaobing 20140514
                  if(ContactsUtils.isShowDoubleButton())	
                    GNdialButtonPressed(view.getId() == R.id.aurora_dialButton_1 ? 1 : 0);
                  else
                  {
                	 int sub = 0;
                   	 try {
                            int simstate = android.provider.Settings.Global.getInt(ContactsApplication.getInstance().getContentResolver(),
                      		   "mobile_data"+ 2);            
                            Log.d(TAG, "updateCardState restore simstate= " + simstate);
                            if(simstate == 2) {
                            	sub = 1;
                            } 
                        } catch (Exception e) {
                     	    e.printStackTrace();
                        }  
                	  GNdialButtonPressed(sub);
                  }
                } else {
                	dialButtonPressed();
                }
               
                return;
            } 							  
            case R.id.digits: {
                if (!isDigitsEmpty()) {
                    mDigits.setCursorVisible(true);
                }
                showDialpad(true, true);
                return;
            }
            case R.id.contacts: {
            	//startActivity(IntentFactory.newGoPeopleIntent());//aurora change zhouxiaobing 20130912
            	startActivity(IntentFactory.newInsertContactIntent(true,getDigitsText(),null,null));
            	return;
            }
            case R.id.aurora_addcontacts_msim:
            case R.id.aurora_addcontacts: {
            	//aurora change liguangyu 20131107 for BUG #458 start
            	//aurora change liguangyu 201311128 start
//            	getActivity().startActivity(IntentFactory.newCreateContactIntent(getDigitsText()));
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MENU);
                event.setSource(99);
                this.getActivity().onKeyDown(KeyEvent.KEYCODE_MENU, event);
                //aurora change liguangyu 201311128 end
            	//aurora change liguangyu 20131107 for BUG #458 end
                return;
            }            
        }
    }

    public boolean onLongClick(View view) {
        /**
         * Change Feature by mediatek .inc
         */
        boolean handled = onLongClickInternal(view);
        if(handled)
            return handled;
        /**
         * Change Feature by mediatek .inc end
         */        
        int id = view.getId();
        switch (id) {
            case R.id.deleteButton: {
                // digits.clear();
                clearDigits();
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                mDeleteMsim.setPressed(false);
                switchEmptyView(false);
                return true;
            }
            case R.id.one: {
                if (isDigitsEmpty()) {
                    if (isVoicemailAvailableProxy()) {
                        callVoicemail();
                    } else if (getActivity() != null) {
                        if(GNContactsUtils.isMultiSimEnabled()) {
                            DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                                    R.string.dialog_voicemail_not_ready_title,
                                    R.string.dialog_voicemail_not_ready_message);
                            dialogFragment.show(getFragmentManager(), "voicemail_not_ready");
                        }
                    }
                    return true;
                }
                return false;
            }
            case R.id.zero: 
            case R.id.aurora_zero:	
            {
                keyPressed(KeyEvent.KEYCODE_PLUS);
                return true;
            }
            case R.id.digits: {
                // Right now EditText does not show the "paste" option when cursor is not visible.
                // To show that, make the cursor visible, and return false, letting the EditText
                // show the option by itself.
                mDigits.setCursorVisible(true);
                return false;
            }
        }
        return false;
    }

    public void callVoicemail() {
    	
        
        startActivity(newVoicemailIntent());
        //mDigits.getText().clear(); // TODO: Fix bug 1745781
        //getActivity().finish();
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private int mTitleResId;
        private Integer mMessageResId;  // can be null

        private static final String ARG_TITLE_RES_ID = "argTitleResId";
        private static final String ARG_MESSAGE_RES_ID = "argMessageResId";

        public static ErrorDialogFragment newInstance(int titleResId) {
            return newInstanceInter(titleResId, null);
        }

        public static ErrorDialogFragment newInstance(int titleResId, int messageResId) {
            return newInstanceInter(titleResId, messageResId);
        }

        private static ErrorDialogFragment newInstanceInter(
                int titleResId, Integer messageResId) {
            final ErrorDialogFragment fragment = new ErrorDialogFragment();
            final Bundle args = new Bundle();
            args.putInt(ARG_TITLE_RES_ID, titleResId);
            if (messageResId != null) {
                args.putInt(ARG_MESSAGE_RES_ID, messageResId);
            }
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitleResId = getArguments().getInt(ARG_TITLE_RES_ID);
            if (getArguments().containsKey(ARG_MESSAGE_RES_ID)) {
                mMessageResId = getArguments().getInt(ARG_MESSAGE_RES_ID);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(getActivity());
            builder.setTitle(mTitleResId)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dismiss();
                                }
                            });
            if (mMessageResId != null) {
                builder.setMessage(mMessageResId);
            }
            return builder.create();
        }
    }

    /**
     * Plays the specified tone for TONE_LENGTH_MS milliseconds.
     *
     * The tone is played locally, using the audio stream for phone calls.
     * Tones are played only if the "Audible touch tones" user preference
     * is checked, and are NOT played if the device is in silent mode.
     *
     * @param tone a tone code from {@link ToneGenerator}
     */
    void playTone(int tone) {
        // if local tone playback is disabled, just return.
        if (!mDTMFToneEnabled) {
        	//Gionee zengxuanhui 20120803 modify for CR00647555 being
            if (!mDTMFToneEnabled) {
                mDTMFToneEnabled = Settings.System.getInt(getActivity().getContentResolver(),
                        Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
                if(!mDTMFToneEnabled){
                	return;
                }
            }
            //Gionee zengxuanhui 20120803 modify for CR00647555 end
        }

        // Also do nothing if the phone is in silent mode.
        // We need to re-check the ringer mode for *every* playTone()
        // call, rather than keeping a local flag that's updated in
        // onResume(), since it's possible to toggle silent mode without
        // leaving the current activity (via the ENDCALL-longpress menu.)
        AudioManager audioManager =
                (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        if ((ringerMode == AudioManager.RINGER_MODE_SILENT)
            || (ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
            return;
        }
    }

    /**
     * Brings up the "dialpad chooser" UI in place of the usual Dialer
     * elements (the textfield/button and the dialpad underneath).
     *
     * We show this UI if the user brings up the Dialer while a call is
     * already in progress, since there's a good chance we got here
     * accidentally (and the user really wanted the in-call dialpad instead).
     * So in this situation we display an intermediate UI that lets the user
     * explicitly choose between the in-call dialpad ("Use touch tone
     * keypad") and the regular Dialer ("Add call").  (Or, the option "Return
     * to call in progress" just goes back to the in-call UI with no dialpad
     * at all.)
     *
     * @param enabled If true, show the "dialpad chooser" instead
     *                of the regular Dialer UI
     */
    private void showDialpadChooser(boolean enabled) {
        // Check if onCreateView() is already called by checking one of View objects.
        log("showDialpadChooser, enabled = " + enabled);
        if (!isLayoutReady()) {
            return;
        }
        
        log("showDialpadChooser  : " + enabled);

        if (enabled) {            
            mDialpadChooser.setVisibility(View.VISIBLE);
            if (mDialpadChooserAdapter == null) {
                mDialpadChooserAdapter = new DialpadChooserAdapter(getActivity());
            }
            mDialpadChooser.setAdapter(mDialpadChooserAdapter);
        } else {
            mDialpadChooser.setVisibility(View.GONE);
        }
        // Gionee <xuhz> <2013-08-02> add for CR00838232 begin
        getActivity().invalidateOptionsMenu();
        // Gionee <xuhz> <2013-08-02> add for CR00838232 end
    }

    /**
     * @return true if we're currently showing the "dialpad chooser" UI.
     */
    private boolean dialpadChooserVisible() {
        return mDialpadChooser.getVisibility() == View.VISIBLE;
    }

    /**
     * Simple list adapter, binding to an icon + text label
     * for each item in the "dialpad chooser" list.
     */
    private static class DialpadChooserAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        // Simple struct for a single "choice" item.
        static class ChoiceItem {
            String text;
            Bitmap icon;
            int id;

            public ChoiceItem(String s, Bitmap b, int i) {
                text = s;
                icon = b;
                id = i;
            }
        }

        // IDs for the possible "choices":
        static final int DIALPAD_CHOICE_USE_DTMF_DIALPAD = 101;
        static final int DIALPAD_CHOICE_RETURN_TO_CALL = 102;
        static final int DIALPAD_CHOICE_ADD_NEW_CALL = 103;

        private static final int NUM_ITEMS = 3;
        private ChoiceItem mChoiceItems[] = new ChoiceItem[NUM_ITEMS];

        public DialpadChooserAdapter(Context context) {
            // Cache the LayoutInflate to avoid asking for a new one each time.
            mInflater = LayoutInflater.from(context);

            // Initialize the possible choices.
            // TODO: could this be specified entirely in XML?

            // - "Use touch tone keypad"
            mChoiceItems[0] = new ChoiceItem(
                    context.getString(R.string.dialer_useDtmfDialpad),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_tt_keypad),
                    DIALPAD_CHOICE_USE_DTMF_DIALPAD);

            // - "Return to call in progress"
            mChoiceItems[1] = new ChoiceItem(
                    context.getString(R.string.dialer_returnToInCallScreen),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_current_call),
                    DIALPAD_CHOICE_RETURN_TO_CALL);

            // - "Add call"
            mChoiceItems[2] = new ChoiceItem(
                    context.getString(R.string.dialer_addAnotherCall),
                    BitmapFactory.decodeResource(context.getResources(),
                                                 R.drawable.ic_dialer_fork_add_call),
                    DIALPAD_CHOICE_ADD_NEW_CALL);
        }

        public int getCount() {
            return NUM_ITEMS;
        }

        /**
         * Return the ChoiceItem for a given position.
         */
        public Object getItem(int position) {
            return mChoiceItems[position];
        }

        /**
         * Return a unique ID for each possible choice.
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a view for each row.
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            // When convertView is non-null, we can reuse it (there's no need
            // to reinflate it.)
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dialpad_chooser_list_item, null);
            }

            TextView text = (TextView) convertView.findViewById(R.id.text);
            text.setText(mChoiceItems[position].text);

            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            icon.setImageBitmap(mChoiceItems[position].icon);

            return convertView;
        }
    }

    /**
     * Handle clicks from the dialpad chooser.
     */
    public void onItemClick(AdapterView parent, View v, int position, long id) {
        DialpadChooserAdapter.ChoiceItem item =
                (DialpadChooserAdapter.ChoiceItem) parent.getItemAtPosition(position);
        int itemId = item.id;
        switch (itemId) {
            case DialpadChooserAdapter.DIALPAD_CHOICE_USE_DTMF_DIALPAD:
                // Log.i(TAG, "DIALPAD_CHOICE_USE_DTMF_DIALPAD");
                // Fire off an intent to go back to the in-call UI
                // with the dialpad visible.
                returnToInCallScreen(true);
                break;

            case DialpadChooserAdapter.DIALPAD_CHOICE_RETURN_TO_CALL:
                // Log.i(TAG, "DIALPAD_CHOICE_RETURN_TO_CALL");
                // Fire off an intent to go back to the in-call UI
                // (with the dialpad hidden).
                returnToInCallScreen(false);
                break;

            case DialpadChooserAdapter.DIALPAD_CHOICE_ADD_NEW_CALL:
                // Log.i(TAG, "DIALPAD_CHOICE_ADD_NEW_CALL");
                // Ok, guess the user really did want to be here (in the
                // regular Dialer) after all.  Bring back the normal Dialer UI.
                showDialpadChooser(false);
                log("dialpad choise add new call, adjust list view layout parameters");                
                break;

            default:
                Log.w(TAG, "onItemClick: unexpected itemId: " + itemId);
                break;
        }
    }

    /**
     * Returns to the in-call UI (where there's presumably a call in
     * progress) in response to the user selecting "use touch tone keypad"
     * or "return to call" from the dialpad chooser.
     */
    private void returnToInCallScreen(boolean showDialpad) {
        // Gionee:wangth 20130329 add for CR00791133 begin
        if (GNContactsUtils.isOnlyQcContactsSupport() && GNContactsUtils.isMultiSimEnabled()) {
            final boolean isVTActive = AuroraTelephonyManager.getDefault().isVTCallActive(); 
            if(isVTActive) {
                startActivity(GNContactsUtils.startQcVideoCallIntent(getDigitsText()));
            }
            
            AuroraTelephonyManager.getDefault().showCallScreenWithDialpad(showDialpad);
            
            return;
        }
        // Gionee:wangth 20130329 add for CR00791133 end
        
        TelephonyUtils.showInCallScreen(getActivity(), showDialpad);

        // Finally, finish() ourselves so that we don't stay on the
        // activity stack.
        // Note that we do this whether or not the showCallScreenWithDialpad()
        // call above had any effect or not!  (That call is a no-op if the
        // phone is idle, which can happen if the current call ends while
        // the dialpad chooser is up.  In this case we can't show the
        // InCallScreen, and there's no point staying here in the Dialer,
        // so we just take the user back where he came from...)
        // getActivity().finish();
    }

    /**
     * Returns true whenever any one of the options from the menu is selected.
     * Code changes to support dialpad options
     */
    private final String CALL_SETTING_PACKAGENAME = "com.android.phone";
    private final String CALL_SETTING_CLASSNAME = "com.android.phone.CallSettings";

    /**
     * Updates the dial string (mDigits) after inserting a Pause character (,)
     * or Wait character (;).
     */
    private void updateDialString(String newDigits) {
        int selectionStart;
        int selectionEnd;

        // SpannableStringBuilder editable_text = new SpannableStringBuilder(mDigits.getText());
        int anchor = mDigits.getSelectionStart();
        int point = mDigits.getSelectionEnd();

        selectionStart = Math.min(anchor, point);
        selectionEnd = Math.max(anchor, point);

        Editable digits = mDigits.getText();
        if (selectionStart != -1) {
            if (selectionStart == selectionEnd) {
                // then there is no selection. So insert the pause at this
                // position and update the mDigits.
                digits.replace(selectionStart, selectionStart, newDigits);
            } else {
                digits.replace(selectionStart, selectionEnd, newDigits);
                // Unselect: back to a regular cursor, just pass the character inserted.
                mDigits.setSelection(selectionStart + 1);
            }
        } else {
            int len = mDigits.length();
            digits.replace(len, len, newDigits);
        }
        //ginoee xuhz 20120423 add for CR00576275 start
        mDigits.invalidate();
        //ginoee xuhz 20120423 add for CR00576275 end
    }

    /**
     * Update the enabledness of the "Dial" and "Backspace" buttons if applicable.
     */
    private void updateDialAndDeleteButtonEnabledState() {
        final boolean digitsNotEmpty = !isDigitsEmpty();

        if (mDialButton != null) {
            // On CDMA phones, if we're already on a call, we *always*
            // enable the Dial button (since you can press it without
            // entering any digits to send an empty flash.)
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                mDialButton.setEnabled(true);
            } else {
                // Common case: GSM, or CDMA but not on a call.
                // Enable the Dial button if some digits have
                // been entered, or if there is a last dialed number
                // that could be redialed.
            	
            	mDialButton.setEnabled(digitsNotEmpty ||
                        !TextUtils.isEmpty(mLastNumberDialed));
            }
        }
        
        if (mDialButton0 != null) {
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                mDialButton0.setEnabled(true);
            } else {
            	mDialButton0.setEnabled(digitsNotEmpty ||
                        !TextUtils.isEmpty(mLastNumberDialed));
            }
        }
        
        if (mDialButton1 != null) {
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                mDialButton1.setEnabled(true);
            } else {
            	mDialButton1.setEnabled(digitsNotEmpty ||
                        !TextUtils.isEmpty(mLastNumberDialed));
            }
        }

        mDelete.setEnabled(digitsNotEmpty);
        mDeleteMsim.setEnabled(digitsNotEmpty);
    }

    /**
     * Check if voicemail is enabled/accessible.
     *
     * @return true if voicemail is enabled and accessibly. Note that this can be false
     * "temporarily" after the app boot.
     * @see TelephonyManager#getVoiceMailNumber()
     */
    private boolean isVoicemailAvailable() {
        try {
            /**
             * change by mediatek .inc
             * description : use TextUtils.isEmpty
            
            return (TelephonyManager.getDefault().getVoiceMailNumber() != null);
            */
            return !TextUtils.isEmpty(TelephonyManager.getDefault().getVoiceMailNumber());
            /**
             * change by mediatek .inc end
             */
        } catch (SecurityException se) {
            // Possibly no READ_PHONE_STATE privilege.
            Log.w(TAG, "SecurityException is thrown. Maybe privilege isn't sufficient.");
        }
        return false;
    }

    /**
     * This function return true if Wait menu item can be shown
     * otherwise returns false. Assumes the passed string is non-empty
     * and the 0th index check is not required.
     */
    private static boolean showWait(int start, int end, String digits) {
        if (start == end) {
            // visible false in this case
            if (start > digits.length()) return false;

            // preceding char is ';', so visible should be false
            if (digits.charAt(start - 1) == ';') return false;

            // next char is ';', so visible should be false
            if ((digits.length() > start) && (digits.charAt(start) == ';')) return false;
        } else {
            // visible false in this case
            if (start > digits.length() || end > digits.length()) return false;

            // In this case we need to just check for ';' preceding to start
            // or next to end
            if (digits.charAt(start - 1) == ';') return false;
        }
        return true;
    }

    /**
     * @return true if the widget with the phone number digits is empty.
     */
    private boolean isDigitsEmpty() {
        return mDigits.length() == 0;
    }

    /**
     * Starts the asyn query to get the last dialed/outgoing
     * number. When the background query finishes, mLastNumberDialed
     * is set to the last dialed number or an empty string if none
     * exists yet.
     */
    // Gionee zhangxx 2012-05-24 modify for CR00608225 begin
    // private void queryLastOutgoingCall() {
    public void queryLastOutgoingCall() {
    // Gionee zhangxx 2012-05-24 modify for CR00608225 end
        //aurora delete liguangyu 20140923 for #8441 start
//        mLastNumberDialed = EMPTY_NUMBER;
        //aurora delete liguangyu 20140923 for #8441 end
        if (getActivity() == null) {
            return;
        }
        CallLogAsync.GetLastOutgoingCallArgs lastCallArgs =
                new CallLogAsync.GetLastOutgoingCallArgs(
                    getActivity(),
                    new CallLogAsync.OnLastOutgoingCallComplete() {
                        public void lastOutgoingCall(String number) {
                            // TODO: Filter out emergency numbers if
                            // the carrier does not want redial for
                            // these.
                        	//Gionee:huangzy 20121023 modify for CR00717200 start
                            /*mLastNumberDialed = number;*/
                        	mLastNumberDialed = "-1".equals(number) ? EMPTY_NUMBER : number;
                        	//Gionee:huangzy 20121023 modify for CR00717200 end
                            updateDialAndDeleteButtonEnabledState();
                        }
                    });
        mCallLog.getLastOutgoingCall(lastCallArgs);
    }

    // Helpers for the call intents.
    private Intent newVoicemailIntent() {
        final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED,
                                         Uri.fromParts("voicemail", EMPTY_NUMBER, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    private Intent newFlashIntent() {
        final Intent intent = IntentFactory.newDialNumberIntent(EMPTY_NUMBER, 
        		IntentFactory.DIAL_NUMBER_INTENT_NORMAL, false);
        intent.putExtra(EXTRA_SEND_EMPTY_FLASH, true);
        return intent;
    }

    /* below are added by mediatek .Inc */
    private static final int DIAL_NUMBER_INTENT_NORMAL = 0;
    private static final int DIAL_NUMBER_INTENT_IP = 1;
    private static final int DIAL_NUMBER_INTENT_VIDEO = 2;

    private static final int MSG_DIALER_SEARCH_CONTROLLER_ON_RESUME = 0;
    private static final int MSG_GET_TEXT_WATCHER = 1;

    enum FragmentState {
        UNKNOWN,
        CREATED,
        RESUMED,
        PAUSED,
        STOPPED,
        DESTROYED
    }

    private static final String ACTION = "com.android.phone.OutgoingCallReceiver";

    private FragmentState mFragmentState = FragmentState.UNKNOWN;

    private ImageView mDialpadButton;
    private View mAddToContactButton;

    private AuroraListView mListView;
    private View mDivider;
    private View mDialpadDivider;
  
    //Gionee:huangzy 20121011 add for CR00710695 start    
    /*private DialerSearchController mDialerSearchController;*/
    private IDialerSearchController mDialerSearchController;
    //Gionee:huangzy 20121011 add for CR00710695 end
    
    private SpeedDial mSpeedDial;
    private TextWatcher mTextWatcher;
    private boolean mLaunch = true;

    public void dialButtonPressed() {
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
        dialButtonPressedInner(getDigitsText(), DIAL_NUMBER_INTENT_NORMAL);
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
    }

    public void dialButtonPressed(int type) {
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
        dialButtonPressedInner(getDigitsText(), type);
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
    }

    public void dialButtonPressed(String number) {
        dialButtonPressedInner(number, DIAL_NUMBER_INTENT_NORMAL);
    }

    protected void dialButtonPressedInner(String number, int type) {
        if(DBG)log("dialButtonPressedInner number: "+ number + "type:"+type);
        if (TextUtils.isEmpty(number)) { // No number entered.
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                // This is really CDMA specific. On GSM is it possible
                // to be off hook and wanted to add a 3rd party using
                // the redial feature.
                startActivity(newFlashIntent());
            } else {
                if (!TextUtils.isEmpty(mLastNumberDialed)) {
                    // Recall the last number dialed.
                    //mDigits.setText(mLastNumberDialed);
                	Editable digits = mDigits.getText();
                    digits.replace(0, digits.length(), mLastNumberDialed); //aurora change zhouxiaobing 20130926
                    
                    // ...and move the cursor to the end of the digits string,
                    // so you'll be able to delete digits using the Delete
                    // button (just as if you had typed the number manually.)
                    //
                    // Note we use mDigits.getText().length() here, not
                    // mLastNumberDialed.length(), since the EditText widget now
                    // contains a *formatted* version of mLastNumberDialed (due to
                    // mTextWatcher) and its length may have changed.

                    // gionee xuhz 20120508 add  start                    
                    mDigits.setCursorVisible(true);
                    mDigits.requestFocus();
                    //mDigits.setSelection(0);
                   // mDigits.setSelection(mLastNumberDialed.length());
                    afterTextChanged(digits);//aurora change zhouxiaobing 20130926
                    // gionee xuhz 20120508 add  end
                } else {
                    // There's no "last number dialed" or the
                    // background query is still running. There's
                    // nothing useful for the Dial button to do in
                    // this case.  Note: with a soft dial button, this
                    // can never happens since the dial button is
                    // disabled under these conditons.
                    playTone(ToneGenerator.TONE_PROP_NACK);
                }
            }
        } else {
            // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
            // test equipment.
            // TODO: clean it up.
            if (number != null
                    && !TextUtils.isEmpty(mProhibitedPhoneNumberRegexp)
                    && number.matches(mProhibitedPhoneNumberRegexp)
                    && (SystemProperties.getInt("persist.radio.otaspdial", 0) != 1)) {
                Log.i(TAG, "The phone number is prohibited explicitly by a rule.");
                if (getActivity() != null) {
                    DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                                    R.string.dialog_phone_call_prohibited_title);
                    dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
                }

                // Clear the digits just in case.
                clearDigits();
            } else {
            	// gionee xuhz 20121215 modify for Dual Sim Select start
            	Intent intent;
           
                intent = IntentFactory.newDialNumberIntent(number, type);
    		    //aurora change liguangyu 20131208 start
                if(intent != null) {                    	
                	intent.putExtra("contactUri", getDialContactUri(number));
                }
    	        //aurora add liguangyu 20131206 end
    			//aurora change liguangyu 20131208 end
  
                
                if (getActivity() instanceof AuroraDialtactsActivityV2) {
                    intent.putExtra(AuroraDialtactsActivityV2.EXTRA_CALL_ORIGIN,
                            AuroraDialtactsActivityV2.CALL_ORIGIN_DIALTACTS);
                }
                clearDigits();
                startActivity(intent);
            }
        }
    }

	//GIONEE:liuying 2012-7-5 modify for CR00637517 start
    public void GNdialButtonPressed(int simId) {
        GNdialButtonPressedInner(mDigits.getText().toString(), DIAL_NUMBER_INTENT_NORMAL, simId);
    }
	
    protected void GNdialButtonPressedInner(String number, int type, int simId) {
        if(DBG)log("dialButtonPressedInner number: "+ number + " type:"+type +" simId:"+simId);
        if (TextUtils.isEmpty(number)) { // No number entered.
            if (ContactsUtils.phoneIsCdma() && ContactsUtils.phoneIsOffhook()) {
                // This is really CDMA specific. On GSM is it possible
                // to be off hook and wanted to add a 3rd party using
                // the redial feature.
                startActivity(newFlashIntent());
            } else {
                if (!TextUtils.isEmpty(mLastNumberDialed)) {
                    // Recall the last number dialed.
                    //mDigits.setText(mLastNumberDialed);
                	Editable digits = mDigits.getText();
                    digits.replace(0, digits.length(), mLastNumberDialed); //aurora change zhouxiaobing 20130926
                    
                    // ...and move the cursor to the end of the digits string,
                    // so you'll be able to delete digits using the Delete
                    // button (just as if you had typed the number manually.)
                    //
                    // Note we use mDigits.getText().length() here, not
                    // mLastNumberDialed.length(), since the EditText widget now
                    // contains a *formatted* version of mLastNumberDialed (due to
                    // mTextWatcher) and its length may have changed.

                    // gionee xuhz 20120508 add  start                    
                    mDigits.setCursorVisible(true);
                    mDigits.requestFocus();
                    //mDigits.setSelection(0);
                   // mDigits.setSelection(mLastNumberDialed.length());
                    afterTextChanged(digits);//aurora change zhouxiaobing 20130926
                    // gionee xuhz 20120508 add  end
                } else {
                    // There's no "last number dialed" or the
                    // background query is still running. There's
                    // nothing useful for the Dial button to do in
                    // this case.  Note: with a soft dial button, this
                    // can never happens since the dial button is
                    // disabled under these conditons.
                    playTone(ToneGenerator.TONE_PROP_NACK);
                }
            }
        } else {
            // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
            // test equipment.
            // TODO: clean it up.
            if (number != null
                    && !TextUtils.isEmpty(mProhibitedPhoneNumberRegexp)
                    && number.matches(mProhibitedPhoneNumberRegexp)
                    && (SystemProperties.getInt("persist.radio.otaspdial", 0) != 1)) {
                Log.i(TAG, "The phone number is prohibited explicitly by a rule.");
                if (getActivity() != null) {
                    DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
                                    R.string.dialog_phone_call_prohibited_title);
                    dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
                }

                // Clear the digits just in case.
                clearDigits();
            } else {
                final Intent intent = ContactsUtils.getCallNumberIntent(number,simId,type);//IntentFactory.newDialNumberIntent(number, type);//aurora change zhouxiaobing 20140513
                if (getActivity() instanceof AuroraDialtactsActivityV2) {
                    intent.putExtra(AuroraDialtactsActivityV2.EXTRA_CALL_ORIGIN,
                            AuroraDialtactsActivityV2.CALL_ORIGIN_DIALTACTS);
                }
                intent.putExtra(ContactsUtils.getSlotExtraKey(), simId);
				if (simId == ContactsFeatureConstants.GEMINI_SIM_1){
					intent.putExtra("DialFromSIM1", true);
				} else {
					intent.putExtra("DialFromSIM1", false);
				}
				ContactsLog.log("number1="+number);
				
				//aurora add liguangyu 20140526 for BUG #5099  start
                if(intent != null) {                    	
                	intent.putExtra("contactUri", getDialContactUri(number));
                }
                clearDigits();
                //aurora add liguangyu 20140526 for BUG #5099  end
				startActivity(intent);//aurora change zhouxiaobing 20140513
            }
        }
    }
	//GIONEE:liuying 2012-7-5 modify for CR00637517 end	
    
    public boolean isDialpadShowing() {    	
    	return //null != mDialpadFlipper && mDialpadFlipper.getVisibility() == View.VISIBLE &&
    		null != mDigitKeyboard && mDigitKeyboard.getVisibility() == View.VISIBLE ;
    }
    
    public void showAllDialpad(boolean show) {
    	showDialpad(show, false);

    }

    public boolean isYellowPagesShowing() {    	
    	return //null != mDialpadFlipper && mDialpadFlipper.getVisibility() == View.VISIBLE &&
    		null != mYellowPages && mYellowPages.getVisibility() == View.VISIBLE ;
    }
    
    private Animation mYellowPagesShowAnim = null;
    private Animation mYellowPagesHideAnim = null;
    public void showYellowPages(boolean withAnim){
        if (mIsYellowPagesAniming) {
            return;
        }
        mIsYellowPagesAniming = true;
        if(withAnim){
            if (mYellowPages.getVisibility() == View.VISIBLE) {
            	mIsYellowPagesAniming = false;
                return;
            } else {
                int animDuration = YELLOW_PAGES_ANIM_DURATION;
        		if(mYellowPagesShowAnim == null){
        			int animMoveY = fragmentView.getHeight();
        			mYellowPagesShowAnim = new TranslateAnimation(0.0f, 0.0f, animMoveY, 0.0f);   
        			mYellowPagesShowAnim.setInterpolator(new DecelerateInterpolator());//aurora change zhouxiaobing 20140314
        			mYellowPagesShowAnim.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        	mYellowPages.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                        	mIsYellowPagesAniming = false;
                    		if(Build.VERSION.SDK_INT == 19
                    				|| Build.VERSION.SDK_INT >= 21) {

                    			ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), true);
                    		}
                        }
                    });
        			mYellowPagesShowAnim.setDuration(animDuration);
        		}
        		mYellowPages.startAnimation(mYellowPagesShowAnim);
            }
        } else {
    		mYellowPages.setVisibility(View.VISIBLE);
    		mIsYellowPagesAniming = false;
    		if(Build.VERSION.SDK_INT == 19
    				|| Build.VERSION.SDK_INT >= 21) {

    			ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), true);
    		}
        }
    }

    public void hideYellowPages(boolean withAnim){
        if (mIsYellowPagesAniming) {
            return;
        }
        mIsYellowPagesAniming = true;
    	if(withAnim){
            if (mYellowPages.getVisibility() == View.GONE) {
            	mIsYellowPagesAniming = false;
                return;
            } else {
                int animDuration = YELLOW_PAGES_ANIM_DURATION;
        		if(mYellowPagesHideAnim == null){
                    int animMoveY = mYellowPages.getHeight();
                    mYellowPagesHideAnim = new TranslateAnimation(0.0f, 0.0f, 0.0f, animMoveY);
                    mYellowPagesHideAnim.setInterpolator(new DecelerateInterpolator());
                    mYellowPagesHideAnim.setAnimationListener(new AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }
                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            
                        }
                        @Override
                        public void onAnimationEnd(Animation animation) {
                        	mIsYellowPagesAniming = false;
                        	mYellowPages.setVisibility(View.GONE);
                        }
                    });
                    mYellowPagesHideAnim.setDuration(animDuration);
        		}
        		mYellowPages.startAnimation(mYellowPagesHideAnim);
            }
    	} else {
    		mYellowPages.setVisibility(View.GONE);
    		mIsYellowPagesAniming = false;
    	}
		if(Build.VERSION.SDK_INT == 19
				|| Build.VERSION.SDK_INT >= 21) {

			ChangeStatusBar.changeStatusBar((AuroraActivity) getActivity(), false);
		}
    }
    
    public void showDialpad(boolean show, boolean withAnim) {
    	//Gionee <huangzy> <2013-06-07> add for CR00822902 begin
    	Log.i("James", "showDialpad begin");
    	//Gionee <huangzy> <2013-06-07> add for CR00822902 end
    	
    	if(getActivity() == null) {
    		return;
    	}
    	
        if (mIsDialpadAniming) {
            return;
        }
        
        final View animView = mDigitKeyboard;
        final int visibility = show ? View.VISIBLE : View.GONE;
        if (animView.getVisibility() == visibility) {
            return;
        }
        
        mIsDialpadAniming = true;
        
        int animDuration = withAnim ? ANIM_DURATION : 0;
    	int animMoveY = getResources().getDimensionPixelOffset(R.dimen.aurora_dialpad_height); 
        
        if (!show) {
            if (null == mKeySectionHideAnim) {
                animMoveY = animView.getHeight();
                mKeySectionHideAnim = new TranslateAnimation(0.0f, 0.0f, 0.0f, animMoveY);
                mKeySectionHideAnim.setInterpolator(new DecelerateInterpolator());//aurora change zhouxiaobing 20140314
                mKeySectionHideAnim.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        mDigits.setCursorVisible(false);//aurora change zhouxiaobing 20131028
				        //Gionee <huangzy> <2013-04-13> remove for CR00786343 start
                        mListView.setPadding(0, 0, 0, 0);
				        //Gionee <huangzy> <2013-04-13> remove for CR00786343 start
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                    	animView.setVisibility(visibility);
                    }
                });
            }
            

            mKeySectionHideAnim.setDuration(animDuration);
            animView.startAnimation(mKeySectionHideAnim);        
        } else {            
            if (null == mKeySectionShowAnim) {
                mKeySectionShowAnim = new TranslateAnimation(0.0f, 0.0f, animMoveY, 0.0f);   
                mKeySectionShowAnim.setInterpolator(new DecelerateInterpolator());//aurora change zhouxiaobing 20140314
                mKeySectionShowAnim.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mDigits.setCursorVisible(false);
				         //Gionee <huangzy> <2013-04-13> remove for CR00786343 start
                    	int padding = getResources().getDimensionPixelOffset(R.dimen.aurora_dialpad_height);                     
                        mListView.setPadding(0, 0, 0, padding);
				         //Gionee <huangzy> <2013-04-13> remove for CR00786343 end
                        //aurora move liguangyu 20131113 start
                    	animView.setVisibility(visibility);
                    	//aurora move liguangyu 20131113 end
                    }
                });
            }
            

            mKeySectionShowAnim.setDuration(animDuration);
            animView.startAnimation(mKeySectionShowAnim);
        }
        
        //Gionee <huangzy> <2013-06-07> add for CR00822902 begin
    	Log.i("James", "showDialpad end  show = " + show);
    	//Gionee <huangzy> <2013-06-07> add for CR00822902 end
        mIsDialpadAniming = false;
    }

    public boolean onLongClickInternal(View view) {
        int key = -1;
        switch(view.getId()) {
        	case R.id.aurora_deleteButton_msim:
            case R.id.aurora_deleteButton:
                clearDigits();
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                mDeleteMsim.setPressed(false);
                
                //Gionee:huangzy 20130325 remove for CR00788980 start
                /*mAutoScaleTextSizeWatcher.trigger(true);*/
                //Gionee:huangzy 20130325 remove for CR00788980 end
                return true;
            case R.id.aurora_two:
                key = 2;
                break;
            case R.id.aurora_three:
                key = 3;
                break;
            case R.id.aurora_four:
                key = 4;
                break;
            case R.id.aurora_five:
                key = 5;
                break;
            case R.id.aurora_six:
                key = 6;
                break;
            case R.id.aurora_seven:
                key = 7;
                break;
            case R.id.aurora_eight:
                key = 8;
                break;
            case R.id.aurora_nine:
                key = 9;
                break;
            case R.id.aurora_star: {
            	return gnOnStarLongClick();
            }
            default:
                return false;
        }

        if(!(ContactsApplication.sSpeedDial && isDigitsEmpty()))
            return false;
        if(DBG) log("onLongClickInternal key: "+ key);
        return mSpeedDial.dial(key);
    }

    protected boolean onClickInternal(View view) {
        switch(view.getId()) {
            case R.id.deleteButton:
                keyPressed(KeyEvent.KEYCODE_DEL);
                //Gionee:huangzy 20130325 remove for CR00788980 start
                /*mAutoScaleTextSizeWatcher.trigger(true);*/
                //Gionee:huangzy 20130325 remove for CR00788980 end
                return true;
            case R.id.addToContact:
                if(!isDigitsEmpty()) {
                    //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
                	startActivity(IntentFactory.newInsert2ExistContactIntent(getDigitsText()));                   
                    //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
                }
                return true;
            default:
                return false;
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        //
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null)
                mDialerSearchController.onScrollStateChanged(view, scrollState);
        }
    }

    private boolean isVoicemailAvailableProxy() {
        if(GNContactsUtils.isMultiSimEnabled()) {
            // OutgoingCallBroadcaster will do the stuffs
            // just return true
            if(ContactsUtils.getActiveSubscriptionInfoCount() == 0)
                return false;

            final long defaultSim = Settings.System.getLong(getActivity().getContentResolver(),
                    ContactsFeatureConstants.VOICE_CALL_SIM_SETTING, ContactsFeatureConstants.DEFAULT_SIM_NOT_SET);

            if(defaultSim == ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET)
                return false;

            return true;
        }

        return isVoicemailAvailable();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        log("KeyEvent = " + event.getKeyCode() + "mDigits.hasFocus(): " + mDigits.hasFocus() + "keyCode " + keyCode);
        switch (keyCode) {
		case KeyEvent.KEYCODE_CALL:
			dialButtonPressed();
            return true;
		case KeyEvent.KEYCODE_BACK:
			mClearDigitsOnStop = true;
			return false;
		}

        // focus the mDigits and let it handle the key events
        if(!isTrackBallEvent(event)) {
            if(!ContactsUtils.phoneIsOffhook() && mDigits.getVisibility() != View.VISIBLE) {
                mDigits.setVisibility(View.VISIBLE);
            }

            if(!mDigits.hasFocus()) {
                mDigits.requestFocus();
                return mDigits.onKeyDown(keyCode, event);
            }
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_1: {
                playTone(ToneGenerator.TONE_DTMF_1);
                keyPressed(KeyEvent.KEYCODE_1);
                return true;
            }
            case KeyEvent.KEYCODE_2: {
                playTone(ToneGenerator.TONE_DTMF_2);
                keyPressed(KeyEvent.KEYCODE_2);
                return true;
            }
            case KeyEvent.KEYCODE_3: {
                playTone(ToneGenerator.TONE_DTMF_3);
                keyPressed(KeyEvent.KEYCODE_3);
                return true;
            }
            case KeyEvent.KEYCODE_4: {
                playTone(ToneGenerator.TONE_DTMF_4);
                keyPressed(KeyEvent.KEYCODE_4);
                return true;
            }
            case KeyEvent.KEYCODE_5: {
                playTone(ToneGenerator.TONE_DTMF_5);
                keyPressed(KeyEvent.KEYCODE_5);
                return true;
            }
            case KeyEvent.KEYCODE_6: {
                playTone(ToneGenerator.TONE_DTMF_6);
                keyPressed(KeyEvent.KEYCODE_6);
                return true;
            }
            case KeyEvent.KEYCODE_7: {
                playTone(ToneGenerator.TONE_DTMF_7);
                keyPressed(KeyEvent.KEYCODE_7);
                return true;
            }
            case KeyEvent.KEYCODE_8: {
                playTone(ToneGenerator.TONE_DTMF_8);
                keyPressed(KeyEvent.KEYCODE_8);
                return true;
            }
            case KeyEvent.KEYCODE_9: {
                playTone(ToneGenerator.TONE_DTMF_9);
                keyPressed(KeyEvent.KEYCODE_9);
                return true;
            }
            case KeyEvent.KEYCODE_0: {
                playTone(ToneGenerator.TONE_DTMF_0);
                keyPressed(KeyEvent.KEYCODE_0);
                return true;
            }
            case KeyEvent.KEYCODE_POUND: {
                playTone(ToneGenerator.TONE_DTMF_P);
                keyPressed(KeyEvent.KEYCODE_POUND);
                return true;
            }
            case KeyEvent.KEYCODE_STAR: {
                playTone(ToneGenerator.TONE_DTMF_S);
                keyPressed(KeyEvent.KEYCODE_STAR);
                return true;
            }
            case KeyEvent.KEYCODE_DEL: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return true;
            }

        }
        return false;
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    boolean isTrackBallEvent(KeyEvent event) {
        int keycode = event.getKeyCode();
        return keycode == KeyEvent.KEYCODE_DPAD_LEFT || keycode == KeyEvent.KEYCODE_DPAD_UP
                || keycode == KeyEvent.KEYCODE_DPAD_RIGHT || keycode == KeyEvent.KEYCODE_DPAD_DOWN;
    }

    private boolean isDialpadChooserVisible() {
        return mDialpadChooser != null && mDialpadChooser.getVisibility() == View.VISIBLE;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_DIALER_SEARCH_CONTROLLER_ON_RESUME:
                if (mDialerSearchController != null) {
                    mDialerSearchController.onResume();
                    Log.i("James", "mDialerSearchController.onResume()");
                }
                break;
                case MSG_GET_TEXT_WATCHER:
                if (msg.obj instanceof TextWatcher)
                    mTextWatcher = (TextWatcher) msg.obj;
                break;
                //aurora add zhouxiaobing 20131122 start
                case 2:
                	setImageView();
                	break;
                //aurora add zhouxiaobing 20131122 end
            }
        }
    };
//aurora zhouxiaobing add 20131122 start
public void setImageView()
{
	ImageView iv1=(ImageView)fragmentView.findViewById(R.id.aurora_one);
	iv1.setBackgroundResource(R.drawable.aurora_dial_num_1x);
	ImageView iv2=(ImageView)fragmentView.findViewById(R.id.aurora_two);
	iv2.setBackgroundResource(R.drawable.aurora_dial_num_2x);
	ImageView iv3=(ImageView)fragmentView.findViewById(R.id.aurora_three);
	iv3.setBackgroundResource(R.drawable.aurora_dial_num_3x);
	ImageView iv4=(ImageView)fragmentView.findViewById(R.id.aurora_four);
	iv4.setBackgroundResource(R.drawable.aurora_dial_num_4x);
	ImageView iv5=(ImageView)fragmentView.findViewById(R.id.aurora_five);
	iv5.setBackgroundResource(R.drawable.aurora_dial_num_5x);
	ImageView iv6=(ImageView)fragmentView.findViewById(R.id.aurora_six);
	iv6.setBackgroundResource(R.drawable.aurora_dial_num_6x);
	ImageView iv7=(ImageView)fragmentView.findViewById(R.id.aurora_seven);
	iv7.setBackgroundResource(R.drawable.aurora_dial_num_7x);
	ImageView iv8=(ImageView)fragmentView.findViewById(R.id.aurora_eight);
	iv8.setBackgroundResource(R.drawable.aurora_dial_num_8x);
	ImageView iv9=(ImageView)fragmentView.findViewById(R.id.aurora_nine);
	iv9.setBackgroundResource(R.drawable.aurora_dial_num_9x);
	ImageView iv0=(ImageView)fragmentView.findViewById(R.id.aurora_zero);
	iv0.setBackgroundResource(R.drawable.aurora_dial_num_0x);
	ImageView ivstar=(ImageView)fragmentView.findViewById(R.id.aurora_star);
	ivstar.setBackgroundResource(R.drawable.aurora_dial_num_starx);
	ImageView ivpound=(ImageView)fragmentView.findViewById(R.id.aurora_pound);
	ivpound.setBackgroundResource(R.drawable.aurora_dial_num_poundx);
	ImageView ivadd=(ImageView)fragmentView.findViewById(R.id.aurora_addcontacts);
	ivadd.setBackgroundResource(R.drawable.aurora_dial_addcontactx);
	ImageView ivadd_msim=(ImageView)fragmentView.findViewById(R.id.aurora_addcontacts_msim);
	ivadd_msim.setBackgroundResource(R.drawable.aurora_dial_addcontactx_msim);
	ImageView ivdial0=(ImageView)fragmentView.findViewById(R.id.aurora_dialButton_0);
	ivdial0.setBackgroundResource(R.drawable.aurora_dial_callx_0);
	ImageView ivdial1=(ImageView)fragmentView.findViewById(R.id.aurora_dialButton_1);
	ivdial1.setBackgroundResource(R.drawable.aurora_dial_callx_1);
	ImageView ivdial=(ImageView)fragmentView.findViewById(R.id.aurora_dialButton);
	ivdial.setBackgroundResource(R.drawable.aurora_dial_callx);
	ImageView ivdel=(ImageView)fragmentView.findViewById(R.id.aurora_deleteButton);
	ivdel.setBackgroundResource(R.drawable.aurora_dial_delx);	
	ImageView ivdel_msim=(ImageView)fragmentView.findViewById(R.id.aurora_deleteButton_msim);
	ivdel_msim.setBackgroundResource(R.drawable.aurora_dial_delx_msim);	
	//aurora liguangyu add 20141218 begin    
	showDialpad(true, true);
	//aurora liguangyu add 20141218 end    
}
//aurora zhouxiaobing add 20121122 end    
    public void updateDialerSearch(){
        mDialerSearchController.updateDialerSearch();
    }
    
    // *******************************above is mtk****************************************
     
    private TextView mNumArea;
     
    private TextView mNoMatchView;
     
     
    private View mAddContactView, mAddContactViewMsim;
    
    private CallLogObserver mCallLogObserver;
    
    @Override
	public void onDialerSearchResult(DialerSearchResult dialerSearchResult) {
		int resultCount = dialerSearchResult.getCount();
		int digitsLen = getDigitsTextLen();		
		Log.v(TAG, "onDialerSearchResult resultCount="+resultCount);
		if (0 == resultCount) {
	        switchEmptyView(true);
		} else {
		    switchEmptyView(false);
		}
	}
    //aurora change liguangyu 20131128 start
    public String getDigitsText() {
    //aurora change liguangyu 20131128 end
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
        if (null == mDigits) {
            return null;
        }
        
        String text = mDigits.getText().toString();
        text = text.replace("P", ",");
        text = text.replace("W", ";");
		return text;
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
	}
    
    public int getDigitsTextLen() {
		return (null == mDigits ? "" : mDigits.getText().toString()).length();
	}
    
    protected void switchEmptyView(boolean flag) {
        if (flag) {
            if (View.VISIBLE != mNoMatchView.getVisibility() && getDigitsTextLen() > 1) {
                mNoMatchView.setVisibility(View.VISIBLE);
            }
        } else {
            mNoMatchView.setVisibility(View.GONE);
        }
        
        if (getDigitsTextLen() == 0 && mNoMatchView != null) {
            mNoMatchView.setVisibility(View.GONE);
        }
    }
    
    private Animation mKeySectionShowAnim = null;
    private Animation mKeySectionHideAnim = null;
    private final int ANIM_DURATION = 250;
    private final int YELLOW_PAGES_ANIM_DURATION = 250;
    private boolean mIsDialpadAniming = false;
    private boolean mIsYellowPagesAniming = false;

	//Gionee:huangzy 20130319 add for CR00786102 start
    public Rect getHideDialpadTouchRect() {
    	if (null == mDigits) {
    		return null;
    	}
    	
    	Rect rect = new Rect();
    	mDigits.getGlobalVisibleRect(rect);
    	rect = new Rect(0, 0, rect.right, rect.top);
    	
    	return rect; 
    }
	//Gionee:huangzy 20130319 add for CR00786102 end
    
    public Rect getKeyBoardFlipperRect() {
    	
    	Rect rect = new Rect();
    	return rect; 
    }
    
    public Rect getDialpadFlipperRect() {
    	if (null == mDigitKeyboard) {
    		return null;
    	}
    	
    	Rect rect = new Rect();
    	mDigitKeyboard.getGlobalVisibleRect(rect);
    	return rect; 
    }
    
    private boolean gnIsVisible(View view) {
    	if (null == view)
    		return false;
    	
    	return view.getVisibility() == View.VISIBLE;
    }
    
    private void gnSetVisible(View view, int visibility) {
        if (null != view && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }
    
    private EditTextSwitchAdder mEditTextSwitchAdder = null;
    protected boolean gnOnStarLongClick() {		
		// gionee xuhz 20120507 add for CR00589238 start
		if (mDigits.getSelectionStart() <= 0) {
		    return false;
		}
		// gionee xuhz 20120507 add for CR00589238 end
		
		//Gionee:huangzhiyuan 20120507 add for CR00585981 start
		//aurora change liguangyu 20131116 for BUG #780 start
//		new EditTextSwitchAdder(mDigits, mHandler, new String[]{"P", "W"}) {	
		mEditTextSwitchAdder = new EditTextSwitchAdder(mDigits, mHandler, new String[]{",", ";"}) {
		//aurora change liguangyu 20131116 for BUG #780 end
			@Override
			public boolean isGoingOn() {
				return mUpdateDialpadButtonlistener.isPressing();
			}
		};
		mEditTextSwitchAdder.run();
		//Gionee:huangzhiyuan 20120507 add for CR00585981 end
		
		return true;
	}
    
    protected void setDigitsContainerVisible(boolean visible) {
        
    }

    class CallLogObserver extends ContentObserver {
        public CallLogObserver(Handler handler) {
            super(handler);            
        }
        
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            
            // Query the last dialed number. Do it first because hitting
            // the DB is 'slow'. This call is asynchronous.
            queryLastOutgoingCall();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();
        
        mCallLogObserver = new CallLogObserver(mHandler);
        getActivity().getContentResolver().registerContentObserver(
                Calls.CONTENT_URI, false, mCallLogObserver);
    }
    
    //Gionee:huangzy 20120731 add CR00614808 for start
    private void clearDigits() {
    	if(mDigits != null && mDigits.length() > 0) {
    		mDigits.getText().clear();
    		mDigits.setCursorVisible(false);
    	}
    	
    	if (mNumArea != null && mNumArea.isShown()) {
            mNumArea.setVisibility(View.GONE);
            //aurora add liguangyu 20140108 for bug #1781 start
            mNumArea.setText("");
            //aurora add liguangyu 20140108 for bug #1781 end
        }
    	
    	if (mNoMatchView != null && mNoMatchView.isShown()) {
    	    mNoMatchView.setVisibility(View.GONE);
    	}
    }
    //Gionee:huangzy 20120731 add CR00614808 for end
    
    private boolean isActivityFinishing() {
    	return (null == getActivity() || getActivity().isFinishing());
    }
    //Gionee:huangzy 20120815 add for CR00663853 end
    
	//Gionee:huangzy 20121011 add for CR00710695 start
    @Override
	public void onGnDialerSearchCompleted(int count) {
		Log.v(TAG, "onGnDialerSearchCompleted");
		
		if (0 == count) {
	        switchEmptyView(true);
	      //aurora add zhouxiaobing 20131028 start     
	        //aurora change liguangyu 20131112  start
	        if(this.getDigitsTextLen()>=3) {
	        //aurora change liguangyu 20131112  end
	            mAddContactView.setEnabled(true);
	            mAddContactViewMsim.setEnabled(true);
	        }
	        else {
	            mAddContactView.setEnabled(false);
	            mAddContactViewMsim.setEnabled(false);
	        }	
	      //aurora add zhouxiaobing 20131028 end      
		}
	//aurora add zhouxiaobing 20131028 start	
		else
		{
		    switchEmptyView(false);
		    //aurora change liguangyu 20131208 start
			if(((AuroraDialerSearchController)mDialerSearchController).hasContact() || this.getDigitsTextLen() < 3 ) {
				   mAddContactView.setEnabled(false);
		           mAddContactViewMsim.setEnabled(false);
			} else {
		    	   mAddContactView.setEnabled(true);
		           mAddContactViewMsim.setEnabled(true);
			}
			//aurora change liguangyu 20131208 end
		}
	//aurora add zhouxiaobing 20131028 end		
	}
	//Gionee:huangzy 20121011 add for CR00710695 end

    //Gionee:xuhz 20121014 add for CR00686812 start
    private static SparseIntArray DIGIT_ID_TO_EVENT_KEY = new SparseIntArray();
    private static SparseIntArray KEYCODE_TO_TONE_DTMF = new SparseIntArray();
    static {
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_one, KeyEvent.KEYCODE_1);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_two, KeyEvent.KEYCODE_2);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_three, KeyEvent.KEYCODE_3);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_four, KeyEvent.KEYCODE_4);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_five, KeyEvent.KEYCODE_5);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_six, KeyEvent.KEYCODE_6);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_seven, KeyEvent.KEYCODE_7);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_eight, KeyEvent.KEYCODE_8);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_nine, KeyEvent.KEYCODE_9);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_zero, KeyEvent.KEYCODE_0);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_star, KeyEvent.KEYCODE_STAR);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_pound, KeyEvent.KEYCODE_POUND);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_deleteButton, KeyEvent.KEYCODE_DEL);
    	DIGIT_ID_TO_EVENT_KEY.put(R.id.aurora_deleteButton_msim, KeyEvent.KEYCODE_DEL);
    	
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_1, ToneGenerator.TONE_DTMF_1);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_2, ToneGenerator.TONE_DTMF_2);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_3, ToneGenerator.TONE_DTMF_3);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_4, ToneGenerator.TONE_DTMF_4);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_5, ToneGenerator.TONE_DTMF_5);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_6, ToneGenerator.TONE_DTMF_6);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_7, ToneGenerator.TONE_DTMF_7);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_8, ToneGenerator.TONE_DTMF_8);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_9, ToneGenerator.TONE_DTMF_9);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_0, ToneGenerator.TONE_DTMF_0);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_STAR, ToneGenerator.TONE_DTMF_S);
    	KEYCODE_TO_TONE_DTMF.put(KeyEvent.KEYCODE_POUND, ToneGenerator.TONE_DTMF_P);
    }
    
    private void setKeyboardClickListener() {
    	if (null != mDigitKeyboard) {
    		for (int i = 0, size = DIGIT_ID_TO_EVENT_KEY.size(); i < size; i++) {
    			int id = DIGIT_ID_TO_EVENT_KEY.keyAt(i);
    			View view = mDigitKeyboard.findViewById(id);
    			if (null != view) {
    				view.setOnClickListener(this);
    			}
        	}
    		
    		mDigitKeyboard.findViewById(R.id.aurora_one).setOnLongClickListener(this);
    		mDigitKeyboard.findViewById(R.id.aurora_star).setOnLongClickListener(this);
    		mDigitKeyboard.findViewById(R.id.aurora_zero).setOnLongClickListener(this);
    	    //gionee xuhz 20130226 add for speed dial start
    		if (ContactsApplication.sIsGnSpeedDialSupport) {
        		mDigitKeyboard.findViewById(R.id.aurora_two).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_three).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_four).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_five).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_six).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_seven).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_eight).setOnLongClickListener(this);
        		mDigitKeyboard.findViewById(R.id.aurora_nine).setOnLongClickListener(this);
    		}
    	    //gionee xuhz 20130226 add for speed dial end
    	}
    }
	//Gionee:xuhz 20121014 add for CR00686812 end

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
	
	private void vibrate() {
		mHaptic.vibrate();
	}
	
    //aurora add liguangyu 20131113 for #864 start
    public interface updateDiapadButtonListener {
        void updateDialpadButton(boolean show);
        boolean isPressing();
    }
    
    private updateDiapadButtonListener mUpdateDialpadButtonlistener = null;
        
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	mUpdateDialpadButtonlistener = (updateDiapadButtonListener) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement updateDiapadButtonListener");
        }
    }
    //aurora add liguangyu 20131113 for #864 end
    
//	private final static String TONE_1 = "dialpad/1.ogg";
//	private final static String TONE_2 = "dialpad/2.ogg";
//	private final static String TONE_3 = "dialpad/3.ogg";
//	private final static String TONE_4 = "dialpad/4.ogg";
//	private final static String TONE_5 = "dialpad/5.ogg";
//	private final static String TONE_6 = "dialpad/6.ogg";
//	private final static String TONE_7 = "dialpad/7.ogg";
//	private final static String TONE_8 = "dialpad/8.ogg";
//	private final static String TONE_9 = "dialpad/9.ogg";
//	private final static String TONE_0 = "dialpad/0.ogg";
//	private final static String TONE_STAR = "dialpad/start.ogg";
//	private final static String TONE_POUND = "dialpad/pound.ogg";
    
    private final static String TONE_1 = "system/media/audio/ui/1.ogg";
	private final static String TONE_2 = "system/media/audio/ui/2.ogg";
	private final static String TONE_3 = "system/media/audio/ui/3.ogg";
	private final static String TONE_4 = "system/media/audio/ui/4.ogg";
	private final static String TONE_5 = "system/media/audio/ui/5.ogg";
	private final static String TONE_6 = "system/media/audio/ui/6.ogg";
	private final static String TONE_7 = "system/media/audio/ui/7.ogg";
	private final static String TONE_8 = "system/media/audio/ui/8.ogg";
	private final static String TONE_9 = "system/media/audio/ui/9.ogg";
	private final static String TONE_0 = "system/media/audio/ui/0.ogg";
	private final static String TONE_STAR = "system/media/audio/ui/Asterisk.ogg";
	private final static String TONE_POUND = "system/media/audio/ui/well.ogg";
	private final static String TONE_DEL = "system/media/audio/ui/del.ogg";
	
	private static HashMap<Integer, String> VIEW_TO_FILE = new HashMap<Integer, String>();
    static {
    	VIEW_TO_FILE.put(R.id.aurora_one, TONE_1);
    	VIEW_TO_FILE.put(R.id.aurora_two, TONE_2);
    	VIEW_TO_FILE.put(R.id.aurora_three, TONE_3);
    	VIEW_TO_FILE.put(R.id.aurora_four, TONE_4);
    	VIEW_TO_FILE.put(R.id.aurora_five, TONE_5);
    	VIEW_TO_FILE.put(R.id.aurora_six, TONE_6);
    	VIEW_TO_FILE.put(R.id.aurora_seven, TONE_7);
    	VIEW_TO_FILE.put(R.id.aurora_eight, TONE_8);
    	VIEW_TO_FILE.put(R.id.aurora_nine, TONE_9);
    	VIEW_TO_FILE.put(R.id.aurora_zero, TONE_0);
    	VIEW_TO_FILE.put(R.id.aurora_star, TONE_STAR);
    	VIEW_TO_FILE.put(R.id.aurora_pound, TONE_POUND);
    	VIEW_TO_FILE.put(R.id.aurora_deleteButton, TONE_DEL);
    	VIEW_TO_FILE.put(R.id.aurora_deleteButton_msim, TONE_DEL);
    }
    
    
    private void playMusic(final int viewId) {
    	
		if (!mDTMFToneEnabled) {
			mDTMFToneEnabled = Settings.System.getInt(getActivity().getContentResolver(), Settings.System.DTMF_TONE_WHEN_DIALING, 1) == 1;
			if (!mDTMFToneEnabled) {
				Log.v(TAG, "playMusic = mDTMFToneEnabled false");
				return;
			}
		}
    	
		if (mIsUseSoundPool) {
			AudioManager am = (AudioManager) getActivity().getSystemService(
					Context.AUDIO_SERVICE);
			final int currentVol = am
					.getStreamVolume(AudioManager.STREAM_SYSTEM);
			Log.v(TAG, "playMusic currentVol = " + currentVol);
			new Thread() {
				public void run() {
					// aurora modify liguangyu 20140808 for BUG #7399 start
					Log.v(TAG, "playMusic mIsInitSound = " + mIsInitSound
							+ " soundPool = " + soundPool + " soundPoolMap="
							+ soundPoolMap);
					if (mIsInitSound && soundPool != null
							&& soundPoolMap != null) {
						soundPool.play(soundPoolMap.get(viewId), currentVol,
								currentVol, 1, 0, 1f);
					}
					// aurora modify liguangyu 20140808 for BUG #7399 end
				}
			}.start();
		} else {

			final String path = VIEW_TO_FILE.get(viewId);
			Log.v(TAG, "playMusic path =" + path);
			 new Thread(){
				 public void run(){
					try {
						MediaPlayer mMediaPlayer = new MediaPlayer();
						mMediaPlayer.reset();
						// AssetFileDescriptor afd =
						// getActivity().getAssets().openFd(path);
						// mMediaPlayer.setDataSource(afd.getFileDescriptor(),
						// afd.getStartOffset(), afd.getLength());
						// afd.close();
						mMediaPlayer.setDataSource(path);
						mMediaPlayer.setAudioStreamType(AudioManager.STREAM_SYSTEM);
		
						mMediaPlayer.prepare();
						mMediaPlayer.start();
					} catch (IOException ex) {
						Log.d(TAG, "create failed:", ex);
						// fall through
					} catch (IllegalArgumentException ex) {
						Log.d(TAG, "create failed:", ex);
						// fall through
					} catch (SecurityException ex) {
						Log.d(TAG, "create failed:", ex);
						// fall through
					}
				 }
			 }.start();
		}
            

    }
    
    private HashMap<Integer, Integer> soundPoolMap;
    private SoundPool soundPool;   

    private void updateCallButtonVisbilityAndWidth() {
    	
    	if(getActivity() == null) {
    		return;
    	}
    	
	    boolean showDouble = ContactsUtils.isShowDoubleButton();
//	    mDialButton.setVisibility(simCount>1 ? View.GONE : View.VISIBLE);
//	    mDialButton0.setVisibility(simCount>1 ? View.VISIBLE : View.GONE);
//	    mDialButton1.setVisibility(simCount>1 ? View.VISIBLE : View.GONE);
//    	
//        int w = getActivity().getResources().getDimensionPixelOffset(simCount<2 ? R.dimen.aurora_call_button_width_1 : R.dimen.aurora_call_button_width_2);
//	    LayoutParams para;
//        para = mDelete.getLayoutParams();
//        para.width = w;
//        mDelete.setLayoutParams(para);
//        para = mAddContactView.getLayoutParams();
//        para.width = w;
//        mAddContactView.setLayoutParams(para);
        Log.v("SHIJIAN", "updateCallButtonVisbilityAndWidth =showDouble" + showDouble);   
        //aurora modify liguangyu 20140825 for BUG #7913 start  
	    mDoubleDialLine.setVisibility(showDouble ? View.VISIBLE : View.GONE);
	    mSingleDialLine.setVisibility(showDouble ? View.GONE : View.VISIBLE);	    
        //aurora modify liguangyu 20140825 for BUG #7913 end
	    int padding = getResources().getDimensionPixelOffset(R.dimen.aurora_dialpad_height);
	    float density = getActivity().getResources().getDisplayMetrics().density;
        ViewGroup.LayoutParams params = mDialpadBg.getLayoutParams();
        params.height = padding + (int)(showDouble ? (69 * density - padding/5) : 0);
        mDialpadBg.setLayoutParams(params);
	    if(showDouble) {
//	    	padding += 14 * 4; 
	    	padding += 14 * density; 
	    }
	    if(mDigitKeyboard.getVisibility() != View.VISIBLE) {
	    	mListView.setPadding(0, 0, 0, padding);
	    }
    }
    
	private static final String AURORA_STATE_CHANGED_ACTION = "PhoneServiceStateChanged";
    private final PhoneStateReceiver mPhoneStateReceiver = new PhoneStateReceiver();
    private class PhoneStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {     	
        	updateCallButtonVisbilityAndWidth();
        }
    }
    
    //aurora add liguangyu 20140808 for BUG #7399 start
    private void initSound() {
        log("initSound");
        soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);    
        soundPoolMap = new HashMap<Integer, Integer>();    
        
        Iterator iter = VIEW_TO_FILE.entrySet().iterator();
        while (iter.hasNext()) {
	        Map.Entry entry = (Map.Entry) iter.next();
	        Object viewId = entry.getKey();
	        Object path = entry.getValue();
	        soundPoolMap.put((Integer)viewId, soundPool.load((String)path, 1));
        }
        log("initSound end");
    }
    //aurora add liguangyu 20140808 for BUG #7399 end
    
    public void animationBeforeSetTab() {
    	if(isYellowPagesShowing()){
			mDigitKeyboard.setVisibility(View.GONE);
    		hideYellowPages(true);
    		mDialpadBg.setVisibility(View.VISIBLE);
    		Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_dialpad_bg_out);
      		out.setAnimationListener(new AuroraAnimationListener() {    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				mDialpadBg.setVisibility(View.GONE);
    			}
    		});
      		mDialpadBg.startAnimation(out);
    	} else {
    		mDialpadBg.setVisibility(View.VISIBLE);
    		Animation out;
        	if(mDigitKeyboard.getVisibility() != View.VISIBLE) {
          		out = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_no_dialpad_bg_out);
        	} else {
          		out = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_dialpad_bg_out);
          		Animation alphaOut = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_out);		
          		mDigitKeyboard.startAnimation(alphaOut);	    	
        	}
      		out.setAnimationListener(new AuroraAnimationListener() {    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				mDialpadBg.setVisibility(View.GONE);
    			}
    		});
      		mDialpadBg.startAnimation(out);
    	}
  		mfirstShow = false;
   }
      
     
    private boolean mfirstShow = true;
    public void animationAfterSetTab() {
        boolean isDigitsEmpty = isDigitsEmpty();
        if(mfirstShow) {        	
        	mfirstShow = false;     	
        	return;
        }
    	if(ContactsApplication.sIsAuroraYuloreSupport){
    		if(isYellowPagesShowing()){
    			hideYellowPages(false);
    		}
			updateViewOnResumeAnimation();
    	}
    	mDialpadBg.setVisibility(View.VISIBLE);
		Animation in;
    	if(mDigitKeyboard.getVisibility() != View.VISIBLE && !isDigitsEmpty) {    		                
      		in = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_no_dialpad_bg_in);    
     		in.setStartOffset(0);         		
      		in.setAnimationListener(new AuroraAnimationListener() {    			
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				mDialpadBg.setVisibility(View.GONE);
    			}
    		});
    	} else {
    		if(isDigitsEmpty) {	
    			updateViewOnResumeAnimation();
    		}
    		in = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_dialpad_bg_in);
    		in.setStartOffset(0);        		
	  		in.setAnimationListener(new AuroraAnimationListener() {				
				
				@Override
				public void onAnimationEnd(Animation animation) {
					mHandler.postDelayed(new Runnable(){
						public void run(){
							mDialpadBg.setVisibility(View.GONE);
						}
					}, 250);
				}
			});
	  		Animation alphaIn = AnimationUtils.loadAnimation(getActivity(), R.anim.aurora_alpha_in);
			alphaIn.setStartOffset(250); 
	  		mDigitKeyboard.startAnimation(alphaIn);
  		}    
  		mDialpadBg.startAnimation(in);
    }
    
    
    private void updateViewOnResumeAnimation() {
        if (mDigitKeyboard.getVisibility() != View.VISIBLE) {    		       
            mDigits.setCursorVisible(false);
        	int padding = getResources().getDimensionPixelOffset(R.dimen.aurora_dialpad_height);                     
            mListView.setPadding(0, 0, 0, padding);
            mDigitKeyboard.setVisibility(View.VISIBLE);
        }
		    			
        resetDialpadButton();
    }
    
     
    private boolean mIsInitSound = false; 
    private class InitSoundTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... voids) {
        	initSound();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Log.v(TAG, "onPostExecute = " + result);   
        	mIsInitSound = result;
        }
    }
    
    private class AuroraAnimationListener implements Animation.AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {
		}
		
		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
		@Override
		public void onAnimationEnd(Animation animation) {
		}
    }
    
    private Uri getDialContactUri(String number) {
        Uri contactUri = ((AuroraDialerSearchController)mDialerSearchController).getContactUri();
		if(contactUri == null) {
			contactUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));        		
		}
    	return contactUri;
    }    
    
    private ContentObserver mSimModeObserver = new ContentObserver(new Handler()) {
   	 public void onChange(boolean selfChange){
   	       Log.i(TAG, "mSimModeObserver onChange :");
   	       super.onChange(selfChange);
   	       updateCallButtonVisbilityAndWidth();
   	 }
   };
   
   public void onSubscriptionsChanged() {
		Log.d(TAG, "onSubscriptionsChanged start");
		 updateCallButtonVisbilityAndWidth();        		
   }
   
    private boolean mIsUseSoundPool  = true;
}

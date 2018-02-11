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

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Contacts.Intents.Insert;
import android.provider.Contacts.People;
import android.provider.Contacts.Phones;
import android.provider.Contacts.PhonesColumns;
import gionee.provider.GnContactsContract.Contacts;
import android.provider.Settings;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewStub;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import aurora.widget.AuroraButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import aurora.widget.AuroraListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import java.util.HashSet;
import java.util.Set;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.activities.DialtactsActivity.ViewPagerVisibilityListener;
import com.android.contacts.list.BaseTextIconAdapter;
import com.android.contacts.util.Constants;
import com.android.contacts.util.EditTextControler.EditTextSwitchAdder;
import com.android.contacts.util.IntentFactory;
import com.android.contacts.util.NumberAreaUtil;
import com.android.contacts.util.PhoneNumberFormatter;
import com.android.internal.telephony.ITelephony;
import com.android.contacts.util.CallLogAsync;
import com.android.contacts.util.HapticFeedback;


import com.mediatek.contacts.SpecialCharSequenceMgrProxy;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.activities.SpeedDialManageActivity;
import com.mediatek.contacts.dialpad.AutoScaleTextSizeWatcher;
import com.mediatek.contacts.dialpad.DialerSearchController;
import com.mediatek.contacts.dialpad.DialerSearchController.DialerSearchResult;
import com.mediatek.contacts.dialpad.DialerSearchController.OnDialerSearchResult;
import com.mediatek.contacts.dialpad.SpeedDial;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.contacts.util.OperatorUtils;
import com.mediatek.contacts.util.TelephonyUtils;
import com.mediatek.contacts.ContactsFeatureConstants;
import com.mediatek.contacts.HyphonManager;
import com.mediatek.contacts.Profiler;
import com.mediatek.contacts.CallOptionHandler;
import com.mediatek.contacts.SimAssociateHandler;

//Gionee zhanglina 2012-07-10 add for CR00640388 begin
import android.os.SystemProperties;
//Gionee zhanglina 2012-07-10 add for CR00640388 end

import gionee.telephony.GnTelephonyManager;

import aurora.widget.AuroraEditText;

/**
 * Fragment that displays a twelve-key phone dialpad.
 */
public class DialpadFragment extends Fragment
        implements View.OnClickListener,
        View.OnLongClickListener, View.OnKeyListener,
        AdapterView.OnItemClickListener, TextWatcher,
        PopupMenu.OnMenuItemClickListener,
        ViewPagerVisibilityListener, OnScrollListener, OnDialerSearchResult {
    private static final String TAG = DialpadFragment.class.getSimpleName();

    private static final String EMPTY_NUMBER = "";

    /** The length of DTMF tones in milliseconds */
    private static final int TONE_LENGTH_MS = 150;

    /** The DTMF tone volume relative to other sounds in the stream */
    private static final int TONE_RELATIVE_VOLUME = 80;

    /** Stream type used to play the DTMF tones off call, and mapped to the volume control keys */
    private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private PopupMenu popup = null;
	
    private View fragmentView = null;

    private static final boolean DBG = true;
  //Gionee zhanglina 2012-07-10 add for CR00640388 begin
    private static final boolean gnIpdialSupport = SystemProperties.get("ro.gn.ip.dial").equals("yes");
  //Gionee zhanglina 2012-07-10 add for CR00640388 end
  //Gionee tianliang 2012-07-25 add for CR00657611 begin
	private static final boolean gnSpeedDial = SystemProperties.get("ro.gn.speed.dial").equals("yes");
 //Gionee tianliang 2012-07-25 add for CR00657611 end
    public interface Listener {
        public void onSearchButtonPressed();
    }

    /**
     * View (usually FrameLayout) containing mDigits field. This can be null, in which mDigits
     * isn't enclosed by the container.
     */
    private View mDigitsContainer;
    private AuroraEditText mDigits;

    private View mDelete;
    private ToneGenerator mToneGenerator;
    private Object mToneGeneratorLock = new Object();
    private View mDialpad;
    private View mAdditionalButtonsRow;

    private View mSearchButton;
    private Listener mListener;

    private View mDialButton;
    private AuroraListView mDialpadChooser;
    private DialpadChooserAdapter mDialpadChooserAdapter;

    /**
     * Regular expression prohibiting manual phone call. Can be empty, which means "no rule".
     */
    private String mProhibitedPhoneNumberRegexp;

    private boolean mShowOptionsMenu;


    // Last number dialed, retrieved asynchronously from the call DB
    // in onCreate. This number is displayed when the user hits the
    // send key and cleared in onPause.
    CallLogAsync mCallLog = new CallLogAsync();
    private String mLastNumberDialed = EMPTY_NUMBER;

    // determines if we want to playback local DTMF tones.
    private boolean mDTMFToneEnabled;

    // Vibration (haptic feedback) for dialer key presses.
    private HapticFeedback mHaptic = new HapticFeedback();

    /** Identifier for the "Add Call" intent extra. */
    static final String ADD_CALL_MODE_KEY = "add_call_mode";

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
    static final String EXTRA_SEND_EMPTY_FLASH
            = "com.android.phone.extra.SEND_EMPTY_FLASH";

    private String mCurrentCountryIso;

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
                final boolean phoneIsInUse = phoneIsInUse();
                if(dialpadChooserVisible()) {
                    if (!phoneIsInUse) {
                        showDialpadChooser(false);
                        adjustListViewLayoutParameters();
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

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mWasEmptyBeforeTextChange = TextUtils.isEmpty(s);
    }

    public void onTextChanged(CharSequence input, int start, int before, int changeCount) {
        if (mWasEmptyBeforeTextChange != TextUtils.isEmpty(input)) {
            final Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }

        // DTMF Tones do not need to be played here any longer -
        // the DTMF dialer handles that functionality now.
    }

    public void afterTextChanged(Editable input) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnAfterTextChanged(input);
    		return;
    	}
    	
        /**
         * Change Feature by mediatek .inc
         * description : use SpecialCharSequenceMgrProxy to handle special char 
         * sequence, it's a wrapper of SpecialCharSequence
         */
        if (SpecialCharSequenceMgrProxy.handleChars(getActivity(), input.toString(), mDigits)) {
            // A special sequence was entered, clear the digits
            mDigits.getText().clear();
        }

        int digitsVisibility = getDigitsVisibility();
        updateDialAndDeleteButtonEnabledState();

        final boolean isDigitsEmpty = isDigitsEmpty();
        if (isDigitsEmpty) {
            mDigits.setCursorVisible(false);
        } else {
            if(!isDialpadChooserVisible()) {
                log("afterTextChanged, show digits");
                if(mDigitsContainer != null){
                    mDigitsContainer.setVisibility(View.VISIBLE);
                    mDigits.setVisibility(View.VISIBLE);
                    mDelete.setVisibility(View.VISIBLE);
                }
                else {
                    mDigits.setVisibility(View.VISIBLE);
                    mDelete.setVisibility(View.VISIBLE);
                }
            }
        }

        // digits visibility changed !
        if(ContactsApplication.sDialerSearchSupport) {
            if(digitsVisibility != getDigitsVisibility()) {
                log("afterTextChanged, digitsVisibility changed");
                adjustListViewLayoutParameters();
            }
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        Profiler.trace(Profiler.DialpadFragmentEnterOnCreate);
        if(DBG)log("onCreate start...");

        mCurrentCountryIso = ContactsUtils.getCurrentCountryIso();
        log("current country iso : " + mCurrentCountryIso);

        try {
            mHaptic.init(getActivity(),
                         getResources().getBoolean(R.bool.config_enable_dialer_key_vibration));
        } catch (Resources.NotFoundException nfe) {
             Log.e(TAG, "Vibrate control bool missing.", nfe);
        }

        setHasOptionsMenu(true);

        mProhibitedPhoneNumberRegexp = getResources().getString(
                R.string.config_prohibited_phone_number_regexp);

        /**
         * Change Feature by mediatek .inc
         * description : initialize speed dial
         */
        if(ContactsApplication.sSpeedDial)
            mSpeedDial = new SpeedDial(getActivity());
        mShowDialpadDrawable = getResources().getDrawable(R.drawable.ic_show_dialpad_holo_dark);
        mHideDialpadDrawable = getResources().getDrawable(R.drawable.ic_dialpad_holo_dark);
        mCallOptionHandler = new CallOptionHandler(getActivity());
        mFragmentState = FragmentState.CREATED;
        /**
         * Change Feature by mediatek end
         */
        if(DBG)log("onCreate end...");
        Profiler.trace(Profiler.DialpadFragmentLeaveOnCreate);
    }

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");

        mFragmentState = FragmentState.DESTROYED;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		return gnOnCreateView(inflater, container, savedState);    		
    	}
    	
        Profiler.trace(Profiler.DialpadFragmentEnterOnCreateView);
        fragmentView = inflater.inflate(R.layout.dialpad_fragment, container, false);
        mLaunch = true;
        // Load up the resources for the text field.
        Resources r = getResources();

        mDigitsContainer = fragmentView.findViewById(R.id.digits_container);
        mDigits = (AuroraEditText) fragmentView.findViewById(R.id.digits);
        mDigits.setKeyListener(DialerKeyListener.getInstance());
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);
        mDigits.setOnLongClickListener(this);
        mDigits.addTextChangedListener(this);
        /**
         * Change Feature by mediatek .inc
         */
        mAutoScaleTextSizeWatcher = new AutoScaleTextSizeWatcher(mDigits);
        mAutoScaleTextSizeWatcher.setAutoScaleParameters(r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size_min), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size_delta), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_width)); 
        mDigits.addTextChangedListener(mAutoScaleTextSizeWatcher);

        if(ContactsApplication.sDialerSearchSupport) {
            mListView = (AuroraListView) fragmentView.findViewById(R.id.list_view);
            if(mListView != null) {
                mListView.setOnScrollListener(this);
                mDialerSearchController = new DialerSearchController(getActivity(), mListView, mCallOptionHandler);
                mDialerSearchController.setDialerSearchTextWatcher(mDigits);
            }
            mDivider = fragmentView.findViewById(R.id.divider);
            mDialpadDivider = fragmentView.findViewById(R.id.dialpadDivider);			
        }
        /**
         * Change Feature by mediatek .inc end
         */

        mDigits.setTag(mHandler.obtainMessage(MSG_GET_TEXT_WATCHER));
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), mDigits);

        // Soft menu button should appear only when there's no hardware menu button.
        //final View overflowMenuButton = fragmentView.findViewById(R.id.overflow_menu);
        if(ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            mVideoDialButton = fragmentView.findViewById(R.id.videoDialButton);
            if (mVideoDialButton != null) {
                mVideoDialButton.setOnClickListener(this);
            }

            mAddToContactButton = fragmentView.findViewById(R.id.addToContact);
            if (mAddToContactButton != null) {
                mAddToContactButton.setOnClickListener(this);
            }
        } else {
            mOverflowMenuButton = fragmentView.findViewById(R.id.overflow_menu);
            if (mOverflowMenuButton != null) {
                mOverflowMenuButton.setOnClickListener(this);
            }
        }

        // Check for the presence of the keypad
        View oneButton = fragmentView.findViewById(R.id.one);
        if (oneButton != null) {
            setupKeypad(fragmentView);
        }

        mAdditionalButtonsRow = fragmentView.findViewById(R.id.dialpadAdditionalButtons);

        /**
         * Change Feature by medialtek .Inc
         * description : remove search button
         */
        //mSearchButton = mAdditionalButtonsRow.findViewById(R.id.searchButton);
        mSearchButton = fragmentView.findViewById(R.id.searchButton);
        if (mSearchButton != null) {
            mSearchButton.setOnClickListener(this);
        }
        /**
         * Change Feature by mediatek .Inc end
         */

        // Check whether we should show the onscreen "Dial" button.
        mDialButton = mAdditionalButtonsRow.findViewById(R.id.dialButton);

        if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
            mDialButton.setOnClickListener(this);
        } else {
            mDialButton.setVisibility(View.GONE); // It's VISIBLE by default
            mDialButton = null;
        }

        /**
         * Change Feature by mediatek .inc
         * original android code:
         * mDelete = mAdditionalButtonsRow.findViewById(R.id.deleteButton);
         * description : delete button is moved to the right of digits, nolonger
         * in the additional buttons row
         */
        mDelete = fragmentView.findViewById(R.id.deleteButton);
        /**
         * Change Feature by mediatek.inc end
         */
        if(mDelete != null){
            mDelete.setOnClickListener(this);
            mDelete.setOnLongClickListener(this);
            mDelete.setVisibility(View.GONE);
        }

        mDialpad = fragmentView.findViewById(R.id.dialpad);  // This is null in landscape mode.

        // In landscape we put the keyboard in phone mode.
        if (null == mDialpad) {
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            mDigits.setCursorVisible(false);
        }

        // Set up the "dialpad chooser" UI; see showDialpadChooser().
        mDialpadChooser = (AuroraListView) fragmentView.findViewById(R.id.dialpadChooser);
        mDialpadChooser.setOnItemClickListener(this);

        /**
         * Change Feature by mediatek .Inc
         * description : replace search button with hide dialpad button
         */
        mDialpadButton = (ImageButton) mAdditionalButtonsRow.findViewById(R.id.dialpadButton);
        if(mDialpadButton != null)
            mDialpadButton.setOnClickListener(this);
        configureScreenFromIntent(getActivity().getIntent());

//        fragmentView.getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);

        if(mDigitsContainer != null) {
            mDigitsContainer.setVisibility(View.GONE);
        } else {
            mDigits.setVisibility(View.GONE);
            mDelete.setVisibility(View.GONE);
        }
        /**
         * Change Feature by mediatek .Inc end
         */
        Profiler.trace(Profiler.DialpadFragmentLeaveOnCreateView);
        return fragmentView;
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
                if ("tel".equals(uri.getScheme())) {
                    // Put the requested number into the input area
                    String data = uri.getSchemeSpecificPart();
                    setFormattedDigits(data, null);
                    // clear the data
                    intent.setData(null);
                    
                    // gionee xuhz 20120508 add for CR00594040 start
                    if (ContactsApplication.sIsGnContactsSupport) {
                        if (data != null && !data.isEmpty()) {
                            mDigits.setSelection(0);
                            mDigits.setVisibility(View.VISIBLE);
                            mDigits.setCursorVisible(true);
                        }
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
                if (!isAddCallMode && phoneIsInUse()) {
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
            if (phoneIsInUse()) {
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

    private void setupKeypad(View fragmentView) {
        // Setup the listeners for the buttons
        View view = fragmentView.findViewById(R.id.one);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.two);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.three);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.four);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.five);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.six);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.seven);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.eight);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.nine);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        view = fragmentView.findViewById(R.id.star);
        view.setOnClickListener(this);
        if (ContactsApplication.sIsGnContactsSupport) {
        	view.setOnLongClickListener(this);
        }

        view = fragmentView.findViewById(R.id.zero);
        view.setOnClickListener(this);
        view.setOnLongClickListener(this);

        fragmentView.findViewById(R.id.pound).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        if (ContactsApplication.sIsGnContactsSupport) {
        	gnOnResume();
        	return;
        }
        Profiler.trace(Profiler.DialpadFragmentEnterOnResume);
        log("onResume, mFragmentState = " + mFragmentState);
        if(mFragmentState == FragmentState.RESUMED) {
            log("duplicate resumed state, bial out...");
            return;
        }
        mFragmentState = FragmentState.RESUMED;
		
        if(mDelete == null){
            mDelete = fragmentView.findViewById(R.id.deleteButton);
            if(mDelete != null){
                mDelete.setOnClickListener(this);
                mDelete.setOnLongClickListener(this);
            }
        }
	
        /**
         * add by mediatek .inc
         * description : start query SIM association
         */
        //SimAssociateHandler.getInstance().load();
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

        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    // we want the user to be able to control the volume of the dial tones
                    // outside of a call, so we use the stream type that is also mapped to the
                    // volume control keys for this activity
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                    getActivity().setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }

        Activity parent = getActivity();
        if (parent instanceof DialtactsActivity) {
            // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
            // digits in the dialer field.
            fillDigitsIfNecessary(parent.getIntent());
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
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_1);
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE);
        }
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
        if (phoneIsInUse()) {
            mDigits.setHint(R.string.dialerDialpadHintText);
        } else {
            // Common case; no hint necessary.
            mDigits.setHint(null);

            // Also, a sanity-check: the "dialpad chooser" UI should NEVER
            // be visible if the phone is idle!
            showDialpadChooser(false);
            updateDialAndDeleteButtonEnabledState();

            if(isDigitsEmpty()) {
                log("onResume, hide digits");
                if(mDigitsContainer != null) {
                    mDigitsContainer.setVisibility(View.GONE);
                } else {
                    mDigits.setVisibility(View.GONE);
                    mDelete.setVisibility(View.GONE);
                }
            }
        }

        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null && !mLaunch)
                mDialerSearchController.onResume();
        }

        if(ContactsApplication.sDialerSearchSupport) {
            log("onResume adjust list view layout parameters");
            adjustListViewLayoutParameters();
        }
        if(mDialpadDivider != null)
            mDialpadDivider.setVisibility(View.VISIBLE);
        Profiler.trace(Profiler.DialpadFragmentLeaveOnResume);
    }

    @Override
    public void onPause() {
        super.onPause();

        /**
         * add by mediatek .inc
         * description : mark the fragment state
         */
        log("onPause");
        mFragmentState = FragmentState.PAUSED;
        /**
         * add by mediatek .inc end
         */
        if (popup != null) {
            popup.dismiss();
        }

        // Stop listening for phone state changes.
        TelephonyManager telephonyManager =
                (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator != null) {
                mToneGenerator.release();
                mToneGenerator = null;
            }
        }
        // TODO: I wonder if we should not check if the AsyncTask that
        // lookup the last dialed number has completed.
        mLastNumberDialed = EMPTY_NUMBER;  // Since we are going to query again, free stale number.
        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null)
                mDialerSearchController.onPause();
        }
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
        if(mDigits != null && mDigits.length() > 0) {
            if(mDialerSearchController != null) {
                mDialerSearchController.onStop();
            } else {
                mDigits.setText(EMPTY_NUMBER);
            }
        }

        if(FeatureOption.MTK_GEMINI_SUPPORT)
            if(mCallOptionHandler != null)
                mCallOptionHandler.onStop();
        /**
         * Change Feature by mediatek end
         */
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if(DBG)log("mShowOptionsMenu: "+ mShowOptionsMenu);
        if (mShowOptionsMenu && ViewConfiguration.get(getActivity()).hasPermanentMenuKey() &&
                isLayoutReady() && mDialpadChooser != null) {
        	if(DBG) log("onCreateOptionsMenu ");
        	
        	if (ContactsApplication.sIsGnContactsSupport) {
        		inflater.inflate(R.menu.gn_dialpad_options, menu);
        	} else {
        		inflater.inflate(R.menu.dialpad_options, menu);
        	}
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Hardware menu key should be available and Views should already be ready.
        if(DBG)log("mShowOptionsMenu: "+ mShowOptionsMenu);
		
        // gionee xuhz 20120507 add for CR00589272 start
        if (ContactsApplication.sIsGnContactsSupport) {
            if (!isDigitsEmpty()) {
                mDigits.requestFocus();
            }
        }
        // gionee xuhz 20120507 add for CR00589272 end
        if (mShowOptionsMenu && ViewConfiguration.get(getActivity()).hasPermanentMenuKey() &&
                isLayoutReady() && mDialpadChooser != null) {
             if(DBG)log("onPrepareOptionsMenu ");
             
             if (ContactsApplication.sIsGnContactsSupport) {
            	 gnSetupMenuItems(menu);
             } else {
            	 setupMenuItems(menu);
             }
        }
    }

    private void setupMenuItems(Menu menu) {
        final MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_call_settings_dialpad);
        final MenuItem addToContactMenuItem = menu.findItem(R.id.menu_add_contacts);
        final MenuItem twoSecPauseMenuItem = menu.findItem(R.id.menu_2s_pause);
        final MenuItem waitMenuItem = menu.findItem(R.id.menu_add_wait);
        final MenuItem ipDialMenuItem = menu.findItem(R.id.menu_ip_dial);
        final MenuItem videoCallMenuItem = menu.findItem(R.id.menu_video_call);

        // Check if all the menu items are inflated correctly. As a shortcut, we assume all menu
        // items are ready if the first item is non-null.
        if (callSettingsMenuItem == null) {
            return;
        }

        final Activity activity = getActivity();
        if (activity != null && ViewConfiguration.get(activity).hasPermanentMenuKey()) {
            // Call settings should be available via its parent Activity.
            callSettingsMenuItem.setVisible(false);
        } else {
            callSettingsMenuItem.setVisible(true);
            callSettingsMenuItem.setIntent(DialtactsActivity.getCallSettingsIntent());
        }

        // We show "add to contacts", "2sec pause", and "add wait" menus only when the user is
        // seeing usual dialpads and has typed at least one digit.
        // We never show a menu if the "choose dialpad" UI is up.
        videoCallMenuItem.setVisible(false);
        addToContactMenuItem.setVisible(false);
        if (dialpadChooserVisible() || isDigitsEmpty()) {
            twoSecPauseMenuItem.setVisible(false);
            waitMenuItem.setVisible(false);
            ipDialMenuItem.setVisible(false);
        } else {
            final CharSequence digits = mDigits.getText();

            final boolean hasPermanentMenuKey = ViewConfiguration.get(getActivity()).hasPermanentMenuKey();
            if(FeatureOption.MTK_VT3G324M_SUPPORT)
                videoCallMenuItem.setVisible(!hasPermanentMenuKey);

            // Put the current digits string into an intent
            if(FeatureOption.MTK_VT3G324M_SUPPORT || !hasPermanentMenuKey) {
                addToContactMenuItem.setIntent(getAddToContactIntent(digits));
                addToContactMenuItem.setVisible(true);
            }

            ipDialMenuItem.setVisible(true);

            // Check out whether to show Pause & Wait option menu items
            int selectionStart;
            int selectionEnd;
            String strDigits = digits.toString();

            selectionStart = mDigits.getSelectionStart();
            selectionEnd = mDigits.getSelectionEnd();

            if (selectionStart != -1) {
                if (selectionStart > selectionEnd) {
                    // swap it as we want start to be less then end
                    int tmp = selectionStart;
                    selectionStart = selectionEnd;
                    selectionEnd = tmp;
                }

                if (selectionStart != 0) {
                    // Pause can be visible if cursor is not in the begining
                    twoSecPauseMenuItem.setVisible(true);

                    // For Wait to be visible set of condition to meet
                    waitMenuItem.setVisible(showWait(selectionStart, selectionEnd, strDigits));
                } else {
                    // cursor in the beginning both pause and wait to be invisible
                    twoSecPauseMenuItem.setVisible(false);
                    waitMenuItem.setVisible(false);
                }
            } else {
                twoSecPauseMenuItem.setVisible(true);

                // cursor is not selected so assume new digit is added to the end
                int strLength = strDigits.length();
                waitMenuItem.setVisible(showWait(strLength, strLength, strDigits));
            }
        }        
    }
    
    private void gnSetupMenuItems(Menu menu) {
        final MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_call_settings_dialpad);        

        // gionee xuhz 20120621 add for CR00625981 start
        int sim1Slotid = SimCardUtils.SimSlot.SLOT_ID1;
        int sim2Slotid = -1;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            sim2Slotid = SimCardUtils.SimSlot.SLOT_ID2;
        }
        // gionee xuhz 20120621 add for CR00625981 end
        
        // Check if all the menu items are inflated correctly. As a shortcut, we assume all menu
        // items are ready if the first item is non-null.
        if (callSettingsMenuItem == null) {
            return;
        }

        final Activity activity = getActivity();
        if (activity != null && ViewConfiguration.get(activity).hasPermanentMenuKey()) {
            // Call settings should be available via its parent Activity.
            callSettingsMenuItem.setVisible(false);
        } else {
            callSettingsMenuItem.setVisible(true);
            callSettingsMenuItem.setIntent(DialtactsActivity.getCallSettingsIntent());
        }
		//Gionee tianliang 2012-07-25 add for CR00657611 begin
		final MenuItem speeddialMenuItem = menu.findItem(R.id.menu_speed_dial);

                //Gionee songganggang 2012-07-28 modify for CR00658748 begin
                if (speeddialMenuItem != null) {
		    if (gnSpeedDial == true) {
			speeddialMenuItem.setVisible(true);
		    } else {
                        speeddialMenuItem.setVisible(false);
		    }
                }
               //Gionee songganggang 2012-07-28 modify for CR00658748 end

		//Gionee tianliang 2012-07-25 add for CR00657611 end
        //Gionee zhanglina 2012-07-10 add for CR00640388 begin
        if (gnIpdialSupport != true){
        	final MenuItem ipdialsettingMenuItem = menu.findItem(R.id.menu_ip_dial_setting);
			if(null != ipdialsettingMenuItem){
        	ipdialsettingMenuItem.setVisible(false);
			}
        }
        //Gionee zhanglina 2012-07-10 add for CR00640388 end
        final MenuItem gnminiSimManMenuItem = menu.findItem(R.id.menu_gemini_sim_management);
        if(!FeatureOption.MTK_GEMINI_SUPPORT) {
        	gnminiSimManMenuItem.setVisible(false);
        // gionee xuhz 20120621 add for CR00625981 start
        } else if (!SimCardUtils.isSimInserted(sim1Slotid) 
        		&& !SimCardUtils.isSimInserted(sim2Slotid)) {
        	gnminiSimManMenuItem.setVisible(false);
        // gionee xuhz 20120621 add for CR00625981 end
        } else {
        	boolean isWifiOnly = isWifiOnly();
        	gnminiSimManMenuItem.setEnabled(!isWifiOnly);
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
        mHaptic.vibrate();
        if(DBG)log("keyPressed keyCode: "+ keyCode);		
        KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        mDigits.onKeyDown(keyCode, event);

        // If the cursor is at the end of the text we hide it.
        final int length = mDigits.length();
        if (length == mDigits.getSelectionStart() && length == mDigits.getSelectionEnd()) {
            mDigits.setCursorVisible(false);
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
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnOnClick(view);
    		return;
    	}
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
        switch (view.getId()) {
            case R.id.one: {
                playTone(ToneGenerator.TONE_DTMF_1);
                keyPressed(KeyEvent.KEYCODE_1);
                return;
            }
            case R.id.two: {
                playTone(ToneGenerator.TONE_DTMF_2);
                keyPressed(KeyEvent.KEYCODE_2);
                return;
            }
            case R.id.three: {
                playTone(ToneGenerator.TONE_DTMF_3);
                keyPressed(KeyEvent.KEYCODE_3);
                return;
            }
            case R.id.four: {
                playTone(ToneGenerator.TONE_DTMF_4);
                keyPressed(KeyEvent.KEYCODE_4);
                return;
            }
            case R.id.five: {
                playTone(ToneGenerator.TONE_DTMF_5);
                keyPressed(KeyEvent.KEYCODE_5);
                return;
            }
            case R.id.six: {
                playTone(ToneGenerator.TONE_DTMF_6);
                keyPressed(KeyEvent.KEYCODE_6);
                return;
            }
            case R.id.seven: {
                playTone(ToneGenerator.TONE_DTMF_7);
                keyPressed(KeyEvent.KEYCODE_7);
                return;
            }
            case R.id.eight: {
                playTone(ToneGenerator.TONE_DTMF_8);
                keyPressed(KeyEvent.KEYCODE_8);
                return;
            }
            case R.id.nine: {
                playTone(ToneGenerator.TONE_DTMF_9);
                keyPressed(KeyEvent.KEYCODE_9);
                return;
            }
            case R.id.zero: {
                playTone(ToneGenerator.TONE_DTMF_0);
                keyPressed(KeyEvent.KEYCODE_0);
                return;
            }
            case R.id.pound: {
                playTone(ToneGenerator.TONE_DTMF_P);
                keyPressed(KeyEvent.KEYCODE_POUND);
                return;
            }
            case R.id.star: {
                playTone(ToneGenerator.TONE_DTMF_S);
                keyPressed(KeyEvent.KEYCODE_STAR);
                return;
            }
            case R.id.deleteButton: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return;
            }
            case R.id.dialButton: {
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys
                Profiler.trace(Profiler.DialpadFragmentEnterClick);
                dialButtonPressed();
                Profiler.trace(Profiler.DialpadFragmentLeaveClick);
                return;
            } 
            /*
            case R.id.searchButton: {
                mHaptic.vibrate();
                if (mListener != null) {
                    mListener.onSearchButtonPressed();
                }
                return;
            }
            */
            case R.id.digits: {
                if (!isDigitsEmpty()) {
                    mDigits.setCursorVisible(true);
                }
                return;
            }
            case R.id.overflow_menu: {
                popup = constructPopupMenu(view);
                if (popup != null) {
                    popup.show();
                }
                return;
            }
        }
    }

    private PopupMenu constructPopupMenu(View anchorView) {
        final Context context = getActivity();
        if (context == null) {
            return null;
        }
        final PopupMenu popupMenu = new PopupMenu(context, anchorView);
        final Menu menu = popupMenu.getMenu();
        if (ContactsApplication.sIsGnContactsSupport) {
        	popupMenu.inflate(R.menu.gn_dialpad_options);
        	gnSetupMenuItems(menu);
    	} else {
    		popupMenu.inflate(R.menu.dialpad_options);
    		setupMenuItems(menu);
    	}        
        popupMenu.setOnMenuItemClickListener(this);        
        setupPopupMenuItems(menu);
        return popupMenu;
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
                mDigits.setText(EMPTY_NUMBER);
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                return true;
            }
            case R.id.one: {
                if (isDigitsEmpty()) {
                    if (isVoicemailAvailableProxy()) {
                        callVoicemail();
                    } else if (getActivity() != null) {
                        if(!ContactsApplication.sGemini) {
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
            case R.id.zero: {
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
        mCallOptionHandler.startActivity(newVoicemailIntent());
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
     * In most cases, when the dial button is pressed, there is a
     * number in digits area. Pack it in the intent, start the
     * outgoing call broadcast as a separate task and finish this
     * activity.
     *
     * When there is no digit and the phone is CDMA and off hook,
     * we're sending a blank flash for CDMA. CDMA networks use Flash
     * messages when special processing needs to be done, mainly for
     * 3-way or call waiting scenarios. Presumably, here we're in a
     * special 3-way scenario where the network needs a blank flash
     * before being able to add the new participant.  (This is not the
     * case with all 3-way calls, just certain CDMA infrastructures.)
     *
     * Otherwise, there is no digit, display the last dialed
     * number. Don't finish since the user may want to edit it. The
     * user needs to press the dial button again, to dial it (general
     * case described above).
     */
//    public void dialButtonPressed() {
//        if (isDigitsEmpty()) { // No number entered.
//            if (phoneIsCdma() && phoneIsOffhook()) {
//                // This is really CDMA specific. On GSM is it possible
//                // to be off hook and wanted to add a 3rd party using
//                // the redial feature.
//                startActivity(newFlashIntent());
//            } else {
//                if (!TextUtils.isEmpty(mLastNumberDialed)) {
//                    // Recall the last number dialed.
//                    mDigits.setText(mLastNumberDialed);
//
//                    // ...and move the cursor to the end of the digits string,
//                    // so you'll be able to delete digits using the Delete
//                    // button (just as if you had typed the number manually.)
//                    //
//                    // Note we use mDigits.getText().length() here, not
//                    // mLastNumberDialed.length(), since the EditText widget now
//                    // contains a *formatted* version of mLastNumberDialed (due to
//                    // mTextWatcher) and its length may have changed.
//                    mDigits.setSelection(mDigits.getText().length());
//                } else {
//                    // There's no "last number dialed" or the
//                    // background query is still running. There's
//                    // nothing useful for the Dial button to do in
//                    // this case.  Note: with a soft dial button, this
//                    // can never happens since the dial button is
//                    // disabled under these conditons.
//                    playTone(ToneGenerator.TONE_PROP_NACK);
//                }
//            }
//        } else {
//            final String number = mDigits.getText().toString();
//
//            // "persist.radio.otaspdial" is a temporary hack needed for one carrier's automated
//            // test equipment.
//            // TODO: clean it up.
//            if (number != null
//                    && !TextUtils.isEmpty(mProhibitedPhoneNumberRegexp)
//                    && number.matches(mProhibitedPhoneNumberRegexp)
//                    && (SystemProperties.getInt("persist.radio.otaspdial", 0) != 1)) {
//                Log.i(TAG, "The phone number is prohibited explicitly by a rule.");
//                if (getActivity() != null) {
//                    DialogFragment dialogFragment = ErrorDialogFragment.newInstance(
//                                    R.string.dialog_phone_call_prohibited_title);
//                    dialogFragment.show(getFragmentManager(), "phone_prohibited_dialog");
//                }
//
//                // Clear the digits just in case.
//                mDigits.getText().clear();
//            } else {
//                final Intent intent = newDialNumberIntent(number);
//                if (getActivity() instanceof DialtactsActivity) {
//                    intent.putExtra(DialtactsActivity.EXTRA_CALL_ORIGIN,
//                            DialtactsActivity.CALL_ORIGIN_DIALTACTS);
//                }
//                startActivity(intent);
//                mDigits.getText().clear();  // TODO: Fix bug 1745781
//                getActivity().finish();
//            }
//        }
//    }

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
            return;
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

        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
                return;
            }

            // Start the new tone (will stop any playing tone)
            mToneGenerator.startTone(tone, TONE_LENGTH_MS);
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
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnShowDialpadChooser(enabled);
    		return;
    	}
        // Check if onCreateView() is already called by checking one of View objects.
        log("showDialpadChooser, enabled = " + enabled);
        if (!isLayoutReady()) {
            return;
        }

        if (enabled) {
            // Log.i(TAG, "Showing dialpad chooser!");
            log("showDialpadChooser, hide digits");
            if (mDigitsContainer != null) {
                mDigitsContainer.setVisibility(View.GONE);
            } else {
                // mDigits is not enclosed by the container. Make the digits field itself gone.
                mDigits.setVisibility(View.GONE);
                mDelete.setVisibility(View.GONE);
            }
            if (mDialpad != null) mDialpad.setVisibility(View.GONE);
            mAdditionalButtonsRow.setVisibility(View.GONE);
            mDialpadChooser.setVisibility(View.VISIBLE);

            if(ContactsApplication.sDialerSearchSupport) {
                if(mListView != null)
                    mListView.setVisibility(View.GONE);
            }

            // Instantiate the DialpadChooserAdapter and hook it up to the
            // ListView.  We do this only once.
            if (mDialpadChooserAdapter == null) {
                mDialpadChooserAdapter = new DialpadChooserAdapter(getActivity());
            }
            mDialpadChooser.setAdapter(mDialpadChooserAdapter);
        } else {
            // Log.i(TAG, "Displaying normal Dialer UI.");
            log("showDialpadChooser, show digits");
            if (mDigitsContainer != null) {
                mDigitsContainer.setVisibility(View.VISIBLE);
            } else {
                mDigits.setVisibility(View.VISIBLE);
                mDelete.setVisibility(View.VISIBLE);
            }
            if (mDialpad != null) mDialpad.setVisibility(View.VISIBLE);
            mAdditionalButtonsRow.setVisibility(View.VISIBLE);
            mDialpadChooser.setVisibility(View.GONE);

            if(ContactsApplication.sDialerSearchSupport) {
                if(mListView != null)
                    mListView.setVisibility(View.VISIBLE);

                if(mDialpadButton != null && mDialpadButton.getDrawable() == mShowDialpadDrawable)
                    mDialpadButton.setImageDrawable(mHideDialpadDrawable);
            }
        }
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
                if(ContactsApplication.sDialerSearchSupport)
                    adjustListViewLayoutParameters();
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
     * @return true if the phone is "in use", meaning that at least one line
     *              is active (ie. off hook or ringing or dialing).
     */
    public static boolean phoneIsInUse() {
        boolean phoneInUse = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneInUse = !phone.isIdle();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isIdle() failed", e);
        }
        return phoneInUse;
    }

    /**
     * @return true if the phone is a CDMA phone type
     */
    private boolean phoneIsCdma() {
        boolean isCdma = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                isCdma = (phone.getActivePhoneType() == TelephonyManager.PHONE_TYPE_CDMA);
            }
        } catch (RemoteException e) {
            Log.w(TAG, "phone.getActivePhoneType() failed", e);
        }
        return isCdma;
    }

    /**
     * @return true if the phone state is OFFHOOK
     */
    private boolean phoneIsOffhook() {
        boolean phoneOffhook = false;
        try {
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) phoneOffhook = phone.isOffhook();
        } catch (RemoteException e) {
            Log.w(TAG, "phone.isOffhook() failed", e);
        }
        return phoneOffhook;
    }

    /**
     * Returns true whenever any one of the options from the menu is selected.
     * Code changes to support dialpad options
     */
    private final String CALL_SETTING_PACKAGENAME = "com.android.phone";
    private final String CALL_SETTING_CLASSNAME = "com.android.phone.CallSettings";
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        	case R.id.menu_gemini_sim_management:
        		ContactsUtils.goGeminiSimManagement(getActivity());
        		return true;
            case R.id.menu_2s_pause:
                updateDialString(",");
                return true;
            case R.id.menu_add_wait:
                updateDialString(";");
                return true;
            case R.id.menu_ip_dial_setting:
            	ContactsUtils.goIpCallSetting(getActivity());
            	return true;
            default:
                /*
                 * new feature by mediatek begin
                 * original android code :
                 * return false;
                 * description : handle 'ip dial' and 'speed dial'
                 */
                return onOptionsItemSelectedInternal(item);
                /*
                 * new feature by mediatek end
                 */
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        return onOptionsItemSelected(item);
    }

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
            if (phoneIsCdma() && phoneIsOffhook()) {
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

        // gionee xuhz 20120523 add for CR00607990 start
        if (ContactsApplication.sIsGnContactsSupport) {
            if (mVedioDialButton != null) {
                // On CDMA phones, if we're already on a call, we *always*
                // enable the Dial button (since you can press it without
                // entering any digits to send an empty flash.)
                if (phoneIsCdma() && phoneIsOffhook()) {
                    mVedioDialButton.setEnabled(true);
                } else {
                    // Common case: GSM, or CDMA but not on a call.
                    // Enable the Dial button if some digits have
                    // been entered, or if there is a last dialed number
                    // that could be redialed.
                    mVedioDialButton.setEnabled(digitsNotEmpty ||
                            !TextUtils.isEmpty(mLastNumberDialed));
                }
            }
        }
        // gionee xuhz 20120523 add for CR00607990 end
        
        mDelete.setEnabled(digitsNotEmpty);
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
        mLastNumberDialed = EMPTY_NUMBER;
        CallLogAsync.GetLastOutgoingCallArgs lastCallArgs =
                new CallLogAsync.GetLastOutgoingCallArgs(
                    getActivity(),
                    new CallLogAsync.OnLastOutgoingCallComplete() {
                        public void lastOutgoingCall(String number) {
                            // TODO: Filter out emergency numbers if
                            // the carrier does not want redial for
                            // these.
                            mLastNumberDialed = number;
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

    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        //mShowOptionsMenu = visible;
        mShowOptionsMenu = true;
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
    private View mOverflowMenuButton;
    private View mVideoDialButton;
    private View mAddToContactButton;

    private AuroraListView mListView;
    private View mDivider;
    private View mDialpadDivider;	
    private AutoScaleTextSizeWatcher mAutoScaleTextSizeWatcher;
    private DialerSearchController mDialerSearchController;
    private SpeedDial mSpeedDial;

    private TextWatcher mTextWatcher;

    private Drawable mShowDialpadDrawable;
    private Drawable mHideDialpadDrawable;

    private CallOptionHandler mCallOptionHandler;

    private boolean mLaunch = true;
//    private PostDrawListener mPostDrawListener = new PostDrawListener();

    public void dialButtonPressed() {
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
        dialButtonPressedInner(gnGetDigitsText(), DIAL_NUMBER_INTENT_NORMAL);
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
    }

    public void dialButtonPressed(int type) {
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
        dialButtonPressedInner(gnGetDigitsText(), type);
        //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
    }

    public void dialButtonPressed(String number) {
        dialButtonPressedInner(number, DIAL_NUMBER_INTENT_NORMAL);
    }

    protected void dialButtonPressedInner(String number, int type) {
        if(DBG)log("dialButtonPressedInner number: "+ number + "type:"+type);
        if (TextUtils.isEmpty(number)) { // No number entered.
            if (phoneIsCdma() && phoneIsOffhook()) {
                // This is really CDMA specific. On GSM is it possible
                // to be off hook and wanted to add a 3rd party using
                // the redial feature.
                startActivity(newFlashIntent());
            } else {
                if (!TextUtils.isEmpty(mLastNumberDialed)) {
                    // Recall the last number dialed.
                    mDigits.setText(mLastNumberDialed);

                    // ...and move the cursor to the end of the digits string,
                    // so you'll be able to delete digits using the Delete
                    // button (just as if you had typed the number manually.)
                    //
                    // Note we use mDigits.getText().length() here, not
                    // mLastNumberDialed.length(), since the EditText widget now
                    // contains a *formatted* version of mLastNumberDialed (due to
                    // mTextWatcher) and its length may have changed.

                    // gionee xuhz 20120508 add  start
                    if (ContactsApplication.sIsGnContactsSupport) {
                        mDigits.setSelection(0);
                        mDigits.setCursorVisible(true);
                    } else {
                        mDigits.setSelection(mDigits.getText().length());
                    }
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
                mDigits.getText().clear();
            } else {
                final Intent intent = IntentFactory.newDialNumberIntent(number, type);
                if (getActivity() instanceof DialtactsActivity) {
                    intent.putExtra(DialtactsActivity.EXTRA_CALL_ORIGIN,
                            DialtactsActivity.CALL_ORIGIN_DIALTACTS);
                }
                mCallOptionHandler.startActivity(intent);
                //mDigits.getText().clear();  // TODO: Fix bug 1745781
                //getActivity().finish();
            }
        }
    }

    protected void showDialpad(boolean show) {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnShowDialpad(show);
    		return;
    	}
        int visibility = show ? View.VISIBLE : View.GONE;
        final Drawable drawable = show ? mHideDialpadDrawable : mShowDialpadDrawable;
        if(DBG) log("showDialpad visibility: "+ visibility + "drawable:"+drawable);
        mDialpad.setVisibility(visibility);
        mDialpadDivider.setVisibility(visibility);
        if(mDigitsContainer != null)
            mDigitsContainer.setVisibility(visibility);
        else {
            mDigits.setVisibility(visibility);
            mDelete.setVisibility(visibility);
        }
        mDialpadButton.setImageDrawable(drawable);

        if(ContactsApplication.sDialerSearchSupport) {
            log("showDialpad, adjust list view layout parameters");
            adjustListViewLayoutParameters();
        }
    }

    protected boolean onOptionsItemSelectedInternal(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_ip_dial:
                return onIPDialMenuItemSelected();
            case R.id.menu_speed_dial:
                return onSpeedDialMenuItemSelected();
            case R.id.menu_people:
                return onPeopleMenuItemSelected();
            case R.id.menu_video_call:
                dialButtonPressed(DIAL_NUMBER_INTENT_VIDEO);
                return true;
        }
        return false;
    }

    public boolean onLongClickInternal(View view) {
        int key = -1;
        switch(view.getId()) {
            case R.id.deleteButton:
                // mDigits.getText().clear();
                mDigits.setText(EMPTY_NUMBER);
                // TODO: The framework forgets to clear the pressed
                // status of disabled button. Until this is fixed,
                // clear manually the pressed status. b/2133127
                mDelete.setPressed(false);
                mAutoScaleTextSizeWatcher.trigger(true);
                return true;
            case R.id.two:
                key = 2;
                break;
            case R.id.three:
                key = 3;
                break;
            case R.id.four:
                key = 4;
                break;
            case R.id.five:
                key = 5;
                break;
            case R.id.six:
                key = 6;
                break;
            case R.id.seven:
                key = 7;
                break;
            case R.id.eight:
                key = 8;
                break;
            case R.id.nine:
                key = 9;
                break;
            case R.id.star: {
                //Gionee:huangzhiyuan 20120507 add for CR00585981 start
            	return gnOnStarLongClick();
            	//Gionee:huangzhiyuan 20120507 add for CR00585981 end
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
            case R.id.dialpadButton:
                // Gionee xuhz 20120619 modify for CR00626198 start
                if (ContactsApplication.sIsGnContactsSupport) {
                    mHaptic.vibrate(); // Vibrate here too, just like we do for the regular keys
                }
                // Gionee xuhz 20120619 modify for CR00626198 end
                onShowDialpadButtonClick();
                return true;
            case R.id.deleteButton:
                keyPressed(KeyEvent.KEYCODE_DEL);
                mAutoScaleTextSizeWatcher.trigger(true);
                return true;
            case R.id.videoDialButton:
                // gionee xuhz 20120508 add for CR00592549 start
                if (ContactsApplication.sIsGnContactsSupport) {
                    mHaptic.vibrate();
                }
                // gionee xuhz 20120508 add for CR00592549 end
                dialButtonPressed(DIAL_NUMBER_INTENT_VIDEO);
                return true;
            case R.id.addToContact:
                if(!isDigitsEmpty()) {
                    //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
                    if (ContactsApplication.sIsGnContactsSupport) {                        
                    	startActivity(IntentFactory.newInsert2ExistContactIntent(gnGetDigitsText()));
                    } else {
                        startActivity(getAddToContactIntent(mDigits.getText().toString()));
                    }
                    //Gionee:huangzhiyuan 20120606 modify for CR00585981 end
                }
                return true;
            default:
                return false;
        }
    }

    protected boolean onShowDialpadButtonClick() {
        if(mDialpad != null) {
            final boolean show = mDialpad.getVisibility() != View.VISIBLE;
            showDialpad(show);
            return true;
        }
        return false;
    }

    protected boolean onIPDialMenuItemSelected() {
        dialButtonPressed(DIAL_NUMBER_INTENT_IP);
        return true;
    }

    protected boolean onSpeedDialMenuItemSelected() {
        final Intent intent = new Intent();
        intent.setClass(getActivity(), SpeedDialManageActivity.class);
        getActivity().startActivity(intent);
        return true;
    }

    protected boolean onPeopleMenuItemSelected() {
        final Intent intent = new Intent("com.android.contacts.action.LIST_DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
        return true;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
        //
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState != OnScrollListener.SCROLL_STATE_IDLE) {
            showDialpad(false);
        }

        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null)
                mDialerSearchController.onScrollStateChanged(view, scrollState);
        }
    }

    private boolean isVoicemailAvailableProxy() {
        if(ContactsApplication.sGemini) {
            // OutgoingCallBroadcaster will do the stuffs
            // just return true
            if(SIMInfoWrapper.getDefault().getInsertedSimCount() == 0)
                return false;

            final long defaultSim = Settings.System.getLong(getActivity().getContentResolver(),
                    ContactsFeatureConstants.VOICE_CALL_SIM_SETTING, ContactsFeatureConstants.DEFAULT_SIM_NOT_SET);

            if(defaultSim == ContactsFeatureConstants.VOICE_CALL_SIM_SETTING_INTERNET)
                return false;

            return true;
        }

        return isVoicemailAvailable();
    }

    private void setupPopupMenuItems(Menu menu) {
        final MenuItem callSettingsMenuItem = menu.findItem(R.id.menu_call_settings_dialpad);
        callSettingsMenuItem.setVisible(true);
        callSettingsMenuItem.setIntent(DialtactsActivity.getCallSettingsIntent());
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        log("KeyEvent = " + event.getKeyCode() + "mDigits.hasFocus(): " + mDigits.hasFocus() + "keyCode " + keyCode);
        if(event.getKeyCode() == KeyEvent.KEYCODE_CALL) {
            dialButtonPressed();
            return true;
        }

        // focus the mDigits and let it handle the key events
        if(!isTrackBallEvent(event)) {
            if(!phoneIsOffhook() && mDigits.getVisibility() != View.VISIBLE) {
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

    void adjustListViewLayoutParameters() {
    	if (ContactsApplication.sIsGnContactsSupport) {
    		gnAdjustListViewLayoutParameters();
    		return;
    	}
    	
        final boolean dialpadVisible = mDialpad != null && mDialpad.getVisibility() == View.VISIBLE;
        final boolean digitsVisible = getDigitsVisibility() == View.VISIBLE;
        log("adjustListViewLayoutParameters, dialpadVisible = " + dialpadVisible + " digitsVisible = " + digitsVisible);

        if(mListView != null) {
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) mListView.getLayoutParams();

            int above;
            if(dialpadVisible) {
                if(digitsVisible)
                    above = R.id.digits_container;
                else {
                    if(mDivider != null)
                        above = R.id.divider;
                    else
                        above = R.id.dialpad;
                }
            } else {
                if(digitsVisible)
                    above = R.id.digits_container;
                else
                    above = R.id.dialpadAdditionalButtons;
            }

            lParams.addRule(RelativeLayout.ABOVE, above);
            mListView.setLayoutParams(lParams);
        }
    }

    void log(String msg) {
        Log.d(TAG, msg);
    }

    boolean isTrackBallEvent(KeyEvent event) {
        int keycode = event.getKeyCode();
        return keycode == KeyEvent.KEYCODE_DPAD_LEFT || keycode == KeyEvent.KEYCODE_DPAD_UP
                || keycode == KeyEvent.KEYCODE_DPAD_RIGHT || keycode == KeyEvent.KEYCODE_DPAD_DOWN;
    }

    int getDigitsVisibility() {
        if(mDigitsContainer != null) {
            return mDigitsContainer.getVisibility();
        } else {
            return mDigits.getVisibility();
        }
    }

    private boolean isDialpadChooserVisible() {
        return mDialpadChooser != null && mDialpadChooser.getVisibility() == View.VISIBLE;
    }

//    private class PostDrawListener implements android.view.ViewTreeObserver.OnPostDrawListener {
//        public boolean onPostDraw() {
//            if(mLaunch) {
//                mLaunch = false;
//                mHandler.sendEmptyMessage(MSG_DIALER_SEARCH_CONTROLLER_ON_RESUME);
//            }
//            Profiler.trace(Profiler.DialpadFragmentOnPostDraw);
//            return true;
//        }
//    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case MSG_DIALER_SEARCH_CONTROLLER_ON_RESUME:
                    if(ContactsApplication.sDialerSearchSupport) {
                        if(mDialerSearchController != null)
                            mDialerSearchController.onResume();
                    }
                    break;
                case MSG_GET_TEXT_WATCHER:
                    if(msg.obj instanceof TextWatcher)
                        mTextWatcher = (TextWatcher) msg.obj;
                    break;
            }
        }
    };

    public void updateDialerSearch(){
        mDialerSearchController.updateDialerSearch();
    }
    
    // *******************************above is mtk****************************************
    protected ViewGroup mKeyLayout;
    protected ViewGroup mResultLayout;
    protected TextView mNumArea;
    protected TextView mSugestView;
    protected TextView mNoMatchView;
    protected View mStrangeNumberHolder;
    protected View mNewContactItem;
    protected View mSendMessageItem;
    protected View mVedioDialButton;
    
    protected AuroraButton mNewContactButton;
    protected AuroraButton mToContactButton;
    protected AuroraButton mSendMessageButton;
    //Gionee:huangzy 20120611 modify for CR00623367 start
    private ImageView mGnDialpadButton;
    private boolean mHasPermanentMenuKey;
    //Gionee:huangzy 20120611 modify for CR00623367 end
    
    public View gnOnCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {

        Profiler.trace(Profiler.DialpadFragmentEnterOnCreateView);
        fragmentView = inflater.inflate(R.layout.gn_dialpad_fragment, container, false);
        mLaunch = true;
        // Load up the resources for the text field.
        Resources r = getResources();

        mDigitsContainer = fragmentView.findViewById(R.id.digits_container);
        mDigits = (AuroraEditText) fragmentView.findViewById(R.id.digits);
        mDigits.setKeyListener(DialerKeyListener.getInstance());
        mDigits.setOnClickListener(this);
        mDigits.setOnKeyListener(this);
        mDigits.setOnLongClickListener(this);
        mDigits.addTextChangedListener(this);
        /**
         * Change Feature by mediatek .inc
         */
        mAutoScaleTextSizeWatcher = new AutoScaleTextSizeWatcher(mDigits);
        mAutoScaleTextSizeWatcher.setAutoScaleParameters(r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size_min), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_text_size_delta), r
                .getDimensionPixelSize(R.dimen.dialpad_digits_width)); 
        mDigits.addTextChangedListener(mAutoScaleTextSizeWatcher);
        
        mNumArea = (TextView)fragmentView.findViewById(R.id.num_area);

        if(ContactsApplication.sDialerSearchSupport) {
        	mKeyLayout = (ViewGroup) fragmentView.findViewById(R.id.key_layout);
//        	// gionee xuhz 20120530 add for gn theme start
//        	if (!ContactsApplication.sIsGnDarkStyle) {
//        	    mKeyLayout.setBackgroundResource(R.drawable.dialpad_background);
//        	}
//        	// gionee xuhz 20120530 add for gn theme end
        	
        	mResultLayout = (ViewGroup) fragmentView.findViewById(R.id.result_layout);
            mListView = (AuroraListView) fragmentView.findViewById(R.id.list_view);           
            mListView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
            
            if(mResultLayout != null && mListView != null) {
                mListView.setOnScrollListener(this);
                mDialerSearchController = new DialerSearchController(getActivity(), mListView, mCallOptionHandler);
                mDialerSearchController.setDialerSearchTextWatcher(mDigits);
                mDialerSearchController.setOnDialerSearchResult(this);
                mListView.setEmptyView(fragmentView.findViewById(R.id.empty_view));
                
                mSugestView = (TextView)fragmentView.findViewById(R.id.sugest_view);
                if (null != mSugestView) {                	
                    mSugestView.setText(Html.fromHtml(getSearchTip()));
                }
                
                mNoMatchView = (TextView)fragmentView.findViewById(R.id.no_match_view);
                mStrangeNumberHolder = (View) fragmentView.findViewById(R.id.take_strange_num);
                
                mNewContactButton = (AuroraButton)mStrangeNumberHolder.findViewById(R.id.gn_strange_num_add_new_contact);
                if (mNewContactButton != null) {
                    mNewContactButton.setOnClickListener(this);
                }
                mToContactButton = (AuroraButton)mStrangeNumberHolder.findViewById(R.id.gn_strange_num_add_to_contact);
                if (mToContactButton != null) {
                    mToContactButton.setOnClickListener(this);
                }
                mSendMessageButton = (AuroraButton)mStrangeNumberHolder.findViewById(R.id.gn_strange_num_send_sms);
                if (mSendMessageButton != null) {
                    mSendMessageButton.setOnClickListener(this);
                }
                
                mDivider = fragmentView.findViewById(R.id.divider);
            }
        }
        /**
         * Change Feature by mediatek .inc end
         */

        mDigits.setTag(mHandler.obtainMessage(MSG_GET_TEXT_WATCHER));
        PhoneNumberFormatter.setPhoneNumberFormattingTextWatcher(getActivity(), mDigits);

        // Soft menu button should appear only when there's no hardware menu button.
        //final View overflowMenuButton = fragmentView.findViewById(R.id.overflow_menu);
        if(ViewConfiguration.get(getActivity()).hasPermanentMenuKey()) {
            mVideoDialButton = fragmentView.findViewById(R.id.videoDialButton);
            if (mVideoDialButton != null) {
                mVideoDialButton.setOnClickListener(this);
            }
        } else {
            mOverflowMenuButton = fragmentView.findViewById(R.id.overflow_menu);
            if (mOverflowMenuButton != null) {
                mOverflowMenuButton.setOnClickListener(this);
            }
        }

        // Check for the presence of the keypad
        View oneButton = fragmentView.findViewById(R.id.one);
        if (oneButton != null) {
            setupKeypad(fragmentView);
        }

        mAdditionalButtonsRow = fragmentView.findViewById(R.id.dialpadAdditionalButtons);
        // gionee xuhz 20120530 add for gn theme start
        if (ContactsApplication.sIsGnContactsSupport) {
            gnShowAdditionalButtons(false);
        }
        if (!ContactsApplication.sIsGnDarkStyle) {
            mAdditionalButtonsRow.setBackgroundResource(R.drawable.gn_dialpad_additional_button_background);
        }
        // gionee xuhz 20120530 add for gn theme end

        /**
         * Change Feature by medialtek .Inc
         * description : remove search button
         */
        //mSearchButton = mAdditionalButtonsRow.findViewById(R.id.searchButton);
        mSearchButton = fragmentView.findViewById(R.id.searchButton);
        if (mSearchButton != null) {
            mSearchButton.setOnClickListener(this);
        }
        /**
         * Change Feature by mediatek .Inc end
         */

        // Check whether we should show the onscreen "Dial" button.
        mDialButton = mAdditionalButtonsRow.findViewById(R.id.dialButton);        

        if (r.getBoolean(R.bool.config_show_onscreen_dial_button)) {
            mDialButton.setOnClickListener(this);
        } else {
            mDialButton.setVisibility(View.GONE); // It's VISIBLE by default
            mDialButton = null;
        }
        
        mVedioDialButton = mAdditionalButtonsRow.findViewById(R.id.videoDialButton);
        if (null != mVedioDialButton) {
        	mVedioDialButton.setOnClickListener(this);
        }

        /**
         * Change Feature by mediatek .inc
         * original android code:
         * mDelete = mAdditionalButtonsRow.findViewById(R.id.deleteButton);
         * description : delete button is moved to the right of digits, nolonger
         * in the additional buttons row
         */
        mDelete = fragmentView.findViewById(R.id.deleteButton);
        /**
         * Change Feature by mediatek.inc end
         */
        if(mDelete != null){
            mDelete.setOnClickListener(this);
            mDelete.setOnLongClickListener(this);
        }
        
        mDialpadButton = (ImageView) fragmentView.findViewById(R.id.dialpadButton);
        if (null != mDialpadButton) {
        	mDialpadButton.setOnClickListener(this);
        }

        mDialpad = fragmentView.findViewById(R.id.dialpad);  // This is null in landscape mode.
        
        // In landscape we put the keyboard in phone mode.
        if (null == mDialpad) {
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        } else {
            mDigits.setCursorVisible(false);
            // gionee xuhz 20120523 modify for CR00607921 start
            /*//Gionee:huangzhiyuan 20120507 add for CR00585981 start
            mDigits.setInputType(android.text.InputType.TYPE_NULL);            
            //Gionee:huangzhiyuan 20120507 add for CR00585981 end*/
            mDigits.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            // gionee xuhz 20120523 modify for CR00607921 end
        }

        // Set up the "dialpad chooser" UI; see showDialpadChooser().
        mDialpadChooser = (AuroraListView) fragmentView.findViewById(R.id.dialpadChooser);
        mDialpadChooser.setOnItemClickListener(this);

        /**
         * Change Feature by mediatek .Inc
         * description : replace search button with hide dialpad button
         */
        configureScreenFromIntent(getActivity().getIntent());

//        fragmentView.getViewTreeObserver().addOnPostDrawListener(mPostDrawListener);
        
        //Gionee:huangzy 20120611 modify for CR00623367 start
        mGnDialpadButton = (ImageView) fragmentView.findViewById(R.id.gn_dialpadButton);
        if (null != mGnDialpadButton) {
            mGnDialpadButton.setOnClickListener(this);
        }
        mHasPermanentMenuKey = ViewConfiguration.get(getActivity()).hasPermanentMenuKey();
        //Gionee:huangzy 20120611 modify for CR00623367 end
    
        /**
         * Change Feature by mediatek .Inc end
         */
        Profiler.trace(Profiler.DialpadFragmentLeaveOnCreateView);
        return fragmentView;
    }
    
    public void gnOnResume() {
        Profiler.trace(Profiler.DialpadFragmentEnterOnResume);
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

        // if the mToneGenerator creation fails, just continue without it.  It is
        // a local audio signal, and is not as important as the dtmf tone itself.
        synchronized (mToneGeneratorLock) {
            if (mToneGenerator == null) {
                try {
                    // we want the user to be able to control the volume of the dial tones
                    // outside of a call, so we use the stream type that is also mapped to the
                    // volume control keys for this activity
                    mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE, TONE_RELATIVE_VOLUME);
                    getActivity().setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Exception caught while creating local tone generator: " + e);
                    mToneGenerator = null;
                }
            }
        }

        Activity parent = getActivity();
        if (parent instanceof DialtactsActivity) {
            // See if we were invoked with a DIAL intent. If we were, fill in the appropriate
            // digits in the dialer field.
            fillDigitsIfNecessary(parent.getIntent());
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
        if(FeatureOption.MTK_GEMINI_SUPPORT) {
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_1);
            GnTelephonyManager.listenGemini(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE, ContactsFeatureConstants.GEMINI_SIM_2);
        } else {
            telephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE|PhoneStateListener.LISTEN_SERVICE_STATE);
        }
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
        if (phoneIsInUse()) {
            mDigits.setHint(R.string.dialerDialpadHintText);
        } else {
            // Common case; no hint necessary.
            mDigits.setHint(null);

            // Also, a sanity-check: the "dialpad chooser" UI should NEVER
            // be visible if the phone is idle!
            showDialpadChooser(false);
            updateDialAndDeleteButtonEnabledState();
        }

        if(ContactsApplication.sDialerSearchSupport) {
            if(mDialerSearchController != null && !mLaunch)
                mDialerSearchController.onResume();
        }
        
        if (isDigitsEmpty() && null != mDialpad) {
        	mDigitsContainer.setVisibility(View.GONE);
        	gnShowDialpad(true);
        } else {
        	mDigitsContainer.setVisibility(View.VISIBLE);
        }

        Profiler.trace(Profiler.DialpadFragmentLeaveOnResume);
    }
    
    public void gnOnClick(View view) {
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
        switch (view.getId()) {
            case R.id.one: {
                playTone(ToneGenerator.TONE_DTMF_1);
                keyPressed(KeyEvent.KEYCODE_1);
                return;
            }
            case R.id.two: {
                playTone(ToneGenerator.TONE_DTMF_2);
                keyPressed(KeyEvent.KEYCODE_2);
                return;
            }
            case R.id.three: {
                playTone(ToneGenerator.TONE_DTMF_3);
                keyPressed(KeyEvent.KEYCODE_3);
                return;
            }
            case R.id.four: {
                playTone(ToneGenerator.TONE_DTMF_4);
                keyPressed(KeyEvent.KEYCODE_4);
                return;
            }
            case R.id.five: {
                playTone(ToneGenerator.TONE_DTMF_5);
                keyPressed(KeyEvent.KEYCODE_5);
                return;
            }
            case R.id.six: {
                playTone(ToneGenerator.TONE_DTMF_6);
                keyPressed(KeyEvent.KEYCODE_6);
                return;
            }
            case R.id.seven: {
                playTone(ToneGenerator.TONE_DTMF_7);
                keyPressed(KeyEvent.KEYCODE_7);
                return;
            }
            case R.id.eight: {
                playTone(ToneGenerator.TONE_DTMF_8);
                keyPressed(KeyEvent.KEYCODE_8);
                return;
            }
            case R.id.nine: {
                playTone(ToneGenerator.TONE_DTMF_9);
                keyPressed(KeyEvent.KEYCODE_9);
                return;
            }
            case R.id.zero: {
                playTone(ToneGenerator.TONE_DTMF_0);
                keyPressed(KeyEvent.KEYCODE_0);
                return;
            }
            case R.id.pound: {
                playTone(ToneGenerator.TONE_DTMF_P);
                keyPressed(KeyEvent.KEYCODE_POUND);
                return;
            }
            case R.id.star: {
                playTone(ToneGenerator.TONE_DTMF_S);
                keyPressed(KeyEvent.KEYCODE_STAR);
                return;
            }
            case R.id.deleteButton: {
                keyPressed(KeyEvent.KEYCODE_DEL);
                return;
            }
            case R.id.dialButton: {
                mHaptic.vibrate();  // Vibrate here too, just like we do for the regular keys
                Profiler.trace(Profiler.DialpadFragmentEnterClick);
                dialButtonPressed();
                Profiler.trace(Profiler.DialpadFragmentLeaveClick);
                return;
            } 
            case R.id.videoDialButton: {
                mHaptic.vibrate();
                dialButtonPressed(DIAL_NUMBER_INTENT_VIDEO);
                return;
            }
            case R.id.digits: {
                if (!isDigitsEmpty()) {
                    mDigits.setCursorVisible(true);
                }
                gnShowDialpad(true);
                return;
            }
            case R.id.overflow_menu: {
                popup = constructPopupMenu(view);
                if (popup != null) {
                    popup.show();
                }
                return;
            }
            case R.id.gn_strange_num_add_new_contact: {
            	startActivity(IntentFactory.newCreateContactIntent(gnGetDigitsText()));
                return;
            }
            case R.id.gn_strange_num_add_to_contact: {
            	startActivity(IntentFactory.newInsert2ExistContactIntent(gnGetDigitsText()));
                return;
            }
            case R.id.gn_strange_num_send_sms: {
            	startActivity(IntentFactory.newCreateSmsIntent(gnGetDigitsText()));
                return;
            }
            //Gionee:huangzy 20120611 modify for CR00623367 start
            case R.id.gn_dialpadButton: {
                // Gionee xuhz 20120619 modify for CR00626198 start
                mHaptic.vibrate(); // Vibrate here too, just like we do for the regular keys
                // Gionee xuhz 20120619 modify for CR00626198 end
                gnShowDialpad(!(mDialpad.getVisibility() == View.VISIBLE));
                return;
            }
            //Gionee:huangzy 20120611 modify for CR00623367 end
        }
    }
    
    private int mTextChangeSwitchEmptyIfOverZero = -1;
    private int digitsLen2SwithchType(int digitsLen) {
    	int switchType = 0;
        if (digitsLen <= 0) {
        	switchType = 0;
        } else if (digitsLen <= 3) {
        	switchType = 1;
        } else {
        	switchType = 2;
        }
        return switchType;
	}
    
    @Override
	public void onDialerSearchResult(DialerSearchResult dialerSearchResult) {
		int resultCount = dialerSearchResult.getCount();
		int digitsLen = gnGetDigitsTextLen();
						
		if (digitsLen > 0 && resultCount == 0) {
			mTextChangeSwitchEmptyIfOverZero = -1;
		} else {
			mTextChangeSwitchEmptyIfOverZero = -2;
		}
		
		gnSwitchEmptyView(digitsLen2SwithchType(digitsLen));
	}
    
    public void gnAfterTextChanged(Editable input) {    	
        /**
         * Change Feature by mediatek .inc
         * description : use SpecialCharSequenceMgrProxy to handle special char 
         * sequence, it's a wrapper of SpecialCharSequence
         */    	        	
        if (SpecialCharSequenceMgrProxy.handleChars(getActivity(), input.toString(), mDigits)) {
            // A special sequence was entered, clear the digits
            mDigits.getText().clear();
        }

        int digitsVisibility = getDigitsVisibility();
        updateDialAndDeleteButtonEnabledState();

        final int digitsLen = gnGetDigitsTextLen();
        
        //Gionee:huangzy 20120530 add for CR00611637 start
        if (0 == digitsLen) {
            if(mDialerSearchController != null) {
                mDialerSearchController.onResume();
            }
        }
        //Gionee:huangzy 20120530 add for CR00611637 end
        
        if (0 == digitsLen && gnIsVisible(mDialpad)) {
        	mDigitsContainer.setVisibility(View.GONE);
            mDigits.setCursorVisible(false);
            mNumArea.setText(null);
        } else {
            if(!isDialpadChooserVisible()) {
                log("afterTextChanged, show digits");
                if(mDigitsContainer != null){
                    mDigitsContainer.setVisibility(View.VISIBLE);
                }                
            }
            
            if (null != mNumArea) {
                //Gionee:huangzhiyuan 20120606 modify for CR00585981 start
            	mNumArea.setText(NumberAreaUtil.getInstance(getActivity()).getNumAreaFromAora(getActivity(), mDigits.getText().toString(), false));
            	//Gionee:huangzhiyuan 20120606 modify for CR00585981 end
            }
        }
        
        {
        	if ((++mTextChangeSwitchEmptyIfOverZero) > 0) {        		
        		gnSwitchEmptyView(digitsLen2SwithchType(digitsLen));
            }
        }
    }
    
    protected String gnGetDigitsText() {
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
    
    protected int gnGetDigitsTextLen() {
		return (null == mDigits ? "" : mDigits.getText().toString()).length();
	}

    void gnAdjustListViewLayoutParameters() {
        return;
    }
    
    void gnSwitchEmptyView(int type) {
    	switch (type) {
		case 0:
			if (mSugestView != null && View.VISIBLE != mSugestView.getVisibility()) {
				mSugestView.setVisibility(View.VISIBLE);
				mNoMatchView.setVisibility(View.GONE);
				mStrangeNumberHolder.setVisibility(View.GONE);
			}
			break;
		case 1:
			if (mNoMatchView != null && View.VISIBLE != mNoMatchView.getVisibility()) {
				mSugestView.setVisibility(View.GONE);
				mNoMatchView.setVisibility(View.VISIBLE);
				mStrangeNumberHolder.setVisibility(View.GONE);
			}
			break;
		case 2:
			if (mStrangeNumberHolder != null && View.VISIBLE != mStrangeNumberHolder.getVisibility()) {
				mSugestView.setVisibility(View.GONE);
				mNoMatchView.setVisibility(View.GONE);
				mStrangeNumberHolder.setVisibility(View.VISIBLE);
			}
			break;

		default:
			break;
		}
    }
    
    private float mAnimMoveY;
    private float mDigitsTop;
    private Animation mKeySectionShowAnim = null;
    private Animation mKeySectionHideAnim = null;
    private boolean mIsDialpadAniming = false;
    //Gionee:huangzy 20120611 modify for CR00623367 start
    public void gnShowDialpad(boolean show) {
    	if (mIsDialpadAniming) {    		
    		return;
    	}    	
    	
    	if (mListView != null && gnIsVisible(mListView.getEmptyView()) && !mHasPermanentMenuKey) {
    		if (!show && (gnIsVisible(mSugestView) || gnIsVisible(mNoMatchView) || gnIsVisible(mStrangeNumberHolder)) ) {
    			return;
    		}
    	}    	
    	
        final int visibility = show ? View.VISIBLE : View.GONE;
        
        if (mDialpad.getVisibility() == visibility) {
        	return;
        }
        
        if (0 == mAnimMoveY) {
        	mAnimMoveY = mDialpad.getHeight() + mAdditionalButtonsRow.getHeight();
        	
        	int[] location = new int[2];
        	mDigits.getLocationInWindow(location);
        	mDigitsTop = location[1];
        }
        
        if (!show) {
        	if (null == mKeySectionHideAnim) {
        		mKeySectionHideAnim = new TranslateAnimation(0.0f, 0.0f, 0.0f, mAnimMoveY);
                mKeySectionHideAnim.setDuration(250);
                mKeySectionHideAnim.setInterpolator(new AccelerateInterpolator());
                mKeySectionHideAnim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
						mIsDialpadAniming = true;
						mDigits.setCursorVisible(false);
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
						
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						mGnDialpadButton.setVisibility(View.VISIBLE);
			        	mDelete.setVisibility(View.GONE);
			        	mDigitsContainer.setVisibility(View.VISIBLE);
			        	mDialpad.setVisibility(visibility);
			            mAdditionalButtonsRow.setVisibility(visibility);
			            mIsDialpadAniming = false;
					}
				});
        	}
        	
        	mKeyLayout.startAnimation(mKeySectionHideAnim);        
        } else {        	
        	if (null == mKeySectionShowAnim) {
	        	mKeySectionShowAnim = new TranslateAnimation(0.0f, 0.0f, mAnimMoveY, 0.0f);   
	            mKeySectionShowAnim.setDuration(250);
	            mKeySectionShowAnim.setInterpolator(new AccelerateInterpolator());
	            mKeySectionShowAnim.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation animation) {
					    mGnDialpadButton.setVisibility(View.GONE);
			        	mDelete.setVisibility(View.VISIBLE);
			        	mDigitsContainer.setVisibility(TextUtils.isEmpty(mDigits.getText()) ? View.GONE : View.VISIBLE);
						mDialpad.setVisibility(visibility);
			            mAdditionalButtonsRow.setVisibility(visibility);			            
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
						
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						mDigits.setCursorVisible(true);
					}
	            });
        	}        	
        	mKeyLayout.startAnimation(mKeySectionShowAnim);
        }
    }
    //Gionee:huangzy 20120611 modify for CR00623367 end
    
    private void gnShowDialpadChooser(boolean enabled) {
        // Check if onCreateView() is already called by checking one of View objects.
        log("showDialpadChooser, enabled = " + enabled);
        if (!isLayoutReady()) {
            return;
        }

        if (enabled) {            
            mDialpadChooser.setVisibility(View.VISIBLE);

            if(ContactsApplication.sDialerSearchSupport) {
                if(mResultLayout != null)
                	mResultLayout.setVisibility(View.GONE);
            }
            
            mKeyLayout.setVisibility(View.GONE);

            // Instantiate the DialpadChooserAdapter and hook it up to the
            // ListView.  We do this only once.
            if (mDialpadChooserAdapter == null) {
                mDialpadChooserAdapter = new DialpadChooserAdapter(getActivity());
            }
            mDialpadChooser.setAdapter(mDialpadChooserAdapter);
        } else {
            mDialpadChooser.setVisibility(View.GONE);
            if (mKeyLayout != null) {
                mKeyLayout.setVisibility(View.VISIBLE);
            }

            if(ContactsApplication.sDialerSearchSupport) {
                if(mResultLayout != null)
                	mResultLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public Rect gnGetKeyLayoutRect() {
    	if (null == mKeyLayout) {
    		return null;
    	}
    	
    	Rect rect = new Rect();
    	mKeyLayout.getGlobalVisibleRect(rect);
    	
    	return rect; 
    }
    
    private boolean gnIsVisible(View view) {
    	if (null == view)
    		return false;
    	
    	return view.getVisibility() == View.VISIBLE;
    }
    
    protected boolean isWifiOnly() {
    	ConnectivityManager cm = (ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return (cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE) == false);
	}
    
    protected boolean gnOnStarLongClick() {
		if (!ContactsApplication.sIsGnContactsSupport)
			return false;
		
		// gionee xuhz 20120507 add for CR00589238 start
		if (mDigits.getSelectionStart() <= 0) {
		    return false;
		}
		// gionee xuhz 20120507 add for CR00589238 end
		
		//Gionee:huangzhiyuan 20120507 add for CR00585981 start
		new EditTextSwitchAdder(mDigits, mHandler, new String[]{"P", "W"}) {
			DialtactsActivity mAct = (DialtactsActivity) getActivity();
			@Override
			public boolean isGoingOn() {
				return mAct.getIsPressing();
			}
		}.run();
		//Gionee:huangzhiyuan 20120507 add for CR00585981 end
		
		return true;
	}
    
    protected String getSearchTip() {
    	String color = "<font color=" + getResources().getColor(R.color.people_app_theme_color) + ">";
    	
    	String searchTip = getString(R.string.gn_dialer_suggest_show);
    	if (null == searchTip) {
    		return null;
    	}
    	
    	searchTip = searchTip.replace("$", color);
    	searchTip = searchTip.replace("^", "</font>");
    	
    	return searchTip;
	}

    // gionee xuhz 20120606 add start
    public void gnShowAdditionalButtons(boolean isShowAdditionalButtons) {
        if (!ContactsApplication.sIsGnContactsSupport) {
            return;
        }
        if (mAdditionalButtonsRow == null) {
            return;
        }
        if (mAdditionalButtonsRow.getVisibility() == View.GONE) {
            return;
        }
        if (isShowAdditionalButtons) {
            mAdditionalButtonsRow.setVisibility(View.VISIBLE);
        } else {
            mAdditionalButtonsRow.setVisibility(View.INVISIBLE);
        }
    }
    // gionee xuhz 20120530 add end
    
}

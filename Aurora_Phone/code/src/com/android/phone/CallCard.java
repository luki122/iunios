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

package com.android.phone;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

import java.util.List;
import java.util.jar.Attributes.Name;

import javax.crypto.NullCipher;

import android.widget.FrameLayout;
import android.os.SystemVibrator;

import com.android.internal.telephony.ITelephony;
import com.android.phone.PhoneGlobals;

import android.os.ServiceManager;
import android.os.Build;
/**
 * "Call card" UI element: the in-call screen contains a tiled layout of call
 * cards, each representing the state of a current "call" (ie. an active call,
 * a call on hold, or an incoming call.)
 */
public class CallCard extends RelativeLayout
        implements CallTime.OnTickListener, AuroraCallerInfoAsyncQuery.OnQueryCompleteListener,
                   ContactsAsyncHelper.OnImageLoadCompleteListener {
    private static final String LOG_TAG = "CallCard";
    private static final boolean DBG = (PhoneGlobals.DBG_LEVEL >= 2);

    private static final int TOKEN_UPDATE_PHOTO_FOR_CALL_STATE = 0;
    private static final int TOKEN_DO_NOTHING = 1;

    /**
     * Used with {@link ContactsAsyncHelper#startObtainPhotoAsync(int, Context, Uri,
     * ContactsAsyncHelper.OnImageLoadCompleteListener, Object)}
     */
    private static class AsyncLoadCookie {
        public final ImageView imageView;
        public final AuroraCallerInfo callerInfo;
        public final Call call;
        public AsyncLoadCookie(ImageView imageView, AuroraCallerInfo callerInfo, Call call) {
            this.imageView = imageView;
            this.callerInfo = callerInfo;
            this.call = call;
        }
    }

    /**
     * Reference to the InCallScreen activity that owns us.  This may be
     * null if we haven't been initialized yet *or* after the InCallScreen
     * activity has been destroyed.
     */
    private InCallScreen mInCallScreen;

    // Phone app instance
    private PhoneGlobals mApplication;

    // Top-level subviews of the CallCard
    /** Container for info about the current call(s) */
    private ViewGroup mCallInfoContainer;
    /** Primary "call info" block (the foreground or ringing call) */
    protected ViewGroup mPrimaryCallInfo;
    /** "Call banner" for the primary call */
    private ViewGroup mPrimaryCallBanner;
    /** Secondary "call info" block (the background "on hold" call) */
    private ViewStub mSecondaryCallInfo;

    /**
     * Container for both provider info and call state. This will take care of showing/hiding
     * animation for those views.
     */
    private ViewGroup mSecondaryInfoContainer;
    private ViewGroup mProviderInfo;
    private TextView mProviderLabel;
    private TextView mProviderAddress;

    // "Call state" widgets
    private TextView mCallStateLabel;
    private DialingView mDialingView;
    private TextView mElapsedTime, mDtmfElapsedTime;

    // Text colors, used for various labels / titles
    private int mTextColorCallTypeSip;

    // The main block of info about the "primary" or "active" call,
    // including photo / name / phone number / etc.
    private ImageView mPhoto, mPhotoV, mPhotoCover;
    private View mPhotoDimEffect;
    private ImageView mSimIcon, mSimIcon1, mSimIcon2 , mSimIcon3, mSimIcon4; 


    private TextView mName;
    private TextView mPhoneNumber;
    private View mNumberSeparator;
    private TextView mLabel;
    private TextView mCallTypeLabel;
    // private TextView mSocialStatus;

    /**
     * Uri being used to load contact photo for mPhoto. Will be null when nothing is being loaded,
     * or a photo is already loaded.
     */
    private Uri mLoadingPersonUri;

    // Info about the "secondary" call, which is the "call on hold" when
    // two lines are in use.
    private TextView mSecondaryCallName;
    private ImageView mSecondaryCallPhoto;
    private TextView mSecondCallTime;
    private View mSecondaryCallPhotoDimEffect;

    // Onscreen hint for the incoming call RotarySelector widget.
    private int mIncomingCallWidgetHintTextResId;
    private int mIncomingCallWidgetHintColorResId;

    private CallTime mCallTime;

    // Track the state for the photo.
    private ContactsAsyncHelper.ImageTracker mPhotoTracker;

    // Cached DisplayMetrics density.
    private float mDensity;

    private boolean mAudioDeviceInitialized = false;

    // Constants for TelephonyProperties.PROPERTY_IMS_AUDIO_OUTPUT property.
    // Currently, the default audio output is headset if connected, bluetooth
    // if connected, speaker/earpiece for video/voice call.
    private static final int IMS_AUDIO_OUTPUT_DEFAULT = 0;
    private static final int IMS_AUDIO_OUTPUT_DISABLE_SPEAKER = 1;
    /**
     * Controls audio route for VT calls.
     * 0 - Use the default audio routing strategy.
     * 1 - Disable the speaker. Route the audio to Headset or Bloutooth
     *     or Earpiece, based on the default audio routing strategy.
     * This property is for testing purpose only.
     */
    static final String PROPERTY_IMS_AUDIO_OUTPUT =
                                "persist.radio.ims.audio.output";


    /**
     * Sent when it takes too long (MESSAGE_DELAY msec) to load a contact photo for the given
     * person, at which we just start showing the default avatar picture instead of the person's
     * one. Note that we will *not* cancel the ongoing query and eventually replace the avatar
     * with the person's photo, when it is available anyway.
     */
    private static final int MESSAGE_SHOW_UNKNOWN_PHOTO = 101;
    private static final int DIALING_CALL_WIDGET_PING = 102;
    private static final int MESSAGE_DELAY = 500; // msec
    private static final int MESSAGE_DIALING_DELAY = 1500; // msec
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_UNKNOWN_PHOTO:
                	//aurora change liguangyu 20131206 start
                	setPhotoVisible();
                	if(mPhoto.getTag() == null) {
                		showImage(mPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                	}
                	//aurora change liguangyu 20131206 end
                    break;
                case DIALING_CALL_WIDGET_PING:
                	   Log.d(LOG_TAG, "DIALING_CALL_WIDGET_PING ");
                	mDialingView.startAnim();
                	mHandler.removeMessages(DIALING_CALL_WIDGET_PING);
                    mHandler.sendEmptyMessageDelayed(DIALING_CALL_WIDGET_PING,
                    		MESSAGE_DIALING_DELAY);
                	break;
                default:
                    Log.wtf(LOG_TAG, "mHandler: unexpected message: " + msg);
                    break;
            }
        }
    };

    public CallCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (DBG) log("CallCard constructor...");
        if (DBG) log("- this = " + this);
        if (DBG) log("- context " + context + ", attrs " + attrs);

        mApplication = PhoneGlobals.getInstance();

        mCallTime = new CallTime(this);

        // create a new object to track the state for the photo.
        mPhotoTracker = new ContactsAsyncHelper.ImageTracker();

        mDensity = getResources().getDisplayMetrics().density;
        mRobotoLightTf = Typeface.createFromFile("system/fonts/Roboto-Light.ttf");
        
        if (DBG) log("- Density: " + mDensity);
    }

    /* package */ void setInCallScreenInstance(InCallScreen inCallScreen) {
        mInCallScreen = inCallScreen;
       if(mInCallScreen != null) {
//         mAnimController = mInCallScreen.mCallCardAnimController;
       }
    }


    @Override
    public void onTickForCallTimeElapsed(long timeElapsed) {
        // While a call is in progress, update the elapsed time shown
        // onscreen.
        updateElapsedTimeWidget(timeElapsed);
    }

    /* package */ void stopTimer() {
        mCallTime.cancelTimer();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        if (DBG) log("CallCard onFinishInflate(this = " + this + ")...");

        mCallInfoContainer = (ViewGroup) findViewById(R.id.call_info_container);
        mPrimaryCallInfo = (ViewGroup) findViewById(R.id.primary_call_info);
        mPrimaryCallBanner = (ViewGroup) findViewById(R.id.primary_call_banner);

        mSecondaryInfoContainer = (ViewGroup) findViewById(R.id.secondary_info_container);
        mProviderInfo = (ViewGroup) findViewById(R.id.providerInfo);
        mProviderLabel = (TextView) findViewById(R.id.providerLabel);
        mProviderAddress = (TextView) findViewById(R.id.providerAddress);
        mCallStateLabel = (TextView) findViewById(R.id.callStateLabel);
        mDialingView = (DialingView) findViewById(R.id.dialing_anim);
//        mElapsedTime = (TextView) findViewById(R.id.elapsedTime);  

        // Text colors
        mTextColorCallTypeSip = getResources().getColor(R.color.incall_callTypeSip);

        // "Caller info" area, including photo / name / phone numbers / etc
        mPhoto = (ImageView) findViewById(R.id.photo);
        mPhotoCover = (ImageView) findViewById(R.id.photo_cover);
        mPhotoV = (ImageView) findViewById(R.id.photoV);
        mPhotoDimEffect = findViewById(R.id.dim_effect_for_primary_photo);

        mName = (TextView) findViewById(R.id.name);
        mPhoneNumber = (TextView) findViewById(R.id.phoneNumber);
        mNumberSeparator = findViewById(R.id.number_separator);
        mLabel = (TextView) findViewById(R.id.label);
        mCallTypeLabel = (TextView) findViewById(R.id.callTypeLabel);
        // mSocialStatus = (TextView) findViewById(R.id.socialStatus);

        // Secondary info area, for the background ("on hold") call
        mSecondaryCallInfo = (ViewStub) findViewById(R.id.secondary_call_info);
        mThirdCallInfo = (ViewStub) findViewById(R.id.third_call_info);
        mFourthCallInfo = (ViewStub) findViewById(R.id.fourth_call_info);
        mWaitingCallInfo = (ViewStub) findViewById(R.id.call_waiting_info);


        mSimIcon = (ImageView) findViewById(R.id.aurora_sim_slot);
        mSimIcon.setAlpha(0.7f);

 	
		mSloganNote = (TextView)findViewById(R.id.slogan);
 	
        mNoteLine =  findViewById(R.id.note_line);
        mNote = (TextView) findViewById(R.id.note);
        mAdrress = (TextView) findViewById(R.id.numberAddress);
//        meclipstimebg=(ImageView)findViewById(R.id.meclapsedbg);
        mFirstCallInfo = (ViewStub) findViewById(R.id.first_call_info);   
//        guangquan=findViewById(R.id.photoguangquan);
        mPhoneNumber.setTypeface(mRobotoLightTf);                 

        setVisibility(View.INVISIBLE);               
        
    }

    /**
     * Updates the state of all UI elements on the CallCard, based on the
     * current state of the phone.
     */
    /* package */ void updateState(CallManager cm) {
        if (DBG) log("updateState(" + cm + ")...");

        // Update the onscreen UI based on the current state of the phone.

        PhoneConstants.State state = cm.getState();  // IDLE, RINGING, or OFFHOOK
        Call ringingCall = cm.getFirstActiveRingingCall();
        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();

        // Update the overall layout of the onscreen elements, if in PORTRAIT.
        // Portrait uses a programatically altered layout, whereas landscape uses layout xml's.
        // Landscape view has the views side by side, so no shifting of the picture is needed
        if (!PhoneUtils.isLandscape(this.getContext())) {
            updateCallInfoLayout(state);
        }

        // If the FG call is dialing/alerting, we should display for that call
        // and ignore the ringing call. This case happens when the telephony
        // layer rejects the ringing call while the FG call is dialing/alerting,
        // but the incoming call *does* briefly exist in the DISCONNECTING or
        // DISCONNECTED state.
        Log.v("PhoneAnim", "updateState state="+state+"ringingCall.getState()="+ringingCall.getState()+"fgCall="+fgCall.getState()); 

//        Log.v("CallCard", "is_incoming_to_incall_start="+is_incoming_to_incall_start+"is_reject_to_end_start="+is_reject_to_end_start
//        		+"is_reject_to_sms_start="+is_reject_to_sms_start+"is_hide_start="+is_hide_start+"is_show_start="+is_show_start);
        if ((ringingCall.getState() != Call.State.IDLE)
                && !fgCall.getState().isDialing()) {
			// A phone call is ringing, call waiting *or* being rejected
			// (ie. another call may also be active as well.)
//			if (mAnimController.isRejectToEndStart()) {
//				return;
//			}

//			if (!mAnimController.isAnimationingInRinging()) {
				// aurora add zhouxiaobing 20140106
				updateRingingCall(cm);
//			}

//			mAnimController.updateRingingCall(ringingCall);

        } else if ((fgCall.getState() != Call.State.IDLE)
                || (bgCall.getState() != Call.State.IDLE)) {
            // We are here because either:
            // (1) the phone is off hook. At least one call exists that is
            // dialing, active, or holding, and no calls are ringing or waiting,
            // or:
            // (2) the phone is IDLE but a call just ended and it's still in
            // the DISCONNECTING or DISCONNECTED state. In this case, we want
            // the main CallCard to display "Hanging up" or "Call ended".
            // The normal "foreground call" code path handles both cases.
            updateForegroundCall(cm);
            updateOtherForegroundCall(cm);
//            mAnimController.updateForegroundCall(fgCall, bgCall);

            if(!mApplication.inCallUiState.showDialpad) {
            	setVisibility(View.VISIBLE);
            }
        } else {
            // We don't have any DISCONNECTED calls, which means that the phone
            // is *truly* idle.
        	Log.v("PhoneAnim", " other fgCall.getState()="+fgCall.getState());
        	
        	boolean show = false;
         	List<Call> l = cm.getForegroundCalls();
        	if(l != null) {
	        	for(Call call : l) {
	        		if(call.getState() != Call.State.IDLE) {
	        			show = true;
	        			break;
	        		}
	        	}
        	}
        	if(!show && !mInCallScreen.mIsUserHangup) {
        		setVisibility(View.INVISIBLE);
        	}
        	
            if (mApplication.inCallUiState.showAlreadyDisconnectedState) {
                // showAlreadyDisconnectedState implies the phone call is disconnected
                // and we want to show the disconnected phone call for a moment.
                //
                // This happens when a phone call ends while the screen is off,
                // which means the user had no chance to see the last status of
                // the call. We'll turn off showAlreadyDisconnectedState flag
                // and bail out of the in-call screen soon.
                updateAlreadyDisconnected(cm);
            } else {
                // It's very rare to be on the InCallScreen at all in this
                // state, but it can happen in some cases:
                // - A stray onPhoneStateChanged() event came in to the
                //   InCallScreen *after* it was dismissed.
                // - We're allowed to be on the InCallScreen because
                //   an MMI or USSD is running, but there's no actual "call"
                //   to display.
                // - We're displaying an error dialog to the user
                //   (explaining why the call failed), so we need to stay on
                //   the InCallScreen so that the dialog will be visible.
                //
                // In these cases, put the callcard into a sane but "blank" state:
                updateNoCall(cm);
            }
        }
        
       updateDtmfHold();
        
    }

    /**
     * Updates the overall size and positioning of mCallInfoContainer and
     * the "Call info" blocks, based on the phone state.
     */
    private void updateCallInfoLayout(PhoneConstants.State state) {
        boolean ringing = (state == PhoneConstants.State.RINGING);
        if (DBG) log("updateCallInfoLayout()...  ringing = " + ringing);

        // Based on the current state, update the overall
        // CallCard layout:

        // - Update the bottom margin of mCallInfoContainer to make sure
        //   the call info area won't overlap with the touchable
        //   controls on the bottom part of the screen.

        int reservedVerticalSpace = mInCallScreen.getInCallTouchUi().getTouchUiHeight();
        ViewGroup.MarginLayoutParams callInfoLp =
                (ViewGroup.MarginLayoutParams) mCallInfoContainer.getLayoutParams();
        callInfoLp.bottomMargin = reservedVerticalSpace;  // Equivalent to setting
                                                          // android:layout_marginBottom in XML
        //aurora add liguangyu 20141225 for #10728 start
        if(!DeviceUtils.hasKey() && !DeviceUtils.is8910()) {
        	callInfoLp.bottomMargin -= getResources().getDimensionPixelOffset(R.dimen.honor6_callcard_area_extra_height);  
        }
        //aurora add liguangyu 20141225 for #10728 end        
        
        if (DBG) log("  ==> callInfoLp.bottomMargin: " + reservedVerticalSpace);
        mCallInfoContainer.setLayoutParams(callInfoLp);
    }

    /**
     * Updates the UI for the state where the phone is in use, but not ringing.
     */
    protected void updateForegroundCall(CallManager cm) {
        if (DBG) log("updateForegroundCall()...");
        // if (DBG) PhoneUtils.dumpCallManager();

        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();

        if (fgCall.getState() == Call.State.IDLE) {
            if (DBG) log("updateForegroundCall: no active call, show holding call");
            // TODO: make sure this case agrees with the latest UI spec.

            // Display the background call in the main info area of the
            // CallCard, since there is no foreground call.  Note that
            // displayMainCallStatus() will notice if the call we passed in is on
            // hold, and display the "on hold" indication.
            fgCall = bgCall;

            // And be sure to not display anything in the "on hold" box.
            if(!PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())) {
            	bgCall = null;
            }
        }

        displayMainCallStatus(cm, fgCall);

        Phone phone = fgCall.getPhone();

        int phoneType = phone.getPhoneType();
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if ((mApplication.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                displaySecondaryCallStatus(cm, fgCall);
            } else {
                //This is required so that even if a background call is not present
                // we need to clean up the background call area.
                displaySecondaryCallStatus(cm, bgCall);
            }
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
                || (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
            displaySecondaryCallStatus(cm, bgCall);
        }
        
        displayCallWaitingCallStatus(cm, null);
    }

    /**
     * Updates the UI for the state where an incoming call is ringing (or
     * call waiting), regardless of whether the phone's already offhook.
     */
    protected void updateRingingCall(CallManager cm) {
        if (DBG) log("updateRingingCall()...");

        Call ringingCall = cm.getFirstActiveRingingCall();
        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();

        if (fgCall.getState() == Call.State.IDLE) {           
            fgCall = bgCall;
        }
        
        
        // Display caller-id info and photo from the incoming call:
        displayMainCallStatus(cm, ringingCall);

        // And even in the Call Waiting case, *don't* show any info about
        // the current ongoing call and/or the current call on hold.
        // (Since the caller-id info for the incoming call totally trumps
        // any info about the current call(s) in progress.)
        displaySecondaryCallStatus(cm, null);
        
        displayCallWaitingCallStatus(cm, fgCall);
    }

    /**
     * Updates the UI for the state where an incoming call is just disconnected while we want to
     * show the screen for a moment.
     *
     * This case happens when the whole in-call screen is in background when phone calls are hanged
     * up, which means there's no way to determine which call was the last call finished. Right now
     * this method simply shows the previous primary call status with a photo, closing the
     * secondary call status. In most cases (including conference call or misc call happening in
     * CDMA) this behaves right.
     *
     * If there were two phone calls both of which were hung up but the primary call was the
     * first, this would behave a bit odd (since the first one still appears as the
     * "last disconnected").
     */
    private void updateAlreadyDisconnected(CallManager cm) {
        // For the foreground call, we manually set up every component based on previous state.
    	setPrimaryCallInfoVisibility(true);
        mSecondaryInfoContainer.setLayoutTransition(null);
        mProviderInfo.setVisibility(View.GONE);
        mCallStateLabel.setVisibility(View.VISIBLE);
        mCallStateLabel.setText(mContext.getString(R.string.card_title_call_ended));
//        mElapsedTime.setVisibility(View.VISIBLE);
        mCallTime.cancelTimer();

        // Just hide it.
        displaySecondaryCallStatus(cm, null);
    }

    /**
     * Updates the UI for the state where the phone is not in use.
     * This is analogous to updateForegroundCall() and updateRingingCall(),
     * but for the (uncommon) case where the phone is
     * totally idle.  (See comments in updateState() above.)
     *
     * This puts the callcard into a sane but "blank" state.
     */
    private void updateNoCall(CallManager cm) {
        if (DBG) log("updateNoCall()...");

        displayMainCallStatus(cm, null);
        displaySecondaryCallStatus(cm, null);
        displayCallWaitingCallStatus(cm, null);
    }

    /**
     * Updates the main block of caller info on the CallCard
     * (ie. the stuff in the primaryCallInfo block) based on the specified Call.
     */
    private void displayMainCallStatus(CallManager cm, Call call) {
        if (DBG) log("displayMainCallStatus(call " + call + ")...");

        if (call == null) {
            // There's no call to display, presumably because the phone is idle.
//                	setPrimaryCallInfoVisibility(false);
            return;
        }
     //aurora add zhouxiaobing 20140108 start   
        if(call.getState()==Call.State.DISCONNECTING) {
           	displaySecondaryCallStatus(cm, null);
}
     //aurora add zhouxiaobing 20140108 end  
    	setPrimaryCallInfoVisibility(true);

        Call.State state = call.getState();
        if (DBG) log("  - call.state: " + call.getState());

        switch (state) {
            case ACTIVE:
            case DISCONNECTING:
                // update timer field
                if (DBG) log("displayMainCallStatus: start periodicUpdateTimer");
                mCallTime.setActiveCallMode(call);
                mCallTime.reset();
                mCallTime.periodicUpdateTimer();

                break;

            case HOLDING:
                // update timer field
                mCallTime.cancelTimer();

                break;

            case DISCONNECTED:
                // Stop getting timer ticks from this call
                mCallTime.cancelTimer();

                break;

            case DIALING:
            case ALERTING:
                // Stop getting timer ticks from a previous call
                mCallTime.cancelTimer();

                break;

            case INCOMING:
            case WAITING:
                // Stop getting timer ticks from a previous call
            	if(cm.hasActiveFgCall()) {
	                mCallTime.reset();
	                mCallTime.periodicUpdateTimer();
            	} else {
                    mCallTime.cancelTimer();
            	}

                break;

            case IDLE:
                // The "main CallCard" should never be trying to display
                // an idle call!  In updateState(), if the phone is idle,
                // we call updateNoCall(), which means that we shouldn't
                // have passed a call into this method at all.
                Log.w(LOG_TAG, "displayMainCallStatus: IDLE call in the main call card!");

                // (It is possible, though, that we had a valid call which
                // became idle *after* the check in updateState() but
                // before we get here...  So continue the best we can,
                // with whatever (stale) info we can get from the
                // passed-in Call object.)

                break;

            default:
                Log.w(LOG_TAG, "displayMainCallStatus: unexpected call state: " + state);
                break;
        }

        updateCallStateWidgets(call);
        

        if (PhoneUtils.isConferenceCall(call)) {
            // Update onscreen info for a conference call.
            updateDisplayForConference(call);
        } else {
            // Update onscreen info for a regular call (which presumably
            // has only one connection.)
            Connection conn = null;
            int phoneType = call.getPhone().getPhoneType();
            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                conn = call.getLatestConnection();
            } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM || phoneType == PhoneConstants.PHONE_TYPE_IMS)
                  || (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
                conn = call.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }

            if (conn == null) {
                if (DBG) log("displayMainCallStatus: connection is null, using default values.");
                // if the connection is null, we run through the behaviour
                // we had in the past, which breaks down into trivial steps
                // with the current implementation of getCallerInfo and
                // updateDisplayForPerson.
                AuroraCallerInfo info = PhoneUtils.getCallerInfo(getContext(), null /* conn */);
                updateDisplayForPerson(info, PhoneConstants.PRESENTATION_ALLOWED, false, call,
                        conn);
            } else {
                if (DBG) log("  - CONN: " + conn + ", state = " + conn.getState());
                int presentation = conn.getNumberPresentation();

                // make sure that we only make a new query when the current
                // callerinfo differs from what we've been requested to display.
                boolean runQuery = true;
                Object o = conn.getUserData();
                if (o instanceof PhoneUtils.CallerInfoToken) {
                    runQuery = mPhotoTracker.isDifferentImageRequest(
                            ((PhoneUtils.CallerInfoToken) o).currentInfo);
                } else {
                    runQuery = mPhotoTracker.isDifferentImageRequest(conn);
                }

                // Adding a check to see if the update was caused due to a Phone number update
                // or CNAP update. If so then we need to start a new query
                if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                    Object obj = conn.getUserData();
                    String updatedNumber = conn.getAddress();
                    String updatedCnapName = conn.getCnapName();
                    AuroraCallerInfo info = null;
                    if (obj instanceof PhoneUtils.CallerInfoToken) {
                        info = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                    } else if (o instanceof AuroraCallerInfo) {
                        info = (AuroraCallerInfo) o;
                    }

                    if (info != null) {
                        if (updatedNumber != null && !updatedNumber.equals(info.phoneNumber)) {
                            if (DBG) log("- displayMainCallStatus: updatedNumber = "
                                    + updatedNumber);
                            runQuery = true;
                        }
                        if (updatedCnapName != null && !updatedCnapName.equals(info.cnapName)) {
                            if (DBG) log("- displayMainCallStatus: updatedCnapName = "
                                    + updatedCnapName);
                            runQuery = true;
                        }
                    }
                }

                if (runQuery) {
                    if (DBG) log("- displayMainCallStatus: starting CallerInfo query...");
                    PhoneUtils.CallerInfoToken info =
                            PhoneUtils.startGetCallerInfo(getContext(), conn, this, call);
                    updateDisplayForPerson(info.currentInfo, presentation, !info.isFinal,
                                           call, conn);
                } else {
                    // No need to fire off a new query.  We do still need
                    // to update the display, though (since we might have
                    // previously been in the "conference call" state.)
                    if (DBG) log("- displayMainCallStatus: using data we already have...");
                    if (o instanceof AuroraCallerInfo) {
                    	AuroraCallerInfo ci = (AuroraCallerInfo) o;
                        // Update CNAP information if Phone state change occurred
                        ci.cnapName = conn.getCnapName();
                        ci.numberPresentation = conn.getNumberPresentation();
                        ci.namePresentation = conn.getCnapNamePresentation();
                        if (DBG) log("- displayMainCallStatus: CNAP data from Connection: "
                                + "CNAP name=" + ci.cnapName
                                + ", Number/Name Presentation=" + ci.numberPresentation);
                        if (DBG) log("   ==> Got CallerInfo; updating display: ci = " + ci);
                        updateDisplayForPerson(ci, presentation, false, call, conn);
                    } else if (o instanceof PhoneUtils.CallerInfoToken){
                    	AuroraCallerInfo ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                        if (DBG) log("- displayMainCallStatus: CNAP data from Connection: "
                                + "CNAP name=" + ci.cnapName
                                + ", Number/Name Presentation=" + ci.numberPresentation);
                        if (DBG) log("   ==> Got CallerInfoToken; updating display: ci = " + ci);
                        updateDisplayForPerson(ci, presentation, true, call, conn);
                    } else {
                        Log.w(LOG_TAG, "displayMainCallStatus: runQuery was false, "
                              + "but we didn't have a cached CallerInfo object!  o = " + o);
                        // TODO: any easy way to recover here (given that
                        // the CallCard is probably displaying stale info
                        // right now?)  Maybe force the CallCard into the
                        // "Unknown" state?
                    }
                }
            }
        }

        // In some states we override the "photo" ImageView to be an
        // indication of the current state, rather than displaying the
        // regular photo as set above.
        updatePhotoForCallState(call);

        // One special feature of the "number" text field: For incoming
        // calls, while the user is dragging the RotarySelector widget, we
        // use mPhoneNumber to display a hint like "Rotate to answer".
        if (mIncomingCallWidgetHintTextResId != 0) {
            // Display the hint!
            mPhoneNumber.setText(mIncomingCallWidgetHintTextResId);
            mPhoneNumber.setTextColor(getResources().getColor(mIncomingCallWidgetHintColorResId));
            mPhoneNumber.setVisibility(View.VISIBLE);
            mNumberSeparator.setVisibility(View.VISIBLE);
            mLabel.setVisibility(View.GONE);
        }
        // If we don't have a hint to display, just don't touch
        // mPhoneNumber and mLabel. (Their text / color / visibility have
        // already been set correctly, by either updateDisplayForPerson()
        // or updateDisplayForConference().)
        
        hideViewsForPrivate(call);
        
    }


    /**
     * Return true if mPhoto is available and is visible
     *
     * @return
     */
    private boolean isPhotoVisible() {
        return ((mPhoto != null) && (mPhoto.getVisibility() == View.VISIBLE));
    }



    /**
     * Implemented for CallerInfoAsyncQuery.OnQueryCompleteListener interface.
     * refreshes the CallCard data when it called.
     */
    @Override
    public void onQueryComplete(int token, Object cookie, AuroraCallerInfo ci) {
        if (DBG) log("onQueryComplete: token " + token + ", cookie " + cookie + ", ci " + ci);

        if (cookie instanceof Call) {
            // grab the call object and update the display for an individual call,
            // as well as the successive call to update image via call state.
            // If the object is a textview instead, we update it as we need to.
            if (DBG) log("callerinfo query complete, updating ui from displayMainCallStatus()");
            Call call = (Call) cookie;
            Connection conn = null;
            int phoneType = call.getPhone().getPhoneType();
            if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                conn = call.getLatestConnection();
            } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
                  || (phoneType == PhoneConstants.PHONE_TYPE_SIP)
                  || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
                conn = call.getEarliestConnection();
            } else {
                throw new IllegalStateException("Unexpected phone type: " + phoneType);
            }
            PhoneUtils.CallerInfoToken cit =
                   PhoneUtils.startGetCallerInfo(getContext(), conn, this, null);

            int presentation = PhoneConstants.PRESENTATION_ALLOWED;
            if (conn != null) presentation = conn.getNumberPresentation();
            if (DBG) log("- onQueryComplete: presentation=" + presentation
                    + ", contactExists=" + ci.contactExists);

            // Depending on whether there was a contact match or not, we want to pass in different
            // CallerInfo (for CNAP). Therefore if ci.contactExists then use the ci passed in.
            // Otherwise, regenerate the CIT from the Connection and use the CallerInfo from there.
            if (ci.contactExists) {
                updateDisplayForPerson(ci, PhoneConstants.PRESENTATION_ALLOWED, false, call, conn);
            } else {
                updateDisplayForPerson(cit.currentInfo, presentation, false, call, conn);
            			
            }
            updatePhotoForCallState(call);

        } else if (cookie instanceof TextView){
            if (DBG) log("callerinfo query complete, updating ui from ongoing or onhold");
            ((TextView) cookie).setText(PhoneUtils.getCompactNameFromCallerInfo(ci, mContext));
        }
    }

    /**
     * Implemented for ContactsAsyncHelper.OnImageLoadCompleteListener interface.
     * make sure that the call state is reflected after the image is loaded.
     */
    @Override
    public void onImageLoadComplete(int token, Drawable photo, Bitmap photoIcon, Object cookie) {
        mHandler.removeMessages(MESSAGE_SHOW_UNKNOWN_PHOTO);
        if (mLoadingPersonUri != null) {
            // Start sending view notification after the current request being done.
            // New image may possibly be available from the next phone calls.
            //
            // TODO: may be nice to update the image view again once the newer one
            // is available on contacts database.
            PhoneUtils.sendViewNotificationAsync(mApplication, mLoadingPersonUri);
        } else {
            // This should not happen while we need some verbose info if it happens..
            Log.w(LOG_TAG, "Person Uri isn't available while Image is successfully loaded.");
        }
        mLoadingPersonUri = null;

        AsyncLoadCookie asyncLoadCookie = (AsyncLoadCookie) cookie;
        AuroraCallerInfo callerInfo = asyncLoadCookie.callerInfo;
        ImageView imageView = asyncLoadCookie.imageView;
        Call call = asyncLoadCookie.call;

        callerInfo.cachedPhoto = photo;
        callerInfo.cachedPhotoIcon = photoIcon;
        callerInfo.isCachedPhotoCurrent = true;
 	    log(" showCachedImage remove cover");
       	if(imageView == mPhoto) {
       		mPhotoCover.setVisibility(View.GONE);
       	}
        

        // Note: previously ContactsAsyncHelper has done this job.
        // TODO: We will need fade-in animation. See issue 5236130.
        //aurora add liguangyu 20131206 start
        //mPhoto.setVisibility(View.VISIBLE);
//        if(!mAnimController.getIncoming_to_incall_anim_ex()){
        	imageView.setVisibility(View.VISIBLE);
//        }
        //aurora add liguangyu 20131206 end
        if (photo != null) {
            showImage(imageView, photo);
        } else if (photoIcon != null) {
            showImage(imageView, photoIcon);
        } else {
            showImage(imageView, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
        }

        if (token == TOKEN_UPDATE_PHOTO_FOR_CALL_STATE) {
            updatePhotoForCallState(call);
        }
    }

    /**
     * Updates the "call state label" and the elapsed time widget based on the
     * current state of the call.
     */
    private void updateCallStateWidgets(Call call) {
        if (DBG) log("updateCallStateWidgets(call " + call + ")...");
        final Call.State state = call.getState();
        final Context context = getContext();
        final Phone phone = call.getPhone();
        final int phoneType = phone.getPhoneType();

        String callStateLabel = null;  // Label to display as part of the call banner
        int bluetoothIconId = 0;  // Icon to display alongside the call state label

        switch (state) {
            case IDLE:
                // "Call state" is meaningless in this state.
                break;

            case ACTIVE:
                // We normally don't show a "call state label" at all in
                // this state (but see below for some special cases).
                break;

            case HOLDING:
                callStateLabel = context.getString(R.string.card_title_on_hold);
                break;

            case DIALING:
            case ALERTING:
                callStateLabel = context.getString(R.string.card_title_dialing);
                break;

            case INCOMING:
            case WAITING:
                //callStateLabel = context.getString(R.string.card_title_incoming_call);//aurora change zhouxiaobing 20131015

                // Also, display a special icon (alongside the "Incoming call"
                // label) if there's an incoming call and audio will be routed
                // to bluetooth when you answer it.
                if (mApplication.showBluetoothIndication()) {
                    bluetoothIconId = R.drawable.ic_incoming_call_bluetooth;
                }
                break;

            case DISCONNECTING:
                // While in the DISCONNECTING state we display a "Hanging up"
                // message in order to make the UI feel more responsive.  (In
                // GSM it's normal to see a delay of a couple of seconds while
                // negotiating the disconnect with the network, so the "Hanging
                // up" state at least lets the user know that we're doing
                // something.  This state is currently not used with CDMA.)
                callStateLabel = context.getString(R.string.card_title_hanging_up);
                break;

            case DISCONNECTED:
                callStateLabel = getCallFailedString(call);
                break;

            default:
                Log.wtf(LOG_TAG, "updateCallStateWidgets: unexpected call state: " + state);
                break;
        }

        // Check a couple of other special cases (these are all CDMA-specific).

        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if ((state == Call.State.ACTIVE)
                && (mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing() || mApplication.cdmaPhoneCallState.getSiggleDialingState())) {
                // Display "Dialing" while dialing a 3Way call, even
                // though the foreground call state is actually ACTIVE.
                callStateLabel = context.getString(R.string.card_title_dialing);
            } else if (PhoneGlobals.getInstance().notifier.getIsCdmaRedialCall()) {
                callStateLabel = context.getString(R.string.card_title_redialing);
            }
        }
        
        if(!TextUtils.isEmpty(callStateLabel) && callStateLabel.equalsIgnoreCase(context.getString(R.string.card_title_dialing))) {
        	mDialingView.setVisibility(View.VISIBLE);
        	mHandler.removeMessages(DIALING_CALL_WIDGET_PING);
            mHandler.sendEmptyMessage(DIALING_CALL_WIDGET_PING);
        } else {
        	mHandler.removeMessages(DIALING_CALL_WIDGET_PING);
        	mDialingView.stopAnim();
        	mDialingView.setVisibility(View.GONE);
        }
        
//        if (PhoneUtils.isPhoneInEcm(phone)) {
//            // In emergency callback mode (ECM), use a special label
//            // that shows your own phone number.
//            callStateLabel = getECMCardTitle(context, phone);
//        }

        final InCallUiState inCallUiState = mApplication.inCallUiState;
        if (DBG) {
            log("==> callStateLabel: '" + callStateLabel
                    + "', bluetoothIconId = " + bluetoothIconId
                    + ", providerInfoVisible = " + inCallUiState.providerInfoVisible);
        }

        // Animation will be done by mCallerDetail's LayoutTransition, but in some cases, we don't
        // want that.
        // - DIALING: This is at the beginning of the phone call.
        // - DISCONNECTING, DISCONNECTED: Screen will disappear soon; we have no time for animation.
        final boolean skipAnimation = (state == Call.State.DIALING
                || state == Call.State.DISCONNECTING
                || state == Call.State.DISCONNECTED);
        LayoutTransition layoutTransition = null;
        if (skipAnimation) {
            // Evict LayoutTransition object to skip animation.
            layoutTransition = mSecondaryInfoContainer.getLayoutTransition();
            mSecondaryInfoContainer.setLayoutTransition(null);
        }
        //aurora add zhouxiaobing 20130927 start
        Connection conn = null;
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
              || (phoneType == PhoneConstants.PHONE_TYPE_SIP)
              || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
     	new AuroraSimContactsTask().execute(conn.getAddress());
		Object userdata = conn.getUserData();
		if (userdata instanceof AuroraCallerInfo) {
			AuroraCallerInfo aci = (AuroraCallerInfo) userdata;
	        updateAddress(aci, call);
		} else {
			updateAddressNull();
		}
         
        if(inCallUiState.providerInfoVisible) {
            mProviderInfo.setVisibility(View.VISIBLE);
            mProviderLabel.setText(context.getString(R.string.calling_via_template,
                    inCallUiState.providerLabel));
            mProviderAddress.setText(inCallUiState.providerAddress);

            mInCallScreen.requestRemoveProviderInfoWithDelay();
        } else {
            mProviderInfo.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(callStateLabel)) {
            mCallStateLabel.setVisibility(View.VISIBLE);
            mCallStateLabel.setText(callStateLabel);

            // ...and display the icon too if necessary.
            if (bluetoothIconId != 0) {
                mCallStateLabel.setCompoundDrawablesWithIntrinsicBounds(bluetoothIconId, 0, 0, 0);
                mCallStateLabel.setCompoundDrawablePadding((int) (mDensity * 5));
            } else {
                // Clear out any icons
                mCallStateLabel.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }
        } else {
            mCallStateLabel.setVisibility(View.GONE);
            // Gravity is aligned left when receiving an incoming call in landscape.
            // In that rare case, the gravity needs to be reset to the right.
            // Also, setText("") is used since there is a delay in making the view GONE,
            // so the user will otherwise see the text jump to the right side before disappearing.
            if(mCallStateLabel.getGravity() != Gravity.RIGHT) {
                mCallStateLabel.setText("");
             //   mCallStateLabel.setGravity(Gravity.RIGHT);//aurora change zhouxiaobing 20131012
            }
        }
        if (skipAnimation) {
            // Restore LayoutTransition object to recover animation.
            mSecondaryInfoContainer.setLayoutTransition(layoutTransition);
        }

       	if(AuroraPhoneUtils.isShowDoubleButton()) {
       		mSimIcon.setVisibility(View.VISIBLE);
	        mSimIcon.setImageResource(SimIconUtils.getSmallSimIcon(PhoneUtils.getSubscription(call.getPhone())));
       	} else {
    		mSimIcon.setVisibility(View.GONE);
       	}
        
        // ...and update the elapsed time widget too.
        switch (state) {
            case DIALING:
            case ALERTING:
            case INCOMING:	
            //aurora add liguangyu 20131112 for BUG #645 start
            case WAITING:
            //aurora add liguangyu 20131112 for BUG #645 end
            	mElapsedTime.setVisibility(View.GONE);
//            	meclipstimebg.setVisibility(View.INVISIBLE);//aurora add zhouxiaobing 20131008
            	updateElapsedTimeWidget(0);
            	Object o = conn.getUserData();
                if (o instanceof AuroraCallerInfo) {
                    AuroraCallerInfo aci = (AuroraCallerInfo) o;
                  	updateNote(state, aci);
                   	updateMark(state, aci);
                } 
               	updateSlogan();
            	break;
            case ACTIVE:            
                // Show the time with fade-in animation.
            	Log.v("CallCard", "DISCONNECTING2");
                //AnimationUtils.Fade.show(mElapsedTime);
                //AnimationUtils.Fade.show(meclipstimebg);
                //aurora add liguangyu 20140730 for BUG #7013 start
             	if(PhoneGlobals.getInstance().mCM.getFgPhone().getPhoneType() != PhoneConstants.PHONE_TYPE_CDMA
            		|| !PhoneGlobals.getInstance().cdmaPhoneCallState.getSiggleDialingState()) {
                //aurora add liguangyu 20140730 for BUG #7013 end         
//             		if(!mAnimController.getIncoming_to_incall_anim_ex()) {
             		if(!PhoneGlobals.getInstance().mCM.hasActiveBgCall() && !mApplication.inCallUiState.showDialpad ) {
                 		mElapsedTime.setVisibility(View.VISIBLE);
             		} else {
             			mElapsedTime.setVisibility(View.GONE);
             		}
//	            		meclipstimebg.setVisibility(View.VISIBLE);
//	            		meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
//             		}
            		updateElapsedTimeWidget(call);  
            	}//aurora add zhouxiaobing 20131008
                //mNote.setVisibility(View.GONE);
                AnimationUtils.Fade.hide(mNote, View.GONE);
    
                mSloganNote.setVisibility(View.GONE);
//            	if(!mAnimController.getIncoming_to_incall_anim_ex()) {  
            		mPhotoV.setVisibility(View.INVISIBLE);
//            	}
                break;
            case HOLDING:
        		mElapsedTime.setVisibility(View.GONE);
//        		meclipstimebg.setVisibility(View.VISIBLE);
//        		meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
        		updateElapsedTimeWidget(call);  
            	break;
            case DISCONNECTING:
            case DISCONNECTED:
                // In the "Call ended" state, leave the mElapsedTime widget
                // visible, but don't touch it (so we continue to see the
                // elapsed time of the call that just ended.)
                // Check visibility to keep possible fade-in animation.
            	mElapsedTime.setVisibility(View.GONE);
                break;

            default:
                // Call state here is IDLE, ACTIVE, HOLDING, DIALING, ALERTING,
                // INCOMING, or WAITING.
                // In all of these states, the "elapsed time" is meaningless, so
                // don't show it.
//            	meclipstimebg.clearAnimation();
                AnimationUtils.Fade.hide(mElapsedTime, View.INVISIBLE);
//                AnimationUtils.Fade.hide(meclipstimebg, View.INVISIBLE);//aurora add zhouxiaobing 20131008
                // Additionally, in call states that can only occur at the start
                // of a call, reset the elapsed time to be sure we won't display
                // stale info later (like if we somehow go straight from DIALING
                // or ALERTING to DISCONNECTED, which can actually happen in
                // some failure cases like "line busy").
                if ((state ==  Call.State.DIALING) || (state == Call.State.ALERTING)) {
                    updateElapsedTimeWidget(0);
                }

                break;
        }
    }

    /**
     * Updates mElapsedTime based on the given {@link Call} object's information.
     *
     * @see CallTime#getCallDuration(Call)
     * @see Connection#getDurationMillis()
     */
    /* package */ void updateElapsedTimeWidget(Call call) {
        long duration = CallTime.getCallDuration(call);  // msec
        if (DBG) log("updateElapsedTimeWidget: duration = " + duration);
        updateElapsedTimeWidget(duration / 1000);
        // Also see onTickForCallTimeElapsed(), which updates this
        // widget once per second while the call is active.
    }

    /**
     * Updates mElapsedTime based on the specified number of seconds.
     */
    private void updateElapsedTimeWidget(long timeElapsed) {
         if (DBG) log("updateElapsedTimeWidget: " + timeElapsed);
        //aurora add liguangyu 20140730 for BUG #7013 start
    	if(PhoneGlobals.getInstance().mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA 
    			&& PhoneGlobals.getInstance().cdmaPhoneCallState.getSiggleDialingState()) {
    		return;
    	}
		// aurora add liguangyu 20140730 for BUG #7013 end
		mElapsedTime.setText(AuroraPhoneUtils.getTimeString(timeElapsed));
		if(mDtmfElapsedTime != null) {
			mDtmfElapsedTime.setText(AuroraPhoneUtils.getTimeString(timeElapsed));
		} else {
			getDtmfCallTime() ;
		}
		// aurora add zhouxiaobing 20131010 start
		if (isNeedUpdateFirstInfoTime() && mfirstcalltime != null) {
			mfirstcalltime.setText(AuroraPhoneUtils.getTimeString(timeElapsed));	
			if(mSecondCallTime != null) {
				  Call bgCall = PhoneGlobals.getInstance().mCM.getFirstActiveBgCall();
				  Connection c = bgCall.getEarliestConnection();
				  if(c!= null) {
						long secondTimeElapsed = bgCall.getEarliestConnection().getDurationMillis()/1000;
						mSecondCallTime.setText(AuroraPhoneUtils.getTimeString(secondTimeElapsed));
				  }
			}
		}
		// aurora add zhouxiaobing 20131010 end	
		  if(isNeedUpdateWaitingInfoTime()) {
				if (mWaitingCallTime != null) {
					mWaitingCallTime.setText(AuroraPhoneUtils.getTimeString(timeElapsed));
				}
		  }
		
    }

    /**
     * Updates the "on hold" box in the "other call" info area
     * (ie. the stuff in the secondaryCallInfo block)
     * based on the specified Call.
     * Or, clear out the "on hold" box if the specified call
     * is null or idle.
     */
    private void displaySecondaryCallStatus(CallManager cm, Call call) {
        if (DBG) log("displayOnHoldCallStatus(call =" + call + ")...");

        if ((call == null || call.getState() == Call.State.IDLE) 
        		&& PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())
        		&& cm.hasActiveFgCall()) {
        	
            if (DBG) log("displayOnHoldCallStatus(call =1111111");
        	showSecondaryCallInfo();


      	   if (PhoneUtils.isConferenceCall(cm.getActiveFgCall())) {
               if (DBG) log("==> conference call.");
               mFirstCallName.setText(getContext().getString(R.string.confCall));
               showImage(mFirstCallPhoto, R.drawable.picture_conference);
           } else {       
	            //aurora add zhouxiaobing 20140411 start
            	new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(cm.getActiveFgCall()));
	            PhoneUtils.CallerInfoToken infoToken2 = PhoneUtils.startGetCallerInfo(
	                    getContext(), cm.getActiveFgCall(), this, mFirstCallName);
	            mFirstCallName.setText(
	                    PhoneUtils.getCompactNameFromCallerInfo(infoToken2.currentInfo,
	                                                            getContext()));
	            if (infoToken2.isFinal) {
	            	if (DBG) log(" displaySecondaryCallStatus 1");
	             // if(mSecondaryCallPhoto.getDrawable()==null)	
	                showCachedImage(mFirstCallPhoto, infoToken2.currentInfo);
	            } else {
	            	if (DBG) log(" displaySecondaryCallStatus 2");
	                showImage(mFirstCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
	            }
	           //aurora add zhouxiaobing 20140411 end
           }
        	
        	
            mSecondaryCallInfo.setVisibility(View.GONE);
            return;
        } else if ((call == null) || (PhoneGlobals.getInstance().isOtaCallInActiveState())) {
        	//aurora add zhouxiaobing 20131008
        	mFirstCallInfo.setVisibility(View.GONE);
            mSecondaryCallInfo.setVisibility(View.GONE);
            return;
        }

        Phone phone = call.getPhone();
        Call.State state = call.getState();
        switch (state) {
            case HOLDING:
                // Ok, there actually is a background call on hold.
                // Display the "on hold" box.

                // Note this case occurs only on GSM devices.  (On CDMA,
                // the "call on hold" is actually the 2nd connection of
                // that ACTIVE call; see the ACTIVE case below.)
                showSecondaryCallInfo();

                if (PhoneUtils.isConferenceCall(call)) {
                    if (DBG) log("==> conference call.");
                    mSecondaryCallName.setText(getContext().getString(R.string.confCall));
                    showImage(mSecondaryCallPhoto, R.drawable.picture_conference);
                } else {
                    // perform query and update the name temporarily
                    // make sure we hand the textview we want updated to the
                    // callback function.
                    if (DBG) log("==> NOT a conf call; call startGetCallerInfo...");
                //aurora add zhouxiaobing 20140411 start             
                	new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(call));
                //aurora add zhouxiaobing 20140411 end        
                    PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                            getContext(), call, this, mSecondaryCallName);
                    mSecondaryCallName.setText(
                            PhoneUtils.getCompactNameFromCallerInfo(infoToken.currentInfo,
                                                                    getContext()));

                    // Also pull the photo out of the current CallerInfo.
                    // (Note we assume we already have a valid photo at
                    // this point, since *presumably* the caller-id query
                    // was already run at some point *before* this call
                    // got put on hold.  If there's no cached photo, just
                    // fall back to the default "unknown" image.)
                    if (infoToken.isFinal) {
                    	if (DBG) log(" displaySecondaryCallStatus mSecondaryCallPhoto 1="+infoToken.currentInfo.cachedPhoto);
                    	// if(mSecondaryCallPhoto.getDrawable()==null)	
						if (secondphoto != infoToken.currentInfo.cachedPhoto
								|| (secondphoto == null && infoToken.currentInfo.cachedPhoto == null)) {
							secondphoto = infoToken.currentInfo.cachedPhoto;
							showCachedImage(mSecondaryCallPhoto, infoToken.currentInfo);
						}
                    } else {
                    	if (DBG) log(" displaySecondaryCallStatus 2");
                        showImage(mSecondaryCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
                    }
                    
                   if(cm.hasActiveFgCall()) {
                	   if (PhoneUtils.isConferenceCall(cm.getActiveFgCall())) {
                           if (DBG) log("==> conference call.");
                           mFirstCallName.setText(getContext().getString(R.string.confCall));
                           showImage(mFirstCallPhoto, R.drawable.picture_conference);
                       } else {                	   
		                    //aurora add zhouxiaobing 20140411 start		      
		                 	new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(cm.getActiveFgCall()));
		                    PhoneUtils.CallerInfoToken infoToken2 = PhoneUtils.startGetCallerInfo(
		                            getContext(), cm.getActiveFgCall(), this, mFirstCallName);
		                    mFirstCallName.setText(
		                            PhoneUtils.getCompactNameFromCallerInfo(infoToken2.currentInfo,
		                                                                    getContext()));
		
		                    // Also pull the photo out of the current CallerInfo.
		                    // (Note we assume we already have a valid photo at
		                    // this point, since *presumably* the caller-id query
		                    // was already run at some point *before* this call
		                    // got put on hold.  If there's no cached photo, just
		                    // fall back to the default "unknown" image.)
		                    if (infoToken2.isFinal) {
		                    	if (DBG) log(" displaySecondaryCallStatus 1");
		                     // if(mSecondaryCallPhoto.getDrawable()==null)	
		                        showCachedImage(mFirstCallPhoto, infoToken2.currentInfo);
		                    } else {
		                    	if (DBG) log(" displaySecondaryCallStatus 2");
		                        showImage(mFirstCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
		                    }
		                      //aurora add zhouxiaobing 20140411 end	
                       }
                   } else {
                	   mFirstCallInfo.setVisibility(View.GONE);
                   }
                }

 //               AnimationUtils.Fade.show(mSecondaryCallPhotoDimEffect);
                break;

            case ACTIVE:
                // CDMA: This is because in CDMA when the user originates the second call,
                // although the Foreground call state is still ACTIVE in reality the network
                // put the first call on hold.
                if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                    showSecondaryCallInfo();

                    List<Connection> connections = call.getConnections();
                    if (connections.size() > 2) {
                        // This means that current Mobile Originated call is the not the first 3-Way
                        // call the user is making, which in turn tells the PhoneGlobals that we no
                        // longer know which previous caller/party had dropped out before the user
                        // made this call.
                        mSecondaryCallName.setText(
                                getContext().getString(R.string.card_title_in_call));
                        showImage(mSecondaryCallPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                    } else {
                        // This means that the current Mobile Originated call IS the first 3-Way
                        // and hence we display the first callers/party's info here.
                        Connection conn = call.getEarliestConnection();
                        PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                                getContext(), conn, this, mSecondaryCallName);

                        // Get the compactName to be displayed, but then check that against
                        // the number presentation value for the call. If it's not an allowed
                        // presentation, then display the appropriate presentation string instead.
                        AuroraCallerInfo info = infoToken.currentInfo;

                        String name = PhoneUtils.getCompactNameFromCallerInfo(info, getContext());
                        boolean forceGenericPhoto = false;
                        if (info != null && info.numberPresentation !=
                                PhoneConstants.PRESENTATION_ALLOWED) {
                            name = PhoneUtils.getPresentationString(
                                    getContext(), info.numberPresentation);
                            forceGenericPhoto = true;
                        }
                        mSecondaryCallName.setText(name);

                        // Also pull the photo out of the current CallerInfo.
                        // (Note we assume we already have a valid photo at
                        // this point, since *presumably* the caller-id query
                        // was already run at some point *before* this call
                        // got put on hold.  If there's no cached photo, just
                        // fall back to the default "unknown" image.)
                        if (!forceGenericPhoto && infoToken.isFinal) {
                            showCachedImage(mSecondaryCallPhoto, info);
                        } else {
                            showImage(mSecondaryCallPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                        }
                    }
                } else {
                    // We shouldn't ever get here at all for non-CDMA devices.
                    Log.w(LOG_TAG, "displayOnHoldCallStatus: ACTIVE state on non-CDMA device");
                    mFirstCallInfo.setVisibility(View.GONE);//aurora add zhouxiaobing 20131008
                    mSecondaryCallInfo.setVisibility(View.GONE);
                }

//                AnimationUtils.Fade.hide(mSecondaryCallPhotoDimEffect, View.GONE);
                break;

            default:
                // There's actually no call on hold.  (Presumably this call's
                // state is IDLE, since any other state is meaningless for the
                // background call.)
                mSecondaryCallInfo.setVisibility(View.GONE);
                mFirstCallInfo.setVisibility(View.GONE);//aurora add zhouxiaobing 20131008
                break;
        }
    }

    private void showSecondaryCallInfo() {
        // This will call ViewStub#inflate() when needed.
        mSecondaryCallInfo.setVisibility(View.VISIBLE);
        if (mSecondaryCallName == null) {
            mSecondaryCallName = (TextView) findViewById(R.id.secondaryCallName);
        }
        if (mSecondaryCallPhoto == null) {
            mSecondaryCallPhoto = (ImageView) findViewById(R.id.secondaryCallPhoto);
        }
        if (mSecondCallTime == null) {
        	mSecondCallTime = (TextView) findViewById(R.id.secondaryCallTime);
        	mSecondCallTime.setTypeface(mRobotoLightTf);      
        }
        
        if (mSecondaryCallPhotoDimEffect == null) {
            mSecondaryCallPhotoDimEffect = findViewById(R.id.dim_effect_for_secondary_photo);
            mSecondaryCallPhotoDimEffect.setOnClickListener(mInCallScreen);
            // Add a custom OnTouchListener to manually shrink the "hit target".
//            mSecondaryCallPhotoDimEffect.setOnTouchListener(new SmallerHitTargetTouchListener());
        }
        auroraUpdateFirstInfo();
        mInCallScreen.updateButtonStateOutsideInCallTouchUi();
        
        updateCallerInfoPosition();
    }

    /**
     * Method which is expected to be called from
     * {@link InCallScreen#updateButtonStateOutsideInCallTouchUi()}.
     */
    /* package */ void setSecondaryCallClickable(boolean clickable) {
  //      if (mSecondaryCallPhotoDimEffect != null) {
   //         mSecondaryCallPhotoDimEffect.setEnabled(clickable);
  //      }
    }

    private String getCallFailedString(Call call) {
        Connection c = call.getEarliestConnection();
        int resID;

        if (c == null) {
            if (DBG) log("getCallFailedString: connection is null, using default values.");
            // if this connection is null, just assume that the
            // default case occurs.
            resID = R.string.card_title_call_ended;
        } else {

        	AuroraDisconnectCause cause = new AuroraDisconnectCause(c);

            // TODO: The card *title* should probably be "Call ended" in all
            // cases, but if the DisconnectCause was an error condition we should
            // probably also display the specific failure reason somewhere...
            if(cause.mCause == AuroraDisconnectCause.BUSY) {
                resID = R.string.callFailed_userBusy;
            } else if(cause.mCause == AuroraDisconnectCause.CONGESTION) {
                resID = R.string.callFailed_congestion;
            } else if(cause.mCause == AuroraDisconnectCause.TIMED_OUT) {
                resID = R.string.callFailed_timedOut;
            } else if(cause.mCause == AuroraDisconnectCause.SERVER_UNREACHABLE) {
                resID = R.string.callFailed_server_unreachable;
            } else if(cause.mCause == AuroraDisconnectCause.NUMBER_UNREACHABLE) {
                resID = R.string.callFailed_number_unreachable;
            } else if(cause.mCause == AuroraDisconnectCause.INVALID_CREDENTIALS) {
                resID = R.string.callFailed_invalid_credentials;
            } else if(cause.mCause == AuroraDisconnectCause.SERVER_ERROR) {
                resID = R.string.callFailed_server_error;
            } else if(cause.mCause == AuroraDisconnectCause.OUT_OF_NETWORK) {
                resID = R.string.callFailed_out_of_network;
            } else if(cause.mCause == AuroraDisconnectCause.LOST_SIGNAL || cause.mCause == AuroraDisconnectCause.CDMA_DROP) {
                resID = R.string.callFailed_noSignal;
            } else if(cause.mCause == AuroraDisconnectCause.LIMIT_EXCEEDED) {
                resID = R.string.callFailed_limitExceeded;
            } else if(cause.mCause == AuroraDisconnectCause.POWER_OFF) {
                resID = R.string.callFailed_powerOff;
            } else if(cause.mCause == AuroraDisconnectCause.ICC_ERROR) {
            	   resID = R.string.callFailed_simError;
            } else if(cause.mCause == AuroraDisconnectCause.OUT_OF_SERVICE) {
                resID = R.string.callFailed_outOfService;
            } else if(cause.mCause == AuroraDisconnectCause.INVALID_NUMBER || cause.mCause == AuroraDisconnectCause.UNOBTAINABLE_NUMBER) {
                resID = R.string.callFailed_unobtainable_number;
            } else {
                resID = R.string.card_title_call_ended;
            }
        }
        return getContext().getString(resID);
    }

    /**
     * Updates the name / photo / number / label fields on the CallCard
     * based on the specified CallerInfo.
     *
     * If the current call is a conference call, use
     * updateDisplayForConference() instead.
     */
    private void updateDisplayForPerson(AuroraCallerInfo info,
                                        int presentation,
                                        boolean isTemporary,
                                        Call call,
                                        Connection conn) {
        if (DBG) log("updateDisplayForPerson(" + info + ")\npresentation:" +
                     presentation + " isTemporary:" + isTemporary);

        // inform the state machine that we are displaying a photo.
        mPhotoTracker.setPhotoRequest(info);
        mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);

        // The actual strings we're going to display onscreen:
        boolean displayNameIsNumber = false;
        String displayName;
        String displayNumber = null;
        String label = null;
        Uri personUri = null;
        // String socialStatusText = null;
        // Drawable socialStatusBadge = null;

        // Gather missing info unless the call is generic, in which case we wouldn't use
        // the gathered information anyway.
        if (info != null && !call.isGeneric()) {

            // It appears that there is a small change in behaviour with the
            // PhoneUtils' startGetCallerInfo whereby if we query with an
            // empty number, we will get a valid CallerInfo object, but with
            // fields that are all null, and the isTemporary boolean input
            // parameter as true.

            // In the past, we would see a NULL callerinfo object, but this
            // ends up causing null pointer exceptions elsewhere down the
            // line in other cases, so we need to make this fix instead. It
            // appears that this was the ONLY call to PhoneUtils
            // .getCallerInfo() that relied on a NULL CallerInfo to indicate
            // an unknown contact.

            // Currently, info.phoneNumber may actually be a SIP address, and
            // if so, it might sometimes include the "sip:" prefix.  That
            // prefix isn't really useful to the user, though, so strip it off
            // if present.  (For any other URI scheme, though, leave the
            // prefix alone.)
            // TODO: It would be cleaner for CallerInfo to explicitly support
            // SIP addresses instead of overloading the "phoneNumber" field.
            // Then we could remove this hack, and instead ask the CallerInfo
            // for a "user visible" form of the SIP address.
            String number = info.phoneNumber;
            if ((number != null) && number.startsWith("sip:")) {
                number = number.substring(4);
            }
            
        	if(!TextUtils.isEmpty(number)) {
        		number = number.replaceAll("-", "");
        	}

            if (TextUtils.isEmpty(info.name)) {
                // No valid "name" in the CallerInfo, so fall back to
                // something else.
                // (Typically, we promote the phone number up to the "name" slot
                // onscreen, and possibly display a descriptive string in the
                // "number" slot.)
                if (TextUtils.isEmpty(number)) {
                    // No name *or* number!  Display a generic "unknown" string
                    // (or potentially some other default based on the presentation.)
                    displayName = PhoneUtils.getPresentationString(getContext(), presentation);
                    if (DBG) log("  ==> no name *or* number! displayName = " + displayName);
                } else if (presentation != PhoneConstants.PRESENTATION_ALLOWED && !AuroraPhoneUtils.isSimulate()) {
                    // This case should never happen since the network should never send a phone #
                    // AND a restricted presentation. However we leave it here in case of weird
                    // network behavior
                    displayName = PhoneUtils.getPresentationString(getContext(), presentation);
                    if (DBG) log("  ==> presentation not allowed! displayName = " + displayName);
                } else if (!TextUtils.isEmpty(info.cnapName)) {
                    // No name, but we do have a valid CNAP name, so use that.
                    displayName = info.cnapName;
                    info.name = info.cnapName;
                    displayNumber = number;
                    if (DBG) log("  ==> cnapName available: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                } else {
                    // No name; all we have is a number.  This is the typical
                    // case when an incoming call doesn't match any contact,
                    // or if you manually dial an outgoing number using the
                    // dialpad.

                    // Promote the phone number up to the "name" slot:
//                    displayName = number;               
                    displayName = AuroraPhoneUtils.formatNumber(number);
                    if(TextUtils.isEmpty(displayName)) {
                    	displayName = number;
                    }
                    displayNameIsNumber = true;

                    // ...and use the "number" slot for a geographical description
                    // string if available (but only for incoming calls.)
                    if ((conn != null) && (conn.isIncoming())) {
                        // TODO (CallerInfoAsyncQuery cleanup): Fix the CallerInfo
                        // query to only do the geoDescription lookup in the first
                        // place for incoming calls.
                        displayNumber = null;//info.geoDescription;  // may be null//aurora change zhouxiaobing 20131028 for bug245
                    }

                    if (DBG) log("  ==>  no name; falling back to number: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                }
            } else {
                // We do have a valid "name" in the CallerInfo.  Display that
                // in the "name" slot, and the phone number in the "number" slot.
                if (presentation != PhoneConstants.PRESENTATION_ALLOWED && !AuroraPhoneUtils.isSimulate()) {
                    // This case should never happen since the network should never send a name
                    // AND a restricted presentation. However we leave it here in case of weird
                    // network behavior
                    displayName = PhoneUtils.getPresentationString(getContext(), presentation);
                    if (DBG) log("  ==> valid name, but presentation not allowed!"
                                 + " displayName = " + displayName);
                } else {
                    displayName = info.name;
                    displayNumber = number;
                    label = info.phoneLabel;
                    if (DBG) log("  ==>  name is present in CallerInfo: displayName '"
                                 + displayName + "', displayNumber '" + displayNumber + "'");
                }
            }
            personUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, info.person_id);
            if (DBG) log("- got personUri: '" + personUri
                         + "', based on info.person_id: " + info.person_id);
            if(info.person_id > 0){
                mContacts = "ContactsId"+info.person_id;
            } else if((TextUtils.isEmpty(number) || info.isEmergencyNumber() || info.isVoiceMailNumber()) == false){
                mContacts = "ContactsNumber"+number.replaceAll("-", "");
            } else{
                mContacts = getContext().getString(R.string.unknown);
            }
        } else {
            displayName = PhoneUtils.getPresentationString(getContext(), presentation);
        }

        if (call.isGeneric()) {
            updateGenericInfoUi();
        } else {
            updateInfoUi(displayName, displayNumber, label, displayNameIsNumber);
        }
        mNameText = mName.getText().toString();
        if(conn != null) {
        	mNumberStr = conn.getAddress();
        }
        if(mNumberStr != null) {
        	mNumberStr.replaceAll(" ", "");
        }

        // Update mPhoto
        // if the temporary flag is set, we know we'll be getting another call after
        // the CallerInfo has been correctly updated.  So, we can skip the image
        // loading until then.

        // If the photoResource is filled in for the CallerInfo, (like with the
        // Emergency Number case), then we can just set the photo image without
        // requesting for an image load. Please refer to CallerInfoAsyncQuery.java
        // for cases where CallerInfo.photoResource may be set.  We can also avoid
        // the image load step if the image data is cached.
        if (isTemporary && (info == null || !info.isCachedPhotoCurrent)) {
            mPhoto.setTag(null);
//            if(!mAnimController.getIncoming_to_incall_anim_ex()){
            	mPhoto.setVisibility(View.INVISIBLE);
//            }
        } else if (info != null && info.photoResource != 0){
            showImage(mPhoto, info.photoResource);
        } else if(info != null && info.isCachedPhotoCurrent && info.cachedPhoto != null) {
            if(call.getState() != Call.State.DISCONNECTING && call.getState() != Call.State.DISCONNECTED){
                setPhotoVisible();
            	showCachedImage(mPhoto, info);
            }
        } else if (!showCachedImage(mPhoto, info)) {
            if(call.getState()==Call.State.DISCONNECTING || call.getState()==Call.State.DISCONNECTED){
        	} else if (personUri == null) {
                Log.w(LOG_TAG, "personPri is null. Just use Unknown picture.");
                showImage(mPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
            } else if (personUri.equals(mLoadingPersonUri)) {
                if (DBG) {
                    log("The requested Uri (" + personUri + ") is being loaded already."
                            + " Ignoret the duplicate load request.");
                }
            } else {
                // Remember which person's photo is being loaded right now so that we won't issue
                // unnecessary load request multiple times, which will mess up animation around
                // the contact photo.
                mLoadingPersonUri = personUri;

                // Forget the drawable previously used.
                mPhoto.setTag(null);
                // Show empty screen for a moment.
//                if(!mAnimController.getIncoming_to_incall_anim_ex()){
                	mPhoto.setVisibility(View.INVISIBLE);
//                }
                // Load the image with a callback to update the image state.
                // When the load is finished, onImageLoadComplete() will be called.
                mHandler.removeMessages(MESSAGE_SHOW_UNKNOWN_PHOTO);
                mHandler.sendEmptyMessageDelayed(MESSAGE_SHOW_UNKNOWN_PHOTO, MESSAGE_DELAY);
                ContactsAsyncHelper.startObtainPhotoAsync(TOKEN_UPDATE_PHOTO_FOR_CALL_STATE,
                        getContext(), personUri, this, new AsyncLoadCookie(mPhoto, info, call));

                // If the image load is too slow, we show a default avatar icon afterward.
                // If it is fast enough, this message will be canceled on onImageLoadComplete().
            }
        }

        // If the phone call is on hold, show it with darker status.
        // Right now we achieve it by overlaying opaque View.
        // Note: See also layout file about why so and what is the other possibilities.
/*        if (call.getState() == Call.State.HOLDING) {
            AnimationUtils.Fade.show(mPhotoDimEffect);
        } else {
            AnimationUtils.Fade.hide(mPhotoDimEffect, View.GONE);
        }
*///aurora change zhouxiaobing 20131015
        // Other text fields:
        updateCallTypeLabel(call);
        // updateSocialStatus(socialStatusText, socialStatusBadge, call);  // Currently unused
    }

    /**
     * Updates the info portion of the UI to be generic.  Used for CDMA 3-way calls.
     */
    private void updateGenericInfoUi() {
        mName.setText(R.string.card_title_in_call);
        mPhoneNumber.setVisibility(View.GONE);
        mNumberSeparator.setVisibility(View.GONE);
        mLabel.setVisibility(View.GONE);
    }

    /**
     * Updates the info portion of the call card with passed in values.
     */
    private void updateInfoUi(String displayName, String displayNumber, String label, boolean isShowNumberReplaceName) {
        mName.setText(displayName);
        mName.setVisibility(View.VISIBLE);
        if(isShowNumberReplaceName) {
        	mName.setTextSize(28);
        } else {
        	mName.setTextSize(26);
        }

        if (TextUtils.isEmpty(displayNumber)) {
            mPhoneNumber.setVisibility(View.GONE);
            mNumberSeparator.setVisibility(View.GONE);
            // We have a real phone number as "mName" so make it always LTR
            mName.setTextDirection(View.TEXT_DIRECTION_LTR);
        } else {
            mPhoneNumber.setText(displayNumber);
            mPhoneNumber.setVisibility(View.VISIBLE);
          	if(AuroraPhoneUtils.isShowDoubleButton() || mAdrress.getVisibility() == View.VISIBLE) {
          		mNumberSeparator.setVisibility(View.VISIBLE);
          	} else {
                mNumberSeparator.setVisibility(View.GONE);
          	}
            // We have a real phone number as "mPhoneNumber" so make it always LTR
            mPhoneNumber.setTextDirection(View.TEXT_DIRECTION_LTR);
        }

        if (TextUtils.isEmpty(label)) {
            mLabel.setVisibility(View.GONE);
        } else {
            mLabel.setText(label);
            //mLabel.setVisibility(View.VISIBLE);//aurora change zhouxiaobing 20140605
        }
    }

    /**
     * Updates the name / photo / number / label fields
     * for the special "conference call" state.
     *
     * If the current call has only a single connection, use
     * updateDisplayForPerson() instead.
     */
    private void updateDisplayForConference(Call call) {
        if (DBG) log("updateDisplayForConference()...");

        int phoneType = call.getPhone().getPhoneType();
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            // This state corresponds to both 3-Way merged call and
            // Call Waiting accepted call.
            // In this case we display the UI in a "generic" state, with
            // the generic "dialing" icon and no caller information,
            // because in this state in CDMA the user does not really know
            // which caller party he is talking to.
//            showImage(mPhoto, R.drawable.picture_dialing);
        	  showImage(mPhoto, R.drawable.picture_conference);
            mName.setText(R.string.card_title_in_call);
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
                || (phoneType == PhoneConstants.PHONE_TYPE_SIP)
                || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
            // Normal GSM (or possibly SIP?) conference call.
            // Display the "conference call" image as the contact photo.
            // TODO: Better visual treatment for contact photos in a
            // conference call (see bug 1313252).
            showImage(mPhoto, R.drawable.picture_conference);
            mName.setText(R.string.card_title_conf_call);
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }

        mName.setVisibility(View.VISIBLE);
        mNameText = mName.getText().toString();

        // TODO: For a conference call, the "phone number" slot is specced
        // to contain a summary of who's on the call, like "Bill Foldes
        // and Hazel Nutt" or "Bill Foldes and 2 others".
        // But for now, just hide it:
        mPhoneNumber.setVisibility(View.GONE);
        mNumberSeparator.setVisibility(View.GONE);
        mLabel.setVisibility(View.GONE);

        // Other text fields:
        updateCallTypeLabel(call);
        // updateSocialStatus(null, null, null);  // socialStatus is never visible in this state

        // TODO: for a GSM conference call, since we do actually know who
        // you're talking to, consider also showing names / numbers /
        // photos of some of the people on the conference here, so you can
        // see that info without having to click "Manage conference".  We
        // probably have enough space to show info for 2 people, at least.
        //
        // To do this, our caller would pass us the activeConnections
        // list, and we'd call PhoneUtils.getCallerInfo() separately for
        // each connection.
    }

    /**
     * Updates the CallCard "photo" IFF the specified Call is in a state
     * that needs a special photo (like "busy" or "dialing".)
     *
     * If the current call does not require a special image in the "photo"
     * slot onscreen, don't do anything, since presumably the photo image
     * has already been set (to the photo of the person we're talking, or
     * the generic "picture_unknown" image, or the "conference call"
     * image.)
     */
    private void updatePhotoForCallState(Call call) {
        if (DBG) log("updatePhotoForCallState(" + call + ")...");
        int photoImageResource = 0;

        // Check for the (relatively few) telephony states that need a
        // special image in the "photo" slot.
        Call.State state = call.getState();
        switch (state) {
            case DISCONNECTED:
                // Display the special "busy" photo for BUSY or CONGESTION.
                // Otherwise (presumably the normal "call ended" state)
                // leave the photo alone.
                Connection c = call.getEarliestConnection();
                // if the connection is null, we assume the default case,
                // otherwise update the image resource normally.
                if (c != null) {
                    AuroraDisconnectCause cause = new AuroraDisconnectCause(c);
                    if ((cause.mCause == AuroraDisconnectCause.BUSY)
                        || (cause.mCause == AuroraDisconnectCause.CONGESTION)) {
                        photoImageResource = R.drawable.picture_busy;
                    }
                } else if (DBG) {
                    log("updatePhotoForCallState: connection is null, ignoring.");
                }

                // TODO: add special images for any other DisconnectCauses?
                break;
            case ACTIVE:            
            case HOLDING:	
                //aurora modify liguangyu 20140730 for BUG #7013 start
               	if(PhoneGlobals.getInstance().mCM.getFgPhone().getPhoneType() != PhoneConstants.PHONE_TYPE_CDMA
              		|| !PhoneGlobals.getInstance().cdmaPhoneCallState.getSiggleDialingState()) {
//               	  if(!mAnimController.getIncoming_to_incall_anim_ex()){
//	            	mPhoto.setVisibility(View.INVISIBLE);//aurora add zhouxiaobing 20131009
//	            	guangquan.setVisibility(View.INVISIBLE);
//               	  }
               	}
                //aurora modify liguangyu 20140730 for BUG #7013 end
            	break;
            case DISCONNECTING:	
            	break;
            case ALERTING:
            case DIALING:
            default:
                // Leave the photo alone in all other states.
                // If this call is an individual call, and the image is currently
                // displaying a state, (rather than a photo), we'll need to update
                // the image.
                // This is for the case where we've been displaying the state and
                // now we need to restore the photo.  This can happen because we
                // only query the CallerInfo once, and limit the number of times
                // the image is loaded. (So a state image may overwrite the photo
                // and we would otherwise have no way of displaying the photo when
                // the state goes away.)

                // if the photoResource field is filled-in in the Connection's
                // caller info, then we can just use that instead of requesting
                // for a photo load.

                // look for the photoResource if it is available.
                if (DBG) log("- overrriding photo image: 1");
                //aurora delete liguangyu 20131206 start
               if(state.isDialing()) {
            	//aurora add zhouxiaobing 20140311                
            	   setPhotoVisible();
               }
//               if(state.isRinging() && DeviceUtils.is7503()) {
               if(state.isRinging()) {
            	   setPhotoVisible();
               }
                //aurora delete liguangyu 20131206 end
                AuroraCallerInfo ci = null;
                {
                    Connection conn = null;
                    int phoneType = call.getPhone().getPhoneType();
                    if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
                        conn = call.getLatestConnection();
                    } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
                            || (phoneType == PhoneConstants.PHONE_TYPE_SIP)
                            || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
                        conn = call.getEarliestConnection();
                    } else {
                        throw new IllegalStateException("Unexpected phone type: " + phoneType);
                    }

                    if (conn != null) {
                        Object o = conn.getUserData();
                        if (o instanceof AuroraCallerInfo) {
                            ci = (AuroraCallerInfo) o;
                        } else if (o instanceof PhoneUtils.CallerInfoToken) {
                            ci = ((PhoneUtils.CallerInfoToken) o).currentInfo;
                        }
                    }
                }

                if (ci != null) {
                    photoImageResource = ci.photoResource;
                }

                // If no photoResource found, check to see if this is a conference call. If
                // it is not a conference call:
                //   1. Try to show the cached image
                //   2. If the image is not cached, check to see if a load request has been
                //      made already.
                //   3. If the load request has not been made [DISPLAY_DEFAULT], start the
                //      request and note that it has started by updating photo state with
                //      [DISPLAY_IMAGE].
                if (photoImageResource == 0) {
                    if (!PhoneUtils.isConferenceCall(call)) {
                        if (!showCachedImage(mPhoto, ci) && (mPhotoTracker.getPhotoState() ==
                                ContactsAsyncHelper.ImageTracker.DISPLAY_DEFAULT)) {
                            Uri photoUri = mPhotoTracker.getPhotoUri();
                            if (photoUri == null) {
                                Log.w(LOG_TAG, "photoUri became null. Show default avatar icon");
                                showImage(mPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                            } else {
                                if (DBG) {
                                    log("start asynchronous load inside updatePhotoForCallState()");
                                }
                                mPhoto.setTag(null);
                                // Make it invisible for a moment
//                                if(!mAnimController.getIncoming_to_incall_anim_ex()){
                                	mPhoto.setVisibility(View.INVISIBLE);
//                                }
                                ContactsAsyncHelper.startObtainPhotoAsync(TOKEN_DO_NOTHING,
                                        getContext(), photoUri, this,
                                        new AsyncLoadCookie(mPhoto, ci, null));
                            }
                            mPhotoTracker.setPhotoState(
                                    ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
                        }
                    }
                } else {
                    showImage(mPhoto, photoImageResource);
                    mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_IMAGE);
                    return;
                }
                break;
        }

        if (photoImageResource != 0) {
            if (DBG) log("- overrriding photo image: " + photoImageResource);
            showImage(mPhoto, photoImageResource);
            // Track the image state.
            mPhotoTracker.setPhotoState(ContactsAsyncHelper.ImageTracker.DISPLAY_DEFAULT);
        }
    }

    /**
     * Try to display the cached image from the callerinfo object.
     *
     *  @return true if we were able to find the image in the cache, false otherwise.
     */
    private final boolean showCachedImage(ImageView view, AuroraCallerInfo ci) {
    	
		log(" showCachedImage start");
    	
		if (view.getVisibility() != View.VISIBLE) {
		    log(" showCachedImage return");
			return false;
		}
    	
        if (ci instanceof AuroraCallerInfo) {
            if(((AuroraCallerInfo)ci).isPrivate()) {
            	 showImage(view, mdefault_photo_id);
            	 return true;
            }
        }
    	
       
        if ((ci != null) && ci.isCachedPhotoCurrent) {
            if (ci.cachedPhoto != null) {
            	if (DBG) log(" showCachedImage 1");
            	if(view == mPhoto) {
	            	if(ci.isUseYoloreImage) {
	                 	mPhotoCover.setVisibility(View.VISIBLE);
	            	} else if( ci.person_id > 0) {
	                  	mPhotoCover.setVisibility(View.GONE);
	            	}
            	}
                showImage(view, ci.cachedPhoto);
            } else {
            	if (DBG) log(" showCachedImage 2");
                showImage(view, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
            }
            return true;
        }
        return false;
    }

    /** Helper function to display the resource in the imageview AND ensure its visibility.*/
    private final void showImage(ImageView view, int resource) {
        try {
        	showImage(view, view.getContext().getResources().getDrawable(resource));
        	for (int r : largeRandomContactPhotoId) {
        		if(r == resource) {
        		    log(" showCachedImage add cover");
        		  	if(view == mPhoto) {
        		  		mPhotoCover.setVisibility(View.VISIBLE);
        		  	}
        			break;
        		} 
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private final void showImage(ImageView view, Bitmap bitmap) {
        showImage(view, new BitmapDrawable(view.getContext().getResources(), bitmap));
    }

    /**
     * Helper function to display the drawable in the imageview AND ensure its
     * visibility. The InCallContactPhoto and VideoCallPanel are mutually
     * exclusive. Show InCallContactPhoto view only if VideoCallPanel is not
     * visible.
     */
	private final void showImage(ImageView view, Drawable drawable) {
		log(" showImage start");
		if (view.getVisibility() != View.VISIBLE) {
			if (DBG)
				log(" showImage return 1");
			return;
		}

		Call fgCall = PhoneGlobals.getInstance().mCM.getActiveFgCall();
		if (fgCall.getState() == Call.State.DISCONNECTING
				|| fgCall.getState() == Call.State.DISCONNECTED) {
			if (DBG)
				log(" showImage return 2");
			return;
		}
//		BitmapDrawable bd = (BitmapDrawable) drawable;
//		Bitmap bitmap1 = bd.getBitmap();
		Bitmap bitmap1 = RoundBitmapUtils.drawable2bitmap(drawable);

		if(view != mPhoto) {
			Bitmap bitmap2 = RoundBitmapUtils.toRoundBitmap(bitmap1);
			drawable = new BitmapDrawable(view.getContext().getResources(), bitmap2);
		} else {
			drawable = new BitmapDrawable(view.getContext().getResources(), bitmap1);
		}
		// aurora zhouxiaobing add 20131111 end
		Resources res = view.getContext().getResources();
		Drawable current = (Drawable) view.getTag();

		if (current == null) {
			if (DBG)
				log("Start fade-in animation for " + view);
			view.setImageDrawable(drawable);
			// aurora delete liguangyu 20131206 start
			// AnimationUtils.Fade.show(view);
			// aurora delete liguangyu 20131206 end
			view.setTag(drawable);
		} else {
			AnimationUtils.startCrossFade(view, current, drawable);
			view.setVisibility(View.VISIBLE);
		}
		
	
	}

    /**
     * Returns the special card title used in emergency callback mode (ECM),
     * which shows your own phone number.
     */
    private String getECMCardTitle(Context context, Phone phone) {
        String rawNumber = phone.getLine1Number();  // may be null or empty
        String formattedNumber;
        if (!TextUtils.isEmpty(rawNumber)) {
            formattedNumber = PhoneNumberUtils.formatNumber(rawNumber);
        } else {
            formattedNumber = context.getString(R.string.unknown);
        }
        String titleFormat = context.getString(R.string.card_title_my_phone_number);
        return String.format(titleFormat, formattedNumber);
    }

    /**
     * Updates the "Call type" label, based on the current foreground call.
     * This is a special label and/or branding we display for certain
     * kinds of calls.
     *
     * (So far, this is used only for SIP calls, which get an
     * "Internet call" label.  TODO: But eventually, the telephony
     * layer might allow each pluggable "provider" to specify a string
     * and/or icon to be displayed here.)
     */
    private void updateCallTypeLabel(Call call) {
        int phoneType = (call != null) ? call.getPhone().getPhoneType() :
                PhoneConstants.PHONE_TYPE_NONE;
        if (phoneType == PhoneConstants.PHONE_TYPE_SIP) {
            mCallTypeLabel.setVisibility(View.VISIBLE);
            mCallTypeLabel.setText(R.string.incall_call_type_label_sip);
            mCallTypeLabel.setTextColor(mTextColorCallTypeSip);
            // If desired, we could also display a "badge" next to the label, as follows:
            //   mCallTypeLabel.setCompoundDrawablesWithIntrinsicBounds(
            //           callTypeSpecificBadge, null, null, null);
            //   mCallTypeLabel.setCompoundDrawablePadding((int) (mDensity * 6));
        } else {
            mCallTypeLabel.setVisibility(View.GONE);
        }
    }

    /**
     * Updates the "social status" label with the specified text and
     * (optional) badge.
     */
    /*private void updateSocialStatus(String socialStatusText,
                                    Drawable socialStatusBadge,
                                    Call call) {
        // The socialStatus field is *only* visible while an incoming call
        // is ringing, never in any other call state.
        if ((socialStatusText != null)
                && (call != null)
                && call.isRinging()
                && !call.isGeneric()) {
            mSocialStatus.setVisibility(View.VISIBLE);
            mSocialStatus.setText(socialStatusText);
            mSocialStatus.setCompoundDrawablesWithIntrinsicBounds(
                    socialStatusBadge, null, null, null);
            mSocialStatus.setCompoundDrawablePadding((int) (mDensity * 6));
        } else {
            mSocialStatus.setVisibility(View.GONE);
        }
    }*/

    /**
     * Hides the top-level UI elements of the call card:  The "main
     * call card" element representing the current active or ringing call,
     * and also the info areas for "ongoing" or "on hold" calls in some
     * states.
     *
     * This is intended to be used in special states where the normal
     * in-call UI is totally replaced by some other UI, like OTA mode on a
     * CDMA device.
     *
     * To bring back the regular CallCard UI, just re-run the normal
     * updateState() call sequence.
     */
    public void hideCallCardElements() {
      	setPrimaryCallInfoVisibility(false);
        mSecondaryCallInfo.setVisibility(View.GONE);
    }

    /*
     * Updates the hint (like "Rotate to answer") that we display while
     * the user is dragging the incoming call RotarySelector widget.
     */
    /* package */ void setIncomingCallWidgetHint(int hintTextResId, int hintColorResId) {
        mIncomingCallWidgetHintTextResId = hintTextResId;
        mIncomingCallWidgetHintColorResId = hintColorResId;
    }

    // Accessibility event support.
    // Since none of the CallCard elements are focusable, we need to manually
    // fill in the AccessibilityEvent here (so that the name / number / etc will
    // get pronounced by a screen reader, for example.)
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            dispatchPopulateAccessibilityEvent(event, mName);
            dispatchPopulateAccessibilityEvent(event, mPhoneNumber);
            return true;
        }

        dispatchPopulateAccessibilityEvent(event, mCallStateLabel);
        dispatchPopulateAccessibilityEvent(event, mPhoto);
        dispatchPopulateAccessibilityEvent(event, mName);
        dispatchPopulateAccessibilityEvent(event, mPhoneNumber);
        dispatchPopulateAccessibilityEvent(event, mLabel);
        // dispatchPopulateAccessibilityEvent(event, mSocialStatus);
        if (mSecondaryCallName != null) {
            dispatchPopulateAccessibilityEvent(event, mSecondaryCallName);
        }
        if (mSecondaryCallPhoto != null) {
            dispatchPopulateAccessibilityEvent(event, mSecondaryCallPhoto);
        }
        return true;
    }

    protected void dispatchPopulateAccessibilityEvent(AccessibilityEvent event, View view) {
        List<CharSequence> eventText = event.getText();
        int size = eventText.size();
        view.dispatchPopulateAccessibilityEvent(event);
        // if no text added write null to keep relative position
        if (size == eventText.size()) {
            eventText.add(null);
        }
    }

    public void clear() {
        // The existing phone design is to keep an instance of call card forever.  Until that
        // design changes, this method is needed to clear (reset) the call card for the next call
        // so old data is not shown.
    	if(PhoneGlobals.getInstance().mCM.getState() != PhoneConstants.State.IDLE) {
    		return;
    	}

        // Other elements can also be cleared here.  Starting with elapsed time to fix a bug.
        mElapsedTime.setVisibility(View.GONE);
        mElapsedTime.setText(null);
        if(mDtmfElapsedTime != null) {
        	mDtmfElapsedTime.setText(null);
        }else {
			getDtmfCallTime() ;
		}
    	
		Resources res = mPhoto.getContext().getResources();
    	Drawable drawable = res.getDrawable(mdefault_photo_id);
//    	BitmapDrawable bd = (BitmapDrawable) drawable;
//		drawable = new BitmapDrawable(res, RoundBitmapUtils.toRoundBitmap(bd.getBitmap()));
	
//		mPhoto.setImageDrawable(drawable);
    	mPhoto.setImageResource(mdefault_photo_id);
		// aurora delete liguangyu 20131206 start
		// AnimationUtils.Fade.show(view);
		// aurora delete liguangyu 20131206 end
		mPhoto.setTag(drawable);
		 
    }


    // Debugging / testing code

    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Log.e(LOG_TAG, msg);
    }
   
    
//aurora add zhouxiaobing 20131231 start
    private View mNoteLine;
	private TextView mNote;
	private TextView mAdrress;

	private ViewStub mFirstCallInfo;
	private TextView mFirstCallName;
	private ImageView mFirstCallPhoto;
	private View mFirstCallPhotoDimEffect;
//	private ImageView meclipstimebg;
	private TextView mfirstcalltime;
	private TextView mfirstcallstate;
	private static int mdefault_photo_id = R.drawable.large_svg_dial_default_photo1;

//	private View guangquan;


	Drawable secondphoto = null;	


	private String mContacts = "";
	private String mNameText = "";
	private String mNumberStr = "";

	public String getContacts() {
		return mContacts;
	}

	public String getNameStr() {
		return mNameText;
	}

	public String getNumberStr() {
		return mNumberStr;
	}


	
	private TextView mSloganNote;
	

	 
	 void OnShoworHideCallCard(boolean is_show){
//		 mAnimController.OnShoworHideCallCard(is_show);
	 }
	
	void RingToSms() {
//		mAnimController.RingToSms();
	}	      
    
    private ViewStub mThirdCallInfo;
    private TextView mThirdCallName;
    private ImageView mThirdCallPhoto; 
    private View mThirdCallPhotoDimEffect;  

    private ViewStub mFourthCallInfo;
    private TextView mFourthCallName;
    private ImageView mFourthCallPhoto; 
    private View mFourthCallPhotoDimEffect;  
     
    
    protected void updateOtherForegroundCall(CallManager cm) {
        if (DBG) log("updateOtherForegroundCall()...");

        if (!PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())) {
        	mThirdCallInfo.setVisibility(View.GONE);
            mFourthCallInfo.setVisibility(View.GONE);
        	return;
        } 

        
        Phone phone = PhoneGlobals.getInstance().getPhone(PhoneUtils.getOtherActiveSub(PhoneUtils.getActiveSubscription()));
        Call fgCall = phone.getForegroundCall();
        Call bgCall = phone.getBackgroundCall();

//        if (fgCall.getState() == Call.State.IDLE) {
//            if (DBG) log("updateOtherForegroundCall: no active call, show holding call");
//            fgCall = bgCall;
//            bgCall = null;
//        }  


        int phoneType = phone.getPhoneType();
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            if ((mApplication.cdmaPhoneCallState.getCurrentCallState()
                    == CdmaPhoneCallState.PhoneCallState.THRWAY_ACTIVE)
                    && mApplication.cdmaPhoneCallState.IsThreeWayCallOrigStateDialing()) {
                displayOtherSecondaryCallStatus(cm, fgCall);
            } else {
                //This is required so that even if a background call is not present
                // we need to clean up the background call area.
            	displayOtherSecondaryCallStatus(cm, bgCall);
            }
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
                || (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
        	displayOtherSecondaryCallStatus(cm, bgCall);
        }
        
    }
    
    
    private void displayOtherSecondaryCallStatus(CallManager cm, Call call) {
        if (DBG) log("displayOtherSecondaryCallStatus(call =" + call + ")...");

        int sub = PhoneUtils.getOtherActiveSub(PhoneUtils.getActiveSubscription());
        
        Call otherFgCall = null;
        if(sub != -1) {
        	otherFgCall = PhoneGlobals.getInstance().getPhone(sub).getForegroundCall();
        }
        
        if ((call == null || call.getState() == Call.State.IDLE) 
        		&& PhoneUtils.isAnyOtherSubActive(PhoneUtils.getActiveSubscription())
        		&& (otherFgCall != null && !otherFgCall.isIdle())) {
        	
            if (DBG) log("displayOtherSecondaryCallStatus(call =  11111");
        	
        	   showOtherSecondaryCallInfo();

          	   if (PhoneUtils.isConferenceCall(otherFgCall)) {
                   if (DBG) log("==> conference call.");
                   mThirdCallName.setText(getContext().getString(R.string.confCall));
                   showImage(mThirdCallPhoto, R.drawable.picture_conference);
               } else {    	   
	               new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(otherFgCall));
	               PhoneUtils.CallerInfoToken infoToken2 = PhoneUtils.startGetCallerInfo(
	                       getContext(), otherFgCall, this, mThirdCallName);
	               mThirdCallName.setText(
	                       PhoneUtils.getCompactNameFromCallerInfo(infoToken2.currentInfo,
	                                                               getContext()));
	
	               if (infoToken2.isFinal) {
	               	if (DBG) log(" displayThirdCallStatus 1");
	                // if(mSecondaryCallPhoto.getDrawable()==null)	
	                   showCachedImage(mThirdCallPhoto, infoToken2.currentInfo);
	               } else {
	               	if (DBG) log(" displayThirdCallStatus 2");
	                   showImage(mThirdCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
	               }
	              //aurora add zhouxiaobing 20140411 end
               }
        	
        	
               mFourthCallInfo.setVisibility(View.GONE);
               return;
        } else if ((call == null) || (PhoneGlobals.getInstance().isOtaCallInActiveState())) {
        	//aurora add zhouxiaobing 20131008
        	mThirdCallInfo.setVisibility(View.GONE);
            mFourthCallInfo.setVisibility(View.GONE);
            return;
        }

        Phone phone = call.getPhone();
        Call.State state = call.getState();
        switch (state) {
            case HOLDING:
                // Ok, there actually is a background call on hold.
                // Display the "on hold" box.

                // Note this case occurs only on GSM devices.  (On CDMA,
                // the "call on hold" is actually the 2nd connection of
                // that ACTIVE call; see the ACTIVE case below.)
                showOtherSecondaryCallInfo();

                if (PhoneUtils.isConferenceCall(call)) {
                    if (DBG) log("==> conference call.");
                    mFourthCallName.setText(getContext().getString(R.string.confCall));
                    showImage(mFourthCallPhoto, R.drawable.picture_conference);
                } else {
                    // perform query and update the name temporarily
                    // make sure we hand the textview we want updated to the
                    // callback function.
                    if (DBG) log("==> NOT a conf call; call startGetCallerInfo...");
                    //aurora add zhouxiaobing 20140411 start    
                    new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(call));
                    //aurora add zhouxiaobing 20140411 end  
                    PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                            getContext(), call, this, mFourthCallName);
                    mFourthCallName.setText(
                            PhoneUtils.getCompactNameFromCallerInfo(infoToken.currentInfo,
                                                                    getContext()));

                    // Also pull the photo out of the current CallerInfo.
                    // (Note we assume we already have a valid photo at
                    // this point, since *presumably* the caller-id query
                    // was already run at some point *before* this call
                    // got put on hold.  If there's no cached photo, just
                    // fall back to the default "unknown" image.)
                    if (infoToken.isFinal) {
                    	if (DBG) log(" displayFourthCallStatus 1");
//                      if(mFourthCallPhoto.getDrawable()==null)	
                        showCachedImage(mFourthCallPhoto, infoToken.currentInfo);
                    } else {
                    	if (DBG) log(" displayFourthCallStatus 2");
                        showImage(mFourthCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
                    }
                    
                    otherFgCall = phone.getForegroundCall();
                    
                    if(!otherFgCall.isIdle()) {                    	
                 	   if (PhoneUtils.isConferenceCall(otherFgCall)) {
                           if (DBG) log("==> conference call.");
                           mThirdCallName.setText(getContext().getString(R.string.confCall));
                           showImage(mThirdCallPhoto, R.drawable.picture_conference);
                       } else {    
		                    //aurora add zhouxiaobing 20140411 start
		                    new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(otherFgCall));
		                    PhoneUtils.CallerInfoToken infoToken2 = PhoneUtils.startGetCallerInfo(
		                            getContext(), otherFgCall, this, mThirdCallName);
		                    mThirdCallName.setText(
		                            PhoneUtils.getCompactNameFromCallerInfo(infoToken2.currentInfo,
		                                                                    getContext()));
		
		                    // Also pull the photo out of the current CallerInfo.
		                    // (Note we assume we already have a valid photo at
		                    // this point, since *presumably* the caller-id query
		                    // was already run at some point *before* this call
		                    // got put on hold.  If there's no cached photo, just
		                    // fall back to the default "unknown" image.)
		                    if (infoToken2.isFinal) {
		                    	if (DBG) log(" displayThirdCallStatus 1");
		                     // if(mSecondaryCallPhoto.getDrawable()==null)	
		                        showCachedImage(mThirdCallPhoto, infoToken2.currentInfo);
		                    } else {
		                    	if (DBG) log(" displayThirdCallStatus 2");
		                        showImage(mThirdCallPhoto, mdefault_photo_id);//aurora change zhouxiaobing 20131002
		                    }
		                   //aurora add zhouxiaobing 20140411 end
                       }
                    }  else {
                 	   mThirdCallInfo.setVisibility(View.GONE);
                    }
                }
 //               AnimationUtils.Fade.show(mSecondaryCallPhotoDimEffect);
                break;

            case ACTIVE:
                // CDMA: This is because in CDMA when thecm.getActiveFgCall() user originates the second call,
                // although the Foreground call state is still ACTIVE in reality the network
                // put the first call on hold.
                if (phone.getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
                    showOtherSecondaryCallInfo();

                    List<Connection> connections = call.getConnections();
                    if (connections.size() > 2) {
                        // This means that current Mobile Originated call is the not the first 3-Way
                        // call the user is making, which in turn tells the PhoneGlobals that we no
                        // longer know which previous caller/party had dropped out before the user
                        // made this call.
                        mFourthCallName.setText(
                                getContext().getString(R.string.card_title_in_call));
                        showImage(mFourthCallPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                    } else {
                        // This means that the current Mobile Originated call IS the first 3-Way
                        // and hence we display the first callers/party's info here.
                        Connection conn = call.getEarliestConnection();
                        PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                                getContext(), conn, this, mFourthCallName);

                        // Get the compactName to be displayed, but then check that against
                        // the number presentation value for the call. If it's not an allowed
                        // presentation, then display the appropriate presentation string instead.
                        AuroraCallerInfo info = infoToken.currentInfo;

                        String name = PhoneUtils.getCompactNameFromCallerInfo(info, getContext());
                        boolean forceGenericPhoto = false;
                        if (info != null && info.numberPresentation !=
                                PhoneConstants.PRESENTATION_ALLOWED) {
                            name = PhoneUtils.getPresentationString(
                                    getContext(), info.numberPresentation);
                            forceGenericPhoto = true;
                        }
                        mFourthCallName.setText(name);

                        // Also pull the photo out of the current CallerInfo.
                        // (Note we assume we already have a valid photo at
                        // this point, since *presumably* the caller-id query
                        // was already run at some point *before* this call
                        // got put on hold.  If there's no cached photo, just
                        // fall back to the default "unknown" image.)
                        if (!forceGenericPhoto && infoToken.isFinal) {
                            showCachedImage(mFourthCallPhoto, info);
                        } else {
                            showImage(mFourthCallPhoto, /*R.drawable.picture_unknown*/mdefault_photo_id);//aurora change zhouxiaobing 20131002
                        }
                    }
                } else {
                    // We shouldn't ever get here at all for non-CDMA devices.
                    Log.w(LOG_TAG, "displayOtherOnHoldCallStatus: ACTIVE state on non-CDMA device");
                    mThirdCallInfo.setVisibility(View.GONE);//aurora add zhouxiaobing 20131008
                    mFourthCallInfo.setVisibility(View.GONE);
                }

//                AnimationUtils.Fade.hide(mSecondaryCallPhotoDimEffect, View.GONE);
                break;

            default:
                // There's actually no call on hold.  (Presumably this call's
                // state is IDLE, since any other state is meaningless for the
                // background call.)
            	mFourthCallInfo.setVisibility(View.GONE);
                mThirdCallInfo.setVisibility(View.GONE);//aurora add zhouxiaobing 20131008
                break;
        }
    }
    
    
    private void showOtherSecondaryCallInfo() {
        // This will call ViewStub#inflate() when needed.
    	CallManager cm =PhoneGlobals.getInstance().mCM;
    	 Phone phone = PhoneGlobals.getInstance().getPhone(PhoneUtils.getOtherActiveSub(PhoneUtils.getActiveSubscription()));
         Call fgCall = phone.getForegroundCall();
         Call bgCall = phone.getBackgroundCall();
        Call.State state = fgCall.getState();
        mFourthCallInfo.setVisibility(View.VISIBLE);
        if (mFourthCallName == null) {
            mFourthCallName = (TextView) findViewById(R.id.fourthCallName);
        }
        if (mFourthCallPhoto == null) {
            mFourthCallPhoto = (ImageView) findViewById(R.id.fourthCallPhoto);
        }
        if (mFourthCallPhotoDimEffect == null) {
            mFourthCallPhotoDimEffect = findViewById(R.id.dim_effect_for_fourth_photo);
            mFourthCallPhotoDimEffect.setOnClickListener(mInCallScreen);
            // Add a custom OnTouchListener to manually shrink the "hit target".
//            mSecondaryCallPhotoDimEffect.setOnTouchListener(new SmallerHitTargetTouchListener());
        }
        //aurora add zhouxiaobing 20131008 start
//             	setPrimaryCallInfoVisibility(false);
        mThirdCallInfo.setVisibility(View.VISIBLE);
        
        
//        if (mSimIcon3 == null) {
//        	mSimIcon3 = (ImageView) findViewById(R.id.aurora_sim_slot_3);
//        }
//        
//        if (mSimIcon4 == null) {
//        	mSimIcon4 = (ImageView) findViewById(R.id.aurora_sim_slot_4);
//        }
//        
//       	if(PhoneUtils.isMultiSimEnabled()) {
//       		mSimIcon3.setVisibility(View.VISIBLE);
//       		mSimIcon4.setVisibility(View.VISIBLE);
//	        mSimIcon3.setImageResource(SimIconUtils.getSmallSimIcon(PhoneUtils.getSubscription(fgCall.getPhone())));
//	        mSimIcon4.setImageResource(SimIconUtils.getSmallSimIcon(PhoneUtils.getSubscription(bgCall.getPhone())));	        
//       	} else {
//    		mSimIcon3.setVisibility(View.GONE);
//    		mSimIcon4.setVisibility(View.GONE);
//       	}/**///aurora change zhouxiaobing 20140523
        
        if (mThirdCallName == null) {
            mThirdCallName = (TextView) findViewById(R.id.thirdCallName);
        }
//        mThirdCallName.setText(mName.getText());
//        if(mThirdcalltime==null)
//        {
//        	mThirdcalltime=(TextView) findViewById(R.id.thirdCallTime);
//        }
        String callStateLabel=null;
        final Context context = getContext();
        int bluetoothIconId=0;
        switch (state) {
        case IDLE:
            // "Call state" is meaningless in this state.
            break;

        case ACTIVE:
            // We normally don't show a "call state label" at all in
            // this state (but see below for some special cases).
//        	is_time_need_update=true;
        	callStateLabel=mElapsedTime.getText().toString();
            break;

        case HOLDING:
            //callStateLabel = context.getString(R.string.card_title_on_hold);
            break;

        case DIALING:
        case ALERTING:
//        	is_time_need_update=false;
            callStateLabel = context.getString(R.string.card_title_dialing);
            break;

        case INCOMING:
        case WAITING:
//        	is_time_need_update=false;
           // callStateLabel = context.getString(R.string.card_title_incoming_call);//aurora change zhouxiaobing 20131015

            // Also, display a special icon (alongside the "Incoming call"
            // label) if there's an incoming call and audio will be routed
            // to bluetooth when you answer it.
            if (mApplication.showBluetoothIndication()) {
                bluetoothIconId = R.drawable.ic_incoming_call_bluetooth;
            }
            break;

        case DISCONNECTING:
//        	is_time_need_update=false;
            // While in the DISCONNECTING state we display a "Hanging up"
            // message in order to make the UI feel more responsive.  (In
            // GSM it's normal to see a delay of a couple of seconds while
            // negotiating the disconnect with the network, so the "Hanging
            // up" state at least lets the user know that we're doing
            // something.  This state is currently not used with CDMA.)
            callStateLabel = context.getString(R.string.card_title_hanging_up);
            break;

        case DISCONNECTED:
//        	is_time_need_update=false;
            callStateLabel = getCallFailedString(fgCall);
//            is_sencond_end=true;
            break;

        default:
            Log.wtf(LOG_TAG, "updateCallStateWidgets: unexpected call state: " + state);
            break;
    }
//        mThirdcalltime.setText(callStateLabel);
        if (mThirdCallPhoto == null) {
        	mThirdCallPhoto = (ImageView) findViewById(R.id.thirdCallPhoto);
        }
//        mThirdCallPhoto.setImageDrawable(mPhoto.getDrawable());
        if (mThirdCallPhotoDimEffect == null) {
        	mThirdCallPhotoDimEffect = findViewById(R.id.dim_effect_for_third_photo);
        	mThirdCallPhotoDimEffect.setOnClickListener(mInCallScreen);
            // Add a custom OnTouchListener to manually shrink the "hit target".
        	//mFirstCallPhotoDimEffect.setOnTouchListener(new SmallerHitTargetTouchListener());
        }       
      //aurora add zhouxiaobing 20131008 end
        mInCallScreen.updateButtonStateOutsideInCallTouchUi();
    }
    
    void updateAuroraDefaultPhotoId(int is_sim) {
    	if(PhoneApp.isV2) {
    		mdefault_photo_id = R.drawable.large_svg_dial_default_photo1;
    		return;
    	}

    }         

	private void auroraUpdateFirstInfo() {
       //aurora add zhouxiaobing 20131008 start
        CallManager cm = PhoneGlobals.getInstance().mCM;
        Call fgCall = cm.getActiveFgCall();
        Call bgCall = cm.getFirstActiveBgCall();
        Call.State state = fgCall.getState();
//            	setPrimaryCallInfoVisibility(false);
        
        mFirstCallInfo.setVisibility(View.VISIBLE);
        
        
//        if (mSimIcon1 == null) {
//        	mSimIcon1 = (ImageView) findViewById(R.id.aurora_sim_slot_1);
//        }
//        
//        if (mSimIcon2 == null) {
//        	mSimIcon2 = (ImageView) findViewById(R.id.aurora_sim_slot_2);
//        }
//        
//       	if(PhoneUtils.isMultiSimEnabled()) {
//       		mSimIcon1.setVisibility(View.VISIBLE);
//       		mSimIcon2.setVisibility(View.VISIBLE);
//	        mSimIcon1.setImageResource(SimIconUtils.getSmallSimIcon(PhoneUtils.getSubscription(fgCall.getPhone())));
//	        mSimIcon2.setImageResource(SimIconUtils.getSmallSimIcon(PhoneUtils.getSubscription(bgCall.getPhone())));        
//       	} else {
//    		mSimIcon1.setVisibility(View.GONE);
//    		mSimIcon2.setVisibility(View.GONE);
//       	}/**///aurora change zhouxiaobing 20140523
        
        if (mFirstCallName == null) {
            mFirstCallName = (TextView) findViewById(R.id.firstCallName);
        }
        mFirstCallName.setText(mName.getText());
		if (mfirstcalltime == null) {
			mfirstcalltime = (TextView) findViewById(R.id.firstCallTime);
	        mfirstcalltime.setTypeface(mRobotoLightTf);        
		}		
		
		if(mfirstcallstate == null) {
			mfirstcallstate =	(TextView) findViewById(R.id.firstCallState);
		}
        String callStateLabel=null;
        final Context context = getContext();
        int bluetoothIconId=0;
        switch (state) {
        case IDLE:
            break;

        case ACTIVE:
//        	callStateLabel=mElapsedTime.getText().toString();
        	callStateLabel =  context.getString(R.string.card_title_in_progress);
            break;

        case HOLDING:
            break;

        case DIALING:
        case ALERTING:
            callStateLabel = context.getString(R.string.card_title_dialing);
            break;

        case INCOMING:
        case WAITING:
            if (mApplication.showBluetoothIndication()) {
                bluetoothIconId = R.drawable.ic_incoming_call_bluetooth;
            }
            break;

        case DISCONNECTING:
            callStateLabel = context.getString(R.string.card_title_hanging_up);
            break;

        case DISCONNECTED:
            callStateLabel = getCallFailedString(fgCall);
            break;

        default:
            Log.wtf(LOG_TAG, "updateCallStateWidgets: unexpected call state: " + state);
            break;
    }

         mfirstcallstate.setText(callStateLabel);
        
        if (mFirstCallPhoto == null) {
            mFirstCallPhoto = (ImageView) findViewById(R.id.firstCallPhoto);
        }
//        BitmapDrawable bd = (BitmapDrawable)mPhoto.getDrawable();
//		Bitmap bitmap1 = bd.getBitmap();
//		Bitmap bitmap2 = RoundBitmapUtils.toRoundBitmap(bitmap1);
//		Drawable drawable = new BitmapDrawable(mPhoto.getContext().getResources(), bitmap2);
//        mFirstCallPhoto.setImageDrawable(drawable);  
	}

	
	void setDefaultPhoto(int id) {
		if(PhoneApp.isV2) {
			return;
		}
		mdefault_photo_id = id;
	}	
	
	private void updateAddress(AuroraCallerInfo aci, Call call) {
		String address = aci.mArea;
        Log.d(LOG_TAG, "updateAddress address =  " + address);
		// aurora change liguangyu 20131104 for BUG #356 start
		if (address == null || address.length() == 0
				|| PhoneUtils.isConferenceCall(call)) {
			// aurora change liguangyu 20131104 for BUG #356 end
			mAdrress.setVisibility(View.GONE);
//			mPhoneNumber.setPadding(0, 0, 0, 0);
		} else {
			mAdrress.setVisibility(View.VISIBLE);
			mAdrress.setText(address);
			int right = getContext().getResources().getDimensionPixelSize(
					R.dimen.call_number_address_distance);
//			mPhoneNumber.setPadding(0, 0, right, 0);
		}
		// aurora add zhouxiaobing 20130927 end		
	}
	
	private void updateAddressNull() {
		mAdrress.setVisibility(View.GONE);
//		mPhoneNumber.setPadding(0, 0, 0, 0);
	}
	
	
	private void updateNote(Call.State state, AuroraCallerInfo aci) {	 
		String note = aci.mNote;
        if(note == null || note.length() == 0 || state == Call.State.DIALING || state == Call.State.ALERTING) {       
        	mNoteLine.setVisibility(View.GONE);
        	mNote.setText("");
        } else {
            mNoteLine.setVisibility(View.VISIBLE);
            mNote.setVisibility(View.VISIBLE);
        	mNote.setText(note);
        }                                 
	}
	
	private void updateMark(Call.State state, AuroraCallerInfo aci) {	 
        if(!TextUtils.isEmpty(aci.name)) {
       
        	return;
        }
                                       
        if(TextUtils.isEmpty(aci.mMark)||state == Call.State.DIALING ||state == Call.State.ALERTING) {
        } else {   
            String finalNote = aci.mMark + " |";
            if(!TextUtils.isEmpty(aci.mMarkNumber)) {
            	if(Integer.valueOf(aci.mMarkNumber) > 0) {
            		finalNote += " " + aci.mMarkNumber + getContext().getString(R.string.aurora_sogou_mark);
            	}            
            } else {
            	finalNote += getContext().getString(R.string.aurora_sogou_local_mark);           
            }

        }
      
	}
	
	
	private void updateSlogan() {	 		        
	    if(PhoneGlobals.getInstance().mYuloreUtils.isInit()) {
	    	String slogan = YuLoreUtils.getSlogan();
            mSloganNote.setText(slogan);
            if(!TextUtils.isEmpty(slogan)) {
            	mSloganNote.setVisibility(View.VISIBLE);
            } else {
               	mSloganNote.setVisibility(View.GONE);
            }
            mPhotoV.setVisibility(YuLoreUtils.getIsV() ? View.VISIBLE : View.INVISIBLE);
	    }
	}
	
	private boolean isNeedUpdateFirstInfoTime() {
	    Call fgCall = PhoneGlobals.getInstance().mCM.getActiveFgCall();
        Call.State fgState = fgCall.getState();
        Call bgCall = PhoneGlobals.getInstance().mCM.getFirstActiveBgCall();
        Call.State bgState = bgCall.getState();
        boolean isDualActive = false;
        if(PhoneUtils.isMultiSimEnabled()) {
        	if(PhoneGlobals.getInstance().getPhone(0).getState() != PhoneConstants.State.IDLE 
        			&& PhoneGlobals.getInstance().getPhone(1).getState() != PhoneConstants.State.IDLE) {
        		isDualActive = true;
        	}
        }
        return (fgState == Call.State.ACTIVE && bgState == Call.State.HOLDING) || isDualActive;
	}
	
	private void hideViewsForPrivate(Call call) {
        if (DBG) log("hideViewsForPrivate");
		
        final Phone phone = call.getPhone();
        final int phoneType = phone.getPhoneType();
        
        Connection conn = null;
        if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
            conn = call.getLatestConnection();
        } else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
              || (phoneType == PhoneConstants.PHONE_TYPE_SIP)
              || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
            conn = call.getEarliestConnection();
        } else {
            throw new IllegalStateException("Unexpected phone type: " + phoneType);
        }
        
        boolean isNotShowBecausePrivate = false;
        Object obj = conn.getUserData();
        AuroraCallerInfo info = null;
        if (obj instanceof AuroraCallerInfo) {
            info = (AuroraCallerInfo)obj;
        }
        if (info != null) {
            isNotShowBecausePrivate = ((AuroraCallerInfo)info).isPrivate();
            if (DBG) log("isNotShowBecausePrivate = " + isNotShowBecausePrivate);
        } else {
        	return;
        }          
        
        if(isNotShowBecausePrivate) {
        	mNoteLine.setVisibility(View.GONE);
    
            mSloganNote.setVisibility(View.GONE);
    		mPhotoV.setVisibility(View.INVISIBLE);
            mPhoneNumber.setVisibility(View.GONE);
            mNumberSeparator.setVisibility(View.GONE);
            mLabel.setVisibility(View.GONE);
            mAdrress.setVisibility(View.GONE);
        } 
	}
	
    public class AuroraSimContactsTask extends AsyncTask<String, Void, Boolean> {
        private static final String LOG_TAG = "AuroraSimContactsTask";
        private int mIsSimContact = -1;
        @Override
        protected void onPreExecute() {
        	
        }
    	
    	
        @Override
        protected Boolean doInBackground(String... c) {   
            mIsSimContact = ProviderUtils.isSimCardPhoneNumber(mApplication, c[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	Log.v(LOG_TAG, "onPostExecute = ");   
            updateAuroraDefaultPhotoId(mIsSimContact);
        }
        

    }

    
    private void setPhotoVisible() {
//    	if(!mAnimController.getIncoming_to_incall_anim_ex()) {
    		mPhoto.setVisibility(View.VISIBLE);
//    	}
    }
    
    private void getDtmfCallTime() {
    	if(mDtmfElapsedTime == null) {
    		mDtmfElapsedTime = mInCallScreen.getDtmfCallTime();
    	}
    }
    
    private ViewStub mWaitingCallInfo;
    private TextView mWaitingCallName;
    private ImageView mWaitingCallPhoto; 
	private ImageView mWaitingCallEnd;
	private TextView mWaitingCallTime;
	
	private boolean isNeedUpdateWaitingInfoTime() {
	    Call fgCall = PhoneGlobals.getInstance().mCM.getActiveFgCall();
  	    Call ringingCall = PhoneGlobals.getInstance().mCM.getFirstActiveRingingCall();
        return fgCall.getState() == Call.State.ACTIVE && ringingCall.getState() == Call.State.WAITING;
	}
    
    private void displayCallWaitingCallStatus(CallManager cm, Call call) {
        if (DBG) log("displayOnHoldCallStatus(call =" + call + ")...");
        
        if(call == null) {
            mWaitingCallInfo.setVisibility(View.GONE);
        	return;
        }
        
        Phone phone = call.getPhone();
        Call.State state = call.getState();
        
        if(state == Call.State.IDLE) {
            mWaitingCallInfo.setVisibility(View.GONE);
        	return;
        }
        
        mWaitingCallInfo.setVisibility(View.VISIBLE);
        if (mWaitingCallName == null) {
            mWaitingCallName = (TextView) findViewById(R.id.waitingCallName);
        }
        if (mWaitingCallEnd == null) {
        	mWaitingCallEnd = (ImageView) findViewById(R.id.waitingCallEnd);
        	mWaitingCallEnd.setOnClickListener(mInCallScreen);
        }
        
   	    int phoneType = phone.getPhoneType();
        mWaitingCallEnd.setEnabled(phoneType == PhoneConstants.PHONE_TYPE_CDMA ? false : true);
        
        if (mWaitingCallPhoto == null) {
            mWaitingCallPhoto = (ImageView) findViewById(R.id.waitingCallPhoto);
        }
   
        
        if(mWaitingCallTime == null) {
        	mWaitingCallTime =	(TextView) findViewById(R.id.waitingCallTime); 
        	mWaitingCallTime.setTypeface(mRobotoLightTf);      
		}
       	mWaitingCallTime.setText("");

       
        
        if (PhoneUtils.isConferenceCall(call)) {
            if (DBG) log("==> conference call.");
            mWaitingCallName.setText(getContext().getString(R.string.confCall));
            showImage(mWaitingCallPhoto, R.drawable.picture_conference);
        } else {
          
            if (DBG) log("==> NOT a conf call; call startGetCallerInfo...");
           //aurora add zhouxiaobing 20140411 start             
        	new AuroraSimContactsTask().execute(ProviderUtils.getNumberFromCall(call));
           //aurora add zhouxiaobing 20140411 end        
            PhoneUtils.CallerInfoToken infoToken = PhoneUtils.startGetCallerInfo(
                    getContext(), call, this, mWaitingCallName);
            mWaitingCallName.setText(
                    PhoneUtils.getCompactNameFromCallerInfo(infoToken.currentInfo,
                                                            getContext()));
          
            if (infoToken.isFinal) {
            	if (DBG) log(" displayWaitingCallStatus mWaitingCallPhoto 1="+infoToken.currentInfo.cachedPhoto);
			     showCachedImage(mWaitingCallPhoto, infoToken.currentInfo);				
            } else {
            	if (DBG) log(" displayWaitingCallStatus 2");
                showImage(mWaitingCallPhoto, mdefault_photo_id);
            }
                   
        }

    }
    
    private void setPrimaryCallInfoVisibility(boolean visible) {
    	mPrimaryCallInfo.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
    
    private void updateDtmfHold() {
    	TextView tv = mInCallScreen.getDtmfHold();
    	if(tv != null) {
    		tv.setVisibility(PhoneGlobals.getInstance().mCM.hasActiveBgCall() ? View.VISIBLE : View.GONE);
    	}
    }
    
    public TextView getCallTime() {
    	return mElapsedTime;
    }
    
	private void updateCallerInfoPosition() {
		CallManager cm = PhoneGlobals.getInstance().mCM;
		Call fgCall = cm.getActiveFgCall();
		Call bgCall = cm.getFirstActiveBgCall();
		long fgtime = fgCall.getEarliestCreateTime();
		long bgtime = bgCall.getEarliestCreateTime();
		int firstTop = getResources().getDimensionPixelOffset(
				R.dimen.first_callerinfo_top);
		int secondTop = getResources().getDimensionPixelOffset(
				R.dimen.second_callerinfo_top);
		if (fgtime < bgtime) {
			ViewGroup.MarginLayoutParams firstCallInfoLp = (ViewGroup.MarginLayoutParams) mFirstCallInfo
					.getLayoutParams();
			firstCallInfoLp.topMargin = firstTop;
			mFirstCallInfo.setLayoutParams(firstCallInfoLp);

			ViewGroup.MarginLayoutParams secondCallInfoLp = (ViewGroup.MarginLayoutParams) mSecondaryCallInfo
					.getLayoutParams();
			secondCallInfoLp.topMargin = secondTop;
			mSecondaryCallInfo.setLayoutParams(secondCallInfoLp);
		
		} else {
			ViewGroup.MarginLayoutParams firstCallInfoLp = (ViewGroup.MarginLayoutParams) mFirstCallInfo
					.getLayoutParams();
			firstCallInfoLp.topMargin = secondTop;
			mFirstCallInfo.setLayoutParams(firstCallInfoLp);

			ViewGroup.MarginLayoutParams secondCallInfoLp = (ViewGroup.MarginLayoutParams) mSecondaryCallInfo
					.getLayoutParams();
			secondCallInfoLp.topMargin = firstTop;
			mSecondaryCallInfo.setLayoutParams(secondCallInfoLp);
		}
	}
	
	private Typeface mRobotoLightTf;
	
    private static int[] largeRandomContactPhotoId=new int[]{
		R.drawable.large_contact_photo_dog,
		R.drawable.large_contact_photo_bear,
		R.drawable.large_contact_photo_bird,
		R.drawable.large_contact_photo_cat,
		R.drawable.large_contact_photo_cattle,//4
		R.drawable.large_contact_photo_crocodile,
		R.drawable.large_contact_photo_elephant,
		R.drawable.large_contact_photo_fox,
		R.drawable.large_contact_photo_jellyfish,
		R.drawable.large_contact_photo_parrot,
		R.drawable.large_contact_photo_rhinoceros,
		R.drawable.large_contact_photo_sheep,//11
		R.drawable.large_contact_photo_swan,
		R.drawable.large_svg_dial_default_photo1
	};
    
    public void  setCallTime(TextView time) {
    	mElapsedTime =  time;
    	mElapsedTime.setTypeface(mRobotoLightTf);           
    }
	
}

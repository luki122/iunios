/*
 * Copyright (C) 2013 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.incallui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.incallui.InCallPresenter.InCallState;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import android.telecom.VideoProfile;

/**
 *
 */
public class AuroraAnswerFragment extends BaseFragment<AuroraAnswerPresenter, AuroraAnswerPresenter.AnswerUi>
        implements SlideView2.AnswerListener, AuroraAnswerPresenter.AnswerUi,  View.OnClickListener, GlowPadView.WhenTouchListener, GlowPadView.OnTriggerListener {

    public static final int TARGET_SET_FOR_AUDIO_WITHOUT_SMS = 0;
    public static final int TARGET_SET_FOR_AUDIO_WITH_SMS = 1;
    public static final int TARGET_SET_FOR_VIDEO_WITHOUT_SMS = 2;
    public static final int TARGET_SET_FOR_VIDEO_WITH_SMS = 3;
    public static final int TARGET_SET_FOR_VIDEO_UPGRADE_REQUEST = 4;

    /**
     * The popup showing the list of canned responses.
     *
     * This is an AlertDialog containing a ListView showing the possible choices.  This may be null
     * if the InCallScreen hasn't ever called showRespondViaSmsPopup() yet, or if the popup was
     * visible once but then got dismissed.
     */
    private Dialog mCannedResponsePopup = null;

    /**
     * The popup showing a text field for users to type in their custom message.
     */
    private AlertDialog mCustomMessagePopup = null;

    private ArrayAdapter<String> mSmsResponsesAdapter;

    private final List<String> mSmsResponses = new ArrayList<>();

    private SlideView2 mIncomingCallWidget;
    private GlowPadView mOriginIncomingCallWidget;
    private TextView mSlideDownText;
    
	private TextView smsrespond1;
	private TextView smsrespond2;
	private TextView smsrespond3;
	private TextView smsrespond4;
	
    private View callWaitingView;
    private ImageView callWaitingViewAnswer1;
    private ImageView callWaitingViewAnswer2;
    private ImageView callWaitingViewEnd;
    
    private AuroraInCallTouchUiAnimationController mAnimController;
    
    private boolean mIsSmsShow = false;

    public AuroraAnswerFragment() {
    }

    @Override
    public AuroraAnswerPresenter createPresenter() {
        return new AuroraAnswerPresenter();
    }

    @Override
    AuroraAnswerPresenter.AnswerUi getUi() {
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.aurora_answer_fragment,
                container, false);
        
        mIncomingCallWidget = (SlideView2) parent.findViewById(R.id.incomingCallWidget);
        mIncomingCallWidget.setAnswerListener(this);
        mIncomingCallWidget.setActivity((InCallActivity)getActivity());
        
        mOriginIncomingCallWidget = (GlowPadView) parent.findViewById(R.id.OriginIncomingCallWidget);
        mOriginIncomingCallWidget.setActivity((InCallActivity)getActivity());
   		mOriginIncomingCallWidget.setWhenTouchListener(this);
   	    mOriginIncomingCallWidget.setOnTriggerListener(this);
   		
   		mSlideDownText = (TextView) parent.findViewById(R.id.slidedowntext);
        
        
    	smsrespond1 = (TextView) parent.findViewById(R.id.smsrespond1);
		smsrespond1.setOnClickListener(this);
    	smsrespond2 = (TextView) parent.findViewById(R.id.smsrespond2);
		smsrespond2.setOnClickListener(this);
    	smsrespond3 = (TextView) parent.findViewById(R.id.smsrespond3);
		smsrespond3.setOnClickListener(this);
    	smsrespond4 = (TextView) parent.findViewById(R.id.smsrespond4);
		smsrespond4.setOnClickListener(this);
		
        callWaitingView = parent.findViewById(R.id.call_waiting_line);
        callWaitingViewAnswer1 = (ImageView)callWaitingView.findViewById(R.id.answer_callwaiting_hold);
        callWaitingViewAnswer1.setOnClickListener(this);
        callWaitingViewAnswer2 = (ImageView)callWaitingView.findViewById(R.id.answer_callwaiting_hangup);
        callWaitingViewAnswer2.setOnClickListener(this);
        callWaitingViewEnd = (ImageView)callWaitingView.findViewById(R.id.answer_callwaiting_end);
        callWaitingViewEnd.setOnClickListener(this);
               
        mAnimController = ((InCallActivity)getActivity()).mInCallTouchUiAnimationController;
        mAnimController.setView(parent);

        Log.d(this, "Creating view for answer fragment ", this);
        Log.d(this, "Created from activity", getActivity());

        return parent;
    }
    
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        Log.d(this, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    public void showAnswerUi(boolean show) {
        getView().setVisibility(show ? View.VISIBLE : View.GONE);
        updateUi();
        if(!show) {
        	mAnimController.stopSlideTextAnim();
        }
        Log.d(this, "Show answer UI: " + show);
    }


    @Override
    public void showMessageDialog() {
    	((InCallActivity) getActivity()).mCallCardAnimationController.RingToSms();		
	    mAnimController.ringToSms();
	    mIsSmsShow = true;

    }
    
    public void dismissPendingDialogues() {
    	mIsSmsShow = false;
    	updateUi();
    }


    public boolean hasPendingDialogs() {
    	return mIsSmsShow;
    }


    @Override
    public Context getContext() {
        return getActivity();
    }

    @Override
    public void onAnswer(int videoState, Context context) {
		((InCallActivity) getActivity()).mCallCardAnimationController.startAcceptRingingCallAnimation();		
        //aurora change zhouxiaobing 20131029 start
        mAnimController.stopSlideTextAnim();
        mAnimController.ManDongzuoStartAnimationRight();
//        getPresenter().onAnswer(videoState, context);
    }
    
    public void onAnswerNoAnimate(int videoState, Context context) {
        getPresenter().onAnswer(videoState, context);
    }

    @Override
    public void onDecline() {
		((InCallActivity) getActivity()).mCallCardAnimationController.startRejectRingingCallAnimation();
//        getPresenter().onDecline();
    }

    @Override
    public void onText() {			
        getPresenter().onText();
    }

    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d(this, "onClick(View " + view + ", id " + id + ")...");

        switch(id) {
            case R.id.smsrespond1:
                getPresenter().rejectCallWithMessage(smsrespond1.getText().toString());
                break;
            case R.id.smsrespond2:
                getPresenter().rejectCallWithMessage(smsrespond2.getText().toString());
                break;
            case R.id.smsrespond3:
                getPresenter().rejectCallWithMessage(smsrespond3.getText().toString());
                break;
            case R.id.smsrespond4:
            	getPresenter().onDecline();
                getPresenter().launchGnSmsCompose();
                break;
            case R.id.answer_callwaiting_hold:
            	onAnswerNoAnimate(VideoProfile.VideoState.AUDIO_ONLY, getContext());
                break; 
            case R.id.answer_callwaiting_hangup:
            	getPresenter().onDeclineActive();
//            	onAnswerNoAnimate(VideoProfile.VideoState.AUDIO_ONLY, getContext());
                break; 
            case R.id.answer_callwaiting_end:
            	getPresenter().onDecline();
                break; 

            default:
                Log.wtf(this, "onClick: unexpected");
                break;
        }
    }
    
    public void updateUi(){
    	if(getPresenter().isCallWating()) {
    		Log.d(this, "updateUi isCallWating");
	 		callWaitingView.setVisibility(View.VISIBLE);
	 		if(GlowPadView.mIsUseNewUI) {
		 		mIncomingCallWidget.setVisibility(View.GONE);
	 		} else {
		 		mOriginIncomingCallWidget.setVisibility(View.GONE);
		 		 mSlideDownText.setVisibility(View.GONE);
	 		}


    	} else if(getPresenter().isCallIncoming()) {
    		Log.d(this, "updateUi isCallIncoming");
    		callWaitingView.setVisibility(View.GONE);
	 		if(GlowPadView.mIsUseNewUI) {
		 		mIncomingCallWidget.setVisibility(View.VISIBLE);
	 		} else {
		 		mOriginIncomingCallWidget.setVisibility(View.VISIBLE);
		 		 mSlideDownText.setVisibility(View.VISIBLE);
	 		}

	    	
	    	  if(!GlowPadView.mIsUseNewUI) {
	    			
	  	        // TODO: wouldn't be ok to suppress this whole request if the widget is already VISIBLE
	  	        //       and we don't need to reset it?
	  	        // log("showIncomingCallWidget(). widget visibility: " + mIncomingCallWidget.getVisibility());
	  	
	  	        ViewPropertyAnimator animator = mOriginIncomingCallWidget.animate();
	  	        if (animator != null) {
	  	            animator.cancel();
	  	            // If animation is cancelled before it's running,
	  	            // onAnimationCancel will not be called and mIncomingCallWidgetIsFadingOut
	  	            // will be alway true. hideIncomingCallWidget() will not be excuted in this case.
//	  	            mIncomingCallWidgetIsFadingOut = false;
	  	        }
	  	        mOriginIncomingCallWidget.setAlpha(1.0f);
	  	
	  	        // Update the GlowPadView widget's targets based on the state of
	  	        // the ringing call.  (Specifically, we need to disable the
	  	        // "respond via SMS" option for certain types of calls, like SIP
	  	        // addresses or numbers with blocked caller-id.)
	  	//        final boolean allowRespondViaSms =
	  	//                RespondViaSmsManager.allowRespondViaSmsForCall(mInCallScreen, ringingCall);
	  	        final boolean allowRespondViaSms = false;
	  	        final int targetResourceId = allowRespondViaSms
	  	                ? R.array.incoming_call_widget_3way_targets
	  	                : R.array.aurora_incoming_call_widget_2way_targets;
	  	        // The widget should be updated only when appropriate; if the previous choice can be reused
	  	        // for this incoming call, we'll just keep using it. Otherwise we'll see UI glitch
	  	        // everytime when this method is called during a single incoming call.
	  	        if (targetResourceId != mOriginIncomingCallWidget.getTargetResourceId()) {
	  	            if (allowRespondViaSms) {
	  	                // The GlowPadView widget is allowed to have all 3 choices:
	  	                // Answer, Decline, and Respond via SMS.
	  	            	mOriginIncomingCallWidget.setTargetResources(targetResourceId);
	  	            	mOriginIncomingCallWidget.setTargetDescriptionsResourceId(
	  	                        R.array.incoming_call_widget_3way_target_descriptions);
	  	            	mOriginIncomingCallWidget.setDirectionDescriptionsResourceId(
	  	                        R.array.incoming_call_widget_3way_direction_descriptions);
	  	            } else {
	  	                // You only get two choices: Answer or Decline.
	  	            	mOriginIncomingCallWidget.setTargetResources(targetResourceId);
	  	            	mOriginIncomingCallWidget.setTargetDescriptionsResourceId(
	  	                        R.array.incoming_call_widget_2way_target_descriptions);
	  	            	mOriginIncomingCallWidget.setDirectionDescriptionsResourceId(
	  	                        R.array.incoming_call_widget_2way_direction_descriptions);
	  	            }
	  	

	  	        }
	
  	            // Watch out: be sure to call reset() and setVisibility() *after*
  	            // updating the target resources, since otherwise the GlowPadView
  	            // widget will make the targets visible initially (even before you
  	            // touch the widget.)
  	        	mOriginIncomingCallWidget.reset(false);
//  	            mIncomingCallWidgetShouldBeReset = false;
	  	        
	  	
	  	        // On an incoming call, if the layout is landscape, then align the "incoming call" text
	  	        // to the left, because the incomingCallWidget (black background with glowing ring)
	  	        // is aligned to the right and would cover the "incoming call" text.
	  	        // Note that callStateLabel is within CallCard, outside of the context of InCallTouchUi
//	  	        if (PhoneUtils.isLandscape(this.getContext())) {
//	  	            TextView callStateLabel = (TextView) mOriginIncomingCallWidget
//	  	                    .getRootView().findViewById(R.id.callStateLabel);
//	  	            if (callStateLabel != null) callStateLabel.setGravity(Gravity.LEFT);
//	  	        }
	  	
	  	        mOriginIncomingCallWidget.setVisibility(View.VISIBLE);
	  	    	mSlideDownText.setVisibility(View.VISIBLE);
	  	
	  	        // Finally, manually trigger a "ping" animation.
	  	        //
	  	        // Normally, the ping animation is triggered by RING events from
	  	        // the telephony layer (see onIncomingRing().)  But that *doesn't*
	  	        // happen for the very first RING event of an incoming call, since
	  	        // the incoming-call UI hasn't been set up yet at that point!
	  	        //
	  	        // So trigger an explicit ping() here, to force the animation to
	  	        // run when the widget first appears.
	  	        //
	  	        mHandler.removeMessages(INCOMING_CALL_WIDGET_PING);
	  	        mHandler.sendEmptyMessageDelayed(
	  	                INCOMING_CALL_WIDGET_PING,
	  	                // Visual polish: add a small delay here, to make the
	  	                // GlowPadView widget visible for a brief moment
	  	                // *before* starting the ping animation.
	  	                // This value doesn't need to be very precise.
	  	                250 /* msec */);
	     	   }
	    	
	    	  
		    	if(!mAnimController.isStartSlideToAnswer()) {
			    	mAnimController.setIncomingcallVisble();
			    	mAnimController.showIncomingCallWidget();
		    	}
	    	
    	} else {
     		Log.d(this, "updateUi else");
    		callWaitingView.setVisibility(View.GONE);
    		if(GlowPadView.mIsUseNewUI) {
		 		mIncomingCallWidget.setVisibility(View.GONE);
	 		} else {
		 		mOriginIncomingCallWidget.setVisibility(View.GONE);
		 		 mSlideDownText.setVisibility(View.GONE);
	 		}
    	}
    	mAnimController.updateState();
    }
    	
    public void whenTouch(boolean touch){
    	InCallState state = InCallPresenter.getInstance().getInCallState();
    	if(state == InCallState.INCOMING) {
    		mSlideDownText.setVisibility(touch ? View.GONE : View.VISIBLE);
    	}
    	if(mOriginIncomingCallWidget.getVisibility() != View.VISIBLE) {
    		mSlideDownText.setVisibility(View.GONE);
    	}
    }
    
    // Parameters for the GlowPadView "ping" animation; see triggerPing().
    private static final boolean ENABLE_PING_ON_RING_EVENTS = false;
    private static final boolean ENABLE_PING_AUTO_REPEAT = true;
    private static final long PING_AUTO_REPEAT_DELAY_MSEC = 2100;

    private static final int INCOMING_CALL_WIDGET_PING = 101;
    private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // If the InCallScreen activity isn't around any more,
                // there's no point doing anything here.
//                if (mInCallScreen == null) return;

                switch (msg.what) {
                    case INCOMING_CALL_WIDGET_PING:
                       Log.d(this,"INCOMING_CALL_WIDGET_PING...");
                        triggerPing();
                        break;
                    default:
//                        Log.wtf(LOG_TAG, "mHandler: unexpected message: " + msg);
                        break;
                }
            }
        };

        /**
         * Runs a single "ping" animation of the GlowPadView widget,
         * or do nothing if the GlowPadView widget is no longer visible.
         *
         * Also, if ENABLE_PING_AUTO_REPEAT is true, schedule the next ping as
         * well (but again, only if the GlowPadView widget is still visible.)
         */
        public void triggerPing() {
        	Log.d(this,"triggerPing: mOriginIncomingCallWidget = " + mOriginIncomingCallWidget);

            if (getActivity() == null || !((InCallActivity)getActivity()).isForegroundActivity()) {
                // InCallScreen has been dismissed; no need to run a ping *or*
                // schedule another one.
                Log.d(this, "- triggerPing: InCallScreen no longer in foreground; ignoring...");
                return;
            }

            if (mOriginIncomingCallWidget == null) {
                // This shouldn't happen; the GlowPadView widget should
                // always be present in our layout file.
            	Log.d(this,"- triggerPing: null mIncomingCallWidget!");
                return;
            }

            Log.d(this,"- triggerPing: mIncomingCallWidget visibility = "
                         + mOriginIncomingCallWidget.getVisibility());

            if (mOriginIncomingCallWidget.getVisibility() != View.VISIBLE) {
            	Log.d(this,"- triggerPing: mIncomingCallWidget no longer visible; ignoring...");
                return;
            }

            // Ok, run a ping (and schedule the next one too, if desired...)

            mOriginIncomingCallWidget.ping();

            if (ENABLE_PING_AUTO_REPEAT) {
                // Schedule the next ping.  (ENABLE_PING_AUTO_REPEAT mode
                // allows the ping animation to repeat much faster than in
                // the ENABLE_PING_ON_RING_EVENTS case, since telephony RING
                // events come fairly slowly (about 3 seconds apart.))

                // No need to check here if the call is still ringing, by
                // the way, since we hide mIncomingCallWidget as soon as the
                // ringing stops, or if the user answers.  (And at that
                // point, any future triggerPing() call will be a no-op.)

                // TODO: Rather than having a separate timer here, maybe try
                // having these pings synchronized with the vibrator (see
                // VibratorThread in Ringer.java; we'd just need to get
                // events routed from there to here, probably via the
                // PhoneApp instance.)  (But watch out: make sure pings
                // still work even if the Vibrate setting is turned off!)

                mHandler.sendEmptyMessageDelayed(INCOMING_CALL_WIDGET_PING,
                                                 PING_AUTO_REPEAT_DELAY_MSEC);
            }
        }
        
        private static final int AURORA_ANSWER_CALL_ID = 3;   
        private static final int AURORA_DECLINE_CALL_ID = 1;  
        
        public void onTrigger(View view, int whichHandle) {
        	switch (whichHandle) {
            case AURORA_ANSWER_CALL_ID:
            	Log.d(this, "ANSWER_CALL_ID: answer! mIsUseNewUI ");
                ((InCallActivity) getActivity()).mCallCardAnimationController.ManDongzuoToStartAnimationV2();
                mAnimController.ManDongzuoStartAnimationRightV2();
            	mHandler.postDelayed(new Runnable() {
       			@Override
       			public void run() {
       				getPresenter().onAnswer(VideoProfile.VideoState.AUDIO_ONLY, getContext());
       			}

       		}, AuroraAnimationConstant.ANIMATION_DURATION_300);

//                mShowInCallControlsDuringHidingAnimation = true;
                //aurora change zhouxiaobing 20131029 start
      			 mOriginIncomingCallWidget.setVisibility(View.GONE);
      			 mSlideDownText.setVisibility(View.GONE);
 
                break;

            case AURORA_DECLINE_CALL_ID:
            	Log.d(this, "DECLINE_CALL_ID: reject!");
            	 getPresenter().onDecline();
                // Same as "answer" case.
//                mInCallControls.setVisibility(View.GONE);
//                end_bottom_button.setVisibility(View.GONE);
                break;

            default:
            	Log.d(this, "onDialTrigger: unexpected whichHandle value: " + whichHandle);
                break;
        }
        }
        
        @Override
        public void onGrabbed(View v, int handle) {

        }

        @Override
        public void onReleased(View v, int handle) {

        }
        
        @Override
        public void onGrabbedStateChange(View v, int grabbedState) {
        
        }

		public void onFinishFinalAnimation() {
		    // Not used
		}
		
	    public void onDestoryView(){
	    	super.onDestroyView();
	    	mAnimController.setView(null);
	    }
	    
	    public void onDetach() {
	    	super.onDetach();
	    }
	    
}

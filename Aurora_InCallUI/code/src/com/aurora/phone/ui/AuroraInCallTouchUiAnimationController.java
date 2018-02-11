package com.android.incallui;

import android.animation.*;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;

import com.android.incallui.*;

import android.telecom.VideoProfile;

import com.android.incallui.InCallPresenter.InCallState;

public class AuroraInCallTouchUiAnimationController {

	private static final String LOG_TAG = "AuroraInCallTouchUiAnimationController";
	
	private View mInCallTouchUi;
	private InCallActivity mInCallActivity;

	private ImageView text_fuhao;
	private SlideTextView mSlideText;

	private SlideView2 mIncomingCallWidget;
	private View mInCallControlsContainer, mInCallControls , mInCallControlsAnim;
    private CompoundButton mDialpadButton;

	private ViewGroup respond_line;
	Animation[] anims;
	private TextView smsrespond1;
	private TextView smsrespond2;
	private TextView smsrespond3;
	private TextView smsrespond4;
	private ImageView mEndButton;
	private TextView endcall_text;
	private ImageView endcall_keypad;
	private TextView endcall_keypad_text;
	private ImageView hide_keypad;
	private TextView hide_keypad_text;
	private ViewGroup end_bottom_button;
	private int button_movedistance;
	private View incomingcall;
	private boolean is_first_show_incoming = true;
	private ObjectAnimator mIncomingcallCurrToRightAnim;
	private ObjectAnimator mIncomingcallAlphaToZeroAnim;
	private ObjectAnimator mInCallControlsLeftToCurrAnim;
	private ObjectAnimator mInCallControlsAlphaToOneAnim;
	private ObjectAnimator mEndButtonLeftToCurrAnim;
	private ObjectAnimator mEndButtonAlphaToOneAnim;
	private long now_time;
	private float incamingcallx;

	private boolean is_textslide_anim_start = false;
	private boolean is_incallcontrol_anim_start = false;
	private boolean is_end_call_keypad_anim_start = false;
	private boolean is_incoming_is_jie = false;



	AuroraInCallTouchUiAnimationController(InCallActivity a) {
		mInCallActivity = a;
	}

	
	void setView(View v) {
		mInCallTouchUi = v;
		if(mInCallControlsContainer != null && mInCallActivity != null && mInCallTouchUi != null) {
			createViewforAnim();
		}
	}
	
	void setInCallControlsView(View v) {
		mInCallControlsContainer = v;	
		if(mInCallControlsContainer != null && mInCallActivity != null && mInCallTouchUi != null) {
			createViewforAnim();
		}
	}

	// aurora add zhouxiaobing 20131231 start

	void ReleaseSource() {
//		mInCallActivity.getBackgroundView().release();
		mSlideText.release();
	}

	private void createViewforAnim() {       
        mIncomingCallWidget = (SlideView2) mInCallTouchUi.findViewById(R.id.incomingCallWidget);		
        mDialpadButton = (CompoundButton) mInCallControlsContainer.findViewById(R.id.dialpadButton);
        mInCallControls = mInCallControlsContainer.findViewById(R.id.bottomButtons);
        mInCallControlsAnim = mInCallControls.findViewById(R.id.bottomButtonsAnimField);
        
		end_bottom_button = (ViewGroup)mInCallControlsContainer.findViewById(R.id.end_bottom_button);
		mEndButton = (ImageView) end_bottom_button.findViewById(R.id.endButton);
		endcall_text = (TextView) end_bottom_button
				.findViewById(R.id.end_calltext);
		endcall_keypad = (ImageView) end_bottom_button
				.findViewById(R.id.end_keypad);
		endcall_keypad_text = (TextView) end_bottom_button
				.findViewById(R.id.endcall_text_keypad);
		hide_keypad = (ImageView) end_bottom_button
				.findViewById(R.id.hide_keypad);
		hide_keypad_text = (TextView) end_bottom_button
				.findViewById(R.id.hide_keypad_text);

		// aurora add zhouxiaobing 20131012
		incomingcall = mInCallTouchUi.findViewById(R.id.incomingcall);
		button_movedistance = mInCallActivity.getResources().getDimensionPixelSize(
				R.dimen.button_movedistance);
		text_fuhao = (ImageView) mInCallTouchUi.findViewById(R.id.text_fuhao);
		mSlideText = (SlideTextView) mInCallTouchUi.findViewById(R.id.huadongjieting);
		respond_line = (ViewGroup) mInCallTouchUi.findViewById(R.id.smsrespond_line);
		anims = new Animation[4];
		for (int i = 0; i < 4; i++)
			anims[i] = android.view.animation.AnimationUtils.loadAnimation(mInCallActivity, R.anim.smsscale);

		smsrespond1 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond1);
		smsrespond2 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond2);
		smsrespond3 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond3);
		smsrespond4 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond4);
		creatAnimation();
        mOriginIncomingCallWidget = (GlowPadView) mInCallTouchUi.findViewById(R.id.OriginIncomingCallWidget);
        mButtonLine1 = (ViewGroup)mInCallControlsContainer.findViewById(R.id.button_line_1);
        mButtonLine2 = (ViewGroup)mInCallControlsContainer.findViewById(R.id.button_line_2);

	}

	// aurora add zhouxiaobing 20131101 start


	private void creatAnimation() {
		incamingcallx = incomingcall.getX();
		mIncomingcallCurrToRightAnim = ObjectAnimator.ofFloat(incomingcall, "x", incamingcallx,
				incamingcallx + 500);
		mIncomingcallCurrToRightAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_530);
		mIncomingcallAlphaToZeroAnim = ObjectAnimator.ofFloat(incomingcall, "alpha", 1, 0);
		mIncomingcallAlphaToZeroAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_520);
		mIncomingcallCurrToRightAnim.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				is_textslide_anim_start = true;
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub

				incomingcall.setX(incamingcallx);
				incomingcall.setAlpha(1.0f);
				incomingcall.setVisibility(View.INVISIBLE);
				if(mIncomingCallWidget != null) { 
					mIncomingCallWidget.setVisibility(View.GONE);
				}
//				mInCallTouchUi.updateInCallUiButton();
				mInCallControls.setVisibility(View.VISIBLE);
				end_bottom_button.setVisibility(View.VISIBLE);
				mInCallControlsLeftToCurrAnim.start();
				mInCallControlsAlphaToOneAnim.start();
				mEndButtonLeftToCurrAnim.start();
				mEndButtonAlphaToOneAnim.start();

			}

		});

		mInCallControlsLeftToCurrAnim = ObjectAnimator.ofFloat(mInCallControls, "x", -400, 0);
		mInCallControlsLeftToCurrAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_200);
		mInCallControlsAlphaToOneAnim = ObjectAnimator.ofFloat(mInCallControls, "alpha", 0.5f, 1);
		mInCallControlsAlphaToOneAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_200);

		mEndButtonLeftToCurrAnim = ObjectAnimator.ofFloat(end_bottom_button, "x", -400, 0);
		mEndButtonLeftToCurrAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_250);
		mEndButtonAlphaToOneAnim = ObjectAnimator.ofFloat(end_bottom_button, "alpha", 0.5f, 1);
		mEndButtonAlphaToOneAnim.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_250);
		mEndButtonAlphaToOneAnim.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub

				mInCallControls.setVisibility(View.VISIBLE);
				mInCallControls.setX(0);
				mInCallControls.setAlpha(1.0f);
				end_bottom_button.setVisibility(View.VISIBLE);
				end_bottom_button.setX(0);
				end_bottom_button.setAlpha(1.0f);
				mInCallActivity.getAnswerFragment().getPresenter().onAnswer(VideoProfile.VideoState.AUDIO_ONLY, mInCallActivity);
				// mIncomingCallWidget.setCantouch(true);
				is_textslide_anim_start = false;
				Log.v("CallCard", "onAnimationEnd mEndButtonAlphaToOneAnim");
			}

		});
		now_time = 0;
	}

	//用户开始右滑
	void ManDongzuoStartAnimationRight() {
		if (!mInCallActivity.mCallCardAnimationController.getCanStartAnim()) {
			return;
		}
		is_incoming_is_jie = true;
		long time = mIncomingcallCurrToRightAnim.getCurrentPlayTime();
		mIncomingcallCurrToRightAnim.start();
		mIncomingcallAlphaToZeroAnim.start();
		mIncomingcallCurrToRightAnim.setCurrentPlayTime(now_time);
		mIncomingcallAlphaToZeroAnim.setCurrentPlayTime(now_time);
		mInCallControls.setVisibility(View.INVISIBLE);
		end_bottom_button.setVisibility(View.INVISIBLE);
	}

	void ResetAnimationRight() {
		mIncomingcallCurrToRightAnim.setCurrentPlayTime(0);
		mIncomingcallAlphaToZeroAnim.setCurrentPlayTime(0);
		now_time = 0;
		is_textslide_anim_start = false;
		Animation anim = android.view.animation.AnimationUtils.loadAnimation(mInCallActivity, R.anim.huadong);
		text_fuhao.startAnimation(anim);
		mSlideText.startAnim();
	}

	//来电UI开始消失
	void ManDongzuoAnimationRight(int per) {
		text_fuhao.clearAnimation();
		mSlideText.stopAnim();
		mIncomingcallCurrToRightAnim.setCurrentPlayTime(per);
		mIncomingcallAlphaToZeroAnim.setCurrentPlayTime(per);
		now_time = per;
		is_textslide_anim_start = true;
	}


	void OnHideShowInCallControl(boolean is_show, boolean is_anim) {
		if (is_anim) {
			if (!mInCallActivity.mCallCardAnimationController.getCanStartAnim()) {
				return;
			}
			if (is_show) {
				is_incallcontrol_anim_start = true;
				AnimationSet anim = new AnimationSet(false);
				TranslateAnimation ta = new TranslateAnimation(0, 0,
						mInCallControlsAnim.getHeight(), 0);
				ta.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_400);
				AlphaAnimation aa = new AlphaAnimation(0, 1);
				aa.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_400);
				anim.addAnimation(ta);
				anim.addAnimation(aa);
				anim.setAnimationListener(new AuroraAnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						mInCallControlsAnim.setVisibility(View.VISIBLE);// aurora add zhouxiaobing 20131008
					}
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
		
						mInCallActivity.getAnswerFragment().updateUi();
						is_incallcontrol_anim_start = false;
						mDialpadButton.setEnabled(true);
					}
				});
				mInCallControlsAnim.startAnimation(anim);
			} else {
				is_incallcontrol_anim_start = true;
				AnimationSet anim = new AnimationSet(false);
				TranslateAnimation ta = new TranslateAnimation(0, 0, 0,
						mInCallControlsAnim.getHeight());
				ta.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_400);
				AlphaAnimation aa = new AlphaAnimation(1, 0);
				aa.setDuration(AuroraAnimationConstant.ANIMATION_DURATION_400);
				anim.addAnimation(ta);
				anim.addAnimation(aa);
				anim.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						mInCallControlsAnim.setVisibility(View.GONE);
						is_incallcontrol_anim_start = false;
						mDialpadButton.setEnabled(true);
					}
				});
				mInCallControlsAnim.startAnimation(anim);
			}
		} else {
			if (is_show)
				mInCallControlsAnim.setVisibility(View.VISIBLE);
			else
				mInCallControlsAnim.setVisibility(View.GONE);
		}

	}

	void OnShowHideKeypadButton(boolean is_show, boolean is_anim) {
		if (is_anim) {
			if (!mInCallActivity.mCallCardAnimationController.getCanStartAnim()) {
				return;
			}
			int duration = AuroraAnimationConstant.ANIMATION_DURATION_400;
			if (is_show) {
				ScaleAnimation anim = new ScaleAnimation(1, 0.5f, 1, 1, 0,
						mEndButton.getHeight() / 2);
				anim.setDuration(duration);
				TranslateAnimation taa = new TranslateAnimation(0,
						-button_movedistance, 0, 0);
				taa.setDuration(duration);
				AnimationSet aass = new AnimationSet(false);
				aass.addAnimation(anim);
				aass.addAnimation(taa);
				// anim.setFillAfter(true);
				mEndButton.startAnimation(aass);
				TranslateAnimation ta = new TranslateAnimation(0,
						-mEndButton.getWidth() / 4 - button_movedistance, 0, 0);
				ta.setDuration(duration);

				// ta.setFillAfter(true);
				endcall_text.startAnimation(ta);
				AnimationSet ta2 = new AnimationSet(false);
				ScaleAnimation bsa = new ScaleAnimation(0.1f, 1, 1, 1,
						hide_keypad.getWidth(), hide_keypad.getHeight() / 2);
				bsa.setDuration(duration);
				AlphaAnimation baa = new AlphaAnimation(0, 1);
				baa.setDuration(duration);
				ta2.addAnimation(bsa);
				ta2.addAnimation(baa);
				hide_keypad.startAnimation(ta2);
				hide_keypad_text.startAnimation(ta2);
				ta.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						is_end_call_keypad_anim_start = true;
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						setKeypadEnable(true);
						setEndButtonShowOrHide(false);
						is_end_call_keypad_anim_start = false;				
					}
				});
			} else {
				ScaleAnimation anim = new ScaleAnimation(1, 2, 1, 1, 0,
						mEndButton.getHeight() / 2);
				anim.setDuration(duration);
				TranslateAnimation taa = new TranslateAnimation(0,
						button_movedistance, 0, 0);
				taa.setDuration(duration);
				AnimationSet aass = new AnimationSet(false);
				aass.addAnimation(anim);
				aass.addAnimation(taa);
				// anim.setFillAfter(true);
				endcall_keypad.startAnimation(aass);
				TranslateAnimation ta = new TranslateAnimation(0,
						mEndButton.getWidth() / 4 + button_movedistance, 0, 0);
				ta.setDuration(duration);
				// ta.setFillAfter(true);
				endcall_keypad_text.startAnimation(ta);

				AnimationSet ta2 = new AnimationSet(false);
				ScaleAnimation bsa = new ScaleAnimation(1, 0.1f, 1, 1,
						hide_keypad.getWidth(), hide_keypad.getHeight() / 2);
				bsa.setDuration(duration);
				AlphaAnimation baa = new AlphaAnimation(1, 0);
				baa.setDuration(duration);
				ta2.addAnimation(bsa);
				ta2.addAnimation(baa);
				hide_keypad.startAnimation(ta2);
				hide_keypad_text.startAnimation(ta2);
				ta.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						is_end_call_keypad_anim_start = true;
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						setKeypadEnable(true);
						setEndButtonShowOrHide(true);					
						is_end_call_keypad_anim_start = false;
					}
				});
			}
		} else {
			if (is_show) {
				setEndButtonShowOrHide(false);
			} else {
				setEndButtonShowOrHide(true);
			}
		}

	}

	private void setEndButtonShowOrHide(boolean is_show_end) {
		if (!is_show_end) {
			mEndButton.setVisibility(View.INVISIBLE);
			endcall_text.setVisibility(View.INVISIBLE);
			endcall_keypad.setVisibility(View.VISIBLE);
			endcall_keypad_text.setVisibility(View.VISIBLE);
			hide_keypad.setVisibility(View.VISIBLE);
			hide_keypad_text.setVisibility(View.VISIBLE);
		} else {
			mEndButton.setVisibility(View.VISIBLE);
			endcall_text.setVisibility(View.VISIBLE);
			endcall_keypad.setVisibility(View.INVISIBLE);
			endcall_keypad_text.setVisibility(View.INVISIBLE);
			hide_keypad.setVisibility(View.INVISIBLE);
			hide_keypad_text.setVisibility(View.INVISIBLE);
		}

	}

	void resetInCallTouchUiAnimation() {
		if (is_textslide_anim_start) {
			if (mIncomingcallCurrToRightAnim.isRunning()) {
				mIncomingcallCurrToRightAnim.end();
			}
			if (mIncomingcallAlphaToZeroAnim.isRunning()) {
				mIncomingcallAlphaToZeroAnim.end();
			}
			if (mInCallControlsLeftToCurrAnim.isRunning()) {
				mInCallControlsLeftToCurrAnim.end();
			}
			if (mInCallControlsAlphaToOneAnim.isRunning()) {
				mInCallControlsAlphaToOneAnim.end();
			}
			if (mEndButtonLeftToCurrAnim.isRunning()) {
				mEndButtonLeftToCurrAnim.end();
			}
			if (mEndButtonAlphaToOneAnim.isRunning()) {
				mEndButtonAlphaToOneAnim.end();
			}
			is_textslide_anim_start = false;
		}
		if (is_incallcontrol_anim_start) {
			is_incallcontrol_anim_start = false;
			mInCallControls.clearAnimation();
		}
		if (is_end_call_keypad_anim_start) {
			is_end_call_keypad_anim_start = false;
			mEndButton.clearAnimation();
			endcall_text.clearAnimation();
			endcall_keypad.clearAnimation();
			endcall_keypad_text.clearAnimation();
			hide_keypad.clearAnimation();
			hide_keypad_text.clearAnimation();
		}
		mHandler.removeMessages(0);
		smsrespond1.clearAnimation();
		smsrespond2.clearAnimation();
		smsrespond3.clearAnimation();
		smsrespond4.clearAnimation();
		respond_line.setVisibility(View.GONE);
		smsrespond1.setVisibility(View.INVISIBLE);
		smsrespond2.setVisibility(View.INVISIBLE);
		smsrespond3.setVisibility(View.INVISIBLE);
		smsrespond4.setVisibility(View.INVISIBLE);
		mInCallActivity.getAnswerFragment().dismissPendingDialogues();
		is_incoming_is_jie = false;
		setEndButtonShowOrHide(true);
		mDialpadButton.setEnabled(true);
		setKeypadEnable(true);
		if(mIncomingCallWidget != null) { 
			mIncomingCallWidget.Release();// aurora change zhouxiaobing 20140313 for
		}// can't touch;
		mButtonLine2.setVisibility(View.VISIBLE);
	}

	// aurora add zhouxiaobing 20131231 end

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// aurora add zhouxiaobing 20131101 start
			if (msg.what == 0) {
				respond_line.getChildAt(msg.arg1).setVisibility(View.VISIBLE);
				respond_line.getChildAt(msg.arg1).startAnimation(anims[msg.arg1]);
			}
			// aurora add zhouxiaobing 20131101 end
		}
	};

	void ringToSms() {
		if(mIncomingCallWidget != null) { 
			mIncomingCallWidget.setVisibility(View.GONE);
		}
		mInCallControls.setVisibility(View.GONE);
		end_bottom_button.setVisibility(View.GONE);
		respond_line.setVisibility(View.VISIBLE);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(0, 0, 0), 450);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(0, 1, 0), 500);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(0, 2, 0), 550);
		mHandler.sendMessageDelayed(mHandler.obtainMessage(0, 3, 0), 600);
	}

	void updateState() {
		if(mInCallActivity == null) {
			return;
		}		
		// aurora add zhouxiaobing 20140103 start
		InCallState state = InCallPresenter.getInstance().getInCallState();
		final Call ringingCall = mInCallActivity.getAnswerFragment().getPresenter().getCall();
		final Call fgCall = CallList.getInstance().getActiveCall();
		if (state == InCallState.INCOMING && fgCall == null) {
			if (mInCallActivity.getAnswerFragment().hasPendingDialogs()
					|| ringingCall.getState() == Call.State.DISCONNECTING
					|| ringingCall.getState() == Call.State.DISCONNECTED) {
			} else {
				if(mInCallActivity.getBackgroundView() != null) {
					mInCallActivity.getBackgroundView().setBackgroundId(R.drawable.bg);
				}

			}
			if (!mInCallActivity.getAnswerFragment().hasPendingDialogs()) {
				respond_line.setVisibility(View.GONE);
				smsrespond1.setVisibility(View.INVISIBLE);
				smsrespond2.setVisibility(View.INVISIBLE);
				smsrespond3.setVisibility(View.INVISIBLE);
				smsrespond4.setVisibility(View.INVISIBLE);
			}
			incomingcall.setX(incamingcallx);
			incomingcall.setAlpha(1.0f);
		} else {
			is_incoming_is_jie = false;
			if(mInCallActivity.getBackgroundView() != null) {
				mInCallActivity.getBackgroundView().setBackgroundId(R.drawable.bg);
			}
		}
	}

	void updateEndButtonText(boolean canEndCall) {
		// aurora add zhouxiaobing 20140417 start
		if (canEndCall) {
			endcall_text.setAlpha(1);
			endcall_text.setText(R.string.endcall);
		} else {
			endcall_text.setAlpha(0.15f);
			endcall_text.setText(R.string.aurora_call_disconnecting);
		}
		// aurora add zhouxiaobing 20140417 end
	}

	void setKeypadEnable(boolean enable) {
		mEndButton.setEnabled(enable);	
		endcall_keypad.setEnabled(enable);
		hide_keypad.setEnabled(enable);
	}

	void setIncomingcallVisble() {
		incomingcall.setVisibility(View.VISIBLE);
	}

	boolean isStartSlideToAnswer() {
		return is_incoming_is_jie;
	}

	void showIncomingCallWidget() {
		Animation anim = android.view.animation.AnimationUtils.loadAnimation(
				mInCallActivity, R.anim.huadong);
		text_fuhao.startAnimation(anim);
		mSlideText.startAnim();
	}

	void stopSlideTextAnim() {
		text_fuhao.clearAnimation();
		mSlideText.stopAnim();
	}

  private GlowPadView mOriginIncomingCallWidget;
    private ViewGroup mButtonLine1;
    private ViewGroup mButtonLine2;
 
	
	void ManDongzuoStartAnimationRightV2() {
		if (!mInCallActivity.mCallCardAnimationController.getCanStartAnim()) {
			return;
		}
		is_incoming_is_jie = true;					
		mOriginIncomingCallWidget.setVisibility(View.GONE);
	
//		mInCallTouchUi.updateInCallUiButton();
		mInCallControls.setVisibility(View.VISIBLE);
		mButtonLine2.setVisibility(View.GONE);
		mButtonLine1.startLayoutAnimation();
		mHandler.postDelayed(new Runnable(){
			public void run() {
				mButtonLine2.setVisibility(View.VISIBLE);
					mButtonLine2.startLayoutAnimation();
			}
		}, AuroraAnimationConstant.ANIMATION_DURATION_150);
		
		mHandler.postDelayed(new Runnable(){
			public void run() {
				end_bottom_button.setVisibility(View.VISIBLE);
				end_bottom_button.startLayoutAnimation();
			}
		}, AuroraAnimationConstant.ANIMATION_DURATION_300);
	}
}
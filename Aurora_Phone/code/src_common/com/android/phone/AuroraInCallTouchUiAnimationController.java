package com.android.phone;

import android.animation.*;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import com.android.internal.telephony.*;

public class AuroraInCallTouchUiAnimationController {

	private static final String LOG_TAG = "AuroraInCallTouchUiAnimationController";
	static AuroraInCallTouchUiAnimationController sMe;
	private PhoneGlobals mApplication;
	private InCallTouchUi mInCallTouchUi;
	private InCallScreen mInCallScreen;

	private ImageView text_fuhao;
	private SlideTextView text;

	private SlideView2 mIncomingCallWidget;
	private View mInCallControls;
    private BackgroundView groundview;
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

//	static AuroraInCallTouchUiAnimationController getInstance() {
//		if (sMe == null) {
//			sMe = new AuroraInCallTouchUiAnimationController();
//		}
//		return sMe;
//	}

	AuroraInCallTouchUiAnimationController(InCallScreen inCallScreen) {
		mInCallScreen = inCallScreen;
		groundview = (BackgroundView) mInCallScreen.findViewById(R.id.background);
		mApplication = PhoneGlobals.getInstance();
	}

//	void setInCallScreen(InCallScreen inCallScreen) {
//		mInCallScreen = inCallScreen;
//		if (mInCallScreen != null) {
//			groundview = (BackgroundView) mInCallScreen.findViewById(R.id.background);
//		} else {
//			groundview.release();
//			groundview = null;
//		}
//	}

	void setInCallTouchUi(InCallTouchUi inCallTouchUi) {
		mInCallTouchUi = inCallTouchUi;
		createViewforAnim();
	}

	// aurora add zhouxiaobing 20131231 start

	void ReleaseSource() {
		groundview.release();
		text.release();
	}

	private void createViewforAnim() {
        mInCallControls = mInCallTouchUi.findViewById(R.id.inCallControls);
        mIncomingCallWidget = (SlideView2) mInCallTouchUi.findViewById(R.id.incomingCallWidget);		
        mDialpadButton = (CompoundButton) mInCallControls.findViewById(R.id.dialpadButton);
        
		end_bottom_button = (ViewGroup)mInCallTouchUi.findViewById(R.id.end_bottom_button);
		mEndButton = (ImageView) end_bottom_button.findViewById(R.id.endButton);
		mEndButton.setOnClickListener(mInCallTouchUi);
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
		endcall_keypad.setOnClickListener(mInCallTouchUi);
		hide_keypad.setOnClickListener(mInCallTouchUi);

		// aurora add zhouxiaobing 20131012
		incomingcall = mInCallTouchUi.findViewById(R.id.incomingcall);
		button_movedistance = mApplication.getResources().getDimensionPixelSize(
				R.dimen.button_movedistance);
		text_fuhao = (ImageView) mInCallTouchUi.findViewById(R.id.text_fuhao);
		text = (SlideTextView) mInCallTouchUi.findViewById(R.id.huadongjieting);
		respond_line = (ViewGroup) mInCallTouchUi.findViewById(R.id.smsrespond_line);
		anims = new Animation[4];
		for (int i = 0; i < 4; i++)
			anims[i] = android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.smsscale);

		smsrespond1 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond1);
		smsrespond1.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				mInCallScreen.getRespondViaSmsManager().clickSmsRespond(false, smsrespond1.getText().toString());
			}
		});
		smsrespond2 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond2);
		smsrespond2.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				mInCallScreen.getRespondViaSmsManager().clickSmsRespond(false, smsrespond2.getText().toString());
			}
		});
		smsrespond3 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond3);
		smsrespond3.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				mInCallScreen.getRespondViaSmsManager().clickSmsRespond(false, smsrespond3.getText().toString());
			}
		});
		smsrespond4 = (TextView) mInCallTouchUi.findViewById(R.id.smsrespond4);
		smsrespond4.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				mInCallScreen.getRespondViaSmsManager().clickSmsRespond(true, null);
			}
		});
		creatAnimation();
		// aurora add zhouxiaobing 20131012
		// aurora changes zhouxiaobing 20131101 start
        mOriginIncomingCallWidget = (GlowPadView) mInCallTouchUi.findViewById(R.id.OriginIncomingCallWidget);
        mButtonLine1 = (ViewGroup)mInCallTouchUi.findViewById(R.id.button_line_1);
        mButtonLine2 = (ViewGroup)mInCallTouchUi.findViewById(R.id.button_line_2);
	}

	// aurora add zhouxiaobing 20131101 start


	private void creatAnimation() {
		if (mIncomingcallCurrToRightAnim != null)
			return;
		incamingcallx = incomingcall.getX();
		mIncomingcallCurrToRightAnim = ObjectAnimator.ofFloat(incomingcall, "x", incamingcallx,
				incamingcallx + 500);
		mIncomingcallCurrToRightAnim.setDuration(530);
		mIncomingcallAlphaToZeroAnim = ObjectAnimator.ofFloat(incomingcall, "alpha", 1, 0);
		mIncomingcallAlphaToZeroAnim.setDuration(520);
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
				mInCallTouchUi.updateInCallUiButton();
				mInCallControls.setVisibility(View.VISIBLE);
				end_bottom_button.setVisibility(View.VISIBLE);
				mInCallControlsLeftToCurrAnim.start();
				mInCallControlsAlphaToOneAnim.start();
				mEndButtonLeftToCurrAnim.start();
				mEndButtonAlphaToOneAnim.start();

			}

		});

		mInCallControlsLeftToCurrAnim = ObjectAnimator.ofFloat(mInCallControls, "x", -400, 0);
		mInCallControlsLeftToCurrAnim.setDuration(200);
		mInCallControlsAlphaToOneAnim = ObjectAnimator.ofFloat(mInCallControls, "alpha", 0.5f, 1);
		mInCallControlsAlphaToOneAnim.setDuration(200);

		mEndButtonLeftToCurrAnim = ObjectAnimator.ofFloat(end_bottom_button, "x", -400, 0);
		mEndButtonLeftToCurrAnim.setDuration(250);
		mEndButtonAlphaToOneAnim = ObjectAnimator.ofFloat(end_bottom_button, "alpha", 0.5f, 1);
		mEndButtonAlphaToOneAnim.setDuration(250);
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
				mInCallScreen.handleOnscreenButtonClick(R.id.incomingCallAnswer);
				// mIncomingCallWidget.setCantouch(true);
				is_textslide_anim_start = false;
				Log.v("CallCard", "onAnimationEnd mEndButtonAlphaToOneAnim");
			}

		});
		now_time = 0;
	}

	//用户开始右滑
	void ManDongzuoStartAnimationRight() {
		if (!mInCallScreen.mCallCardAnimController.getCanStartAnim()) {
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
		Animation anim = android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.huadong);
		text_fuhao.startAnimation(anim);
		text.startAnim();
	}

	//来电UI开始消失
	void ManDongzuoAnimationRight(int per) {
		text_fuhao.clearAnimation();
		text.stopAnim();
		mIncomingcallCurrToRightAnim.setCurrentPlayTime(per);
		mIncomingcallAlphaToZeroAnim.setCurrentPlayTime(per);
		now_time = per;
		is_textslide_anim_start = true;
	}


	void OnHideShowInCallControl(boolean is_show, boolean is_anim) {
		if (is_anim) {
			if (!mInCallScreen.mCallCardAnimController.getCanStartAnim()) {
				return;
			}
			if (is_show) {
				is_incallcontrol_anim_start = true;
				AnimationSet anim = new AnimationSet(false);
				TranslateAnimation ta = new TranslateAnimation(0, 0,
						mInCallControls.getHeight(), 0);
				ta.setDuration(400);
				AlphaAnimation aa = new AlphaAnimation(0, 1);
				aa.setDuration(400);
				anim.addAnimation(ta);
				anim.addAnimation(aa);
				anim.setAnimationListener(new AuroraAnimationListener() {
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						mInCallControls.setVisibility(View.VISIBLE);// aurora add zhouxiaobing 20131008
						mInCallScreen.updateInCallTouchUi();
						is_incallcontrol_anim_start = false;
						mDialpadButton.setEnabled(true);
					}
				});
				mInCallControls.startAnimation(anim);
			} else {
				is_incallcontrol_anim_start = true;
				AnimationSet anim = new AnimationSet(false);
				TranslateAnimation ta = new TranslateAnimation(0, 0, 0,
						mInCallControls.getHeight());
				ta.setDuration(400);
				AlphaAnimation aa = new AlphaAnimation(1, 0);
				aa.setDuration(400);
				anim.addAnimation(ta);
				anim.addAnimation(aa);
				anim.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						mInCallControls.setVisibility(View.GONE);
						is_incallcontrol_anim_start = false;
						mDialpadButton.setEnabled(true);
					}
				});
				mInCallControls.startAnimation(anim);
			}
		} else {
			if (is_show)
				mInCallControls.setVisibility(View.VISIBLE);
			else
				mInCallControls.setVisibility(View.GONE);
		}

	}

	void OnShowHideKeypadButton(boolean is_show, boolean is_anim) {
		if (is_anim) {
			if (!mInCallScreen.mCallCardAnimController.getCanStartAnim()) {
				return;
			}
			if (is_show) {
				ScaleAnimation anim = new ScaleAnimation(1, 0.5f, 1, 1, 0,
						mEndButton.getHeight() / 2);
				anim.setDuration(300);
				TranslateAnimation taa = new TranslateAnimation(0,
						-button_movedistance, 0, 0);
				taa.setDuration(300);
				AnimationSet aass = new AnimationSet(false);
				aass.addAnimation(anim);
				aass.addAnimation(taa);
				// anim.setFillAfter(true);
				mEndButton.startAnimation(aass);
				TranslateAnimation ta = new TranslateAnimation(0,
						-mEndButton.getWidth() / 4 - button_movedistance, 0, 0);
				ta.setDuration(300);

				// ta.setFillAfter(true);
				endcall_text.startAnimation(ta);
				AnimationSet ta2 = new AnimationSet(false);
				ScaleAnimation bsa = new ScaleAnimation(0.1f, 1, 1, 1,
						hide_keypad.getWidth(), hide_keypad.getHeight() / 2);
				bsa.setDuration(300);
				AlphaAnimation baa = new AlphaAnimation(0, 1);
				baa.setDuration(300);
				ta2.addAnimation(bsa);
				ta2.addAnimation(baa);
				hide_keypad.startAnimation(ta2);
				hide_keypad_text.startAnimation(ta2);
				ta2.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						is_end_call_keypad_anim_start = true;
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						mEndButton.setVisibility(View.INVISIBLE);
						endcall_text.setVisibility(View.INVISIBLE);
						endcall_keypad.setVisibility(View.VISIBLE);
						endcall_keypad_text.setVisibility(View.VISIBLE);
						hide_keypad.setVisibility(View.VISIBLE);
						hide_keypad_text.setVisibility(View.VISIBLE);
						is_end_call_keypad_anim_start = false;
						hide_keypad.setEnabled(true);
					}
				});
			} else {
				ScaleAnimation anim = new ScaleAnimation(1, 2, 1, 1, 0,
						mEndButton.getHeight() / 2);
				anim.setDuration(300);
				TranslateAnimation taa = new TranslateAnimation(0,
						button_movedistance, 0, 0);
				taa.setDuration(300);
				AnimationSet aass = new AnimationSet(false);
				aass.addAnimation(anim);
				aass.addAnimation(taa);
				// anim.setFillAfter(true);
				endcall_keypad.startAnimation(aass);
				TranslateAnimation ta = new TranslateAnimation(0,
						mEndButton.getWidth() / 4 + button_movedistance, 0, 0);
				ta.setDuration(300);
				// ta.setFillAfter(true);
				endcall_keypad_text.startAnimation(ta);

				AnimationSet ta2 = new AnimationSet(false);
				ScaleAnimation bsa = new ScaleAnimation(1, 0.1f, 1, 1,
						hide_keypad.getWidth(), hide_keypad.getHeight() / 2);
				bsa.setDuration(300);
				AlphaAnimation baa = new AlphaAnimation(1, 0);
				baa.setDuration(300);
				ta2.addAnimation(bsa);
				ta2.addAnimation(baa);
				hide_keypad.startAnimation(ta2);
				hide_keypad_text.startAnimation(ta2);
				ta2.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						is_end_call_keypad_anim_start = true;
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						mEndButton.setVisibility(View.VISIBLE);
						endcall_text.setVisibility(View.VISIBLE);
						endcall_keypad.setVisibility(View.INVISIBLE);
						endcall_keypad_text.setVisibility(View.INVISIBLE);
						hide_keypad.setVisibility(View.INVISIBLE);
						hide_keypad_text.setVisibility(View.INVISIBLE);
						hide_keypad.setEnabled(true);
						is_end_call_keypad_anim_start = false;
					}
				});
			}
		} else {
			if (is_show) {
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
		mInCallScreen.getRespondViaSmsManager().dismissPopup();
		is_incoming_is_jie = false;
		setEndButtonShowOrHide(true);
		mDialpadButton.setEnabled(true);
		hide_keypad.setEnabled(true);
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
		// aurora add zhouxiaobing 20140103 start
		CallManager cm = PhoneGlobals.getInstance().mCM;
		PhoneConstants.State state = cm.getState();
		final Call ringingCall = cm.getFirstActiveRingingCall();
		final Call.State fgCallState = cm.getActiveFgCallState();
		if (state == PhoneConstants.State.RINGING && !fgCallState.isAlive()) {
			if (mInCallScreen.isQuickResponseDialogShowing()
					|| ringingCall.getState() == Call.State.DISCONNECTING
					|| ringingCall.getState() == Call.State.DISCONNECTED) {
			} else {
				groundview.setBackgroundId(R.drawable.bg);

			}
			if (!mInCallScreen.isQuickResponseDialogShowing()) {
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
			groundview.setBackgroundId(R.drawable.bg);
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
				mInCallScreen, R.anim.huadong);
		text_fuhao.startAnimation(anim);
		text.startAnim();
	}

	void stopSlideTextAnim() {
		text_fuhao.clearAnimation();
		text.stopAnim();
	}
	
    private GlowPadView mOriginIncomingCallWidget;
    private ViewGroup mButtonLine1;
    private ViewGroup mButtonLine2;
 
	
	void ManDongzuoStartAnimationRightV2() {
		if (!mInCallScreen.mCallCardAnimController.getCanStartAnim()) {
			return;
		}
		is_incoming_is_jie = true;					
		mOriginIncomingCallWidget.setVisibility(View.GONE);
	
		mInCallTouchUi.updateInCallUiButton();
		mInCallControls.setVisibility(View.VISIBLE);
		mButtonLine2.setVisibility(View.GONE);
		mButtonLine1.startLayoutAnimation();
		mHandler.postDelayed(new Runnable(){
			public void run() {
				mButtonLine2.setVisibility(View.VISIBLE);
					mButtonLine2.startLayoutAnimation();
			}
		}, 150);
		
		mHandler.postDelayed(new Runnable(){
			public void run() {
				end_bottom_button.setVisibility(View.VISIBLE);
				end_bottom_button.startLayoutAnimation();
			}
		}, 300);

		

	
	}
}
package com.android.phone;

import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.Context;

import com.android.internal.telephony.Call;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.CallStateException;
import com.android.internal.telephony.Connection;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyCapabilities;
import com.android.phone.AuroraCallerInfo;
import com.android.phone.AuroraPhoneUtils;
import com.android.phone.CallTime;
import com.android.phone.InCallScreen;
import com.android.phone.PhoneGlobals;
import com.android.phone.SimIconUtils;

import android.widget.LinearLayout.LayoutParams;

import java.lang.reflect.*;
import java.util.ArrayList;

import javax.crypto.NullCipher;

import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.GestureDetector.OnGestureListener;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.widget.CompoundButton;
import android.provider.ContactsContract.Contacts;
import android.content.ContentUris;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.view.animation.*;
import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;

public class AuroraFloatView extends LinearLayout implements
		View.OnClickListener,
		ContactsAsyncHelper.OnImageLoadCompleteListener,
		CallTime.OnTickListener, OnGestureListener {

	private static final boolean DBG = true;
	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int viewHeight;

	/**
	 * 记录系统状态栏的高度
	 */
	private static int statusBarHeight;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	/**
	 * 小悬浮窗的参数
	 */
	private WindowManager.LayoutParams mParams;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;

	private TextView mName, mArea, mCheat, mTime;
	private ImageButton mHangup, mAnswer, mAnswerSpeaker;
	private phoneCompoundButton mMute, mSpeaker;
	private View mButtonsInCall, mButtonsRinging;
	private ImageView mPhoto;
	private CallManager mCM;
	private GestureDetector gDetector;
	private static final String LOG_TAG = "AuroraFloatView";
	private Context mContext;
	private View mBg, mLeftBg, mRightBg;
	private PopbackgroundView mBgContainer;
	private View mMain, mAction, mPhotoContainer;
	private View mView;

	public AuroraFloatView(Context context) {
		super(context);
		mContext = context;
		mCM = PhoneGlobals.getInstance().mCM;
		gDetector = new GestureDetector(this);
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.incoming_pop, this);
		View view = findViewById(R.id.parent_container);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		mName = (TextView) findViewById(R.id.name);
		mArea = (TextView) findViewById(R.id.area);
		mCheat = (TextView) findViewById(R.id.image_name);
		mTime = (TextView) findViewById(R.id.time);
		mHangup = (ImageButton) findViewById(R.id.hangup);
		mHangup.setOnClickListener(this);
		mAnswer = (ImageButton) findViewById(R.id.answer);
		mAnswer.setOnClickListener(this);
		mSpeaker = (phoneCompoundButton) findViewById(R.id.speaker);
		mSpeaker.setOnClickListener(this);
		mMute = (phoneCompoundButton) findViewById(R.id.mute);
		mMute.setOnClickListener(this);
		mAnswerSpeaker = (ImageButton) findViewById(R.id.answer_speaker);
		mAnswerSpeaker.setOnClickListener(this);

		mButtonsInCall = findViewById(R.id.incall_action_area);
		mButtonsRinging = findViewById(R.id.ringing_action_area);

		mPhoto = (ImageView) findViewById(R.id.photo_image);
		// mBg = findViewById(R.id.pop_bg);
		// mLeftBg = findViewById(R.id.pop_left_bg);
		// mRightBg = findViewById(R.id.pop_right_bg);
		mBgContainer = (PopbackgroundView) findViewById(R.id.incoming_pop_bg);
		mAction = findViewById(R.id.action_area);
		mMain = findViewById(R.id.main_content);
		mMain.setOnClickListener(this);
		mPhotoContainer = findViewById(R.id.image_container);

		mView = findViewById(R.id.incoming_pop);
		mDensity = context.getResources().getDisplayMetrics().density;
		mCallTime = new CallTime(this);
		updateUI();		
//		mView.setOnClickListener(this);
		this.setOnKeyListener(null);
        this.setOnTouchListener(new OnTouchListener() {  
            
            @Override  
            public boolean onTouch(View v, MotionEvent event) {  
                return gDetector.onTouchEvent(event);  
            }  
        });  
	}


	public void onClick(View view) {
		int id = view.getId();
		log("onClick(View " + view + ", id " + id + ")...");

		boolean isOpenSpeaker = false;

		switch (id) {
		case R.id.hangup:
			HideAndEndAnimationV2();	
			break;
		case R.id.answer_speaker:
			isOpenSpeaker = true;
		case R.id.answer:
			// internalAnswerCall();
			animAnswer();
			if (!isOpenSpeaker) {
				break;
			}
		case R.id.speaker:
			if(isOpenSpeaker) {
				mHandler.postDelayed(new Runnable() {
					public void run() {
						toggleSpeaker();
						updateUI();
					}
				},  (int) (600 * PhoneGlobals.mAnimFactor));
			} else {
				toggleSpeaker();
			}


			break;
		case R.id.main_content:
			PhoneGlobals.getInstance().displayCallScreen();
			FloatWindowManager.removeWindowFinal(PhoneGlobals.getInstance());
			break;
		case R.id.mute:
			boolean newMuteState = !PhoneUtils.getMute();
			PhoneUtils.setMute(newMuteState);
			break;
		}
		if(id != R.id.hangup) {
			updateUI();
		}
	}

	public void toggleSpeaker() {
		boolean newSpeakerState = !PhoneUtils.isSpeakerOn(PhoneGlobals
				.getInstance());
		PhoneUtils.turnOnSpeaker(PhoneGlobals.getInstance(), newSpeakerState,
				true);
	}

	private void internalAnswerCall() {
			log("internalAnswerCall()...");

		final boolean hasRingingCall = mCM.hasActiveRingingCall();

		if (hasRingingCall) {
			Phone phone = mCM.getRingingPhone();
			if (DBG)
				log(" Ringing Phone" + phone);
			Call ringing = mCM.getFirstActiveRingingCall();
			int phoneType = phone.getPhoneType();
			if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
				if (DBG)
					log("internalAnswerCall: answering (CDMA)...");
				if (mCM.hasActiveFgCall()
						&& mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_SIP) {
					// The incoming call is CDMA call and the ongoing
					// call is a SIP call. The CDMA network does not
					// support holding an active call, so there's no
					// way to swap between a CDMA call and a SIP call.
					// So for now, we just don't allow a CDMA call and
					// a SIP call to be active at the same time.We'll
					// "answer incoming, end ongoing" in this case.
					if (DBG)
						log("internalAnswerCall: answer "
								+ "CDMA incoming and end SIP ongoing");
					PhoneUtils.answerAndEndActive(mCM, ringing);
				} else {
					PhoneUtils.answerCall(ringing);
				}
			} else if (phoneType == PhoneConstants.PHONE_TYPE_SIP) {
				if (DBG)
					log("internalAnswerCall: answering (SIP)...");
				if (mCM.hasActiveFgCall()
						&& mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA) {
					// Similar to the PHONE_TYPE_CDMA handling.
					// The incoming call is SIP call and the ongoing
					// call is a CDMA call. The CDMA network does not
					// support holding an active call, so there's no
					// way to swap between a CDMA call and a SIP call.
					// So for now, we just don't allow a CDMA call and
					// a SIP call to be active at the same time.We'll
					// "answer incoming, end ongoing" in this case.
					if (DBG)
						log("internalAnswerCall: answer "
								+ "SIP incoming and end CDMA ongoing");
					PhoneUtils.answerAndEndActive(mCM, ringing);
				} else {
					PhoneUtils.answerCall(ringing);
				}
			} else if (phoneType == PhoneConstants.PHONE_TYPE_GSM || phoneType == PhoneConstants.PHONE_TYPE_IMS) {
				if (DBG)
					log("internalAnswerCall: answering (GSM)...");
				// GSM: this is usually just a wrapper around
				// PhoneUtils.answerCall(), *but* we also need to do
				// something special for the "both lines in use" case.

				final boolean hasActiveCall = mCM.hasActiveFgCall();
				final boolean hasHoldingCall = mCM.hasActiveBgCall();

				if (hasActiveCall && hasHoldingCall) {
					if (DBG)
						log("internalAnswerCall: answering (both lines in use!)...");
					// The relatively rare case where both lines are
					// already in use. We "answer incoming, end ongoing"
					// in this case, according to the current UI spec.
					// PhoneUtils.answerAndEndActive(mCM, ringing);
					PhoneUtils.answerAndEndHolding(mCM, ringing);

					// Alternatively, we could use
					// PhoneUtils.answerAndEndHolding(mPhone);
					// here to end the on-hold call instead.
				} else {
					if (DBG)
						log("internalAnswerCall: answering...");
					PhoneUtils.answerCall(ringing); // Automatically holds the
													// current active call,
													// if there is one
				}
			} else {
				throw new IllegalStateException("Unexpected phone type: "
						+ phoneType);
			}

			// Call origin is valid only with outgoing calls. Disable it on
			// incoming calls.
			PhoneGlobals.getInstance().setLatestActiveCallOrigin(null);
		}
	}

	private void log(String msg) {
		Log.d(LOG_TAG, msg);
	}

	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		mParams.x = (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		windowManager.updateViewLayout(this, mParams);
	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

//	@Override
//	public boolean onDown(MotionEvent e) {
//		// TODO Auto-generated method stub
//		return false;
//	}
//
//	@Override
//	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//			float velocityY) {
//		// TODO Auto-generated method stub
//		Log.v("SlideView", "onFling");
//
//		float y1 = e1.getY();
//		float y2 = e2.getY();
//
////		if (Math.abs(y1 - y2) > 100) {
////			FloatWindowManager.removeWindow(PhoneGlobals.getInstance());
////			return true;
////		}
//		return false;
//	}
//
//	@Override
//	public void onLongPress(MotionEvent e) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
//			float distanceY) {
//		return false;
//	}
//
//	@Override
//	public void onShowPress(MotionEvent e) {
//		// TODO Auto-generated method stub
//	}
//
//	@Override
//	public boolean onSingleTapUp(MotionEvent e) {
//		// TODO Auto-generated method stub
//		return false;
//	}

	void updateUI() {
		log("updateUI");
		if(isAnswerAniming || isEndAnim) {
			return;
		}
		Call call;
		if (mCM.getState() == PhoneConstants.State.OFFHOOK) {
			log("updateUI OFFHOOK");
			mButtonsInCall.setVisibility(View.VISIBLE);
			mButtonsRinging.setVisibility(View.GONE);
			mSpeaker.setChecked(PhoneUtils.isSpeakerOn(PhoneGlobals
					.getInstance()));
			mMute.setChecked(PhoneUtils.getMute());
			call = mCM.getActiveFgCall();
		} else {
			log("updateUI ringing");
			mButtonsInCall.setVisibility(View.GONE);
			mButtonsRinging.setVisibility(View.VISIBLE);
			mButtonsRinging.setAlpha(1);
			call = mCM.getFirstActiveRingingCall();
		}	
		AuroraCallerInfo info = AuroraPhoneUtils.getAuroraCallerInfo(call);
		if (info != null) {
			if (!TextUtils.isEmpty(info.name)) {
				mName.setText(info.name);
				if(!TextUtils .isEmpty(info.mArea) && !info.mArea.equalsIgnoreCase("null")) {
					mArea.setText(info.phoneNumber + " " + info.mArea);
				} else {
					mArea.setText(info.phoneNumber);
				}
			} else {
				mName.setText(info.phoneNumber);
				mArea.setText(info.mArea);
			}
			
	     	Drawable simicon = getResources().getDrawable(SimIconUtils.getIncomingSimIcon(call.getPhone().getPhoneId()));	     	
	     	int w= simicon.getIntrinsicWidth();
	     	int h= simicon.getIntrinsicHeight();
	     	simicon.setBounds(0, 0, w, h);
	     	if(AuroraPhoneUtils.isShowDoubleButton()) {
		     	mArea.setCompoundDrawables(simicon, null, null, null);
		     	mTime.setCompoundDrawables(simicon, null, null, null);
	     	} else {
	     	  	mArea.setCompoundDrawables(null, null, null, null);
		     	mTime.setCompoundDrawables(null, null, null, null);
	     	}
        	
			mArea.setAlpha(1);
			mName.setAlpha(1);
			Bitmap photo = info.cachedPhotoIcon;
			log("updateUI startObtainPhotoAsync info.isCachedPhotoCurrent  = " + info.isCachedPhotoCurrent  + "  info.person_id = " +  info.person_id);
			if (photo != null) {
				log("updateUI cachedPhotoIcon");
				photo = RoundBitmapUtils.toRoundBitmap(photo);
				mPhoto.setImageBitmap(photo);
			} else if (!info.isCachedPhotoCurrent && info.person_id > 0) {
				ContactsAsyncHelper.startObtainPhotoAsync(0, mContext,
						ContentUris.withAppendedId(Contacts.CONTENT_URI,
								info.person_id), this, info);
			} else {
				log("updateUI default photo");
				photo = BitmapFactory.decodeResource(mContext.getResources(),
						R.drawable.photo_default);
				photo = RoundBitmapUtils.toRoundBitmap(photo);
				mPhoto.setImageBitmap(photo);
			}

//			if (!TextUtils.isEmpty(info.mMark) && info.mMark.contains("诈骗")) {
//				mCheat.setVisibility(View.VISIBLE);
//			} else {
//				mCheat.setVisibility(View.GONE);
//			}
		}

		updateCallTime();
		log("updateUI end");
	}

	@Override
	public void onImageLoadComplete(int token, Drawable photo,
			Bitmap photoIcon, Object cookie) {
		log("onImageLoadComplete");
		AuroraCallerInfo callerInfo = (AuroraCallerInfo) cookie;
		callerInfo.cachedPhoto = photo;
		callerInfo.cachedPhotoIcon = photoIcon;
		callerInfo.isCachedPhotoCurrent = true;
		if(photoIcon != null) { 
			Bitmap p = RoundBitmapUtils.toRoundBitmap(photoIcon);
			mPhoto.setImageBitmap(p);
		}
	}

	public void showAnimation() {
		int duration = 200 * PhoneGlobals.mAnimFactor;

		mAction.setVisibility(View.INVISIBLE);
		mHangup.setVisibility(View.INVISIBLE);
		mMain.setVisibility(View.INVISIBLE);
	    mBgContainer.setVisibility(View.VISIBLE);
		
		ArrayList<PropertyValuesHolder> photoProps = new ArrayList<PropertyValuesHolder>(
				3);
		photoProps.add(PropertyValuesHolder.ofFloat("ScaleX", 0, 1));
		photoProps.add(PropertyValuesHolder.ofFloat("ScaleY", 0, 1));
		photoProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 0.45f));
		ObjectAnimator mPhotoAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mPhotoContainer,
				photoProps.toArray(new PropertyValuesHolder[3]));
		mPhotoAnimation.setDuration(duration);
		mPhotoAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mPhotoContainer.invalidate();
			}
		});
		mPhotoAnimation.setInterpolator(mShowInterpolator);
		mPhotoAnimation.start();
		
		ArrayList<PropertyValuesHolder> bgProps = new ArrayList<PropertyValuesHolder>(
				3);
		bgProps.add(PropertyValuesHolder.ofFloat("ScaleX", 0, 1));
		bgProps.add(PropertyValuesHolder.ofFloat("ScaleY", 0, 1));
		bgProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 0.45f));
		ObjectAnimator mBgAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mBgContainer,
				bgProps.toArray(new PropertyValuesHolder[3]));
		mBgAnimation.setDuration(duration);
		mBgAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mBgContainer.invalidate();
			}
		});
		mBgAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				showAnimationStep2();
			}
		});
		mBgAnimation.setInterpolator(mShowInterpolator);
		mBgAnimation.start();

	}
	
	public void showAnimationV2() {
		int duration = 500 * PhoneGlobals.mAnimFactor;
		
		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				2);
		props.add(PropertyValuesHolder.ofFloat("TranslationY", -56*mDensity, 0));
		props.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
		ObjectAnimator mShow = ObjectAnimator.ofPropertyValuesHolder(
				this,
				props.toArray(new PropertyValuesHolder[2]));
		mShow.setDuration(duration);
		mShow.setInterpolator(mShowInterpolator);
		mShow.start();		
	}

	public void showAnimationStep2() {

		final int duration = 500 * PhoneGlobals.mAnimFactor;
		final int delay = 0;

		final int left = mView.getLeft();
		final int right = mView.getRight();

		int move = (int) ((right - left) / 2 - 24 * mDensity);

		ArrayList<PropertyValuesHolder> photoProps = new ArrayList<PropertyValuesHolder>(
				2);
		photoProps.add(PropertyValuesHolder.ofFloat("TranslationX", move, 0));
		photoProps.add(PropertyValuesHolder.ofFloat("alpha", 0.45f, 1));
		ObjectAnimator mPhotoAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mPhotoContainer,
				photoProps.toArray(new PropertyValuesHolder[2]));
		mPhotoAnimation.setDuration(duration);
		mPhotoAnimation.setStartDelay(delay);
		mPhotoAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mPhotoContainer.invalidate();
			}
		});
		mPhotoAnimation.setInterpolator(mShowInterpolator);
		mPhotoAnimation.start();

		mHandler.postDelayed(mShowRunnable, 400 * PhoneGlobals.mAnimFactor);

		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				2);
		props.add(PropertyValuesHolder.ofFloat("ww", 0.0f, 272 * mDensity));
		props.add(PropertyValuesHolder.ofFloat("alpha", 0.45f, 1));
		ObjectAnimator mWidthAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mBgContainer, props.toArray(new PropertyValuesHolder[2]));
		mWidthAnimation.setDuration(duration);
		mWidthAnimation.setStartDelay(delay);
		mWidthAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mBgContainer.invalidate();
			}
		});
		mWidthAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				 mView.setBackgroundResource(R.drawable.pop_bg_ripple);
				mBgContainer.setVisibility(View.GONE);
			}
		});
		mWidthAnimation.setInterpolator(mShowInterpolator);
		mWidthAnimation.start();
	}

	private Handler mHandler = new Handler();

	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (!mIsFirstShow) {
			mIsFirstShow = true;
//			mBgContainer.setCenterX((mView.getRight() - mView.getLeft()) / 2);
//			mBgContainer.setCenterY((mView.getBottom() - mView.getTop()) / 2);
//			final int photoleft = mPhotoContainer.getLeft();
//			final int photoright = mPhotoContainer.getRight();
//			mPhotoContainer.setTranslationX((mView.getRight()  - mView.getLeft()) / 2
//					- (photoright - photoleft) / 2 - 4 * mDensity);
//			showAnimation();
			showAnimationV2();
		}
	}

	private Runnable mShowRunnable = new Runnable() {
		public void run() {
			mAction.setVisibility(View.VISIBLE);
			mHangup.setVisibility(View.VISIBLE);
			mMain.setVisibility(View.VISIBLE);
			final int duration = 400 * PhoneGlobals.mAnimFactor;

			ArrayList<PropertyValuesHolder> mainProps = new ArrayList<PropertyValuesHolder>(
					2);
			mainProps.add(PropertyValuesHolder.ofFloat("TranslationX",
					6 * mDensity, 0));
			mainProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
			ObjectAnimator mMainAnimation = ObjectAnimator
					.ofPropertyValuesHolder(mMain,
							mainProps.toArray(new PropertyValuesHolder[2]));
			mMainAnimation.setDuration(duration);
			mMainAnimation.addUpdateListener(new AnimatorUpdateListener() {
				public void onAnimationUpdate(ValueAnimator animation) {
					mMain.invalidate();
				}
			});
			mMainAnimation.setInterpolator(mShowInterpolator);
			mMainAnimation.start();

			ArrayList<PropertyValuesHolder> actionProps = new ArrayList<PropertyValuesHolder>(
					2);
			actionProps.add(PropertyValuesHolder.ofFloat("TranslationX", -18
					* mDensity, 0));
			actionProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
			ObjectAnimator mActionAnimation = ObjectAnimator
					.ofPropertyValuesHolder(mAction,
							actionProps.toArray(new PropertyValuesHolder[2]));
			mActionAnimation.setDuration(duration);
			mActionAnimation.addUpdateListener(new AnimatorUpdateListener() {
				public void onAnimationUpdate(ValueAnimator animation) {
					mAction.invalidate();
				}
			});
			mActionAnimation.setInterpolator(mShowInterpolator);
			mActionAnimation.start();

			ArrayList<PropertyValuesHolder> hangupProps = new ArrayList<PropertyValuesHolder>(
					2);
			hangupProps.add(PropertyValuesHolder.ofFloat("TranslationX", -18
					* mDensity, 0));
			hangupProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
			ObjectAnimator mHangupAnimation = ObjectAnimator
					.ofPropertyValuesHolder(mHangup,
							hangupProps.toArray(new PropertyValuesHolder[2]));
			mHangupAnimation.setDuration(duration);
			mHangupAnimation.addUpdateListener(new AnimatorUpdateListener() {
				public void onAnimationUpdate(ValueAnimator animation) {
					mHangup.invalidate();
				}
			});
			mHangupAnimation.setInterpolator(mShowInterpolator);
			mHangupAnimation.start();
		}
	};
	
	private boolean isEndAnim = false;
	public void HideAnimation() {
		isEndAnim = true;		
		
		final int duration = 200 * PhoneGlobals.mAnimFactor;
		
		mBgContainer.setVisibility(View.VISIBLE);
		 mView.setBackground(null);

		ArrayList<PropertyValuesHolder> mainProps = new ArrayList<PropertyValuesHolder>(
				2);
		mainProps.add(PropertyValuesHolder.ofFloat("TranslationX",
				0, 6 * mDensity));
		mainProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator mMainAnimation = ObjectAnimator
				.ofPropertyValuesHolder(mMain,
						mainProps.toArray(new PropertyValuesHolder[2]));
		mMainAnimation.setDuration(duration);
		mMainAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mMain.invalidate();
			}
		});
		mMainAnimation.setInterpolator(easeInterpolator);
		mMainAnimation.start();

		ArrayList<PropertyValuesHolder> actionProps = new ArrayList<PropertyValuesHolder>(
				2);
		actionProps.add(PropertyValuesHolder.ofFloat("TranslationX", 0, -18
				* mDensity));
		actionProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator mActionAnimation = ObjectAnimator
				.ofPropertyValuesHolder(mAction,
						actionProps.toArray(new PropertyValuesHolder[2]));
		mActionAnimation.setDuration(duration);
		mActionAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mAction.invalidate();
			}
		});
		mActionAnimation.setInterpolator(easeInterpolator);
		mActionAnimation.start();

		ArrayList<PropertyValuesHolder> hangupProps = new ArrayList<PropertyValuesHolder>(
				2);
		hangupProps.add(PropertyValuesHolder.ofFloat("TranslationX", 0, -18
				* mDensity));
		hangupProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator mHangupAnimation = ObjectAnimator
				.ofPropertyValuesHolder(mHangup,
						hangupProps.toArray(new PropertyValuesHolder[2]));
		mHangupAnimation.setDuration(duration);
		mHangupAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mHangup.invalidate();
			}
		});
		mHangupAnimation.setInterpolator(easeInterpolator);
		mHangupAnimation.start();
	
		
//		final int delay = 50* PhoneGlobals.mAnimFactor;
		final int delay = 0;
		final int left = mView.getLeft();
		final int right = mView.getRight();

		int move = (int) ((right - left) / 2 - 24 * mDensity);

		ArrayList<PropertyValuesHolder> photoProps = new ArrayList<PropertyValuesHolder>(
				2);
		photoProps.add(PropertyValuesHolder.ofFloat("TranslationX", 0, move));
		photoProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0.45f));
		ObjectAnimator mPhotoAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mPhotoContainer,
				photoProps.toArray(new PropertyValuesHolder[2]));
		mPhotoAnimation.setDuration(duration);
		mPhotoAnimation.setStartDelay(delay);
		mPhotoAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mPhotoContainer.invalidate();
			}
		});
		mPhotoAnimation.setInterpolator(easeInterpolator);
		mPhotoAnimation.start();

		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				2);
		props.add(PropertyValuesHolder.ofFloat("ww", 272 * mDensity, 0.0f));
		props.add(PropertyValuesHolder.ofFloat("alpha", 1, 0.45f));
		ObjectAnimator mWidthAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mBgContainer, props.toArray(new PropertyValuesHolder[2]));
		mWidthAnimation.setDuration(duration);
		mWidthAnimation.setStartDelay(delay);
		mWidthAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mBgContainer.invalidate();
			}
		});
		mWidthAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				hideAnimationStep2();
			}
		});
		mWidthAnimation.setInterpolator(easeInterpolator);
		mWidthAnimation.start();
	
	}
	
	public void HideAnimationV2() {
		isEndAnim = true;		
		int duration = 200 * PhoneGlobals.mAnimFactor;
		
		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				2);
		props.add(PropertyValuesHolder.ofFloat("TranslationY", 0,  - 56*mDensity));
		props.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator mHide = ObjectAnimator.ofPropertyValuesHolder(
				this,
				props.toArray(new PropertyValuesHolder[2]));
		mHide.setDuration(duration);
		mHide.setInterpolator(mHideInterpolator);
		mHide.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				FloatWindowManager.removeWindowFinal(PhoneGlobals.getInstance());
				isEndAnim = false;
			}
		});
		mHide.start();		
	}
	
	public void HideAndEndAnimationV2() {
		 final Call ringingCall = mCM.getFirstActiveRingingCall();
		 if (ringingCall.getState() == Call.State.WAITING || mCM.hasActiveBgCall()) {
			  PhoneUtils.hangup(mCM);
			  return;
		}
		isEndAnim = true;		
		int duration = 200 * PhoneGlobals.mAnimFactor;
		
		ArrayList<PropertyValuesHolder> props = new ArrayList<PropertyValuesHolder>(
				2);
		props.add(PropertyValuesHolder.ofFloat("TranslationY", 0,  - 56*mDensity));
		props.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator mHide = ObjectAnimator.ofPropertyValuesHolder(
				this,
				props.toArray(new PropertyValuesHolder[2]));
		mHide.setDuration(duration);
		mHide.setInterpolator(mHideInterpolator);
		mHide.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {	
				FloatWindowManager.removeWindowFinal(PhoneGlobals.getInstance());
				PhoneUtils.hangup(mCM);
				isEndAnim = false;
			}
		});
		mHide.start();		
	}
	
	private void hideAnimationStep2() {

		int duration = 150 * PhoneGlobals.mAnimFactor;
//		final int delay = 200* PhoneGlobals.mAnimFactor;
		final int delay = 0;
		
		
		ArrayList<PropertyValuesHolder> photoProps = new ArrayList<PropertyValuesHolder>(
				3);
		photoProps.add(PropertyValuesHolder.ofFloat("ScaleX", 1, 0));
		photoProps.add(PropertyValuesHolder.ofFloat("ScaleY", 1, 0));
		photoProps.add(PropertyValuesHolder.ofFloat("alpha", 0.45f, 0));
		ObjectAnimator mPhotoAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mPhotoContainer,
				photoProps.toArray(new PropertyValuesHolder[3]));
		mPhotoAnimation.setDuration(duration);
		mPhotoAnimation.setStartDelay(delay);
		mPhotoAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mPhotoContainer.invalidate();
			}
		});
		mPhotoAnimation.setInterpolator(easeOutInterpolator);
		mPhotoAnimation.start();
		
		ArrayList<PropertyValuesHolder> bgProps = new ArrayList<PropertyValuesHolder>(
				3);
		bgProps.add(PropertyValuesHolder.ofFloat("ScaleX", 1, 0));
		bgProps.add(PropertyValuesHolder.ofFloat("ScaleY", 1, 0));
		bgProps.add(PropertyValuesHolder.ofFloat("alpha", 0.45f, 0));
		ObjectAnimator mBgAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mBgContainer,
				bgProps.toArray(new PropertyValuesHolder[3]));
		mBgAnimation.setDuration(duration);
		mBgAnimation.setStartDelay(delay);
		mBgAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mBgContainer.invalidate();
			}
		});
		mBgAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
				FloatWindowManager.removeWindowFinal(PhoneGlobals.getInstance());
			}
		});
		mBgAnimation.setInterpolator(easeOutInterpolator);
		mBgAnimation.start();
	
	}

	private boolean isAnswerAniming = false;
	private void animAnswer() {
		log("animAnswer");
		isAnswerAniming = true;
		final int duration = 200 * PhoneGlobals.mAnimFactor;

		ArrayList<PropertyValuesHolder> areaProps = new ArrayList<PropertyValuesHolder>(
				1);
		areaProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator areaAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mArea, areaProps.toArray(new PropertyValuesHolder[1]));
		areaAnimation.setDuration(duration);
		areaAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mArea.invalidate();
			}
		});
		areaAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {
//				mArea.setVisibility(View.GONE);
//				mArea.setAlpha(1);
			}
		});
		areaAnimation.setInterpolator(easeInterpolator);
		areaAnimation.start();

		mTime.setVisibility(View.VISIBLE);
		mTime.setAlpha(0);
		ArrayList<PropertyValuesHolder> timeProps = new ArrayList<PropertyValuesHolder>(
				2);
		timeProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
		timeProps.add(PropertyValuesHolder.ofFloat("TranslationX",
				8 * mDensity, 0));
		ObjectAnimator timeAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mTime, timeProps.toArray(new PropertyValuesHolder[2]));
		timeAnimation.setDuration(duration);
		timeAnimation.setStartDelay(280 * PhoneGlobals.mAnimFactor);
		timeAnimation.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animation) {
				mTime.invalidate();
			}
		});
		timeAnimation.setInterpolator(easeInterpolator);
		timeAnimation.start();
		
		mButtonsInCall.setVisibility(View.VISIBLE);
		mButtonsInCall.setAlpha(0);
		ArrayList<PropertyValuesHolder> actionInCallProps = new ArrayList<PropertyValuesHolder>(
				1);
		actionInCallProps.add(PropertyValuesHolder.ofFloat("alpha", 0, 1));
		ObjectAnimator actionIncallAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mButtonsInCall, actionInCallProps.toArray(new PropertyValuesHolder[1]));
		actionIncallAnimation.setDuration(duration);
//		actionIncallAnimation.addUpdateListener(new AnimatorUpdateListener() {
//			public void onAnimationUpdate(ValueAnimator animation) {
//				mButtonsInCall.invalidate();
//			}
//		});
		actionIncallAnimation.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animator) {		
				mButtonsRinging.setVisibility(View.GONE);
				log("animAnswer end");
				isAnswerAniming = false;
				internalAnswerCall();
			}
		});
		timeAnimation.setStartDelay(200 * PhoneGlobals.mAnimFactor);
		actionIncallAnimation.setInterpolator(easeInterpolator);
		actionIncallAnimation.start();
		
		mButtonsRinging.setVisibility(View.VISIBLE);
		ArrayList<PropertyValuesHolder> actionRingProps = new ArrayList<PropertyValuesHolder>(
				1);
		actionRingProps.add(PropertyValuesHolder.ofFloat("alpha", 1, 0));
		ObjectAnimator actionRingAnimation = ObjectAnimator.ofPropertyValuesHolder(
				mButtonsRinging, actionRingProps.toArray(new PropertyValuesHolder[1]));
		actionRingAnimation.setDuration(duration);
//		actionRingAnimation.addUpdateListener(new AnimatorUpdateListener() {
//			public void onAnimationUpdate(ValueAnimator animation) {
//				mButtonsRinging.invalidate();
//			}
//		});
		actionRingAnimation.setInterpolator(easeInterpolator);
		actionRingAnimation.start();
	}

	private CallTime mCallTime;

	private void updateCallTime() {
		Call call = mCM.getActiveFgCall();
		if (mCM.getState() == PhoneConstants.State.RINGING) {
			call = mCM.getFirstActiveRingingCall();
			mTime.setVisibility(View.GONE);
			mArea.setVisibility(View.VISIBLE);
		} else {
			mTime.setVisibility(View.VISIBLE);
			mArea.setVisibility(View.GONE);
		}

		if (call == null) {
			return;
		}
		Call.State state = call.getState();
		if (DBG)
			log("  - call.state: " + call.getState());

		switch (state) {
		case ACTIVE:
		case DISCONNECTING:
			// update timer field
			if (DBG)
				log("displayMainCallStatus: start periodicUpdateTimer");
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
			if (mCM.hasActiveFgCall()) {
				mCallTime.reset();
				mCallTime.periodicUpdateTimer();
			} else {
				mCallTime.cancelTimer();
			}

			break;

		case IDLE:

			break;

		default:
			break;
		}
	}

	@Override
	public void onTickForCallTimeElapsed(long timeElapsed) {
		updateElapsedTimeWidget(timeElapsed);
	}

	private void updateElapsedTimeWidget(long timeElapsed) {
//		log("updateElapsedTimeWidget: " + timeElapsed);
		if (mCM.getFgPhone().getPhoneType() == PhoneConstants.PHONE_TYPE_CDMA
				&& PhoneGlobals.getInstance().cdmaPhoneCallState
						.getSiggleDialingState()) {
			return;
		}
		mTime.setText(AuroraPhoneUtils.getTimeString(timeElapsed));
	}

	private boolean mIsFirstShow = false;

	final PathInterpolator mShowInterpolator = new PathInterpolator(0.2f, 0.9f,
			0, 1);
	final PathInterpolator mHideInterpolator = new PathInterpolator(0.45f, 0,
			0.6f, 0.3f);
	final TimeInterpolator easeInterpolator = Ease.Cubic.easeInOut;
	final TimeInterpolator easeInInterpolator = Ease.Cubic.easeIn;
	final TimeInterpolator easeOutInterpolator = Ease.Cubic.easeOut;
	private float mDensity = 3.0f;
	
	
	

    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}	

    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last
     *              call to onScroll. This is NOT the distance between {@code e1}
     *              and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last
     *              call to onScroll. This is NOT the distance between {@code e1}
     *              and {@code e2}.
     * @return true if the event is consumed, else false
     */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		Log.v("SlideView", "onScroll");		
		float y1=e1.getY();
		float y2=e2.getY();
		
		if(Math.abs(y1-y2) > 5 * mDensity ){
			FloatWindowManager.removeWindowFinal(PhoneGlobals.getInstance());
			CallNotifier notifier = PhoneGlobals.getInstance().notifier;
			notifier.silenceRinger();
			InCallScreen.mIsOnPuaseWhenRinging = true;
			PhoneGlobals.getInstance().notificationMgr.updateInCallNotification();
			return true;
		} 
		return false;
	}

    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1 The first down motion event that started the fling.
     * @param e2 The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second
     *              along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second
     *              along the y axis.
     * @return true if the event is consumed, else false
     */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onFling");		
		return false;
	}

	
}
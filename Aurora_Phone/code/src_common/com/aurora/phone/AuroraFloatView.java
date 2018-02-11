package com.android.phone;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Button;
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

import android.widget.LinearLayout.LayoutParams;

import java.lang.reflect.*;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.view.GestureDetector.OnGestureListener;

public class AuroraFloatView extends LinearLayout implements
		View.OnClickListener, OnGestureListener{

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

	private TextView mName, mArea;
	private Button mHangup, mAnswer;
	private CallManager mCM;
	private GestureDetector gDetector; 
	private static final String LOG_TAG = "AuroraFloatView";

	public AuroraFloatView(Context context) {
		super(context);
		mCM = PhoneGlobals.getInstance().mCM;
		gDetector = new GestureDetector(this);
		windowManager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.incoming_pop, this);
		View view = findViewById(R.id.incoming_pop);
		viewWidth = view.getLayoutParams().width;
		viewHeight = view.getLayoutParams().height;
		mName = (TextView) findViewById(R.id.name);
		mArea = (TextView) findViewById(R.id.area);
		mHangup = (Button) findViewById(R.id.hangup);
		mHangup.setOnClickListener(this);
		mAnswer = (Button) findViewById(R.id.answer);
		mAnswer.setOnClickListener(this);
		View main = findViewById(R.id.main_content);
		main.setOnClickListener(this);
		Call call = mCM.getFirstActiveRingingCall();
		int phoneType = call.getPhone().getPhoneType();
		Connection conn = null;
		if (phoneType == PhoneConstants.PHONE_TYPE_CDMA) {
			conn = call.getLatestConnection();
		} else if ((phoneType == PhoneConstants.PHONE_TYPE_GSM)
				|| (phoneType == PhoneConstants.PHONE_TYPE_SIP)) {
			conn = call.getEarliestConnection();
		}
		AuroraCallerInfo info = PhoneUtils.getCallerInfo(getContext(), conn);
		if (info != null) {
			if (!TextUtils.isEmpty(info.name)) {
				mName.setText(info.name);
				mArea.setText(info.phoneNumber + " " + info.mArea);
			} else {
				mName.setText(info.phoneNumber);
				mArea.setText(info.mArea);
			}
		}

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
        return gDetector.onTouchEvent(event);
    }

	public void onClick(View view) {
		int id = view.getId();
		log("onClick(View " + view + ", id " + id + ")...");

		switch (id) {
		case R.id.hangup:
			PhoneUtils.hangup(mCM);
			FloatWindowManager.removeWindow(PhoneGlobals.getInstance());
			break;
		case R.id.answer:
			internalAnswerCall();
			FloatWindowManager.removeWindow(PhoneGlobals.getInstance());
			PhoneGlobals.getInstance().displayCallScreen();
			break;
		case R.id.main_content:
			PhoneGlobals.getInstance().displayCallScreen();
			FloatWindowManager.removeWindow(PhoneGlobals.getInstance());
			break;
		}
	}

	private void internalAnswerCall() {
		if (DBG)
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
			} else if (phoneType == PhoneConstants.PHONE_TYPE_GSM) {
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

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		Log.v("SlideView", "onFling");		

		float y1=e1.getY();
		float y2=e2.getY();
		
		if(Math.abs(y1-y2) > 100){
			FloatWindowManager.removeWindow(PhoneGlobals.getInstance());
			return true;
		}
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}	
	 
}

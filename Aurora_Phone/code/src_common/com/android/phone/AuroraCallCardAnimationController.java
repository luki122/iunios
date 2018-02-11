package com.android.phone;

import java.util.Set;

import android.animation.*;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import java.util.Iterator;
import java.util.HashSet;

import com.android.internal.telephony.*;

public class AuroraCallCardAnimationController {
	
    private static final String LOG_TAG = "AuroraCallCardAnimationController";
    static AuroraCallCardAnimationController sMe;
    
    private float mDensity;
    private PhoneGlobals mApplication;
    private CallCard mCallCard;
    private InCallScreen mInCallScreen;
    private ViewGroup mPrimaryCallInfo;
    private TextView mCallStateLabel;
    private TextView mElapsedTime;
    private ImageView mPhoto, mPhotoV;
    private TextView mName;
    private TextView mPhoneNumber;
    
  	private TextView mNote;
  	private TextView mAdrress;
  	private ImageView meclipstimebg;
  	
  	private int incoming_call_photo_height;
  	private int incoming_call_photo_height_v2;
  	private int incoming_call_callcard_height;
  	private int incoming_call_top_margin;
 	private int incoming_call_top_margin_v2;
  	private int incoming_call_name_size;
  	private int incoming_call_name_size_v2;
  	private int other_call_photo_height;
  	private int other_call_callcard_height;
  	private int other_call_top_margin;
  	private int other_call_name_size;
  	private int photo_move_distance;
  	private int photo_move_distance2;
  	private int name_move_distance;

  	private int incoming_call_photo_height_real;
 	private int incoming_call_photo_height_real_v2;
  	private int other_call_photo_height_real;
  	private View photo_container;
  	private View callcard_container;
  	private View guangquan1, guangquan2, guangquan3, guangquan4, guangquan;
  	private Animation guang1_anim, guang11_anim, guang2_anim, guang3_anim, guang4_anim;

  	private View labelAndNumber;
  	private View namenote;
  	private ObjectAnimator mPhotoRotationYAnimator, mPhotoVRotationYAnimator;
  	private ObjectAnimator mElapsedTimeRotationYAnimator;
  	private ObjectAnimator mPhotoCircleRotationYAnimator;
  	
  	private ObjectAnimator mPhotoRotationXAnimator, mPhotoVRotationXAnimator;
  	private ObjectAnimator mElapsedTimeRotationXAnimator;
  	private ObjectAnimator mPhotoCircleRotationXAnimator;
  	
  	private ObjectAnimator mPhotoScaleXAnimator, mPhotoVScaleXAnimator;
  	private ObjectAnimator mElapsedTimeScaleXAnimator;
  	private ObjectAnimator mPhotoCircleScaleXAnimator;
  	private ObjectAnimator mPhotoScaleYAnimator, mPhotoVScaleYAnimator;
  	private ObjectAnimator mElapsedTimeScaleYAnimator;
  	private ObjectAnimator mPhotoCircleScaleYAnimator;
  	
  	private ObjectAnimator mPhotoToTopAnimator, mPhotoVToTopAnimator;
  	private ObjectAnimator mElapsedTimeToTopAnimator;
  	private ObjectAnimator mPhotoCircleToTopAnimator;
  	
  	private ObjectAnimator mNamenoteToTopAnimator;
  	private ObjectAnimator mLabelAndNumberToTopAnimator;
  	private ObjectAnimator mNameTextSizeToSmallAnimator;
  	private ObjectAnimator mEclipstimeBgAlphaToShowAnimator;

  	private ObjectAnimator mSogouLineToTopAnimator, mSloganRejectToTopAnimator;

  	private long now_time;
  	private boolean is_guangquan_anim_start = false;
  	private boolean is_incoming_to_incall_start = false;
  	private boolean is_reject_to_end_start = false;
  	private boolean is_reject_to_sms_start = false;
  	private boolean is_hide_start = false;
  	private boolean is_show_start = false;
  	private boolean is_user_reject_call = false;

  	private float photoy_incoming = -1;
  	private float namey_incoming = 0;
  	private float numbery_incoming = 0;
  	private boolean is_disconnect_cannot_anim = false;
  	BackgroundView backgroudview;
    
  	
	private View mSogouLine;
	private TextView mSogouNote;
	private TextView mSloganNote;
    
    
//    static AuroraCallCardAnimationController getInstance() {
//        if (sMe == null) {
//        	sMe = new AuroraCallCardAnimationController();        	
//        }
//        return sMe;
//    }
    
    AuroraCallCardAnimationController(InCallScreen inCallScreen) {
        mApplication = PhoneGlobals.getInstance();
        mInCallScreen = inCallScreen;
        createSomeDimenForAnim(mInCallScreen);
        backgroudview = (BackgroundView)mInCallScreen.findViewById(R.id.background);
    }
    
//    void setInCallScreen(InCallScreen inCallScreen) {
//        mInCallScreen = inCallScreen;
//        if(mInCallScreen != null) {
//            backgroudview = (BackgroundView)mInCallScreen.findViewById(R.id.background);
//        } else {
//        	backgroudview = null;
//        }
//        
//    }
    
    void setCallCard(CallCard callcard) {  
    	mIsCreate = false;
    	
    	mCallCard = callcard;
    	
        mPrimaryCallInfo = (ViewGroup) mCallCard.findViewById(R.id.primary_call_info);

        mCallStateLabel = (TextView) mCallCard.findViewById(R.id.callStateLabel);
        mElapsedTime = (TextView) mCallCard.findViewById(R.id.elapsedTime);

        // "Caller info" area, including photo / name / phone numbers / etc
        mPhoto = (ImageView) mCallCard.findViewById(R.id.photo);
        mPhotoV = (ImageView) mCallCard.findViewById(R.id.photoV);
        mName = (TextView) mCallCard.findViewById(R.id.name);
        mPhoneNumber = (TextView) mCallCard.findViewById(R.id.phoneNumber);

        // mSocialStatus = (TextView) mCallCard.findViewById(R.id.socialStatus);

        GetSomeViewAndAnim();
    	
    }
    
	private void GetSomeViewAndAnim() {
		mSogouLine = mCallCard.findViewById(R.id.sogou_line);
		mSogouNote = (TextView) mCallCard.findViewById(R.id.sogou_note);
		mSloganNote = (TextView) mCallCard.findViewById(R.id.slogan);
		labelAndNumber = mCallCard.findViewById(R.id.labelAndNumber);
		namenote = mCallCard.findViewById(R.id.namenote);
		mNote = (TextView) mCallCard.findViewById(R.id.note);
		mAdrress = (TextView) mCallCard.findViewById(R.id.numberAddress);
		meclipstimebg = (ImageView) mCallCard.findViewById(R.id.meclapsedbg);
		// mElapsedTime.setTypeface(Typeface.createFromFile("system/fonts/number.ttf"));
		photo_container = mCallCard.findViewById(R.id.photo_area);
		callcard_container = mCallCard.findViewById(R.id.callcard_area);
		guangquan1 = mCallCard.findViewById(R.id.photoguangquan1);
		guangquan2 = mCallCard.findViewById(R.id.photoguangquan2);
		guangquan3 = mCallCard.findViewById(R.id.photoguangquan3);
		guangquan4 = mCallCard.findViewById(R.id.photoguangquan4);
		guangquan = mCallCard.findViewById(R.id.photoguangquan);
	    guang1_anim=android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.scale1);
	    guang11_anim=android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.scale11);
	    guang2_anim=android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.scale2);
	    guang3_anim=android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.scale3);
	    guang4_anim=android.view.animation.AnimationUtils.loadAnimation(mApplication, R.anim.scale4);
		guang1_anim.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (is_guangquan_anim_start) {
					guangquan1.startAnimation(guang11_anim);
				}
			}
		});
		guang11_anim.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				if (is_guangquan_anim_start) {
					startGuangQuanAnimation();
				}
			}
		});
		guang2_anim.setFillAfter(true);
		guang3_anim.setFillAfter(true);
		guang4_anim.setFillAfter(true);

	}
    
    private static final int RING_TO_SMS = 3;
    private static final int HIDE_FOR_DIALPAD = 4;
    private static final int HANGUP_RING_CALL = 6;
    private static final int SHOW_BECAUSE_DIALPAD = 7;
    private static final int INIT_CALLCARD_ANIMATION = 9;
    
    private Handler animHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub

			int action = msg.what;
			Log.v(LOG_TAG, "handleMessage action=" + action);
			if (is_disconnect_cannot_anim) {
				return;
			}
			if (action == RING_TO_SMS) {
				ringToSmsFinal();
			} else if (action == HIDE_FOR_DIALPAD) {
				hideCallCardForDialpadFinal();				
			} else if (action == HANGUP_RING_CALL) {
				hangupRingingCallAlmostFinal();
			} else if (action == SHOW_BECAUSE_DIALPAD) {
				showCallCardBecauseHideDialpad();
			} else if (action == INIT_CALLCARD_ANIMATION) {
				createIncallCardViewAnimation();
			}
			super.handleMessage(msg);
		}

	};
	
	private void ringToSmsFinal() {
		Log.v(LOG_TAG, "ringToSmsFinal");
		final int left = mPhoto.getLeft();
		final int right = mPhoto.getRight();
		final int top = mPhoto.getTop();
		final int bottom = mPhoto.getBottom();
		final int left1 = namenote.getLeft();
		final int right1 = namenote.getRight();
		final int top1 = namenote.getTop();
		final int bottom1 = namenote.getBottom();
		final int left2 = labelAndNumber.getLeft();
		final int right2 = labelAndNumber.getRight();
		final int top2 = labelAndNumber.getTop();
		final int bottom2 = labelAndNumber.getBottom();
		TranslateAnimation ta;
		final int numbermovedistance = name_move_distance - incoming_call_name_size + other_call_name_size;
		ta = new TranslateAnimation(0, 0, 0, -numbermovedistance);
		ta.setDuration(ANIMATION_DURATION_510);
		ta.setInterpolator(new DecelerateInterpolator());
		ta.setFillAfter(true);
		ta.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				Log.v(LOG_TAG, "ringToSmsFinal onAnimationEnd");
				labelAndNumber.clearAnimation();
				labelAndNumber.layout(left2, top2 - numbermovedistance, right2, bottom2 - numbermovedistance);
				namenote.clearAnimation();
				namenote.layout(left1, top1 - name_move_distance, right1, bottom1 - name_move_distance);
				mPhoto.clearAnimation();
				guangquan.clearAnimation();
				RelativeLayout.LayoutParams fllp = new RelativeLayout.LayoutParams(other_call_photo_height_real, other_call_photo_height_real);
//				fllp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;		
				fllp.addRule(RelativeLayout.CENTER_IN_PARENT);
				mPhoto.setLayoutParams(fllp);
				guangquan.setLayoutParams(fllp);
				callcard_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, other_call_callcard_height));
				photo_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, other_call_photo_height));
//				photo_container.setPadding(0, 0, 0, other_call_top_margin);
				mPhoto.layout(mElapsedTime.getLeft(), top - photo_move_distance, mElapsedTime.getRight(), top - photo_move_distance + mElapsedTime.getHeight());
//				guangquan.layout(mElapsedTime.getLeft(), top - photo_move_distance, mElapsedTime.getRight(), top - photo_move_distance + mElapsedTime.getHeight());
				guangquan.setVisibility(View.GONE);
				meclipstimebg.setBackgroundResource(R.drawable.smsreject_circle);
				meclipstimebg.setVisibility(View.VISIBLE);

				final AlphaAnimation aa = new AlphaAnimation(0, 1);
				aa.setDuration(ANIMATION_DURATION_500);
				aa.setInterpolator(new DecelerateInterpolator());
				aa.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						Log.v(LOG_TAG, "ringToSmsFinal onAnimationEnd2");
						// TODO Auto-generated method stub
						final RotateAnimation ra = new RotateAnimation(0, 360, meclipstimebg.getWidth() / 2, meclipstimebg.getHeight() / 2);
						ra.setDuration(ANIMATION_DURATION_3000);
						ra.setRepeatCount(Animation.INFINITE);
						meclipstimebg.startAnimation(ra);
						is_reject_to_sms_start = false;
					}
				});
				meclipstimebg.startAnimation(aa);

				mCallCard.setDefaultPhoto(R.drawable.photo_default_outgoing);

				mName.setTextSize(other_call_name_size);
				mName.setTextColor(0xFFFFFFFF);
				mPhoneNumber.setTextColor(0xFFFFFFFF);
				mAdrress.setTextColor(0xFFFFFFFF);
				mNote.setTextColor(0xFFFFFFFF);
				mSogouNote.setTextColor(0xFFFFFFFF);
				mSloganNote.setTextColor(0xFFFFFFFF);
			}
		});
		
		
		labelAndNumber.startAnimation(ta);
	
	}
	
	private void hideCallCardForDialpadFinal() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -296);
		ta.setDuration(ANIMATION_DURATION_400);
		ta.setInterpolator(new DecelerateInterpolator());
		ta.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub

				mCallCard.setVisibility(View.GONE);
				photo_container.setVisibility(View.VISIBLE);
				namenote.setVisibility(View.VISIBLE);
			}
		});
		
		
		AlphaAnimation aa = new AlphaAnimation(1, 0);
		aa.setDuration(ANIMATION_DURATION_400);
		aa.setInterpolator(new DecelerateInterpolator());
		AnimationSet as = new AnimationSet(false);
		as.addAnimation(aa);
		as.addAnimation(ta);
		labelAndNumber.startAnimation(as);
	}
	
	private void hangupRingingCallAlmostFinal() {

		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -600);
		ta.setDuration(ANIMATION_DURATION_500);
		ta.setInterpolator(new DecelerateInterpolator());
		AlphaAnimation aa = new AlphaAnimation(1, 0);
		aa.setDuration(ANIMATION_DURATION_500);
		aa.setInterpolator(new DecelerateInterpolator());
		AnimationSet as = new AnimationSet(false);				
		as.addAnimation(aa);
		as.addAnimation(ta);

		mCallStateLabel.setVisibility(View.INVISIBLE);
		as.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub

				photo_container.setVisibility(View.INVISIBLE);
				namenote.setVisibility(View.INVISIBLE);
				labelAndNumber.setVisibility(View.INVISIBLE);
				callcard_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, other_call_callcard_height));
				mCallStateLabel.setVisibility(View.VISIBLE);
				mCallStateLabel.setText(mApplication.getString(R.string.card_title_hanging_up));
				
				if (!is_disconnect_cannot_anim) {							
					hangupRingingCallFinal();
				}

			}
		});

		labelAndNumber.startAnimation(as);
	
	}
	
	private void hangupRingingCallFinal() {
		TranslateAnimation ta = new TranslateAnimation(0, 0, 300, -120);
		ta.setDuration(ANIMATION_DURATION_300);
		ta.setInterpolator(new DecelerateInterpolator());
		AlphaAnimation aa = new AlphaAnimation(0, 1);
		aa.setDuration(ANIMATION_DURATION_300);
		aa.setInterpolator(new DecelerateInterpolator());
		AnimationSet as = new AnimationSet(false);
		as.setFillAfter(true);
		as.addAnimation(aa);
		as.addAnimation(ta);
		as.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				is_reject_to_end_start = false;
				mInCallScreen.handleOnscreenButtonClick(R.id.incomingCallReject);
			}
		});
		mCallStateLabel.startAnimation(as);
	}
	
	private void showCallCardBecauseHideDialpad() {

		
		TranslateAnimation ta = new TranslateAnimation(0, 0, -296, 0);
		ta.setDuration(ANIMATION_DURATION_410);
		ta.setInterpolator(new DecelerateInterpolator());
		ta.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				is_show_start = false;
				mCallCard.setVisibility(View.VISIBLE);

				mCallStateLabel.setVisibility(View.VISIBLE);
				labelAndNumber.setVisibility(View.VISIBLE);
				labelAndNumber.setAlpha(1);
				mCallStateLabel.setAlpha(1);
				is_show_start = false;
		        Call fgCall = mApplication.mCM.getActiveFgCall();
				if (fgCall.getState() == Call.State.ACTIVE
						&& mPhoto.getVisibility() == View.VISIBLE) {
					mElapsedTime.setVisibility(View.VISIBLE);
					meclipstimebg.setVisibility(View.VISIBLE);
					meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
					// final RotateAnimation rota=new
					// RotateAnimation(0,360,meclipstimebg.getWidth()/2,meclipstimebg.getHeight()/2);
					// rota.setDuration(ANIMATION_DURATION_3000);
					// rota.setRepeatCount(Animation.INFINITE);
					// meclipstimebg.startAnimation(rota);
					Log.v(LOG_TAG, "lgyphoto9");
					mPhoto.setVisibility(View.INVISIBLE);
					guangquan.setVisibility(View.GONE);
					mCallStateLabel.setText("");

				}
			}
		});
		
		
		AlphaAnimation aa = new AlphaAnimation(0, 0.6f);
		aa.setDuration(ta.getDuration());
		aa.setInterpolator(ta.getInterpolator());
		AnimationSet as = new AnimationSet(false);
		as.addAnimation(aa);
		as.addAnimation(ta);
		labelAndNumber.setAlpha(1);
		labelAndNumber.startAnimation(as);
	
	}
	
	
	private void startGuangQuanAnimation() {
		if(!GlowPadView.mIsUseNewUI && mApplication.mCM.hasActiveRingingCall()) {
	    	 return;
	    }
		guangquan1.clearAnimation();
		guangquan2.clearAnimation();
		guangquan3.clearAnimation();
		guangquan4.clearAnimation();
		guangquan1.setVisibility(View.VISIBLE);
		guangquan2.setVisibility(View.VISIBLE);
		guangquan3.setVisibility(View.VISIBLE);
		guangquan4.setVisibility(View.VISIBLE);
		guangquan2.setAlpha(0.7f);
		guangquan3.setAlpha(0f);
		guangquan4.setAlpha(0f);
		guangquan1.startAnimation(guang1_anim);
		guangquan2.startAnimation(guang2_anim);
		animHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (is_guangquan_anim_start) {
					guangquan3.setAlpha(0.7f);
					guangquan3.startAnimation(guang3_anim);
				}
			}

		}, 400);
		animHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (is_guangquan_anim_start) {
					guangquan4.setAlpha(0.7f);
					guangquan4.startAnimation(guang4_anim);
				}
			}

		}, 800);

	}
	
	void startGuangQuanAnimationIfNotStart() {
		if(!is_guangquan_anim_start) {
			is_guangquan_anim_start = true;
			startGuangQuanAnimation();
		}
	}

	void stopGuangQuanAnimation() {
		is_guangquan_anim_start = false;
		guang1_anim.cancel();
		guang1_anim.reset();
		guang11_anim.cancel();
		guang11_anim.reset();
		guang2_anim.cancel();
		guang2_anim.reset();
		guang3_anim.cancel();
		guang3_anim.reset();
		guang4_anim.cancel();
		guang4_anim.reset();
		guangquan1.clearAnimation();
		guangquan2.clearAnimation();
		guangquan3.clearAnimation();
		guangquan4.clearAnimation();
		guangquan1.setVisibility(View.GONE);
		guangquan2.setVisibility(View.GONE);
		guangquan3.setVisibility(View.GONE);
		guangquan4.setVisibility(View.GONE);
	}
	
	void stopGuangQuanAnimationIfStart(){
		if(is_guangquan_anim_start) {
			stopGuangQuanAnimation();
		}
	}
	
	private void createSomeDimenForAnim(Context context) {

		incoming_call_photo_height = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_height);
		incoming_call_photo_height_v2 = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_height_v2);
		incoming_call_callcard_height = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_callcard_height);
		incoming_call_top_margin = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_top_magin);
		incoming_call_top_margin_v2 = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_top_magin_v2);
		incoming_call_name_size = 28;// context.getResources().getDimensionPixelSize(R.dimen.incoming_call_name_text_size);
		incoming_call_name_size_v2 = 24;
		other_call_photo_height = context.getResources().getDimensionPixelSize(R.dimen.other_call_photo_height);
		other_call_callcard_height = context.getResources().getDimensionPixelSize(R.dimen.other_call_callcard_height);
		other_call_top_margin = context.getResources().getDimensionPixelSize(R.dimen.other_call_photo_top_magin);
		other_call_name_size = 23;// context.getResources().getDimensionPixelSize(R.dimen.other_call_name_text_size);
		if(GlowPadView.mIsUseNewUI) {
			photo_move_distance = context.getResources().getDimensionPixelSize(R.dimen.photo_move_distance);
			name_move_distance = context.getResources().getDimensionPixelSize(R.dimen.name_move_distance);
		} else {
			photo_move_distance = context.getResources().getDimensionPixelSize(R.dimen.photo_move_distance_v2);
			name_move_distance = context.getResources().getDimensionPixelSize(R.dimen.name_move_distance_v2);
		}
		photo_move_distance2 = context.getResources().getDimensionPixelSize(R.dimen.photo_move_distance2);
		incoming_call_photo_height_real = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_height_real);
		incoming_call_photo_height_real_v2 = context.getResources().getDimensionPixelSize(R.dimen.incoming_call_photo_height_real_v2);
		other_call_photo_height_real = context.getResources().getDimensionPixelSize(R.dimen.other_call_photo_height_real);
		
	    mDensity = context.getResources().getDisplayMetrics().density;

	}

	void OnShoworHideCallCard(boolean is_show) {
		if (is_disconnect_cannot_anim) {
			return;
		}
		
		if (mPrimaryCallInfo.getVisibility() == View.VISIBLE) {
			if (is_show) {
				photo_container.setVisibility(View.VISIBLE);
				namenote.setVisibility(View.VISIBLE);
				labelAndNumber.setVisibility(View.INVISIBLE);
				mCallStateLabel.setVisibility(View.INVISIBLE);
				labelAndNumber.setAlpha(0);
				mCallStateLabel.setAlpha(0);
				mCallCard.setVisibility(View.VISIBLE);

				is_show_start = true;
				
				
				AnimationSet as_show = new AnimationSet(false);
				TranslateAnimation ta = new TranslateAnimation(0, 0, -296, 0);
				ta.setDuration(ANIMATION_DURATION_400);
				ta.setInterpolator(new DecelerateInterpolator());
				AlphaAnimation aa = new AlphaAnimation(0, 1);
				aa.setDuration(ANIMATION_DURATION_400);
				aa.setInterpolator(new DecelerateInterpolator());
				as_show.addAnimation(ta);
				as_show.addAnimation(aa);
				as_show.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						photo_container.setVisibility(View.VISIBLE);
						namenote.setVisibility(View.VISIBLE);
						labelAndNumber.setAlpha(1);
						mCallStateLabel.setAlpha(1);
					}
				});
				
				photo_container.startAnimation(as_show);
				namenote.startAnimation(as_show);

				animHandler.sendMessageDelayed(animHandler.obtainMessage(SHOW_BECAUSE_DIALPAD), 50);

			} else {
				is_hide_start = true;
				photo_container.setVisibility(View.VISIBLE);
				namenote.setVisibility(View.VISIBLE);
				mCallCard.setVisibility(View.VISIBLE);
				
				AnimationSet as_hide = new AnimationSet(false);
				TranslateAnimation ta_hide = new TranslateAnimation(0, 0, 0, -296);
				ta_hide.setDuration(ANIMATION_DURATION_400);
				ta_hide.setInterpolator(new DecelerateInterpolator());
				AlphaAnimation aa_hide = new AlphaAnimation(1, 0);
				aa_hide.setDuration(ANIMATION_DURATION_400);
				aa_hide.setInterpolator(new DecelerateInterpolator());
				as_hide.addAnimation(ta_hide);
				as_hide.addAnimation(aa_hide);
				as_hide.setAnimationListener(new AuroraAnimationListener() {

					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
						is_hide_start = false;
						photo_container.setVisibility(View.INVISIBLE);
						namenote.setVisibility(View.INVISIBLE);

					}
				});
				
				photo_container.startAnimation(as_hide);
				namenote.startAnimation(as_hide);
				mCallStateLabel.setVisibility(View.GONE);
				animHandler.sendMessageDelayed(animHandler.obtainMessage(HIDE_FOR_DIALPAD), 30);

			}
		} else {
			mCallCard.setVisibility(is_show ? View.VISIBLE : View.GONE);
		}

	}

	boolean isStartRingToSmsAnim() {
		return is_reject_to_sms_start;
	}


	void RingToSms() {
		
		Log.v(LOG_TAG, "RingToSms");
		if (is_disconnect_cannot_anim) {
			return;
		}

		is_reject_to_sms_start = true;
		stopGuangQuanAnimationIfStart();

		final int left = mPhoto.getLeft();
		final int right = mPhoto.getRight();
		final int top = mPhoto.getTop();
		final int bottom = mPhoto.getBottom();
		mNote.setVisibility(View.INVISIBLE);
		mSogouLine.setVisibility(View.GONE);
		mSloganNote.setVisibility(View.GONE);
		mPhotoV.setVisibility(View.INVISIBLE);
		TranslateAnimation ta = new TranslateAnimation(0, 0, 0, -photo_move_distance2);
		float tempParam = mDensity > 3 ? 0.78f : 0.84f;
		final ScaleAnimation sa = new ScaleAnimation(1, tempParam, 1, tempParam, (right - left) / 2, (bottom - top) / 2);
		ta.setDuration(ANIMATION_DURATION_400);
		sa.setDuration(ANIMATION_DURATION_400);
		ta.setFillAfter(true);
		sa.setFillAfter(true);
		ta.setInterpolator(new DecelerateInterpolator());
		sa.setInterpolator(new DecelerateInterpolator());

		AnimationSet ass = new AnimationSet(false);
		ass.addAnimation(ta);
		ass.addAnimation(sa);
		ass.setFillAfter(true);

		mPhoto.startAnimation(ass);
		guangquan.startAnimation(ass);

		TranslateAnimation ta3 = new TranslateAnimation(0, 0, 0, -name_move_distance);
		ta3.setDuration(ANIMATION_DURATION_510);
		ta3.setInterpolator(new DecelerateInterpolator());
		namenote.startAnimation(ta3);
		ta3.setFillAfter(true);
		
		animHandler.sendMessageDelayed(animHandler.obtainMessage(RING_TO_SMS), 50);
		ValueAnimator oa = ObjectAnimator.ofFloat(mName, "TextSize", 28.0f, 23.0f);
		oa.setDuration(ANIMATION_DURATION_400);
		oa.start();

		// aurora add zhouxiaobing 20140208 start
		backgroudview.startDisplayAnim();
		// aurora add zhouxiaobing 20140208 end
	}

	void setIncallCardViewAnimationManDongZuo(int per) {
		if (is_disconnect_cannot_anim) {
			return;
		}
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}

		stopGuangQuanAnimation();

		is_incoming_to_incall_start = true;		
		
		Iterator<ObjectAnimator> iterator= mRingingAnims.iterator();
		while(iterator.hasNext()){
			iterator.next().setCurrentPlayTime(per);
		}
			
		now_time = per;
		mNote.setVisibility(View.INVISIBLE);// aurora add zhouxiaobing 20131119
		Log.v(LOG_TAG, "per=" + per);
	}

	void ResetIncallCardViewAnimationManDongZuo() {
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}

		resetIncallCardViewAnimationWhenRinging();

		is_guangquan_anim_start = true;
		startGuangQuanAnimation();

		mNote.setVisibility(View.VISIBLE);// aurora add zhouxiaobing 20131119
	}

	void ResetIncallCardViewAnimationManDongZuo2() {
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}
		
		resetIncallCardViewAnimationWhenRinging();

	}
	
	private void resetIncallCardViewAnimationWhenRinging() {
		Iterator<ObjectAnimator> iterator= mRingingAnims.iterator();
		while(iterator.hasNext()){
			iterator.next().setCurrentPlayTime(0);
		}
		now_time = 0;
		is_incoming_to_incall_start = false;
	}

	private void ManDongzuoToStartAnimation() {
		if (is_disconnect_cannot_anim) {
			return;
		}
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}

		is_incoming_to_incall_start = true;
			
		if (mSloganNote.getVisibility() == View.VISIBLE) {
			int slonganTop = mSloganNote.getTop();
			final int numbermovedistance = name_move_distance - incoming_call_name_size + other_call_name_size;
			mSloganRejectToTopAnimator = ObjectAnimator.ofFloat(mSloganNote, "Y", slonganTop, slonganTop - numbermovedistance);
			mSloganRejectToTopAnimator.setDuration(ANIMATION_DURATION_540);
			mSloganRejectToTopAnimator.setInterpolator(new DecelerateInterpolator());
			mSloganRejectToTopAnimator.start();
		}
			
		Iterator<ObjectAnimator> iterator= mRingingAnims.iterator();
		while(iterator.hasNext()){
			ObjectAnimator a = iterator.next();
			if(a.getTarget() != null && ((View)a.getTarget()).getVisibility() == View.VISIBLE) {
				a.start();
			}
			a.setCurrentPlayTime(now_time);
		}

	}

	private void createIncallCardViewAnimation() {
		if (mIsCreate) {
			return;
		}

		photoy_incoming = mPhoto.getTop();
		namey_incoming = namenote.getTop();
		numbery_incoming = labelAndNumber.getTop();
//		if (photoy_incoming < 100 || namey_incoming < 100
//				|| numbery_incoming < 100) {
		if (namey_incoming < 100 || numbery_incoming < 100) {
			animHandler.sendMessageDelayed(animHandler.obtainMessage(INIT_CALLCARD_ANIMATION), 50);
			return;
		}

		final int top = mPhoto.getTop();
		float rotateAngle = GlowPadView.mIsUseNewUI ?  90f : 85f;
		float rotateAngleX = 30f;
		float photeScale = 1.1f;
		mIsCreate = true;
		mPhotoRotationYAnimator = ObjectAnimator.ofFloat(mPhoto, "rotationY", 0f, rotateAngle);
		mPhotoRotationYAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoRotationXAnimator = ObjectAnimator.ofFloat(mPhoto, "rotationX", 0f, rotateAngleX);
		mPhotoRotationXAnimator.setDuration(ANIMATION_DURATION_300);		
		mPhotoScaleXAnimator = ObjectAnimator.ofFloat(mPhoto, "scaleX", 1f, photeScale);
		mPhotoScaleXAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoScaleYAnimator = ObjectAnimator.ofFloat(mPhoto, "scaleY", 1f, photeScale);
		mPhotoScaleYAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoToTopAnimator = ObjectAnimator.ofFloat(mPhoto, "Y", top, top - photo_move_distance / 2);
		mPhotoToTopAnimator.setDuration(ANIMATION_DURATION_300);
		
		mPhotoVRotationYAnimator = ObjectAnimator.ofFloat(mPhotoV, "rotationY", 0f, rotateAngle);
		mPhotoVRotationYAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoVRotationXAnimator = ObjectAnimator.ofFloat(mPhotoV, "rotationX", 0f, rotateAngleX);
		mPhotoVRotationXAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoVScaleXAnimator = ObjectAnimator.ofFloat(mPhotoV, "scaleX", 1f, photeScale);
		mPhotoVScaleXAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoVScaleYAnimator = ObjectAnimator.ofFloat(mPhotoV, "scaleY", 1f, photeScale);
		mPhotoVScaleYAnimator.setDuration(ANIMATION_DURATION_300);
		final int topV = mPhotoV.getTop();
		Log.v(LOG_TAG, "top = " + top + " topV = " +   topV);
		mPhotoVToTopAnimator = ObjectAnimator.ofFloat(mPhotoV, "Y", topV, topV - photo_move_distance / 2);		
		mPhotoVToTopAnimator.setDuration(ANIMATION_DURATION_300);		


		mPhotoCircleRotationYAnimator = ObjectAnimator.ofFloat(guangquan, "rotationY", 0f, rotateAngle);
		mPhotoCircleRotationYAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoCircleRotationXAnimator = ObjectAnimator.ofFloat(guangquan, "rotationX", 0f, rotateAngleX);
		mPhotoCircleRotationXAnimator.setDuration(ANIMATION_DURATION_300);		
		mPhotoCircleScaleXAnimator = ObjectAnimator.ofFloat(guangquan, "scaleX", 1f, photeScale);
		mPhotoCircleScaleXAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoCircleScaleYAnimator = ObjectAnimator.ofFloat(guangquan, "scaleY", 1f, photeScale);
		mPhotoCircleScaleYAnimator.setDuration(ANIMATION_DURATION_300);
		mPhotoCircleToTopAnimator = ObjectAnimator.ofFloat(guangquan, "Y", top, top - photo_move_distance / 2);
		mPhotoCircleToTopAnimator.setDuration(ANIMATION_DURATION_300);

		mElapsedTimeRotationYAnimator = ObjectAnimator.ofFloat(mElapsedTime, "rotationY", 270f, 360f);
		mElapsedTimeRotationYAnimator.setDuration(ANIMATION_DURATION_220);
		mElapsedTimeRotationXAnimator = ObjectAnimator.ofFloat(mElapsedTime, "rotationX", 30f, 0f);
		mElapsedTimeRotationXAnimator.setDuration(ANIMATION_DURATION_220);
		mElapsedTimeScaleXAnimator = ObjectAnimator.ofFloat(mElapsedTime, "scaleX", 0.79f, 1f);
		mElapsedTimeScaleXAnimator.setDuration(ANIMATION_DURATION_300);
		mElapsedTimeScaleYAnimator = ObjectAnimator.ofFloat(mElapsedTime, "scaleY", 0.79f, 1f);
		mElapsedTimeScaleYAnimator.setDuration(ANIMATION_DURATION_300);
		mElapsedTimeToTopAnimator = ObjectAnimator.ofFloat(mElapsedTime, "Y", top - photo_move_distance / 2, top - photo_move_distance);
		mElapsedTimeToTopAnimator.setDuration(ANIMATION_DURATION_220);

		final int left1 = namenote.getLeft();
		final int right1 = namenote.getRight();
		final int top1 = namenote.getTop();
		final int bottom1 = namenote.getBottom();
		final int left2 = labelAndNumber.getLeft();
		final int right2 = labelAndNumber.getRight();
		final int top2 = labelAndNumber.getTop();
		final int bottom2 = labelAndNumber.getBottom();
		
		int TextToTopDuration = GlowPadView.mIsUseNewUI ? ANIMATION_DURATION_540 : ANIMATION_DURATION_300;

		mNamenoteToTopAnimator = ObjectAnimator.ofFloat(namenote, "Y", namenote.getTop(), namenote.getTop() - name_move_distance);
		mNamenoteToTopAnimator.setDuration(TextToTopDuration);
		mNamenoteToTopAnimator.setInterpolator(new DecelerateInterpolator());

		final int numbermovedistance = name_move_distance - incoming_call_name_size + other_call_name_size;
		mLabelAndNumberToTopAnimator = ObjectAnimator.ofFloat(labelAndNumber, "Y", labelAndNumber.getTop(), labelAndNumber.getTop() - numbermovedistance);
		mLabelAndNumberToTopAnimator.setDuration(TextToTopDuration);
		mLabelAndNumberToTopAnimator.setInterpolator(new DecelerateInterpolator());

		float sogouTop = mApplication.getResources().getDimension(R.dimen.aurora_sogou_top);
		// mSogouLineToTopAnimator=ObjectAnimator.ofFloat(mSogouLine, "Y",
		// mSogouLine.getTop(),mSogouLine.getTop()-numbermovedistance);
		mSogouLineToTopAnimator = ObjectAnimator.ofFloat(mSogouLine, "Y", sogouTop, sogouTop - numbermovedistance);
		mSogouLineToTopAnimator.setDuration(TextToTopDuration);
		mSogouLineToTopAnimator.setInterpolator(new DecelerateInterpolator());
				
		int slonganTop = mSloganNote.getTop();
		mSloganRejectToTopAnimator = ObjectAnimator.ofFloat(mSloganNote, "Y", slonganTop, slonganTop - numbermovedistance);
		mSloganRejectToTopAnimator.setDuration(TextToTopDuration);
		mSloganRejectToTopAnimator.setInterpolator(new DecelerateInterpolator());

		mNameTextSizeToSmallAnimator = ObjectAnimator.ofFloat(mName, "TextSize", 28, 23);
		mNameTextSizeToSmallAnimator.setDuration(ANIMATION_DURATION_500);

		mEclipstimeBgAlphaToShowAnimator = ObjectAnimator.ofFloat(meclipstimebg, "Alpha", 0, 1);
		mEclipstimeBgAlphaToShowAnimator.setDuration(ANIMATION_DURATION_500);
		mEclipstimeBgAlphaToShowAnimator.setInterpolator(new DecelerateInterpolator());

		mPhotoRotationYAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				Log.v(LOG_TAG, "lgyphoto10");
					
				
				mPhoto.setVisibility(View.INVISIBLE);
				mPhotoV.setVisibility(View.INVISIBLE);
				guangquan.setVisibility(View.INVISIBLE);
				clearRotation();
				
				if(GlowPadView.mIsUseNewUI) { 
					mElapsedTime.setY(top - photo_move_distance / 2);
				} else {
					mElapsedTime.setRotationX(30);
//					mElapsedTime.setScaleX(0.79f);
//					mElapsedTime.setScaleY(0.79f);
				}
				mElapsedTime.setRotationY(270);			
				mElapsedTime.setVisibility(View.VISIBLE);
				
				final AnimatorSet aos2 = new AnimatorSet();
				if(GlowPadView.mIsUseNewUI) {			
					aos2.playTogether(mElapsedTimeRotationYAnimator, mElapsedTimeToTopAnimator);
				} else {
					aos2.playTogether(mElapsedTimeRotationYAnimator, mElapsedTimeRotationXAnimator);
				}
				aos2.setInterpolator(new AccelerateDecelerateInterpolator());
				aos2.addListener(new AnimatorListenerAdapter() {
					
//					@Override
//					public void onAnimationStart(Animator animation) {
//						// TODO Auto-generated method stub
//						
//						if(!GlowPadView.mIsUseNewUI) { 
//							photo_container.setLayoutParams(new LayoutParams(
//									LayoutParams.MATCH_PARENT, other_call_photo_height));
//						}
//					}

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
						// mElapsedTime.setY(mElapsedTime.getTop());
						if(GlowPadView.mIsUseNewUI) {				
							mElapsedTime.layout(meclipstimebg.getLeft(), top - photo_move_distance, meclipstimebg.getRight(), top - photo_move_distance + mElapsedTime.getHeight());
							meclipstimebg.layout(meclipstimebg.getLeft(), top - photo_move_distance, meclipstimebg.getRight(), top - photo_move_distance + mElapsedTime.getHeight());
						}
						mElapsedTime.setTranslationY(0);
						meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
						meclipstimebg.setVisibility(View.VISIBLE);
						mEclipstimeBgAlphaToShowAnimator.start();
					}

				});

				aos2.start();
			}
		});


		mLabelAndNumberToTopAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				clearTranslationY();
				labelAndNumber.layout(left2, top2 - numbermovedistance, right2, bottom2 - numbermovedistance);

				namenote.layout(left1, top1 - name_move_distance, right1, bottom1 - name_move_distance);

				callcard_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, other_call_callcard_height));
				photo_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, other_call_photo_height));
//				photo_container.setPadding(0, 0, 0, other_call_top_margin);

				mName.setTextSize(other_call_name_size);
//				is_incoming_to_incall_start = false;

			}

		});

		mEclipstimeBgAlphaToShowAnimator.addListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						// TODO Auto-generated method stub
//						mEclipstimeBgAlphaToShowAnimator.removeAllListeners();
//						mEclipstimeBgAlphaToShowAnimator.cancel();
						is_incoming_to_incall_start = false;
						mCallCard.updateState(PhoneGlobals.getInstance().mCM);
					}

				});
		now_time = 0;

		
		mRingingAnims = new HashSet<ObjectAnimator>();
		mRingingAnims.add(mPhotoRotationYAnimator);
		mRingingAnims.add(mPhotoVRotationYAnimator);
		mRingingAnims.add(mPhotoToTopAnimator);
		mRingingAnims.add(mPhotoVToTopAnimator);
		mRingingAnims.add(mPhotoCircleRotationYAnimator);
		mRingingAnims.add(mPhotoCircleToTopAnimator);
		mRingingAnims.add(mNamenoteToTopAnimator);
		mRingingAnims.add(mLabelAndNumberToTopAnimator);
		mRingingAnims.add(mSogouLineToTopAnimator);
		mRingingAnims.add(mSogouLineToTopAnimator);
		mRingingAnims.add(mNameTextSizeToSmallAnimator);
		
		mRingingAnimsV2 = new HashSet<ObjectAnimator>();
		mRingingAnimsV2.add(mPhotoRotationYAnimator);
		mRingingAnimsV2.add(mPhotoVRotationYAnimator);
		mRingingAnimsV2.add(mPhotoCircleRotationYAnimator);
		
//		mRingingAnimsV22 = new HashSet<ObjectAnimator>(mRingingAnims);
		mRingingAnimsV22 = new HashSet<ObjectAnimator>();
		mRingingAnimsV22.add(mPhotoRotationYAnimator);
		mRingingAnimsV22.add(mPhotoVRotationYAnimator);	
		mRingingAnimsV22.add(mPhotoCircleRotationYAnimator);	
		mRingingAnimsV22.add(mPhotoRotationXAnimator);
		mRingingAnimsV22.add(mPhotoVRotationXAnimator);
		mRingingAnimsV22.add(mPhotoCircleRotationXAnimator);
		
		
		
	}

	void startAcceptRingingCallAnimation() {
		stopGuangQuanAnimation();
		ManDongzuoToStartAnimation();
	}

	void startRejectRingingCallAnimation() {
		if (is_disconnect_cannot_anim) {
			return;
		}
		
		stopGuangQuanAnimationIfStart();
		
		is_user_reject_call = true;
		is_reject_to_end_start = true;
		mNote.setVisibility(View.GONE);
		mSogouLine.setVisibility(View.GONE);
		mPhotoV.setVisibility(View.INVISIBLE);
		mSloganNote.setVisibility(View.GONE);
		mCallStateLabel.setTextSize(20);
		backgroudview.startDisplayAnim();
		TranslateAnimation va1 = new TranslateAnimation(0, 0, 0, -600);
		va1.setDuration(ANIMATION_DURATION_500);
		va1.setInterpolator(new DecelerateInterpolator());
		AlphaAnimation va2 = new AlphaAnimation(1, 0);
		va2.setDuration(ANIMATION_DURATION_500);
		va2.setInterpolator(new DecelerateInterpolator());
		AnimationSet as = new AnimationSet(false);
		as.addAnimation(va1);
		as.addAnimation(va2);
		photo_container.startAnimation(as);
		as.setAnimationListener(new AuroraAnimationListener() {

			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				photo_container.setVisibility(View.INVISIBLE);
				namenote.setVisibility(View.INVISIBLE);
			}
		});

		TranslateAnimation va3 = new TranslateAnimation(0, 0, 0, -600);
		va3.setDuration(ANIMATION_DURATION_500);
		va3.setInterpolator(new DecelerateInterpolator());
		AlphaAnimation va4 = new AlphaAnimation(1, 0);
		va4.setDuration(ANIMATION_DURATION_500);
		va4.setInterpolator(new DecelerateInterpolator());
		AnimationSet as2 = new AnimationSet(false);
		as2.addAnimation(va3);
		as2.addAnimation(va4);
		namenote.startAnimation(as2);

		animHandler.sendMessageDelayed(animHandler.obtainMessage(HANGUP_RING_CALL), 60);

	}

	boolean getCanStartAnim() {
		return !is_disconnect_cannot_anim;
	}
	
	boolean getIncoming_to_incall_anim_ex() {
		return is_incoming_to_incall_start && (mPhotoRotationYAnimator.isStarted() || mElapsedTimeRotationYAnimator.isStarted() || mEclipstimeBgAlphaToShowAnimator.isStarted());
	}
	
	void ResetCallCardAnimation() {
		is_disconnect_cannot_anim = true;
		stopGuangQuanAnimationIfStart();
		if (is_reject_to_sms_start) {			
			auroraClearViewAnimation(mPhoto);
			auroraClearViewAnimation(guangquan);
			auroraClearViewAnimation(namenote);
			auroraClearViewAnimation(labelAndNumber);
			is_reject_to_sms_start = false;
		}
		
		if (is_reject_to_end_start) {
			auroraClearViewAnimation(photo_container);
			auroraClearViewAnimation(namenote);
			auroraClearViewAnimation(labelAndNumber);	
			auroraClearViewAnimation(mCallStateLabel);	
			is_reject_to_end_start = false;
			is_user_reject_call = false;
		}

		animHandler.removeMessages(0);
		meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
		meclipstimebg.setVisibility(View.INVISIBLE);
		meclipstimebg.clearAnimation();

		if (is_incoming_to_incall_start) {
			is_incoming_to_incall_start = false;
			if (mPhotoRotationYAnimator.isRunning()) {
				mPhotoRotationYAnimator.end();
				mPhotoRotationXAnimator.end();
				mPhotoVRotationYAnimator.end();
				mPhotoVRotationXAnimator.end();
				mPhotoToTopAnimator.end();
				mPhotoVToTopAnimator.end();

				mPhotoCircleRotationYAnimator.end();
				mPhotoCircleRotationXAnimator.end();
				mPhotoCircleToTopAnimator.end();

				mElapsedTimeRotationYAnimator.end();
				mElapsedTimeRotationXAnimator.end();
				mElapsedTimeToTopAnimator.end();
			} else if (mElapsedTimeRotationYAnimator.isRunning()) {
				mElapsedTimeRotationYAnimator.end();
				mElapsedTimeRotationXAnimator.end();
				mElapsedTimeToTopAnimator.end();
			}
			if (mNamenoteToTopAnimator.isRunning()) {
				mNamenoteToTopAnimator.end();
			}
			if (mLabelAndNumberToTopAnimator.isRunning()) {
				mLabelAndNumberToTopAnimator.end();
			}
			if (mSogouLineToTopAnimator.isRunning()) {
				mSogouLineToTopAnimator.end();
			}
			if (mSloganRejectToTopAnimator.isRunning()) {
				mSloganRejectToTopAnimator.end();
			}
			mNameTextSizeToSmallAnimator.end();
			mEclipstimeBgAlphaToShowAnimator.end();
		}
		
		if (is_show_start) {
			is_show_start = false;			
			auroraClearViewAnimation(photo_container);
			auroraClearViewAnimation(namenote);
			auroraClearViewAnimation(labelAndNumber);	
		}
		
		if (is_hide_start) {
			is_hide_start = false;
			auroraClearViewAnimation(photo_container);
			auroraClearViewAnimation(namenote);
			auroraClearViewAnimation(labelAndNumber);			
			mCallCard.setVisibility(View.VISIBLE);
		}
		
		animHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (mCallCard.getVisibility() != View.VISIBLE)
					mCallCard.setVisibility(View.VISIBLE);
			}

		}, 10);
		
		if (mCallCard.getVisibility() != View.VISIBLE) {
			mCallCard.setVisibility(View.VISIBLE);
		}
		
		backgroudview.is_anim_start = false;

		log("ResetCallCardAnimation");
	}

	// aurora add zhouxiaobing 20131231 end
	
	

	void setIncomingCallViewDimension() {
		Log.v(LOG_TAG, "setIncomingCallViewDimension");		
		if (is_incoming_to_incall_start || is_reject_to_end_start || is_reject_to_sms_start) {
			return;
		}
		photo_container.setVisibility(View.VISIBLE);
		namenote.setVisibility(View.VISIBLE);
		labelAndNumber.setVisibility(View.VISIBLE);
		callcard_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, incoming_call_callcard_height));
		photo_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, incoming_call_photo_height));
		if(mDensity > 3) {
		    photo_container.setPadding(0, 0, 0, incoming_call_top_margin);//aurora change zhouxiaobing 20140609
		} else {
			photo_container.setPadding(0, incoming_call_top_margin, 0, 0);
		}
		RelativeLayout.LayoutParams fllp2 = new RelativeLayout.LayoutParams(incoming_call_photo_height_real, incoming_call_photo_height_real);
//		fllp2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
		fllp2.addRule(RelativeLayout.CENTER_IN_PARENT);
		mPhoto.setLayoutParams(fllp2);
		guangquan.setVisibility(View.VISIBLE);
		guangquan.setLayoutParams(fllp2);
		mName.setTextSize(incoming_call_name_size);
		int nameColor = PhoneGlobals.getInstance().getResources().getColor(R.color.aurora_name_text_color);
		mName.setTextColor(nameColor);
		mNote.setTextColor(nameColor);
		mSogouNote.setTextColor(nameColor);
		mSloganNote.setTextColor(nameColor);
		mCallStateLabel.setTextSize(12);
		mCallStateLabel.clearAnimation();
		mNote.setVisibility(View.VISIBLE);
		mPhoneNumber.setTextColor(nameColor);
		mAdrress.setTextColor(nameColor);
		meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
		meclipstimebg.setVisibility(View.INVISIBLE);
		meclipstimebg.clearAnimation();
		guangquan1.setBackgroundResource(R.drawable.incoming_gq1);
		guangquan2.setBackgroundResource(R.drawable.incoming_gq2);
		guangquan3.setBackgroundResource(R.drawable.incoming_gq3);
		guangquan4.setBackgroundResource(R.drawable.incoming_gq4);
		mCallCard.setDefaultPhoto(R.drawable.photo_default);

		clearRotation();

		clearTranslationY();

		Log.v(LOG_TAG, "photoy = " + mPhoto.getY() + " phototop = " + mPhoto.getTop());
		Log.v(LOG_TAG, "photo_animation = " + photo_container.getAnimation());
		Log.v(LOG_TAG, "mNamey = " + namenote.getY() + " mNametop = " + namenote.getTop());
		Log.v(LOG_TAG, "mName_animation = " + namenote.getAnimation());
		Log.v(LOG_TAG, "labelAndNumbery = " + labelAndNumber.getY() + " labelAndNumbertop = " + labelAndNumber.getTop());
		Log.v(LOG_TAG, "labelAndNumber_animation = " + labelAndNumber.getAnimation());

	}
	
	void setIncomingCallViewDimensionV2() {
		Log.v(LOG_TAG, "setIncomingCallViewDimensionV2");		
		if (is_incoming_to_incall_start || is_reject_to_end_start || is_reject_to_sms_start) {
			return;
		}
		photo_container.setVisibility(View.VISIBLE);
		namenote.setVisibility(View.VISIBLE);
		labelAndNumber.setVisibility(View.VISIBLE);
		guangquan.setVisibility(View.VISIBLE);
		if(GlowPadView.mIsUseNewUI) {
			callcard_container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, incoming_call_callcard_height));
			LayoutParams pc =new LayoutParams(LayoutParams.MATCH_PARENT, incoming_call_photo_height_v2);
			pc.setMargins(0, incoming_call_top_margin_v2, 0, 0);
			photo_container.setLayoutParams(pc);
		
			RelativeLayout.LayoutParams fllp2 = new RelativeLayout.LayoutParams(incoming_call_photo_height_real_v2, incoming_call_photo_height_real_v2);
	//		fllp2.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
			fllp2.addRule(RelativeLayout.CENTER_IN_PARENT);
			mPhoto.setLayoutParams(fllp2);
	//	    photo_container.setPadding(0, incoming_call_top_margin_v2, 0, 0);
			guangquan.setLayoutParams(fllp2);
		}
		mName.setTextSize(incoming_call_name_size_v2);
		int nameColor = PhoneGlobals.getInstance().getResources().getColor(R.color.aurora_name_text_color);
		mName.setTextColor(nameColor);
		mNote.setTextColor(nameColor);
		mSogouNote.setTextColor(nameColor);
		mSloganNote.setTextColor(nameColor);
		mCallStateLabel.setTextSize(14);
		mCallStateLabel.clearAnimation();
		mNote.setVisibility(View.VISIBLE);
		mPhoneNumber.setTextColor(nameColor);
		mAdrress.setTextColor(nameColor);
		meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
		meclipstimebg.setVisibility(View.INVISIBLE);
		meclipstimebg.clearAnimation();
		mCallCard.setDefaultPhoto(R.drawable.photo_default);

		clearRotation();

		clearTranslationY();

		Log.v(LOG_TAG, "photoy = " + mPhoto.getY() + " phototop = " + mPhoto.getTop());
		Log.v(LOG_TAG, "photo_animation = " + photo_container.getAnimation());
		Log.v(LOG_TAG, "mNamey = " + namenote.getY() + " mNametop = " + namenote.getTop());
		Log.v(LOG_TAG, "mName_animation = " + namenote.getAnimation());
		Log.v(LOG_TAG, "labelAndNumbery = " + labelAndNumber.getY() + " labelAndNumbertop = " + labelAndNumber.getTop());
		Log.v(LOG_TAG, "labelAndNumber_animation = " + labelAndNumber.getAnimation());

	}

	void setOtherCallViewDimension() {
		Log.v(LOG_TAG, "setOtherCallViewDimension");	
		if(getIncoming_to_incall_anim_ex()) {
			Log.v(LOG_TAG, "setOtherCallViewDimension return");	
			return ;
		}
		guangquan.setVisibility(View.VISIBLE);
		if(GlowPadView.mIsUseNewUI) {
			callcard_container.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, other_call_callcard_height));
			photo_container.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, other_call_photo_height));
	//		photo_container.setPadding(0, 0, 0, other_call_top_margin);
			RelativeLayout.LayoutParams fllp = new RelativeLayout.LayoutParams(
					other_call_photo_height_real, other_call_photo_height_real);
	//		fllp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
			fllp.addRule(RelativeLayout.CENTER_IN_PARENT);
			mPhoto.setLayoutParams(fllp);
			guangquan.setLayoutParams(fllp);
			mName.setTextSize(other_call_name_size);
		}
		
	
	
		namenote.setVisibility(View.VISIBLE);
		labelAndNumber.setVisibility(View.VISIBLE);
		guangquan1.setBackgroundResource(R.drawable.outgoing_gq1);
		guangquan2.setBackgroundResource(R.drawable.outgoing_gq2);
		guangquan3.setBackgroundResource(R.drawable.outgoing_gq3);
		guangquan4.setBackgroundResource(R.drawable.outgoing_gq4);
		mCallCard.setDefaultPhoto(R.drawable.photo_default_outgoing);
		Log.v(LOG_TAG, "photoy=" + mPhoto.getY() + "phototop=" + mPhoto.getTop());
		Log.v(LOG_TAG, "photo_animation=" + photo_container.getAnimation());
		Log.v(LOG_TAG, "mNamey=" + namenote.getY() + "mNametop=" + namenote.getTop());
		Log.v(LOG_TAG, "mName_animation=" + namenote.getAnimation());
		Log.v(LOG_TAG, "labelAndNumbery=" + labelAndNumber.getY() + "labelAndNumbertop=" + labelAndNumber.getTop());
		Log.v(LOG_TAG, "labelAndNumber_animation=" + labelAndNumber.getAnimation());

		clearRotation();
		
		//aurora add liguangyu 20141204 for #10342 start
		mElapsedTime.setTranslationY(0);
		mElapsedTime.setRotationY(0);
		mElapsedTime.setRotationX(0);
		mElapsedTime.setScaleX(1f);
		mElapsedTime.setScaleY(1f);
		//aurora add liguangyu 20141204 for #10342 end

		clearTranslationY();
		mCallStateLabel.clearAnimation();
	}
	
	private void auroraClearViewAnimation(View view) {
		Animation anim = view.getAnimation();
		if (anim != null) {
			anim.cancel();
		}
		view.clearAnimation();
	}
	
	void updateForegroundCall(Call fgCall, Call bgCall) {
		if ((fgCall.getState() != Call.State.DISCONNECTED && fgCall.getState() != Call.State.DISCONNECTING)) {
			setDisconnectCannotAnim(false);
		}
		// aurora add zhouxiaobing 20140102 start
		if ((fgCall.getState() == Call.State.DIALING || fgCall.getState() == Call.State.ALERTING) && (bgCall.getState() == Call.State.IDLE)) {
			photo_container.setVisibility(View.VISIBLE);
			namenote.setVisibility(View.VISIBLE);
			labelAndNumber.setVisibility(View.VISIBLE);
			mCallStateLabel.setTextSize(12);
			setOtherCallViewDimension();
			meclipstimebg.setBackgroundResource(R.drawable.photo_circle_incall);
			meclipstimebg.setVisibility(View.INVISIBLE);
			meclipstimebg.clearAnimation();
			startGuangQuanAnimationIfNotStart();
		} else {
			photo_container.setVisibility(View.VISIBLE);
			namenote.setVisibility(View.VISIBLE);
			labelAndNumber.setVisibility(View.VISIBLE);
			mCallStateLabel.setTextSize(12);			
			setOtherCallViewDimension();
			stopGuangQuanAnimationIfStart();
		} 
		// aurora add zhouxiaobing 20140102 end
	}
	
	void updateRingingCall(Call ringingCall) {
		// aurora add zhouxiaobing 20140102 start
		// if(ringingCall.getState()==Call.State.INCOMING||ringingCall.getState()==Call.State.WAITING)
		if (ringingCall.getState() == Call.State.INCOMING) {
			setDisconnectCannotAnim(false);
			if (!mInCallScreen.isQuickResponseDialogShowing()) {
				if (!isRingingToInCallStart()) {
					if(GlowPadView.mIsUseNewUI) {
						setIncomingCallViewDimension();
					} else {
						setIncomingCallViewDimensionV2();
					}
					initCallAnim();
					startGuangQuanAnimationIfNotStart();
				}
			}
		} else {
			if (ringingCall.getState() != Call.State.DISCONNECTING
					&& ringingCall.getState() != Call.State.DISCONNECTED) {
				setOtherCallViewDimension();
			}
		}
		// aurora add zhouxiaobing 20140102 end
		Log.v("PhoneAnim", "ringingCall.getState() = " + ringingCall.getState()); 
	}
	
	boolean isAnimationingInRinging() {
		return isRingingToInCallStart() || isRejectToEndStart()
				|| isRejectToSmsStart() || backgroudview.is_anim_start;
	}
	
	
    private static void log(String msg) {
        Log.d(LOG_TAG, msg);
    }
    
    void setDisconnectCannotAnim(boolean value){
    	is_disconnect_cannot_anim = value;
    }   
    
    boolean isRejectToSmsStart(){
    	return is_reject_to_sms_start;
    }
    
    boolean isRejectToEndStart(){
    	return is_reject_to_end_start;
    }
    
    boolean isRingingToInCallStart() {
    	return is_incoming_to_incall_start;
    }
    
    private void initCallAnim() {
       	animHandler.sendMessageDelayed(animHandler.obtainMessage(INIT_CALLCARD_ANIMATION), 50);
    }
   
    
	void setPhototRotateAnimation(int per) {
		if (is_disconnect_cannot_anim) {
			return;
		}
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}

		is_incoming_to_incall_start = true;
		Iterator<ObjectAnimator> iterator= mRingingAnimsV2.iterator();
		while(iterator.hasNext()){
			iterator.next().setCurrentPlayTime(per);
		}

		now_time = per;
		Log.v(LOG_TAG, "per=" + per);
	}
    
    void resetPhotoRotateAnimation() {
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}
		
		Iterator<ObjectAnimator> iterator= mRingingAnimsV2.iterator();
		while(iterator.hasNext()){
			iterator.next().setCurrentPlayTime(0);
		}

		
		now_time = 0;
		is_incoming_to_incall_start = false;

	}
    
    void ManDongzuoToStartAnimationV2() {

		if (is_disconnect_cannot_anim) {
			return;
		}
		// aurora add zhouxiaobing 20140317 for null
		if (!mIsCreate) {
			return;
		}

		is_incoming_to_incall_start = true;
		
		mNote.setVisibility(View.INVISIBLE);
		mSloganNote.setVisibility(View.GONE);

		Iterator<ObjectAnimator> iterator= mRingingAnimsV22.iterator();
		while(iterator.hasNext()){
			ObjectAnimator a = iterator.next();
			if(a.getTarget() != null && ((View)a.getTarget()).getVisibility() == View.VISIBLE) {
				a.start();
			}
		}

	
    }
    
	private boolean mIsCreate = false;
    
    private static final int factor = 1;
    private static final int ANIMATION_DURATION_3000 = 3000 * factor; 
    private static final int ANIMATION_DURATION_540 = 540 * factor;     
    private static final int ANIMATION_DURATION_510 = 510 * factor; 
    private static final int ANIMATION_DURATION_500 = 500 * factor; 
    private static final int ANIMATION_DURATION_410 = 410 * factor; 
    private static final int ANIMATION_DURATION_400 = 400 * factor; 
    private static final int ANIMATION_DURATION_300 = 300 * factor; 
    private static final int ANIMATION_DURATION_220 = 220 * factor; 
    
    private Set<ObjectAnimator> mRingingAnims;    
	private Set<ObjectAnimator> mRingingAnimsV2; 
	private Set<ObjectAnimator> mRingingAnimsV22; 
    
    private void clearRotation() {
    	mPhoto.setTranslationY(0);
		mPhoto.setRotationY(0);
		mPhoto.setRotationX(0);
		mPhoto.setScaleX(1f);
		mPhoto.setScaleY(1f);		
		
		mPhotoV.setTranslationY(0);
		mPhotoV.setRotationY(0);
		mPhotoV.setRotationX(0);
		mPhotoV.setScaleX(1f);
		mPhotoV.setScaleY(1f);

		guangquan.setTranslationY(0);
		guangquan.setRotationY(0);	
		guangquan.setRotationX(0);
		guangquan.setScaleX(1f);
		guangquan.setScaleY(1f);
    }
    
    private void clearTranslationY() {
		labelAndNumber.setTranslationY(0);
		mSogouLine.setTranslationY(0);
		mSloganNote.setTranslationY(0);
		namenote.setTranslationY(0);
    }
}
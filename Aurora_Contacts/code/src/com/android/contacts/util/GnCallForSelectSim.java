package com.android.contacts.util;

import java.util.List;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.mediatek.contacts.simcontact.SIMInfoWrapper;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

import android.app.Activity;
import aurora.app.AuroraAlertDialog; // import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.TelephonyManager;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.ITelephony;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import gionee.telephony.GnTelephonyManager;
//Gionee <xuhz> <2013-04-12> modify for CR00796251 start
import android.telephony.PhoneNumberUtils;
//Gionee <xuhz> <2013-04-12> modify for CR00796251 end

import aurora.app.AuroraActivity;


public class GnCallForSelectSim extends Activity implements OnClickListener {
    private int mStartX = 0;
    private int mStartY = 0;
    //Gionee <xuhz> <2013-06-04> delete for CR00821838 begin
    //private List<SIMInfo> mSimInfoList;
    //Gionee <xuhz> <2013-06-04> delete for CR00821838 end
    private int mSimCount = 0;
	private int mSelectedSimId;
	private int mSlotId = -1;
	private boolean mIsVideoCall;
	private String mNumber;
	private String mCallUri;
	private long mDefaultSim;
    private Animation mAnimation = null;
	private Context mContext;
	private View mViewlLayout;
	private ImageView mClosed;
	private View mSim1Layout;
	private View mSim2Layout;
	private ImageButton mSim1Button;
	private ImageButton mSim2Button;
	private TextView mSim1Name;
	private TextView mSim2Name;
    private TextView mSim1Num;
    private TextView mSim2Num;
    
	private TranslateAnimation mTranslateAnimation;
	private AlphaAnimation mAlphaAnimation;
	private DisplayMetrics mDisplayMetrics;
	private int mAnimType;
    
    private boolean isFinishView = false;

    private final static int SELECT_INVALID = -1;
    private final static int SELECT_SIM1 = 0;
    private final static int SELECT_SIM2 = 1;
    private int mSelectSim = SELECT_INVALID;
    
    private final static int ANIM_TO_TOP = 100;   // 显示在正上方
    private final static int ANIM_TO_RIGHT_AND_TOP = 101;     // 显示在右上方
    private final static int ANIM_TO_RIGHT_AND_BOTTOM = 102;    // 显示在右下方
    private final static int ANIM_TO_LEFT_AND_TOP = 103;    // 显示在左上方
    private final static int ANIM_TO_LEFT_AND_BOTTOM = 104;    // 显示在左下方
    private final static int ANIM_TO_BOTTOM = 105;   // 显示在正下方
    
    private static final String VOICE_CALL_SIM_SETTING = "voice_call_sim_setting";
    private static final long DEFAULT_SIM_NOT_SET = -5;
    
    
    AnimationSet setStart = new AnimationSet(true);
    AnimationSet setEnd = new AnimationSet(true);
	private ScaleAnimation mScaleAnimation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        // Show GnCallForSelectSim Activity in front of soft input
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        
        mContext = this;
        
        mDisplayMetrics = getResources().getDisplayMetrics();
        
        getSimInfoList();
        
        Intent intent = getIntent();
        if (intent != null) {
            mIsVideoCall = intent.getBooleanExtra(Constants.EXTRA_IS_VIDEO_CALL, false);
            mStartX = intent.getIntExtra("x", 0);
            mStartY = intent.getIntExtra("y", 0);
            mNumber = intent.getStringExtra("number");
            mCallUri = intent.getStringExtra("callUri");
        }
		// Gionee <xuhz> <2013-04-12> modify for CR00796251 start
        String number = null;
        if (!TextUtils.isEmpty(mNumber)) {
        	number = mNumber;
        } else if (!TextUtils.isEmpty(mCallUri)) {
        	Uri uri = Uri.parse(mCallUri);
            String scheme = uri.getScheme();
            if (!TextUtils.isEmpty(scheme)) {
                if (scheme.equals("tel") || scheme.equals("sip")) {
                    number = uri.getSchemeSpecificPart();
                }
            }
        }
        boolean isEccNumber = !TextUtils.isEmpty(number) && PhoneNumberUtils.isEmergencyNumber(number);
		if (!isEccNumber && mSimCount > 1 && mDefaultSim < 0) {
		//old : if (mSimCount > 1 && mDefaultSim < 0) {
		// Gionee <xuhz> <2013-04-12> modify for CR00796251 end
	        initAnimType();
	        
	        setContentView(R.layout.gn_anim_view);

	        initView();
	        initSimInfo();
	        initIntent();
	        
	        setStartView();
		} else {
			startActivity(sendIntentToPhone());
	        finish();
		}
	}

	private void setStartView() {
		switch (mAnimType) {
			case ANIM_TO_TOP:
	            mViewlLayout.setBackgroundResource(R.drawable.gn_anim_middle_bottom_bg);
	            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 100.0f, 0.0f);
	            break;
			case ANIM_TO_BOTTOM:
	            mViewlLayout.setBackgroundResource(R.drawable.gn_anim_middle_up_bg);
	            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, -100.0f, 0.0f);
//	            mScaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 1.5f);
	            break;
			case ANIM_TO_RIGHT_AND_BOTTOM:
	            mViewlLayout.setBackgroundResource(R.drawable.gn_anim_up_bg);
	            mTranslateAnimation = new TranslateAnimation(100.0f, 0.0f, -100.0f, 0.0f);
//	            mScaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f);
	            break;
			case ANIM_TO_RIGHT_AND_TOP:
	            mViewlLayout.setBackgroundResource(R.drawable.gn_anim_down_bg);
	            mTranslateAnimation = new TranslateAnimation(100.0f, 0.0f, 100.0f, 0.0f);
//	            mScaleAnimation = new ScaleAnimation(0.3f, 1.0f, 0.3f, 1.0f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f);
	            break;
		}

        mAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);

        // gionee xuhz 20130225 modify for CR00773436 start
        if (mTranslateAnimation != null) {
            setStart.addAnimation(mTranslateAnimation);
        }
        // gionee xuhz 20130225 modify for CR00773436 end
//        setStart.addAnimation(mScaleAnimation);
        setStart.addAnimation(mAlphaAnimation);
        setStart.setDuration(20);
        mViewlLayout.startAnimation(setStart);
    
	}

    private void setEndView() {
        // gionee xuhz 20130304 add for CR00775077 start
    	if (setEnd != null && setEnd.hasStarted()) {
    		return;
    	}
    	// gionee xuhz 20130304 add for CR00775077 end
    	
		switch (mAnimType) {
		case ANIM_TO_TOP:
            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 100.0f);
            mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 1.5f);
            break;
		case ANIM_TO_BOTTOM:
            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, -100.0f);
            mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 1.5f);
            break;
		case ANIM_TO_RIGHT_AND_BOTTOM:
            mTranslateAnimation = new TranslateAnimation(0.0f, 100.0f, 0.0f, -100.0f);
            mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            break;
		case ANIM_TO_RIGHT_AND_TOP:
            mTranslateAnimation = new TranslateAnimation(0.0f, 100.0f, 0.0f, 100.0f);
            mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            break;
		}
        mAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        // gionee xuhz 20130225 modify for CR00773436 start
        if (mTranslateAnimation != null) {
            setEnd.addAnimation(mTranslateAnimation);
        }
        // gionee xuhz 20130225 modify for CR00773436 end
//        setEnd.addAnimation(mScaleAnimation);
        setEnd.addAnimation(mAlphaAnimation);
        setEnd.setDuration(200);
        mViewlLayout.startAnimation(setEnd);
        setEnd.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.d("setEndView", "onAnimationEnd");
                isFinishView = true;
                mViewlLayout.setVisibility(View.GONE);
                if (mSelectSim != SELECT_INVALID) {
                    // qc begin
                    if (GNContactsUtils.isOnlyQcContactsSupport()) {
                        mSelectedSimId = mSelectSim + 1;
                        mSlotId = mSelectSim;
                    } else {
                        mSelectedSimId = SIMInfoWrapper.getDefault().getSimIdBySlotId(mSelectSim);
                        mSlotId = mSelectSim;
                    }
                    // qc end
                	
                	if (!TextUtils.isEmpty(mCallUri)) {
            	        if(Constants.VOICEMAIL_URI.equals(mCallUri.toString())) {
            	            TelephonyManager telephonyManager =
            	                (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
            	            if(TextUtils.isEmpty(GnTelephonyManager.getVoiceMailNumberGemini(mSlotId))) {
            	                AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(mContext);
            	                builder.setTitle(R.string.no_vm_number)
            	                       .setMessage(R.string.no_vm_number_msg)
            	                       .setNegativeButton(android.R.string.no, (DialogInterface.OnClickListener)null)
            	                       .setPositiveButton(R.string.add_vm_number_str, new DialogInterface.OnClickListener() {
            	                        public void onClick(DialogInterface dialog, int which) {
            	                            Intent intent = new Intent(Intent.ACTION_MAIN);
            	                            intent.setClassName(Constants.PHONE_PACKAGE, "com.mediatek.settings.VoiceMailSetting");
            	                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	                            intent.putExtra("simId", mSlotId);
            	                            startActivity(intent);
            	                            finish();
            	                        }
            	                    });
            	                Dialog dialog = builder.create();
            	                dialog.setOnDismissListener(new OnDismissListener() {
    								@Override
    								public void onDismiss(DialogInterface dialog) {
    									finish();
    								}
    							});
            	                dialog.show();
            	                return;
            	            }
            	        }
                	}
                	Intent intent = sendIntentToPhone();
                	intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, mSelectedSimId);
                	intent.putExtra(Constants.EXTRA_SLOT_ID, mSlotId);
//                	intentSim1.putExtra(Phone.GEMINI_SIM_ID_KEY, mSlotId);
                	// Gionee:wangth 20130328 add for CR00790643 begin
                	if (GNContactsUtils.isOnlyQcContactsSupport() && ContactsApplication.isMultiSimEnabled) {
                	    intent.putExtra("subscription", mSlotId);
                	    //Gionee <wangth><2013-05-04> add for CR00807878 begin
                	    intent.putExtra(IntentFactory.KEY_AUTO_IP_DIAL_IF_SUPPORT, true);
                	    //Gionee <wangth><2013-05-04> add for CR00807878 end
                	}
                	// Gionee:wangth 20130328 add for CR00790643 end
                    startActivity(intent);
                }
                finish();
            }
        });
    }
    
	private Intent sendIntentToPhone() {
		Intent intent = null;
		if (!TextUtils.isEmpty(mNumber)) {
	        intent = IntentFactory.newDialNumberIntent(mNumber);
		} else if (!TextUtils.isEmpty(mCallUri)){
			intent = IntentFactory.newDialNumberIntent(Uri.parse(mCallUri));
			intent.setClassName(Constants.PHONE_PACKAGE, Constants.OUTGOING_CALL_BROADCASTER);
		}
        if (mIsVideoCall) {
            intent.putExtra(Constants.EXTRA_IS_VIDEO_CALL, true);
        }
        intent.putExtra(Constants.EXTRA_ORIGINAL_SIM_ID, mSelectedSimId);
    	return intent;
	}
	
    private void initView() {
        mViewlLayout = findViewById(R.id.view);
        mViewlLayout.requestFocus();
        mSim1Layout = findViewById(R.id.gn_anim_sim1_layout);
        mSim2Layout = findViewById(R.id.gn_anim_sim2_layout);
        mSim1Button = (ImageButton) findViewById(R.id.gn_anim_sim1);
        mSim2Button = (ImageButton) findViewById(R.id.gn_anim_sim2);
        mSim1Name = (TextView) findViewById(R.id.gn_anim_sim1_name);
        mSim2Name = (TextView) findViewById(R.id.gn_anim_sim2_name);
        mSim1Num = (TextView) findViewById(R.id.gn_anim_sim1_num);
        mSim2Num = (TextView) findViewById(R.id.gn_anim_sim2_num);

        mSim1Layout.setOnClickListener(this);
        mSim1Button.setOnClickListener(this);
        mSim1Name.setOnClickListener(this);
        mSim2Layout.setOnClickListener(this);
        mSim2Button.setOnClickListener(this);
        mSim2Name.setOnClickListener(this);
    }

	private void initAnimType() {
		if (mStartX == 0) {
			if (mStartY < 0) {
				mAnimType = ANIM_TO_BOTTOM;
			} else {
				mAnimType = ANIM_TO_TOP;
			}
		} else if (mStartX < 0) {
			if (mStartY + mDisplayMetrics.heightPixels/2 < 140) {
				mAnimType = ANIM_TO_LEFT_AND_BOTTOM;
			} else {
				mAnimType = ANIM_TO_LEFT_AND_TOP;
			}
		} else {
			if (mStartY + mDisplayMetrics.heightPixels/2 < 140) {
				mAnimType = ANIM_TO_RIGHT_AND_BOTTOM;
			} else {
				mAnimType = ANIM_TO_RIGHT_AND_TOP;
			}
		}
	}
	
    private void initIntent() {
        Intent intent = getIntent();
        Log.d("selectSim", "initIntent mStartX = "+mStartX);
        Log.d("selectSim", "initIntent mStarty = "+mStartY);
        Window window = getWindow();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.99f; //0.0-1.0
        lp.x = mStartX;
        if (mAnimType == ANIM_TO_BOTTOM ||  mAnimType == ANIM_TO_LEFT_AND_BOTTOM || mAnimType == ANIM_TO_RIGHT_AND_BOTTOM) {
        	lp.y = mStartY + 60;
        } else if (mAnimType == ANIM_TO_TOP) {
        	lp.y = mStartY - 70;
        } else {
        	lp.y = mStartY - 30;
        }
        /*if (mStartY > 0) {
            lp.y = mStartY - 70;
        } else if (mStartY + mDisplayMetrics.heightPixels/2 < 140){
            lp.y = mStartY + 70;
        } else {
            lp.y = mStartY + 4;
        }*/
        window.setAttributes(lp);
    }

    private int[] iconIds = {R.drawable.gn_anim_sim1, R.drawable.gn_anim_sim2,
            R.drawable.gn_anim_sim3, R.drawable.gn_anim_sim4};

    //Gionee <xuhz> 2013-06-17> add for CR00824673 begin
    private int getColorResId(SIMInfo simInfo) {
    	if (simInfo == null) {
    		return iconIds[0];
    	}
    	if (simInfo.mColor < 0 || simInfo.mColor > 3) {
    		return iconIds[0];
    	}
    	return iconIds[simInfo.mColor];
    }
    //Gionee <xuhz> 2013-06-17> add for CR00824673 end
    
    private void initSimInfo() {
        // qc begin
        if (GNContactsUtils.isOnlyQcContactsSupport() && ContactsApplication.isMultiSimEnabled && mSimCount > 1) {
            mSim1Name.setText(SIMInfoWrapper.getDefault().getSimDisplayNameBySlotId(0));
            mSim2Name.setText(SIMInfoWrapper.getDefault().getSimDisplayNameBySlotId(1));
            return;
        }
        // qc end
        //Gionee <xuhz> <2013-06-04> modify for CR00821838 begin
        //old: if (mSimInfoList != null && mSimCount > 1) {
    	if (mSimCount > 1) {
            SIMInfo simInfo1 = SIMInfoWrapper.getDefault().getSimInfoBySlot(0);
            //Gionee <xuhz> 2013-06-17> modify for CR00824673 begin
            //old: mSim1Button.setImageResource(iconIds[simInfo1.mColor]);
            mSim1Button.setImageResource(getColorResId(simInfo1));
            //Gionee <xuhz> 2013-06-17> modify for CR00824673 end
            SIMInfo simInfo2 = SIMInfoWrapper.getDefault().getSimInfoBySlot(1);
            //Gionee <xuhz> 2013-06-17> modify for CR00824673 begin
            //old: mSim2Button.setImageResource(iconIds[simInfo2.mColor]);
            mSim2Button.setImageResource(getColorResId(simInfo2));
            //Gionee <xuhz> 2013-06-17> modify for CR00824673 end
       //Gionee <xuhz> <2013-06-04> modify for CR00821838 end
            
            String name1 = simInfo1.mDisplayName;
            String name2 = simInfo2.mDisplayName;
            String num1 = "";
            if (!TextUtils.isEmpty(simInfo1.mNumber)) {
                switch(simInfo1.mDispalyNumberFormat) {
                    //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                        if(simInfo1.mNumber.length() <= 4)
                            num1 = simInfo1.mNumber;
                        else
                            num1 = simInfo1.mNumber.substring(0, 4);
                        break;
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                        if(simInfo1.mNumber.length() <= 4)
                            num1 = simInfo1.mNumber;
                        else
                            num1 = simInfo1.mNumber.substring(simInfo1.mNumber.length() - 4);
                        break;
                    case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        num1 = "";
                        break;
                }
            }
            String num2 = "";
            if (!TextUtils.isEmpty(simInfo2.mNumber)) {
                switch(simInfo2.mDispalyNumberFormat) {
                    //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                        if(simInfo2.mNumber.length() <= 4)
                            num2 = simInfo2.mNumber;
                        else
                            num2 = simInfo2.mNumber.substring(0, 4);
                        break;
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                        if(simInfo2.mNumber.length() <= 4)
                            num2 = simInfo2.mNumber;
                        else
                            num2 = simInfo2.mNumber.substring(simInfo2.mNumber.length() - 4);
                        break;
                    case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        num2 = "";
                        break;
                }
            }
            mSim1Num.setText(num1);
            mSim2Num.setText(num2);
            mSim1Name.setText(name1);
            mSim2Name.setText(name2);
    	}
    }

    private void getSimInfoList() {
        mDefaultSim = Settings.System.getLong(getContentResolver(),
                VOICE_CALL_SIM_SETTING, DEFAULT_SIM_NOT_SET);
        
        if (mDefaultSim < 0) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                //Gionee <xuhz> <2013-06-04> modify for CR00821838 begin
                //mSimInfoList = SIMInfo.getInsertedSIMList(mContext);
                //mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
                mSimCount = SIMInfoWrapper.getDefault().getInsertedSimCount();
                //Gionee <xuhz> <2013-06-04> modify for CR00821838 end
            } else { // single SIM
                ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
                if (phone != null) {
                    try {
                        mSimCount = phone.hasIccCard() ? 1 : 0;
                    } catch (RemoteException e) {
                        Log.e("SimSelectView", "check sim insert status failed");
                        mSimCount = 0;
                    }
                }
            }
            
            // qc begin
            if (GNContactsUtils.isOnlyQcContactsSupport() && ContactsApplication.isMultiSimEnabled) {
                final boolean isAlwaysAsk = GNContactsUtils.isAlwaysAsk();
                if (GNContactsUtils.isQCSimReady(0) && GNContactsUtils.isQCSimReady(1) && isAlwaysAsk) {
                    mSimCount = 2;
                }
            }
            // qc end
        }
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d("SelectSim", "onPause");
        if (!isFinishView) {
            finish();
        }
        super.onPause();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        Log.d("SelectSim", "onConfigurationChanged");
        finish();
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d("SelectSim", "onKeyDown   keyCode  =  "+keyCode);
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            setEndView();
            return true;

        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSelectSim == SELECT_SIM1 || mSelectSim == SELECT_SIM2) {
        	return true;
        }
        Log.d("SelectSim", "onTouchEvent"+event.getAction());
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            break;
        case MotionEvent.ACTION_UP:
            setEndView();
            break;

        default:
            break;
        }
        return true;
    }
    
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.gn_anim_sim1_layout:
        case R.id.gn_anim_sim1:
        case R.id.gn_anim_sim1_name:
        	mSelectSim = SELECT_SIM1;
            setEndView();
            break;
        case R.id.gn_anim_sim2_layout:
        case R.id.gn_anim_sim2:
        case R.id.gn_anim_sim2_name:
        	mSelectSim = SELECT_SIM2;
            setEndView();
            break;

        default:
            break;
        }
    }
}

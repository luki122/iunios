package com.gionee.mms.ui;

import java.util.List;

import com.android.mms.MmsApp;
import com.aurora.featureoption.FeatureOption;
import gionee.telephony.GnTelephonyManager;

import android.R.string;
import aurora.app.AuroraAlertDialog;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import aurora.widget.AuroraSwitch;
import android.widget.TextView;
import android.os.ServiceManager;
import gionee.provider.GnTelephony.SIMInfo;
import com.gionee.internal.telephony.GnPhone;
//import android.telephony.MSimTelephonyManager;
import gionee.provider.GnTelephony;
import com.android.mms.R;
import com.android.internal.telephony.ITelephony;
import com.gionee.mms.popup.PopUpMsgActivity;
import com.gionee.mms.popup.PopUpView;

public class SimSelectView extends Activity implements OnClickListener{

    private static Context mContext;
    private int mStartX = 0;
    private int mStarty = 0;
    private List<SIMInfo> mSimInfoList;
    private int mSimCount;
    private FrameLayout mSim1Layout;
    private FrameLayout mSim2Layout;
    private ImageButton mSim1Button;
    private ImageButton mSim2Button;
    private TextView mSim1Name;
    private TextView mSim2Name;

    private TextView mSim1Num;
    private TextView mSim2Num;

    private RelativeLayout mViewlLayout;

    private Animation mTranslateAnimation;
    private Animation mScaleAnimation;
    private Animation mAlphaAnimation = null;
    AnimationSet setStart = new AnimationSet(true);
    AnimationSet setEnd = new AnimationSet(true);

    private boolean isFinishView = false;
    private boolean mCheckSim = false;
    private String TAG = "SimSelectView";

    private boolean isPopUpView = false;
    private int mSimid = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        setContentView(R.layout.gn_anim_view);
        mContext = getApplicationContext();

        initView();
        initSimInfo();

        initIntent();
        setStartView();
    }

    private void initView() {
        // TODO Auto-generated method stub
        mViewlLayout = (RelativeLayout) findViewById(R.id.view);
        mViewlLayout.requestFocus();
        mSim1Layout = (FrameLayout) findViewById(R.id.gn_anim_sim1_layout);
        mSim2Layout = (FrameLayout) findViewById(R.id.gn_anim_sim2_layout);
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

    private int[] iconIds = {-1/*R.drawable.gn_anim_sim1*/, -1/*R.drawable.gn_anim_sim2*/,
            -1/*R.drawable.gn_anim_sim3*/, -1/*R.drawable.gn_anim_sim4*/};

    private void initSimInfo() {
        // TODO Auto-generated method stub
        getSimInfoList();
        SIMInfo simInfo1 = mSimInfoList.get(0);
        //Gionee <zhouyj> <2013-04-27> add for CR00803019 start
        if (simInfo1.mColor < 0 || simInfo1.mColor > iconIds.length) {
            simInfo1.mColor = 0;
        }
        //Gionee <zhouyj> <2013-04-27> add for CR00803019 end
        mSim1Button.setImageResource(iconIds[simInfo1.mColor]);
        SIMInfo simInfo2 = mSimInfoList.get(1);
        //Gionee <zhouyj> <2013-04-27> add for CR00803019 start
        if (simInfo2.mColor < 0 || simInfo2.mColor > iconIds.length) {
            simInfo2.mColor = 1;
        }
        //Gionee <zhouyj> <2013-04-27> add for CR00803019 end
        mSim2Button.setImageResource(iconIds[simInfo2.mColor]);

        String name1 = simInfo1.mDisplayName;
        String name2 = simInfo2.mDisplayName;
        String num1 = "";
        if (!TextUtils.isEmpty(simInfo1.mNumber)) {
            switch(simInfo1.mDispalyNumberFormat) {
                //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                case GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                    if(simInfo1.mNumber.length() <= 4)
                        num1 = simInfo1.mNumber;
                    else
                        num1 = simInfo1.mNumber.substring(0, 4);
                    break;
                case GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
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
                case GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                    if(simInfo2.mNumber.length() <= 4)
                        num2 = simInfo2.mNumber;
                    else
                        num2 = simInfo2.mNumber.substring(0, 4);
                    break;
                case GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
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

    private void getSimInfoList() {
        if (MmsApp.mGnMultiSimMessage) {
            mSimInfoList = SIMInfo.getInsertedSIMList(this);
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
        } else { // single SIM
            if (GnPhone.phone != null) {
                try {
                    
                    mSimCount = GnPhone.isSimInsert() ? 1 : 0;
                    
                } catch (Exception e) {
                    mSimCount = 0;
                }
            }
        }
    
        /*if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mSimInfoList = SIMInfo.getInsertedSIMList(mContext);
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
        } else { // single SIM
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                try {
                    mSimCount = phone.hasIccCard() ? 1 : 0;
                } catch (RemoteException e) {
                    Log.e(MmsApp.TXN_TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }*/
    }

    private void initIntent() {
        Intent intent = getIntent();
        mStartX = intent.getIntExtra("x", 0);
        mStarty = intent.getIntExtra("y", 0);
        Log.d(TAG, "initIntent mStartX = "+mStartX);
        Log.d(TAG, "initIntent mStarty = "+mStarty);
        isPopUpView = intent.getBooleanExtra("isPop", false);
        Window window = getWindow();
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.99f; //0.0-1.0
        if (!isPopUpView) {
            lp.x = mStartX - 20;
            if (mStarty > 0) {
                lp.y = mStarty - 76;
            } else {
                lp.y = mStarty;
            }
        } else {
            lp.x = mStartX;
            lp.y = mStarty;
        }
        window.setAttributes(lp);
    }

    private void setStartView() {
        if (!isPopUpView) {
            if (mStarty > 0) {
                //mViewlLayout.setBackgroundResource(R.drawable.gn_anim_down_bg);
                mTranslateAnimation = new TranslateAnimation(100.0f, 0.0f, 100.0f, 0.0f);
            } else {
                //mViewlLayout.setBackgroundResource(R.drawable.gn_anim_bg);
                mTranslateAnimation = new TranslateAnimation(100.0f, 0.0f, 0.0f, 0.0f);
            }
        } else {
            //mViewlLayout.setBackgroundResource(R.drawable.gn_anim_middle_bottom_bg);
            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 100.0f, 0.0f);
        }
        mAlphaAnimation = new AlphaAnimation(0.0f, 1.0f);

        setStart.addAnimation(mTranslateAnimation);
//        setStart.addAnimation(mScaleAnimation);
        setStart.addAnimation(mAlphaAnimation);
        setStart.setDuration(20);
        mViewlLayout.startAnimation(setStart);
    }

    private void setEndView() {
        if (!isPopUpView) { 
            if (mStarty > 0) { 
                mTranslateAnimation = new TranslateAnimation(0.0f, 50.0f, 0.0f, 50.0f);
                mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 1.5f);
            } else {
                mTranslateAnimation = new TranslateAnimation(0.0f, 50.0f, 0.0f, 0.0f);
                mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 2.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            }
        } else {
            mTranslateAnimation = new TranslateAnimation(0.0f, 0.0f, 0.0f, 100.0f);
            mScaleAnimation = new ScaleAnimation(1.0f, 0.3f, 1.0f, 0.3f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.5f);
        }
        mAlphaAnimation = new AlphaAnimation(1.0f, 0.0f);

        setEnd.addAnimation(mTranslateAnimation);
        setEnd.addAnimation(mScaleAnimation);
        setEnd.addAnimation(mAlphaAnimation);
        setEnd.setDuration(200);
        mViewlLayout.startAnimation(setEnd);
        setEnd.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                // TODO Auto-generated method stub
                Log.d(TAG, "onAnimationEnd");
                isFinishView = true;
                if (!mCheckSim) {
                    setResult(RESULT_OK, new Intent("finish"));
                }
                if (isPopUpView) {
                    Intent intent = new Intent(PopUpMsgActivity.SIM_INFO_RECEIVER_ACTION);
                    intent.putExtra("simid", mSimid);
                    sendBroadcast(intent);
                }
                finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onClick  v.getId() = "+v.getId());
        switch (v.getId()) {
        case R.id.gn_anim_sim1_layout:
        case R.id.gn_anim_sim1:
        case R.id.gn_anim_sim1_name:
            mCheckSim = true;
            //gionee gaoj 2013-2-17 modified for CR00771326 start
            mSimid = (int) mSimInfoList.get(0).mSimId;
            //gionee gaoj 2013-2-17 modified for CR00771326 end
            setResult(RESULT_OK, new Intent("sim1"));
            break;
        case R.id.gn_anim_sim2_layout:
        case R.id.gn_anim_sim2:
        case R.id.gn_anim_sim2_name:
            mCheckSim = true;
            //gionee gaoj 2013-2-17 modified for CR00771326 start
            mSimid = (int) mSimInfoList.get(1).mSimId;
            //gionee gaoj 2013-2-17 modified for CR00771326 end
            setResult(RESULT_OK, new Intent("sim2"));
            break;

        default:
            break;
        }
        setEndView();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        Log.d(TAG, "onPause");
        if (!isFinishView) {
            setEndView();
        }
        super.onPause();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onTouchEvent"+event.getAction());
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
//            setEndView();
            break;
        case MotionEvent.ACTION_UP:
            setEndView();
            break;

        default:
            break;
        }
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onConfigurationChanged");
        setEndView();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        Log.d(TAG, "onKeyDown   keyCode  =  "+keyCode);
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            setEndView();
            return true;

        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
}

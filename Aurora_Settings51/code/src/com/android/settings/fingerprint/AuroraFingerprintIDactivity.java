package com.android.settings.fingerprint;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.aurora.svgview.PathView;
import com.gionee.fingerprint.IGnIdentifyCallback;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraAlertActivity;

/**
 * Created by joy on 1/27/16.
 */
public class AuroraFingerprintIDactivity extends AuroraAlertActivity {

    private TextView msgTv;
    private static final int CONFIRM_PASSWD_REQUEST = 101;
    private AuroraFingerprintUtils mFingerprintUtils;
    private int[] mFingerIdList;
    private int backId = -1;
    private boolean needExitFinish = true;
    public static final int IDENTIFY_SUCCESSED = 201;
    public static final int IDENTIFY_PAUSED = 202;
    public static final int IDENTIFY_CANCELED = 203;
    public static final int IDENTIFY_FAILED = 204;
    public static final String IS_NEED_PASSWD = "is_need_passwd";
    private PathView mPathView;
    private boolean ID_FLAG = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initParams();
        setupAlert();
        initView();
        initData();

        if (getIntent().getBooleanExtra(IS_NEED_PASSWD, false)) {
            launchPasswdID();
        } else {
            startIdentifyFinger(true);
        }
        setFinishOnTouchOutside(false);
    }

    private void initParams() {
        View view = LayoutInflater.from(this).inflate(R.layout.identify_activity_layout, null);
        mPathView = (PathView) view.findViewById(R.id.pathView);
        mPathView.setPercentage(1);
        //mPathView.setFillAfter(true);
        //mPathView.useNaturalColors();
        //fingerIdAnim();

        mAlertParams.mView = view;
        mAlertParams.mNegativeButtonText = getString(R.string.cancel);
        mAlertParams.mNegativeButtonListener = new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                setResult(IDENTIFY_CANCELED);
                dismiss();
            }
        };
    }

    private void fingerIdAnimFirst() {
        ID_FLAG = false;
        isAnimRunning = true;
        mPathView.getPathAnimator(mListenEnd).
                setAnimDirect(mPathView, true).
                duration(700).
                interpolator(new AccelerateInterpolator(2)).
                start();
    }

    private void fingerIdAnimLast() {
        ID_FLAG = true;
        isAnimRunning = true;
        mPathView.getPathAnimator(mListenEnd).
                setAnimDirect(mPathView, false).
                duration(700).
                interpolator(new AccelerateInterpolator(2)).
                start();
    }

    private boolean isAnimRunning = false;
    PathView.AnimatorBuilder.ListenerEnd mListenEnd = new PathView.AnimatorBuilder.ListenerEnd() {
        @Override
        public void onAnimationEnd() {
            isAnimRunning = false;
            if (isConfirOk) {
                confirmOkFinish();
            } else {
                fingerIdAnimLast();
                if (ID_FLAG) {
                    mFingerHandler.sendEmptyMessageDelayed(AuroraFingerprintUtils.MSG_REIDENTIFY, 800);
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        cancelIdentifyFinger();
        mHandler.removeCallbacks(mStartIdentifyRunnable);
        if (needExitFinish) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initData() {
        mFingerprintUtils = new AuroraFingerprintUtils();
        mFingerIdList = getIntent().getIntArrayExtra(AuroraFingerprintMainActivity.FINGER_ID_KEY);
    }

    private void initView() {
        msgTv = (TextView) findViewById(R.id.msg_tv);
    }

    private Handler mHandler = new Handler();

    private void startIdentifyFinger(boolean delay) {
        if (delay) {
            mHandler.postDelayed(mStartIdentifyRunnable, 500);
        } else {
            Log.d("Fingerprint_Settings", "startIdentifyFinger()---");
            mFingerprintUtils.startIdentify(identifyCb, mFingerIdList);
        }
    }

    private Runnable mStartIdentifyRunnable = new Runnable() {
        public void run() {
            Log.d("Fingerprint_Settings", "startIdentifyFinger()---for delay 500ms");
            mFingerprintUtils.startIdentify(identifyCb, mFingerIdList);
        }
    };

    private void cancelIdentifyFinger() {
        mFingerprintUtils.cancel();
    }

    private int errorNum = 0;
    private final int MAX_ERROR_NUM = 5;
    private boolean isConfirOk = false;
    // identify callback
    private IGnIdentifyCallback identifyCb = new IGnIdentifyCallback() {

        public void onWaitingForInput() {
            Log.d("Fingerprint_Settings", "onWaitingForInput()---");
        }

        public void onInput() {
            Log.d("Fingerprint_Settings", "onInput()--- " + isAnimRunning + " " + System.currentTimeMillis());
            //fingerIdAnimFirst();
        }

        public void onCaptureCompleted() {
            Log.d("Fingerprint_Settings", "onCaptureCompleted()--- " + isAnimRunning);
            fingerIdAnimFirst();
        }

        public void onCaptureFailed(int reason) {
            Log.d("Fingerprint_Settings", "onCaptureFailed()--- " + System.currentTimeMillis());
            //fingerIdAnimLast();
        }

        public void onIdentified(int fingerId, boolean updated) {
            Log.d("Fingerprint_Settings", "onIdentified()--- " + System.currentTimeMillis());
            backId = fingerId;
            isConfirOk = true;
            //confirmOkFinish();
        }

        public void onNoMatch(int reason) {
            Log.d("Fingerprint_Settings", "onNoMatch()---reason= " + reason + " " + System.currentTimeMillis());
            //fingerIdAnimLast();

            if (reason == AuroraFingerprintUtils.REASON_NOMATCH) {
                showError();
                errorNum++;
                if (errorNum >= MAX_ERROR_NUM) {
                    // to do
                    launchPasswdID();
                }/* else {
                    mFingerHandler.sendEmptyMessageDelayed(AuroraFingerprintUtils.MSG_REIDENTIFY, 300);
                }*/
            } else if (reason == AuroraFingerprintUtils.REASON_TIMEOUT) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }

        public void onExtIdentifyMsg(Message msg, String description) {
            Log.d("Fingerprint_Settings", "onExtIdentifyMsg()---");
        }
    };

    private Handler mFingerHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case AuroraFingerprintUtils.MSG_REIDENTIFY:
                    startIdentifyFinger(false);
                    msgTv.setText(getString(R.string.comfirm_finger_text));
                    break;
                case AuroraFingerprintUtils.MSG_REFRESH_HEADER:
                    break;
                case AuroraFingerprintUtils.MSG_DEFAULT_HEADER:
                    break;
                default:
                    break;
            }
        }
    };

    private void launchPasswdID() {
        needExitFinish = false;
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(AuroraFingerprintIDactivity.this);
        helper.launchConfirmationExitActivity(
                CONFIRM_PASSWD_REQUEST, null, null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONFIRM_PASSWD_REQUEST) {
            needExitFinish = true;
            if (resultCode == RESULT_OK) {
                errorNum = 0;
                startIdentifyFinger(true);
            } else {
                setResult(IDENTIFY_FAILED);
                finish();
            }
        }
    }

    private void confirmOkFinish() {
        Intent backIntent = new Intent();
        backIntent.putExtra(AuroraFingerprintMainActivity.FINGER_ID_KEY, backId);
        setResult(IDENTIFY_SUCCESSED, backIntent);
        finish();
    }

    private void showError() {
        msgTv.setText(getString(R.string.not_identified_msg));
    }

}
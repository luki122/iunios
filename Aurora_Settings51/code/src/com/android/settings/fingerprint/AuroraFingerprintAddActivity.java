package com.android.settings.fingerprint;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.settings.R;
import com.aurora.utils.SystemUtils;
import com.gionee.fingerprint.IGnEnrolCallback;
import com.mediatek.settings.sim.Log;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTextView;

public class AuroraFingerprintAddActivity extends AuroraActivity {
    private static final String TAG = "AuroraFingerprintAddActivity";

    private static final int ADD_FINGER_START = 0;
    private static final int ADD_FINGER_PROGRESS = 1;
    private static final int ADD_FINGER_COMPLETE = 2;
    private static final int ADD_FINGER_FAIL = 3;
    private static final int ADD_FINGER_TIMEOUT = 4;
    private static final int FRAME_DURATION = 45; //ms
    private static final int FRAME_DURATION_FIRST = 13; //ms
    private AuroraTextView mDisplayHint2;
    private AuroraTextView mDisplayHint1;
    private AuroraTextView mNameView;
    private AuroraButton mButtonOk;
    private AuroraButton mButtonBack;
    private AuroraFingerprintUtils mFingerPrintUtils;
    private ImageView mPressView;
    private ImageView mInsideIvView;
    AnimationDrawable pressAnim;
    private int mAddFingerState = 0;
    private int pressFrameIndex = 0;
    private int mCaptureFailCount = 0;
    private int mCaptureFastCount = 0;
    private int mHint1ResId = 0;
    private int mProgress = 0;
    private Handler mHandler = new Handler();
    private PowerManager.WakeLock mWakeLock;
    private int returnId = -1;
    public static final String RETURN_FINGER_ID = "finger_add_return_id";
    private String mFingerName;
    private static final int MAX_NAME_LEN = 10;
    private String[] mFingerNameList;
    private final int WAKE_LOCK_TIME = 90 * 1000;

    private static final int FINGER_PRESS_ANIM[] = {
            R.drawable.fingerprint_inside_00,
            R.drawable.fingerprint_inside_01,
            R.drawable.fingerprint_inside_02,
            R.drawable.fingerprint_inside_03,
            R.drawable.fingerprint_inside_04,
            R.drawable.fingerprint_inside_05,
            R.drawable.fingerprint_inside_06,
            R.drawable.fingerprint_inside_07,
            R.drawable.fingerprint_inside_08,
            R.drawable.fingerprint_inside_09,
            R.drawable.fingerprint_inside_10,
            R.drawable.fingerprint_inside_11,
            R.drawable.fingerprint_inside_12,
            R.drawable.fingerprint_inside_13,
            R.drawable.fingerprint_inside_14,
            R.drawable.fingerprint_inside_15,
            R.drawable.fingerprint_inside_16,
            R.drawable.fingerprint_inside_17,
            R.drawable.fingerprint_inside_18,
            R.drawable.fingerprint_inside_19,
            R.drawable.fingerprint_inside_20,
            R.drawable.fingerprint_inside_21,
            R.drawable.fingerprint_inside_22,
            R.drawable.fingerprint_inside_23,
            R.drawable.fingerprint_inside_24,
            R.drawable.fingerprint_inside_25,
            R.drawable.fingerprint_inside_26,
            R.drawable.fingerprint_inside_27,
            R.drawable.fingerprint_inside_28,
            R.drawable.fingerprint_inside_29,
            R.drawable.fingerprint_inside_30,
            R.drawable.fingerprint_inside_31,
            R.drawable.fingerprint_inside_32,
            R.drawable.fingerprint_inside_33,
            R.drawable.fingerprint_inside_34,
            R.drawable.fingerprint_inside_35,
            R.drawable.fingerprint_inside_36,
            R.drawable.fingerprint_inside_37,
            R.drawable.fingerprint_inside_38,
            R.drawable.fingerprint_inside_39,
            R.drawable.fingerprint_inside_40,
            R.drawable.fingerprint_inside_41,
            R.drawable.fingerprint_inside_42,
            R.drawable.fingerprint_inside_43,
            R.drawable.fingerprint_inside_44,
            R.drawable.fingerprint_inside_45,
            R.drawable.fingerprint_inside_46,
            R.drawable.fingerprint_inside_47,
            R.drawable.fingerprint_inside_48,
            R.drawable.fingerprint_inside_49,

            R.drawable.fingerprint_outside_00,
            R.drawable.fingerprint_outside_01,
            R.drawable.fingerprint_outside_02,
            R.drawable.fingerprint_outside_03,
            R.drawable.fingerprint_outside_04,
            R.drawable.fingerprint_outside_05,
            R.drawable.fingerprint_outside_06,
            R.drawable.fingerprint_outside_07,
            R.drawable.fingerprint_outside_08,
            R.drawable.fingerprint_outside_09,
            R.drawable.fingerprint_outside_10,
            R.drawable.fingerprint_outside_11,
            R.drawable.fingerprint_outside_12,
            R.drawable.fingerprint_outside_13,
            R.drawable.fingerprint_outside_14,
            R.drawable.fingerprint_outside_15,
            R.drawable.fingerprint_outside_16,
            R.drawable.fingerprint_outside_17,
            R.drawable.fingerprint_outside_18,
            R.drawable.fingerprint_outside_19,
            R.drawable.fingerprint_outside_20,
            R.drawable.fingerprint_outside_21,
            R.drawable.fingerprint_outside_22,
            R.drawable.fingerprint_outside_23,
            R.drawable.fingerprint_outside_24,
            R.drawable.fingerprint_outside_25,
            R.drawable.fingerprint_outside_26,
            R.drawable.fingerprint_outside_27,
            R.drawable.fingerprint_outside_28,
            R.drawable.fingerprint_outside_29,
            R.drawable.fingerprint_outside_30,
            R.drawable.fingerprint_outside_31,
            R.drawable.fingerprint_outside_32,
            R.drawable.fingerprint_outside_33,
            R.drawable.fingerprint_outside_34,
            R.drawable.fingerprint_outside_35,
            R.drawable.fingerprint_outside_36,
            R.drawable.fingerprint_outside_37,
            R.drawable.fingerprint_outside_38,
            R.drawable.fingerprint_outside_39,
            R.drawable.fingerprint_outside_40,
            R.drawable.fingerprint_outside_41,
            R.drawable.fingerprint_outside_42,
            R.drawable.fingerprint_outside_43,
            R.drawable.fingerprint_outside_44,
            R.drawable.fingerprint_outside_45,
            R.drawable.fingerprint_outside_46,
            R.drawable.fingerprint_outside_47,
            R.drawable.fingerprint_outside_48,
            R.drawable.fingerprint_outside_49,
    };

    private final AuroraButton.OnClickListener mSuccessListener = new AuroraButton.OnClickListener() {

        public void onClick(View v) {
            Log.d(TAG, "Button onClick mAddFingerState=" + mAddFingerState);

            if (mAddFingerState == ADD_FINGER_COMPLETE) {
                Intent intent = new Intent();
                intent.putExtra(RETURN_FINGER_ID, returnId);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                rePrintFinger();
            }
            /*else if (mAddFingerState == ADD_FINGER_TIMEOUT) {
                Intent intent = new Intent();
                intent.putExtra(AuroraFingerprintMainActivity.FAIL_REASON, AuroraFingerprintMainActivity.FAIL_TIMEOUT);
                setResult(RESULT_CANCELED, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();*/
        }
    };

    private final AuroraButton.OnClickListener mBackListener = new AuroraButton.OnClickListener() {
        public void onClick(View v) {
            Log.d(TAG, "Button onClick mAddFingerState=" + mAddFingerState);
            if (mAddFingerState == ADD_FINGER_COMPLETE) {
                setResult(RESULT_OK);
            } else if (mAddFingerState == ADD_FINGER_TIMEOUT) {
                Intent intent = new Intent();
                intent.putExtra(AuroraFingerprintMainActivity.FAIL_REASON, AuroraFingerprintMainActivity.FAIL_TIMEOUT);
                setResult(RESULT_CANCELED, intent);
            } else {
                setResult(RESULT_CANCELED);
            }
            finish();
        }
    };

    private Runnable mVibratorRunnable = new Runnable() {
        public void run() {
            vibrator();
        }
    };
    private View.OnClickListener mNameListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            showEditFingerDialog();
        }
    };

    private final TextWatcher mNameWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() > 0) {
                if (s.length() > MAX_NAME_LEN) {
                    String newStr = s.toString().substring(0, MAX_NAME_LEN);
                    mFingerNameEdit.setText(newStr);
                    mFingerNameEdit.setSelection(newStr.length());
                    Toast.makeText(AuroraFingerprintAddActivity.this,
                            R.string.aurora_fingerprint_name_len_note, Toast.LENGTH_SHORT).show();
                }
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
            } else {
                alertDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private AuroraAlertDialog alertDialog = null;
    private boolean isEditTvInit = false;
    private AuroraEditText mFingerNameEdit = null;
    private boolean cancelFlag = false;

    private void showEditFingerDialog() {
        if (!isEditTvInit) {
            isEditTvInit = true;
            mFingerNameEdit = new AuroraEditText(this);
            mFingerNameEdit.setText(mFingerName);
            mFingerNameEdit.setMaxLines(1);
            mFingerNameEdit.requestFocus();
            mFingerNameEdit.addTextChangedListener(mNameWatcher);
            mFingerNameEdit.setPadding(0, 30, 0, 48);
        }

        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
        builder.setView(mFingerNameEdit)
                .setTitle(R.string.rename_fingerprint)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e(TAG, "mFingerNameEdit.getText = " + mFingerNameEdit.getText());
                        String newName = mFingerNameEdit.getText().toString();
                        if (saveFingerName(newName)) {
                            mFingerName = newName;
                            mNameView.setText(newName);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        alertDialog.show();
        if (mFingerName != null && mFingerName.length() > 0) {
            Log.e(TAG, "mName.length = " + mFingerName.length());
            mFingerNameEdit.setText(mFingerName);
            mFingerNameEdit.setSelection(0, mFingerName.length());
        }
    }

    private boolean saveFingerName(String newName) {
        if (newName == null || newName.length() == 0) {
            Toast.makeText(AuroraFingerprintAddActivity.this,
                    R.string.aurora_fingerprint_edit_save_note, Toast.LENGTH_SHORT).show();
        }
        if (mFingerName.equals(newName)) {
            alertDialog.dismiss();
        }
        if (isFingerNameExist(newName)) {
            Toast.makeText(AuroraFingerprintAddActivity.this,
                    R.string.aurora_fingerprint_name_exist, Toast.LENGTH_SHORT).show();
            return false;
        }
        mFingerPrintUtils.renameById(returnId, newName);
        return true;
    }

    /*@Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, com.aurora.R.anim.aurora_activity_close_exit);
    }*/

    @Override
    protected void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        Log.d(TAG, "onCreate()");
        setAuroraContentView(R.layout.aurora_fingerprint_add, AuroraActionBar.Type.Normal);
        initAcitonBar();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "AuroraFingerprintAddActivity");
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(WAKE_LOCK_TIME);

        initViews();
        setListeners();

        returnId = getIntent().getIntExtra(AuroraFingerprintMainActivity.KEY_FINGER_ID, 0);
        mFingerName = getIntent().getStringExtra(AuroraFingerprintMainActivity.KEY_FINGER_NAME);
        mFingerPrintUtils = new AuroraFingerprintUtils();
        initData();

        /*mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFingerPrintUtils.startEnrol(enrolCb, returnId, mFingerName);
            }
        }, 10000);*/
        mFingerPrintUtils.startEnrol(enrolCb, returnId, mFingerName);

        refreshView();
    }

    private void initData() {
        mFingerNameList = mFingerPrintUtils.getNames();
        pressFrameIndex = 0;
        mHint1ResId = R.string.aurora_fingerprint_placing_finger;
        mAddFingerState = 0;
        pressFrameIndex = 0;
        mCaptureFailCount = 0;
        mCaptureFastCount = 0;
        mHint1ResId = 0;
    }

    private boolean isFingerNameExist(String name) {
        if (mFingerNameList != null) {
            for (int i = 0; i < mFingerNameList.length; i++) {
                if (mFingerNameList[i].compareTo(String.valueOf(name)) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void rePrintFinger() {
        cancelFlag = false;
        mWakeLock.release();

        initData();
        mDisplayHint1.setText(R.string.aurora_fingerprint_note2);
        mDisplayHint2.setText(R.string.aurora_fingerprint_placing_finger);

        mFingerPrintUtils.startEnrol(enrolCb, returnId, mFingerName);
        mButtonBack.setVisibility(View.VISIBLE);
        mButtonOk.setVisibility(View.INVISIBLE);
        mPressView.setBackground(null);

        mWakeLock.acquire(WAKE_LOCK_TIME);
    }

    private void setListeners() {
        mButtonOk.setOnClickListener(mSuccessListener);
        mButtonBack.setOnClickListener(mBackListener);
        mNameView.setOnClickListener(mNameListener);
    }

    private void initViews() {
        mDisplayHint2 = (AuroraTextView) findViewById(R.id.finger_hint2);
        mNameView = (AuroraTextView) findViewById(R.id.finger_name);
        mDisplayHint1 = (AuroraTextView) findViewById(R.id.finger_hint1);
        mPressView = (ImageView) findViewById(R.id.press_anim);
        mInsideIvView = (ImageView) findViewById(R.id.img_inside);
        mButtonOk = (AuroraButton) findViewById(R.id.set_success);
        mButtonBack = (AuroraButton) findViewById(R.id.set_back);
    }

    private void initAcitonBar() {
        AuroraActionBar actionBar = getAuroraActionBar();
        actionBar.setVisibility(View.GONE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            com.aurora.utils.SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_BLACK,
                    AuroraFingerprintAddActivity.this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        //refreshView();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause()");
        mHandler.removeCallbacks(mVibratorRunnable);
        mWakeLock.release();

        if (!cancelFlag && mAddFingerState != ADD_FINGER_COMPLETE) {
            mFingerPrintUtils.cancel();
        }
        Intent intent = new Intent();
        intent.putExtra(AuroraFingerprintMainActivity.FAIL_REASON, AuroraFingerprintMainActivity.FAIL_PAUSE);
        finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pressAnim != null) {
            pressAnim.stop();
        }
    }

    private void refreshView() {
        Log.e(TAG, "refreshView mAddFingerState = " + mAddFingerState + " mProgress = " + mProgress);
        switch (mAddFingerState) {
            case ADD_FINGER_START:
                mNameView.setVisibility(View.GONE);
                mButtonOk.setVisibility(View.GONE);
                mPressView.setVisibility(View.GONE);
                break;
            case ADD_FINGER_PROGRESS:
                mNameView.setVisibility(View.GONE);
                mDisplayHint1.setText(R.string.aurora_fingerprint_note2);
                mDisplayHint2.setText(R.string.aurora_fingerprint_placing_finger);
                mButtonOk.setVisibility(View.GONE);
                mPressView.setVisibility(View.VISIBLE);
                break;
            case ADD_FINGER_COMPLETE:
                mNameView.setVisibility(View.VISIBLE);
                mNameView.setText(mFingerName);
                mDisplayHint1.setText(R.string.aurora_fingerprint_set_success);
                mDisplayHint2.setText(R.string.aurora_fingerprint_note4);
                mButtonOk.setVisibility(View.VISIBLE);
                mButtonOk.setText(R.string.wizard_finish);
                mButtonBack.setVisibility(View.INVISIBLE);
                mPressView.setVisibility(View.VISIBLE);
                mPressView.setBackgroundResource(R.drawable.fingerprint_success);
                break;
            case ADD_FINGER_FAIL:
                mNameView.setVisibility(View.GONE);
                mDisplayHint1.setText(R.string.aurora_fingerprint_set_fail);
                mDisplayHint2.setText(R.string.aurora_fingerprint_add_fail_note);
                mButtonOk.setVisibility(View.VISIBLE);
                mButtonOk.setText(R.string.aurora_fingerprint_retry);
                mPressView.setVisibility(View.VISIBLE);
                if (mProgress < 100) {
                    mPressView.setBackgroundResource(R.drawable.fingerprint_add_failed);
                    Log.e(TAG, "ADD_FINGER_FAIL " + mProgress);
                }
                break;
            case ADD_FINGER_TIMEOUT:
                mNameView.setVisibility(View.GONE);
                mDisplayHint1.setText(R.string.aurora_fingerprint_set_fail);
                mDisplayHint2.setText(R.string.aurora_fingerprint_timeout_note);
                mButtonOk.setVisibility(View.VISIBLE);
                mButtonOk.setText(R.string.aurora_fingerprint_retry);
                mPressView.setVisibility(View.VISIBLE);
                if (mProgress < 100) {
                    mPressView.setBackgroundResource(R.drawable.fingerprint_add_failed);
                    Log.e(TAG, "ADD_FINGER_TIMEOUT " + mProgress);
                }
                break;
            default:
                break;
        }
    }

    private AnimationDrawable createPressAnimation(int startFrame, int frameCount) {
        AnimationDrawable anim = new AnimationDrawable();
        anim.setOneShot(true);
        //int totalTime = 1000;
        int frameTime;
        if (startFrame == 0) {
            frameTime = FRAME_DURATION_FIRST;
        } else {
            frameTime = FRAME_DURATION;
        }
        for (int i = startFrame; i < startFrame + frameCount; i++) {
            if (i < FINGER_PRESS_ANIM.length) {
                Drawable frame = getResources().getDrawable(FINGER_PRESS_ANIM[i]);
                anim.addFrame(frame, frameTime);
            }
        }
        anim.setLevel(10000);
        return anim;
    }

    private void vibrator() {
        Log.e(TAG, "vibrator");
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(100);
        }
    }

    private void displayPressAnimFirstFrame() {
        pressAnim = createPressAnimation(0, 4);
        mPressView.setBackground((Drawable) pressAnim);
        pressAnim.start();
    }

    private void displayPressAnim(int startFrame, int frameCount) {
        Log.e(TAG, "startFrame = " + startFrame + " frameCount = " + frameCount + " mProgress " + mProgress);
        if (pressAnim != null) {
            pressAnim.stop();
            pressAnim = null;
        }
        if (frameCount > 0) {
            pressAnim = createPressAnimation(startFrame, frameCount);
            mPressView.setBackground((Drawable) pressAnim);
            pressAnim.start();
        }
    }

    private void playAnimEndVibrator(int time) {
        mHandler.postDelayed(mVibratorRunnable, time);
    }

    private void captureFail() {
        Log.e(TAG, "captureFail > 7 " + mCaptureFailCount);
        cancelFlag = true;
        mFingerPrintUtils.cancel();
        mAddFingerState = ADD_FINGER_FAIL;
        refreshView();
    }

    private void enrolFail() {
        Log.e(TAG, "captureFail > 7 " + mCaptureFailCount);
        cancelFlag = true;
        //mFingerPrintUtils.cancel();
        mAddFingerState = ADD_FINGER_FAIL;
        refreshView();
    }

    private void switchText(int resId, int progress) {
        if (resId != mHint1ResId && progress != 100) {
            mDisplayHint2.startAnimation(loadAnim(R.anim.slide_left_out, TextAnimListener));
            mHint1ResId = resId;
        }
    }

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(this, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

    Animation.AnimationListener TextAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationEnd(Animation arg0) {
            mDisplayHint2.setText(mHint1ResId);
            mDisplayHint2.startAnimation(loadAnim(R.anim.slide_right_in, null));
        }

        @Override
        public void onAnimationRepeat(Animation arg0) {
        }

        @Override
        public void onAnimationStart(Animation arg0) {
        }
    };

    private int mFrameCount = 16;

    private void analyzeOnProgressMsg(Bundle bundle) {
        boolean bAcceptance = bundle.getInt("guidedAcceptance") == 1;
        boolean bImmobile = bundle.getBoolean("guidedImmobile", false);
        boolean bStitched = bundle.getBoolean("guidedStitched", false);
        boolean bLowCoverage = bundle.getBoolean("guidedLowCoverage", false);
        boolean bLowQuality = bundle.getBoolean("guidedLowQuality", false);

        int progress = bundle.getInt("guidedProgress");
        if (progress > 50 && mProgress < 50) {
            progress = 50;
        }

        int frameCount = (FINGER_PRESS_ANIM.length * progress) / 100 - pressFrameIndex;
        if (frameCount == 0) {
            frameCount = 4;
        }
        //int frameCount = 4;
        Log.d(TAG, "onProgress()-- " + "progress = " + progress +
                " frameCount = " + frameCount + " pressFrameIndex=" + pressFrameIndex);
        mCaptureFastCount = 0;

        String strFeedback = null;
        if (bAcceptance) {
            if (!bStitched) {
                strFeedback = "Latest Touch outside enrolled area";
                switchText(R.string.aurora_fingerprint_add_status2, progress);
            } else if (!bImmobile) {
                strFeedback = "Good Finger Placement";
                //vibrator();
                switchText(R.string.aurora_fingerprint_placing_finger, progress);
                mCaptureFailCount = 0;
            } else {
                strFeedback = "Touch too similar to the previous one";
                switchText(R.string.aurora_fingerprint_add_status3, progress);
                mCaptureFailCount++;
            }
        } else {
            if (bLowQuality) {
                strFeedback = "Rejected due to poor image quality";
                switchText(R.string.aurora_fingerprint_add_status4, progress);
                mCaptureFailCount++;
            } else if (bLowCoverage) {
                strFeedback = "Rejected due to low Sensor Coverage";
            } else { // Other error
                strFeedback = "Touch rejected";
            }
        }
        //Log.d(TAG, "onProgress()--	strFeedback=" + strFeedback);

        Log.e(TAG, "progress " + progress + " mProgress " + mProgress);
        if (mProgress != 0 && mProgress == progress) {
            pressFrameIndex -= mFrameCount;
            if (pressFrameIndex < 0) {
                pressFrameIndex = 0;
            }
        }
        mProgress = progress;
        /*if (mCaptureFailCount >= 7) {
            captureFail();
            playAnimEndVibrator(FRAME_DURATION);
        } else {*/
        if (progress > 50) {
            mInsideIvView.setVisibility(View.VISIBLE);
        }
        if (progress == 0) {
            Log.e(TAG, "displayPressAnimFirstFrame");
            displayPressAnimFirstFrame();
            playAnimEndVibrator(FRAME_DURATION);
        } else {
            displayPressAnim(pressFrameIndex, frameCount);
            playAnimEndVibrator(FRAME_DURATION * frameCount);
            pressFrameIndex = pressFrameIndex + frameCount;
            mFrameCount = frameCount;
        }
        /*}*/
        mProgress = progress;
        if (progress == 100) {
            mAddFingerState = ADD_FINGER_COMPLETE;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private IGnEnrolCallback enrolCb = new IGnEnrolCallback() {

        public void onWaitingForInput() {
            Log.d(TAG, "onWaitingForInput()---");
        }

        public void onInput() {
            Log.d(TAG, "onInput()-- pressFrameIndex = " + pressFrameIndex);
            if (mAddFingerState == ADD_FINGER_START) {
                mAddFingerState = ADD_FINGER_PROGRESS;
                refreshView();
            }
        }

        public void onCaptureCompleted() {
            Log.d(TAG, "onCaptureCompleted()--");
        }

        public void onCaptureFailed(int reason) {
            Log.d(TAG, "onCaptureFailed()--reason = " + reason + " mCaptureFastCount = " + mCaptureFastCount);
            if (reason == 1) {
                mCaptureFastCount++;
                mCaptureFailCount++;

                if (mCaptureFastCount >= 3) {
                    switchText(R.string.aurora_fingerprint_add_status1, 0);
                    mCaptureFastCount = 0;
                }

                if (mCaptureFailCount >= 7) {
                    captureFail();
                }

                if (pressFrameIndex == 0 && mAddFingerState < 3) {
                    Log.e(TAG, "displayPressAnimFirstFrame");
                    displayPressAnimFirstFrame();
                }
                playAnimEndVibrator(FRAME_DURATION);
            }
        }

        public void onProgress(Message msg) {
            Log.d(TAG, "onProgress()--msg=" + msg);
            analyzeOnProgressMsg((Bundle) msg.obj);
        }

        public void onEnrolled(int fingerId) {
            Log.d(TAG, "onEnrolled()-- fingerId = " + fingerId);
            mAddFingerState = ADD_FINGER_COMPLETE;
            refreshView();
            returnId = fingerId;
        }

        public void onEnrollmentFailed(int reason) {
            Log.d(TAG, "onEnrollmentFailed()-- int reason=" + reason);
            mInsideIvView.setVisibility(View.INVISIBLE);
            if (reason == 1) {
                mAddFingerState = ADD_FINGER_TIMEOUT;
                refreshView();
            } else {
                enrolFail();
            }
        }

        public void onExtEnrolMsg(Message msg, String description) {
            Log.d("Fingerprint_Settings", "onExtEnrolMsg()--");
        }
    };

}

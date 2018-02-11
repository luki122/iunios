/*
 * Copyright (C) 2012 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.keyguard;

import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.view.ShowDigitView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R;
import com.android.keyguard.utils.AnimUtils;
import com.android.keyguard.utils.Blur;
import com.android.keyguard.utils.LockScreenBgUtils;

import android.R.animator;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Displays a PIN pad for unlocking.
 */
public class KeyguardDigitView extends KeyguardAbsKeyInputView implements KeyguardSecurityView,
        OnEditorActionListener, TextWatcher {

    private Context mContext;
    private ShowDigitView mShowDigitView;
    private View mMsgView;
    private View mNumPad;

    public KeyguardDigitView(Context context) {
        this(context, null);
    }

    public KeyguardDigitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    protected void resetState() {
        if (KeyguardUpdateMonitor.getInstance(mContext).getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_password_instructions, true);
        }
        mPasswordEntry.setEnabled(true);
        mShowDigitView.onTextChange(0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBlurBg(this);
        setBackground(new BitmapDrawable(bitmap));
        Log.d("KeyguardSecurityView", "KeyguardDigitView=====onAttachedToWindow====="+bitmap);
    }

    @Override
    public void onResume(int reason) {
        super.onResume(reason);
        //Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBlurBg(this);
        //setBackground(new BitmapDrawable(bitmap));
        Log.d("KeyguardSecurityView", "KeyguardDigitView=====onResume=====");
    }

    @Override
    public void playAppearAnim() {
    	Log.d("KeyguardSecurityView", "KeyguardDigitView=====playAppearAnim=====");
        startAnim(true);
    }

    @Override
    protected int getPasswordTextViewId() {
        return R.id.digitEntry;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mShowDigitView = ( ShowDigitView ) findViewById(R.id.show_digit_view);

        /*final View ok = findViewById(R.id.key_enter);
        if (ok != null) {
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doHapticKeyClick();
                    if (mPasswordEntry.isEnabled()) {
                        verifyPasswordAndUnlock();
                    }
                }
            });
            ok.setOnHoverListener(new LiftToActivateListener(getContext()));
        }*/

        // The delete button is of the PIN keyboard itself in some (e.g. tablet) layouts,
        // not a separate view
        ImageView digitDelete = ( ImageView ) findViewById(R.id.delete_button);
        updateDelButton(mContext, digitDelete);
        if (digitDelete != null) {
            digitDelete.setVisibility(View.VISIBLE);
            digitDelete.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        CharSequence str = mPasswordEntry.getText();
                        if (str.length() > 0) {
                            mPasswordEntry.setText(str.subSequence(0, str.length() - 1));
                        }
                    }
                    doHapticKeyClick();
                }
            });
            digitDelete.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    // check for time-based lockouts
                    if (mPasswordEntry.isEnabled()) {
                        mPasswordEntry.setText("");
                    }
                    doHapticKeyClick();
                    return true;
                }
            });
        }

        mPasswordEntry.setKeyListener(DigitsKeyListener.getInstance());
        mPasswordEntry.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);

        mPasswordEntry.requestFocus();
        mPasswordEntry.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (mShowDigitView != null) {
                    mShowDigitView.onTextChange(s.length());
                }
                if (mCallback != null) {
                    mCallback.userActivity(0);
                }
                if (s.length() >= 4) {
                    verifyPasswordAndUnlock();
                }
            }
        });
        mMsgView = findViewById(R.id.keyguard_message_area);
        mNumPad = findViewById(R.id.num_key);
    }

    @Override
    public void showUsabilityHint() {
    }

    @Override
    public int getWrongPasswordStringId() {
        return R.string.kg_wrong_password;
    }

    @Override
    protected void verifyPasswordAndUnlock() {
        mPasswordEntry.setEnabled(false);
        String entry = mPasswordEntry.getText().toString();
        if (mLockPatternUtils.checkPassword(entry)) {
            startAnim(false);
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.reportSuccessfulUnlockAttempt();
                    mPasswordEntry.setEnabled(true);
                    mCallback.dismiss(true);
                }
            }, 250);
        } else if (entry.length() > MINIMUM_PASSWORD_LENGTH_BEFORE_REPORT) {
            // to avoid accidental lockout, only count attempts that are long enough to be a
            // real password. This may require some tweaking.
            mCallback.reportFailedUnlockAttempt();
            if (mCallback.getFailedAttempts() > 0
                    && 0 == (mCallback.getFailedAttempts() % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                handleAttemptLockout(deadline);
            }
            mSecurityMessageDisplay.setMessage(getWrongPasswordStringId(), true);
            if (mShowDigitView != null) {
                mShowDigitView.onTextChange(5);
                Animation anim = AnimationUtils.loadAnimation(getContext(), R.anim.shake);
                mShowDigitView.startAnimation(anim);
            }
        }
        postDelayed(mClearTextRunnable, 600);
    }

    /*@Override
    public void onPause() {
        super.onPause();
        if (mShowDigitView != null) {
            mShowDigitView.onTextChange(0);
        }
    }*/
    Runnable mClearTextRunnable = new Runnable() {

        @Override
        public void run() {
            if (0 != (mCallback.getFailedAttempts() % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                mPasswordEntry.setText("");
                resetState();
            }
        }
    };

    private void updateDelButton(Context context, ImageView img) {
        if ("CN".equals(context.getResources().getConfiguration().locale.getCountry())) {
            img.setImageResource(R.drawable.lockscreen_del_button);
        } else {
            img.setImageResource(R.drawable.lockscreen_del_eng_button);
        }
    }

    private void startAnim(boolean appeared) {
        AnimatorSet set = new AnimatorSet();
        if (appeared) {
            createShowDigitAnim(appeared);
            set.playTogether(createMsgViewAnim(appeared), createNumPadAnim(appeared));
        } else {
            set.playTogether(createMsgViewAnim(appeared), createNumPadAnim(appeared),
                    createShowDigitAnim(appeared));
        }
        set.start();
		  // Aurora liugj 2015-01-06 added for bug-10669 start
        if(mCallback != null) {
			mCallback.setRunningAnim(true);
		}
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				if(mCallback != null) {
					mCallback.setRunningAnim(false);
				}
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		// Aurora liugj 2015-01-06 added for bug-10669 end
    }

    private Animator createMsgViewAnim(boolean appeared) {
        ObjectAnimator animator = null;
        PropertyValuesHolder pvhAlpha = null;
        PropertyValuesHolder pvhY = null;
        if (appeared) {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", mMsgView.getTranslationY() - 100,
                    mMsgView.getTranslationY());
            animator = AnimUtils.ofPropertyValuesHolder(mMsgView, pvhAlpha, pvhY);
            animator.setInterpolator(new OvershootInterpolator());
            animator.setDuration(300);
        } else {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", -100f);
            animator = AnimUtils.ofPropertyValuesHolder(mMsgView, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(300);
        }
        return animator;
    }

    private Animator createNumPadAnim(boolean appeared) {
        ObjectAnimator animator = null;
        PropertyValuesHolder pvhY = null;
        PropertyValuesHolder pvhAlpha = null;
        if (appeared) {
            pvhY = PropertyValuesHolder.ofFloat("translationY", mNumPad.getTranslationY() + 1000f,
                    mNumPad.getTranslationY());
            animator = AnimUtils.ofPropertyValuesHolder(mNumPad, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(300);
        } else {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", 1000f);
            animator = AnimUtils.ofPropertyValuesHolder(mNumPad, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    View view = ( View ) (( ObjectAnimator ) animation).getTarget();
                    view.setTranslationY(0.0f);
                }
            });
            animator.setDuration(300);
        }
        return animator;
    }

    private Animator createShowDigitAnim(boolean appeared) {
        ObjectAnimator animator = null;
        if (appeared) {
            mShowDigitView.startInitAnim();
        } else {
            animator = AnimUtils.ofFloat(mShowDigitView, "alpha", 0.0f);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(300);
        }
        return animator;
    }

    @Override
    public void playDisAppearAnim() {
        // TODO Auto-generated method stub

    }
}

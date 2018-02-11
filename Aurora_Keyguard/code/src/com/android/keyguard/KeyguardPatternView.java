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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.LockPatternView;
import com.android.keyguard.utils.AnimUtils;
import com.android.keyguard.utils.Blur;
import com.android.keyguard.utils.LockScreenBgUtils;
import com.android.keyguard.view.AuroraLockPatternView;

import java.io.IOException;
import java.util.List;

public class KeyguardPatternView extends LinearLayout implements KeyguardSecurityView {

    private static final String TAG = "SecurityPatternView";
    private static final boolean DEBUG = false;

    // how long before we clear the wrong pattern
    private static final int PATTERN_CLEAR_TIMEOUT_MS = 600;

    // how long we stay awake after each key beyond MIN_PATTERN_BEFORE_POKE_WAKELOCK
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_MS = 7000;

    // how long we stay awake after the user hits the first dot.
    private static final int UNLOCK_PATTERN_WAKE_INTERVAL_FIRST_DOTS_MS = 2000;

    // how many cells the user has to cross before we poke the wakelock
    private static final int MIN_PATTERN_BEFORE_POKE_WAKELOCK = 2;

    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private int mTotalFailedPatternAttempts = 0;
    private CountDownTimer mCountdownTimer = null;
    private LockPatternUtils mLockPatternUtils;
    private AuroraLockPatternView mLockPatternView;
    private Button mForgotPatternButton;
    private KeyguardSecurityCallback mCallback;
    private boolean mEnableFallback;
    private View mMsgView;
    private View mEmergencyView;

    /**
     * Keeps track of the last time we poked the wake lock during dispatching of the touch event.
     * Initialized to something guaranteed to make us poke the wakelock when the user starts
     * drawing the pattern.
     * @see #dispatchTouchEvent(android.view.MotionEvent)
     */
    private long mLastPokeTime = -UNLOCK_PATTERN_WAKE_INTERVAL_MS;

    /**
     * Useful for clearing out the wrong pattern after a delay
     */
    private Runnable mCancelPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
            displayDefaultSecurityMessage();
        }
    };
    private Rect mTempRect = new Rect();
    private SecurityMessageDisplay mSecurityMessageDisplay;
    private View mEcaView;
    private Drawable mBouncerFrame;

    enum FooterMode {
        Normal,
        ForgotLockPattern,
        VerifyUnlocked
    }

    public KeyguardPatternView(Context context) {
        this(context, null);
    }

    public KeyguardPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setKeyguardCallback(KeyguardSecurityCallback callback) {
        mCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils utils) {
        mLockPatternUtils = utils;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mLockPatternUtils = mLockPatternUtils == null
                ? new LockPatternUtils(mContext) : mLockPatternUtils;

        mLockPatternView = ( AuroraLockPatternView ) findViewById(R.id.lockPatternView);
        mLockPatternView.setSaveEnabled(false);
        mLockPatternView.setFocusable(false);
        mLockPatternView.setOnPatternListener(new UnlockPatternListener());

        // stealth mode will be the same for the life of this screen
        mLockPatternView.setInStealthMode(!mLockPatternUtils.isVisiblePatternEnabled());

        // vibrate mode will be the same for the life of this screen
        mLockPatternView.setTactileFeedbackEnabled(mLockPatternUtils.isTactileFeedbackEnabled());

        mForgotPatternButton = (Button) findViewById(R.id.forgot_password_button);
        // note: some configurations don't have an emergency call area
        if (mForgotPatternButton != null) {
            mForgotPatternButton.setText(R.string.kg_forgot_pattern_button_text);
            mForgotPatternButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mCallback.showBackupSecurity();
                }
            });
        }

        setFocusableInTouchMode(true);

        maybeEnableFallback(mContext);
        mSecurityMessageDisplay = new KeyguardMessageArea.Helper(this);
        mEcaView = findViewById(R.id.keyguard_selector_fade_container);
        View bouncerFrameView = findViewById(R.id.keyguard_bouncer_frame);
        if (bouncerFrameView != null) {
            mBouncerFrame = bouncerFrameView.getBackground();
        }
        mMsgView = findViewById(R.id.keyguard_message_area);
        mEmergencyView = findViewById(R.id.emergency_call_button);
    }

    private void updateFooter(FooterMode mode) {
        if (mForgotPatternButton == null) return; // no ECA? no footer

        switch (mode) {
            case Normal:
                if (DEBUG) Log.d(TAG, "mode normal");
                mForgotPatternButton.setVisibility(View.GONE);
                break;
            case ForgotLockPattern:
                if (DEBUG) Log.d(TAG, "mode ForgotLockPattern");
                mForgotPatternButton.setVisibility(View.VISIBLE);
                break;
            case VerifyUnlocked:
                if (DEBUG) Log.d(TAG, "mode VerifyUnlocked");
                mForgotPatternButton.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean result = super.onTouchEvent(ev);
        // as long as the user is entering a pattern (i.e sending a touch event that was handled
        // by this screen), keep poking the wake lock so that the screen will stay on.
        final long elapsed = SystemClock.elapsedRealtime() - mLastPokeTime;
        if (result && (elapsed > (UNLOCK_PATTERN_WAKE_INTERVAL_MS - 100))) {
            mLastPokeTime = SystemClock.elapsedRealtime();
        }
        mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(mLockPatternView, mTempRect);
        ev.offsetLocation(mTempRect.left, mTempRect.top);
        result = mLockPatternView.dispatchTouchEvent(ev) || result;
        ev.offsetLocation(-mTempRect.left, -mTempRect.top);
        return result;
    }

    public void reset() {
        // reset lock pattern
        mLockPatternView.enableInput();
        mLockPatternView.setEnabled(true);
        mLockPatternView.clearPattern();

        // if the user is currently locked out, enforce it.
        long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
        int failedCount = KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts();
        if (deadline != 0 && failedCount > 0) {
            handleAttemptLockout(deadline);
        } else {
            displayDefaultSecurityMessage();
        }

        // the footer depends on how many total attempts the user has failed
        if (mCallback.isVerifyUnlockOnly()) {
            updateFooter(FooterMode.VerifyUnlocked);
        } else if (mEnableFallback &&
                (mTotalFailedPatternAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
            updateFooter(FooterMode.ForgotLockPattern);
        } else {
            updateFooter(FooterMode.Normal);
        }

    }

    private void displayDefaultSecurityMessage() {
        if (KeyguardUpdateMonitor.getInstance(mContext).getMaxBiometricUnlockAttemptsReached()) {
            mSecurityMessageDisplay.setMessage(R.string.faceunlock_multiple_failures, true);
        } else {
            mSecurityMessageDisplay.setMessage(R.string.kg_pattern_instructions, true);
        }
    }

    @Override
    public void showUsabilityHint() {
    }

    /** TODO: hook this up */
    public void cleanUp() {
        if (DEBUG) Log.v(TAG, "Cleanup() called on " + this);
        mLockPatternUtils = null;
        mLockPatternView.setOnPatternListener(null);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (hasWindowFocus) {
            // when timeout dialog closes we want to update our state
            reset();
        }
    }

    private class UnlockPatternListener implements AuroraLockPatternView.OnPatternListener {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mCancelPatternRunnable);
        }

        public void onPatternCleared() {
        }

        public void onPatternCellAdded(List<AuroraLockPatternView.Cell> pattern) {
            // To guard against accidental poking of the wakelock, look for
            // the user actually trying to draw a pattern of some minimal length.
            if (pattern.size() > MIN_PATTERN_BEFORE_POKE_WAKELOCK) {
                mCallback.userActivity(UNLOCK_PATTERN_WAKE_INTERVAL_MS);
            } else {
                // Give just a little extra time if they hit one of the first few dots
                mCallback.userActivity(UNLOCK_PATTERN_WAKE_INTERVAL_FIRST_DOTS_MS);
            }
        }

        public void onPatternDetected(List<AuroraLockPatternView.Cell> pattern) {
            if (mLockPatternView.checkPattern(pattern)) {
                startAnim(false);
                postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        mCallback.reportSuccessfulUnlockAttempt();
                        mLockPatternView.setDisplayMode(AuroraLockPatternView.DisplayMode.Correct);
                        mTotalFailedPatternAttempts = 0;
                        mCallback.dismiss(true);
                    }
                }, 250);
            } else {
                if (pattern.size() > MIN_PATTERN_BEFORE_POKE_WAKELOCK) {
                    mCallback.userActivity(UNLOCK_PATTERN_WAKE_INTERVAL_MS);
                }
                mLockPatternView.setDisplayMode(AuroraLockPatternView.DisplayMode.Wrong);
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mTotalFailedPatternAttempts++;
                    mFailedPatternAttemptsSinceLastTimeout++;
                    mCallback.reportFailedUnlockAttempt();

                    if (KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts() > 0
                            && 0 == (KeyguardUpdateMonitor.getInstance(mContext).getFailedUnlockAttempts() % LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT)) {
                        long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
                        handleAttemptLockout(deadline);
							 // Aurora liugj 2014-11-17 added for bug-9605 start
                        return;
							 // Aurora liugj 2014-11-17 added for bug-9605 end
                    }
                }
//                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
//                    long deadline = mLockPatternUtils.setLockoutAttemptDeadline();
//                    handleAttemptLockout(deadline);
//                } else {
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mSecurityMessageDisplay.setMessage(R.string.kg_wrong_pattern, true);
                } else {
                    mSecurityMessageDisplay.setMessage(R.string.kg_invalid_pattern, true);
                }
                mLockPatternView.postDelayed(mCancelPatternRunnable, PATTERN_CLEAR_TIMEOUT_MS);
//                }
            }
        }
    }

    private void maybeEnableFallback(Context context) {
        // Ask the account manager if we have an account that can be used as a
        // fallback in case the user forgets his pattern.
        AccountAnalyzer accountAnalyzer = new AccountAnalyzer(AccountManager.get(context));
        accountAnalyzer.start();
    }

    private class AccountAnalyzer implements AccountManagerCallback<Bundle> {
        private final AccountManager mAccountManager;
        private final Account[] mAccounts;
        private int mAccountIndex;

        private AccountAnalyzer(AccountManager accountManager) {
            mAccountManager = accountManager;
            mAccounts = accountManager.getAccountsByTypeAsUser("com.google",
                    new UserHandle(mLockPatternUtils.getCurrentUser()));
        }

        private void next() {
            // if we are ready to enable the fallback or if we depleted the list of accounts
            // then finish and get out
            if (mEnableFallback || mAccountIndex >= mAccounts.length) {
                return;
            }

            // lookup the confirmCredentials intent for the current account
            mAccountManager.confirmCredentialsAsUser(mAccounts[mAccountIndex], null, null, this,
                    null, new UserHandle(mLockPatternUtils.getCurrentUser()));
        }

        public void start() {
            mEnableFallback = false;
            mAccountIndex = 0;
            next();
        }

        public void run(AccountManagerFuture<Bundle> future) {
            try {
                Bundle result = future.getResult();
                if (result.getParcelable(AccountManager.KEY_INTENT) != null) {
                    mEnableFallback = true;
                }
            } catch (OperationCanceledException e) {
                // just skip the account if we are unable to query it
            } catch (IOException e) {
                // just skip the account if we are unable to query it
            } catch (AuthenticatorException e) {
                // just skip the account if we are unable to query it
            } finally {
                mAccountIndex++;
                next();
            }
        }
    }

    private void handleAttemptLockout(long elapsedRealtimeDeadline) {
        mLockPatternView.clearPattern();
        mLockPatternView.setEnabled(false);
        final long elapsedRealtime = SystemClock.elapsedRealtime();
        if (mEnableFallback) {
            updateFooter(FooterMode.ForgotLockPattern);
        }

        mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline - elapsedRealtime, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                final int secondsRemaining = (int) (millisUntilFinished / 1000);
                mSecurityMessageDisplay.setMessage(
                        R.string.kg_too_many_failed_attempts_countdown, true, secondsRemaining);
            }

            @Override
            public void onFinish() {
                mLockPatternView.setEnabled(true);
                displayDefaultSecurityMessage();
                // TODO mUnlockIcon.setVisibility(View.VISIBLE);
                mFailedPatternAttemptsSinceLastTimeout = 0;
                if (mEnableFallback) {
                    updateFooter(FooterMode.ForgotLockPattern);
                } else {
                    updateFooter(FooterMode.Normal);
                }
            }

        }.start();
    }

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public void onPause() {
    	  Log.d("KeyguardSecurityView", TAG+"=====onPause=====");
        if (mCountdownTimer != null) {
            mCountdownTimer.cancel();
            mCountdownTimer = null;
        }
    }

    @Override
    public void onResume(int reason) {
        reset();
        //Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBlurBg(this);
        //setBackground(new BitmapDrawable(bitmap));
        Log.d("KeyguardSecurityView", TAG+"=====onResume=====");
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Bitmap bitmap = LockScreenBgUtils.getInstance().getLockScreenBlurBg(this);
        setBackground(new BitmapDrawable(bitmap));
        Log.d("KeyguardSecurityView", TAG+"=====onAttachedToWindow====="+bitmap);
    }

    @Override
    public KeyguardSecurityCallback getCallback() {
        return mCallback;
    }

    @Override
    public void showBouncer(int duration) {
        // Aurora <zhang_xin> <2013-9-17> modify for policy begin
//        KeyguardSecurityViewHelper.
//                showBouncer(mSecurityMessageDisplay, mEcaView, mBouncerFrame, duration);
        // Aurora <zhang_xin> <2013-9-17> modify for policy end
    }

    @Override
    public void hideBouncer(int duration) {
        // Aurora <zhang_xin> <2013-9-17> modify for policy begin
//        KeyguardSecurityViewHelper.
//                hideBouncer(mSecurityMessageDisplay, mEcaView, mBouncerFrame, duration);
        // Aurora <zhang_xin> <2013-9-17> modify for policy end
    }

    @Override
    public void playAppearAnim() {
    	Log.d("KeyguardSecurityView", TAG+"=====playAppearAnim=====");
        startAnim(true);
    }

    @Override
    public void playDisAppearAnim() {
        // TODO Auto-generated method stub

    }

    private void startAnim(boolean appeared) {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(createMsgViewAnim(appeared), createPatternAnim(appeared),
                createEmergencyAnim(appeared));
		  // Aurora liugj 2015-01-06 added for bug-10669 start
        set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				if(mCallback != null) {
					mCallback.setRunningAnim(true);
				}
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
		set.start();
		// Aurora liugj 2015-01-06 added for bug-10669 end
    }

    private Animator createPatternAnim(boolean appeared) {
        ObjectAnimator animator = null;
        PropertyValuesHolder pvhY = null;
        PropertyValuesHolder pvhAlpha = null;
        if (appeared) {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", mLockPatternView.getTranslationY() + 500.0f,
                    mLockPatternView.getTranslationY());
            animator = AnimUtils.ofPropertyValuesHolder(mLockPatternView, pvhY, pvhAlpha);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(300);
        } else {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", 500.0f);
            animator = AnimUtils.ofPropertyValuesHolder(mLockPatternView, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
				
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
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
					if (view.getTranslationY() != 0.0f) {
	                    view.setTranslationY(0.0f);
	                    reset();
					}
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
                    view.setTranslationY(0.0f);
				}
			});
            animator.setDuration(250);
        }
        return animator;
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
            animator.setDuration(500);
        } else {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", -100f);
            animator = AnimUtils.ofPropertyValuesHolder(mMsgView, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
				
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
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
					if (view.getTranslationY() != 0.0f) {
	                    view.setTranslationY(0.0f);
					}
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
                    view.setTranslationY(0.0f);
				}
			});
            animator.setDuration(250);
        }
        return animator;
    }

    private Animator createEmergencyAnim(boolean appeared) {
        ObjectAnimator animator = null;
        PropertyValuesHolder pvhAlpha = null;
        PropertyValuesHolder pvhY = null;
        if (appeared) {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 0.0f, 1.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", mEmergencyView.getTranslationY() + 200.0f,
                    mEmergencyView.getTranslationY());
            animator = AnimUtils.ofPropertyValuesHolder(mEmergencyView, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.setDuration(450);
        } else {
            pvhAlpha = PropertyValuesHolder.ofFloat("alpha", 1.0f, 0.0f);
            pvhY = PropertyValuesHolder.ofFloat("translationY", 300.0f);
            animator = AnimUtils.ofPropertyValuesHolder(mEmergencyView, pvhAlpha, pvhY);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					// TODO Auto-generated method stub
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
					if (view.getTranslationY() != 0.0f) {
	                    view.setTranslationY(0.0f);
					}
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					View view = ( View ) (( ObjectAnimator ) animation).getTarget();
                    view.setTranslationY(0.0f);
				}
			});
            animator.setDuration(250);
        }
        return animator;
    }
}

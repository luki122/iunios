/*
 * Copyright (C) 2007 The Android Open Source Project
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

package aurora.lib.app;

import java.text.NumberFormat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.aurora.lib.R;
import com.aurora.lib.utils.DensityUtil;

/**
 * <p>A dialog showing a progress indicator and an optional text message or view.
 * Only a text message or a view can be used at the same time.</p>
 * <p>The dialog can be made cancelable on back key press.</p>
 * <p>The progress range is 0..10000.</p>
 */
public class AuroraProgressDialog extends AuroraAlertDialog {
    
    /** Creates a ProgressDialog with a circular, spinning progress
     * bar. This is the default.
     */
    public static final int STYLE_SPINNER = 0;
    
    /** Creates a ProgressDialog with a horizontal progress bar.
     */
    public static final int STYLE_HORIZONTAL = 1;
    
    private ProgressBar mProgress;
    private TextView mMessageView;
    
    private int mProgressStyle = STYLE_SPINNER;
    private TextView mProgressNumber;
    private String mProgressNumberFormat;
    private TextView mProgressPercent;
    private NumberFormat mProgressPercentFormat;
    
    private int mMax;
    private int mProgressVal;
    private int mSecondaryProgressVal;
    private int mIncrementBy;
    private int mIncrementSecondaryBy;
    private Drawable mProgressDrawable;
    private Drawable mIndeterminateDrawable;
    private CharSequence mMessage;
    private boolean mIndeterminate;
    
    private boolean mHasStarted;
    private Handler mViewUpdateHandler;
    
    private boolean mIsSpinnerProgress;
    
    private CharSequence mMsg;
    
    private LinearLayout mSpinnerBody;
    
    public AuroraProgressDialog(Context context) {
        super(context);
        initFormats();
    }

    public AuroraProgressDialog(Context context, int theme) {
        super(context, 0);
        initFormats();
    }

    private void initFormats() {
        mProgressNumberFormat = "#.##";
        mProgressPercentFormat = NumberFormat.getPercentInstance();
        mProgressPercentFormat.setMaximumFractionDigits(0);
    }
    
    public static AuroraProgressDialog show(Context context, CharSequence title,
            CharSequence message) {
        return show(context, title, message, false);
    }

    public static AuroraProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate) {
        return show(context, title, message, indeterminate, false, null);
    }

    public static AuroraProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate, boolean cancelable) {
        return show(context, title, message, indeterminate, cancelable, null);
    }

    public static AuroraProgressDialog show(Context context, CharSequence title,
            CharSequence message, boolean indeterminate,
            boolean cancelable, OnCancelListener cancelListener) {
        AuroraProgressDialog dialog = new AuroraProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setIndeterminate(indeterminate);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(cancelListener);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TypedArray a = getContext().obtainStyledAttributes(null,
        		com.aurora.lib.R.styleable.AuroraAlertDialog,
                0, getThemeStyleId());
        if (mProgressStyle == STYLE_HORIZONTAL) {
            mIsSpinnerProgress = false;
            /* Use a separate handler to update the text views as they
             * must be updated on the same thread that created them.
             */
            mViewUpdateHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    
                    /* Update the number and percent */
                    int progress = mProgress.getProgress();
                    int max = mProgress.getMax();
                    if (mProgressNumberFormat != null) {
                        String format = mProgressNumberFormat;
                        mProgressNumber.setText(String.format(format, progress, max));
                    } else {
                        mProgressNumber.setText("");
                    }
                    if (mProgressPercentFormat != null) {
                        double percent = ((double) progress / (double) max);
                        SpannableString tmp = new SpannableString(mProgressPercentFormat.format(percent));
//                        tmp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
//                                0, tmp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                        String tmp = String.valueOf(percent);
                        mProgressPercent.setText(tmp);
                    } else {
                        mProgressPercent.setText("");
                    }
                }
            };
            View view = inflater.inflate(a.getResourceId(
            		com.aurora.lib.R.styleable.AuroraAlertDialog_aurorahorizontalProgressLayout,
                    com.aurora.lib.R.layout.aurora_alert_dialog_progress), null);
            mProgress = (ProgressBar) view.findViewById(com.aurora.lib.R.id.aurora_progress);
            mProgressNumber = (TextView) view.findViewById(com.aurora.lib.R.id.aurora_progress_number);
            mProgressPercent = (TextView) view.findViewById(com.aurora.lib.R.id.aurora_progress_percent);
            setView(view);
        } else {
            mIsSpinnerProgress = true;
            View view = inflater.inflate(a.getResourceId(
            		com.aurora.lib.R.styleable.AuroraAlertDialog_auroraprogressLayout,
            		com.aurora.lib.R.layout.aurora_progress_dialog), null);
            mSpinnerBody = (LinearLayout)view.findViewById(com.aurora.lib.R.id.aurora_body);
            mProgress = (ProgressBar) view.findViewById(com.aurora.lib.R.id.aurora_progress);
            mMessageView = (TextView) view.findViewById(com.aurora.lib.R.id.aurora_message);
            setView(view);
        }
        a.recycle();
        if (mMax > 0) {
            setMax(mMax);
        }
        if (mProgressVal > 0) {
            setProgress(mProgressVal);
        }
        if (mSecondaryProgressVal > 0) {
            setSecondaryProgress(mSecondaryProgressVal);
        }
        if (mIncrementBy > 0) {
            incrementProgressBy(mIncrementBy);
        }
        if (mIncrementSecondaryBy > 0) {
            incrementSecondaryProgressBy(mIncrementSecondaryBy);
        }
        if (mProgressDrawable != null) {
            setProgressDrawable(mProgressDrawable);
        }
        if (mIndeterminateDrawable != null) {
            setIndeterminateDrawable(mIndeterminateDrawable);
        }
        if (mMessage != null) {
            setMessage(mMessage);
        }
        setIndeterminate(mIndeterminate);
        setCanceledOnTouchOutside(false);
        onProgressChanged();
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public void onStart() {
        super.onStart();
        mHasStarted = true;
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mHasStarted = false;
    }

    public void setProgress(int value) {
        if (mHasStarted) {
            mProgress.setProgress(value);
            onProgressChanged();
        } else {
            mProgressVal = value;
        }
    }

    public void setSecondaryProgress(int secondaryProgress) {
        if (mProgress != null) {
            mProgress.setSecondaryProgress(secondaryProgress);
            onProgressChanged();
        } else {
            mSecondaryProgressVal = secondaryProgress;
        }
    }

    public int getProgress() {
        if (mProgress != null) {
            return mProgress.getProgress();
        }
        return mProgressVal;
    }

    public int getSecondaryProgress() {
        if (mProgress != null) {
            return mProgress.getSecondaryProgress();
        }
        return mSecondaryProgressVal;
    }

    public int getMax() {
        if (mProgress != null) {
            return mProgress.getMax();
        }
        return mMax;
    }

    public void setMax(int max) {
        if (mProgress != null) {
            mProgress.setMax(max);
            onProgressChanged();
        } else {
            mMax = max;
        }
    }

    public void incrementProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementBy += diff;
        }
    }

    public void incrementSecondaryProgressBy(int diff) {
        if (mProgress != null) {
            mProgress.incrementSecondaryProgressBy(diff);
            onProgressChanged();
        } else {
            mIncrementSecondaryBy += diff;
        }
    }

    public void setProgressDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setProgressDrawable(d);
        } else {
            mProgressDrawable = d;
        }
    }

    public void setIndeterminateDrawable(Drawable d) {
        if (mProgress != null) {
            mProgress.setIndeterminateDrawable(d);
        } else {
            mIndeterminateDrawable = d;
        }
    }

    public void setIndeterminate(boolean indeterminate) {
        if(mProgressPercent!= null){
            mProgressPercent.setVisibility(indeterminate?View.GONE:View.VISIBLE);
        }
        if (mProgress != null) {
            mProgress.setIndeterminate(indeterminate);
        } else {
            mIndeterminate = indeterminate;
        }
    }

    public boolean isIndeterminate() {
        if (mProgress != null) {
            return mProgress.isIndeterminate();
        }
        return mIndeterminate;
    }
    
    @Override
    public void setMessage(CharSequence message) {
        if (mProgress != null) {
            if (mProgressStyle == STYLE_HORIZONTAL) {
                super.setMessage(message);
            } else {
                mMessageView.setText(message);
                if(mSpinnerBody != null){
                if(!TextUtils.isEmpty(message)){
                    mMessageView.setVisibility(View.VISIBLE);
                    mMessageView.setPadding(DensityUtil.dip2px(mContext, 12), mMessageView.getPaddingTop(), 
                            mMessageView.getPaddingRight(), mMessageView.getPaddingBottom());
                    mSpinnerBody.setGravity(Gravity.CENTER|Gravity.CENTER_VERTICAL);
//                    mSpinnerBody.setPadding( mSpinnerBody.getPaddingLeft(), mSpinnerBody.getPaddingTop(), 
//                           DensityUtil.dip2px(mContext, 11), mSpinnerBody.getPaddingBottom());
                }else{
                    mMessageView.setVisibility(View.GONE);
                    mMessageView.setPadding(0, mMessageView.getPaddingTop(), 
                            mMessageView.getPaddingRight(), mMessageView.getPaddingBottom());
                    mSpinnerBody.setGravity(Gravity.CENTER);
//                    mSpinnerBody.setPadding(0, mSpinnerBody.getPaddingTop(), 
//                            mSpinnerBody.getPaddingRight(), mSpinnerBody.getPaddingBottom());
                }
                }
            }
        } else {
            mMessage = message;
        }
        
        mMsg = message;
    }
    
    public void setProgressStyle(int style) {
        mProgressStyle = style;
    }

    /**
     * Change the format of the small text showing current and maximum units
     * of progress.  The default is "%1d/%2d".
     * Should not be called during the number is progressing.
     * @param format A string passed to {@link String#format String.format()};
     * use "%1d" for the current number and "%2d" for the maximum.  If null,
     * nothing will be shown.
     */
    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        onProgressChanged();
    }

    /**
     * Change the format of the small text showing the percentage of progress.
     * The default is
     * {@link NumberFormat#getPercentInstance() NumberFormat.getPercentageInstnace().}
     * Should not be called during the number is progressing.
     * @param format An instance of a {@link NumberFormat} to generate the
     * percentage text.  If null, nothing will be shown.
     */
    public void setProgressPercentFormat(NumberFormat format) {
        mProgressPercentFormat = format;
        onProgressChanged();
    }
    
    private void onProgressChanged() {
        if (mProgressStyle == STYLE_HORIZONTAL) {
            if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
                mViewUpdateHandler.sendEmptyMessage(0);
            }
        }
    }
    
    @Override
    public synchronized void show() {
        // TODO Auto-generated method stub
        super.show();
    }
    
    
    
}

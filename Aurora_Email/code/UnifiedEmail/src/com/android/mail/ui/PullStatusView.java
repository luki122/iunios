/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.mail.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.android.mail.analytics.Analytics;
import com.android.mail.browse.ConversationCursor;
import com.android.mail.preferences.AccountPreferences;
import com.android.mail.preferences.MailPrefs;
import com.android.mail.providers.Account;
import com.android.mail.providers.Folder;
import com.android.mail.utils.LogTag;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.Utils;

import android.widget.RelativeLayout;
import android.text.InputType;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import android.util.Log;
import com.android.mail.utils.MyLog;
import android.view.ViewGroup;

import android.widget.ProgressBar;
import com.android.mail.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PullStatusView extends LinearLayout
        /*implements ConversationSpecialItemView, SwipeableItemView*/ {
	private static final String LOG_TAG = LogTag.getLogTag();
	//for ui presentation.
	public static final int STATUS_INITAL = 0;
	public static final int STATUS_LOADING = 1;
	public static final int STATUS_SENDING = 2;
	public static final int STATUS_SEND_FAILURE_TIP = 3;
	public static final int STATUS_SEND_SUCCESS_TIP = 4;
	public static final int STATUS_COUNT = 5;
	private int mCurrentStatus = STATUS_INITAL - 1;
	
	//for finger gestures
	public static final int FINGER_STATE_TAP_TO_REFRESH = 0;
    public static final int FINGER_STATE_PULL_TO_REFRESH = 1;
    public static final int FINGER_STATE_RELEASE_TO_REFRESH = 2;
    public static final int FINGER_STATE_REFRESHING = 3;
    private int mFingerState;

    private LinearLayout mContentContainer;
	private View mLoadingView;
	private View mSendingView;
	private View mSendFailureTipView;
	private View mSendSuccessTipView;
	
	private TextView mLastUpdateTimeTextView;
	private TextView mLoadingText;
	
	private static final String PREF_NAME = "PULL_STATUS_VIEW_PREF_NAME";
	private static final String PREF_KEY_LAST_UPDATE_TIME = "PULL_STATUS_VIEW_LAST_UPDATE_TIME";
	
	//Aurora <SQF> <2014-12-10>  for NEW_UI begin
    //private static final String AURORA_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
    //private static final Typeface AURORA_FONT = Typeface.createFromFile(AURORA_FONT_PATH);
    //Aurora <SQF> <2014-12-10>  for NEW_UI end
	
	private int mVisibleHeight;
	
	private MailActivity mActivity;
	
	public void setActivity(MailActivity activity) {
		mActivity = activity;
	}
	
	private void setupViews() {
		mContentContainer = (LinearLayout)findViewById(R.id.content_container);
		mLoadingView = findViewById(R.id.loading);
		mSendingView = findViewById(R.id.sending_mail);
		mSendFailureTipView = findViewById(R.id.send_failure_tip);
		mSendSuccessTipView = findViewById(R.id.send_success_tip);
		
		mLoadingText = (TextView)findViewById(R.id.loading_text);
		mLastUpdateTimeTextView = (TextView)findViewById(R.id.last_update_time_textview);
		//mLoadingText.setTypeface(AURORA_FONT);
		mLoadingText.setTypeface(Typeface.DEFAULT);   //cjs modify
		//mLastUpdateTimeTextView.setTypeface(AURORA_FONT);
		mLastUpdateTimeTextView.setTypeface(Typeface.DEFAULT);  //cjs modify
		stopLoading();
	}
	
	public View getViewByStatus(int status) {
		switch(status) {
		case STATUS_INITAL:
		case STATUS_LOADING:
			return mLoadingView;
		case STATUS_SENDING:
			return mSendingView;
		case STATUS_SEND_FAILURE_TIP:
			return mSendFailureTipView;
		case STATUS_SEND_SUCCESS_TIP:
			return mSendSuccessTipView;
		}
		return null;
	}

	public void switchStatus(int toStatus) {
		if(mCurrentStatus == toStatus) return;
		View toView = getViewByStatus(toStatus);
		for(int i=0; i < STATUS_COUNT; i++) {
			View hideView = getViewByStatus(i);
			if(hideView != toView) {
				//Log.i("SQF_LOG", "hide view : " + i);
				hideView.setVisibility(View.GONE);
			}
		}
		toView.setVisibility(View.VISIBLE);
		mCurrentStatus = toStatus;
		if(mCurrentStatus == STATUS_LOADING) {
			startLoading();
		} else {
			stopLoading();
		}
	}
	
	public void setFingerState(int fingerState) {
		Log.i("SQF_LOG", "PullStatusView::setFingerState::" + fingerState);
		mFingerState = fingerState;
	}
	
	public int getFingerState() {
		return mFingerState;
	}

    private Account mAccount = null;
    private Folder mFolder = null;
    
    public String getCurrentAccount() {
    	if(mActivity == null) {
    		//Log.i("SQF_LOG", "==============getCurrentAccount: return 1111" );
    		return null;
    	}
    	if(mActivity.getAccountController() == null) {
    		//Log.i("SQF_LOG", "==============getCurrentAccount: return 2222" );
    		return null;
    	}
    	//Log.i("SQF_LOG", "==============getCurrentAccount:" + mActivity.getAccountController().getAccount().name);
    	return mActivity.getAccountController().getAccount().name;
    }
	
    private Context mContext;
    private View mSwipeableContent;

    private int mAnimatedHeight = -1;

    public PullStatusView(final Context context) {
        this(context, null);
    }

    public PullStatusView(final Context context, final AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PullStatusView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        //final Resources resources = context.getResources();
    }
    
    @Override
    protected void onFinishInflate() {
    	//Log.i("SQF_LOG", "PullStatusView::onFinishInflate");
    	setupViews();
        switchStatus(STATUS_INITAL);
        mSwipeableContent = this;
    }

    private String getLastUpdateTime() {
    	SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		return sharedPreferences.getString(getCurrentAccount() + PREF_KEY_LAST_UPDATE_TIME, "");
    }
    
    private void setLastUpdateTime() {
    	SimpleDateFormat df = new SimpleDateFormat("MM/dd HH:mm");//yyyy-MM-dd HH:mm:ss
    	String nowTime = df.format(new Date());
    	SharedPreferences sharedPreferences = mContext.getSharedPreferences(PREF_NAME,
				Context.MODE_PRIVATE);
		Editor editor = sharedPreferences.edit();
		editor.putString(getCurrentAccount() + PREF_KEY_LAST_UPDATE_TIME, nowTime);
		editor.commit();
    }
    
    public void updateLastUpdateTime() {
    	if(mLastUpdateTimeTextView == null) return;
    	String lastUpdateTime = getLastUpdateTime();
    	String format = mContext.getResources().getString(R.string.aurora_update_at_time);
    	if(TextUtils.isEmpty(lastUpdateTime)) {
    		lastUpdateTime = mContext.getResources().getString(R.string.aurora_one_year_ago);
    	}
    	String text = String.format(format, lastUpdateTime);
    	//Log.i("SQF_LOG", "updateLastUpdateTime ----" + text);
    	mLastUpdateTimeTextView.setText(text);
    }
    
    
    //control ui
	public void startLoading() {
		if(mCurrentStatus == STATUS_LOADING) {
			return;
		}
		ProgressBar loadingView = (ProgressBar)mLoadingView.findViewById(R.id.loading_view2);
		//loadingView.startLoading();
		setLastUpdateTime();
	}
	
	public void stopLoading() {
		ProgressBar loadingView = (ProgressBar)mLoadingView.findViewById(R.id.loading_view2);
		//loadingView.stopLoading();
		updateLastUpdateTime();
	}
	
	public void setVisibleHeight(int height) {
		if (height < 0)
			height = 0;
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mContentContainer.getLayoutParams();
		lp.height = height;
		mVisibleHeight = height;
		mContentContainer.setLayoutParams(lp);
	}

	public int getVisibleHeight() {
		return mVisibleHeight == 0 ? mContentContainer.getHeight() : mVisibleHeight;
	}
	
	public void hide() {
		mContentContainer.setVisibility(View.INVISIBLE);
	}
	
	public void show() {
		mContentContainer.setVisibility(View.VISIBLE);
	}
}

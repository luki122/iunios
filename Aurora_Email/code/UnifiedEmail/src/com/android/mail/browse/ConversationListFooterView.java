/*
 * Copyright (C) 2012 Google Inc.
 * Licensed to The Android Open Source Project.
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

package com.android.mail.browse;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.android.mail.providers.Folder;
import com.android.mail.providers.UIProvider;
import com.android.mail.ui.ViewMode;
import com.android.mail.utils.MyLog;
import com.android.mail.utils.Utils;

public final class ConversationListFooterView extends LinearLayout implements View.OnClickListener,
        ViewMode.ModeChangeListener {

    public interface FooterViewClickListener {
        void onFooterViewErrorActionClick(Folder folder, int errorStatus);
        void onFooterViewLoadMoreClick(Folder folder);
    }

    private View mLoading;
    private TextView mLoadingText;
    private View mNetworkError;
    private View mLoadMore;
    private Button mErrorActionButton;
    private TextView mErrorText;
    private Folder mFolder;
    private Uri mLoadMoreUri;
    private int mErrorStatus;
    private FooterViewClickListener mClickListener;
    private final boolean mTabletDevice;
    // Backgrounds for different states.
    private static Drawable sWideBackground;
    private static Drawable sNormalBackground;
    
    //Aurora <SQF> <2014-12-10>  for NEW_UI begin
    //private static final String AURORA_FONT_PATH = "system/fonts/DroidSansFallback.ttf";
    //private static final Typeface AURORA_FONT = Typeface.createFromFile(AURORA_FONT_PATH);
    //Aurora <SQF> <2014-12-10>  for NEW_UI end

    public ConversationListFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTabletDevice = Utils.useTabletUI(context.getResources());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mLoading = findViewById(R.id.loading);
        mLoadingText = (TextView)findViewById(R.id.loading_text);
        //mLoadingText.setTypeface(AURORA_FONT);
        mLoadingText.setTypeface(Typeface.DEFAULT);  //cjs modify
        mNetworkError = findViewById(R.id.network_error);
        mLoadMore = findViewById(R.id.load_more);
        mLoadMore.setOnClickListener(this);
        mErrorActionButton = (Button) findViewById(R.id.error_action_button);
        mErrorActionButton.setOnClickListener(this);
        mNetworkError.setOnClickListener(this);//SQF ADDED ON 2014.12.02
        mErrorText = (TextView)findViewById(R.id.error_text);
    }

    public void setClickListener(FooterViewClickListener listener) {
        mClickListener = listener;
    }
    
    //Aurora <SQF> <2014-12-12>  for NEW_UI begin
    public void loadMore() {
    	if(mClickListener != null) {
    		//MyLog.i("SQF_LOG", "ConversationListFooterView::loadMore---");
    		mClickListener.onFooterViewLoadMoreClick(mFolder);
    	}
    }
    //Aurora <SQF> <2014-12-12>  for NEW_UI end

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        final Folder f = (Folder) v.getTag();
        if (id == R.id.error_action_button || id == R.id.network_error) {//SQF ADDED "|| id == R.id.network_error"
        	if(id == R.id.network_error) {
        		showLoadingTip();
        	}
            mClickListener.onFooterViewErrorActionClick(f, mErrorStatus);
        } else if (id == R.id.load_more) {
            //Aurora <SQF> <2014-11-28>  for NEW_UI begin
        	showLoadingTip();
            //Aurora <SQF> <2014-11-28>  for NEW_UI end
            mClickListener.onFooterViewLoadMoreClick(f);
        } 
    }

    public void setFolder(Folder folder) {
        mFolder = folder;
        mErrorActionButton.setTag(mFolder);
        mNetworkError.setTag(mFolder);//SQF ADDED ON 2014.12.02
        mLoadMore.setTag(mFolder);
        mLoadMoreUri = folder.loadMoreUri;
    }

    //Aurora <SQF> <2014-11-28>  for NEW_UI begin
    public void showLoadingTip() {
    	//MyLog.i2("SQF_LOG", "ConversationListFooterView::showLoadingTip---------------------Loading.......");
    	mLoading.setVisibility(View.VISIBLE);
        mNetworkError.setVisibility(View.GONE);
        mLoadMore.setVisibility(View.GONE);
    }
    
    public void showErrorTip(int errorStatus) {
    	//MyLog.i2("SQF_LOG", "ConversationListFooterView::showLoadingTip---------------------ERROR.......");
    	mNetworkError.setVisibility(View.VISIBLE);
        mErrorText.setText(Utils.getSyncStatusText(getContext(), errorStatus));
        mLoading.setVisibility(View.GONE);
        mLoadMore.setVisibility(View.GONE);
        // Only show the "Retry" button for I/O errors; it won't help for
        // internal errors.
        //Aurora <SQF> <2014-12-02>  for NEW_UI begin
        //ORIGINALLY:
        //mErrorActionButton.setVisibility(errorStatus != UIProvider.LastSyncResult.SECURITY_ERROR ? View.VISIBLE : View.GONE);
        //SQF MODIFIED TO:
        mErrorActionButton.setVisibility(View.GONE);
        /*
        if(errorStatus == UIProvider.LastSyncResult.CONNECTION_ERROR) {
        	mErrorActionButton.setVisibility(View.GONE);
        } else {
        	mErrorActionButton.setVisibility(errorStatus != UIProvider.LastSyncResult.SECURITY_ERROR ? View.VISIBLE : View.GONE);
        }
        */
        //Aurora <SQF> <2014-12-02>  for NEW_UI end
    }
    
    public void showLoadMoreTip() {
    	//MyLog.i2("SQF_LOG", "ConversationListFooterView::showLoadingTip---------------------LOAD MORE.......");
    	mLoading.setVisibility(View.GONE);
        mNetworkError.setVisibility(View.GONE);
        mLoadMore.setVisibility(View.VISIBLE);
    }
    //Aurora <SQF> <2014-11-28>  for NEW_UI end

    /**
     * Update the view to reflect the new folder status.
     */
    public boolean updateStatus(final ConversationCursor cursor) {
        if (cursor == null) {
        	//Aurora <SQF> <2014-11-28>  for NEW_UI begin
            //ORIGINALLY:
        	showLoadingTip();
            //SQF MODIFIED TO:
        	/*
        	mLoading.setVisibility(View.VISIBLE);
            mNetworkError.setVisibility(View.GONE);
            mLoadMore.setVisibility(View.GONE);
        	*/
            //Aurora <SQF> <2014-11-28>  for NEW_UI end
            return true;
        }
        boolean showFooter = true;
        final Bundle extras = cursor.getExtras();
        final int cursorStatus = extras.getInt(UIProvider.CursorExtraKeys.EXTRA_STATUS);
        mErrorStatus = extras.containsKey(UIProvider.CursorExtraKeys.EXTRA_ERROR) ?
                extras.getInt(UIProvider.CursorExtraKeys.EXTRA_ERROR)
                : UIProvider.LastSyncResult.SUCCESS;
        final int totalCount = extras.getInt(UIProvider.CursorExtraKeys.EXTRA_TOTAL_COUNT);
        if (UIProvider.CursorStatus.isWaitingForResults(cursorStatus)) {
            //Aurora <SQF> <2014-11-28>  for NEW_UI begin
            //ORIGINALLY:
        	showLoadingTip();
            //SQF MODIFIED TO:
        	/*
        	mLoading.setVisibility(View.VISIBLE);
            mNetworkError.setVisibility(View.GONE);
            mLoadMore.setVisibility(View.GONE);
        	*/
            //Aurora <SQF> <2014-11-28>  for NEW_UI end
            
        } else if (mErrorStatus != UIProvider.LastSyncResult.SUCCESS) {
        	
            //Aurora <SQF> <2014-11-28>  for NEW_UI begin
            //ORIGINALLY:
        	/*
        	mNetworkError.setVisibility(View.VISIBLE);
            mErrorText.setText(Utils.getSyncStatusText(getContext(), mErrorStatus));
            mLoading.setVisibility(View.GONE);
            mLoadMore.setVisibility(View.GONE);
            // Only show the "Retry" button for I/O errors; it won't help for
            // internal errors.
            mErrorActionButton.setVisibility(
                    mErrorStatus != UIProvider.LastSyncResult.SECURITY_ERROR ?
                    View.VISIBLE : View.GONE);
            */
            //SQF MODIFIED TO:
        	//showErrorTip(mErrorStatus);
            //Aurora <SQF> <2014-11-28>  for NEW_UI end
            

            final int actionTextResourceId;
            switch (mErrorStatus) {
                case UIProvider.LastSyncResult.CONNECTION_ERROR:
                    actionTextResourceId = R.string.retry;
                    break;
                case UIProvider.LastSyncResult.AUTH_ERROR:
                    actionTextResourceId = R.string.signin;
                    break;
                case UIProvider.LastSyncResult.SECURITY_ERROR:
                    actionTextResourceId = R.string.retry;
                    mNetworkError.setVisibility(View.GONE);
                    break; // Currently we do nothing for security errors.
                case UIProvider.LastSyncResult.STORAGE_ERROR:
                    actionTextResourceId = R.string.info;
                    break;
                case UIProvider.LastSyncResult.INTERNAL_ERROR:
                    actionTextResourceId = R.string.report;
                    break;
                default:
                    actionTextResourceId = R.string.retry;
                    mNetworkError.setVisibility(View.GONE);
                    break;
            }
            mErrorActionButton.setText(actionTextResourceId);

        } else if (mLoadMoreUri != null && cursor.getCount() < totalCount) {
        	//MyLog.i("SQF_LOG", " cursor.getCount():" + cursor.getCount() + " totalCount:" + totalCount);
            //Aurora <SQF> <2014-11-28>  for NEW_UI begin
            //ORIGINALLY:
        	/*
        	mLoading.setVisibility(View.GONE);
            mNetworkError.setVisibility(View.GONE);
            mLoadMore.setVisibility(View.VISIBLE);
            */
            //SQF MODIFIED TO:
        	
        	//showLoadMoreTip();
        	/*
        	if(UIProvider.CursorStatus.isLoaded(cursorStatus)) {
        		MyLog.i("SQF_LOG", "ConversationListFooterView::CursorStatus  isLoaded....");
        		showLoadMoreTip();
        		//showFooter = true;
        	} else if(UIProvider.CursorStatus.errorOccurred(cursorStatus)) {
        		MyLog.i("SQF_LOG", "ConversationListFooterView::CursorStatus  error occurred....");
        	} else if(UIProvider.CursorStatus.isCompleted(cursorStatus)) {
        		MyLog.i("SQF_LOG", "ConversationListFooterView::CursorStatus  isCompleted....");
        		//showLoadMoreTip();
        		showLoadingTip();
        		//showFooter = false;
        	}
        	*/
        	
            //Aurora <SQF> <2014-11-28>  for NEW_UI end
            
        } else {
            showFooter = false;
        }
        return showFooter;
    }

    /**
     * Update to the appropriate background when the view mode changes.
     */
    @Override
    public void onViewModeChanged(int newMode) {
        //Aurora <SQF> <2014-11-29>  for ANNOTATION begin
        /*
        final Drawable drawable;
        if (mTabletDevice && newMode == ViewMode.CONVERSATION_LIST) {
            drawable = getWideBackground();
        } else {
            drawable = getNormalBackground();
        }
        setBackgroundDrawable(drawable);
        */
        //Aurora <SQF> <2014-11-29>  for ANNOTATION end
    }

    private Drawable getWideBackground() {
        if (sWideBackground == null) {
            sWideBackground = getBackground(R.drawable.conversation_wide_unread_selector);
        }
        return sWideBackground;
    }

    private Drawable getNormalBackground() {
        if (sNormalBackground == null) {
            sNormalBackground = getBackground(R.drawable.conversation_unread_selector);
        }
        return sNormalBackground;
    }

    private Drawable getBackground(int resId) {
        return getContext().getResources().getDrawable(resId);
    }
}

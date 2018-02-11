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
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.android.mail.R;
import com.android.mail.analytics.Analytics;
import com.android.mail.browse.ConversationCursor;
import com.android.mail.preferences.AccountPreferences;
import com.android.mail.preferences.MailPrefs;
import com.android.mail.providers.Account;
import com.android.mail.providers.Folder;
import com.android.mail.ui.MailActivity;
import com.android.mail.utils.LogTag;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.Utils;

import android.widget.RelativeLayout;
import aurora.widget.AuroraSearchView;
import android.text.InputType;
import aurora.app.AuroraActivity;
import aurora.app.AuroraActivity.OnSearchViewQuitListener;
import android.util.Log;

/**
 * A tip displayed on top of conversation view to indicate that Gmail sync is
 * currently disabled on this account.
 */
public class ConversationSearchView extends RelativeLayout
        implements ConversationSpecialItemView, SwipeableItemView, OnSearchViewQuitListener{

	public boolean quit(){
		//if(null != mListControl) mListControl.setFliterString(null);
	    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.CONVERSATION_LIST begin
		if(mViewMode != null){
			if(mViewMode.getMode() == ViewMode.SEARCH_RESULTS_LIST)
				mViewMode.enterConversationListMode();
		}
		
		if(null != mListController) mListController.refreshList();//SQF ADDED ON 2014.12.30
	    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.CONVERSATION_LIST end
		return true;    
	}

	public interface ListController {
		void setFliterString(String fliter);
		void refreshList();
	}

    private static final String LOG_TAG = LogTag.getLogTag();

    private static int sScrollSlop = 0;
    private static int sShrinkAnimationDuration;

    private Account mAccount = null;
    private Folder mFolder = null;
    private AccountPreferences mAccountPreferences;
    private AnimatedAdapter mAdapter;
	
    private Activity mActivity;
    private View mSwipeableContent;
	protected AuroraSearchView mSearchView;
	private ListController mListController;
		
    private int mAnimatedHeight = -1;



    /** Whether we are on a tablet device or not */
    private final boolean mTabletDevice;
    /** When in conversation mode, true if the list is hidden */
    private final boolean mListCollapsible;
    
    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST begin
    private ViewMode mViewMode = null;
    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST end
    
	public void setListController(ListController listController){
		mListController = listController;
	}
    public ConversationSearchView(final Context context) {
        this(context, null);
    }

    public ConversationSearchView(final Context context, final AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public ConversationSearchView(
            final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final Resources resources = context.getResources();

        if (sScrollSlop == 0) {
            sScrollSlop = resources.getInteger(R.integer.swipeScrollSlop);
            sShrinkAnimationDuration = resources.getInteger(
                    R.integer.shrink_animation_duration);
        }

        mTabletDevice = Utils.useTabletUI(resources);
        mListCollapsible = resources.getBoolean(R.bool.list_collapsible);
    }

    public void bindAccount(Account account, ControllableActivity activity) {
    	//add by shihao
    	mViewMode = activity.getViewMode(); 
    	//end
    	
        mAccount = account;
        mAccountPreferences = AccountPreferences.get(getContext(), account.getEmailAddress());
        mActivity = (Activity) activity;
		((AuroraActivity)mActivity).setOnSearchViewQuitListener(this);
    }
	
	private final class searchListener implements aurora.app.AuroraActivity.OnSearchViewQueryTextChangeListener {
			@Override
			public boolean onQueryTextChange(String queryString) {
				if(null != mListController) mListController.setFliterString(queryString);
				return true;
			}
	
			@Override
			public boolean onQueryTextSubmit(String query) {
				return true;
			}
	}

    @Override
    public void onGetView() {
        // Do nothing
    }

    @Override
    protected void onFinishInflate() {
		mSwipeableContent = findViewById(R.id.goto_search_mode);
		mSwipeableContent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
			    //Aurora <SQF> <2014-12-01>  for NEW_UI begin
				if(((MailActivity)mActivity).getAuroraActionBarManager().isInSelectionMode() ) {
					//manager.setMode(AuroraActionBarManager.Mode.INITIAL_MODE);
					return;
				}
				//Aurora <SQF> <2014-12-01>  for NEW_UI end
				
			    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST begin
				if(mViewMode != null)
					mViewMode.enterSearchResultsListMode();
			    //Aurora <shihao> <2014-11-24> for set ViewMode to ViewMode.SEARCH_RESULTS_LIST end
				if (mAccount == null) {
					// We cannot search if there is no account. Drop the request to the floor.
					return;
				}
				
				((AuroraActivity) mActivity).showSearchviewLayout();
				mSearchView = ((AuroraActivity) mActivity).getSearchView(); 
				if (null == mSearchView) {
					return;
				}	
				
				mSearchView.setMaxLength(30);
				mSearchView.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
				((AuroraActivity) mActivity ).setOnQueryTextListener(new searchListener());
			}
		});
    }

    @Override
    public void onUpdate(Folder folder, ConversationCursor cursor) {
        mFolder = folder;
    }

    @Override
    public boolean getShouldDisplayInList() {
		return true;
    }

   

    @Override
    public int getPosition() {
        // We want this teaser to go before the first real conversation
        return 0;
    }

    @Override
    public void setAdapter(AnimatedAdapter adapter) {
        mAdapter = adapter;
    }

    @Override
    public void bindFragment(LoaderManager loaderManager, final Bundle savedInstanceState) {
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void onConversationSelected() {
        // DO NOTHING
    }

    @Override
    public void onCabModeEntered() {
    }

    @Override
    public void onCabModeExited() {
        // Do nothing
    }

    @Override
    public void onConversationListVisibilityChanged(final boolean visible) {
        // Do nothing
    }

    @Override
    public void saveInstanceState(final Bundle outState) {
        // Do nothing
    }

    @Override
    public boolean acceptsUserTaps() {
        return true;
    }

    @Override
    public void dismiss() {

    }

    @Override
    public SwipeableView getSwipeableView() {
        return SwipeableView.from(mSwipeableContent);
    }

    @Override
    public boolean canChildBeDismissed() {
        return false;
    }

    @Override
    public float getMinAllowScrollDistance() {
        return sScrollSlop;
    }


    /**
     * This method is used by the animator.  It is explicitly kept in proguard.flags to prevent it
     * from being removed, inlined, or obfuscated.
     * Edit ./vendor/unbundled/packages/apps/UnifiedGmail/proguard.flags
     * In the future, we want to use @Keep
     */
    public void setAnimatedHeight(final int height) {
        mAnimatedHeight = height;
        requestLayout();
    }


}

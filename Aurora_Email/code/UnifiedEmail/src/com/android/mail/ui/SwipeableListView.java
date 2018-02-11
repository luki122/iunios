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

package com.android.mail.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Scroller;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ListView;

import com.android.email.activity.UiUtilities;
import com.android.mail.R;
import com.android.mail.analytics.Analytics;
import com.android.mail.analytics.AnalyticsUtils;
import com.android.mail.browse.ConversationCursor;
import com.android.mail.browse.ConversationItemView;
import com.android.mail.browse.SwipeableConversationItemView;
import com.android.mail.providers.Account;
import com.android.mail.providers.Conversation;
import com.android.mail.providers.Folder;
import com.android.mail.providers.FolderList;
import com.android.mail.providers.UIProvider;
import com.android.mail.ui.SwipeHelper.Callback;
import com.android.mail.utils.LogTag;
import com.android.mail.utils.LogUtils;
import com.android.mail.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.android.mail.utils.MyLog;

import android.util.Log;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

import com.google.common.collect.ImmutableList;
import com.android.mail.ui.AbstractActivityController;
import com.android.mail.ui.AnimatedAdapter;
import com.android.mail.ui.ConversationListHelper;
import com.android.mail.ui.MailActivity;
import com.android.mail.ui.PullStatusView;

import android.view.LayoutInflater;
import android.view.View.MeasureSpec;
import android.view.animation.DecelerateInterpolator;
import android.widget.HeaderViewListAdapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Field;

import com.android.mail.ui.AuroraActionBarManager.Mode;
import com.aurora.email.AuroraComposeActivity;
import com.aurora.email.AuroraComposeActivity.AuroraSendStatusCallback;

import android.widget.AbsListView.OnScrollListener;

import com.android.mail.providers.UIProvider.ConversationColumns;
import com.android.mail.browse.ConversationListFooterView;

import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraAlertDialog.Builder;

import com.android.mail.providers.UIProvider.FolderType;

public class SwipeableListView extends AuroraListView implements Callback, OnScrollListener, 
								AuroraBackOnClickListener, 
								AuroraDeleteItemListener, 
								AuroraSendStatusCallback, 
								AuroraActionBarManager.ModeChangeListener, 
								ViewMode.ModeChangeListener {
	
	
    private final SwipeHelper mSwipeHelper;
    private boolean mEnableSwipe = false;

    public static final String LOG_TAG = LogTag.getLogTag();
    /**
     * Set to false to prevent the FLING scroll state from pausing the photo manager loaders.
     */
    private final static boolean SCROLL_PAUSE_ENABLE = true;

    /**
     * Set to true to enable parallax effect for attachment previews as the scroll position varies.
     * This effect triggers invalidations on scroll (!) and requires more memory for attachment
     * preview bitmaps.
     */
    public static final boolean ENABLE_ATTACHMENT_PARALLAX = true;

    /**
     * Set to true to queue finished decodes in an aggregator so that we display decoded attachment
     * previews in an ordered fashion. This artificially delays updating the UI with decoded images,
     * since they may have to wait on another image to finish decoding first.
     */
    public static final boolean ENABLE_ATTACHMENT_DECODE_AGGREGATOR = true;

    /**
     * The amount of extra vertical space to decode in attachment previews so we have image data to
     * pan within. 1.0 implies no parallax effect.
     */
    public static final float ATTACHMENT_PARALLAX_MULTIPLIER_NORMAL = 1.5f;
    public static final float ATTACHMENT_PARALLAX_MULTIPLIER_ALTERNATIVE = 2.0f;

    private ConversationSelectionSet mConvSelectionSet;
    private int mSwipeAction;
    private Account mAccount;
    private Folder mFolder;
    private ListItemSwipedListener mSwipedListener;
    private boolean mScrolling;

    private SwipeListener mSwipeListener;
    
    //Aurora <SQF> <2014-11-01>  for NEW_UI begin
    
    private boolean mEnableMultiSelection = true;
    
    private MailActivity mActivity;
    private PullStatusView mPullStatusView;
    private int mPullStatusViewHeight;
    private int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private boolean mBounceHack;
    
    private int mRefreshOriginalTopPadding;
    private int mLastMotionY;
    private int mMoveDistance = 0;
    private int mPullStatusViewRelativeTop;
    private float mLastY = -1;
    private final static float OFFSET_RADIO = 1.8f;
    
    // for mScroller, scroll back from header.
 	private int mScrollBack;
 	private final static int SCROLLBACK_HEADER = 0;
 	private final static int SCROLL_DURATION = 400; // scroll back duration
    
    private Scroller mScroller;
    private boolean mEnablePullRefresh = true;
    private boolean mPullRefreshing = false; // is refreshing?
    
    private OnRefreshListener mOnRefreshListener;
    
    public boolean isPullRefreshing() {
    	return mPullRefreshing;
    }
    
    
    public interface OnRefreshListener {
        public void onRefresh();
    }
    
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }
    
    public void setEnableMultiSelection(boolean enable) {
    	mEnableMultiSelection = enable;
    }
    
    public boolean isEnableMultiSelection() {
    	return mEnableMultiSelection;
    }
    
    private AuroraSlideRightListener mSlideRightListener = new AuroraSlideRightListener() {

		@Override
		public void prepareDraged(int index) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void slideBack(int index) {
			// TODO Auto-generated method stub
			if(mSlideRightToggleRead) {
				toggleAsRead(mSlideRightToggleConversation, ! mSlideRightToggleConversation.read);
				//refreshList();
				mSlideRightToggleRead = false;
				//paul add for animation start
				mConversationItemView.startAnimation();
				mConversationItemView = null;
				//paul add for animation end
			}
		}

		@Override
		public void slideSucess(int index) {
			// TODO Auto-generated method stub
			
		}
    	
    };

    private boolean mSlideRightToggleRead = false;
    private Conversation mSlideRightToggleConversation;// just for toggle as read/unread when slide right.
	private ConversationItemView mConversationItemView;//paul add for animation
	
    private class SlideRightOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			int id = view.getId();
			//paul modify for animation start
			mConversationItemView = ((SwipeableConversationItemView)view.getTag()).getSwipeableItemView();
			Conversation conv = mConversationItemView.getConversation();
			//paul modify for animation end
			if(id == R.id.btn_mark_as) {
				if(! (view.getTag() instanceof SwipeableConversationItemView) ) {
					return;
				}
				mSlideRightToggleRead = true;
				mSlideRightToggleConversation = conv;
				
				startBackWithAnim();
			} else if(id == R.id.btn_move_to) {
				moveToFolder(conv);
			}
		}
    	
    }
    
    private SlideRightOnClickListener mSlideRightOnClickListener = new SlideRightOnClickListener();
    
    public void setActivity(MailActivity activity) {
    	this.mActivity = activity;
    	if(mPullStatusView != null) {
    		//Log.i("SQF_LOG", "mPullStatusView -----> setActivity...........");
    		mPullStatusView.setActivity(mActivity);
    		mPullStatusView.updateLastUpdateTime();
    	}
    	//Log.i("SQF_LOG", "mPullStatusView -----> setActivity...........mActivity == null:" + (mActivity == null));
    	mActivity.getAuroraActionBarManager().registerModeChangeListener(this);
    }
    
    @Override
    public void auroraActionBarModeChanged(Mode changedTo) {
    	AbstractActivityController controller = (AbstractActivityController)mActivity.getFolderController();
    	if(changedTo == AuroraActionBarManager.Mode.DASH_BOARD_MODE) {
    		//Log.i("SQF_LOG", "auroraActionBarModeChanged -----> ....disableLeftAndRightSlide");
    		controller.setDrawerLockMode(false);
        	disableLeftAndRightSlide();
    	} else {
    		//Log.i("SQF_LOG", "auroraActionBarModeChanged -----> ....enableLeftAndRightSlide");
    		controller.setDrawerLockMode(true);
        	enableLeftAndRightSlide();
    	}
    }
    
    private void initPullStatusView(Context context) {
    	//Log.i("SQF_LOG", "SwipeableListView::initPullStatusView -----");
    	LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mPullStatusView = (PullStatusView) inflater.inflate(R.layout.aurora_pull_status_view, null);
    	addHeaderView(mPullStatusView, null, false);
    }  
    
    public void onRefresh() {
    	//Log.i("SQF_LOG", " onRefresh =================================");
        if (mOnRefreshListener != null) {
        	//Log.i("SQF_LOG", "SwipeableListView::onRefresh ==============================startLoading===");
        	mPullStatusView.startLoading();
            mOnRefreshListener.onRefresh();
        }
        startTimeout();
    }
    
    /*
     * if isChangingFolderRefresh, then showFooter return false, don't show footer
     * @return does listview need show load more ?
     */
    public boolean checkToStopRefresh(final ConversationCursor cursor, boolean isChangingFolderRefresh) {
    	AbstractActivityController controller = (AbstractActivityController)mActivity.getFolderController();
    	boolean showFooter = mPullRefreshing ? false : true;
    	if (cursor == null) {
    		showFooter = false;
    		//Log.i("SQF_LOG", "SwipeableListView::checkToStopRefresh 111111--->showFooter:" +  showFooter );
    		return showFooter;
        }
        final Bundle extras = cursor.getExtras();
        final int cursorStatus = extras.getInt(UIProvider.CursorExtraKeys.EXTRA_STATUS);
        int mErrorStatus = extras.containsKey(UIProvider.CursorExtraKeys.EXTRA_ERROR) ?
                extras.getInt(UIProvider.CursorExtraKeys.EXTRA_ERROR)
                : UIProvider.LastSyncResult.SUCCESS;
        final int totalCount = extras.getInt(UIProvider.CursorExtraKeys.EXTRA_TOTAL_COUNT);
        if (UIProvider.CursorStatus.isWaitingForResults(cursorStatus)) {
        	//do nothing.
        } else if (mErrorStatus != UIProvider.LastSyncResult.SUCCESS) {
        	//MyLog.i("SQF_LOG", "SwipeableListView::checkToStopRefresh stopRefresh 11111");
        	stopRefresh();
        	controller.setChangingFolderRefresh(false);
        } else if (mActivity.getFolderController().getFolder().loadMoreUri != null && cursor.getCount() < totalCount) {
        	//MyLog.i("SQF_LOG", "SwipeableListView::checkToStopRefresh stopRefresh 22222");
        	stopRefresh();
        	controller.setChangingFolderRefresh(false);
        } else {
            showFooter = false;
        }
        //Log.i("SQF_LOG", "SwipeableListView::checkToStopRefresh 22222--->" + ( showFooter & (!isChangingFolderRefresh) ) );
        return showFooter & (!isChangingFolderRefresh);
    }
    
    
    public void onRefreshComplete() {
        stopRefresh();
    }

    //Aurora <SQF> <2014-11-01>  for NEW_UI end
    
    //Aurora <SQF> <2014-10-24>  for NEW_UI begin
    
    public boolean isInSelectionMode() {
    	AnimatedAdapter adapter = getAnimatedAdapter();//(AnimatedAdapter)getAdapter();
    	return adapter.isInSelectionMode();
    }

    private void enableLeftAndRightSlide() {
    	//MyLog.i2("SQF_LOG", "initAuroraListView");
    	auroraSetNeedSlideDelete(true);
    	auroraSetAuroraBackOnClickListener(this);
    	auroraSetDeleteItemListener(this);
    	enableSlideRight();
    }
    
    private void disableLeftAndRightSlide() {
    	auroraSetNeedSlideDelete(false);
    	auroraSetAuroraBackOnClickListener(null);
    	auroraSetDeleteItemListener(null);
    	disableSlideRight();
    }
    //Aurora <SQF> <2014-10-24>  for NEW_UI end

    // Instantiated through view inflation
    @SuppressWarnings("unused")
    public SwipeableListView(Context context) {
        this(context, null);
    }

    public SwipeableListView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeableListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //Aurora <SQF> <2014-10-24>  for NEW_UI begin
        
        mScroller = new Scroller(context, new DecelerateInterpolator());
        enableLeftAndRightSlide();
        setSlideRightListener(mSlideRightListener);
        //Aurora <SQF> <2014-10-24>  for NEW_UI end
        setOnScrollListener(this);
        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(context, SwipeHelper.X, this, densityScale,
                pagingTouchSlop);
        
        //Aurora <SQF> <2014-10-31>  for NEW_UI begin
        initPullStatusView(context);
        mPullStatusViewHeight = getResources().getDimensionPixelSize(R.dimen.aurora_pull_status_view_height);    
        //Log.i("SQF_LOG", "mPullStatusViewHeight = " + mPullStatusViewHeight);
        setDividerHeight(1);
        //Aurora <SQF> <2014-10-31>  for NEW_UI end
        AuroraComposeActivity.registerSendStatusCallback(this);
        
        super.auroraEnableOverScroll(false);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        LogUtils.d(Utils.VIEW_DEBUGGING_TAG,
                "START CLF-ListView.onFocusChanged layoutRequested=%s root.layoutRequested=%s",
                isLayoutRequested(), getRootView().isLayoutRequested());
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        LogUtils.d(Utils.VIEW_DEBUGGING_TAG, new Error(),
                "FINISH CLF-ListView.onFocusChanged layoutRequested=%s root.layoutRequested=%s",
                isLayoutRequested(), getRootView().isLayoutRequested());
    }

    /**
     * Enable swipe gestures.
     */
    public void enableSwipe(boolean enable) {
        mEnableSwipe = enable;
    }

    public void setSwipeAction(int action) {
        mSwipeAction = action;
    }

    public void setSwipedListener(ListItemSwipedListener listener) {
        mSwipedListener = listener;
    }

    public int getSwipeAction() {
        return mSwipeAction;
    }

    public void setSelectionSet(ConversationSelectionSet set) {
        mConvSelectionSet = set;
    }

    public void setCurrentAccount(Account account) {
        mAccount = account;
    }

    public void setCurrentFolder(Folder folder) {
        mFolder = folder;
    }

    @Override
    public ConversationSelectionSet getSelectionSet() {
        return mConvSelectionSet;
    }

    //Aurora <SQF> <2014-10-30>  for NEW_UI begin
    //ORIGINALLY:
    /*
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrolling || !mEnableSwipe) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
		}
	}
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnableSwipe) {
            return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
        } else {
            return super.onTouchEvent(ev);
        }
    }
    */
    //SQF MODIFIED TO:
    
    
    private static final int REFRESH_ANIM_TIMEOUT = 10000;
    private Handler mRefreshTimeOutHandler = new Handler();
    
    public void startTimeout() {
	    mRefreshTimeOutHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				stopRefresh();
			}
		}, REFRESH_ANIM_TIMEOUT);
    }
    
    public boolean isDoingSlideRightOperation() {
    	//// when slide right or left, (auroraGetAuroraItemState() != 0) return true
    	if(auroraIsSliding() || auroraGetAuroraItemState() != 0) {
    		return true;
    	}
    	return false;
    }
    
    private boolean mAllowSlideDown = true;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(auroraIsSliding() || auroraGetAuroraItemState() != 0 || !mAllowSlideDown) {
    		//// when slide right or left, (auroraGetAuroraItemState() != 0) return true
    		mAllowSlideDown = false;
    		if(event.getAction() == MotionEvent.ACTION_UP) {
    			mAllowSlideDown = true;
    		}
    		return super.onTouchEvent(event);
    	}
    	
    	if (mLastY == -1) {
			mLastY = event.getRawY();
		}
    	
        switch (event.getAction()) {
        
        case MotionEvent.ACTION_DOWN:
        	mLastY = event.getRawY();
            break;
            
        case MotionEvent.ACTION_MOVE:
			final float deltaY = event.getRawY() - mLastY;
			mLastY = event.getRawY();
			if (getFirstVisiblePosition() == 0 && mEnablePullRefresh
					&& (mPullStatusView.getVisibleHeight() > 0 || deltaY > 0)) {
				// the first item is showing, header has shown or pull down.
				updateHeaderHeight(deltaY / OFFSET_RADIO);
				//invokeOnScrolling();
			}
			break;
		default:
			mLastY = -1; // reset
			if (getFirstVisiblePosition() == 0) {
				// invoke refresh
				if (mEnablePullRefresh
						&& mPullStatusView.getVisibleHeight() > mPullStatusViewHeight) {
					mPullRefreshing = true;
					mPullStatusView.startLoading();
					onRefresh();
				}
				resetHeaderHeight();
			} 
			
			stopStarredFolderRefreshing();
			
			break;
            
        }
        return super.onTouchEvent(event);
    }
    
    public void stopStarredFolderRefreshing() {
    	//when in starred Folder, stop Refreshing immediately.
		if(mActivity.getFolderController().getFolder().isStared()) {
        	new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					stopRefreshWithoutJudgeScrolling();
				}
        	}, 500);
        }
    }
    
	private void resetHeaderHeight() {
		int height = mPullStatusView.getVisibleHeight();
		if (height == 0) // not visible.
			return;
		// refreshing and header isn't shown fully. do nothing.
		if (mPullRefreshing && height <= mPullStatusViewHeight) {
			return;
		}
		int finalHeight = 0; // default: scroll back to dismiss header.
		// is refreshing, just scroll back to show all the header.
		if (mPullRefreshing && height > mPullStatusViewHeight) {
			finalHeight = mPullStatusViewHeight;
		}
		mScrollBack = SCROLLBACK_HEADER;
		//Log.i("SQF_LOG", "height:" + height + " ---------------- " + " finalHeight - height:" + (finalHeight - height));
		mScroller.startScroll(0, height, 0, finalHeight - height,
				SCROLL_DURATION);
		// trigger computeScroll
		invalidate();
	}
    
	public void startRefreshing() {
		if (mPullRefreshing)
			return;

		mPullRefreshing = true;
		mPullStatusView.startLoading();

		if (mPullStatusViewHeight > 0) {
			mPullStatusView.setVisibleHeight(mPullStatusViewHeight);
			mScroller.startScroll(0, mPullStatusViewHeight, 0, 0, 0);
			setSelection(0);
			scrollTo(0, 0);
		}

		onRefresh();
	}
	
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			if (mScrollBack == SCROLLBACK_HEADER) {
				mPullStatusView.setVisibleHeight(mScroller.getCurrY());
			}
			postInvalidate();
		}
		super.computeScroll();
	}

//	public void startRefreshingAndScrollBack() {
//		if (mPullRefreshing)
//			return;
//
//		mPullRefreshing = true;
//		mPullStatusView.startLoading();
//
//		if (mPullStatusViewHeight > 0) {
//			mPullStatusView.setVisiableHeight(mPullStatusViewHeight);
//			mScroller.startScroll(0, mPullStatusViewHeight, 0, 0, 0);
//			setSelection(0);
//			scrollTo(0, 0);
//		}
//
//		onRefresh();
//	}
    //Aurora <SQF> <2014-10-30>  for NEW_UI end
    

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        // find the view under the pointer, accounting for GONE views
        final int count = getChildCount();
        final int touchY = (int) ev.getY();
        int childIdx = 0;
        View slidingChild;
        for (; childIdx < count; childIdx++) {
            slidingChild = getChildAt(childIdx);
            if (slidingChild.getVisibility() == GONE) {
                continue;
            }
            if (touchY >= slidingChild.getTop() && touchY <= slidingChild.getBottom()) {
                if (slidingChild instanceof SwipeableConversationItemView) {
                    return ((SwipeableConversationItemView) slidingChild).getSwipeableItemView();
                }
                return slidingChild;
            }
        }
        return null;
    }

    @Override
    public boolean canChildBeDismissed(SwipeableItemView v) {
        return v.canChildBeDismissed();
    }

    @Override
    public void onChildDismissed(SwipeableItemView v) {
        if (v != null) {
            v.dismiss();
        }
    }

    // Call this whenever a new action is taken; this forces a commit of any
    // existing destructive actions.
    public void commitDestructiveActions(boolean animate) {
        final AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter != null) {
            adapter.commitLeaveBehindItems(animate);
        }
    }

    public void dismissChild(final ConversationItemView target) {
        final ToastBarOperation undoOp;

        undoOp = new ToastBarOperation(1, mSwipeAction, ToastBarOperation.UNDO, false /* batch */,
                mFolder);
        Conversation conv = target.getConversation();
        target.getConversation().position = findConversation(target, conv);
        final AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter == null) {
            return;
        }
        adapter.setupLeaveBehind(conv, undoOp, conv.position, target.getHeight());
        ConversationCursor cc = (ConversationCursor) adapter.getCursor();
        Collection<Conversation> convList = Conversation.listOf(conv);
        ArrayList<Uri> folderUris;
        ArrayList<Boolean> adds;

        Analytics.getInstance().sendMenuItemEvent("list_swipe", mSwipeAction, null, 0);

        if (mSwipeAction == R.id.remove_folder) {
            FolderOperation folderOp = new FolderOperation(mFolder, false);
            HashMap<Uri, Folder> targetFolders = Folder
                    .hashMapForFolders(conv.getRawFolders());
            targetFolders.remove(folderOp.mFolder.folderUri.fullUri);
            final FolderList folders = FolderList.copyOf(targetFolders.values());
            conv.setRawFolders(folders);
            final ContentValues values = new ContentValues();
            folderUris = new ArrayList<Uri>();
            folderUris.add(mFolder.folderUri.fullUri);
            adds = new ArrayList<Boolean>();
            adds.add(Boolean.FALSE);
            ConversationCursor.addFolderUpdates(folderUris, adds, values);
            ConversationCursor.addTargetFolders(targetFolders.values(), values);
            cc.mostlyDestructiveUpdate(Conversation.listOf(conv), values);
        } else if (mSwipeAction == R.id.archive) {
            cc.mostlyArchive(convList);
        } else if (mSwipeAction == R.id.delete) {
            cc.mostlyDelete(convList);
        }
        if (mSwipedListener != null) {
            mSwipedListener.onListItemSwiped(convList);
        }
        adapter.notifyDataSetChanged();
        if (mConvSelectionSet != null && !mConvSelectionSet.isEmpty()
                && mConvSelectionSet.contains(conv)) {
            mConvSelectionSet.toggle(conv);
            // Don't commit destructive actions if the item we just removed from
            // the selection set is the item we just destroyed!
            if (!conv.isMostlyDead() && mConvSelectionSet.isEmpty()) {
                commitDestructiveActions(true);
            }
        }
    }

    @Override
    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
        cancelDismissCounter();

        // Notifies {@link ConversationListView} to disable pull to refresh since once
        // an item in the list view has been picked up, we don't want any vertical movement
        // to also trigger refresh.
        if (mSwipeListener != null) {
            mSwipeListener.onBeginSwipe();
        }
    }

    @Override
    public void onDragCancelled(SwipeableItemView v) {
        final AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter != null) {
            adapter.startDismissCounter();
            adapter.cancelFadeOutLastLeaveBehindItemText();
        }
    }

    /**
     * Archive items using the swipe away animation before shrinking them away.
     */
    public boolean destroyItems(Collection<Conversation> convs,
            final ListItemsRemovedListener listener) {
        if (convs == null) {
            LogUtils.e(LOG_TAG, "SwipeableListView.destroyItems: null conversations.");
            return false;
        }
        final AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter == null) {
            LogUtils.e(LOG_TAG, "SwipeableListView.destroyItems: Cannot destroy: adapter is null.");
            return false;
        }
        adapter.swipeDelete(convs, listener);
        return true;
    }

    public int findConversation(ConversationItemView view, Conversation conv) {
        int position = INVALID_POSITION;
        long convId = conv.id;
        try {
            position = getPositionForView(view);
        } catch (Exception e) {
            position = INVALID_POSITION;
            LogUtils.w(LOG_TAG, e, "Exception finding position; using alternate strategy");
        }
        if (position == INVALID_POSITION) {
            // Try the other way!
            Conversation foundConv;
            long foundId;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child instanceof SwipeableConversationItemView) {
                    foundConv = ((SwipeableConversationItemView) child).getSwipeableItemView()
                            .getConversation();
                    foundId = foundConv.id;
                    if (foundId == convId) {
                        position = i + getFirstVisiblePosition();
                        break;
                    }
                }
            }
        }
        return position;
    }
    
    //Aurora <SQF> <2014-11-27>  for NEW_UI begin
    public void setShowFooter(boolean show) {
    	AnimatedAdapter adapter = getAnimatedAdapter();
    	if(adapter == null) return;
    	adapter.setShowFooter(show);
    }
    //Aurora <SQF> <2014-11-27>  for NEW_UI end

    public AnimatedAdapter getAnimatedAdapter() {
        //Aurora <SQF> <2014-10-31>  for NEW_UI begin
        if(getAdapter() instanceof HeaderViewListAdapter) {
        	return (AnimatedAdapter) ((HeaderViewListAdapter)getAdapter()).getWrappedAdapter();
        } 
        //Aurora <SQF> <2014-10-31>  for NEW_UI end
        return (AnimatedAdapter) getAdapter();
    }
    
    @Override
    public boolean performItemClick(View view, int pos, long id) {
    	
        //Aurora <SQF> <2014-10-24>  for NEW_UI begin
    	if(isInSelectionMode()) {
    		((AnimatedAdapter)getAnimatedAdapter()).setNeedsCheckBoxAnim(false);
    		toggleMultiSelection(view, pos);
    		return true;
    	} 
    	//Aurora <SQF> <2014-10-24>  for NEW_UI end

        final int previousPosition = getCheckedItemPosition();
        final boolean selectionSetEmpty = mConvSelectionSet.isEmpty();

        // Superclass method modifies the selection set
        final boolean handled = super.performItemClick(view, pos, id);

        // If we are in CAB mode then a click shouldn't
        // activate the new item, it should only add it to the selection set
        if (!selectionSetEmpty && previousPosition != -1) {
            setItemChecked(previousPosition, true);
        }
        // Commit any existing destructive actions when the user selects a
        // conversation to view.
        commitDestructiveActions(true);
        return handled;
    }

    @Override
    public void onScroll() {
        commitDestructiveActions(true);
    }

    public interface ListItemsRemovedListener {
        public void onListItemsRemoved();
    }

    public interface ListItemSwipedListener {
        public void onListItemSwiped(Collection<Conversation> conversations);
    }
    
	public void setPullRefreshEnable(boolean enable) {
		mEnablePullRefresh = enable;
		if (!mEnablePullRefresh) { // disable, hide the content
			stopRefreshWithoutJudgeScrolling();
		} else {
			mPullStatusView.show();
		}
	}
    
	public void updateHeaderHeight(float delta) {
		mPullStatusView.setVisibleHeight((int) delta + mPullStatusView.getVisibleHeight());
		if (mEnablePullRefresh && ! mPullRefreshing) {
			if (mPullStatusView.getVisibleHeight() > mPullStatusViewHeight) {
				mPullStatusView.startLoading();
			} else {
				mPullStatusView.stopLoading();
			}
		}
		setSelection(0); // scroll to top each time
	}
	
	Handler mDelayStopRefreshHandler = new Handler();
	/**
	 * stop refresh, reset header view.
	 */
	public void stopRefresh() {
		if( ! mScroller.isFinished()) {
			//MyLog.i("SQF_LOG", "SwipeableListView::stopRefresh.....! mScroller.isFinished()  return ...............11111");
			return;
		}
		//MyLog.i("SQF_LOG", "stopRefresh.....");
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			mPullStatusView.stopLoading();
			resetHeaderHeight();
			//MyLog.i2("SQF_LOG", "SwipeableListView::stopRefresh....................................................22222");
		} else {
			mPullStatusView.setVisibleHeight(0);
			postInvalidate();
			//MyLog.i("SQF_LOG", "SwipeableListView::stopRefresh................................................33333");
		}
	}
	
	public void stopRefreshWithoutJudgeScrolling() {
		if (mPullRefreshing == true) {
			mPullRefreshing = false;
			mPullStatusView.stopLoading();
			resetHeaderHeight();
		} else {
			mPullStatusView.setVisibleHeight(0);
			postInvalidate();
		}
	}

    @Override
    public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    	/*
    	MyLog.i("SQF_LOG", "onScroll 2222222 firstVisibleItem:" + firstVisibleItem + 
    			" visibleItemCount:" + visibleItemCount + 
    			" totalItemCount:" + totalItemCount);
		*/
        //Aurora <SQF> <2014-12-12>  for NEW_UI begin
    	
    	boolean isInSearchResultsListMode = mActivity == null ? false : mActivity.getViewMode().getMode() == ViewMode.SEARCH_RESULTS_LIST;
    	if(firstVisibleItem + visibleItemCount >= totalItemCount && mScrolling && ! isInSearchResultsListMode) {
    		//trigger load more
    		if(getFooterView() != null) {
    			getFooterView().loadMore();
    		}
    	}
    	//Aurora <SQF> <2014-12-12>  for NEW_UI end
    	
        //Aurora <SQF> <2014-12-12>  for ANNOTATION begin
        /*
        if (ENABLE_ATTACHMENT_PARALLAX) {
            for (int i = 0, len = getChildCount(); i < len; i++) {
                final View child = getChildAt(i);
                if (child instanceof OnScrollListener) {
                    ((OnScrollListener) child).onScroll(view, firstVisibleItem, visibleItemCount,
                            totalItemCount);
                }
            }
        } 
        */
        //Aurora <SQF> <2014-12-12>  for ANNOTATION end
    	
    }

    @Override
    public void onScrollStateChanged(final AbsListView view, final int scrollState) {
        //Aurora <SQF> <2014-10-31>  for NEW_UI begin
		/*
    	if(scrollState == SCROLL_STATE_FLING) {
    		Log.i("SQF_LOG", "SwipeableListView::onScrollStateChanged  change to scrollState : SCROLL_STATE_FLING");
    	} else if(scrollState == SCROLL_STATE_TOUCH_SCROLL) {
    		Log.i("SQF_LOG", "SwipeableListView::onScrollStateChanged  change to scrollState : SCROLL_STATE_TOUCH_SCROLL");
    	} else if(scrollState == SCROLL_STATE_IDLE) {
    		Log.i("SQF_LOG", "SwipeableListView::onScrollStateChanged  change to scrollState : SCROLL_STATE_IDLE");
    	}
		*/
        //Aurora <SQF> <2014-10-31>  for NEW_UI end

        mScrolling = scrollState != OnScrollListener.SCROLL_STATE_IDLE;
        if (!mScrolling) {
            final Context c = getContext();
            if (c instanceof ControllableActivity) {
                final ControllableActivity activity = (ControllableActivity) c;
                activity.onAnimationEnd(null /* adapter */);
            } else {
                LogUtils.wtf(LOG_TAG, "unexpected context=%s", c);
            }
        }

        if (SCROLL_PAUSE_ENABLE) {
            AnimatedAdapter adapter = getAnimatedAdapter();
            if (adapter != null) {
                adapter.onScrollStateChanged(scrollState);
            }
            ConversationItemView.setScrollStateChanged(scrollState);
        }
    }
    
    //Aurora <SQF> <2014-10-31>  for NEW_UI begin	
    @Override
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null) {
        	getAnimatedAdapter().setSlideRightOnClickListener(mSlideRightOnClickListener);
        }
    }
	//Aurora <SQF> <2014-10-31>  for NEW_UI end

    public boolean isScrolling() {
        return mScrolling;
    }

    @Override
    public void cancelDismissCounter() {
        AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter != null) {
            adapter.cancelDismissCounter();
        }
    }

    @Override
    public LeaveBehindItem getLastSwipedItem() {
        AnimatedAdapter adapter = getAnimatedAdapter();
        if (adapter != null) {
            return adapter.getLastLeaveBehindItem();
        }
        return null;
    }

    public void setSwipeListener(SwipeListener swipeListener) {
        mSwipeListener = swipeListener;
    }

    public interface SwipeListener {
        public void onBeginSwipe();
    }
    
    
    
    //Aurora <SQF> <2014-10-24>  for NEW_UI begin

    public boolean isContentHeightLessThanArea() {
    	//Log.e("SQF_LOG", "isContentHeightLessThanArea ===================1");
		ListAdapter mAdapter = (ListAdapter)getAnimatedAdapter();
		if(mAdapter == null) {
			//Log.i("SQF_LOG", "isContentHeightLessThanArea 1111111 true");
			//Log.e("SQF_LOG", "isContentHeightLessThanArea ===================2");
			return true;
		}
	    int totalHeight = 0;
	    int listHeight = getHeight();
	    //Log.e("SQF_LOG", "mAdapter getCount() --> "+ mAdapter.getCount() + " listHeight:" + listHeight);
	    
	    for (int i = 1; i < mAdapter.getCount() - 1; i++) {//i start from 1, to exclude PullStatusView
	        View view = mAdapter.getView(i, null, this);
	        //Log.i("SQF_LOG", "getView class :" + view.getClass().getName());
	        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
	        view.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED),
	                MeasureSpec.makeMeasureSpec(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED));
	        totalHeight += view.getMeasuredHeight();
	        if(totalHeight > listHeight) {
	        	//Log.i("SQF_LOG", "isContentHeightLessThanArea 2222222 false");
	        	//Log.e("SQF_LOG", "isContentHeightLessThanArea ===================2");
		    	return false;
		    }
	        //Log.e("SQF_LOG", "totalHeight:" + String.valueOf(totalHeight));
	        
	    }
	    
	    //Log.e("SQF_LOG", "listHeight:" + listHeight);
	    if(totalHeight <= listHeight) {
	    	//Log.i("SQF_LOG", "isContentHeightLessThanArea 3333333 true");
	    	//Log.e("SQF_LOG", "isContentHeightLessThanArea ===================2");
	    	return true;
	    }
	    //Log.i("SQF_LOG", "isContentHeightLessThanArea 4444444 false");
	    //Log.e("SQF_LOG", "isContentHeightLessThanArea ===================2");
	    return false;
	}
    
    @Override
  	public void auroraOnClick(final int position) {
    	Log.i("JXH", "auroraOnClick");
    	/*if(ConversationListHelper.getSetDividerPostion()>position){
    		ConversationListHelper.setSetDividerPostion(0);
    	}*/
    	Folder folder = mActivity.getFolderController().getFolder();
    	if(folder.type == UIProvider.FolderType.DRAFT) {
    		//only in DRAFT, need alertdialog.
	    	AuroraAlertDialog builder = new AuroraAlertDialog.Builder(mActivity)
	        .setTitle(R.string.aurora_list_delete_item_tip_title)
			.setMessage(R.string.aurora_list_delete_item_tip_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					
					auroraDeleteSelectedItemAnim();
				}
			}).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int arg1) {
					// TODO Auto-generated method stub
					dialog.dismiss();
				}
			}).create();
	    	builder.show();
    	
    	} else {
    		auroraDeleteSelectedItemAnim();
    	}
    }

    @Override
    public void auroraPrepareDraged(int position) {
    	Log.i("SQF_LOG", "auroraPrepareDraged");
  	}

    @Override
    public void auroraDragedSuccess(int position) {
    	Log.i("SQF_LOG", "auroraDragedSuccess");
  	}

    @Override
    public void auroraDragedUnSuccess(int position) {
    	Log.i("SQF_LOG", "auroraDragedUnSuccess");
  	}
    
    @Override
    public void auroraDeleteItem(View v, int position) { 	
    	ConversationItemView item = findConversationItemView(v);
    	if(item == null) return;
    	Conversation conv = item.getConversation();
        ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
        if (cursor != null) {
            cursor.delete(ImmutableList.of(conv));
        }
    }
    
    public void deleteSelectedConversations() {
    	ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
    	cursor.delete(mConvSelectionSet.values());
    	getAnimatedAdapter().notifyDataSetChanged();
    }
    
    public void toggleAsStarred(boolean trueOrFalse) {
    	Collection<Conversation> selectedConversations = mConvSelectionSet.values();
    	ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
    	Iterator<Conversation> it = selectedConversations.iterator();
    	while(it.hasNext()) {
    		cursor.updateBoolean(it.next(), ConversationColumns.STARRED, trueOrFalse);
    	}
    }
    
    /*
     * toggleAsRead(boolean trueOrFalse) for Aurora menu
     */
    public void toggleAsRead(boolean trueOrFalse) {
    	Collection<Conversation> selectedConversations = mConvSelectionSet.values();
    	ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
    	Iterator<Conversation> it = selectedConversations.iterator();
    	while(it.hasNext()) {
    		cursor.updateBoolean(it.next(), ConversationColumns.READ, trueOrFalse);
    	}
    }
    
    /*
     * toggleAsRead(Conversation converation, boolean trueOrFalse) for slide right
     */
    public void toggleAsRead(Conversation converation, boolean trueOrFalse) {
    	ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
    	cursor.updateBoolean(converation, ConversationColumns.READ, trueOrFalse);
    }
    
    public void archiveSelectedConversation() {
    	Collection<Conversation> selectedConversations = mConvSelectionSet.values();
    	ConversationCursor cursor = getAnimatedAdapter().getConversationCursor();
    	cursor.archive(selectedConversations);
    }

    public ConversationItemView findConversationItemView(View view) {
    	ViewGroup v = (ViewGroup)view.findViewById(com.aurora.R.id.aurora_listview_front);
    	if(v == null) return null;
    	for(int i=0; i<v.getChildCount(); i++) {
    		View child = v.getChildAt(i);
    		if(child instanceof SwipeableConversationItemView) {
    			SwipeableConversationItemView swipeableConvItemView = (SwipeableConversationItemView)child;
    			return (ConversationItemView)swipeableConvItemView.getChildAt(0);
    		}
    	}
    	return null;
    }

    public void setBottomBarEnableStatus() {
    	boolean enableBottomBar = ! mConvSelectionSet.isEmpty();
    	mActivity.getAuroraActionBarManager().enableActionBarBottomMenu(enableBottomBar);
    }
    
	public void toggleMultiSelection(View view, int pos) {
		if (!isInSelectionMode()) return;
		//Object obj = view.getTag();
		ConversationItemView itemView = findConversationItemView(view);
		if (itemView == null) return;
		
		if (ConversationItemView.mStarInvisiable) {
			itemView.setStarInvisiable();
		}
		
		Conversation conv = itemView.getConversation();
		mConvSelectionSet.toggle(conv);
		refreshList();
		
		mActivity.getAuroraActionBarManager().checkAllConversationSelected();
		setMiddleText(mConvSelectionSet.size());
		setBottomBarEnableStatus();
	}
	
	public void toggleSelections(ArrayList<Conversation> list) {
		for(Conversation c : list) {
			mConvSelectionSet.toggle(c);
		}
		refreshList();
		setBottomBarEnableStatus();
	}
	
	public void selectAll(ArrayList<Conversation> list) {
		mConvSelectionSet.putAll(list);
		refreshList();
		setBottomBarEnableStatus();
		setMiddleText(mConvSelectionSet.size());
		
	}
	
	public void cancelAllSelect(ArrayList<Conversation> list) {
		mConvSelectionSet.removeAll(list);
		refreshList();
		setBottomBarEnableStatus();
	}
	
	public void setMiddleText(int listsize) {
			mActivity.getAuroraActionBar().getMiddleTextView()
			.setText(mActivity.getResources().getString(R.string.seleted, listsize));
	}
	
	public void refreshList() {
		AnimatedAdapter adapter = getAnimatedAdapter();
		adapter.notifyDataSetChanged();
	}

	public void moveToFolder(Conversation conv) {
		mConvSelectionSet.clear();
		mConvSelectionSet.put(conv);
		Intent intent = new Intent(mActivity, AuroraSelectMoveToFolderActivity.class);
		Account account = mActivity.getAccountController().getAccount();
		Folder currentFolder = mActivity.getFolderController().getFolder();
		intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_ACCOUNT, account);
		intent.putExtra(AuroraSelectMoveToFolderActivity.INTENT_KEY_CURRENT_FOLDER, currentFolder);
		mActivity.startActivityForResult(intent, AbstractActivityController.SELECT_FOLDER_REQUEST_CODE);
	}
	
	
	private static final int SEND_MAIL_TIP_DELAY_SECONDS = 3000;

	
	
	private static final int MSG_SEND_MAIL_TIP_SUCCESS = 0;
	private static final int MSG_SEND_MAIL_TIP_FAILURE = 0;
	private Handler mSendMailTipHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//super.handleMessage(msg);
			if(msg.what == MSG_SEND_MAIL_TIP_SUCCESS) {
				Log.i("SQF_LOG", "SwipeableListView::mSendMailTipHandler::handleMessage MSG_SEND_MAIL_TIP_SUCCESS...");
				mPullStatusView.switchStatus(PullStatusView.STATUS_INITAL);
			} else if(msg.what == MSG_SEND_MAIL_TIP_FAILURE) {
				Log.i("SQF_LOG", "SwipeableListView::mSendMailTipHandler::handleMessage MSG_SEND_MAIL_TIP_FAILURE...");
				mPullStatusView.switchStatus(PullStatusView.STATUS_INITAL);
			}
		}
		
	};

	public void onSendStatusChanged(AuroraComposeActivity.AuroraSendStatus status) {
		if(status == AuroraComposeActivity.AuroraSendStatus.AURORAEMAIL_SEND_START) {
			Log.i("SQF_LOG", "SwipeableListView::onSendStatusChanged start");
			mPullStatusView.switchStatus(PullStatusView.STATUS_SENDING);
		} else if(status == AuroraComposeActivity.AuroraSendStatus.AURORAEMAIL_SEND_SUCCES) {
			Log.i("SQF_LOG", "SwipeableListView::onSendStatusChanged AURORAEMAIL_SEND_SUCCES...");
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mPullStatusView.switchStatus(PullStatusView.STATUS_SEND_SUCCESS_TIP);
				}
			});
			mSendMailTipHandler.sendEmptyMessageDelayed(MSG_SEND_MAIL_TIP_SUCCESS, SEND_MAIL_TIP_DELAY_SECONDS);
			
		} else if(status == AuroraComposeActivity.AuroraSendStatus.AURORAEMAIL_SEND_FAIL) {
			Log.i("SQF_LOG", "SwipeableListView::onSendStatusChanged AURORAEMAIL_SEND_FAIL...");
			mActivity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mPullStatusView.switchStatus(PullStatusView.STATUS_SEND_FAILURE_TIP);
				}
			});
			mSendMailTipHandler.sendEmptyMessageDelayed(MSG_SEND_MAIL_TIP_FAILURE, SEND_MAIL_TIP_DELAY_SECONDS);
		}
	}
	
	private void showSearchView(boolean toShow) {
		/*
		AnimatedAdapter adapter = getAnimatedAdapter();
		Log.i("SQF_LOG", "SwipeableListView::::: showSearchView =====:");
		if(adapter.getCount() <= 0 || adapter.getConversationCursor() == null) return;
		Log.i("SQF_LOG", "SwipeableListView::::: showSearchView 00000:");
		for(int i=0; i<adapter.getCount(); i++) {
			View v = adapter.getView(i, null, null);
			Log.i("SQF_LOG", "SwipeableListView::::: showSearchView 11111:");
			if(v instanceof ConversationSearchView) {
				Log.i("SQF_LOG", "SwipeableListView::::: showSearchView toShow:" + toShow);
				if(toShow) {
					v.setVisibility(View.VISIBLE);
				} else {
					v.setVisibility(View.GONE);
				}
				return;
			}
		}
		*/
		
		AnimatedAdapter adapter = getAnimatedAdapter();
		adapter.setNeedsSearchView(toShow);
	}
	
    @Override
    public void onViewModeChanged(int newMode) {
        if (newMode == ViewMode.CONVERSATION_LIST) {
        	auroraSetRubbishBackNoAnim();//for left slide
        	startBack();//for right slide
        	setPullRefreshEnable(true);
        	setEnableMultiSelection(true);
        	enableLeftAndRightSlide();
        } else if(newMode == ViewMode.SEARCH_RESULTS_LIST){
        	setPullRefreshEnable(false);
        	setEnableMultiSelection(false);
        	disableLeftAndRightSlide();
        }
        //getAnimatedAdapter().notifyDataSetChanged();
    }
    
    public ConversationListFooterView getFooterView() {
    	if(mActivity != null && mActivity.getConversationListFragment() != null) {
    		return mActivity.getConversationListFragment().getFooterView();
    	}
    	return null;
    }
    //Aurora <SQF> <2014-10-24>  for NEW_UI end
}

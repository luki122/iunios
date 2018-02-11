package com.android.systemui.recent;

import com.android.systemui.recent.AuroraPageNormal.onViewDeletedCallback;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import com.android.systemui.recent.RecentsPanelView.TaskDescriptionAdapter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.LayoutInflater;
import com.android.systemui.R;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import android.util.Log;

import android.content.Intent;

public class AuroraRecentPage extends AuroraPagedView
	implements RecentsPanelView.RecentsScrollView{
	private TaskDescriptionAdapter mAdapter;
	private RecentsCallback mCallback;
	private Context mCon;
	private HashSet<View> mRecycledViews;
	private static final int DEFAULT_PAGE_INDEX = 1;
	
	private onViewDeletedCallback mOnViewDeletedCallback = new onViewDeletedCallback(){
		@Override
		public void onViewDeleted(View v){		
			mCallback.handleSwipe(v);
		}
	};

	public AuroraRecentPage(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mCon = context;
		mRecycledViews = new HashSet<View>();

	}

	@Override
	public void syncPages() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void syncPageItems(int page, boolean immediate) {
		// TODO Auto-generated method stub
		
	}
	
	public void removeUnlockedViews(){
		super.removeUnlockedViews();
	}

	public int numItemsInOneScreenful() {
		return PAGE_VIEW_NUM;
	}
	
    public void setCallback(RecentsCallback callback) {
        mCallback = callback;
    }

	public void setAdapter(TaskDescriptionAdapter adapter) {
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			public void onChanged() {
					update();
			}

			public void onInvalidated() {
					update();
			}
		});

	}

    public void setMinSwipeAlpha(float minAlpha) {

    }

	public View findViewForTask(int persistentTaskId) {
        for (int i = 1; i < getChildCount(); i++) {
            ViewGroup page = (ViewGroup)getChildAt(i);
			for (int j = 0; j < page.getChildCount(); j++) {
				View v = page.getChildAt(j);
	            RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) v.getTag();
	            if (holder.taskDescription.persistentTaskId == persistentTaskId) {
	                return v;
	            }
			}
        }
        return null;

	}

    private void addToRecycledViews(View v) {
        if (mRecycledViews.size() < PAGE_VIEW_NUM) {
            mRecycledViews.add(v);
        }
    }

    // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
    /**
     * Remove all children views.
     */
    public void removeChildPages() {
        Log.d("felix", "AuroraRecentPage.DEBUG removeChildPages() getChildCount() = " + getChildCount());
        ArrayList<View> childList = new ArrayList<View>();
        for (int i = 1; i < getChildCount(); i++) {
            childList.add(getChildAt(i));
        }
        for (View child : childList) {
            removeView(child);
        }
    }
    // Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
	
    public void setStatusBarBG(boolean isTransparent) {
        if (!isTransparent) {
            Intent StatusBarBGIntent = new Intent();
            StatusBarBGIntent.setAction("aurora.action.CHANGE_STATUSBAR_BG");
            StatusBarBGIntent.putExtra("transparent", false);
            mCon.sendBroadcast(StatusBarBGIntent);
        }
    }

	private void update() {
        Log.d("felix", "AuroraRecentPage.DEBUG update()");
		final int N = mAdapter.getCount()  - 1;

		if(N < 0){
			return;
		}


        ViewGroup pageOne = (ViewGroup)getChildAt(1);
        // Aurora <Felix.Duan> <2014-4-9> <BEGIN> Optimize pull up animation laggy
        removeChildPages();
		// Aurora <Felix.Duan> <2014-4-9> <END> Optimize pull up animation laggy
		//Iterator<View> recycledViews = mRecycledViews.iterator();
		
		AuroraPageNormal page = null;
		
		
		int index = N;
		for (; index >= 0 ; --index) {

			if(0 == (N - index) % PAGE_VIEW_NUM){
				page = (AuroraPageNormal)LayoutInflater.from(mCon).inflate(R.layout.aurora_recent_pageview, null);	
				page.setViewDeletedCallback(mOnViewDeletedCallback);
				addView(page);
			}

            View old = null;
			final View view = mAdapter.getView(index, old, this);
            // We don't want a click sound when we dimiss recents
            view.setSoundEffectsEnabled(false);
			RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) view.getTag();
			final View iconView = holder.iconView;
			
            OnClickListener launchAppListener = new OnClickListener() {
                public void onClick(View v) {
                    mCallback.handleOnClick(view);
                    setStatusBarBG(false); 
                }
            };
            iconView.setClickable(true);
            iconView.setOnClickListener(launchAppListener);

			
			page.addView(view);	
		}
		setDataIsReady();
		invalidatePageData(DEFAULT_PAGE_INDEX);
		snapToPage(DEFAULT_PAGE_INDEX);
	}
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation begin
	public void deleteRecentsTast(View v){
		mCallback.handleSwipe(v);
	}
	//Aurora <tongyh> <2013-12-13> add recents rubbish animation end
}

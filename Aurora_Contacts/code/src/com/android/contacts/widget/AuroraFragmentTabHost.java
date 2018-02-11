package com.android.contacts.widget;

import java.util.ArrayList;
import java.util.List;

import com.android.contacts.ContactsApplication;

import android.R;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import com.android.contacts.activities.*;

public class AuroraFragmentTabHost extends TabHost {
	private TabWidget mTabWidget;
    private FrameLayout mTabContent;
    private OnKeyListener mTabKeyListener;
    private List<TabFragmentSpec> mTabSpecs = new ArrayList<TabFragmentSpec>(2);
    private int mCurrentTab = -1;
    private OnFragmentTabChangeListener mOnTabChangeListener;

	public AuroraFragmentTabHost(Context context) {
		super(context);
	}

	public AuroraFragmentTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void onFinishInflate() {
		super.onFinishInflate();
        setup();
        mTabWidget = getTabWidget();
        mTabContent = getTabContentView();
	}

	public void setCurrentTab(int index) {
        if (index < 0 || index >= mTabSpecs.size()) {
            return;
        }

        if (index == mCurrentTab) {
            return;
        }
        mCurrentTab = index;
        TabFragmentSpec spec = mTabSpecs.get(index);

        // Call the tab widget's focusCurrentTab(), instead of just
        // selecting the tab.
        mTabWidget.focusCurrentTab(mCurrentTab);


//        FragmentManager fm = ((Activity)getContext()).getFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.replace(android.R.id.tabcontent, spec.mFragment);
//        ft.commitAllowingStateLoss();
        mTabContent.removeAllViews();
        if(spec.mContentView == null && mTabHostAdapter != null){
        	spec.mContentView = mTabHostAdapter.getView(index);
        }
        mTabContent.addView(spec.mContentView);

        if (!mTabWidget.hasFocus()) {
            // if the tab widget didn't take focus (likely because we're in touch mode)
            // give the current tab content view a shot
        	mTabContent.requestFocus();
        }

        //mTabContent.requestFocus(View.FOCUS_FORWARD);
        invokeOnTabChangeListener();
	}

//	public TabWidget getTabWidget() {
//		return mTabWidget;
//	}

    private void invokeOnTabChangeListener() {
        if (mOnTabChangeListener != null) {
            mOnTabChangeListener.onTabChanged(getCurrentTabTag());
        }
    }

    public String getCurrentTabTag() {
        if (mCurrentTab >= 0 && mCurrentTab < mTabSpecs.size()) {
            return mTabSpecs.get(mCurrentTab).getTag();
        }
        return null;
    }

	public int getCurrentTab() {
		return mCurrentTab;
	}

	public View getCurrentView() {
		return mTabContent;
	}

	public static class TabFragmentSpec {
		private String mTag;
		private View mContentView;
		private View mView;

		public TabFragmentSpec(String tag) {
			mTag = tag;
		}
		
		public String getTag(){
			return mTag;
		}

		public TabFragmentSpec setContent(View view) {
			mContentView = view;
			return this;
		}

		public TabFragmentSpec setIndicator(View view) {
			mView = view;
			return this;
		}
	}

    /**
     * Register a callback to be invoked when the selected state of any of the items
     * in this list changes
     * @param l
     * The callback that will run
     */
    public void setOnTabChangedListener(OnFragmentTabChangeListener l) {
        mOnTabChangeListener = l;
    }

    /**
     * Interface definition for a callback to be invoked when tab changed
     */
    public static interface OnFragmentTabChangeListener {
        void onTabChanged(String tabId);
    }

	public static TabFragmentSpec newFragmentTabSpec(String name) {
		return new TabFragmentSpec(name);
	}
	
	public void clearAllTabs() {
		mTabWidget.removeAllViews();
		mTabContent.removeAllViews();
		mTabSpecs.clear();
		requestLayout();
		invalidate();
	}

	public void addTab(TabFragmentSpec tabSpec) {
		View tabIndicator = tabSpec.mView;
		if(tabIndicator == null){
			throw new IllegalArgumentException(
					"you must specify a way to create the tab indicator.");
		}
		tabIndicator.setOnKeyListener(mTabKeyListener);

		// If this is a custom view, then do not draw the bottom strips for
		// the tab indicators.
		mTabWidget.setStripEnabled(false);

		mTabWidget.addView(tabIndicator);
		mTabSpecs.add(tabSpec);

//		if (mCurrentTab == -1) {
//			setCurrentTab(0);
//		}
	}
	
	public interface TabHostAdapter{
		public View getView(int tab);
	}
	private TabHostAdapter mTabHostAdapter;
	public void setTabHostAdapter(TabHostAdapter adapter){
		mTabHostAdapter = adapter;
	}
}

/**
 * Vulcan created this file in 2014年12月30日 下午4:00:28 .
 */
package com.android.phase1.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import aurora.app.AuroraAlertDialog;

import com.android.browser.BrowserSettings;
import com.android.browser.NavScreen;
import com.android.browser.NavTabView;
import com.android.browser.PhoneUi;
import com.android.browser.R;
import com.android.browser.Tab;
import com.android.browser.TabControl;
import com.android.browser.UiController;
import com.android.phase1.view.stackedscroller.StackView;
import com.android.phase1.view.stackedscroller.StackViewLayout;

/**
 * Vulcan created StackedNavScreen in 2014年12月30日 .
 * 
 */
public class StackedNavScreen extends NavScreen {

	/**
	 * @param activity
	 * @param ctl
	 * @param ui
	 */
	public StackedNavScreen(Activity activity, UiController ctl, PhoneUi ui) {
		super(activity, ctl, ui, true);
		init();
	}

	private void init() {
		LayoutInflater.from(mContext)
				.inflate(R.layout.stacked_nav_screen, this);
		setContentDescription(mContext.getResources().getString(
				R.string.accessibility_transition_navscreen));

		mListTabView = createTabViewList(mActivity);

		mStackedScroller = (StackViewLayout) findViewById(R.id.stacked_scroller);
		mStackedScroller.setContentViewList(mListTabView);
		mStackedScroller.setCallbacks(new StackViewLayout.StackViewLayoutCallbacks() {
			@Override public void onAllStackViewDismissed() {}
			@Override public void onStackViewClicked(StackViewLayout svl, View v) {}
			@Override public void onStackViewTransformed(StackView sv, View contentView) { }

			@Override
			public void onStackViewDismissed(View v) {
				if(mUiController.getTabControl().getTabCount() == 1) {
					StackedNavScreen.this.openNewTab();
				}
				Log.d("vbrowser","onStackViewDismissed is called");
				onCloseTab((Tab)v.getTag());
				
				mWebView2Tab.remove(((AuroraNavTabView)v).getImageView());
				mTabViews.remove((Tab)v.getTag());
				
				refreshBtnNewTab();
				
				setFocusable(true);
				setFocusableInTouchMode(true);
				requestFocus();
			}

		});
		
		mCloseAllTab = (ImageButton) findViewById(R.id.close_all_tab);
		mCloseAllTab.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				openDialogAskIfCloseAllTab(mContext);
				
			}
		});
		
		
		mNewTab = (ImageButton) findViewById(R.id.new_tab);
		mNewTab.setOnClickListener(mNewTabClickListener);

		/*
		 * mBookmarks = (ImageButton) findViewById(R.id.bookmarks); mNewTab =
		 * (ImageButton) findViewById(R.id.newtab); mMore = (ImageButton)
		 * findViewById(R.id.more); mBookmarks.setOnClickListener(this);
		 * mNewTab.setOnClickListener(this); mMore.setOnClickListener(this);
		 * mScroller = (NavTabScroller) findViewById(R.id.scroller); TabControl
		 * tc = mUiController.getTabControl(); mTabViews = new HashMap<Tab,
		 * View>(tc.getTabCount()); mAdapter = new TabAdapter(mContext, tc);
		 * mScroller.setOrientation(mOrientation ==
		 * Configuration.ORIENTATION_LANDSCAPE ? LinearLayout.HORIZONTAL :
		 * LinearLayout.VERTICAL); // update state for active tab
		 * mScroller.setAdapter(mAdapter,
		 * mUiController.getTabControl().getTabPosition(mUi.getActiveTab()));
		 * mScroller.setOnRemoveListener(new OnRemoveListener() { public void
		 * onRemovePosition(int pos) { Tab tab = mAdapter.getItem(pos);
		 * onCloseTab(tab); } }); mNeedsMenu =
		 * !ViewConfiguration.get(getContext()).hasPermanentMenuKey(); if
		 * (!mNeedsMenu) { mMore.setVisibility(View.GONE); }
		 */
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年2月4日 下午3:53:33 .
	 */
	private void refreshBtnNewTab() {
		boolean canCreateNewTab = canCreateNewTab();
		if(!canCreateNewTab) {
			mNewTab.setEnabled(false);
			mNewTab.setOnClickListener(mNewTabClickNullListener);
		}
		else {
			mNewTab.setEnabled(true);
			mNewTab.setOnClickListener(mNewTabClickListener);
		}
	}

	
	@Override
    public void refreshAdapter() {

/*		mStackedScroller.handleDataChanged(
                mUiController.getTabControl().getTabPosition(mUi.getActiveTab()));*/
		mListTabView = createTabViewList(mActivity);
		mStackedScroller.setContentViewList(mListTabView);
		
		refreshBtnNewTab();
		
		
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		
		
		return;

    }
	
    @Override
    public void onThumbnailUpdated(Tab t) {
        View v = mTabViews.get(t);
        if (v != null) {
            v.invalidate();
        }
    }
    
    @Override
	public NavTabView getTabView(int pos) {
        return (NavTabView)mListTabView.get(pos);
    }
    
    /* (non-Javadoc)
	 * @see com.android.browser.NavScreen#getTabView(com.android.browser.Tab)
	 */
	@Override
	protected View getTabViewImage(Tab t) {
		return mTabViews.get(t);
	}

	@Override
    public void finishScroller() {
    	Log.d("vbrowser","finishScroller. try to finish scroller");
    	mStackedScroller.finishScroller();
    	return;
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午3:39:14 .
     * @return
     */
    protected int getScrollerX() {
    	return mStackedScroller.getScrollerX();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年1月31日 下午3:40:15 .
     * @return
     */
    protected int getScrollerY() {
    	return mStackedScroller.getScrollerY();
    }
    
    @Override
    protected void openNewTab() {
        // need to call openTab explicitely with setactive false
        final Tab tab = mUiController.openTab(BrowserSettings.getInstance().getHomePage(),
                false, false, false);
        if (tab != null) {
            mUiController.setBlockEvents(true);
            //final int tix = mUi.mTabControl.getTabPosition(tab);
/*            final int tix = mUiController.getTabControl().getTabPosition(tab);
            mScroller.setOnLayoutListener(new OnLayoutListener() {

                @Override
                public void onLayout(int l, int t, int r, int b) {
                    mUi.hideNavScreen(tix, true);
                    switchToTab(tab);
                }
            });
            mScroller.handleDataChanged(tix);*/
            final int tix = mUiController.getTabControl().getTabPosition(tab);
            mListTabView = createTabViewList(mActivity);
            //mStackedScroller.setContentViewList(mListTabView);
            mStackedScroller.addView2List(mListTabView.get(mListTabView.size() - 1), 200);
            ((com.android.phase1.AuroraPhoneUi)mUi).hideNavScreen(tix, true, true);
          
            switchToTab(tab);
            mUiController.setBlockEvents(false);
        }
    }
    
    private int getViewCenter(boolean isHorizontal, View v) {
        if (isHorizontal) {
            return v.getLeft() + v.getWidth() / 2;
        } else {
            return v.getTop() + v.getHeight() / 2;
        }
    }

    private int getScreenCenter(boolean isHorizontal) {
        if (isHorizontal) {
            return getScrollX() + getWidth() / 2;
        } else {
            return getScrollY() + getHeight() / 2;
        }
    }
    
    private float getAlpha(boolean isHorizontal, View v, float distance) {
        return 1 - (float) Math.abs(distance) / (isHorizontal ? v.getHeight() : v.getWidth());
    }
    
    private AnimatorSet mAnimator = null;
    public void animateOut(View v) {
        if (v == null) return;
        
        float flingVelocity = getContext().getResources().getDisplayMetrics().density
                * 1500;
        animateOut(v, -flingVelocity);
    }

    private void animateOut(final View v, float velocity) {
        float start = v.getTranslationX();
        animateOut(v, velocity, start);
    }

    private void animateOut(final View v, float velocity, float start) {
    	
    	final boolean isHorizontal = false;
    	
        if ((v == null) || (mAnimator != null)) return;
        //final int position = mContentView.indexOfChild(v);
        final int position = mStackedScroller.getChildIndex(v);
        int target = 0;
        if (velocity < 0) {
            target = isHorizontal ? -getHeight() :  -getWidth();
        } else {
            target = isHorizontal ? getHeight() : getWidth();
        }
        int distance = target - (isHorizontal ? v.getTop() : v.getLeft());
        long duration = (long) (Math.abs(distance) * 1000 / Math.abs(velocity));
        int scroll = 0;
        int translate = 0;
        int gap = isHorizontal ? v.getWidth() : v.getHeight();
        int centerView = getViewCenter(isHorizontal, v);
        int centerScreen = getScreenCenter(isHorizontal);
        //int newpos = -1;//INVALID_POSITION;
        if (centerView < centerScreen - gap / 2) {
            // top view
            scroll = - (centerScreen - centerView - gap);
            translate = (position > 0) ? gap : 0;
            //newpos = position;
        } else if (centerView > centerScreen + gap / 2) {
            // bottom view
            scroll = - (centerScreen + gap - centerView);
            //if (position < mAdapter.getCount() - 1) {
            if (position < mStackedScroller.getChildCount() - 1) {
                translate = -gap;
            }
        } else {
            // center view
            scroll = - (centerScreen - centerView);
            //if (position < mAdapter.getCount() - 1) {
            if (position < mStackedScroller.getChildCount() - 1) {
                translate = -gap;
            } else {
                scroll -= gap;
            }
        }
        //mGapPosition = position;
        //final int pos = newpos;
        ObjectAnimator trans = ObjectAnimator.ofFloat(v,
                (isHorizontal ? TRANSLATION_Y : TRANSLATION_X), start, target);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(v, ALPHA, getAlpha(isHorizontal, v,start),
                getAlpha(isHorizontal, v,target));
        AnimatorSet set1 = new AnimatorSet();
        set1.playTogether(trans, alpha);
        set1.setDuration(duration);
        mAnimator = new AnimatorSet();
        ObjectAnimator trans2 = null;
        ObjectAnimator scroll1 = null;
        if (scroll != 0) {
            if (isHorizontal) {
                scroll1 = ObjectAnimator.ofInt(this, "scrollX", getScrollX(), getScrollX() + scroll);
            } else {
                scroll1 = ObjectAnimator.ofInt(this, "scrollY", getScrollY(), getScrollY() + scroll);
            }
        }
        if (translate != 0) {
            trans2 = ObjectAnimator.ofInt(this, "gap", 0, translate);
        }
        final int duration2 = 200;
        if (scroll1 != null) {
            if (trans2 != null) {
                AnimatorSet set2 = new AnimatorSet();
                set2.playTogether(scroll1, trans2);
                set2.setDuration(duration2);
                mAnimator.playSequentially(set1, set2);
            } else {
                scroll1.setDuration(duration2);
                mAnimator.playSequentially(set1, scroll1);
            }
        } else {
            if (trans2 != null) {
                trans2.setDuration(duration2);
                mAnimator.playSequentially(set1, trans2);
            }
        }
        mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator a) {
            	mAnimator = null;
            	//handleDataChanged(pos);
/*                if (mRemoveListener !=  null) {
                    mRemoveListener.onRemovePosition(position);
                    mAnimator = null;
                    mGapPosition = INVALID_POSITION;
                    mGap = 0;
                    handleDataChanged(pos);
                }*/
            }
        });
        mAnimator.start();
    }
    
    /**
     * 
     * Vulcan created this method in 2015年2月2日 下午6:26:41 .
     */
    protected void openDialogAskIfCloseAllTab(final Context context) {
		new AuroraAlertDialog.Builder(context)
		.setTitle(R.string.ask_if_close_all_tab)
		.setPositiveButton(R.string.close_all, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mListTabView = createTabViewList(mActivity);
				
				if(canCreateNewTab()) {
					StackedNavScreen.this.openNewTab();
					for(View v: mListTabView) {
						if((Tab)v.getTag() != mUi.getActiveTab()) {
							onCloseTab((Tab)v.getTag());
						}
					}
				}
				else {
					View v;
					for(int nn = 1;nn < mListTabView.size();nn ++) {
						v = mListTabView.get(nn);
						onCloseTab((Tab)v.getTag());
					}
					StackedNavScreen.this.openNewTab();
					v = mListTabView.get(0);
					onCloseTab((Tab)v.getTag());
				}
				
			}
		})
		.setNegativeButton(android.R.string.cancel, null)
		.show();
    	return;
    }
	
	/**
	 * 
	 * Vulcan created this method in 2015年1月31日 下午1:52:10 .
	 * @return
	 */
	private List<View> createTabViewList(Context context) {
		List<View> listTabs = new ArrayList<View>();

		TabControl tabCtr = mUiController.getTabControl();
		List<Tab> tabs = tabCtr.getTabs();
		
		mWebView2Tab.clear();
		mTabViews = new HashMap<Tab, View>(tabCtr.getTabCount());
		for(Tab t: tabs) {
			final AuroraNavTabView tabView = new AuroraNavTabView(context);
			//int position = tabCtr.getTabPosition(t);
			tabView.setWebView(t);
			tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                	if(tabView.isWebView(v)) {
                		Tab clickedTab = mWebView2Tab.get(v);
                		int newPosition = mUiController.getTabControl().getTabPosition(clickedTab);
                		close(newPosition);
                	}
                	else if(tabView.isClose(v)) {
                		mStackedScroller.dismissChild(tabView);
                	}
                }
            });
			
			mTabViews.put(t, tabView.getImageView());
			mWebView2Tab.put(tabView.getImageView(), t);
			
			tabView.setTitleIcon(t.getFavicon());
			
			tabView.setTag(t);
			listTabs.add(tabView);
		}

		
/*        final NavTabView tabview = new NavTabView(mActivity);
        final Tab tab = getItem(position);
        tabview.setWebView(tab);
        mTabViews.put(tab, tabview.mImage);
        tabview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tabview.isClose(v)) {
                    mScroller.animateOut(tabview);
                } else if (tabview.isTitle(v)) {
                    switchToTab(tab);
                    mUi.getTitleBar().setSkipTitleBarAnimations(true);
                    close(position, false);
                    mUi.editUrl(false, true);
                    mUi.getTitleBar().setSkipTitleBarAnimations(false);
                } else if (tabview.isWebView(v)) {
                    close(position);
                }
            }
        });
        return tabview;*/
		
		
		return listTabs;
	}
	
    @Override
    protected void onConfigurationChanged(Configuration newconfig) {
    	return;
    }
	

	/**
	 * 
	 * Vulcan created this method in 2015年2月4日 下午3:08:19 .
	 * @return
	 */
	protected boolean canCreateNewTab() {
        return mListTabView.size() < MAX_NUM_SHOWING_TAB;
    }
	
	protected ImageButton mCloseAllTab;
	
	private final static int MAX_NUM_SHOWING_TAB = 10;
	private final OnClickListener mNewTabClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			StackedNavScreen.this.openNewTab();
			mNewTab.setEnabled(false);
			mNewTab.setOnClickListener(mNewTabClickNullListener);
		}
	};
	private final OnClickListener mNewTabClickNullListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			return;
		}
	};

	/* (non-Javadoc)
	 * @see android.view.View#setAlpha(float)
	 */
	@Override
	public void setAlpha(float alpha) {
		// TODO Auto-generated method stub
		super.setAlpha(alpha);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月28日 下午4:38:04 .
	 * @return
	 */
	public List<View> getNavTabViews() {
		return mListTabView;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2015年3月30日 上午11:10:24 .
	 * @param v
	 * @return
	 */
	public AuroraTab getTabByNavTabView(View v) {
		return (AuroraTab)v.getTag();
	}

	List<View> mListTabView = null;
	StackViewLayout mStackedScroller;
	
	final HashMap<View, Tab> mWebView2Tab = new HashMap<View,Tab>();
}

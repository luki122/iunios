package com.android.contacts.widget;

import com.android.contacts.ContactsApplication;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TabHost;

import com.android.contacts.R;
import com.android.contacts.activities.*;

public class AuroraTabHost extends TabHost {
	private int is_first_set;

	public AuroraTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		is_first_set = 0;
		// TODO Auto-generated constructor stub

	}

	public AuroraTabHost(Context context) {
		super(context);
		is_first_set = 0;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCurrentTab(int index) {
		// TODO Auto-generated method stub

		if (is_first_set < 3) {
			is_first_set++;
			return;
		}

		if (this.getCurrentView() != null && this.getCurrentTab() != index && Build.VERSION.SDK_INT <= 21) {
			mIndex = index;
			animationBeforeSetTab();
		} else {
			super.setCurrentTab(index);
		}
	}

	public void setCurrentTabNoAnimation(int index) {

		if (is_first_set < 3) {
			is_first_set++;
			return;
		}
		super.setCurrentTab(index);
	}

	public void firstCurrentTab(int index) {
		super.setCurrentTab(index);
	}

	private int mIndex = 0;
	private Handler mHandler = new Handler();

	public void auroraDelaySetCurrentTab(int index) {
		// TODO Auto-generated method stub
		super.setCurrentTab(index);
	}

	private Runnable r = new Runnable() {
		public void run() {
			auroraDelaySetCurrentTab(mIndex);
			animationAfterSetTab();
		}
	};

	public void animationBeforeSetTab() {
		if (mTabChangeAnimation != null) {
			mTabChangeAnimation.animationBeforeSetTab();
			getTabWidget().setEnabled(false);
		}
		int duration = ContactsApplication.getInstance().getResources()
				.getInteger(R.integer.common_anim_duration);
		mHandler.postDelayed(r, duration);
		mHandler.postDelayed(new Runnable() {
			public void run() {
				getTabWidget().setEnabled(true);
			}
		}, 2 * duration);
	}

	public void animationAfterSetTab() {
		if (mTabChangeAnimation != null) {
			mTabChangeAnimation.animationAfterSetTab();
		}
	}


	private TabChangeAnimation mTabChangeAnimation;
	public interface TabChangeAnimation {
		void animationBeforeSetTab();

		void animationAfterSetTab();
	}
	
	public void setTabChangeAnimation(TabChangeAnimation tabChangeAnimation){
		mTabChangeAnimation = tabChangeAnimation;
	}
}

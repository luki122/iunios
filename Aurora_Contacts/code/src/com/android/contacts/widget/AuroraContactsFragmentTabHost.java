package com.android.contacts.widget;

import com.android.contacts.ContactsApplication;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import com.android.contacts.R;
import com.android.contacts.activities.*;

public class AuroraContactsFragmentTabHost extends AuroraFragmentTabHost {
	private int is_first_set;

	public AuroraContactsFragmentTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
		is_first_set = 0;
		// TODO Auto-generated constructor stub

	}

	public AuroraContactsFragmentTabHost(Context context) {
		super(context);
		is_first_set = 0;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setCurrentTab(int index) {
		// TODO Auto-generated method stub

		if (is_first_set < 1) {
			is_first_set++;
			return;
		}

		if (this.getCurrentView() != null && this.getCurrentTab() != index) {
			mIndex = index;
			animationBeforeSetTab();
		} else {
			if(mTabChangeDataSet != null){
				mTabChangeDataSet.changeDataBeforeSetTab(index);
			}
			super.setCurrentTab(index);
			if(mTabChangeDataSet != null){
				mTabChangeDataSet.changeDataAfterSetTab();
			}
		}
	}

	public void setCurrentTabNoAnimation(int index) {

		if (is_first_set < 1) {
			is_first_set++;
			return;
		}
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataBeforeSetTab(index);
		}
		super.setCurrentTab(index);
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataAfterSetTab();
		}
	}

	public void firstCurrentTab(int index) {
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataBeforeSetTab(index);
		}
		super.setCurrentTab(index);
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataAfterSetTab();
		}
	}

	private int mIndex = 0;
	private Handler mHandler = new Handler();

	public void auroraDelaySetCurrentTab(int index) {
		// TODO Auto-generated method stub
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataBeforeSetTab(index);
		}
		super.setCurrentTab(index);
		if(mTabChangeDataSet != null){
			mTabChangeDataSet.changeDataAfterSetTab();
		}
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

	private TabChangeDataSet mTabChangeDataSet;
	public interface TabChangeDataSet{
		void changeDataBeforeSetTab(int newTab);
		void changeDataAfterSetTab();
	}
	
	public void setTabChangeDataSet(TabChangeDataSet tabChangeDataSet){
		mTabChangeDataSet = tabChangeDataSet;
	}
}

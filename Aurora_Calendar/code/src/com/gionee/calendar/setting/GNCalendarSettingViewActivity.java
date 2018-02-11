/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.gionee.calendar.setting;

import android.accounts.Account;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import aurora.widget.AuroraButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import com.android.calendar.R;

import com.android.calendar.CalendarController;
import com.android.calendar.selectcalendars.SelectSyncedCalendarsMultiAccountActivity;
import com.android.calendar.selectcalendars.SelectVisibleCalendarsActivity;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
//Gionee <jiating> <2013-05-06> modify for CR00000000 begin
//Gionee <pengwei><20130807> modify for CR00850530 begin
public class GNCalendarSettingViewActivity extends AuroraActivity {
	private static final int CHECK_ACCOUNTS_DELAY = 3000;
	private Account[] mAccounts;
	private Handler mHandler = new Handler();
	private boolean mHideMenuButtons = false;
	private View customView;
	private RelativeLayout backButton;
	private TextView addAcountText;
	private LinearLayout mCalendarGeneralSetting;
	private TextView actionTitle;
	private View addAcount;
	private LinearLayout mCalendarReminderSetting;
	private LinearLayout mCalendarShowCalendar;
	private LinearLayout mCalendarSynecdCalendar;
	
	//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 begin
	private LinearLayout mGnSettingAbout;
	//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 end
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.CalendarTheme_WithActionBar);
		
		setContentView(R.layout.gn_setting);
		initActionBar();
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mCalendarGeneralSetting = (LinearLayout) findViewById(R.id.gn_setting_general);
		mCalendarReminderSetting = (LinearLayout) findViewById(R.id.gn_setting_reminder);
		mCalendarShowCalendar = (LinearLayout) findViewById(R.id.gn_setting_show_calendar);
		mCalendarSynecdCalendar=(LinearLayout) findViewById(R.id.gn_setting_synecd_calendar);
		
		//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 begin
		mGnSettingAbout =(LinearLayout) findViewById(R.id.gn_setting_about);
				
		mGnSettingAbout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(GNCalendarSettingViewActivity.this,Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_POPUP_DEFAULT_REMINDER_TIME_ABOUT);
				Intent intent = new Intent(GNCalendarSettingViewActivity.this,
						GNCalendarAboutActivity.class);
				startActivity(intent);
			}
		});
		
		//Gionee <Author: lihongyu> <2013-05-07> add for CR000000 end
		
		mCalendarGeneralSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(GNCalendarSettingViewActivity.this,Statistics.SLIDING_VIEW_SETTING_VIEW_SETTING);
				Intent intent = new Intent(GNCalendarSettingViewActivity.this,
						GNCalendarSettingActivity.class);
				intent.putExtra(GNCalendarSettingActivity.SETTING_VIEW_TYPE,
						GNCalendarSettingActivity.SETTING_VIEW_FRAGMENT);
				startActivity(intent);
			}
		});

		mCalendarReminderSetting.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(GNCalendarSettingViewActivity.this,Statistics.SLIDING_VIEW_SETTING_VIEW_REMINDER_SETTINGS);
				Intent intent = new Intent(GNCalendarSettingViewActivity.this,
						GNCalendarSettingActivity.class);
				intent.putExtra(GNCalendarSettingActivity.SETTING_VIEW_TYPE,
						GNCalendarSettingActivity.SETTING_REMINDER_FRAGMENT);
				startActivity(intent);
			}
		});
		mCalendarShowCalendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(GNCalendarSettingViewActivity.this,Statistics.SLIDING_VIEW_SETTING_DISPLAY);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setClass(GNCalendarSettingViewActivity.this,
						SelectVisibleCalendarsActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		});
		
		mCalendarSynecdCalendar.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Statistics.onEvent(GNCalendarSettingViewActivity.this,Statistics.SLIDING_VIEW_SETTING_SYNC);
				Intent intent = new Intent(Intent.ACTION_VIEW);
		        intent.setClass(GNCalendarSettingViewActivity.this, SelectSyncedCalendarsMultiAccountActivity.class);
		        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		        startActivity(intent);
			}
		});

	}

	private void initActionBar() {
		// TODO Auto-generated method stub
//		AuroraActionBar bar = getAuroraActionBar();
//		if (bar != null) {
//			bar.setIcon(R.drawable.gn_event_actionbar_icon);
//			bar.setDisplayHomeAsUpEnabled(false);
//			bar.setDisplayShowTitleEnabled(false);
//			bar.setDisplayShowHomeEnabled(false);
//			bar.setDisplayShowCustomEnabled(true);
//			customView = LayoutInflater.from(bar.getThemedContext()).inflate(
//					R.layout.gn_calendar_setting_activity_action_bar_custom,
//					null);
//			bar.setCustomView(customView, new AuroraActionBar.LayoutParams(
//					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//			backButton = (RelativeLayout) customView
//					.findViewById(R.id.gn_setting_actionbar_back);
//			addAcount =  customView
//					.findViewById(R.id.actionbar_add_acount);
//			addAcountText = (TextView) customView
//					.findViewById(R.id.actionbar_add_acount_text);
//			actionTitle = (TextView) customView
//					.findViewById(R.id.actionbar_title);
//
//			actionTitle.setText(R.string.menu_preferences);
//			addAcountText.setText(R.string.add_account);
//			backButton.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					finish();
//				}
//			});
//
//			addAcount.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Statistics.onEvent(GNCalendarSettingViewActivity.this, Statistics.SLIDING_VIEW_SETTING_ADD_ACOUNT);
//					Intent nextIntent = new Intent(Settings.ACTION_ADD_ACCOUNT);
//					final String[] array = { "com.android.calendar" };
//					nextIntent.putExtra(Settings.EXTRA_AUTHORITIES, array);
//					nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//					startActivity(nextIntent);
//				}
//			});
//		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// /M: To remove its CalendarController instance if exists @{
		CalendarController.removeInstance(this);
		// /@}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Statistics.onResume(this);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Statistics.onPause(this);
	}
	
}
//Gionee <jiating> <2013-05-06> modify for CR00000000 end
//Gionee <pengwei><20130807> modify for CR00850530 end
/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.gionee.calendar.clearevent;

import aurora.widget.AuroraActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.calendar.AbstractCalendarActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.selectcalendars.SelectSyncedCalendarsMultiAccountActivity;
//Gionee <jiating> <2013-05-06> modify for CR00000000 begin
///M:#ClearAllEvents#
public class GNSelectClearableCalendarsActivity extends AbstractCalendarActivity {
    private GNSelectClearableCalendarsFragment mFragment;
    //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar begin
    private TextView actionTitle;
	private View addAcount;
	private View customView;
	private RelativeLayout backButton;
    //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar end
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        setContentView(R.layout.simple_frame_layout);
      //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar begin
        initActionBar();
        //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar end
        mFragment = (GNSelectClearableCalendarsFragment) getFragmentManager().findFragmentById(
                R.id.main_frame);

        if (mFragment == null) {
            mFragment = new GNSelectClearableCalendarsFragment(R.layout.calendar_sync_item);

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_frame, mFragment);
            ft.show(mFragment);
            ft.commit();
        }
    }
    
    //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar begin
    private void initActionBar() {
		// TODO Auto-generated method stub
    	AuroraActionBar bar = getAuroraActionBar();
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
//			addAcount.setVisibility(View.GONE);
//			actionTitle = (TextView) customView
//					.findViewById(R.id.actionbar_title);
//
//			actionTitle.setText(R.string.select_clear_calendars_title);
//			
//			backButton.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					finish();
//				}
//			});
//		}
			
	}
    //Gionee <jiating> <2013-05-06> modify for CR00000000 actionbar end

    // Needs to be in proguard whitelist
    // Specified as listener via android:onClick in a layout xml
    public void handleSelectSyncedCalendarsClicked(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setClass(this, SelectSyncedCalendarsMultiAccountActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getAuroraActionBar()
//                .setDisplayOptions(AuroraActionBar.DISPLAY_HOME_AS_UP, AuroraActionBar.DISPLAY_HOME_AS_UP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            Utils.returnToCalendarHome(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // To remove its CalendarController instance if exists
        CalendarController.removeInstance(this);
    }
}

//Gionee <jiating> <2013-05-06> modify for CR00000000 end

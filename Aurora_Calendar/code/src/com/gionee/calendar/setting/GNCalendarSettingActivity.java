package com.gionee.calendar.setting;

import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.calendar.R;
//Gionee <jiating> <2013-05-06> modify for CR00000000 begin
public class GNCalendarSettingActivity  extends AuroraActivity{
	
	public static final String SETTING_VIEW_TYPE="settingViewType";
	public static final int SETTING_VIEW_FRAGMENT=1;
	public static final int SETTING_REMINDER_FRAGMENT=2;
	
    private View customView;
	private RelativeLayout backButton;

	private TextView  actionTitle;
	private View addAcount;
	private AuroraActionBar mActionBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setTheme(R.style.CalendarSettingTheme_WithActionBar);
    	
//    	setContentView(R.layout.gn_calendar_setting);
		setAuroraContentView(R.layout.gn_calendar_setting,
                AuroraActionBar.Type.Normal);
    	initActionBar();
		Intent intent=getIntent();
		int type=intent.getIntExtra(SETTING_VIEW_TYPE, SETTING_VIEW_FRAGMENT);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
	   setMainPane(ft, R.id.setting_pane, type);
	   ft.commit();
		
	}
	
	private void initActionBar() {
		mActionBar = getAuroraActionBar();//AuroraActionBar aurora.app.AuroraActivity.getAuroraActionBar()
		// TODO Auto-generated method stub
//    	AuroraActionBar bar = getAuroraActionBar();
//		if (bar != null) {
//			bar.setIcon(R.drawable.gn_event_actionbar_icon);
//            bar.setDisplayHomeAsUpEnabled(false);
//            bar.setDisplayShowTitleEnabled(false);
//            bar.setDisplayShowHomeEnabled(false);
//            bar.setDisplayShowCustomEnabled(true);
//    		customView = LayoutInflater.from(bar.getThemedContext())
//    				.inflate(R.layout.gn_calendar_setting_activity_action_bar_custom, null);
//    		 bar.setCustomView(customView, new AuroraActionBar.LayoutParams(
//    				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//    		 backButton=(RelativeLayout)customView.findViewById(R.id.gn_setting_actionbar_back);
//    		 addAcount=customView.findViewById(R.id.actionbar_add_acount);
//    		 addAcount.setVisibility(View.GONE);
//     		actionTitle=(TextView)customView.findViewById(R.id.actionbar_title);
//     		
//     		actionTitle.setText(R.string.preferences_general_title);
//     		
//     		backButton.setOnClickListener(new OnClickListener() {
// 				
// 				@Override
// 				public void onClick(View v) {
// 					// TODO Auto-generated method stub
// 					finish();
// 				}
// 			});
//     		
//	}
    }
	
	private void  setMainPane(FragmentTransaction ft, int viewId, int viewType){
		 FragmentManager fragmentManager = getFragmentManager();
		 Fragment frag = null;
		 if(SETTING_VIEW_FRAGMENT==viewType){
			 frag=new GNCalendarViewSettingFragment();
			 if(mActionBar!=null){
				 mActionBar.setTitle(R.string.preferences_general_title);
			 }
			
		}else if(SETTING_REMINDER_FRAGMENT==viewType){
			frag=new GNCalendarReminderSettingFragment();
			 if(mActionBar!=null){
				 mActionBar.setTitle(R.string.preferences_reminder_title);
			 }
			
		}
		 
		 ft.replace(viewId, frag);
	}

}

//Gionee <jiating> <2013-05-06> modify for CR00000000 end

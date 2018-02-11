package com.mediatek.calendar.selectevent;

import java.util.Calendar;

import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.app.FragmentTransaction;
import aurora.app.AuroraDatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraDatePicker;
import android.widget.RelativeLayout;
import aurora.widget.AuroraSearchView;
import android.widget.TextView;

import com.android.calendar.CalendarController;
import com.android.calendar.R;
import com.android.calendar.Utils;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.gionee.calendar.agenda.GNAgendaActivity;
import com.gionee.calendar.agenda.GNAgendaFragment;
import com.gionee.calendar.view.GNCustomTimeDialog;

import com.mediatek.calendar.extension.IAgendaChoiceForExt;
//Gionee <jiating><2013-05-21> modify for CR00811053 new AgendaView begin
///M:This class is for Choice calendar item 
//Gionee <pengwei><20130809> modify for CR00853574 begin
public class AgendaChoiceActivity extends AuroraActivity implements IAgendaChoiceForExt {
    private static final String KEY_OTHER_APP_RESTORE_TIME = "other_app_request_time";

    private CalendarController mController;
    
    private View customView;
	private RelativeLayout backButton;
	private AuroraButton backToday;
	private RelativeLayout mSelectDate;
	private AuroraSearchView  mCalnedarSearchView;
	private TextView mAgendaActionTitle;
	 private GNCustomTimeDialog  mGNCustomTimeDialog;
	 private GNAgendaFragment frag;
	 
	 private final Runnable mTimeUpdater = new Runnable() {
	        @Override
	        public void run() {
	
	        }
	    };
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // This needs to be created before setContentView
        mController = CalendarController.getInstance(this);
        setContentView(R.layout.agenda_choice);
        
        long timeMillis = -1;
        if (icicle != null) {
            timeMillis = icicle.getLong(KEY_OTHER_APP_RESTORE_TIME);
        } else {
            timeMillis = System.currentTimeMillis();
        }
        AuroraActionBar bar = getAuroraActionBar();
		if (bar != null) {
//			bar.setIcon(R.drawable.gn_event_actionbar_icon);
//            bar.setDisplayHomeAsUpEnabled(false);
//            bar.setDisplayShowTitleEnabled(false);
//            bar.setDisplayShowHomeEnabled(false);
//            bar.setDisplayShowCustomEnabled(true);
//    		customView = LayoutInflater.from(bar.getThemedContext())
//    				.inflate(R.layout.gn_calendar_agenda_activity_action_bar_custom, null);
//    		 bar.setCustomView(customView, new AuroraActionBar.LayoutParams(
//    				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
//    		backButton=(RelativeLayout)customView.findViewById(R.id.gn_event_info_actionbar_back);
    		mSelectDate=(RelativeLayout)findViewById(R.id.gn_calendar_agenda_activity_action_select_date);
    		backToday=(AuroraButton)findViewById(R.id.gn_agenda_activity_actionbar_return_today_view);
    		mAgendaActionTitle=(TextView)findViewById(R.id.actionbar_note_title);
    		mCalnedarSearchView=(AuroraSearchView)findViewById(R.id.gn_calendar_search_event);
    		mCalnedarSearchView.setVisibility(View.GONE);
//    		backButton.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Log.i("jiating","backButton.....");
//					
//			        AgendaChoiceActivity.this.finish();
//				}
//			});
    		
    		backToday.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i("jiating","backToday.....");
					 Time t = null;
			         int viewType = ViewType.CURRENT;
			         long extras = CalendarController.EXTRA_GOTO_TIME;
					 viewType = ViewType.CURRENT;
			         t = new Time( Utils.getTimeZone(AgendaChoiceActivity.this, mTimeUpdater));
			         t.setToNow();
			         extras |= CalendarController.EXTRA_GOTO_TODAY;
//			         mController.sendEvent(this, EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
			         
			         frag.goTo(t);
				}
			});
    		
    		mSelectDate.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Time t = null;
					t = new Time( Utils.getTimeZone(AgendaChoiceActivity.this, mTimeUpdater));
			         t.setToNow();
			         
			        Calendar cal=Calendar.getInstance();
			        cal.setTimeInMillis(t.toMillis(false));
			        Log.i("jiating","onClick.....cal.Year="+cal.YEAR+"cal.MONTH"+"cal.DAY_OF_MONTH="+cal.DAY_OF_MONTH);
					// TODO Auto-generated method stub
					mGNCustomTimeDialog=new GNCustomTimeDialog(AgendaChoiceActivity.this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, new OnDateSetListener() {
						
						@Override
						public void onDateSet(AuroraDatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							// TODO Auto-generated method stub
							
							Time t=null;
							t = new Time( Utils.getTimeZone(AgendaChoiceActivity.this, mTimeUpdater));

							t.year=year;
							t.month= monthOfYear;
							t.monthDay=dayOfMonth;
							t.normalize(true);
							Log.i("jiating","GnAgendaActivity....selectTime..year="+year+"monthOfYear="+monthOfYear+"dayOfMonth="+dayOfMonth+"t="+t.toMillis(false));
							frag.goTo(t);
							
						}
					}, cal.get(Calendar.YEAR),  cal.get(Calendar.MONTH),  cal.get(Calendar.DAY_OF_MONTH));
					mGNCustomTimeDialog.show();
				}
				
				
			});
		}
    		
        setFragments(timeMillis);
    }

    private void setFragments(long timeMillis) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
         frag = new EventSelectionFragment(timeMillis);
        ft.replace(R.id.agenda_choice_frame, frag);
        ft.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_OTHER_APP_RESTORE_TIME, mController.getTime());
    }

    @Override
    public void retSelectedEvent(Intent ret) {
        setResult(AuroraActivity.RESULT_OK, ret);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // To remove its CalendarController instance if exists
        CalendarController.removeInstance(this);
    }
}
///@}
//Gionee <jiating><2013-05-21> modify for CR00811053 new AgendaView end
//Gionee <pengwei><20130809> modify for CR00853574 end
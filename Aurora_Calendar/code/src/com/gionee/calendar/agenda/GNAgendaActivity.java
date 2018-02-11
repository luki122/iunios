package com.gionee.calendar.agenda;

//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
//Gionee <pengwei><20130807> modify for CR00850530 begin
import java.util.Calendar;

import com.android.calendar.R;
import aurora.widget.AuroraActionBar;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraDatePickerDialog.OnDateSetListener;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraButton;
import aurora.widget.AuroraDatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import aurora.widget.AuroraSearchView;
import android.widget.TextView;

import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventHandler;
import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.Utils;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.GNCustomTimeDialog;

//Gionee <pengwei><20130809> modify for CR00853574 begin
public class GNAgendaActivity extends AuroraActivity implements  EventHandler
		 {

	private GNAgendaFragment agendFragment;
	private CalendarController mController;
	private Intent intent = null;
	private long curentTime;
	private Menu mOptionsMenu;
	private MenuItem mSearchMenu;
	private AuroraSearchView mSearchView;
	private boolean mIsInSearchMode = false;
	private String mSearchString = null;
	private View customView;
	private RelativeLayout backButton;
	private AuroraButton backToday;
	private RelativeLayout mSelectDate;
	private AuroraSearchView  mCalnedarSearchView;
	private TextView mAgendaActionTitle;
    private static final int HANDLER_KEY = 0;
    private boolean mUsedForSearch = false;
    private GNCustomTimeDialog  mGNCustomTimeDialog;
    private ContentResolver mContentResolver;
    

	public static final String AGENDAACTIVITYACTION = "com.gionee.calendar.GNAgendaActivity";
	public static final String AGENDAACTIVITYEXTRANAME = "agendaTime";
	
	public static final String AGENDAACTIVITYEXTRASERACH = "agendaSearch";
	
	
	 private final Runnable mTimeUpdater = new Runnable() {
	        @Override
	        public void run() {
	
	        }
	    };
	    
	    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
	        @Override
	        public boolean deliverSelfNotifications() {
	            return true;
	        }

	        @Override
	        public void onChange(boolean selfChange) {
	        	Log.i("jiating","GNDayActivity---onChange---");
	            eventsChanged();
	        }
	    };
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Intent intent = getIntent();

		if (intent != null) {
			curentTime = intent.getLongExtra(AGENDAACTIVITYEXTRANAME,
					System.currentTimeMillis());
			mIsInSearchMode=intent.getBooleanExtra(AGENDAACTIVITYEXTRASERACH, false);
		} else {
			curentTime = System.currentTimeMillis();
			mIsInSearchMode=false;
		}
		Log.v("Calendar","GNAgendaActivity---onCreate---mIsInSearchMode == " + mIsInSearchMode);
//		setContentView(R.layout.simple_frame_layout);
		setAuroraContentView(R.layout.simple_frame_layout, AuroraActionBar.Type.Normal);
		mController = CalendarController.getInstance(this);
//		mController.registerFirstEventHandler(HANDLER_KEY, this);
		// Get the fragment if exists
		agendFragment = (GNAgendaFragment) getFragmentManager().findFragmentById(
				R.id.main_frame);
	    mContentResolver = getContentResolver();
		  mContentResolver.registerContentObserver(CalendarContract.Events.CONTENT_URI,
	                true, mObserver);
		
		// Remove the application title
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
//    		backButton.setOnClickListener(new OnClickListener() {
//				
//				@Override
//				public void onClick(View v) {
//					// TODO Auto-generated method stub
//					Log.i("jiating","backButton.....");
//					InputMethodManager inputMethodManager = (InputMethodManager) GNAgendaActivity.this
//					.getSystemService(Context.INPUT_METHOD_SERVICE);
//			        inputMethodManager.hideSoftInputFromWindow(mCalnedarSearchView.getWindowToken(), 0);
//					GNAgendaActivity.this.finish();
//				}
//			});
    		
    		backToday.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i("jiating","backToday.....");
					Statistics.onEvent(GNAgendaActivity.this, Statistics.SLIDING_VIEW_ALL_ACTIVITY_RETURN_TODAY_CLICK);
					 Time t = null;
			         int viewType = ViewType.CURRENT;
			         long extras = CalendarController.EXTRA_GOTO_TIME;
					 viewType = ViewType.CURRENT;
			         t = new Time( Utils.getTimeZone(GNAgendaActivity.this, mTimeUpdater));
			         t.setToNow();
			         extras |= CalendarController.EXTRA_GOTO_TODAY;
			         mController.sendEvent(this, EventType.GO_TO, t, null, t, -1, viewType, extras, null, null);
			         
//			         agendFragment.goTo(t);
				}
			});
    		
    		mSelectDate.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Statistics.onEvent(GNAgendaActivity.this, Statistics.SLIDING_VIEW_ALL_ACTIVITY_TIME_CHANGE);
					Time t = null;
					t = new Time( Utils.getTimeZone(GNAgendaActivity.this, mTimeUpdater));
			         t.setToNow();
			         
			        Calendar cal=Calendar.getInstance();
			        cal.setTimeInMillis(t.toMillis(false));
			        Log.i("jiating","onClick.....cal.Year="+cal.YEAR+"cal.MONTH"+"cal.DAY_OF_MONTH="+cal.DAY_OF_MONTH);
					// TODO Auto-generated method stub
					mGNCustomTimeDialog=new GNCustomTimeDialog(GNAgendaActivity.this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN, new OnDateSetListener() {
						
						@Override
						public void onDateSet(AuroraDatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							// TODO Auto-generated method stub
							
							Time t=null;
							t = new Time( Utils.getTimeZone(GNAgendaActivity.this, mTimeUpdater));

							t.year=year;
							t.month= monthOfYear;
							t.monthDay=dayOfMonth;
							t.normalize(true);
							Log.i("jiating","GnAgendaActivity....selectTime..year="+year+"monthOfYear="+monthOfYear+"dayOfMonth="+dayOfMonth+"t="+t.toMillis(false));
							agendFragment.goTo(t);
							
						}
					}, cal.get(Calendar.YEAR),  cal.get(Calendar.MONTH),  cal.get(Calendar.DAY_OF_MONTH));
					mGNCustomTimeDialog.show();
				}
				
				
			});
    		
    		
    		mCalnedarSearchView.onActionViewExpanded();
    		mCalnedarSearchView.setIconified(false);
    		InputMethodManager inputMethodManager = (InputMethodManager) GNAgendaActivity.this
			.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(mCalnedarSearchView.getWindowToken(), 0);
    		mCalnedarSearchView.setOnQueryTextListener(new AuroraSearchView.OnQueryTextListener() {
    			@Override
    			public boolean onQueryTextSubmit(String query) {
    				InputMethodManager inputMethodManager = (InputMethodManager) GNAgendaActivity.this
    						.getSystemService(Context.INPUT_METHOD_SERVICE);
    				inputMethodManager.hideSoftInputFromWindow(mCalnedarSearchView.getWindowToken(), 0);
    				agendFragment.isSearching=true;
    				agendFragment.searchEvents(query);
    				
    				return true;
    			}
    			
    			@Override
    			public boolean onQueryTextChange(String newText) {
    				
    				// TODO Auto-generated method stub
    				agendFragment.searchEvents(newText);
    				agendFragment.isSearching=true;
    				return false;
    			}
    		});
    		
		}

		// Create a new fragment if none exists
		if (agendFragment == null) {
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction ft = fragmentManager.beginTransaction();
			agendFragment = new GNAgendaFragment(curentTime, false);
			ft.replace(R.id.main_frame, agendFragment);
			ft.commit();
			mController.registerEventHandler(R.id.main_frame,
					agendFragment);
		}
		
		setSearchMode(mIsInSearchMode);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);

		mOptionsMenu = menu;
//		getMenuInflater().inflate(R.menu.agenda_title_bar,menu);
//
//		mSearchMenu = menu.findItem(R.id.action_search);
//		mSearchView = (SearchView) mSearchMenu.getActionView();
//		if (mSearchView != null) {
//			Utils.setUpSearchView(mSearchView, this);
//			mSearchView.setOnQueryTextListener(this);
//			mSearchView.setOnSuggestionListener(this);
//		}
//
//		/**
//		 * M: If in search mode, enter @{
//		 */
//		if (mIsInSearchMode) {
//			enterSearchMode();
//		}
//		// Note: we should set search string after enterSearchMode(), because
//		// enterSearchMode() will
//		// set it to null
//		if (mSearchView != null) {
//			// restore search string to UI
//			mSearchView.setQuery(mSearchString, false);
//		}

		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
//		   if (item.getItemId() != R.id.action_search) {
//	            exitSearchMode();
//	        } else {
//	            enterSearchMode();
//	        }
		   if (item.getItemId() == android.R.id.home) {
	            finish();
	            return true;
	        }
		   
		   return true;
	}

	@Override
	public long getSupportedEventTypes() {
		// TODO Auto-generated method stub
		 return EventType.GO_TO | EventType.EVENTS_CHANGED | ((mUsedForSearch)?EventType.SEARCH:0);
	}

	@Override
	public void handleEvent(EventInfo event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void eventsChanged() {
		// TODO Auto-generated method stub
		mController.sendEvent(this, EventType.EVENTS_CHANGED, null, null, -1, ViewType.CURRENT);
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Statistics.onPause(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mContentResolver.unregisterContentObserver(mObserver);
	}
	
	  private void setSearchMode(boolean mode) {
	    	
	    		if (true == mode) {
	    			backButton.setVisibility(View.VISIBLE);
	        		mSelectDate.setVisibility(View.GONE);
	        		backToday.setVisibility(View.GONE);
	        		mAgendaActionTitle.setVisibility(View.GONE);
	        		mCalnedarSearchView.setVisibility(View.VISIBLE);
	    	     
	        	} else {
	        		backButton.setVisibility(View.VISIBLE);
	        		mSelectDate.setVisibility(View.VISIBLE);
	        		backToday.setVisibility(View.VISIBLE);
	        		mAgendaActionTitle.setVisibility(View.VISIBLE);
	        		mCalnedarSearchView.setVisibility(View.GONE);
	        	}
	    	
	    }
	  
	 public View getCalendarSearchView(){
		 return mCalnedarSearchView;
	 }

	 
	//Gionee <jiating><2013-06-25> modify for CR00829225 begin 
	 @Override
	 public void onConfigurationChanged(Configuration newConfig) {
	 	// TODO Auto-generated method stub
	 	super.onConfigurationChanged(newConfig);
	 }
	 //Gionee <jiating><2013-06-25> modify for CR00829225 end
	 
	 @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Statistics.onResume(this);
	}
	 
}


//Gionee <jiating> <2013-04-24> modify for CR00000000  end
//Gionee <pengwei><20130807> modify for CR00850530 end
//Gionee <pengwei><20130809> modify for CR00853574 end
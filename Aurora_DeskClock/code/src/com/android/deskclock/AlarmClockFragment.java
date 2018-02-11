package com.android.deskclock;

import java.util.ArrayList;
import java.util.Calendar;

import com.android.deskclock.Alarm;
import com.android.deskclock.Alarms;
import com.android.deskclock.AuroraSetAlarm;
import com.android.deskclock.GnSelectionManager;
import com.android.deskclock.ToastMaster;
import com.aurora.worldtime.UsefulUtils;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraSwitch;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;


public class AlarmClockFragment extends Fragment implements
	OnItemClickListener, OnClickListener{
	
	public static final String NEAREST_ALARM_PREFERENCES = "NearestAlarm";
	private LayoutInflater mFactory;
	private AuroraListView mAlarmsList;
	private Cursor mCursor;
	private AuroraActivity mActivity;
	private Context mContext;
	private AlarmTimeAdapter adapter;
	private View mRootView;
	private TextView noAlarmTextView;
	private LinearLayout noAlarmView;
	private LinearLayout currentTimeText;
	private DigitalClock mcurrenttimeDigitalClock;
	private int itemNub;
	
	private TextView dividerTV;
	
	private class AlarmTimeAdapter extends CursorAdapter {
        
        private ArrayList<Integer> choiceId; // save selected item's ID
        private GnSelectionManager<Integer> mSelectionManager;
        
        //记录下所有闹钟使能的标志，如果某一时刻二者不相等，就不跑bindview了，防止switch来回弹的bug
        private boolean[] alarmEnabled = new boolean[1000];

        public GnSelectionManager<Integer> getmSelectionManager() {
            return mSelectionManager;
        }

        public void setmSelectionManager(GnSelectionManager<Integer> mSelectionManager) {
            this.mSelectionManager = mSelectionManager;
        }

        public ArrayList<Integer> getChoiceId() {
            return choiceId;
        }

        // Gionee baorui 2013-01-05 modify for CR00746150 end

        public AlarmTimeAdapter(Context context, Cursor cursor) {
			super(context, cursor);

            choiceId = new ArrayList<Integer>();
		}
        
        public void initAllAlarmIfEnabled( Cursor cursor ) {
        	Alarm alarm = null;
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        do {
                        	alarm = new Alarm(cursor);
                        	//Log.e("-initAllAlarmIfEnabled alarm.id = ---" + alarm.id);
                        	alarmEnabled[alarm.id] = alarm.enabled;
                        	//Log.e("-initAllAlarmIfEnabled alarmEnabled = ---" + alarmEnabled[alarm.id]);
                        } while (cursor.moveToNext());
                    }
                } catch (Exception e) {
					// TODO: handle exception
				}
            }
        }

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

	        View view = (View) mFactory.inflate(com.aurora.R.layout.aurora_slid_listview, parent, false);
	        

	        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
	        //front.setMinimumHeight(context.getResources().getDimensionPixelSize(R.dimen.list_item_height));
	        //AuroraCheckBox mCheckBox = (AuroraCheckBox) view.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
	        //((LayoutParams)mCheckBox.getLayoutParams()).topMargin = 45;
	        LinearLayout linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);	 
	        linear.setBackgroundResource(R.drawable.list);
	     
			View ret = mFactory.inflate(R.layout.alarm_time, front);
			DigitalClock digitalClock = (DigitalClock) ret.findViewById(R.id.digitalClock);
			digitalClock.setLive(false);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			//Log.e("---bindView---------111111111111111111---------");

			final Alarm alarm = new Alarm(cursor);
			
			//Log.e("-bindView alarm.id = ---" + alarm.id);
			//Log.e("-bindView alarmEnabled = ---" + alarmEnabled[alarm.id]);
			if ( alarmEnabled[alarm.id] != alarm.enabled ) {
				return;
			}
			
			View indicator = view.findViewById(R.id.indicator);
			final LeftTimeTextView timetoalarmText = (LeftTimeTextView) view.findViewById(R.id.timetoalarm);
            timetoalarmText.setLeftTimeAlarm(alarm);
            //ImageView mIVLine = (ImageView) view.findViewById(R.id.iv_line);
            //final CheckBox mCheckSelect = (CheckBox) view.findViewById(R.id.select_alarm);
			
            if (null != mSelectionManager) { // In selection mode!
                //mIVLine.setVisibility(View.GONE);
                indicator.setVisibility(View.GONE);
                //mCheckSelect.setVisibility(View.VISIBLE);
                //mCheckSelect.setChecked(mSelectionManager.isSelected(alarm.id));
            } else {
                //mIVLine.setVisibility(View.VISIBLE);
                indicator.setVisibility(View.VISIBLE);
                //mCheckSelect.setVisibility(View.GONE);

                final AuroraSwitch clockOnOff = (AuroraSwitch) indicator.findViewById(R.id.clock_onoff);
                
                clockOnOff.setOnCheckedChangeListener(null);

                clockOnOff.setChecked(alarm.enabled);
                indicator.setOnTouchListener(new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						// TODO Auto-generated method stub
						switch (event.getAction()) {
						case MotionEvent.ACTION_DOWN:
							if ( mAlarmsList.auroraIsRubbishOut() ) {
	                    		mAlarmsList.auroraSetRubbishBack();
	                    	}
							break;

						default:
							break;
						}
						return false;
					}
				});
                indicator.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        //clockOnOff.toggle();
                    	if ( !mAlarmsList.auroraIsRubbishOut() ) {
                    		alarmEnabled[alarm.id] = !alarm.enabled;
                    		clockOnOff.performClick();
                    	} else {
                    		mAlarmsList.auroraSetRubbishBack();
                    	}
                    }
                });

                clockOnOff.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        // TODO Auto-generated method stub
                        updateAlarm(clockOnOff.isChecked(), clockOnOff, alarm,timetoalarmText.getLeftTime());
                    }
                });

            }
            
			
			DigitalClock digitalClock = (DigitalClock) view
					.findViewById(R.id.digitalClock);

			// set the alarm text
			final Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, alarm.hour);
			c.set(Calendar.MINUTE, alarm.minutes);
			digitalClock.updateTime(c);

			// Set the repeat text or leave it blank if it does not repeat.
			TextView daysOfWeekView = (TextView) view
					.findViewById(R.id.daysOfWeek);

			final String daysOfWeekStr = alarm.daysOfWeek.toString(mActivity,
					false);
            if (daysOfWeekStr != null && daysOfWeekStr.length() != 0) {
            	//法定工作日去掉后面括号的内容
            	if ( alarm.daysOfWeek.getCoded() == 0x9f ) {
            		daysOfWeekView.setText(R.string.officialweekday2);
            	} else {
            		daysOfWeekView.setText(daysOfWeekStr);
            	}
                daysOfWeekView.setVisibility(View.VISIBLE);
            } else {

                daysOfWeekView.setText(R.string.onlyonce);

                daysOfWeekView.setVisibility(View.VISIBLE);
            }
            
            

            /*
			// Display the label
			TextView labelView = (TextView) view.findViewById(R.id.label);
			if (alarm.label != null && alarm.label.length() != 0) {
				labelView.setText(alarm.label);
				labelView.setVisibility(View.VISIBLE);

			} else {
				labelView.setText(null);
				labelView.setHint(R.string.label);
				labelView.setVisibility(View.VISIBLE);
			}
			*/
		}
		
		/*
		 * register receiver
		 */
		public void registerReceiverForTime() {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.addAction(Intent.ACTION_TIME_CHANGED);
			filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
			filter.addAction("alarm_notalarm_timetoalarm");

			mContext.registerReceiver(mIntentReceiver, filter, null, null);
		}

		public void unregisterReceiverForTime() {
			mContext.unregisterReceiver(mIntentReceiver);
		}
		
		/*
		 * receiver broadcast , refresh UI
		 */
		private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				mHandler.sendEmptyMessage(0);
			}
		};
		private Handler mHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				super.handleMessage(msg);
				notifyDataSetChanged();
			};
		};
	};
	
	
	
    private void updateAlarm(boolean enabled, AuroraSwitch clockOnOff ,Alarm alarm,long leftTime) {
        Alarms.enableAlarm(mActivity, alarm.id, enabled);

        if (enabled) {
            AuroraSetAlarm.popAlarmSetToast(mActivity, leftTime);
        }
    }
    
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		alarmClockAuroraListviewOnResume( );

		//暂时不用显示无闹钟的提示了 aurora add by tangjun 2014.2.25
		/*
		if (adapter.getCount() > 0) {
			noAlarmView.setVisibility(View.GONE);
		} else {
			noAlarmView.setVisibility(View.VISIBLE);
		}
		*/
		itemNub=	getActivity().getResources().getInteger(R.integer.item_num);
		adapter.initAllAlarmIfEnabled( mCursor );
		Log.e("-----------AlarmClockFragment onResume---adapter.getCount() = -------" + adapter.getCount());
		if ( adapter.getCount() <= itemNub ) {
			mAlarmsList.getLayoutParams().height = UsefulUtils.dip2px(mContext, 70) * adapter.getCount();
			adapter.notifyDataSetChanged();
		} else {
			mAlarmsList.getLayoutParams().height = UsefulUtils.dip2px(mContext, 70) * itemNub;
			adapter.notifyDataSetChanged();
		}
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		alarmClockAuroraListviewOnPause( );
	}
	
	/**
	 * 必须调用auroraListview添加的函数 aurora add by tangjun 2014.2.18
	 */
	public void alarmClockAuroraListviewOnPause( ) {
		if ( mAlarmsList != null ) {
			mAlarmsList.auroraOnPause();
		}
	}
	
	/**
	 * 必须调用auroraListview添加的函数 aurora add by tangjun 2014.2.18
	 */
	public void alarmClockAuroraListviewOnResume( ) {
		if ( mAlarmsList != null && adapter.getCount() > 0 ) {
			mAlarmsList.auroraOnResume();
		}
	}
	
	@Override
	public void onAttach(Activity activity) {
			Log.e("onAttach");
		super.onAttach(activity);
        mActivity = (AuroraActivity) activity;
        // mContext = mActivity.getApplicationContext();
        mContext = mActivity;
        
        try {
        	mOnAnimationCompleteListener = (onAlarmClockAnimationCompleteListener) activity;
        } catch (ClassCastException e) {
        	throw new ClassCastException(activity.toString() + " must implement onAlarmClockAnimationCompleteListener");
        }
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("onCreate");
		mFactory = LayoutInflater.from(mActivity);
		mCursor = Alarms.getAlarmsCursor(mActivity.getContentResolver());
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * @param position auroraJudgeIfDeleteAlarm
	 */
	private void auroraJudgeIfDeleteAlarm( final int position ) {
        // Confirm that the alarm will be deleted.
        new AuroraAlertDialog.Builder(mActivity, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                .setTitle(getString(R.string.deletealarm))
                .setMessage(getString(R.string.suretodeletealarm))
                // .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.alarm_delete_sure, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                    	auroraDeleteAlarm( position );
                    }
                }).setNegativeButton(R.string.alarm_alert_dismiss_text, null).show();
	}
	
	/**
	 * @param position auroraDeleteAlarm
	 */
	private void auroraDeleteAlarm( int position ) {
        
        mAlarmsList.auroraSetAuroraItemState(0);
        
        final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
        final Alarm alarm = new Alarm(c);
        
        Alarms.deleteAlarm(mContext, alarm.id);
        
        if(alarm!=null&&alarm.id==AlarmReceiver.wakeupAlarmId)
        {
        	AlarmReceiver.is_wakeup_noalarm=false;
        	AlarmReceiver.wakeupAlarmId=-1;
        }
        if (Alarms.mIfDismiss == true && Alarms.mAlarmId == alarm.id) {

            mActivity.stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        }
        
        // aurora mod by tangjun 2013.12.20
        //NotificationOperate.cancelNotification(mContext, alarm.id);

        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = Alarms.getAlarmsCursor(mActivity.getContentResolver());
        adapter.changeCursor(mCursor);
        onResume();
        
        if ( adapter.getCount() < 5 ) {
			Animation anim = AnimationUtils.loadAnimation(mActivity, R.anim.translatedown);
        	currentTimeText.startAnimation(anim);
        	mcurrenttimeDigitalClock.startAnimation(anim);
        }
	}

	private void updateLayout() {
		Log.e("updateLayout()");
		
		currentTimeText = (LinearLayout)mRootView.findViewById(R.id.currenttime);
		mcurrenttimeDigitalClock = (DigitalClock)mRootView.findViewById(R.id.currenttimeclock);

		noAlarmTextView = (TextView) mRootView.findViewById(R.id.no_alarm_text);
		dividerTV=(TextView) mRootView.findViewById(R.id.first_divider);
		mAlarmsList = (AuroraListView) mRootView.findViewById(R.id.alarms_list);
		
		//noAlarmView = (LinearLayout) mRootView.findViewById(R.id.alarm_list_empty);
		adapter = new AlarmTimeAdapter(mActivity, mCursor);
		adapter.registerReceiverForTime();

		mAlarmsList.auroraSetNeedSlideDelete(true);
mAlarmsList.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				 switch (scrollState) {  
	             case OnScrollListener.SCROLL_STATE_IDLE: //  
	            	 dividerTV.setVisibility(View.VISIBLE);
	                
	                 System.out.println("停止...");  
	                 break;  
	             case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:  
	            	 dividerTV.setVisibility(View.INVISIBLE);
               
	                 System.out.println("正在滑动...");  
	                 break;  
	             case OnScrollListener.SCROLL_STATE_FLING:  
	            	 dividerTV.setVisibility(View.INVISIBLE);
	              
	                 System.out.println("开始滚动...");  
	   
	                 break;  
	             }  
				
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				 
				
			}
		});

		mAlarmsList.setAdapter(adapter);
		mAlarmsList.setVerticalScrollBarEnabled(true);
		mAlarmsList.setOnItemClickListener(this);
		mAlarmsList.auroraSetAuroraBackOnClickListener(new AuroraBackOnClickListener() {
			
			@Override
			public void auroraPrepareDraged(int position) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraOnClick(int position) {
				// TODO Auto-generated method stub
				auroraJudgeIfDeleteAlarm(position);
			}
			
			@Override
			public void auroraDragedUnSuccess(int position) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void auroraDragedSuccess(int position) {
				// TODO Auto-generated method stub
				
			}
		});
		
		startAlarmClockFragmentAnim( );

	}
	
    @Override  
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  
            Bundle savedInstanceState) {  
    	mRootView = inflater.inflate(R.layout.alarm_clock,  
                container, false);  
        updateLayout();

        //initGnActionModeHandler();

        //mActivity.registerReceiver(mReceiver, filter);
        
        return mRootView;  
    }  
    
    public void startAlarmClockFragmentAnim( ) {
    	Log.e("--------------startAlarmClockFragmentAnim()---------------"); 
    	
    	mAlarmsList.setVisibility(View.VISIBLE);
    	dividerTV.setVisibility(View.VISIBLE);
    	mAlarmsList.clearAnimation();
    	currentTimeText.clearAnimation();
    	mcurrenttimeDigitalClock.clearAnimation();

    	Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmclocklistview_enter);
    	mAlarmsList.startAnimation(animation);
    	
    	animation = AnimationUtils.loadAnimation(mActivity, R.anim.currenttimetext_enter);
    	currentTimeText.startAnimation(animation);
    	
    	animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmcurrenttime_enter);
    	mcurrenttimeDigitalClock.startAnimation(animation);
    	
//		Animation animation=AnimationUtils.loadAnimation(mActivity, R.anim.mark_list_item_show);
//	    LayoutAnimationController lac=new LayoutAnimationController(animation);
//	    lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
//	    lac.setDelay(0);
//	    listView.setLayoutAnimation(lac);
//	    listView.startLayoutAnimation();
    }
    
    public void startAlarmClockFragmentAnim2( ) {
    	Log.e("--------------startAlarmClockFragmentAnim2()---------------"); 
    	
    	mAlarmsList.setVisibility(View.VISIBLE);
    	mAlarmsList.clearAnimation();
    	currentTimeText.clearAnimation();
    	mcurrenttimeDigitalClock.clearAnimation();

    	//Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmclocklistview_enter);
    	//mAlarmsList.startAnimation(animation);
    	
    	Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.currenttimetext_enter);
    	currentTimeText.startAnimation(animation);
    	
    	animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmcurrenttime_enter);
    	mcurrenttimeDigitalClock.startAnimation(animation);
    	
		Animation animation2 = AnimationUtils.loadAnimation(mActivity, R.anim.alarmclocklistviewitem_enter);
	    LayoutAnimationController lac = new LayoutAnimationController(animation2);
	    lac.setOrder(LayoutAnimationController.ORDER_NORMAL);
	    lac.setDelay(0.2f);
	    mAlarmsList.setLayoutAnimation(lac);
	    mAlarmsList.startLayoutAnimation();
    }
    
    public interface onAlarmClockAnimationCompleteListener {
        void onAlarmClockFragmentAnimationComplete();
    }
    
    private onAlarmClockAnimationCompleteListener mOnAnimationCompleteListener = null;
    
    public boolean endAlarmClockFragmentAnim( ) {
    	Log.e("--------------endAlarmClockFragmentAnim()---------------");
    	
    	/*
    	if ( noAlarmView.getVisibility() == View.VISIBLE ) {
    		return false;
    	}
    	*/
    	
    	mAlarmsList.clearAnimation();
    	currentTimeText.clearAnimation();
    	mcurrenttimeDigitalClock.clearAnimation();
    	
    	Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.currenttimetext_exit);
    	currentTimeText.startAnimation(animation);
    	
    	//animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmclocklistview_exit);
    	//mAlarmsList.startAnimation(animation);
    	
    	Animation animation2 = AnimationUtils.loadAnimation(mActivity, R.anim.alarmclocklistviewitem_exit);
	    LayoutAnimationController lac = new LayoutAnimationController(animation2);
	    lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
	    lac.setDelay(0.2f);
	    mAlarmsList.setLayoutAnimation(lac);
	    mAlarmsList.startLayoutAnimation();
    	
    	animation = AnimationUtils.loadAnimation(mActivity, R.anim.alarmcurrenttime_exit);
    	mcurrenttimeDigitalClock.startAnimation(animation);
    	
    	animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				dividerTV.setVisibility(View.VISIBLE);
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub	
				mAlarmsList.clearAnimation();
//				mAlarmsList.setVisibility(View.INVISIBLE);
				if (mOnAnimationCompleteListener != null) {
					mOnAnimationCompleteListener.onAlarmClockFragmentAnimationComplete();
				}
			}
		});
    	
    	return true;
    }
    
    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        adapter.unregisterReceiverForTime();
        //mActivity.unregisterReceiver(mReceiver);
    }
    
	@Override
	public void onDestroy() {
		super.onDestroy();
		ToastMaster.cancelToast();
		if (mCursor != null) {
			mCursor.close();
		}
	}
	
    public void auroraAddNewAlarm() {
    	startActivity(new Intent(mActivity, AuroraSetAlarm.class));
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
        final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(position);
        final Alarm alarm = new Alarm(c);
        Intent intent = new Intent(mActivity, AuroraSetAlarm.class);
        intent.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        intent.putExtra("position", position);
        startActivity(intent);
	}
	
	public AuroraListView getAlarmClockListView( ) {
		return mAlarmsList;
	}
}

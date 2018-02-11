package com.android.deskclock;

import com.android.db.AlarmAddUpHelp;
import com.android.deskclock.R;
import com.android.deskclock.RepeatListPopupWindow.OnButtonListClickListener;
import com.android.deskclock.RepeatListPopupWindow.OnRepeatListPopupWindowDismissListener;
import com.android.deskclock.RepeatPopupWindow.OnButtonClickListener;
import com.android.deskclock.RepeatPopupWindow.OnRepeatPopupWindowDismissListener;
import com.aurora.utils.Blur;
import com.aurora.utils.GnRingtoneUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.drm.DrmStore.RightsStatus;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.app.AuroraTimePickerDialog;
import aurora.preference.AuroraPreferenceManager;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraTimePicker;
import aurora.widget.AuroraTimePicker.OnTimeChangedListener;

public class AuroraSetAlarm extends AuroraActivity{
	
    private static final String KEY_CURRENT_ALARM = "currentAlarm";
    private static final String KEY_ORIGINAL_ALARM = "originalAlarm";
	
	private RelativeLayout repeatRelative,ringtonrl,remarkrl,rl_delete;
	private TextView selfdefinecycleText,repeatTextView,rington,ringtontext,remark,remarktext;
	private Alarm.DaysOfWeek mSetDaysOfWeek = new Alarm.DaysOfWeek(0);
    private int     mId;
    private int     mHour;
    private int     mMinute;
    private Alarm   mOriginalAlarm;
    
    private AuroraTimePicker auroraTimePicker;
    
    private int position = -1;
    
    private int mOldDay = 0;
    
    private boolean repeatclickselfdefine = false;
    
	
    private String editLabel;
    private Uri selectUri;
    
    private LinearLayout ll_menu;
    
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		setContentView(R.layout.aurorasetalarm);
        
        Intent i = getIntent();
        Alarm alarm = i.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        
        position = i.getIntExtra("position", -1);
        
        if (alarm == null) {
            // No alarm means create a new alarm.
            alarm = new Alarm(this);
            
            //aurora add by tangjun 2013.12.23 start
            alarm.vibrate =  AuroraPreferenceManager.getDefaultSharedPreferences(this).getBoolean("default_vibrate", true);
            //aurora add by tangjun 2013.12.23 end
            Log.e("---alarm.vibrate--- = " + alarm.vibrate);
        }
        mOriginalAlarm = alarm;
        initViews( );
        // Populate the prefs with the original alarm data.  updatePrefs also
        // sets mId so it must be called before checking mId below.
        updatePrefs(mOriginalAlarm);
        
	}
	
	private void initViews( ) {
		
		RelativeLayout aurorasetalarmback = (RelativeLayout)findViewById(R.id.aurorasetalarmback);
	    Blur.showBgBlurView(this, aurorasetalarmback, AlarmClock.lockscreenDefaultPath);
//		if ( AlarmClock.mScreenBitmapMatrix != null ) {
//			aurorasetalarmback.setBackground(new BitmapDrawable(AlarmClock.mScreenBitmapMatrix));
//		} else {
//			aurorasetalarmback.setBackgroundResource(R.drawable.background);
//		}
		ll_menu=(LinearLayout)findViewById(R.id.ll_menu);
		
		repeatRelative = (RelativeLayout)findViewById(R.id.repeatrela);
        repeatRelative.setOnClickListener(rlClickListener);
        repeatRelative.setOnTouchListener(tlTouchListener);
        rl_delete=(RelativeLayout)findViewById(R.id.rl_delete);
        repeatTextView = (TextView)findViewById(R.id.repeattext);
        selfdefinecycleText = (TextView)findViewById(R.id.selfdefinecycle);
        ringtonrl=(RelativeLayout)findViewById(R.id.ringtonrl);
        ringtonrl.setOnClickListener(rlClickListener);
        ringtonrl.setOnTouchListener(tlTouchListener);
        rington=(TextView)findViewById(R.id.rington);
        ringtontext=(TextView)findViewById(R.id.ringtontext);
        String title=GnRingtoneUtil.gnGetRingtoneTile(this, mOriginalAlarm.alert);
        if(title==null||TextUtils.isEmpty(title))
        {
        	mOriginalAlarm.alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        	title=GnRingtoneUtil.gnGetRingtoneTile(this, mOriginalAlarm.alert);
        }
        ringtontext.setText(title);
        remarkrl=(RelativeLayout)findViewById(R.id.remarkrl);
        remarkrl.setOnClickListener(rlClickListener);
        remarkrl.setOnTouchListener(tlTouchListener);
        remark=(TextView)findViewById(R.id.remark);
        remarktext=(TextView)findViewById(R.id.remarktext);
        remarktext.setText(mOriginalAlarm.getRemakrs(this));
        auroraTimePicker = (AuroraTimePicker)findViewById(R.id.timepicker);
        auroraTimePicker.setTextColor( this.getResources().getColor(R.color.sometransparent1), this.getResources().getColor(R.color.gn_white), 
        		this.getResources().getColor(R.color.sometransparent2));
        if ( Alarms.get24HourMode(this) ) {
        	auroraTimePicker.setWidgetGapWidth(111f);
        }
        auroraTimePicker.setWidgetWidth(240f);
        
        ImageView cancelBtn = (ImageView)findViewById(R.id.cancel);
        cancelBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				judgeIfGiveUpSaveTheAlarm( );
			}
        });
        ImageView sureBtn = (ImageView)findViewById(R.id.sure);
        sureBtn.setOnClickListener(new OnClickListener() {

        	@Override
        	public void onClick(View v) {
        		long time=0l;
        		AlarmReceiver.is_wakeup_noalarm=false;
        		AlarmReceiver.wakeupAlarmId=-1;
        		time= saveAlarm(null);
        		if(selectUri!=null)
        		{
        		  Alarms.saveLastSelectRingtonUri(selectUri.toString(), AuroraSetAlarm.this);
        		}
        		popAlarmSetToast(AuroraSetAlarm.this, time);
        		finish();
        	}
        });
        
        //暂时用不到删除，先隐藏吧，后面用的时候在打开
        rl_delete.setVisibility(View.VISIBLE);
        
        rl_delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//		        Intent mIntent = new Intent();
//		        mIntent.putExtra("position", position);
//		        Log.e("------alarmDelete---position = -----" + position);
//		        setResult(RESULT_OK, mIntent);	//该函数在finish()调用后就不起作用了，所以不能放在onDestroy()等方法中
//		        finish();
				deleteAlarm();
			}
		});
        //新增界面不用删除按钮
        if (mOriginalAlarm.id == -1) {
        	rl_delete.setVisibility(View.GONE);
        }
	}
	
	private OnClickListener rlClickListener =new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.repeatrela:
				showRepeatListPopupWindow();
				break;
			case R.id.ringtonrl:
				toRingtonSelect();
				break;
			case R.id.remarkrl:
				showEditLabelDialog();
				break;
			default:
				break;
			}
		}
	};
	
	private AuroraAlertDialog labelDialog;
	private EditText et_input_label;
	private void setEditableDialog(){
		View labelView = LayoutInflater.from(this).inflate(com.android.deskclock.R.layout.edit_label_dialog, null);
		et_input_label = (EditText) labelView.findViewById(R.id.et_input_label);
		et_input_label.setBackgroundResource(com.aurora.R.drawable.aurora_editlayout_background);
		labelDialog = new AuroraAlertDialog.Builder(this,AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
				.setTitle(getString(R.string.set_alarm_remarks))
				.setView(labelView)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								editLabel = et_input_label.getText().toString();
								if (editLabel != null && !editLabel.equals("")) {
									remarktext.setText(editLabel);
								}
								hideKeyBoard();
							}
						})
				.setNegativeButton(android.R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								hideKeyBoard();
							}
						}).create();
	}
	
	private void hideKeyBoard() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE); 
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(et_input_label.getWindowToken(), 0);
		}
		et_input_label.clearFocus();
	}
	private void showEditLabelDialog(){
		if(labelDialog==null)
		{
			setEditableDialog();
		}
		et_input_label.setFocusable(true);
		labelDialog.show();
	}
	
	public static  final int REQUEST_CODE=1;
	
	private Uri getAlarmUri(Uri alertUri){
		if(selectUri!=null)
			return selectUri;
		if (RingtoneManager.isDefault(alertUri)) {

            return RingtoneManager.getActualDefaultRingtoneUri(this,
                    RingtoneManager.TYPE_ALARM);
        }
        return alertUri;
	}
	private void toRingtonSelect(){
		Intent intent = new Intent("gn.com.android.audioprofile.action.RINGTONE_PICKER");
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
    	
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, this.getResources().getString(R.string.default_ringtone_setting_title));
    	intent.putExtra("lockscreenpath", AlarmClock.lockscreenDefaultPath);
    	intent.putExtra("fullscreen", true);
    	intent.putExtra("chronometer", true);
    	if(getAlarmUri(mOriginalAlarm.alert).toString().equals(AlarmClock.VALUE_RINGTONE_SILENT))
    	{
    		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, getAlarmUri(mOriginalAlarm.alert));
    	}else{
    		intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, getAlarmUri(mOriginalAlarm.alert));
    	}
    	
    	intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, getAlarmUri(mOriginalAlarm.alert).toString().equals(AlarmClock.VALUE_RINGTONE_SILENT));
    	startActivityForResult(intent, REQUEST_CODE);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
    	Log.e("---onActivityResult--");
    	if ( requestCode == REQUEST_CODE ) {
    		if ( resultCode == RESULT_OK) {
    			Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
    			ringtontext.setText(GnRingtoneUtil.gnGetRingtoneTile(this, ringtoneUri));
    			selectUri=ringtoneUri;
    		}
    	}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private OnTouchListener tlTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			TextView tempTitle=null,tempContent=null;
			switch (v.getId()) {
			case R.id.repeatrela:
				tempTitle=selfdefinecycleText;
				tempContent=repeatTextView;
				break;
			case R.id.ringtonrl:
				tempTitle=rington;
				tempContent=ringtontext;
				break;
			case R.id.remarkrl:
				tempTitle=remark;
				tempContent=remarktext;
				break;
			}
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_DOWN:
				tempContent.setTextColor(getApplicationContext().getResources().getColor(R.color.repeattextcolorpress));
				tempTitle.setTextColor(getApplicationContext().getResources().getColor(R.color.selfdefinecyclepress));
				break;
			case MotionEvent.ACTION_UP:
				tempContent.setTextColor(getApplicationContext().getResources().getColor(R.color.repeattextcolor));
				tempTitle.setTextColor(getApplicationContext().getResources().getColor(R.color.selfdefinecycle));
				break;
			default:
				break;
			}
			
			return false;
		}
	};
	
    /**
     * @param position PopupWindow按钮响应
     */
    private void buttonClickRespond(int position) {
		Alarm.DaysOfWeek setDaysOfWeek;
		switch (position) {
		case 0:
			setDaysOfWeek = new Alarm.DaysOfWeek(0);
			mSetDaysOfWeek.set(setDaysOfWeek);
			repeatTextView.setText(mSetDaysOfWeek.toString(AuroraSetAlarm.this, true));
			break;
		case 1:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x7f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			repeatTextView.setText(mSetDaysOfWeek.toString(AuroraSetAlarm.this, true));
			break;	
		case 2:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x1f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			repeatTextView.setText(mSetDaysOfWeek.toString(AuroraSetAlarm.this, true));
			break;
		case 3:
			setDaysOfWeek = new Alarm.DaysOfWeek(0x9f);
			mSetDaysOfWeek.set(setDaysOfWeek);
			repeatTextView.setText(mSetDaysOfWeek.toString(AuroraSetAlarm.this, true));
			break;
		case 4:
			repeatclickselfdefine = true;
			//runRepeatDismissAnim();
			final RepeatPopupWindow repeat = new RepeatPopupWindow(AuroraSetAlarm.this, R.layout.repeatcheck);
			repeat.showRepeatPopupWindow();
			repeat.setOnButtonClickListener(new OnButtonClickListener() {
				
				@Override
				public void onSureClick() {
					// TODO Auto-generated method stub
					Alarm.DaysOfWeek setDaysOfWeek = new Alarm.DaysOfWeek(repeat.getSelectDays());
					mSetDaysOfWeek.set(setDaysOfWeek);
					repeatTextView.setText(mSetDaysOfWeek.toString(AuroraSetAlarm.this, true));
				}
				
				@Override
				public void onCancelClick() {
					// TODO Auto-generated method stub
				}
			});
			repeat.setOnRepeatPopupWindowDismissListener(new OnRepeatPopupWindowDismissListener() {
				
				@Override
				public boolean dismiss() {
					// TODO Auto-generated method stub
					repeatclickselfdefine = false;
					runRepeatShowAnim();
					return false;
				}
			});
			break;
		default:
			break;
		}
    }
    
    private void runRepeatDismissAnim ( ) {
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.alarmtitle_exit);
    	ll_menu.startAnimation(anim);
    	rl_delete.startAnimation(anim);
    }
    
    private void runRepeatShowAnim ( ) {
    	Animation anim = AnimationUtils.loadAnimation(this, R.anim.alarmtitle_enter);
    	ll_menu.startAnimation(anim);
    	rl_delete.startAnimation(anim);
    }
    
    /**
     * 显示周期选择列表
     */
    private void showRepeatListPopupWindow( ) {
    	
    	runRepeatDismissAnim( );
		final RepeatListPopupWindow repeat = new RepeatListPopupWindow(AuroraSetAlarm.this, R.layout.repeatlistcheck);
		repeat.showRepeatListPopupWindow();
		repeat.setOnButtonListClickListener(new OnButtonListClickListener() {

			@Override
			public void onButtonListClick(int position) {
				// TODO Auto-generated method stub
				buttonClickRespond(position);
			}
			
		});
		repeat.setOnRepeatListPopupWindowDismissListener(new OnRepeatListPopupWindowDismissListener() {
			
			@Override
			public boolean dismiss() {
				// TODO Auto-generated method stub
				if ( !repeatclickselfdefine ) {
					runRepeatShowAnim();
				}
				return false;
			}
		});
    }
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_ORIGINAL_ALARM, mOriginalAlarm);
        outState.putParcelable(KEY_CURRENT_ALARM, buildAlarmFromUi());
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);

        Alarm alarmFromBundle = state.getParcelable(KEY_ORIGINAL_ALARM);
        if (alarmFromBundle != null) {
            mOriginalAlarm = alarmFromBundle;
        }

        alarmFromBundle = state.getParcelable(KEY_CURRENT_ALARM);
        if (alarmFromBundle != null) {
            updatePrefs(alarmFromBundle);
        }
    }
    
    @Override
    protected void onDestroy() {

    	super.onDestroy();
    }
    
    private boolean isModify(){
    	boolean isSame=mOriginalAlarm.minutes == mMinute && mOriginalAlarm.hour == mHour &&mOriginalAlarm.daysOfWeek.getCoded() == mOldDay
    			 &&GnRingtoneUtil.gnGetRingtoneTile(this, mOriginalAlarm.alert).equals(ringtontext.getText().toString())&&remarktext.getText().toString().equals(mOriginalAlarm.getRemakrs(this));
    	return !isSame;
    }
    
    private void judgeIfGiveUpSaveTheAlarm( ) {
    	mHour = auroraTimePicker.getCurrentHour();
        mMinute = auroraTimePicker.getCurrentMinute();
        Log.e("mOriginalAlarm.daysOfWeek = " + mOriginalAlarm.daysOfWeek.getCoded() + ", mOldDay = " + mOldDay);
        //没做修改就不保存
        if (!isModify()) {
        	revert();
        	finish();
        } else {
        	new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
            .setTitle(getString(R.string.hint))
            .setMessage(getString(R.string.ifgiveupchange))
            .setPositiveButton(R.string.alarm_delete_sure, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                	revert();
                	finish();
                }
            }).setNegativeButton(R.string.alarm_alert_dismiss_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface d, int w) {
                	
                }
            }).show();
            
        }
    }
    @Override
    public void onBackPressed() {
    	judgeIfGiveUpSaveTheAlarm( );
    }
	
    private void updatePrefs(Alarm alarm) {
        mId = alarm.id;

        mHour = alarm.hour;
        mMinute = alarm.minutes;
        mSetDaysOfWeek = alarm.daysOfWeek;
        mOldDay = alarm.daysOfWeek.getCoded();
        
        //aurora add by tangjun
        repeatTextView.setText(alarm.daysOfWeek.toString(this, true));

        if (alarm.alert != null && !RingtoneManager.isDefault(alarm.alert)
                && !GnRingtoneUtil.isRingtoneExist(alarm.alert, getContentResolver())&& !alarm.alert.toString().equals(AlarmClock.VALUE_RINGTONE_SILENT)) {
            // alarm.alert = Settings.System.DEFAULT_ALARM_ALERT_URI;
            String mData = Alarms.getAlertInfoStr(this, alarm.id);
            int mVolumes = Alarms.getVolumes(this, alarm.id);
            if (Alarms.isUpdateRintoneUri(mData, alarm.alert, this, mVolumes)) {
                alarm.alert = Alarms.updateRintoneUri(mData, alarm.alert, this, mVolumes);
            } else {
                alarm.alert = Settings.System.DEFAULT_ALARM_ALERT_URI;
            }

            saveAlarm(alarm);
        }

        auroraTimePicker.setIs24HourView(DateFormat.is24HourFormat(this));
        auroraTimePicker.setCurrentHour(mHour);
        auroraTimePicker.setCurrentMinute(mMinute);
    }
    
    private long saveAlarm(Alarm alarm) {
        if (alarm == null) {
            alarm = buildAlarmFromUi();
        }

        long time;
        if (alarm.id == -1) {
            time = Alarms.addAlarm(this, alarm);
            alarmAddUp();
            // addAlarm populates the alarm with the new id. Update mId so that
            // changes to other preferences update the new alarm.
            mId = alarm.id;
        } else {
            time = Alarms.setAlarm(this, alarm);
        }
        return time;
    }

    private void alarmAddUp(){
    	AlarmAddUpHelp.getInstance(this).add(AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_ADD_ALARM);
    	AlarmAddUpHelp.AlarmAddUpType type=null;
    	switch (mSetDaysOfWeek.getCoded()) {
		case 0:
			type=AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_ONCE;
			break;
		case 0x7f:
			type=AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_EVERYDAY;
			break;
		case 0x1f:
			type=AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_WEEKDAY;
			break;
		case 0x9f:
			type=AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_WORKDAY;
			break;
		default:
			type=AlarmAddUpHelp.AlarmAddUpType.ITEM_TAG_CUSTOM;
			break;
		}
    	AlarmAddUpHelp.getInstance(this).add(type);
    }
    
    private String getRemarks(){
    	if(remarktext.getText().toString().equals(getString(R.string.empty_label)))
    		return "";
    	return remarktext.getText().toString();
    }
    private Alarm buildAlarmFromUi() {
    	mHour = auroraTimePicker.getCurrentHour();
        mMinute = auroraTimePicker.getCurrentMinute();
        
        Alarm alarm = new Alarm();
        alarm.id = mId;
        alarm.enabled = true;
        alarm.hour = mHour;
        alarm.minutes = mMinute;
        alarm.daysOfWeek = mSetDaysOfWeek;
        alarm.alert=getAlarmUri(mOriginalAlarm.alert);
        alarm.label=getRemarks();
        
        return alarm;
    }
    
    private void deleteAlarm() {
    	//android jiating 20120608 change dialog info begin

        new AuroraAlertDialog.Builder(this, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                  .setTitle(getString(R.string.android_delete_alarm))
                   .setMessage(getString(R.string.isDelete_the_alarm))
                   //android jiating 20120608 change dialog info begin
                  .setPositiveButton(android.R.string.ok,
                  
         
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int w) {
                                Alarms.deleteAlarm(AuroraSetAlarm.this, mId);
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void revert() {
        int newId = mId;
        // "Revert" on a newly created alarm should delete it.
        if (mOriginalAlarm.id == -1) {
            Alarms.deleteAlarm(AuroraSetAlarm.this, newId);
        } else {
        	//重新置回原来的周期选择
        	Alarm.DaysOfWeek setDaysOfWeek = new Alarm.DaysOfWeek(mOldDay);
			mSetDaysOfWeek.set(setDaysOfWeek);
			
            saveAlarm(mOriginalAlarm);
        }
    }

    /**
     * Display a toast that tells the user how long until the alarm
     * goes off.  This helps prevent "am/pm" mistakes.
     */
    static void popAlarmSetToast(Context context, int hour, int minute,
                                 Alarm.DaysOfWeek daysOfWeek) {
        popAlarmSetToast(context,
                Alarms.calculateAlarm(hour, minute, daysOfWeek)
                .getTimeInMillis());
    }

    static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    /**
     * format "Alarm set for 2 days 7 hours and 53 minutes from
     * now"
     */
    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" :
                (days == 1) ? context.getString(R.string.day) :
                context.getString(R.string.days, Long.toString(days));

        String minSeq = (minutes == 0) ? "" :
                (minutes == 1) ? context.getString(R.string.minute) :
                context.getString(R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" :
                (hours == 1) ? context.getString(R.string.hour) :
                context.getString(R.string.hours, Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) |
                    (dispHour ? 2 : 0) |
                    (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }
    
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }
}

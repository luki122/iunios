package com.gionee.calendar.view;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;

import android.view.LayoutInflater;
import android.view.View;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraDatePicker.OnDateChangedListener;
import android.widget.TextView;
import aurora.widget.AuroraTimePicker;
import aurora.widget.AuroraTimePicker.OnTimeChangedListener;
import android.widget.Toast;
import com.android.calendar.R;


//Gionee <jiating> <2013-04-24> modify for CR00000000  begin 
public class GNDateTimeDialog extends AuroraAlertDialog implements OnClickListener, OnTimeChangedListener{

	private static final String YEAR = "year";
	private static final String MONTH = "month";
	private static final String DAY = "day";

	private AuroraDatePicker mDatePicker;

	private static final String HOUR = "hour";
	private static final String MINUTE = "minute";
	private static final String IS_24_HOUR = "is24hour";

	private AuroraTimePicker mTimePicker;

	private final OnDateTimeSetListener mDateTimeCallback;
	int mYear;
	int mMonthOfYear;
	int mDayOfMonth;
	int mHourOfDay;
	int mMinute;
	boolean mIs24HourView;
	Calendar mCalendar;
	Date mTextDate;

	private TextView mDateView;
	private TextView mTimeView;

	private Context mContext;
	private long mTime = -1;

	private boolean mDialogShow = false;
	public interface OnDateTimeSetListener {
		/*
		 * view The view associated with this listener.
		 * year The year that was set
		 * monthOfYear The month that was set (0-11) for compatibility
		 * dayOfMonth The day of the month that was set.
		 * hourOfDay The hour that was set.
		 * minute The minute that was set.
		 * */
		void onDateTimeSet(Calendar calendar);
	}

	public GNDateTimeDialog(Context context,
			OnDateTimeSetListener datetimecallBack,
			Calendar calendar) {
		this(context, AuroraAlertDialog.THEME_HOLO_DARK, datetimecallBack, calendar,0);
	}

	/**
	 * @param context The context the dialog is to run in.
	 * @param theme the theme to apply to this dialog
	 * @param callBack How the parent is notified that the date is set.
	 * @param year The initial year of the dialog.
	 * @param monthOfYear The initial month of the dialog.
	 * @param dayOfMonth The initial day of the dialog.
	 */
	public GNDateTimeDialog(Context context,
			int theme,
			OnDateTimeSetListener datetimecallBack,
			Calendar calendar,long minTime) {
		super(context, theme);
		mContext = context;
		mDateTimeCallback = datetimecallBack;
		if (calendar == null) {
			calendar = Calendar.getInstance();
			calendar.setTimeInMillis(calendar.getTimeInMillis() + 10 * 60 * 1000);
		}
		mYear = calendar.get(Calendar.YEAR);
		mMonthOfYear = calendar.get(Calendar.MONTH);
		mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		mMinute = calendar.get(Calendar.MINUTE);
		mIs24HourView = DateFormat.is24HourFormat(context);
		mCalendar = calendar;

		setCanceledOnTouchOutside(false);
		setButton(BUTTON_POSITIVE, getContext().getText(R.string.gn_edit_event_date_time_set), this);
		setButton(BUTTON_NEGATIVE, getContext().getText(R.string.gn_edit_event_date_time_cancel), this);
		setIcon(0);
		setTitle(R.string.gn_edit_event_date_time_dialog_title);
		LayoutInflater inflater =
			(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.gn_date_time_picker_dialog, null);

		initTextView(view,minTime);

		setView(view);
	}

	private void updateDate() {
		mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
		Log.i( "DateTimeDialog------RegularlyDateTimeDialog....updateDate   mCalendar "+mCalendar.getTimeInMillis());
		mTextDate = mCalendar.getTime();
	}

	private void initTextView(View view,long minTime) {
		mTextDate = mCalendar.getTime();
		mDateView = (TextView) view.findViewById(R.id.gn_date_text);
		mDateView.setText(DateFormat.getDateFormat(mContext).format(mTextDate));
		mDateView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDatePicker.setVisibility(View.VISIBLE);
				mTimePicker.setVisibility(View.GONE);
				mDateView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_select_bg));
				mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_light_bg));
			}
		});
		mTimeView = (TextView) view.findViewById(R.id.gn_time_text);
		mTimeView.setText(DateFormat.getTimeFormat(mContext).format(mTextDate));
		mTimeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mDatePicker.setVisibility(View.GONE);
				mTimePicker.setVisibility(View.VISIBLE);
				mDateView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_light_bg));
				mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_select_bg));
			}
		});

		mDatePicker = (AuroraDatePicker) view.findViewById(R.id.gn_datePicker);
		mDatePicker.init(mYear, mMonthOfYear, mDayOfMonth, new DateChangedListener());

		mCalendar.clear();
		// gn lilg 2012-11-08 modify for CR00727797 start
//		mCalendar.setTimeInMillis(System.currentTimeMillis() - 10 * 60 * 1000);
//		mCalendar.set(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
//		mDatePicker.setMinDate(mCalendar.getTimeInMillis());
		// gn lilg 2012-11-08 modify for CR00727797 start


		//gionee gaoj 2012-8-21 added for CR00678516 start
		Time maxTime=new Time();
		maxTime.set(59,59,23,31,11,2036);//2037/12/31
		long maxDate=maxTime.toMillis(false);
		Calendar cal=Calendar.getInstance();
		cal.setTimeInMillis(maxDate);
		Log.i("jiating..year="+cal.get(Calendar.YEAR)+"month="+cal.get(Calendar.MONTH)+"day="+cal.get(Calendar.DAY_OF_MONTH));
//		maxDate=maxDate+999;//in millsec
//		mDatePicker.setMaxDate(maxDate);
		//gionee gaoj 2012-8-21 added for CR00678516 end

		mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
		mTimePicker = (AuroraTimePicker) view.findViewById(R.id.gn_timePicker);
		mTimePicker.setIs24HourView(mIs24HourView);
		mTimePicker.setCurrentHour(mHourOfDay);
		mTimePicker.setCurrentMinute(mMinute);
		mTimePicker.setOnTimeChangedListener(this);


	}

	private class DateChangedListener implements OnDateChangedListener {
		@Override
		public void onDateChanged(AuroraDatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			UnShowDialog();
			mYear = year;
			mMonthOfYear = monthOfYear;
			mDayOfMonth = dayOfMonth;
			updateDate();
			mDateView.setText(DateFormat.getDateFormat(mContext).format(mTextDate));
		}
	}

	@Override
	public void onTimeChanged(AuroraTimePicker view, int hourOfDay, int minute) {
		Log.i("jiating.....onTimeChanged..........hourOfDay="+hourOfDay+"minute="+minute);
			//Gionee <jiating><2013-07-01> modify for CR00829431 begin
//		if (!isTrueTime(hourOfDay, minute)) {
//			Calendar calendar = Calendar.getInstance();
//			calendar.setTimeInMillis(calendar.getTimeInMillis() + 1 * 60 * 1000);
//			mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
//			mMinute = calendar.get(Calendar.MINUTE);
//			mTimePicker.setCurrentHour(mHourOfDay);
//			mTimePicker.setCurrentMinute(mMinute);
//		} else {
			
			mHourOfDay = hourOfDay;
			mMinute = minute;
//		}
		//Gionee <jiating><2013-07-01> modify for CR00829431 end
		updateDate();
		mTimeView.setText(DateFormat.getTimeFormat(mContext).format(mTextDate));
		UnShowDialog();
	}

	private boolean isTrueTime(int h, int m) {
		//Gionee <jiating><2013-07-01> modify for CR00829431 begin
//		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(c.getTimeInMillis() + 1 * 60 * 1000);
//		int year = c.get(Calendar.YEAR);
//		int month = c.get(Calendar.MONTH);
//		int day = c.get(Calendar.DAY_OF_MONTH);
//		int hour = c.get(Calendar.HOUR_OF_DAY);
//		int minute = c.get(Calendar.MINUTE);
//		if (year == mYear && month == mMonthOfYear && day == mDayOfMonth) {
//			if (h < hour) {
//				return false;
//			} else if (h == hour && m < minute) {
//				return false;
//			}
//			return true;
//		}
		//Gionee <jiating><2013-07-01> modify for CR00829431 begin
		return true;
	}

	public void onClick(DialogInterface dialog, int which) {

		switch (which) {
		case BUTTON_POSITIVE:
			Log.i("DataTimeDialog------click button positive!");

			long currentTime = System.currentTimeMillis();
			Log.d("DataTimeDialog------mCalendar.getTimeInMillis: " + mCalendar.getTimeInMillis() + ", System.currentTimeMillis: " + currentTime);

			long timeToSetMinute = mCalendar.getTimeInMillis() / 60000;
			long timeCurrentMinute = currentTime / 60000;
			Log.d("DataTimeDialog------mCalendar.getTimeInMillis/60000: " + timeToSetMinute + ", System.currentTimeMillis/60000: " + timeCurrentMinute);

		
		
				if (mDateTimeCallback != null) {
					mDateTimeCallback.onDateTimeSet(mCalendar);
					mTime = mCalendar.getTimeInMillis();
				}
				if (mDatePicker.getVisibility() == View.VISIBLE) {
					mDatePicker.clearFocus();
				} else if (mTimePicker.getVisibility() == View.VISIBLE) {
					mTimePicker.clearFocus();
				}
			
			break;
		case BUTTON_NEGATIVE:
			UnShowDialog();
			if (mTime != -1) {
				mCalendar.setTimeInMillis(mTime);
			}
			break;

		default:
			break;
		}
	}

	public void updateDate(Calendar c) {
		mDatePicker.updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
		mTimePicker.setCurrentHour(c.get(Calendar.HOUR_OF_DAY));
		mTimePicker.setCurrentMinute(c.get(Calendar.MINUTE));
	}

	public void updateDate(int year, int monthOfYear, int dayOfMonth) {
		mDatePicker.updateDate(year, monthOfYear, dayOfMonth);
	}

	public void updateTime(int hourOfDay, int minutOfHour) {
		mTimePicker.setCurrentHour(hourOfDay);
		mTimePicker.setCurrentMinute(minutOfHour);
	}

	@Override
	public Bundle onSaveInstanceState() {
		Bundle state = super.onSaveInstanceState();
		state.putInt(YEAR, mDatePicker.getYear());
		state.putInt(MONTH, mDatePicker.getMonth());
		state.putInt(DAY, mDatePicker.getDayOfMonth());

		state.putInt(HOUR, mTimePicker.getCurrentHour());
		state.putInt(MINUTE, mTimePicker.getCurrentMinute());
		state.putBoolean(IS_24_HOUR, mTimePicker.is24HourView());
		return state;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int year = savedInstanceState.getInt(YEAR);
		int month = savedInstanceState.getInt(MONTH);
		int day = savedInstanceState.getInt(DAY);
		mDatePicker.init(year, month, day, new DateChangedListener());

		int hour = savedInstanceState.getInt(HOUR);
		int minute = savedInstanceState.getInt(MINUTE);
		mTimePicker.setIs24HourView(savedInstanceState.getBoolean(IS_24_HOUR));
		mTimePicker.setCurrentHour(hour);
		mTimePicker.setCurrentMinute(minute);
	}

	private void UnShowDialog() {
		if (mDialogShow) {
			mDialogShow = false;
			try {
				Field field = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
				field.setAccessible(true);
				field.set(this, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	@Override
	public void onBackPressed() {
		Log.i("DateTimeDialog------onBackPressed!");
		UnShowDialog();
		super.onBackPressed();
	}

}


//Gionee <jiating> <2013-04-24> modify for CR00000000  end
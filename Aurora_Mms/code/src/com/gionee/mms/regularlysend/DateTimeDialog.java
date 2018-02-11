package com.gionee.mms.regularlysend;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import com.android.mms.MmsApp;
import com.android.mms.R;

import aurora.app.AuroraAlertDialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraDatePicker;
import aurora.widget.AuroraDatePicker.OnDateChangedListener;
import android.widget.TextView;
import aurora.widget.AuroraTimePicker;
import android.widget.Toast;
import aurora.widget.AuroraTimePicker.OnTimeChangedListener;

/**
 *#Lunar#
 * A simple dialog containing an {@link aurora.widget.AuroraDatePicker}.
 *
 * <p>See the <a href="{@docRoot}resources/tutorials/views/hello-datepicker.html">Date Picker
 * tutorial</a>.</p>
 */
public class DateTimeDialog extends AuroraAlertDialog implements OnClickListener, OnTimeChangedListener{

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private AuroraDatePicker mDatePicker;

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String IS_24_HOUR = "is24hour";
    //Gionee <zhouyj> <2013-06-27> add for CR00829787 begin
    private static final long DEFALUT_REGULARLY_TIME = 10 * 60 * 1000;//10min
    private static final long SET_MAX_TIME = 2145887999999L;//2037-12-31 23:59
    //Gionee <zhouyj> <2013-06-27> add for CR00829787 end

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

    public DateTimeDialog(Context context,
            OnDateTimeSetListener datetimecallBack,
            Calendar calendar) {
        this(context, AuroraAlertDialog.THEME_HOLO_DARK, datetimecallBack, calendar);
    }

    /**
     * @param context The context the dialog is to run in.
     * @param theme the theme to apply to this dialog
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    public DateTimeDialog(Context context,
            int theme,
            OnDateTimeSetListener datetimecallBack,
            Calendar calendar) {
        super(context, theme);
        mContext = context;
        mDateTimeCallback = datetimecallBack;
        if (calendar == null) {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + DEFALUT_REGULARLY_TIME);
        }
        //Gionee <zhouyj> <2013-06-27> add for CR00829787 begin
        if (calendar.getTimeInMillis() > SET_MAX_TIME) {
            Log.i("DateTimeDialog", "init DateTimeDialog : System time is so large. Date = " 
                    + new Date(calendar.getTimeInMillis()).toString());
            calendar.setTimeInMillis(SET_MAX_TIME);
        }
        //Gionee <zhouyj> <2013-06-27> add for CR00829787 end
        mYear = calendar.get(Calendar.YEAR);
        mMonthOfYear = calendar.get(Calendar.MONTH);
        mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        mMinute = calendar.get(Calendar.MINUTE);
        mIs24HourView = DateFormat.is24HourFormat(context);
        mCalendar = calendar;

        setCanceledOnTouchOutside(false);
        setButton(BUTTON_POSITIVE, getContext().getText(R.string.date_time_set), this);
        setButton(BUTTON_NEGATIVE, getContext().getText(R.string.cancel), this);
        setIcon(0);
        setTitle(R.string.gn_dialog_title);

        LayoutInflater inflater =
                (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gn_date_time_picker_dialog, null);

        initTextView(view);

        setView(view);
    }

    private void updateDate() {
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth, mHourOfDay, mMinute);
        Log.d("Regularly", "DateTimeDialog....updateDate   mCalendar "+mCalendar.getTimeInMillis());
        mTextDate = mCalendar.getTime();
    }

    private void initTextView(View view) {
        mTextDate = mCalendar.getTime();
        mDateView = (TextView) view.findViewById(R.id.gn_date_text);
        mDateView.setText(DateFormat.getDateFormat(mContext).format(mTextDate));
        mDateView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDatePicker.setVisibility(View.VISIBLE);
                mTimePicker.setVisibility(View.GONE);
                /*mDateView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_select_bg));
                mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_light_bg));
            */}
        });
        mTimeView = (TextView) view.findViewById(R.id.gn_time_text);
        mTimeView.setText(DateFormat.getTimeFormat(mContext).format(mTextDate));
        mTimeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDatePicker.setVisibility(View.GONE);
                mTimePicker.setVisibility(View.VISIBLE);
                /*mDateView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_light_bg));
                mTimeView.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.gn_select_bg));
            */}
        });

        mDatePicker = (AuroraDatePicker) view.findViewById(R.id.gn_datePicker);
        mDatePicker.init(mYear, mMonthOfYear, mDayOfMonth, new DateChangedListener());

        mCalendar.clear();
        mCalendar.set(mYear, mMonthOfYear, mDayOfMonth);
//Gionee <guoyx> <2013-08-07> delete for CR00834099 begin
//        mDatePicker.setMinDate(mCalendar.getTimeInMillis());
//        //gionee gaoj 2012-8-21 added for CR00678516 start
//        Time maxTime=new Time();
//        maxTime.set(59,59,23,31,11,2037);//2037/12/31
//        long maxDate=maxTime.toMillis(false);
//        maxDate=maxDate+999;//in millsec
//        mDatePicker.setMaxDate(maxDate);
//        //gionee gaoj 2012-8-21 added for CR00678516 end
//Gionee <guoyx> <2013-08-07> delete for CR00834099 end

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
            // TODO Auto-generated method stub
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
        // TODO Auto-generated method stub
        if (!isTrueTime(hourOfDay, minute)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + 1 * 60 * 1000);
            mHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            mMinute = calendar.get(Calendar.MINUTE);
            mTimePicker.setCurrentHour(mHourOfDay);
            mTimePicker.setCurrentMinute(mMinute);
        } else {
            mHourOfDay = hourOfDay;
            mMinute = minute;
        }
        updateDate();
        mTimeView.setText(DateFormat.getTimeFormat(mContext).format(mTextDate));
        UnShowDialog();
    }

    private boolean isTrueTime(int h, int m) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(c.getTimeInMillis() + 1 * 60 * 1000);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        if (year == mYear && month == mMonthOfYear && day == mDayOfMonth) {
            if (h < hour) {
                return false;
            } else if (h == hour && m < minute) {
                return false;
            }
            return true;
        }
        return true;
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case BUTTON_POSITIVE:
            if (mCalendar.getTimeInMillis() <= System.currentTimeMillis()) {
                mDialogShow = true;
                try {
                    Field field = dialog.getClass().getSuperclass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(this, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(mContext, mContext.getString(R.string.gn_time_early),
                        Toast.LENGTH_SHORT).show();
            } else {
                if (mDateTimeCallback != null) {
                    mDateTimeCallback.onDateTimeSet(mCalendar);
                    mTime = mCalendar.getTimeInMillis();
                }
                if (mDatePicker.getVisibility() == View.VISIBLE) {
                    mDatePicker.clearFocus();
                } else if (mTimePicker.getVisibility() == View.VISIBLE) {
                    mTimePicker.clearFocus();
                }
            }
            //Gionee <guoyx> <2013-06-18> add for CR00818727 begin 
            hideInputMethod();
            //Gionee <guoyx> <2013-06-18> add for CR00818727 end
            break;
        case BUTTON_NEGATIVE:
            UnShowDialog();
            if (mTime != -1) {
                mCalendar.setTimeInMillis(mTime);
            }
            //Gionee <guoyx> <2013-06-18> add for CR00818727 begin 
            hideInputMethod();
            //Gionee <guoyx> <2013-06-18> add for CR00818727 end
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

    //gionee gaoj 2012-9-28 added for CR00705551 start
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        switch (keyCode) {
        case KeyEvent.KEYCODE_BACK:
            UnShowDialog();
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }
    //gionee gaoj 2012-9-28 added for CR00705551 end
    
    //Gionee <guoyx> <2013-06-18> add for CR00818727 begin
    private void hideInputMethod() {
        InputMethodManager inputMethodManager = (InputMethodManager) mContext
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (this.getWindow() != null
                && this.getWindow().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(this.getWindow()
                    .getCurrentFocus().getWindowToken(), 0);
        }
    }
    //Gionee <guoyx> <2013-06-18> add for CR00818727 end
}
package com.aurora.note.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import aurora.widget.AuroraNumberPicker;
import aurora.widget.AuroraTimePicker;

import com.aurora.note.R;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.TimeUtils;

import java.util.Calendar;

/**
 * 添加备忘提醒选择日期和时间的控件
 * 
 * @author JimXia
 * @date 2014-4-11 下午5:38:55
 */
public class DateTimePickerView extends FrameLayout {

	private static final int TEXT_SIZE = 18; // dp

	private OnWeekSetListener mOnWeekSetListener;
	private Calendar mCurrentDate;

	private AuroraTimePicker mTimePicker;
	private AuroraNumberPicker mWeekPicker;

	private int mNumberOfDay;
	private String[] mWeekValues;

	private boolean isChineseEnvironment = true;

	public static interface OnWeekSetListener {
		void onWeekSet(Calendar paramCalendar);
	}

	public DateTimePickerView(Context context) {
		this(context, null);
	}

	public DateTimePickerView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DateTimePickerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		isChineseEnvironment = SystemUtils.isChineseEnvironment();

		LayoutInflater.from(context).inflate(R.layout.date_time_picker_view, this, true);

		initCalendar();
		initView();

		mWeekPicker.setOnValueChangedListener(new AuroraNumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(AuroraNumberPicker picker, int oldVal, int newVal) {
                String dateStringNoYear = mWeekValues[newVal];
                if (oldVal == mNumberOfDay - 1 && newVal == mNumberOfDay) {
                    mCurrentDate.add(Calendar.YEAR, 1);
                    mCurrentDate.set(Calendar.MONTH, Calendar.JANUARY);
                    mCurrentDate.set(Calendar.DAY_OF_MONTH, 1);
                } else if (oldVal == mNumberOfDay && newVal == mNumberOfDay - 1) {
                    mCurrentDate.add(Calendar.YEAR, -1);
                    mCurrentDate.set(Calendar.MONTH, Calendar.DECEMBER);
                    mCurrentDate.set(Calendar.DAY_OF_MONTH, 31);
                } else {
                    String simpleDateString = TimeUtils.getSimpleDateString(mCurrentDate.get(Calendar.YEAR),
                            dateStringNoYear, isChineseEnvironment);
                    String[] date = simpleDateString.split("-");
                    mCurrentDate.set(Calendar.MONTH, Integer.parseInt(date[1]) - 1);
                    mCurrentDate.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[2]));
                }
                notifyDataChanged();
            }
        });
	}

	public void setOnWeekSetListener(OnWeekSetListener onWeekSetListener) {
		mOnWeekSetListener = onWeekSetListener;
	}

	private void notifyDataChanged() {
		if (mOnWeekSetListener != null) {
			mOnWeekSetListener.onWeekSet(mCurrentDate);
		}
	}

	private void initCalendar() {
		mCurrentDate = Calendar.getInstance();
		mCurrentDate.set(Calendar.SECOND, 0);
		mCurrentDate.set(Calendar.MILLISECOND, 0);
	}

	private void initView() {
		mWeekPicker = (AuroraNumberPicker) findViewById(R.id.aurora_week);
		mTimePicker = (AuroraTimePicker) findViewById(R.id.aurora_time_picker);
		initWeekPicker();
		initTimePicker();
	}

	private void initTimePicker() {
		mTimePicker.setTextSize(TEXT_SIZE);
		mTimePicker.hideAmPm(true);
		mTimePicker.setIs24HourView(true);
		mTimePicker.setCurrentHour(mCurrentDate.get(Calendar.HOUR_OF_DAY)); // 很奇怪，明明设置了24小时
        mTimePicker.setOnTimeChangedListener(new AuroraTimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(AuroraTimePicker auroraTimePicker, int hour, int minute) {
                mCurrentDate.set(Calendar.HOUR_OF_DAY, hour);
                mCurrentDate.set(Calendar.MINUTE, minute);
                notifyDataChanged();
            }
        });
	}

	private void initWeekPicker() {
		initWeekPickerDatas();

		mWeekPicker.setMinValue(0);
		mWeekPicker.setTextSize(TEXT_SIZE);
		mWeekPicker.setMaxValue(mWeekValues.length - 1);
		mWeekPicker.setDisplayedValues(mWeekValues);
		mWeekPicker.setWrapSelectorWheel(false);

		final String dateNoYearStr = getDateString(mCurrentDate);
		int index = 0;
		while (index < mNumberOfDay) {
			if (mWeekValues[index].equals(dateNoYearStr)) {
				break;
			}
			index++;
		}
	    mWeekPicker.setValue(index);
	}

	private void initWeekPickerDatas() {
		final int numberOfNextYearDay = mCurrentDate.getActualMaximum(Calendar.DAY_OF_YEAR);
		mNumberOfDay = mCurrentDate.getActualMaximum(Calendar.DAY_OF_YEAR);
		final int numberOfDay = mNumberOfDay;
		final int length = numberOfDay + numberOfNextYearDay;
		mWeekValues = new String[length];
		final Calendar weekDateCalendar = Calendar.getInstance();		
		for (int i = 0; i < numberOfDay; i++) {
		    weekDateCalendar.set(Calendar.YEAR, mCurrentDate.get(Calendar.YEAR));
            weekDateCalendar.set(Calendar.DAY_OF_YEAR, i + 1);
            mWeekValues[i] = getDateString(weekDateCalendar);
		}

		int day = 1;
		for (int i = numberOfDay; i < length; i++) {
		    weekDateCalendar.set(Calendar.YEAR, mCurrentDate.get(Calendar.YEAR) + 1);
            weekDateCalendar.set(Calendar.DAY_OF_YEAR, day ++);
            mWeekValues[i] = getDateString(weekDateCalendar);
		}
	}

	private String getDateString(Calendar weekDateCalendar) {
		return TimeUtils.formatDateNoYear(weekDateCalendar.getTimeInMillis(), isChineseEnvironment);
	}

	public Calendar getSelectedDateTime() {
	    return mCurrentDate;
	}

	public void setSelectedDateTime(long timestamp) {
	    Calendar calendar = Calendar.getInstance();
	    calendar.setTimeInMillis(timestamp);

	    int startIndex = 0;
	    int endIndex = mNumberOfDay;
	    if (calendar.get(Calendar.YEAR) > mCurrentDate.get(Calendar.YEAR)) {
	        startIndex = endIndex;
	        endIndex = mWeekValues.length;
	    }
	    final String strTime = getDateString(calendar);
	    final String[] values = mWeekValues;
	    while (startIndex < endIndex) {
	        if (values[startIndex].equals(strTime)) {
	            break;
	        }
	        startIndex ++;
	    }
	    mWeekPicker.setValue(startIndex);

	    mTimePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
	    mTimePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));

	    mCurrentDate.setTimeInMillis(timestamp);
	}
}
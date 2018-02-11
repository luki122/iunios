package com.android.auroramusic.widget;

import java.util.Calendar;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.music.R;

public class TimeShowView extends LinearLayout {

	private static String TAG = "liumx";
	private ImageView ivHourTens;
	private ImageView ivHourUnits;
	private ImageView ivMinTens;
	private ImageView ivMinUnits;
	private Context context;
	private int[] numImageResource = new int[10];
	private Calendar mCalendar;

	public TimeShowView(Context context) {
		super(context);
		//Log.i(TAG, "zll ---- TimeShowView 1");
		init(context);
	}

	public TimeShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		//Log.i(TAG, "zll ---- TimeShowView 2");
		init(context);
	}

	public TimeShowView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//Log.i(TAG, "zll ---- TimeShowView 3");
		init(context);
	}

	private void init(Context context) {
		if (context == null) {
			//Log.i(TAG, "zll ---- TimeShowView init --- fail");
			return;
		}

		numImageResource[0] = R.drawable.num_0;
		numImageResource[1] = R.drawable.num_1;
		numImageResource[2] = R.drawable.num_2;
		numImageResource[3] = R.drawable.num_3;
		numImageResource[4] = R.drawable.num_4;
		numImageResource[5] = R.drawable.num_5;
		numImageResource[6] = R.drawable.num_6;
		numImageResource[7] = R.drawable.num_7;
		numImageResource[8] = R.drawable.num_8;
		numImageResource[9] = R.drawable.num_9;

		//Log.i(TAG, "zll ---- TimeShowView init --- 1");
		this.context = context;
		LayoutInflater.from(context).inflate(
				R.layout.aurora_lock_screen_time_display, this);
		mCalendar = Calendar.getInstance();
		initView();
		return;
	}

	private void initView() {
		ivHourTens = (ImageView) findViewById(R.id.iv_hour_tens);
		ivHourUnits = (ImageView) findViewById(R.id.iv_hour_units);
		ivMinTens = (ImageView) findViewById(R.id.iv_min_tens);
		ivMinUnits = (ImageView) findViewById(R.id.iv_min_units);
	}

	public void updateTime(String mFormat) {
		mCalendar.setTimeInMillis(System.currentTimeMillis());

		CharSequence newTime = DateFormat.format(mFormat, mCalendar);
		if (newTime != null) {
			if (newTime.length() == 5) {
				String mHourTens = newTime.toString().substring(0, 1);
				String mHourUnits = newTime.toString().substring(1, 2);
				String mMinTens = newTime.toString().substring(3, 4);
				String mMinUnits = newTime.toString().substring(4);
				//Log.i(TAG, "zll --- updateTime mHourTens:" + mHourTens);
				//Log.i(TAG, "zll --- updateTime mHourUnits:" + mHourUnits);
				//Log.i(TAG, "zll --- updateTime mMinTens:" + mMinTens);
				//Log.i(TAG, "zll --- updateTime mMinUnits:" + mMinUnits);
				if (mHourTens != null) {
					int tem = Integer.parseInt(mHourTens);
					if (tem < 10) {
						ivHourTens.setImageResource(numImageResource[tem]);
					}

				}
				if (mHourUnits != null) {
					int tem = Integer.parseInt(mHourUnits);
					if (tem < 10) {
						ivHourUnits.setImageResource(numImageResource[tem]);
					}
				}
				if (mMinTens != null) {
					int tem = Integer.parseInt(mMinTens);
					if (tem < 10) {
						ivMinTens.setImageResource(numImageResource[tem]);
					}
				}
				if (mMinUnits != null) {
					int tem = Integer.parseInt(mMinUnits);
					if (tem < 10) {
						ivMinUnits.setImageResource(numImageResource[tem]);
					}
				}

			}
			//Log.i(TAG, "zll --- updateTime newTime:" + newTime);

		} else {
			//Log.i(TAG, "zll ---- TimeShowView updateTime --- " + newTime);
		}

	}

}

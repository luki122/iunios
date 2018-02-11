package com.android.settings.notification;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.android.settings.R;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.util.Log;
import android.service.notification.ZenModeConfig;

import aurora.app.AuroraTimePickerDialog;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraPreferenceCategory;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;
import aurora.widget.AuroraTimePicker;

import com.android.settings.R;
import com.android.settings.AuroraSettingsPreferenceFragment.SettingsDialogFragment;
import com.android.settings.SettingsPreferenceFragment;

public class AuroraDndTimingPickerPreference extends AuroraPreference implements
		OnClickListener, AuroraTimePickerDialog.OnTimeSetListener {

	private static final String TAG = "AuroraDndTimingPickerPreference";

	// 时间设置类型：0x01设置开始时间 0x02设置结束时间
	private static final int DIALOG_STARTTIME_PICKER = 0x01;
	private static final int DIALOG_ENDTIME_PICKER = 0x02;

	// 保存勿扰模式
	private ZenModeConfig mConfigTag = null;

	private int mStartMin = 0;
	private int mEndMin = 0;
	private boolean bNextDayFlag = false;
	private final static SimpleDateFormat gdFormatter = new SimpleDateFormat(
			"kk:mm");

	// 标记设置类型
	private int mTimingSetType = 0;

	// 回调用于更新勿扰模式
	private Callback mCallback;

	private Context mContext;

	private LinearLayout mStartTimingLayout = null;
	private LinearLayout mEndTimingLayout = null;
	private TextView mStartTimingTextView = null;
	private TextView mEndTimingTextView = null;

	public AuroraDndTimingPickerPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	public AuroraDndTimingPickerPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AuroraDndTimingPickerPreference(Context context) {
		this(context, null);
	}

	public void AuroraDndTimingScreenInit(ZenModeConfig config) {
		Log.d(TAG, "***AuroraDndTimingScreenInit***");

		if (config != null) {
			mConfigTag = config;
			mStartMin = 60 * config.sleepStartHour + config.sleepStartMinute;
			mEndMin = 60 * config.sleepEndHour + config.sleepEndMinute;
		}

	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		Log.d(TAG, "***onBindView***");

		mStartTimingLayout = (LinearLayout) view
				.findViewById(R.id.aurora_timing_start);
		mStartTimingLayout.setOnClickListener(this);

		mEndTimingLayout = (LinearLayout) view
				.findViewById(R.id.aurora_timing_end);
		mEndTimingLayout.setOnClickListener(this);

		mStartTimingTextView = (TextView) view
				.findViewById(R.id.timing_start_value);
		mEndTimingTextView = (TextView) view
				.findViewById(R.id.timing_end_value);

		updateSummary(mConfigTag, true);
	}

	@Override
	public void onClick(View v) {
		final Calendar calendar = Calendar.getInstance();
		switch (v.getId()) {
		case R.id.aurora_timing_start:
			mTimingSetType = DIALOG_STARTTIME_PICKER;

			AuroraTimePickerDialog start = new AuroraTimePickerDialog(mContext,
					this, mConfigTag.sleepStartHour,
					mConfigTag.sleepStartMinute,
					DateFormat.is24HourFormat(mContext));

			start.show();
			break;

		case R.id.aurora_timing_end:
			mTimingSetType = DIALOG_ENDTIME_PICKER;

			AuroraTimePickerDialog end = new AuroraTimePickerDialog(mContext,
					this, mConfigTag.sleepEndHour, mConfigTag.sleepEndMinute,
					DateFormat.is24HourFormat(mContext));

			end.show();
			break;

		default:
			break;
		}
	}

	// 更新状态
	// firstinit 第一次初始化
	private void updateSummary(ZenModeConfig config, boolean firstinit) {
		final Calendar c = Calendar.getInstance();

		// 如果开始时间有更新
		if (config.sleepStartHour != mConfigTag.sleepStartHour
				|| config.sleepStartMinute != mConfigTag.sleepStartMinute
				|| firstinit) {

			mConfigTag.sleepStartHour = config.sleepStartHour;
			mConfigTag.sleepStartMinute = config.sleepStartMinute;
			mStartMin = 60 * config.sleepStartHour + config.sleepStartMinute;

			// 对比结束时间是否需要显示“次日”
			bNextDayFlag = mStartMin >= mEndMin;
			Log.d(TAG, "mStartMin:" + mStartMin + " mEndMin:" + mEndMin);

			c.set(Calendar.HOUR_OF_DAY, config.sleepStartHour);
			c.set(Calendar.MINUTE, config.sleepStartMinute);
			// 显示开始时间
			mStartTimingTextView.setText(DateFormat.getTimeFormat(mContext)
					.format(c.getTime()));

			// 更新结束时间
			Log.d(TAG, "bNextDayFlag:" + bNextDayFlag);
			c.set(Calendar.HOUR_OF_DAY, mConfigTag.sleepEndHour);
			c.set(Calendar.MINUTE, mConfigTag.sleepEndMinute);
			if (bNextDayFlag) {
				mEndTimingTextView.setText("次日 "
						+ DateFormat.getTimeFormat(mContext)
								.format(c.getTime()));
			} else {
				mEndTimingTextView.setText(DateFormat.getTimeFormat(mContext)
						.format(c.getTime()));
			}
		}

		// 如果结束时间有更新
		if (config.sleepEndHour != mConfigTag.sleepEndHour
				|| config.sleepEndMinute != mConfigTag.sleepEndMinute
				|| firstinit) {

			mConfigTag.sleepEndHour = config.sleepEndHour;
			mConfigTag.sleepEndMinute = config.sleepEndMinute;
			mEndMin = 60 * config.sleepEndHour + config.sleepEndMinute;

			// 对比结束时间是否需要显示“次日”
			bNextDayFlag = mStartMin >= mEndMin;

			c.set(Calendar.HOUR_OF_DAY, mConfigTag.sleepEndHour);
			c.set(Calendar.MINUTE, mConfigTag.sleepEndMinute);

			// 更新结束时间
			if (bNextDayFlag) {
				mEndTimingTextView.setText("次日 "
						+ DateFormat.getTimeFormat(mContext)
								.format(c.getTime()));
			} else {
				mEndTimingTextView.setText(DateFormat.getTimeFormat(mContext)
						.format(c.getTime()));
			}
		}

	}

	@Override
	public void onTimeSet(AuroraTimePicker arg0, int hour, int minute) {

		final ZenModeConfig newConfig = mConfigTag.copy();

		// 开始时间
		if (mTimingSetType == DIALOG_STARTTIME_PICKER) {

			newConfig.sleepStartHour = hour;
			newConfig.sleepStartMinute = minute;

			// 回调更新数据
			if (mCallback != null) {
				mCallback.onSetStartTime(hour, minute);
			}
			// 结束时间
		} else if (mTimingSetType == DIALOG_ENDTIME_PICKER) {

			newConfig.sleepEndHour = hour;
			newConfig.sleepEndMinute = minute;

			// 回调更新数据
			if (mCallback != null) {
				mCallback.onSetEndTime(hour, minute);
			}
		}
		// 更新UI
		updateSummary(newConfig, false);

	}

	public void setCallback(Callback callback) {
		mCallback = callback;
	}

	public interface Callback {
		boolean onSetStartTime(int hour, int minute);

		boolean onSetEndTime(int hour, int minute);
	}
}

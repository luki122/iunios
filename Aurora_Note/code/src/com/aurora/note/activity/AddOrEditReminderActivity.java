package com.aurora.note.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;

import com.aurora.note.R;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.widget.DateTimePickerView;

import java.util.Calendar;

/**
 * 添加编辑提醒界面
 * 
 * @author JimXia
 * @date 2014-4-14 上午11:14:37
 */
public class AddOrEditReminderActivity extends AuroraActivity implements OnClickListener {

	private static final String TAG = "AddReminderActivity";

	public static final String KEY_REMINDER_DATE_TIMESTAMP = "reminderDateTimestamp";

	private TextView mSelectedDateTimeTv;
	private DateTimePickerView mDateTimePickerView;
	private TextView mDelReminderBtn;

	private long mSelectedDateTimestamp;

	public static final int MODE_ADD_REMINDER = 1;
	public static final int MODE_EDIT_REMINDER = 2;
	public static final int MODE_DEL_REMINDER = 3;
	public static final String KEY_MODE = "mode";
	private int mMode = MODE_ADD_REMINDER;

	private boolean isChineseEnvironment = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		mMode = intent.getIntExtra(KEY_MODE, MODE_ADD_REMINDER);
		if (mMode == MODE_EDIT_REMINDER) {
			mSelectedDateTimestamp = intent.getLongExtra(
					KEY_REMINDER_DATE_TIMESTAMP, -1L);
			if (mSelectedDateTimestamp == -1L) {
			    Log.e(TAG, "编辑模式，提醒时间不能为空");
				finish();
				return;
			}
		}
		isChineseEnvironment = SystemUtils.isChineseEnvironment();
		setAuroraContentView(R.layout.add_reminder_activity, AuroraActionBar.Type.Dashboard);
		initActionBar();
		initViews();
		setListeners();
		initData();
	}

	private void initActionBar() {
		AuroraActionBar actionBar = getAuroraActionBar();
		actionBar.getCancelButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(false, false);
			}
		});
		actionBar.getOkButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish(false, true);
			}
		});

		TextView titleView = actionBar.getMiddleTextView();
		if (mMode == MODE_EDIT_REMINDER) {
			titleView.setText(R.string.menu_mod_reminder);
		} else {
			titleView.setText(R.string.menu_add_reminder);
		}
	}

	private void initViews() {
		mSelectedDateTimeTv = (TextView) findViewById(R.id.selected_date_time_tv);
		mDateTimePickerView = (DateTimePickerView) findViewById(R.id.date_time_picker);
		mDelReminderBtn = (TextView) findViewById(R.id.delete_reminder_btn);

		if (mMode == MODE_EDIT_REMINDER) {
			mDelReminderBtn.setVisibility(View.VISIBLE);
		}
	}

	private void setListeners() {
		mDateTimePickerView.setOnWeekSetListener(
				new DateTimePickerView.OnWeekSetListener() {
					@Override
					public void onWeekSet(Calendar dateTimeCalendar) {
						setSelectedDateTime(dateTimeCalendar);
					}
				});
		mDelReminderBtn.setOnClickListener(this);
	}

	private void setSelectedDateTime(Calendar calendar) {
		mSelectedDateTimestamp = calendar.getTimeInMillis();
		// mSelectedDateTimeTv.setText(TimeUtils.formatTimestamp(mSelectedDateTimestamp));
		mSelectedDateTimeTv.setText(TimeUtils.formatDateTime(mSelectedDateTimestamp, isChineseEnvironment));
	}

	private void initData() {
		if (mMode == MODE_EDIT_REMINDER) {
			mDateTimePickerView.setSelectedDateTime(mSelectedDateTimestamp);
		}
		setSelectedDateTime(mDateTimePickerView.getSelectedDateTime());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.delete_reminder_btn:
			confirmDeleteReminder();
			break;
		}
	}

	private void confirmDeleteReminder() {
		new AuroraAlertDialog.Builder(this).setTitle(R.string.delete_confirm_title)
				//.setMessage(R.string.delete_confirm_message)
				.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish(true, true);
					}
				})
				.setNegativeButton(R.string.delete_confirm_cancel_btn, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish(true, false);
					}
				}).create().show();
	}

	private void finish(boolean delete, boolean success) {
		if (success) {
			Intent data = new Intent();
			if (delete) {
				data.putExtra(KEY_MODE, MODE_DEL_REMINDER);
			} else {
				data.putExtra(KEY_REMINDER_DATE_TIMESTAMP,
						mSelectedDateTimestamp);
			}
			setResult(RESULT_OK, data);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}
}
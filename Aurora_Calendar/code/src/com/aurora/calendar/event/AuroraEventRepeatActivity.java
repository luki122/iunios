package com.aurora.calendar.event;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import aurora.app.AuroraListActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraListView;

import com.android.calendar.R;

public class AuroraEventRepeatActivity extends AuroraListActivity {

	public final String TAG = AuroraEventRepeatActivity.class.getSimpleName();
	public final String START_TIME = "start_time";
	public final String IS_WEEKDAY_EVENT = "is_weekday";
	public final String IS_CUSTOM = "is_custom";
	public final String POSITION = "position";
	private ArrayList<String> repeatArray;
	private ArrayList<Integer> recurrenceIndexes;
	private int mPosition;
	private long millisStart;
	private Context mContext;
	private boolean isWeekdayEvent, isCustomRecurrence;
	private Time startTime;
	private OnAuroraActionBarBackItemClickListener mOnAuroraActionBarBackItemClickListener = 
			new OnAuroraActionBarBackItemClickListener() {
				@Override
				public void onAuroraActionBarBackItemClicked(int arg0) {
					savePositionAndReturnResult(mPosition);
				}
			};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getApplicationContext();
		Bundle extras = getIntent().getExtras();

		if (extras != null) {
			millisStart = extras.getLong(START_TIME);
			isWeekdayEvent = extras.getBoolean(IS_WEEKDAY_EVENT);
			isCustomRecurrence = extras.getBoolean(IS_CUSTOM);
			mPosition = extras.getInt(POSITION);
			startTime = new Time();
			startTime.set(millisStart);
			Log.e(TAG, "startTime----------------------------" + startTime);
			Log.e(TAG, "mPosition----------------------------" + mPosition);
		}

		setAuroraContentView(R.layout.aurora_simple_list_activity, AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.aurora_event_edit_repeat_title);
		getAuroraActionBar().setmOnActionBarBackItemListener(mOnAuroraActionBarBackItemClickListener);

		AuroraListView lv = getListView();
		lv.setSelector(new ColorDrawable(Color.TRANSPARENT));
		if (repeatArray == null || repeatArray.isEmpty()) {
			repeatArray = new ArrayList<String>(0);
			prepareChooseItems();
		}
	}

	private void prepareChooseItems() {
		Resources r = getResources();

		String[] ordinals = r.getStringArray(R.array.ordinal_labels);

		repeatArray.add(r.getString(R.string.aurora_does_not_repeat));

		repeatArray.add(r.getString(R.string.daily));

		if (isWeekdayEvent) {
			repeatArray.add(r.getString(R.string.aurora_every_weekday));
		}

		String format = r.getString(R.string.aurora_weekly);
		repeatArray.add(String.format(format, startTime.format("%A")));

		// Calculate whether this is the 1st, 2nd, 3rd, 4th, or last appearance
		// of the given day.
		int dayNumber = (startTime.monthDay - 1) / 7;
		format = r.getString(R.string.aurora_monthly_on_day_count);
		repeatArray.add(String.format(format, ordinals[dayNumber], startTime.format("%A")));

		format = r.getString(R.string.monthly_on_day);
		repeatArray.add(String.format(format, startTime.monthDay));

		long when = startTime.toMillis(false);
		format = r.getString(R.string.yearly_plain);
		int flags = 0;
		if (DateFormat.is24HourFormat(mContext)) {
			flags |= DateUtils.FORMAT_24HOUR;
		}
		repeatArray.add(format);

		if (isCustomRecurrence) {
			repeatArray.add(r.getString(R.string.custom));
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				AuroraEventRepeatActivity.this,
				R.layout.aurora_edit_event_repeat_item, repeatArray);
		setListAdapter(adapter);
		getListView().setChoiceMode(AuroraListView.CHOICE_MODE_SINGLE);
		getListView().setItemChecked(mPosition, true);
		getListView().setOnItemClickListener(mOnItemClickListener);

	}

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (view == null) {
				return;
			}
			mPosition = position;
			Log.i("liumx", "Recurrence mPosition choosen is :" + mPosition);
		}

	};

	private void savePositionAndReturnResult(int position) {
		Intent intent = new Intent();
		intent.putExtra("position", position);

		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		savePositionAndReturnResult(mPosition);
		super.onBackPressed();
	}

}
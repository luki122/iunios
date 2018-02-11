package com.aurora.worldtime;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.deskclock.Log;
import com.android.deskclock.R;

/**
 * dataAdapter for city list
 * 
 *
 */
public class WorldTimeListAdapter extends BaseAdapter {

	private TextView mCity, mTime, mDay, mYear;
	private AnalogClocks nalogClock;
	CheckBox mDeleteOneCheckBox;
	private LayoutInflater layoutInflater;
	private int mark;
	List<Boolean> mChecked;
	ArrayList<Object> choicemNameData;
	List<Object> choiceIdData;
	List<Object> choicePosi;
	// private final Handler mHandler = new Handler();
	Context contexts;

	List<City> mWorldTimeList = new ArrayList<City>();

	public WorldTimeListAdapter(Context contexts,
			LayoutInflater layoutInflater, int mark, List<City> mWorldTimeList) {
		this.layoutInflater = layoutInflater;
		this.mark = mark;
		this.mWorldTimeList = mWorldTimeList;
		this.contexts = contexts;
		choicemNameData = new ArrayList<Object>();
		choiceIdData = new ArrayList<Object>();
		choicePosi = new ArrayList<Object>();
		mChecked = new ArrayList<Boolean>();
		for (int i = 0; i < mWorldTimeList.size(); i++) {
			mChecked.add(false);
		}

	}

	/*
	 * register receiver
	 */
	public void registerReceiverForTime() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_TIME_TICK);
		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

		contexts.registerReceiver(mIntentReceiver, filter, null, null);
	}

	public void unregisterReceiverForTime() {
		contexts.unregisterReceiver(mIntentReceiver);
	}
	public void updateWorldTiemDada(List<City> list) {
		mWorldTimeList = list;
		notifyDataSetChanged();
	}


	@Override
	public int getCount() {
		return mWorldTimeList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mWorldTimeList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	public void updateView(Boolean isChceck) {

		mChecked.clear();
		choicemNameData.clear();
		choiceIdData.clear();
		choicePosi.clear();
		// gionee 20120628 by yuchunfei start
		for (int i = 0; i < mWorldTimeList.size(); i++) {

			if (isChceck) {
				mChecked.add(true);
				choicePosi.add(mDeleteOneCheckBox.getTag());
				choicemNameData.add(mWorldTimeList.get(i).getName());
				choiceIdData.add(mWorldTimeList.get(i).getId());

			} else {
				mChecked.add(false);
			}

		}
		// gionee 20120628 by yuchunfei end
		notifyDataSetChanged();
	}
	/*
	 * change the position
	 */
	public void exchangeMore(int startPosition, int endPosition) {
		// slipping down
		if (startPosition < endPosition) {
			Object startObject = getItem(startPosition);
			for (int i = (endPosition - startPosition); i > 0; i--) {
				Log.d("startPosition < endPosition--" + i);
				Object endObject = getItem(endPosition - i + 1);
				mWorldTimeList.set(endPosition - i, (City) endObject);
			}
			mWorldTimeList.set(endPosition, (City) startObject);
		}
		// slipping up
		if (startPosition > endPosition) {
			Object startObject = getItem(startPosition);
			for (int i = (startPosition - endPosition); i > 0; i--) {
				Log.d("startPosition > endPosition--" + i);
				Object endObject = getItem(endPosition + i - 1);
				mWorldTimeList.set(endPosition + i, (City) endObject);
			}
			mWorldTimeList.set(endPosition, (City) startObject);
		}

		notifyDataSetChanged();
	}

	public ArrayList<Object> getChoicemNameData() {
		return choicemNameData;
	}

	public void setChoicemNameData(ArrayList<Object> choicemNameData) {
		this.choicemNameData = choicemNameData;
	}

	public List<Object> getChoiceIdData() {
		return choiceIdData;
	}

	public void setChoiceIdData(List<Object> choiceIdData) {
		this.choiceIdData = choiceIdData;
	}

	public List<Object> getChoicePosi() {
		return choicePosi;
	}

	public void setChoicePosi(List<Object> choicePosi) {
		this.choicePosi = choicePosi;
	}

	@Override
	public View getView(int arg0, View view, ViewGroup arg2) {
		if (view == null) {
			view = layoutInflater.inflate(R.layout.worldtimelist_white,
						null);
		}
		UpdateTime(arg0, view);

		if (mark == 1) {
			mDeleteOneCheckBox = (CheckBox) view
					.findViewById(R.id.select_world_time);
			mDeleteOneCheckBox.setVisibility(View.VISIBLE);

			final int p = arg0;
			mDeleteOneCheckBox.setTag(arg0);
			mDeleteOneCheckBox.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					CheckBox cb = (CheckBox) v;

					mChecked.set(p, cb.isChecked());
					// gionee 20120628 by yuchunfei start
					if (cb.isChecked()) {
						choicePosi.add(mDeleteOneCheckBox.getTag());

						choicemNameData.add(mWorldTimeList.get(
								Integer.parseInt(cb.getTag().toString()))
								.getName());
						choiceIdData.add(mWorldTimeList.get(
								Integer.parseInt(cb.getTag().toString()))
								.getId());
					} else {
						choicePosi.remove(mDeleteOneCheckBox.getTag());
						choicemNameData.remove(mWorldTimeList.get(
								Integer.parseInt(cb.getTag().toString()))
								.getName());
						choiceIdData.remove(mWorldTimeList.get(
								Integer.parseInt(cb.getTag().toString()))
								.getId());
					}
					// gionee 20120628 by yuchunfei end
					//context.updateContent();

				}
			});
			mDeleteOneCheckBox.setChecked(mChecked.get(arg0));
			//context.updateContent();
		}

		if (mark == 2) {
			ImageView sortImageView = (ImageView) view
					.findViewById(R.id.iv_world_time_sort_hot);
			sortImageView.setVisibility(View.VISIBLE);
		}

		return view;
	}

	/*
	 * time ui
	 */
	public void UpdateTime(int position, View view) {
		nalogClock = (AnalogClocks) view.findViewById(R.id.anologclock);
		mCity = (TextView) view.findViewById(R.id.tv_world_city);
		mTime = (TextView) view.findViewById(R.id.tv_world_time);
		mDay = (TextView) view.findViewById(R.id.tv_world_day);
		mYear = (TextView) view.findViewById(R.id.tv_world_year);
		Time time = nalogClock
				.setTimeZone(mWorldTimeList.get(position).getId());
		// nalogClock.setClockImage(context, dial, hour, minute);
		mCity.setText(mWorldTimeList.get(position).getName());
		mCity.setSelected(true);

		if (time.hour <= 17 && time.hour >= 6) {
			nalogClock.setClockImage(R.drawable.world_time_dial_black_white, R.drawable.world_time_hour_black_white,
					R.drawable.world_time_minute_black_white);
		} else {
			nalogClock.setClockImage(R.drawable.world_time_clock_dial_white,
					R.drawable.world_time_clock_hour_black_white, R.drawable.world_time_clock_minute_black_white);
		}

        // Gionee baorui 2013-01-09 modify for CR00756424 begin
        // int hour = time.hour > 12 ? time.hour - 12 : time.hour;
        int hour = -1;
        if (nalogClock.ismShowDay()) { // M12
            hour = time.hour > 12 ? time.hour - 12 : time.hour;
            // Gionee baorui 2013-01-17 modify for CR00764929 begin
            mDay.setVisibility(View.VISIBLE);
            // Gionee baorui 2013-01-17 modify for CR00764929 end
        } else { // M24
            hour = time.hour > 12 ? time.hour : time.hour;
            // Gionee baorui 2013-01-17 modify for CR00764929 begin
            mDay.setVisibility(View.GONE);
            // Gionee baorui 2013-01-17 modify for CR00764929 end
        }
        // Gionee baorui 2013-01-09 modify for CR00756424 end
        String hours = hour < 10 ? "0" + hour : "" + hour;
        String minute = time.minute < 10 ? "0" + time.minute : "" + time.minute;
        String times = hours + ":" + minute;
        mTime.setText(times);
        
        String day = time.hour > 11 ? contexts.getResources().getString(R.string.world_time_afernoon)
                : contexts.getResources().getString(R.string.world_time_morning);
        mDay.setText(day);
        
		int month = time.month + 1;
		String year = time.year + "-" + month + "-" + time.monthDay;

		mYear.setText(year);
	}

	/*
	 * receiver broadcast , refresh UI
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			handler.sendEmptyMessage(0);
		}
	};

	private Handler handler = new Handler(){
		
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			notifyDataSetChanged();
		};
		
	};
	
	
	public void onViewClick(int position) {
		if(mark == 1){
			if (mChecked.get(position)) {
				mChecked.set(position, false);
				choicePosi.remove(mDeleteOneCheckBox.getTag());
				choicemNameData.remove(mWorldTimeList.get(position).getName());
				choiceIdData.remove(mWorldTimeList.get(position).getId());
			} else {
				mChecked.set(position, true);
				choicePosi.add(mDeleteOneCheckBox.getTag());
				choicemNameData.add(mWorldTimeList.get(position).getName());
				choiceIdData.add(mWorldTimeList.get(position).getId());
			}
			//context.updateContent();
			notifyDataSetChanged();
		}
		
	}

}
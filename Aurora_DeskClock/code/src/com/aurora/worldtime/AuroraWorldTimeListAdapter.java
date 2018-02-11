package com.aurora.worldtime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.deskclock.Log;
import com.android.deskclock.R;

/**
 * dataAdapter for city list
 * 
 *
 */
public class AuroraWorldTimeListAdapter extends BaseAdapter {

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
	
	//aurora add by tangjun 2011.1.4 start AnalogClocks改成ImageView,策划用不到，只用到里面的函数
	ImageView dayornightView;
	//aurora add by tangjun 2014.1.3 end
	
	//GMT时间统计 aurora add by tangjun 2014.2.19
	private int mCount = 0;

	public AuroraWorldTimeListAdapter(Context contexts,
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
		
		this.mCount = getGMTCount(getTimeZoneText());

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
	        view = (View) layoutInflater.inflate(com.aurora.R.layout.aurora_slid_listview, arg2, false);
	        RelativeLayout front = (RelativeLayout)view.findViewById(com.aurora.R.id.aurora_listview_front);
			View ret = layoutInflater.inflate(R.layout.auroraworldtimelist, front);
		}
		LinearLayout linear = (LinearLayout)view.findViewById(com.aurora.R.id.content);	
		 linear.setBackgroundResource(R.drawable.list);
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
		
		/*
		if (mark == 2) {
			ImageView sortImageView = (ImageView) view
					.findViewById(R.id.iv_world_time_sort_hot);
			sortImageView.setVisibility(View.VISIBLE);
		}
		*/

		return view;
	}

	/*
	 * time ui
	 */
	public void UpdateTime(int position, View view) {
		dayornightView = (ImageView)view.findViewById(R.id.dayornight);
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
			dayornightView.setImageResource(R.drawable.day);
		} else {
			nalogClock.setClockImage(R.drawable.world_time_clock_dial_white,
					R.drawable.world_time_clock_hour_black_white, R.drawable.world_time_clock_minute_black_white);
			dayornightView.setImageResource(R.drawable.night);
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

		//aurora mod by tangjun 2014.1.3 start 改为显示GMT
		//mYear.setText(year);
		Log.e("---position = " + position);
		Log.e("---getGmt = " + mWorldTimeList.get(position).getGmt());
		mYear.setText(getMoreGMT( mWorldTimeList.get(position).getGmt() ) + " " + mWorldTimeList.get(position).getGmt());
		//aurora mod by tangjun 2014.1.3 end
	}
	
	/**
	 * @param string 	获取早晚时间 aurora add by tangjun 2014.2.17
	 * @return
	 */
	private String getMoreGMT( String string ) {
		Log.e("--**********************************************************************88 --" + string);
		boolean plusOrminus = (string.charAt(3) == '+') ? true : false;
		String tmpString1 = null;
		String tmpString2 = null;
		int tmpflag = 0xffff;
		
		//Log.e("--string = --" + string);
		for (int i = 4; i < string.length(); i++ ) {
			//Log.e("--string.charAt(i) = --" + string.charAt(i));
			if ( string.charAt(i) == ':') {
				tmpflag = i;
				tmpString1 = string.substring(4, tmpflag);
			}
			if ( i > tmpflag ) {
				tmpString2 = string.substring(i, i + 2);
				break;
			}
		}
		//Log.e("--11tmpString1 = --" + tmpString1);
		//Log.e("--11tmpString2 = --" + tmpString2);
		int count = Integer.parseInt(tmpString1) * 60 + Integer.parseInt(tmpString2);
		int dis = 0;
		
		if( plusOrminus ) {
			dis = this.mCount - count;
		} else {
			dis = this.mCount + count;
		}
		
		if ( dis > 0 ) {
			if ( dis % 60 != 0 ) {
				tmpString1 = contexts.getString(R.string.worldtimelate1, dis / 60, dis % 60);
			} else {
				tmpString1 = contexts.getString(R.string.worldtimelate2, dis / 60);
			}
		} else if ( dis < 0 ) {
			dis = Math.abs(dis);
			if ( dis % 60 != 0 ) {
				tmpString1 = contexts.getString(R.string.worldtimeearly1, dis / 60, dis % 60);
			} else {
				tmpString1 = contexts.getString(R.string.worldtimeearly2, dis / 60);
			}
		} else {
			tmpString1 = contexts.getString(R.string.worldtimesame);
		}
		
		//Log.e("--tmpString1 = --" + tmpString1);
		return tmpString1;
		
	}

	/*
	 * receiver broadcast , refresh UI
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			if ( intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED) ) {
				mCount = getGMTCount(getTimeZoneText());
			}
			handler.sendEmptyMessage(0);
		}
	};

	private Handler handler  = new Handler(){
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
	
	/**
	 * @param string 	获取GMT时间统计 aurora add by tangjun 2014.2.17
	 * @return
	 */
	private int getGMTCount( String string ) {
		boolean plusOrminus = (string.charAt(3) == '+') ? true : false;
		String tmpString1 = null;
		String tmpString2 = null;
		int tmpflag = 0xffff;
		int count = 0;
		
		Log.e("--string = --" + string);
		for (int i = 4; i < string.length(); i++ ) {
			//Log.e("--string.charAt(i) = --" + string.charAt(i));
			if ( string.charAt(i) == ':') {
				tmpflag = i;
				tmpString1 = string.substring(4, tmpflag);
			}
			if ( i > tmpflag ) {
				tmpString2 = string.substring(i, i + 2);
				break;
			}
		}
		Log.e("--11tmpString1 = --" + tmpString1);
		Log.e("--11tmpString2 = --" + tmpString2);
		count = Integer.parseInt(tmpString1) * 60 + Integer.parseInt(tmpString2);
		
		if ( !plusOrminus ) {
			count = -count;
		}
		
		return count;
		
	}
	
	/**
	 * @return 得到GMT字符串 aurora add by tangjun 2014.2.19
	 */
	private String getTimeZoneText( ) {
		TimeZone tz = Calendar.getInstance().getTimeZone();
		// Similar to new SimpleDateFormat("'GMT'Z, zzzz").format(new Date()), but
		// we want "GMT-03:00" rather than "GMT-0300".
		Date now = new Date();
		return formatOffset(new StringBuilder(), tz, now).toString();
	}

	private StringBuilder formatOffset(StringBuilder sb, TimeZone tz, Date d) {
		int off = tz.getOffset(d.getTime()) / 1000 / 60;

		sb.append("GMT");
		if (off < 0) {
			sb.append('-');
			off = -off;
		} else {
			sb.append('+');
		}

		int hours = off / 60;
		int minutes = off % 60;

		sb.append((char) ('0' + hours / 10));
		sb.append((char) ('0' + hours % 10));

		sb.append(':');

		sb.append((char) ('0' + minutes / 10));
		sb.append((char) ('0' + minutes % 10));

		return sb;
	}
}
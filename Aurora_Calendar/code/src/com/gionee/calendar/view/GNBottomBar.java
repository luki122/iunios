package com.gionee.calendar.view;

import java.util.Timer;
import java.util.TimerTask;

import com.android.calendar.AllInOneActivity;
import com.android.calendar.CalendarController;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController.ViewType;
import com.android.calendar.R;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;


import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

//Gionee <jiating>  <2013-04-11> modify for CR00000000 mainView  bottom view change begin
public class GNBottomBar extends LinearLayout {

	private LinearLayout mBottomBar;
	private ImageButton mDayButton;
	private ImageButton mWeekButton;
	private ImageButton mMonthButton;
	private Paint mPaint;
	public Timer timer = null;
	public MyTimeTask task = null;
	private CalendarController mController;
	private Context mContext;
	public GNBottomBar(Context context) {
		this(context, null);
		mContext = context;
	}

	public GNBottomBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mController = CalendarController.getInstance(context);
		setWillNotDraw(false);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);

		LayoutInflater.from(context).inflate(
				R.layout.gn_all_in_one_view_change_bottom_bar, this, true);

		mBottomBar = (LinearLayout) findViewById(R.id.gn_bottom_bar);

		mDayButton = (ImageButton) findViewById(R.id.gn_day_button);
		mWeekButton = (ImageButton) findViewById(R.id.gn_week_button);
		mMonthButton = (ImageButton) findViewById(R.id.gn_month_button);
		mBottomBar.setVisibility(View.GONE);

		mDayButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
				switch (AllInOneActivity.mCurrentView) {
				case ViewType.WEEK:
//					Statistics.onEvent(mContext, Statistics.WEEK_VIEW_GOTO_DAY_VIEW);
					Statistics.WEEK_VIEW_GOTO_DAY_VIEW_NUM++;
					break;
				case ViewType.MONTH:
//					Statistics.onEvent(mContext, Statistics.MONTH_VIEW_GOTO_DAY_VIEW);
					Statistics.MONTH_VIEW_GOTO_DAY_VIEW_NUM++;
					break;
				default:
					break;
				}
				//Gionee <pengwei><2013-05-20> modify for CR00813693 end
				mController.sendEvent(this, EventType.GO_TO, null, null, -1,
						ViewType.DAY);
				resetBottomBackground(ViewType.DAY);
				setButtonEnabled(false);
				startEnableTimer();
			}
		});

		mWeekButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
				switch (AllInOneActivity.mCurrentView) {
				case ViewType.DAY:
//					Statistics.onEvent(mContext, Statistics.DAY_VIEW_GOTO_WEEK_VIEW);
					Statistics.DAY_VIEW_GOTO_WEEK_VIEW_NUM++;
					break;
				case ViewType.MONTH:
//					Statistics.onEvent(mContext, Statistics.MONTH_VIEW_GOTO_WEEK_VIEW);
					Statistics.MONTH_VIEW_GOTO_WEEK_VIEW_NUM++;
					break;
				default:
					break;
				}
				//Gionee <pengwei><2013-05-20> modify for CR00813693 end
				mController.sendEvent(this, EventType.GO_TO, null, null, -1,
						ViewType.WEEK);
				resetBottomBackground(ViewType.WEEK);
				setButtonEnabled(false);
				startEnableTimer();
			}
		});

		mMonthButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
				switch (AllInOneActivity.mCurrentView) {
				case ViewType.WEEK:
//					Statistics.onEvent(mContext, Statistics.WEEK_VIEW_GOTO_MONTH_VIEW);
					Statistics.WEEK_VIEW_GOTO_MONTH_VIEW_NUM++;
					break;
				case ViewType.DAY:
//					Statistics.onEvent(mContext, Statistics.DAY_VIEW_GOTO_MONTH_VIEW);
					Statistics.DAY_VIEW_GOTO_MONTH_VIEW_NUM++;
					break;
				default:
					break;
				}
				//Gionee <pengwei><2013-05-20> modify for CR00813693 end
				mController.sendEvent(this, EventType.GO_TO, null, null, -1,
						ViewType.MONTH);
				resetBottomBackground(ViewType.MONTH);
				setButtonEnabled(false);
				startEnableTimer();
			}
		});

	}

	
	@SuppressWarnings("deprecation")
	public void resetBottomBackground(int viewType) {
		switch (viewType) {

		case ViewType.DAY:

			mDayButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_day_view_on));
             
			mWeekButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_week_view_off));
			mMonthButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_month_view_off));
			break;
		case ViewType.WEEK:

			mDayButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_day_view_off));
             
			mWeekButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_week_view_on));
			mMonthButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_month_view_off));
			break;
		case ViewType.MONTH:

			mDayButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_day_view_off));
             
			mWeekButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_week_view_off));
			mMonthButton.setBackground(getResources().getDrawable(
					R.drawable.gn_all_in_one_bottom_month_view_on));
			break;
		default:
			break;
		}
	}

	public void setButtonEnabled(boolean enabled) {
		mDayButton.setEnabled(enabled);
		mWeekButton.setEnabled(enabled);
		mMonthButton.setEnabled(enabled);
	}

	private void startEnableTimer() {
		timer = new Timer();
		task = new MyTimeTask();
		timer.schedule(task, 200);
	}

	public void cancelTask() {
		if (null != task) {
			task.cancel();
			task = null;
		}
		if (null != timer) {
			timer.cancel();
			timer = null;
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 3:
				setButtonEnabled(true);
				cancelTask();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public class MyTimeTask extends TimerTask {

		@Override
		public void run() {
			Message message = new Message();
			message.what = 3;
			handler.sendMessage(message);
		}
	}

}
//Gionee <jiating>  <2013-04-11> modify for CR00000000 mainView bottom view change end
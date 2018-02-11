package com.gionee.calendar.day;

import com.gionee.almanac.GNAlmanacConstants;


import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController;
import com.android.calendar.R;
//Gionee <pengwei><20130719> modify for CR00838262 begin
//Gionee <pengwei><20130809> modify for CR00853574 begin
public class AlmanacView implements DayScheduleInterface{
	private Context mContext;
	private String mFitting;
	private String mUnFitting;
	private int mElement;
	private LayoutInflater mInflater;
	
	public AlmanacView(Context context,String fitting,String unfitting,int element) {
		// TODO Auto-generated constructor stub
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.mFitting = fitting;
		this.mUnFitting = unfitting;
		this.mElement = element;
	}
	
	@Override
	public View getView(View view) {
		// TODO Auto-generated method stub
		view = mInflater.inflate(R.layout.gn_day_almanac_list_item, null);
		ImageView fittingView = (ImageView) view.findViewById(R.id.gn_day_almanac_fitting);
		ImageView unFittingView = (ImageView) view.findViewById(R.id.gn_day_almanac_unfitting);
		TextView fittingText= (TextView) view.findViewById(R.id.gn_day_almanac_fitting_text);
		TextView unFittingText = (TextView) view.findViewById(R.id.gn_day_almanac_unfitting_text);
		view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    if (DayUtils.isFastDoubleClick()) {
			        return;
			    }
				CalendarController controller = CalendarController
						.getInstance(mContext);
				// Gionee <jiangxiao> <2013-07-23> modify for CR00837096 begin
				// provide current day view date to almanac activity
				controller.sendEvent(this, EventType.LAUNCH_ALMANAC, mQueryDate, null, 0, 0);
				// Gionee <jiangxiao> <2013-07-23> modify for CR00837096 begin
			}
		});
		switch (mElement) {
		case GNAlmanacConstants.FIVE_ELEMENTS_FIRE:
			fittingView.setImageResource(R.drawable.gn_almanac_yi_icon_huo);
			unFittingView.setImageResource(R.drawable.gn_almanac_ji_icon_huo);
			break;
		case GNAlmanacConstants.FIVE_ELEMENTS_WATER:
			fittingView.setImageResource(R.drawable.gn_almanac_yi_icon_shui);
			unFittingView.setImageResource(R.drawable.gn_almanac_ji_icon_shui);
			break;
		case GNAlmanacConstants.FIVE_ELEMENTS_WOOD:
			fittingView.setImageResource(R.drawable.gn_almanac_yi_icon_mu);
			unFittingView.setImageResource(R.drawable.gn_almanac_ji_icon_mu);
			break;
		case GNAlmanacConstants.FIVE_ELEMENTS_SOIL:
			fittingView.setImageResource(R.drawable.gn_almanac_yi_icon_tu);
			unFittingView.setImageResource(R.drawable.gn_almanac_ji_icon_tu);
			break;
		case GNAlmanacConstants.FIVE_ELEMENTS_METAL:
			fittingView.setImageResource(R.drawable.gn_almanac_yi_icon_jin);
			unFittingView.setImageResource(R.drawable.gn_almanac_ji_icon_jin);
			break;
		default:
			break;
		}
		fittingText.setText(mFitting);
		if(mUnFitting == null ||"".equals(mUnFitting)){
			unFittingText.setText(mContext.getResources().getString(R.string.almanac_today_bad_default));
		}else{
			unFittingText.setText(mUnFitting);
		}
		return view;
	}

	// Gionee <jiangxiao> <2013-07-23> add for CR00837096 begin
	private Time mQueryDate = new Time();

	public void setQueryDate(Time t) {
		mQueryDate.set(t);
	}
	// Gionee <jiangxiao> <2013-07-23> add for CR00837096 end
}
//Gionee <pengwei><20130719> modify for CR00838262 end
//Gionee <pengwei><20130809> modify for CR00853574 end
package com.gionee.horoscope;
//Gionee <jiating><2013-05-29> modify for CR00000000 begin 
//Gionee <pengwei><2013-06-07> modify for CR00000000 begin
import aurora.app.AuroraActivity;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.calendar.R;
import com.gionee.astro.GNAstroUtils;
import com.gionee.astro.GNAstroUtils.DayAstroInfo;
import com.gionee.calendar.day.AstroView.ViewHolder;
import com.gionee.calendar.view.Log;
import com.gionee.astro.GNAstroUtils.WeekAstroInfo;
import android.text.method.ScrollingMovementMethod;
public class GNHoroscopeWeekFragment  extends Fragment{
	
	private View view;
	private Context context;
	private Long selectTime;
	private TextView   hocosposeContent;
	private TextView  hocosposeNoContent;
	private RelativeLayout mProgressBarView;
	private View hocosposeUpContentView;
	private View mImageDiver;
	private View tengxunLog;
    private boolean isFirst=true;
    private boolean isFailed=false;
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.v("GNHoroscopeTodayFragment...onAttach---");
		context=activity;
	
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		isFirst=true;
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view=inflater.inflate(R.layout.gn_hocospose_week_item, null);
		
//Gionee <jiating><2013-06-20> modify for CR00827812 begin		
		Log.v("GNHoroscopeTodayFragment...onCreateView---view == null---");
		hocosposeContent= (TextView) view.findViewById(R.id.hocospose_content);
//		hocosposeContent.setMovementMethod(ScrollingMovementMethod.getInstance()) ;
//Gionee <jiating><2013-06-20> modify for CR00827812 end
	
		hocosposeNoContent=(TextView) view.findViewById(R.id.no_content);
		mProgressBarView=(RelativeLayout) view.findViewById(R.id.hocospose_info_loading_msg);
		tengxunLog=view.findViewById(R.id.tengxun_logo);
		tengxunLog.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				    Uri uri = Uri.parse("http://astro.lady.qq.com/");  
				    Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
				    intent.addCategory(Intent.CATEGORY_BROWSABLE);
				    intent.setPackage("com.android.browser");
				    startActivity(intent);
				
			}
		});
		setViewState();
		return view;
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		setViewState();
		Log.v("GNHoroscopeWeekFragment...onResume---");
	}

	private void setViewState(){
		if(isFirst|| GNHoroscopeActivity.isChangeHoroscope()){
			mProgressBarView.setVisibility(View.VISIBLE);
			hocosposeContent.setVisibility(View.GONE);
			hocosposeNoContent.setVisibility(View.GONE);
//			tengxunLog.setVisibility(View.GONE);
			
		}else if(isFailed){
			mProgressBarView.setVisibility(View.GONE);
			hocosposeContent.setVisibility(View.GONE);
			hocosposeNoContent.setVisibility(View.VISIBLE);
//			tengxunLog.setVisibility(View.GONE);
		}else{
			mProgressBarView.setVisibility(View.GONE );
			hocosposeContent.setVisibility(View.VISIBLE);
			hocosposeNoContent.setVisibility(View.GONE);
//			tengxunLog.setVisibility(View.VISIBLE);
		}
	}
	
	public void setViewData(Message msg) {
		isFirst=false;
		GNHoroscopeActivity.setChangeHoroscope(false);
		WeekAstroInfo weekAstroInfo = (WeekAstroInfo)msg.obj;
		try {
			Log.i("GNHoroscopeWeekragment...setViewData"+msg.arg1);
			if (msg.arg1 == GNAstroUtils.GET_ASTRO_STATUS_OK) {
			Log.i("GNHoroscopeWeekragment...setViewData" + weekAstroInfo.fortuneContent[0]);
				hocosposeContent.setText(weekAstroInfo.fortuneContent[0]);
				isFailed=false;
			
			}else{
				Log.i("GNHoroscopeWeekragment...setViewData...failed");
				isFailed = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
			isFailed=true;
			Log.v("GNHoroscopeTodayFragment---getView---e == " + e);
		}finally{
			setViewState();
		}
		
		
	}
	

	

}
//Gionee <pengwei><2013-06-07> modify for CR00000000 end
//Gionee <jiating><2013-05-29> modify for CR00000000 end
package com.gionee.horoscope;

//Gionee <jiating><2013-05-29> modify for CR00000000 begin 
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
import android.text.method.ScrollingMovementMethod;
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

public class GNHoroscopeTomorowFragment extends Fragment {

	private View view;
	private Context context;
	private Long selectTime;
	public ImageView astroImg;
	public RatingBar astroLuckIndexRat;
	public RatingBar healthIndexRat;
	public RatingBar workIndexRat;
	public RatingBar loveIndexRat;
	public RatingBar wealthIndexRat;
	public TextView luckyColorText;
	public TextView luckyNumberText;
	public TextView speedDatingText;
	private TextView hocosposeContent;
	private TextView hocosposeNoContent;
	private RelativeLayout mProgressBarView;
	private View hocosposeUpContentView;
	private View mImageDiver;
	private View tengxunLog;
	private boolean isFirst = true;
	private boolean isFailed = false;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		Log.v("GNHoroscopeTomorowFragment...onAttach---");
		context = activity;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFirst=true;
		Log.v("GNHoroscopeTomorowFragment...onCreate---");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.gn_hocospose_today_item, null);

		Log.v("GNHoroscopeTomorowFragment...onCreateView---view == null---");

		astroLuckIndexRat = (RatingBar) view
				.findViewById(R.id.constellation_comprehensive_luck_ratingbar);
		healthIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_health_index_ratingbar);
		workIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_work_index_ratingbar);
		loveIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_love_index_ratingbar);
		wealthIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_wealth_index_ratingbar);
		luckyColorText = (TextView) view
				.findViewById(R.id.gn_day_constellation_lucky_color_view);
		luckyNumberText = (TextView) view
				.findViewById(R.id.gn_day_constellation_lucky_numbers_view);
		speedDatingText = (TextView) view
				.findViewById(R.id.gn_day_constellation_speed_dating_constellation_view);
		hocosposeContent = (TextView) view.findViewById(R.id.hocospose_content);
		// Gionee <jiating><2013-06-20> modify for CR00827812 begin 
		hocosposeContent.setMovementMethod(ScrollingMovementMethod
				.getInstance());
		// Gionee <jiating><2013-06-20> modify for CR00827812 end
		hocosposeNoContent = (TextView) view.findViewById(R.id.no_content);
		mProgressBarView = (RelativeLayout) view
				.findViewById(R.id.hocospose_info_loading_msg);
		hocosposeUpContentView = view.findViewById(R.id.hocospose_up_content);
		mImageDiver = view.findViewById(R.id.gn_deit_reminder_item_diver);
		tengxunLog = view.findViewById(R.id.tengxun_logo);
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
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		setViewState();
		Log.v("GNHoroscopeTomorowFragment...onResume---");
	}

	private void setViewState() {
		Log.v("GNHoroscopeTomorowFragment...setViewState---.."+"isFirst="+isFirst+"isFailed="+isFailed+"GNHoroscopeActivity.isChangeHoroscope()="+GNHoroscopeActivity.isChangeHoroscope());

		if (isFirst || GNHoroscopeActivity.isChangeHoroscope()) {
			mProgressBarView.setVisibility(View.VISIBLE);
			hocosposeContent.setVisibility(View.GONE);
			hocosposeNoContent.setVisibility(View.GONE);
			hocosposeUpContentView.setVisibility(View.GONE);
			mImageDiver.setVisibility(View.GONE);
//			tengxunLog.setVisibility(View.GONE);
		}else if(isFailed){

			mProgressBarView.setVisibility(View.GONE);
			hocosposeContent.setVisibility(View.GONE);
			hocosposeNoContent.setVisibility(View.VISIBLE);
			hocosposeUpContentView.setVisibility(View.GONE);
			mImageDiver.setVisibility(View.GONE);
//			tengxunLog.setVisibility(View.GONE);
		} else {
			mProgressBarView.setVisibility(View.GONE);
			hocosposeContent.setVisibility(View.VISIBLE);
			hocosposeNoContent.setVisibility(View.GONE);
			hocosposeUpContentView.setVisibility(View.VISIBLE);
			mImageDiver.setVisibility(View.VISIBLE);
//			tengxunLog.setVisibility(View.VISIBLE);
		}
	}

	public void setViewData(Message msg) {
		isFirst = false;
		GNHoroscopeActivity.setChangeHoroscope(false);
		DayAstroInfo dayAstroInfo = (DayAstroInfo) msg.obj;
		try {
			Log.i("GNHoroscopeTomorowFragment...setViewDatadayAstroInfo="
					+ (dayAstroInfo == null));

			if (msg.arg1 == GNAstroUtils.GET_ASTRO_STATUS_OK) {
				astroLuckIndexRat
						.setRating(dayAstroInfo.indexComprehensionStar);
				healthIndexRat.setRating(dayAstroInfo.indexHealthStar);
				workIndexRat.setRating(dayAstroInfo.indexCareerStar);
				loveIndexRat.setRating(dayAstroInfo.indexLoveStar);
				wealthIndexRat.setRating(dayAstroInfo.indexFinanceStar);
				speedDatingText.setText(dayAstroInfo.QFriend);
				luckyColorText.setText(dayAstroInfo.LuckyColor);
				luckyNumberText.setText(dayAstroInfo.luckyNumber);
				hocosposeContent.setText("    " + dayAstroInfo.description);
				isFailed = false;
			} else {
				isFailed = true;
				Log.i("GNHoroscopeTomorowFragment...setViewDatadayAstroInfo..getDataFailed");
			}
			setViewState();
		} catch (Exception e) {
			// TODO: handle exception
			isFailed = true;
			setViewState();
			Log.v("GNHoroscopeTomorowFragment---getView---e == " + e);
		}
	}

}
// Gionee <jiating><2013-05-29> modify for CR00000000 end
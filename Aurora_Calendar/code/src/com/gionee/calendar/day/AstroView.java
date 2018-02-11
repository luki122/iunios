package com.gionee.calendar.day;

import com.android.calendar.CalendarController.EventInfo;
import com.android.calendar.CalendarController.EventType;
import com.android.calendar.CalendarController;
import com.android.calendar.Event;
import com.android.calendar.EventInfoFragment;
import com.android.calendar.Utils;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.provider.CalendarContract.Attendees;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import com.android.calendar.R;
import com.gionee.astro.GNAstroUtils.DayAstroInfo;
import com.gionee.calendar.statistics.StatisticalName;
import com.gionee.calendar.statistics.Statistics;
import com.gionee.calendar.view.Log;
import com.gionee.astro.GNAstroUtils;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

//Gionee <pengwei>  <2013-04-12> modify for DayView begin
//Gionee <pengwei><2013-05-20> modify for CR00813693 begin
public class AstroView implements DayScheduleInterface {
	private LayoutInflater mInflater;
	private Context mContext;
	private DayAstroInfo dayAstroInfo;
	private String mAstroName;
	private int mPressColor;
	private int mNoPressColor;
	private int mItemPressColor;
	private int mItemNoPressColor;
	AstroView(Context context, DayAstroInfo dayAstroInfo, String astroName) {
		this.mInflater = LayoutInflater.from(context);
		this.dayAstroInfo = dayAstroInfo;
		this.mContext = context;
		this.mAstroName = astroName;
		mPressColor = mContext.getResources().getColor(R.color.gn_text_day_listview_item_time_and_line_press);
		mNoPressColor = mContext.getResources().getColor(R.color.gn_text_day_agenda_time);
		mItemPressColor = mContext.getResources().getColor(R.color.gn_text_day_listview_item_press);
		mItemNoPressColor = mContext.getResources().getColor(R.color.gn_text_day_listview_item_no_press);
	}

	@Override
	public View getView(View view) {
		// TODO Auto-generated method stub
		final ViewHolder holder;
		Log.v("ScheduleView---view == null---");
		view = mInflater.inflate(R.layout.gn_day_astro_list_item, null);
		holder = new ViewHolder();
		holder.linearLayout = (LinearLayout) view.findViewById(R.id.gn_day_astro_list_item);
		holder.astroImg = (ImageView) view.findViewById(R.id.astro_pic);
		holder.astroLuckIndexRat = (RatingBar) view
				.findViewById(R.id.constellation_comprehensive_luck_ratingbar);
		holder.astroLuckIndexRatLuckPress = (RatingBar) view
				.findViewById(R.id.constellation_comprehensive_luck_ratingbar_press);
		holder.astroLuckText = (TextView) view
				.findViewById(R.id.constellation_comprehensive_luck_text);
		holder.healthIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_health_index_ratingbar);
		holder.healthIndexRatPress = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_health_index_ratingbar_press);
		holder.workIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_work_index_ratingbar);
		holder.workIndexRatPress = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_work_index_ratingbar_press);
		holder.loveIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_love_index_ratingbar);
		holder.loveIndexRatPress = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_love_index_ratingbar_press);
		holder.wealthIndexRat = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_wealth_index_ratingbar);
		holder.wealthIndexRatPress = (RatingBar) view
				.findViewById(R.id.gn_day_constellation_wealth_index_ratingbar_press);
		holder.luckyColorText = (TextView) view
				.findViewById(R.id.gn_day_constellation_lucky_color_view);
		holder.luckyNumberText = (TextView) view
				.findViewById(R.id.gn_day_constellation_lucky_numbers_view);
		holder.speedDatingText = (TextView) view
				.findViewById(R.id.gn_day_constellation_speed_dating_constellation_view);
		holder.ComprehensiveLuckText = (TextView) view
		.findViewById(R.id.constellation_comprehensive_luck_text);
		holder.ComprehensiveWorkText = (TextView) view
		.findViewById(R.id.gn_day_constellation_work_index_text);
		holder.ComprehensiveLoveText = (TextView) view
		.findViewById(R.id.gn_day_constellation_love_index_text);
		holder.ComprehensiveHealthText = (TextView) view
		.findViewById(R.id.gn_day_constellation_health_index_text);
		holder.ComprehensiveWealthText = (TextView) view
		.findViewById(R.id.gn_day_constellation_wealth_index_text);
		holder.ComprehensivespeedText = (TextView) view
		.findViewById(R.id.gn_day_constellation_speed_dating_constellation_text);
		holder.ComprehensiveLuckyColorText = (TextView) view
		.findViewById(R.id.gn_day_constellation_lucky_color_text);
		holder.ComprehensiveLuckyNumberText = (TextView) view
		.findViewById(R.id.gn_day_constellation_lucky_numbers_text);
		try {
			int astroNameIndex = GNAstroUtils.getAstroIndexFromPref(mContext);
			String astroName = DayUtils.getAstro(mContext, astroNameIndex);
			Drawable drawable = DayUtils.getAstroPics(mContext, astroNameIndex);
			holder.astroImg.setImageDrawable(drawable);
			String luckText = mContext.getResources().getString(
					R.string.gn_day_constellation_comprehensive_luck);
			String astroLuckText = astroName + luckText;
			holder.astroLuckText.setText(astroLuckText);
			holder.astroLuckIndexRat
					.setRating(dayAstroInfo.indexComprehensionStar);
			holder.astroLuckIndexRatLuckPress
					.setRating(dayAstroInfo.indexComprehensionStar);
			holder.healthIndexRat.setRating(dayAstroInfo.indexHealthStar);
			holder.healthIndexRatPress.setRating(dayAstroInfo.indexHealthStar);
			holder.workIndexRat.setRating(dayAstroInfo.indexCareerStar);
			holder.workIndexRatPress.setRating(dayAstroInfo.indexCareerStar);
			holder.loveIndexRat.setRating(dayAstroInfo.indexLoveStar);
			holder.loveIndexRatPress.setRating(dayAstroInfo.indexLoveStar);
			holder.wealthIndexRat.setRating(dayAstroInfo.indexFinanceStar);
			holder.wealthIndexRatPress.setRating(dayAstroInfo.indexFinanceStar);
			holder.speedDatingText.setText(dayAstroInfo.QFriend);
			holder.luckyColorText.setText(dayAstroInfo.LuckyColor);
			holder.luckyNumberText.setText(dayAstroInfo.luckyNumber);
		} catch (Exception e) {
			// TODO: handle exception
			Log.v("AstroView---getView---e == " + e);
		}
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			    if (DayUtils.isFastDoubleClick()) {
			        return;
			    }
				CalendarController controller = CalendarController
						.getInstance(mContext);
				controller.sendEvent(mContext, EventType.LAUNCH_HOROSCOPE,
						null, null, 0, 0);
			}
		});
		view.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				// TODO Auto-generated method stub
				int action = event.getAction();
				switch (action) {
				case MotionEvent.ACTION_DOWN:
					holder.astroLuckIndexRat.setVisibility(View.GONE);
					holder.astroLuckIndexRatLuckPress
							.setVisibility(View.VISIBLE);
					holder.healthIndexRat.setVisibility(View.GONE);
					holder.healthIndexRatPress.setVisibility(View.VISIBLE);
					holder.workIndexRat.setVisibility(View.GONE);
					holder.workIndexRatPress.setVisibility(View.VISIBLE);
					holder.loveIndexRat.setVisibility(View.GONE);
					holder.loveIndexRatPress.setVisibility(View.VISIBLE);
					holder.wealthIndexRat.setVisibility(View.GONE);
					holder.wealthIndexRatPress.setVisibility(View.VISIBLE);
					
					holder.linearLayout.setBackgroundResource(R.drawable.gn_content_single_bg_on);
					holder.ComprehensiveLuckText.setTextColor(mPressColor);
					holder.ComprehensiveWorkText.setTextColor(mPressColor);
					holder.ComprehensiveLoveText.setTextColor(mPressColor);
					holder.ComprehensiveHealthText.setTextColor(mPressColor);
					holder.ComprehensiveWealthText.setTextColor(mPressColor);
					holder.ComprehensivespeedText.setTextColor(mPressColor);
					holder.ComprehensiveLuckyColorText.setTextColor(mPressColor);
					holder.ComprehensiveLuckyNumberText.setTextColor(mPressColor);
					holder.astroLuckText.setTextColor(mPressColor);
					holder.luckyColorText.setTextColor(mPressColor);
					holder.luckyNumberText.setTextColor(mPressColor);
					holder.speedDatingText.setTextColor(mPressColor);

					break;
				case MotionEvent.ACTION_UP:
					holder.astroLuckIndexRat.setVisibility(View.VISIBLE);
					holder.astroLuckIndexRatLuckPress.setVisibility(View.GONE);
					holder.healthIndexRat.setVisibility(View.VISIBLE);
					holder.healthIndexRatPress.setVisibility(View.GONE);
					holder.workIndexRat.setVisibility(View.VISIBLE);
					holder.workIndexRatPress.setVisibility(View.GONE);
					holder.loveIndexRat.setVisibility(View.VISIBLE);
					holder.loveIndexRatPress.setVisibility(View.GONE);
					holder.wealthIndexRat.setVisibility(View.VISIBLE);
					holder.wealthIndexRatPress.setVisibility(View.GONE);
					holder.linearLayout.setBackgroundResource(R.drawable.gn_content_single_bg_on);
					holder.linearLayout.setBackgroundColor(mItemNoPressColor);
					holder.ComprehensiveLuckText.setTextColor(mNoPressColor);
					holder.ComprehensiveWorkText.setTextColor(mNoPressColor);
					holder.ComprehensiveLoveText.setTextColor(mNoPressColor);
					holder.ComprehensiveHealthText.setTextColor(mNoPressColor);
					holder.ComprehensiveWealthText.setTextColor(mNoPressColor);
					holder.ComprehensivespeedText.setTextColor(mNoPressColor);
					holder.ComprehensiveLuckyColorText.setTextColor(mNoPressColor);
					holder.ComprehensiveLuckyNumberText.setTextColor(mNoPressColor);
					holder.astroLuckText.setTextColor(mNoPressColor);
					holder.luckyColorText.setTextColor(mNoPressColor);
					holder.luckyNumberText.setTextColor(mNoPressColor);
					holder.speedDatingText.setTextColor(mNoPressColor);

					break;
				case MotionEvent.ACTION_CANCEL:
					holder.astroLuckIndexRat.setVisibility(View.VISIBLE);
					holder.astroLuckIndexRatLuckPress.setVisibility(View.GONE);
					holder.healthIndexRat.setVisibility(View.VISIBLE);
					holder.healthIndexRatPress.setVisibility(View.GONE);
					holder.workIndexRat.setVisibility(View.VISIBLE);
					holder.workIndexRatPress.setVisibility(View.GONE);
					holder.loveIndexRat.setVisibility(View.VISIBLE);
					holder.loveIndexRatPress.setVisibility(View.GONE);
					holder.wealthIndexRat.setVisibility(View.VISIBLE);
					holder.wealthIndexRatPress.setVisibility(View.GONE);
					
					holder.linearLayout.setBackgroundColor(mItemNoPressColor);
					holder.ComprehensiveLuckText.setTextColor(mNoPressColor);
					holder.ComprehensiveWorkText.setTextColor(mNoPressColor);
					holder.ComprehensiveLoveText.setTextColor(mNoPressColor);
					holder.ComprehensiveHealthText.setTextColor(mNoPressColor);
					holder.ComprehensiveWealthText.setTextColor(mNoPressColor);
					holder.ComprehensivespeedText.setTextColor(mNoPressColor);
					holder.ComprehensiveLuckyColorText.setTextColor(mNoPressColor);
					holder.ComprehensiveLuckyNumberText.setTextColor(mNoPressColor);
					holder.astroLuckText.setTextColor(mNoPressColor);
					holder.luckyColorText.setTextColor(mNoPressColor);
					holder.luckyNumberText.setTextColor(mNoPressColor);
					holder.speedDatingText.setTextColor(mNoPressColor);
					break;
				default:
					break;
				}
				return false;
			}
		});

		return view;
	}

	public final class ViewHolder {
		public LinearLayout linearLayout;
		public ImageView astroImg;
		public TextView ComprehensiveLuckText;
		public TextView ComprehensiveWorkText;
		public TextView ComprehensiveLoveText;
		public TextView ComprehensiveHealthText;
		public TextView ComprehensiveWealthText;
		public TextView ComprehensivespeedText;
		public TextView ComprehensiveLuckyColorText;
		public TextView ComprehensiveLuckyNumberText;
		public RatingBar astroLuckIndexRat;
		public RatingBar astroLuckIndexRatLuckPress;
		public TextView astroLuckText;
		public RatingBar healthIndexRat;
		public RatingBar healthIndexRatPress;
		public RatingBar workIndexRat;
		public RatingBar workIndexRatPress;
		public RatingBar loveIndexRat;
		public RatingBar loveIndexRatPress;
		public RatingBar wealthIndexRat;
		public RatingBar wealthIndexRatPress;
		public TextView luckyColorText;
		public TextView luckyNumberText;
		public TextView speedDatingText;
	}

}
// Gionee <pengwei><2013-05-20> modify for CR00813693 end
// Gionee <pengwei> <2013-04-12> modify for DayView end
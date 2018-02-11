package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.popupview.BasePopupView;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.OnSurelistener;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;

public class BubbleAirTable extends UIPart {

	private List<String> mFlightList = new ArrayList<String>();
	private TextView mDuoquBaseDepartCity;
	private TextView mDuoquBaseArriveCity;
	private TextView mDuoquBaseDepartAirport;
	private TextView mDuoquBaseArriveAirport;
	private TextView mDuoquBaseDepartTime;
	private TextView mDuoquBaseArriveTime;
	private TextView mDuoquBaseDepartDate;
	private TextView mDuoquBaseArriveDate;
	private TextView mDuoquBaseFlightNumber;
	private TextView mGO;
	private ImageView mImgLine;
	private ImageView mDuoquBaseSelectFlightNumber;
	int flightSize = 0;

	public BubbleAirTable(Activity context, BusinessSmsMessage message,
			XyCallBack callback, int layoutId, ViewGroup root, int partId) {
		super(context, message, callback, layoutId, root, partId);
	}

	@Override
	public void initUi() {

		// TODO Auto-generated method stub
		mDuoquBaseDepartCity = (TextView) super.mView
				.findViewById(R.id.duoqu_base_depart_city);
		mDuoquBaseArriveCity = (TextView) super.mView
				.findViewById(R.id.duoqu_base_arrive_city);
		mDuoquBaseDepartAirport = (TextView) super.mView
				.findViewById(R.id.duoqu_base_depart_airport);
		mDuoquBaseArriveAirport = (TextView) super.mView
				.findViewById(R.id.duoqu_base_arrive_airport);
		mDuoquBaseDepartTime = (TextView) super.mView
				.findViewById(R.id.duoqu_base_depart_time);
		mDuoquBaseArriveTime = (TextView) super.mView
				.findViewById(R.id.duoqu_base_arrive_time);
		mDuoquBaseDepartDate = (TextView) super.mView
				.findViewById(R.id.duoqu_base_depart_date);
		mDuoquBaseArriveDate = (TextView) super.mView
				.findViewById(R.id.duoqu_base_arrive_date);
		mDuoquBaseFlightNumber = (TextView) super.mView
				.findViewById(R.id.duoqu_base_flight_number);
		mGO = (TextView) super.mView
		        .findViewById(R.id.duoqu_base_go);
		mImgLine = (ImageView) super.mView
		        .findViewById(R.id.duoqu_img_line);
		mDuoquBaseSelectFlightNumber = (ImageView) super.mView
				.findViewById(R.id.duoqu_base_select_flight_number);
	}

	@Override
	public void setContent(BusinessSmsMessage message, boolean isRebind)
			throws Exception {
		this.mMessage = message;
		ContentUtil.setViewVisibility(super.mView, View.VISIBLE);
		
		ViewManger.setViewBg(mContext, mView,
                message.getImgNameByKey("v_by_bg"),
                R.drawable.duoqu_rectangledrawble, 1);
        
        //内容全字体颜色
        String contentColor=(String) message.getValue("v_by_color");
        if (!StringUtils.isNull(contentColor)) {
            
            ContentUtil.textSetColor(mDuoquBaseDepartCity, contentColor);
            ContentUtil.textSetColor(mDuoquBaseArriveCity, contentColor);
            ContentUtil.textSetColor(mDuoquBaseDepartAirport, contentColor);
            ContentUtil.textSetColor(mDuoquBaseArriveAirport, contentColor);
            ContentUtil.textSetColor(mDuoquBaseDepartTime, contentColor);
            ContentUtil.textSetColor(mDuoquBaseArriveTime, contentColor);
            ContentUtil.textSetColor(mDuoquBaseDepartDate, contentColor);
            ContentUtil.textSetColor(mDuoquBaseArriveDate, contentColor);
            ContentUtil.textSetColor(mDuoquBaseFlightNumber, contentColor);
        }else {
			mDuoquBaseDepartCity.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseArriveCity.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseDepartAirport.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseArriveAirport.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseDepartTime.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseArriveTime.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseDepartDate.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDuoquBaseFlightNumber.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
        }
        
        //内容图标字体颜色
        String directionTextColor=(String) message.getValue("v_by_mid_color");
        if (!StringUtils.isNull(directionTextColor)) {
            mGO.setTextColor(ResourceCacheUtil.parseColor(directionTextColor));
        }else {
            mGO.setTextColor(mView.getResources().getColor(
					R.color.duoqu_mark_text_color));
        }
        
        //方向箭头
        String centerIconLogo= (String) message.getImgNameByKey("v_center_icon");
        if (!StringUtils.isNull(centerIconLogo)) {
            ViewManger.setViewBg(Constant.getContext(), mImgLine, centerIconLogo,
                    0, -1, true);
        }else {
        	mImgLine.setBackgroundResource(R.drawable.duoqu_train_directions);
		}
        
        //下拉选择按钮
		String iconLogo = (String) message.getImgNameByKey("v_by_icon_1");
		if (!StringUtils.isNull(iconLogo)) {
			ViewManger.setViewBg(Constant.getContext(),
					mDuoquBaseSelectFlightNumber, iconLogo, 0, -1, true);
		} else {
			mDuoquBaseSelectFlightNumber
					.setBackgroundResource(R.drawable.duoqu_air_more);
		}
        
		bindFightNumSelect(message);

		bindAirTickeInfo(message);

		changeCardBodySplitHeight();

	}

	private void changeCardBodySplitHeight() {
		HashMap<String, Object> param = new HashMap<String, Object>();
		param.put("showDashing", false);
		param.put("type", 3);
		if (mBasePopupView != null) {
			mBasePopupView.changeData(param);
		}

	}

	private void bindAirTickeInfo(final BusinessSmsMessage smsMessage) {
		String fightNumArr = (String) smsMessage
				.getValue("view_fight_number_list");
		if (StringUtils.isNull(fightNumArr))
			return;

		String viewContentKey = "View_air_content_0";
		String dataIndex = (String) smsMessage.getValue("db_air_data_index");
		if (!StringUtils.isNull(dataIndex)) {
			viewContentKey = "View_air_content_" + dataIndex;
		}
		JSONObject fightNumInfoJSONArr = (JSONObject) smsMessage
				.getValue(viewContentKey);
		if (fightNumInfoJSONArr == null) {
			return;
		}
		setText(fightNumInfoJSONArr);
		// }

	}

	private void bindFightNumSelect(BusinessSmsMessage message) {
		String fightNumArr = (String) message
				.getValue("view_fight_number_list");
		if (StringUtils.isNull(fightNumArr))
			return;
		String[] fightNums = fightNumArr.split(";");
		if (fightNums == null || fightNums.length == 0) {
			return;
		} else if (fightNums.length == 1) {
			showSelectFightNumImageView(false, mDuoquBaseFlightNumber);
		} else if (fightNums.length > 1) {
			showSelectFightNumImageView(true, mDuoquBaseFlightNumber);
		}

		mFlightList = Arrays.asList(fightNums);

		if (mFlightList != null && !mFlightList.isEmpty()) {
			flightSize = mFlightList.size();
		}

		/**
		 * 有多个航班的情况
		 */
		if (mFlightList != null && flightSize >= 2
				&& message.messageBody.indexOf("单程") == -1) {

			// 动态设置下拉三角形所在的位置
			RelativeLayout.LayoutParams params = (LayoutParams) mDuoquBaseFlightNumber
					.getLayoutParams();
			params.addRule(RelativeLayout.LEFT_OF, 0);
			mDuoquBaseFlightNumber.setLayoutParams(params);
			mDuoquBaseSelectFlightNumber.setVisibility(View.VISIBLE);//
			// 显示下拉列表
			mDuoquBaseSelectFlightNumber.setOnClickListener(onclickChange);
			mDuoquBaseFlightNumber.setOnClickListener(onclickChange);

			// 暂时不切换
			// mduoqu_base_select_flight_number.setVisibility(View.GONE);
			// mduoqu_base_flight_number.setOnClickListener(null);
		} else {
			mDuoquBaseSelectFlightNumber.setVisibility(View.GONE);// 影藏下拉列表
			mDuoquBaseSelectFlightNumber.setOnClickListener(null);
			mDuoquBaseFlightNumber.setOnClickListener(null);
		}
	}

	PopupWindow pop = null;
	// 单击事件
	public OnClickListener onclickChange = new OnClickListener() {

		LinearLayout ly = null;

		@Override
		public void onClick(View arg0) {
			// 获取selectImg的坐标
			final int[] location = new int[2];
			mDuoquBaseSelectFlightNumber.getLocationOnScreen(location);
			// 创建一个布局
//			if (pop == null) {
				ly = new LinearLayout(mContext);
				ly.setOrientation(LinearLayout.VERTICAL);
				LinearLayout.LayoutParams lyparam = new LinearLayout.LayoutParams(
						ViewUtil.dp2px(mContext, 104), ViewUtil.dp2px(mContext,
								30));
				LinearLayout.LayoutParams lyparamview = new LinearLayout.LayoutParams(
						ViewUtil.dp2px(mContext, 104), ViewUtil.dp2px(mContext,
								1));
				TextView tv = null;
				for (int i = 0; i < flightSize; i++) {
					// 创建textView
					tv = new TextView(mContext);
					ViewUtil.setTextViewValue(tv, mFlightList.get(i));
					tv.setTextColor(ResourceCacheUtil.parseColor("#FFFFFF"));
					tv.setTextSize(14);
					tv.setGravity(Gravity.CENTER_VERTICAL
							| Gravity.CENTER_HORIZONTAL);
					tv.setLayoutParams(lyparam);
					tv.setId(i);
					ly.addView(tv);

					if (i < flightSize - 1) {
						View v = new View(mContext);
						v.setLayoutParams(lyparamview);
						v.setBackgroundColor(ResourceCacheUtil
								.parseColor("#264a51"));
						ly.addView(v);
					}
					// 绑定单击事件
					tv.setOnClickListener(onClick);
				}
				pop = new PopupWindow(ly, ViewUtil.dp2px(mContext, 104),
						ViewGroup.LayoutParams.WRAP_CONTENT, false);
				pop.setBackgroundDrawable(mContext.getResources().getDrawable(
						R.drawable.duoqu_alphabg));
				pop.setOutsideTouchable(true);

				pop.showAsDropDown(mDuoquBaseDepartTime);

//			} 
//			else {
//			    
//				pop.showAsDropDown(mDuoquBaseDepartTime);
//			}
		}
	};

	/**
	 * 根据航班号切换对应的数据
	 */
	public OnClickListener onClick = new OnClickListener() {

		@Override
		public void onClick(View view) {
			int position = view.getId();
			view.setOnTouchListener(onTouch);// 动态改变颜色
			ViewUtil.setTextViewValue(mDuoquBaseFlightNumber,
					mFlightList.get(position) + "航班");// 改变航班号
			// 关闭popwindow
			if (pop != null) {
				pop.dismiss();
				pop = null;
			}
			mMessage.putValue("db_air_data_index", String.valueOf(position));
			bindAirTickeInfo(mMessage);
		}
	};

	public OnTouchListener onTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent even) {
			if (even.getAction() == even.ACTION_DOWN) {// 按下
				view.setBackgroundColor(ResourceCacheUtil.parseColor("#264a51"));
			}
			if (even.getAction() == even.ACTION_UP) {// 松开
				view.setBackgroundColor(ResourceCacheUtil
						.parseColor("#15175d6c"));
			}
			return false;
		}
	};

	private void setText(JSONObject msg) {
		if (msg == null) {
			return;
		}

		ContentUtil.setText(mDuoquBaseFlightNumber, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_fight_number"),
				ContentUtil.NO_DATA_EN);

		ContentUtil.setText(mDuoquBaseDepartCity, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_depart_city"),
				ContentUtil.NO_DATA_EN);

		ContentUtil.setText(mDuoquBaseDepartDate, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_depart_date"),
				ContentUtil.NO_DATA_EN);

		ContentUtil.setText(mDuoquBaseDepartTime, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_depart_time"),
				ContentUtil.NO_DATA_TIME);

		ContentUtil.setText(mDuoquBaseArriveCity, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_arrive_city"),
				ContentUtil.NO_DATA_EN);

		if (!StringUtils.isNull((String) JsonUtil.getValueFromJsonObject(msg,
				"view_arrive_date"))) {
			ContentUtil.setText(mDuoquBaseArriveDate, (String) JsonUtil
					.getValueFromJsonObject(msg, "view_arrive_date"),
					ContentUtil.NO_DATA_EN);
		}else {
			mDuoquBaseArriveDate.setText(ContentUtil.NO_DATA_EN);
		}

		ContentUtil.setText(mDuoquBaseArriveTime, (String) JsonUtil
				.getValueFromJsonObject(msg, "view_arrive_time"),
				ContentUtil.NO_DATA_TIME);

		setAirport(mDuoquBaseDepartAirport, mDuoquBaseArriveAirport, msg);

	}

	private void setAirport(TextView t1, TextView t2, JSONObject msg) {
		t1.setVisibility(View.VISIBLE);
		t2.setVisibility(View.VISIBLE);
		String s1 = (String) JsonUtil.getValueFromJsonObject(msg,
				"view_from_place_arr_complex");
		String s2 = (String) JsonUtil.getValueFromJsonObject(msg,
				"view_to_place_arr_complex");
		if (StringUtils.isNull(s1) && StringUtils.isNull(s2)) {
			t1.setVisibility(View.GONE);
			t2.setVisibility(View.GONE);
		}
		ContentUtil.setText(t1, s1, ContentUtil.NO_DATA_EN);
		ContentUtil.setText(t2, s2, ContentUtil.NO_DATA_EN);
	}

	public void setLableVisibility(TextView t1, String value, String color) {
		if (!StringUtils.isNull(value)) {
			t1.setVisibility(View.VISIBLE);
			ContentUtil.setText(t1, value, null);
			ContentUtil.setTextColor(t1, color);
		} else {
			t1.setVisibility(View.GONE);
		}
	}

	private void showSelectFightNumImageView(boolean show, TextView textView) {
		ContentUtil.setViewVisibility(mDuoquBaseSelectFlightNumber,
				(show ? View.VISIBLE : View.GONE));
		textView.setPadding(0, 0, (show ? 0 : 0), 0);

	}

}

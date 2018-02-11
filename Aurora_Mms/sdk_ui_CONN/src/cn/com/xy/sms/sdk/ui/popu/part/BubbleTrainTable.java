package cn.com.xy.sms.sdk.ui.popu.part;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.Iservice.XyCallBack;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.net.NetUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ResourceCacheUtil;
import cn.com.xy.sms.sdk.ui.popu.util.ViewManger;
import cn.com.xy.sms.sdk.ui.popu.util.ViewUtil;
import cn.com.xy.sms.sdk.ui.popu.widget.ListDialog;
import cn.com.xy.sms.sdk.ui.popu.widget.OnSurelistener;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class BubbleTrainTable extends UIPart {

    private TextView mDepartCity;
    private TextView mDepartTime;
    private TextView mDepartDate;
    private TextView mTrainNumber;
    private TextView mSeatInfo;
    private TextView mArriveCity;
    private TextView mArriveTime;
    private TextView mArriveDate;
    private TextView mGO;
    private ImageView mSelectTrainPlace;
    private ImageView mImgLine;
    private static final int DESTINATION_TEXT_PADDING_RIGHT = ViewUtil.dp2px(Constant.getContext(), 14);
    private static boolean mIsPopupStationInfoList = false;
    private static final String NO_TRAIN_INFO = ContentUtil.getResourceString(
            Constant.getContext(), R.string.duoqu_no_train_info);
    private final int DUOQU_DEFULT_GRAY = mContext.getResources().getColor(
            R.color.duoqu_black2);
    private final Handler mHandler = new Handler();

    public BubbleTrainTable(Activity context, BusinessSmsMessage message,
            XyCallBack callback, int layoutId, ViewGroup root, int partId) {
        super(context, message, callback, layoutId, root, partId);
    }

    @Override
    public void initUi() throws Exception {
        mDepartCity = (TextView) super.mView
                .findViewById(R.id.duoqu_base_depart_city);
        mDepartTime = (TextView) super.mView
                .findViewById(R.id.duoqu_base_depart_time);
        mDepartDate = (TextView) super.mView
                .findViewById(R.id.duoqu_base_depart_date);
        mTrainNumber = (TextView) super.mView
                .findViewById(R.id.duoqu_base_train_number);
        mSeatInfo = (TextView) super.mView
                .findViewById(R.id.duoqu_base_seat_info);
        mArriveCity = (TextView) super.mView
                .findViewById(R.id.duoqu_base_arrive_city);
        mArriveTime = (TextView) super.mView
                .findViewById(R.id.duoqu_base_arrive_time);
        mArriveDate = (TextView) super.mView
                .findViewById(R.id.duoqu_base_arrive_date);
        mGO = (TextView) super.mView.findViewById(R.id.duoqu_base_go);
        mSelectTrainPlace = (ImageView) super.mView
                .findViewById(R.id.duoqu_base_select_train_place);
        mImgLine = (ImageView) super.mView.findViewById(R.id.duoqu_img_line);
    }

    @Override
    public void setContent(BusinessSmsMessage message, boolean isRebind)
            throws Exception {
        this.mMessage = message;

        ViewManger.setViewBg(mContext, mView,
                message.getImgNameByKey("v_by_bg"),
                R.drawable.duoqu_rectangledrawble, 1);

        // 内容全字体颜色
        String contentColor = (String) message.getValue("v_by_color");
        if (!StringUtils.isNull(contentColor)) {
            ContentUtil.textSetColor(mDepartCity,contentColor);
            ContentUtil.textSetColor(mDepartTime,contentColor);
            ContentUtil.textSetColor(mDepartDate,contentColor);
            ContentUtil.textSetColor(mTrainNumber,contentColor);
            ContentUtil.textSetColor(mSeatInfo,contentColor);
            ContentUtil.textSetColor(mArriveCity,contentColor);
            ContentUtil.textSetColor(mArriveTime,contentColor);
            ContentUtil.textSetColor(mArriveDate,contentColor);
        } else {
			mDepartCity.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDepartTime.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mDepartDate.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mTrainNumber.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mSeatInfo.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mArriveCity.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mArriveTime.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
			mArriveDate.setTextColor(mView.getResources().getColor(
					R.color.duoqu_black2));
        }

        // 内容图标字体颜色
        String directionTextColor = (String) message.getValue("v_by_mid_color");
        if (!StringUtils.isNull(directionTextColor)) {
            ContentUtil.textSetColor(mGO,directionTextColor);
        } else {
            mGO.setTextColor(ResourceCacheUtil.parseColor("#7fffffff"));
        }

        // 方向箭头
        String centerIconLogo = (String) message
                .getImgNameByKey("v_center_icon");
        if (!StringUtils.isNull(centerIconLogo)) {
            ViewManger.setViewBg(Constant.getContext(), mImgLine,
                    centerIconLogo, 0, -1, true);
        }else {
        	mImgLine.setBackgroundResource(R.drawable.duoqu_train_directions);
		}
        
		// 下拉选择按钮
		String iconLogo = (String) message.getImgNameByKey("v_by_icon_2");
		if (!StringUtils.isNull(iconLogo)) {
			ViewManger.setViewBg(Constant.getContext(), mSelectTrainPlace,
					iconLogo, 0, -1, true);
		} else {
			mSelectTrainPlace
					.setBackgroundResource(R.drawable.duoqu_train_more);
		}

        bindTrainTickeInfo(message);

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

    private void bindTrainTickeInfo(BusinessSmsMessage message) {

        ContentUtil.setText(mDepartDate,
                (String) message.getValue("view_depart_date"),
                ContentUtil.NO_DATA);
        ContentUtil.setText(mDepartTime,
                (String) message.getValue("view_depart_time"),
                ContentUtil.NO_DATA_TIME);
        ContentUtil.setText(mDepartCity,
                (String) message.getValue("view_depart_city"),
                ContentUtil.NO_DATA);
        ContentUtil.setText(mSeatInfo,
                (String) message.getValue("view_seat_info"), null);
        ContentUtil.setText(mTrainNumber,
                (String) message.getValue("view_train_number"),
                ContentUtil.NO_DATA);
        ContentUtil.setText(mArriveTime,
                (String) message.getValue("view_arrive_time"),
                ContentUtil.NO_DATA_TIME);
        ContentUtil.setText(mArriveCity,
                (String) message.getValue("view_arrive_city"),
                ContentUtil.NO_DATA);

        setArrayCity(message);
        JSONArray allStationInfoJSONArray = null;
        try {
            allStationInfoJSONArray = (JSONArray) message
                    .getValue("station_list_obj");
            if (allStationInfoJSONArray == null
                    && message.getValue("station_list") != null) {
                allStationInfoJSONArray = new JSONArray(
                        (String) message.getValue("station_list"));
                if (allStationInfoJSONArray != null) {
                    message.bubbleJsonObj.put("station_list_obj",
                            allStationInfoJSONArray);
                }
            }
        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }

        trainInfoCompletion(allStationInfoJSONArray);
        queryTrainData(message);

    }

    private void setArrayCity(final BusinessSmsMessage smsMessage) {
        String value = (String) smsMessage.getValue("view_arrive_city");

        String dbValue = (String) smsMessage.getValue("db_train_arrive_city");

        if (StringUtils.isNull(value) && StringUtils.isNull(dbValue)) {
            mArriveCity.setTextColor(Color.parseColor((String) smsMessage.getValue("v_by_color")));
            mArriveCity.setText("未知");
        } else {
            mArriveCity.setText(value);
            mArriveCity.setTextColor(Color.parseColor((String) smsMessage.getValue("v_by_color")));
        }
    }

    private void queryTrainData(final BusinessSmsMessage smsMessage) {
        if (smsMessage == null || smsMessage.bubbleJsonObj == null
                || smsMessage.messageBody == null) {
            return;
        }
        try {
            if (!StringUtils.isNull((String) smsMessage
                    .getValue("station_list"))) {
                return;
            }
            long queryTime = smsMessage.bubbleJsonObj.optLong("QueryTime");
            String networkState = (String) JsonUtil.getValueFromJsonObject(
                    smsMessage.bubbleJsonObj, "networkState");

            if ((DateUtils.isToday(queryTime) && !"offNetwork"
                    .equalsIgnoreCase(networkState))
                    || (!NetUtil.checkAccessNetWork() && "offNetwork"
                            .equalsIgnoreCase(networkState))) {
                return;
            }

            smsMessage.bubbleJsonObj.put("QueryTime",
                    System.currentTimeMillis());
            SdkCallBack xyCallBack = new SdkCallBack() {

                @Override
                public void execute(final Object... obj) {
                    if (obj == null || obj.length != 6 || obj[0] == null
                            || obj[1] == null) {
                        try {
                            if (obj != null
                                    && obj.length > 0
                                    && obj[0] != null
                                    && "offNetwork".equalsIgnoreCase(obj[0]
                                            .toString())) {
                                smsMessage.bubbleJsonObj.put("networkState",
                                        "offNetwork");
                            }
                            if (obj != null && obj.length > 0 && obj[0] == null) {
                                smsMessage.bubbleJsonObj.put("QueryTime",
                                        0);
                            }
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }

                    try {

                        final String stationList = JsonUtil
                                .getValueFromJsonObject((JSONObject) obj[1],
                                        "station_list").toString();

                        smsMessage.bubbleJsonObj.put("station_list",
                                stationList);

                        final JSONArray allStationInfoJsonArray = new JSONArray(
                                stationList);
                        smsMessage.bubbleJsonObj.put("station_list_obj",
                                allStationInfoJsonArray);

                        String smsId = obj[0].toString();
                        String thisSmsId = String.valueOf(mMessage.getSmsId());

                        if (!StringUtils.isNull(thisSmsId)
                                && smsId.equals(thisSmsId)) {
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    trainInfoCompletion(allStationInfoJsonArray);
                                }
                            });
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            };

            String smsId = String.valueOf(smsMessage.getSmsId());
            String trainNum = (String) smsMessage.getValue("view_train_number");
            trainNum = trainNum == null ? null : trainNum.replace("次", "");
            String departCity = (String) smsMessage
                    .getValue("view_depart_city");
            String arriveCity = (String) smsMessage
                    .getValue("view_arrive_city");

            Map<String, Object> extend = new HashMap<String, Object>();
            extend.put("phoneNumber", (String) smsMessage.getValue("phoneNum"));
            extend.put("titleNo", smsMessage.getTitleNo());
            extend.put("msgId", smsId);
            extend.put("bubbleJsonObj", smsMessage.bubbleJsonObj.toString());
            extend.put("messageBody", smsMessage.getMessageBody());
            // remove networkState
            smsMessage.bubbleJsonObj.put("networkState", null);
            ParseManager.queryTrainInfo(smsId, trainNum, departCity,
                    arriveCity, extend, xyCallBack);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    private void trainInfoCompletion(JSONArray allStationInfoJsonArray) {
        if (allStationInfoJsonArray == null) {
            showSelectStationImageView(false);
            return;
        }

        try {

            String departCity = (String) mMessage.getValue("view_depart_city");
            String departDatetime = (String) mMessage
                    .getValue("view_depart_date");
            String arriveCity = (String) mMessage.getValue("view_arrive_city");
            String arriveDatetime = (String) mMessage
                    .getValue("view_arrive_datetime");

            final String stationNameKey = "name";
            final String stationArriveTimeKey = "spt";
            final String stationDepartureTimeKey = "stt";

            if (departCity == null
                    || departCity.equalsIgnoreCase(ContentUtil.NO_DATA)) {
                departCity = (String) JsonUtil.getValueFromJsonObject(
                        allStationInfoJsonArray.getJSONObject(0),
                        stationNameKey);
                departDatetime = (String) JsonUtil.getValueFromJsonObject(
                        allStationInfoJsonArray.getJSONObject(0),
                        stationDepartureTimeKey);
                ContentUtil.setText(mDepartCity, departCity,
                        ContentUtil.NO_DATA);
            }

            if (arriveCity == null
                    || arriveCity.equalsIgnoreCase(ContentUtil.NO_DATA)) {
                int lastIndex = allStationInfoJsonArray.length() - 1;
                arriveCity = (String) JsonUtil.getValueFromJsonObject(
                        allStationInfoJsonArray.getJSONObject(lastIndex),
                        stationNameKey);
                arriveDatetime = (String) JsonUtil.getValueFromJsonObject(
                        allStationInfoJsonArray.getJSONObject(lastIndex),
                        stationArriveTimeKey);

                showSelectStationImageView(true);
                bindPopupStationInfoList(allStationInfoJsonArray);

            }
            if (!StringUtils.isNull(departDatetime)
                    && !StringUtils.isNull(arriveDatetime)
                    && !departDatetime.equalsIgnoreCase(ContentUtil.NO_DATA)
                    && !arriveDatetime.equalsIgnoreCase(ContentUtil.NO_DATA)) {
                return;
            }

            String stationName = null;
            boolean needSetDepartDatetime = (departDatetime == null || departDatetime
                    .equalsIgnoreCase(ContentUtil.NO_DATA));

            int len = allStationInfoJsonArray.length();
            for (int i = 0; i < len; i++) {
                stationName = (String) JsonUtil.getValueFromJsonObject(
                        allStationInfoJsonArray.getJSONObject(i),
                        stationNameKey);

                if (needSetDepartDatetime
                        && stationName.equalsIgnoreCase(departCity)) {
                    departDatetime = (String) JsonUtil.getValueFromJsonObject(
                            allStationInfoJsonArray.getJSONObject(i),
                            stationDepartureTimeKey);
                    ContentUtil.setText(mDepartDate, departDatetime,
                            ContentUtil.NO_DATA);
                }

                if (stationName.equalsIgnoreCase(arriveCity)) {
                    arriveDatetime = (String) JsonUtil.getValueFromJsonObject(
                            allStationInfoJsonArray.getJSONObject(i),
                            stationArriveTimeKey);
                    ContentUtil.setText(mArriveTime, arriveDatetime,
                            ContentUtil.NO_DATA);
                    break;
                }

            }

        } catch (JSONException ex) {
            ex.printStackTrace();
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        } finally {
            String arriveCity = (String) this.mMessage
                    .getValue("db_train_arrive_city");
            String arriveTime = (String) this.mMessage
                    .getValue("db_train_arrive_time");

            if (!StringUtils.isNull(arriveCity)) {
                ContentUtil.setText(mArriveCity, arriveCity,
                        ContentUtil.NO_DATA);
                 mArriveCity.setTextColor(Color.parseColor((String) mMessage.getValue("v_by_color")));
            }
            if (!StringUtils.isNull(arriveTime)) {
                ContentUtil.setText(mArriveTime, arriveTime,
                        ContentUtil.NO_DATA);
            }
        }

    }

    /**
     * bind station selection list
     * 
     * @param allStationInfoJsonArray
     *            station list data source
     * @param trainNum
     *            train number
     * @param departName
     *            name of the depart
     * @param arriveName
     *            destination name
     */
    private void bindPopupStationInfoList(
            final JSONArray allStationInfoJsonArray) {
        OnClickListener popupStationInfoListClickListener = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mIsPopupStationInfoList) {
                    return;
                }

                String trainNum = (String) mMessage
                        .getValue("view_train_number");
                if (allStationInfoJsonArray == null) {
                    Toast.makeText(mContext,
                            NO_TRAIN_INFO.replace("{0}", trainNum),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                String departCity = (String) mMessage
                        .getValue("view_depart_city");
                String arriveCity = (String) mMessage
                        .getValue("view_arrive_city");

                JSONArray mListJsonArray = null;
                try {
                    mListJsonArray = stationFilter(allStationInfoJsonArray,
                            departCity, arriveCity);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    return;
                }

                if (mListJsonArray == null || mListJsonArray.length() == 0) {
                    Toast.makeText(mContext,
                            NO_TRAIN_INFO.replace("{0}", trainNum),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                mIsPopupStationInfoList = true;
                String sestinationName = arriveCity;
                if (mArriveCity != null) {
                    sestinationName = mArriveCity.getText().toString().trim();
                }

                ListDialog dialog = new ListDialog(osl, mListJsonArray,
                        mContext, R.style.ShareDialog, sestinationName);
                dialog.show();
            }
        };

        if (mArriveCity != null) {
            mArriveCity.setOnClickListener(popupStationInfoListClickListener);
            mSelectTrainPlace
                    .setOnClickListener(popupStationInfoListClickListener);

        }
    }

    /**
     * station list callback process
     */
    OnSurelistener osl = new OnSurelistener() {

        @Override
        public void onClick(JSONObject jsonobject) {
            mIsPopupStationInfoList = false;
            if (jsonobject == null) {
                return;
            }

            String stationName = (String) JsonUtil.getValueFromJsonObject(
                    jsonobject, "name");
            String arriveTime = (String) JsonUtil.getValueFromJsonObject(
                    jsonobject, "spt");

            if (!StringUtils.isNull(stationName)) {
                ContentUtil.setText(mArriveCity, stationName,
                        ContentUtil.NO_DATA);
                mArriveCity.setTextColor(Color.parseColor((String) mMessage.getValue("v_by_color")));
            }

            if (!StringUtils.isNull(arriveTime)) {
                ContentUtil.setText(mArriveTime, arriveTime + "",
                        ContentUtil.NO_DATA);
            }

            if (mMessage == null || mMessage.bubbleJsonObj == null) {
                return;
            }

            try {
                mMessage.bubbleJsonObj.put("db_train_arrive_city", stationName);
                mMessage.bubbleJsonObj.put("db_train_arrive_time", arriveTime);
                ParseManager.updateMatchCacheManager(mMessage);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    };

    /**
     * filter station info
     * 
     * @param allStationInfoJsonArray
     * @param departName
     * @param arriveName
     * @return
     * @throws JSONException
     */
    private static JSONArray stationFilter(
            final JSONArray allStationInfoJsonArray, final String departName,
            final String arriveName) throws JSONException {
        JSONArray listJsonArray = new JSONArray();
        boolean addStationInfo = false;
        String stationName = null;

        int len = allStationInfoJsonArray.length();

        for (int i = 0; i < len; i++) {
            stationName = (String) JsonUtil.getValueFromJsonObject(
                    allStationInfoJsonArray.getJSONObject(i), "name");

            if (addStationInfo) {
                listJsonArray.put(allStationInfoJsonArray.getJSONObject(i));
            }

            if (stationName.equalsIgnoreCase(departName)) {
                addStationInfo = true;
            }

            if (stationName.equalsIgnoreCase(arriveName)) {
                break;
            }
        }
        return listJsonArray;
    }

    private void showSelectStationImageView(boolean show) {
        ContentUtil.setViewVisibility(mSelectTrainPlace, (show ? View.VISIBLE
                : View.GONE));
        if (mArriveCity != null) {
            mArriveCity.setPadding(0, 0, (show ? DESTINATION_TEXT_PADDING_RIGHT
                    : 0), 0);
        }

    }

}

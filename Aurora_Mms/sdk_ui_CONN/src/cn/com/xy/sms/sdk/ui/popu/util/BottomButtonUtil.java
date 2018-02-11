package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.log.PrintTestLogUtil;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class BottomButtonUtil {
    
    public static JSONArray getAddMap(Context context, String adAction,
            BusinessSmsMessage message) {
        try {
            JSONArray jsonArray = null;
            if (!StringUtils.isNull(adAction)) {
                jsonArray = new JSONArray(adAction);
            }
            if (ViewUtil.getChannelType() == 2
                    || ViewUtil.getChannelType() == 8) {// 中兴渠道，最多2个按钮
                if (jsonArray != null && jsonArray.length() > 0) {
                    if (jsonArray.length() >= 2) {

                        JSONArray tempJsonArr = new JSONArray();
                        tempJsonArr.put(jsonArray.get(0));
                        tempJsonArr.put(jsonArray.get(1));
                        return tempJsonArr;
                    }
                }
                if (message.viewType == 1) {
                    // 气泡的话，增加按钮
                    JSONObject json = getCallJson(message);
                    if (json != null) {
                        JSONArray tempArray = new JSONArray();
                        tempArray.put(json);
                        if (jsonArray != null && jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                tempArray.put(i + 1, jsonArray.get(i));
                            }
                        }
                        return tempArray;
                    }

                }
            } else if (ViewUtil.getChannelType() == 5 && message.viewType == 0) {// 神奇工厂，添加删除按钮
                JSONObject json = getDeleJson(message);
                if (json != null) {
                    JSONArray tempArray = new JSONArray();
                    tempArray.put(json);
                    if (jsonArray != null && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            tempArray.put(i + 1, jsonArray.get(i));
                        }
                    }
                    return tempArray;
                }
            } else if (ViewUtil.getChannelType() == 7) {// 华为渠道气泡，最多只显示1个按钮
                if (message.viewType == 1) {
                    if (jsonArray != null && jsonArray.length() >= 1) {
                        JSONArray tempJsonArr = new JSONArray();
                        tempJsonArr.put(jsonArray.get(0));
                        return tempJsonArr;
                    }
                }
            }
            return jsonArray;
        } catch (Exception e) {
            // e.printStackTrace();
        }
        return null;
    }
    
    public static JSONObject getCallJson(BusinessSmsMessage message) {
        try {
            if (!StringUtils.isNull((String) message
                    .getValue("travel_hotelorder_hotelphonenum"))
                    || !StringUtils
                            .isNull((String) message
                                    .getValue("travel_hotelorderpayment_hotelphonenum"))) {
                JSONObject jSonObject = new JSONObject();
                jSonObject.put("type", "call_phone");
                jSonObject.put("action", "call_phone");
                jSonObject.put("titleNo",
                        (String) message.getValue("title_num"));
                
                String phoneNum = (String) message.getValue("travel_hotelorder_hotelphonenum");
                if(StringUtils.isNull(phoneNum)){
                    phoneNum = (String) message.getValue("travel_hotelorderpayment_hotelphonenum");
                }

                String action_data = getJsonString("type", "call_phone",
                        "phone", phoneNum, "titleNo",
                        (String) message.getValue("title_num"));
                jSonObject.put("action_data", action_data);

                return jSonObject;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;

    }

    public static JSONObject getDeleJson(BusinessSmsMessage message) {
        try {
            JSONObject jSonObject = new JSONObject();
            jSonObject.put("type", "del_msg");
            jSonObject.put("action", "del_msg");
            if (ContentUtil.getLanguage() == "zh") {
            	jSonObject.put("btn_name", Constant.getContext().getResources()
            			.getString(R.string.duoqu_delete));
			}else {
				jSonObject.put("egName", Constant.getContext().getResources()
            			.getString(R.string.duoqu_delete_eg));
			}
            jSonObject.put("titleNo", (String) message.getValue("title_num"));
            jSonObject.put("msgId", (String) message.getValue("msgId"));
            String action_data = getJsonString("type", "del_msg", "titleNo",
                    (String) message.getValue("msgId"), "titleNo",
                    (String) message.getValue("title_num"));
            jSonObject.put("action_data", action_data);

            return jSonObject;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;

    }
    
    public static String getJsonString(String... obj) {

        try {
            JSONObject json = new JSONObject();
            if (obj == null || obj.length % 2 != 0)
                return "";
            for (int i = 0; i < obj.length; i += 2) {
                json.put(obj[i], obj[i + 1]);
            }

            String encode = StringUtils.encode(json.toString());

            return encode;
        } catch (Exception e) {

        }
        return "";
    }

    

    public static JSONArray getActionArrayData(Context context, String adAction, BusinessSmsMessage message) {
        try {
            JSONArray jsonArray = null;
            if (!StringUtils.isNull(adAction)) {
                jsonArray = new JSONArray(adAction);
            }
            return jsonArray;
        } catch (Exception e) {
        }
        return null;
    }

    public static void setButtonTextAndImg(TextView buttonText, String action, boolean disLogo) {
        try {
            String buttonName = buttonText.getText().toString();
            boolean setText = StringUtils.isNull(buttonName);

            int resLogoId = SimpleButtonUtil.bindButtonData(buttonText, action, setText, true);

            if (disLogo && resLogoId != -1) {
                Drawable dw = Constant.getContext().getResources().getDrawable(resLogoId);
                buttonText.setCompoundDrawablesWithIntrinsicBounds(dw, null, null, null);
            } else {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setBotton(View button, final TextView buttonText, final JSONObject actionMap, boolean disLogo,
            final Activity mContext, final BusinessSmsMessage message) {
        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    // do action
                    JSONObject jsonObject = (JSONObject) v.getTag();
                    HashMap<String, String> valueMap = new HashMap<String, String>();
                    valueMap.put("simIndex", message.simIndex + "");
                    valueMap.put("phoneNum", message.originatingAddress + "");
                    valueMap.put("content", message.getMessageBody() + "");
                    byte viewType = message.viewType;
                    valueMap.put("viewType", viewType + "");
                    String msgId = message.getExtendParamValue("msgId") + "";
                    valueMap.put("msgId", msgId);
                    LogManager.e("setBotton", "message.getMessageBody()=" + message.getMessageBody(),null);
                    JsonUtil.putJsonToMap(jsonObject, valueMap);
                    String action_data = (String) JsonUtil.getValueFromJsonObject(jsonObject, "action_data");
                    DuoquUtils.doAction(mContext, action_data, valueMap);
                } catch (Exception e) {
                    if (LogManager.debug) {
                        e.printStackTrace();
                    }
                }

            }

        };

        if (actionMap != null) {
            if (!disLogo) {
                buttonText.setCompoundDrawables(null, null, null, null);
            }
            final String action = (String) JsonUtil.getValueFromJsonObject(actionMap, "action");
            String btnName = ContentUtil.getBtnName(actionMap);
            if (!StringUtils.isNull(btnName)) {
                buttonText.setText(btnName);
                BottomButtonUtil.setButtonTextAndImg(buttonText, action, disLogo);
            }
            if (!StringUtils.isNull(action)) {
                button.setTag(actionMap);
                button.setOnClickListener(onClickListener);
            }
        }
        ViewManger.setRippleDrawable(button);
    }
}

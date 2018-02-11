package cn.com.xy.sms.sdk.ui.popu.util;

import java.util.Calendar;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.util.JsonUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class ContentUtil {
    
    public static final String NO_DATA = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_double_line);
    public static final String NO_DATA_TIME = ContentUtil.getResourceString(Constant.getContext(), R.string.duoqu_double_line_an);
//    public static final Typeface SIMPLIFIED = Typeface.createFromAsset(Constant.getContext().getAssets(), "fonts/Founder_Lanting_black.TTF");
//    public static final Typeface SIMPLIFIED_BLACK = Typeface.createFromAsset(Constant.getContext().getAssets(), "fonts/Founder_Lanting_black_GBK.TTF");
    public static final String NO_DATA_EN = ContentUtil.getResourceString(Constant.getContext(),
            R.string.duoqu_double_line_en);
    public static final String COR_BLACK = "#000000";
    public static final String COR_WHITE = "#ffffff";
    public static final String COR_LIGHT_BLUE = "#D4EEFB";
    public static final String COR_GRAY = "#7F000000";
    public static final String COR_RED = "#FF1F00";
    public static final String CHINESE = "[\u4e00-\u9fa5]";

    public static void setTextColor(TextView textView, String textColor) {
        try {
            if (textView != null && !StringUtils.isNull(textColor)) {
                int res = ResourceCacheUtil.parseColor(textColor);
                textView.setTextColor(res);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Set the text view content
     * 
     * @param textView
     * @param value
     * @param defaultValue
     */
    public static void setText(TextView textView, String value, String defaultValue) {
        if (textView == null) {
            return;
        }

        if (StringUtils.isNull(value)) {
            textView.setText(defaultValue);
            return;
        }
        textView.setText(value.trim());
    }

    /**
     * Set the enabled state of view.
     * 
     * @param view
     * @param visibility
     */
    public static void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
           
        }
    }

    /**
     * Return the string value associated with a particular resource ID
     * 
     * @param context
     * @param id
     * @return
     */
    public static String getResourceString(Context context, int id) {
        if (context != null) {
            try {
                return context.getResources().getString(id);
            } catch (NotFoundException ex) {
                return null;
            }
        }
        return null;
    }
    
    /**
     *  get the length of the string in Chinese or English
     * */
	public static int getStringLength(String value) {
		int valueLength = 0;
		 
		for (int i = 0; i < value.length(); i++) {
			Character temp = value.charAt(i);
			if (temp.toString().matches(CHINESE)) {
				valueLength += 2;
			} else {
				valueLength += 1;
			}
		}
		return valueLength;
	}
	 /**
     * 获取时间文本，如果系统为12小时制时添加上午下午提示
     * 
     */
    public static String getTimeText(Context content, long time) {
        if (time <= 0) {
            return null;
        }

        Calendar timeCalendar = Calendar.getInstance();
        timeCalendar.setTimeInMillis(time);
        String timeText = null;

        if (DateFormat.is24HourFormat(content)) {
            timeText = DateFormat.format("kk:mm", timeCalendar).toString();
        } else {
            String ampmValues = null;
            if (timeCalendar.get(Calendar.AM_PM) == 0) {
                ampmValues = content.getString(R.string.duoqu_am);
            } else {
                ampmValues = content.getString(R.string.duoqu_pm);
            }
            timeText = ampmValues + " "
                    + DateFormat.format("h:mm", timeCalendar).toString();
        }

        return timeText;
    }
    
    
    public static void textSetColor(TextView textView, String color) {
        int colors = ResourceCacheUtil.parseColor(color);
        if (colors != -1 && textView != null){
            textView.setTextColor(colors);
        }
    }
    
    public static void isTextSetColor(TextView textView, String color,int rescolor) {
        if (!StringUtils.isNull(color)&& textView != null){
            textView.setTextColor(ResourceCacheUtil.parseColor(color));
        }else {
        	textView.setTextColor(rescolor);
		}
    }
    
    
    public static String getLanguage(){
    	if ("zh".equals(Locale.getDefault().getLanguage())) {
			return "zh";
		}else {
			return "en";
		}
    }
    
    public static String getBtnName(final JSONObject actionMap) {
		String btnName = (String) JsonUtil.getValueFromJsonObject(actionMap, "btn_name");
        if (getLanguage() != "zh") { 
			String egName = (String) JsonUtil.getValueFromJsonObject(actionMap, "egName");
			if(!StringUtils.isNull(egName)){
			    btnName =  egName;
			}
		}
		return btnName;
	}
}

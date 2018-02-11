package com.gionee.calendar.day;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.android.calendar.R;
import com.gionee.calendar.view.Log;
import com.mediatek.calendar.lunar.LunarUtil;

import aurora.app.AuroraAlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.format.Time;
import android.graphics.drawable.Drawable;
//Gionee <pengwei><2013-04-12> modify for DayView begin
//Gionee <pengwei><2013-06-07> modify for CR00000000 begin
public class DayUtils {
	public static AuroraAlertDialog alertDialog;
	public static String[] mMonths = { "农历一月", "农历二月", "农历三月", "农历四月", "农历五月",
			"农历六月", "农历七月", "农历八月", "农历九月", "农历十月", "农历十一月", "农历腊月" };
    public static String[] mWeeks = {"星期天","星期一", "星期二", "星期三", "星期四", "星期五",
    "星期六"};
    public static String[] mAstro = {"白羊座","金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
        "天秤座","天蝎座","射手座","摩羯座","水瓶座","双鱼座"};
    public static Drawable[] mAstroPics = new Drawable[12];
    public static void initWeeks(Context context){
        mWeeks[0] = context.getResources().getString(R.string.gn_day_Sunday);
        mWeeks[1] = context.getResources().getString(R.string.gn_day_Monday);
        mWeeks[2] = context.getResources().getString(R.string.gn_day_Tuesday);
        mWeeks[3] = context.getResources().getString(R.string.gn_day_Wednesday);
        mWeeks[4] = context.getResources().getString(R.string.gn_day_Thursday);
        mWeeks[5] = context.getResources().getString(R.string.gn_day_Friday);
        mWeeks[6] = context.getResources().getString(R.string.gn_day_Saturday);
    }
	
	public static String getLunarWeek(Context context,int weekInt) {
		try {
			initWeeks(context);
			return mWeeks[weekInt];
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
	
	public static String getLunarDate(Context context,String lunarDate) {
		if(lunarDate == null && "".equals(lunarDate) && lunarDate.length() < 5){
			return "";
		}
		String lunarDateTemp = lunarDate.substring(0,lunarDate.length());
		Log.e("lunarDateTemp---------------"+lunarDateTemp);
		return context.getString(R.string.gn_day_lunar) + " " + lunarDateTemp;
	}
	
	public static String getMillisecondToTime(long msTime){
		Date date = new Date(msTime);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		return sdf.format(date); 

	}
	
	public static String getMillisecondToHour(long msTime){
		Date date = new Date(msTime);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(date); 
	}
	
    public static final String VCALENDAR_TYPE = "text/x-vcalendar";
    public static final String VCALENDAR_URI = "content://com.mediatek.calendarimporter/";
    /**
     * M: Share event by event id.
     * @param context
     * @param eventId event id
     */
    public static void sendShareEvent(Context context, long eventId) {
        Log.d("DayUtils.sendShareEvent() eventId=" + eventId);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(VCALENDAR_URI + eventId));
        intent.setType(VCALENDAR_TYPE);
        context.startActivity(intent);
    }

    public static boolean isChinsesLunarSetting() {  
        String language = getLanguageEnv();  
  
        if (language != null  
                && (language.trim().equals("zh-CN") || language.trim().equals("zh-TW")))  
            return true;  
        else  
            return false;  
    }  
    
    public static String getLanguageEnv() {  
        Locale l = Locale.getDefault();  
        String language = l.getLanguage();  
        String country = l.getCountry().toLowerCase();  
        if ("zh".equals(language)) {  
            if ("cn".equals(country)) {  
                language = "zh-CN";  
            } else if ("tw".equals(country)) {  
                language = "zh-TW";  
            }  
        } else if ("pt".equals(language)) {  
            if ("br".equals(country)) {  
                language = "pt-BR";  
            } else if ("pt".equals(country)) {  
                language = "pt-PT";  
            }  
        }  
        return language;  
    }  
    
    public static int dip2px(Context context, float dpValue) {  
        final float scale = context.getResources().getDisplayMetrics().density;  
        return (int) (dpValue * scale + 0.5f);  
    }  
 
    /**
    *
    * @param spValue
    * @param fontScale（DisplayMetrics:scaledDensity）
    * @return
    */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);
    } 
    
    public static void initAstro(Context context){
       	mAstro[0] = context.getResources().getString(R.string.gn_day_astro_name_aries);
        mAstro[1] = context.getResources().getString(R.string.gn_day_astro_name_taurus);
        mAstro[2] = context.getResources().getString(R.string.gn_day_astro_name_gemini);
        mAstro[3] = context.getResources().getString(R.string.gn_day_astro_name_cancer);
        mAstro[4] = context.getResources().getString(R.string.gn_day_astro_name_leo);
        mAstro[5] = context.getResources().getString(R.string.gn_day_astro_name_virgo);
        mAstro[6] = context.getResources().getString(R.string.gn_day_astro_name_libra);
       	mAstro[7] = context.getResources().getString(R.string.gn_day_astro_name_scorpio);
        mAstro[8] = context.getResources().getString(R.string.gn_day_astro_name_sagittarius);
        mAstro[9] = context.getResources().getString(R.string.gn_day_astro_name_capricorn);
        mAstro[10] = context.getResources().getString(R.string.gn_day_astro_name_aquarius);
        mAstro[11] = context.getResources().getString(R.string.gn_day_astro_name_pisces);
    }
    
	public static String getAstro(Context context,int astroInt) {
		try {
			initAstro(context);
			return mAstro[astroInt];
		} catch (Exception e) {
			// TODO: handle exception
			return "";
		}
	}
    
    public static void initAstroPic(Context context){
    	mAstroPics[0] = context.getResources().getDrawable(R.drawable.gn_day_aries);
    	mAstroPics[1] = context.getResources().getDrawable(R.drawable.gn_day_taurus);
    	mAstroPics[2] = context.getResources().getDrawable(R.drawable.gn_day_gemini);
    	mAstroPics[3] = context.getResources().getDrawable(R.drawable.gn_day_cancer);
    	mAstroPics[4] = context.getResources().getDrawable(R.drawable.gn_day_leo);
    	mAstroPics[5] = context.getResources().getDrawable(R.drawable.gn_day_virgo);
    	mAstroPics[6] = context.getResources().getDrawable(R.drawable.gn_day_libra);
    	mAstroPics[7] = context.getResources().getDrawable(R.drawable.gn_day_scorpio);
    	mAstroPics[8] = context.getResources().getDrawable(R.drawable.gn_day_sagittarius);
    	mAstroPics[9] = context.getResources().getDrawable(R.drawable.gn_day_capricorn);
    	mAstroPics[10] = context.getResources().getDrawable(R.drawable.gn_day_aquarius);
    	mAstroPics[11] = context.getResources().getDrawable(R.drawable.gn_day_pisces);
    }
	
	public static Drawable getAstroPics(Context context,int astroInt) {
		try {
			initAstroPic(context);
			return mAstroPics[astroInt];
		} catch (Exception e) {
			// TODO: handle exception
			return context.getResources().getDrawable(R.drawable.gn_day_aries);
		}
	}
    
	//Gionee <pengwei><20130815> modify for CR00854142 begin
    private static long lastClickTime;
    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        //Gionee <pengwei><2013-3-21> add for CR00787297 begin
        if(lastClickTime > time){
        	lastClickTime = time;
        	Log.v("CommonUtils---isFastDoubleClick---lastClickTime > time---" + lastClickTime);
        	return false;
        }
        //Gionee <pengwei><2013-3-21> add for CR00787297 end
        if ( time - lastClickTime < 1000) {
        	Log.v("CommonUtils---isFastDoubleClick---time - lastClickTime < 1000---" + lastClickTime);
            return true;   
        }   
        lastClickTime = time;
    	Log.v("CommonUtils---isFastDoubleClick---time - lastClickTime > 1000---" + lastClickTime);
        return false;   
    }
    
	public static boolean dateOutOfRange(Time time){
		String dateTime = time.toString().substring(0,4);
    	Log.v("CommonUtils---dateOutOfRange---dateTime == " + dateTime);
		String minDate = "1970";
		String maxDate = "2036";
		if(minDate.compareTo(dateTime) > 0 || maxDate.compareTo(dateTime) < 0){
			return true;
		}
		return false;
	}
	
	/*return like 农历十二月二十三*/
	public static String getLaunarDateForDayAndAlmanac(Context context,Time time){
		LunarUtil mLunarUtil = LunarUtil.getInstance(context);
		String mLunarDate = mLunarUtil.getLunarDateString(time.year,
				time.month + 1, time.monthDay);
		mLunarDate = mLunarDate.replace("十二月","腊月");
		String lunarStr = context.getResources().getString(R.string.gn_get_lunar_fail);
		if(mLunarDate != null){
			lunarStr = DayUtils.getLunarDate(context, mLunarDate);
			Log.e("mLunarDate---------------"+mLunarDate);
		}
		return lunarStr;
	}
	
	//Gionee <pengwei><20130815> modify for CR00854142 end
}
//Gionee <pengwei><2013-04-12> modify for DayView end
//Gionee <pengwei><2013-06-07> modify for CR00000000 end
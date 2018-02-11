
package cn.com.xy.sms.sdk.ui.notification;

import java.lang.reflect.Field;

import android.app.Notification;
import android.os.Build;
import android.text.TextUtils;



public class MeizuUtils {

    private static final String TAG = "MeizuUtils";

    /*
     * 通过魅族适配字段获取flyme版本，3.4.5 返回34，即前两位 当版本只有两位时，3.2
     * 返回32，如果版本低于两位，或者不是a.b方式命名，则返回0，标示为无法识别或非flyme
     */
    static int getNumberVersion() {
        int intVersion = 0;
        String version = Build.DISPLAY;
        if (TextUtils.isEmpty(version) || !version.toLowerCase().contains("flyme")) {
            return 0;
        }
        String fullVersion = version.replaceAll(" ", "");
        try {
            String[] temVersion = fullVersion.toLowerCase().split("\\.");
            if (temVersion.length < 2) {// xyy:版本长度在两位及两位以上均正确
                return 0;
            } else {
                intVersion = Integer.valueOf(temVersion[0].substring(temVersion[0].length() - 1));
                intVersion = intVersion * 10 + Integer.valueOf(temVersion[1].substring(0, 1));
            }
        } catch (Exception e) {

        }
        return intVersion;
    }

    public static boolean isFlyme() {
        int numberVersion = getNumberVersion();
        return numberVersion > 0;
    }

    /**
     * add by zhangjinyuan at 2012-1-12 M9修改Notification
     * 增加一个internalApp字段来标识内部应用，强制修改为1 begin xuyingying:目前所有魅族手机均采用此法处理
     *
     * @param notification
     * @return
     */
    public static void updateNotification(Notification notification) {
        try {
            Field internalField = Notification.class.getDeclaredField("internalApp");
            internalField.setAccessible(true);
            internalField.set(notification, Integer.valueOf(1));
           
        } catch (Exception e) {
            e.printStackTrace();
          
        }
        // return notification;
    }

    /**
     * 判断是否是魅族手机，包括了目前魅族手机的所有model号
     *
     * @return
     */
    public static boolean isMeizu() {
        if (Build.MODEL.equalsIgnoreCase("meizu_m9") || Build.MODEL.equalsIgnoreCase("m9") || Build.MODEL.equalsIgnoreCase("meizu mx") || Build.MODEL.equalsIgnoreCase("mx")
                || Build.MODEL.equalsIgnoreCase("m030") || Build.MODEL.equalsIgnoreCase("m031") || Build.MODEL.equalsIgnoreCase("m032") || Build.MODEL.equalsIgnoreCase("m040")
                || Build.MODEL.equalsIgnoreCase("m045") || Build.MODEL.equalsIgnoreCase("m351") || Build.MODEL.equalsIgnoreCase("m353") || Build.MODEL.equalsIgnoreCase("m355")
                || Build.MODEL.equalsIgnoreCase("m356") || Build.MODEL.equalsIgnoreCase("mx4")) {
            return true;
        }
        return false;
    }

    public static boolean isMX() {
        // TODO Auto-generated method stub
        if (Build.MODEL.equalsIgnoreCase("meizu mx") || Build.MODEL.equalsIgnoreCase("mx") || Build.MODEL.equalsIgnoreCase("m030") || Build.MODEL.equalsIgnoreCase("m031")
                || Build.MODEL.equalsIgnoreCase("m032")) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否是魅族 MX2
     *
     * @return
     */
    public static boolean isMX2() {
        return Build.MODEL.equalsIgnoreCase("m040") || Build.MODEL.equalsIgnoreCase("m045");
    }

    public static boolean isMX3() {
        return Build.MODEL.equalsIgnoreCase("m351") || Build.MODEL.equalsIgnoreCase("m353") || Build.MODEL.equalsIgnoreCase("m355") || Build.MODEL.equalsIgnoreCase("m356");
    }

    public static boolean isSupportIPCall() {
        return !(isMeizu() && isFlyme());
    }

    /**
     * 判断魅族M9
     */
    public static boolean isM9() {
        return Build.MODEL.equalsIgnoreCase("meizu_m9") || Build.MODEL.equalsIgnoreCase("m9");
    }

    /**
     * flyme3以前，系统不允许第三方监听短信；
     * flyme361基于android4.4，android4.4限制如果不是默认应用程序不能监听短信，魅族不允许第三方设定自己为默认短信程序；
     * 
     * @return
     */
    public static boolean needMissedSMSOper() {
        return isBeforFlyme3() || (getNumberVersion() >= 36);
    }

    public static boolean isBeforFlyme3() {
        int numberVersion = getNumberVersion();
        return (numberVersion > 0 && numberVersion < 30);
    }
}

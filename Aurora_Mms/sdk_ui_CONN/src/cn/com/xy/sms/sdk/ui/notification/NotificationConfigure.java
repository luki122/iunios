package cn.com.xy.sms.sdk.ui.notification;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.xy.sms.sdk.R;


public class NotificationConfigure {

	//当前当前通知栏背景界面是浅色（通过标题灰度值判断，切换通知栏布局）
    private static final int DIVIDE_SHALLOW_COLOR = 120;
    private static final int DIVIDE_ALPHA_IGNORE = 5;
    private static final int COLOR_CENTRAL = 130;
    public static final int sysVersion = VERSION.SDK_INT;
    public static final int defaultCustomNotificationColor = Color.rgb(COLOR_CENTRAL, COLOR_CENTRAL, COLOR_CENTRAL);
       

    //获取系统通知中字体的颜色
    public static Integer getSystemNotificationTextColor(Context c){
    	Integer defaultNotificationColor = null;
    	
    	if(VERSION.SDK_INT >= 9){
    		//预处理某些ROM(VIVO)
    		defaultNotificationColor = preProcessNotificationColor(c);
    		if(Build.MODEL.contains("X909T")){
    			defaultNotificationColor = Color.WHITE;
    		}
            if(defaultNotificationColor == null){
            	TextView tv = new TextView(c);
                int id = Resources.getSystem().getIdentifier("TextAppearance.StatusBar.EventContent.Title","style", "android");
                tv.setTextAppearance(c, id);
                defaultNotificationColor = tv.getTextColors().getDefaultColor();
            }
    	}else{
    		defaultNotificationColor = searchNotificationPrimaryText(c);
    	}
    	
    	if(defaultNotificationColor != null){
    		return defaultNotificationColor.intValue();
    	}
    	return null;
    }

    // Retrieve notification textColor with android < 2.3
    private static Integer searchNotificationPrimaryText(Context aContext) {
        Integer notificationColor = null;
        try {
            Notification ntf = newNotification(aContext);
            LinearLayout group = new LinearLayout(aContext);
            ViewGroup resultEvent = (ViewGroup) ntf.contentView.apply(aContext, group);
            if (null != resultEvent && resultEvent instanceof ViewGroup) {
                notificationColor = recurseSearchNotificationPrimaryText((ViewGroup) resultEvent,
                        aContext.getString(R.string.app_name));
                group.removeAllViews();
            }
        } catch (Exception e) {
        }
        return notificationColor;
    }
    
    private static Integer recurseSearchNotificationPrimaryText(ViewGroup gp, String searchText) {
        final int count = gp.getChildCount();
        for (int i = 0; i < count; ++i) {
            if (gp.getChildAt(i) instanceof TextView) {
                final TextView text = (TextView) gp.getChildAt(i);
                final String szText = text.getText().toString();
                if (searchText.equals(szText)) {
                    return text.getTextColors().getDefaultColor();
                }
            } else if (gp.getChildAt(i) instanceof ViewGroup) {
                return recurseSearchNotificationPrimaryText((ViewGroup) gp.getChildAt(i), searchText);
            }
        }
        return null;
    }
    
    //vivo X3t等手机会自己绘制通知栏背景(使用.9pitch)，并且字体颜色不是通过style获取的，只能通过取背景色来判断灰度，做简单处理，详见BUG74217 
    private static Integer preProcessNotificationColor(Context c) {
        //for compatibility 17  vivo定制版  L1E.DD.FW.F8.0813.V3.02  L1E
        //L1ETS.HYKJ3.Q.F9.1217.V3.05  M3
        String romDisplay = Build.DISPLAY;
        String model = Build.MODEL;
        if (!TextUtils.isEmpty(romDisplay) && !TextUtils.isEmpty(model)) {
            if ((romDisplay.equalsIgnoreCase("L1E.DD.FW.F8.0813.V3.02") && model.startsWith("vivo")) || 
                    romDisplay.equalsIgnoreCase("L1ETS.HYKJ3.Q.F9.1217.V3.05")) {
                return defaultCustomNotificationColor;
            }
        }
        try {
            Drawable notifBack = getRemoteViewRootBackGround(c);
            if (null != notifBack && !(notifBack instanceof ColorDrawable) && !(notifBack instanceof GradientDrawable)) {
                Bitmap bitmap = Bitmap.createBitmap(notifBack.getIntrinsicWidth(), notifBack.getIntrinsicHeight(),
                        notifBack.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
                boolean alphaAvailable = (notifBack.getOpacity() != PixelFormat.OPAQUE);
                if (null != bitmap) {
                    Canvas canvas = new Canvas(bitmap);
                    notifBack.setBounds(0, 0, notifBack.getIntrinsicWidth(), notifBack.getIntrinsicHeight());
                    notifBack.draw(canvas);
                    int height = bitmap.getHeight();
                    int width = bitmap.getWidth();
                    int count, redTotal, greenTotal, blueTotal, alphaTotal;
                    count = redTotal = greenTotal = blueTotal = alphaTotal = 0;
                    //需要判断长宽大于等于4
                    if (width >= 4 && height >= 4) {
                        for (int left = width / 4; left < width; left += width / 4) {
                            for (int top = height / 4; top < height; top += height / 4) {
                                count++;
                                redTotal += Color.red(bitmap.getPixel(left, top));
                                greenTotal += Color.green(bitmap.getPixel(left, top));
                                blueTotal += Color.blue(bitmap.getPixel(left, top));
                                if (alphaAvailable) {
                                    alphaTotal += Color.alpha(bitmap.getPixel(left, top));
                                }
                            }
                        }
                        if (count != 0) {
                            int gray = (redTotal / count * 299 + greenTotal / count * 587 + blueTotal / count * 114 + 500) / 1000;
                            //如果获取通知栏背景的颜色alpha值均非常小，则不予考虑，详解机型 GoDonie S4
                            if (!alphaAvailable || (alphaTotal / count) > DIVIDE_ALPHA_IGNORE) {
                                if (gray < DIVIDE_SHALLOW_COLOR) {
                                    return Color.WHITE;
                                } else {
                                    return Color.BLACK;
                                }
                            }
                        } //end count
                    }
                }//end null != bitmap
            }
        } catch (Throwable b) {
        }
        return adaptNotificationColorFromModel();
    }

    /**
     * 通过model来适配部分通知栏前景字体颜色
     * 
     * @return
     */
    private static Integer adaptNotificationColorFromModel() {
        String model = Build.MODEL;
        //note all model is lowercase
        final HashMap<String, ArrayList<Integer>> arrayModelVerifiednotificationColorRight = new HashMap<String, ArrayList<Integer>>();
        arrayModelVerifiednotificationColorRight.put("vivo x3t", null);
        arrayModelVerifiednotificationColorRight.put("vivo x909t", null);
        arrayModelVerifiednotificationColorRight.put("vivo s3", null);
        arrayModelVerifiednotificationColorRight.put("vivo v1", null);
        arrayModelVerifiednotificationColorRight.put("vivo s7i(t)", null);

        final HashMap<String, ArrayList<Integer>> arrayModelVerifiednotificationColorWrong = new HashMap<String, ArrayList<Integer>>();
        arrayModelVerifiednotificationColorWrong.put("bbk s6t", null);
        arrayModelVerifiednotificationColorWrong.put("bafei b610", null);
        ArrayList<Integer> sdkVersionArray = new ArrayList<Integer>();
        sdkVersionArray.add(15);
        arrayModelVerifiednotificationColorWrong.put("imi", sdkVersionArray);

        if (!TextUtils.isEmpty(model) && sysVersion >= 14) {
            String lowerModel = model.toLowerCase();
            if (lowerModel.startsWith("vivo")) {
                if (!arrayModelVerifiednotificationColorRight.containsKey(lowerModel)) {
                    return defaultCustomNotificationColor;
                } else if (arrayModelVerifiednotificationColorRight.get(lowerModel) != null) {
                    if (!arrayModelVerifiednotificationColorRight.get(lowerModel).contains(sysVersion)) {
                        return defaultCustomNotificationColor;
                    }
                }
                //else ok
            } else if (arrayModelVerifiednotificationColorWrong.containsKey(lowerModel)) {
                if (arrayModelVerifiednotificationColorWrong.get(lowerModel) == null) {
                    return defaultCustomNotificationColor;
                } else if (arrayModelVerifiednotificationColorWrong.get(lowerModel).contains(sysVersion)) {
                    return defaultCustomNotificationColor;
                }
                //else ok
            }
        }
        return null;
    }

    
    public static Drawable getRemoteViewRootBackGround(Context c) {
        Notification ntf = newNotification(c);
        Drawable notifBack = null;
        try {
            LinearLayout group = new LinearLayout(c);
            LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View resultEvent = inflater.inflate(ntf.contentView.getLayoutId(), group, false);
            if (null != resultEvent) {
                notifBack = resultEvent.getBackground().getCurrent();
            }
        } catch (Throwable e) {
        }
        try {
            if (null == notifBack) {
                int id = Resources.getSystem().getIdentifier("notification_bg", "drawable", "android");
                notifBack = c.getResources().getDrawable(id).getCurrent();
            }
        } catch (Throwable e) {
        }
        return notifBack;
    }

       
    // xuyingying:魅族手机上创建通知栏需要特殊适配，所以此处封装创建notification对象的方法；
    public static Notification newNotification(Context context) {
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(context.getString(R.string.app_name))
				.setContentText(context.getString(R.string.app_name)).build();
        
        // xuyingying:魅族手机发通知栏，必须使用该方法，经自己标示为 内部应用
        if (MeizuUtils.isMeizu()) {
            MeizuUtils.updateNotification(notification);
        }
        return notification;
    }
}

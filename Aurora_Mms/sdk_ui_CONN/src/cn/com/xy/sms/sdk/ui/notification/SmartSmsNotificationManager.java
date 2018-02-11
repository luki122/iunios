package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.ui.popu.util.XySdkUtil;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.sdk.util.XyUtil;
import cn.com.xy.sms.util.PublicInfoParseManager;
import cn.com.xy.sms.util.SdkCallBack;

public class SmartSmsNotificationManager {
	
	public static final int DUOQU_NOTIFICATION_LOGO_WIGHT =100;
	public static final int DUOQU_NOTIFICATION_LOGO_HEIGHT =100;
	/**
	 * @author xiaoyuan
	 * @param context
	 *            上下文
	 * @param msgId
	 *            消息id
	 * @param phoneNum
	 *            短信接入码
	 * @param smsCenterNum
	 *            接收短信的短信中心号码
	 * @param msg
	 *            短信内容
	 * @param smsReceiveTime
	 *            短信接收时间
	 * @param extendMap
	 *            拓展参数
	 * **/
	public static boolean callApiToNotification(Context context, long msgId,
			String phoneNum, String smsCenterNum, String msg,
			long smsReceiveTime, HashMap<String, String> extendMap) {
		try {
			Notification notification = new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.duoqu_mms_logo).setAutoCancel(true)
			.setDefaults(Notification.DEFAULT_ALL)
			.setPriority(NotificationCompat.PRIORITY_MAX).build();
			return callApiToNotification(context, msgId, phoneNum, smsCenterNum, msg, smsReceiveTime, notification, extendMap);
		} catch (Exception e) {
			return false;
		}
	}
	/**
	 * @author xiaoyuan
	 * @param context
	 *            上下文
	 * @param msgId
	 *            消息id
	 * @param phoneNum
	 *            短信接入码
	 * @param smsCenterNum
	 *            接收短信的短信中心号码
	 * @param msg
	 *            短信内容
	 * @param smsReceiveTime
	 *            短信接收时间
	 * @param Notification   对象
	 *       
	 * @param extendMap
	 *            拓展参数
	 * **/
	public static boolean callApiToNotification(Context context, long msgId,
			String phoneNum, String smsCenterNum, String msg,
			long smsReceiveTime,Notification notification, HashMap<String, String> extendMap) {
		try {
			// 调用api
			Map<String, Object> valueMap = XySdkUtil.getNotifyDataCacheByMsgId(
					msgId, true);
			if (valueMap != null) {
				return createNotification(context, msgId, phoneNum,
						msg, smsReceiveTime, valueMap,notification, extendMap);
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean createNotification(Context context, long msgId,
			String phoneNum, String msg, long smsReceiveTime,
			Map<String, Object> resultMap, HashMap<String, String> extend) {
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.duoqu_mms_logo).setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_ALL)
				.setPriority(NotificationCompat.PRIORITY_MAX)
				.build();
		return createNotification(context, msgId, phoneNum, msg,
				smsReceiveTime, resultMap, notification, extend);
	}

	public static boolean createNotification(Context context, long msgId,
			String phoneNum, String msg, long smsReceiveTime,
			Map<String, Object> resultMap, Notification notification,
			HashMap<String, String> extend) {
		if (!checkVaildData(context, msgId, phoneNum, msg, smsReceiveTime,
				resultMap, extend)) {
			return false;
		}
		bindSmartNotifyView(context, notification, (int) msgId,
        		null, resultMap, String.valueOf(msgId), phoneNum, smsReceiveTime, msg, extend);
		getNotificationManager(context).notify(
				Integer.parseInt(String.valueOf(msgId)), notification);
		return true;
	}

	private static NotificationManager getNotificationManager(Context context) {
		if (mNotifyManager == null) {
			mNotifyManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		return mNotifyManager;
	}

	private static NotificationManager mNotifyManager = null;

	private static boolean checkVaildData(Context context, long msgId,
			String phoneNum, String msg, long smsReceiveTime,
			Map<String, Object> resultMap, Map<String, String> extend) {
		if (context == null) {
			return false;
		}
		if (msgId == 0 || StringUtils.isNull(phoneNum)
				|| StringUtils.isNull(msg) || resultMap == null
				|| resultMap.size() == 0) {
			return false;
		}
		return true;
	}

	@SuppressLint("NewApi")
	public static boolean bindSmartNotifyView(Context context,Notification notification, 
			int notificationId,
			Bitmap avatar,
			Map<String,Object> smartResultMap,
			String msgId,
			String phoneNum,
			long smsReceiveTime,
			String msg,
			HashMap<String,String> extend) {
		if (smartResultMap == null) {
			return false;
		}
		if (avatar == null) {
			
			String iccid = XySdkUtil.getICCID(context);
			avatar = getDefaultLogo(context,smartResultMap,extend);
		}
		try {
			notification.headsUpContentView  = SmartSmsNotificationManager.getFloatContentView(context, notificationId, avatar, smartResultMap, msgId, phoneNum, smsReceiveTime, msg, extend);
			notification.contentView = SmartSmsNotificationManager.getFloatContentView(context, notificationId, avatar, smartResultMap, msgId, phoneNum, smsReceiveTime, msg, extend);
			notification.bigContentView = SmartSmsNotificationManager.getBigContentView(context, notificationId, avatar, smartResultMap, msgId, phoneNum, smsReceiveTime, msg, extend);
			if (notification.headsUpContentView!=null
					&&notification.contentView != null
					&& notification.bigContentView != null) {
				return true;
			}
		} catch (Exception e) {
			// SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsNotificationManager.bindSmartNotifyView error: "+e.getMessage(),
			// e);
			e.printStackTrace();
		}
		return false;
	}

	public static boolean bindSmartNotityDropContentView(Context context,Notification notification, 
			int notificationId,
			Bitmap avatar,
			Map<String,Object> smartResultMap,
			String msgId,
			String phoneNum,
			long smsReceiveTime,
			String msg,
			HashMap<String,String> extend) {
		try {
			if (smartResultMap != null) {
				notification.contentView = SmartSmsNotificationManager
						.getContentView(context, notificationId, avatar, smartResultMap, msgId, phoneNum, smsReceiveTime, msg, extend);
				notification.bigContentView = SmartSmsNotificationManager
						.getBigContentView(context, notificationId, avatar, smartResultMap, msgId, phoneNum, smsReceiveTime, msg, extend);
				if (notification.contentView != null
						&& notification.bigContentView != null) {
					return true;
				}
			}
		} catch (Exception e) {
			// SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsNotificationManager.bindSmartNotityDropContentView error: "+e.getMessage(),
			// e);
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * judge valueMap whether enable
	 * 
	 * @param valueMap
	 * @return
	 */
	// private static boolean isEnableResult(Map<String, Object> valueMap) {
	// if (valueMap != null && valueMap.containsKey("Result")) {
	// return (Boolean) valueMap.get("Result");
	// }
	// return false;
	// }
	/**
	 * get duoqu contentView
	 * 
	 * @param context
	 * @param number
	 * @param msg
	 * @return
	 */
	private static RemoteViews getFloatContentView(Context context,
			int notificationId,
			Bitmap avatar,
			Map<String,Object> smartResultMap,
			String msgId,
			String phoneNum,
			long smsReceiveTime,
			String msg,
			HashMap<String,String> extend) {
		
		
		// if (isEnableResult(valueMap)) {
		return DuoquNotificationViewManager.getContentView(context, msgId,
				phoneNum, msg, smartResultMap, extend, avatar,
				DuoquNotificationViewManager.TYPE_BIG_CONTENT);
		// }
		// return null;
	}

	/**
	 * get duoqu contentView
	 * 
	 * @param context
	 * @param number
	 * @param msg
	 * @return
	 */
	private static RemoteViews getContentView(Context context,
			int notificationId,
			Bitmap avatar,
			Map<String,Object> smartResultMap,
			String msgId,
			String phoneNum,
			long smsReceiveTime,
			String msg,
			HashMap<String,String> extend) {
		// if (isEnableResult(valueMap)) {
		
		
		return DuoquNotificationViewManager.getContentView(context, msgId,
				phoneNum, msg, smartResultMap, extend, avatar,
				DuoquNotificationViewManager.TYPE_BIG_CONTENT);
		// }
		// return null;
	}

	/**
	 * get duoqu bigContentView
	 * 
	 * @param context
	 * @param number
	 * @param msg
	 * @return
	 */
	private static RemoteViews getBigContentView(Context context,
			int notificationId,
			Bitmap avatar,
			Map<String,Object> smartResultMap,
			String msgId,
			String phoneNum,
			long smsReceiveTime,
			String msg,
			HashMap<String,String> extend) {
		return DuoquNotificationViewManager.getContentView(context, msgId,
				phoneNum, msg, smartResultMap, extend, avatar,
				DuoquNotificationViewManager.TYPE_BIG_CONTENT);

	}

	public static void doNotifyAction(Context context, Intent intent) {
		if (context == null || intent == null) {
			// SmartSmsSdkUtil.smartSdkExceptionLog("SmartSmsNotificationManager.doNotifyAction intent or context is null",
			// null);
			return;
		}
		String actionData = intent.getStringExtra("actionData");
		if (!StringUtils.isNull(actionData)) {
			HashMap<String, String> valueMap = new HashMap<String, String>();
			// put extend data
			DuoquUtils.doActionContext(context, actionData, valueMap);
		}
	}

	public static Bitmap getDefaultLogo(final Context ctx,
			final Map<String, Object> smartResultMap,final Map<String, String> extend) {
		
		String phoneNum = (String) smartResultMap.get("phoneNum");
		
		Drawable drawable = PublicInfoParseManager.queryLogoByPhone(ctx,
				phoneNum, 1, 2, XyUtil.getIccid(), SmartSmsNotificationManager.DUOQU_NOTIFICATION_LOGO_WIGHT,
				SmartSmsNotificationManager.DUOQU_NOTIFICATION_LOGO_HEIGHT, extend,
				null);

		if (drawable == null) {
			drawable = getLocalDrawableByNumber(ctx, phoneNum, smartResultMap);
		} else {
			drawable = (getRoundedCornerBitmap((BitmapDrawable) drawable));
			if (drawable != null) {
				return ((BitmapDrawable) drawable).getBitmap();
			} else {
				drawable = getLocalDrawableByNumber(ctx, phoneNum,
						smartResultMap);
			}
		}
		Bitmap bitmap = null;
		if (drawable != null) {
			bitmap = ((BitmapDrawable) drawable).getBitmap();
		}
		return bitmap;
	}
	
	public static Drawable getLocalDrawableByNumber(Context context,String number,
			Map<String, Object> smartResultMap) {
		String titleNum = (String) smartResultMap.get("title_num");
		Drawable logoDrawable = context.getResources().getDrawable(
				R.drawable.duoqu_mms_logo);
		if (titleNum.startsWith("00")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_00);
		} else if (titleNum.startsWith("01")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_01);
		} else if (titleNum.startsWith("02")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_02);
		} else if (titleNum.startsWith("03")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_03);
		} else if (titleNum.startsWith("04")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_04);
		} else if (titleNum.startsWith("05")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_05);
		} else if (titleNum.startsWith("08")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_08);
		} else if (titleNum.startsWith("11")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_11);
		} else if (titleNum.startsWith("12")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_12);
		} else if (titleNum.startsWith("13")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_13);
		}else if (titleNum.startsWith("14")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_14);
		} else if (titleNum.startsWith("15")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_15);
		}	else if (titleNum.startsWith("16")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_16);
		}else if (titleNum.startsWith("17")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_17);
		}else if (titleNum.startsWith("18")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_18);
		}else if (titleNum.startsWith("19")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_19);
		}else if (titleNum.startsWith("0501")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_051);
		}else if (titleNum.startsWith("0502")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_052);
		}else if (titleNum.startsWith("0601")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_0601);
		} else if (titleNum.startsWith("06201")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_06201);
		} else if (titleNum.startsWith("06301")) {
			logoDrawable = context.getResources().getDrawable(
					R.drawable.duoqu_default_logo_06301);
		}
		return logoDrawable;
	}

	 //获得圆角图片的方法
    public static BitmapDrawable getRoundedCornerBitmap(
    		BitmapDrawable bitmapDrawable) {
        if (bitmapDrawable == null)
            return null;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null)
            return null;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // Bitmap output=getFactoryBitmap(bitmap,width,height);
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        float roundPx = width / 2;
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        // 使图像内存变小
        // output=getFactoryBitmap(output,width,height);
        return new BitmapDrawable(output);
    }
}

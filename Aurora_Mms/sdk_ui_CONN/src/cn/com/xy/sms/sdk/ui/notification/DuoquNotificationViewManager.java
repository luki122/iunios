package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.log.LogManager;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
//import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.util.StringUtils;

public class DuoquNotificationViewManager {

    private static NotificationManager mNotifyManager = null;
    public static final int BUTTON_ONE_CLICK_ACTION = 1;
    public static final int BUTTON_TWO_CLICK_ACTION = 2;
    public static final int NOFITY_CLICK_ACTION = 0;
    public static final int TYPE_FLOAT = 1;
    public static final int TYPE_CONTENT = 2;
    public static final int TYPE_BIG_CONTENT = 3;
    
    public static RemoteViews getContentView(Context context,
            String msgId, String phoneNum, String msg, Map<String, Object> resultMap, 
            HashMap<String, String> extend, Bitmap avatar, int viewType) {
        String mTitle = (String) resultMap.get("view_content_title");
        String mText = (String) resultMap.get("view_content_text");
        LogManager.d("xiaoyuan_notify","mTitle : "+mTitle+" mText: "+mText);
        if (StringUtils.isNull(mTitle)) {
            LogManager.d("xiaoyuan_notify","mTitle IS NULL");
            return null;
        }
        if (StringUtils.isNull(mText)) {
         
            mText = msg.trim();
        }
        BaseNotificationView dropView = null;
//        if(viewType == TYPE_FLOAT){
//            dropView = new PopupNotificationView();
//        }else 
        if(viewType == TYPE_FLOAT||viewType == TYPE_CONTENT || viewType == TYPE_BIG_CONTENT){
            dropView = new DropNotificationView();
        }else{
            LogManager.d("xiaoyuan_notify NOT FOUND viewType "+viewType, null);
            return null;
        }
        RemoteViews remoteView = dropView.getRemoteViews(context);
        
        if(remoteView != null){
            LogManager.d("xiaoyuan_notify","getContentView remoteView is not null.");
            int notificationId = 0;
            String smsReceiveTime = null;
            try {
                if(extend!=null && !StringUtils.isNull(extend.get("notificationId"))){
                	notificationId = Integer.valueOf(extend.get("notificationId"));	  
                }else{
                	notificationId = Integer.valueOf(msgId);
                }
                if(extend!=null && !StringUtils.isNull(extend.get("smsReceiveTime"))){
                	smsReceiveTime =  ContentUtil.getTimeText(context, Long.valueOf(extend.get("smsReceiveTime")));  
                }
                
			} catch (Exception e) {
				e.printStackTrace();
			}
  
            dropView.bindViewData(
                    context,
                    notificationId,
                    msgId,
                    phoneNum,
                    smsReceiveTime,
                    avatar,
                    mTitle,
                    mText,
                    getButtonName(resultMap),viewType,extend);
        }else{
            LogManager.d("xiaoyuan_notify getContentView remoteView is null.", null);
        }
        
        return remoteView;
    }

    private static JSONArray getButtonName(Map<String, Object> map) {
        if (map == null) {
            return new JSONArray();
        }
        try {
            String adAction = (String) map.get("ADACTION");
            if (!StringUtils.isNull(adAction)) {
                 return new JSONArray(adAction);
            }
        } catch (Exception e) {
        	e.printStackTrace();
//            SmartSmsSdkUtil.smartSdkExceptionLog("DuoquNotificationViewManager.getButtonName ERROR: "+e.getMessage(), e);
        }
        return new JSONArray();
    }

    private static NotificationManager getNotificationManager(Context context) {
        if (mNotifyManager == null) {
            mNotifyManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotifyManager;
    }

    public static void cancelNotification(Context context, int cancelId) {
        if (cancelId != 0) {
            getNotificationManager(context).cancel(cancelId);
        }
    }

}

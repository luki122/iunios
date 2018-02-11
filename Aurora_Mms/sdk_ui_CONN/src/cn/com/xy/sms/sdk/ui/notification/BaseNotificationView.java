package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;
import cn.com.xy.sms.sdk.R;
import cn.com.xy.sms.sdk.util.StringUtils;

public abstract class BaseNotificationView {
    public static final int REQUEST_TYPE_LAYOUT= 0;
    public static final int REQUEST_TYPE_BTN_ONE_CLICK= 1;
    public static final int REQUEST_TYPE_BTN_TWO_CLICK= 2;
	public static final int DUOQU_ACTION_DATA_TYPE = 0;
	public static final int DUOQU_MSGID_DATA_TYPE = 1;
	public static final int DUOQU_NO_DATA_TYPE = 2;
    private static int mRequestBtnOneClick = 100000;
    private static int mRequestBtnTwoClick = 200000;
    private static int mRequestLayoutClick = 300000;
    protected RemoteViews mRemoteViews = null;
    protected int mLayoutId = -1;

    public BaseNotificationView(int layoutId){
        this.mLayoutId = layoutId;
    }
    public RemoteViews getRemoteViews(Context ctx) {
        if(mLayoutId == -1){
            return null;
        }
        mRemoteViews = new RemoteViews(ctx.getPackageName(), mLayoutId);
        return mRemoteViews;
    }

    public void bindViewData(Context ctx, int notificationId, String msgId,String phoneNum, String smsReceiveTime, Bitmap logoBitmap,
            String contentTitle, String contentText, JSONArray actionJsons,int viewType,HashMap<String,String> extend) {
        setContentText(ctx,logoBitmap,contentTitle,contentText,smsReceiveTime);
        bindBtnView(ctx,notificationId,msgId,phoneNum,actionJsons,viewType,extend);
    }
    protected void bindBtnView(Context ctx,int notificationId,String msgId,String phoneNum,JSONArray actionJsons,int viewType,HashMap<String,String> extend) {
        
    }
    protected void setContentText(Context ctx, Bitmap logoBitmap,
            String contentTitle, String contentText, String smsReceiveTime){
          mRemoteViews.setImageViewBitmap(R.id.duoqu_logo_img, logoBitmap);
          mRemoteViews.setTextViewText(R.id.duoqu_sms_receive_time, smsReceiveTime);
          mRemoteViews.setTextViewText(R.id.duoqu_content_title, contentTitle);
          mRemoteViews.setTextViewText(R.id.duoqu_content_text, contentText);
    }
    protected void setButtonListener(Context ctx,int notificationId,String msgId,String phoneNum,int viewId,int requestCode,JSONObject action,int dataType,HashMap<String,String> extend){
        mRemoteViews.setOnClickPendingIntent(viewId,getNotifyActionIntent(ctx,notificationId,msgId,phoneNum,requestCode,action == null ? null:action.optString("action_data"),dataType,extend));
    }
    protected int getRequestCode(int requestType){
        int res = 0;
        if( REQUEST_TYPE_LAYOUT ==requestType){
            if (mRequestLayoutClick == 399999) {
                mRequestLayoutClick = 300000;
            }else{
                mRequestLayoutClick++;
            }
            res = mRequestLayoutClick;
        }
        else if( REQUEST_TYPE_BTN_TWO_CLICK  ==requestType){
            if (mRequestBtnTwoClick == 299999) {
            	mRequestBtnTwoClick = 200000;
            }else{
            	mRequestBtnTwoClick++;
            }
            res = mRequestBtnTwoClick;
        }
        else if( REQUEST_TYPE_BTN_ONE_CLICK  ==requestType){
        	if (mRequestBtnOneClick == 199999) {
        		mRequestBtnOneClick = 100000;
        	}else{
        		mRequestBtnOneClick++;
        	}
        	res = mRequestBtnOneClick;
        }
        return res;
    }
    protected PendingIntent getNotifyActionIntent(Context context,int notificationId,String msgId,String phoneNum,int id,
            String actionData,int dataType,HashMap<String,String> extend) {
//    	Log.i("yangzhi", "getNotifyActionIntent id:"+id);
        Intent contentIntent = new Intent();
        contentIntent.setClassName(context,"cn.com.xy.sms.sdk.ui.notification.DoActionActivity");
        if(!StringUtils.isNull(actionData)){
        	contentIntent.putExtra("actionData", actionData);
        }
        if(extend==null){
        	extend = new HashMap<String, String>();
        }
        contentIntent.putExtra("notificationId", notificationId);
        contentIntent.putExtra("msgId", msgId);
        contentIntent.putExtra("phoneNum", phoneNum);
        contentIntent.putExtra("dataType", dataType);
        contentIntent.putExtra("extend", extend);
        PendingIntent pendIntent = PendingIntent.getActivity(context, id,contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendIntent;
    }

    /**
     * 获取按钮名称
     * @param btnDataJson
     * @return
     */
    protected static String getButtonName(JSONObject btnDataJson) {
        if (btnDataJson == null) {
            return "";
        }

        String buttonName = btnDataJson.optString("btn_short_name");
        if (StringUtils.isNull(buttonName)) {
            buttonName = btnDataJson.optString("btn_name");
        }
        return buttonName;
    }
}

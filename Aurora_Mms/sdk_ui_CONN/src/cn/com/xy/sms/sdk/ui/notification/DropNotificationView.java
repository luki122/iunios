package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;
import org.json.JSONArray;

import android.content.Context;
import android.view.View;
import cn.com.xy.sms.sdk.R;

public class DropNotificationView extends BaseNotificationView {

    public DropNotificationView() {
        super(R.layout.duoqu_drop_notification);
    }
    
    @Override
    public void bindBtnView(Context ctx, int notificationId, String msgId,
			String phoneNum, JSONArray actionJsons, int viewType,
			HashMap<String, String> extend) {
	     try{
	    	 String hasReadButton = "";
	    	 if(extend != null && !extend.isEmpty() && extend.containsKey("hasReadButton")){
	    		 hasReadButton = (String) extend.get("hasReadButton");
	    	 }
	    	 // the layout click event
	    	 setButtonListener(ctx,notificationId,msgId,phoneNum,R.id.duoqu_drop_notify_layout, getRequestCode(REQUEST_TYPE_LAYOUT), null, BaseNotificationView.DUOQU_NO_DATA_TYPE,extend);
	    	 // at least has one button
	    	 if("true".equals(hasReadButton) || actionJsons.length() > 0){
	    		 if(viewType != DuoquNotificationViewManager.TYPE_CONTENT){
	    			 mRemoteViews.setViewVisibility(R.id.duoqu_drop_split_line, View.VISIBLE);
	    			 mRemoteViews.setViewVisibility(R.id.duoqu_drop_btn_ll, View.VISIBLE);
	    		 }
	    		 if("true".equals(hasReadButton)){
	    			 mRemoteViews.setViewVisibility(R.id.duoqu_drop_btn_one_ll, View.VISIBLE);
	    			 mRemoteViews.setTextViewText(R.id.duoqu_drop_btn_one,ctx.getResources().getString(R.string.duqou_mark_btn_name));
	    			 setButtonListener(ctx,notificationId,msgId,phoneNum,R.id.duoqu_drop_btn_one_ll, getRequestCode(REQUEST_TYPE_BTN_ONE_CLICK), null, BaseNotificationView.DUOQU_MSGID_DATA_TYPE,extend);
	    		 }
	    		 if(actionJsons.length() > 0){
	    			 mRemoteViews.setViewVisibility(R.id.duoqu_drop_btn_two_ll, View.VISIBLE);
	    			 mRemoteViews.setTextViewText(R.id.duoqu_drop_btn_two,super.getButtonName(actionJsons.optJSONObject(0)));	             
	    			 setButtonListener(ctx,notificationId,msgId,phoneNum,R.id.duoqu_drop_btn_two_ll, getRequestCode(REQUEST_TYPE_BTN_TWO_CLICK), actionJsons.optJSONObject(0),BaseNotificationView.DUOQU_ACTION_DATA_TYPE,extend);
	    		 }
	    	 }
	     }catch(Exception e){
//	         e.printStackTrace();
	     }
	}
}

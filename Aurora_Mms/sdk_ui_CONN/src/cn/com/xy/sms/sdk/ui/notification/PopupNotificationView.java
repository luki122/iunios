package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;

import org.json.JSONArray;






import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import cn.com.xy.sms.sdk.R;

public class PopupNotificationView extends BaseNotificationView {

    public PopupNotificationView() {
        super(R.layout.duoqu_popup_notification);
    }
  
    @Override
    public void bindBtnView(Context ctx,int notificationId,String msgId,String phoneNum, JSONArray actionJsons,int viewType,HashMap<String, String> extend) {
    	
    }
}

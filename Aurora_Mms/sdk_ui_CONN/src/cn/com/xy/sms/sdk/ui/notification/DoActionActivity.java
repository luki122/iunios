package cn.com.xy.sms.sdk.ui.notification;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import cn.com.xy.sms.sdk.util.DuoquUtils;
import cn.com.xy.sms.sdk.util.StringUtils;

public class DoActionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String actionData = getIntent().getStringExtra("actionData");
        int notificationId = getIntent().getIntExtra("notificationId", 0);
        String msgId = getIntent().getStringExtra("msgId");
        String phoneNum = getIntent().getStringExtra("phoneNum");
        int dataType = getIntent().getIntExtra("dataType", 0);
        HashMap<String, String> extend = null;
        try {
            extend = (HashMap<String, String>) getIntent().getSerializableExtra("extend");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (extend == null) {
            extend = new HashMap<String, String>();
        }
        
		if (dataType == BaseNotificationView.DUOQU_ACTION_DATA_TYPE ||
				dataType == BaseNotificationView.DUOQU_MSGID_DATA_TYPE) {
			if (!StringUtils.isNull(actionData)) {
				// put extend data
				DuoquUtils.doActionContext(this, actionData, extend);
			}
			DuoquUtils.getSdkDoAction().markAsReadForDatabase(this,
					String.valueOf(msgId));
		} else if (dataType == BaseNotificationView.DUOQU_NO_DATA_TYPE) {
			DuoquUtils.getSdkDoAction().openSms(this, phoneNum, extend);
		}
		
		DuoquNotificationViewManager.cancelNotification(this, notificationId);
        this.finish();
    }
}

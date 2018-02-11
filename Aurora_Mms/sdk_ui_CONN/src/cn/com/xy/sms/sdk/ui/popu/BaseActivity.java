
package cn.com.xy.sms.sdk.ui.popu;

import android.app.Activity;
import android.os.Bundle;
import cn.com.xy.sms.sdk.log.TimeLog;
 

public class BaseActivity extends Activity{

	public cn.com.xy.sms.sdk.log.TimeLog timeLog = new TimeLog();
    

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        initBefore(savedInstanceState);

        setView();
        try {
            initAfter();
        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public void initBefore(Bundle savedInstanceState) {

    }

    public void initAfter() {

    }

    public boolean isBl() {
        return true;
    }

    public void setView() {
        setContentView(getLayoutId());

    }

    public int getLayoutId() {
        return 0;
    }

    public Activity getActivity() {
        return BaseActivity.this;
    }
 
 
}

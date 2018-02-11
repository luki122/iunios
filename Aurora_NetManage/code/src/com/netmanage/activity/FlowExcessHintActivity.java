package com.netmanage.activity;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.aurora.netmanage.R;
import com.netmanage.utils.NetworkUtils;

/**
 * 流量超额提示对话框
 * @author chengrq
 *
 */
public class FlowExcessHintActivity extends Activity implements OnClickListener{	

	private boolean isWifiConnection = false;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.aurora_alert_dialog_internal); 
        
        ((TextView)findViewById(R.id.aurora_alertTitle)).setText(R.string.flow_excess_hint); 
  
        Button button1 = ((Button)findViewById(R.id.button1));  
        button1.setText(R.string.continue_to_use); 
        button1.setOnClickListener(this); 
        
        Button button2 = ((Button)findViewById(R.id.button2)); 
    	button2.setText(R.string.close_2G_3G_net); 
    	button2.setOnClickListener(this); 
    	
    	((TextView)findViewById(R.id.message)).setText(R.string.flow_excess_hint_msg);    
    }
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.button1:	        	
				finish();
				break;
			case R.id.button2:
				NetworkUtils.setMobileDataEnabled(this,false);
				finish();
				break;
		}	
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


}
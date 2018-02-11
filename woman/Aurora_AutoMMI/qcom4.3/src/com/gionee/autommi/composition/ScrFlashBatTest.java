package com.gionee.autommi.composition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.gionee.autommi.AutoMMI;
import com.gionee.autommi.BaseActivity;
import com.gionee.autommi.BatteryTest;
import android.os.Handler;

public class ScrFlashBatTest extends BaseActivity {
	
	private BroadcastReceiver battInfoRec;
	private String[] result = new String[4];
	private boolean[] flag = new boolean[2];
    private static final String chargeCurNodePath = "/sys/class/power_supply/battery/current_now";
    private static final String charegeVolNodepath = "/sys/class/power_supply/battery/charge_voltage_now";
    
    private Camera mCamera;
    Camera.Parameters mParameters = null;
    
    View mColorView;
    WindowManager.LayoutParams mlp;
    
    Handler mHandler = new Handler() {
    	public void handleMessage(Message msg) {
			getChargeInfo();		
			if(flag[0] && flag[1]) {
				String content = "";
				for(int i = 0 ; i <  result.length; i++) {
					content += result[i];
                    content += (i == result.length - 1) ? "" :"|";
				}
				((AutoMMI)getApplication()).recordResult(BatteryTest.TAG, content, "2");
			}
    	}
    };
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		battInfoRec = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
					Integer a = intent.getIntExtra(BatteryManager.EXTRA_STATUS,  -1);
					Integer b = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
					result[0] = a.toString();
					result[1] = b.toString();
					flag[0] = true;
                    mHandler.sendEmptyMessageDelayed(0, 1000);
				}	
			}
		};
		
        mColorView = new View(this);
        mlp = getWindow().getAttributes();
        mlp.screenBrightness = 0.75f;
        getWindow().setAttributes(mlp);
        mColorView.setBackgroundColor(Color.WHITE);
        setContentView(mColorView);
	}

	protected void getChargeInfo() {
		String c = extractNodeInfo(chargeCurNodePath);
		int l = c.length();
		if ( l > 3) {
			if (c.startsWith("-"))
				c = c.substring(1, l - 3);
			else
				c = c.substring(0,l - 3);
		}
		
		String v = extractNodeInfo(charegeVolNodepath);
		l = v.length();
		if (l > 4) {
			v = v.substring(0, l - 3);
		}
		
		if(c != null && v != null ) {
			result[2] = c;
			result[3] = v;
			flag[1] = true;
		} else {
			flag[1] = false;
		}
	}

	private String extractNodeInfo(String path) {
	    try {
			InputStream is = new FileInputStream(path);
			int len = is.available();
			byte[] bytes = new byte[len];
			is.read(bytes);
			return new String(bytes).trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		openFlash();
		this.registerReceiver(battInfoRec, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		this.unregisterReceiver(battInfoRec);
		this.finish();
	}

    public void openFlash() {
        if (null == mCamera) {
            try {
                mCamera = Camera.open();     
                mParameters = mCamera.getParameters();
                String currFlashMode = mParameters.getFlashMode();
                if (currFlashMode == null
                        || (!currFlashMode.equals(Camera.Parameters.FLASH_MODE_TORCH))) {
                    mParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    mCamera.setParameters(mParameters);
                    mCamera.startPreview();
                } 
             }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

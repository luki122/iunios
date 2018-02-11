
package gn.com.android.mmitest.item;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
//Gionee xiaolin 20120604 add for CR00556847 start
import android.os.FileObserver;
//Gionee xiaolin 20120604 add for CR00556847 end
import gn.com.android.mmitest.Model;
import android.os.SystemProperties;

public class BatteryTest extends Activity implements OnClickListener {
    private Resources mRs;

    private TextView mTitleTv, mContentTv;

    private Button mRightBtn, mWrongBtn, mRestartBtn;

    private static final String TAG = "BatteryTest";
    
    private Intent lastBatteryData ;
    private  Model mModel;    

    // Gionee xiaolin 20120629 add for CR00556847 start
    private boolean stopQuery;
    // Gionee xiaolin 20120629 add for CR00556847 end
    
    //Gionee xiaolin 20120604 modify for CR00556847 start
    private FileObserver fo;
    
    // Gionee xiaolin 20120629 add for CR00556847 start
    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (stopQuery)
                return;
            
            switch(msg.what){
                case 0:
                    exBatInfo(lastBatteryData);
                    this.sendEmptyMessageDelayed(0, 1000);
                    break;
            }
        }
    };
    // Gionee xiaolin 20120629 add for CR00556847 end

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            lastBatteryData = intent;          
            exBatInfo(intent);
        }

    };
     
    private void exBatInfo(Intent intent) {
    	// Gionee xiaolin 20130228 add for CR00774074 start
    	if (null == intent)
    		return;
    	// Gionee xiaolin 20130228 add for CR00774074 end
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {

            int status = intent.getIntExtra("status", 0);
            int health = intent.getIntExtra("health", 0);
            boolean present = intent.getBooleanExtra("present", false);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 0);
            int icon_small = intent.getIntExtra("icon-small", 0);
            int plugged = intent.getIntExtra("plugged", 0);
            Log.d(TAG, "plugged:"+plugged);
            int voltage = intent.getIntExtra("voltage", 0);
            int temperature = intent.getIntExtra("temperature", 0)/10;
            String technology = intent.getStringExtra("technology");

            String statusString = "";
            switch (status) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    statusString = mRs.getString(R.string.battery_status_unknow);
                    break;
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    statusString = mRs.getString(R.string.battery_status_charging);
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    statusString = mRs.getString(R.string.battery_status_discharging);
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    statusString = mRs.getString(R.string.battery_status_nocharging);
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    statusString = mRs.getString(R.string.battery_status_full);
                    break;
            }

            String healthString = "";

            switch (health) {
                case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                    healthString = mRs.getString(R.string.battery_health_unknow);
                    break;
                case BatteryManager.BATTERY_HEALTH_GOOD:
                    healthString = mRs.getString(R.string.battery_health_good);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                    healthString = mRs.getString(R.string.battery_health_overheart);
                    break;
                case BatteryManager.BATTERY_HEALTH_DEAD:
                    healthString = mRs.getString(R.string.battery_health_dead);
                    break;
                case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                    healthString = mRs.getString(R.string.battery_health_over_voltage);
                    break;
                case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                    healthString = mRs.getString(R.string.battery_health_unspecified_failure);
                    break;
            }

            String acString = mRs.getString(R.string.no_battery_plugged);
            //Gionee <bug> <zhangxiaowei> <20131013> modify for CR00919802 begin
            if (0 == plugged) {
                mRightBtn.setEnabled(false);
            } else {
                mRightBtn.setEnabled(true);
            }
            //Gionee <bug> <zhangxiaowei> <20131013> modify for CR00919802 end
            switch (plugged) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    acString = mRs.getString(R.string.battery_plugged_ac);
                    break;
                case BatteryManager.BATTERY_PLUGGED_USB:
                    acString = mRs.getString(R.string.battery_plugged_usb);
                    break;
            }
            String content = "";
            String chargeVoltage1 = getChargeVoltage();
            int leng1 = chargeVoltage1.length();
    	    String chargeVoltage = "";
    	    if(leng1 > 3) {
    	    	chargeVoltage = chargeVoltage1.substring(0,leng1-3);
    	    }else {
    	    	chargeVoltage = chargeVoltage1;
			}
            String chargeCurrent1 = getChargeCurrent();
    	    int leng = chargeCurrent1.length();
    	    String chargeCurrent = "";
    	    if(leng > 3) {
    	    	chargeCurrent = chargeCurrent1.substring(1,leng-3);
    	    }else {
    	    	chargeCurrent = chargeCurrent1;
			}
    	    Log.i(TAG, "chargeCurrent = " + chargeCurrent);
            Log.i(TAG, "chargeVoltage = " + chargeVoltage);
			
			
		    if(mModel.isU2()){
			     voltage = Integer.parseInt(getVoltage())/1000;	
				 String[] ary = chargeVoltage.split(" ");
				 chargeVoltage = ary[0].substring(7); 
				 chargeVoltage = " " +Integer.parseInt(chargeVoltage)/1000;
			}
			
            if (plugged != 0 && chargeVoltage != null) {
                // Gionee <xiaolin><2013-3-27> modify for CR00789808 start
                content = mRs.getString(R.string.battery_health) + healthString + "\n"
                        + mRs.getString(R.string.battery_plugged) + acString + "\n"
                        + mRs.getString(R.string.charge_v) + chargeVoltage + " mV\n";

               if( chargeCurrent != null) 
                   content += mRs.getString(R.string.charge_i) + chargeCurrent + " mA\n";

                 content += mRs.getString(R.string.battery_v) + voltage + " mV\n"
                        + mRs.getString(R.string.battery_capacity) + scale + "\n"
                        + mRs.getString(R.string.battery_power) + level + "\n"
                        + mRs.getString(R.string.battery_technology) + technology + "\n"
                        + mRs.getString(R.string.battery_temperature) + temperature+mRs.getString(R.string.temperature);
                 
                // Gionee <xiaolin><2013-3-27> modify for CR00789808 end

            } else {
                content = mRs.getString(R.string.battery_health) + healthString + "\n"
                        + mRs.getString(R.string.battery_plugged) + acString + "\n"
                        + mRs.getString(R.string.battery_v) + voltage + " mV\n"
                        + mRs.getString(R.string.battery_capacity) + scale + "\n"
                        + mRs.getString(R.string.battery_power) + level + "\n"
                        + mRs.getString(R.string.battery_technology) + technology+ "\n"
                        + mRs.getString(R.string.battery_temperature) + temperature+mRs.getString(R.string.temperature);
            }
            mTitleTv.setText(mRs.getString(R.string.battery_status) + statusString);
            mContentTv.setText(content);
          //Gionee <bug> <zhangxiaowei> <20131013> modify for CR00919802 begin 

           // mRightBtn.setEnabled(true);
            
          //Gionee <bug> <zhangxiaowei> <20131013> modify for CR00919802 end 

        }
    }
    //Gionee xiaolin 20120604 modify for CR00556847 end
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED; 
      //  lp.dispatchAllKey = 1;
        getWindow().setAttributes(lp);

        mRs = getResources();
        setContentView(R.layout.common_textview);
        mTitleTv = (TextView) findViewById(R.id.test_title);
        mContentTv = (TextView) findViewById(R.id.test_content);
        mWrongBtn = (Button) findViewById(R.id.wrong_btn);
        mRightBtn = (Button) findViewById(R.id.right_btn);
        mRightBtn.setOnClickListener(this);
        mWrongBtn.setOnClickListener(this);
        mRestartBtn = (Button) findViewById(R.id.restart_btn);
        mRestartBtn.setOnClickListener(this);
        mModel = Model.getInstance();

		String str = "/sys/class/power_supply/battery/charge_voltage_now" ;
		if(mModel.isU2()){
		    str = "/sys/class/power_supply/battery/charge_now";
		}
		
        //Gionee xiaolin 20120604 add for CR00556847 start
        fo = new FileObserver(str, FileObserver.MODIFY) {
            @Override
            public void onEvent(int event, String path) {
                // Gionee xiaolin 20120629 modify for CR00556847 start
                uiHandler.sendEmptyMessage(0);
                // Gionee xiaolin 20120629 modify for CR00556847 end
            }
        };
        //Gionee xiaolin 20120604 add for CR00556847 end
    }

    @Override
    public void onStart() {
        super.onStart();
        registerReceiver(mBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        // Gionee xiaolin 20120629 modify for CR00556847 start
        stopQuery = false;
        uiHandler.sendEmptyMessage(0);
        // Gionee xiaolin 20120629 modify for CR00556847 end
    }

    @Override
    public void onStop() {
        super.onStop();
        this.unregisterReceiver(mBroadcastReceiver);
        // Gionee xiaolin 20120629 modify for CR00556847 start
        stopQuery = true;
        uiHandler.removeMessages(0);
        // Gionee xiaolin 20120629 modify for CR00556847 end
        
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

            case R.id.right_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.rightPress(TAG, this);
                break;
            }

            case R.id.wrong_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.wrongPress(TAG, this);
                break;
            }
            
            case R.id.restart_btn: {
                mRightBtn.setEnabled(false);
                mWrongBtn.setEnabled(false);
                mRestartBtn.setEnabled(false);
                TestUtils.restart(this, TAG);
                break;
            }
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return true;
    }
    private String getRightFilename(String string){
    File file = new File(string);
    String filePath = null;
        if (!file.exists() || !file.isDirectory()) {
        return  filePath;  
        }
    File[] listFiles = file.listFiles();
        for (File child : listFiles) {
        filePath = child.getAbsolutePath().toLowerCase();
                if (filePath.contains("qpnp-vadc-")){
                return filePath;
                }
        }
    return filePath;
    }
	
    public String getChargeVoltage() {
        String changeVoltage = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
  		String mFileName;
		if(mModel.isU2()){
        		mFileName = getRightFilename("/sys/devices")+"/usb_in";
        	}else{
			    mFileName = "/sys/class/power_supply/battery/charge_voltage_now";
		}
		
		Log.v("gary_mmi","-----mFileName===="+mFileName);
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File voltageFilePath = new File(mFileName);
                if (voltageFilePath.exists()) {
                    fileInputStream = new FileInputStream(voltageFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        changeVoltage = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return changeVoltage;
    }
    
    public String getChargeCurrent() {
        String chargeCurrent = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/class/power_supply/battery/current_now";
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(mFileName);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        chargeCurrent = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chargeCurrent;
    }

        public String getVoltage() {
        String chargeCurrent = null;
        //Gionee xiaolin 20120302 modify for CR00535627 start
        String mFileName = "/sys/class/power_supply/battery/voltage_now";
        //Gionee xiaolin 20120302 modify for CR00535627 end
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader br = null;
        try {
            try {
                File currentFilePath = new File(mFileName);
                if (currentFilePath.exists()) {
                    fileInputStream = new FileInputStream(currentFilePath);
                    inputStreamReader = new InputStreamReader(fileInputStream);
                    br = new BufferedReader(inputStreamReader);
                    String data = null;
                    while ((data = br.readLine()) != null) {
                        chargeCurrent = data;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                if (br != null) {
                    br.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return chargeCurrent;
    }
    
}

package com.android.settings;

import java.io.File;
import java.io.FileOutputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AuroraSystemUIReceiver extends BroadcastReceiver {
	
	public static final String WRITE_FLASHLIGHT_BROADCAST = "com.android.systemui.writeflashlight";
	
	public static final String FLASH_FILE_PATH = "/sys/class/flashlightdrv/kd_camera_flashlight/torch";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(WRITE_FLASHLIGHT_BROADCAST)){
			writePreferenceClick(intent.getIntExtra("writeflashlight", 0),FLASH_FILE_PATH);
		}

	}
	
	public  void writePreferenceClick(int num, String filePath) {
    	FileOutputStream out = null;
    	File outFile = null;
    	try {
	    	outFile = new File(filePath);
	    	if(!outFile.exists()){
	    		return;
	    	}
	    	out = new FileOutputStream(outFile);

	    	out.write((num+"\n").getBytes());
	    
	    	if (null != out) {  
				out.flush();  
				out.close();
		    }
	    } catch (Exception e) {
	    
			e.printStackTrace();
		}
    }

}

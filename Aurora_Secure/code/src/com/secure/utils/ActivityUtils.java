package com.secure.utils;

import android.os.Handler;
import android.os.Message;

public class ActivityUtils {
    /**
     * 载入屏幕前先睡眠一段时间，这样activity切换动画更为流畅
     */
    public static void sleepForloadScreen(final long sleepTime,final LoadCallback callBack){   	    
		new Thread() {
			@Override
			public void run() {	
				try{
					Thread.sleep(sleepTime);
				}catch(Exception e){
					e.printStackTrace();
				}
				Message msg = new Message();
				msg.obj = callBack;
	            handler.sendMessage(msg);   
			}
		}.start();
    }
    
    private static Handler handler = new Handler() {
	    @Override
	    public void handleMessage(Message msg) {
	    	LoadCallback callBack = (LoadCallback)msg.obj;
	    	if(callBack != null){
	    		callBack.loaded();
	    	}
	   }
	};
    
    public interface LoadCallback {
		public void loaded();
	}
}

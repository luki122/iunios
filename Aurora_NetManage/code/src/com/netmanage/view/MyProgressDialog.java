package com.netmanage.view;

import android.app.ProgressDialog;
import android.content.Context;

public class MyProgressDialog {
	private ProgressDialog dialog = null;
	
	/**
	 * @param context
	 * @param title
	 * @param message
	 * @param Style
	 * @param cancel
	 */
    public void show(Context context,String title,String message,int Style ,boolean cancel){
    	if(dialog == null){			
    		dialog = new ProgressDialog(context);
    		dialog.setTitle(title);
    		dialog.setMessage(message);
    		dialog.setProgressStyle(Style);
    		dialog.setCancelable(cancel);
    		if(!dialog.isShowing()){
    			dialog.show();
    		}   		
    	}	
    }
    
  
    public void show(Context context,String title,String message){
    	show(context,title,message,ProgressDialog.STYLE_SPINNER,false);
    }
    
    public boolean isShowing(){
    	if(dialog != null){
    		return dialog.isShowing();
    	}else{
    		return false;
    	}
    }
    
    public void close(){
    	if(dialog != null){
    		if(dialog.isShowing()){
    			dialog.dismiss();
    		}  		
    		dialog = null;
    	}
    }
}

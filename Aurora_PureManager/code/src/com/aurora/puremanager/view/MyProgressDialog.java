package com.aurora.puremanager.view;

import android.content.Context;
import aurora.app.AuroraProgressDialog;

public class MyProgressDialog {
	private AuroraProgressDialog dialog = null;
	
	/**
	 * @param context
	 * @param title
	 * @param message
	 * @param Style
	 * @param cancel
	 */
    public void show(Context context,String title,String message,int Style ,boolean cancel){
    	if(dialog == null){			
    		dialog = new AuroraProgressDialog(context);
    		dialog.setTitle(title);
    		dialog.setMessage(message);
    		dialog.setProgressStyle(Style);
    		dialog.setCancelable(cancel);
    		if(!dialog.isShowing()){
    			try{
    				dialog.show();
    			}catch(Exception e){
    				e.printStackTrace();
    			}   			
    		}   		
    	}	
    }
    
  
    public void show(Context context,String title,String message){
    	show(context,title,message,AuroraProgressDialog.STYLE_SPINNER,false);
    }
    
    public void close(){
    	if(dialog != null){
    		if(dialog.isShowing()){
    			try{
    				dialog.dismiss();
    			}catch(Exception e){
    				e.printStackTrace();
    			}   			
    		}  		
    		dialog = null;
    	}
    }
}

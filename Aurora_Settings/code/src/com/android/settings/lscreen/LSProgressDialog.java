package com.android.settings.lscreen;

import android.app.ProgressDialog;
import android.content.Context;

public class LSProgressDialog{
	private ProgressDialog dialog;
	
    public void show(Context context,String title,String message){
    	this.show(context,title,message,ProgressDialog.STYLE_SPINNER,false);
    }

    /**
     * 显示加载界面
     *@param numble
     *@return
     *@exception 
     *@author penggangding
     *@Time
     */
    public void show(Context context,String title,String message,int Style ,boolean cancel){
    	if(dialog == null){			
    		dialog = new ProgressDialog(context);
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
    
    public void close()
    {
    	if(dialog!=null)
    	{
    		dialog.cancel();
    	}
    }


}

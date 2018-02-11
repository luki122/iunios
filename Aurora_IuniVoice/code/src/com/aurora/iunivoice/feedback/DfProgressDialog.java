package com.aurora.iunivoice.feedback;

import android.app.ProgressDialog;
import android.content.Context;

public class DfProgressDialog{
	private ProgressDialog dialog;
	
    public void show(Context context,String title,String message){
    	this.show(context,title,message,ProgressDialog.STYLE_SPINNER,false);
    }

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
    		dialog.dismiss();
    	}
    }


}

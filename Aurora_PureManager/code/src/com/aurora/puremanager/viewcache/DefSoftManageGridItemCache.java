package com.aurora.puremanager.viewcache;

import com.aurora.puremanager.R;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class DefSoftManageGridItemCache {
    private View baseView;	   
    private ImageView appIcon;
    private ImageView imgFlag;
    private View sysAppFlag;
    private FrameLayout itemLayout;
       
    public DefSoftManageGridItemCache(View baseView) {
        this.baseView = baseView;
    }
    
    public ImageView getAppIcon() {
        if (appIcon == null) {
        	appIcon = (ImageView) baseView.findViewById(R.id.appIcon);
        }
        return appIcon;
    }
    
    public ImageView getImgFlag(){
    	if(imgFlag == null){
    		imgFlag = (ImageView) baseView.findViewById(R.id.imgFlag);
    	}
    	return imgFlag;
    }
    
    public View getSysAppFlag(){
    	if(sysAppFlag == null){
    		sysAppFlag = baseView.findViewById(R.id.sysAppFlag);
    	}
    	return sysAppFlag;
    }
    
    public FrameLayout getItemLayout(){
    	if(itemLayout == null){
    		itemLayout = (FrameLayout) baseView.findViewById(R.id.itemLayout);
    	}
    	return itemLayout;
    }
}

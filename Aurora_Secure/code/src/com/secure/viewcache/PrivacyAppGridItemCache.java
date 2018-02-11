package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class PrivacyAppGridItemCache {
    private View baseView;	   
    private ImageView appIcon;
    private ImageView imgFlag;
    private FrameLayout itemLayout;
    private TextView appName;
       
    public PrivacyAppGridItemCache(View baseView) {
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
    
    public FrameLayout getItemLayout(){
    	if(itemLayout == null){
    		itemLayout = (FrameLayout) baseView.findViewById(R.id.itemLayout);
    	}
    	return itemLayout;
    }
    
    public TextView getAppName(){
    	if(appName == null){
    		appName = (TextView) baseView.findViewById(R.id.appName);
    	}
    	return appName;
    }
}

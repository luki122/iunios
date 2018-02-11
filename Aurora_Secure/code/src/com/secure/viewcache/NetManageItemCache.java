package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class NetManageItemCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private ImageView netSwitch;
    private TextView networkTraffic;
    private ProgressBar netProgressBar;
       
    public NetManageItemCache(View baseView) {
        this.baseView = baseView;
    }
        
    public ImageView getAppIcon() {
        if (appIcon == null) {
        	appIcon = (ImageView) baseView.findViewById(R.id.appIcon);
        }
        return appIcon;
    }
     
    public TextView getAppName() {
        if (appName == null) {
        	appName = (TextView) baseView.findViewById(R.id.appName);
        }
        return appName;
    }
    
    public ProgressBar getProgressBar() {
        if (netProgressBar == null) {
        	netProgressBar = (ProgressBar) baseView.findViewById(R.id.netProgressBar);
        }
        return netProgressBar;
    }
    
    public TextView getNetworkTraffic() {
        if (networkTraffic == null) {
        	networkTraffic = (TextView) baseView.findViewById(R.id.networkTraffic);
        }
        return networkTraffic;
    }
    
    public ImageView getNetSwitch(){
    	if(netSwitch == null){
    		netSwitch = (ImageView) baseView.findViewById(R.id.netSwitch);
    	}
    	return netSwitch;
    }
}

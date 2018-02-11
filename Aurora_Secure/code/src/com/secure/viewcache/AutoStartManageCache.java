package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AutoStartManageCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private Button autoStartSwitch;
       
    public AutoStartManageCache(View baseView) {
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
    
    public Button getAutoStartSwitch() {
        if (autoStartSwitch == null) {
        	autoStartSwitch = (Button) baseView.findViewById(R.id.autoStartSwitch);
        }
        return autoStartSwitch;
    }
}

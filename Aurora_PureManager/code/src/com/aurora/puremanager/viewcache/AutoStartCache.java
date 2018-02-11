package com.aurora.puremanager.viewcache;

import com.aurora.puremanager.R;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraSwitch;

public class AutoStartCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private AuroraSwitch autoStartSwitch;
       
    public AutoStartCache(View baseView) {
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
    
    public AuroraSwitch getAutoStartSwitch() {
        if (autoStartSwitch == null) {
        	autoStartSwitch = (AuroraSwitch) baseView.findViewById(R.id.autoStartSwitch);
        }
        return autoStartSwitch;
    }
}

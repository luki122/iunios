package com.aurora.puremanager.viewcache;

import com.aurora.puremanager.R;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FreezeAppCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private Button frrrzeBtn;
       
    public FreezeAppCache(View baseView) {
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
    
    public Button getFreezeBtn() {
        if (frrrzeBtn == null) {
        	frrrzeBtn = (Button) baseView.findViewById(R.id.freezeBtn);
        }
        return frrrzeBtn;
    }
}

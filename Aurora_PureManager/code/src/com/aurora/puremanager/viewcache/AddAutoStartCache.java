package com.aurora.puremanager.viewcache;

import com.aurora.puremanager.R;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AddAutoStartCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private Button addBtn;
    private TextView alreadyAdded;
       
    public AddAutoStartCache(View baseView) {
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
    
    public Button getAddBtn() {
        if (addBtn == null) {
        	addBtn = (Button) baseView.findViewById(R.id.addBtn);
        }
        return addBtn;
    }
    
    public TextView getAlreadyAdded() {
        if (alreadyAdded == null) {
        	alreadyAdded = (TextView) baseView.findViewById(R.id.alreadyAdded);
        }
        return alreadyAdded;
    }
}

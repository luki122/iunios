package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AdAppListItemCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private TextView subText;
       
    public AdAppListItemCache(View baseView) {
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
    
    public TextView getSubText() {
        if (subText == null) {
        	subText = (TextView) baseView.findViewById(R.id.subText);
        }
        return subText;
    }
}

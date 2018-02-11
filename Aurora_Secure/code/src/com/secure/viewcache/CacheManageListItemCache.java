package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CacheManageListItemCache {
    private View baseView;	    
    private ImageView appIcon;	    
    private TextView appName;
    private TextView cacheText;
    private CheckBox checkBox;
    private RelativeLayout leftLayout;
    private RelativeLayout midLayout;
    private ImageView img_set_go;
       
    public CacheManageListItemCache(View baseView) {
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
    
    public TextView getCacheText() {
        if (cacheText == null) {
        	cacheText = (TextView) baseView.findViewById(R.id.cacheText);
        }
        return cacheText;
    }
    
    public CheckBox getCheckBox() {
        if (checkBox == null) {
        	checkBox = (CheckBox) baseView.findViewById(R.id.checkBox);
        }
        return checkBox;
    }
       
    public RelativeLayout getLeftLayout() {
        if (leftLayout == null) {
        	leftLayout = (RelativeLayout) baseView.findViewById(R.id.leftLayout);
        }
        return leftLayout;
    }
    
    public RelativeLayout getMidLayout() {
        if (midLayout == null) {
        	midLayout = (RelativeLayout) baseView.findViewById(R.id.midLayout);
        }
        return midLayout;
    }
    
    public ImageView getImgSetGo() {
        if (img_set_go == null) {
        	img_set_go = (ImageView) baseView.findViewById(R.id.img_set_go);
        }
        return img_set_go;
    }
}

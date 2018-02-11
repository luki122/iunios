package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

public class PermissionHintListItemCache {
    private View baseView;	    
    private TextView permissionName;
    private CheckBox checkBox;
       
    public PermissionHintListItemCache(View baseView) {
        this.baseView = baseView;
    }
     
    public TextView getPermissionName() {
        if (permissionName == null) {
        	permissionName = (TextView) baseView.findViewById(R.id.permissionName);
        }
        return permissionName;
    }

    
    public CheckBox getCheckBox() {
        if (checkBox == null) {
        	checkBox = (CheckBox) baseView.findViewById(R.id.checkBox);
        }
        return checkBox;
    }
}

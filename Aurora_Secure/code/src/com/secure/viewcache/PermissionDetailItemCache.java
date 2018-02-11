package com.secure.viewcache;

import com.aurora.secure.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraSwitch;

public class PermissionDetailItemCache {
    private View baseView;	   
    private RelativeLayout itemLayout;
    private ImageView permissionIcon;	    
    private TextView permissionName;
    private TextView  permissionSwitch;
    private int position = -1;
    
    public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public PermissionDetailItemCache(View baseView) {
        this.baseView = baseView;
    }
    
    public RelativeLayout getItemLayout() {
        if (itemLayout == null) {
        	itemLayout = (RelativeLayout) baseView.findViewById(R.id.itemLayout);
        }
        return itemLayout;
    }
        
    public ImageView getPermissionIcon() {
        if (permissionIcon == null) {
        	permissionIcon = (ImageView) baseView.findViewById(R.id.permissionIcon);
        }
        return permissionIcon;
    }
     
    public TextView getPermissionName() {
        if (permissionName == null) {
        	permissionName = (TextView) baseView.findViewById(R.id.permissionName);
        }
        return permissionName;
    }
    
    public TextView getPermissionSwitch() {
        if (permissionSwitch == null) {
        	permissionSwitch = (TextView) baseView.findViewById(R.id.permissionSwitch);
        }
        return permissionSwitch;
    }
}

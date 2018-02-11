package com.android.viewcache;

import com.android.packageinstaller.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InstallConfirmListItemCache {
    private View baseView;	   
    private LinearLayout itemLayout;
    private View topSpaceView;
    private View bottomSpaceView;
    private ImageView permissionIcon;	    
    private TextView permissionName;
       
    public InstallConfirmListItemCache(View baseView) {
        this.baseView = baseView;
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
    
    public LinearLayout getItemLayout() {
        if (itemLayout == null) {
        	itemLayout = (LinearLayout) baseView.findViewById(R.id.itemLayout);
        }
        return itemLayout;
    }
    
    public View getTopSpaceView() {
        if (topSpaceView == null) {
        	topSpaceView = (View) baseView.findViewById(R.id.topSpaceView);
        }
        return topSpaceView;
    }
    
    public View getBottomSpaceView() {
        if (bottomSpaceView == null) {
        	bottomSpaceView = (View) baseView.findViewById(R.id.bottomSpaceView);
        }
        return bottomSpaceView;
    }
}

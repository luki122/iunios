package com.aurora.puremanager.viewcache;

import com.aurora.puremanager.R;
import com.aurora.puremanager.view.GridViewForEmbed;
import android.view.View;
import android.widget.TextView;

public class DefSoftManageListItemCache {
    private View baseView;	
    private TextView labelText;
    private GridViewForEmbed itemGridView;
       
    public DefSoftManageListItemCache(View baseView) {
        this.baseView = baseView;
    }
    
    public GridViewForEmbed getItemGridView() {
        if (itemGridView == null) {
        	itemGridView = (GridViewForEmbed) baseView.findViewById(R.id.itemGridView);
        }
        return itemGridView;
    }
    
    public TextView getLabelText() {
        if (labelText == null) {
        	labelText = (TextView) baseView.findViewById(R.id.labelText);
        }
        return labelText;
    }
}

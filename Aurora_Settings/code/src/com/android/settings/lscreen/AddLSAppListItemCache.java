package com.android.settings.lscreen;

import com.android.settings.R;

import android.view.View;
import android.widget.TextView;


public class AddLSAppListItemCache {
    private View baseView;	
    private TextView labelText;
    private GridViewForEmbed itemGridView;
       
    public AddLSAppListItemCache(View baseView) {
        this.baseView = baseView;
    }
    
    public GridViewForEmbed getItemGridView() {
        if (itemGridView == null) {
        	itemGridView = (GridViewForEmbed) baseView.findViewById(R.id.itemGridView);
        }
        return itemGridView;
    }
    
//    public TextView getLabelText() {
//        if (labelText == null) {
//        	labelText = (TextView) baseView.findViewById(R.id.labelText);
//        }
//        return labelText;
//    }
}

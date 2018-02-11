package com.secure.viewcache;

import com.aurora.secure.R;
import com.secure.view.GridViewForEmbed;
import android.view.View;
import android.widget.TextView;

public class AddPrivacyAppListItemCache {
    private View baseView;	
    private TextView labelText;
    private GridViewForEmbed itemGridView;
       
    public AddPrivacyAppListItemCache(View baseView) {
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

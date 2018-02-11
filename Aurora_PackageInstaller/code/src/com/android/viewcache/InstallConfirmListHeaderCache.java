package com.android.viewcache;

import com.android.packageinstaller.R;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class InstallConfirmListHeaderCache {
    private View baseView;	   
    private ImageView arrowImg;	    
    private TextView labelText;
    private TextView numText;
    private RelativeLayout labelLayout;
    private View spaceView;
       
    public InstallConfirmListHeaderCache(View baseView) {
        this.baseView = baseView;
    }
        
    public ImageView getArrowImg() {
        if (arrowImg == null) {
        	arrowImg = (ImageView) baseView.findViewById(R.id.arrowImg);
        }
        return arrowImg;
    }
     
    public TextView getLabelText() {
        if (labelText == null) {
        	labelText = (TextView) baseView.findViewById(R.id.labelText);
        }
        return labelText;
    }
    
    public TextView getNumText() {
        if (numText == null) {
        	numText = (TextView) baseView.findViewById(R.id.numText);
        }
        return numText;
    }
    
    public RelativeLayout getLabelLayout(){
    	if(labelLayout == null){
    		labelLayout = (RelativeLayout) baseView.findViewById(R.id.labelLayout);
    	}
    	return labelLayout;
    }
    
    public View getSpaceView(){
    	if(spaceView == null){
    		spaceView = baseView.findViewById(R.id.spaceView);
    	}
    	return spaceView;
    }
}

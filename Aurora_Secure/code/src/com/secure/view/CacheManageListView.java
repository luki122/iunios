package com.secure.view;

import android.content.Context; 
import android.util.AttributeSet;
import android.widget.ListView; 
import aurora.widget.AuroraListView;

public class CacheManageListView extends AuroraListView { 
	
	public CacheManageListView(Context context) { 
	    super(context, null);
	} 
	
	public CacheManageListView(Context context, AttributeSet attrs) { 
	    super(context, attrs); 
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {		
//		 if (!changed) return;
		 super.onLayout(changed, l, t, r, b);
	}
} 




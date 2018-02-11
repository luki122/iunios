package com.android.deskclock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import aurora.preference.AuroraPreference;

public class AuroraTimePreference extends AuroraPreference{
	
	private Context mContext;
	
    public AuroraTimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.preferenceStyle);
    }
    
    public AuroraTimePreference(Context context) {
        this(context, null);
    }
    
    public AuroraTimePreference(Context context, AttributeSet attrs, int defStyle) {
    	super(context, attrs, defStyle);
    	mContext = context;
    	setLayoutResource(R.layout.timepicker);
    }
    
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		
	}
}

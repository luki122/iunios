package com.android.email.preferences;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.email.R;
import aurora.preference.AuroraSwitchPreference;

public class AuroraMlSwitchPreference extends AuroraSwitchPreference{
	private Context mContext;
	public AuroraMlSwitchPreference(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public AuroraMlSwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public AuroraMlSwitchPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;		
	}
	
	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		TextView titleView = (TextView)view.findViewById(android.R.id.title);
		if(titleView != null){
			titleView.setTextSize(14);   //cjs modify 
			titleView.setTextColor(mContext.getResources().getColorStateList(R.color.aurora_manual_title_color));
		}
	}
}

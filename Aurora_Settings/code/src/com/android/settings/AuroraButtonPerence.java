package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import aurora.preference.AuroraPreference;

public class AuroraButtonPerence extends AuroraPreference{
	private Button mButton = null;
	private View.OnClickListener mListener = null;
	public final static String TAG = "AuroraButtonPerence";
	
	public AuroraButtonPerence(Context context) {
		super(context);
        setWidgetLayoutResource(R.layout.aurora_button_preference);
	}
	
	public AuroraButtonPerence(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		 setWidgetLayoutResource(R.layout.aurora_button_preference);
	}
	
	public AuroraButtonPerence(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		 setWidgetLayoutResource(R.layout.aurora_button_preference);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		mButton = (Button)view.findViewById(R.id.button);
		if(mListener != null){
			mButton.setOnClickListener(mListener);
		}
	}
	
	public void setOnClickListener(OnClickListener listener){
		mListener = listener;
		if(mButton != null){
			mButton.setOnClickListener(mListener);
		}
	}
	
	
}
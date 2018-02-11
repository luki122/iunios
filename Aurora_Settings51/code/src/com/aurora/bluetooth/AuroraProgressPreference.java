package com.android.settings.bluetooth;


import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import aurora.preference.*;
import com.android.settings.R;



public class AuroraProgressPreference extends AuroraPreference{
	
	private boolean mProgress = true;

	public AuroraProgressPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setWidgetLayoutResource(R.layout.aurora_bt_scan_preference_widget_layout);
	}

	public AuroraProgressPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AuroraProgressPreference(Context context) {
		this(context, null);
	}

	
	protected void onBindView(View view) {		
		super.onBindView(view);
		final ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.bt_scan_progress);
		if(progressBar != null){
			progressBar.setVisibility(mProgress ? View.VISIBLE : View.GONE);
		}
	}
	
	public void setProgress(boolean progressOn) {
        mProgress = progressOn;
        notifyChanged();
    }
	

}

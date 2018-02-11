package com.android.settings.notification;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import aurora.preference.*;
import aurora.widget.AuroraCheckedTextView;

import com.android.settings.R;

public class AuroraNotificationPerence extends AuroraPreference {
	
	private AuroraCheckedTextView mCheckView;
	private boolean mChecked;
	public AuroraNotificationPerence(Context context) {
		super(context,null);
		// TODO Auto-generated constructor stub
		setWidgetLayoutResource(R.layout.aurora_preference_notify_widget);
	}

	public AuroraNotificationPerence(Context context, AttributeSet attrs) {
		super(context, attrs,0);
		// TODO Auto-generated constructor stub
		setWidgetLayoutResource(R.layout.aurora_preference_notify_widget);
	}

	public AuroraNotificationPerence(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setWidgetLayoutResource(R.layout.aurora_preference_notify_widget);
	}
	
	public void setChecked(boolean checked){
		mChecked = checked;
		notifyChanged();
	}
	public boolean getChecked(){
		return mChecked;
	}
	
	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		mCheckView = 	(AuroraCheckedTextView)view.findViewById(R.id.check);
		mCheckView.setChecked(getChecked());
	}

}
package com.android.settings.bluetooth;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import aurora.preference.AuroraPreferenceCategory;

public class AuroraPreferenceCategoryBT extends AuroraPreferenceCategory {
	public AuroraPreferenceCategoryBT(Context context) {
		super(context);
	}
	
	public AuroraPreferenceCategoryBT(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AuroraPreferenceCategoryBT(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	private ProgressBar progressBar;
	private boolean viewProgressBar;
	public void viewProgressBar(boolean viewProgressBar){
		this.viewProgressBar = viewProgressBar;
		if(progressBar!=null){
			progressBar.setVisibility(viewProgressBar?View.VISIBLE:View.GONE);			
		}
	}
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		TextView titleView = (TextView) view.findViewById(com.aurora.internal.R.id.aurora_preference_category_title);
		LinearLayout.LayoutParams tLp = (LinearLayout.LayoutParams)titleView.getLayoutParams();
		tLp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		tLp.weight = 1;
		progressBar = new ProgressBar(getContext(), null, -1, com.aurora.R.style.Widget_Aurora_Light_ProgressBar_Small);
		LinearLayout linearLayout = (LinearLayout)view.findViewById(com.aurora.internal.R.id.aurora_preference_category_layout);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout.LayoutParams tLpP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		tLpP.gravity = Gravity.CENTER_VERTICAL;
		tLpP.rightMargin = (int)(getContext().getResources().getDisplayMetrics().density*10);
		linearLayout.addView(progressBar, tLpP);
		
		viewProgressBar(viewProgressBar);
	}
	
}

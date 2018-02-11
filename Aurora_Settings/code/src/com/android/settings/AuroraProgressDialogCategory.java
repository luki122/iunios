package com.android.settings;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import aurora.preference.AuroraPreferenceCategory;

public class AuroraProgressDialogCategory extends AuroraPreferenceCategory{
	 private Context mContext;
	 private boolean mIsVisible=true;

	public AuroraProgressDialogCategory(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public AuroraProgressDialogCategory(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public AuroraProgressDialogCategory(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	/*@Override
	protected View onCreateView(ViewGroup parent) {
		// TODO Auto-generated method stub
		DisplayMetrics dm = new DisplayMetrics();
		dm = mContext.getResources().getDisplayMetrics();			
		int screenHeight = dm.heightPixels;	
		Log.i("qy", "screenHeight = "+screenHeight);
		RelativeLayout layout=new RelativeLayout (mContext);
		
		View factoryView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.privacy_settings_main, null);
		layout.addView(factoryView,  new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, screenHeight - 297));
		return layout;
		
	}*/
	
	public void setVisible(boolean isVisilbe){
		mIsVisible = isVisilbe;
		notifyChanged();
	}

	@Override
    protected void onBindView(View view) {
        super.onBindView(view);

		
        ProgressBar pb = (ProgressBar)view.findViewById(R.id.app_icon_scanning_progress);
        if(!mIsVisible){
        	pb.setVisibility(View.GONE);
        }
		
		Log.i("qy", "onBindView()****");
    }
}

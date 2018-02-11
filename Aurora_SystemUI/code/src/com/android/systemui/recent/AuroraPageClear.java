package com.android.systemui.recent;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.content.res.Configuration;

import com.android.systemui.R;
import android.view.Gravity;
import android.content.res.Configuration;

public class AuroraPageClear extends LinearLayout implements AuroraPage{
	private ArrayList<View> mList = new ArrayList<View>();
	public AuroraPageClear(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSize();
		// TODO Auto-generated constructor stub
	}

	@Override
	public void addView(View v){
		mList.add(v);
		super.addView(v);
	}
	
	@Override
	public void removeView(View v){
		mList.remove(v);
		super.removeView(v);
	}
	@Override
	public int getPageChildCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public View getChildOnPageAt(int i) {
		// TODO Auto-generated method stub
		return mList.get(i);
	}

	@Override
	public void removeAllViewsOnPage() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeViewOnPageAt(int i) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int indexOfChildOnPage(View v) {
		// TODO Auto-generated method stub
		for(int i=0;i<mList.size();++i){
			if(mList.get(i).equals(v)){
				return i;
			}
		}
		return 0;
	}
	
	@Override
	public View getChildOnPageAtPoint(float x,float y) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isOrientationLand(){
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return false;
		return true;
	}

	private int screenOrientation = getScreenOrientation();

	private int getScreenOrientation(){
		return getResources().getConfiguration().orientation;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		android.util.Log.e("AuroraPageClear", "Rubbish View onConfigurationChanged new: " + newConfig.orientation + " old is: " + newConfig.orientation);
		if(newConfig.orientation != screenOrientation){
			// is screen orientation change, we should reload level res
			screenOrientation = newConfig.orientation;
			updateView();
		}
		super.onConfigurationChanged(newConfig);
	}

	private View rubbishView;
	private void initView(){
		if(rubbishView == null){
			rubbishView = findViewById(R.id.clear_all);
		}
	}

	private boolean isOrientationPortrait(){
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) return true;
		return false;
	}

	int rubbishWidth;
	int rubbishHeight;
	int rubbishWidthLand;
	int rubbishHeightLand;

	private void initSize(){
		rubbishWidth = getResources().getDimensionPixelSize(R.dimen.recent_rubbishview_width);
		rubbishHeight = getResources().getDimensionPixelSize(R.dimen.recent_rubbishview_height);
		rubbishWidthLand = getResources().getDimensionPixelSize(R.dimen.recent_rubbishview_width_land);
		rubbishHeightLand = getResources().getDimensionPixelSize(R.dimen.recent_rubbishview_height_land);
	}

	private void updateView(){
		initView();

		LinearLayout.LayoutParams rubbishParam = (LinearLayout.LayoutParams)rubbishView.getLayoutParams();
		if(isOrientationPortrait()){
			rubbishParam.width = rubbishWidth;
			rubbishParam.height = rubbishHeight;
			rubbishParam.gravity = Gravity.BOTTOM;
		} else {
			rubbishParam.width = rubbishWidthLand;
			rubbishParam.height = rubbishHeightLand;
			rubbishParam.gravity = Gravity.BOTTOM | Gravity.RIGHT;
		}
		rubbishView.setLayoutParams(rubbishParam);
	}

}

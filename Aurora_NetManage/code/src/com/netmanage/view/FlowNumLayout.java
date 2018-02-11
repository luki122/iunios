package com.netmanage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import com.aurora.netmanage.R;

public class FlowNumLayout extends LinearLayout {			
	private int fatherLayoutWidth;
	private int defRightMargin = 0;
	private FrameLayout.LayoutParams layoutParams;
 		 		
	public FlowNumLayout(Context context) {
		super(context);
		init();
	}
	
	public FlowNumLayout(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    init();
	}
	
	private void init(){
		fatherLayoutWidth = (int)(getContext().getResources().
				getDimension(R.dimen.main_act_enter_animation_size));		
		defRightMargin = (int)(getContext().getResources().
				getDimension(R.dimen.main_act_flow_num_margin_right));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateLayoutParams(w);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	private void updateLayoutParams(int w){				
		int tmpRightMargin = fatherLayoutWidth/2-w/2;
		if(tmpRightMargin > defRightMargin){
			tmpRightMargin = defRightMargin;
		}
			
		FrameLayout.LayoutParams layoutParams = getMyLayoutParams();
		if(layoutParams != null &&
				tmpRightMargin != layoutParams.rightMargin){
			layoutParams.rightMargin = tmpRightMargin;			
			post(action);
		}
	}
	
	Runnable action = new Runnable( ) {			
		@Override
		public void run() {
			/**
			 * setLayoutParams必须放在post中调用，
			 * 如果在onSizeChanged()方法中直接调用，会因为viewRoot正在更新view布局，
			 * 而不会响应setLayoutParams的操作（详见setLayoutParams()方法的实现逻辑）。
			 */
			FrameLayout.LayoutParams layoutParams = getMyLayoutParams();
			if(layoutParams != null){
				setLayoutParams(layoutParams);	
			}					
		}
	};
	
	private FrameLayout.LayoutParams getMyLayoutParams(){
		if(layoutParams == null){
			layoutParams = (FrameLayout.LayoutParams)getLayoutParams();
		}
		return layoutParams;
	}	
}

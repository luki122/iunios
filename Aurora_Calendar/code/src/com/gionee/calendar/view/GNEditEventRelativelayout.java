package com.gionee.calendar.view;

import com.android.calendar.AllInOneActivity;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
//Gionee <jiating> <2013-05-17> modify for CR00000000  begin 
public class GNEditEventRelativelayout extends RelativeLayout{

	
	private Context context;
	 private OnResizeListener mListener;  
	public interface OnResizeListener {  
		         void OnResize(int w, int h, int oldw, int oldh);  
		    } 
	
	public void setOnResizeListener(OnResizeListener l) {  
		          mListener = l;  
		     } 
	public GNEditEventRelativelayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context=context;
		// TODO Auto-generated constructor stub
	}

	public GNEditEventRelativelayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context=context;
		// TODO Auto-generated constructor stub
	}

	public GNEditEventRelativelayout(Context context) {
		super(context);
		this.context=context;
		// TODO Auto-generated constructor stub
	}
    
@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	// TODO Auto-generated method stub
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	getHeight();
	Log.i("jiating","Scroll....onMeasure....widthMeasureSpec="+widthMeasureSpec+"heightMeasureSpec="+heightMeasureSpec+"getHeight="+getHeight());
}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		
		Log.i("jiating","Scroll...onSizeChanged....w="+w+"h="+h+"oldw="+oldw+"oldh="+oldh);
		if (mListener != null) {  
            mListener.OnResize(w, h, oldw, oldh);  
        } 
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		Log.i("jiating","Scroll....onLayout....changed="+changed+"left="+left+"top="+top+"right="+right+"bottom="+bottom);
	}
	
	
}
//Gionee <jiating> <2013-05-17> modify for CR00000000  end
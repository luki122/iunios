package com.aurora.voiceassistant.view;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class CustomerScrollView extends ScrollView {
	
	public CustomerScrollView(Context context) {
		this(context,null);
	}

	public CustomerScrollView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public CustomerScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private CustomerScrollViewListener mCustomerScrollViewListener;
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mCustomerScrollViewListener.onSizeChanged(this,w, h, oldw, oldh);
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		mCustomerScrollViewListener.onScrollChanged(this,l, t, oldl, oldt);
	}
	
	public void setListener(CustomerScrollViewListener l){
		mCustomerScrollViewListener = l;
	}
	
	interface CustomerScrollViewListener{
		void onSizeChanged(CustomerScrollView v,int w, int h, int oldw, int oldh);
		void onScrollChanged(CustomerScrollView v,int l, int t, int oldl, int oldt);
	}

}
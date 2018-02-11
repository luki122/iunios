package com.aurora.thememanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.thememanager.R;
public class BannerView extends FrameLayout {

	
	private LayoutInflater mInflater;
	
	private Gallery mGallery;
	
	private LinearLayout mIndexLayout;
	
	private ImageView[] mIndex;
	
	private OnGalleryImageChangedListener mListener;
	
	public BannerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		initView();
	}
	
	public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		initView();
	}
	
	private void initView(){
		mInflater = LayoutInflater.from(getContext());
		mInflater.inflate(R.layout.theme_preview_gallery, this);
	}
	
	
	public void setOnImageChangedListener(OnGalleryImageChangedListener listener){
		mListener = listener;
	}
	
	
	@Override
	protected void onMeasure(int arg0, int arg1) {
		// TODO Auto-generated method stub
		super.onMeasure(arg0, arg1);
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
	}
	
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return super.onInterceptTouchEvent(ev);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}

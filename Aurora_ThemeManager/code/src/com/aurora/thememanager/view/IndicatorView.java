package com.aurora.thememanager.view;

import com.aurora.thememanager.R;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class IndicatorView extends LinearLayout {
	private int imageWidth;
	private int pageCount;
	private int seekpointHighlight =  R.drawable.workspace_seekpoint_highlight;
	private int seekpointNormal =  R.drawable.workspace_seekpoint_normal;
	public int getPageCount() {
		return pageCount;
	}
	public void setPageCount(int pageCount) {
		if(pageCount<=0){
			return;
		}
		if(this.pageCount<pageCount){
			LayoutInflater mLayoutInflater = LayoutInflater.from(getContext());
			int needAddCount = pageCount - this.pageCount;
			for(int i=0; i<needAddCount; i++){
				mLayoutInflater.inflate(R.layout.indicator_for_one, this, true);
				imageWidth = getResources().getDrawable(seekpointHighlight).getIntrinsicWidth();
			}
		}else if(this.pageCount>pageCount){
			int needAddCount = this.pageCount - pageCount;
			for(int i=0; i<needAddCount; i++){
				removeViewAt(0);
			}
		}
		oneImageWidth = getWidth()/pageCount;
		this.pageCount = pageCount;
		offsetClearance = pageCount>1?1.0d/(pageCount-1):0;
		invalidate();
		requestLayout();
	}
	
	public IndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	double offsetClearance;
	int oneImageWidth;
	/**
	 * ��ǰ������ڰٷֱ�
	 * @param offset
	 */
	public void updateScrollingIndicatorPosition(int position){
		Message msg = new Message();
		msg.what = position;
		mHandler.sendMessage(msg);
	}
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			float position = msg.what;
			if(pageCount>1){
				int resId = 0;
				for(int i=0; i<pageCount; i++){
					ImageView mImageView = (ImageView)(getChildAt(i).findViewById(R.id.indicator_image));				
					if(i == position){
						resId = seekpointHighlight;
					}else{
						resId = seekpointNormal;
					}
					mImageView.setImageResource(resId);
					mImageView.requestLayout();
				}
			}else{
				if(getChildAt(0)!=null){
					ImageView mImageView = (ImageView)(getChildAt(0).findViewById(R.id.indicator_image));		
					mImageView.getLayoutParams().width = (int)(imageWidth*1.25);
					mImageView.requestLayout();
				}				
			}
		};
	};
}

package com.android.auroramusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import aurora.widget.AuroraListView;

import com.android.auroramusic.online.AuroraNetTrackDetail;

public class AuroraTrackListVIew extends AuroraListView{
	private AuroraNetTrackDetail mTrackDetail;
	public void setAuroraNetTrackDetail(AuroraNetTrackDetail detail){
		mTrackDetail = detail;
	}
	public AuroraTrackListVIew(Context context){
		this(context,null);
	}
	public AuroraTrackListVIew(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}
	public AuroraTrackListVIew(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX,
			int scrollY, int scrollRangeX, int scrollRangeY,
			int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
		// TODO Auto-generated method stub
		if(mTrackDetail!=null){
			mTrackDetail.startAnimation(scrollY,deltaY);
		}
		return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
				scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
	}

}

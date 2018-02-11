package com.aurora.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author tangjun  9.26
 *
 */
public class ThumbnailImage extends LinearLayout{
	
	public static final int COUNTINONEPAGE = 7;
	
	public static int ITEMWIDTH = 0;
	
	private Launcher mLauncher;
	
	private TextView mTextView;
	
	private int lastpage = 0;
	
	public Launcher getLauncher() {
		return mLauncher;
	}

	public void setLauncher(Launcher mLauncher) {
		this.mLauncher = mLauncher;
	}
	
	public void setItemWidth(int itemWidth){
		ITEMWIDTH = itemWidth;
	}
	
	
	public ThumbnailImage(Context context) {
		super(context);
	}

	public ThumbnailImage(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public int getLastpage() {
		return lastpage;
	}

	public void setLastpage(int lastpage) {
		this.lastpage = lastpage;
	}

	/**
	 * @param curpage
	 */
	public void movetoItem( int curpage ) {
		
//AURORA-START::Thumnail animator::Shi guiqiang::20131114
//		if ( curpage >= mLauncher.getHorizontalScrollView().getLeftpage() + COUNTINONEPAGE ) {
//			mLauncher.smoothPreviewBarScrollForSnapTo (1);
//		} else if ( curpage < mLauncher.getHorizontalScrollView().getLeftpage() ) {
//			mLauncher.smoothPreviewBarScrollForSnapTo (0);
//		}
//AURORA-END::Thumnail animator::Shi guiqiang::20131114
	
		mTextView.setText(String.valueOf(curpage + 1));
		
		int count = this.getChildCount();
		for( int i = 0; i < count; i ++ ) {
			if ( this.getChildAt(i) != null ) {
				this.getChildAt(i).setBackgroundResource(R.drawable.thumbnail_no_focus);
			}
		}
		if ( this.getChildAt(curpage) != null ) {
			lastpage = curpage;
			this.getChildAt(curpage).setBackgroundResource(R.drawable.thumbnail_focus);
		}
	}
	
	/**
	 * @param textView  
	 */
	public void setThumbnailImagetextView(TextView textView) {
		mTextView = textView;
	}

}

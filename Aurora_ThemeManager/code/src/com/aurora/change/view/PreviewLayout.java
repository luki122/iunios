package com.aurora.change.view;

import com.aurora.thememanager.R;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class PreviewLayout extends RelativeLayout {
    private TimeLayout mTimeLayout;
//    private ImageView mClockImg;
    private PreviewTimeView mTimeView;
    /*private int[] sClockRes = {R.drawable.clock6, R.drawable.clock8, R.drawable.clock10, R.drawable.clock0,
            R.drawable.clock2, R.drawable.clock4, R.drawable.clock6, R.drawable.clock8, R.drawable.clock10,
            R.drawable.clock0, R.drawable.clock2, R.drawable.clock4};*/
    private boolean mIsSingle = false;

    public PreviewLayout(Context context) {
        this(context, null);
    }

    public PreviewLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreviewLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTimeLayout = ( TimeLayout ) findViewById(R.id.time_layout);
//        mClockImg = ( ImageView ) findViewById(R.id.preview_clock);
        mTimeView = ( PreviewTimeView ) findViewById(R.id.preview_time);
        mTimeLayout.refresh(0);
//        mClockImg.setImageResource(sClockRes[0]);
    }

    public void onPageSelected(int position) {
        mTimeLayout.onPageSelected(position);
//        mClockImg.setImageResource(sClockRes[position]);
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mTimeLayout.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    public void setViewPager(ViewPager viewPager) {
        mTimeView.setViewPager(viewPager);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener onPageChangeListener) {
        mTimeView.setOnPageChangeListener(onPageChangeListener);
    }

    public void setSingle(boolean bool) {
        mIsSingle = bool;
        if (!mIsSingle) {
            mTimeView.setVisibility(View.VISIBLE);
        } else {
            mTimeView.setVisibility(View.GONE);
        }
        mTimeLayout.setSingle(bool);
    }
    
    public boolean getSingle() {
    	return this.mIsSingle;
    }
    
    public void updateSingleTime() {
		mTimeLayout.updateClock();
	}
    
    public void setBlackStyle(boolean bool, int color) {
		mTimeLayout.setBlackStyle(bool, color);
	}
}

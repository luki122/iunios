package views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout.LayoutParams;

public class HoursContents extends AbstractWeatherView {
	private AnimLinearlayout mAnimLinearlayout;
	public HoursContents(Context context) {
		super(context);
		initView(context);
	}

	public HoursContents(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public HoursContents(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}
	private void initView(Context context){
		mAnimLinearlayout=new AnimLinearlayout(context);
		addView(mAnimLinearlayout, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mAnimLinearlayout.setData();
	}
	
	
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		if(mAnimLinearlayout!=null)mAnimLinearlayout.startAnim();
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
    public void setHideViewBg(Drawable d,int y){
    	mAnimLinearlayout.setHideViewBg(d,y);
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		if(mAnimLinearlayout!=null)mAnimLinearlayout.outAnim();
	}
}

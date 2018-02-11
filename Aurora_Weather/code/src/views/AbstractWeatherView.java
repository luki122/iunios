package views;

import interfaces.IWeatherView;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import datas.CityListShowData;

@SuppressLint("NewApi")
public abstract class AbstractWeatherView extends FrameLayout implements interfaces.IWeatherView{
	
	/**
	 * just test time consuming !!!
	 */
	private long mTimeStart,mTimeEnd;
	
	/**
	 * each View has a state member
	 */
	protected int mCurState = IWeatherView.COMING_IN;
	
	/**
	 * city which to show !!!
	 * each child get data throw mCityIndex 
	 */
	private int mCityIndex = -1;
	
	protected LayoutInflater mInflater;
	
	protected CityListShowData mCityListShowData;
	public AbstractWeatherView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public AbstractWeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	
	public AbstractWeatherView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	//public resource provided for child View
	private void init(Context context)
	{
		mInflater = LayoutInflater.from(context);
		mCityListShowData=CityListShowData.getInstance(context);
	}
	
	@Override
	public void startEntryAnim() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void normalShow() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void startExitAnim() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * push down to update data, surface changed!!!
	 */
	public void onUpdating()
	{
		
	}
	
	/**
	 * after update,data Changed ,we should update our Surface !!!
	 */
	public void onDataChanged()
	{
		
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStateChanged(int state) {
		// TODO Auto-generated method stub
		
		mCurState = state;
		
		switch(state)
		{
			case IWeatherView.COMING_IN:
				startEntryAnim();
				break;
			case IWeatherView.NORMAL_SHOWING:
				normalShow();
				break;
			case IWeatherView.COMING_OUT:
				startExitAnim();
				break;
			case IWeatherView.QUIT_APP:
				onDestroy();
				break;
			case IWeatherView.UPDATING:
				onUpdating();
				break;
			case IWeatherView.CURRENT_PAGE_SELECT:
				onCurrentpageChange();
				break;
		}
		
		//Log.e("111111", "------onStateChanged-----------" + this.getClass());
		if ( this.getChildAt(0) instanceof ViewGroup ) {
			notifyChildsStateChange(((ViewGroup)this.getChildAt(0)), state);
		}
	}
	
	protected void onCurrentpageChange() {
		// TODO Auto-generated method stub

	}
	
	private void notifyChildsStateChange(ViewGroup group,int state){
		int size=group.getChildCount();
		View child;
		for(int i=0;i<size;i++)
		{
			child=group.getChildAt(i);
			if(child instanceof AbstractWeatherView)
			{
				((AbstractWeatherView)child).onStateChanged(state);
			}else if(child instanceof ViewGroup)
			{
				notifyChildsStateChange((ViewGroup)child, state);
			}
		}
		
	}
	
	
	
	/**
	 * @each child view should to know which city information to show !!!
	 * @param index : data index 
	 */
	@Override
	public void setIndex(int index)
	{
		mCityIndex = index;

		if ( this.getChildAt(0) instanceof ViewGroup ) {
			int size = ((ViewGroup)this.getChildAt(0)).getChildCount();
			setChildsIndex(((ViewGroup)this.getChildAt(0)), index);
		}
	}

	private void setChildsIndex(ViewGroup viewGroup,int index){
		int size=viewGroup.getChildCount();
		for(int i=0;i<size;i++)
		{
			View view = viewGroup.getChildAt(i);
			if ( view instanceof AbstractWeatherView ) {
				AbstractWeatherView  weatherView = (AbstractWeatherView)view;
				weatherView.setIndex(index);
			}else if(view instanceof ViewGroup)
			{
				setChildsIndex((ViewGroup)view, index);
			}
		}
	}
	
	/**
	 * test time consuming !!!
	 */
	protected void startMeasure()
	{
		mTimeStart = System.currentTimeMillis();
	}
	
	/**
	 * test time consuming !!!
	 */
	protected void endMeasure(String TAG)
	{
		mTimeEnd = System.currentTimeMillis();
		
		Log.e(TAG, "time consuming = " + (mTimeEnd - mTimeStart));
		
		mTimeEnd = mTimeStart = 0;
	}
	/**
	 * 获取属性动画
	 * @param view 
	 * @param propertyName 属性名
	 * @param fromValue
	 * @param toValue
	 * @return
	 */
	public ObjectAnimator getAnimator(View view,String propertyName,float fromValue,float toValue){
		return ObjectAnimator.ofFloat(view, propertyName, fromValue,toValue);
	}
	
	/**
	 * child view to get city index
	 * @return
	 */
	public int getIndex()
	{
		return mCityIndex;
	}
}

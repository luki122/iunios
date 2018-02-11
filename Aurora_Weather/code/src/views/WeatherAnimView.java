package views;

import interfaces.IWeatherAnim;
import interfaces.IWeatherView;

import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import aurora.view.AuroraGLSurfaceView;
import datas.WeatherCityInfo;


public class WeatherAnimView /*extends AuroraGLSurfaceView implements IWeatherView*/{
	
//	/**
//	 * different state , different surface
//	 */
//	protected int mCurState;
//	
//	/**
//	 * city which to show !!!
//	 * each child get data throw mCityIndex 
//	 */
//	private int mCityIndex = 0;
//	
//	private boolean mCityChanged = false;
//	//Weather type
//	private String wType = "";
//	
//	private IWeatherAnim mAnim;
//	
//	private datas.WeatherData mData;
//	
//	private HashMap<String , IWeatherAnim> mAnimCash = new HashMap<String , IWeatherAnim>();
//	
//	private static final String RAIN_WITH_SNOW = "雨夹雪";
//	//雨夹雪天气类型中，雨的类型
//	private static final String RAINTYPE_OF_RAIN_WITH_SNOW = "小雨";
//	
//	public WeatherAnimView(Context context) {
//		this(context,null);
//		// TODO Auto-generated constructor stub
//	}
//
//	public WeatherAnimView(Context context, AttributeSet attrs) {
//		super(context, attrs);
//		// TODO Auto-generated constructor stub
//		
//		this.setRenderModeAuto();
//		
//		mData = datas.WeatherData.getInstance(this.getContext());
//	}
//	
//	
//	@Override
//	public void startEntryAnim() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void normalShow() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void startExitAnim() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onStateChanged(int state) {
//		// TODO Auto-generated method stub
//		mCurState = state;
//	}
//
//	@Override
//	public void onDestroy() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onUpdating() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onDataChanged() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setIndex(final int index) {
//		
//		onAnimPause();
//		
//		queueEvent(
//				new Runnable() {
//				public void run() {
//					onCityChanged(index);
//				}
//			}
//		);
//		
//		onAnimResume();
//	}
//	
//	
//	
//	@Override
//	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//		// TODO Auto-generated method stub
//		super.onSurfaceCreated(gl, config);
//		
//	}
//	
//	
//	
//	@Override
//	protected void doDraw(GL10 gl) {
//		// TODO Auto-generated method stub
//		super.doDraw(gl);
//		
//		mAnim = mAnimCash.get(wType);
//		
//		if(mAnim != null)
//			mAnim.draw();
//		
//		if(wType.equals(RAIN_WITH_SNOW)) {
//			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).draw();
//		}
//		
//	}
//	
//	
//	
//	private void createAnimObject()
//	{	
//		String name = getClassName();
//		try
//		{
//			Class clz= Class.forName(name);
//			
//			IWeatherAnim mAnim = (IWeatherAnim) clz.getDeclaredConstructor(AuroraGLSurfaceView.class).newInstance(this);
//			
//			mAnimCash.put(wType , mAnim);
//			
//			judgeIfRainWithSnow();
//			
//		}
//		catch(Exception e)
//		{
//			e.printStackTrace();
//		}
//	}
//	
//	/**
//	 * 判断当前天气类型是否是雨夹雪，如果是并且mAnimCash中没有添加雨的动画的实例，则添加。
//	 * @throws Exception
//	 */
//	private void judgeIfRainWithSnow() throws Exception {
//		if(wType.equals(RAIN_WITH_SNOW) && !mAnimCash.containsKey(RAINTYPE_OF_RAIN_WITH_SNOW)) {
//			Class c = Class.forName((String)mData.getWeatherAnimInfo().getWeatherAnimClassName(RAINTYPE_OF_RAIN_WITH_SNOW));
//			IWeatherAnim anim = (IWeatherAnim) c.getDeclaredConstructor(AuroraGLSurfaceView.class).newInstance(this);
//			mAnimCash.put(RAINTYPE_OF_RAIN_WITH_SNOW, anim);
//		}
//	}
//	
//	private String getClassName()
//	{
//		//here we should make sure the weather type !!!
//		wType = getCityWeatherType();
//		
//		// test
//		//wType = getContext().getResources().getStringArray(R.array.weather_types)[mCityIndex+3];
//		
//		return (String)mData.getWeatherAnimInfo().getWeatherAnimClassName(wType);
//	}
//	
//	/**
//	 * get current city weather type !!!
//	 * @return
//	 */
//	private String getCityWeatherType()
//	{
//		String cityName = getCurCity();
//		if(mData.getCityWeatherInfo(cityName)==null)
//			return null;
//		String type = mData.getCityWeatherInfo(cityName).getHourInfo().getWeatherType();
//		return type;
//	}
//	
//	/**
//	 * get city Name
//	 * @return
//	 */
//	private String getCurCity()
//	{
//		WeatherCityInfo cityInfo = mData.getAllCitys().get(mCityIndex);
//		
//		return cityInfo.getCityName();
//	}
//	
//	
//	private void onCityChanged(int index)
//	{
//		
//		mCityIndex = index;
//		
//		if ( mCityIndex >= mData.getAllCitys().size() ) {
//			return;
//		}
//		
//		String type = getCityWeatherType();
//		
//		if(type!=null&&!wType.equals(type))
//		{
//			wType = type;
//			
//			if(mAnimCash.get(type) == null)
//			{
//				createAnimObject();
//			}
//			
//			if(mAnimCash.get(wType) != null) {
//				((AbstractWeatherAnim)mAnimCash.get(wType)).emptyAnimAlpha();
//			}
//			
//			if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
//				((AbstractWeatherAnim)mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW)).emptyAnimAlpha();
//			}	
//		}
//	}
//	
//	//因为activity的可见或者不可见都会影响到这个方法的调用，导致动画播放时间错乱
//	
////	@Override
////	protected void onVisibilityChanged(View changedView, int visibility)
////	{
////		if(visibility == View.GONE || visibility == View.INVISIBLE)
////    	{
////			onAnimPause();
////			Log.e("jadon", "不可见");
////		}
////		else if(visibility == View.VISIBLE)
////		{
////			onAnimResume();
////			Log.e("jadon", "可见");
////		}
////	}
//	
//	//因为activity的可见或者不可见都会影响到这个方法的调用，导致动画播放时间错乱
////	@Override
////    public void onWindowFocusChanged(boolean hasFocus) {
////        
////    	super.onWindowFocusChanged(hasFocus);
////    	if(hasFocus)
////    	{
////    		onAnimResume();
////    		Log.e("jadon", "有焦点");
////    	}
////    	else
////    	{
////    		Log.e("jadon", "无焦点");
////    		onAnimPause();
////    	}
////    	
////    }
//	
//	public void onAnimPause()
//	{
//		if(mAnim != null)
//			mAnim.onPause();
//		
//		if(wType.equals(RAIN_WITH_SNOW)) {
//			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).onPause();
//		}
//	}
//	
//	public void onAnimResume()
//	{
//		if(mAnim != null)
//			mAnim.onResume();
//		
//		if(wType.equals(RAIN_WITH_SNOW)) {
//			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).onResume();
//		}
//	}
//	
//	@Override
//	protected void onDetachedFromWindow()
//	{
//		mAnimCash.clear();
//		mAnimCash = null;
//	}
}

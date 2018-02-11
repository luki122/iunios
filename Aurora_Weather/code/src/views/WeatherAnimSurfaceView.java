package views;

import interfaces.IWeatherAnim;
import interfaces.IWeatherView;

import java.util.HashMap;

import com.aurora.weatherforecast.AuroraWeatherMain;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import datas.CityListShowData;

public class WeatherAnimSurfaceView extends SurfaceView implements SurfaceHolder.Callback, IWeatherView{
	
	private SurfaceHolder mHolder;
	private SusThread mThread;
	
	/**
	 * city which to show !!!
	 * each child get data throw mCityIndex 
	 */
	private int mCityIndex = 0;
	
	//Weather type
	private String wType = "";
	
	private IWeatherAnim mAnim;
	
	private HashMap<String , IWeatherAnim> mAnimCash = new HashMap<String , IWeatherAnim>();
	
	private static final String RAIN_WITH_SNOW = "雨夹雪";
	//雨夹雪天气类型中，雨的类型
	private static final String RAINTYPE_OF_RAIN_WITH_SNOW = "小雨";
	
	private Paint paint = new Paint();
	
	private Context context;
	private boolean isDrawing =true;
	
	private boolean isViewCreate = false;
	
	private static class SusThread extends Thread {  
		  
	    private String control = ""; // 只是需要一个对象而已，这个对象没有实际意义  
	    private boolean suspend = true;
	    public boolean mIsRunning = true;
	    private WeatherAnimSurfaceView mAnimSurfaceView;
	    
	    public SusThread(WeatherAnimSurfaceView mAnimSurfaceView){
	    	this.mAnimSurfaceView = mAnimSurfaceView;
	    }
	    
	    public void setSuspend(boolean suspend) {  
	        if (!suspend && mAnimSurfaceView.isViewCreate) {  
	            synchronized (control) {  
	                control.notifyAll();  
	            }  
	        }  
	        this.suspend = suspend;
	    }  
	  
	    public boolean isSuspend() {  
	        return suspend;  
	    }  
	  
	    public void run() {  
	        while (mIsRunning) {  
	            synchronized (control) {  
	                if (suspend) {  
	                    try {
	                    	mAnimSurfaceView.clear();
	                        control.wait();  
	                    } catch (InterruptedException e) {  
	                        e.printStackTrace();  
	                    }  
	                }  
	            } 
	            if(!suspend)
	            {
		            mAnimSurfaceView.surfaceDraw();
	            }
	        } 
	        if(mAnimSurfaceView != null)
	        {
	        	mAnimSurfaceView.clear();
	        }
	    }  
	}
	
	private void clear(){
		Canvas canvas = null;
		try {
			canvas = mHolder.lockCanvas();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		if(canvas != null)
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		try {
			mHolder.unlockCanvasAndPost(canvas);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void surfaceDraw(){
		if(!isViewCreate)
			return;
		   Canvas canvas = null;
		try {
			canvas = mHolder.lockCanvas();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
			if (canvas != null) {
				doDraw( canvas );
			}

			if (null != canvas) {
				try {
					mHolder.unlockCanvasAndPost(canvas);
					canvas = null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	public void stop(){
		if(mThread!=null)
		{
		   mThread.setSuspend(true);
		}
		Log.e(TAG, "stop");
	}
	
	
	public void start(){
		Log.e(TAG, "start");
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mThread!=null)
				{
					if(mThread.isSuspend() && isViewCreate)
					{
				       mThread.setSuspend(false);
				       if(mAnimCash.get(wType) != null) {
							((AbstractWeatherAnim)mAnimCash.get(wType)).emptyAnimAlpha();
						}
						
						if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
							((AbstractWeatherAnim)mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW)).emptyAnimAlpha();
						}	
					}
				}
			}
		}, 800);
	}
	
	public WeatherAnimSurfaceView(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}
	
	public WeatherAnimSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public WeatherAnimSurfaceView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		
		init(context);
		
	}
	private CityListShowData mCityListShowData;
	private void init(Context context) {
		this.context=context;
		mCityListShowData=CityListShowData.getInstance(context);
		setZOrderOnTop(true);
		mThread = new SusThread(this);
		mThread.start();
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setFormat(PixelFormat.TRANSLUCENT); // 顶层绘制SurfaceView设成透明
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
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

	@Override
	public void onStateChanged(int state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if(mThread != null)
		{
			mThread.mIsRunning = false;
			mThread.setSuspend(false);
		}
	}
	
	public void destoryDrawThread(){
		if(mThread != null)
		{
			mThread.mIsRunning = false;
			mThread.setSuspend(false);
			mThread = null;
		}
	}

	@Override
	public void onUpdating() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void setIndex(int index) {
		// TODO Auto-generated method stub
		if(mAnim != null)
			mAnim.onPause();
		
		if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).onPause();
		}
		
		if(onCityChanged(index))
		{
			onAnimResume();
		}
		
	}
	
	private void createAnimObject()
	{	
		String name = getClassName();
		try
		{
			Class clz= Class.forName(name);
			
			IWeatherAnim mAnim = (IWeatherAnim) clz.getDeclaredConstructor(SurfaceView.class).newInstance(this);
			
			mAnimCash.put(wType , mAnim);
			
			judgeIfRainWithSnow();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 判断当前天气类型是否是雨夹雪，如果是并且mAnimCash中没有添加雨的动画的实例，则添加。
	 * @throws Exception
	 */
	private void judgeIfRainWithSnow() throws Exception {
		if(wType.equals(RAIN_WITH_SNOW) && !mAnimCash.containsKey(RAINTYPE_OF_RAIN_WITH_SNOW)) {
			Class c = Class.forName((String)mCityListShowData.getWeatherAnimInfo().getWeatherAnimClassName(RAINTYPE_OF_RAIN_WITH_SNOW));
			IWeatherAnim anim = (IWeatherAnim) c.getDeclaredConstructor(SurfaceView.class).newInstance(this);
			mAnimCash.put(RAINTYPE_OF_RAIN_WITH_SNOW, anim);
		}
	}
	
	private String getClassName()
	{
		//here we should make sure the weather type !!!
		wType = getCityWeatherType();
		
		// test
		//wType = getContext().getResources().getStringArray(R.array.weather_types)[mCityIndex+3];
		
		return (String)mCityListShowData.getWeatherAnimInfo().getWeatherAnimClassName(wType);
	}
	
	/**
	 * get current city weather type !!!
	 * @return
	 */
	private String getCityWeatherType()
	{
		String type = mCityListShowData.getWeatherDateItem(mCityIndex).getWeahterType();
		return type;
	}
	
	private String getCityWeatherType(int index)
	{
		String type = mCityListShowData.getWeatherDateItem(index).getWeahterType();
		return type;
	}
	
	private boolean onCityChanged(int index)
	{
		mCityIndex = index;
		
		if ( mCityIndex >= mCityListShowData.getDatas().size() ) {
			return false;
		}
		
		String type = getCityWeatherType();
		
		if(type!=null&&!wType.equals(type))
		{
			wType = type;
			
			if(mAnimCash.get(type) == null)
			{
				createAnimObject();
			}
			
			mAnim = mAnimCash.get(wType);
			
			if(mAnimCash.get(wType) != null) {
				((AbstractWeatherAnim)mAnimCash.get(wType)).emptyAnimAlpha();
			}
			
			if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
				((AbstractWeatherAnim)mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW)).emptyAnimAlpha();
			}	
		}
		return true;
	}

	private final String TAG = "jadon1";
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		isViewCreate = true; 
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(mThread !=null)
		{
			mThread.setSuspend(true);
		}
		isViewCreate = false;
	}

	public void setAlphaFactory(float alphaFactory){
		if(mAnim==null)
			return;
		((AbstractWeatherAnim)mAnim).setAlphaFactory(alphaFactory);
	}
	
	private float alphaFactory=1;
	private synchronized void doDraw(Canvas canvas) {
		
		canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		
		mAnim = mAnimCash.get(wType);

		if(mAnim != null)
			mAnim.draw(canvas);

		if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).draw(canvas);
		}
	}
	
	public void onAnimPause()
	{
		if(mAnim != null)
			mAnim.onPause();
		
		if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).onPause();
		}
		if(mThread != null)
			mThread.setSuspend(true);
	}
	
	public void onAnimResume()
	{
		//Log.e("333333", "-onAnimResume mAnim = ---" + mAnim);
		if(mAnim != null)
			mAnim.onResume();
		
		if(wType.equals(RAIN_WITH_SNOW) && mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW) != null) {
			mAnimCash.get(RAINTYPE_OF_RAIN_WITH_SNOW).onResume();
		}
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mThread != null && isViewCreate)
					mThread.setSuspend(false);
			}
		}, 800);
	}
	
	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow()
	{
		mAnimCash.clear();
		mAnimCash = null;
	}

}

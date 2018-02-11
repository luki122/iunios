package views;

import com.aurora.weatherforecast.R;

import views.TemperatureView.Data;
import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.aurora.utils.DensityUtil;
/**
 * 天气详情动画的布局
 * @author j
 *
 */
@SuppressLint("NewApi")
public class AnimLinearlayout extends LinearLayout implements AnimatorListener,AnimationListener{

	private TemperatureView temperatureView;
	private Context context;
	private FrameLayout topLayout;
	private final int DURAION=300;
	
	public AnimLinearlayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public AnimLinearlayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public AnimLinearlayout(Context context) {
		super(context);
		initView(context);
	}

	
	@SuppressLint("NewApi")
	private void initView(Context context){
		this.context=context;
		setOrientation(LinearLayout.VERTICAL);
		topLayout=new FrameLayout(context);
		addView(topLayout,LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		setBottomView();
		setTemperatureView();
		setAnimBg();
		addLowestAndHighTempView();
		setLineView();
	}
	
	private void setTemperatureView(){
		temperatureView= new TemperatureView(context);
		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		topLayout.addView(temperatureView,lp);
	}
	
      public void setHideViewBg(Drawable d,int y){
    	  Bitmap.Config config = d.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888:Bitmap.Config.RGB_565; 
    	Bitmap bit=Bitmap.createBitmap(temperatureView.getTWidth(), getResources().getDisplayMetrics().heightPixels, config);
    	Canvas canvas=new Canvas(bit);
    	d.setBounds(0, 0, bit.getWidth(), bit.getHeight());
    	d.draw(canvas);
    	bit=Bitmap.createBitmap(bit, 0, y, temperatureView.getTWidth(), temperatureView.getTHeight());
		rl.setBackgroundDrawable(new BitmapDrawable(bit));
	  }
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(temperatureView!=null&&wheatherBottomContainer!=null)
		{
			setMeasuredDimension((int)(temperatureView.getTWidth()), (int)(temperatureView.getTHeight()+getViewWidthAndHeight(wheatherBottomContainer)[1]));
		}
		
	}
	
	public void setData() {
		float[] ts = new float[8];
		for (int i = 0; i < ts.length; i++) {
			ts[i] = (float) (Math.random() * 20+10);
		}
		temperatureView.setTemperatures(ts);
		wheatherBottomContainer.setDateList(temperatureView.getTData());
		setPosition();
	}
	private FrameLayout rl;
	private TranslateAnimation tl_in,tl_out;
	@SuppressLint("NewApi")
	private void setAnimBg(){
		tl_in=new TranslateAnimation(0, temperatureView.getTWidth(), 0, 0);
	    tl_out=new TranslateAnimation(-temperatureView.getTWidth(),0, 0, 0);
		rl=new FrameLayout(context);
		final ImageView iv_anim=new ImageView(context);
		iv_anim.setBackgroundResource(R.drawable.bottom);
		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,temperatureView.getTHeight());
		rl.addView(iv_anim, lp);
		topLayout.addView(rl, lp);
		tl_in.setDuration(DURAION);
		tl_out.setDuration(DURAION);
		tl_in.setAnimationListener(this);
		tl_out.setAnimationListener(this);
	}
	
	
	/**
	 * 根据数据计算子布局的位置
	 */
	public void setPosition(){
		if(lv!=null)lv.setDate(temperatureView.getTData());
        if(tv_high!=null&&tv_lower!=null)setLowestAnidHighPosition();
        
	}
	
	public void startAnim(){
		rl.startAnimation(tl_in);
	}
	
	public void outAnim(){
		rl.startAnimation(tl_out);
		tempDataLowOutAnimator.start();
		tempDataHighOutAnimator.start();
		wheatherBottomContainer.startAnimOut();
		lv.startAnimOut();
	}
	
	private LineView lv;
	private void setLineView(){
		lv=new LineView(context);
		FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(temperatureView.getTWidth(),temperatureView.getTHeight());
		lv.setLayoutParams(lp);
		topLayout.addView(lv, lp);
	}
	
	private WheatherBottomContainer wheatherBottomContainer;
	private void setBottomView(){
		wheatherBottomContainer=(WheatherBottomContainer)LayoutInflater.from(context).inflate(R.layout.weather_bottom_container_layout, null);
		addView(wheatherBottomContainer,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
	}
	
	
	private ObjectAnimator tempDataHighInAnimator,tempDataLowInAnimator,tempDataHighOutAnimator,tempDataLowOutAnimator;
	
	private void setLowestAnidHighPosition(){
		float delY=DensityUtil.dip2px(context, 15);
		Data highData=temperatureView.getHighestData();
		Data lowData=temperatureView.getLowestData();
		int[] whh=getViewWidthAndHeight(tv_high);
		int[] whl=getViewWidthAndHeight(tv_lower);
		if(highData.px<0.1)
		{
			tv_high.setX(highData.px+delY);
		}else if(Math.abs(highData.px-temperatureView.getTWidth())<0.1)
		{
			tv_high.setX(highData.px-whh[0]-delY);
		}else{
			tv_high.setX(highData.px-whh[0]/2);
		}
		
		if(lowData.px<0.1)
		{
			tv_lower.setX(lowData.px+delY);
		}else if(Math.abs(lowData.px-temperatureView.getTWidth())<0.1)
		{
			tv_lower.setX(lowData.px-whh[0]-delY);
		}else{
			tv_lower.setX(lowData.px-whh[0]/2);
		}
		tv_high.setY(highData.py-DensityUtil.dip2px(context, 4));
		tv_lower.setY(lowData.py-DensityUtil.dip2px(context, 4));
		PropertyValuesHolder ip1=PropertyValuesHolder.ofFloat("y", highData.py-whh[1]+delY,highData.py-whh[1]);
		PropertyValuesHolder ip2=PropertyValuesHolder.ofFloat("alpha", 0,1);
		PropertyValuesHolder ip3=PropertyValuesHolder.ofFloat("y", lowData.py-whh[1]+delY,lowData.py-whl[1]);
		PropertyValuesHolder ip4=PropertyValuesHolder.ofFloat("alpha", 0,1);
		tempDataHighInAnimator=ObjectAnimator.ofPropertyValuesHolder(tv_high, ip1,ip2).setDuration(500);
		tempDataLowInAnimator=ObjectAnimator.ofPropertyValuesHolder(tv_lower, ip3,ip4).setDuration(500);
		PropertyValuesHolder op1=PropertyValuesHolder.ofFloat("y", highData.py-whh[1],highData.py-whh[1]+delY);
		PropertyValuesHolder op2=PropertyValuesHolder.ofFloat("alpha", 1,0);
		PropertyValuesHolder op3=PropertyValuesHolder.ofFloat("y",lowData.py-whl[1], lowData.py-whh[1]+delY);
		PropertyValuesHolder op4=PropertyValuesHolder.ofFloat("alpha", 1,0);
		tempDataHighOutAnimator=ObjectAnimator.ofPropertyValuesHolder(tv_high, op1,op2).setDuration(DURAION);
		tempDataLowOutAnimator=ObjectAnimator.ofPropertyValuesHolder(tv_lower, op3,op4).setDuration(DURAION);
		tempDataHighInAnimator.addListener(this);
		tempDataLowOutAnimator.addListener(this);
	}
	private TextView tv_high,tv_lower;
	@SuppressLint("NewApi")
	private void addLowestAndHighTempView(){
		tv_high=new TextView(context);
		tv_high.setTextSize(15);
		tv_lower=new TextView(context);
		tv_lower.setTextSize(15);
		tv_high.setVisibility(View.INVISIBLE);
		tv_lower.setVisibility(View.INVISIBLE);
		tv_high.setTextColor(Color.WHITE);
		tv_lower.setTextColor(Color.WHITE);
		tv_high.setText("最高");
		tv_lower.setText("最低");
		topLayout.addView(tv_high);
		topLayout.addView(tv_lower);
		
	}
	
	public int[] getViewWidthAndHeight(View view){
		int[] wh=new int[2];
		int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
		view.measure(width,height);
		wh[0]=view.getMeasuredWidth();
		wh[1]=view.getMeasuredHeight();
		return wh;
	}

	/**
	 * AnimatorListener
	 */
	@Override
	public void onAnimationStart(Animator animation) {
		if(animation==tempDataHighInAnimator)
		{
			tv_high.setVisibility(View.VISIBLE);
			tv_lower.setVisibility(View.VISIBLE);
		}else if(animation==tempDataHighOutAnimator){
			tv_high.setVisibility(View.VISIBLE);
			tv_lower.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		// TODO Auto-generated method stub
		   if(animation==tempDataHighOutAnimator){
			tv_high.setVisibility(View.INVISIBLE);
			tv_lower.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onAnimationCancel(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onAnimationRepeat(Animator animation) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * AnimationListener
	 */
	@Override
	public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		if(animation==tl_in)
		{
			rl.setVisibility(View.VISIBLE);
		}else if(animation==tl_out)
		{
			rl.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		// TODO Auto-generated method stub
		if(animation==tl_in)
		{
			rl.setVisibility(View.INVISIBLE);
			wheatherBottomContainer.startAnimIn();
			tempDataHighInAnimator.start();
			tempDataLowInAnimator.start();
			lv.startAnimIn();
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	}
	
}

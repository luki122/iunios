package com.netmanage.view;

import java.util.concurrent.atomic.AtomicBoolean;
import com.aurora.netmanage.R;
import com.netmanage.interfaces.TweensAnimCallBack;
import com.netmanage.model.ConfigModel;
import com.netmanage.utils.FlowUtils;
import com.netmanage.animation.TweensAnimation;
import com.netmanage.data.ConfigData;
import com.netmanage.data.Constants;
import com.netmanage.data.WaveData;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.GetChars;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout.LayoutParams;

public abstract class MaskedImage extends ImageView {
	private static final Xfermode MASK_XFERMODE;
	private final int WAVE_ANI_DURING_TIME = 2100;
	private final String CHINA_LANGUAGE = "中文";
	private Bitmap mask;
	private Paint paint;
	private WaveData frontWaveData,backWaveData;
	private Bitmap front_waves_img,back_waves_img;
	private int frontWavesImgWidth,backWavesImgWidth;
	private int frontWaveMoveMaxDistance,backWaveMoveMaxDistance;
	private Animation waveAnim  = null;
	private int lastProgress = -1;
	private float frontWaveTopMargin;
	private float backWaveTopMargin;
	private int scale = 1;
	private ProcessChangeCallBack mProcessChangeCallBack;
	private DisplayMetrics dm;
	private int fatherLayoutWidth;
	private Resources resources;
	private String curLanguage;

	static {
		PorterDuff.Mode localMode = PorterDuff.Mode.DST_IN;
		MASK_XFERMODE = new PorterDuffXfermode(localMode);
	}

	public MaskedImage(Context paramContext) {
		super(paramContext);
		initView();
	}

	public MaskedImage(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		initView();
	}

	public MaskedImage(Context paramContext, AttributeSet paramAttributeSet,
			int paramInt) {
		super(paramContext, paramAttributeSet, paramInt);
		initView();
	}
	
	int getMyWidth(){
		int width = getWidth();
		if(width == 0){
			width = (int)(resources.getDimension(R.dimen.main_act_progress_view_size));
		}
		return width;
	}
	
	int getMyHeight(){
		int height = getHeight();
		if(height == 0){
			height = (int)(resources.getDimension(R.dimen.main_act_progress_view_size));
		}
		return height;
	}
	
	private void initView(){	
		curLanguage = getResources().getConfiguration().locale.getDisplayLanguage();				
		front_waves_img =((BitmapDrawable)getResources().getDrawable(R.drawable.front_waves_img)).getBitmap();
		back_waves_img = ((BitmapDrawable)getResources().getDrawable(R.drawable.back_waves_img)).getBitmap();
		resources = getContext().getResources();
		dm = getContext().getResources().getDisplayMetrics(); 	
		scale = dm.densityDpi/160;

		frontWavesImgWidth = front_waves_img.getWidth();
		backWavesImgWidth = back_waves_img.getWidth();
		
		frontWaveMoveMaxDistance = Math.abs(frontWavesImgWidth-getMyWidth())-5*scale;
		backWaveMoveMaxDistance = Math.abs(backWavesImgWidth-getMyWidth());
		
		frontWaveData = new WaveData(WaveData.WAVE_WHERE_OF_FRONT);
		backWaveData = new WaveData(WaveData.WAVE_WHERE_OF_BACK);
		
		frontWaveData.initOrReset(WaveData.MOVE_TO_RIGHT, 
				frontWaveMoveMaxDistance, 
				getMyWidth()-frontWavesImgWidth);
		
		backWaveData.initOrReset(WaveData.MOVE_TO_LEFT, 
				backWaveMoveMaxDistance, 
				0);		
		
		Bitmap num_zero  = ((BitmapDrawable)getResources().
				getDrawable(R.drawable.num_zero)).getBitmap();
		numImgWidth = num_zero.getWidth();	
		
		Bitmap flow_mb  = ((BitmapDrawable)getResources().
				getDrawable(R.drawable.remainder_gb_img)).getBitmap();
		flowMbImgWidth = flow_mb.getWidth();		
		numToFlowMbDistance = (int)(resources.getDimension(R.dimen.main_act_num_to_flow_mb_distance));
		fatherLayoutWidth = (int)(resources.getDimension(R.dimen.main_act_enter_animation_size));
	}
	
	public void initAnim(){
		curProgress = 0;
		changeProgress(0);
	}

	public abstract Bitmap createMask();

	protected void onDraw(Canvas paramCanvas) {
		try {
			if (this.paint == null) {			
				this.paint = new Paint();
				/**
				 * 如果该项设置为true，则图像在动画进行中会滤掉对Bitmap图像的优化操作，
				 * 加快显示 速度，本设置项依赖于dither和xfermode的设置
				 */
				this.paint.setFilterBitmap(false);
				this.paint.setXfermode(MASK_XFERMODE);
			}
			
			float f1 = getMyWidth();
			float f2 = getMyHeight();
			
			int i = paramCanvas.saveLayer(0.0F, 0.0F, f1, f2, null, Canvas.ALL_SAVE_FLAG);			
			paramCanvas.drawBitmap(front_waves_img,frontWaveData.getCurLeftMargin(),frontWaveTopMargin, null);
			paramCanvas.drawBitmap(back_waves_img,backWaveData.getCurLeftMargin(),backWaveTopMargin, null);
			
			if ((this.mask == null) || (this.mask.isRecycled())) {
				Bitmap localBitmap1 = createMask();
				this.mask = localBitmap1;
			}
			
			paramCanvas.drawBitmap(mask, 0.0F, 0.0F, paint);
			paramCanvas.restoreToCount(i);
			return;
		} catch (Exception localException) {
			StringBuilder localStringBuilder = new StringBuilder()
					.append("Attempting to draw with recycled bitmap. View ID = ");
			System.out.println("localStringBuilder==" + localStringBuilder);
		}
	}
	
	public void startAnimPublic(){
		startWaveAnim();
 	}
	
	private void startWaveAnim(){ 
		if(waveAnim == null){
			waveAnim = new TweensAnimation(new TweensAnimCallBack(){
				public void callBack(float interpolatedTime, Transformation t) {
					if(interpolatedTime == 1){
						animationEnd();
					}else{
						frontWaveData.computeCurLeftMargin(interpolatedTime);
						backWaveData.computeCurLeftMargin(interpolatedTime);
					}
					postInvalidate();
			}});
			waveAnim.setDuration(WAVE_ANI_DURING_TIME);
			waveAnim.setInterpolator(new LinearInterpolator());
		}
		clearAnimation();
		startAnimation(waveAnim);	
	}
		
	/**
	 * 动画一轮执行完毕后调用的函数
	 */
	private void animationEnd(){			
		frontWaveData.initOrReset(WaveData.MOVE_TO_LEFT^frontWaveData.getMoveTo(), 
				frontWaveMoveMaxDistance, 
			    frontWaveData.getMoveTo()==WaveData.MOVE_TO_LEFT?
			    		getMyWidth()-frontWavesImgWidth:0);
		
		backWaveData.initOrReset(WaveData.MOVE_TO_LEFT^backWaveData.getMoveTo(), 
				backWaveMoveMaxDistance, 
				backWaveData.getMoveTo()==WaveData.MOVE_TO_LEFT?
			    		getMyWidth()-backWavesImgWidth:0);	
				
		startWaveAnim();
	}
	 	 
/*********************begin**********************/
	private AtomicBoolean isDuringSetProgress= new AtomicBoolean(false);
	private int curProgress = 0;
	private boolean isWarningProgress;
	private int numImgWidth;
	private int flowMbImgWidth;
	private int numToFlowMbDistance;
	private LinearLayout flowNumLayout ;
	
	 /**
	  * 设置进度
	  * @param isNeedAnim
	  * @param monthlyFlow(单位KB)
	  * @param usedFlow(单位KB)
	  */
	 public void setProgress(boolean isNeedAnim,final long totalFlow,final long progressFlow){
		 if(isDuringSetProgress.get()){
				return ;
		 }
		 isDuringSetProgress.set(true);
		 setVisibility(View.VISIBLE);
		 if(isNeedAnim){
				postDelayed(new Runnable() {			
					@Override
					public void run() {
						setProgressWithAnim(totalFlow,progressFlow); 
					}
				}, EnterAnimationView.PROCESS_ANI_WAIT_TIME);				 
		 }else{
			 setProgressNoAnim(totalFlow,progressFlow); 
		 }	 
	 }
	
	/**
	 * 设置进度,不并伴随动画
	 * @param totalFlow (单位KB)
	 * @param progressFlow (单位KB)
	 */
	private void setProgressNoAnim(final long totalFlow,final long progressFlow){
		calculateCurProgress(totalFlow,progressFlow);	
		resetViewOfNum();
		changeProgress(getProgressForAnim(1));
		updateViewOfNum(totalFlow,progressFlow,1);
		isDuringSetProgress.set(false);
	}	
	
	boolean isWaveAniEnd = false;
	boolean isTextAniEnd = false;
	
	/**
	 * 设置进度,并伴随动画
	 * @param totalFlow (单位KB)
	 * @param progressFlow (单位KB)
	 */
	private void setProgressWithAnim(final long totalFlow,final long progressFlow){
		isWaveAniEnd = false;
		isTextAniEnd = false;
		final Object lock = new Object();
		
		calculateCurProgress(totalFlow,progressFlow);
		resetViewOfNum();
        //播放水波纹动画
        Animation animForWave = new TweensAnimation(new TweensAnimCallBack(){
			public void callBack(float interpolatedTime, Transformation t) {
				changeProgress(getProgressForAnim(interpolatedTime));
				if(interpolatedTime == 1){
					synchronized (lock) {
						isWaveAniEnd = true;
						if(isWaveAniEnd && isTextAniEnd){
							isDuringSetProgress.set(false);
						}
					}				
					startAnimPublic();
				}
		}});
        long duringTimeOfWaveTime = EnterAnimationView.PROCESS_ANI_DURING_TIME*curProgress/100;
        if(duringTimeOfWaveTime <= EnterAnimationView.PROCESS_ANI_DURING_TIME*30/100){
        	duringTimeOfWaveTime = EnterAnimationView.PROCESS_ANI_DURING_TIME*30/100;
        }       
        animForWave.setDuration(duringTimeOfWaveTime);
        animForWave.setInterpolator(new LinearInterpolator());
		startAnimation(animForWave);	
		
		//播放流量值动画
		Handler waitForCanChangeFlowVaule = new Handler();
		waitForCanChangeFlowVaule.postDelayed(new Runnable() {			
			@Override
			public void run() {
				Animation animForFlow = new TweensAnimation(new TweensAnimCallBack(){
					public void callBack(float interpolatedTime, Transformation t) {
						updateViewOfNum(totalFlow,progressFlow,interpolatedTime);
						if(interpolatedTime == 1){
							synchronized (lock) {
								isTextAniEnd = true;
								if(isWaveAniEnd && isTextAniEnd){
									isDuringSetProgress.set(false);
								}
							}	
						}
				}});
				animForFlow.setDuration(EnterAnimationView.FLOW_VALUE_ANI_DURING_TIME);
				animForFlow.setInterpolator(new LinearInterpolator());
				getBitImg().startAnimation(animForFlow);				
			}
		}, EnterAnimationView.FLOW_VALUE_ANI_WAIT_TIME);
	}
	
	/**
	 * 根据总流量和当前流量值计算进度条的比例
	 * @param totalFlow
	 * @param progressFlow
	 * @return
	 */
	public static synchronized int getProgressValue(long totalFlow,long progressFlow){
		int progressValue = 0;
		if(totalFlow == 0){
			progressValue = 50;
		}else{
			progressValue =(int)Math.round(100.0*progressFlow/totalFlow);
		}
		if(progressValue <0){
			progressValue = 0;
		}else if(progressValue >100){
			progressValue = 100;
		}
		return progressValue;
	}
	
	/**
	 * 计算curProgress的值
	 * @param totalFlow
	 * @param progressFlow
	 */
	private void calculateCurProgress(long totalFlow,long progressFlow){
		curProgress = getProgressValue(totalFlow,progressFlow);
		isWarningProgress = FlowUtils.isWarningProgress(getContext());
	}
	
	private ImageView kiloImg;
	private ImageView hundredImg;
	private ImageView tenImg;
	private ImageView bitImg;
	private ImageView flowUnitImg;
	private ImageView decimalPointImg;
	private ImageView decimal1Img;
	private ImageView decimal2Img;
	
	private long curNum = -1;
	private int curNumOfK = -1;
	private int curNumOfH = -1;
	private int curNumOfT = -1;
	private int curNumOfA = -1;
	private int curNumOfDecimal1 = -1; 
	private int curNumOfDecimal2 = -1; 
	
	private void resetViewOfNum(){
		curNum = curNumOfK = curNumOfH = curNumOfT = curNumOfA = -1;	
		getKiloImg().setVisibility(View.GONE);
		getKiloImg().setImageResource(Constants.num[0]);
		
		getHundredImg().setVisibility(View.GONE);
		getHundredImg().setImageResource(Constants.num[0]);
		
		getTenImg().setVisibility(View.GONE);
		getTenImg().setImageResource(Constants.num[0]);
		
		getBitImg().setVisibility(View.GONE);
		getBitImg().setImageResource(Constants.num[0]);
//		setNumLayoutParams(false,false,false);
	}
	
	/**
	 * 获取当前在主界面显示的流量值（带单位），传给桌面
	 * @param totalFlow
	 * @param progressFlow
	 * @return
	 */
	public static synchronized String getFlowNumStr(long totalFlow,long progressFlow){
		String flowUnit = null;
		double scale = 1.0;//必须要这样做，如果progressFlow=9999GB的时候，就会出现数据溢出
        long tmpNum = Math.round(progressFlow*scale);	//起始值为0	
		boolean isExceedMonthlyFlow = false;//是否超出月结流量
		if(tmpNum < 0){
			isExceedMonthlyFlow = true;
			tmpNum = Math.abs(tmpNum);
		}
			
		double showNum;
        if(progressFlow >= 1024 || totalFlow >= 1024){//使用流量已经操过1M
        	if(tmpNum>1024*1024){
    			if(totalFlow == 0){
    				flowUnit = "GB";
    			}else{
    				if(isExceedMonthlyFlow){
    					flowUnit = "GB";
    				}else{
    					flowUnit = "GB";
    				} 				
    			}	   			
    			showNum = 1.0*tmpNum/(1024*1024);
    			if(showNum < 10 ){
    				showNum = Math.round(showNum*100)/100.0;
    			}else if(showNum < 100){
    				showNum = Math.round(showNum*10)/10.0;
    			}else{
    				showNum = (int)Math.round(showNum);
    			}
    		}else{
    			if(totalFlow == 0){
    				flowUnit = "MB";
    			}else{
    				if(isExceedMonthlyFlow){
    					flowUnit = "MB";
    				}else{
    					flowUnit = "MB";
    				} 	  					
    			}		
    			showNum = (int)Math.round(1.0*tmpNum/(1024));
    		}
		}else{
			if(totalFlow == 0){
				flowUnit = "KB";
			}else{
				if(isExceedMonthlyFlow){
					flowUnit = "KB";
				}else{
					flowUnit = "KB";
				} 				
			}		
			showNum = (int)tmpNum;
		}
		int tmpNumOfDecimal1 = (int)(showNum*10%10); 
		int tmpNumOfDecimal2 = (int)(showNum*100%10); 
		if(tmpNumOfDecimal1 ==0 && tmpNumOfDecimal2==0){
			return (long)showNum+flowUnit;
		}else{
			return showNum+flowUnit;
		}        
	}
	
	/**
	 * @param totalFlow 单位为KB
	 * @param progressFlow 单位为KB
	 * @param interpolatedTime
	 */
	private void updateViewOfNum(long totalFlow,long progressFlow,float interpolatedTime){
        double scale = interpolatedTime;//必须要这样做，如果progressFlow=9999GB的时候，就会出现数据溢出	
        long tmpNum = Math.round(progressFlow*scale);	//起始值为0	
		if(tmpNum == curNum){
			return ;
		}
		curNum = tmpNum;
		boolean isExceedMonthlyFlow = false;//是否超出月结流量
		if(tmpNum < 0){
			isExceedMonthlyFlow = true;
			tmpNum = Math.abs(tmpNum);
		}
		double showNum;
        if(progressFlow >= 1024 || totalFlow >= 1024){//使用流量已经操过1M
        	if(tmpNum>1024*1024){
    			if(totalFlow == 0){
    				if(CHINA_LANGUAGE.equals(curLanguage)){
    					setFlowUnitImg(R.drawable.use_gb_img);
    				}else{
    					setFlowUnitImg(R.drawable.use_gb_img_en);
    				}    				
    			}else{
    				if(isExceedMonthlyFlow){
    					if(CHINA_LANGUAGE.equals(curLanguage)){
    						setFlowUnitImg(R.drawable.exceed_gb_img);
        				}else{
        					setFlowUnitImg(R.drawable.exceed_gb_img_en);
        				}   					
    				}else{
    					if(CHINA_LANGUAGE.equals(curLanguage)){
    						setFlowUnitImg(R.drawable.remainder_gb_img);
        				}else{
        					setFlowUnitImg(R.drawable.remainder_gb_img_en);
        				}    					
    				} 				
    			}	
    			if(interpolatedTime < 1 ){
    				showNum = (int)Math.round(1.0*tmpNum/(1024*1024));
    			}else{
    				showNum = 1.0*tmpNum/(1024*1024);
        			if(showNum < 10 ){
        				showNum = Math.round(showNum*100)/100.0;
        			}else if(showNum < 100){
        				showNum = Math.round(showNum*10)/10.0;
        			}else{
        				showNum = (int)Math.round(showNum);
        			}
    			}  			
    		}else{
    			if(totalFlow == 0){ 				
    				if(CHINA_LANGUAGE.equals(curLanguage)){
    					setFlowUnitImg(R.drawable.use_mb_img);
    				}else{
    					setFlowUnitImg(R.drawable.use_mb_img_en);
    				} 
    			}else{
    				if(isExceedMonthlyFlow){
    					if(CHINA_LANGUAGE.equals(curLanguage)){
    						setFlowUnitImg(R.drawable.exceed_mb_img);
        				}else{
        					setFlowUnitImg(R.drawable.exceed_mb_img_en);
        				}    					
    				}else{
    					if(CHINA_LANGUAGE.equals(curLanguage)){
    						setFlowUnitImg(R.drawable.remainder_mb_img);
        				}else{
        					setFlowUnitImg(R.drawable.remainder_mb_img_en);
        				}      					
    				} 	  					
    			}		
    			showNum = (int)Math.round(1.0*tmpNum/(1024));
    		}
		}else{
			if(totalFlow == 0){
				if(CHINA_LANGUAGE.equals(curLanguage)){
					setFlowUnitImg(R.drawable.use_kb_img);
				}else{
					setFlowUnitImg(R.drawable.use_kb_img_en);
				}				
			}else{
				if(isExceedMonthlyFlow){
					if(CHINA_LANGUAGE.equals(curLanguage)){
						setFlowUnitImg(R.drawable.exceed_kb_img);
    				}else{
    					setFlowUnitImg(R.drawable.exceed_kb_img_en);
    				} 				
				}else{
					if(CHINA_LANGUAGE.equals(curLanguage)){
						setFlowUnitImg(R.drawable.remainder_kb_img);
    				}else{
    					setFlowUnitImg(R.drawable.remainder_kb_img_en);
    				}					
				} 				
			}		
			showNum = (int)tmpNum;
		}
        Log.i("ProgressScrollView", "showNum="+showNum);
		
		int tmpNumOfK = (int)(showNum/1000);
		int tmpNumOfH = (int)(showNum%1000/100);
		int tmpNumOfT = (int)(showNum%100/10);
		int tmpNumOfA = (int)(showNum%10);
		int tmpNumOfDecimal1 = (int)(showNum*10%10); 
		int tmpNumOfDecimal2 = (int)(showNum*100%10); 
		boolean kShow = false,hShow = false,tShow = false;
		boolean decimal1Show = false,decimal2Show = false;
		
		if(tmpNumOfK > 0){
			kShow = hShow = tShow = true;
		}else if(tmpNumOfH > 0){
			hShow = tShow = true;
		}else if(tmpNumOfT > 0){
			tShow = true;
		}
		
		if(tmpNumOfDecimal2 > 0){
			decimal1Show = decimal2Show = true;
		}else if(tmpNumOfDecimal1 > 0){
			decimal1Show = true;
		}
		
		if(kShow){	
			if(getKiloImg().getVisibility() != View.VISIBLE){
				getKiloImg().setVisibility(View.VISIBLE);	
			}
			if(tmpNumOfK!=curNumOfK){
				curNumOfK = tmpNumOfK;			
				getKiloImg().setImageResource(getNumImgRes(tmpNumOfK));
			}			
		}else {
			if(getKiloImg().getVisibility() == View.VISIBLE){
				getKiloImg().setVisibility(View.GONE);
			}
		}
		
		if(hShow){
			if(getHundredImg().getVisibility() != View.VISIBLE){
				getHundredImg().setVisibility(View.VISIBLE);
			}
			if(tmpNumOfH!=curNumOfH){
				curNumOfH = tmpNumOfH;			
				getHundredImg().setImageResource(getNumImgRes(tmpNumOfH));
			}		
		}else {
			if(getHundredImg().getVisibility() == View.VISIBLE){
				getHundredImg().setVisibility(View.GONE);
			}
		}
		
		if(tShow){
			if(getTenImg().getVisibility() != View.VISIBLE){
				getTenImg().setVisibility(View.VISIBLE);
			}
			if(tmpNumOfT!=curNumOfT){
				curNumOfT = tmpNumOfT;
				getTenImg().setImageResource(getNumImgRes(tmpNumOfT));
			}		
		}else {
			if(getTenImg().getVisibility() == View.VISIBLE){
				getTenImg().setVisibility(View.GONE);
			}
		}
		
		if(tmpNumOfA!=curNumOfA){
			curNumOfA = tmpNumOfA;
			getBitImg().setVisibility(View.VISIBLE);
			getBitImg().setImageResource(getNumImgRes(tmpNumOfA));
		}
		
		if(decimal2Show){
			if(getDecimal2Img().getVisibility() != View.VISIBLE){
				getDecimal2Img().setVisibility(View.VISIBLE);
			}
			if(tmpNumOfDecimal2!=curNumOfDecimal2){
				curNumOfDecimal2 = tmpNumOfDecimal2;
				getDecimal2Img().setImageResource(getNumImgRes(tmpNumOfDecimal2));
			}	
		}else{
			if(getDecimal2Img().getVisibility() == View.VISIBLE){
				getDecimal2Img().setVisibility(View.GONE);
			}
		}
		
		if(decimal1Show){
			if(getDecimalPointImg().getVisibility() != View.VISIBLE){
				getDecimalPointImg().setVisibility(View.VISIBLE);
			}			
			if(getDecimal1Img().getVisibility() != View.VISIBLE){
				getDecimal1Img().setVisibility(View.VISIBLE);
			}	
			if(tmpNumOfDecimal1!=curNumOfDecimal1){
				curNumOfDecimal1 = tmpNumOfDecimal1;
				getDecimal1Img().setImageResource(getNumImgRes(tmpNumOfDecimal1));
			}	
		}else{
			if(getDecimalPointImg().getVisibility() == View.VISIBLE){
				getDecimalPointImg().setVisibility(View.GONE);
			}
			if(getDecimal1Img().getVisibility() == View.VISIBLE){
				getDecimal1Img().setVisibility(View.GONE);
			}
		}
	}
	
	/**
	 * 获取数字对应的图片资源
	 * @param num
	 * @return
	 */
	private int getNumImgRes(int num){
		if(num >= Constants.num.length){
			return Constants.num[Constants.num.length-1];
		}else if(num <0){
			return Constants.num[0];
		}else{
			return Constants.num[num];
		}
	}
	
	private int lastResIdOfFlowUnit = 0;
	private void setFlowUnitImg(int resId){
		if(resId == lastResIdOfFlowUnit){
			return ;
		}
		getFlowUnitImg().setImageResource(resId);
		lastResIdOfFlowUnit = resId;
	}
	
	private ImageView getKiloImg(){
		if(kiloImg == null){
			kiloImg = (ImageView)((Activity)getContext()).findViewById(R.id.kiloImg);
		}
		return kiloImg;
	}
	
	private ImageView getHundredImg(){
		if(hundredImg == null){
			hundredImg = (ImageView)((Activity)getContext()).findViewById(R.id.hundredImg);
		}
		return hundredImg;
	}
	
	private ImageView getTenImg(){
		if(tenImg == null){
			tenImg = (ImageView)((Activity)getContext()).findViewById(R.id.tenImg);
		}
		return tenImg;
	}
	
	private ImageView getBitImg(){
		if(bitImg == null){
			bitImg = (ImageView)((Activity)getContext()).findViewById(R.id.bitImg);
		}
		return bitImg;
	}
	
	private ImageView getDecimalPointImg(){
		if(decimalPointImg == null){
			decimalPointImg = (ImageView)((Activity)getContext()).findViewById(R.id.decimalPointImg);
		}
		return decimalPointImg;
	}
	
	private ImageView getDecimal1Img(){
		if(decimal1Img == null){
			decimal1Img = (ImageView)((Activity)getContext()).findViewById(R.id.decimal1Img);
		}
		return decimal1Img;
	}
	
	private ImageView getDecimal2Img(){
		if(decimal2Img == null){
			decimal2Img = (ImageView)((Activity)getContext()).findViewById(R.id.decimal2Img);
		}
		return decimal2Img;
	}
	
	private ImageView getFlowUnitImg(){
		if(flowUnitImg == null){
			flowUnitImg = (ImageView)((Activity)getContext()).findViewById(R.id.flowUnitImg);
		}
		return flowUnitImg;
	}
	
	private LinearLayout getFlowNumLayout(){
		if(flowNumLayout == null){
			flowNumLayout = (LinearLayout)((Activity)getContext()).findViewById(R.id.flowNumLayout);
		}
		return flowNumLayout;
	}
		
	private void changeProgress(int progress){
		 if(progress < 0 || progress > 100 || lastProgress == progress){
			 return;
		 }
		 this.lastProgress = progress;
		 float tmpTopMargin = getMyHeight()-1.0f*getMyHeight()*progress/100;
		 frontWaveTopMargin =tmpTopMargin-3*scale;
		 backWaveTopMargin=(int)(frontWaveTopMargin-1.5*scale);
		 postInvalidate();
		 if(mProcessChangeCallBack != null){
			 mProcessChangeCallBack.callBack(curProgress, progress,isWarningProgress);
		 }
	}
	
	public void updateOfExcessEarlyWarning(){
		isWarningProgress = FlowUtils.isWarningProgress(getContext());
		if(mProcessChangeCallBack != null){
			 mProcessChangeCallBack.callBack(curProgress, curProgress,isWarningProgress);
		 }
	}
	
	private int getProgressForAnim(float interpolatedTime){
//		return (int)(50+(curProgress-50)*interpolatedTime);	//起始点为中线
		return (int)(curProgress*interpolatedTime);	//起始点为底部
	}
	
	 public void setProcessChangeCallBack(ProcessChangeCallBack mProcessChangeCallBack){
		 this.mProcessChangeCallBack = mProcessChangeCallBack;
	 }
	 
	 public interface ProcessChangeCallBack {
		public void callBack(int progress, int tmpProgress,boolean isWarningProgress);			
	 }
}

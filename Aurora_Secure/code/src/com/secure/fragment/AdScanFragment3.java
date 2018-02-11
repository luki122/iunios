package com.secure.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.AbsoluteSizeSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.aurora.secure.R;
import com.secure.activity.AdAppListActivity;
import com.secure.activity.CustomApplication;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.MyArrayList;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.model.AdScanModel;
import com.secure.model.AdScanSoundModel;
import com.secure.model.ConfigModel;
import com.secure.provider.AppAdInfoProvider;
import com.secure.utils.ApkUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.Utils;

public class AdScanFragment3 extends Fragment {  
	public static final int UIHandler_MSG_TYPE_OF_UPDATE = 1;
	public static final int UIHandler_MSG_TYPE_OF_STOP = 2;
	public static final int UIHandler_MSG_TYPE_OF_COMPLETE = 3;
    private final  int MAX_ALPHA = 255;
    private final  float MIN_PERCENT = 0.5f;    
    private final int LINE_SPACE_NUM = 2;//整个区域在垂直方向上被分成几个区域    
    private int MAX_HIGHT = 0,MAX_WIDTH = 0;   
    private MyArrayList<LineHolderData> lineHolderList = new MyArrayList<LineHolderData>(); 
    private LinearLayout appAnimHolder;
    private Point appAnimHolderCenter;
    private int appAnimHolderCircleR;
	
    private View holdView;
    private ImageView scanAnimView; 
    private TextView scanAppName;
    private TextView scanAppNum;
    private TextView adAppNum;
    private UIHandler mUIhandler;
    private Context mApplicationContext;
    private MyArrayList<AppInfo> needScanAppList = new MyArrayList<AppInfo>();
    
    private final int ADD_APP_ICON_WAIT_TIME = 150;
    private boolean isStopAppIconAnim;
    private int curAppIndex;
    private int curLineIndex;
    private AdScanSoundModel adScanSoundModel;
    private int lastAdAppNum;
    private final float appHolderSize;
    private final float appIconSize;
    
    public AdScanFragment3( ){
    	mUIhandler = new UIHandler(Looper.getMainLooper());
    	mApplicationContext = CustomApplication.getApplication();
    	adScanSoundModel = new AdScanSoundModel(mApplicationContext);
    	appHolderSize = mApplicationContext.getResources().getDimension(R.dimen.ad_scan_app_anim_holder_size);
    	appIconSize = mApplicationContext.getResources().getDimension(R.dimen.ad_scan_app_icon_size);
    		
    	int eachHeight = (int)(appHolderSize)/LINE_SPACE_NUM;
    	MAX_HIGHT = MAX_WIDTH = (int)(appIconSize);
    	
    	appAnimHolderCenter = new Point( (int)(appHolderSize/2),
    			(int)(appHolderSize/2));
    	appAnimHolderCircleR = (int)(appHolderSize/2);

    	lineHolderList.clear();
    	for(int i=0;i<LINE_SPACE_NUM;i++){ 		
    		SpaceData spaceData = new SpaceData(0,(int)(eachHeight-appIconSize));  
    		Point leftTop = new Point(0,eachHeight*i);
    		LineHolderData lineHolderData = new LineHolderData(mApplicationContext,
    				spaceData,
    				eachHeight,
    				leftTop);
    		lineHolderList.add(lineHolderData);
    	}
    }
	    
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		holdView = inflater.inflate(R.layout.ad_scan_fragment3, null);
		scanAnimView = (ImageView)holdView.findViewById(R.id.scanAnimView);
		scanAppName = (TextView)holdView.findViewById(R.id.scanAppName);
		scanAppNum = (TextView)holdView.findViewById(R.id.scanAppNum);
		adAppNum = (TextView)holdView.findViewById(R.id.adAppNum);
		setAdAppNumText("0");
		appAnimHolder = (LinearLayout)holdView.findViewById(R.id.appAnimHolder);
		
		startScanAdApp(mApplicationContext);	
		return holdView ;	
	}
	
	public void startScanAdApp(final Context context){
		if(context == null){
			return ;
		}		
		needScanAppList.clear();
        ConfigModel configModel = ConfigModel.getInstance();
        if(configModel != null){
            AppsInfo userAppsInfo = configModel.getAppInfoModel().getThirdPartyAppsInfo();
            int size = userAppsInfo==null?0:userAppsInfo.size();
            for(int i=0;i<size;i++){
            	AppInfo appInfo = (AppInfo)userAppsInfo.get(i);
            	if(appInfo == null){
            		continue ;
            	}
            	needScanAppList.add(appInfo);
            }
        }
        updateView(); 
        lastAdAppNum = 0;
        AdScanModel.getInstance(context).scanForManualUpdate(mUIhandler,needScanAppList); 
        startAppIconAnim();
	}
	
    private void updateView(){ 
    	appAnimHolder.removeAllViews();
    	for(int i=0;i<lineHolderList.size();i++){
    		LineHolderData lineHolderData = lineHolderList.get(i);
    		if(lineHolderData != null){
    			lineHolderData.updateView();
    		}
    	}
   }
    
	private void startAppIconAnim(){
		isStopAppIconAnim = false;
		curAppIndex = 0;
		curLineIndex = 0;	
		startEachLineAnim();	
	}
	
	private void startEachLineAnim(){
		holdView.postDelayed(new Runnable() {			
			@Override
			public void run() {
				if(lineHolderList.size()<=curLineIndex){
					return ;
				}
				LineHolderData lineHolderData = lineHolderList.get(curLineIndex);
				if(lineHolderData != null){
					lineHolderData.appIconAnimFunc();
				}
				curLineIndex++;
				startEachLineAnim();
			}
		}, 380); 
	}
    
	@Override
	public void onStart() {
		showScanCircleAnim(mApplicationContext);       
		super.onStart();
	}
			
	@Override
	public void onResume() {
		adScanSoundModel.onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		adScanSoundModel.onPause();
		super.onPause();
	}

	private void showScanCircleAnim(final Context context){
		scanAnimView.postDelayed(new Runnable() {			
			@Override
			public void run() {
				Animation rotate_rigth = null;
				try{
					rotate_rigth = AnimationUtils.loadAnimation(context,R.anim.btrote_right);
				}catch(Exception e){
					e.printStackTrace();
				}
				if(rotate_rigth != null){
			        LinearInterpolator lir = new LinearInterpolator(); 
			        rotate_rigth.setInterpolator(lir); 
			        scanAnimView.startAnimation(rotate_rigth);
				}
			}
		}, 100); 
	}
	
	final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
         }

 		@Override
 	    public void handleMessage(Message msg) { 
 			if(getActivity() == null||
 					AdScanFragment3.this == null||
 					getActivity().isFinishing()||
 					getActivity().isDestroyed() ||
 					AdScanFragment3.this.isRemoving() ||
 					AdScanFragment3.this.isDetached() ){
 				return ;
 			}
 			switch(msg.what){
 			case UIHandler_MSG_TYPE_OF_UPDATE:
 	 			AppInfo appInfo = (AppInfo)msg.obj;
 	 			if(appInfo != null){
 	 	 			int index =needScanAppList.indexOf(appInfo);	 		
 	 	 			String appName = ApkUtils.getApkName(getActivity(),appInfo.getApplicationInfo());
 	 	 			scanAppName.setText(String.format(getString(R.string.scan_apk_name),appName));
 	 	 		    scanAppNum.setText((index+1)+"/"+needScanAppList.size());
 	 	 		    int curAdAppNum = AdScanModel.getInstance(mApplicationContext).getAdApkNum();
 	 	 		    if(curAdAppNum > lastAdAppNum){
 	 	 		    	lastAdAppNum = curAdAppNum;
 	 	 		    	try{
 	 	 		    		adScanSoundModel.playFindAdSound();
 	 	 		    	}catch (Exception e) {
 	 	 		    		e.printStackTrace();
						} 	 		    	
 	 	 		    }
 	 	 		    setAdAppNumText(""+curAdAppNum);
 	 			}	
 				break;
 			case UIHandler_MSG_TYPE_OF_STOP:
 				isStopAppIconAnim = true;
 				if(MySharedPref.getIsCompleteScanAdApp(mApplicationContext)){
 					getActivity().finish();
 				}else{
 					Intent intent = new Intent(getActivity(),AdAppListActivity.class);
	 	 			getActivity().startActivity(intent);
	 	 			getActivity().overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,
	 	 					com.aurora.R.anim.aurora_activity_open_exit);
 				}
 				break;
 			case UIHandler_MSG_TYPE_OF_COMPLETE:
	 				Intent intent = new Intent(getActivity(),AdAppListActivity.class);
	 				getActivity().startActivity(intent); 
	 				getActivity().overridePendingTransition(com.aurora.R.anim.aurora_activity_open_enter,
	 	 					com.aurora.R.anim.aurora_activity_open_exit);
 				break;
 			}				
 	    }
 	}
	
	private void setAdAppNumText(String adNum){
		String textStr = String.format(getString(R.string.have_ad_app_num),adNum);
		SpannableString msp = new SpannableString(textStr);
		int index = textStr.indexOf(adNum);
		if(index>-1){
		    msp.setSpan(new AbsoluteSizeSpan(34,true),
		    		index, 
		    		index+adNum.length(), 
		    		Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  
	        //第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素，同上。 
		}
	    adAppNum.setText(msp); 
	}
	
	private void loadImgForView(Context context,View view,String pkgName){
    	ImageView imageView = (ImageView)view.findViewById(R.id.imgView);   
    	imageView.setTag(pkgName);
    	Drawable cachedImage = ImageLoader.getInstance(context).displayImage(
				imageView,
				pkgName, 
				pkgName,
			new ImageCallback() {
				public void imageLoaded(Drawable imageDrawable, Object viewTag) {
					if(imageDrawable == null || 
									viewTag == null){
						return ;
					}	
					ImageView imageViewByTag = (ImageView)appAnimHolder.findViewWithTag(viewTag);
					if (imageViewByTag != null) {
						imageViewByTag.setImageDrawable(imageDrawable);
					}					
				}
		});
		if(cachedImage != null) {
			imageView.setImageDrawable(cachedImage);
		}else{
			imageView.setImageResource(R.drawable.def_app_icon);
		}
	}
    
    public void releaseObject(){
    	if(needScanAppList != null){
    		needScanAppList.clear();
    	} 
    	for(int i=0;i<lineHolderList.size();i++){
    		LineHolderData lineHolderData = lineHolderList.get(i);
    		if(lineHolderData != null){
    			lineHolderData.releaseObject();
    		}
    	}
    	lineHolderList.clear();
    	mApplicationContext = null;
	}
    
    final class SpaceData{
    	public SpaceData(int yMin,int yMax){
    		this.yMin = yMin;
    		this.yMax = yMax;
    		canUse = true;
    	}
    	private int yMin;
    	private int yMax;
    	boolean canUse;
    	
    	public int getYMin(){
    		return yMin;
    	}
    	
    	public int getYMax(){
    		return yMax;
    	}
    }
	
    final class LineHolderData{
        final int SHOW_VIEW_NUM = 1;
        final int TOTAL_VIEW_NUM = SHOW_VIEW_NUM*2;       
        MyArrayList<View> allViewList = new MyArrayList<View>();
        MyArrayList<View> noUseViewList = new MyArrayList<View>();   
        MyArrayList<View> useViewList = new MyArrayList<View>();  
        FrameLayout holderView;
        LinearLayout.LayoutParams lineHolderParams;
        SpaceData spaceData;
        private Animation appInAnim,appOutAnim;
        private Handler dealHandler;
        private Point leftTopPointToAppAnimHolder;
        
        public LineHolderData(Context activity,SpaceData spaceData,int height,
        		Point leftTopPointToAppAnimHolder){
            for(int i=0;i<TOTAL_VIEW_NUM;i++){
        		View view = LayoutInflater.from(activity).inflate(R.layout.pager_item3, null, false);
        		allViewList.add(view);
        		noUseViewList.add(view);
        	}
            this.spaceData = spaceData;
            this.holderView = new FrameLayout(activity);            
            lineHolderParams = new LinearLayout.
             		LayoutParams(LayoutParams.FILL_PARENT,height);
            this.leftTopPointToAppAnimHolder = leftTopPointToAppAnimHolder;
            
            appInAnim = AnimationUtils.loadAnimation(activity,R.anim.ani_ad_scan_app_in3);
            appInAnim.setAnimationListener(new AnimationListener(){
    			@Override
    			public void onAnimationEnd(Animation animation) {
    				appIconAnimFunc();
    			}
    			@Override
    			public void onAnimationRepeat(Animation animation) { }
    			@Override
    			public void onAnimationStart(Animation animation) { }		
    		});	
            
            appOutAnim = AnimationUtils.loadAnimation(activity,R.anim.ani_ad_scan_app_out3);
            appOutAnim.setAnimationListener(new AnimationListener(){
    			@Override
    			public void onAnimationEnd(Animation animation) {
					if(useViewList.size() > 1){
    					View needRemoveView = useViewList.get(useViewList.size()-1);
    					if(needRemoveView != null){
    						useViewList.remove(needRemoveView);
        					holderView.removeView(needRemoveView);
        					noUseViewList.add(needRemoveView);	
    					} 									
    				}  				
    			}
    			@Override
    			public void onAnimationRepeat(Animation animation) { }
    			@Override
    			public void onAnimationStart(Animation animation) { }		
    		}); 
            dealHandler = new Handler();
        }
        
    	void appIconAnimFunc(){
    		dealHandler.postDelayed(new Runnable() {			
    			@Override
    			public void run() {
    				if(isStopAppIconAnim){
    					return ;
    				}
    				if(needScanAppList.size()<=curAppIndex){
    					curAppIndex = 0;
    				}
    				AppInfo appInfo = needScanAppList.get(curAppIndex);
    				if(appInfo != null){
    					showNewApp(appInfo.getPackageName());
    				}
    				curAppIndex++;
    			}
    		}, ADD_APP_ICON_WAIT_TIME); 
    	}
        
    	void showNewApp(String pkgName){
    		if(noUseViewList.size() == 0){
    			return ;
    		}
    		if(useViewList.size() >= SHOW_VIEW_NUM){
    			View needRemoveView = useViewList.get(useViewList.size()-1);
                if(needRemoveView != null){
                	needRemoveView.clearAnimation();
        			needRemoveView.startAnimation(appOutAnim);
                }  		
    		}
    				
    		View noUseView = noUseViewList.get(noUseViewList.size()-1);
    		if(noUseView != null){
        		noUseViewList.remove(noUseView);
        		loadImgForView(mApplicationContext,noUseView,pkgName);

        		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
        				(int)(appIconSize),
        				(int)(appIconSize));
        		
        		while(true){
        			layoutParams.leftMargin = (int)Math.round(Math.random()*
        					(appHolderSize-appIconSize-0)+0);
            		layoutParams.topMargin = (int)Math.round(Math.random()*
            				(spaceData.yMax-spaceData.yMin)+spaceData.yMin);
            		if(isInAppAnimHolderCircle(layoutParams.leftMargin,layoutParams.topMargin)){
            			break;
            		}
        		}
        		      		
    			holderView.addView(noUseView, 0, layoutParams);
        		useViewList.add(0,noUseView);

        		noUseView.clearAnimation();
        		noUseView.startAnimation(appInAnim);
        		//随机调整透明度
        		noUseView.setAlpha(Math.round(Math.random()*(1-0.5)+0.5));
    		}
    	}   
    	
        void updateView(){ 
        	appAnimHolder.addView(holderView,lineHolderParams);
        	noUseViewList.clear();
        	useViewList.clear();
    	    for(int i=0;i<allViewList.size();i++){
    	    	View childView = allViewList.get(i);
    	    	if(childView == null){
    	    		continue ;
    	    	}
    			ViewGroup parentView =(ViewGroup)childView.getParent();
    			if(parentView != null){
    				parentView.removeView(childView);
    			}
    			noUseViewList.add(childView);
    		}
        }
        
        /**
         * 判断当前图标是不是在AppAnimHolder的圆圈内
         * @param left
         * @param top
         * @return
         */
        boolean isInAppAnimHolderCircle(int left,int top){
        	Point checkPoint = new Point(left+leftTopPointToAppAnimHolder.x,
        			top+leftTopPointToAppAnimHolder.y);    	
        	if(Math.pow(appAnimHolderCenter.x-checkPoint.x,2)+
        			Math.pow(appAnimHolderCenter.y-checkPoint.y,2)>=
        			Math.pow(appAnimHolderCircleR,2) ||
        	   Math.pow(appAnimHolderCenter.x-(checkPoint.x+MAX_WIDTH),2)+
        			Math.pow(appAnimHolderCenter.y-checkPoint.y,2)>=
        			Math.pow(appAnimHolderCircleR,2) ||
        	   Math.pow(appAnimHolderCenter.x-checkPoint.x,2)+
        			Math.pow(appAnimHolderCenter.y-(checkPoint.y+MAX_HIGHT),2)>=
        			Math.pow(appAnimHolderCircleR,2) ||
        	   Math.pow(appAnimHolderCenter.x-(checkPoint.x+MAX_WIDTH),2)+
        			Math.pow(appAnimHolderCenter.y-(checkPoint.y+MAX_HIGHT),2)>=
        			Math.pow(appAnimHolderCircleR,2)	){
        		return false;
        	}else{
        		return true;
        	}       	
        }
        
        void releaseObject(){
        	allViewList.clear();
        	noUseViewList.clear();
        	useViewList.clear();
        	adScanSoundModel.realseObject();
        }     
    }    
}

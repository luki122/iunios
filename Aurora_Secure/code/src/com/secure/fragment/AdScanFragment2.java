package com.secure.fragment;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.MyArrayList;
import com.secure.imageloader.ImageCallback;
import com.secure.imageloader.ImageLoader;
import com.secure.model.AdScanModel;
import com.secure.model.ConfigModel;
import com.secure.provider.AppAdInfoProvider;
import com.secure.utils.ApkUtils;
import com.secure.utils.MySharedPref;
import com.secure.utils.Utils;

public class AdScanFragment2 extends Fragment {  
	public static final int UIHandler_MSG_TYPE_OF_UPDATE = 1;
	public static final int UIHandler_MSG_TYPE_OF_STOP = 2;
	public static final int UIHandler_MSG_TYPE_OF_COMPLETE = 3;
    private final  int MAX_ALPHA = 255;
    private final  float MIN_PERCENT = 0.5f;
    private int MAX_HIGHT = 0,MAX_WIDTH = 0;	
    
    private final int TOTAL_VIEW_NUM = 2;
    private ArrayList<View> allViewList = new ArrayList<View>();
    private ArrayList<View> noUseViewList = new ArrayList<View>();   
    private FrameLayout appAnimHolder;
	
    private View holdView;
    private ImageView scanAnimView; 
    private TextView scanAppName;
    private TextView scanAppNum;
    private TextView adAppNum;
    private UIHandler mUIhandler;
    private Context mApplicationContext;
    private MyArrayList<AppInfo> needScanAppList = new MyArrayList<AppInfo>();
    private Animation appInAnim,appOutAnim;
    
    public AdScanFragment2(Activity activity){
    	mUIhandler = new UIHandler(Looper.getMainLooper());
    	mApplicationContext = activity.getApplicationContext();
    	
    	for(int i=0;i<TOTAL_VIEW_NUM;i++){
    		View view = LayoutInflater.from(activity).inflate(R.layout.pager_item, null, false);
    		allViewList.add(view);
    		noUseViewList.add(view);
    	}
    	
        DisplayMetrics metrics = Utils.getDisplayMetrics(activity);
        float scale = metrics.densityDpi/160.0f;
        MAX_HIGHT = MAX_WIDTH = (int)(65*scale);
        
        appInAnim = AnimationUtils.loadAnimation(activity,R.anim.ani_ad_scan_app_in);
        appInAnim.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) {
				
			}		
		});	
        
        appOutAnim = AnimationUtils.loadAnimation(activity,R.anim.ani_ad_scan_app_out);
        appOutAnim.setAnimationListener(new AnimationListener(){
			@Override
			public void onAnimationEnd(Animation animation) { }
			@Override
			public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationStart(Animation animation) { 
				if(appAnimHolder.getChildCount() > 1){
					View needRemoveView = appAnimHolder.getChildAt(appAnimHolder.getChildCount()-1);				
					appAnimHolder.removeViewAt(appAnimHolder.getChildCount()-1);
					noUseViewList.add(needRemoveView);					
				}
			}		
		});	
    }
	    
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		holdView = inflater.inflate(R.layout.ad_scan_fragment2, null);
		scanAnimView = (ImageView)holdView.findViewById(R.id.scanAnimView);
		scanAppName = (TextView)holdView.findViewById(R.id.scanAppName);
		scanAppNum = (TextView)holdView.findViewById(R.id.scanAppNum);
		adAppNum = (TextView)holdView.findViewById(R.id.adAppNum);
		adAppNum.setText("0");
		appAnimHolder = (FrameLayout)holdView.findViewById(R.id.appAnimHolder);
		
		startScanAdApp(mApplicationContext);	
		return holdView ;	
	}
	
	public void startScanAdApp(Context context){
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
        updatePagerView();    
        AdScanModel.getInstance(context).scanForManualUpdate(mUIhandler,needScanAppList);
	}
	
    private void updatePagerView(){ 
    	noUseViewList.clear();
	    for(int i=0;i<allViewList.size();i++){
			ViewGroup parentView =(ViewGroup)allViewList.get(i).getParent();
			if(parentView != null){
				parentView.removeView(allViewList.get(i));
			}
			noUseViewList.add(allViewList.get(i));
		}
   }
    
	@Override
	public void onStart() {
		showScanCircleAnim(mApplicationContext);       
		super.onStart();
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
 					AdScanFragment2.this == null||
 					getActivity().isFinishing()||
 					getActivity().isDestroyed() ||
 					AdScanFragment2.this.isRemoving() ||
 					AdScanFragment2.this.isDetached() ){
 				return ;
 			}
 			switch(msg.what){
 			case UIHandler_MSG_TYPE_OF_UPDATE:
 	 			AppInfo appInfo = (AppInfo)msg.obj;
 	 			if(appInfo != null){
 	 	 			int index =needScanAppList.indexOf(appInfo);
 	 				showNewApp(appInfo.getPackageName());
 	 				String appName = ApkUtils.getApkName(getActivity(),appInfo.getApplicationInfo());
 	 	 			scanAppName.setText(String.format(getString(R.string.scan_apk_name),appName));
 	 	 		    scanAppNum.setText((index+1)+"/"+needScanAppList.size());
 	 	 		    adAppNum.setText(""+AdScanModel.getInstance(mApplicationContext).getAdApkNum());	 	 			 	 			 				 	 		    
 	 			}	
 				break;
 			case UIHandler_MSG_TYPE_OF_STOP:
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
	
	private void showNewApp(String pkgName){
		if(noUseViewList.size() == 0){
			return ;
		}
		
		View noUseView = noUseViewList.get(noUseViewList.size()-1);
		noUseViewList.remove(noUseView);
		loadImgForView(mApplicationContext,noUseView,pkgName);
		appAnimHolder.addView(noUseView, 0);
		noUseView.clearAnimation();
		noUseView.startAnimation(appInAnim);
		if(appAnimHolder.getChildCount() > 1){
			View needRemoveView = appAnimHolder.getChildAt(appAnimHolder.getChildCount()-1);
			needRemoveView.clearAnimation();
			needRemoveView.startAnimation(appOutAnim);
		}	
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
    	allViewList.clear();
    	noUseViewList.clear(); 
        
    	mApplicationContext = null;
	}
}

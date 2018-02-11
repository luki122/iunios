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
import android.widget.Button;
import android.widget.EditText;
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

public class AdScanFragment extends Fragment {  
	public static final int UIHandler_MSG_TYPE_OF_UPDATE = 1;
	public static final int UIHandler_MSG_TYPE_OF_STOP = 2;
	public static final int UIHandler_MSG_TYPE_OF_COMPLETE = 3;
	
    private final int LAYOUT_PAGE_NUM = 3;//同时展示三个pager
    private final int  PAGE_LIMIT_NUM = LAYOUT_PAGE_NUM*2-1;   
    private final int TOTAL_VIEW_NUM = PAGE_LIMIT_NUM*2+1;  
    private final  int MAX_ALPHA = 255;
    private final  float MIN_PERCENT = 0.5f;
    private int MAX_HIGHT = 0,MAX_WIDTH = 0;	
	private View [] views = new View [TOTAL_VIEW_NUM];
    int curItemIndex = 0;
    private ViewPager viewPager;
    private MyPagerAdapter myPagerAdapter;
    private MyOnPageChangeListener myOnPageChangeListener;
	
    private View holdView;
    private ImageView scanAnimView; 
    private TextView scanAppName;
    private TextView scanAppNum;
    private TextView adAppNum;
    private UIHandler mUIhandler;
    private Context mApplicationContext;
    private MyArrayList<AppInfo> needScanAppList = new MyArrayList<AppInfo>();
    
    public AdScanFragment(Activity activity){
    	mUIhandler = new UIHandler(Looper.getMainLooper());
    	mApplicationContext = activity.getApplicationContext();

    	myPagerAdapter = new MyPagerAdapter();
        myOnPageChangeListener = new MyOnPageChangeListener();             
        for(int i=0;i<TOTAL_VIEW_NUM;i++){
        	views[i] = LayoutInflater.from(activity).inflate(R.layout.pager_item, null, false);
        }
        DisplayMetrics metrics = Utils.getDisplayMetrics(activity);
        float scale = metrics.densityDpi/160.0f;
        MAX_HIGHT = MAX_WIDTH = (int)(65*scale);
    }
    
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		holdView = inflater.inflate(R.layout.ad_scan_fragment, null);
		scanAnimView = (ImageView)holdView.findViewById(R.id.scanAnimView);
		scanAppName = (TextView)holdView.findViewById(R.id.scanAppName);
		scanAppNum = (TextView)holdView.findViewById(R.id.scanAppNum);
		adAppNum = (TextView)holdView.findViewById(R.id.adAppNum);
		adAppNum.setText("0");
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
   	    for(int i=0;i<TOTAL_VIEW_NUM;i++){
   		    ViewGroup parentView =(ViewGroup)views[i].getParent();
        	if(parentView != null){
        		parentView.removeView(views[i]);
        	}
        }
   	    viewPager = (ViewPager)holdView.findViewById(R.id.view_pager);
        viewPager.setAdapter(myPagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT_NUM);
        viewPager.setOnPageChangeListener(myOnPageChangeListener);        
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
 					AdScanFragment.this == null||
 					getActivity().isFinishing()||
 					getActivity().isDestroyed() ||
 					AdScanFragment.this.isRemoving() ||
 					AdScanFragment.this.isDetached() ){
 				return ;
 			}
 			switch(msg.what){
 			case UIHandler_MSG_TYPE_OF_UPDATE:
 	 			AppInfo appInfo = (AppInfo)msg.obj;
 	 			if(appInfo != null){
 	 	 			int index =needScanAppList.indexOf(appInfo);
 	 	 			if(0 <= index && index < needScanAppList.size()){
 	 	 				viewPager.setCurrentItem(index,true);
 	 	 			}
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
	  
    class MyPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return needScanAppList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
        	Context context = mApplicationContext;
        	if(context == null){
        		return null;
        	}
        	
        	View view = views[position%TOTAL_VIEW_NUM];
        	((ViewPager)container).removeView(view);
        	ImageView imageView = (ImageView)view.findViewById(R.id.imgView);   
        	if(position < needScanAppList.size()){
        		AppInfo appInfo = needScanAppList.get(position);
        		if(appInfo != null){
        			String pkgName = appInfo.getPackageName();
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
            					
            					ImageView imageViewByTag = (ImageView)viewPager.findViewWithTag(viewTag);
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
        	}else{
        		imageView.setImageResource(R.drawable.def_app_icon);
        	}
              	
            ((ViewPager)container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//            ((ViewPager)container).removeView((View)object);
        }
    }
    
    public class MyOnPageChangeListener implements OnPageChangeListener {
        @Override
        public void onPageSelected(int position) { }
        
		@Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if(positionOffset == 0){
				return ;
			}
        	float percent = (0.5f)*(2-positionOffset);      	 	
        	changeImageView(position,percent);     	
        	changeImageView(position+1,(1-percent*MIN_PERCENT));
        	changeImageView(position+2,MIN_PERCENT);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {}
    }
    
    @SuppressLint({ "NewApi", "NewApi" })
	private void changeImageView(int position,float percent){
    	if(position < needScanAppList.size()){
    		View view = views[position%TOTAL_VIEW_NUM];
    		view.setAlpha((int)(MAX_ALPHA*percent));
    		ImageView imageView =  (ImageView)view.findViewById(R.id.imgView);
    		//如果通过下面的方式设置透明度，会改变图片的透明度，导致其他使用该图片的地方受到影响
//    		imageView.setImageAlpha((int)(MAX_ALPHA*percent));
    		
    		LayoutParams layoutParams = imageView.getLayoutParams();
    		layoutParams.width = (int)(MAX_WIDTH*percent);
    		layoutParams.height =  (int)(MAX_HIGHT*percent);
    		imageView.setLayoutParams(layoutParams);
    	} 
    }
    
    public void releaseObject(){
    	if(needScanAppList != null){
    		needScanAppList.clear();
    	} 
    	mApplicationContext = null;
	}
}

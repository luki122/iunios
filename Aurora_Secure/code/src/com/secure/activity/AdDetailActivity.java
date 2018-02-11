package com.secure.activity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.adblock.data.AdProviderData;
import com.adblock.data.AppAdData;
import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.Constants;
import com.secure.data.MainActivityItemData;
import com.secure.data.MyArrayList;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.AdObserver;
import com.secure.interfaces.AdSubject;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AdScanModel;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.AutoStartModel;
import com.secure.model.ConfigModel;
import com.secure.model.DefSoftModel;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.totalCount.TotalCount;
import com.secure.utils.ActivityUtils;
import com.secure.utils.DisableChanger;
import com.secure.utils.StringUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.AppDetailInfoView;
import com.secure.view.AppSizeView;
import com.secure.view.InfoDialog;

import aurora.widget.AuroraSwitch;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class AdDetailActivity extends AuroraActivity implements 
                                           OnClickListener,
                                           AdObserver,
                                           PrivacyAppObserver{	  
	final float Holder_MARGIN_TOP_LEN = 4.83f;
	final float Holder_MARGIN_BOTTOM_LEN = 4.83f;
	final float HOLDEER_PADDING_LEFT_LEN = 12.16f;//itemHolder左边第一个子元素距离自己的距离，以160dpi屏幕为基准
	final float HOLDEER_PADDING_Right_LEN = 12.16f;//itemHolder右边最后个子元素距离自己的距离，以160dpi屏幕为基准
	
	final float TEXT_PADDING_LEN = 10;//自定义"textView字"距离父边界的距离，以160dpi屏幕为基准
	final float TEXT_MARGIN_LEFT_LEN = 4.83f;//自定义textView左边距离其他空间的距离，以160dpi屏幕为基准
	final float TEXT_MARGIN_RIGHT_LEN = 4.83f;//自定义textView右边距离其他空间的距离，以160dpi屏幕为基准
	
	private AppInfo curAppInfo = null;
	private AppAdData appAdData = null;
	private MyArrayList<String> permList = new MyArrayList<String>();
	private MyArrayList<String> permNameList = new MyArrayList<String>();
	private MyArrayList<View> permViewList = new MyArrayList<View>();
	private PackageManager packageManager = null;
	private boolean isHaveAdTypeOfNotify = false,isHaveAdTypeOfView = false;
	private DisplayMetrics metrics;
	private float scale;
	private Animation adFlagAniIn,adFlagAniOut;
	private Animation blockLayoutAniIn,blockLayoutAniOut;
	private Animation unBlockLayoutAniIn,unBlockLayoutAniOut;
	private Animation aniAdNoPermIn,aniAdNoPermOut;
	private ImageView adBlockedFlag;
	private View unBlockLayout;
	private View BlockLayout;
	private FrameLayout notifyTypeLayout,viewTypeLayout;
	private ImageView notifyAniView,viewAniView;
	private AnimationDrawable notifyBlockAni,notifyUnBlockAni;
	private AnimationDrawable viewBlockAni,viewUnBlockAni;
	private Handler waitHanler;
	private TextView noPermText;
	private LinearLayout adPermHolderLayout;
	private int endAniNumOfPermView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setAuroraContentView(R.layout.ad_detail_activity,
        		AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.ad_detail); 
        metrics = Utils.getDisplayMetrics(this);
        scale = metrics.densityDpi/160.0f;
        packageManager = getPackageManager();
        adBlockedFlag = (ImageView)findViewById(R.id.adBlockedFlag);
        
        unBlockLayout = findViewById(R.id.NoBlockLayout);
        BlockLayout = findViewById(R.id.BlockLayout);
        notifyTypeLayout = (FrameLayout)findViewById(R.id.notifyTypeLayout);
        viewTypeLayout = (FrameLayout)findViewById(R.id.viewTypeLayout);
        notifyAniView = (ImageView)findViewById(R.id.notifyTypeAniView);
        viewAniView = (ImageView)findViewById(R.id.viewTypeAniView);
        noPermText = (TextView)findViewById(R.id.noPermText);
    	adPermHolderLayout = (LinearLayout)findViewById(R.id.adPermHolderLayout);	
                
        adFlagAniIn = AnimationUtils.loadAnimation(this,R.anim.ani_ad_block_flag_in);
        adFlagAniIn.setAnimationListener(adFlagAniInListener);	  
        
        adFlagAniOut = AnimationUtils.loadAnimation(this,R.anim.ani_ad_block_flag_out);
        adFlagAniOut.setAnimationListener(adFlagAniOutListener);	
        
        blockLayoutAniIn = AnimationUtils.loadAnimation(this,R.anim.ani_block_layout_in);
        blockLayoutAniIn.setAnimationListener(blockLayoutAniInListener);	 
        
        blockLayoutAniOut = AnimationUtils.loadAnimation(this,R.anim.ani_block_layout_out);
        blockLayoutAniOut.setAnimationListener(blockLayoutAniOutListener);	
        
        unBlockLayoutAniIn = AnimationUtils.loadAnimation(this,R.anim.ani_unblock_layout_in);
        unBlockLayoutAniIn.setAnimationListener(unBlockLayoutAniInListener);	
        
        unBlockLayoutAniOut = AnimationUtils.loadAnimation(this,R.anim.ani_unblock_layout_out);
        unBlockLayoutAniOut.setAnimationListener(unBlockLayoutAniOutListener);	
        
        aniAdNoPermIn = AnimationUtils.loadAnimation(this,R.anim.ani_ad_no_perm_in);
        aniAdNoPermIn.setAnimationListener(aniAdNoPermInListener);	   
        
        aniAdNoPermOut = AnimationUtils.loadAnimation(this,R.anim.ani_ad_no_perm_out);
        aniAdNoPermOut.setAnimationListener(aniAdNoPermOutListener);
        
        waitHanler = new Handler();
        
        initData();           
    } 
    
    private AnimationListener adFlagAniInListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) { 
			adBlockedFlag.setVisibility(View.VISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) {
			adBlockedFlag.setVisibility(View.VISIBLE);
		}		
	};
	
	private AnimationListener adFlagAniOutListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) { 
			adBlockedFlag.setVisibility(View.INVISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) { }		
	};
	
	private AnimationListener blockLayoutAniInListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) { 
			BlockLayout.setVisibility(View.VISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) {
			BlockLayout.setVisibility(View.VISIBLE);
		}		
	};
		
    private AnimationListener blockLayoutAniOutListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) {
			BlockLayout.setVisibility(View.INVISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) { }		
	};
			
    private AnimationListener unBlockLayoutAniInListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) {
			unBlockLayout.setVisibility(View.VISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) {
			unBlockLayout.setVisibility(View.VISIBLE);
		}		
	};
				
    private AnimationListener unBlockLayoutAniOutListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) { 
			unBlockLayout.setVisibility(View.INVISIBLE);
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) { }		
	};
	


	private void initData(){
    	receiveData();
        if(curAppInfo == null || 
        		!curAppInfo.getIsInstalled() || 
        		appAdData == null){
        	finish();
        	return ;
        }
        initOrUpdateAdTypeView();  
    	findViewById(R.id.mBlockButton).setOnClickListener(this);
    	findViewById(R.id.uninstallBtn).setOnClickListener(this);
    	((AppDetailInfoView)findViewById(R.id.appDetailInfoLayout)).setCurAppInfo(curAppInfo);
        initOrUpdateAdPermList();
        AdScanModel.getInstance(this).attach(this); 
        AuroraPrivacyManageModel.getInstance(this).attach(this);
        updateViewWithBolckState(curAppInfo.getIsBlockAd(),false);
        showAllPermView_ex(curAppInfo.getIsBlockAd());
        if(curAppInfo.getIsBlockAd()){
        	noPermText.setVisibility(View.VISIBLE);
			adPermHolderLayout.setVisibility(View.GONE);
        }else{
         	noPermText.setVisibility(View.GONE);
			adPermHolderLayout.setVisibility(View.VISIBLE);
        }
    }
    
	private void receiveData() {
		if(getIntent() != null && getIntent().getData() != null){
			String packageName = getIntent().getData().getSchemeSpecificPart();
			curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(packageName);
			if(curAppInfo != null){
				appAdData = AdScanModel.getInstance(this).getAppAdData(curAppInfo.getPackageName());	
			}			
		}		
	} 
	
	private void initOrUpdateAdTypeView(){		 
		 MyArrayList<AdProviderData> adProviderList = appAdData.getAdProviderList();
		 int size = adProviderList == null?0:adProviderList.size();
		 for(int i=0;i<size;i++){
			 AdProviderData adProviderData = adProviderList.get(i);
			 if(adProviderData == null){
				 continue ;
			 }
			 if(adProviderData.getIsHaveNotifyAd()){
				 isHaveAdTypeOfNotify = true;
			 }
			 if(adProviderData.getIsHaveViewAd()){
				 isHaveAdTypeOfView = true;
			 }
		 }

         if(!isHaveAdTypeOfNotify){
        	 notifyTypeLayout.setVisibility(View.GONE);
         }else{
        	 notifyBlockAni = (AnimationDrawable)getResources().getDrawable(R.anim.notify_ad_block_ani);
        	 notifyUnBlockAni = (AnimationDrawable)getResources().getDrawable(R.anim.notify_ad_unblock_ani);
         }
         if(!isHaveAdTypeOfView){
        	 viewTypeLayout.setVisibility(View.GONE);
         }else{
        	 viewBlockAni = (AnimationDrawable)getResources().getDrawable(R.anim.view_ad_block_ani);
        	 viewUnBlockAni = (AnimationDrawable)getResources().getDrawable(R.anim.view_ad_unblock_ani);
         }
         if(!isHaveAdTypeOfNotify || !isHaveAdTypeOfView){
        	 findViewById(R.id.spaceView).setVisibility(View.GONE);
         }
	}
	
	private void updateViewWithBolckState(boolean isBlocked,boolean needAni){
		if(adBlockedFlag == null){
			return ;
		}
			
		if(isBlocked){
			if(isHaveAdTypeOfNotify){
				notifyBlockAni.stop();
				notifyBlockAni.selectDrawable(0);
				notifyAniView.setBackground(notifyBlockAni);
			}
			
			if(isHaveAdTypeOfView){		
				viewBlockAni.stop();
				viewBlockAni.selectDrawable(0);
				viewAniView.setBackground(viewBlockAni);
			}
			
			if(needAni){
				if(isHaveAdTypeOfNotify) notifyBlockAni.start();
				if(isHaveAdTypeOfView) viewBlockAni.start();
				waitHanler.postDelayed(new Runnable() {				
					@Override
					public void run() {
						//有可能在等待的时间里，拦截状态已经改变，所以需要重新判断一次
						if(curAppInfo != null && curAppInfo.getIsBlockAd()){
							unBlockLayout.clearAnimation();						
							BlockLayout.clearAnimation();
							if(isHaveAdTypeOfNotify && isHaveAdTypeOfView){
								unBlockLayout.startAnimation(unBlockLayoutAniOut);	
								BlockLayout.startAnimation(blockLayoutAniIn);
							}else{
								unBlockLayoutAniOutListener.onAnimationEnd(null);
								blockLayoutAniInListener.onAnimationEnd(null);
							}
							
							adBlockedFlag.clearAnimation();
							adBlockedFlag.startAnimation(adFlagAniIn);
						}							
					}
				}, 39*15);
			}else{
				adFlagAniInListener.onAnimationEnd(null);
				if(isHaveAdTypeOfNotify) 
					notifyBlockAni.selectDrawable(notifyBlockAni.getNumberOfFrames()-1);
				if(isHaveAdTypeOfView) 
				    viewBlockAni.selectDrawable(viewBlockAni.getNumberOfFrames()-1);
				unBlockLayoutAniOutListener.onAnimationEnd(null);
				blockLayoutAniInListener.onAnimationEnd(null);
			}			
		}else{
			if(isHaveAdTypeOfNotify){
				notifyUnBlockAni.stop();
				notifyUnBlockAni.selectDrawable(0);
				notifyAniView.setBackground(notifyUnBlockAni);
			}
			
			if(isHaveAdTypeOfView){		
				viewUnBlockAni.stop();
				viewUnBlockAni.selectDrawable(0);
				viewAniView.setBackground(viewUnBlockAni);
			}
			
        	if(needAni){
        		adBlockedFlag.clearAnimation();
            	adBlockedFlag.startAnimation(adFlagAniOut);
            	
            	unBlockLayout.clearAnimation();
            	BlockLayout.clearAnimation();
            	if(isHaveAdTypeOfNotify && isHaveAdTypeOfView){
            		unBlockLayout.startAnimation(unBlockLayoutAniIn);			
    				BlockLayout.startAnimation(blockLayoutAniOut);	 				 
    				waitHanler.postDelayed(new Runnable() {				
    					@Override
    					public void run() {
    						//有可能在等待的时间里，拦截状态已经改变，所以需要重新判断一次
    						if(curAppInfo != null && !curAppInfo.getIsBlockAd()){
    							if(isHaveAdTypeOfNotify) notifyUnBlockAni.start();
        						if(isHaveAdTypeOfView) viewUnBlockAni.start();
    						}   			          	
    					}
    				}, 450);
            	}else{
            		unBlockLayoutAniInListener.onAnimationEnd(null);
            		blockLayoutAniOutListener.onAnimationEnd(null);
            		if(isHaveAdTypeOfNotify) notifyUnBlockAni.start();
					if(isHaveAdTypeOfView) viewUnBlockAni.start();
            	}            	
        	}else{
        		adFlagAniOutListener.onAnimationEnd(null);
        		unBlockLayoutAniInListener.onAnimationEnd(null);
        		blockLayoutAniOutListener.onAnimationEnd(null);
        		if(isHaveAdTypeOfNotify) 
        			notifyUnBlockAni.selectDrawable(notifyUnBlockAni.getNumberOfFrames()-1);
				if(isHaveAdTypeOfView) 
				    viewUnBlockAni.selectDrawable(viewUnBlockAni.getNumberOfFrames()-1);
        	} 		
		}
		
		Button mBlockButton = (Button)findViewById(R.id.mBlockButton);
		if(isBlocked){
			mBlockButton.setText(R.string.cancel_block);
		}else{
			mBlockButton.setText(R.string.block_ad);
		}		
	}
	
	private void initOrUpdateAdPermList(){
		if(curAppInfo == null){
			return ;
		}
		permList.clear();
		permNameList.clear();
		
		if(appAdData != null){
			MyArrayList<AdProviderData> adProviderList = appAdData.getAdProviderList();
			int size = adProviderList == null?0:adProviderList.size();
			for(int i=0;i<size;i++){
				addPerm(adProviderList.get(i));
			}
		}
		
		for(int i=0;i<permList.size();i++){
			try{
				PermissionInfo permissionInfo=packageManager.
						getPermissionInfo(permList.get(i), 0);
				if(permissionInfo != null && 
						permissionInfo.protectionLevel != PermissionInfo.PROTECTION_NORMAL){
					permNameList.add(permissionInfo.loadLabel(packageManager).toString());
				}
			}catch(PackageManager.NameNotFoundException e){
				e.printStackTrace();
			} 			
		}
	}
	
	private void addPerm(AdProviderData adProviderData){
		if(adProviderData == null || adProviderData.getAdPermList() == null){
			return ;
		}
		for(int i=0;i<adProviderData.getAdPermList().size();i++){
			String perm = adProviderData.getAdPermList().get(i);
			if(perm == null || permList.contains(perm)){
				continue ;
			}
			permList.add(perm);			
		}
	}
	 
	@Override
	public void onClick(View v) {
		if(curAppInfo == null){
			return ;
		}
		switch(v.getId()){
		case R.id.mBlockButton:	
			if(curAppInfo.getIsBlockAd()^true)
			{
				new TotalCount(this, "28", 1).CountData();
			}
			else
			{
				new TotalCount(this, "27", 1).CountData();
			}
			AdScanModel.getInstance(this).setSwitch(curAppInfo, curAppInfo.getIsBlockAd()^true);
			break;
		case R.id.uninstallBtn:
			if(curAppInfo.getIsUserApp()){
				new TotalCount(this, "29", 1).CountData();
				uninstallFunc(); 		
	    	}
			break;
		}		
	}
	
	/**
	 * 卸载应用
	 */
	private void uninstallFunc(){
		Intent intent = new Intent();
		intent.setAction("android.intent.action.DELETE");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("package:"+curAppInfo.getPackageName()));
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		AdScanModel.getInstance(this).detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){		
		permList.clear();
		permNameList.clear();
		permViewList.clear();
	} 

	@Override
	public void updateOfInit(AdSubject subject) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfInStall(AdSubject subject, String pkgName) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfCoverInStall(AdSubject subject, String pkgName) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfUnInstall(AdSubject subject, String pkgName) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			finish();
		}
	}

	@Override
	public void updateOfExternalAppAvailable(AdSubject subject,
			List<String> pkgList) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfExternalAppUnAvailable(AdSubject subject,
			List<String> pkgList) {
		if(curAppInfo == null){
			return ;
		}
		int size = pkgList==null?0:pkgList.size();
		for(int i=0;i<pkgList.size();i++){
			String pkgName = pkgList.get(i);
			if(pkgName != null && 
					pkgName.equals(curAppInfo.getPackageName())){
				finish();
				break;
			}
		}		
	}

	@Override
	public void updateOfSwitchChange(AdSubject subject, String pkgName,
			boolean swtich) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			updateViewWithBolckState(swtich,true);
			runPermViewAnim(swtich);
		}		
	}

	@Override
	public void updateOfAdLibUpdate(AdSubject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfManualUpdate(AdSubject subject) {
		// TODO Auto-generated method stub
		
	}
		
	private void showAllPermView_ex(boolean isBlocked){		
		adPermHolderLayout.removeAllViews();
		permViewList.clear();
		LayoutInflater inflater = getLayoutInflater();
		int itemWidth = 0; //每行以存放textView的长度
		
		Paint paint = new TextView(this).getPaint();
		ArrayList <LineHolderData> lineHolderList = new ArrayList <LineHolderData>();
		LineHolderData curLineHolder = null;
		//初始化数据
		for(int i=0;i<permNameList.size();i++){
			String permName = permNameList.get(i);
			if(permName == null){
				continue ;
			}
			if(curLineHolder == null){
				curLineHolder = new LineHolderData();
				lineHolderList.add(curLineHolder);
			}
			PermItemData permItemData = new PermItemData();
			permItemData.permName = permName;
			permItemData.strLength = (int) Math.ceil(paint.measureText(permName));
			permItemData.textViewWidth = (int)(TEXT_PADDING_LEN*scale)*2 + permItemData.strLength;
			
			if(metrics.widthPixels-
            		HOLDEER_PADDING_LEFT_LEN*scale -
            		HOLDEER_PADDING_Right_LEN*scale > 
                    itemWidth + permItemData.textViewWidth + 
            		TEXT_MARGIN_LEFT_LEN*scale+TEXT_MARGIN_RIGHT_LEN*scale){
            	
            	itemWidth = (int)(itemWidth + permItemData.textViewWidth + 
	            		TEXT_MARGIN_LEFT_LEN*scale+TEXT_MARGIN_RIGHT_LEN*scale);         	       	
            	curLineHolder.holder.add(permItemData);	
            }else{
            	curLineHolder = new LineHolderData();
            	lineHolderList.add(curLineHolder);
    			
    			itemWidth = (int)(permItemData.textViewWidth + 
	            		TEXT_MARGIN_LEFT_LEN*scale+TEXT_MARGIN_RIGHT_LEN*scale);  
    			curLineHolder.holder.add(permItemData);
            }			
		}
		
		//计算每个textView的长度，每个textView平均增量长度
		for(int i=0;i<lineHolderList.size()-1;i++){
			calculatePermViewWidth(lineHolderList.get(i));
		}
		LineHolderData lineHolder = lineHolderList.get(lineHolderList.size()-1);
		if(lineHolder.holder.size() == 1){
			PermItemData permItemData = lineHolder.holder.get(0);
			if(permItemData.textViewWidth < metrics.widthPixels/3){
				permItemData.textViewWidth = metrics.widthPixels/3;
			}
		}else if(lineHolder.holder.size()>1){
			calculatePermViewWidth(lineHolder);
		}
		
		//开始布局
		LinearLayout.LayoutParams lineHolderParams = new LinearLayout.
         		LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		lineHolderParams.setMargins(0,(int)(Holder_MARGIN_TOP_LEN*scale),
			 0,(int)(Holder_MARGIN_BOTTOM_LEN*scale));
	    
	    LinearLayout.LayoutParams itemParams = new LinearLayout.
         		LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
	    itemParams.setMargins((int)(TEXT_MARGIN_LEFT_LEN*scale),0,
					(int)(TEXT_MARGIN_RIGHT_LEN*scale),0);
	
		for(int i=0;i<lineHolderList.size();i++){
			lineHolder = lineHolderList.get(i);		
			LinearLayout lineHolderLayout= (LinearLayout)inflater.inflate(R.layout.ad_perm_text_item,null);				 
			adPermHolderLayout.addView(lineHolderLayout,lineHolderParams);
			
			for(int j=0;j<lineHolder.holder.size();j++){
				final TextView textView = new TextView(this);
				textView.setBackgroundResource(R.drawable.ad_perm_bg_for_unblock);
				textView.setTextColor(Color.parseColor("#777777"));				
				textView.setText(lineHolder.holder.get(j).permName);		
				textView.setGravity(Gravity.CENTER);
				textView.setWidth(lineHolder.holder.get(j).textViewWidth);
				lineHolderLayout.addView(textView,itemParams);	
				
				permViewList.add(textView);
			}
		}	
	}
	
	private void runPermViewAnim(boolean isBlocked){
		initPermView(isBlocked);
		if(isBlocked){
			hidePermView();
		}else{
			noPermText.startAnimation(aniAdNoPermOut);
		}
	}
	
	private void initPermView(boolean isBlocked){
		if(isBlocked){
			noPermText.clearAnimation();
			noPermText.setVisibility(View.GONE);
			adPermHolderLayout.setVisibility(View.VISIBLE);
			for(int i=0;i<permViewList.size();i++){
	            final View view = permViewList.get(i);
	            if(view == null){
	            	continue ;
	            }
	            view.clearAnimation(); 
	            view.setVisibility(View.VISIBLE);
			}
		}else{			
			for(int i=0;i<permViewList.size();i++){
	            final View view = permViewList.get(i);
	            if(view == null){
	            	continue ;
	            }
	            view.clearAnimation(); 
			}
			adPermHolderLayout.setVisibility(View.GONE);			
			noPermText.clearAnimation();
			noPermText.setVisibility(View.VISIBLE);
		}
	}
	
	private void hidePermView(){
		Random random = new Random();
		endAniNumOfPermView = 0;
		for(int i=0;i<permViewList.size();i++){
            final View view = permViewList.get(i);
            if(view == null){
            	continue ;
            }
            view.clearAnimation();                              
            final Animation adPermAniOut = AnimationUtils.loadAnimation(this,R.anim.ani_ad_perm_out);        	
        	adPermAniOut.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation animation) { 
					endAniNumOfPermView++;
					if(endAniNumOfPermView == permViewList.size()){
						if(curAppInfo != null && curAppInfo.getIsBlockAd()){
							noPermText.setVisibility(View.VISIBLE);
							noPermText.startAnimation(aniAdNoPermIn);
							adPermHolderLayout.setVisibility(View.GONE);
						}				
					}
				}
				@Override
				public void onAnimationRepeat(Animation animation) { }
				@Override
				public void onAnimationStart(Animation animation) { }
			});
        	
        	view.postDelayed(new Runnable() {
				@Override
				public void run() {
					if(curAppInfo != null && curAppInfo.getIsBlockAd()){
						view.startAnimation(adPermAniOut);		
					}								
				}
			}, random.nextInt(300)); 
		}
	}
	
    private AnimationListener aniAdNoPermInListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) { 
			if(curAppInfo != null && curAppInfo.getIsBlockAd()){
				noPermText.setVisibility(View.VISIBLE);
			}			
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) {}		
	};
	
	private AnimationListener aniAdNoPermOutListener = new AnimationListener(){
		@Override
		public void onAnimationEnd(Animation animation) {
			if(curAppInfo != null && !curAppInfo.getIsBlockAd()){
				noPermText.setVisibility(View.GONE);
				adPermHolderLayout.setVisibility(View.VISIBLE);
				showPermView();
			}
		}
		@Override
		public void onAnimationRepeat(Animation animation) { }
		@Override
		public void onAnimationStart(Animation animation) {}		
	};
	
	private void showPermView(){		
		Random random = new Random();
		for(int i=0;i<permViewList.size();i++){
            final View view = permViewList.get(i);
            if(view == null){
            	continue ;
            }
            view.clearAnimation();
        	
        	final Animation adPermAniIn = AnimationUtils.loadAnimation(this,R.anim.ani_ad_perm_in);
        	adPermAniIn.setAnimationListener(new AnimationListener(){
				@Override
				public void onAnimationEnd(Animation animation) { }

				@Override
				public void onAnimationRepeat(Animation animation) { }

				@Override
				public void onAnimationStart(Animation animation) {
					if(curAppInfo != null && !curAppInfo.getIsBlockAd()){
						View tmpView = adPermHolderLayout.findViewWithTag(animation);
						if (tmpView != null) {
							tmpView.setVisibility(View.VISIBLE);
						}
					}					
				}
			});
        	
        	view.setVisibility(View.INVISIBLE);
        	view.setTag(adPermAniIn);
            view.postDelayed(new Runnable() {				
				@Override
				public void run() {	
					if(curAppInfo != null && !curAppInfo.getIsBlockAd()){
						view.startAnimation(adPermAniIn);
					}					
				}
			}, random.nextInt(300));   
		}
	}
		
	/**
	 * 计算每个权限texView的长度
	 * @param lineHolder
	 */
	private void calculatePermViewWidth(LineHolderData lineHolder){
		int childNum = lineHolder.holder.size();
		int spaceWidth = (int)(metrics.widthPixels - 
				HOLDEER_PADDING_LEFT_LEN*scale -HOLDEER_PADDING_Right_LEN*scale -
				(TEXT_MARGIN_LEFT_LEN+TEXT_MARGIN_RIGHT_LEN)*childNum)-lineHolder.getAllViewWidth();
		int eachItemAddWidth = spaceWidth/childNum;
		for(int j=0;j<lineHolder.holder.size();j++){
			lineHolder.holder.get(j).textViewWidth +=eachItemAddWidth;
		}	
	}
	
	class PermItemData{
		String permName;
		int strLength;
		int textViewWidth;
	}
	
	class LineHolderData {
		boolean isFull = false;
		ArrayList<PermItemData> holder = new ArrayList<PermItemData>();
		
		public int getAllViewWidth(){
			int width = 0;
			for(int i=0;i<holder.size();i++){
				width = width+holder.get(i).textViewWidth;
			}
			return width;
		}
	}

	@Override
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}			
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}			
	}
}

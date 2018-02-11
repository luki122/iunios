package com.netmanage.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraMenuBase;

import com.aurora.netmanage.R;
import com.netmanage.data.ConfigData;
import com.netmanage.interfaces.ConfigObserver;
import com.netmanage.interfaces.ConfigSubject;
import com.netmanage.interfaces.SimChangeObserver;
import com.netmanage.interfaces.SimChangeSubject;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.model.NetModel;
import com.netmanage.service.WatchDogService;
import com.netmanage.totalCount.CountInfo;
import com.netmanage.totalCount.TotalCount;
import com.netmanage.utils.ApkUtils;
import com.netmanage.utils.FlowUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;
import com.netmanage.view.EnterAnimationView;
import com.netmanage.view.ProgressView;

public class NetMainActivity extends AuroraActivity implements OnClickListener,
																ConfigObserver,
																SimChangeObserver{

	private final int REQUEST_CODE_OF_PACKAGE_SET = 1;
	private final int REQUEST_CODE_OF_FLOW_SET = 2;
	private String TAG ="NetMainActivity";
	private boolean isNeedStartAnim ;//是否需要播放动画
	EnterAnimationView enterAnimationView;
	RelativeLayout bottomLayout;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	setAuroraContentView(R.layout.net_main_activity,
        		AuroraActionBar.Type.Empty);
        getAuroraActionBar().setTitle(R.string.flow_count);
        getAuroraActionBar().addItem(AuroraActionBarItem.Type.More,R.id.add_menu);  
        getAuroraActionBar().setOnAuroraActionBarListener(new OnAuroraActionBarItemClickListener(){
			@Override
			public void onAuroraActionBarItemClicked(int arg0) {
				showAuroraMenu();
		}}) ;
        enterAnimationView = (EnterAnimationView)findViewById(R.id.enterAnimationView);
        enterAnimationView.initAnimState();
        bottomLayout = (RelativeLayout)findViewById(R.id.bottomLayout);
        bottomLayout.setAlpha(0);
        isNeedStartAnim = true;
        setAuroraSystemMenuCallBack(auroraMenuCallBack);  
        setAuroraMenuItems(R.menu.main_menu);      
        NetModel.getInstance(this).attach(netInfoHandler);
        SimChangeSubject.getInstance().attach(this);
        startService(new Intent(this,WatchDogService.class));
        ConfigSubject.getInstance().attach(this);
        CorrectFlowModel.getInstance(this).saveLastImsi();
        loadAppInfo(); 
    }	
      
    @Override
	protected void onRestart() {
    	restartUpdateView();
		super.onRestart();
	}

	/**
     * 加载程序必须的数据
     */
    private void loadAppInfo(){      	
 		NetModel instance = NetModel.getInstance();  	
     	if(instance != null && instance.isGetFlow()){
     		//用旧数据提前展示界面
     		netInfoHandler.sendEmptyMessage(0);
     	}
    }

    private final Handler netInfoHandler = new Handler() {
    	@Override
	    public void handleMessage(Message msg) {
    		if(isNeedStartAnim){
    			isNeedStartAnim = false;
    			startFirstAnim();
    		}else{
    			initOrUpdateProgressScrollView(false);
    		} 		
	    }
    };
    
    private void startFirstAnim(){
    	initOrUpdateProgressScrollView(true);
    	startAnimOfBottomView();
    }
    
    private void startAnimOfBottomView(){ 
    	bottomLayout.setAlpha(255);
    	initOrUpdateBottomView();
    	findViewById(R.id.modifyLayout).setOnClickListener(NetMainActivity.this);
    	findViewById(R.id.setLayout).setOnClickListener(NetMainActivity.this);
    	bottomLayout.startAnimation(AnimationUtils.loadAnimation(
    			NetMainActivity.this,
    			R.anim.push_bottom_in)); 
    }
    
    private void restartUpdateView(){
    	initOrUpdateProgressScrollView(false);
    	initOrUpdateBottomView();
    }
   
    private void initOrUpdateProgressScrollView(boolean isNeedAnim){
    	ConfigData configData = ConfigModel.getInstance(NetMainActivity.this).getConfigData(); 
    	
//    	// 当前没有插入sim卡时，永远显示为0
//    	long usedFlow = 0;
//    	String curImsi = Utils.getImsi(this);
//		if (!StringUtils.isEmpty(curImsi)) {
//			usedFlow = CorrectFlowModel.getInstance(this).getCorrectFlow()*1024+
//					NetModel.getInstance(NetMainActivity.this).getTotalMoblieFlowForStatistics()/1024;
//		}
    	long usedFlow = ApkUtils.getUsedFlow(this);		
    			  	
    	if(configData.isSetedFlowPackage()){		   		 	
    		long totalFlow = configData.getMonthlyFlow()*1024;
    		long remainderFlow = totalFlow -usedFlow;
    		if(isNeedAnim){
    			enterAnimationView.startAnim(totalFlow,remainderFlow);
    		}else{
    			ProgressView progressView = (ProgressView)findViewById(R.id.progressWaveImg);
        		progressView.setProgress(false,totalFlow,remainderFlow);	
    		}		
    	}else{	
    		if(isNeedAnim){
    			enterAnimationView.startAnim(0,usedFlow);
    		}else{
    			ProgressView progressView = (ProgressView)findViewById(R.id.progressWaveImg);
        		progressView.setProgress(false,0,usedFlow);	
    		}
    	}
    }
    
    private void initOrUpdateBottomView(){
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
    	if(configData.isSetedFlowPackage()){
    		findViewById(R.id.HaveFlowPackageLayout).setVisibility(View.VISIBLE);
    		findViewById(R.id.NoFlowPackageLayout).setVisibility(View.GONE);  
    		
    		//剩余天数
    		TextView remainDataView = (TextView)findViewById(R.id.remainData);
    		remainDataView.setText(
    				""+TimeUtils.getRemainderDaysToMonthEndDate(configData.getMonthEndDate())); 
    	}else{
    		findViewById(R.id.HaveFlowPackageLayout).setVisibility(View.GONE);
    		findViewById(R.id.NoFlowPackageLayout).setVisibility(View.VISIBLE);
    	}
    	refreshViewEnableBySimChange();
    }
    
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.setLayout:
			Intent intent = new Intent(NetMainActivity.this,PackageSetActivity.class);
			startActivityForResult(intent, REQUEST_CODE_OF_PACKAGE_SET);
			break;
		case R.id.modifyLayout:
			if (Utils.isIndiaRom()) {
				FlowUtils.manualCorrectionFlow(NetMainActivity.this);
			} else {
				startActivity(new Intent(NetMainActivity.this,FlowCorrectionActivity.class));
			}
			break;
		}		
	}

	@Override
	protected void onDestroy() {
		ConfigSubject.getInstance().detach(this);
		SimChangeSubject.getInstance().detach(this);
		NetModel.getInstance(this).detach(netInfoHandler);
		releaseObject();
		super.onDestroy();
	}
	
	@Override
	public void updateOfMonthlyFlowChange() {
		initOrUpdateProgressScrollView(false);
		ApkUtils.sendBroasdcastToLauncher(this);
	}
	
	@Override
	public void updateOfExcessEarlyWarning() {
		ProgressView progressView = (ProgressView)findViewById(R.id.progressWaveImg);
		progressView.updateOfExcessEarlyWarning();
		ApkUtils.sendBroasdcastToLauncher(this);
	}
	
	private AuroraMenuBase.OnAuroraMenuItemClickListener auroraMenuCallBack = 
			new AuroraMenuBase.OnAuroraMenuItemClickListener() {
		@Override
		public void auroraMenuItemClick(int itemId) {
			Intent intent;
			ComponentName componentName;
			switch (itemId) {			
			case R.id.btn_network_control:		
				componentName = new ComponentName("com.aurora.secure",
						"com.secure.activity.NetManageActivity");
				intent = new Intent();
				intent.setComponent(componentName);
				startActivity(intent);
				
				// 统计 联网管理
				new TotalCount(NetMainActivity.this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_1, 1).CountData();
				break;
			case R.id.btn_service_network:		
                componentName = new ComponentName("com.aurora.secure",
						"com.secure.activity.BackgroundNetManageActivity");
				intent = new Intent();
				intent.setComponent(componentName);
				startActivity(intent);
				
				// 统计 后台流量详情
				new TotalCount(NetMainActivity.this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_2, 1).CountData();
				break;
			case R.id.btn_set:
				intent = new Intent(NetMainActivity.this,FlowSetActivity.class);
				startActivityForResult(intent,REQUEST_CODE_OF_FLOW_SET);
				
				// 统计 设置流量套餐
				new TotalCount(NetMainActivity.this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_3, 1).CountData();
				break;
				}
			}
	 };
	 
	 /**
	  * 释放不需要用的对象所占用的堆内存
	  */
	private void releaseObject(){
			
	}

	@Override
	public void simChange(SimChangeSubject subject) {
		// TODO Auto-generated method stub
		refreshViewEnableBySimChange();
	}
	
	 private void refreshViewEnableBySimChange(){
		 LinearLayout modifyLayout = (LinearLayout)findViewById(R.id.modifyLayout);
		 TextView modifyLayoutText = (TextView)findViewById(R.id.modifyLayoutText);
		 ImageView haveFlowArrow = (ImageView) findViewById(R.id.haveFlowArrow);
	    	String imsi = Utils.getImsi(this);
	    	if(StringUtils.isEmpty(imsi)){
	    		modifyLayout.setEnabled(false);
	    		if(Utils.isMultiSimEnabled()){
	    			modifyLayoutText.setText(R.string.no_sim_for_MultiSim);
	    		}else{
	    			modifyLayoutText.setText(R.string.no_sim);
	    		}	    		
	    		haveFlowArrow.setVisibility(View.GONE);
	    	}else{
	    		modifyLayout.setEnabled(true);
	    		modifyLayoutText.setText(R.string.flow_error_manual_correction);
	    		haveFlowArrow.setVisibility(View.VISIBLE);
	    	}
	    }		

}

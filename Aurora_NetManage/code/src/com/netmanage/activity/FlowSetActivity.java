package com.netmanage.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;

import com.aurora.netmanage.R;
import com.netmanage.data.ConfigData;
import com.netmanage.interfaces.ConfigObserver;
import com.netmanage.interfaces.ConfigSubject;
import com.netmanage.interfaces.SimChangeObserver;
import com.netmanage.interfaces.SimChangeSubject;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.totalCount.CountInfo;
import com.netmanage.totalCount.TotalCount;
import com.netmanage.utils.FlowUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import com.netmanage.view.InfoDialog;

public class FlowSetActivity extends AuroraActivity implements OnClickListener,
                                                               OnCheckedChangeListener,
                                                               OnSeekBarChangeListener,
                                                               ConfigObserver,
                                                               SimChangeObserver{
//	private final int REQUEST_CODE_OF_PACKAGE_SET = 1;
//	private final int REQUEST_CODE_OF_Flow_Correction = 2;
	private TextView scaleTextView;	
	private TextView warningValueText;
	private ConfigData globalConfigData ;
	private SeekBar seekBar;
	private boolean oldExcessEarlyWarningSwitch;
	private int OldEarlyWarningPercent;
	
	private boolean isFirstLoad = true;		// 是否首次进入此界面（统计需求，防止首次checkbok改变被统计）
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.flow_set_activity);
        }else{
        	setAuroraContentView(R.layout.flow_set_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.flow_set);
        }    
        initView();  
        initData();
        isFirstLoad = false;
    }
    
    @Override
	protected void onResume() {
    	
    	refreshViewEnableBySimChange();
    	
    	oldExcessEarlyWarningSwitch = globalConfigData.getExcessEarlyWarningSwitch();
    	OldEarlyWarningPercent = globalConfigData.getEarlyWarningPercent();
		super.onResume();
	}
       
    @Override
	protected void onPause() {
    	if(oldExcessEarlyWarningSwitch != globalConfigData.getExcessEarlyWarningSwitch() ||
    			OldEarlyWarningPercent != globalConfigData.getEarlyWarningPercent()){
    		ConfigSubject.getInstance().notifyObserversOfExcessEarlyWarning();
    	} 	
		super.onPause();
	}

	private void initView(){
    	seekBar = (SeekBar)findViewById(R.id.seekBar);
    	scaleTextView = (TextView)findViewById(R.id.scaleTextView);	
    	warningValueText = (TextView)findViewById(R.id.warningValueText);	
    	seekBar.setOnSeekBarChangeListener(this);
    	
    	refreshViewEnableBySimChange();
    	
    	findViewById(R.id.packageSetBtn).setOnClickListener(this);
    	findViewById(R.id.flowCorrectionBtn).setOnClickListener(this);
    	findViewById(R.id.manualCorrectionBtn).setOnClickListener(this);
    	findViewById(R.id.statisticalDataClearBtn).setOnClickListener(this);
    	
    	AuroraSwitch excessEarlyWarningSwitch = (AuroraSwitch)findViewById(R.id.excessEarlyWarningSwitch);
    	AuroraSwitch backgroundTrafficTipsSwitch = (AuroraSwitch)findViewById(R.id.backgroundTrafficTipsSwitch);
    	AuroraSwitch dailyExcessTipsSwitch = (AuroraSwitch)findViewById(R.id.dailyExcessTipsSwitch);
    	
    	excessEarlyWarningSwitch.setOnCheckedChangeListener(this);
    	backgroundTrafficTipsSwitch.setOnCheckedChangeListener(this);
    	dailyExcessTipsSwitch.setOnCheckedChangeListener(this);
    	
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
    	excessEarlyWarningSwitch.setChecked(configData.getExcessEarlyWarningSwitch());
    	backgroundTrafficTipsSwitch.setChecked(configData.getBackgroundTrafficTipsSwitch());
    	dailyExcessTipsSwitch.setChecked(configData.getDailyExcessTipsSwitch());
    	
    	int progress = (configData.getEarlyWarningPercent()-50)*2;
    	if(0 <= progress && progress <= 100){
    		seekBar.setProgress(progress);
    	}
    	
    	updateView();
    }
        
    private void refreshViewEnableBySimChange() {
    	
    	if (Utils.isIndiaRom()) {
    		
    		findViewById(R.id.flowCorrectionBtn).setVisibility(View.GONE);
    		findViewById(R.id.manualCorrectionBtn).setVisibility(View.VISIBLE);
    		
    		RelativeLayout manualCorrectionBtn = (RelativeLayout)findViewById(R.id.manualCorrectionBtn);
        	TextView manualCorrectionText = (TextView)findViewById(R.id.manualCorrectionText);
        	ImageView manualCorrectionArrow = (ImageView)findViewById(R.id.manualCorrectionArrow);
    		String imsi = Utils.getImsi(this);
    		if(StringUtils.isEmpty(imsi)){
    			manualCorrectionBtn.setEnabled(false);
    			manualCorrectionText.setEnabled(false);
    			manualCorrectionArrow.setEnabled(false);
        	}else{
        		manualCorrectionBtn.setEnabled(true);
        		manualCorrectionText.setEnabled(true);
        		manualCorrectionArrow.setEnabled(true);
        	}
    		
    	} else {
    		
    		findViewById(R.id.flowCorrectionBtn).setVisibility(View.VISIBLE);
    		findViewById(R.id.manualCorrectionBtn).setVisibility(View.GONE);
    		
    		boolean hasChinaSIM = Utils.hasChinaSIMCard(this);
        	if(!hasChinaSIM) {
        		Log.d("vtraf","refreshViewEnableBySimChange, hasChinaSIM = false");
        		findViewById(R.id.flowCorrectionBtn).setVisibility(View.GONE);
        	}
        	else {
        		Log.d("vtraf","refreshViewEnableBySimChange, hasChinaSIM = true");
        		findViewById(R.id.flowCorrectionBtn).setVisibility(View.VISIBLE);
        	}
        	
        	RelativeLayout flowCorrectionBtn = (RelativeLayout)findViewById(R.id.flowCorrectionBtn);
        	TextView flowCorrectionText = (TextView)findViewById(R.id.flowCorrectionText);
        	ImageView flowCorrectionArrow = (ImageView)findViewById(R.id.flowCorrectionArrow);
        	String imsi = Utils.getImsi(this);
        	if(StringUtils.isEmpty(imsi)){
        		flowCorrectionBtn.setEnabled(false);
        		flowCorrectionText.setEnabled(false);
        		flowCorrectionArrow.setEnabled(false);
        	}else{
        		flowCorrectionBtn.setEnabled(true);
        		flowCorrectionText.setEnabled(true);
        		flowCorrectionArrow.setEnabled(true);
        	}
        	
    	}

    }
    
    /**
     * 有些view需要更新，则把需要更新的view放到下面函数中
     */
    private void updateView(){
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
    	if(configData.getMonthlyFlow()>1024){
    		long earlyWarningValue = configData.getMonthlyFlow()*configData.getEarlyWarningPercent()/100;
    		if(earlyWarningValue<=0){
    			updateViewOfEarlyWarning(false,null);
    		}else{
    			if(configData.getExcessEarlyWarningSwitch()){
    				updateViewOfEarlyWarning(true,""+earlyWarningValue);
    			}else{
    				updateViewOfEarlyWarning(false,""+earlyWarningValue);
    			}  			
    		}
    	}else{
    		double earlyWarningValue = 1.0*configData.getMonthlyFlow()*configData.getEarlyWarningPercent()/100;
    		if(earlyWarningValue<=0){
    			updateViewOfEarlyWarning(false,null);
    		}else{
    			if(configData.getExcessEarlyWarningSwitch()){
    				updateViewOfEarlyWarning(true,""+earlyWarningValue);
    			}else{
    				updateViewOfEarlyWarning(false,""+earlyWarningValue);
    			}  			
    		}
    	}
    }
    
    /**
     * 更新预警值view
     * @param enable
     * @param earlyWarningValue
     */
    private void updateViewOfEarlyWarning(boolean enable,String earlyWarningValue){
    	if(seekBar == null ||
    			scaleTextView == null ||
    			warningValueText == null){
    		return ;
    	}
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
    	
    	seekBar.setEnabled(enable);
		scaleTextView.setEnabled(enable);
		warningValueText.setEnabled(enable);
		
    	if(StringUtils.isEmpty(earlyWarningValue)){
			scaleTextView.setText(
					configData.getEarlyWarningPercent()+
					getResources().getString(R.string.percent));		
		}else{
			scaleTextView.setText(
					configData.getEarlyWarningPercent()+
					getResources().getString(R.string.percent)+
					getResources().getString(R.string.left_brackets)+
					earlyWarningValue+"M"+
					getResources().getString(R.string.right_brackets) );			
		}
    }
    
    private void initData(){ 
    	ConfigSubject.getInstance().attach(this);
    	SimChangeSubject.getInstance().attach(this);
    	globalConfigData = ConfigModel.getInstance(this).getConfigData();
    } 

	@Override
	public void onClick(View v) {
		Intent intent ;
		switch(v.getId()){
		case R.id.packageSetBtn:
			intent = new Intent(this,PackageSetActivity.class);
			startActivity(intent);
			break;
		case R.id.flowCorrectionBtn:
//			FlowUtils.manualCorrectionFlow(FlowSetActivity.this);
			intent = new Intent(this,FlowCorrectionActivity.class);
			startActivity(intent);
			break;
		case R.id.manualCorrectionBtn:
			FlowUtils.manualCorrectionFlow(FlowSetActivity.this);
			break;
		case R.id.statisticalDataClearBtn:
			InfoDialog.showDialog(this, 
					R.string.statistical_data_clear,
					android.R.attr.alertDialogIcon,
					R.string.are_you_statistical_data_clear,
					R.string.sure,
					new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {	                    	
	                    	CorrectFlowModel.getInstance(FlowSetActivity.this).
	                    	   CorrectFlow(TimeUtils.getCurrentTimeMillis(), 0);	   
	                    	
	                    	// 统计 清空成功时
	            			new TotalCount(FlowSetActivity.this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_9, 1).CountData();
	                    }
	                }, 
	                R.string.cancel, 
					null, 
					null);
			break;
		}		
	}
		
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ConfigData configData = ConfigModel.getInstance(this).getConfigData();
		switch(buttonView.getId()){
		case R.id.excessEarlyWarningSwitch:		
			configData.setExcessEarlyWarningSwitch(isChecked);
			updateView();
			
			// 统计 超额预警操作频率改变该设置项开关值时
			if (!isFirstLoad) {
				new TotalCount(this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_5, 1).CountData();
			}
			break;
		case R.id.backgroundTrafficTipsSwitch:
			configData.setBackgroundTrafficTipsSwitch(isChecked);
			
			// 统计 后台流量提示操作频率改变预警值时
			if (!isFirstLoad) {
				new TotalCount(this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_7, 1).CountData();
			}
			break;
		case R.id.dailyExcessTipsSwitch:
			configData.setDailyExcessTipsSwitch(isChecked);
			
			// 统计 每日超额提示操作频率改变预警值时
			if (!isFirstLoad) {
				new TotalCount(this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_8, 1).CountData();
			}
			break;
		}
		
	} 
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(scaleTextView == null || globalConfigData == null){
			return ;
		}
		progress = 50+50*progress/100;//这样进度条的范围为50-100
		
		globalConfigData.setEarlyWarningPercent(progress);
		
		if(globalConfigData.getMonthlyFlow()>1024){
    		long earlyWarningValue = globalConfigData.getMonthlyFlow()*progress/100;
    		if(earlyWarningValue<=0){
    			updateViewOfEarlyWarning(false,null);
    		}else{
    			updateViewOfEarlyWarning(true,""+earlyWarningValue);
    		}
    	}else{
    		double earlyWarningValue = 1.0*globalConfigData.getMonthlyFlow()*progress/100;
    		if(earlyWarningValue<=0){
    			updateViewOfEarlyWarning(false,null);
    		}else{
    			updateViewOfEarlyWarning(true,""+earlyWarningValue);
    		}
    	}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub	
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub	
		// 统计 改变预警值时
		new TotalCount(this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_6, 1).CountData();
	}	
	
	@Override
	public void updateOfMonthlyFlowChange() {
		updateView();		
	}
	
	@Override
	public void simChange(SimChangeSubject subject) {
		// TODO Auto-generated method stub
		refreshViewEnableBySimChange();
	}
	
	@Override
	public void updateOfExcessEarlyWarning() {
		// TODO Auto-generated method stub		
	}

	@Override
	protected void onDestroy() {
		ConfigModel.getInstance(this).saveConfigData();
		ConfigSubject.getInstance().detach(this);
		SimChangeSubject.getInstance().detach(this);
		releaseObject();
		super.onDestroy();
	}
    
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		
	}
}

package com.netmanage.activity;

import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;

import com.aurora.netmanage.R;
import com.netmanage.data.AutoCorrectInfo;
import com.netmanage.data.ConfigData;
import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.data.CorrectionConfig;
import com.netmanage.interfaces.AutoFlowCorrectCallBack;
import com.netmanage.interfaces.SimChangeObserver;
import com.netmanage.interfaces.SimChangeSubject;
import com.netmanage.interfaces.SimInfoSetCallBack;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowBySmsModel;
import com.netmanage.utils.AlarmUtils;
import com.netmanage.utils.FlowUtils;
import com.netmanage.utils.NetworkUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import com.netmanage.view.SimInfoSetDlg;
import com.netmanage.view.SimInfoSetDlg.SetSimInfoTypeEnum;

public class FlowCorrectionActivity extends AuroraActivity implements OnClickListener,
                                                               OnCheckedChangeListener,
                                                               SimInfoSetCallBack,
                                                               SimChangeObserver,
                                                               AutoFlowCorrectCallBack{
    private Spinner provinceSpinner;
	private Spinner packageTypeSpinner;
	private ArrayAdapter<String> provinceSpinnerAdapter;
	private ArrayAdapter<String> packageTypeSpinnerAdapter;
	private SimInfoSetDlg simInfoSetDlg;
	private CorrectFlowBySmsData correctFlowBySmsData;
    private TrafficCorrectionManager mTcMgr;
    private TextView provinceText;
    private TextView operatorsText;
    private TextView brandText;
    private AuroraSwitch flowCorrectionSwitch;
    private boolean isNeedRunAutoCorrectionFunc;
    private boolean isNeedRunOpenaAutoCorrectFunc;    
    private boolean isNeedRunSetCarryFunc;
    private boolean isNeedRunsetBrandFunc;
    private UIHandler mUIhandler;
    private Button autoCorrectionBtn;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.flow_correction_activity);
        }else{
        	setAuroraContentView(R.layout.flow_correction_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.flow_correction);
        } 
        SimChangeSubject.getInstance().attach(this);
        CorrectFlowBySmsModel.getInstance(this).attach(this);
        mTcMgr = CorrectFlowBySmsModel.getInstance(this).getTcMgr();
        correctFlowBySmsData = CorrectFlowBySmsData.getInstance(this);
        isNeedRunAutoCorrectionFunc = false;
        isNeedRunOpenaAutoCorrectFunc =false;
        isNeedRunSetCarryFunc = false;
        isNeedRunsetBrandFunc = false;
        mUIhandler = new UIHandler(Looper.getMainLooper());
        simInfoSetDlg = new SimInfoSetDlg(this,
        		mTcMgr,
        		correctFlowBySmsData,
        		this);
        initView();         
    }
    
    private void initView(){
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
       	
    	findViewById(R.id.provinceLayout).setOnClickListener(this);
    	findViewById(R.id.operatorsLayout).setOnClickListener(this);
    	findViewById(R.id.brandLayout).setOnClickListener(this); 	
    	findViewById(R.id.manualCorrectionBtn).setOnClickListener(this);
    	autoCorrectionBtn = (Button)findViewById(R.id.AutoCorrectionBtn);
    	autoCorrectionBtn.setOnClickListener(this);   	
    	provinceText = (TextView)findViewById(R.id.provinceText);
    	operatorsText = (TextView)findViewById(R.id.operatorsText);
    	brandText = (TextView)findViewById(R.id.brandText);  	
    	flowCorrectionSwitch = (AuroraSwitch)findViewById(R.id.flowCorrectionSwitch);
    	flowCorrectionSwitch.setOnCheckedChangeListener(this);
    	
    	updateView();
    }
    
    private void updateView(){
    	if(flowCorrectionSwitch == null || correctFlowBySmsData == null){
    		return ;
    	}
    	updateSimInfoView();
    	flowCorrectionSwitch.setChecked(correctFlowBySmsData.getIsAutoCorrect());   	
    	autoFlowCorrectCallBack(CorrectFlowBySmsModel.getInstance(this).getAutoCorrectInfo());
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.provinceLayout:
			simInfoSetDlg.startProvinceDlg();
			break;
		case R.id.operatorsLayout:
			setCarry();
			break;
		case R.id.brandLayout:
			setBrand();
			break;
		case R.id.AutoCorrectionBtn:
			isNeedRunAutoCorrectionFunc = true;
			autoCorrectionFunc();
			break;
		case R.id.manualCorrectionBtn:
			if(Utils.getImsi(this) == null){
				//没有可上网的sim卡
				Toast.makeText(this, R.string.no_sim_for_net,Toast.LENGTH_SHORT).show();
			}else{			
				FlowUtils.manualCorrectionFlow(FlowCorrectionActivity.this);
			}			
			break;
		}		
	}
	
	/**
	 * 设置运营商
	 */
	private void setCarry(){
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		if(province == null || city == null){
			isNeedRunSetCarryFunc = true;
			simInfoSetDlg.startProvinceDlg();
		}else{
			isNeedRunSetCarryFunc = false;
			simInfoSetDlg.startCarryDlg();
		}
	}
	
	/**
	 * 设置品牌
	 */
    private void setBrand(){
    	CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		if(province == null || city == null){
			isNeedRunsetBrandFunc = true;
			simInfoSetDlg.startProvinceDlg();
		}else if(carry == null){
			isNeedRunsetBrandFunc = true;
			simInfoSetDlg.startCarryDlg();
		}else{
			isNeedRunsetBrandFunc = false;
			simInfoSetDlg.startBrandDlg();
		}
    }
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		ConfigData configData = ConfigModel.getInstance(this).getConfigData();
		switch(buttonView.getId()){
		case R.id.flowCorrectionSwitch:
			if(correctFlowBySmsData.getIsAutoCorrect() != isChecked){
				if(isChecked){
					isNeedRunOpenaAutoCorrectFunc = true;
					openAutoCorrect();
				}else{
					closeAutoCorrect();
				}
			}			
			break;
		}	
	}
	
	/**
	 * 判断sim的信息设置是否完成
	 * @return
	 */
	private boolean isSimInfoSetFinish(){
		boolean result = true;
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		CodeName brand = correctFlowBySmsData.getBrand();
		
		if(province == null || city == null){
			result = false;
		}else if(carry == null){
			result = false;
		}else if(brand == null){
			result = false;
		}
		return result;
	}
	
	/**
	 * 打开自动校正流量的功能
	 */
	private void openAutoCorrect(){
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		CodeName brand = correctFlowBySmsData.getBrand();
		
		if(province == null || city == null){
			simInfoSetDlg.startProvinceDlg();
		}else if(carry == null){
			simInfoSetDlg.startCarryDlg();
		}else if(brand == null){
			simInfoSetDlg.startBrandDlg();
		}else{
			isNeedRunOpenaAutoCorrectFunc = false;			
			correctFlowBySmsData.setIsAutoCorrect(true);	
			AlarmUtils.setNextAlert(this);
			flowCorrectionSwitch.setChecked(true);
		}
	}
	
	private void closeAutoCorrect(){
		correctFlowBySmsData.setIsAutoCorrect(false);
		AlarmUtils.disableAlarm(this);
		flowCorrectionSwitch.setChecked(false);
	}
	
	/**
	 * 即刻执行自动校正
	 */
	private void autoCorrectionFunc(){		
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		CodeName brand = correctFlowBySmsData.getBrand();
		
		if(province == null || city == null){
			simInfoSetDlg.startProvinceDlg();
		}else if(carry == null){
			simInfoSetDlg.startCarryDlg();
		}else if(brand == null){
			simInfoSetDlg.startBrandDlg();
		}else{
			isNeedRunAutoCorrectionFunc = false;
			CorrectionConfig mConfig = new CorrectionConfig();
			mConfig.mSimIndex = Utils.getDataEnabledSimCard(FlowCorrectionActivity.this);
			mConfig.mProvinceId = province.mCode;
			mConfig.mCityId = city.mCode;
			mConfig.mCarryId = carry.mCode;
			mConfig.mBrandId = brand.mCode;
			mConfig.mClosingDay = ConfigModel.getInstance(getApplicationContext()).getConfigData().getMonthEndDate();
			if(Utils.getImsi(this) == null){//没有可上网的sim卡				
				Toast.makeText(this, R.string.no_sim_for_net,Toast.LENGTH_SHORT).show();
			}else if(!NetworkUtils.isConn(this)){
				Toast.makeText(this, R.string.net_work_error,Toast.LENGTH_SHORT).show();
			}else{
				autoCorrectionBtn.setEnabled(false);
				CorrectFlowBySmsModel.getInstance(this).startAutoCorrectFlow(mConfig);	
			}					
		}		
	}

	@Override
	protected void onDestroy() {
		SimChangeSubject.getInstance().detach(this);
		CorrectFlowBySmsModel.getInstance(this).detach(this);
		if(correctFlowBySmsData != null){
			correctFlowBySmsData.save(this);
		}
		releaseObject();
		super.onDestroy();
	}
    
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		
	}

	@Override
	public void result(boolean isCancel, SetSimInfoTypeEnum type) {		
		if(isCancel){
			isNeedRunAutoCorrectionFunc = false;
			isNeedRunOpenaAutoCorrectFunc = false;
			isNeedRunSetCarryFunc = false;
			isNeedRunsetBrandFunc = false;
		}else if(isNeedRunAutoCorrectionFunc){
			autoCorrectionFunc();
		}else if(isNeedRunOpenaAutoCorrectFunc){
			openAutoCorrect();
		}else if(isNeedRunSetCarryFunc){
			setCarry();
		}else if(isNeedRunsetBrandFunc){
			setBrand();
		}
		updateSimInfoView();
		if(!isSimInfoSetFinish() && !isNeedRunOpenaAutoCorrectFunc){
			closeAutoCorrect();
		}
	}
	
	private void updateSimInfoView(){
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		CodeName brand = correctFlowBySmsData.getBrand();
		
		if(province != null && 
				city != null){	
			if(province.mName != null && 
					province.mName.equals(city.mName)){
				provinceText.setText(province.mName);
			}else{
				provinceText.setText(province.mName+" "+city.mName);
			}			
		}else{
			provinceText.setText(getString(R.string.please_choice));
		}
		
		if(carry != null){
			operatorsText.setText(carry.mName);
		}else{
			operatorsText.setText(getString(R.string.please_choice));
		}
		
		if(brand != null){
			brandText.setText(brand.mName);
		}else{
			brandText.setText(getString(R.string.please_choice));
		}
	}

	@Override
	public void simChange(SimChangeSubject subject) {
		
		boolean hasChinaSim = Utils.hasChinaSIMCard(this);
		if(!hasChinaSim) {
			finish();
			return;
		}
		
		String imsi = Utils.getImsi(this);
    	if(StringUtils.isEmpty(imsi)){
    		Toast.makeText(this, R.string.no_sim_for_net,Toast.LENGTH_SHORT).show();
    		finish();
    	}else{
    		updateView();
    	}		
	}

	@Override
	public void autoFlowCorrectCallBack(AutoCorrectInfo autoCorrectInfo) {
		if(autoCorrectInfo == null){
			return ;
		}
		Message sendMsg = mUIhandler.obtainMessage();
		sendMsg.obj = autoCorrectInfo;
		mUIhandler.sendMessage(sendMsg);
		
	}

	final class UIHandler extends Handler{		
		public UIHandler(Looper looper){
            super(looper);
        }
		@Override
	    public void handleMessage(Message msg) {  
			AutoCorrectInfo autoCorrectInfo = (AutoCorrectInfo)msg.obj;
			if(autoCorrectInfo == null || autoCorrectionBtn == null){
				return ;
			}
			if(!autoCorrectInfo.isSucess || 
					autoCorrectInfo.step == STEP.END){
				autoCorrectionBtn.setEnabled(true);
				if (!TextUtils.isEmpty(autoCorrectInfo.hintMsg)) {
					Toast.makeText(FlowCorrectionActivity.this, autoCorrectInfo.hintMsg,Toast.LENGTH_SHORT).show();
				}
				autoCorrectionBtn.setText(R.string.now_auto_correction);
			}else{
				autoCorrectionBtn.setEnabled(false);
				autoCorrectionBtn.setText(autoCorrectInfo.hintMsg);
			}
	    }
	}
	
}

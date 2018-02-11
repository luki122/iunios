package com.netmanage.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraNumberPicker;
import aurora.widget.AuroraSpinner;

import com.aurora.netmanage.R;
import com.netmanage.data.ConfigData;
import com.netmanage.model.ConfigModel;
import com.netmanage.totalCount.CountInfo;
import com.netmanage.totalCount.TotalCount;
import com.netmanage.utils.FlowUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import com.netmanage.view.InfoDialog;
/**
 * 套餐设置
 */
public class PackageSetActivity extends AuroraActivity {
	private String TAG ="PackageSetActivity";
	
	private final int REQUEST_CODE_OF_Flow_Correction = 1;
	private AuroraSpinner flowUnitSpinner;
	private AuroraEditText flowEditText;
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> flowUnitSpinnerAdapter;
	private AuroraNumberPicker numPicker;
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.package_set_activity);
        }else{
        	setAuroraContentView(R.layout.package_set_activity,
            		AuroraActionBar.Type.Dashboard);
            getAuroraActionBar().setTitle(R.string.package_set);
        }    
        initView();  
        initData();   
        registerAuroraViewListener();
        updateView();
        checkAndShowResetDialog();
    }
    
    private void initView(){
    	flowUnitSpinner = (AuroraSpinner)findViewById(R.id.flowUnitSpinner);
    	flowEditText = (AuroraEditText)findViewById(R.id.flowEditText); 
    	numPicker = (AuroraNumberPicker)findViewById(R.id.aurora_minute);
    	
    	flowEditText.setOnEditorActionListener(new OnEditorActionListener() {  
	        @Override  
	        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {  
	        	if (actionId == EditorInfo.IME_ACTION_NEXT) {
	        		Utils.hideSoftInput(PackageSetActivity.this, flowEditText);
	        		View view = findViewById(R.id.flowLayout);
	        		view.setFocusable(true);
	        		view.setFocusableInTouchMode(true);
	        		flowEditText.clearFocus();
	        		view.requestFocusFromTouch();
	        		return true;
	        	}
	            return false;  
	        }  
    	}); 
    }  
    
    private void initData(){   	
		adapter = new ArrayAdapter<String>(this,
		        R.layout.package_set_spinner_item, 
		        getResources().getStringArray(R.array.dates));
    	
    	flowUnitSpinnerAdapter = new ArrayAdapter<String>(this,
		        R.layout.simple_spinner_item, 
		        getResources().getStringArray(R.array.flow_unit));
    	flowUnitSpinner.setAdapter(flowUnitSpinnerAdapter);
    	  	
    	numPicker.setMinValue(1);
    	numPicker.setMaxValue(31);
    	numPicker.setFormatter(AuroraNumberPicker.TWO_DIGIT_FORMATTER);
    	numPicker.setLabel(getString(R.string.date)); 
    	numPicker.setSelectionSrc(null);

//    	/*数值改变监听，第一个参数AuroraNumuberPicker,第二个是之前选中的数据，第三个是新的数据，
//
//    	*通过这个监听器可以获得滑动时改变的数据值
//
//    	*/
//    	numPicker.setOnValueChangedListener(new OnValueChangeListener() {
//
//    	@Override
//    	public void onValueChange(AuroraNumberPicker picker, int oldValue, int newValue) {
//    	// TODO Auto-generated method stub
//  //  	Log.e(“luofu”, “oldValue:”+oldValue+” newValue:”+newValue);
//    	}
//    	});
    } 
    
    private void updateView(){
    	if(flowEditText == null || 
    			numPicker == null ||
    					flowUnitSpinner == null){
    		return ;
    	}
    	ConfigData configData = ConfigModel.getInstance(this).getConfigData();
    	long monthlyFlow = configData.getMonthlyFlow();
//    	if(monthlyFlow > 9999){
//    		monthlyFlow = Math.round(monthlyFlow/1024.0);
//    		flowUnitSpinner.setSelection(1);
//    	}
    	// billy 201403019
    	if(monthlyFlow != 0 && (monthlyFlow%1024 == 0)){
    		monthlyFlow = monthlyFlow/1024;
    		flowUnitSpinner.setSelection(1);
    	}
		if(StringUtils.isEmpty(flowEditText.getText().toString())){
			flowEditText.setHint(""+monthlyFlow);	
		}else{
			flowEditText.setText(""+monthlyFlow);	
		}  
		numPicker.setValue(configData.getMonthEndDate());
    }
    
    /**
     * 判断是否需要弹出重置流量套餐的对话框
     */
    private void checkAndShowResetDialog(){
    	String curImsi = Utils.getImsi(this);		
		if(StringUtils.isEmpty(curImsi)){
			return ;
		}
		ConfigData configData = ConfigModel.getInstance(this).getConfigData();
		String packageCorrespondingImsi = configData.getImsi();
		
		if(curImsi.equals(packageCorrespondingImsi) || !configData.isSetedFlowPackage()){
			return ;
		}
		
		InfoDialog.showDialog(this, 
				R.string.reset_flow_package,
				android.R.attr.alertDialogIcon,
				R.string.are_you_want_reset_flow_package,
				R.string.sure,
				new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    	FlowUtils.changeMonthlyFlow(PackageSetActivity.this, 0);
                    	FlowUtils.changeMonthEndDate(PackageSetActivity.this,1);
                    	updateView();
                    }
                }, 
                R.string.cancel, 
				null, 
				null);
    }

	@Override
	protected void onDestroy() {
		releaseObject();
		super.onDestroy();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
		case REQUEST_CODE_OF_Flow_Correction:
			updateView();
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
    
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
		
	} 
	
	/**
	 * 保存用户的设置
	 */
    private void saveSetting(){
    	if(numPicker == null || 
				flowUnitSpinner == null || 
				flowEditText == null){
			return ;
		}
		Utils.hideSoftInput(this, flowEditText);
		
		int unitRelativeToMb = 1;
		if(flowUnitSpinner.getSelectedItemPosition() == 1){//gb
			unitRelativeToMb = 1024;
		}
		
		try{		
			long monthlyFlow  = Integer.parseInt(flowEditText.getText().toString())*unitRelativeToMb ;
			FlowUtils.changeMonthlyFlow(this, monthlyFlow);
		}catch(Exception e1){
			try{
				long monthlyFlow  = Integer.parseInt(flowEditText.getHint().toString())*unitRelativeToMb ;
				FlowUtils.changeMonthlyFlow(this, monthlyFlow);
			}catch (Exception e2) {}		
		}
				
		try{
            FlowUtils.changeMonthEndDate(this,numPicker.getValue());
		}catch(Exception e){
			e.printStackTrace();
		}	
		
		// 统计 成功设置流量套餐
		new TotalCount(this, CountInfo.MODULE_ID, CountInfo.ACTION_ID_0, 1).CountData();
    }
	
	private void registerAuroraViewListener(){		
     	getAuroraActionBar().getOkButton().setOnClickListener(
	        new View.OnClickListener() {	
	            @Override
	            public void onClick(View v) {
	            	saveSetting();
	            	PackageSetActivity.this.finish();
	            }
	        });
		
		getAuroraActionBar().getCancelButton().setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                	PackageSetActivity.this.finish();
                }
        });
	}
}

package com.netmanage.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.netmanage.interfaces.SimChangeSubject;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import tmsdk.bg.module.network.CodeName;

public class CorrectFlowBySmsData {
	private static CorrectFlowBySmsData instance;
	
    private CodeName province;//省份
    private CodeName city;//城市
    private CodeName carry;//运营商（例如：中国移动）
    private CodeName brand;//品牌（例如：动感地带）	
    private boolean isAutoCorrect;//是否自动校正流量
    private String recordImsi;
    private Context applicationContext;
    
    private CorrectFlowBySmsData(){}
    
    private CorrectFlowBySmsData(Context context){
    	this();
    	init(context.getApplicationContext());
    }
    
	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static synchronized CorrectFlowBySmsData getInstance(Context context) {
		if (instance == null) {
			instance = new CorrectFlowBySmsData(context);
		}
		return instance;
	}
	
	public String getRecordImsi(){
		return recordImsi;
	}
    
    public synchronized void setProvince(CodeName province){
    	this.province = province;
    }
    
    public CodeName getProvince(){
    	return this.province;
    }
    
    public synchronized void setCity(CodeName city){
    	this.city = city;
    }
    
    public CodeName getCity(){
    	return this.city;   	
    }
    
    public synchronized void setCarry(CodeName carry){
    	this.carry = carry;
    }
    
    public CodeName getCarry(){
    	return this.carry;    	
    }
    
    public synchronized void setBrand(CodeName brand){
    	this.brand = brand;
    }
    
    public CodeName getBrand(){
    	return this.brand;   	
    }
    
    public synchronized void setIsAutoCorrect(boolean isAutoCorrect){
    	this.isAutoCorrect = isAutoCorrect;
    }
    
    public boolean getIsAutoCorrect(){
    	return this.isAutoCorrect;   	
    } 
    
	private void init(Context context){	
		if(context == null){
			return ;
		}
		applicationContext = context;
		SharedPreferences setting = context.getSharedPreferences(mConfig.CORRECT_FLOW_BY_SMS_INFO_KEY, 0);
		if(setting == null){
			return ;
		}
		
		String provinceCode = setting.getString(mConfig.province_code_KEY, null);
		String provinceName = setting.getString(mConfig.province_name_KEY, null);
		if(provinceCode != null && provinceName != null){
			province = new CodeName(provinceCode,provinceName);
		}
		
		String cityCode = setting.getString(mConfig.city_code_KEY, null);
		String cityName = setting.getString(mConfig.city_name_KEY, null);
		if(cityCode != null && cityName != null){
			city = new CodeName(cityCode,cityName);
		}
		
		String carryCode = setting.getString(mConfig.carry_code_KEY, null);
		String carryName = setting.getString(mConfig.carry_name_KEY, null);
		if(carryCode != null && carryName != null){
			carry = new CodeName(carryCode,carryName);
		}
		
		String brandCode = setting.getString(mConfig.brand_code_KEY, null);
		String brandName = setting.getString(mConfig.brand_name_KEY, null);
		if(brandCode != null && brandName != null){
			brand = new CodeName(brandCode,brandName);
		}
		recordImsi = setting.getString(mConfig.IMSI_KEY, null);
		isAutoCorrect = setting.getBoolean(mConfig.isAutoCorrect_INFO_KEY, false);
	}
	
	/**
	 * 检测并处理上网sim更换的情况
	 */
	public synchronized void checkAndDealSimChange(String curImsi){
		if(curImsi != null && 
				!curImsi.equals(getRecordImsi())){
			clearData(applicationContext);
			SimChangeSubject.getInstance().notifyObservers();
		}
	}
	
	/**
	 * 清空数据
	 * @param context
	 */
	public synchronized void clearData(Context context){
		resetData();
		save(context);
	}
	
	private void resetData(){
		if(province != null){
			province = null;
		}
		if(city != null){
			city = null;
		}
		if(carry != null){
			carry = null;
		}
		if(brand != null){
			brand = null;
		}
		isAutoCorrect = false;
	}
    
	public synchronized void save(Context context){
		if(context == null){
			return ;
		}
		
		//add at 20140905 for 拔除sim卡，不删除sim设置的信息 begin
		String curImsi = Utils.getImsi(context);
		if(curImsi == null){
			return ;
		}else{
			recordImsi = curImsi;
		}//add at 20140905 for 拔除sim卡，不删除sim设置的信息 end	
		
		SharedPreferences setting = context.getSharedPreferences(mConfig.CORRECT_FLOW_BY_SMS_INFO_KEY, 0);
		if(setting == null){
			return ;
		}	
        SharedPreferences.Editor editor = setting.edit();		
		if(editor == null){
			return ;
		}
		
		CodeName province = getProvince();
		CodeName city = getCity();
		CodeName carry = getCarry();
		CodeName brand = getBrand();

		if(province != null){
			editor.putString(mConfig.province_code_KEY, province.mCode);
			editor.putString(mConfig.province_name_KEY, province.mName);
		}else{
			editor.putString(mConfig.province_code_KEY, null);
			editor.putString(mConfig.province_name_KEY, null);
		}
		
		if(city != null){
			editor.putString(mConfig.city_code_KEY, city.mCode);
			editor.putString(mConfig.city_name_KEY, city.mName);
		}else{
			editor.putString(mConfig.city_code_KEY, null);
			editor.putString(mConfig.city_name_KEY, null);
		}
		
		if(carry != null){
			editor.putString(mConfig.carry_code_KEY, carry.mCode);
			editor.putString(mConfig.carry_name_KEY, carry.mName);
		}else{
			editor.putString(mConfig.carry_code_KEY, null);
			editor.putString(mConfig.carry_name_KEY, null);
		}
		
		if(brand != null){
			editor.putString(mConfig.brand_code_KEY, brand.mCode);
			editor.putString(mConfig.brand_name_KEY, brand.mName);
		}else{
			editor.putString(mConfig.brand_code_KEY, null);
			editor.putString(mConfig.brand_name_KEY, null);
		}	
		
		editor.putString(mConfig.IMSI_KEY, recordImsi);
		editor.putBoolean(mConfig.isAutoCorrect_INFO_KEY, isAutoCorrect);	
		editor.commit();
	}
}

package com.netmanage.data;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;

public class ConfigData extends BaseData{
	public static final int DEF_EARLY_WARNIMG_PERCENT = 85;
	
	private String imsi;
	private long monthlyFlow = 0;//套餐包月流量（单位MB）
	private int monthEndDate = 1;//套餐月结日
	private int earlyWarningPercent = DEF_EARLY_WARNIMG_PERCENT;//预警值百分比 (0-100之间，代表百分比)
	private boolean ExcessEarlyWarningSwitch = true;//超额预警    
	private boolean backgroundTrafficTipsSwitch = true;//后台流量提示
    private boolean DailyExcessTipsSwitch = true;//每日超额提示
    private boolean AutomaticCorrectionOfFlowSwitch = false;//自动校正流量
    private boolean isAlreadyChoiceMonthEndDate = false;//记录是否已经选择了月结日
    private String province = "";//省份
    private String packageType = "";//套餐类型
    private long statisticsFlowBeginTime = 0;//统计流量开始时间
    private long correctFlow = 0;//校正流量
    private long correctTime=0;//校正流量的时间

	public ConfigData() {
		super("ConfigData");
	}
	
	/**
	 * 重置数据
	 */
	public void reset(){
		imsi = "";
		monthlyFlow = 0;//套餐包月流量（单位MB）
		monthEndDate = 1;//套餐月结日
		earlyWarningPercent = DEF_EARLY_WARNIMG_PERCENT;//预警值百分比 (0-100之间，代表百分比)
		ExcessEarlyWarningSwitch = true;//超额预警    
		backgroundTrafficTipsSwitch = true;//后台流量提示
	    DailyExcessTipsSwitch = true;//每日超额提示
	    AutomaticCorrectionOfFlowSwitch = false;//自动校正流量
	    isAlreadyChoiceMonthEndDate = false;//记录是否已经选择了月结日
	    province = "";//省份
	    packageType = "";//套餐类型
	    statisticsFlowBeginTime = 0;//统计流量开始时间
	    correctFlow = 0;//校正流量
	    correctTime=0;//校正流量的时间
	}
	
	public JSONObject getJson() {				
		JSONObject json = new JSONObject();	
		json.put("imsi", imsi);
		json.put("monthlyFlow", monthlyFlow);
		json.put("monthEndDate", monthEndDate);
		json.put("earlyWarningPercent", earlyWarningPercent);
		json.put("ExcessEarlyWarningSwitch", ExcessEarlyWarningSwitch);
		json.put("backgroundTrafficTipsSwitch", backgroundTrafficTipsSwitch);
		json.put("DailyExcessTipsSwitch", DailyExcessTipsSwitch);
		json.put("AutomaticCorrectionOfFlowSwitch", AutomaticCorrectionOfFlowSwitch);
		json.put("isAlreadyChoiceMonthEndDate", isAlreadyChoiceMonthEndDate);
		json.put("province", province);		
		json.put("packageType", packageType);	
		json.put("statisticsFlowBeginTime", statisticsFlowBeginTime);	
		json.put("correctFlow", correctFlow);
		json.put("correctTime", correctTime);
		return json;
	}
	
	/**
	 * 解析json对象
	 * @param json
	 * @return true 解析成功  false 解析失败
	 */
	public boolean parseJson(JSONObject json) throws Exception{	
		boolean result = false;
		  if (json != null && !json.isEmpty()) {	
			  imsi = json.getString("imsi");   
			  monthlyFlow = json.getLong("monthlyFlow");
			  monthEndDate = json.getIntValue("monthEndDate");
			  earlyWarningPercent = json.getIntValue("earlyWarningPercent");
			  ExcessEarlyWarningSwitch = json.getBooleanValue("ExcessEarlyWarningSwitch");
			  backgroundTrafficTipsSwitch = json.getBooleanValue("backgroundTrafficTipsSwitch");
			  DailyExcessTipsSwitch = json.getBooleanValue("DailyExcessTipsSwitch");
			  AutomaticCorrectionOfFlowSwitch = json.getBooleanValue("AutomaticCorrectionOfFlowSwitch");
			  isAlreadyChoiceMonthEndDate = json.getBooleanValue("isAlreadyChoiceMonthEndDate");
			  province = json.getString("province");   
			  packageType = json.getString("packageType");  
			  statisticsFlowBeginTime = json.getLong("statisticsFlowBeginTime");
			  correctFlow = json.getLong("correctFlow");
			  correctTime = json.getLong("correctTime");
			  result = true;			 			 
		  }		  
		  return result;
	}
	
	/**
	 * 设置当前套餐对应的imsi
	 * @param imsi
	 */
	public void setImsi(String imsi){
		this.imsi = imsi;
	}
	
	/**
	 * 获取当前套餐对应的imsi
	 * @return
	 */
	public String getImsi(){
		return this.imsi;		
	}
	
	/**
	 * 套餐包月流量（单位MB）
	 * @param monthlyFlow
	 */
	public void setMonthlyFlow(long monthlyFlow){
		this.monthlyFlow = monthlyFlow;
	}
	
	/**
	 * 套餐月结日
	 * @param monthEndDate
	 */
	public void setMonthEndDate(int monthEndDate){
		this.monthEndDate = monthEndDate;
	}
	
	/**
	 * 预警值百分比  (0-100之间，代表百分比)
	 * @param monthEndDate
	 */
	public void setEarlyWarningPercent(int earlyWarningPercent){
		this.earlyWarningPercent = earlyWarningPercent;
	}
	
	/**
	 * 超额预警   
	 * @param ExcessEarlyWarningSwitch
	 */
	public void setExcessEarlyWarningSwitch(boolean ExcessEarlyWarningSwitch){
		this.ExcessEarlyWarningSwitch = ExcessEarlyWarningSwitch;
	}
	
	/**
	 * 后台流量提示
	 * @param backgroundTrafficTipsSwitch
	 */
	public void setBackgroundTrafficTipsSwitch(boolean backgroundTrafficTipsSwitch){
		this.backgroundTrafficTipsSwitch = backgroundTrafficTipsSwitch;
	}
	
	/**
	 * 每日超额提示
	 * @param DailyExcessTipsSwitch
	 */
	public void setDailyExcessTipsSwitch(boolean DailyExcessTipsSwitch){
		this.DailyExcessTipsSwitch = DailyExcessTipsSwitch;
	}
	
	/**
	 * 自动校正流量
	 * @param AutomaticCorrectionOfFlowSwitch
	 */
	public void setAutomaticCorrectionOfFlowSwitch(boolean AutomaticCorrectionOfFlowSwitch){
		this.AutomaticCorrectionOfFlowSwitch = AutomaticCorrectionOfFlowSwitch;
	}
	
	/**
	 * 记录是否已经选择了月结日
	 * @param isAlreadyChoiceMonthEndDate
	 */
	public void setIsAlreadyChoiceMonthEndDate(boolean isAlreadyChoiceMonthEndDate){
		this.isAlreadyChoiceMonthEndDate = isAlreadyChoiceMonthEndDate;
	}
	
	/**
	 * 记录是否已经选择了月结日
	 * @return true:已经选择；false没有选择
	 */
	public boolean getIsAlreadyChoiceMonthEndDate(){
		return this.isAlreadyChoiceMonthEndDate;
	}
	
	/**
	 * 省份
	 * @param province
	 */
	public void setProvince(String province){
		this.province = province;
	}
	 
	/**
	 * 套餐类型
	 * @param packageType
	 */
	public void setPackageType(String packageType){
		this.packageType = packageType;
	}
	
	/**
	 * 套餐包月流量（单位MB）
	 * @param monthlyFlow
	 */
	public long getMonthlyFlow(){
		return this.monthlyFlow;
	}
	
	/**
	 * 套餐月结日
	 * @param monthEndDate
	 */
	public int getMonthEndDate( ){
		return this.monthEndDate;
	}
	
	/**
	 * 预警值百分比 (0-100之间，代表百分比)
	 * @param earlyWarningValue
	 */
	public int getEarlyWarningPercent( ){
		return this.earlyWarningPercent;
	}
	
	/**
	 * 超额预警   
	 * @param ExcessEarlyWarningSwitch
	 */
	public boolean getExcessEarlyWarningSwitch( ){
		return this.ExcessEarlyWarningSwitch;
	}
	
	/**
	 * 后台流量提示
	 * @param backgroundTrafficTipsSwitch
	 */
	public boolean getBackgroundTrafficTipsSwitch(){
		return this.backgroundTrafficTipsSwitch;
	}
	
	/**
	 * 每日超额提示
	 * @param DailyExcessTipsSwitch
	 */
	public boolean getDailyExcessTipsSwitch(){
		return this.DailyExcessTipsSwitch;
	}
	
	/**
	 * 自动校正流量
	 * @param AutomaticCorrectionOfFlowSwitch
	 */
	public boolean getAutomaticCorrectionOfFlowSwitch(){
		return this.AutomaticCorrectionOfFlowSwitch;
	}
	
	/**
	 * 省份
	 * @param province
	 */
	public String getProvince(){
		return this.province;
	}
	
	/**
	 * 套餐类型
	 * @param packageType
	 */
	public String getPackageType(){
		return this.packageType;
	}
	
	/**
	 * 判断是否设置流量套餐
	 * @return true 已经设置套餐
	 *         false 没有设置套餐
	 */
	public boolean isSetedFlowPackage(){
		if(monthlyFlow > 0 && isAlreadyChoiceMonthEndDate){
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 统计流量开始时间
	 */
	public void setStatisticsFlowBeginTime(long statisticsFlowBeginTime){
		Log.i(tag,"set statisticsFlowBeginTime = "+statisticsFlowBeginTime);
		this.statisticsFlowBeginTime = statisticsFlowBeginTime;
	}
	
//	/**
//	 * 统计流量开始时间
//	 * @return
//	 */
//	public long getStatisticsFlowBeginTime(Context context){
//		//如果更换sim卡，则之前清空流量的时间点就应该重置	
//		String curImsi = Utils.getImsi(context);
//		if(curImsi != null && imsi != null && !curImsi.equals(imsi)){
//			statisticsFlowBeginTime = 0;
//		}
//		
//		//说明：流量开始统计时间必须在本次月结内，要不然就重置该变量
//		if(!TimeUtils.isTheStampInThisMonthly(monthEndDate, statisticsFlowBeginTime)){
//			statisticsFlowBeginTime = 0;
//		}
//		
//		long result;
//		if(statisticsFlowBeginTime >0){
//			result = statisticsFlowBeginTime;
//		}else if( isSetedFlowPackage() && monthEndDate>0){
//			result = TimeUtils.getLastMonthEndTimeStamp(monthEndDate);
//		}else{
//			result = 0;
//		}	
//		Log.i(tag,"get statisticsFlowBeginTime = "+result);
//		return result;
//	}
	
	/**
	 * 统计流量开始时间
	 * @return
	 */
	public long getFlowBeginTime(Context context){
		long result;
		boolean isSeted = isSetedFlowPackage();
		Log.i(tag,"isSeted="+isSeted+",monthEndDate="+monthEndDate);
		if( isSeted && monthEndDate>0){
			result = TimeUtils.getLastMonthEndTimeStamp(monthEndDate);
		}else{
			result = 0;
		}
		return result;
	}
	  
//	/**
//	 * 校正流量
//	 * @param correctFlow 单位为 mb
//	 */
//	public void setCorrectFlow(long correctFlow){
//		  this.correctFlow = correctFlow;
//	}
	  
//	/**
//	 * 校正流量
//	 * @param correctFlow 单位为 mb
//	 */ 
//	public long getCorrectFlow(Context context){
//		checkResetCorrectFlowAndTime(context);		
//		Log.i(tag,"get correctFlow = "+correctFlow);
//		return correctFlow;
//	}
	
//	/**
//	 * 校正流量的时间
//	 * @param correctTime 单位毫秒
//	 */
//	public void setCorrectTime(long correctTime){
//		  this.correctTime = correctTime;
//	}
	
//	/**
//	 * 校正流量的时间 单位毫秒
//	 * @return
//	 */
//	public long getCorrectTime(Context context){
//		checkResetCorrectFlowAndTime(context);
//		Log.i(tag,"get correctTime = "+correctTime);
//		return this.correctTime;
//	}
	
	/**
	 * 判断校正流量的数据要不要重置
	 * @param context
	 */
	private void checkResetCorrectFlowAndTime(Context context){
		//如果更换sim卡，则之前清空	校正流量
		String curImsi = Utils.getImsi(context);
		if(curImsi != null && !curImsi.equals(imsi)){
			correctTime = 0;
			correctFlow = 0;
		}
		
		//说明：流量开始统计时间必须在本次月结内，要不然就重置该变量
		if(!TimeUtils.isTheStampInThisMonthly(monthEndDate, correctTime)){
			correctTime = 0;
			correctFlow = 0;
		}
	}
}

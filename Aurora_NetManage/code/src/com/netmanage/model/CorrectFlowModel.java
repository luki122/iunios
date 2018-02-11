package com.netmanage.model;

import java.util.HashMap;
import java.util.Set;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netmanage.data.ConfigData;
import com.netmanage.data.FlowData;
import com.netmanage.model.DataUsageSummary.GetFlowListener;
import com.netmanage.utils.ApkUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import com.netmanage.utils.MySharedPref;
import android.net.NetworkStats;

/**
 * 更正流量
 * @author chengrq
 * 记录更正时间点（correctTime），从系统获取的每个应用的流量数据。
 * 这样正确的统计流量就是从系统获取的流量减去更正时间点每个应用的流量（errorFlow），再加上更正的流量（correctFlow）
 */
public class CorrectFlowModel implements GetFlowListener{
	private static CorrectFlowModel instance;
    private long correctTime;
    private long correctFlow;
    private long errorTotalFlow = 0;//错误总流量（包含已删除应用）
    private HashMap<String,FlowData> errorFlowMap;
	private Context context = null;
	private final String TAG = "CorrectFlowModel";
	private DataUsageSummary dataUsageSummary;
	
	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static synchronized CorrectFlowModel getInstance(Context context) {
		if (instance == null) {
			instance = new CorrectFlowModel(context);
		}
		return instance;
	}
	
	private CorrectFlowModel(Context context) {
		this.context = context.getApplicationContext();
		readCacheStr(context);
	}
	
	private boolean readCacheStr(Context context){	
		boolean result = true;	
		String str = null;
		synchronized (mConfig.FILE_ERROR_FLOW){
		   str = FileModel.getInstance(context).readFile(mConfig.FILE_ERROR_FLOW);
		   Log.i(TAG,"read:"+str);
		}	
		if(StringUtils.isEmpty(str)){
			return false;
		}
		
		try {
            parseItem(str);
            result = true;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	private void parseItem(String str) throws Exception {	
		if(errorFlowMap == null){
			errorFlowMap = new HashMap<String,FlowData>();
		}else{
			synchronized(errorFlowMap){
				errorFlowMap.clear();
			}			
		}
		errorTotalFlow = 0;
		
		JSONObject json = JSON.parseObject(str);	
		if (json != null && !json.isEmpty())  {
			try{
				errorTotalFlow = json.getLong("errorTotalFlow");
			}catch(Exception e){
				e.printStackTrace();
			}
			correctTime = json.getLong("correctTime");
			correctFlow = json.getLong("correctFlow");
			JSONArray list = json.getJSONArray("list");
			if ( list != null) {
				for (int i = 0; i < list.size(); i++) {
					JSONObject item = list.getJSONObject(i);
					if (!item.isEmpty()) {
						FlowData flowData = new FlowData();
						if(flowData.parseJson(item)){
							synchronized(errorFlowMap){
								errorFlowMap.put(flowData.getPackageName(), flowData);
							}							
						}
					}
				}
			}			
		}
	}
	
	/**
	 * 获取从月结日到“清空（或更正）流量时间点“，指定应用的流量数据
	 * @param packageName
	 * @return
	 */
	public FlowData getErrorFlow(String packageName){
		if(packageName == null || 
				errorFlowMap == null){
			return null;
		}	
		checkResetCorrectFlowAndTime();
		return errorFlowMap.get(packageName);		
	}
	
	/**
	 * 获取错误总流量
	 * @return
	 */
	public long getErrorTotalFlow(){
		checkResetCorrectFlowAndTime();
		return errorTotalFlow;
	}

	/**
	 * 获取跟正流量的时间戳，如果返回为０，表示在当前月结日，当前卡没有更正过流量数据
	 * @return
	 */
	public long getCorrectTime(){
		checkResetCorrectFlowAndTime();
		return correctTime;
	}
	
	public long getCorrectFlow(){
		checkResetCorrectFlowAndTime();
		return correctFlow;
	}
	
	/**
	 * 判断校正流量的数据要不要重置
	 * @param context
	 */
	private void checkResetCorrectFlowAndTime(){
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		
		if(configData == null){
			return ;
		}
		String oldImsi = configData.getImsi();
		int monthEndDate = configData.getMonthEndDate();
		String curImsi = Utils.getImsi(context);
		
		boolean needRest = false;
		if(!StringUtils.isEmpty(curImsi) && 
				!StringUtils.isEmpty(oldImsi) && 
				!curImsi.equals(oldImsi)){
			//如果更换sim卡，则之前清空校正流量
			needRest = true;	
		}else if(correctTime>0 && !TimeUtils.isTheStampInThisMonthly(monthEndDate,correctTime)){
			//流量开始统计时间不在本次月结内，清空校正流量
			needRest = true;	
		}
		
		if(needRest){
			deleteAllRecord();
		}
	}
	
	/**
	 * 判断没有设置套餐的情况下是否有换SIM卡，有的话重置数据
	 */
	public void checkResetCorrectByChangeSimAndNoSetPackage() {
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		if (configData.isSetedFlowPackage()) {
			return;
		}
		String curImsi = Utils.getImsi(context);
		String oldImsi = MySharedPref.getLastImsi(context);
		// 没有设置过套餐，并且换过sim卡 20140320 billy
		if (StringUtils.isEmpty(curImsi)) {
			return;
		}
		if (!curImsi.equals(oldImsi)) {
			deleteAllRecord();
		}
	}
	
	/**
	 * 保存最后插入手机的sim卡信息
	 * @param context
	 */
	public void saveLastImsi() {
		String curImsi = Utils.getImsi(context);
		if (!StringUtils.isEmpty(curImsi)) {
			MySharedPref.saveLastImsi(context, curImsi);
		}
	}
	
	/**
	 * 更正当前错误流量
	 * @param correctTime 更正流量的时间搓
	 * @param correctFlow 已经使用的流量值（单位为mb）
	 */
	public void CorrectFlow(long correctTime,long correctFlow){
		this.correctTime = correctTime;
		this.correctFlow = correctFlow;
		if(dataUsageSummary == null){
			dataUsageSummary = new DataUsageSummary(context);
			dataUsageSummary.setGetFlowListener(this);
		}
		dataUsageSummary.getFlowData();
	}
	
	/**
	 * 删掉指定应用的记录（例如在某个应用删除后，
	 * 就要把对应的应用记录删除。因为删除一个用用，系统会把对应用的流量数据也清空）
	 * @param packageName
	 */
	public void deleteRecord(String packageName){
		if(StringUtils.isEmpty(packageName) || errorFlowMap == null){
			return ;
		}
		if(errorFlowMap.containsKey(packageName)){
			synchronized(errorFlowMap){
				errorFlowMap.remove(packageName);
			}		
			saveWebStr();
		}
	}
	
	/**
	 * 删除所有错误流量记录，置空更正流量时间，置空更正流量值
	 */
	public void deleteAllRecord(){
		correctTime = 0;
		correctFlow = 0;
		errorTotalFlow = 0;
		if(errorFlowMap != null){
			synchronized(errorFlowMap){
				errorFlowMap.clear();
			}		
		}
		saveWebStr();
	}
	
	@Override
	public void finish(NetworkStats forShowNetworkStats) {
		if(errorFlowMap == null){
			errorFlowMap = new HashMap<String,FlowData>();
		}else{
			errorFlowMap.clear();
		}
		errorTotalFlow = 0;
     	
    	/**
    	 * 说明：data中有可能两条数据的uid是一样的，
         * 那么一条数据表示sim卡前台流量，一个表示smi后台流量
    	 */
    	NetworkStats.Entry entry = null;
    	int size = forShowNetworkStats != null ? forShowNetworkStats.size() : 0;
        for (int i = 0; i < size; i++) {
            entry = forShowNetworkStats.getValues(i, entry); 
            if(entry == null){
            	continue;
            }
            errorTotalFlow = errorTotalFlow+(entry.rxBytes+entry.txBytes);
            if(entry.uid<=0){
            	continue;
            }
            String packageName = ApkUtils.getPackageName(context, entry.uid);
            //统计应用的所有流量                   
            FlowData flowData = errorFlowMap.get(packageName);
            if(flowData == null){
                flowData = new FlowData();
                flowData.setPackageName(packageName);
                flowData.setTotalBytes(entry.rxBytes+entry.txBytes);
                errorFlowMap.put(packageName,flowData);
            }else{
            	flowData.setTotalBytes(flowData.getTotalBytes()+entry.rxBytes+entry.txBytes);
            }
                          
            //统计应用的后台流量
            if(entry.set == NetworkStats.SET_DEFAULT){
            	flowData.setBackBytes(entry.rxBytes+entry.txBytes);
            }
        }
        saveWebStr();
        NetModel instance = NetModel.getInstance();
        if(instance != null){
//        	instance.runCallBack();
        	// 由于可能校正时，别的程序正在产生流量，所以需要再次获取最新流量
        	// instance.runCallBack()调用这个方法可能导致前台显示流量时运算错误
        	instance.resetNetInfo(); 
        }
	}
	
	private void saveWebStr(){
		String needSaveStr = getNeedSaveStr();
		if(context == null || StringUtils.isEmpty(needSaveStr)){
			return ;
		}

		synchronized (mConfig.FILE_ERROR_FLOW){
			Log.i(TAG,"save:"+needSaveStr);
			FileModel.getInstance(context).writeFile(mConfig.FILE_ERROR_FLOW,needSaveStr);
		}		
	}
		
	private String getNeedSaveStr(){		
		JSONObject json = new JSONObject();
		JSONArray jsonList = new JSONArray();
		
		if(errorFlowMap != null && errorFlowMap.size()>0){
			synchronized(errorFlowMap){
				Set<String> keySet = errorFlowMap.keySet();
			    for (String packageName : keySet){
			    	FlowData flowData = errorFlowMap.get(packageName);
			    	jsonList.add(flowData.getJson());
			    }
			}			 
			json.put("list", jsonList);	
		}	
		json.put("correctTime", correctTime);	
		json.put("correctFlow", correctFlow);
		json.put("errorTotalFlow", errorTotalFlow);	
		return json.toJSONString();
	}
	
	
}

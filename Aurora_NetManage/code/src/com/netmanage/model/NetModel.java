package com.netmanage.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import com.netmanage.activity.NetMainActivity;
import com.netmanage.data.ConfigData;
import com.netmanage.data.Constants;
import com.netmanage.data.FlowData;
import com.netmanage.model.DataUsageSummary.GetFlowListener;
import com.netmanage.utils.ApkUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.mConfig;
import com.netmanage.view.MaskedImage;
import com.netmanage.view.ProgressView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.NetworkStats;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

public class NetModel implements GetFlowListener{
	private static NetModel instance;
	private final String TAG ="NetModel";	
	private Context context;
	private long totalFlow = 0;//获取的总流量
	private HashMap<String ,FlowData> flowMap = null;//应用所有流量列表		
	private HashMap<String ,FlowData> flowMapFromCorrectTime = null;
	
	private List<Handler> callBackHandlers;
	private DataUsageSummary dataUsageSummary;
	
	/**
	 * 如果instance为null，不会创建
	 * @return
	 */
	public static synchronized NetModel getInstance() {
		return instance;
	}
	
	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return
	 */
	public static synchronized NetModel getInstance(Context context) {
		if (instance == null) {
			instance = new NetModel(context);
		}
		return instance;
	}
	
	private NetModel(Context context){
		this.context = context.getApplicationContext();
		callBackHandlers = new ArrayList<Handler>();
		dataUsageSummary = new DataUsageSummary(context);
		dataUsageSummary.setGetFlowListener(this);
	}
	
	/**
	 * 重新获取流量数据
	 */
	public void resetNetInfo(){	
		dataUsageSummary.getFlowData();
	}
	
	 /**
     * 最后一次获取流量的sim卡的imsi号
     * @return
     */
    public String getImsiOfLastGetFlow(){
    	return dataUsageSummary.getImsiOfLastGetFlow();
    }
	
	/**
	 * 是不是已经获取过流量数据
	 * @return
	 */
	public boolean isGetFlow(){
		if(flowMap == null || flowMap.size() == 0){
			return false;
		}else{
			return true;
		}
	}
	
	/**
	 * 运行回调去通知界面更新
	 */
	public void runCallBack(){
		updateFlowMapFromCorrectTime();
		ApkUtils.sendBroasdcastToLauncher(context);
		if(callBackHandlers != null){
        	for(Handler handler : callBackHandlers){
				handler.sendEmptyMessage(0);
	   	    }
    	} 
	}
	
	@Override
	public void finish(NetworkStats forShowNetworkStats) {
		endFuncOfGetFlow(forShowNetworkStats);
	}
    
	/**
	 * 获取流量完毕后调用的函数
	 * @param networkStatsForShow 用于显示的流量数据
	 * @param networkStatsForStatistics 用于统计的流量数据
	 */
    private void endFuncOfGetFlow(NetworkStats networkStatsForShow){   
    	if(flowMap == null){
    		flowMap = new HashMap<String ,FlowData>();
    	}else{
    		synchronized(flowMap){
    			flowMap.clear();
    		}  		
    	}
    	totalFlow = 0;
    	   	
    	/**
    	 * 说明：data中有可能两条数据的uid是一样的，
         * 那么一条数据表示sim卡前台流量，一个表示smi后台流量
    	 */
    	NetworkStats.Entry entry = null;
    	int size = networkStatsForShow != null ? networkStatsForShow.size() : 0;
        for (int i = 0; i < size; i++) {
            entry = networkStatsForShow.getValues(i, entry);         
            if(entry == null){
            	continue;
            }
            totalFlow = totalFlow+(entry.rxBytes+entry.txBytes);
            Log.i(TAG,"uid ="+entry.uid+",flow byte="+(entry.rxBytes+entry.txBytes));
            if(entry.uid<=0){
            	continue;
            }
            String packageName = ApkUtils.getPackageName(context, entry.uid);
            if(StringUtils.isEmpty(packageName)){
            	continue;
            }
            //统计应用的所有流量                   
            FlowData flowData = flowMap.get(packageName);
            if(flowData == null){
                flowData = new FlowData();
                flowData.setPackageName(packageName);
                flowData.setTotalBytes(entry.rxBytes+entry.txBytes);
                synchronized(flowMap){
                	flowMap.put(packageName,flowData);
                }              
            }else{
            	flowData.setTotalBytes(flowData.getTotalBytes()+entry.rxBytes+entry.txBytes);
            }
                          
            //统计应用的后台流量
            if(entry.set == NetworkStats.SET_DEFAULT){
            	flowData.setBackBytes(entry.rxBytes+entry.txBytes);
            }          
        }	     
        runCallBack();      	
    }
    
    /**
     * 刷新从更正时间点到此刻的流量数据
     */
    private void updateFlowMapFromCorrectTime(){
    	CorrectFlowModel instance = CorrectFlowModel.getInstance(context);  	
    	if(flowMapFromCorrectTime == null){
			flowMapFromCorrectTime = new HashMap<String ,FlowData>();
		}else{
			flowMapFromCorrectTime.clear();
		}
    	
    	if(flowMap != null && flowMap.size()>0){  		
			List<String>pkgList = new ArrayList<String>();
			/**
			 * 重点：同步块的区域一定要小，最好不要包含自己定义的方法，
			 * 因为有可能同步块内自己写的方法中也加在同样的同步锁，这样就会出现死锁的情况，
			 * 所以下面的代码就做了这样的处理，把checkOneApkAutoStart（）和initOneApkAutoStart（）方法
			 * 放到同步块外面去了。
			 */
			synchronized(flowMap){
				Set<String> packageNames = flowMap.keySet();		
			    for (String packageName : packageNames){
			    	pkgList.add(packageName);
			    }
			}
			for(int i=0;i<pkgList.size();i++){
				String pkgName = pkgList.get(i);
				FlowData flowData = flowMap.get(pkgName);
 		    	if(flowData != null){
 		    		FlowData errorFlowData = instance.getErrorFlow(flowData.getPackageName());
 		    		
 		    		FlowData correctFlowData = new FlowData();
 		    		correctFlowData.setPackageName(flowData.getPackageName());
 		    		if(errorFlowData == null){
 		    			Log.i(TAG,flowData.getPackageName()+":errorFlowData == NULL");
 		    			correctFlowData.setBackBytes(flowData.getBackBytes());
 		    			correctFlowData.setTotalBytes(flowData.getTotalBytes());
 		    		}else{
 		    			Log.i(TAG,flowData.getPackageName()+
 		    					" : errorTotalBytes = "+errorFlowData.getTotalBytes()+
 		    					" : getTotalBytes = "+flowData.getTotalBytes());
 		    			correctFlowData.setBackBytes(flowData.getBackBytes()-errorFlowData.getBackBytes());
 		    			correctFlowData.setTotalBytes(flowData.getTotalBytes()-errorFlowData.getTotalBytes());
 		    		}		    		
 		    		flowMapFromCorrectTime.put(flowData.getPackageName(), correctFlowData);
 		    	}
			}  		
    	} 
    }
    
	 /**
     * 注册观察者对象
     * @param observer
     */
    public void attach(Handler callBackHandler){
	   	 try{
	   		 callBackHandlers.add(callBackHandler);
	   	 }catch(Exception e){
	   		 e.printStackTrace();
	   	 }   	 
    }
    
    /**
     * 删除观察者对象
     * @param observer
     */
    public void detach(Handler callBackHandler){
	   	 try{
	   	     callBackHandlers.remove(callBackHandler);
	   	 }catch(Exception e){
	   		 e.printStackTrace();
	   	 } 
    }
    
    /**
     * 获取存储流量数据的map
     * @return
     */
    public HashMap<String ,FlowData> getFlowMap(){
    	return this.flowMap;
    }
    
    /**
     * 获取存储流量数据的map
     * @return
     */
    public HashMap<String ,FlowData> getFlowMapFromCorrectTime(){
    	return this.flowMapFromCorrectTime;
    }
    
    /**
     * 统计用的2G/3G总流量
     * @return 单位为b
     */
    public long getTotalMoblieFlowForStatistics(){ 	
//hide for 已删除应用的流量要继续统计 20140228    	
//    	long mobileflow = 0;    	
//    	AppsInfo userAppsInfo = ConfigModel.getInstance(context).getAppInfoModel().getThirdPartyAppsInfo();
//    	if(userAppsInfo != null){
//    		for(int i=0;i<userAppsInfo.Count();i++){
//        		AppInfo appInfo = (AppInfo)userAppsInfo.GetData(i);
//        		if(appInfo == null || !appInfo.getIsInstalled()){
//        			continue;
//        		}
//        		FlowData flowData = flowMapFromCorrectTime.get(appInfo.getPackageName());
//        		if(flowData != null){
//        			mobileflow = mobileflow+flowData.getTotalBytes();  
//        		}     	  		
//        	}
//    	}	
//    	
//    	AppsInfo sysAppsInfo = ConfigModel.getInstance(context).getAppInfoModel().getSysAppsInfo();
//    	if(sysAppsInfo != null){
//    		for(int i=0;i<sysAppsInfo.Count();i++){
//        		AppInfo appInfo = (AppInfo)sysAppsInfo.GetData(i);
//        		if(appInfo == null || !appInfo.getIsInstalled()){
//        			continue;
//        		}
//        		FlowData flowData = flowMapFromCorrectTime.get(appInfo.getPackageName());
//        		if(flowData != null){
//        			mobileflow = mobileflow+flowData.getTotalBytes();  
//        		}  
//        	}
//    	}
//    	return mobileflow;
    	
    	//add for 已删除应用的流量要继续统计 20140228       	
    	CorrectFlowModel instance = CorrectFlowModel.getInstance(context); 
    	Log.i(TAG,"getTotalMoblieStatistics totalFlow="+totalFlow+", ErrorTotalFlow="+instance.getErrorTotalFlow());
    	return totalFlow - instance.getErrorTotalFlow();
    }
    
    public static void relaseObject(){  	
    	if(instance != null){
    		instance.context = null;
    		instance.flowMap = null;
    		instance.flowMapFromCorrectTime = null;
    		if(instance.callBackHandlers != null){
				instance.callBackHandlers.clear();
				instance.callBackHandlers = null;
			}
    		instance = null;
    	}
    }
    
/**************获取当日所产生的流量*********************/
	private long lastGetToadyFlowTime = 0;
	private final int GetToadyFlowInterval = 60000;
	private AtomicBoolean isDuringGetToadyFlow = new AtomicBoolean(false);
	
	/**
	 * 获取当日产生的流量
	 * @param callBack
	 */
	public void gettToadyFlow(final Handler callBack){
		//为了防止在E6上出现卡的情况，设置两次获取存储信息的时间间隔必须大于GetInfoTimeTimeInterval
		if(System.currentTimeMillis()-lastGetToadyFlowTime<GetToadyFlowInterval){
			return ;
		}
		lastGetToadyFlowTime = System.currentTimeMillis();		
		if(isDuringGetToadyFlow.get()){
			return ;
		}
		isDuringGetToadyFlow.set(true);
		new Thread() {
			@Override
			public void run() {							
		        try {
		        	long startTime = TimeUtils.getTodayTimeStamp();
		        	long endTime = System.currentTimeMillis();
		        	NetworkStats networkStats = dataUsageSummary.getStatsSession().
		        			getSummaryForAllUid(dataUsageSummary.getTemplate(), 
		        			startTime,
		        			endTime, 
		        			true);	   		        	
		        	Long totalBytesObj = new Long(networkStats.getTotalBytes());
		        	Log.i(TAG,"GET Toady Flow:totalBytes="+totalBytesObj.longValue()+
		        			",start = "+TimeUtils.timeStampToData(startTime)+
		        			", end="+TimeUtils.timeStampToData(endTime));
		        	
		        	Message msg = new Message();
		        	msg.obj = totalBytesObj;
		        	callBack.sendMessage(msg);
		        } catch (Exception e) {
		        	Log.i(TAG,"GET Toady Flow error:"+e.toString());
		            e.printStackTrace();
		        }			 
				isDuringGetToadyFlow.set(false);
			}
		}.start();  
	}
}

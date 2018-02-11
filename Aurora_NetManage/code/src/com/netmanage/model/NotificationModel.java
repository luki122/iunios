package com.netmanage.model;

import com.netmanage.activity.FlowExcessHintActivity;
import com.netmanage.activity.NetMainActivity;
import com.netmanage.activity.PackageSetActivity;
import com.netmanage.data.ConfigData;
import com.netmanage.data.DateData;
import com.netmanage.data.NotificationData;
import com.netmanage.utils.MySharedPref;
import com.netmanage.utils.NetworkUtils;
import com.netmanage.utils.StringUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;
import com.netmanage.utils.ActivityUtils.LoadCallback;
import com.netmanage.view.MyNotification;
import com.aurora.netmanage.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * @author Administrator
 *
 */
public class NotificationModel {   
	private static NotificationModel instance;
	private final String TAG = "NotificationModel";
	private Context context = null;
	private final int NOTIFY_ID_OF_SIM_CHANGE=1;
	private final int NOTIFY_ID_OF_Early_Warning=2;
	private final int NOTIFY_ID_OF_EveryDay=3;
	private final int NOTIFY_ID_OF_BackgroundFlow=4;
	
	
	private NotificationModel(Context context) {
		this.context = context.getApplicationContext();
	}
	
	/**
	 * 如果instance为null，不会创建
	 * @return 返回值有可能为空
	 */
	public static synchronized NotificationModel getInstance(){
		return instance;
	}

	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static synchronized NotificationModel getInstance(Context context) {
		if (instance == null) {
			instance = new NotificationModel(context);
		}
		return instance;
	}	
	
	/**
	 * 处理Sim相关的提醒
	 */
	public void checkSimChangeNotify(String curImsi){			
		if (!StringUtils.isEmpty(curImsi)) {
			if(!isSimChange()){
				Log.i(TAG,"is no Sim Change");
				MySharedPref.clearSimChangeNotifyData(context);
				return ;
			}else{
				NotificationData notifyData = MySharedPref.getSimChangeNotifyData(context);
				DateData lastNotifyTime = notifyData==null?null:notifyData.getLastNotifyTime();
				if(lastNotifyTime == null){
					Log.i(TAG,"lastNotifyTime == null");
					notifyForSimChange(notifyData);
				}else if(notifyData.getAlreadyNotifyTimes()<3 && 
						TimeUtils.isDate2BigThanDate1SomeDays(lastNotifyTime,TimeUtils.getCurDate(),10)){
					Log.i(TAG,"AlreadyNotifyTimes="+notifyData.getAlreadyNotifyTimes());
					notifyForSimChange(notifyData);
				}
			}	
		}
	}
	
	/**
	 * 判断“当前sim卡“与“套餐对应的sim“是否一样
	 */
	private boolean isSimChange(){
		String curImsi = Utils.getImsi(context);		
		if(StringUtils.isEmpty(curImsi)){
			return false;
		}
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		String packageCorrespondingImsi = configData.getImsi();
		
		if(!curImsi.equals(packageCorrespondingImsi)){
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 未设置流量套餐时
	 * 提示条件：检测到有sim卡插入且用户还没进行流量套餐设置时
	 * 提示语：点击立刻设置流量套餐，省流量更省钱！
	 * 提示频率：若用户当天扔没有进行套餐设置，10天后继续弹出，提示最多弹出3次。
	 * 点击消息响应：进入流量管理设置界面
	 */
	private void notifyForSimChange(NotificationData notifyData){
		int titleRes,msgRes;
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		if(configData.isSetedFlowPackage()){
			titleRes = R.string.reset_flow_package;
			msgRes = R.string.reset_flow_package_hint;
		}else{
			titleRes = R.string.set_flow_package;
			msgRes = R.string.set_flow_package_hint;
		}
		
		MyNotification.notify(NOTIFY_ID_OF_SIM_CHANGE,
				context,
				new ComponentName("com.aurora.netmanage",
						"com.netmanage.activity.PackageSetActivity"),
				false,
				R.drawable.ic_launcher,
				context.getString(titleRes),
				context.getString(msgRes));	
		
		DateData lastNotifyTime = new DateData();
		lastNotifyTime.setYear(TimeUtils.getCurYear());
		lastNotifyTime.setMonth(TimeUtils.getCurMonth());
		lastNotifyTime.setDay(TimeUtils.getCurDay());
		
		if(notifyData == null){
			notifyData = new NotificationData();
		}
		notifyData.setAlreadyNotifyTimes(notifyData.getAlreadyNotifyTimes()+1);		
		notifyData.setLastNotifyTime(lastNotifyTime);	
		MySharedPref.saveSimChangeNotifyData(context, notifyData);
	}
	
	/**
	 * 处理流量相关的提醒
	 */
	public void checkFlowNotify(){
		ConfigData configData = ConfigModel.getInstance(context).getConfigData();
		NetModel netModel = NetModel.getInstance();
		checkEarlyWarning(configData,netModel);
		checkEveryDay(configData,netModel);
//		checkBackgroundFlow(configData,netModel);//产品需求：后台流量提示功能被屏蔽
		checkExcess(configData,netModel);		
	}
	
	/**
	 * 获取已经使用的流量
	 * @param configData
	 * @param netModel
	 * @return 单位为 MB
	 */
	private float getUseFlowUnitMb(ConfigData configData,NetModel netModel){
		return (float)(getUseFlowUnitB(configData,netModel)/(1024*1024));
	}
	
	/**
	 * 获取已经使用的流量
	 * @param configData
	 * @param netModel
	 * @return 单位为 B
	 */
	private long getUseFlowUnitB(ConfigData configData,NetModel netModel){
		if(configData == null || 
				netModel == null ){
			return 0 ;
		}
		long mobileflow = netModel.getTotalMoblieFlowForStatistics()+
				CorrectFlowModel.getInstance(context).getCorrectFlow()*1024*1024;
		return mobileflow;
	}
	
	/**
	 * 提前预警提示
	 * @param configData
	 * @param netModel
	 * 提示条件：设置中该开关已打开；达到用户设置的预警提示阀值
	 * 提示语：本月流量套餐已使用85%（此值根据用户设置具体显示），点击查看使用详情！
	 * 提示频率：每月一次
	 * 点击消息响应：应用流量使用排行界面
	 */
	private void checkEarlyWarning(ConfigData configData,NetModel netModel){
		if(configData == null || 
				netModel == null || 
				!configData.isSetedFlowPackage() ||
				!configData.getExcessEarlyWarningSwitch()){
			return ;
		}
		
		NotificationData notifyData = MySharedPref.getEarlyWarningNotifyData(context);
		DateData lastNotifyTime = notifyData==null?null:notifyData.getLastNotifyTime();
		if(TimeUtils.isTheDayInThisMonthly(configData.getMonthEndDate(), lastNotifyTime)){
			return ;
		}
		
		float mobileflow = getUseFlowUnitMb(configData,netModel);
		if(mobileflow <= configData.getMonthlyFlow()*configData.getEarlyWarningPercent()/100){
			return ;
		}
		
		MyNotification.notify(NOTIFY_ID_OF_Early_Warning,
				context,
				new ComponentName("com.aurora.secure",
						"com.secure.activity.NetManageActivity"),
				false,
				R.drawable.ic_launcher,
				context.getString(R.string.flow_notify),
				context.getString(R.string.EarlyWarning_notify_head)+
				configData.getEarlyWarningPercent()+
				context.getString(R.string.EarlyWarning_notify_tail)
				);
		
		
		if(lastNotifyTime == null){
			lastNotifyTime = new DateData();
		}
		lastNotifyTime.setYear(TimeUtils.getCurYear());
		lastNotifyTime.setMonth(TimeUtils.getCurMonth());
		lastNotifyTime.setDay(TimeUtils.getCurDay());
		
		if(notifyData == null){
			notifyData = new NotificationData();
		}
		notifyData.setAlreadyNotifyTimes(1);		
		notifyData.setLastNotifyTime(lastNotifyTime);	
		MySharedPref.saveEarlyWarningNotifyData(context, notifyData);
	}
	
	/**
	 * 每日提示
	 * @param configData
	 * @param netModel
	 * 提示条件：设置中该开关已打开；当天流量使用超过套餐流量的5%
	 * 提示语：今天已使用 XX M流量，本月剩余流量XX M
	 * 提示频率：每天一次
	 * 点击消息响应：进入流量管理主界面
	 */
	private void checkEveryDay(final ConfigData configData,final NetModel netModel){
		if(configData == null || 
				netModel == null || 
				!configData.isSetedFlowPackage()||
				!configData.getDailyExcessTipsSwitch()){
			return ;
		}
		
	    NotificationData notifyData = MySharedPref.getEveryDayNotifyData(context);
		DateData lastNotifyTime = notifyData==null?null:notifyData.getLastNotifyTime();
		if(lastNotifyTime != null && 
				lastNotifyTime.getYear()==TimeUtils.getCurYear() && 
				lastNotifyTime.getMonth()==TimeUtils.getCurMonth() && 
				lastNotifyTime.getDay()==TimeUtils.getCurDay()){
			return ;
		}
		
		final Handler handler = new Handler() {
		    @Override
		    public void handleMessage(Message msg) {
		    	Long toadyBytesObj = (Long)msg.obj;
		    	if(toadyBytesObj == null){
		    		return ;
		    	}
		    	
				long useToadyBytes= toadyBytesObj.longValue();
				if(useToadyBytes <= 1.0*configData.getMonthlyFlow()*1024*1024*5/100){
					return ;
				}
				
				long useTotalBytes= getUseFlowUnitB(configData,netModel);
				long remainderFlow = configData.getMonthlyFlow()*1024*1024-useTotalBytes;
				
				String flow_notify_hint = context.getString(R.string.today_use)+
						Utils.dealMemorySize(context, useToadyBytes)+
						context.getString(R.string.comma);
				
				if(remainderFlow > 0){
					flow_notify_hint = flow_notify_hint+
							context.getString(R.string.this_month_remainder_flow)+
							Utils.dealMemorySize(context, remainderFlow);
				}else{
					flow_notify_hint = flow_notify_hint+
							context.getString(R.string.this_month_exceed_flow)+
							Utils.dealMemorySize(context, Math.abs(remainderFlow));
				}
						    	
				MyNotification.notify(NOTIFY_ID_OF_EveryDay,
						context,
						new ComponentName("com.aurora.secure",
								"com.secure.activity.NetManageActivity"),
						false,
						R.drawable.ic_launcher,
						context.getString(R.string.flow_notify),
						flow_notify_hint);	
				
				NotificationData notifyData = MySharedPref.getEveryDayNotifyData(context);
				DateData lastNotifyTime = notifyData==null?null:notifyData.getLastNotifyTime();
				
				if(lastNotifyTime == null){
					lastNotifyTime = new DateData();
				}
				lastNotifyTime.setYear(TimeUtils.getCurYear());
				lastNotifyTime.setMonth(TimeUtils.getCurMonth());
				lastNotifyTime.setDay(TimeUtils.getCurDay());
				
				if(notifyData == null){
					notifyData = new NotificationData();
				}
				notifyData.setAlreadyNotifyTimes(1);		
				notifyData.setLastNotifyTime(lastNotifyTime);	
				MySharedPref.saveEveryDayNotifyData(context, notifyData);
		   }
		};
		
		netModel.gettToadyFlow(handler);
	}
	
//	/**
//	 * 后台流量提示 （产品需求：后台流量提示功能被屏蔽）
//	 * @param configData
//	 * @param netModel
//	 * 提示条件：设置中该开关已打开；本月结算周期内，应用置于后台时累计流量超过5M或则超过5M的倍数时
//	 * 提示语：发现异常后台流量，点击查看
//	 * 提示频率：满足条件则弹出
//	 * 点击消息响应：进入后台偷跑流量详情界面
//	 */
//	private void checkBackgroundFlow(ConfigData configData,NetModel netModel){
//		if(configData == null || 
//				netModel == null || 
//				!configData.getBackgroundTrafficTipsSwitch()){
//			return ;
//		}
//		
//		int mobileflow = (int)(netModel.getBackgroundTotalMoblieFlow()/(1024*1024));
//		if(mobileflow < 5){
//			MySharedPref.clearBackgroundFlowNotifyData(context);
//			return ;
//		}
//		
//		NotificationData notifyData = MySharedPref.getBackgroundFlowNotifyData(context);
//		DateData lastNotifyDate= notifyData==null?null:notifyData.getLastNotifyTime();
//		
//		if(!TimeUtils.isTheDayInThisMonthly(configData.getMonthEndDate(), lastNotifyDate)){
//			notifyData.setAlreadyNotifyTimes(0);//表示之前提示过的是5mb的多少倍	
//		}
//		
//		if(mobileflow/5<=notifyData.getAlreadyNotifyTimes()){//表示之前提示过的是5mb的多少倍	
//			return ;
//		}
//				
//		MyNotification.notify(NOTIFY_ID_OF_BackgroundFlow,
//				context,
//				new ComponentName("com.aurora.secure",
//						"com.secure.activity.BackgroundNetManageActivity"),
//				false,
//				R.drawable.ic_launcher,
//				context.getString(R.string.flow_notify),
//				context.getString(R.string.background_flow_notify));
//			
//		if(lastNotifyDate == null){
//			lastNotifyDate = new DateData();
//		}
//		lastNotifyDate.setYear(TimeUtils.getCurYear());
//		lastNotifyDate.setMonth(TimeUtils.getCurMonth());
//		lastNotifyDate.setDay(TimeUtils.getCurDay());
//		
//		if(notifyData == null){
//			notifyData = new NotificationData();
//		}
//		notifyData.setAlreadyNotifyTimes(mobileflow/5);	//表示之前提示过的是5mb的多少倍	
//		notifyData.setLastNotifyTime(lastNotifyDate);	
//		MySharedPref.saveBackgroundFlowNotifyData(context, notifyData);
//	}
	
	/**
	 * 超额提示
	 * @param configData
	 * @param netModel
	 * 满足以下提示条件，将以系统中断式提示框弹出提示
	 * 提示条件：用户当月流量使用已达到套餐值95%并发起联网请求时
	 * 提示语：本月使用流量即将超过套餐值，继续使用会产生额外费用，是否关闭数据连接？
	 * 提示频率：每月一次
	 */
	private void checkExcess(ConfigData configData,NetModel netModel){
		if(context == null ||
				configData == null || 
				netModel == null || 
				!configData.isSetedFlowPackage() || 
				NetworkUtils.getNetState(context) != NetworkUtils.NET_TYPE_MOBILE){
			return ;
		}
		
		NotificationData notifyData = MySharedPref.getExcessNotifyData(context);
		DateData lastNotifyTime = notifyData==null?null:notifyData.getLastNotifyTime();
		if(TimeUtils.isTheDayInThisMonthly(configData.getMonthEndDate(), lastNotifyTime)){
			return ;
		}
		
		float useMobileflow = getUseFlowUnitMb(configData,netModel);
		if(useMobileflow < configData.getMonthlyFlow()*95.0/100){
			return ;
		}
		
		Intent intent = new Intent();
	 	intent.setClass(context, FlowExcessHintActivity.class);
	 	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	 	context.startActivity(intent); 	
		
		if(lastNotifyTime == null){
			lastNotifyTime = new DateData();
		}
		lastNotifyTime.setYear(TimeUtils.getCurYear());
		lastNotifyTime.setMonth(TimeUtils.getCurMonth());
		lastNotifyTime.setDay(TimeUtils.getCurDay());
		
		if(notifyData == null){
			notifyData = new NotificationData();
		}
		notifyData.setAlreadyNotifyTimes(1);		
		notifyData.setLastNotifyTime(lastNotifyTime);	
		MySharedPref.saveExcessNotifyData(context, notifyData);
	}
		
	public static void releaseObject(){
		if(instance != null){
			instance.context = null;
		}
		instance = null;
	}
}

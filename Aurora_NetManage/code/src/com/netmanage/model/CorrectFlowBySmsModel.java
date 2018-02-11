package com.netmanage.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.CorrectionDataInfo;
import tmsdk.bg.module.network.ITrafficCorrectionListener;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import tmsdk.common.ErrorCode;
import tmsdk.common.IDualPhoneInfoFetcher;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;

import com.aurora.netmanage.R;
import com.netmanage.data.AutoCorrectInfo;
import com.netmanage.data.ConfigData;
import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.data.CorrectionConfig;
import com.netmanage.data.MyArrayList;
import com.netmanage.interfaces.AutoFlowCorrectCallBack;
import com.netmanage.interfaces.AutoFlowCorrectCallBack.STEP;
import com.netmanage.interfaces.SendMsgCallBack;
import com.netmanage.utils.AlarmUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;

/**
 * 通过发送短信来校正流量
 * @author chengrq
 */
public class CorrectFlowBySmsModel implements SendMsgCallBack{
	private static CorrectFlowBySmsModel instance;
	private static final long Alert_SPACE = 1*60*1000;
	private final int MSG_correctFlow= 1;
	private final int MSG_dealMsg = 2;   
	private final int MSG_dealMsgFromSqlite = 3;
	private final String TAG = CorrectFlowBySmsModel.class.getName();
	
	private Context context;
	private CorrectFlowBySmsData correctFlowBySmsData;
    private TrafficCorrectionManager mTcMgr;
    private SendMsgModel sendMsgModel;
    private String blockMsgNum;//需要拦截的短信号码
    private String mQueryCode;
    private String mQueryPort;
    
	private HandlerThread mBackgroundThread;
	private BackgroundHandler mBackgroundHandler;  
	private ConfigData configData;
	private MyArrayList<AutoFlowCorrectCallBack> observers = new MyArrayList<AutoFlowCorrectCallBack>();	
	private Object locked = new Object();
	private boolean isDuringCorrect = false;//当前是否正在校正
	private AutoCorrectInfo autoCorrectInfo;
	
	//自动校正流量的记录
//	private HashMap<String, ArrayList<CorrectionDataInfo>> autoCorrectRecord ;
		
	private AlarmManager alarmManager;
	private MyAlarmReceiver alarmReceiver=null;
	private PendingIntent pendingIntent = null;
	private String ALARM_RECEIVER_ACTION = TAG+"ALARM_RECEIVER_ACTION";
    
    private CorrectFlowBySmsModel(){}
	private CorrectFlowBySmsModel(Context context) {
		this();
		this.context = context.getApplicationContext();
		
        mBackgroundThread = new HandlerThread(TAG+":Background");
        mBackgroundThread.start();
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());    
        
		mTcMgr = ManagerCreatorB.getManager(TrafficCorrectionManager.class);
		correctFlowBySmsData = CorrectFlowBySmsData.getInstance(this.context);
		sendMsgModel = new SendMsgModel(this.context,this);
//		autoCorrectRecord = new HashMap<String, ArrayList<CorrectionDataInfo>>();
		configData = ConfigModel.getInstance(context).getConfigData(); 
		alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
		registerReceiver();
	}

	/**
	 * 如果instance为null，则会创建一个
	 * @param context
	 * @return 返回值不可能为null
	 */
	public static synchronized CorrectFlowBySmsModel getInstance(Context context) {
		if (instance == null) {
			instance = new CorrectFlowBySmsModel(context);
		}
		return instance;
	}
	
	/**
	 * 如果instance为null，注意不会创建一个
	 * @return 可能为null
	 */
	public static synchronized CorrectFlowBySmsModel getInstance(){
		return instance;
	}
	
	/**
     * 注册观察者对象
     * @param observer
     */
    public void attach(AutoFlowCorrectCallBack CallBack){
    	if(CallBack == null){
    		return ;
    	}
    	for(int i=0;i<observers.size();i++){
    		AutoFlowCorrectCallBack tmp = observers.get(i);
    		if(tmp == CallBack){
    			return ;
    		}
    	}
    	observers.add(CallBack);
    }
    
    /**
     * 删除观察者对象
     * @param observer
     */
    public void detach(AutoFlowCorrectCallBack CallBack){
   	    observers.remove(CallBack);
    }
    
    private void notifyCallBack(AutoCorrectInfo autoCorrectInfo){
    	for(int i=0;i<observers.size();i++){
    		AutoFlowCorrectCallBack CallBack = observers.get(i);
    		if(CallBack != null){
    			CallBack.autoFlowCorrectCallBack(autoCorrectInfo);
    		}
    	}
    }
	
	public TrafficCorrectionManager getTcMgr(){
		return mTcMgr;
	}
	
	/**
	 * 获取记录当前自动校正状态的数据
	 * @return
	 */
	public AutoCorrectInfo getAutoCorrectInfo(){
		if(isDuringCorrect){
			return this.autoCorrectInfo;
		}else{
			return null;
		}		
	}
	
	/**
	 * 开始自动校正流量,用于定时自动校正
	 */
	public void startAutoCorrectFlow(){
		if(Utils.getImsi(context) == null){
			//没有可上网的sim卡
			return ;
		}
		CodeName province = correctFlowBySmsData.getProvince();
		CodeName city = correctFlowBySmsData.getCity();
		CodeName carry = correctFlowBySmsData.getCarry();
		CodeName brand = correctFlowBySmsData.getBrand();
		
		if(province == null && city == null){
			return ;
		}else if(carry == null){
			return ;
		}else if(brand == null){
			return ;
		}else{
			CorrectionConfig mConfig = new CorrectionConfig();
			mConfig.mSimIndex = Utils.getDataEnabledSimCard(context);
			mConfig.mProvinceId = province.mCode;
			mConfig.mCityId = city.mCode;
			mConfig.mCarryId = carry.mCode;
			mConfig.mBrandId = brand.mCode;
			mConfig.mClosingDay = ConfigModel.getInstance(context).getConfigData().getMonthEndDate();
			startAutoCorrectFlow(mConfig);			
		}	
	}
	
	/**
	 * 开始自动校正流量，用于手动点击校正
	 * @param mConfig
	 * @return
	 */
	public void startAutoCorrectFlow(CorrectionConfig mConfig){
		synchronized (locked) {
			if(isDuringCorrect){
				return ;
			}			
			isDuringCorrect = true;
		}		
		Message msg = mBackgroundHandler.obtainMessage();
		msg.what = MSG_correctFlow;
		msg.obj = mConfig;
		mBackgroundHandler.sendMessage(msg);
	}
	
	/**
	 * 处理短信(从短信广播中截获的短信)
	 * @param intent
	 */
    public void dealSms(Intent intent){
    	synchronized (locked) {
			if(!isDuringCorrect){
				return ;
			}
		}
    	if(blockMsgNum == null){//需要拦截的号码为null，表示此时不需要拦截短信
    		return ;
    	}
    	Log.i(TAG, "dealSms");
    	Message msg = mBackgroundHandler.obtainMessage();
		msg.what = MSG_dealMsg;
		msg.obj = intent;
		mBackgroundHandler.sendMessage(msg);	
    }
    
    /**
     * 处理短信(从短信数据库中获取的短信)
     * @param address
     * @param body
     */
    public void dealSmsFromSqlite(String address,String body){
    	synchronized (locked) {
			if(!isDuringCorrect){
				return ;
			}
		}
    	
    	if(blockMsgNum == null || //需要拦截的号码为null，表示此时不需要拦截短信
    			address == null || 
    			body == null){
    		return ;
    	}
    	if(!address.equals(blockMsgNum)){
    		return ;
    	}
    	Log.i(TAG, "dealSmsFromSqlite");
    	Message msg = mBackgroundHandler.obtainMessage();
		msg.what = MSG_dealMsgFromSqlite;
		msg.obj = body;
		mBackgroundHandler.sendMessage(msg);	  	
    }
    
    final class BackgroundHandler extends Handler {
        public BackgroundHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {       	
        	switch(msg.what){
        	case MSG_correctFlow:
        		correctFlowForThread((CorrectionConfig)msg.obj);
        		break;
        	case MSG_dealMsg:
        		dealMsgIntentForThread((Intent)msg.obj);
        		break;
        	case MSG_dealMsgFromSqlite:
        		dealMsgBodyForThread((String)msg.obj);
        		break;
        	}
	    };
    }
    
    /**
     * 作用子线程，通过短信校正流量
     * @param mConfig
     */
    private void correctFlowForThread(CorrectionConfig mConfig){
    	
    	mTcMgr.setTrafficCorrectionListener(iTrafficCorrectionListener);
    	
    	Log.i(TAG, "correctFlowForThread() setConfig: " +
    			"mSimIndex: " + mConfig.mSimIndex + 
    			" mProvinceId: " + mConfig.mProvinceId + 
    			" mCityId: " + mConfig.mCityId + 
    			" mCarryId: " + mConfig.mCarryId + 
    			" mBrandId: " + mConfig.mBrandId + 
    			" mClosingDay: " + mConfig.mClosingDay);
    	
    	int result = mTcMgr.setConfig(
    			mConfig.mSimIndex,
    			mConfig.mProvinceId,
    			mConfig.mCityId,
    			mConfig.mCarryId,
    			mConfig.mBrandId,
				mConfig.mClosingDay);//保存配置。在进行流量校正之前，必要进行设置。返回ErrorCode
    	
    	Log.i(TAG, "setConfig result: " + result);
    	
		if (result != ErrorCode.ERR_NONE) {
			Log.i(TAG, "set config error : " + result);
			
			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END,
     				context.getString(R.string.auto_correction_fail));
			
			return;
		}
		
		int retCode = mTcMgr.startCorrection(mConfig.mSimIndex);
		
		Log.i(TAG, "startCorrection result : " + retCode);
		
		if (retCode != ErrorCode.ERR_NONE) {
			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.BEGIN,
					context.getString(R.string.auto_correction_fail));
			Log.i(TAG, "set config error : " + retCode);
			Log.i(TAG, "卡" + mConfig.mSimIndex + "校正出错终止");
		} else {
			runAutoCorrectCallBack(true,AutoFlowCorrectCallBack.STEP.BEGIN,
					context.getString(R.string.begin_correction_wait));
		}
    	
    	
//    	SendMsgModel tmpSendMsgModel = sendMsgModel;
//		if(mConfig == null || tmpSendMsgModel == null){
//			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.BEGIN,
//				context.getString(R.string.auto_correction_fail));
//			return ;
//		}
		
//		int errorCode = mTcMgr.setConfig(mConfig);
//		if(errorCode != ErrorCode.ERR_NONE) {
//			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.BEGIN,
//					context.getString(R.string.auto_correction_fail));
//			Log.i(TAG, "set config error : "+errorCode);
//		}else{
//			runAutoCorrectCallBack(true,AutoFlowCorrectCallBack.STEP.BEGIN,
//					context.getString(R.string.begin_correction_wait));
//			
//			ArrayList<CorrectionDataInfo> infoList = new ArrayList<CorrectionDataInfo>();
//			mTcMgr.startCorrection(infoList);
//			if(infoList != null && infoList.size() >0){
//				synchronized (locked) {
//					blockMsgNum = null;
//				}
//				String key = ""+System.currentTimeMillis();
//				autoCorrectRecord.put(key, copyInfoList(infoList));
//				for (CorrectionDataInfo di : infoList) {
//					Log.i(TAG, "CorrectionDataInfo :" + di.getAddress()+","+di.getMessage());					
//					synchronized (locked) {
//						blockMsgNum = di.getAddress();
//					}
//					tmpSendMsgModel.sendSMS(key,di.getAddress(), di.getMessage());
//				}
//			}			
//		}
    }
    
    /**
     * 必须要copy一份InfoList，因为后面的代码回去删除list中的元素。
     * @param infoList
     * @return
     */
    private ArrayList<CorrectionDataInfo> copyInfoList(ArrayList<CorrectionDataInfo> infoList){
    	ArrayList<CorrectionDataInfo> copyInfoList = new ArrayList<CorrectionDataInfo>();
    	for (CorrectionDataInfo di : infoList) {
    		copyInfoList.add(di);
		}
    	return copyInfoList;
    }
    
    /**
     * 处理从信息广播获取的短信信息
     * @param intent
     */
    private void dealMsgIntentForThread(Intent intent){
    	synchronized (locked) {
			if(!isDuringCorrect){
				return ;
			}
		}
    	
    	Bundle bundle = null;
    	if(blockMsgNum == null ||
    			intent == null || 
    			(bundle = intent.getExtras()) == null){
    		return ;
    	}
    	
		String sender = null;
		String msgStr = null;

		try{
			StringBuffer msgBuf = new StringBuffer();
			Object[] pdus = (Object[]) bundle.get("pdus");
			SmsMessage[] mges = new SmsMessage[pdus.length];
			
			for (int i = 0; i < pdus.length; i++) {
				mges[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
			}
			if(mges != null && mges.length>0){
				sender = mges[0].getDisplayOriginatingAddress();
			}
			if(sender != null && sender.equals(blockMsgNum)){
				for (SmsMessage mge : mges) {														
					msgBuf.append(mge.getMessageBody());							
				}
				msgStr = msgBuf.toString();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(sender == null || msgStr == null){
			return ;
		}			
		
		Log.i(TAG, "dealMsgIntentForThread() msgStr: " + msgStr);
		
		dealMsgBodyForThread(msgStr);
    }
    
    /**
     * 处理短信信息内容
     * @param intent
     */
    private void dealMsgBodyForThread(String body){
    	synchronized (locked) {
			if(!isDuringCorrect){
				return ;
			}
		}
    	
    	if(blockMsgNum == null){
    		return ;
    	}
    	
		Log.i(TAG, "短信来自：" + blockMsgNum);
		Log.i(TAG, "短信内容：" + body);
		Log.i(TAG, "dealMsgBodyForThread card index: " + Utils.getDataEnabledSimCard(context));
		
		int result = mTcMgr.analysisSMS(Utils.getDataEnabledSimCard(context), mQueryCode, mQueryPort, body);
		Log.i(TAG, "analysisSMS result: " + result);
    	
		if (result != ErrorCode.ERR_NONE) {
			Log.i(TAG, "analysisSMS error : " + result);
			
			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END,
     				context.getString(R.string.auto_correction_fail));
			
			return;
		}
				   	
//		TrafficCorrectionResult result = new TrafficCorrectionResult();
//		int err = mTcMgr.getCorrectionResult(result, blockMsgNum, body);
//		if(err == ErrorCode.ERR_NONE){
//			double scale = 1024.0;
//			if(configData.isSetedFlowPackage()){
//				if(result.mLeftTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){
//					long totalFlow = configData.getMonthlyFlow();	
//					doCorrectSucess(Math.round(totalFlow-result.mLeftTrafficInKb/scale));
//				}else if(result.mUsedTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){				
//					doCorrectSucess(Math.round(result.mUsedTrafficInKb/scale));
//				}			
//			}else{
//				if(result.mUsedTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){				
//					doCorrectSucess(Math.round(result.mUsedTrafficInKb/scale));
//				}
//			}	
//			Log.i(TAG, "流量自动校验成功，"+"剩余："+
//		            result.mLeftTrafficInKb + "Kb, 使用：" + 
//					result.mUsedTrafficInKb + "Kb, err=" + err);
//		}else{
//			Log.i(TAG, "流量自动校验失败，"+"err=" + err);
//		}
    }
    
    /**
     * 校正成功，写入数据
     * @param useFlowbyte
     */
    private void doCorrectSucess(long useFlowMB){
    	disableAlarm(context);
    	CorrectFlowModel.getInstance(context).CorrectFlow(
				TimeUtils.getCurrentTimeMillis(), 
				useFlowMB);			
		stopAutoCorrectFlow(true,AutoFlowCorrectCallBack.STEP.END,
				context.getString(R.string.auto_correction_sucess));
    }
    
	/**
	 * 终止或结束流量自动校正
	 * @param isSucess
	 * @param step
	 * @param hintMsg
	 */
	private void stopAutoCorrectFlow(boolean isSucess,STEP step,String hintMsg){
		runAutoCorrectCallBack(isSucess, step, hintMsg);
		synchronized (locked) {
			isDuringCorrect = false;
			blockMsgNum = null;
		}
	}
	
	private void runAutoCorrectCallBack(boolean isSucess,STEP step,String hintMsg){
		if(isDuringCorrect){
			autoCorrectInfo = new AutoCorrectInfo(isSucess, step, hintMsg);
			notifyCallBack(autoCorrectInfo);
		}	
	}
    
    @Override
	public void result(String key,String num, String msg, boolean isSucess) {
    	if(key == null || num == null || msg == null){
    		return ;
    	}
//    	ArrayList<CorrectionDataInfo> info = autoCorrectRecord.get(key);
//    	if(info == null){
//    		return ;
//    	}
    	
    	if(isSucess){
//    		for(int i =0;i<info.size();i++){
//    			CorrectionDataInfo di = info.get(i);
//    			if(di == null){
//    				continue;
//    			}
//    			if(num.equals(di.getAddress()) && 
//    					msg.equals(di.getMessage())){
//    				info.remove(di);
//    			}
//    		}
//    		if(info.size() == 0){
//    			autoCorrectRecord.remove(key);
    			if(!SendMsgModel.isSendMsgByAuroraWay){
    				runAutoCorrectCallBack(true,AutoFlowCorrectCallBack.STEP.SEND_MSG,
        					context.getString(R.string.msg_sended_wait));
    			}   			
    			setNextAlert(context);
//    		}
    	}else{
//    		autoCorrectRecord.remove(key);
    		stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.SEND_MSG,
					context.getString(R.string.msg_sended_error));
    	}		
	} 
    
    /**
     * 开启定时器，如果在指定的时间内，流量校正没有成功，我就认为此时校正失败
     * @param context
     */
    private void setNextAlert(Context context){
		if(context == null || alarmManager == null){
			 return ;
		}	 
		Calendar calendar = Calendar.getInstance();	       
        long alertTime = calendar.getTimeInMillis()+Alert_SPACE;		 
        Intent intent = new Intent(ALARM_RECEIVER_ACTION);
        pendingIntent = PendingIntent.getBroadcast(context, 
       		 0,intent,PendingIntent.FLAG_CANCEL_CURRENT);        
        alarmManager.set(AlarmManager.RTC_WAKEUP, alertTime, pendingIntent);
        Log.i(TAG,"set Alarm time "+AlarmUtils.sdf.format(alertTime));
	}
    
    /**
     * 取消定时广播
     * @param context
     */
    private void disableAlarm(Context context){
    	if(context == null || pendingIntent == null || alarmManager == null){
			 return ;
		}
    	alarmManager.cancel(pendingIntent);
        Log.i(TAG,"cancel Alarm ");
	}
    
    class MyAlarmReceiver extends BroadcastReceiver {    	
    	public void onReceive(Context context, Intent intent) {
    		Log.i(TAG, "MyAlarmReceiver , onReceive isDuringCorrect="+isDuringCorrect);
	         if(isDuringCorrect){
	        	 stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END,
	     				context.getString(R.string.auto_correction_fail));
	         }
    	}
    }

	private void registerReceiver(){
		alarmReceiver = new MyAlarmReceiver();		
        IntentFilter filter = new IntentFilter();
		filter.addAction(ALARM_RECEIVER_ACTION);
		this.context.registerReceiver(alarmReceiver, filter);
	}
	
	private void unRegisterReceiver(){
		if(alarmReceiver != null){
			try{
				this.context.unregisterReceiver(alarmReceiver);
			}catch(Exception e){
				e.printStackTrace();
			}			
		}		
	}
	
	public static void releaseObject(){
		if(instance != null){
			if(instance.sendMsgModel != null){
				instance.sendMsgModel.destroy();
				instance.sendMsgModel = null;
			}
//			if(instance.autoCorrectRecord != null){
//				instance.autoCorrectRecord.clear();
//			}
			instance.unRegisterReceiver();
		}
		instance = null;
	}
	
	private ITrafficCorrectionListener iTrafficCorrectionListener = new ITrafficCorrectionListener() {
		
		@Override
		public void onTrafficInfoNotify(int simIndex, int trafficClass,
				int subClass, int kBytes) {
			Log.i(TAG, "onTrafficInfoNotify() simIndex: " + simIndex + " trafficClass: " + trafficClass + 
					" subClass: " + subClass + " kBytes: " + kBytes);
			
			double scale = 1024.0;
			
			// subClass 流量子类， 剩余流量为TSC_LeftKByte，已用流量为TSC_UsedKBytes
			if (subClass == ITrafficCorrectionListener.TSC_LeftKByte) {
				Log.i(TAG, "剩余流量为: " + kBytes);
				
				if (configData.isSetedFlowPackage()) {
					long totalFlow = configData.getMonthlyFlow();	
					doCorrectSucess(Math.round(totalFlow - kBytes/scale));
				}
				
			} else if (subClass == ITrafficCorrectionListener.TSC_UsedKBytes) {
				Log.i(TAG, "已用流量为: " + kBytes);
				
				doCorrectSucess(Math.round(kBytes/scale));
				
			} else if (subClass == ITrafficCorrectionListener.TSC_TotalKBytes) {
				Log.i(TAG, "总流量: " + kBytes);
				
				stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END, "");
			} else {
				Log.i(TAG, "返回的流量信息不为剩余或者已用");
				Log.i(TAG, "kBytes: " + kBytes);

				stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END,
	     				context.getString(R.string.auto_correction_fail));
			}
			
			mTcMgr.setTrafficCorrectionListener(null);
		}
		
		@Override
		public void onNeedSmsCorrection(int simIndex, String queryCode,
				String queryPort) {
			Log.i(TAG, "onNeedSmsCorrection() simIndex: " + simIndex + " queryCode: " + queryCode + 
					" queryPort: " + queryPort);
			
			blockMsgNum = queryPort;
			mQueryCode = queryCode;
			mQueryPort = queryPort;
			
			SendMsgModel tmpSendMsgModel = sendMsgModel;
			String key = "" + System.currentTimeMillis();
			tmpSendMsgModel.sendSMS(key, queryPort, queryCode);
		}
		
		@Override
		public void onError(int simIndex, int errorCode) {
			Log.i(TAG, "onError() simIndex: " + simIndex + " errorCode: " + errorCode);
			
			stopAutoCorrectFlow(false,AutoFlowCorrectCallBack.STEP.END,
     				context.getString(R.string.auto_correction_fail));
			
			mTcMgr.setTrafficCorrectionListener(null);
		}
	};
	
}

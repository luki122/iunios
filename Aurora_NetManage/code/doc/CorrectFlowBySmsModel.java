package com.netmanage.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.netmanage.data.CorrectFlowBySmsData;
import com.netmanage.interfaces.SendMsgCallBack;
import com.netmanage.model.SendMsgModel.BackgroundHandler;
import com.netmanage.utils.NetworkUtils;
import com.netmanage.utils.TimeUtils;
import com.netmanage.utils.Utils;

import tmsdk.bg.creator.ManagerCreatorB;
import tmsdk.bg.module.network.CodeName;
import tmsdk.bg.module.network.CorrectionDataInfo;
import tmsdk.bg.module.network.TrafficCorrectionConfig;
import tmsdk.bg.module.network.TrafficCorrectionManager;
import tmsdk.bg.module.network.TrafficCorrectionResult;
import tmsdk.common.ErrorCode;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import com.aurora.netmanage.R;
import com.netmanage.data.ConfigData;

/**
 * 通过发送短信来校正流量
 * @author chengrq
 */
public class CorrectFlowBySmsModel implements SendMsgCallBack{
	private static CorrectFlowBySmsModel instance;
	private final int MSG_correctFlow= 1;
	private final int MSG_dealMsg = 2;   
	private final int MSG_dealMsgFromSqlite = 3;
	private final String TAG = CorrectFlowBySmsModel.class.getName();
	
	private Context context;
	private CorrectFlowBySmsData correctFlowBySmsData;
    private TrafficCorrectionManager mTcMgr;
    private SendMsgModel sendMsgModel;
    private String blockMsgNum;//需要拦截的短信号码
    
	private HandlerThread mBackgroundThread;
	private BackgroundHandler mBackgroundHandler;  
	private UIHandler mUIhandler;
	private ConfigData configData;
	
	//自动校正流量的记录
	private HashMap<String, ArrayList<CorrectionDataInfo>> autoCorrectRecord ;
    
    private CorrectFlowBySmsModel(){}
	private CorrectFlowBySmsModel(Context context) {
		this();
		this.context = context.getApplicationContext();
		
        mBackgroundThread = new HandlerThread(TAG+":Background");
        mBackgroundThread.start();
        mUIhandler = new UIHandler(Looper.getMainLooper());
        mBackgroundHandler = new BackgroundHandler(mBackgroundThread.getLooper());    
        
		mTcMgr = ManagerCreatorB.getManager(TrafficCorrectionManager.class);
		correctFlowBySmsData = CorrectFlowBySmsData.getInstance(this.context);
		sendMsgModel = new SendMsgModel(this.context,this);
		autoCorrectRecord = new HashMap<String, ArrayList<CorrectionDataInfo>>();
		configData = ConfigModel.getInstance(context).getConfigData(); 
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
	
	public TrafficCorrectionManager getTcMgr(){
		return mTcMgr;
	}
	
	/**
	 * 开始自动校正流量
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
			TrafficCorrectionConfig mConfig = new TrafficCorrectionConfig();
			mConfig.mProvinceId = province.mCode;
			mConfig.mCityId = city.mCode;
			mConfig.mCarryId = carry.mCode;
			mConfig.mBrandId = brand.mCode;
			startAutoCorrectFlow(mConfig);			
		}	
	}
	
	/**
	 * 开始自动校正流量
	 * @param mConfig
	 * @return
	 */
	public void startAutoCorrectFlow(TrafficCorrectionConfig mConfig){
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
    	if(blockMsgNum == null){//需要拦截的号码为null，表示此时不需要拦截短信
    		return ;
    	}
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
    	if(blockMsgNum == null || //需要拦截的号码为null，表示此时不需要拦截短信
    			address == null || 
    			body == null){
    		return ;
    	}
    	if(!address.equals(blockMsgNum)){
    		return ;
    	}
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
        		correctFlowForThread((TrafficCorrectionConfig)msg.obj);
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
    private void correctFlowForThread(TrafficCorrectionConfig mConfig){
		if(mConfig == null){
			showHintText(R.string.auto_correction_fail);
			return ;
		}
		
		SendMsgModel tmpSendMsgModel = sendMsgModel;
		if(tmpSendMsgModel == null){
			showHintText(R.string.auto_correction_fail);
			return ;
		}
		
		if(!NetworkUtils.isConn(context)){
			showHintText(R.string.net_work_error);
			return ;
		}
		
		int errorCode = mTcMgr.setConfig(mConfig);
		if(errorCode != ErrorCode.ERR_NONE) {
			showHintText(R.string.auto_correction_fail);
			Log.i(TAG, "set config error : "+errorCode);
		}else{
			ArrayList<CorrectionDataInfo> infoList = new ArrayList<CorrectionDataInfo>();
			mTcMgr.startCorrection(infoList);
			if(infoList != null && infoList.size() >0){
				resetData();
				String key = ""+System.currentTimeMillis();
				autoCorrectRecord.put(key, infoList);
				for (CorrectionDataInfo di : infoList) {
					Log.i(TAG, "CorrectionDataInfo :" + di.getAddress()+","+di.getMessage());
					blockMsgNum = di.getAddress();
					tmpSendMsgModel.sendSMS(key,di.getAddress(), di.getMessage());
				}
			}			
		}
    }
    
    /**
     * 处理从信息广播获取的短信信息
     * @param intent
     */
    private void dealMsgIntentForThread(Intent intent){
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
				Log.i(TAG,"短信来自："+sender);
				Log.i(TAG,"短信内容："+msgStr);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(sender == null || msgStr == null){
			return ;
		}				  
		dealMsgBodyForThread(msgStr);
    }
    
    /**
     * 处理短信信息内容
     * @param intent
     */
    private void dealMsgBodyForThread(String body){
    	if(blockMsgNum == null){
    		return ;
    	}
				   	
		TrafficCorrectionResult result = new TrafficCorrectionResult();
		int err = mTcMgr.getCorrectionResult(result, blockMsgNum, body);
		if(err == ErrorCode.ERR_NONE){
			double scale = 1024.0;
			if(configData.isSetedFlowPackage()){
				if(result.mLeftTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){
					long totalFlow = configData.getMonthlyFlow();	
					doCorrectFlow(Math.round(totalFlow-result.mLeftTrafficInKb/scale));
				}else if(result.mUsedTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){				
					doCorrectFlow(Math.round(result.mUsedTrafficInKb/scale));
				}			
			}else{
				if(result.mUsedTrafficInKb != TrafficCorrectionResult.INVALID_TRAFFIC_DATA){				
					doCorrectFlow(Math.round(result.mUsedTrafficInKb/scale));
				}
			}	
			Log.i(TAG, "流量自动校验成功，"+"剩余："+
		            result.mLeftTrafficInKb + "Kb, 使用：" + 
					result.mUsedTrafficInKb + "Kb, err=" + err);
		}else{
			Log.i(TAG, "流量自动校验失败，"+"err=" + err);
		}
    }
    
    /**
     * 调用校正模块，写入数据
     * @param useFlowbyte
     */
    private void doCorrectFlow(long useFlowMB){
    	CorrectFlowModel.getInstance(context).CorrectFlow(
				TimeUtils.getCurrentTimeMillis(), 
				useFlowMB);
		resetData();				
		showHintText(R.string.auto_correction_sucess);		
    }
    
    private void resetData(){
    	blockMsgNum = null;
    }
    
    @Override
	public void result(String key,String num, String msg, boolean isSucess) {
    	if(key == null || num == null || msg == null){
    		return ;
    	}
    	ArrayList<CorrectionDataInfo> info = autoCorrectRecord.get(key);
    	if(info == null){
    		return ;
    	}
    	
    	if(isSucess){
    		for(int i =0;i<info.size();i++){
    			CorrectionDataInfo di = info.get(i);
    			if(di == null){
    				continue;
    			}
    			if(num.equals(di.getAddress()) && 
    					msg.equals(di.getMessage())){
    				info.remove(di);
    			}
    		}
    		if(info.size() == 0){
    			autoCorrectRecord.remove(key);
    			showHintText(R.string.msg_sended_wait);
    		}
    	}else{
    		autoCorrectRecord.remove(key);
			showHintText(R.string.auto_correction_fail);
    	}		
	}
    
    private void showHintText(int resId){
    	Message sendMsg = mUIhandler.obtainMessage();
		sendMsg.obj = context.getResources().getString(resId);
		mUIhandler.sendMessage(sendMsg);
    }
    
    final class UIHandler extends Handler{		
  		public UIHandler(Looper looper){
             super(looper);
         }
  		@Override
  	    public void handleMessage(Message msg) {  
  			String msgStr = null;
  			if(msg != null){
  				msgStr = (String)msg.obj;
  			}
  			if(msgStr != null){
  				Toast.makeText(context, msgStr, Toast.LENGTH_SHORT).show();
  			}           
  	    }
  	}
	
	public static void releaseObject(){
		if(instance != null){
			if(instance.sendMsgModel != null){
				instance.sendMsgModel.destroy();
				instance.sendMsgModel = null;
			}
			if(instance.autoCorrectRecord != null){
				instance.autoCorrectRecord.clear();
			}
		}
		instance = null;
	}
}

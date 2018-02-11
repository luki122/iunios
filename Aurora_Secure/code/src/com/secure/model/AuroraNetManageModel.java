package com.secure.model;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.netmanage.data.FlowData;
import com.netmanage.service.INetFlowService;
import com.netmanage.service.INetFlowServiceCallback;
import com.secure.interfaces.FlowChangeSubject;
import com.secure.utils.LogUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

public class AuroraNetManageModel extends FlowChangeSubject{	
	private final String TAG = AuroraNetManageModel.class.getName();
	private static AuroraNetManageModel instance;
	private final int MSG_flowChange = 1;
	private final int MSG_bindServiceCallBack = 2;
	private Context context;
	private INetFlowService iNetFlowService = null;
	private final HashMap<String ,FlowData> flowMap = new HashMap<String ,FlowData>();//应用所有流量列表		
	private final UIHandler mUIhandler;
	private final HandlerThread mQueueThread;
	private final QueueHandler mQueueHandler;
    private final Object mLock = new Object();
    private final AtomicBoolean isFlowInfoInited = new AtomicBoolean(false);
    private final Object mLockJustForBindService = new Object();
    private long flowBeginTime = 0;
    private boolean isSetedFlowPackage =false;
		
	public static synchronized AuroraNetManageModel getInstance(Context context) {
		if (instance == null) {
			instance = new AuroraNetManageModel(context);
		}
		return instance;
	}
	
	private AuroraNetManageModel(Context context) {
		this.context = context.getApplicationContext();	
		isFlowInfoInited.set(false);
		mUIhandler = new UIHandler(Looper.getMainLooper());	
        mQueueThread = new HandlerThread(TAG+":Background");
        mQueueThread.start();
        mQueueHandler = new QueueHandler(mQueueThread.getLooper()); 
	}
	
    /**
	 * 统计流量开始时间
	 * @return
	 */
    public long getFlowBeginTime(){
    	if(!isBindService()){
    		bindService(null);
    	}
    	return flowBeginTime;
    }
    
    /**
	 * 判断是否设置流量套餐
	 * @return true 已经设置套餐
	 *         false 没有设置套餐
	 */
	public boolean isSetedFlowPackage(){		
		if(!isBindService()){
    		bindService(null);
    	}
		return isSetedFlowPackage;
	}
	
	/**
     * 获取存储流量数据的map
     * @return
     */
    public HashMap<String ,FlowData> getFlowMap(){
    	if(!isBindService()){
    		bindService(null);
    	}
    	return this.flowMap;
    }
	
	/**
	 * 绑定服务
	 * @param bindCallback
	 */
	private void bindService(final BindServiceCallback bindCallback){	
		synchronized (mLock){
			mQueueHandler.removeMessages(MSG_bindServiceCallBack);
			
			Message msg = mQueueHandler.obtainMessage();
			msg.what = MSG_bindServiceCallBack;
			msg.obj = bindCallback;
			mQueueHandler.sendMessage(msg);	
		}		
    }
	
   final class QueueHandler extends Handler {
        public QueueHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
				case MSG_flowChange:
	 				break;
				case MSG_bindServiceCallBack:
					bindServiceFunc((BindServiceCallback)msg.obj);
					break;
	        }
	    };
    }
    
	/**
	 * 绑定服务
	 */
	public void bindServiceFunc(final BindServiceCallback bindCallback){	
		try{
			Intent intent = new Intent(INetFlowService.class.getName());
			if(!isLowVersion())
			{
				intent = createExplicitFromImplicitIntent(context, intent);
			}
			boolean reslut = context.bindService(
				intent,connection, 
					Context.BIND_AUTO_CREATE);
			if(reslut){
				synchronized (mLockJustForBindService) {
					if (iNetFlowService == null) {
						LogUtils.printWithLogCat(TAG,"bind wait");
						mLockJustForBindService.wait();
	                }
                }		
			}
			LogUtils.printWithLogCat(TAG,"bind result=" + reslut);
		}catch(Exception e){
			LogUtils.printWithLogCat(TAG,"bind result=" + e.toString());
			e.printStackTrace();
		}finally{					
			Message msg = mUIhandler.obtainMessage();
			msg.what = MSG_bindServiceCallBack;
			msg.obj = bindCallback;
			mUIhandler.sendMessage(msg); 
		} 	
	}
	   
	public static boolean isLowVersion() {
		int version = android.os.Build.VERSION.SDK_INT;
		if (version <= 20) {
			return true;
		}
		return false;
	}

	public static Intent createExplicitFromImplicitIntent(Context context,
			Intent implicitIntent) {
		// Retrieve all services that can match the given intent
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent,
				0);

		// Make sure only one match was found
		if (resolveInfo == null || resolveInfo.size() != 1) {
			return null;
		}
		// Get component info and create ComponentName
		ResolveInfo serviceInfo = resolveInfo.get(0);
		String packageName = serviceInfo.serviceInfo.packageName;
		String className = serviceInfo.serviceInfo.name;
		ComponentName component = new ComponentName(packageName, className);
		// Create a new intent. Use the old one for extras and such reuse
		Intent explicitIntent = new Intent(implicitIntent);
		// Set the component to be explicit
		explicitIntent.setComponent(component);
		return explicitIntent;
	}

	
	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) {  
 			switch (msg.what) {
 			case MSG_flowChange:
 				notifyObserversOfFlowChange(flowMap);
 				break;
 			case MSG_bindServiceCallBack:
 				BindServiceCallback bindCallback =(BindServiceCallback)msg.obj;
				if(bindCallback != null){
		    		bindCallback.callback(isBindService());
		    	}
 				break;
 	        }
 	    }
 	 }
    
	/**
	 * 是否绑定了服务
	 * @return true：绑定成功  false：绑定失败
	 */
	private boolean isBindService(){
		if(iNetFlowService == null){
			return false;
		}else{
			return true;
		}
	}
		
	private ServiceConnection connection = new ServiceConnection(){
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (mLockJustForBindService){
				iNetFlowService = INetFlowService.Stub.asInterface(service);
				mLockJustForBindService.notify();
				LogUtils.printWithLogCat(TAG,"notify");
		    }				
			try {
				if(iNetFlowService != null){
					iNetFlowService.registerCallback(iNetFlowServiceCallback);	
					iNetFlowService.getFlowData();
				}				
            } catch (RemoteException e) {
                e.printStackTrace();
            }
		}

		public void onServiceDisconnected(ComponentName name) {
			try {
				if(iNetFlowService != null){
					iNetFlowService.unregisterCallback(iNetFlowServiceCallback);	
				}			
            } catch (RemoteException e) {
                e.printStackTrace();
            }finally{
            	synchronized (mLockJustForBindService){
            		iNetFlowService = null;	
            		mLockJustForBindService.notify();
    				LogUtils.printWithLogCat(TAG,"notify");
            	}           
            }					
		}
	};
	
	private INetFlowServiceCallback iNetFlowServiceCallback  = new INetFlowServiceCallback.Stub(){
		public void valueChanged(List<FlowData> flowList) throws RemoteException {
			flowMap.clear();
			if(flowList != null && flowList.size()>0){				
				FlowData tmpFlowData = null;
				for(int i=0;i<flowList.size();i++){
					tmpFlowData = flowList.get(i);
					flowMap.put(tmpFlowData.getPackageName(), tmpFlowData);
					LogUtils.printWithLogCat(TAG,"pkgName="+tmpFlowData.getPackageName()+
							",total="+tmpFlowData.getTotalBytes());
				}			
			}		
			
			try{
				if(iNetFlowService != null){
					flowBeginTime = iNetFlowService.getFlowBeginTime();
					isSetedFlowPackage = iNetFlowService.isSetedFlowPackage();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
					
			isFlowInfoInited.set(true);
			
			Message msg = mUIhandler.obtainMessage();
			msg.what = MSG_flowChange;
			mUIhandler.sendMessage(msg); 
	}};
		
	/**
	 * 绑定服务的回调
	 */
	public interface BindServiceCallback {
		/**
		 * 是否绑定成功
		 * @param result  true：绑定成功  false：绑定失败
		 */
		public void callback(boolean result);
	}
	
	public static void relaseObject(){  	
    	if(instance != null){
    		instance.context = null;
    		instance.flowMap.clear();
    		instance = null;
    	}
	}
}

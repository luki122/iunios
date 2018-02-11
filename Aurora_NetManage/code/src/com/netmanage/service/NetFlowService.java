package com.netmanage.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.netmanage.data.ConfigData;
import com.netmanage.data.FlowData;
import com.netmanage.model.ConfigModel;
import com.netmanage.model.CorrectFlowModel;
import com.netmanage.model.NetModel;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

public class NetFlowService extends Service{
	private MyHandler netFlowUpdateHandler;
	private INetFlowServiceCallback mCallback;
	
    @Override
	public void onCreate() {
    	netFlowUpdateHandler = new MyHandler(Looper.getMainLooper());
    	NetModel.getInstance(this).attach(netFlowUpdateHandler);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		NetModel.getInstance(this).detach(netFlowUpdateHandler);
		super.onDestroy();
	}

	@Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    
	private class MyHandler extends Handler{		
		public MyHandler(Looper looper){
           super(looper);
        }

		@Override
	    public void handleMessage(Message msg) {
			if(mCallback == null){
				return ;
			}
			try {
				HashMap<String ,FlowData> flowMap = null;
		    	if(CorrectFlowModel.getInstance(NetFlowService.this).getCorrectFlow()<=0){
		    		flowMap = NetModel.getInstance(NetFlowService.this).getFlowMapFromCorrectTime();
		    	}else{
		    		flowMap = NetModel.getInstance(NetFlowService.this).getFlowMap();
		    	}

		    	List<FlowData> flowList = new ArrayList<FlowData>();
		    	if(flowMap != null && flowMap.size() > 0){
		    		synchronized(flowMap){
		    			Set<String> packageNames = flowMap.keySet();
		     		    for (String packageName : packageNames){
		     		    	FlowData flowData = flowMap.get(packageName);
		     		    	flowList.add(flowData);
		     		    } 
		    		} 
		    	}		    		    	
				mCallback.valueChanged(flowList);
			} catch (Exception e) {
				e.printStackTrace();
			}			
	    }
	}
    
    private final INetFlowService.Stub mBinder = new INetFlowService.Stub() {
		@Override
		public void unregisterCallback(INetFlowServiceCallback mCallback)
				throws RemoteException {
			NetFlowService.this.mCallback = null;			
		}

		@Override
		public void registerCallback(INetFlowServiceCallback mCallback)
				throws RemoteException {
			NetFlowService.this.mCallback = mCallback;			
		}

		@Override
		public void getFlowData() throws RemoteException {
			if(NetModel.getInstance(NetFlowService.this).isGetFlow()){
				netFlowUpdateHandler.sendEmptyMessage(0);
			}else{
				NetModel.getInstance(NetFlowService.this).resetNetInfo();
			}			
		}

		@Override
		public long getFlowBeginTime() throws RemoteException {
			ConfigData configData = ConfigModel.getInstance().getConfigData();
			if(configData != null){
				return configData.getFlowBeginTime(NetFlowService.this);
			}else{
				return 0;
			}			
		}

		@Override
		public boolean isSetedFlowPackage() throws RemoteException {
			ConfigData configData = ConfigModel.getInstance().getConfigData();
			if(configData != null){
				return configData.isSetedFlowPackage();
			}else{
				return false;
			}			
		}
    };
}

    
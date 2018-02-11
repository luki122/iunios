/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netmanage.model;

import com.netmanage.data.ConfigData;
import com.netmanage.utils.TimeUtils;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.net.INetworkStatsSession;
import android.net.NetworkStats;
import android.net.NetworkTemplate;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

public class SummaryForAllUidLoader extends AsyncTask<Void, Void, NetworkStats> {
    private static final String KEY_TEMPLATE = "template";

    private final INetworkStatsSession mSession;
    private final Bundle mArgs;
    private DataUsageSummary callObject;
	private NetworkStats forShowNetworkStats;//用于显示的流量数据
	private Context context;
	private final String TAG = "SummaryForAllUidLoader";

    public static Bundle buildArgs(NetworkTemplate template) {
        final Bundle args = new Bundle();
        args.putParcelable(KEY_TEMPLATE, template);
        return args;
    }

    public SummaryForAllUidLoader(Context context, 
    		INetworkStatsSession session, 
    		Bundle args,
    		DataUsageSummary callObject) {
        super();
        this.context = context;
        mSession = session;
        mArgs = args;
        this.callObject = callObject;
    }

    @Override
    public NetworkStats doInBackground(Void... params) {   	
        final NetworkTemplate template = mArgs.getParcelable(KEY_TEMPLATE);      
        long startTimeForShow = 0;
        long endTimeForShow = 0;
        
//        if(ConfigModel.getInstance() != null){
        	ConfigData configData = ConfigModel.getInstance(context).getConfigData();
        	startTimeForShow = configData.getFlowBeginTime(context);
//        }	        
        endTimeForShow = TimeUtils.getCurrentTimeMillis();
        Log.i(TAG,"startTimeForShow = "+TimeUtils.timeStampToData(startTimeForShow)+
        		", endTime="+TimeUtils.timeStampToData(endTimeForShow));   	
        try {
        	forShowNetworkStats = mSession.getSummaryForAllUid(template, startTimeForShow, 
        			endTimeForShow, false);
        } catch (RemoteException e) {
            //igone
        	e.printStackTrace();
        } catch (Exception e) {
			// TODO: handle exception
        	e.printStackTrace();
		}
        return null;
    }
	
    /**   
     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置  
     */  
    @Override  
    protected void onPostExecute(NetworkStats result) { 
    	if(callObject != null){
    		callObject.endFuncOfGetFlow(forShowNetworkStats);
    	}
    }    
}

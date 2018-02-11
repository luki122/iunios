package com.android.settings.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.android.settings.AuroraLightSensorService;

public class AuroraAutoChangeAp {
	
	private WifiManager.ActionListener mConnectListener=null;
	private static final int Interval_r=10;
	private static final int Interval_n=-10;
	private List<WifiConfiguration> configs=null;
	private List<ScanResult> results=null;
	private WifiManager mWifiManager =null;
	private Context mContext=null;
	private WifiInfo info=null;
	
	public AuroraAutoChangeAp(Context context)
	{
		mContext=context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		configs=mWifiManager.getConfiguredNetworks();
		results=mWifiManager.getScanResults();
		info=mWifiManager.getConnectionInfo();
		mConnectListener = new WifiManager.ActionListener() {
	        public void onSuccess() {
	        }
	        public void onFailure(int reason) {
	        }
	    };
	}
	
    // only two equel SSID 
	public void compareSSSIDLevel()    
	{
    	 String CurrentSSID=info.getSSID();
    	 String CurrentBSSID=info.getBSSID();
    	 final int CurrentLevel=info.getRssi();
		 if(!isWIFIConnection(mContext))
		 {
			 return;
		 }
	     if(configs!=null && results!=null)
	     {
	    	WifiSettings.PrintLog("pgd", " compareLevel2 ");
	    	WifiSettings.PrintLog("pgd", " CurrentSSID: "+CurrentSSID+ "   CurrentBSSID: "+CurrentBSSID);
	    	int current_result_i=-1,current_result_j=-1, listenor=0;   //序号
	    	for(int i=0;i <= results.size()-1;i++)
	    	{
//	    		WifiSettings.PrintLog("pgd", "i="+i+"result ii:"+results.get(i).SSID);
	    	    if(results.get(i).SSID.equals("") || results.get(i).SSID.contains("CMCC"))
	    	    {
	    	    	continue;
	    	    }
                for(int j=i+1;j<=results.size()-1;j++)
                {
                	if(results.get(i).SSID.equals(results.get(j).SSID))
                	{
//                		WifiSettings.PrintLog("pgd", " compareLevel3 : I;;"+ results.get(i).SSID + " j:: "+results.get(j).SSID);
                		current_result_i=i;
                		current_result_j=j;
                		listenor++;
                	}
                }
	    	}
	    	if(listenor>=2)
	    	{
	    		return;
	    	}
	    	WifiSettings.PrintLog("pgd", " current_result_i: "+current_result_i+ "   current_result_j: "+current_result_j+" |");
	    	if(current_result_i==-1 && current_result_j == -1 && current_result_i == current_result_j)
	    	{
	    		return;
	    	}
	    	int current_config_i=-1,current_config_j=-1;
	    	for(int i=0;i <= configs.size()-1;i++)
	    	{
                for(int j=i+1;j<=configs.size()-1;j++)
                {
                	if(configs.get(i).SSID.equals(configs.get(j).SSID))
                	{
//                		WifiSettings.PrintLog("pgd", " compareLevel4  : I;;"+ configs.get(i).SSID + "  bssid: "+configs.get(i).BSSID+ " j:: "+configs.get(j).SSID + "  bssid: "+configs.get(j).BSSID);
                		current_config_i=i;
                		current_config_j=j;
                	}
                }
	    	}
	    	if(current_config_i == -1 && current_config_j == -1 && current_config_i == current_config_j)
	    	{
	    		return;
	    	}
//	    	if(!(CurrentBSSID.equals(results.get(current_result_i).BSSID) || CurrentBSSID.equals(results.get(current_result_j).BSSID)))
//	    	{
//	    		WifiSettings.PrintLog("pgd", " compareLevel4.3 ");
//	    		return;
//	    	}
	    	if(!(results.get(current_result_i).BSSID.equals(configs.get(current_config_i).BSSID) || results.get(current_result_i).BSSID.equals(configs.get(current_config_j).BSSID)))
	    	{
	    		return;
	    	}
            /**如果存在则可以根据DB值进行相应的切换*/
            int configLocation = (CurrentBSSID.equals(configs.get(current_config_j).BSSID)) ? current_config_j : current_config_i;
            int resultLocation = (CurrentBSSID.equals(results.get(current_result_j).BSSID)) ? current_result_j : current_result_i;
            if(!(configLocation!=-1 && resultLocation!=-1))
            {
            	return ;
            }
            
//            WifiSettings.PrintLog("pgd", "  currentSSID: "+CurrentSSID + "  currentMac: "+CurrentBSSID + "   level: "+ CurrentLevel);
//            WifiSettings.PrintLog("pgd", "  current_result_i: " + current_result_i +"   "+ results.get(current_result_i).SSID + "  "+ results.get(current_result_i).BSSID  + "  level : "+  results.get(current_result_i).level);
//            WifiSettings.PrintLog("pgd", "  current_result_J: " + current_result_j +"   "+results.get(current_result_j).SSID + "  "+ results.get(current_result_j).BSSID  + "  level : "+  results.get(current_result_j).level);
//            WifiSettings.PrintLog("pgd", "  current_config_i: " + current_config_i +"   "+ configs.get(current_config_i).SSID + "  "+ configs.get(current_config_i).BSSID);
//            WifiSettings.PrintLog("pgd", "  current_config_J: " + current_config_j +"   "+ configs.get(current_config_j).SSID + "  "+ configs.get(current_config_j).BSSID);
            
            if(CurrentBSSID.equals(configs.get(current_config_j).BSSID))
            {
            	if(CurrentBSSID.equals(results.get(current_result_j).BSSID)) //configs.get(current_config_j和results.get(current_result_j)相等
            	{
            		if(results.get(current_result_i).level - results.get(current_result_j).level < (Interval_n))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel5.1 ");
            		}else if(results.get(current_result_i).level - results.get(current_result_j).level > (Interval_r))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel5.2  CONNECTION");
            			autoChangeConnect(configs.get(current_config_i));
            		}
            	}else if(CurrentBSSID.equals(results.get(current_result_i).BSSID))//configs.get(current_config_j)和results.get(current_result_i)相等
            	{
            		if(results.get(current_result_i).level - results.get(current_result_j).level < (Interval_n))// results.get(current_result_j).level
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel5.3  CONNECTION");
            			autoChangeConnect(configs.get(current_result_i));
            		}else if(results.get(current_result_i).level - results.get(current_result_j).level > (Interval_r)) // results.get(current_result_i).level
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel5.4 ");
            		}
            	}
            }else if(CurrentBSSID.equals(configs.get(current_config_i).BSSID))  
            {
                if(CurrentBSSID.equals(results.get(current_result_j).BSSID)) //configs.get(current_config_i).BSSID和results.get(current_result_j).BSSID相等
                {
            		if(results.get(current_result_i).level - results.get(current_result_j).level < (Interval_n))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel6.1 ");
            		}else if(results.get(current_result_i).level - results.get(current_result_j).level > (Interval_r))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel6.2 CONNECTION");
            			autoChangeConnect(configs.get(current_config_j));
            		}
                }else if(CurrentBSSID.equals(results.get(current_result_i).BSSID))//configs.get(current_config_i).BSSID和results.get(current_result_i).BSSID相等
                {
            		if(results.get(current_result_i).level - results.get(current_result_j).level < (Interval_n))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel6.3 CONNECTION");
            			autoChangeConnect(configs.get(current_config_j));
            		}else if(results.get(current_result_i).level - results.get(current_result_j).level > (Interval_r))
            		{
//            			WifiSettings.PrintLog("pgd", " compareLevel6.4 ");
            		}
                }
            }
	    }
	}
	
	
	
	private void autoChangeConnect(WifiConfiguration config)
	{
		 int networkId=config.networkId;
         mWifiManager.connect(networkId,mConnectListener);
	}
	
    public static boolean isWIFIConnection(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            int netWorkType = activeNetworkInfo.getType();
            if ((ConnectivityManager.TYPE_WIFI == netWorkType || ConnectivityManager.TYPE_WIMAX == netWorkType)) {
                return true;
            }
        }
        return false;
    }
	
	public void compareLevel()
	{
		final String CurrentBSSID=info.getBSSID();
		final int Currentlevel=info.getRssi();
		final List<Integer> resultLevelList=new ArrayList<Integer>();
		final Map<Integer,Integer> hashmap = new HashMap<Integer, Integer>();
		for(int i=0;i<=configs.size()-1;i++)
		{
			for(int j=0;j<=results.size()-1;j++)
			{
				if(results.get(j).BSSID.equals(configs.get(i).BSSID))
				{
				    final int level=results.get(j).level;
				    final int serial=i;
				    resultLevelList.add(results.get(j).level);
				    hashmap.put(results.get(j).level, serial);
				}
			}
		}
		int Max=resultLevelList.get(0);
		for(int level : resultLevelList)
		{
			if(Max<level)
			{
				Max=level;
			}
		}
		if(Max>Currentlevel)
		{
			autoChangeConnect(configs.get(hashmap.get(Max)));
		}
	}
	
}

package com.aurora.ota.reporter;

import android.net.TrafficStats;

public class CalculateSendByte {

    private TrafficStats mStats;
    
    private static int mUid = android.os.Process.myUid();
    
    public CalculateSendByte(){
        
    }
    
    private static long calculateNetByte(){
        return TrafficStats.getUidTxBytes(mUid);
    }
    
    private float calculateByte(){
        
        return 0.0f;
    }
    
}

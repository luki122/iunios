package com.aurora.ota.reporter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public interface NetFocade {
    public  boolean isNetworkAvailable() ;
    
    public  boolean isMobileNetwork() ;

    public  boolean isWIFIConnection();
    
    public long getMaxByteOverMobile();
    
    public long getMaxByteOverWifi();

}

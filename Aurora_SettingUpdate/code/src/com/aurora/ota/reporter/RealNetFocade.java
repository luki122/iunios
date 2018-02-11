package com.aurora.ota.reporter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class RealNetFocade implements NetFocade{

    private Context mContext;
    public RealNetFocade(Context context){
        this.mContext = context;
    }
    @Override
    public boolean isNetworkAvailable() {
        boolean flag = false;
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            flag = true;
        } else {
            flag = false;
        }

        return flag;
    }

    @Override
    public boolean isMobileNetwork() {
        ConnectivityManager manager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = manager.getActiveNetworkInfo();

        if (activeNetworkInfo != null) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE
                    && activeNetworkInfo.getState() == android.net.NetworkInfo.State.CONNECTED) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isWIFIConnection() {
        ConnectivityManager manager = (ConnectivityManager) mContext
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

    
    
    @Override
    public long getMaxByteOverMobile() {
        // TODO Auto-generated method stub
        return 80;
    }

    @Override
    public long getMaxByteOverWifi() {
        // TODO Auto-generated method stub
        return 80;
    }
    
    


}

package gn.com.android.update.business;


import gn.com.android.update.business.NetworkConfig.ConnectionType;

public class CheckVersionNeedInfo {
    public int mCheckType = NetworkConfig.CHECK_TYPE_DEFAULT;
    public ConnectionType mConnectionType = ConnectionType.CONNECTION_TYPE_IDLE;
    public int mPushId = Config.ERROR_PUSH_ID;
    public boolean mIsWapNetwork = false;
    public String mImei = "";
    public boolean mIsOtaCheck = true;
    public String mData = null;

}

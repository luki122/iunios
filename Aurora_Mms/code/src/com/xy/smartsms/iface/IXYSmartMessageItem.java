package com.xy.smartsms.iface;

import java.util.HashMap;

public interface IXYSmartMessageItem {

    public long getMsgId();
    public HashMap getSmartSmsExtendMap();
    public String getPhoneNum();
    public String getServiceCenterNum();
    public long getSmsReceiveTime();
    public boolean isSms();
    public String getSmsBody();
    
}

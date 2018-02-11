package com.android.settings.lscreen;

import java.util.List;


public interface LSAppObserver {
	
    public void initOrUpdateLSApp(LSAppSubject subject);
    
    public void addOrUpdateLSApp(LSAppSubject subject,List<AppInfo> datas);
    
    public void delOrUpdateLSApp(LSAppSubject subject,List<AppInfo> datas);
    
    public void allAppAchieve(LSAppSubject subject,List<AppInfo> datas);
    
}

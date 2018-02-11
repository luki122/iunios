package com.xy.smartsms.iface;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

public interface IXYSmartSmsHolder {

    public Activity getActivityContext();
    public boolean isEditAble();
    public ListView getListView();
    public boolean isScrolling();
    // Aurora xuyong 2016-01-28 modified for xy-smartsms start
    public View findViewById(int viewId, Object... obj);
    // Aurora xuyong 2016-01-28 modified for xy-smartsms end
    public void onXyUiSmsEvent(int eventType);
    public boolean isNotifyComposeMessage();
    public void setHasXyMenu(boolean need);
    // Aurora xuyong 2016-03-15 added for xy-smartsms start
    public boolean isBatchMode();
    public boolean isRejectMode();
    // Aurora xuyong 2016-03-15 added for xy-smartsms end
}

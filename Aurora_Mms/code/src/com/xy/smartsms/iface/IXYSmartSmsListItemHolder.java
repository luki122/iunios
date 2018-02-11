package com.xy.smartsms.iface;

import android.view.View;

public interface IXYSmartSmsListItemHolder {

   public void showDefaultListItem();
   // Aurora xuyong 2016-01-28 modified for xy-smartsms start
   public View findViewById(int viewId, Object... obj);
   // Aurora xuyong 2016-01-28 modified for xy-smartsms end
   public View getListItemView();
   public IXYSmartSmsHolder getXySmartSmsHolder();
   public int getShowBubbleMode();
   // Aurora xuyong 2016-02-01 added for xy-smartsms start
   public View getDefaultContent();
   // Aurora xuyong 2016-02-01 added for xy-smartsms end
}

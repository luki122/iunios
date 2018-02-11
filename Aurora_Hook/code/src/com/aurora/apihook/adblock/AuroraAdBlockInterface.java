package com.aurora.apihook.adblock;

import android.content.Context;
import android.view.View;

/**
 * 广告拦截
 */
public interface AuroraAdBlockInterface {      
      /**
       * 判断是否为广告视图，如果是，则隐藏；
       * 添加的地方：
       * （1）public View(Context context)的末尾
       * （2）public View(Context context, AttributeSet attrs, int defStyle)的末尾
       * （3）public void setVisibility(int visibility)的末尾
       * @param context
       * @param object
       * @return
       */
      public boolean auroraHideAdView(Context context,View object);
}

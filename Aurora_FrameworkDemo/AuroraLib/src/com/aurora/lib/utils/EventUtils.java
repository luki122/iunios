
package com.aurora.lib.utils;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * this class is used for deal with event like touch or click;
 * @author luofu
 *
 */
public class EventUtils {

    /**
     * 判断触摸范围是否在指定控件的范围内
     * @param context
     * @param event
     * @param target
     * @return
     */
    public static boolean isOutOfBounds(Context context, MotionEvent event, View target) {
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        final int slop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
        return (x < -slop) || (y < -slop)
                || (x > (target.getWidth() + slop))
                || (y > (target.getHeight() + slop));
    }

}

package com.aurora.community.widget.util;



import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static float getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int statusBarIdentifier = resources.getIdentifier("status_bar_height",
                "dimen", "android");
        if (0 != statusBarIdentifier) {
            return resources.getDimension(statusBarIdentifier);
        }
        return 0;
    }
    
    public static int[] getDisplayHeight(Context context){
        int[] size = new int[2];
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getHeight();
        size[0] = wm.getDefaultDisplay().getHeight();
        size[1] = wm.getDefaultDisplay().getWidth();
        return size;
    }
    
}

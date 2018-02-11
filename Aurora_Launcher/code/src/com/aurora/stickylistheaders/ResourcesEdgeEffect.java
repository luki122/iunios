package com.aurora.stickylistheaders;

import com.aurora.launcher.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

public class ResourcesEdgeEffect extends Resources {
    private int overscroll_edge = getPlatformDrawableId("overscroll_edge");
    private int overscroll_glow = getPlatformDrawableId("overscroll_glow");
    private Context mContext;
 
    public ResourcesEdgeEffect(Context context, AssetManager assets, DisplayMetrics metrics, Configuration config) {
        super(assets, metrics, config);
        mContext = context;
    }
 
    private int getPlatformDrawableId(String name) {
        try {
            int i = ((Integer) Class.forName("com.android.internal.R$drawable").getField(name).get(null)).intValue();
            return i;
        } catch (ClassNotFoundException e) {
            Log.e("[ContextWrapperEdgeEffect].getPlatformDrawableId()", "Cannot find internal resource class");
            return 0;
        } catch (NoSuchFieldException e1) {
            Log.e("[ContextWrapperEdgeEffect].getPlatformDrawableId()", "Internal resource id does not exist: " + name);
            return 0;
        } catch (IllegalArgumentException e2) {
            Log.e("[ContextWrapperEdgeEffect].getPlatformDrawableId()", "Cannot access internal resource id: " + name);
            return 0;
        } catch (IllegalAccessException e3) {
            Log.e("[ContextWrapperEdgeEffect].getPlatformDrawableId()", "Cannot access internal resource id: " + name);
        }
        return 0;
    }
 
    public Drawable getDrawable(int resId) throws Resources.NotFoundException {
        if (resId == this.overscroll_edge)
            return mContext.getResources().getDrawable(R.drawable.edges);
        if (resId == this.overscroll_glow)
            return mContext.getResources().getDrawable(R.drawable.edges);
        return super.getDrawable(resId);
    }
}
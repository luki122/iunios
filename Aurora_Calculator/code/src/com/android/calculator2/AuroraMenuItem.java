package com.android.calculator2;

import android.content.Context;
import android.graphics.drawable.Drawable;

// Aurora liugj 2014-01-21 created for aurora's new feature
public class AuroraMenuItem {
	
	/** 表示无图标情况的资源ID */
    static final int NO_ICON = 0;
    
	/** Context */
    protected Context mContext;
    /** ID */
    protected final int mId;
    /** 标题 */
    protected CharSequence mTitle;
    /** 图标资源ID */
    protected int mIconResId = NO_ICON;
    /** 图标Drawable */
    protected Drawable mIconDrawable;
    
	/** {@link OnItemClickListener} */
    protected OnItemClickListener mOnClickListener;
    
    /** 该item所依附的Menu */
    private AuroraToolMenu mMenu;
    
    public AuroraMenuItem(Context context, int id, CharSequence title) {
    	mContext = context;
        mId = id;
        mTitle = title;
	}
    
    public AuroraMenuItem(Context context, int id, CharSequence title, int iconResId) {
    	mContext = context;
        mId = id;
        mTitle = title;
        mIconResId = iconResId;
	}
    
    /**
     * Constructor
     * @param context Context
     * @param id Menu ID
     * @param title Menu title
     * @param icon Drawable for menu icon
     */
    public AuroraMenuItem(Context context, int id, CharSequence title, Drawable icon) {
        mContext = context;
        mId = id;
        mTitle = title;
        mIconDrawable = icon;
    }
    
    /**
     * 获取该item所依附的menu.
     * @return AuroraToolMenu.
     */
    public AuroraToolMenu getMenu() {
        return mMenu;
    }
    
    /**
     * 设置该item所依附的menu.
     * @param menu AuroraToolMenu
     */
    public void setMenu(AuroraToolMenu menu) {
        mMenu = menu;
    }
    
    /**
     * 获取菜单项ID
     * @return 菜单项ID
     */
    public int getItemId() {
        return mId;
    }
    
    /**
     * 获取菜单项标题
     * @return 标题
     */
    public CharSequence getTitle() {
        return mTitle;
    }
    
	/**
     * 获取菜单项点击Listener
     * @return The {@link OnItemClickListener}
     */
    public OnItemClickListener getOnClickListener() {
        return mOnClickListener;
    }

    /**
     * 设置菜单项点击Listener
     * @param listener The {@link AuroraMenuItem#OnItemClickListener} to set
     */
    public void setOnClickListener(OnItemClickListener listener) {
        this.mOnClickListener = listener;
    }
    
	/**
     * 菜单项点击事件监听
     * 
     * @author lisen02
     * @since 2014-1-21
     */
    public interface OnItemClickListener {
        /**
         * 菜单项点击回调
         * @param item 被点击的菜单项
         */
        void onClick(AuroraMenuItem item);
    }
}

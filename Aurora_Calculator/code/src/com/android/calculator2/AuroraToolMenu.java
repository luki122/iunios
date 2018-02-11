package com.android.calculator2;

import java.util.ArrayList;
import java.util.List;

import com.android.calculator2.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.PopupWindow;

// Aurora liugj 2014-01-21 created for aurora's new feature
public abstract class AuroraToolMenu {
	
	/** Context */
    protected Context mContext;
    /** Resources */
    protected Resources mResources;
    
    /** 所有的菜单项 */
    protected List<AuroraMenuItem> mItems;
    
	/** 点击菜单项后是否自动关闭菜单。 */
    private boolean mDismissOnClick = true;
    
    /** 菜单点击回调 */
    protected AuroraMenuItem.OnItemClickListener mMenuItemClickListener;
    
    /**监听菜单键的listener*/
    protected View.OnKeyListener mKeyClickListener;
    
	/** 用来显示菜单的PopupWindow */
    protected PopupWindow mPopupWindow;
    /** 上下文菜单的View */
     View mMenu;
    /** 要绑定的View */
    protected final View mViewToAttach;
    
	public AuroraToolMenu(View viewToAttach) {
		mViewToAttach = viewToAttach;
        mContext = mViewToAttach.getContext();
        mResources = mViewToAttach.getResources();
        mItems = new ArrayList<AuroraMenuItem>();
        
        mMenu = getMenuView(mContext);
        
        mMenu.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP
                        && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU)) {
                    // Menu键或Back键隐藏菜单
                    dismiss();
                    if (mKeyClickListener != null) {
                    	mKeyClickListener.onKey(v, keyCode, event);
                    }
                    return true;
                }
                return false;
            }
        });
	}
	
	/**
     * 获得菜单View
     * 注意：所返回的View应实现{@link OnMenuSetChangedListener}，否则会抛出异常。
     * @param context Context
     * @return 菜单View
     */
    protected abstract View getMenuView(Context context);
    
	/**
     * 设置点击后是否关闭菜单，默认为true，即点击后关闭菜单。
     * 如果子类希望点击后菜单仍然显示，则应在构造函数中调用此方法进行设置。
     * @param dismissOnClick 是否在点击菜单项后关闭菜单
     */
    protected void setDismissOnClick(boolean dismissOnClick) {
        mDismissOnClick = dismissOnClick;
    }
    
    /**
     * 设置菜单点击回调
     * @param listener 回调接口
     */
    public void setMenuItemClickListener(AuroraMenuItem.OnItemClickListener listener) {
        mMenuItemClickListener = listener;
    }
    
    /**
	 * 设置在这里的菜单键事件监听器 
	 * 
	 * @param listener 菜单键事件监听器 
	 */
    public void setOnKeyListener(View.OnKeyListener listener) {
        mKeyClickListener = listener;
    }
    
    /**
     * 添加菜单项（本对象内部使用）
     * @param id 菜单项ID
     * @param title 名称
     * @param icon 图标
     * @return 添加的菜单项
     */ 
    protected AuroraMenuItem addInternal(int id, CharSequence title, Drawable icon) {
        final AuroraMenuItem item = new AuroraMenuItem(mContext, id, title, icon);
        item.setMenu(this);
        if (mDismissOnClick) {
            item.setOnClickListener(new AuroraMenuItem.OnItemClickListener() {
                @Override
                public void onClick(AuroraMenuItem item) {
                    dismiss();
                    if (mMenuItemClickListener != null) {
                        mMenuItemClickListener.onClick(item);
                    }
                }
            });
        } else {
            item.setOnClickListener(mMenuItemClickListener);
        }
        mItems.add(item);
        return item;
    }
    
    /**
     * 添加一个菜单项（不带图标）
     * @param id 菜单项ID
     * @param title 名称
     * @return 添加的菜单项
     */
    public AuroraMenuItem add(int id, CharSequence title) {
        return addInternal(id, title, null);
    }
    
    /**
     * 添加一个菜单项（不带图标）
     * @param id 菜单项ID
     * @param titleResId 名称的资源ID
     * @return 添加的菜单项
     */
    public AuroraMenuItem add(int id, int titleResId) {
        return addInternal(id, mResources.getString(titleResId), null);
    }
    
    /**
     * 显示菜单。子类可根据需要控制显示位置、过渡效果等。
     * @param window 用来承载菜单的{@link PopupWindow}
     */
    protected abstract void showMenu(PopupWindow window);
    
    /**
     * 确保菜单已完全加载。如果整个菜单需要布局，则应在此方法中根据菜单项列表进行整个菜单的布局。
     * @param menuView 菜单View
     * @param items 菜单项列表
     */
    protected abstract void ensureMenuLoaded(Context context,View menuView, List<AuroraMenuItem> items);
    
    /**
     * 显示菜单
     */
    @SuppressLint("NewApi")
	public void show() {
        ensureMenuLoaded(mContext,mMenu, mItems);
        dismiss(); // 避免多个长按出现
        if (mPopupWindow == null) {
            mPopupWindow = new PopupWindow(mMenu, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            
            // 设置背景，以便使点击PopupWindow以外的地方dismiss window的功能生效
            mPopupWindow.setBackgroundDrawable(mResources.getDrawable(R.drawable.transparent_drawable));
          
            mPopupWindow.setTouchable(true);
            /*mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                	
                }
            });*/
        }
        mViewToAttach.post(new Runnable() {
            @Override
            public void run() {
                showMenu(mPopupWindow);
              
            }
        });
        mMenu.postInvalidate(); // 尝试让菜单重绘一下
    }
    
    /**
     * 隐藏菜单
     */
    public void dismiss() {
        if (mPopupWindow != null) {
            mPopupWindow.dismiss();
           
        }
    }
    
    public int getItemCount() {
		return mItems.size();
	}
}

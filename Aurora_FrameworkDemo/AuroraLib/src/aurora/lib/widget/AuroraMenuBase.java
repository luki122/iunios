package aurora.lib.widget;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import aurora.lib.app.AuroraActivity;

/**
 * @author leftaven
 * @2013年9月12日 aurora menu
 */
public class AuroraMenuBase extends PopupWindow {
	protected AuroraMenuItem auroraMenuItem;
	protected AuroraMenuAdapterBase menuAdapter;
	protected Context mContext;
	protected AuroraActionBar auroraActionBar;
	protected ArrayList<AuroraMenuItem> menuItems;
	protected OnAuroraMenuItemClickListener auroraMenuCallBack;
	protected int mAniTabMenu;

	public AuroraMenuBase(Context context) {
		super(context);
		mContext = context;
	}

	/**
	 * 菜单隐藏
	 */
	protected void dismissMenu() {
		AuroraActivity activity = (AuroraActivity) mContext;
		activity.dismissAuroraMenu();
	}

	/**
	 * 底部菜单消失
	 */
	protected void dismissActionBarMenu() {
		auroraActionBar.setShowBottomBarMenu(false);
		auroraActionBar.showActionBarMenu();
	}

	public interface OnAuroraMenuItemClickListener {
		public void auroraMenuItemClick(int itemId);
	}

	public AuroraMenuAdapterBase getMenuAdapter() {
		return menuAdapter;
	}

	public enum Type {
		System, BottomBar
	}

	protected void defaultMenu(View view, int aniTabMenu) {
		// 设置默认项
		this.setContentView(view);
		this.setWidth(LayoutParams.FILL_PARENT);
		this.setHeight(LayoutParams.WRAP_CONTENT);
		this.setBackgroundDrawable(new BitmapDrawable());// 设置Menu菜单背景
		if(aniTabMenu!=-1)
		this.setAnimationStyle(aniTabMenu);
		this.setOutsideTouchable(true);
		this.setFocusable(true);// menu菜单获得焦点 如果没有获得焦点menu菜单中的控件事件无法响应
		
		this.mAniTabMenu = aniTabMenu;
	}

	protected void addMenuItemById(int itemId) {
	}

	protected void removeMenuItemById(int itemId) {
	}

	public void setMenuIds(Map<Integer, Integer> menuIds) {
		menuAdapter.setMenuIds(menuIds);
	}

	/*
	//专门为U3虚拟键监听而增加的内容 aurora add by tangjun 2014.8.1 start
	private static final String NAVI_KEY_HIDE  = "navigation_key_hide"; // Settings.System 对应的键值
	private View mParent;
	private int mGravity;
	private int mX;
	private int mY;
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		// TODO Auto-generated method stub
		registerNavigationBarObserver();
		mParent = parent;
		mGravity = gravity;
		mX = x;
		mY = y;
		int navigation_bar_height = ((Activity)mContext).getResources().getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height);
		int isNaviBarHide = Settings.System.getInt(mContext.getContentResolver(), NAVI_KEY_HIDE, 0);
		boolean isNaviBarFull = ( ((Activity)mContext).getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION ) != 0;
		//Log.e("111111", "---navigation_bar_height = ---------" + navigation_bar_height);
		Log.e("111111", "---isNaviBarHide = ---------" + isNaviBarHide);
		Log.e("111111", "---isNaviBarFull = ---------" + isNaviBarFull);
		if ( isNaviBarHide == 0 && isNaviBarFull ) {
			this.setAnimationStyle(com.aurora.R.style.PopupAnimationForNavi);
			super.showAtLocation(parent, gravity, x, y + navigation_bar_height);
		} else {
			this.setAnimationStyle(mAniTabMenu);
			super.showAtLocation(parent, gravity, x, y);
		}
	}
	
    @Override
    public void dismiss() {
    	// TODO Auto-generated method stub
    	unregisterNavigationBarObserver();
    	
    	super.dismiss();
    }
	
	private class NavigationBarObserver extends ContentObserver {

		public NavigationBarObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			//Log.e("111111", "-NavigationBarObserver--onChange = ---------");
			AuroraActivity activity = (AuroraActivity) mContext;
			Log.e("111111", "-NavigationBarObserver--activity.isDestroyed() = ---------" + activity.isDestroyed());
			if ( !activity.isDestroyed() ) {
//				dismiss();
				if ( !(AuroraMenuBase.this instanceof AuroraMenu) ) {
					activity.removeCoverView();
					activity.addCoverView();
				}
//				showAtLocation(mParent, mGravity, mX, mY);
			} else {
				unregisterNavigationBarObserver();
			}
		}
    };
    
    private NavigationBarObserver mNavigationBarObserver = new NavigationBarObserver(new Handler());
    
    private void registerNavigationBarObserver() {
    	//Log.e("111111", "-AuroraMenuBase--registerNavigationBarObserver = ---------");
    	Uri uri = Settings.System.getUriFor(NAVI_KEY_HIDE);
    	mContext.getContentResolver().registerContentObserver(uri, true, mNavigationBarObserver);
    }
    
    private void unregisterNavigationBarObserver() {
    	//Log.e("111111", "-AuroraMenuBase--unregisterNavigationBarObserver = ---------");
    	mContext.getContentResolver().unregisterContentObserver(mNavigationBarObserver);
    }

	//专门为U3虚拟键监听而增加的内容 aurora add by tangjun 2014.8.1 end
	*/
	
}

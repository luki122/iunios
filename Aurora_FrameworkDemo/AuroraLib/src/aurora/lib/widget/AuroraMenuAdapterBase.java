package aurora.lib.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.lib.app.AuroraActivity;
import aurora.lib.widget.AuroraMenuItem;

/**
 * @author leftaven 2013年9月29日 TODO 菜单接口设置
 */
@SuppressLint("UseSparseArrays")
public abstract class AuroraMenuAdapterBase extends BaseAdapter {

	protected class ViewHolder {
		protected TextView title;
		protected ImageView icon;
		protected LinearLayout menuItemLayout;
	}
	protected ViewHolder viewHolder;
	protected int iconId;
	protected int titleId;
	protected int layoutId;
	
	// Aurora <Luofu> <2014-1-6> modify for Menu begin
	protected View mMenuView;

	protected LayoutInflater layoutInflater;
	protected ArrayList<AuroraMenuItem> menuItems;
	protected ArrayList<AuroraMenuItem> menuItemsSrc;
	protected Map<Integer, Integer> menuIds = null;
	protected AuroraActivity activity;
	protected Map<Integer, Boolean> menuItemsEnable;
	protected Map<Integer, Boolean> menuItemsVisiable;

	public AuroraMenuAdapterBase(Context context,
			ArrayList<AuroraMenuItem> lists) {
		layoutInflater = LayoutInflater.from(context);
		menuItems = lists;
		menuItemsSrc = new ArrayList<AuroraMenuItem>();
		menuItemsEnable = new HashMap<Integer, Boolean>();
		menuItemsVisiable = new HashMap<Integer, Boolean>();
		activity = (AuroraActivity) context;
		initMenuItemsEnable();
	}

	public ArrayList<AuroraMenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuIds(Map<Integer, Integer> menuIds) {
		this.menuIds = menuIds;
	}

	protected Map<Integer, Integer> getMenuIds() {
		return this.menuIds;
	}

	protected void initMenuItemsEnable() {
		for (int i = 0; i < menuItems.size(); i++) {
			menuItemsEnable.put(menuItems.get(i).getId(), true);
			menuItemsVisiable.put(menuItems.get(i).getId(), true);
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return menuItemsEnable.get(menuItems.get(position).getId());
	}
	
	public boolean isVisiable(int itemId) {
		return menuItemsVisiable.get(itemId);
	}

	@Override
	public int getCount() {
		return menuItems.size();
	}

	@Override
	public Object getItem(int position) {
		return menuItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public AuroraMenu.Type isActionBottomBarMenu() {
		return activity.getAuroraActionBar().getMenuType();
	}

	public void addMenuItemById(int itemId) {
		if (isVisiable(itemId))//防止重复add
			return;
		try {
			int position = getMenuIds().get(itemId);
			AuroraMenuItem auroraMenuItem = null;
			int index = 0;
			for (index = 0; index < menuItemsSrc.size(); index++) {
				auroraMenuItem = menuItemsSrc.get(index);
				if (auroraMenuItem.getId() == itemId) {
					menuItems.add(position, auroraMenuItem);
					menuItemsSrc.remove(auroraMenuItem);
					menuItemsVisiable.put(itemId, true);//防止重复add
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addMenu(int itemId,int titleRes,int iconRes,View menuView){
	    if(isVisiable(itemId)){
//	        throw new 
	    }
	}
	
	public void removeMenuItemById(int itemId) {
		if (!isVisiable(itemId))//防止重复remove
			return;
		try {
			int index = getMenuIds().get(itemId);
			AuroraMenuItem auroraMenuItem = menuItems.get(index);
			menuItemsSrc.add(auroraMenuItem);
			menuItems.remove(auroraMenuItem);
			menuItemsVisiable.put(itemId, false);//防止重复remove
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Aurora <leftaven> <2013年9月13日> modify for OTA begin
	/**
	 * 单个menu项设置是否可用
	 * 
	 * @param itemId
	 * @param isEnable
	 */
	public void setMenuItemEnable(int itemId, boolean isEnable) {
		menuItemsEnable.put(itemId, isEnable);
		this.notifyDataSetChanged();
	}

	// Aurora <leftaven> <2013年9月13日> modify for OTA end
	
	//Aurora add by tangjun 2014.7.7 start
	public void addMenu(int itemId,int titleRes,int iconRes) {
		
		addMenu(itemId, titleRes, iconRes, menuItems.size());
	}
	
	public void addMenu(int itemId,int titleRes,int iconRes, int position) {
		
		AuroraMenuItem item = new AuroraMenuItem();

		if (itemId >= 0 && !judgeIfIdExist(itemId)) {
			item.setId(itemId);
			
			if (iconRes >= 0) {
				item.setIcon(iconRes);
			}
			if (titleRes >= 0) {
				item.setTitle(titleRes);
				item.setTitleText(activity.getResources().getString(item.getTitle()));
			}
			menuItems.add(position, item);
			
			menuItemsEnable.put(itemId, true);
		}
	}
	
	public void addMenu(int itemId,CharSequence menuText,int iconRes) {
		
		addMenu(itemId, menuText, iconRes, menuItems.size());
	}
	
	public void addMenu(int itemId,CharSequence menuText,int iconRes, int position) {
		
		AuroraMenuItem item = new AuroraMenuItem();

		if (itemId >= 0 && !judgeIfIdExist(itemId)) {
			item.setId(itemId);
			
			if (iconRes >= 0) {
				item.setIcon(iconRes);
			}
			if (menuText != null) {
				item.setTitleText(menuText);
			}
			menuItems.add(position, item);
			
			menuItemsEnable.put(itemId, true);
		}
	}
	
	private boolean judgeIfIdExist(int itemId) {
		for( int i = 0; i < menuItems.size(); i++ ) {
			AuroraMenuItem item = menuItems.get(i);
			if ( item.getId() == itemId ) {
				return true;
			}
		}
		return false;
	}
	
	public void removeMenuByItemId(int itemId) {
		for( int i = 0; i < menuItems.size(); i++ ) {
			AuroraMenuItem item = menuItems.get(i);
			if ( item.getId() == itemId ) {
				removeMenuByPosition( i );
				return;
			}
		}
	}
	
	public void removeMenuByPosition(int position) {
		menuItems.remove(position);
	}
	
	public void setMenuTextByItemId(int menuTextResId, int itemId) {
		for( int i = 0; i < menuItems.size(); i++ ) {
			AuroraMenuItem item = menuItems.get(i);
			if ( item.getId() == itemId ) {
				item.setTitle(menuTextResId);
				item.setTitleText(activity.getResources().getString(item.getTitle()));
				return;
			}
		}
	}
	
	public void setMenuTextByItemId(CharSequence menuText, int itemId) {
		for( int i = 0; i < menuItems.size(); i++ ) {
			AuroraMenuItem item = menuItems.get(i);
			if ( item.getId() == itemId ) {
				item.setTitleText(menuText);
				return;
			}
		}
	}
	
	public void setMenuTextByPosition(int menuTextResId, int position) {
		if ( position >= 0 && position < menuItems.size() ) {
			AuroraMenuItem item = menuItems.get(position);
			item.setTitle(menuTextResId);
			item.setTitleText(activity.getResources().getString(item.getTitle()));
		}
	}
	
	public void setMenuTextByPosition(CharSequence menuText, int position) {
		if ( position >= 0 && position < menuItems.size() ) {
			AuroraMenuItem item = menuItems.get(position);
			item.setTitleText(menuText);
		}
	}

	//Aurora add by tangjun 2014.7.7 end
}

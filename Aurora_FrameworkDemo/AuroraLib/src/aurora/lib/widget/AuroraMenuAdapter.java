package aurora.lib.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.lib.app.AuroraActivity;

import com.aurora.lib.R;

/**
 * @author aven
 * @2013年9月12日 menu adapter
 */
public class AuroraMenuAdapter extends BaseAdapter {
	class ViewHolder {
		TextView title;
		ImageView icon;
		LinearLayout menuItemLayout;
	}

	private LayoutInflater layoutInflater;
	private ArrayList<AuroraMenuItem> menuItems;
	private Map<Integer, Integer> menuIds = null;
	private AuroraActivity activity;
	private Map<Integer, Boolean> menuItemsEnable;

	public ArrayList<AuroraMenuItem> getMenuItems() {
		return menuItems;
	}

	public void setMenuIds(Map<Integer, Integer> menuIds) {
		this.menuIds = menuIds;
	}

	private Map<Integer, Integer> getMenuIds() {
		return this.menuIds;
	}

	public AuroraMenuAdapter(Context context, ArrayList<AuroraMenuItem> lists) {
		layoutInflater = LayoutInflater.from(context);
		menuItems = lists;
		menuIds = new HashMap<Integer, Integer>();
		menuItemsEnable = new HashMap<Integer, Boolean>();
		activity = (AuroraActivity) context;
		initMenuItemsEnable();
	}

	public void initMenuItemsEnable() {
		for (int i = 0; i < menuItems.size(); i++) {
			menuItemsEnable.put(menuItems.get(i).getId(), true);
		}
	}

	@Override
	public boolean isEnabled(int position) {
		return menuItemsEnable.get(menuItems.get(position).getId());
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

	@SuppressWarnings("deprecation")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
	    Log.e("luofu", "AuroraMenuAdapter");
		ViewHolder viewHolder;
		int iconId;
		int titleId;
		int layoutId;
		if (AuroraMenu.Type.BottomBar.equals(isActionBottomBarMenu())) {
			iconId = R.id.aurora_action_bottom_bar_menu_item_image;
			titleId = R.id.aurora_action_bottom_bar_menu_item_text;
			layoutId = com.aurora.lib.R.layout.aurora_action_bottom_bar_menu_item;
		} else {
			iconId = R.id.aurora_menu_item_icon;
			titleId = R.id.aurora_menu_item_text;
			layoutId = com.aurora.lib.R.layout.aurora_menu_item;
		}
		if (convertView == null) {
			convertView = layoutInflater.inflate(layoutId, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(titleId);
			viewHolder.icon = (ImageView) convertView.findViewById(iconId);
			viewHolder.menuItemLayout = (LinearLayout) convertView;
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		AuroraMenuItem auroraMenuItem = menuItems.get(position);
		if (auroraMenuItem != null) {
			viewHolder.icon.setBackgroundResource(auroraMenuItem.getIcon());
			if (auroraMenuItem.getTitle() != 0) {
				viewHolder.title.setText(activity.getResources().getString(
						auroraMenuItem.getTitle()));
			}

			// viewHolder.title.setText(menuItems.get(position).getTitle());
			menuIds.put(auroraMenuItem.getId(), position);// 设置itemId,position对应关系

			if (!AuroraMenu.Type.BottomBar.equals(isActionBottomBarMenu())) {
				if (isEnabled(position)) {
					viewHolder.menuItemLayout
							.setBackgroundDrawable(activity
									.getResources()
									.getDrawable(
											com.aurora.lib.R.drawable.aurora_menu_item_select));
				} else {
					//viewHolder.menuItemLayout.setBackgroundColor(Color.RED);
				}
			}
		}
		return convertView;
	}

	private AuroraMenu.Type isActionBottomBarMenu() {
		return activity.getAuroraActionBar().getMenuType();
	}

	public void addMenuItemById(int itemId) {
		getMenuItems().add(menuItems.get(getMenuIds().get(itemId)));
	}

	public void removeMenuItemById(int itemId) {
		getMenuItems().remove(menuItems.get(getMenuIds().get(itemId)));
	}

	// Aurora <leftaven> <2013年9月13日> modify for OTA
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

}

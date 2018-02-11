package aurora.lib.widget;

import java.util.ArrayList;

import com.aurora.lib.R;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class AuroraActionBottomBarMenuAdapter extends AuroraMenuAdapterBase {

	public AuroraActionBottomBarMenuAdapter(Context context,
			ArrayList<AuroraMenuItem> lists) {
		super(context, lists);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		iconId = R.id.aurora_action_bottom_bar_menu_item_image;
		titleId = R.id.aurora_action_bottom_bar_menu_item_text;
		layoutId = com.aurora.lib.R.layout.aurora_action_bottom_bar_menu_item;
		if (convertView == null) {
			convertView = layoutInflater.inflate(layoutId, null);
			viewHolder = new ViewHolder();
			viewHolder.title = (TextView) convertView.findViewById(titleId);
			viewHolder.icon = (ImageView) convertView.findViewById(iconId);
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
			menuIds.put(auroraMenuItem.getId(), position);// 设置itemId,position对应关系
		}
		return convertView;
	}
}

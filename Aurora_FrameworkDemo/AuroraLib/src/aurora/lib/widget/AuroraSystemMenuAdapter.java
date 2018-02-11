package aurora.lib.widget;

import java.util.ArrayList;

import com.aurora.lib.R;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AuroraSystemMenuAdapter extends AuroraMenuAdapterBase {

	public AuroraSystemMenuAdapter(Context context,
			ArrayList<AuroraMenuItem> lists) {
		super(context, lists);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		iconId = R.id.aurora_menu_item_icon;
		titleId = R.id.aurora_menu_item_text;
		layoutId = com.aurora.lib.R.layout.aurora_menu_item;
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
			if ( auroraMenuItem.getIcon() == 0 ) {
				LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
						LayoutParams.MATCH_PARENT);
				lp.leftMargin = 0;
				viewHolder.title.setLayoutParams(lp);
			}
//			if (auroraMenuItem.getTitle() != 0) {
//				viewHolder.title.setText(activity.getResources().getString(
//						auroraMenuItem.getTitle()));
			if (auroraMenuItem.getTitleText() != null) {
				viewHolder.title.setText(auroraMenuItem.getTitleText());
			} else {
				viewHolder.title.setText("");
			}
			menuIds.put(auroraMenuItem.getId(), position);// 设置itemId,position对应关系
			if (isEnabled(position)) {
				viewHolder.menuItemLayout.setBackgroundDrawable(activity
						.getResources().getDrawable(
								com.aurora.lib.R.drawable.aurora_menu_item_select));
			} else {
				viewHolder.menuItemLayout.setBackgroundColor(Color.RED);
			}
		}
		return convertView;
	}
}

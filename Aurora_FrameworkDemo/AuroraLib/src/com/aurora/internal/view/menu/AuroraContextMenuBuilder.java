package com.aurora.internal.view.menu;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.View;
import aurora.lib.widget.AuroraListView;

public class AuroraContextMenuBuilder extends AuroraMenuBuilder implements ContextMenu {

	private Context mContext;
	private Fragment mFragment;
	
	public AuroraContextMenuBuilder(Context context) {
		super(context);
		mContext = context;
	}

	public ContextMenu setHeaderIcon(Drawable icon) {
		return (ContextMenu) super.setHeaderIconInt(icon);
	}

	public ContextMenu setHeaderIcon(int iconRes) {
		return (ContextMenu) super.setHeaderIconInt(iconRes);
	}

	public ContextMenu setHeaderTitle(CharSequence title) {
		return (ContextMenu) super.setHeaderTitleInt(title);
	}

	public ContextMenu setHeaderTitle(int titleRes) {
		return (ContextMenu) super.setHeaderTitleInt(titleRes);
	}

	public ContextMenu setHeaderView(View view) {
		return (ContextMenu) super.setHeaderViewInt(view);
	}

	/**
	 * Shows this context menu, allowing the optional original view (and its ancestors) to add items.
	 * 
	 * @param originalView
	 *            Optional, the original view that triggered the context menu.
	 * @param token
	 *            Optional, the window token that should be set on the context menu's window.
	 * @return If the context menu was shown, the {@link MenuDialogHelper} for dismissing it. Otherwise, null.
	 */
	public AuroraMenuDialogHelper show(View originalView, IBinder token) {
		if (originalView != null && originalView instanceof AuroraListView) {
			if (getVisibleItems().size() > 0) {
//				EventLog.writeEvent(50001, 1);
				AuroraMenuDialogHelper helper = new AuroraMenuDialogHelper(this);
				helper.setFragment(mFragment);
				helper.show(token);
				return helper;
			}
		}
		return null;
	}
	
	public Context getContext(){
		return mContext;
	}
	
	public void setFragment(Fragment fragment){
	    mFragment = fragment;
	}
}

package com.aurora.internal.view.menu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class AuroraSubMenuBuilder extends AuroraMenuBuilder implements SubMenu {
	private AuroraMenuBuilder mParentMenu;
	private AuroraMenuItemImpl mItem;

	public AuroraSubMenuBuilder(Context context, AuroraMenuBuilder parentMenu, AuroraMenuItemImpl item) {
		super(context);

		mParentMenu = parentMenu;
		mItem = item;
	}

	@Override
	public void setQwertyMode(boolean isQwerty) {
		mParentMenu.setQwertyMode(isQwerty);
	}

	@Override
	public boolean isQwertyMode() {
		return mParentMenu.isQwertyMode();
	}

	@Override
	public void setShortcutsVisible(boolean shortcutsVisible) {
		mParentMenu.setShortcutsVisible(shortcutsVisible);
	}

	@Override
	public boolean isShortcutsVisible() {
		return mParentMenu.isShortcutsVisible();
	}

	public Menu getParentMenu() {
		return mParentMenu;
	}

	public MenuItem getItem() {
		return mItem;
	}

	@Override
	public void setCallback(Callback callback) {
		mParentMenu.setCallback(callback);
	}

	@Override
	public AuroraMenuBuilder getRootMenu() {
		return mParentMenu;
	}

	@Override
	boolean dispatchMenuItemSelected(AuroraMenuBuilder AuroraMenu, MenuItem item) {
		return super.dispatchMenuItemSelected(AuroraMenu, item)
				|| mParentMenu.dispatchMenuItemSelected(AuroraMenu, item);
	}

	public SubMenu setIcon(Drawable icon) {
		mItem.setIcon(icon);
		return this;
	}

	public SubMenu setIcon(int iconRes) {
		mItem.setIcon(iconRes);
		return this;
	}

	public SubMenu setHeaderIcon(Drawable icon) {
		return (SubMenu) super.setHeaderIconInt(icon);
	}

	public SubMenu setHeaderIcon(int iconRes) {
		return (SubMenu) super.setHeaderIconInt(iconRes);
	}

	public SubMenu setHeaderTitle(CharSequence title) {
		return (SubMenu) super.setHeaderTitleInt(title);
	}

	public SubMenu setHeaderTitle(int titleRes) {
		return (SubMenu) super.setHeaderTitleInt(titleRes);
	}

	public SubMenu setHeaderView(View view) {
		return (SubMenu) super.setHeaderViewInt(view);
	}

	@Override
	public boolean expandItemActionView(AuroraMenuItemImpl item) {
		return mParentMenu.expandItemActionView(item);
	}

	@Override
	public boolean collapseItemActionView(AuroraMenuItemImpl item) {
		return mParentMenu.collapseItemActionView(item);
	}

	@Override
	public String getActionViewStatesKey() {
		final int itemId = mItem != null ? mItem.getItemId() : 0;
		if (itemId == 0) {
			return null;
		}
		return super.getActionViewStatesKey() + ":" + itemId;
	}
}

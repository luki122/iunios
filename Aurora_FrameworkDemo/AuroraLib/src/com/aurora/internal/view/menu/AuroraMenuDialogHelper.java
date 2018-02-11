package com.aurora.internal.view.menu;

import com.aurora.lib.R;
import android.R.menu;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import aurora.lib.app.AuroraActivity;
import aurora.lib.app.AuroraAlertDialog;

public class AuroraMenuDialogHelper implements DialogInterface.OnKeyListener, DialogInterface.OnClickListener,
		DialogInterface.OnDismissListener, AuroraMenuPresenter.Callback {
	private AuroraMenuBuilder mMenu;
	private AuroraAlertDialog mDialog;
	AuroraListMenuPresenter mPresenter;
	private Fragment mFragment;
	private AuroraMenuPresenter.Callback mPresenterCallback;

	public AuroraMenuDialogHelper(AuroraMenuBuilder menu) {
		mMenu = menu;
	}

	/**
	 * Shows menu as a dialog.
	 * 
	 * @param windowToken
	 *            Optional token to assign to the window.
	 */
	public void show(IBinder windowToken) {
		// Many references to mMenu, create local reference
		final AuroraMenuBuilder menu = mMenu;

		// Get the builder for the dialog
		final AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(menu.getContext());

		mPresenter = new AuroraListMenuPresenter(builder.getContext(),
				R.layout.aurora_list_menu_item_layout);

		mPresenter.setCallback(this);
		mMenu.addMenuPresenter(mPresenter);
		builder.setAdapter(mPresenter.getAdapter(), this);

		// Set the title
		final View headerView = menu.getHeaderView();
		if (headerView != null) {
			// Menu's client has given a custom header view, use it
			builder.setCustomTitle(headerView);
		} else {
			// Otherwise use the (text) title and icon
			builder.setIcon(menu.getHeaderIcon()).setTitle(menu.getHeaderTitle());
		}

		// Set the key listener
		builder.setOnKeyListener(this);

		// Show the menu
		mDialog = builder.create();
		mDialog.setOnDismissListener(this);

		WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
		lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
		if (windowToken != null) {
			lp.token = windowToken;
		}
		lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;

		mDialog.show();
	}

	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
				Window win = mDialog.getWindow();
				if (win != null) {
					View decor = win.getDecorView();
					if (decor != null) {
						KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
						if (ds != null) {
							ds.startTracking(event, this);
							return true;
						}
					}
				}
			} else if (event.getAction() == KeyEvent.ACTION_UP && !event.isCanceled()) {
				Window win = mDialog.getWindow();
				if (win != null) {
					View decor = win.getDecorView();
					if (decor != null) {
						KeyEvent.DispatcherState ds = decor.getKeyDispatcherState();
						if (ds != null && ds.isTracking(event)) {
							mMenu.close(true);
							dialog.dismiss();
							return true;
						}
					}
				}
			}
		}

// Menu shortcut matching
		return mMenu.performShortcut(keyCode, event, 0);

	}

	public void setPresenterCallback(AuroraMenuPresenter.Callback cb) {
		mPresenterCallback = cb;
	}

	/**
	 * Dismisses the menu's dialog.
	 * 
	 * @see Dialog#dismiss()
	 */
	public void dismiss() {
		if (mDialog != null) {
			mDialog.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mPresenter.onCloseMenu(mMenu, true);
	}

	@Override
	public void onCloseMenu(AuroraMenuBuilder menu, boolean allMenusAreClosing) {
		if (allMenusAreClosing || menu == mMenu) {
			dismiss();
		}
		if (mPresenterCallback != null) {
			mPresenterCallback.onCloseMenu(menu, allMenusAreClosing);
		}
		
		AuroraActivity activity = (AuroraActivity) mMenu.getContext();
		activity.onContextMenuClosed(mMenu);
	}

	@Override
	public boolean onOpenSubMenu(AuroraMenuBuilder subMenu) {
		if (mPresenterCallback != null) {
			return mPresenterCallback.onOpenSubMenu(subMenu);
		}
		return false;
	}

	public void onClick(DialogInterface dialog, int which) {
//		mMenu.performItemAction((AuroraMenuItemImpl) mPresenter.getAdapter().getItem(which), 0);
        Context context = mMenu.getContext();
	    
        AuroraMenuItemImpl menuItem = (AuroraMenuItemImpl) mPresenter.getAdapter().getItem(which);

        Intent intent = menuItem.getIntent();
        if (intent != null) {
            context.startActivity(intent);
            return;
        }

        MenuItem.OnMenuItemClickListener clickListener = menuItem.getMenuItemClickListener();
        if (clickListener == null || !clickListener.onMenuItemClick(menuItem)) {
            if (mFragment!=null) {
                mFragment.onContextItemSelected(menuItem);    
            }else{
                ((AuroraActivity)context).onContextItemSelected(menuItem);
            }
        }
    }
	
	public void setFragment(Fragment fragment){
        mFragment = fragment;
    }
}

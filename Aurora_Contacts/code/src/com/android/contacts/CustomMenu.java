package com.android.contacts;

import java.util.ArrayList;


import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

public class CustomMenu implements OnMenuItemClickListener {
    @SuppressWarnings("unused")
    private static final String TAG = "FilterMenu";

    public static class DropDownMenu {
        private TextView mButton;
        private PopupMenu mPopupMenu;
        private Menu mMenu;

        public DropDownMenu(Context context, TextView button, int menuId,
                OnMenuItemClickListener listener) {
            mButton = button;
            mButton.setBackgroundDrawable(context.getResources().getDrawable(
                    R.drawable.gn_dropdown_holo_dark));
            mPopupMenu = new PopupMenu(context, mButton);
            mMenu = mPopupMenu.getMenu();
            mPopupMenu.getMenuInflater().inflate(menuId, mMenu);
            mPopupMenu.setOnMenuItemClickListener(listener);
            mButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mPopupMenu.show();
                }
            });
        }

        public MenuItem findItem(int id) {
            return mMenu.findItem(id);
        }

        public void setTitle(CharSequence title) {
            mButton.setText(title);
        }
    }



    private Context mContext;
    private ArrayList<DropDownMenu> mMenus;
    private OnMenuItemClickListener mListener;

    public CustomMenu(Context context) {
        mContext = context;
        mMenus = new ArrayList<DropDownMenu>();
    }

    public DropDownMenu addDropDownMenu(TextView button, int menuId) {
        DropDownMenu menu = new DropDownMenu(mContext, button, menuId, this);
        mMenus.add(menu);
        return menu;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mListener = listener;
    }

    public MenuItem findMenuItem(int id) {
        MenuItem item = null;
        for (DropDownMenu menu : mMenus) {
            item = menu.findItem(id);
            if (item != null) return item;
        }
        return item;
    }

    public void setMenuItemAppliedEnabled(int id, boolean applied, boolean enabled,
            boolean updateTitle) {
        MenuItem item = null;
        for (DropDownMenu menu : mMenus) {
            item = menu.findItem(id);
            if (item != null) {
                item.setCheckable(true);
                item.setChecked(applied);
                item.setEnabled(enabled);
                if (updateTitle) {
                    menu.setTitle(item.getTitle());
                }
            }
        }
    }

    public void setMenuItemVisibility(int id, boolean visibility) {
        MenuItem item = findMenuItem(id);
        if (item != null) {
            item.setVisible(visibility);
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (mListener != null) {
            return mListener.onMenuItemClick(item);
        }
        return false;
    }
}
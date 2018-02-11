package com.gionee.mms.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

public class MenuItemView extends TextView implements OnClickListener,
        OnLongClickListener {

    private CharSequence mTitle;
    private Drawable mIcon;
    private static final int MAX_ICON_SIZE = 32; // dp:copy from
                                                 // com.android.internal.view.menu.ActionMenuItemView
    private int mMaxIconSize;
    private boolean mVoiceInputEnable = true;

    private OnMenuItemClickListener mOnMenuItemClickListener = null;
    private OnMenuItemLongClickListener mOnMenuItemLongClickListener = null;

    public MenuItemView(Context context) {
        this(context, null);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        final Resources res = context.getResources();
        final float density = res.getDisplayMetrics().density;
        mMaxIconSize = (int) (MAX_ICON_SIZE * density + 0.5f);

        /*SharedPreferences sp = context.getSharedPreferences(
                "com.android.mms_preferences", Context.MODE_WORLD_READABLE);
        if (sp != null) {
            mVoiceInputEnable = sp.getBoolean("pref_key_voice_input", false);
        }*/
    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        if (hasText()) {
            // Don't show the cheat sheet for items that already show text.
            return false;
        }

        final int[] screenPos = new int[2];
        final Rect displayFrame = new Rect();
        getLocationOnScreen(screenPos);
        getWindowVisibleDisplayFrame(displayFrame);

        final Context context = getContext();
        final int width = getWidth();
        final int height = getHeight();
        final int midy = screenPos[1] + height / 2;
        final int screenWidth = context.getResources().getDisplayMetrics().widthPixels;

        Toast cheatSheet = Toast.makeText(context, mTitle, Toast.LENGTH_SHORT);
        if (midy < displayFrame.height()) {
            // Show along the top; follow action buttons
            cheatSheet.setGravity(Gravity.TOP | Gravity.END, screenWidth
                    - screenPos[0] - width / 2, height);
        } else {
            // Show along the bottom center
            cheatSheet.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                    0, height);
        }
        cheatSheet.show();

        if (mVoiceInputEnable && mOnMenuItemLongClickListener != null) {
            mOnMenuItemLongClickListener.onLongClick(this);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (mOnMenuItemClickListener != null) {
            mOnMenuItemClickListener.onClick(this);
        }
    }

    public void setTitle(CharSequence title) {
        mTitle = title;
        setContentDescription(mTitle);
    }

    public void setIcon(Drawable icon) {
        mIcon = icon;
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > mMaxIconSize) {
                final float scale = (float) mMaxIconSize / width;
                width = mMaxIconSize;
                height *= scale;
            }
            if (height > mMaxIconSize) {
                final float scale = (float) mMaxIconSize / height;
                height = mMaxIconSize;
                width *= scale;
            }
            icon.setBounds(0, 0, width, height);
        }
        setCompoundDrawables(icon, null, null, null);
    }

    public void setIcon(int res) {
        setIcon(getContext().getResources().getDrawable(res));
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener l) {
        mOnMenuItemClickListener = l;
        setOnClickListener(l != null ? this : null);
    }

    public void setOnMenuItemLongClickListener(OnMenuItemLongClickListener l) {
        mOnMenuItemLongClickListener = l;
        setOnLongClickListener(l != null ? this : null);
    }

    /**
     * Interface definition for a callback to be invoked when a MenuItemView is
     * clicked.
     */
    public interface OnMenuItemClickListener {
        /**
         * Called when a view has been clicked.
         * 
         * @param v
         *            The view that was clicked.
         */
        public void onClick(View v);
    }

    /**
     * Interface definition for a callback to be invoked when a MenuItemView has
     * been clicked and held.
     */
    public interface OnMenuItemLongClickListener {
        /**
         * Called when a view has been clicked and held.
         * 
         * @param v
         *            The view that was clicked and held.
         * 
         * @return true if the callback consumed the long click, false
         *         otherwise.
         */
        public void onLongClick(View v);
    }

}

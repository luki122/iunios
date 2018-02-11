/**
	File Description:
		Object class for sorting tool icon in notification bar.
	Author: fengjy@gionee.com
	Create Date: 2013/04/24
	Change List:
*/


package com.android.systemui.statusbar.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.FrameLayout;

public class ToolbarIcon {
	
    private int mId;
    private int mNameRes;
	private String mName;
    private int mIconRes;
    private Uri mIconUri;
    private int mImageLevel = -1;
    private int mIndicatorRes;
    private FrameLayout mContentView;
    private boolean mIsTemp;
    private boolean mVisible;
    private boolean mClickable;

	public ToolbarIcon(int id, FrameLayout v) {
        mId = id;
        mContentView = v;
        mIsTemp = false;
        mVisible = true;
        mClickable = true;
    }

	public ToolbarIcon(int id, FrameLayout v, boolean defaultVisible) {
        mId = id;
        mContentView = v;
        mIsTemp = true;
        mVisible = defaultVisible;
        mClickable = true;
    }

    public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}
	
	public int getNameRes() {
		return mNameRes;
	}
	
	public void setNameRes(Context context, int nameRes) {
		mNameRes = nameRes;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}

	public String getName() {
		return mName;
	}

	public void setName(Context context, String name) {
		mName = name;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}

    public int getIconRes() {
		return mIconRes;
	}

	public void setIconRes(Context context, int res) {
		mIconRes = res;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}
	
	public Uri getIconUri() {
		return mIconUri;
	}
	
	public void setIconUri(Context context, Uri uri) {
		mIconUri = uri;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}
	
	public int getImageLevel() {
		return mImageLevel;
	}
	
	public void setImageLevel(Context context, int level) {
		mImageLevel = level;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}
	
	public int getIndicatorRes() {
		return mIndicatorRes;
	}
	
	public void setIndicatorRes(Context context, int res) {
		mIndicatorRes = res;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}
	
	public boolean isTemp() {
		return mIsTemp;
	}
	
	public boolean isVisible() {
		return mVisible;
	}
	
	public void setVisible(Context context, boolean visible) {
		if (context != null && mVisible != visible) {
			mVisible = visible;
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
			List<ToolbarIcon> list = ToolbarIconUtils.getSortedIconList(context);
			int maxCount = ToolbarIconUtils.MAX_SHOWED_COUNT;
			for (int i = 0; i < maxCount; i++) {
				if (list.get(i).mId == this.mId) {
					Log.d(ToolbarIconUtils.TAG, "Refresh toolbar for the tile " + this.mId);
			    	context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLBAR));
				} else if (!list.get(i).mVisible) {
					maxCount++;
				}
			}
		}
	}
	
	public boolean isClickable() {
		return mClickable;
	}
	
	public void setClickable(Context context, boolean clickable) {
		mClickable = clickable;
		if (context != null) {
			context.sendBroadcast(new Intent(ToolbarIconUtils.ACTION_REFRESH_TOOLLIST));
		}
	}
	
	public void doAction() {
		mContentView.performClick();
	}

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof ToolbarIcon && mId == ((ToolbarIcon) o).getId()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }
    
}

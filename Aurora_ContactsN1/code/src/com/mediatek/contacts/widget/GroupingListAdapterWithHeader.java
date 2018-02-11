package com.mediatek.contacts.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.android.common.widget.GroupingListAdapter;

public abstract class GroupingListAdapterWithHeader extends GroupingListAdapter {

    private static final String LOG_TAG = "GroupingListAdapterWithHeader";

    private Cursor mCursor;
    HashMap<Integer, Boolean> mHeaderPositionList = new HashMap<Integer, Boolean>();

    public GroupingListAdapterWithHeader(Context context) {
        super(context);
        mHeaderPositionList.clear();
    }

    public void changeCursor(Cursor cursor) {
        log("changeCursor(), cursor = " + cursor);
        if(null != cursor) {
            log("cursor count = " + cursor.getCount());
        }
        if (mCursor != cursor) {
            mCursor = cursor;
            mHeaderPositionList.clear();
        }
        super.changeCursor(cursor);
    }

    public boolean isDateGroupHeader(int cursorPosition) {
        Boolean isDateGroupHeader = mHeaderPositionList.get(Integer.valueOf(cursorPosition));
        if (null == isDateGroupHeader || !isDateGroupHeader.booleanValue()) {
            return false;
        } else {
            return true;
        }
    }

    public void setGroupHeaderPosition(int cursorPosition) {
        mHeaderPositionList.put(Integer.valueOf(cursorPosition), Boolean.valueOf(true));
    }
    
    private void log(final String log) {
        Log.i(LOG_TAG, log);
    }
}

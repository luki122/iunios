package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class MessageCursorAdapter extends CursorAdapter {
    protected boolean mIsScrolling = false;
    public int mAllShowCheckBox = 0;
    // Aurora xuyong 2014-04-23 modified aurora's new feature start
    public boolean mNeedAnim = false;
    // Aurora xuyong 2014-04-23 modified aurora's new feature end

    public void setIsScrolling(boolean mIsScrolling) {
        this.mIsScrolling = mIsScrolling;
    }

    // Aurora yudingmin 2014-09-01 added for optimize start
    public boolean isScrolling() {
        return mIsScrolling;
    }
    // Aurora yudingmin 2014-09-01 added for optimize end

    // Aurora liugj 2014-01-07 added for allcheck animation start
    public void updateAllCheckBox(int allShow) {
        this.mAllShowCheckBox = allShow;
    }
    // Aurora liugj 2014-01-07 added for allcheck animation end
    
    // Aurora liugj 2014-01-14 added for check animation start
    public void setCheckBoxAnim(boolean needAnim) {
        this.mNeedAnim = needAnim;
    }
    // Aurora liugj 2014-01-14 added for check animation end

    public MessageCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public MessageCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public MessageCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return null;
    }
}

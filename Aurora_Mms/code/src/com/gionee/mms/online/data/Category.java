package com.gionee.mms.online.data;

import android.database.Cursor;

public class Category {

    private int _id;
    private int category_id;
    private String name;
    private int count;
    private boolean tap;

    public Category(Cursor cur) {
        if (cur != null) {
            _id = cur.getInt(cur.getColumnIndex(RecommendColumns._ID));
            category_id = cur.getInt(cur.getColumnIndex(RecommendColumns.CAT_ID));
            name = cur.getString(cur.getColumnIndex(RecommendColumns.NAME));
            count = cur.getInt(cur.getColumnIndex(RecommendColumns.COUNT));
            tap = cur.getInt(cur.getColumnIndex(RecommendColumns.TAP)) == 1;
        }
    }
    
    public int getId() {
        return _id;
    }
    
    public int getCategoryId() {
        return category_id;
    }
    
    public String getCategoryName() {
        return name;
    }
    
    public int getCount() {
        return count;
    }
    
    public boolean isTaped() {
        return tap;
    }

}

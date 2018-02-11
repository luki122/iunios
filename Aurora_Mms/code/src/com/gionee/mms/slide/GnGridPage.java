/*
 * Copyright (C) 2012 gionee Inc.
 *
 * Author:gaoj
 *
 * Description:class for holding the data of recent contact data from database
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.slide;

import java.lang.reflect.Field;

import com.android.mms.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;

public class GnGridPage extends GridView {
    final static String TAG = "GnGridPage";
    final static int CAPACITY_AUTO = -1;
    final static int INVALID_PAGELAYOUT_STARTINDEX = -1;
    int mPageCapacity = CAPACITY_AUTO;
    int mNumRows = 0;
    int mNumColumns = 0;
    //Gionee jipengfei 2011-10-19 add for CR00396558 begin
    int mPreferredWidth = 0;
    int mPreferredHeight = 0;
    //Gionee jipengfei 2011-10-19 add for CR00396558 begin

    //actually been used by GnIconTextGridPageSlideShower as pageLayoutId, every page has a pageLayoutId associated with it.
    public Object mExtraId;
    public int mPageLayoutStartIndex = 0;
    
    public GnGridPage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray a =
            context.obtainStyledAttributes(attrs, R.styleable.GnGridPage,
                    0, 0);
        mPageLayoutStartIndex = a.getInt(R.styleable.GnGridPage_gnPageLayoutStartIndex, INVALID_PAGELAYOUT_STARTINDEX);
        mPageCapacity = a.getInt(R.styleable.GnGridPage_gnPageCapacity, CAPACITY_AUTO);
        
        a.recycle();
    }

    public GnGridPage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GnGridPage(Context context) {
        super(context);
    }
    
    public void setCapacity(int capacity){
        mPageCapacity = capacity;
    }
    
    public int getCapacity(int capacity){
        if(mNumRows > 0 && mNumColumns > 0){
            int actualCapacity = mNumRows * mNumColumns;
            if(mPageCapacity == CAPACITY_AUTO){
                return actualCapacity;
            }else{
                return Math.min(actualCapacity, mPageCapacity);
            }
        }
        return mPageCapacity;
    }
    
    public int getPageLayoutStartIndex(){
        return mPageLayoutStartIndex;
    }
    
    public GnGridPage cloneCharacter(){
        GnGridPage clonee = new GnGridPage(getContext());
//        clonee.mPageCapacity = this.mPageCapacity;
//        clonee.mExtraId = this.mExtraId;
//        clonee.mPageLayoutStartIndex = this.mPageLayoutStartIndex;
//        int measuredWidth = this.getMeasuredWidth();
//        int measuredHeight = this.getMeasuredHeight();
//        clonee.setMeasuredDimension(measuredWidth, measuredHeight);
//        clonee.setStretchMode(this.getStretchMode());
//        transferSuperField(this, clonee, "mColumnWidth");
//        transferSuperField(this, clonee, "mNumColumns");
//        transferSuperField(this, clonee, "mHorizontalSpacing");
//        transferSuperField(this, clonee, "mVerticalSpacing");
//        transferSuperField(this, clonee, "mGravity");
//        transferSuperField(this, clonee, "mStretchMode");
//        transferSuperField(this, clonee, "mItemCount");
//        clonee.mNumColumns = this.mNumColumns;
//        clonee.mNumRows = this.mNumRows;
//        ByteArrayOutputStream baop =new ByteArrayOutputStream();
//        ObjectOutputStream oop;
//        try {
//            oop = new ObjectOutputStream(baop);
//            oop.writeObject(this);
//            ByteArrayInputStream bais = new ByteArrayInputStream(baop.toByteArray());
//            ObjectInputStream ois = new ObjectInputStream(bais);
//
//            return (GnGridPage)ois.readObject();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        return clonee;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Sets up mListPadding
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        ListAdapter adapter = getAdapter();
        //this child has been measured in super
        View child = adapter.getView(0, null, this);
//        child.measure(0, 0);
        int childHeight = child.getMeasuredHeight();
        final int verticalSpacing = stealVerticalSpacing();
        final int rowHeight = childHeight + verticalSpacing;
        mNumRows = (heightSize + verticalSpacing) / rowHeight;
        mNumColumns = stealColumnCountFromSuper();

        //Gionee jipengfei 2011-10-19 add for CR00396558 begin
        mPreferredWidth = this.getMeasuredWidth();
        mPreferredHeight = mNumRows * rowHeight - verticalSpacing;
        //Gionee jipengfei 2011-10-19 add for CR00396558 end
        Log.d(TAG, "nNumRows="+ mNumRows + ";nNumColumns="+mNumColumns);
    }
    
    public int stealColumnCountFromSuper(){
        try {
            Field nColumnCount;
            nColumnCount = this.getClass().getSuperclass()
            .getDeclaredField("mNumColumns");
            nColumnCount.setAccessible(true);
            return (Integer)nColumnCount.get(this);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
    
    public int stealVerticalSpacing(){
        try {
            Field verticalSpacing;
            verticalSpacing = this.getClass().getSuperclass()
            .getDeclaredField("mVerticalSpacing");
            verticalSpacing.setAccessible(true);
            return (Integer)verticalSpacing.get(this);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0;
    }
    
    private void transferSuperField(GnGridPage from, GnGridPage to, String fieldName) {
        try {
            Field fieldFrom;
            Field fieldTo;
            fieldFrom = from.getClass().getSuperclass()
            .getDeclaredField(fieldName);
            fieldFrom.setAccessible(true);
            
            
            fieldTo = from.getClass().getSuperclass()
            .getDeclaredField(fieldName);
            fieldTo.setAccessible(true);
            
            fieldTo.set(to, fieldFrom.get(this));
            
            
            Field fieldTest = from.getClass().getSuperclass()
            .getDeclaredField(fieldName);
            fieldTest.setAccessible(true);
            
            int a = (Integer)fieldFrom.get(this);
            int b = (Integer)fieldTest.get(this);
            assert(a==b);
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public int getMeasuredCol(){
        return mNumColumns;
    }
    
    public int getMeasuredRow(){
        return mNumRows;
    }
    //Gionee jipengfei 2011-10-19 add for CR00396558 begin
    public int getPreferredHeight(){
        return mPreferredHeight;
    }
    
    public int getPreferredWidth(){
        return mPreferredWidth;
    }
    //Gionee jipengfei 2011-10-19 add for CR00396558 end

    public void setExtraId(Object id){
        mExtraId = id;
    }
    
    public Object getExtraId( ){
        return mExtraId;
    }
    
    @Override 
    public boolean equals(Object o){
//        if(o instanceof GnGridPage){
//            GnGridPage other = (GnGridPage)(o);
//            if(other.mPageCapacity == this.mPageCapacity || 
//                    other.mPageCapacity == this.mPageCapacity ||
//                    other.mPageLayoutStartIndex == this.mPageLayoutStartIndex ||
//                    other.mExtraId == this.mExtraId){
//                return true;
//            }
//        }
//        return false;
        return this == o;
    }
    
    // gionee zhouyj 2012-10-10 add for CR00710042 start 
    @Override
    protected void onDetachedFromWindow() {
        try {
            Field f = this.getClass().getSuperclass().getSuperclass().getDeclaredField("mDataSetObserver");
            // gionee zhouyj 2012-11-20 add for CR00733978 start 
            f.setAccessible(true);
            // gionee zhouyj 2012-11-20 add for CR00733978 end 
            if (f != null && f.get(this) == null) {
                setAdapter(null);
            }
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "GnGridPage   onDetachedFromWindow   NoSuchFieldException   " + e.toString());
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "GnGridPage   onDetachedFromWindow   IllegalArgumentException   " + e.toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e(TAG, "GnGridPage   onDetachedFromWindow   Exception   " + e.toString());
        }
        super.onDetachedFromWindow();
    }
    // gionee zhouyj 2012-10-10 add for CR00710042 end 
}

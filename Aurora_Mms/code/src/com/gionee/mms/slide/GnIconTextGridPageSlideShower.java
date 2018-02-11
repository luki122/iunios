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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;

import com.android.mms.MmsApp;
import com.android.mms.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;

public class GnIconTextGridPageSlideShower extends GnViewSlideHost{
    public final static String TAG = "GnIconTextGridPageSlideShower";
    private boolean mNeedRearrange = false;
    
    private final static int mDefaultLayoutId = R.layout.gn_gridpage;
    OnItemLongClickListener mOnItemLongClickListenerAdapter;
    OnItemLongClickListener mOnItemLongClickListenerUser;
    OnItemClickListener mOnItemClickListenerAdapter;
    OnItemClickListener mOnItemClickListenerUser;
    OnItemSelectedListener mOnItemSelectedListenerAdapter;
    OnItemSelectedListener mOnItemSelectedListenerUser;
    OnGridPageFullListener mOnGridPageFullListener;
    int mSavedWidthMeasureSpec;
    int mSavedHeightMeasureSpec;
    int mGridPagePreferredWidth;
    int mGridPagePreferredHeight;
    
    /*store every page's information, keep it ordered by page start index
     * for example, if user set page 0 to layout id 2343, page 3 to layout id 2543, page 6 to layout id 2564.
     * then page 0, 1, 2 --> layout id 2343
     *      page 3, 4, 5 --> layout id 2543
     *      page 6, ...  --> layout id 2564
     */
    ArrayList< GnPageInfo > mPageLayoutIds = new ArrayList< GnPageInfo >(3);
    
    BackUpData<ViewItemInfo> mDatum = new BackUpData<ViewItemInfo>();
    Recycler<View> recycler = new Recycler<View>();
    
    public GnIconTextGridPageSlideShower(Context context) {
        super(context);
    }
    
    public GnIconTextGridPageSlideShower(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mOnItemClickListenerAdapter = new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(null != mOnItemClickListenerUser){
                    Log.d(TAG, "onItemClick");
                    int globalPosition = calcItempositionBase(parent, position);
                    if(globalPosition >= 0){
                        mOnItemClickListenerUser.onItemClick(parent, view, globalPosition, id);
                    }else{
                        Log.e(TAG, "fatal error: unexpected parent");
                    }
                }
            }
        };
        mOnItemLongClickListenerAdapter = new OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(null != mOnItemLongClickListenerUser){
                    int globalPosition = calcItempositionBase(parent, position);
                    if(globalPosition >= 0){
                        return mOnItemLongClickListenerUser.onItemLongClick(parent, view, globalPosition, id);
                    }else{
                        Log.e(TAG, "fatal error: unexpected parent");
                    }
                }
                return false;
            }
            
        }; 
        
        mOnItemSelectedListenerAdapter = new OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(null != mOnItemSelectedListenerUser){
                    int globalPosition = calcItempositionBase(parent, position);
                    if(globalPosition >= 0){
                        mOnItemSelectedListenerUser.onItemSelected(parent, view, globalPosition, id);
                    }else{
                        Log.e(TAG, "fatal error: unexpected parent");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                if(null != mOnItemSelectedListenerUser){
                    mOnItemSelectedListenerUser.onNothingSelected(arg0);
                }
            }
            
        };
        mPageLayoutIds.add(new GnPageInfo(mDefaultLayoutId, 0));
        Drawable background = this.getBackground();
        /*if(background == null){
            //gionee gaoj 2012-6-25 added for CR00601735 CR00628127 start
            if (MmsApp.mDarkStyle) {
                this.setBackgroundResource(R.drawable.gn_icontextgridpage_bg_dark);
            } else {
                //gionee gaoj added for CR00725602 20121201 start
                this.setBackgroundResource(R.drawable.gn_icontextgridpage_bg);
                //gionee gaoj added for CR00725602 20121201 end
            }
            //gionee gaoj 2012-6-25 added for CR00601735 CR00628127 end
        }*/
    }
    
    public GnIconTextGridPageSlideShower(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    protected void onSetUp(){
        //construct layoutids from child
        GnViewSlideShower shower = getSlideShower();
        int count = shower.getChildCount();
        int lastPageStartIndex = -1;
        for(int i=0; i<count; ++i){
            GnGridPage page = (GnGridPage)shower.getChildAt(i);
            int pageLayoutStartIndex = page.getPageLayoutStartIndex();
            if(pageLayoutStartIndex == GnGridPage.INVALID_PAGELAYOUT_STARTINDEX){
                pageLayoutStartIndex = lastPageStartIndex + 1;
            }
            lastPageStartIndex = pageLayoutStartIndex;
            page.setExtraId(page);
            addListenersToPage(page);
            setPageLayoutResId(pageLayoutStartIndex, page);
        }
        unLoadPages();
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(mSavedWidthMeasureSpec != widthMeasureSpec || mSavedHeightMeasureSpec != heightMeasureSpec){
            mNeedRearrange = true;
            mSavedWidthMeasureSpec = widthMeasureSpec;
            mSavedHeightMeasureSpec = heightMeasureSpec;
        }
        if(mNeedRearrange ==  true){
            embedPageForMeasure();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if(mNeedRearrange == true){
            collectMeasuredInfo();
            reassignItemsRecycle();
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mNeedRearrange = false;
        }
    }
    
    private void unLoadPages(){
        GnViewSlideShower shower = getSlideShower();
        int childCount = shower.getChildCount();
        for(int i=0; i<childCount; ++i){
            GnGridPage page = (GnGridPage) shower.getChildAt(i);
            recycler.add(page.getExtraId(), page);
        }
        shower.removeAllViews();
        getSlideIndicator().clear();
    }
    
    protected void embedPageForMeasure(){
        unLoadPages();
       
        //load pages 
        int layoutIdCount = mPageLayoutIds.size();
        for(int i=0; i<layoutIdCount; ++i){
            GnPageInfo info = mPageLayoutIds.get(i);
            GnGridPage page = (GnGridPage)recycler.pick(info.mLayoutResId);
            if(null == page){
                page = constructGridPage(mPageLayoutIds.get(i).mLayoutResId);
            }
            if(null == page.getAdapter()){
                page.setAdapter(new ViewItemAdapter(0, mDatum.size(), mDatum));
            }
            //Gionee jipengfei  2011-10-19 add for CR00396881 begin
            ViewGroup.LayoutParams lp = page.getLayoutParams();
            if (lp == null) {
                lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                page.setLayoutParams(lp);
            } else {
                lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            //Gionee jipengfei  2011-10-19 add for CR00396881 end
            addSlidePage(page);
        }
    }
    
    private GnPageInfo getPageInfoByLayoutId(Object layoutId){
        int layoutIdCount = mPageLayoutIds.size();
        for(int i=0; i<layoutIdCount; ++i){
            GnPageInfo info = mPageLayoutIds.get(i);
            if(info.mLayoutResId.equals(layoutId)){
                return info;
            }
        }
        return null;
    }
    
    protected void collectMeasuredInfo(){
        GnViewSlideShower shower = getSlideShower();
        int childCount = shower.getChildCount();
        for(int i=0; i<childCount; ++i){
            GnGridPage page = (GnGridPage)shower.getChildAt(i);
            GnPageInfo pageInfo = getPageInfoByLayoutId(page.getExtraId());
            if(pageInfo == null){
                Log.e(TAG, "Fatal: no pageInfo of layoutid:"+page.getExtraId());
                continue;
            }
            pageInfo.mMeasuredCol = page.getMeasuredCol();
            pageInfo.mMeasuredRow = page.getMeasuredRow();
            //Gionee jipengfei 2011-10-19 add for CR00396558 begin
            if (i == 0){
                //All pages has the same size as the first page
                mGridPagePreferredWidth = page.getPreferredWidth();
                mGridPagePreferredHeight = page.getPreferredHeight();
            }
            //Gionee jipengfei 2011-10-19 add for CR00396558 end
        }
    }
    
    private void reassignItemsRecycle(){
        unLoadPages();
        int totalItems = mDatum.size();
        for(int curItemCnt = 0, pageIndex = 0; curItemCnt < totalItems; ++ pageIndex){
            GnPageInfo info = getPageInfoByPageIndex(pageIndex);
            Object layoutId = info.mLayoutResId;
            int pageItemCount = info.mMeasuredCol * info.mMeasuredRow;
            GnGridPage gridPage = (GnGridPage) recycler.pick(layoutId);
            if(gridPage == null){
                gridPage = constructGridPage(layoutId);
            }
            if(pageItemCount > totalItems - curItemCnt){
                pageItemCount = totalItems - curItemCnt;
            }
            gridPage.setAdapter(new ViewItemAdapter(curItemCnt, pageItemCount, mDatum));
            curItemCnt += pageItemCount;
            //Gionee jipengfei  2011-10-19 add for CR00396881 begin
            ViewGroup.LayoutParams lp = gridPage.getLayoutParams();
            if (lp == null) {
                lp = new FrameLayout.LayoutParams(mGridPagePreferredWidth, mGridPagePreferredHeight);
                gridPage.setLayoutParams(lp);
            } else {
                lp.width = mGridPagePreferredWidth;
                lp.height = mGridPagePreferredHeight;
            }
            //Gionee jipengfei  2011-10-19 add for CR00396881 end
            addSlidePage(gridPage);    
        }
    }
    
    public GnPageInfo getPageInfoByPageIndex(int index){
        int count = mPageLayoutIds.size();
        int find = 0;
        for(; find<count; ++find){
            GnPageInfo info = mPageLayoutIds.get(find);
            if (info.mPageIndex > index){
                return mPageLayoutIds.get(find - 1);
            }else if(info.mPageIndex == index){
                return mPageLayoutIds.get(find);
            }
        }
        return mPageLayoutIds.get(count - 1);
    }
    
    public void setPageLayoutResId(int index, Object layoutResId){
        mNeedRearrange = true;
        int count = mPageLayoutIds.size();
        int insertPos=0;
        for(; insertPos<count; ++insertPos){
            GnPageInfo info = mPageLayoutIds.get(insertPos);
            if (info.mPageIndex > index){
                break;
            }else if(info.mPageIndex == index){
                info.mLayoutResId = layoutResId;
                return;
            }
        }
        GnPageInfo pageInfo = new GnPageInfo(layoutResId, index);
        mPageLayoutIds.add(insertPos, pageInfo);
    }
    
    public void setPageLayoutResId(int index, int layoutResId){
        Integer boxer = layoutResId;
        setPageLayoutResId(index, boxer);
    }
    
    public void pushItemResources(Object label, int icon, long id){
        View itemView = createItemViewFromResource(label, icon);
        pushItemView(itemView, id);
    }
    
    public void pushItemView(View v, long id){
        ViewItemInfo vii = new ViewItemInfo(v, id);
        mDatum.add(vii);
        mNeedRearrange = true;
    }
    
    public void selectPage(int index){
        selectSlide(index);
    }
    
    private GnGridPage constructGridPage(Object layoutId){
        final Context context = getContext();
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        GnGridPage gridPage = null;
        if(layoutId instanceof Integer ){
            gridPage = (GnGridPage) inflater.inflate((Integer)layoutId,
                    getSlideShower(), // tab widget is the parent
                    false); // no inflate params
        }else if (layoutId instanceof GnGridPage){
            gridPage = ((GnGridPage)layoutId).cloneCharacter();
        }else{
            throw new RuntimeException();
        }
        gridPage.setExtraId(layoutId);
        addListenersToPage(gridPage);
        // Aurora xuyong 2013-10-12 added for aurora's new feature start 
        gridPage.setVerticalSpacing(0);
        gridPage.setHorizontalSpacing(0);
        gridPage.setLayoutParams(new android.view.ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // Aurora xuyong 2013-10-12 added for aurora's new feature end
        
        return gridPage;
    }

    private void addListenersToPage(GnGridPage gridPage) {
        gridPage.setOnItemClickListener(mOnItemClickListenerAdapter);
        gridPage.setOnItemLongClickListener(mOnItemLongClickListenerAdapter);
        gridPage.setOnItemSelectedListener(mOnItemSelectedListenerAdapter);
    }

    public View createItemViewFromResource(Object label, int icon){
        final Context context = getContext();
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.gn_icontextgridpage_item,
                null, // tab widget is the parent
                false); // no inflate params
        final TextView tv = (TextView) itemView.findViewById(R.id.label);
        if(label instanceof Integer){
            // Aurora xuyong 2013-10-12 modified for aurora's new feature start 
            if ((Integer)label == 0) {
                tv.setText("");
            } else {
                tv.setText((Integer)label);
            }
            // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        }else if(label instanceof String){
            tv.setText((String)label);
        }
        if (tv.length() <= 0 ) {
            tv.setVisibility(View.GONE);
        }
        final ImageView iconView = (ImageView) itemView.findViewById(R.id.icon);
        // Aurora xuyong 2013-10-12 modified for aurora's new feature start 
        if (icon == 0) {
            iconView.setImageBitmap(null);
        } else {
            iconView.setImageResource(icon);
        }
        // Aurora xuyong 2015-03-09 modified for aurora's new feature start
        if (icon == R.drawable.aurora_attach_contact_selector) {
        // Aurora xuyong 2015-03-09 modified for aurora's new feature end
            final TextView leftline = (TextView) itemView.findViewById(R.id.aurora_left_line);
            leftline.setVisibility(View.GONE);
        }
        // Aurora xuyong 2013-10-12 modified for aurora's new feature end
        return itemView;
    }
    
    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mOnItemSelectedListenerUser = listener;
    }
    
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListenerUser = listener;
    }
    
    public void setOnItemLongClickListener(OnItemLongClickListener listener){
        mOnItemLongClickListenerUser = listener;
    }
    public void setOnGridPageFullListener(OnGridPageFullListener listener){
        mOnGridPageFullListener = listener;
    }
    
    private int calcItempositionBase(AdapterView<?> parent, int position){
        int positionBase = 0;
        boolean bFound = false;
        GnViewSlideShower grandpa = getSlideShower();
        int nParents = grandpa.getChildCount();
        for(int i=0; i<nParents; ++i){
            if(grandpa.getChildAt(i) == parent){
                bFound = true;
                break;
            }
            positionBase += parent.getChildCount();
        }
        if(true == bFound){
            return positionBase + position;
        }else{
            return -1;
        }
    }
    
    public interface OnGridPageFullListener{
        public void onGridPageFull(GnGridPage gridPage, int pagePos);
    }
    
    public static class ViewItemInfo{
        View view;
        long itemId;
        public ViewItemInfo(View itemView, long id){
            this.view = itemView;
            this.itemId = id;
        }
    }
    
    public static class BackUpData<T> extends Observable{
        private ArrayList<T> datum = new ArrayList<T>(32);
        public void add(T item){
            datum.add(item);
            notifyObservers();
        }
        int size(){
            return datum.size();
        }
        T get(int pos){
            return datum.get(pos);
        }
    }
    
    public static class Recycler<T>{
        final static int RECYLCER_HASH_SIZE = 3;
        final static int ARR_INIT_SIZE = 8;
        HashMap<Object, ArrayList<T> > recycleList = new HashMap<Object, ArrayList<T>>(RECYLCER_HASH_SIZE);
        
        void add(Object id, T e){
            ArrayList<T> list = recycleList.get(id);
            if(list == null){
                list = new ArrayList<T>(ARR_INIT_SIZE);
                recycleList.put(id, list);
            }
            
            if(false == list.contains(e)){
                list.add(e);
            }
        }
        
//        T get(int id){
//            ArrayList<T> list = recycleList.get(id);
//            return list.get(list.size() - 1);
//        }
        
        T pick(Object id){
            ArrayList<T> list = recycleList.get(id);
            if(list != null && list.size() > 0){
                return list.remove(list.size() - 1);
            }
            return null;
        }
        
        void clear(){
            recycleList.clear();
        }
    }
    
    public static class ViewItemAdapter extends BaseAdapter{
        
        int start;
        int count;
        private BackUpData<ViewItemInfo> mViewItems = null;
        
        public ViewItemAdapter(int start, int count, BackUpData<ViewItemInfo> backupData){
            this.start = start;
            this.count = count;
            this.mViewItems = backupData;
        }
        
        public void setRange(int start, int count){
            this.start = start;
            this.count = count;
            notifyDataSetChanged();
        }
        
        public void setData( BackUpData<ViewItemInfo> dataList){
            this.mViewItems = dataList;
        }
        
        @Override
        public int getCount() {
            if(count < 0){
                return mViewItems.size();
            }
            return count;
        }

        @Override
        public Object getItem(int position) {
            return mViewItems.get(position + start);
        }

        @Override
        public long getItemId(int position) {
            return mViewItems.get(position + start).itemId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return mViewItems.get(position + start).view;
        }
    }
    private static class GnPageInfo{
        GnPageInfo(int layoutId, int pageIndex){
            Integer layoutIdObj = layoutId;
            mLayoutResId = layoutIdObj;
            mPageIndex = pageIndex;
        }
        
        GnPageInfo(Object layoutId, int pageIndex){
            mLayoutResId = layoutId;
            mPageIndex = pageIndex;
        }
        
        Object mLayoutResId;
        int mMeasuredRow;
        int mMeasuredCol;
        int mPageIndex;
        //int mCapacity = GnGridPage.CAPACITY_AUTO;
    }
}

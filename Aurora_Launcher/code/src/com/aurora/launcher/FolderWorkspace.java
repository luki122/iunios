package com.aurora.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.aurora.plugin.CalendarIcon;
import com.aurora.plugin.DynIconPlg;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author hao jingjing
 * 
 */
public class FolderWorkspace extends FolderPagedView{
	private static final String TAG = "FolderWorkspace";
	public static final int DEFAULT_CELL_COUNT_X = 4;
	public static final int DEFAULT_CELL_COUNT_Y = 4;
    private static final int MAX_NUM_PER_PAGE = DEFAULT_CELL_COUNT_X * DEFAULT_CELL_COUNT_Y;
    static final int INDEX_MAX_CELL_COUNT_X = DEFAULT_CELL_COUNT_X - 1;
    static final int INDEX_MAX_CELL_COUNT_Y = DEFAULT_CELL_COUNT_Y - 1;
    
    // 最多可以有多少个页面
    private static final int MAX_PAGE_NUM = 5;
    private static final int MAX_ITEMS_NUM = MAX_PAGE_NUM * MAX_NUM_PER_PAGE;
    
    private Launcher mLauncher;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longListener;
    
    private int mCellWidth;
    private int mCellHeight;
    
    private int mWidthGap;
    private int mHeightGap;
    
    protected FolderInfo mInfo;
    private int mNumPages;
    
    private final LayoutInflater mInflater;
    private final IconCache mIconCache;
    
    private ArrayList<View> mItemsInReadingOrder = new ArrayList<View>();
    private ArrayList<View> mAllItemsInReadingOrder = new ArrayList<View>();
    private ShortcutInfo mCurrentDragInfo;
    
    private int mIconDpi;
    private TextView mFolderAddIcon = null;
    
    private boolean mOverscrollTransformsSet;
    private static final float WORKSPACE_OVERSCROLL_ROTATION = 3500f;
    private int mCameraDistance;
    
	public FolderWorkspace(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public FolderWorkspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

    private int mContentPaddingTop;
    private int mContentPaddingButtom;
    
	// 获取到这些值后就可以使用
    public FolderWorkspace(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mInflater = LayoutInflater.from(context);
		mIconCache = ((LauncherApplication)context.getApplicationContext()).getIconCache();
		mContentIsRefreshable = false;
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);
		mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10);
        mCellHeight = a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10);
        mWidthGap = a.getDimensionPixelSize(R.styleable.CellLayout_widthGap, 0);
        mHeightGap = a.getDimensionPixelSize(R.styleable.CellLayout_heightGap, 0);
		a.recycle();
		
		mLauncher = (Launcher) context;
		setFocusableInTouchMode(true);
		
		ActivityManager activityManager =
	            (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	    mIconDpi = activityManager.getLauncherLargeIconDensity();
	    
	    mContentPaddingTop = getPaddingTop();
	    mContentPaddingButtom = getPaddingBottom();
	    
	    mCameraDistance = context.getResources().getInteger(R.integer.config_cameraDistance);
	}
    
	@Override
	public void syncPages() {
		// TODO Auto-generated method stub
	}
    
	@Override
	public void syncPageItems(int page, boolean immediate) {
		// TODO Auto-generated method stub
	}

	public void reArrangeChildren(){
		updatePages();
		placeInReadingOrder(mInfo.contents);
		settleChildrenView();
		// 因为所有调用到该函数的时候都会调用到settleChildrenView，其中对item更新会做数据库的修复，因此该操作完全是多余的
		// updateItemLocationsInDatabase();
	}
	
    private boolean judgeCellLayoutIsEmpty(CellLayout layout) {
    	boolean empty = true;
    	if(layout == null) return empty;
    	for (int j = 0; j < DEFAULT_CELL_COUNT_Y; j++) {
			for (int i = 0; i < DEFAULT_CELL_COUNT_X; i++) {
				View v = layout.getChildAt(i, j);
				if (v != null && !(v.getTag() instanceof FolderAddInfo)) {
					empty = false;
					break;
				} 
			}
    	}
    	return empty;
    }
	
	private void updateIndicator(){
		int count = getChildCount();
		if(getCurrentPage() > count - 1) {
			// 开始使用的是snapToPage，后来发现setCurrentPage也可以实现该效果，暂不知会否有bug
			// Log.e("HJJ", "undo new current page===>" + (count - 1));
			setCurrentPage(count - 1);
			//snapToPage(count - 1, 10);
		}
		((FolderPageIndicator) getScrollingIndicator()).updatePoints(getContext(), count);
		if(getChildCount() == 1){
			getScrollingIndicator().setVisibility(View.INVISIBLE);
		}
	}
	
	private void updatePages() {
		mNumPages = Math.min(MAX_PAGE_NUM, (int) Math.ceil((float) mInfo.contents.size() / (DEFAULT_CELL_COUNT_X * DEFAULT_CELL_COUNT_Y)));
		int childCount = getChildCount();
		if(mNumPages == 0){
			CellLayout layout = new CellLayout(getContext(), mCellWidth, mCellHeight, mWidthGap, mHeightGap, DEFAULT_CELL_COUNT_X, DEFAULT_CELL_COUNT_Y);
			addView(layout);
			//AURORA-START:xiejun:20130923:ID139
			layout.setOnClickListener(clickListener);
			//AURORA-END:xiejun:20130923:ID139
		} else if(childCount == mNumPages){
			return;
		} else if(childCount < mNumPages){
			for (int i = childCount; i < mNumPages; i++) {
				CellLayout layout = new CellLayout(getContext(), mCellWidth, mCellHeight, mWidthGap, mHeightGap, DEFAULT_CELL_COUNT_X, DEFAULT_CELL_COUNT_X);
				addView(layout);
				//AURORA-START:xiejun:20130923:ID139
				layout.setOnClickListener(clickListener);
				//AURORA-END:xiejun:20130923:ID139
			}
		} else if(childCount > mNumPages){
			for(int i= childCount - 1; i>=mNumPages; i--){
				CellLayout layout = (CellLayout)getChildAt(i);
				layout.removeAllViews();
				removeView(layout);
			}
		}
		
		((Folder) getParent()).setParamsAndCenter();
		updateIndicator();
	}
	
	protected boolean isFull() {
		return getItemCount() >= MAX_ITEMS_NUM;
    }
	
	protected boolean findAndSetEmptyCells(int page, ShortcutInfo item) {
		int[] emptyCell = new int[2];
		CellLayout layout = (CellLayout) getChildAt(page);
		if (layout != null && layout.findCellForSpan(emptyCell, item.spanX, item.spanY)) {
			item.cellX = emptyCell[0];
			item.cellY = emptyCell[1];
			item.screen = page;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * the caller uses this method to place the specified item
	 * in the first vacancy of the whole folder
	 * @param item item is the icon to be placed in current folder
	 * @return return true if it succeeded in find a vacancy.
	 */
	private boolean findAndSetEmptyCellsInFolder(ShortcutInfo item) {
		boolean bFound = false;
		for(int ii = mNumPages - 1;ii >= 0;ii --) {
			bFound = findAndSetEmptyCells(ii, item);
			if(bFound) {
				return true;
			}
		}
		return false;
	}

    public void setFolderLongClickListener(View.OnLongClickListener listener){
    	longListener = listener;
    }
    
    public void setFolderClickListener(View.OnClickListener listener){
    	clickListener = listener;
    }
    
    private Drawable getFolderAddDrawable() {
        return getFullResIcon(getResources(),
                R.drawable.folder_add_icon);
    }
    
    private Drawable getFullResIcon(Resources resources, int iconId) {
        Drawable d;
        try {
            d = resources.getDrawableForDensity(iconId, mIconDpi);
        } catch (Resources.NotFoundException e) {
            d = null;
        }

        return (d != null) ? d : getFolderAddDrawable();
    }

    private Bitmap makeFolderAddBitmap() {
        Drawable d = getFolderAddDrawable();
        Bitmap b = Bitmap.createBitmap(Math.max(d.getIntrinsicWidth(), 1),
                Math.max(d.getIntrinsicHeight(), 1),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        d.setBounds(0, 0, b.getWidth(), b.getHeight());
        d.draw(c);
        c.setBitmap(null);
        return b;
    }
    
	protected boolean createAndAddShortcut(int page, ShortcutInfo item) {
		//vulcan changed TextView to BubbleTextView so as to monitoring creating and destroying of the dynamic icon.
		//in 2014-6-3
		//final TextView textView = (TextView) mInflater.inflate(
		//		R.layout.application, this, false);
		final BubbleTextView textView = (BubbleTextView) mInflater.inflate(
				R.layout.application, this, false);

		//vulcan added it in 2014-6-12
		//add feature: we use the newest icon to make an icon for new folder
		textView.mDynIconPlg = DynIconPlg.produceDynIconPlg(item,textView);
		if (textView.mDynIconPlg == null) {//indicates that it is not a dynamic icon
			textView.setCompoundDrawablesWithIntrinsicBounds(null,
					new FastBitmapDrawable(item.getIcon(mIconCache)), null,
					null);
		} else {//it is dynamic icon
			//vulcan added it in 2014-6-20
			//if desk clock icon is in a folder, we DONOT consider it as a dynamic icon.
			if (!DynIconPlg.viewIsDynamicClock(textView)) {
				Drawable d = textView.mDynIconPlg.getCurDrawContent();
				if (d != null) {
					textView.setCompoundDrawablesWithIntrinsicBounds(null, d,
							null, null);
				}
			}
			else {
				textView.setCompoundDrawablesWithIntrinsicBounds(null,
						new FastBitmapDrawable(item.getIcon(mIconCache)), null,
						null);
			}
		}
		textView.setText(item.title);
		textView.setTag(item);

		if (item.newFlag) {
			((BubbleTextView)textView).setNewApp(true);
		}

		//vulcan moved it in 2014-6-12
		//textView.mDynIconPlg = DynIconPlg.produceDynIconPlg(item,textView);
		// vulcan added in 2014-6-3
		//if (item.title.toString().contains("时钟")) {
		//	textView.mDynIconPlg = new DynIconPlg((TextView)textView);
		//}
		
		
		textView.setOnClickListener(clickListener);
		textView.setOnLongClickListener(longListener);

		// We need to check here to verify that the given item's location isn't
		// already occupied
		// by another item.
		CellLayout layout = (CellLayout) getChildAt(page);
		if(layout == null){
			Log.e(TAG, "Can't get CellLayout, page:" + page);
			return false;
		}
		if (layout.getChildAt(item.cellX, item.cellY) != null || item.cellX < 0
				|| item.cellY < 0 || item.cellX >= layout.getCountX()
				|| item.cellY >= layout.getCountY()) {
			// This shouldn't happen, log it.
			Log.e(TAG, "Folder order not properly persisted during bind");
			if (!findAndSetEmptyCells(page, item)) {
				return false;
			}
		}

		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(item.cellX,
				item.cellY, item.spanX, item.spanY);
        // Aurora <jialf> <2013-10-31> remove for fix bug #323 begin
		// textView.setOnKeyListener(new FolderKeyEventListener());
        // Aurora <jialf> <2013-10-31> remove for fix bug #323 end
		
		//boolean insert = false;
		//layout.addViewToCellLayout(textView, insert ? 0:-1, (int) item.id, lp, true);
		layout.addViewToCellLayout(textView,  -1, (int) item.id, lp, true);
		return true;
	}
	   //add by xiexiujie for calender plugin icon start 10.13
	public void createAndAddCalenderPluginIcon(int page) {

		CellLayout layout;
		int currPage = page;
		int[] vacant = new int[2];
		boolean hasCalender = false;
		ArrayList<View> list = getAllItemsInReadingOrder();
		// removeContentViews();

		for (int i = 0; i < list.size(); i++) {

			View v = list.get(i);

			ItemInfo info = (ItemInfo) v.getTag();

			ShortcutInfo item = (ShortcutInfo) info;

			if (item.getPackageName(item.intent).equals("com.android.calendar")) {
				hasCalender = true;
			}

		}
		list.clear();
		if (hasCalender) {

			ArrayList<ShortcutInfo> mylist = LauncherModel
					.getItemsByPackage(mLauncher);

			if (mylist.get(0).container != -100
					&& mylist.get(0).container != -101) {

				layout = (CellLayout) getChildAt(currPage);

				CellLayout.LayoutParams lp2 = new CellLayout.LayoutParams(
						mylist.get(0).cellX, mylist.get(0).cellY,
						mylist.get(0).spanX, mylist.get(0).spanY);
				int childId = LauncherModel.getCellLayoutChildId(
						mylist.get(0).container, mylist.get(0).screen,
						mylist.get(0).cellX, mylist.get(0).cellY,
						mylist.get(0).spanX, mylist.get(0).spanY);

				layout.addWorkSpaceCalenderViewToCellLayout(mLauncher.layout,
						-1, childId, lp2, true);
			}

		}

	}
	   //add by xiexiujie for calender plugin icon start 10.13

	private class GridComparator implements Comparator<ShortcutInfo> {
		@Override
		public int compare(ShortcutInfo lhs, ShortcutInfo rhs) {
			int lhIndex = lhs.screen * MAX_NUM_PER_PAGE + lhs.cellY
					* DEFAULT_CELL_COUNT_X + lhs.cellX;
			int rhIndex = rhs.screen * MAX_NUM_PER_PAGE + rhs.cellY
					* DEFAULT_CELL_COUNT_X + rhs.cellX;
			return (lhIndex - rhIndex);
		}
	}

    public View getViewForInfo(ShortcutInfo item) {
    	CellLayout layout = (CellLayout)getChildAt(item.screen);
    	View v = null;
    	if(null != layout) {
    		v = layout.getChildAt(item.cellX, item.cellY);
    	}
    	return v;
        /*for (int j = 0; j < layout.getCountY(); j++) {
            for (int i = 0; i < layout.getCountX(); i++) {
                View v = layout.getChildAt(i, j);
                if (null != v && v.getTag() == item) {
                    return v;
                }
            }
        }
        return null;*/
    }
    
    public void removeViewAtPage(int page, View view){
    	if(page <= mNumPages -1){
    		CellLayout layout = (CellLayout) getChildAt(page);
    		if(null != layout) layout.removeView(view);
    	}
    }
	
	public void onTitleChanged(CharSequence title) {
		// TODO Auto-generated method stub
		
	}

	public void onItemsChanged() {
		// TODO Auto-generated method stub
		
	}

	public void setMotionEventSplittingEnabled(boolean flag){
		int count = getChildCount();
		for(int i=0;i<count;i++){
			((CellLayout)getChildAt(i)).getShortcutsAndWidgets().setMotionEventSplittingEnabled(false);
		}
	}
	
    public View getViewForInfo(CellLayout layout, ShortcutInfo item) {
        for (int j = 0; j < DEFAULT_CELL_COUNT_Y; j++) {
            for (int i = 0; i < DEFAULT_CELL_COUNT_X; i++) {
                View v = layout.getChildAt(i, j);
                if (v.getTag() == item) {
                    return v;
                }
            }
        }
        return null;
    }
    
    public int getDesiredWidth() {
    	CellLayout layout = (CellLayout)getChildAt(0);
    	if(layout != null){
    		return getPaddingLeft() + layout.getDesiredWidth() + getPaddingRight();
    	} else {
    		return 0;
    	}
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	// TODO Auto-generated method stub
    	if(changed){
    		setPadding(0, mContentPaddingTop, 0, mContentPaddingButtom);
    	}
    	
    	super.onLayout(changed, left, top, right, bottom);
    }
    
    public int getDesiredHeight()  {
    	CellLayout layout = (CellLayout)getChildAt(0);
    	if(layout != null){
    		// Aurora <haojj> <2013-10-14> add for 在没有状态栏的情况得不到top和bottom的值 begin
    		// return getPaddingTop() + layout.getDesiredHeight() + getPaddingBottom();
    		return mContentPaddingTop + layout.getDesiredHeight() + mContentPaddingButtom;
    		// Aurora <haojj> <2013-10-14> end
    	} else {
    		return 0;
    	}
    }

	// add by xiexiujie for longclick bg start
	public int getDesiredHeight2() {
		CellLayout layout = (CellLayout) getChildAt(0);
		int childerNumb = getChildCountAtFristPage();
		if (layout != null) {
			if (childerNumb <= 15) {
				childerNumb = childerNumb + 1;
				if (childerNumb % 4 == 0) {
					return mContentPaddingTop + (childerNumb / 4)
							* layout.getCellHeight() + mContentPaddingButtom;
				} else {
					return mContentPaddingTop + (childerNumb/ 4 + 1)
							* layout.getCellHeight() + mContentPaddingButtom;

				}

			}
			return mContentPaddingTop + layout.getDesiredHeight()
					+ mContentPaddingButtom;

		} else {
			return 0;
		}
	}
  public int getChildCountAtFristPage(){
	  CellLayout layout = (CellLayout) getChildAt(0);
	  int childerNumb = 0;
	  if (layout != null) {
		  for (int i = 0; i < layout.getCountY(); i++) {
			 for (int j = 0; j < layout.getCountX(); j++) {
				  View v = layout.getChildAt(j, i);
				 if (null == v)
					  continue;
				  ItemInfo info = (ItemInfo) v.getTag();
				  if (info instanceof ShortcutInfo) {
					childerNumb++;
				  }
			  }

		  }
	
	  }else{
		  return 0;	
	}
	return childerNumb;
	
	
  }
	// add by xiexiujie for longclick bg end
    public View getItemAt(int childindex) {
    	CellLayout child = (CellLayout)getChildAt(getCurrentPage());
    	if(child != null){
    		return child.getShortcutsAndWidgets().getChildAt(childindex);
    	}
        return null;
    }
    
    public View getItemAt(int page, int childindex) {
    	CellLayout child = (CellLayout)getChildAt(page);
    	if(child != null){
    		return child.getShortcutsAndWidgets().getChildAt(childindex);
    	}
        return null;
    }
    
    public int getItemCount() {
    	int addViewCount = 0;
    	final int count = getChildCount();
    	CellLayout layout = ((CellLayout) getChildAt(count - 1));
    	for (int j = 0; j < DEFAULT_CELL_COUNT_Y; j++) {
			for (int i = 0; i < DEFAULT_CELL_COUNT_X; i++) {
				View v = layout.getChildAt(i, j);
				if (v != null) {
					Object tag = v.getTag();
					if(tag instanceof FolderAddInfo) {
						addViewCount = 1;
						break;
					}
				} else {
					break;
				}
			}
    	}
    	
    	int sum = 0;
    	for(int i=0;i<count;i++){
    		sum += ((CellLayout) getChildAt(i)).getShortcutsAndWidgets().getChildCount();
    	}
    	if(sum > 0) sum -= addViewCount;
    	return sum;
    }
    
    public int getImportItemsMaxCount(){
    	return MAX_ITEMS_NUM - getItemCount();
    }

    // Aurora <haojj> <2013-10-21> add for 移除icon的代码调整 begin
	void arrangeChildrenView(ArrayList<View> list) {
		CellLayout layout;
		int currPage = 0;
		int[] vacant = new int[2];

		removeContentViews();

		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			ItemInfo info = (ItemInfo) v.getTag();

			layout = (CellLayout) getChildAt(currPage);
			layout.getVacantCell(vacant, 1, 1);
			CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v
					.getLayoutParams();
			lp.cellX = vacant[0];
			lp.cellY = vacant[1];

			if (info.screen != currPage || info.cellX != vacant[0]
					|| info.cellY != vacant[1]) {
				info.screen = currPage;
				info.cellX = vacant[0];
				info.cellY = vacant[1];
				LauncherModel.addOrMoveItemInDatabase(mLauncher, info,
						mInfo.id, info.screen, info.cellX, info.cellY);
			}
			
			//boolean insert = false;
			//layout.addViewToCellLayout(v, insert ? 0 : -1, (int) info.id, lp, true);
			layout.addViewToCellLayout(v, -1, (int) info.id, lp, true);

			if (((i + 1) % MAX_NUM_PER_PAGE) == 0) {
				currPage++;
			}
		}
	}
    
	public void onRemove(ShortcutInfo item) {
		// 这个可以改进为只动某一部分
		setDataIsReady();
		
		ArrayList<View> list = getAllItemsInReadingOrder();
		View v = getViewForInfo(item);
		if(v != null) {
			list.remove(v);
		}
		updatePages();
		placeInReadingOrder(mInfo.contents);
		arrangeChildrenView(list);
		list.clear();
	}
	
	public void onMultiRemove(ArrayList<ShortcutInfo> items) {
		// 这个可以改进为只动某一部分
		setDataIsReady();
		
		ArrayList<View> list = getAllItemsInReadingOrder();
		for (int i = 0; i < items.size(); i++) {
			ShortcutInfo item = items.get(i);
			View v = getViewForInfo(item);
			if(v != null) {
				list.remove(v);
			}
		}
		updatePages();
		placeInReadingOrder(mInfo.contents);
		arrangeChildrenView(list);
		list.clear();
	}
	// Aurora <haojj> <2013-10-21> end
    
    // Aurora <haojj> <2013-10-21> add for 添加icon时的代码调整 begin
    public void onAdd(ShortcutInfo item) {
		// TODO Auto-generated method stub
        if (!findAndSetEmptyCells(mNumPages - 1, item)) {
            // The current layout is full, can we expand it?
        	updatePages();
        	
        	//vulcan changed from findAndSetEmptyCells to findAndSetEmptyCellsInFolder in 2014-7-18
            //findAndSetEmptyCells(mNumPages - 1, item);
        	boolean bFound = findAndSetEmptyCellsInFolder(item);
        	Log.d("vulcan-80","FolderWorksapce.onAdd: findAndSetEmptyCellsInFolder = " + bFound);
        }
        
        //vulcan changed from "mNumPages - 1" to "item.screen" in 2014-7-18
        //createAndAddShortcut(mNumPages - 1, item);
        createAndAddShortcut(item.screen, item);
        LauncherModel.addOrMoveItemInDatabase(
                mLauncher, item, mInfo.id, item.screen, item.cellX, item.cellY);
	}
	
	public void onAdd(ShortcutInfo item, int[] empty) {
        item.cellX = empty[0];
        item.cellY = empty[1];
        item.screen = empty[2];
        
        if(createAndAddShortcut(item.screen, item)){
        	LauncherModel.addOrMoveItemInDatabase(
                    mLauncher, item, mInfo.id, item.screen, item.cellX, item.cellY);
        } else {
        	onAdd(item);
        	reArrangeChildren();
        }
	}
	
	public void updateShortcutItem(ShortcutInfo item){
		View v = getViewForInfo(item);
		//2015-03-02 BUG#11532
		if(v == null){
			return;
		}
		//END
		BubbleTextView textView = ((BubbleTextView)v);
		item.updateIcon(mIconCache);
		textView.applyFromShortcutInfo(item, mIconCache);
	}
	
	public void onMultiAdd(ArrayList<ShortcutInfo> items) {
		// TODO Auto-generated method stub
		for(int i=0;i<items.size();i++){
			ShortcutInfo item = items.get(i);
			item.screen = MAX_PAGE_NUM;
			item.cellX = 0;
			item.cellY = 0;
		}
		
		updatePages();
        placeInReadingOrder(mInfo.contents);
        
        ArrayList<ShortcutInfo> overflow = new ArrayList<ShortcutInfo>();
        for (int i = 0; i < items.size(); i++) {
            ShortcutInfo child = (ShortcutInfo) items.get(i);
            if (createAndAddShortcut(child.screen, child)) {
            	LauncherModel.addOrMoveItemInDatabase(
                        mLauncher, child, mInfo.id, child.screen, child.cellX, child.cellY);
            } else {
            	overflow.add(child);
            }
        }
        
        // We rearrange the items in case there are any empty gaps
        settleChildrenView();

        // If our folder has too many items we prune them from the list. This is an issue 
        // when upgrading from the old Folders implementation which could contain an unlimited
        // number of items.
        for (ShortcutInfo item: overflow) {
        	mInfo.remove(item);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }
        
        // 因为所有调用到该函数的时候都会调用到settleChildrenView，其中对item更新会做数据库的修复，因此该操作完全是多余的
        // updateItemLocationsInDatabase();
	}
	// Aurora <haojj> <2013-10-21> end
    
    // Aurora <haojj> <2013-10-21> add for 绑定children begin
    public void setCurrentDragInfo(ShortcutInfo info){
    	mCurrentDragInfo = info;
    }
    
    
    //vulcan created and tested it in 2014-6-19
    //query the icons of folder for previewing the content of folder
	public ArrayList<View> getDynPreviewItems(int previewNum) {
    	ArrayList<View> previewItems = new ArrayList<View>();
    	int pageNum = getChildCount();
    	int previewCount = 0;
    	if(pageNum > 0 && previewNum > 0) {
    		//Log.d("vulcan-iconop","getPreviewItems: previewNum = " + previewNum);
    		CellLayout layout = (CellLayout) getChildAt(0);
    		if(layout != null){
    			for (int j = 0; j < DEFAULT_CELL_COUNT_Y; j++) {
    				for (int i = 0; i < DEFAULT_CELL_COUNT_X; i++) {    					
    					View v = layout.getChildAt(i, j);
    					if(v instanceof BubbleTextView) {
    						BubbleTextView iconView = (BubbleTextView)v;
    						Object tag = iconView.getTag();
    						//DONOT add the button icon to add other icon
    						if(tag instanceof FolderAddInfo) {
    							continue;
    						}
    						else if(tag instanceof ShortcutInfo) {
    							//it arrives here indicates it is a icon of the preview of the folder
								previewCount++;
								if(DynIconPlg.viewIsDynamic(iconView)) {
									if(!DynIconPlg.viewIsDynamicClock(iconView)){
										previewItems.add(iconView);
										Log.d("vulcan-iconop","getPreviewItems:" + iconView.getText());
									}
								}
    						}
    						if(previewCount >= previewNum) {
    							return previewItems;
    						}
    					}
    				}
    			}
    		}
    	}
    	return previewItems;
    }
    
    public ArrayList<View> getItemsInReadingOrder(final int page) {
        return getItemsInReadingOrder(page, true);
    }

    public ArrayList<View> getItemsInReadingOrder(final int page, boolean includeCurrentDragItem) {
		mItemsInReadingOrder.clear();
		CellLayout layout = (CellLayout) getChildAt(page);
		if(layout != null){
			for (int j = 0; j < DEFAULT_CELL_COUNT_Y; j++) {
				for (int i = 0; i < DEFAULT_CELL_COUNT_X; i++) {
					View v = layout.getChildAt(i, j);
					if (v != null) {
						Object tag = v.getTag();
						// 添加用于处理＋这个图标
						if(tag instanceof FolderAddInfo) continue;
						ShortcutInfo info = (ShortcutInfo) v.getTag();
						if (info != mCurrentDragInfo || includeCurrentDragItem) {
							mItemsInReadingOrder.add(v);
						}
					}
				}
			}
		}
        return mItemsInReadingOrder;
    }
    
    public ArrayList<View> getAllItemsInReadingOrder() {
        return getAllItemsInReadingOrder(true);
    }
    
    private ArrayList<View> getAllItemsInReadingOrder(boolean includeCurrentDragItem) {
    	mAllItemsInReadingOrder.clear();
    	
    	int size = getChildCount();
    	for(int child = 0; child < size; child++){
    		ArrayList<View> li = getItemsInReadingOrder(child, includeCurrentDragItem);
    		mAllItemsInReadingOrder.addAll(li);
    		li.clear();
    	}
        return mAllItemsInReadingOrder;
    }
    
    private void removeContentViews(){
    	for(int i = 0; i < getChildCount(); i++){
			CellLayout layout = (CellLayout) getChildAt(i);
			layout.removeAllViews();
		}
    }
    
    /**
     * reload newest itemInfo in this folder
     */
	void reloadContentItemInfo(FolderInfo fi) {		
		ArrayList<View> list = getAllItemsInReadingOrder();
		for (int i = 0; i < list.size(); i++) {
			View v = list.get(i);
			if(v.getTag() instanceof ShortcutInfo) {
				ShortcutInfo si = (ShortcutInfo) v.getTag();
				long itemId = si.id;
				for(int j = 0; j < fi.contents.size(); j ++) {
					if(fi.contents.get(j).id == itemId) {
						v.setTag(fi.contents.get(j));
					}
				}
			}
		}
		
		return;
	}
    
    void settleChildrenView() {
    	CellLayout layout;
        int currPage = 0;
        int[] vacant = new int[2];
        
        ArrayList<View> list = getAllItemsInReadingOrder();
        removeContentViews();

        for (int i = 0; i < list.size(); i++) {
            View v = list.get(i);
            ItemInfo info = (ItemInfo) v.getTag();
            
            layout = (CellLayout)getChildAt(currPage);
            layout.getVacantCell(vacant, 1, 1);
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) v.getLayoutParams();
            lp.cellX = vacant[0];
            lp.cellY = vacant[1];
            
            if (info.screen != currPage || info.cellX != vacant[0] || info.cellY != vacant[1]) {
                Log.d("vulcan-80", String.format("settleChildrenView: moving icon,(%d,%d,%d)->(%d,%d,%d)",
            			currPage,vacant[0],vacant[1],
            			info.screen,info.cellX,info.cellY));
            	info.screen = currPage;
                info.cellX = vacant[0];
                info.cellY = vacant[1];
                LauncherModel.addOrMoveItemInDatabase(mLauncher, info, mInfo.id, info.screen,
                        info.cellX, info.cellY);
            }
            
            //boolean insert = false;
            //layout.addViewToCellLayout(v, insert ? 0 : -1, (int)info.id, lp, true);
            layout.addViewToCellLayout(v,  -1, (int)info.id, lp, true);
            
            if (((i + 1) % MAX_NUM_PER_PAGE) == 0) {
				currPage++;
			}
        }
        list.clear();
   }
    
   private void placeInReadingOrder(ArrayList<ShortcutInfo> items) {
        int count = items.size();
		for (int i = 0; i < count; i++) {
			ShortcutInfo item = items.get(i);
			if (item.cellX > INDEX_MAX_CELL_COUNT_X) {
				item.cellX = INDEX_MAX_CELL_COUNT_X;
			}
			if (item.cellY > INDEX_MAX_CELL_COUNT_Y) {
				item.cellY = INDEX_MAX_CELL_COUNT_Y;
			}
		}

		GridComparator gridComparator = new GridComparator();
		Collections.sort(items, gridComparator);
		
		int currPage = 0, x, y;
		for (int i = 0; i < count; i++) {
			x = (i - currPage * MAX_NUM_PER_PAGE) % DEFAULT_CELL_COUNT_X;
			y = (i - currPage * MAX_NUM_PER_PAGE) / DEFAULT_CELL_COUNT_X;
			ShortcutInfo item = items.get(i);
			item.screen = currPage;
			item.cellX = x;
			item.cellY = y;
			
			if (((i + 1) % MAX_NUM_PER_PAGE) == 0) {
				currPage++;
			}
		}
    }
	
    public void bindWorkspace(FolderInfo info){
        mInfo = info;
        setDataIsReady();
        
        ArrayList<ShortcutInfo> children = mInfo.contents;
        ArrayList<ShortcutInfo> overflow = new ArrayList<ShortcutInfo>();
        
        updatePages();
        placeInReadingOrder(children);
        
        for (int i = 0; i < children.size(); i++) {
            ShortcutInfo child = (ShortcutInfo) children.get(i);
            if (createAndAddShortcut(child.screen, child)) {
            	LauncherModel.addOrMoveItemInDatabase(
                        mLauncher, child, mInfo.id, child.screen, child.cellX, child.cellY);
            } else {
            	overflow.add(child);
            }
        }

        // We rearrange the items in case there are any empty gaps
        settleChildrenView();

        // If our folder has too many items we prune them from the list. This is an issue 
        // when upgrading from the old Folders implementation which could contain an unlimited
        // number of items.
        for (ShortcutInfo item: overflow) {
            mInfo.remove(item);
            LauncherModel.deleteItemFromDatabase(mLauncher, item);
        }
        
        // 因为所有调用到该函数的时候都会调用到settleChildrenView，其中对item更新会做数据库的修复，因此该操作完全是多余的
        // updateItemLocationsInDatabase();
    }
    
    // 这段代码不能去掉，一直想优化，但没有成功，因为做动画时item移动但并没有将数据保存到数据库中
    public void updateItemLocationsInDatabase() {
        ArrayList<View> list = getAllItemsInReadingOrder();
        for (int i = 0; i < list.size(); i++) {
            View v = list.get(i);
            ItemInfo info = (ItemInfo) v.getTag();
            LauncherModel.moveItemInDatabase(mLauncher, info, mInfo.id, info.screen,
                        info.cellX, info.cellY);
        }
        list.clear();
    }
	// Aurora <haojj> <2013-10-21> end
    
    // Aurora <haojj> <2013-10-19> add for addView相关函数 begin
    // 当页面只有一个图标的时候，拖动该图标到删除区域再拖回到folder会出现问题，因为该页面被移除了所以会得到空的页面
    public void onlyDetachFolderAddView(){
    	if(mFolderAddIcon != null && mFolderAddIcon.getParent() != null) {
			CellLayout oldLayout = (CellLayout) mFolderAddIcon.getParent().getParent();
			oldLayout.removeViewWithoutMarkingCells(mFolderAddIcon);
		}
    }
    
    public void detachFolderAddView(boolean removelayout){
    	if(mFolderAddIcon != null && mFolderAddIcon.getParent() != null) {
			CellLayout oldLayout = (CellLayout) mFolderAddIcon.getParent().getParent();
			oldLayout.removeViewWithoutMarkingCells(mFolderAddIcon);
			if(removelayout) {
				removeView(oldLayout);
				updateIndicator();
			} else {
				FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
				if(info != null && info.cellX == 0 && info.cellY == 0){
					removeView(oldLayout);
					updateIndicator();
				}
			}
		}
    }
    
    public void hideFolderAddView(){
    	if(mFolderAddIcon != null){
    		FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
        	if(info != null && info.cellX == 0 && info.cellY == 0){
        		detachFolderAddView(true);
        	} else {
        		mFolderAddIcon.setVisibility(View.INVISIBLE);
        	}
    	}
    }
    
    public void showFolderAddView(){
    	if(mFolderAddIcon != null){
    		FolderAddInfo info = (FolderAddInfo)mFolderAddIcon.getTag();
        	if(info != null && info.cellX == 0 && info.cellY == 0){
        		attachAddViewToNewLayout(info);
        		if(mFolderAddIcon.getVisibility() != View.VISIBLE) {
        			mFolderAddIcon.setVisibility(View.VISIBLE);
        		}
        	} else {
        		mFolderAddIcon.setVisibility(View.VISIBLE);
        	}
    	}
    }
    
    public void attachFolderAddView(boolean animate, long duration, long delay){
    	if(mFolderAddIcon != null){
    		FolderAddInfo info = (FolderAddInfo) mFolderAddIcon.getTag();
    		
    		int screen = getChildCount() - 1;
    		int[] emptyCell = new int[2];
    		
    		if(animate) mFolderAddIcon.setVisibility(View.INVISIBLE);
    		if(info.screen == screen && info.cellX == 0 && info.cellY == 0){
    			int lastScreen = screen - 1;
    			if(lastScreen >= 0){
    				CellLayout lastCellLayout = (CellLayout) getChildAt(lastScreen);
    				if(lastCellLayout != null && lastCellLayout.findCellForSpan(emptyCell, 1, 1)){
    					detachFolderAddView(false);
    	    			CellLayout.LayoutParams lp = new CellLayout.LayoutParams(emptyCell[0], emptyCell[1], 1, 1);
    	        		//boolean insert = false;
    	        		//boolean result = lastCellLayout.addViewToCellLayout(mFolderAddIcon, insert ? 0 : -1, 0, lp, false);
    	    			boolean result = lastCellLayout.addViewToCellLayout(mFolderAddIcon, -1, 0, lp, false);
    	        		if(result){
    	        			if(animate) animateFolderAddView(duration, delay);
    	        			return;
    	        		}
    				}
    			}
    		}
    		
    		CellLayout layout = (CellLayout) getChildAt(screen);
    		if(layout == null) return;
    		if (layout.findCellForSpan(emptyCell, 1, 1)) {
    			detachFolderAddView(false);
    			CellLayout.LayoutParams lp = new CellLayout.LayoutParams(emptyCell[0], emptyCell[1], 1, 1);
        		//boolean insert = false;
        		//boolean result = layout.addViewToCellLayout(mFolderAddIcon, insert ? 0 : -1, 0, lp, false);
    			boolean result = layout.addViewToCellLayout(mFolderAddIcon, -1, 0, lp, false);
        		if(result){
        			info.cellX = emptyCell[0];
        			info.cellY = emptyCell[1];
        			info.screen = screen;
        			mFolderAddIcon.setTag(info);
        		}
    		} else {
    			attachAddViewToNewLayout(info);
    		}
    		
    		if(animate) animateFolderAddView(duration, delay);
    	} 
    }
    
    private void animateFolderAddView(long duration, long delay){
    	if(mFolderAddIcon == null) return;
    	final int translateX = mFolderAddIcon.getMeasuredWidth();
    	PropertyValuesHolder translateXHolder = PropertyValuesHolder.ofFloat("translationX", translateX, 0);

    	ObjectAnimator oa = LauncherAnimUtils.ofPropertyValuesHolder(mFolderAddIcon, translateXHolder);
		oa.setDuration(duration);
		oa.setStartDelay(delay);
		oa.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				if(mFolderAddIcon != null) mFolderAddIcon.setTranslationX(0);
			}
			
			@Override
			public void onAnimationStart(Animator animation) {
				// TODO Auto-generated method stub
				if(mFolderAddIcon != null) {
					mFolderAddIcon.setVisibility(View.VISIBLE);
					mFolderAddIcon.setTranslationX(translateX);
				}
			}
		});
		oa.start();
    }
    
    private void attachAddViewToNewLayout(FolderAddInfo info){
    	if(getChildCount() == MAX_PAGE_NUM) return;
    	CellLayout layout = new CellLayout(getContext(), mCellWidth, mCellHeight, mWidthGap, mHeightGap, DEFAULT_CELL_COUNT_X, DEFAULT_CELL_COUNT_Y);
		addView(layout);
		layout.setOnClickListener(clickListener);
		
		int[] emptyCell = new int[2];
		emptyCell[0] = 0;
		emptyCell[1] = 0;
		int newScreen = getChildCount() - 1;
		
		detachFolderAddView(false);

		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(emptyCell[0], emptyCell[1], 1, 1);
		//boolean insert = false;
		//boolean result = layout.addViewToCellLayout(mFolderAddIcon, insert ? 0 : -1, 0, lp, false);
		boolean result = layout.addViewToCellLayout(mFolderAddIcon, -1, 0, lp, false);
		if(result){
			info.cellX = emptyCell[0];
			info.cellY = emptyCell[1];
			info.screen = newScreen;
			mFolderAddIcon.setTag(info);
		}
    }
    
    public void firstAttachFolderAddView(){
    	if(mNumPages < 1) return ;
    	if(mFolderAddIcon == null){
    		CellLayout layout = (CellLayout) getChildAt(mNumPages - 1);
    		if(layout == null) return ;
    		
    		getFolderAddTextView();
    		if(mFolderAddIcon == null) return;
    		attachFolderAddView(false, 0, 0);
    	} 
    }
    
	private void getFolderAddTextView() {
		if(mFolderAddIcon == null){
			mFolderAddIcon = (TextView) mInflater.inflate(R.layout.application, this, false);
			Drawable d = getResources().getDrawable(R.drawable.folder_add_icon);
			Bitmap v = Utilities.createIconBitmap(d, getContext());
			mFolderAddIcon.setCompoundDrawablesWithIntrinsicBounds(null,
					new FastBitmapDrawable(v), null, null);
			mFolderAddIcon.setText(null);
			FolderAddInfo folderCell= new FolderAddInfo(-1, -1);
			
			mFolderAddIcon.setTag(folderCell);
			mFolderAddIcon.setOnClickListener(clickListener);
		}
		
//		mFolderAddIcon = null;
	}
	
	// 文件夹解散的时候注意清除这个icon,即将mFolderAddIcon = null; 构造函数中有个bitmap
    public void recycleFolderAddView(){
    	if(mFolderAddIcon != null){
    		FastBitmapDrawable fd = (FastBitmapDrawable)mFolderAddIcon.getCompoundDrawables()[1];
    		if(null != fd){
    			Bitmap b = fd.getBitmap();
    			if(!b.isRecycled()) {
    				b.recycle();
    			} 
    		}
    	}
    	mFolderAddIcon = null;
    }
    
    class FolderAddInfo extends ItemInfo{

		public FolderAddInfo(int x, int y) {
			// TODO Auto-generated constructor stub
			cellX = x;
			cellY = y;
		}
    }
    
    // 获取AddIcon对象
    public TextView getFolderAddIcon(){
    	return mFolderAddIcon;
    }
    
    protected void suspendFolderAddIconAnimator(){
		if(mFolderAddIcon != null) mFolderAddIcon.setAlpha(1.0f);
		onlyDetachFolderAddView();
    }
    
    public void setFolderAddIconScaleToSmall(float scale){
    	if(mFolderAddIcon != null && mFolderAddIcon.getVisibility() == View.VISIBLE){
    		mFolderAddIcon.setScaleX(scale);
    		mFolderAddIcon.setScaleY(scale);
    	}
    }
    // Aurora <haojj> <2013-10-19> end
    
    @Override
    protected void screenScrolled(int screenCenter) {
		super.screenScrolled(screenCenter);
		if (mOverScrollX < 0 || mOverScrollX > mMaxScrollX) {
			int index = mOverScrollX < 0 ? 0 : getChildCount() - 1;
			CellLayout cl = (CellLayout) getChildAt(index);
			float scrollProgress = getScrollProgress(screenCenter, cl, index);
			// cl.setOverScrollAmount(Math.abs(scrollProgress), index == 0);
			// float rotation = - WORKSPACE_OVERSCROLL_ROTATION *
			// scrollProgress;
			// cl.setRotationY(rotation);
			float distance = -WORKSPACE_OVERSCROLL_ROTATION * scrollProgress;
			cl.setTranslationX(distance);
			if (!mOverscrollTransformsSet) {
				mOverscrollTransformsSet = true;
				cl.setCameraDistance(mDensity * mCameraDistance);
				cl.setPivotX(cl.getMeasuredWidth() * (index == 0 ? 0.75f : 0.25f));
				cl.setPivotY(cl.getMeasuredHeight() * 0.5f);
				cl.setOverscrollTransformsDirty(true);
			}
		} else {
			if (mOverscrollTransformsSet) {
				mOverscrollTransformsSet = false;
				((CellLayout) getChildAt(0)).resetOverscrollTransforms();
				((CellLayout) getChildAt(getChildCount() - 1)).resetOverscrollTransforms();
			}
			// 添加该方法用于消除S4左右滑动时的黑线问题，又因文字加阴影不会多次重绘，因此不会再出现黑线，注释掉
			// ((Folder)getParent()).invalidate();
		}
    }
    
    /**
     * Desc: find  the item in which page and animate
     * Author: Hazel
     * Date: 2014/04/02
     * */
    public void findItemInFolderPage(View v){
    	ShortcutInfo info  = (ShortcutInfo)v.getTag();
    	setCurrentPageAndIndicator(info.screen);
    	View ItemView = getViewForInfo(info);
    	Animation anim = mLauncher.getQuickLocateItemAnim();
    	if(anim!=null){
    		ItemView.startAnimation(anim);
    	}
    }

	/**
	 * @return the mInfo
	 */
	public FolderInfo getmInfo() {
		return mInfo;
	}

	/**
	 * @param mInfo the mInfo to set
	 */
	public void setmInfo(FolderInfo mInfo) {
		this.mInfo = mInfo;
	}
    
    
    
}

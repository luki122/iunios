/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.browser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

import com.android.browser.BrowserBookmarksAdapter.AllCheckedObserver;
import com.android.browser.BrowserHistoryPage.HistoryQuery;
import com.android.browser.util.HistoryCursorAdapter;

public class BrowserHistorysAdapter extends
        HistoryCursorAdapter<BrowserBookmarksAdapterItem> {
	
	public static final int STATE_NO_CHECKED = 0;
	public static final int STATE_CHECKED_SOME = 1;
	public static final int STATE_ALL_CHECKED = 2;

    public static boolean isInSelectionMode;
	LayoutInflater mInflater;
    Context mContext;
	public int contextItemPos = -1;
	private AuroraListView mListView;
	private AllCheckedObserver mCheckedObserver;
	
	public static HashMap<Integer, Boolean> checkedMap = new HashMap<Integer, Boolean>();
	private long openSelectionModeTime;
	public boolean isListViewIdle = true;
	
	public ArrayList<BrowserBookmarksAdapterItem> list = new ArrayList<BrowserBookmarksAdapterItem>();

    /**
     *  Create a new BrowserBookmarksAdapter.
     */
    public BrowserHistorysAdapter(Context context, AuroraListView listView, AllCheckedObserver checkedObserver) {
        // Make sure to tell the CursorAdapter to avoid the observer and auto-requery
        // since the Loader will do that for us.
        super(context, null);
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListView = listView;
        this.mListView.setOnScrollListener(new HistoryScrollListener());
        this.mCheckedObserver = checkedObserver;
    }

    @Override
    protected long getItemId(Cursor c) {
        return c.getLong(BookmarksLoader.COLUMN_INDEX_ID);
    }

    @Override
    public View newView(Context context, ViewGroup parent) {
    	
    	View retView=mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
    	retView.setBackgroundResource(R.drawable.bookmark_thumb_selector);
    	View divider=retView.findViewById(com.aurora.R.id.aurora_listview_divider);
    	divider.setVisibility(View.VISIBLE);
    	divider.setAlpha(0.5f);
    	retView.findViewById(com.aurora.R.id.control_padding).setPadding(BaseUi.dip2px(context, 12), 0, 0, 0);
		RelativeLayout front = (RelativeLayout) retView.findViewById(com.aurora.R.id.aurora_listview_front);
		View content = mInflater.inflate(R.layout.history_thumbnail, null);
		LinearLayout headContainer = (LinearLayout)retView.findViewById(com.aurora.R.id.aurora_list_header);
		mInflater.inflate(R.layout.view_history_header_date, headContainer);
		front.addView(content);
		return retView;
		
    }

    @Override
    public void bindView(View view, BrowserBookmarksAdapterItem object, int position) {
//        BookmarkContainer container = (BookmarkContainer) view;
//        container.setIgnoreRequestLayout(true);
    	
    	  bindGridView(view, mContext, object, position);
    		
//        container.setIgnoreRequestLayout(false);
    }

    CharSequence getTitle(Cursor cursor) {
//        int type = cursor.getInt(BookmarksLoader.COLUMN_INDEX_TYPE);
//        switch (type) {
//        case Bookmarks.BOOKMARK_TYPE_OTHER_FOLDER:
//            return mContext.getText(R.string.other_bookmarks);
//        }
    	if(cursor == null || cursor.isClosed()) return "";
        return cursor.getString(HistoryQuery.INDEX_TITE);
    }

    void bindGridView(View view, Context context, BrowserBookmarksAdapterItem item, int position) {
        // We need to set this to handle rotation and other configuration change
        // events. If the padding didn't change, this is a no op.
//        int padding = context.getResources()
//                .getDimensionPixelSize(R.dimen.combo_horizontalSpacing);
//        view.setPadding(padding, view.getPaddingTop(),
//                padding, view.getPaddingBottom());
        if(item.visited_date != 0) {
        	if(!contains(item.id)) {
        		list.add(new BrowserBookmarksAdapterItem(item.id, item.visited_date));
        	}
    		ImageView thumb = (ImageView) view.findViewById(R.id.thumb_history);
    		LinearLayout.LayoutParams params = (LayoutParams) thumb.getLayoutParams();
            thumb.setScaleType(ScaleType.CENTER_CROP);
            if (item.thumbnail == null || !item.has_thumbnail) {
                thumb.setImageResource(R.drawable.default_icon_title);
            } else {
                thumb.setImageDrawable(item.thumbnail);
            }
            params.bottomMargin = BaseUi.dip2px(mContext, 6);
            
            TextView tvLabel = (TextView) view.findViewById(R.id.label_history);
            tvLabel.setText(item.title.equals(
            		mContext.getResources().getString(R.string.cannot_find_the_webpage)) ? mContext.getResources().getString(R.string.no_title) : item.title);
            tvLabel.setPadding(0, BaseUi.dip2px(mContext, 6), 0, 0);
            
            TextView tvUrl = (TextView) view.findViewById(R.id.url_history);
            tvUrl.setText(item.url);
        	
        	TextView tvDate = (TextView)view.findViewById(R.id.tv_view_history_header_date);
        	tvDate.setPadding(BaseUi.dip2px(context, 12), 0, 0, 0);
        	setHeaderDate(tvDate,item.visited_date,position);
        	
        	RelativeLayout front = (RelativeLayout) view
    				.findViewById(com.aurora.R.id.aurora_listview_front);
        	AuroraCheckBox cb = (AuroraCheckBox) view
    				.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
    		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cb
    				.getLayoutParams();
    		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);  
    		cb.setLayoutParams(lp);
    		cb.auroraSetChecked(getCheckedFromMap(position), true);
    		if (isInSelectionMode) {
    			if (needCheckBoxAnim(view)) {
    				mListView.auroraStartCheckBoxAppearingAnim(front, cb);
    				openSelectionModeTime = System.currentTimeMillis();
    			} else {
    				if(System.currentTimeMillis() - openSelectionModeTime >= 500) {
    					mListView.auroraSetCheckBoxVisible(front, cb, true);
    				}
    			}
    		} else {
    			if (needCheckBoxAnim(view)) {
    				mListView.auroraStartCheckBoxDisappearingAnim(front, cb);
    			} else {
    				mListView.auroraSetCheckBoxVisible(front, cb, false);
    			}
    		}
        }
    }
    
    private boolean contains(long id) {
    	int size = list.size();
    	for(int i=0;i<size;i++) {
    		if(list.get(i).id == id) {
    			return true;
    		}
    	}
		return false;
	}

	private class HistoryScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				isListViewIdle = true;
			}else if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				isListViewIdle = false;
			}else if(scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				isListViewIdle = false;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			
		}
		
	}
    
    private boolean getCheckedFromMap(int position) {
		if (!checkedMap.containsKey(position)) {
			checkedMap.put(position, false);
		}
		if (contextItemPos != -1 && checkedMap.containsKey(contextItemPos)) {
			checkedMap.put(contextItemPos, true);
			contextItemPos = -1;
		}
		return checkedMap.get(position);
	}
    
    private boolean needCheckBoxAnim(View v) {
		int tag = (Integer) v.getTag();
		if (isInSelectionMode) {
			if (tag == 0) {
				v.setTag(1);
				if(isListViewIdle) {
					return true;
				}
			}
			return false;
		} else {
			if (tag == 1) {
				v.setTag(0);
				if(isListViewIdle) {
					return true;
				}
			}
			return false;
		}
	}

    private void setHeaderDate(TextView tvDate, long visited_date, int position) {
    	if(position == 0) {
    		tvDate.setVisibility(View.VISIBLE);
          	tvDate.setText(getStringDate(visited_date,false));
    	}else {
    		if(position - 1 < list.size()) {
    			BrowserBookmarksAdapterItem item = list.get(position - 1);
    			if(item != null) {
    				if(isTheSameDate(item.visited_date, visited_date)) {
    					tvDate.setVisibility(View.GONE);
    				}else {
    					tvDate.setVisibility(View.VISIBLE);
    					tvDate.setText(getStringDate(visited_date,false));
    				}
    			}
    		}
    	}
	}

	private boolean isTheSameDate(long savedDate, long visited_date) {
		if(getStringDate(savedDate,true).equals(getStringDate(visited_date,true))) {
			return true;
		}
		return false;
	}

	@Override
    public BrowserBookmarksAdapterItem getRowObject(Cursor c,
            BrowserBookmarksAdapterItem item) {
        if (item == null) {
            item = new BrowserBookmarksAdapterItem();
        }
        if(c == null || c.isClosed()) return item;
        Bitmap thumbnail = item.thumbnail != null ? item.thumbnail.getBitmap() : null;
//        thumbnail = BrowserBookmarksPage.getBitmap(c,
//                BrowserHistoryPage.HistoryQuery.INDEX_TOUCH_ICON, thumbnail);
    	thumbnail = BrowserBookmarksPage.getBitmap(c,
    			BrowserHistoryPage.HistoryQuery.INDEX_FAVICON, thumbnail);
        item.has_thumbnail = thumbnail != null;
        if (thumbnail != null
                && (item.thumbnail == null || item.thumbnail.getBitmap() != thumbnail)) {
            item.thumbnail = new BitmapDrawable(mContext.getResources(), thumbnail);
        }
//        item.is_folder = c.getInt(BookmarksLoader.COLUMN_INDEX_IS_FOLDER) != 0;
        item.title = getTitle(c).toString();
        item.url = c.getString(HistoryQuery.INDEX_URL);
        item.visited_date = c.getLong(HistoryQuery.INDEX_DATE_LAST_VISITED);
        item.id = c.getLong(BookmarksLoader.COLUMN_INDEX_ID);
        return item;
    }

    @Override
    public BrowserBookmarksAdapterItem getLoadingObject() {
        BrowserBookmarksAdapterItem item = new BrowserBookmarksAdapterItem();
        return item;
    }
    
    /**
     * 将毫秒转为日期
     * @param millisecond 毫秒数
     * @param needYear 是否需要年份
     * @return
     */
    private String getStringDate(long millisecond,boolean needYear) {
    	String pattern = "";
    	if(needYear) {
    		//需要年份
    		pattern = "yyyy年MM月dd日";
    	}else {
    		pattern = "MM月dd日";
    	}
    	SimpleDateFormat sdf = new SimpleDateFormat(pattern);
    	String date = sdf.format(millisecond);
    	
    	if(!needYear) {
    		date = getPreDate(millisecond) + date;
    	}
    	
    	return date;
    }
    
    /**
	 * 设置当前模式（编辑还是非编辑模式）
	 * 
	 * @param modeSelection
	 */
	public static void setIsInSelectionMode(boolean modeSelection) {
		isInSelectionMode = modeSelection;
		if (isInSelectionMode) {
			checkedMap.clear();
		}
	}

	private String getPreDate(long millisecond) {
		if(getStringDate(millisecond, true).equals(getStringDate(System.currentTimeMillis(), true))) {
			return mContext.getResources().getString(R.string.today) + "     ";
		}else if(getStringDate(millisecond, true).equals(getStringDate(System.currentTimeMillis() - 24 * 60 * 60 * 1000, true))) {
			return mContext.getResources().getString(R.string.yesterday) + "     ";
		}else if(getStringDate(millisecond, true).equals(getStringDate(System.currentTimeMillis() - 24 * 60 * 60 * 1000 * 2, true))) {
			return mContext.getResources().getString(R.string.the_day_before_yesterday) + "     ";
		}
		return "";
	}

	/**
	 * 点击全选或者全不选
	 * 
	 * @param check
	 *            true 全选；false 全不选
	 */
	public void checkAllOrNot(boolean check) {
		checkedMap.clear();
		int allCount = getCount();
		for (int i = 0; i < allCount; i++) {
			checkedMap.put(i, check);
		}
		notifyDataSetChanged();
	}

	public void doCheckState() {
		this.mCheckedObserver.checkState(getCheckState());
	}

	private int getCheckState() {
		int state = STATE_NO_CHECKED;
		Iterator<Integer> iterator = checkedMap.keySet().iterator();
		int checkedCount = 0;
		while (iterator.hasNext()) {
			if (checkedMap.get(iterator.next())) {
				checkedCount++;
			}
		}
		if(checkedCount != 0) {
			if(checkedCount == getCount()) {
				state = STATE_ALL_CHECKED;
			}else {
				state = STATE_CHECKED_SOME;
			}
		}
		return state;
	}

	public void setCheckOrNot(int position) {
		checkedMap.put(position, !getCheckedFromMap(position));
	}
	
	public void removeHistoryDate(long id) {
		int size = list.size();
		BrowserBookmarksAdapterItem item = null;
		for(int i=0; i<size; i++) {
			item = null;
			item = list.get(i);
			if(item.id == id) {
				break;
			}
		}
		if(item != null) {
			list.remove(item);
		}
	}
}

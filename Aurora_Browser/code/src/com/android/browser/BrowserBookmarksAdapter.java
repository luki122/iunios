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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.provider.BrowserContract.Bookmarks;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView;

import com.android.browser.util.ThreadedCursorAdapter;

public class BrowserBookmarksAdapter extends BaseAdapter {

	public static final int STATE_NO_CHECKED = 0;
	public static final int STATE_CHECKED_ONE = 1;
	public static final int STATE_CHECKED_ONE_BUT_ALL = 2;
	public static final int STATE_CHECKED_SOME = 3;
	public static final int STATE_ALL_CHECKED = 4;

	LayoutInflater mInflater;
	Context mContext;
	public static boolean isInSelectionMode;
	private AuroraListView mListView;
	public int contextItemPos = -1;

	private long openSelectionModeTime;

	public static HashMap<Integer, Boolean> checkedMap = new HashMap<Integer, Boolean>();

	private AllCheckedObserver mCheckedObserver;

	private boolean isListViewIdle = true;

	public boolean doDisappearingAnim;
	
	private ArrayList<BrowserBookmarksAdapterItem> list;

	/**
	 * Create a new BrowserBookmarksAdapter.
	 */
	public BrowserBookmarksAdapter(Context context, AuroraListView mListView,
			AllCheckedObserver checkedObserver,ArrayList<BrowserBookmarksAdapterItem> list) {
		// Make sure to tell the CursorAdapter to avoid the observer and
		// auto-requery
		// since the Loader will do that for us.
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.mListView = mListView;
		this.mListView.setOnScrollListener(new BookmarkScrollListener());
		this.mCheckedObserver = checkedObserver;
		this.list = list;
	}

	void bindGridView(View retView, int position) {
		BrowserBookmarksAdapterItem item = list.get(position);
		RelativeLayout front = (RelativeLayout) retView.findViewById(com.aurora.R.id.aurora_listview_front);
		ViewHolder holder = (ViewHolder) front.getTag();
		
		holder.tvTitle.setText(item.title);
		holder.tvUrl.setText(item.url);
		AuroraCheckBox cb = (AuroraCheckBox) retView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) cb
				.getLayoutParams();
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		cb.setLayoutParams(lp);
		cb.auroraSetChecked(getCheckedFromMap(position), true);
		if (isInSelectionMode) {
			if (needCheckBoxAnim(retView)) {
				mListView.auroraStartCheckBoxAppearingAnim(front, cb);
				openSelectionModeTime = System.currentTimeMillis();
			} else {
				if (System.currentTimeMillis() - openSelectionModeTime >= 500) {
					mListView.auroraSetCheckBoxVisible(front, cb, true);
				}
			}
		} else {
			if (needCheckBoxAnim(retView) && doDisappearingAnim) {
				mListView.auroraStartCheckBoxDisappearingAnim(front, cb);
			} else {
				mListView.auroraSetCheckBoxVisible(front, cb, false);
			}
		}

		LinearLayout.LayoutParams params = (LayoutParams) holder.ivIcon.getLayoutParams();
		
		if (item.is_folder) {
			// folder
			holder.ivIcon.setImageResource(R.drawable.thumb_bookmark_folder_icon);
			holder.tvUrl.setVisibility(View.GONE);
			holder.ivArrow.setVisibility(View.VISIBLE);
			holder.tvTitle.setTextSize(18);
			holder.tvTitle.setGravity(Gravity.CENTER_VERTICAL);
			
			holder.tvTitle.setPadding(0, 0, 0, 0);
			holder.tvUrl.setPadding(0, 0, 0, 0);
			params.bottomMargin = 0;
		} else {
			if (item.thumbnail == null || !item.has_thumbnail) {
				holder.ivIcon.setImageResource(R.drawable.default_icon_title);
			} else {
				holder.ivIcon.setImageDrawable(item.thumbnail);
			}
			holder.tvUrl.setVisibility(View.VISIBLE);
			holder.ivArrow.setVisibility(View.GONE);
			holder.tvTitle.setTextSize(15);
			holder.tvTitle.setGravity(Gravity.BOTTOM);
			
			holder.tvTitle.setPadding(0, BaseUi.dip2px(mContext, 5), 0, 0);
			holder.tvUrl.setPadding(0, BaseUi.dip2px(mContext, 1), 0, 0);
			params.bottomMargin = BaseUi.dip2px(mContext, 6);
		}

	}

	private class BookmarkScrollListener implements OnScrollListener {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				isListViewIdle = true;
			} else if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
				isListViewIdle = false;
			} else if (scrollState == OnScrollListener.SCROLL_STATE_FLING) {
				isListViewIdle = false;
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}

	}

	private boolean needCheckBoxAnim(View v) {
		int tag = (Integer) v.getTag();
		if (isInSelectionMode) {
			if (tag == 0) {
				v.setTag(1);
				if (isListViewIdle) {
					return true;
				}
			}
			return false;
		} else {
			if (tag == 1) {
				v.setTag(0);
				if (isListViewIdle) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * 
	 * @param position
	 */
	public void setCheckOrNot(int position) {
		checkedMap.put(position, !getCheckedFromMap(position));
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
		if (checkedCount != 0) {
			if (checkedCount == 1) {
				state = STATE_CHECKED_ONE;
				if (getCount() == 1) {
					state = STATE_CHECKED_ONE_BUT_ALL;
				}
			} else {
				state = STATE_CHECKED_SOME;
			}
			if (checkedCount == getCount() && getCount() != 1) {
				state = STATE_ALL_CHECKED;
			}
		}
		return state;
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

	public interface AllCheckedObserver {
		public void checkState(int state);
	};

	public void doCheckState() {
		this.mCheckedObserver.checkState(getCheckState());
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public BrowserBookmarksAdapterItem getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	class ViewHolder {
		ImageView ivIcon;
		TextView tvTitle;
		TextView tvUrl;
		ImageView ivArrow;
	}
	
	@Override
	public View getView(int position, View retView, ViewGroup parent) {
		ViewHolder holder = null;
		if(retView == null) {
			holder = new ViewHolder();
			retView=mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			retView.setBackgroundResource(R.drawable.bookmark_thumb_selector);
			View divider=retView.findViewById(com.aurora.R.id.aurora_listview_divider);
			divider.setVisibility(View.VISIBLE);
			divider.setAlpha(0.5f);
			retView.findViewById(com.aurora.R.id.control_padding).setPadding(BaseUi.dip2px(mContext, 12), 0, BaseUi.dip2px(mContext, 8), 0);
			RelativeLayout front = (RelativeLayout) retView.findViewById(com.aurora.R.id.aurora_listview_front);
			View content = mInflater.inflate(R.layout.bookmark_thumbnail, null);
			front.addView(content);
			
			holder.ivIcon = (ImageView) content.findViewById(R.id.thumb);
			holder.tvTitle = (TextView) content.findViewById(R.id.label);
			holder.tvUrl = (TextView) content.findViewById(R.id.url);
			holder.ivArrow = (ImageView) content.findViewById(R.id.arrow);
			retView.setTag(0);
			front.setTag(holder);
		}
		
		bindGridView(retView, position);
		
		return retView;
	}

}

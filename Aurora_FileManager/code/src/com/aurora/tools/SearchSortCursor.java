package com.aurora.tools;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.aurora.filemanager.fragment.AuroraMainFragment.SearchSort;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.util.Log;

public class SearchSortCursor extends CursorWrapper {
	private static final String TAG = "SearchSortCursor";
	Cursor mCursor;
	ArrayList<SortEntry> sortList = new ArrayList<SortEntry>();
	int mPos = 0;
	private boolean isTypeAndMusic;
	private String searchKey;

	public class SortEntry {
		public String key;
		public String name;
		public int order;
	}

	@SuppressWarnings("rawtypes")
	private Comparator cmp = Collator.getInstance(java.util.Locale.CHINA);

	@SuppressWarnings("unchecked")
	public Comparator<SortEntry> comparator = new Comparator<SortEntry>() {
		@Override
		public int compare(SortEntry entry1, SortEntry entry2) {
			// if (!isTypeAndMusic) {
			// String lhs = entry1.key;
			// String rhs = entry2.key;
			// return AuroraFileManagerUtil.computeTheSort(lhs, rhs);
			// }
			//
			// int result = entry1.key.compareTo(entry2.key);
			// if (result == 0) {
			// Log.i("SortCursor", "result=" + result);
			// return AuroraFileManagerUtil.computeTheSort(entry1.name,
			// entry2.name);
			// }
			// return entry1.key.compareTo(entry2.key);
			// 中英文混排

			String name1 = entry1.name.toLowerCase();
			String name2 = entry2.name.toLowerCase();

//			LogUtil.log(TAG, "name1=" + name1);
//			LogUtil.log(TAG, "name2=" + name2);
//			LogUtil.log(TAG, "searchKey=" + searchKey);

			// AuroraSearchSortUtil.startCompare(name1, searchKey);
			AuroraSearchSortUtil util1 = new AuroraSearchSortUtil(name1,
					searchKey);
			AuroraSearchSortUtil util2 = new AuroraSearchSortUtil(name2,
					searchKey);
			int position1 = util1.getIndexOfStr();
			int position2 = util2.getIndexOfStr();

//			LogUtil.log(TAG, "position1=" + position1);
//			LogUtil.log(TAG, "position2=" + position2);

			if (position1 < position2)
				return -1;

			if (position1 > position2)
				return 1;

			return name1.compareToIgnoreCase(name2);
		}
	};

	public SearchSortCursor(Cursor cursor, String columnName) {
		super(cursor);
		isTypeAndMusic = false;
		// TODO Auto-generated constructor stub
		mCursor = cursor;
		if (mCursor != null && mCursor.getCount() > 0) {
			int i = 0;
			int column = mCursor.getColumnIndexOrThrow(columnName);
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
					.moveToNext(), i++) {
				SortEntry sortKey = new SortEntry();
				sortKey.key = mCursor.getString(column);
				sortKey.order = i;
				sortList.add(sortKey);
			}
		}
		// Sort
		Collections.sort(sortList, comparator);
	}

	public boolean moveToPosition(int position) {
		if (position >= 0 && position < sortList.size()) {
			mPos = position;
			int order = sortList.get(position).order;
			return mCursor.moveToPosition(order);
		}
		if (position < 0) {
			mPos = -1;
		}
		if (position >= sortList.size()) {
			mPos = sortList.size();
		}
		return mCursor.moveToPosition(position);
	}

	public boolean moveToFirst() {
		return moveToPosition(0);
	}

	public boolean moveToLast() {
		return moveToPosition(getCount() - 1);
	}

	public boolean moveToNext() {
		return moveToPosition(mPos + 1);
	}

	public boolean moveToPrevious() {
		return moveToPosition(mPos - 1);
	}

	public boolean move(int offset) {
		return moveToPosition(mPos + offset);
	}

	public int getPosition() {
		return mPos;
	}

	public SearchSortCursor(Cursor cursor, String columnName,
			boolean isTypeAndMusic, String searchKey,SearchSort task) {
		super(cursor);
		// TODO Auto-generated constructor stub
		mCursor = cursor;
		this.searchKey = searchKey.toLowerCase();
		this.isTypeAndMusic = false;
		this.isTypeAndMusic = isTypeAndMusic;
		if (mCursor != null && mCursor.getCount() > 0 && isTypeAndMusic) {
			int i = 0;
			int column = mCursor.getColumnIndexOrThrow(columnName);
			String displayName = null;
			String name = null;
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
					.moveToNext(), i++) {
				if(task!=null&&task.isCancelled()){
					break;
				}
				SortEntry sortKey = new SortEntry();
				displayName = mCursor.getString(column);
//				LogUtil.d(TAG, "before--->" + displayName);
				if (displayName != null) {
					name = displayName
							.substring(displayName.lastIndexOf("/") + 1);// 排序名字获取
				}
				sortKey.key = displayName;
				sortKey.name = name;
//				LogUtil.d(TAG, "ortKey.name--->" + sortKey.name);
				sortKey.order = i;
				sortList.add(sortKey);
			}
		}
		// Sort
		Collections.sort(sortList, comparator);
	}
}
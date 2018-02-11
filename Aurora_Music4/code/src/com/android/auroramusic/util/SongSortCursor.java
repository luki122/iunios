package com.android.auroramusic.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.android.auroramusic.util.HanziToPinyin;
import com.android.auroramusic.util.HanziToPinyin.Token;

public class SongSortCursor extends CursorWrapper {
	Cursor mCursor;
	ArrayList<SortEntry> sortList = new ArrayList<SortEntry>();
	int mPos = 0;

	public class SortEntry {
		public String key;
		public int order;
		public boolean flag;

		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return "key = " + key + " order =" + order;
		}
	}

	public Comparator comparator = new Comparator() {

		@Override
		public int compare(Object lhs, Object rhs) {
			return ((SortEntry)lhs).key.compareToIgnoreCase(((SortEntry)rhs).key);
		}
	};

	public SongSortCursor(Cursor cursor, String columnName) {
		super(cursor);
		mCursor = cursor;
		if (mCursor != null && mCursor.getCount() > 0) {
			int i = 0;
			int column = cursor.getColumnIndexOrThrow(columnName);
			for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor
					.moveToNext(), i++) {

				SortEntry sortKey = new SortEntry();
				sortKey.key = cursor.getString(column);
				sortKey.key = getSpell(sortKey.key);
				sortKey.order = i;
				sortList.add(sortKey);
			}
		}
		Collections.sort(sortList, comparator);
	}

	public boolean moveToPosition(int position) {

		if (position >= 0 && position < sortList.size()) {
			mPos = position;
			int order = ((SortEntry) sortList.get(position)).order;
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
	public static String getSpell(String str){
		StringBuffer buffer=new StringBuffer();
		
		if(str!=null && !str.equals("")){
			char[] cc=str.toCharArray();
			for(int i=0;i<cc.length;i++){
				ArrayList<Token> mArrayList = HanziToPinyin.getInstance().get(String.valueOf(cc[i]));
				if(mArrayList.size() > 0 ){
					String n = mArrayList.get(0).target;
					buffer.append(n);
				}
			}
		}
		String spellStr = buffer.toString();
		return spellStr.toUpperCase();
	}

}

/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.aurora.reject.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraTextView;
import gionee.provider.GnCallLog.Calls;
import gionee.provider.GnContactsContract.Contacts;
import gionee.provider.GnContactsContract.Data;
import gionee.provider.GnContactsContract.RawContacts;

import com.aurora.reject.R;


public class AddBlackFromCallLogAdapter extends CursorAdapter {
	
	private Context mContext;
	
	private ArrayList<String> mCheckedItem = new ArrayList<String>();
	
	public static final String[] CALL_LOG_PROJECTION = new String[] {
        Calls._ID,
        Calls.NUMBER,
        Contacts.DISPLAY_NAME,
        "area",
        "mark",
        "black_name"
    };
	
	static class ViewHolder {
		AuroraCheckBox checkBox;
		AuroraTextView nameTv;
		AuroraTextView numberTv;
        int position;
	}
	
	public AddBlackFromCallLogAdapter(Context context) {
		super(context, null, false);
		
		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		if (null == view) {
			return;
		}
		
		ViewHolder vholder;
		vholder = (ViewHolder) view.getTag();
		int position = cursor.getPosition();
		String number = cursor.getString(1);
		String name = cursor.getString(2);
		String area = cursor.getString(3);
		
		if (mCheckedItem != null && mCheckedItem.contains(number)) {
			vholder.checkBox.setChecked(true);
		} else {
			vholder.checkBox.setChecked(false);
		}
		
		if (name == null || name.isEmpty()) {
			name = number;
			number = area;
		}
		vholder.nameTv.setText(name);
		if (number != null && !number.isEmpty()) {
			vholder.numberTv.setText(number);
		}
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
        ViewHolder vholder;
		
		if (null == convertView) {
			vholder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.call_log_list_item_layout, parent, false);
			vholder.checkBox = (AuroraCheckBox)convertView.findViewById(R.id.check_box);
			vholder.nameTv = (AuroraTextView)convertView.findViewById(R.id.name);
			vholder.numberTv = (AuroraTextView)convertView.findViewById(R.id.number);
			vholder.position = position;
			
			convertView.setTag(vholder);
		} else {
			vholder = (ViewHolder)convertView.getTag();
		}
		
		Cursor cursor = mCursor;
		if (position <= cursor.getCount()) {
			cursor.moveToPosition(position);
		}
		
		bindView(convertView, mContext, cursor);
		
	    return convertView;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		int position = cursor.getPosition();
		View listItemView = LayoutInflater.from(mContext).inflate(
				R.layout.call_log_list_item_layout, parent, false);
		ViewHolder vholder;
		vholder = new ViewHolder();
		vholder.checkBox = (AuroraCheckBox)listItemView.findViewById(R.id.check_box);
		vholder.nameTv = (AuroraTextView)listItemView.findViewById(R.id.name);
		vholder.numberTv = (AuroraTextView)listItemView.findViewById(R.id.number);
		vholder.position = position;
		listItemView.setTag(vholder);
		
		return listItemView;
	}
	
	public String getName(int position) {
		Cursor cursor = (Cursor) getItem(position);
		if (cursor == null) {
            return "";
        }
		
		return cursor.getString(2);
	}
	
	public String getNumber(int position) {
		Cursor cursor = (Cursor) getItem(position);
		if (cursor == null) {
            return null;
        }
		
		return cursor.getString(1);
	}
	
	public void setCheckedItem(String number) {
		if (mCheckedItem != null && !mCheckedItem.contains(number)) {
			mCheckedItem.add(number);
		}
	}
	
    public void removeCheckedItem(String number) {
    	if (mCheckedItem != null && mCheckedItem.contains(number)) {
			mCheckedItem.remove(number);
		}
	}
    
    public void clearCheckedItem() {
    	if (mCheckedItem != null) {
			mCheckedItem.clear();
		}
	}
	
}

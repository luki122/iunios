package com.android.contacts.widget;

import android.view.View;
import android.view.ViewGroup;

public abstract class LinearListAdapter {
	private LinearListView mOwner;
	public abstract int getCount();
	public abstract View getView(int position, View convertView, ViewGroup parent);
	public void notifyDataSetChanged(){
		mOwner.DataSetChanged();
	}
	public void setOwner(LinearListView owner) {
		mOwner = owner;
	}

}

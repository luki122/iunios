package com.android.contacts.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class LinearListView extends LinearLayout{
	
	private LinearListAdapter mAdapter;

	public LinearListView(Context context) {
		super(context);
		init();
	}

	public LinearListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LinearListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init(){
		setOrientation(LinearLayout.VERTICAL);
	}
	
	public void setAdapter(LinearListAdapter adapter){
		removeAllViews();
		mAdapter = adapter;
		mAdapter.setOwner(this);
		DataSetChanged();
	}
	
	public LinearListAdapter getAdapter(){
		return mAdapter;
	}

	public void DataSetChanged() {
		int oldCount = getChildCount();
		int i = 0;
		for(;i<mAdapter.getCount();i++){
			View view;
			if(i < oldCount){
				view = mAdapter.getView(i,  getChildAt(i), this);
			} else {
				view = mAdapter.getView(i,  null, this);
				addView(view);
			}
		}
		if(oldCount > i){
			removeViews(i, oldCount - i);
		}
	}

}

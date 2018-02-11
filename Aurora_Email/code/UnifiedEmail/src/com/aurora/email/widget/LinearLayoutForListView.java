package com.aurora.email.widget;

import com.android.mail.utils.MyLog;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * 取代ListView的LinearLayout，使之能够成功嵌套在ScrollView中
 * 
 */
public class LinearLayoutForListView extends LinearLayout{

	private static final String TAG = "AuroraComposeActivity";
	
	public LinearLayoutForListView(Context context) {
		super(context);
		setOrientation(1);
	}

	public LinearLayoutForListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(1);
		// TODO Auto-generated constructor stub
	}

	private BaseAdapter adapter;
	private OnClickListener onClickListener = null;

	/**
	 * 绑定布局
	 */
	public void bindLinearLayout() {
		int count = adapter.getCount();
		this.removeAllViews();
		for (int i = 0; i < count; i++) {
			View v = adapter.getView(i, null, null);

			v.setOnClickListener(this.onClickListener);
			addView(v, i);
		}
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
		adapter.registerDataSetObserver(new DataSetObserver() {

			@Override
			public void onChanged() {
				MyLog.d(TAG, "onChanged()");
				bindLinearLayout();
			}

			@Override
			public void onInvalidated() {
				MyLog.d(TAG, "onInvalidated()");
				super.onInvalidated();
			}
			
		});
	}
	public void setOnItemClick(OnClickListener onClickListener){
		this.onClickListener = onClickListener;
	}
}
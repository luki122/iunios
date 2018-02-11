package com.android.contacts.widget;

import com.android.contacts.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ContactSimpleInfoView extends LinearLayout {
	public ImageView mHeadIv;
	public TextView mNameTv;
	public LinearListView mNumberTv;
	
	public ContactSimpleInfoView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public ContactSimpleInfoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mHeadIv = (ImageView) findViewById(R.id.head_iv);
		mNameTv = (TextView) findViewById(R.id.name_tv);
		mNumberTv = (LinearListView) findViewById(R.id.number_tv);
	}
	
	public static ContactSimpleInfoView create(Context context) {
		return (ContactSimpleInfoView) View.inflate(context, R.layout.contact_simple_info_view, null);
	}
}

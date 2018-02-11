package com.android.contacts.widget;

import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AuroraDialerSearchItemView extends RelativeLayout {
	
	public AuroraDialerSearchItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AuroraDialerSearchItemView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public View mMainContainer;
    public TextView mName;
    public TextView mNumber;
    public TextView mArea;
    public ImageView mSecondaryButton;
    public View mCallLog;
    public View mDivider;
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();		
		mMainContainer = findViewById(R.id.main_container);
		mName = (TextView) findViewById(R.id.name);
		mNumber = (TextView) findViewById(R.id.number);
		mArea = (TextView) findViewById(R.id.area);
		mSecondaryButton  = (ImageView)findViewById(R.id.secondary_action_icon);
		mCallLog = findViewById(R.id.aurora_search_calllog);
		mDivider = findViewById(R.id.aurora_second_divider);
	}
	
	public static AuroraDialerSearchItemView create(Context context) {
		if(GNContactsUtils.isMultiSimEnabled()) {
			return (AuroraDialerSearchItemView) LayoutInflater.from(context).inflate(R.layout.aurora_dialer_search_item_view, null);
		}else{
			return (AuroraDialerSearchItemView) LayoutInflater.from(context).inflate(R.layout.aurora_dialer_search_item_view1, null);
		}
		
	}

}

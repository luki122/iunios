package com.android.contacts.widget;

import com.android.contacts.ContactsApplication;
import com.android.contacts.GNContactsUtils;
import com.android.contacts.R;
import com.android.contacts.calllog.AuroraCallLogFragmentV2;
import com.mediatek.contacts.dialpad.AuroraDialerSearchAdapter;

import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraRoundedImageView;

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
//    public ImageView mSecondaryButton;
    public View mCallLog;
//    public View mDivider;
    public AuroraRoundedImageView contactPhoto;
//    public ImageView contactPhoto;
//    public View contact_photo_rl1;
//    public View contact_expand_layout;
    public View name_ll,number_ll;
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();		
		mMainContainer = findViewById(R.id.main_container);
		mName = (TextView) findViewById(R.id.name);
		mNumber = (TextView) findViewById(R.id.number);
		mArea = (TextView) findViewById(R.id.area);
//		mSecondaryButton  = (ImageView)findViewById(R.id.secondary_action_icon);
		mCallLog = findViewById(R.id.aurora_search_calllog);
//		mDivider = findViewById(R.id.aurora_second_divider);
		
		contactPhoto=(AuroraRoundedImageView)findViewById(R.id.contact_photo1);
//		contact_photo_rl1=findViewById(R.id.contact_photo_rl1);
//		contact_expand_layout=findViewById(R.id.contact_expand_layout);
		name_ll=findViewById(R.id.name_ll);
		number_ll=findViewById(R.id.number_ll);
		
	}
	
	public static AuroraDialerSearchItemView create(Context context) {
		if(ContactsApplication.isMultiSimEnabled) {
			return (AuroraDialerSearchItemView) LayoutInflater.from(context).inflate(R.layout.aurora_dialer_search_item_view, null);
		}else{
			return (AuroraDialerSearchItemView) LayoutInflater.from(context).inflate(R.layout.aurora_dialer_search_item_view1, null);
		}
		
	}
	
//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		try{
//			if(AuroraDialerSearchAdapter.dismissPopupWindow()) return true;
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//		return false;
//	}
}

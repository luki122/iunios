package com.android.contacts.quickcontact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryView;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.contacts.R;

import android.net.Uri;
import android.database.Cursor;
import android.graphics.drawable.Drawable;


public class PhoneExpandingEntryCardView extends ExpandingEntryCardView {

	private Context mContext;
	public PhoneExpandingEntryCardView(Context context) {
		this(context, null);
	}

	public PhoneExpandingEntryCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	protected View createEntryView(LayoutInflater layoutInflater,
			final Entry entry, int iconVisibility) {
		View view = super
				.createEntryView(layoutInflater, entry, iconVisibility);
		final TextView header = (TextView) view.findViewById(R.id.header);
		final TextView subheader = (TextView) view.findViewById(R.id.sub_header);		
		final View viewseparator = view.findViewById(R.id.sub_header_separator);				
		String number = entry.getHeader();	
		String recent = mContext.getString(R.string.aurora_recent_call);
		if(number.contains(recent)) {
			int end = number.lastIndexOf(recent);
			number = number.substring(0, end -1).trim();
			header.setText(number);			
			viewseparator.setVisibility(View.VISIBLE);
			subheader.setVisibility(View.VISIBLE);
			subheader.setText(recent);
		} 
		if ((entry.getIconResourceId() == R.drawable.ic_phone_24dp) && isBlackNumber(number)) {
			Drawable right = mContext.getResources().getDrawable(
					R.drawable.svg_dial_reject);
			int w = mContext.getResources().getDimensionPixelSize(
	                R.dimen.aurora_reject_icon_width);
			int h = mContext.getResources().getDimensionPixelSize(
	                R.dimen.aurora_reject_icon_height);
			right.setBounds(0, 0, w, h);
			header.setCompoundDrawables(null, null, right, null);
		} else {
			header.setCompoundDrawables(null, null, null, null);
		}

		  final TextView text = (TextView) view.findViewById(R.id.text);
        if(text.getVisibility() == View.GONE) {
			RelativeLayout.LayoutParams fllp = (RelativeLayout.LayoutParams)header.getLayoutParams();
			fllp.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
			fllp.addRule(RelativeLayout.CENTER_VERTICAL);
			header.setLayoutParams(fllp);
        }
		return view;
	}

	private static Uri black_uri = Uri.parse("content://com.android.contacts/black");

	private static final String[] BLACK_PROJECTION = new String[] {
		"_id",   
		"isblack",  
		"lable",   
		"black_name", 
		"number", 
		"reject" 
	};

	public String StringFilter(String str)   throws   PatternSyntaxException   {      
		// 只允许字母和数字        
		// String   regEx  =  "[^a-zA-Z0-9]";
		// 清除掉所有特殊字符
		String regEx = "[^0-9]";
//		String regEx="[`~!@#$%^&*()+-=|{}':;',//[//].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]P";
		Pattern   p   =   Pattern.compile(regEx);     
		Matcher   m   =   p.matcher(str);     
		return   m.replaceAll("").trim();     
	} 


	public boolean isBlackNumber(String number) {
		Log.v("PhoneExpandingEntryCardView", " isBlackNumber number = " + number);

		number=StringFilter(number);
		number=number.replaceAll(" ", "");
		Log.d("liyang","\\number:"+number);

		if(TextUtils.isEmpty(number)) {
			return false;
		}

		Cursor cursor = mContext.getContentResolver().query(black_uri, BLACK_PROJECTION,
				"(reject = '1' OR reject = '3') AND PHONE_NUMBERS_EQUAL(number, " + number + ", 0)", null, null);
		Log.v("PhoneExpandingEntryCardView", " cursor = " + cursor);
		try {
			if (cursor != null && cursor.getCount() > 0) {
				return true;
			}
			return false;
		} finally {
			if(cursor != null) {
				cursor.close();
				cursor = null;
			}
		}
	}

}
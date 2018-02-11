package com.android.contacts.quickcontact;

import com.android.contacts.quickcontact.ExpandingEntryCardView.Entry;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryTag;
import com.android.contacts.quickcontact.ExpandingEntryCardView.EntryView;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.contacts.R;

import android.util.Log;

public class CallLogExpandingEntryCardView extends ExpandingEntryCardView {
	ImageView mTitleIcon;

	private final int mMissedCallColor;

	public CallLogExpandingEntryCardView(Context context) {
		this(context, null);
	}

	public CallLogExpandingEntryCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// Aurora xuyong 2016-01-13 deleted fora aurora 2.0 new feature start
		// mTitleIcon = (ImageView) findViewById(R.id.title_icon);
		// mTitleIcon.setVisibility(View.VISIBLE);
		// Aurora xuyong 2016-01-13 deleted fora aurora 2.0 new feature end
		mMissedCallColor = context.getResources().getColor(
				R.color.aurora_calllog_missed_color_v2);
	}

	protected EntryView createEntryViewLayout(LayoutInflater layoutInflater) {
		EntryView view = (EntryView) layoutInflater.inflate(
				R.layout.calllog_expanding_entry_card_item, this, false);
		return view;
	}

	protected View createEntryView(LayoutInflater layoutInflater,
			final Entry entry, int iconVisibility) {
		View view = super.createEntryView(layoutInflater, entry, View.GONE);
		final ImageView alternateIcon = (ImageView) view
				.findViewById(R.id.icon_alternate);
		alternateIcon.setOnClickListener(null);

		final TextView text = (TextView) view.findViewById(R.id.text);
		if (!TextUtils.isEmpty(entry.getText())) {
			if (entry.shouldApplyColor()) {
				text.setTextColor(mMissedCallColor);
			}
		}

		return view;
	}

	public void setCallLogMenuButtonListener(OnClickListener l) {
		// Aurora xuyong 2016-01-13 deleted fora aurora 2.0 new feature start
		// mTitleIcon.setOnClickListener(l);
		// Aurora xuyong 2016-01-13 deleted fora aurora 2.0 new feature end
	}

}
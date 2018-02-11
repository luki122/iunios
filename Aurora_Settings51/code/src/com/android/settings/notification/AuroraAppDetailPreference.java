package com.android.settings.notification;

import com.android.settings.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.preference.AuroraPreference;

public class AuroraAppDetailPreference extends AuroraPreference {

	public ImageView mIcon;
	public TextView mAppName;
	public TextView mVersion;

	public Drawable mDrawable;
	public CharSequence mName;
	public CharSequence mVersionName;

	public AuroraAppDetailPreference(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public AuroraAppDetailPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public AuroraAppDetailPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		setLayoutResource(R.layout.app_info_layout);
		setSelectable(false);
	}

	@Override
	protected void onBindView(View view) {
		// TODO Auto-generated method stub
		super.onBindView(view);
		mIcon = (ImageView) view.findViewById(R.id.appIcon);
		if (mDrawable != null) {
			mIcon.setBackground(mDrawable);
		}

		mAppName = (TextView) view.findViewById(R.id.appName);
		if (mName != null) {
			mAppName.setText(mName);
		}

		mVersion = (TextView) view.findViewById(R.id.version);
		if (mVersionName != null) {
			mVersion.setText(mVersionName);
		}
	}

	public void setIcon(Drawable drawable) {
		mDrawable = drawable;
	}

	public void setAppName(CharSequence name) {
		mName = name;
	}

	public void setVersion(String name) {
		mVersionName = name;
	}

}
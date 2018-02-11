package com.android.contacts.widget;

import com.android.contacts.R;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class GnDigitsContainer extends FrameLayout {

	public GnDigitsContainer(Context context) {
		super(context);
	}
	
	public GnDigitsContainer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public GnDigitsContainer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		
		View area = findViewById(R.id.num_area);
		View relatively = findViewById(R.id.digits);
		if (null != area && null != relatively) {
			if (area.getLayoutParams() instanceof LinearLayout.LayoutParams) {				
				LinearLayout.LayoutParams params = ((LinearLayout.LayoutParams)(area.getLayoutParams()));				
				params.setMargins(params.leftMargin, 0, 0, relatively.getHeight()/2);
			}
		}
	}
}

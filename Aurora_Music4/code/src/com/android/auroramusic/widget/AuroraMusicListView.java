package com.android.auroramusic.widget;

import android.content.Context;
import android.util.AttributeSet;
import aurora.widget.AuroraListView;

public class AuroraMusicListView extends AuroraListView {

	public AuroraMusicListView(Context context) {
		this(context, null);

	}

	public AuroraMusicListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);

	}

	public AuroraMusicListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

	}

	/**
	 * 设置不滚动
	 */
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}
}

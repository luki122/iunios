package com.aurora.thememanager.widget;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.LinearLayout;
import com.aurora.thememanager.R;
public class ThemeListCardView extends LinearLayout{
	private MyOutLineProvider mOutLineProvider;

	private boolean mHasMeasured = false;
	public ThemeListCardView(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
	}

	public ThemeListCardView(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
	}

	public ThemeListCardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public ThemeListCardView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	
	
	/**
	 * clip this view to cycle by set outline
	 */
	private void clip() {
		if (mOutLineProvider == null) {
			mOutLineProvider = new MyOutLineProvider();
		}
		setClipToOutline(true);
		setOutlineProvider(mOutLineProvider);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!mHasMeasured) {
			clip();
			mHasMeasured = true;
		}
	}

	
	
	class MyOutLineProvider extends ViewOutlineProvider {

		@Override
		public void getOutline(View view, Outline outline) {
			// TODO Auto-generated method stub
			int radius = getResources().getDimensionPixelSize(R.dimen.theme_list_card_radius);
			outline.setRoundRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), radius);
		}

	}
	
	
	
	
	
	
	
	
}

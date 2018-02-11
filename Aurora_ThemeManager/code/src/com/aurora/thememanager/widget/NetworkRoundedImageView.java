package com.aurora.thememanager.widget;

import android.content.Context;
import android.graphics.Outline;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.aurora.internetimage.NetworkImageView;

public class NetworkRoundedImageView extends NetworkImageView {
	private MyOutLineProvider mOutLineProvider;

	private boolean mHasMeasured = false;

	private boolean mHasLayouted = false;
	public NetworkRoundedImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public NetworkRoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public NetworkRoundedImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		// TODO Auto-generated method stub
		super.onLayout(changed, left, top, right, bottom);
		if (!mHasLayouted && !mHasMeasured) {
			final int width = right - left;
			final int height = bottom - top;
			resizeInLayout(width, height);
			setClipToOutline(true);
			setOutlineProvider(new ViewOutlineProvider() {

				@Override
				public void getOutline(View view, Outline outline) {
					// TODO Auto-generated method stub
					outline.setOval(0, 0, width, height);
				}

			});
			mHasLayouted = true;
		}
	}

	private void resizeInLayout(int width, int height) {
		ViewGroup.LayoutParams params = getLayoutParams();
		params.width = width;
		params.height = height;
		setLayoutParams(params);
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

	/**
	 * ensure this view is a cycle
	 */
	private void resize() {
		ViewGroup.LayoutParams params = getLayoutParams();
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		if (width > height) {
			height = width;
		} else {
			width = height;
		}
		params.width = width;
		params.height = height;

		setLayoutParams(params);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!mHasMeasured) {
			resize();
			clip();
			mHasMeasured = true;
		}
	}

	class MyOutLineProvider extends ViewOutlineProvider {

		@Override
		public void getOutline(View view, Outline outline) {
			// TODO Auto-generated method stub
			outline.setOval(0, 0, view.getMeasuredWidth(),
					view.getMeasuredHeight());
		}

	}
	

}

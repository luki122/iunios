package com.aurora.account.widget;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.account.R;
import com.aurora.account.widget.QuickSearchBar.OnTouchingLetterChangedListener;
import com.aurora.account.widget.stickylistheaders.StickyListHeadersListView;

public class QuickSearchView extends FrameLayout {
	
	private FrameLayout toastFatherLayout;
	private LinearLayout toastLayout;
	private TextView overlay;
	private Handler handler = new Handler();
	private OverlayThread overlayThread = new OverlayThread();
	private int initMarginRight;
	private LayoutParams layoutParams;
	private QuickSearchBar quickSearchBar;

	public QuickSearchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public QuickSearchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public QuickSearchView(Context context) {
		super(context);
		initView(context);
	}

	private void initView(Context context) {
		this.toastFatherLayout = (FrameLayout) LayoutInflater.from(context)
				.inflate(R.layout.view_quick_search, this);
		toastLayout = (LinearLayout) toastFatherLayout.findViewById(R.id.toastLayout);
		overlay = (TextView) toastFatherLayout.findViewById(R.id.tvLetter);
		quickSearchBar = (QuickSearchBar) toastFatherLayout.findViewById(R.id.quickSearchBar);
	}
	
	private class OverlayThread implements Runnable {
		public void run() {
			if (toastLayout != null) {
				toastLayout.setVisibility(View.GONE);
			}
		}
	}

	/**
	 * 更换显示的内容
	 * 
	 * @param s
	 * @param positionOfY
	 * @param listview
	 * @param adapter
	 */
	public void LetterChanged(String s, float positionOfY, StickyListHeadersListView listview,
			BaseAdapter adapter) {
		if (overlay == null || toastLayout == null || handler == null
				|| overlayThread == null || listview == null || adapter == null) {
			return;
		}
		
		if (layoutParams == null) {
			initMarginRight = toastFatherLayout.getWidth() - toastLayout.getRight();
			layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.RIGHT;
		}
		
		int position;
//		updateOverlayPosition(positionOfY);
		overlay.setText(s.toUpperCase());
		toastLayout.setVisibility(View.VISIBLE);
		handler.removeCallbacks(overlayThread);
		handler.postDelayed(overlayThread, 500);
		
		position = alphaIndexer(adapter, s);
		
		if (QuickSearchBar.CHARTS[0].equals(s) && adapter.getCount() > 0) {
			listview.setSelection(0);
		} else if (position >= 0) {
			listview.setSelection(position);
		}
	}

	/**
	 * 更新显示选中字母的位置
	 * @param downPositionOfY 点击点在Y轴的位置
	 */
	private void updateOverlayPosition(float downPositionOfY) {
		if (toastLayout == null || toastFatherLayout == null
				|| layoutParams == null) {
			return;
		}

		int top = (int) (downPositionOfY - toastLayout.getHeight() / 2);
		if (top < 0) {
			top = 0;
		} else if (top + toastLayout.getHeight() > toastFatherLayout
				.getHeight()) {
			top = toastFatherLayout.getHeight() - toastLayout.getHeight();
		}

		layoutParams.setMargins(0, top, initMarginRight, 0);
		toastFatherLayout.updateViewLayout(toastLayout, layoutParams);
	}

	private int alphaIndexer(BaseAdapter adapter, String s) {
		int position = -1;
		if (TextUtils.isEmpty(s) || adapter == null) {
			return position;
		}
		
		if (quickSearchBar.getOnTouchingLetterChangedListener() != null) {
			return quickSearchBar.getOnTouchingLetterChangedListener().getToPosition(s);
		}
		
		return position;
	}
	
	public void setOnTouchingLetterChangedListener(
			OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		if (quickSearchBar != null) {
			quickSearchBar.setOnTouchingLetterChangedListener(onTouchingLetterChangedListener);
		}
	}
	
	public void setCurChooseTitle(String title) {
		if (quickSearchBar != null) {
			quickSearchBar.setCurChooseTitle(title);
		}
	}

}

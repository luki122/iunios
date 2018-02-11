package com.android.phase1.cinema;

import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.browser.R;

public class WebViewCinema extends Cinema {

	protected boolean contentViewExpanded = false;
	private int titlebarHeight;
	private int toolbarHeight;

	public WebViewCinema(Context context, View contentView, View fixedTitlebarContainer,
			View toolBarContainer) {
		super(context,contentView, fixedTitlebarContainer, toolBarContainer);
		// TODO Auto-generated constructor stub
		titlebarHeight = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_browser_titlebar_height);
		 toolbarHeight = context.getResources().getDimensionPixelSize(
				R.dimen.aurora_setting_bar_height);
	}

	
	@Override
	public void setProgress(float progress) {
		if (Float.compare(progress, 0f) == 0) {
			shrinkContentView();
		}
		if (Float.compare(progress, 1f) == 0) {
			expandContentView();
		}

	}

	public void expandContentView() {
		

		if (!contentViewExpanded) {
			contentViewExpanded = true;
			int newHeight = mContentView.getHeight()
					+ titlebarHeight
					+toolbarHeight;
			int newWidth = mContentView.getWidth();
			mContentView.setLayoutParams(new RelativeLayout.LayoutParams(
					newWidth, newHeight));
		}
	}

	public void shrinkContentView() {
	

		if (contentViewExpanded) {
			contentViewExpanded = false;
			int newHeight = mContentView.getHeight()
					- titlebarHeight
					- toolbarHeight;
			int newWidth = mContentView.getWidth();
			mContentView.setLayoutParams(new RelativeLayout.LayoutParams(
					newWidth, newHeight));
		}
	}

	@Override
	public void create(Context context) {
		// TODO Auto-generated method stub
		
	}

}

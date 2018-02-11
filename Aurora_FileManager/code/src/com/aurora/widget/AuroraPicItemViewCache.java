package com.aurora.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.filemanager.R;

public class AuroraPicItemViewCache {
	private ImageView firstImage;
	private ImageView picCheckbox_yes;
	private ImageView picCheckbox_no;
	private RelativeLayout layout;
	private View baseView;
	private View headerView;

	public View getHeaderView() {
		if (headerView == null) {
			headerView = (View) baseView.findViewById(R.id.headerView);
		}
		return headerView;
	}

	public AuroraPicItemViewCache(View baseView) {
		this.baseView = baseView;
	}

	

	public ImageView getFirstImage() {
		if (firstImage == null) {
			firstImage = (ImageView) baseView.findViewById(R.id.firstImage);
		}
		return firstImage;
	}

	public ImageView getPicCheckboxNo() {
		if (picCheckbox_no == null) {
			picCheckbox_no = (ImageView) baseView
					.findViewById(R.id.picCheckbox_no);
		}
		return picCheckbox_no;
	}

	public ImageView getPicCheckboxYes() {
		if (picCheckbox_yes == null) {
			picCheckbox_yes = (ImageView) baseView
					.findViewById(R.id.picCheckbox_ok);
		}
		return picCheckbox_yes;
	}

	public RelativeLayout getLayout() {
		if (layout == null) {
			layout = (RelativeLayout) baseView.findViewById(R.id.layout);
		}
		return layout;
	}
}

package com.aurora.widget;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.filemanager.R;

/**
 * 图片分类大图
 */
public class AuroraPicViewCache {
	private TextView picFoldName;
	private TextView picCount;
	private ImageView firstImage;
	private ImageView secondImage;
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

	public AuroraPicViewCache(View baseView) {
		this.baseView = baseView;
	}

	public TextView getPicFoldName() {
		if (picFoldName == null) {
			picFoldName = (TextView) baseView.findViewById(R.id.picFoldName);
		}
		return picFoldName;
	}

	public TextView getPicCount() {
		if (picCount == null) {
			picCount = (TextView) baseView.findViewById(R.id.picCount);
		}
		return picCount;
	}

	public ImageView getFirstImage() {
		if (firstImage == null) {
			firstImage = (ImageView) baseView.findViewById(R.id.firstImage);
		}
		return firstImage;
	}

	public ImageView getSecondImage() {
		if (secondImage == null) {
			secondImage = (ImageView) baseView.findViewById(R.id.secondImage);
		}
		return secondImage;
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

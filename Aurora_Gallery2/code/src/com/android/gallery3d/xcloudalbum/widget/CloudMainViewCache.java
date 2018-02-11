package com.android.gallery3d.xcloudalbum.widget;

import java.util.List;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;
import com.android.gallery3d.local.widget.GalleryAlbumItemView;
import com.android.gallery3d.local.widget.MediaFileInfo;

public class CloudMainViewCache {

	private ImageView checkbox_ok, checkbox_no;
	private ImageView cloud_first_img, cloud_second_img, cloud_third_img, cloud_fourth_img;
	private RelativeLayout aroura_cloud_title, layout;
	private TextView album_title, album_amount;

	private View rootView;

	public CloudMainViewCache(View rootView) {
		super();
		this.rootView = rootView;
	}

	public ImageView getCheckboxOk() {
		if (checkbox_ok == null) {
			checkbox_ok = (ImageView) rootView.findViewById(R.id.checkbox_ok);
		}
		return checkbox_ok;
	}

	public ImageView getCheckboxNo() {
		if (checkbox_no == null) {
			checkbox_no = (ImageView) rootView.findViewById(R.id.checkbox_no);
		}
		return checkbox_no;
	}

	public RelativeLayout getRelativeLayoutTitle() {
		if (aroura_cloud_title == null) {
			aroura_cloud_title = (RelativeLayout) rootView
					.findViewById(R.id.aroura_cloud_title);
		}
		return aroura_cloud_title;
	}

	public TextView getAlbumTitle() {
		if (album_title == null) {
			album_title = (TextView) rootView.findViewById(R.id.album_title);
		}
		return album_title;
	}

	public TextView getAlbumAmount() {
		if (album_amount == null) {
			album_amount = (TextView) rootView.findViewById(R.id.album_amount);
		}
		return album_amount;
	}

	public RelativeLayout getLayout() {
		if (layout == null) {
			layout = (RelativeLayout) rootView.findViewById(R.id.layout);
		}
		return layout;
	}

	public ImageView getCloud_first_img() {
		if (cloud_first_img == null) {
			cloud_first_img = (ImageView) rootView.findViewById(R.id.cloud_first_img);
		}
		return cloud_first_img;
	}

	public ImageView getCloud_second_img() {
		if (cloud_second_img == null) {
			cloud_second_img = (ImageView) rootView.findViewById(R.id.cloud_second_img);
		}
		return cloud_second_img;
	}

	public ImageView getCloud_third_img() {
		if (cloud_third_img == null) {
			cloud_third_img = (ImageView) rootView.findViewById(R.id.cloud_third_img);
		}
		return cloud_third_img;
	}

	public ImageView getCloud_fourth_img() {
		if (cloud_fourth_img == null) {
			cloud_fourth_img = (ImageView) rootView.findViewById(R.id.cloud_fourth_img);
		}
		return cloud_fourth_img;
	}

}

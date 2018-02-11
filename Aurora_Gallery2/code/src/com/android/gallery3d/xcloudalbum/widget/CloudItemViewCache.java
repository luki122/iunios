package com.android.gallery3d.xcloudalbum.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

public class CloudItemViewCache {

	private ImageView album_image, checkbox_ok, checkbox_no;
	private RelativeLayout layout;

	private View rootView;

	public CloudItemViewCache(View rootView) {
		super();
		this.rootView = rootView;
	}

	public ImageView getAlbumImage() {
		if (album_image == null) {
			album_image = (ImageView) rootView.findViewById(R.id.album_image);
		}
		return album_image;
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

	public RelativeLayout getRelativeLayout() {
		if (layout == null) {
			layout = (RelativeLayout) rootView.findViewById(R.id.layout);
		}
		return layout;
	}


}

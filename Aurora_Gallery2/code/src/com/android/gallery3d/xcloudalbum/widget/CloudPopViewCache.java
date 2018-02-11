package com.android.gallery3d.xcloudalbum.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.gallery3d.R;

public class CloudPopViewCache {

	private ImageView album_image;
	private TextView album_name;

	private View rootView;

	public CloudPopViewCache(View rootView) {
		super();
		this.rootView = rootView;
	}

	public ImageView getAlbumImage() {
		if (album_image == null) {
			album_image = (ImageView) rootView.findViewById(R.id.pop_image);
		}
		return album_image;
	}

	public TextView getAlbumName() {
		if (album_name == null) {
			album_name = (TextView) rootView.findViewById(R.id.album_name);
		}
		return album_name;
	}

}

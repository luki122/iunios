package com.android.gallery3d.local.widget;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.gallery3d.R;

public class AlbumsPopViewCache {

	private ImageView album_image;
	private TextView album_name, album_name_num;
	private FrameLayout image_frame;
	private View rootView;

	public AlbumsPopViewCache(View rootView) {
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

	public TextView getAlbumNameNum() {
		if (album_name_num == null) {
			album_name_num = (TextView) rootView.findViewById(R.id.album_name_num);
		}
		return album_name_num;
	}

	public FrameLayout getImageframe() {
		if (image_frame == null) {
			image_frame = (FrameLayout) rootView.findViewById(R.id.image_frame);
		}
		return image_frame;
	}

}

package com.android.gallery3d.local.widget;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

public class GalleryItemView {

	private ImageView album_image, checkbox_ok, checkbox_no,collection_image,isvideo_image;
	private RelativeLayout layout;
	private FrameLayout album_image_frame;

	private View rootView;

	public GalleryItemView(View rootView) {
		super();
		this.rootView = rootView;
	}

	public ImageView getAlbumImage() {
		if (album_image == null) {
			album_image = (ImageView) rootView.findViewById(R.id.album_image);
		}
		return album_image;
	}
	
	public ImageView getCollectionImage(){
		if (collection_image == null) {
			collection_image = (ImageView) rootView.findViewById(R.id.collection_image);
		}
		return collection_image;
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
	
	public ImageView getIsvideoImage() {
		if (isvideo_image == null) {
			isvideo_image = (ImageView) rootView.findViewById(R.id.isvideo_image);
		}
		return isvideo_image;
	}

	public RelativeLayout getRelativeLayout() {
		if (layout == null) {
			layout = (RelativeLayout) rootView.findViewById(R.id.layout);
		}
		return layout;
	}
	
	public FrameLayout getFrameLayout() {
		if (album_image_frame == null) {
			album_image_frame = (FrameLayout) rootView.findViewById(R.id.album_image_frame);
		}
		return album_image_frame;
	}


}

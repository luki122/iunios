package com.android.gallery3d.xcloudalbum.widget;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.gallery3d.R;

public class CloudMainViewCache {

	private ImageView album_image, checkbox_ok, checkbox_no;
	private RelativeLayout aroura_cloud_title, layout;
	private TextView album_title, album_subtitle, album_amount,
			create_album_text;

	private View rootView, headView;

	public CloudMainViewCache(View rootView) {
		super();
		this.rootView = rootView;
	}

	public View getHeadView() {
		if (headView == null) {
			headView = rootView.findViewById(R.id.headview);
		}
		return headView;
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

	public TextView getCreateAlbumText() {
		if (create_album_text == null) {
			create_album_text = (TextView) rootView
					.findViewById(R.id.create_album_text);
		}
		return create_album_text;
	}

	public TextView getAlbumSubtitle() {
		if (album_subtitle == null) {
			album_subtitle = (TextView) rootView
					.findViewById(R.id.album_subtitle);
		}
		return album_subtitle;
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

}

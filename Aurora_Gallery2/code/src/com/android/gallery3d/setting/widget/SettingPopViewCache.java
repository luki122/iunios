package com.android.gallery3d.setting.widget;

import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraSwitch;

import com.android.gallery3d.R;

public class SettingPopViewCache {

	private ImageView album_image;
	private TextView album_name,album_name_num;
	private AuroraSwitch mAuroraSwitch;
	private FrameLayout image_frame;

	private View rootView;

	public SettingPopViewCache(View rootView) {
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

	public AuroraSwitch getmAuroraSwitch() {
		if (mAuroraSwitch == null) {
			mAuroraSwitch = (AuroraSwitch) rootView.findViewById(R.id.setting_checkbox);
		}
		return mAuroraSwitch;
	}

	public FrameLayout getImageframe() {
		if (image_frame == null) {
			image_frame = (FrameLayout) rootView.findViewById(R.id.image_frame);
		}
		return image_frame;
	}
}

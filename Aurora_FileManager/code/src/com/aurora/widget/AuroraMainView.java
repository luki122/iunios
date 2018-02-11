package com.aurora.widget;

import com.aurora.dbutil.FileCategoryHelper.CategoryInfo;
import com.aurora.dbutil.FileCategoryHelper.FileCategory;

import android.view.View;
import android.view.ViewStub;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurora.filemanager.R;

public class AuroraMainView {

	private RelativeLayout category_picture, category_video, category_document,
			category_apk, category_music, category_download;
	private TextView category_picture_count, category_video_count,
			category_document_count, category_apk_count, category_music_count,
			category_download_count;

	private RelativeLayout usb_card_storage_single, usb_card_storage,
			sd_card_storage_rela, category_page;
	private TextView usb_card_storage_size, sd_card_storage_size,
			usb_card_storage_size_2;
	private RelativeLayout sd_card_storage, search_liner;
	private View rootView, b_view;
	private ViewStub searchViewStub,sdViewStub;

	public AuroraMainView(View rootView) {
		super();
		this.rootView = rootView;
	}

	public View getBottomView() {
		if (b_view == null) {
			b_view = rootView.findViewById(R.id.b_view);
		}
		return b_view;
	}

	public RelativeLayout getSearch_liner() {
		if (search_liner == null) {
			search_liner = (RelativeLayout) rootView
					.findViewById(R.id.goto_search_mode);
		}
		return search_liner;
	}

	public ViewStub getSearchViewStub() {
		if (searchViewStub == null) {
			searchViewStub = (ViewStub)rootView.findViewById(R.id.search_view_stub);
		}
		return searchViewStub;
	}
	
	public ViewStub getSdViewStub() {
		if (sdViewStub == null) {
			sdViewStub = (ViewStub)rootView.findViewById(R.id.sd_view_stub);
		}
		return sdViewStub;
	}

	/**
	 * @return the category_picture
	 */
	public RelativeLayout getCategory_picture() {
		if (category_picture == null) {
			category_picture = (RelativeLayout) rootView
					.findViewById(R.id.category_picture);
		}
		return category_picture;
	}

	/**
	 * @return the category_video
	 */
	public RelativeLayout getCategory_video() {
		if (category_video == null) {
			category_video = (RelativeLayout) rootView
					.findViewById(R.id.category_video);
		}
		return category_video;
	}

	/**
	 * @return the category_document
	 */
	public RelativeLayout getCategory_document() {
		if (category_document == null) {
			category_document = (RelativeLayout) rootView
					.findViewById(R.id.category_document);
		}
		return category_document;
	}

	/**
	 * @return the category_apk
	 */
	public RelativeLayout getCategory_apk() {
		if (category_apk == null) {
			category_apk = (RelativeLayout) rootView
					.findViewById(R.id.category_apk);
		}
		return category_apk;
	}

	/**
	 * @return the category_music
	 */
	public RelativeLayout getCategory_music() {
		if (category_music == null) {
			category_music = (RelativeLayout) rootView
					.findViewById(R.id.category_music);
		}
		return category_music;
	}

	/**
	 * @return the category_download
	 */
	public RelativeLayout getCategory_download() {
		if (category_download == null) {
			category_download = (RelativeLayout) rootView
					.findViewById(R.id.category_download);
		}
		return category_download;
	}

	/**
	 * @return the category_picture_count
	 */
	public TextView getCategory_picture_count() {
		if (category_picture_count == null) {
			category_picture_count = (TextView) rootView
					.findViewById(R.id.category_picture_count);
		}
		return category_picture_count;
	}

	/**
	 * @return the category_video_count
	 */
	public TextView getCategory_video_count() {
		if (category_video_count == null) {
			category_video_count = (TextView) rootView
					.findViewById(R.id.category_video_count);
		}
		return category_video_count;
	}

	/**
	 * @return the category_document_count
	 */
	public TextView getCategory_document_count() {
		if (category_document_count == null) {
			category_document_count = (TextView) rootView
					.findViewById(R.id.category_document_count);
		}
		return category_document_count;
	}

	/**
	 * @return the category_apk_count
	 */
	public TextView getCategory_apk_count() {
		if (category_apk_count == null) {
			category_apk_count = (TextView) rootView
					.findViewById(R.id.category_apk_count);
		}
		return category_apk_count;
	}

	/**
	 * @return the category_music_count
	 */
	public TextView getCategory_music_count() {
		if (category_music_count == null) {
			category_music_count = (TextView) rootView
					.findViewById(R.id.category_music_count);
		}
		return category_music_count;
	}

	/**
	 * @return the category_download_count
	 */
	public TextView getCategory_download_count() {
		if (category_download_count == null) {
			category_download_count = (TextView) rootView
					.findViewById(R.id.category_download_count);
		}
		return category_download_count;
	}

	/**
	 * @return the usb_card_storage_single
	 */
	public RelativeLayout getUsb_card_storage_single() {
		if (usb_card_storage_single == null) {
			usb_card_storage_single = (RelativeLayout) rootView
					.findViewById(R.id.usb_card_storage_single);
		}
		return usb_card_storage_single;
	}

	/**
	 * @return the usb_card_storage
	 */
	public RelativeLayout getUsb_card_storage() {
		if (usb_card_storage == null) {
			usb_card_storage = (RelativeLayout) rootView
					.findViewById(R.id.usb_card_storage);
		}
		return usb_card_storage;
	}

	/**
	 * @return the sd_card_storage_rela
	 */
	public RelativeLayout getSd_card_storage_rela() {
		if (sd_card_storage_rela == null) {
			sd_card_storage_rela = (RelativeLayout) rootView
					.findViewById(R.id.sd_card_storage_rela);
		}
		return sd_card_storage_rela;
	}

	/**
	 * @return the category_page
	 */
	public RelativeLayout getCategory_page() {
		if (category_page == null) {
			category_page = (RelativeLayout) rootView
					.findViewById(R.id.category_page);
		}
		return category_page;
	}

	/**
	 * @return the usb_card_storage_size
	 */
	public TextView getUsb_card_storage_size() {
		if (usb_card_storage_size == null) {
			usb_card_storage_size = (TextView) rootView
					.findViewById(R.id.usb_card_storage_size);
		}
		return usb_card_storage_size;
	}

	/**
	 * @return the sd_card_storage_size
	 */
	public TextView getSd_card_storage_size() {
		if (sd_card_storage_size == null) {
			sd_card_storage_size = (TextView) rootView
					.findViewById(R.id.sd_card_storage_size);
		}
		return sd_card_storage_size;
	}

	/**
	 * @return the usb_card_storage_size_2
	 */
	public TextView getUsb_card_storage_size_2() {
		if (usb_card_storage_size_2 == null) {
			usb_card_storage_size_2 = (TextView) rootView
					.findViewById(R.id.usb_card_storage_size_2);
		}
		return usb_card_storage_size_2;
	}

	/**
	 * @return the sd_card_storage
	 */
	public RelativeLayout getSd_card_storage() {
		if (sd_card_storage == null) {
			sd_card_storage = (RelativeLayout) rootView
					.findViewById(R.id.sd_card_storage);
		}
		return sd_card_storage;
	}

	public void setFileCategoryInfo(FileCategory fc, long count) {
		switch (fc) {
		case Music:
			getCategory_music_count().setText(count + "");
			break;
		case Video:
			getCategory_video_count().setText(count + "");
			break;
		case Picture:
			getCategory_picture_count().setText(count + "");
			break;
		case Doc:
			getCategory_document_count().setText(count + "");
			break;
		case DownLoad:
			getCategory_download_count().setText(count + "");
			break;
		case Apk:
			getCategory_apk_count().setText(count + "");
			break;

		default:
			break;
		}
	}

}

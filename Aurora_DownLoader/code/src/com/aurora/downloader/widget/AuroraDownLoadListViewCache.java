package com.aurora.downloader.widget;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

import com.aurora.downloader.R;


public class AuroraDownLoadListViewCache {
	private View baseView;

	private DownloadButton download_control;
	private TextView download_title;
//	private TextView download_title_done;
	private TextView modified_time;
	private TextView totalSize;
	private TextView status_text;
	private TextView size_text;

	private RelativeLayout mainUi;
	private ProgressBar download_progress;
	private ImageView download_icon;

	private AuroraCheckBox checkbox;

	public AuroraDownLoadListViewCache(View baseView) {
		this.baseView = baseView;
	}
	
	public View getTagByKey(String key){
		return baseView.findViewWithTag(key);
	}

	public RelativeLayout getLayoutMainUi() {
		if (mainUi == null) {
			mainUi = (RelativeLayout) baseView
					.findViewById(com.aurora.R.id.aurora_listview_front);
		}
		return mainUi;
	}

	public AuroraCheckBox getCheckBox() {
		if (checkbox == null) {
			checkbox = (AuroraCheckBox) baseView
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		}
		return checkbox;
	}

	public DownloadButton getDownload_control() {
		if (download_control == null) {
			download_control = (DownloadButton) baseView
					.findViewById(R.id.download_control);
		}
		return download_control;
	}

	public TextView getDownload_title() {
		if (download_title == null) {
			download_title = (TextView) baseView
					.findViewById(R.id.download_title);
		}
		return download_title;
	}

//	public TextView getDownload_title_done() {
//		if (download_title_done == null) {
//			download_title_done = (TextView) baseView
//					.findViewById(R.id.download_title_done);
//		}
//		return download_title_done;
//	}

	public TextView getLast_modified_time() {
		if (modified_time == null) {
			modified_time = (TextView) baseView
					.findViewById(R.id.modified_time);
		}
		return modified_time;
	}

	public TextView getTotalSize() {
		if (totalSize == null) {
			totalSize = (TextView) baseView.findViewById(R.id.totalSize);
		}
		return totalSize;
	}

	public TextView getStatus_text() {
		if (status_text == null) {
			status_text = (TextView) baseView.findViewById(R.id.status_text);
		}
		return status_text;
	}

	public TextView getSize_text() {
		if (size_text == null) {
			size_text = (TextView) baseView.findViewById(R.id.size_text);
		}
		return size_text;
	}

	public ProgressBar getDownload_progress() {
		if (download_progress == null) {
			download_progress = (ProgressBar) baseView
					.findViewById(R.id.download_progress);
		}
		return download_progress;
	}

	public ImageView getDownload_icon() {
		if (download_icon == null) {
			download_icon = (ImageView) baseView
					.findViewById(R.id.download_icon);
		}
		return download_icon;
	}

}

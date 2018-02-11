package com.aurora.widget;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

import com.aurora.filemanager.R;

/**
 * files item显示
 * 
 * @author jiangxh
 * @CreateTime 2014年5月8日 上午11:15:41
 * @Description com.aurora.widget AuroraFilesItemView.java
 */
public class AuroraFilesItemView {

	private ImageView file_image, file_image_frame;
	private TextView file_name, modified_time, file_size,file_count;
	private RelativeLayout files_info_layout;
	private View rootView;
	private AuroraCheckBox checkbox;
	private RelativeLayout mainUi;

	public AuroraFilesItemView(View rootView) {
		super();
		this.rootView = rootView;
	}

	public RelativeLayout getLayoutMainUi() {
		if (mainUi == null) {
			mainUi = (RelativeLayout) rootView
					.findViewById(com.aurora.R.id.aurora_listview_front);
		}
		return mainUi;
	}

	/**
	 * @return the file_image_frame
	 */
	public ImageView getFile_image_frame() {
//		if (file_image_frame == null) {
//			file_image_frame = (ImageView) rootView
//					.findViewById(R.id.file_image_frame);
//		}
		return file_image_frame;
	}

	/**
	 * @return the file_image
	 */
	public ImageView getFile_image() {
		if (file_image == null) {
			file_image = (ImageView) rootView.findViewById(R.id.file_image);
		}
		return file_image;
	}

	/**
	 * @return the file_name
	 */
	public TextView getFile_name() {
		if (file_name == null) {
			file_name = (TextView) rootView.findViewById(R.id.file_name);
		}
		return file_name;
	}

	/**
	 * @return the modified_time
	 */
	public TextView getModified_time() {
		if (modified_time == null) {
			modified_time = (TextView) rootView
					.findViewById(R.id.modified_time);
		}
		return modified_time;
	}

	/**
	 * @return the file_size
	 */
	public TextView getFile_size() {
		if (file_size == null) {
			file_size = (TextView) rootView.findViewById(R.id.file_size);
		}
		return file_size;
	}

	/**
	 * @return the files_info_layout
	 */
	public RelativeLayout getFiles_info_layout() {
//		if (files_info_layout == null) {
//			files_info_layout = (RelativeLayout) rootView
//					.findViewById(R.id.files_info_layout);
//		}
		return files_info_layout;
	}

	public AuroraCheckBox getCheckBox() {
		if (checkbox == null) {
			checkbox = (AuroraCheckBox) rootView
					.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		}
		return checkbox;
	}

	public TextView getFileCount(){
		if(file_count==null){
			file_count = (TextView)rootView.findViewById(R.id.file_count);
		}
		return file_count;
	}
}
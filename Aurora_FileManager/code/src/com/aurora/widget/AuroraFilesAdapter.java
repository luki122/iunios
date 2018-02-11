package com.aurora.widget;

import java.util.List;

import com.aurora.filemanager.R;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.fragment.FileCategoryFragment;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;


/**
 * 文件分类适配器
 * 
 * @author jiangxh
 * @CreateTime 2014年5月8日 上午11:28:41
 * @Description com.aurora.widget AuroraFilesAdapter.java
 */
public class AuroraFilesAdapter extends FileBaseAdapter {

	private static final String TAG = "AuroraFilesAdapter";

	public AuroraFilesAdapter(List<FileInfo> fileInfos, Context context,
			FileCategoryFragment fragment) {
		super(fileInfos, (FileExplorerTabActivity)context, fragment);
	}

	
	@Override
	public void setItemViewData(AuroraFilesItemView itemView, FileInfo fileInfo) {
		super.setItemViewData(itemView, fileInfo);
		TextView fileName = itemView.getFile_name();
		TextView fileSize = itemView.getFile_size();
		TextView modifiedTime = itemView.getModified_time();
		final AuroraCheckBox checkbox = itemView.getCheckBox();
		if (fileInfo != null) {
			fileName.setText(Util.getEllipsize(fileInfo.fileName,
					itemView.getFile_name(), getMaxLength()));
			fileSize.setText(Util.convertStorage(fileInfo.fileSize));
			modifiedTime.setText(Util.formatInfoDateString(getActivity(),
					fileInfo.ModifiedDate));
			if (fileIconHelper.ismPaused() && !isLock()) {
				fileIconHelper.resume();
			}
			ImageView icon = itemView.getFile_image();
			icon.setTag(fileInfo.filePath);
			fileIconHelper.setIcon(fileInfo, icon);
			if (getSelectFiles() != null && getSelectFiles().contains(fileInfo)) {
				checkbox.setChecked(true);
			} else {
				checkbox.setChecked(false);

			}

		}
	}
	@Override
	public void setItemTextViewStyle(AuroraFilesItemView itemView) {
		super.setItemTextViewStyle(itemView);
		setTextViewColor(itemView.getFile_name(), itemView.getFile_size(), itemView.getModified_time(), true);
	}

	/**
	 * 改变选择颜色
	 * 
	 * @param fileName
	 * @param fileSize
	 * @param modifiedTime
	 * @param normal
	 */
	private void setTextViewColor(TextView fileName, TextView fileSize,
			TextView modifiedTime, boolean normal) {
		if (normal) {
			fileName.setTextAppearance(getActivity(), R.style.aurora_file_name_normal);
			fileSize.setTextAppearance(getActivity(),
					R.style.aurora_file_other_normal);
			modifiedTime.setTextAppearance(getActivity(),
					R.style.aurora_file_other_normal);
		} else {
			fileName.setTextAppearance(getActivity(), R.style.aurora_file_name_select);
			fileSize.setTextAppearance(getActivity(),
					R.style.aurora_file_other_select);
			modifiedTime.setTextAppearance(getActivity(),
					R.style.aurora_file_other_select);
		}
	}

}
package com.aurora.widget;

import java.util.List;

import com.aurora.filemanager.R;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.widget.AuroraCheckBox;

import com.aurora.filemanager.FileExplorerTabActivity;
import com.aurora.filemanager.fragment.FileViewFragment;
import com.aurora.tools.FileInfo;
import com.aurora.tools.FileInfoTask;
import com.aurora.tools.LogUtil;
import com.aurora.tools.LruMemoryCacheByInteger;
import com.aurora.tools.Util;


public class AuroraFileBrowserAdater extends FileBaseAdapter {
	private static final String TAG = "AuroraFileBrowserAdater";
	private LruMemoryCacheByInteger cacheByInteger;
	private boolean isRoot;// 判断是否为SD卡列表
	public boolean isRoot() {
		return isRoot;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	public AuroraFileBrowserAdater(List<FileInfo> fileInfos, Context context,FileViewFragment fragment) {
		super(fileInfos, (FileExplorerTabActivity) context, fragment);
		cacheByInteger = LruMemoryCacheByInteger.getInstance();
	}
	
	private FileInfoTask fileInfoTask;
	@Override
	public void setItemViewData(AuroraFilesItemView itemView, final FileInfo fileInfo) {
		final TextView fileName = itemView.getFile_name();
		final TextView fileSize = itemView.getFile_size();
		final TextView modifiedTime = itemView.getModified_time();
		final TextView fileCount = itemView.getFileCount();
		final AuroraCheckBox checkbox = itemView.getCheckBox();

		if (fileInfo != null) {
			if (isRoot()) {
				fileName.setText(getActivity()
						.getStorageNodeName(fileInfo.filePath));
				modifiedTime.setVisibility(View.GONE);
				fileCount.setVisibility(View.GONE);
				fileSize.setVisibility(View.GONE);

				if (fileInfo.filePath
						.equals(FileExplorerTabActivity.mSDCardPath)) {
					itemView.getFile_image().setImageResource(
							R.drawable.internal_storage_light);
				} else {
					itemView.getFile_image().setImageResource(
							R.drawable.sd_card_light);
				}
			} else {
				if (fileInfo.IsDir) {
					fileCount.setVisibility(View.VISIBLE);
					if (fileInfo.Count == -1) {
						if(fileInfo.filePath!=null){
							Integer v = cacheByInteger
									.getBitmapFromMemCache(fileInfo.filePath+fileInfo.ModifiedDate);
							fileCount.setText("("+(v == null?0:v)+")");
							LogUtil.d(TAG, " ----"+fileInfo.filePath+fileInfo.ModifiedDate+" fileCount:"+v);
							if(v==null){
								handler.postDelayed(new Runnable() {
									
									@Override
									public void run() {
										fileInfoTask = new FileInfoTask(fileCount);
										fileInfoTask.executeOnExecutor(getActivity().getFULL_TASK_EXECUTOR(), fileInfo);
										
									}
								}, 500);
							}
						}
					} else {
						fileCount.setText("(" + fileInfo.Count + ")");
						LogUtil.d(TAG, " ----"+fileInfo.filePath+" fileCount:"+fileInfo.Count);
					}
					fileSize.setText("");
				} else {
					fileCount.setVisibility(View.GONE);
					fileCount.setText("");
					fileSize.setText(Util.convertStorage(fileInfo.fileSize));
					if (fileIconHelper.ismPaused()&&!isLock()) {
						fileIconHelper.resume();
					}
				}
				ImageView icon = itemView.getFile_image();
				icon.setTag(fileInfo.filePath);
				fileIconHelper.setIcon(fileInfo, icon);

				fileName.setText(Util.getEllipsize(fileInfo.fileName,
						itemView.getFile_name(), getMaxLength()));
				modifiedTime.setText(Util.formatInfoDateString(getActivity(),
						fileInfo.ModifiedDate));
				if (getSelectFiles() != null && getSelectFiles().contains(fileInfo)) {
					checkbox.setChecked(true);
				} else {
					checkbox.setChecked(false);

				}
			}

		}
	}

	
	@Override
	public void setItemTextViewStyle(AuroraFilesItemView itemView) {
		setTextViewColor(itemView.getFile_name(), itemView.getFile_size(), itemView.getFileCount(), itemView.getModified_time(), true);
	}
	
	/**
	 * 改变选择颜色
	 * 
	 * @param fileName
	 * @param fileSize
	 * @param fileCount
	 * @param modifiedTime
	 * @param normal
	 */
	private void setTextViewColor(TextView fileName, TextView fileSize,
			TextView fileCount, TextView modifiedTime, boolean normal) {
		setTextViewColor(fileName, fileSize, modifiedTime, normal);
		if (normal) {
			fileCount.setTextAppearance(getActivity(),
					R.style.aurora_file_name_normal);
		} else {
			fileCount.setTextAppearance(getActivity(),
					R.style.aurora_file_name_select);
		}
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
			if (!isRoot()) {
				fileName.setTextAppearance(getActivity(),
						R.style.aurora_file_name_select);
			} else {
				fileName.setTextAppearance(getActivity(),
						R.style.aurora_file_name_select_rlayout);
			}
			fileSize.setTextAppearance(getActivity(),
					R.style.aurora_file_other_select);
			modifiedTime.setTextAppearance(getActivity(),
					R.style.aurora_file_other_select);
		}
	}

}

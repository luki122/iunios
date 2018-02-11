package com.aurora.widget;

import com.aurora.dbutil.FileCategoryHelper;
import com.aurora.filemanager.FileExplorerTabActivity;

import android.R.integer;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.filemanager.R;
import com.aurora.tools.LogUtil;
import com.aurora.tools.FileIconHelper;
import com.aurora.tools.FileInfo;
import com.aurora.tools.Util;

/**
 * 搜索界面适配器
 * 
 * @author jiangxh
 * @CreateTime 2014年5月6日 下午2:54:43
 * @Description com.aurora.widget SearchAdapter.java
 */
public class SearchAdapter extends CursorAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private FileExplorerTabActivity activity;
	private int titleLength=750;

	public SearchAdapter(Cursor c, Context mContext) {
		super(mContext, c);
		this.mContext = mContext;
		mInflater = LayoutInflater.from(mContext);
		mFileIconHelper = FileIconHelper.getInstance(mContext);
		activity = (FileExplorerTabActivity) mContext;
		titleLength = (int) mContext.getResources().getDimension(
				R.dimen.search_title_length);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return mInflater.inflate(R.layout.search_item, parent, false);
	}

	private boolean isLock;
	public void setLoadIcon(boolean isLock){
		this.isLock = isLock;
		if(isLock){
			mFileIconHelper.pause();
		}else {
			mFileIconHelper.resume();
		}
		
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		LogUtil.e("FileIconLoader", "bindView");
		String path = cursor.getString(FileCategoryHelper.COLUMN_PATH);
		TextView search_file_path = (TextView) view
				.findViewById(R.id.search_file_path);
		search_file_path.setText(activity.getStorageName(path));
		setFileIcon(view, path);
		TextView titleText = (TextView) view
				.findViewById(R.id.search_file_name);
		String title = cursor.getString(FileCategoryHelper.COLUMN_TITLE);
		titleText.setText(Util.getEllipsize(
				title, titleText, titleLength));
	}

	private FileIconHelper mFileIconHelper;

	private void setFileIcon(View view, String path) {
		final ImageView fileIcon = (ImageView) view
				.findViewById(R.id.search_file_icon);
		FileInfo fileInfo = Util.GetFileInfo(path);
		if (fileInfo != null) {
			if (mFileIconHelper.ismPaused()&&!isLock) {
				mFileIconHelper.resume();
			}
			fileIcon.setTag(fileInfo.filePath);
			mFileIconHelper.setIcon(fileInfo, fileIcon);
		}
	}

}

package com.android.gallery3d.local.widget;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import aurora.app.AuroraActivity;

import com.android.gallery3d.R;
import com.android.gallery3d.data.LocalImage;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.AuroraAlbumsFilenameFilter;
import com.android.gallery3d.local.tools.AuroraFilenameFilter;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

public class PhotoPopupWindowUtil {

	private final static String TAG ="PopupWindowUtil";
	private AuroraActivity activity;
	private boolean isShowPopupWindow = false;
	private PhotoSelectPopupWindow selectPopupWindow;

	public boolean isShowPopupWindow() {
		return isShowPopupWindow;
	}

	public AuroraActivity getActivity() {
		return activity;
	}

	public void setActivity(AuroraActivity activity) {
		this.activity = activity;
	}

	private static PhotoPopupWindowUtil popupWindowUtil;

	public static PhotoPopupWindowUtil getInstance(AuroraActivity activity) {
		return new PhotoPopupWindowUtil(activity);
	}

	public PhotoPopupWindowUtil(AuroraActivity activity) {
		super();
		this.activity = activity;
	}

	private boolean visibility;

	/**
	 *  取消CheckBox Cancel 
	 * @param visibility
	 */
	public void setCheckBoxAndPopCancel(boolean visibility) {
		this.visibility = visibility;
	}
	
	/**
	 * after showSelectPopupWindow
	 * @param selectFileInfos
	 */
	public void setSelectFileInfos(List<MediaFileInfo> selectFileInfos){
		if(selectPopupWindow!=null){
			selectPopupWindow.setSelectFileInfos(selectFileInfos);
		}
	}

	public void setSelectPath(List<MediaItem> path){
		if(path==null||path.isEmpty()){
			LogUtil.d(TAG, "----setSelectPath path isnull or isEmpty");
			return;
		}
		List<MediaFileInfo> selectFileInfos = new ArrayList<MediaFileInfo>();
		for (MediaItem temp : path) {
			MediaFileInfo fileInfo = new MediaFileInfo();
			fileInfo.filePath = temp.getFilePath();
			if(temp instanceof LocalImage){
				fileInfo.isImage = true;
			}else{
				fileInfo.isImage = false;
			}
			selectFileInfos.add(fileInfo);
		}
		setSelectFileInfos(selectFileInfos);
	}
	
	
	/**
	 * 适合移动到本地相册
	 * @param title
	 * @param rootView
	 */
	@SuppressLint("RtlHardcoded")
	public void showSelectPopupWindow(String title, View rootView) {
		if (rootView == null) {
			return;
		}
		AlbumsAsyncTask asyncTask = new AlbumsAsyncTask();
		asyncTask.execute();
		selectPopupWindow = null;
		selectPopupWindow = new PhotoSelectPopupWindow(activity);
		selectPopupWindow.show();
		selectPopupWindow.setCheckBoxAndPopCancel();
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		selectPopupWindow.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				isShowPopupWindow = false;
			}
		});
	}
	

	public void dismissSelectPopupWindow() {
		if (selectPopupWindow != null) {
			selectPopupWindow.dismiss();
		}
	}

	private class AlbumsAsyncTask extends AsyncTask<Void, Void, List<MediaFileInfo>> {
		List<MediaFileInfo> mediaFileInfos = new ArrayList<MediaFileInfo>();

		@Override
		protected List<MediaFileInfo> doInBackground(Void... params) {
			File file = new File(GalleryLocalActivity.dcimPath);
			File[] fs = file.listFiles(AuroraFilenameFilter.getInstance());
			if (fs != null && fs.length > 0) {
				List<File> files = Arrays.asList(fs);
				Collections.sort(files, new FolderComparator());
				for (File f : files) {
					MediaFileInfo info = new MediaFileInfo();
					File[] fileFs = f.listFiles(AuroraAlbumsFilenameFilter.getInstance());
					info.filePath = f.getAbsolutePath();
					info.IsDir = f.isDirectory();
					info.createDate = f.lastModified();
					if (fileFs != null&&fileFs.length>0) {
						List<File> fileList = Arrays.asList(fileFs);
						info.Count = fileList.size();
						Collections.sort(fileList, new FolderComparator());
						info.filePath = fileList.get(0).getAbsolutePath();
						info.IsDir = false;
						info.createDate = fileList.get(0).lastModified();
					}
					info.fileName = f.getName();
					if(info.Count!=0){
						mediaFileInfos.add(info);
					}
				}
			}
			return mediaFileInfos;
		}

		@Override
		protected void onPostExecute(List<MediaFileInfo> result) {
			super.onPostExecute(result);
			result.add(0, new MediaFileInfo());
			selectPopupWindow.showList(result);
		}

		
	}

	private class FolderComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			return longToCompareInt(rhs.lastModified() - lhs.lastModified());
		}

	}

	private int longToCompareInt(long result) {
		return result > 0 ? 1 : (result < 0 ? -1 : 0);
	}

}

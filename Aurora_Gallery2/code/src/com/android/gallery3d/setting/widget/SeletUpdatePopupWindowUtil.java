package com.android.gallery3d.setting.widget;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.CycleInterpolator;
import android.widget.PopupWindow.OnDismissListener;
import aurora.app.AuroraActivity;

import com.android.gallery3d.R;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.AuroraAlbumsFilenameFilter;
import com.android.gallery3d.local.tools.AuroraFilenameFilter;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.setting.SettingsActivity;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;

public class SeletUpdatePopupWindowUtil {

	private final static String TAG ="SeletUpdatePopupWindowUtil";
	private AuroraActivity activity;
	private boolean isShowPopupWindow = false;
	private SeletUpdatePopupWindow selectPopupWindow;

	public boolean isShowPopupWindow() {
		return isShowPopupWindow;
	}

	public AuroraActivity getActivity() {
		return activity;
	}

	public void setActivity(AuroraActivity activity) {
		this.activity = activity;
	}

	private static SeletUpdatePopupWindowUtil popupWindowUtil;

	public static SeletUpdatePopupWindowUtil getInstance(AuroraActivity activity) {
		return new SeletUpdatePopupWindowUtil(activity);
	}

	public SeletUpdatePopupWindowUtil(AuroraActivity activity) {
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
//	public void setSelectFileInfos(List<MediaFileInfo> selectFileInfos){
//		if(selectPopupWindow!=null){
//			selectPopupWindow.setSelectFileInfos(selectFileInfos);
//		}
//	}

//	public void setSelectPath(List<String> path){
//		if(path==null||path.isEmpty()){
//			LogUtil.d(TAG, "----setSelectPath path isnull or isEmpty");
//			return;
//		}
//		List<MediaFileInfo> selectFileInfos = new ArrayList<MediaFileInfo>();
//		for (String temp : path) {
//			MediaFileInfo fileInfo = new MediaFileInfo();
//			fileInfo.filePath = temp;
//			selectFileInfos.add(fileInfo);
//		}
//		setSelectFileInfos(selectFileInfos);
//	}
	
	ValueAnimator animation;
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
		selectPopupWindow = new SeletUpdatePopupWindow(R.style.ActionBottomBarMorePopupAnimation, activity);
		selectPopupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
		selectPopupWindow.setCheckBoxAndPopCancel();
		isShowPopupWindow = true;
		selectPopupWindow.setTitleText(title);
		selectPopupWindow.setOutsideTouchable(true);
		
        
		selectPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				isShowPopupWindow = false;
				animation.cancel();	//wenyongzhe 2016.2.22 
				 WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
	             lp.alpha = 1f;
	             activity.getWindow().setAttributes(lp);
			}
		});
		//wenyongzhe 2016.2.22 start
		animation = (ValueAnimator) AnimatorInflater
                .loadAnimator(activity, R.anim.popuwindow_in);
		animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
            	WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		        lp.alpha = (Float)animation.getAnimatedValue();
		        activity.getWindow().setAttributes(lp);
            }
        });
        animation.start();
    	//wenyongzhe 2016.2.22 end
	}
	

	public void dismissSelectPopupWindow() {
		if (selectPopupWindow != null) {
			selectPopupWindow.dismiss();
		}
	}

	protected List<String> allNoShowPaths = new ArrayList<String>();
	private class AlbumsAsyncTask extends AsyncTask<Void, Void, List<MediaFileInfo>> {
		List<MediaFileInfo> mediaFileInfos = new ArrayList<MediaFileInfo>();

		@Override
		protected List<MediaFileInfo> doInBackground(Void... params) {
			List<String> noShowPaths = GalleryLocalUtils.doParseXml(activity);
			allNoShowPaths.clear();
			allNoShowPaths.addAll(noShowPaths);
			
			File file = new File(SettingsActivity.dcimPath);
			File fileScreenShots  = new File(SettingsActivity.screenShotsPath);//wenyongzhe 2016.2.27 
			//File[] fs = file.listFiles(AuroraFilenameFilter.getInstance());
			File[] fs = file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if(pathname.isHidden()||pathname.isFile()){
						return false;
					}
					return true;
				}
			});
			
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
						info.firstPhotoPath = f.getAbsolutePath();
						info.IsDir = false;
						info.createDate = fileList.get(0).lastModified();
					}else{
						info.firstPhotoPath = f.getAbsolutePath();
					}
					info.fileName = f.getName();
					if( !allNoShowPaths.contains(info.firstPhotoPath)){
						if( !info.firstPhotoPath.contains("100ANDRO") && !info.firstPhotoPath.contains("/cloud") && info.Count!=0){	//wenyongzhe 2016.2.27 
							mediaFileInfos.add(info);
						}
					}
				}
			}
			//wenyongzhe 2016.2.27 ScreenShots start
			if(fileScreenShots.exists() && !fileScreenShots.isHidden() && !fileScreenShots.isFile()){
				MediaFileInfo info = new MediaFileInfo();
				File[] fileFs = fileScreenShots.listFiles(AuroraAlbumsFilenameFilter.getInstance());
				info.filePath = fileScreenShots.getAbsolutePath();
				info.IsDir = fileScreenShots.isDirectory();
				info.createDate = fileScreenShots.lastModified();
				if (fileFs != null&&fileFs.length>0) {
					List<File> fileList = Arrays.asList(fileFs);
					info.Count = fileList.size();
					Collections.sort(fileList, new FolderComparator());
					info.filePath = fileList.get(0).getAbsolutePath();
					info.firstPhotoPath = fileScreenShots.getAbsolutePath();
					info.IsDir = false;
					info.createDate = fileList.get(0).lastModified();
				}else{
					info.firstPhotoPath = fileScreenShots.getAbsolutePath();
				}
				info.fileName = fileScreenShots.getName();
				if( !allNoShowPaths.contains(info.firstPhotoPath)){
					mediaFileInfos.add(info);
				}
			}
			//wenyongzhe 2016.2.27 end
			return mediaFileInfos;
		}

		@Override
		protected void onPostExecute(List<MediaFileInfo> result) {
			super.onPostExecute(result);
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

package com.android.gallery3d.xcloudalbum.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import aurora.app.AuroraActivity;

import com.android.gallery3d.app.AbstractGalleryActivity;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.inter.IOperationComplete;
import com.android.gallery3d.xcloudalbum.uploaddownload.FakeTaskManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.widget.InformationDialog;
import com.android.gallery3d.xcloudalbum.widget.TextInputDialog;
import com.android.gallery3d.xcloudalbum.widget.TextInputDialog.OnFinishListener;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import com.android.gallery3d.R;

public class OperationUtil {
	private static final String TAG = "OperationUtil";

	private AuroraActivity activity;
	private TextInputDialog inputDialog;
	private InformationDialog informationDialog;
	private InputMethodManager inputMethodManager;
	private BaiduAlbumUtils baiduAlbumUtils;
	private CommonFileInfo moveOrCopyTarget;
	

	public CommonFileInfo getMoveOrCopyTarget() {
		return moveOrCopyTarget;
	}

	public void setMoveOrCopyTarget(CommonFileInfo moveOrCopyTarget) {
		this.moveOrCopyTarget = moveOrCopyTarget;
	}

	private BasicFragment fragment;

	public BasicFragment getBasicFragment() {
		if (activity instanceof CloudActivity) {
			fragment = (BasicFragment) ((CloudActivity) activity)
					.getCurrentFragment();
		}
		return fragment;
	}

	// SQF ADDED ON 2015.4.27 begin
	public void setOperationComplete(IOperationComplete listener) {
		baiduAlbumUtils.setOperationComplete(listener);
	}

	// SQF ADDED ON 2015.4.27 end

	public void setOperationComplete(BasicFragment fragment) {
		baiduAlbumUtils.setOperationComplete(fragment);
	}

	public OperationUtil(Context activity) {
		super();
		this.activity = (AuroraActivity) activity;
		inputMethodManager = (InputMethodManager) activity
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		baiduAlbumUtils = BaiduAlbumUtils.getInstance(activity
				.getApplicationContext());

	}

	private List<CommonFileInfo> getSelectImages() {
		return getBasicFragment().getSelectImages();
	}

	private List<CommonFileInfo> getImageInfos() {
		if (activity instanceof CloudActivity){
			return ((CloudActivity)activity).getAlbumInfos();
		}
		return null;
	}

	/**
	 * 重命名相册
	 */
	public void renameAlbumName() {
		List<CommonFileInfo> selects = getSelectImages();
		if (selects.size() != 1) {
			return;
		}
		inputDialog = new TextInputDialog(activity,
				R.string.aurora_album_rename, selects.get(0), getImageInfos(),
				inputMethodManager, onFinishListener);
		inputDialog.show();
	}

	/**
	 * 移动或复制
	 * 
	 * @param fileInfos
	 * @param tInfo
	 * @param isMove
	 */
	public void moveOrCopyPhoto(CommonFileInfo tInfo, boolean isMove) {
		List<CommonFileInfo> selects = getSelectImages();
		if (selects.size() == 0) {
			return;
		}
		baiduAlbumUtils.moveOrCopyPhotoFromBaidu(selects, tInfo, isMove);
	}

	/**
	 * 移动或复制
	 * 
	 * @param fileInfos
	 * @param path
	 * @param isMove
	 */
	public void moveOrCopyPhoto() {
		List<CommonFileInfo> selects = getSelectImages();
		if (selects.size() == 0) {
			return;
		}
		setMoveCreateAlbum(false);// rest move status
		baiduAlbumUtils.moveOrCopyPhotoFromBaidu(selects, getTargetMovePath(),
				isMoveCheckBox());
	}

	public List<List<CommonFileInfo>> getDownloadTask() {
		return ((GalleryAppImpl) activity.getApplication()).getDownloadTask();
	}
	

	/**
	 * 下载
	 *//*
	public void downloadPhoto() {
		downloadPhoto(0);
	}

	public void downloadPhoto(int position) {
		synchronized (getDownloadTask()) {
			if(getDownloadTask().size()==0){
				if (temps.size()>0) {
					temps.remove(0);
				}
				getDownloadTask().add(temps);
			}
			if (getDownloadTask().size() > position) {
				baiduAlbumUtils.downloadFromBaidu(getDownloadTask().get(
						position));
			}
			
		}
	}*/
//	private List<CommonFileInfo> temps = new ArrayList<CommonFileInfo>();

	/**
	 * 添加到下载队列
	 */
	public void addDownloadTask() {
		List<CommonFileInfo> selects = new ArrayList<CommonFileInfo>(
				getSelectImages());
//		temps.addAll(selects);
		if (selects.size() == 0) {
			return;
		}
		
		/*//SQF ADDED BEGIN
		for(CommonFileInfo info : selects) {
			
			
			 * onGetTaskStatus bean :Bundle[EMPTY_PARCEL]message: 
			 * source:/storage/emulated/0/DCIM/CloudIMG_20150508_170815_1.jpg 
			 * target:/apps/iuni云/hh/IMG_20150508_170815_1.jpg 
			 * fileName:CloudIMG_20150508_170815_1.jpg fileTaskId:78 
			 * totalSize:2428271 
			 * currentSize:704512 
			 * type:2 
			 * errorCode:-1 
			 * statusType:3 
			 * statusTaskCode:101
			 * 
			 
			FileTaskStatusBean bean = new FileTaskStatusBean();
			String fileName = getFileNameFromPath(info.path);
			bean.setSource(AlbumConfig.DOWNLOADPATH + fileName);
			bean.setTarget(info.path);
			bean.setType(FileTaskStatusBean.TYPE_TASK_DOWNLOAD);
			bean.setFileName(fileName);
			bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
			FakeTaskManager.getInstance().addDownloadFakeBeans(bean);
		}*/
		//SQF ADDED END

		baiduAlbumUtils.downloadFromBaidu(getSelectImages());
//		synchronized (getDownloadTask()) {
//				getDownloadTask().add(selects);
//				if (getDownloadTask().size() == 1) {
//					downloadPhoto();
//				}
//		}
	}

	private boolean isMoveCreateAlbum = false;

	public boolean isMoveCreateAlbum() {
		return isMoveCreateAlbum;
	}

	public void setMoveCreateAlbum(boolean isMoveCreateAlbum) {
		this.isMoveCreateAlbum = isMoveCreateAlbum;
	}

	private boolean isMoveCheckBox = false;
	private String targetMovePath;

	public boolean isMoveCheckBox() {
		return isMoveCheckBox;
	}

	public void setMoveCheckBox(boolean isMoveCheckBox) {
		this.isMoveCheckBox = isMoveCheckBox;
	}

	public String getTargetMovePath() {
		return targetMovePath;
	}

	public void setTargetMovePath(String targetMovePath) {
		this.targetMovePath = targetMovePath;
	}

	/**
	 * 创建相册
	 */
	public void createAlbum(int stringId,AuroraActivity activity) {
		inputDialog = new TextInputDialog(activity, stringId, getImageInfos(),
				inputMethodManager, onFinishListener);
		inputDialog.show();
	}
	
	//SQF ADDED ON 2015.6.16 BEGIN
	public void createAlbum(int stringId,AuroraActivity activity, List<CommonFileInfo> fileInfos) {
		inputDialog = new TextInputDialog(activity, stringId, fileInfos,
				inputMethodManager, onFinishListener);
		inputDialog.show();
	}
	//SQF ADDED ON 2015.6.16 END
	
	/**
	 * 删除相册或者图片
	 */
	public void deleteAlbumOrPhoto() {
		List<CommonFileInfo> selects = getSelectImages();
		int size = selects.size();
//		LogUtil.d(TAG, "deleteAlbumOrPhoto:::" + size+" selects::"+selects);
		if (size == 0) {
			return;
		}
		boolean isDir = false;
		String msg = activity.getString(R.string.aurora_del_album);
		if (getBasicFragment() instanceof CloudMainFragment) {
			List<CommonFileInfo> photos = new ArrayList<CommonFileInfo>();
			ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap = ((CloudMainFragment) getBasicFragment())
					.getConcurrentHashMap();
			for (CommonFileInfo commonFileInfo : selects) {
				List<CommonFileInfo> infos = concurrentHashMap
						.get(commonFileInfo);
				if (infos != null) {
					photos.addAll(infos);
				}
			}
			size = photos.size();
			photos.clear();
			isDir = true;
		} else if (getBasicFragment() instanceof CloudItemFragment) {
			msg = activity.getString(R.string.aurora_del_picture);
			isDir = false;
		}
		msg = String.format(msg, size);
		informationDialog = new InformationDialog(activity,
				R.string.aurora_error_notice_title, msg, isDir,
				onFinishListener);
		informationDialog.show();
	}

	//wenyongzhe 2015.11.19 bug17169 multi language start
	public void createAlbumFromBaidu(String text){
		if(baiduAlbumUtils != null ){
			baiduAlbumUtils.createAlbumFromBaidu(text);
		}
	}
	//wenyongzhe 2015.11.19 bug17169 multi language start
	
	private OnFinishListener onFinishListener = new OnFinishListener() {

		@Override
		public boolean onFinish(boolean isRename, String text, boolean isCancel) {
			if (!TextUtils.isEmpty(text)) {
				if (isRename) {
					baiduAlbumUtils.renameFromBaidu(getSelectImages().get(0),
							text);
				} else {
					baiduAlbumUtils.createAlbumFromBaidu(text);
					if (isMoveCreateAlbum()) {
						setTargetMovePath(AlbumConfig.REMOTEPATH + text);
					}
				}
			} else if (!isCancel) {
				getBasicFragment().showLoadingImage(true);
				baiduAlbumUtils.deletePhotoOrAlbum(getSelectImages(), isRename);
			} else if (isCancel && !isMoveCreateAlbum()) {
				getBasicFragment().cancelOperation();
			}
			return true;
		}
	};

	/**
	 * upload to xCloud album
	 */
	public void uploadToAlbum(ArrayList<String> localFilePaths,
			CommonFileInfo remoteAlbum) {
		try {
			ArrayList<FileUpDownloadInfo> infos = new ArrayList<FileUpDownloadInfo>();
			for (String filePath : localFilePaths) {
				/*
				Log.i("SQF_LOG", "uploadToAlbum ---> UPLOAD ----> :" + filePath
						+ " " + remoteAlbum.path.toString() + File.separator
						+ getFileNameFromPath(filePath));
				*/
				String fileName = getFileNameFromPath(filePath);
				String target = remoteAlbum.path.toString() + File.separator + fileName;
				FileUpDownloadInfo info = new FileUpDownloadInfo(filePath,
						target,
						FileUpDownloadInfo.TYPE_UPLOAD,
						FileUpDownloadInfo.OVER_WRITE);
				infos.add(info);
				
				FileTaskStatusBean bean = new FileTaskStatusBean();
				bean.setSource(filePath);
				bean.setType(FileTaskStatusBean.TYPE_TASK_UPLOAD);
				bean.setTarget(target);
				bean.setFileName(fileName);
				bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
				FakeTaskManager.getInstance().addUploadFakeBeans(bean);
				//XCloudTaskListenerManager.getInstance(activity).updateList(bean, false);
				//XCloudTaskListenerManager.getInstance(activity).remove
			}
			/*
			 * if(activity instanceof AbstractGalleryActivity) { GalleryAppImpl
			 * app =
			 * (GalleryAppImpl)((AbstractGalleryActivity)activity).getApplication
			 * (); UploadTaskListManager manager =
			 * app.getUploadTaskListManager(); manager.addTaskList(infos); }
			 */
			GalleryAppImpl app = (GalleryAppImpl)activity.getApplication();
			UploadTaskListManager manager = app.getUploadTaskListManager();
			manager.addTaskList(infos);
			baiduAlbumUtils.uploadToAlbum(infos);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFileNameFromPath(String filePath) {
		if (TextUtils.isEmpty(filePath)) {
			Log.e("SQF_LOG", "error occurs in getFileNameFromPath @1");
			return "";
		}
		int i = filePath.lastIndexOf("/");
		if (i != -1) {
			String fileName = filePath.substring(i + 1);
			//Log.i("SQF_LOG", "OperationUtil::getFileNameFromPath fileName::::::::::::::::::::" + fileName);
			return fileName;
		}
		Log.e("SQF_LOG", "error occurs in getFileNameFromPath @2");
		return "";
	}

}

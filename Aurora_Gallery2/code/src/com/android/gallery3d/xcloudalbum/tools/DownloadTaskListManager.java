package com.android.gallery3d.xcloudalbum.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;

public class DownloadTaskListManager {
	private static final String TAG = "DownloadTaskListManager";
	private BaiduAlbumUtils baiduAlbumUtils;

	public void setBaiduAlbumUtils(BaiduAlbumUtils baiduAlbumUtils) {
		this.baiduAlbumUtils = baiduAlbumUtils;
	}

	public class FileDownloadInfo {
		private FileUpDownloadInfo downloadInfo;
		private boolean isComplete;

		public FileUpDownloadInfo getDownloadInfo() {
			return downloadInfo;
		}

		public void setDownloadInfo(FileUpDownloadInfo downloadInfo) {
			this.downloadInfo = downloadInfo;
		}

		public boolean isComplete() {
			return isComplete;
		}

		public void setComplete(boolean isComplete) {
			this.isComplete = isComplete;
		}

		public FileDownloadInfo(FileUpDownloadInfo downloadInfo) {
			super();
			this.downloadInfo = downloadInfo;
			this.isComplete = false;
		}

		@Override
		public String toString() {
			return "FileDownloadInfo [downloadInfo=" + downloadInfo
					+ ", isComplete=" + isComplete + "]";
		}

	}

	public List<FileUpDownloadInfo> getFileDownlaodIndexOne() {
		ArrayList<FileDownloadInfo> downloads = downloadinfos.get(0);
		List<FileUpDownloadInfo> infos = new ArrayList<FileUpDownloadInfo>();
		for (FileDownloadInfo FileDownloadInfo : downloads) {
			infos.add(FileDownloadInfo.getDownloadInfo());
		}
		return infos;
	}

	private List<ArrayList<FileDownloadInfo>> downloadinfos = Collections
			.synchronizedList(new ArrayList<ArrayList<FileDownloadInfo>>());

	public List<ArrayList<FileDownloadInfo>> getDownloadinfos() {
		return downloadinfos;
	}

	private int mCurrentTaskIndex = -1;
	private int mCompleteIndex = 0;
	private int mDownloadSize = 0;
	private FileDownloadInfo mCurrentTaskInfo;

	public ArrayList<FileDownloadInfo> getCurrentTaskInfos() {
		return downloadinfos.get(0);
	}

	public int getDownloadTaskSize() {
		return downloadinfos.size();
	}

	public int getCurrentTaskIndex() {
		return mCurrentTaskIndex;
	}

	public int getCompleteIndex() {
		return mCompleteIndex;
	}

	public int getDownloadSize() {
		return mDownloadSize;
	}

	public FileDownloadInfo getCurrentTaskInfo() {
		return mCurrentTaskInfo;
	}

	public void addDownloadTask(List<FileUpDownloadInfo> downloadInfos) {
		if (downloadInfos == null)
			return;
		ArrayList<FileDownloadInfo> infos = new ArrayList<FileDownloadInfo>();
		for (FileUpDownloadInfo info : downloadInfos) {
			FileDownloadInfo downloadInfo = new FileDownloadInfo(info);
			infos.add(downloadInfo);
		}
		synchronized (downloadinfos) {
			downloadinfos.add(infos);
		}

	}

	public void updateDownloadStatus(FileTaskStatusBean bean) {
		if (bean == null)
			return;
		mCurrentTaskInfo = findFileDownloadInfoByBean(bean);
		if (mCurrentTaskInfo == null)
			return;
		switch (bean.getStatusTaskCode()) {
		case FileTaskStatusBean.STATE_TASK_PENDING:
			break;
		case FileTaskStatusBean.STATE_TASK_CREATE:
			break;
		case FileTaskStatusBean.STATE_TASK_CANCELLED:
			mCurrentTaskInfo.setComplete(true);
			break;
		case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
			break;
		case FileTaskStatusBean.STATE_TASK_DONE:
			mCurrentTaskInfo.setComplete(true);
			break;
		case FileTaskStatusBean.STATE_TASK_FAILED:
			break;
		case FileTaskStatusBean.STATE_TASK_PAUSE:
			break;
		case FileTaskStatusBean.STATE_TASK_RUNNING:
			break;
		}
		mCompleteIndex = getTaskComplete(mCurrentTaskIndex);
		if (mCompleteIndex == mDownloadSize) {
			synchronized (downloadinfos) {
				downloadinfos.remove(mCurrentTaskIndex);
			}
			if(baiduAlbumUtils!=null){
				baiduAlbumUtils.downloadFromBaidu();
			}
		}
	}

	public FileDownloadInfo findFileDownloadInfoByBean(FileTaskStatusBean bean) {
		for (int i = 0; i < downloadinfos.size(); i++) {
			ArrayList<FileDownloadInfo> downloadinfo = downloadinfos.get(i);
			if (downloadinfo != null) {
				for (FileDownloadInfo info : downloadinfo) {
					if (info != null) {
						FileUpDownloadInfo download = info.getDownloadInfo();
						if (download != null
								&& download.getSource()
										.equals(bean.getTarget())
								&& download.getTarget()
										.equals(bean.getSource())) {
							mDownloadSize = downloadinfo.size();
							mCurrentTaskIndex = i;
							return info;
						}
					}
				}
			}
		}
		return null;
	}

	public int getTaskComplete(int taskIndex) {
		ArrayList<FileDownloadInfo> downloadInfo = downloadinfos.get(taskIndex);
		int complete = 0;
		if (downloadInfo != null) {
			for (FileDownloadInfo info : downloadInfo) {
				if (info != null && info.isComplete()) {
					complete++;
				}
			}
		}
		return complete;
	}
}

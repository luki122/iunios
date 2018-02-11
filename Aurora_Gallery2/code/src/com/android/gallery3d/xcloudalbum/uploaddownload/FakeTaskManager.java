package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import android.util.Log;

import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

public class FakeTaskManager {
	
	private IndexedLinkedHashMap mFakeUploadBeans = new IndexedLinkedHashMap();
	private IndexedLinkedHashMap mFakeDownloadBeans = new IndexedLinkedHashMap();
	
	private static FakeTaskManager mInstance;
	
	public static FakeTaskManager getInstance() {
		if(mInstance == null) {
			mInstance = new FakeTaskManager();
		}
		return mInstance;
	}
	
	private FakeTaskManager() {
		
	}
	
	public void debugUploadInfo() {
		Log.i("SQF_LOG", "=========================FakeTaskManager BEGIN==========================");
		for(int i=0; i<mFakeUploadBeans.getSize(); i++) {
			FileTaskStatusBean bean = mFakeUploadBeans.getByPosition(i);
			Log.i("SQF_LOG", "BEAN: ------> " + bean.toString());
		}
		Log.i("SQF_LOG", "=========================FakeTaskManager END==========================");
	}
	
	public void addUploadFakeBeans(FileTaskStatusBean bean) {
		//mFakeUploadBeans.add(bean);
		String key = bean.getSource() + bean.getTarget();
		if(mFakeUploadBeans.containsKey(key)) {
			return;
		}
		mFakeUploadBeans.put(key, bean);
	}
	
	public void addDownloadFakeBeans(FileTaskStatusBean bean) {
		String key = bean.getSource() + bean.getTarget();
		if(mFakeDownloadBeans.containsKey(key)) {
			return;
		}
		mFakeDownloadBeans.put(key, bean);
	}
	
	public void removeAllByType(int type) {
		if(type == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			mFakeUploadBeans.clear();
		} else if(type == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			mFakeDownloadBeans.clear();
		}
	}

	public IndexedLinkedHashMap getFakeUploadBeans() {
		return mFakeUploadBeans;
	}
	
	public IndexedLinkedHashMap getFakeDownloadBeans() {
		return mFakeDownloadBeans;
	}

	public void updateFakeTaskBean(FileTaskStatusBean bean) {
		updateList(bean);
	}
	
	private void updateList(FileTaskStatusBean bean) {
		//Log.i("SQF_LOG", "FakeTaskManager::updateList ----> bean:" + bean.toString());
		String key = bean.getSource() + bean.getTarget();
		if (bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			FileTaskStatusBean b = getFakeUploadBeans().get(key);
			if (b != null) {
				updateBeanStatus(b, bean);
			} else {
				addUploadFakeBeans(bean);
			}
		} else if (bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			FileTaskStatusBean b = getFakeDownloadBeans().get(key);
			if (b != null) {
				updateBeanStatus(b, bean);
			} else {
				addDownloadFakeBeans(bean);
			}
		} else {
			Log.e("SQF_LOG",
					"UploadDownloadListActivity:: mUploadBeans updateList ERROR "
							+ bean.getFileTaskId());
		}
	}
	
    private void updateBeanStatus(FileTaskStatusBean from, FileTaskStatusBean to) {
    	if(! from.getSource().equals(to.getSource()) || 
    			! from.getTarget().equals(to.getTarget()) ||
    			from.getType() != to.getType()) return;
    	//if(to.getStatusType() != FileTaskStatusBean.STATUS_END) {
    		from.setCurrentSize(to.getCurrentSize());
    		from.setTotalSize(to.getTotalSize());
    	//}
    	from.setFileName(to.getFileName());
    	from.setFileTaskId(to.getFileTaskId());
    	from.setErrorCode(to.getErrorCode());
    	from.setMessage(to.getMessage());
    	from.setStatusTaskCode(to.getStatusTaskCode());
    	from.setStatusType(to.getStatusType());
    }

}

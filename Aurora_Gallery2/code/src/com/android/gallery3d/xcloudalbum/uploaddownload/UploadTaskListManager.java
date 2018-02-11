package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.ArrayList;

import android.os.Handler;
import android.os.Message;

import com.android.gallery3d.util.MyLog;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileUpDownloadInfo;
import android.util.Log;

public class UploadTaskListManager {
	
	public static int UPDATE_FILE_UPLOAD_TASK_ERROR = -1;//wenyongzhe 2015.11.4 upload toash bug
	public static int UPDATE_FILE_UPLOAD_TASK_OK = 1;//wenyongzhe 2015.11.4 upload toash bug
	
	public class FileUploadTaskInfo {
    	public FileUpDownloadInfo uploadInfo;
    	public boolean isComplete;
    	public FileUploadTaskInfo(FileUpDownloadInfo info) {
    		uploadInfo = info;
    		isComplete = false;
    	}
    }
    
    private ArrayList<ArrayList<FileUploadTaskInfo>> mUploadTaskList = new ArrayList<ArrayList<FileUploadTaskInfo>>();
    
    /*
    private int MSG_REMOVE_PARCEL = 1;
    private Handler mRemoveParcelHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == MSG_REMOVE_PARCEL) {
				int parcelIndex = msg.arg1;
				Log.i("SQF_LOG", "UploadTaskListManager::mRemoveParcelHandler ----> MSG_REMOVE_PARCEL ");
				removeTaskParcel(parcelIndex);
			}
		}
    	
    };
    */
    
    private int mCurrentTaskParcelIndex = -1;
    private int mHowManyPhotosUnderCurrentParcel = 0; //decided by mCurrentTaskParcelIndex;
    private int mCompleteNumUnderCurrentParcel = 0; //decided by mCurrentTaskParcelIndex;
    
    private FileUploadTaskInfo mCurrentBeanInfo;
    
    public int getCurrentTaskParcelIndex() {
    	return mCurrentTaskParcelIndex;
    }
    
    public int howManyPhotosUnderCurrentParcel() {
    	return mHowManyPhotosUnderCurrentParcel;
    }
    
    public int completeNumberUnderCurrentParcel() {
    	return mCompleteNumUnderCurrentParcel;
    }
    
    public int howManyPhotosUnderParcel(int parcelIndex) {
    	return mUploadTaskList.get(parcelIndex).size();
    }
    
    public FileUploadTaskInfo getCurrentBeanInfo() {
    	return mCurrentBeanInfo;
    }
    
    public void addTaskList(ArrayList<FileUpDownloadInfo> taskList) {
    	//Log.i("SQF_LOG", "UploadTaskListManager::addTaskList --------- taskList.size():" + taskList.size());
    	ArrayList<FileUploadTaskInfo> taskInfo = new ArrayList<FileUploadTaskInfo>();
    	for(int i=0; i<taskList.size(); i++) {
    		FileUploadTaskInfo info = new FileUploadTaskInfo(taskList.get(i));
    		taskInfo.add(info);
    	}
    	mUploadTaskList.add(taskInfo);
    }
    
    public int updateFileUploadTaskInfo(FileTaskStatusBean bean) {//wenyongzhe 2015.11.4 upload toash bug
    	//MyLog.i2("SQF_LOG", "UploadTaskListManager::updateFileUploadTaskInfo --------- 1111111111");
    	FileUploadTaskInfo info;
    	mCurrentBeanInfo = findTaskInfoByBean(bean);
    	
    	//mCurrentTaskParcelIndex = findTaskParcelIndex(bean);
    	//mHowManyPhotosUnderCurrentParcel = (mCurrentTaskParcelIndex != NOT_FOUND) ? howManyPhotosUnderParcel(mCurrentTaskParcelIndex) : 0;
    	
    	
    	if(null == mCurrentBeanInfo) return UPDATE_FILE_UPLOAD_TASK_ERROR; //wenyongzhe 2015.11.4 upload toash bug
    	//MyLog.i("SQF_LOG", "UploadTaskListManager::updateFileUploadTaskInfo --------- 22222222222 ");
    	switch(bean.getStatusTaskCode()) {
    	case FileTaskStatusBean.STATE_TASK_PENDING:
    		break;
    	case FileTaskStatusBean.STATE_TASK_CREATE:
    		break;
    	case FileTaskStatusBean.STATE_TASK_CANCELLED:
    		mCurrentBeanInfo.isComplete = true;
    		break;
    	case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
    		break;
    	case FileTaskStatusBean.STATE_TASK_DONE:
    		mCurrentBeanInfo.isComplete = true;
    		break;
    	case FileTaskStatusBean.STATE_TASK_FAILED:
    		break;
    	case FileTaskStatusBean.STATE_TASK_PAUSE:
    		break;
    	case FileTaskStatusBean.STATE_TASK_RUNNING:
    		break;
    	}
    	
    	mCompleteNumUnderCurrentParcel = getPacelComplete(mCurrentTaskParcelIndex);
    	
    	if(mCompleteNumUnderCurrentParcel == mHowManyPhotosUnderCurrentParcel) {
    		//Message msg = mRemoveParcelHandler.obtainMessage(MSG_REMOVE_PARCEL, mCurrentTaskParcelIndex, 0);
    		//mRemoveParcelHandler.sendMessageDelayed(msg, 1000);
    		removeTaskParcel(mCurrentTaskParcelIndex);
    		return UPDATE_FILE_UPLOAD_TASK_OK;//wenyongzhe 2015.11.4 upload toash bug
    	}
		return UPDATE_FILE_UPLOAD_TASK_ERROR;//wenyongzhe 2015.11.4 upload toash bug
    }
    
    public static final int NOT_FOUND = -1;
    public int findTaskParcelIndex(FileTaskStatusBean bean) {
    	for(int i=0; i<mUploadTaskList.size(); i++) {
    		ArrayList<FileUploadTaskInfo> taskList = mUploadTaskList.get(i);
    		for(FileUploadTaskInfo info : taskList) {
    			if(info.uploadInfo.getSource().equals(bean.getSource()) && 
    					info.uploadInfo.getTarget().equals(bean.getTarget())) {
    				return i;
    			}
    		}
    	}
    	return NOT_FOUND;
    }
    
    public int getParcelSize() {
    	return mUploadTaskList.size();
    }
    
    public boolean noTaskParcel() {
    	Log.i("SQF_LOG", "mUploadTaskList noTaskParcel isEmpty ----> " + mUploadTaskList.size());
    	return mUploadTaskList.isEmpty() || mUploadTaskList.size() == 0;
    }
    
    public void removeTaskParcel(int index) {
    	mUploadTaskList.remove(index);
    }

    public FileUploadTaskInfo findTaskInfoByBean(FileTaskStatusBean bean) {
    	for(int i=0; i<mUploadTaskList.size(); i++) {
    		ArrayList<FileUploadTaskInfo> taskList = mUploadTaskList.get(i);
    		
    		for(FileUploadTaskInfo info : taskList) {
    			/*
    			Log.i("SQF_LOG", "UploadTaskListManager::findTaskInfoByBean :" + info.uploadInfo.getSource() + " " 
    					+ info.uploadInfo.getTarget() + " " + bean.getSource() + " " + bean.getTarget());
    			*/
    			if(info.uploadInfo.getSource().equals(bean.getSource()) && 
    					info.uploadInfo.getTarget().equals(bean.getTarget())) {
    				mCurrentTaskParcelIndex = i;
    				mHowManyPhotosUnderCurrentParcel = taskList.size();
    				return info;
    			}
    		}
    	}
    	return null;
    }
    
    public int getPacelComplete(int parcelIndex) {
    	//wenyongzhe 2015.11.3 ArrayIndexOutOfBoundsException start
    	if(parcelIndex<0 || parcelIndex>=mUploadTaskList.size()){
    		return 0;
    	}
    	//wenyongzhe 2015.11.3 ArrayIndexOutOfBoundsException end
    	ArrayList<FileUploadTaskInfo> list = mUploadTaskList.get(parcelIndex);
    	int complete = 0;
    	for(int i=0; i<list.size(); i++) {
    		FileUploadTaskInfo info = list.get(i);
    		if(info.isComplete) {
    			++ complete;
    		}
    	}
    	return complete;
    }
}

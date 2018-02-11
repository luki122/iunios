package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.cache.bean.BeanCacheManager;
import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.android.gallery3d.xcloudalbum.uploaddownload.FakeProgressDb;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadBroadcastReceiver;

public class XCloudTaskListenerManager {
	
	public static final int PROGRESS_INTERVAL = 2000;
	private static final String TAG ="XCloudTaskListenerManager";
	
	private Context mContext;
	private static XCloudTaskListenerManager mInstance;
	
    private static final int MSG_UPDATE_LIST = 1000;
    private static final int MSG_UPDATE_BEAN_LIST = 1001;
    public static final int MSG_NOTIFY_UI = 1002;
    public static final int MSG_NOTIFY_DECREASE_CLEAR_LIST = 1003;
    
    public static final int ARG_CLEARING_LIST = 1;
	private LinkedList<FileTaskStatusBean> mUploadBeans = new LinkedList<FileTaskStatusBean>();
	private LinkedList<FileTaskStatusBean> mDownloadBeans = new LinkedList<FileTaskStatusBean>();
	private static final String BEAN_CACHE_KEY_UPLOAD = "BEAN_CACHE_KEY_UPLOAD";
	private static final String BEAN_CACHE_KEY_DOWNLOAD = "BEAN_CACHE_KEY_DOWNLOAD";
	private BeanCacheManager mBeanCacheManager;
	private FakeTaskManager mFakeTaskManager;
	private ArrayList<IAlbumTaskListener> mListeners = new ArrayList<IAlbumTaskListener>();
	private static final int MSG_GET_PHOTO_TASK_LIST = 1234;
	private boolean mPhotoTaskListGot = false;
	private Handler mNotifyUiHandler;
	private GetPhotoTaskListFinishListener mGetPhotoTaskListFinishListener;
	
	public LinkedList<FileTaskStatusBean> getUploadBeans() {
		return mUploadBeans;
	}
	
	public LinkedList<FileTaskStatusBean> getDownloadBeans() {
		return mDownloadBeans;
	}
	
	public interface GetPhotoTaskListFinishListener {
		public void getPhotoTaskListFinished();
	}

	public void setIsPhotoTaskListGot(boolean value) {
		mPhotoTaskListGot = value;
	}
	public boolean isPhotoTaskListGot() {
		return mPhotoTaskListGot;
	}
	
	public void sendGetPhotoTaskListDelayed() {
		if(isPhotoTaskListGot()) return;
		//MyLog.i2("SQF_LOG", "get photo task list-------------------------------------------------------------------22222");
		Message msg = mHandler.obtainMessage(MSG_GET_PHOTO_TASK_LIST);
		mHandler.sendMessageDelayed(msg, 100);
	}
	
	public void sendGetPhotoTaskListDelayed(GetPhotoTaskListFinishListener listener) {
		mGetPhotoTaskListFinishListener = listener;
		sendGetPhotoTaskListDelayed();
	}
	
	public void setNotifyUiHandler(Handler handler) {
		mNotifyUiHandler = handler;
	}
	
	public void debugUploadInfo() {
		//Log.i("SQF_LOG", "=========================XCloudTaskListManager BEGIN==========================");
		for(int i=0; i<getUploadBeans().size(); i++) {
			FileTaskStatusBean bean = getUploadBeans().get(i);
			//Log.i("SQF_LOG", "XCloudTaskListManager BEAN: ------> " + bean.toString());
		}
		//Log.i("SQF_LOG", "=========================XCloudTaskListManager END==========================");
	}
	
	private Handler mHandler = new Handler() {
		@Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            System.out.println(msg.what);
            switch (msg.what) {
            	case MSG_GET_PHOTO_TASK_LIST:
        			BaiduAlbumUtils.getInstance(mContext).getPhotoTaskList();
        		break;
            	case MSG_UPDATE_LIST:
            		FileTaskStatusBean b = (FileTaskStatusBean)msg.obj;
            		updateList(b, false);
            		notifyUi(false);
            	break;
            	case MSG_UPDATE_BEAN_LIST:
            		List<FileTaskStatusBean> list = (List<FileTaskStatusBean>)msg.obj;
            		updateList(list, true);
            		break;
            }
		}
	};

	private void sendUpdateListMessage(List<FileTaskStatusBean> beans) {
		//Log.i("SQF_LOG", "XCloudTaskListenerManager::sendUpdateListMessage 111");
    	mHandler.obtainMessage(MSG_UPDATE_BEAN_LIST, beans).sendToTarget();
    }
    
    private void sendUpdateListMessage(FileTaskStatusBean bean) {
    	//Log.i("SQF_LOG", "XCloudTaskListenerManager::sendUpdateListMessage 222");
    	mHandler.obtainMessage(MSG_UPDATE_LIST, bean).sendToTarget();
    }    

    private void insertByTaskIdDesc( FileTaskStatusBean bean) {
    	if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		int insertPos = 0;
    		for(FileTaskStatusBean tmpBean : getUploadBeans()) {
    			if(tmpBean.getFileTaskId() > bean.getFileTaskId()) {
    				insertPos ++;
    			} else {
    				break;
    			}
    		}
    		getUploadBeans().add(insertPos, bean);

    	} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
    		int insertPos = 0;
    		for(FileTaskStatusBean tmpBean : getDownloadBeans()) {
    			if(tmpBean.getFileTaskId() > bean.getFileTaskId()) {
    				insertPos ++;
    			} else {
    				break;
    			}
    		}

    		getDownloadBeans().add(insertPos, bean);
    	}
    }
    
    public FileTaskStatusBean findBeanInTwoList(FileTaskStatusBean bean) {
    	FileTaskStatusBean retBean = null;
    	if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		String key = bean.getSource() + bean.getTarget();
    		retBean = mFakeTaskManager.getFakeUploadBeans().get(key);
    		if(retBean != null) {
    			return retBean;
    		}
    		
    		retBean = binarySearch(retBean);
    		return retBean;
    	} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
    		MyLog.i2("SQF_LOG", "unimplemented....");
    	}
    	return null;
    }

    private static Comparator<FileTaskStatusBean> mComparator = new Comparator<FileTaskStatusBean>() {
		@Override
		public int compare(FileTaskStatusBean arg0,
				FileTaskStatusBean arg1) {
			// TODO Auto-generated method stub
			//Log.i("SQF_LOG", "arg0: id:" + arg0.getFileTaskId() + " arg1 id:" + arg1.getFileTaskId());
			if(arg0.getFileTaskId() > arg1.getFileTaskId()) return -1;
			if(arg0.getFileTaskId() < arg1.getFileTaskId()) return 1;
			return 0;
		}
	} ;
	
	public static FileTaskStatusBean binarySearch(List<FileTaskStatusBean> list, FileTaskStatusBean bean) {
    		int i = Collections.binarySearch(list, bean, mComparator);
    		if(i < 0) {
    			//Log.i("SQF_LOG", "------i:" + i + " bean id :" + bean.getFileTaskId() + " NOT FOUND .");
    			return null;
    		}
    		//Log.i("SQF_LOG", "------FOUND bean id:" + bean.getFileTaskId() + " i:" + i + " mUploadBeans.size:" + mUploadBeans.size());
    		return list.get(i);
	}
    
    private FileTaskStatusBean binarySearch(FileTaskStatusBean bean) {
    	if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		int i = Collections.binarySearch(getUploadBeans(), bean, mComparator);
    		if(i < 0) {
    			//Log.i("SQF_LOG", "------i:" + i + " bean id :" + bean.getFileTaskId() + " NOT FOUND .");
    			return null;
    		}
    		//Log.i("SQF_LOG", "------FOUND bean id:" + bean.getFileTaskId() + " i:" + i + " mUploadBeans.size:" + mUploadBeans.size());
    		return getUploadBeans().get(i);
    	} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
    		int i = Collections.binarySearch(getDownloadBeans(), bean, mComparator);
    		if(i < 0) {
    			//Log.i("SQF_LOG", "------i:" + i + " bean id :" + bean.getFileTaskId() + " NOT FOUND .");
    			return null;
    		}
    		//Log.i("SQF_LOG", "------FOUND bean id:" + bean.getFileTaskId() + " i:" + i + " mDownloadBeans.size:" + mDownloadBeans.size());
    		return getDownloadBeans().get(i);
    	}
    	return null;
    }
    
    private boolean foundInFakeTaskManager(FileTaskStatusBean bean) {
    	String key = bean.getSource() + bean.getTarget();
    	if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		if(mFakeTaskManager.getFakeUploadBeans().containsKey(key)) {
    			return true;
    		}
    	} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
    		if(mFakeTaskManager.getFakeDownloadBeans().containsKey(key)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    public static final int FOUR_MEGA_BYTES = 4 * 1024 * 1024; 
    
    private void removeFromList(FileTaskStatusBean bean) {
    	
    }
    
    private void updateList(List<FileTaskStatusBean> beans, boolean fromPhotoTaskList) {
    		for(FileTaskStatusBean bean : beans) {
    			updateList(bean, fromPhotoTaskList);
    		}
    }
    
	public void updateList(FileTaskStatusBean bean, boolean fromPhotoTaskList) {
		//Log.i("SQF_LOG", "updateList bean:" + bean.toString());
			if(bean.getTotalSize() >= FOUR_MEGA_BYTES && 
				bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD && 
				bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_RUNNING) {
				FakeProgressDb.TaskInfo info = FakeProgressDb.getTaskInfoByTaskId(mContext, bean.getFileTaskId());
				if(info == null) {
					info = new FakeProgressDb.TaskInfo();
					info.taskId = bean.getFileTaskId();
					info.taskState = bean.getStatusTaskCode();
					info.currentSize = bean.getCurrentSize() == -1 ? 0 : bean.getCurrentSize();
					info.totalSize = bean.getTotalSize();
				}
				//update TaskInfo
				if(bean.getCurrentSize() > info.currentSize) {
					info.currentSize = bean.getCurrentSize();
				}
				info.totalSize = bean.getTotalSize();
				FakeProgressDb.saveTaskInfo(mContext, info);
			}
			
			//first found if BEAN is in FakeTaskManager
			boolean foundInFakeTask = foundInFakeTaskManager(bean);
			if(foundInFakeTask) {
				//Log.i("SQF_LOG", "XCloudTaskListenerManager::updateList --> foundInFakeTask:" + foundInFakeTask);
				if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE && fromPhotoTaskList) {
					//we reset the state to make user feel it's re-uploading. but in fact it may not be uploading.
					bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_CREATE);
				}
				mFakeTaskManager.updateFakeTaskBean(bean);
				//delete in task manager.
				removeBeanFromTaskManager(bean);
				return;
			}
			
			//if not in FakeTaskManager, update list or add into list.
			if (bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
				FileTaskStatusBean b = binarySearch(bean);
				if (b != null) {
					//Log.i("SQF_LOG", "XCloudTaskListenerManager::updateBeanStatus --> bean :"  + bean.toString());
					updateBeanStatus(b, bean);
				} else {
					//MyLog.i2("SQF_LOG", "XCloudTaskListenerManager::insertByTaskIdDesc --> bean :"  + bean.toString());
					insertByTaskIdDesc(bean);
				}
			} else if (bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
				FileTaskStatusBean b = binarySearch(bean);
				if (b != null) {
					updateBeanStatus(b, bean);
				} else {
					insertByTaskIdDesc(bean);
				}
			} else {
				Log.e("SQF_LOG",
						"UploadDownloadListActivity:: mUploadBeans updateList ERROR "
								+ bean.getFileTaskId());
			}
	}
	
	public void removeBeanFromTwoList(FileTaskStatusBean delBean) {
		//Log.i("SQF_LOG", "XCloudTaskListenerManager::removeBeanFromTwoList delBean:" + delBean.toString());
		String key = delBean.getSource() + delBean.getTarget();
		if(delBean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			if(mFakeTaskManager.getFakeUploadBeans().containsKey(key)) {
				mFakeTaskManager.getFakeUploadBeans().remove(key);
				//Log.i("SQF_LOG", "XCloudTaskListenerManager::removeBeanFromTwoList delBean 11111");
			} else {
				//Log.i("SQF_LOG", "XCloudTaskListenerManager::removeBeanFromTwoList delBean 22222");
				removeBeanFromTaskManager(delBean);
			}
		} else if(delBean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			if(mFakeTaskManager.getFakeDownloadBeans().containsKey(key)) {
				mFakeTaskManager.getFakeDownloadBeans().remove(key);
			} else {
				removeBeanFromTaskManager(delBean);
			}
		}
	}
	
//	public void removeBeanFromTaskManagerByTraverse(FileTaskStatusBean bean) {
//		if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
//			for(int i=0; i<getUploadBeans().size(); i++) {
//				FileTaskStatusBean tmp = getUploadBeans().get(i);
//				if(tmp.getFileTaskId() == bean.getFileTaskId() || 
//					tmp.)
//				getUploadBeans().remove(delBean);
//			}
//			
//		} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
//			getDownloadBeans().remove(delBean);
//		}
//	}
	
	private void removeBeanFromTaskManager(FileTaskStatusBean bean) {
		FileTaskStatusBean delBean = binarySearch(bean);
		if(delBean == null) {
			//Log.i("SQF_LOG", "removeBeanFromTaskManager : not found. return" + bean.toString());
			return;
		}
		if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			getUploadBeans().remove(delBean);
		} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			getDownloadBeans().remove(delBean);
		}
	}
	
	public ArrayList<FileTaskStatusBean> getUploadingBeans() {
		
		ArrayList<FileTaskStatusBean> beans = new ArrayList<FileTaskStatusBean>();
		//fake task manager 
		IndexedLinkedHashMap fakeUploadBeans = mFakeTaskManager.getFakeUploadBeans();
		for(int i=0; i < fakeUploadBeans.getKeys().size(); i++) {
			String key = fakeUploadBeans.getKeys().get(i);
			FileTaskStatusBean bean = fakeUploadBeans.get(key);
			if(bean == null) continue;
			if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_RUNNING) {
				beans.add(bean);
				if(beans.size() >= AlbumClientProxy.DEFAULT_THREAD_COUNT) {
					return beans;
				}
			}
		}
		//task manager.
		LinkedList<FileTaskStatusBean> uploadBeans = getUploadBeans();
		for(int i=0; i < uploadBeans.size(); i++) {
			FileTaskStatusBean bean = uploadBeans.get(i);
			if(bean == null) continue;
			if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_RUNNING) {
				beans.add(bean);
				if(beans.size() >= AlbumClientProxy.DEFAULT_THREAD_COUNT) {
					return beans;
				}
			}
		}
		
		return beans;
	}
    
    private void updateBeanStatus(FileTaskStatusBean from, FileTaskStatusBean to) {
    	if(from.getFileTaskId() != to.getFileTaskId() || 
    			! from.getFileName().equals(to.getFileName()) ||
    			! from.getSource().equals(to.getSource()) ||
    			! from.getTarget().equals(to.getTarget()) ||
    			from.getType() != to.getType() ) return;
    	 /*
    	 Log.i("SQF_LOG", "updateBeanStatus from.to. id:" + from.getFileTaskId() + " from getCurrentSize:" + from.getCurrentSize() + 
    										" from getTotalSize:" + from.getTotalSize() + 
    										" to getCurrentSize:" + to.getCurrentSize() + 
    										" to getTotalSize:" + to.getTotalSize() + 
    										" from and to statusType:" + from.getStatusType() + " " + to.getStatusType());
    										*/
    	if(to.getStatusType() != FileTaskStatusBean.STATUS_END) {
    		from.setCurrentSize(to.getCurrentSize());
    		from.setTotalSize(to.getTotalSize());
    	}
    	from.setErrorCode(to.getErrorCode());
    	from.setMessage(to.getMessage());
    	from.setStatusTaskCode(to.getStatusTaskCode());
    	from.setStatusType(to.getStatusType());
    }
    
    public void removeUploadCache() {
    	mBeanCacheManager.removeCache(BEAN_CACHE_KEY_UPLOAD);
    }
    
    public void removeDownloadCache() {
    	mBeanCacheManager.removeCache(BEAN_CACHE_KEY_DOWNLOAD);
    }
    
    public void notifyUi(boolean clearingList) {
    	if(mNotifyUiHandler != null) {
			Message msg = mNotifyUiHandler.obtainMessage(MSG_NOTIFY_UI);
			if(clearingList) {
				msg.arg1 = XCloudTaskListenerManager.ARG_CLEARING_LIST;//clearing list
			}
			//mNotifyUiHandler.removeMessages(MSG_NOTIFY_UI);
			mNotifyUiHandler.sendMessage(msg);
		}
    }
	
	private IAlbumTaskListener mAlbumTaskListener = new IAlbumTaskListener() {
		
		@Override
		public void onGetTaskStatus(FileTaskStatusBean bean) {
			//MyLog.i("SQF_LOG", "!!!!!!!!!!!!! \n onGetTaskStatus bean :" + bean.toString());
			if (!TextUtils.isEmpty(bean.getMessage()) && bean.getMessage().equals("remove success")) {
				removeBeanFromTwoList(bean);
				notifyUi(true);
			} else {
				sendUpdateListMessage(bean);
			}
			for(IAlbumTaskListener listener : mListeners) {
				listener.onGetTaskStatus(bean);
			}
		}

		@Override
		public long progressInterval() {
			// TODO Auto-generated method stub
			return PROGRESS_INTERVAL;
		}

		@Override
		public void onGetTaskListFinished(
				List<FileTaskStatusBean> fileTaskStatusBeanList) {
			// TODO Auto-generated method stub
			//Log.i("SQF_LOG", "!!!!!!!!!!!!!!!!!!!!!22222222222 XCloudTaskListenerManager::onGetTaskListFinished ----> ");
			
			boolean hasUploadingTask = false;
			
			mBeanCacheManager.removeCache(BEAN_CACHE_KEY_UPLOAD);
			mBeanCacheManager.removeCache(BEAN_CACHE_KEY_DOWNLOAD);
			List<FileTaskStatusBean> uploadBeans = new ArrayList<FileTaskStatusBean>();
			List<FileTaskStatusBean> downloadBeans = new ArrayList<FileTaskStatusBean>();
			for(int i=0; i<fileTaskStatusBeanList.size(); i++) {
				FileTaskStatusBean bean = fileTaskStatusBeanList.get(i);
				if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
					uploadBeans.add(bean);
					if(bean.getStatusTaskCode() != FileTaskStatusBean.STATE_TASK_DONE) {
						hasUploadingTask = true;
					}
				} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
					downloadBeans.add(bean);
				}
			}
			mBeanCacheManager.saveCache(BEAN_CACHE_KEY_UPLOAD, uploadBeans);
			mBeanCacheManager.saveCache(BEAN_CACHE_KEY_DOWNLOAD, downloadBeans);
			//Log.i("SQF_LOG", "mQueryTaskStatusListener -------> 333 onGetTaskListFinished fileTaskStatusBeanList size:" + fileTaskStatusBeanList.size());
			/*
			for(FileTaskStatusBean bean : fileTaskStatusBeanList) {
				Log.i("SQF_LOG", "mQueryTaskStatusListener -------> 333:" + bean.toString());
				//updateList(bean);
			}
			*/
			if(!hasUploadingTask) {
				XCloudAutoUploadBroadcastReceiver.sendNotifyUserCenter(mContext);
			}
			sendUpdateListMessage(fileTaskStatusBeanList);
			
			for(IAlbumTaskListener listener : mListeners) {
				listener.onGetTaskListFinished(fileTaskStatusBeanList);
			}
			
			if(mGetPhotoTaskListFinishListener != null) {
				setIsPhotoTaskListGot(true);
				mGetPhotoTaskListFinishListener.getPhotoTaskListFinished();
			}
		}
		
	};
	
	private XCloudTaskListenerManager(Context context) {
		//do nothing.
		mContext = context;
		mBeanCacheManager = BeanCacheManager.getInstance(context);
		mFakeTaskManager = FakeTaskManager.getInstance();
		//wenyongzhe 2015.9.21
//		updateListFromCache();
	}
	
    private void updateListFromCache() {
    	//Log.i("SQF_LOG", "-===============================updateListFromCache  ");
    	List<FileTaskStatusBean> uploadBeans = mBeanCacheManager.getFromCache(BEAN_CACHE_KEY_UPLOAD);
    	List<FileTaskStatusBean> downloadBeans = mBeanCacheManager.getFromCache(BEAN_CACHE_KEY_DOWNLOAD);
    	if(uploadBeans != null) {
    		updateList(uploadBeans, true);
    	}
    	if(downloadBeans != null) {
    		updateList(downloadBeans, true);
    	}
    }

	public static XCloudTaskListenerManager getInstance(Context context) {
		if(mInstance == null) {
			mInstance = new XCloudTaskListenerManager(context);
		}
		return mInstance;
	}
	
	public IAlbumTaskListener getAlbumTaskListener() {
		//LogUtil.d(TAG, "getAlbumTaskListener::"+mAlbumTaskListener);
		return mAlbumTaskListener;
	}
	
	public void registerListener(IAlbumTaskListener listener) {
		if( ! mListeners.contains(listener)) {
			mListeners.add(listener);
			//Log.i("SQF_LOG", "XCloudTaskListener::registerListener ----> size:" + mListeners.size());
		}
	}
	
	public void unregisterListener(IAlbumTaskListener listener) {
		if(mListeners.contains(listener)) {
			//Log.i("SQF_LOG", "XCloudTaskListener::unregisterListener ---->  ");
			mListeners.remove(listener);
		}
	}
	
	public void removeAllByType(int type) {
		if(type == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			mFakeTaskManager.removeAllByType(FileTaskStatusBean.TYPE_TASK_UPLOAD);
			mUploadBeans.clear();
		} else if(type == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			mFakeTaskManager.removeAllByType(FileTaskStatusBean.TYPE_TASK_DOWNLOAD);
			mDownloadBeans.clear();
		}
	}
	
	public int getTotalTaskSize(int type) {
		if(type == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			return mFakeTaskManager.getFakeUploadBeans().getSize() + mUploadBeans.size();
		} else if(type == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			return mFakeTaskManager.getFakeDownloadBeans().getSize() + mDownloadBeans.size();
		}
		return 0;
	}
}

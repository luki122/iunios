package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import aurora.widget.AuroraListView;
import aurora.widget.AuroraCheckBox;

import com.android.gallery3d.R;
import com.android.gallery3d.ui.AuroraRoundProgressBar;
import com.android.gallery3d.util.LinkedNode.List;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader.ImageProcessingCallback;
import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.media.ThumbnailUtils;
import android.os.Handler;
import android.os.Message;

import com.android.gallery3d.xcloudalbum.uploaddownload.MaskImageView;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity.NetworkStateChangeNotifier;

import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;

public class UploadDownloadListAdapter extends BaseAdapter implements NetworkStateChangeNotifier{
	
	private LayoutInflater mInflater;
	private LinkedList<FileTaskStatusBean> mBeansList;
	private Context mContext;
	private String mProgressFormat;
	private ImageLoader mImageLoader;
	private Handler mHandler;
	private int mItemHeight;
	
    public static final int MSG_PAUSE = 101;
    public static final int MSG_RESUME = 102;
    public static final int MSG_RESTART = 103;
    public static final int MSG_REMOVE = 104;
    //wenyongzhe 2015.9.1
    public static final int MSG_REMOVE_SELECT = 105;
    
    private int mControlBtnResumeTextColor;
    
    private int mCancelBtnRightMargin;
    private int mPauseResumBtnRightMargin;
    private int mCancelBtnWidth;
    private int mCancelBtnHeight;
    private int mPauseResumeBtnWidth;
    private int mPauseResumeBtnHeight;
    
    private FakeTaskManager mFakeTaskManager;
    private XCloudTaskListenerManager mTaskManager;
    
    private int mType;
    
    private Timer mFakeProgressTimer;
    private TimerTask mFakeProgressTimerTask;
    
    
    private boolean mIsListViewScrolling;
    private volatile boolean mIsLocked; 
    private Object mLock = new Object();
    
    private int mClearedNum;
    private int mTotalClearNum;
    
    private Dialog mDialog;
    private FileTaskStatusBean mBeanToDelete;
    
    //wenyongzhe 2015.8.31
    private boolean isPlayAnima = false,isOperationFile = false;
    
    public void notifyListViewIsScrolling(boolean isScrolling) {
    	
    	mIsListViewScrolling = isScrolling;
    	if( ! mIsListViewScrolling) {
    		synchronized (mLock) {
    			if(mIsLocked) {
    				mIsLocked = false;
    				//Log.i("SQF_LOG","UploadDownloadListAdapter::lock notifyAll........................................");
    				mLock.notifyAll();
    				
    			}
			}
    	}
    }
    
    public void setTotalClearNum() {
    	mTotalClearNum = mTaskManager.getTotalTaskSize(mType); 
    }
    
    public int getTotalClearNum() {
    	return mTaskManager.getTotalTaskSize(mType);
    }
    
    public void resetClearedNum() {
    	mClearedNum = 0;
    }
    
    public void increaseClearedNum() {
    	++ mClearedNum;
    	//Log.i("SQF_LOG", " increaseClearedNum --> " + mClearedNum);
    }
    
    public int getClearedNum() {
    	return mClearedNum;
    }

    public boolean isClearingList() {
    	//return mIsClearingList;
    	if(mTotalClearNum == 0 &&  mClearedNum == 0) {
    		return false;
    	}
    	if(mTotalClearNum > 0 && getTotalClearNum() != getClearedNum()) {
    		//Log.i("SQF_LOG", "UploadDownloadListAdapter::isClearingList true");
    		return true;
    	}
    	return false;
    }
	
	public void killFakeProgressTimer() {
		
		if(mType != FileTaskStatusBean.TYPE_TASK_UPLOAD) return;
		
		if(mFakeProgressTimerTask != null) {
			mFakeProgressTimerTask.cancel();
			mFakeProgressTimerTask = null;
		}
		if(mFakeProgressTimer != null) {
			mFakeProgressTimer.cancel();
			mFakeProgressTimer = null;
		}
	}
	
	public void startFakeProgressTimer() {
		//MyLog.i2("SQF_LOG", "startFakeProgressTimer ===== = = = =");
//		if(true) {
//			return;
//		}
		if(mType != FileTaskStatusBean.TYPE_TASK_UPLOAD) return;
		mFakeProgressTimer = new Timer();
		mFakeProgressTimerTask = new TimerTask() {
	    	
	    	private long mPrevRx = -1;
	    	private long mNetRate = 0;

			@Override
			public void run() {
				synchronized (mLock) {
					try {
						if(mIsListViewScrolling) {
							mIsLocked = true;
							Log.i("SQF_LOG","UploadDownloadListAdapter::lock........................................");
							mLock.wait();
						}
					} catch (InterruptedException e) {
						
					}
				}
				if(isClearingList()) {
					//Log.i("SQF_LOG","UploadDownloadListAdapter::isClearingList..........return...........");
					return;
				}
				
				// TODO Auto-generated method stub
				long rx = NetworkUtil.getTotalRxBytes(mContext);
				//Log.i("SQF_LOG", "---------------- rx:" + rx);
				if(mPrevRx != -1) {
					mNetRate = rx - mPrevRx;
				}
				mPrevRx = rx;
				
				ArrayList<FileTaskStatusBean> uploadingBeans = mTaskManager.getUploadingBeans();
				for(int i=0; i<uploadingBeans.size(); i++) {
					FileTaskStatusBean bean = uploadingBeans.get(i);
					
					FakeProgressDb.TaskInfo info = FakeProgressDb.getTaskInfoByTaskId(mContext, bean.getFileTaskId());
					if(info == null) {
						info = new FakeProgressDb.TaskInfo();
						info.taskId = bean.getFileTaskId();
						info.taskState = FileTaskStatusBean.STATE_TASK_RUNNING;
						info.currentSize = bean.getCurrentSize() == -1 ? 0 : bean.getCurrentSize();
						info.totalSize = bean.getTotalSize();
					}
					//update TaskInfo
					long currentSize = info.currentSize + mNetRate;
					currentSize = currentSize >= (bean.getTotalSize() * 0.99) ? ((long)(bean.getTotalSize() * 0.99)) : currentSize;
					info.currentSize = currentSize;
					info.totalSize = bean.getTotalSize();
					FakeProgressDb.saveTaskInfo(mContext, info);
					
					//update bean
					bean.setCurrentSize(currentSize);
					//Log.i("SQF_LOG", " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! mFakeProgressTimerTask mTaskManager.updateList(bean, false)");
					if( ! isClearingList()) {
						mTaskManager.updateList(bean, false);
					}
				}
				sendMessage(XCloudTaskListenerManager.MSG_NOTIFY_UI, null);
			}
		};
		mFakeProgressTimer.scheduleAtFixedRate(mFakeProgressTimerTask, 0, 1000);
	}

	public UploadDownloadListAdapter(Context context, int type, Handler handler) {
		super();
		
		mType = type;
		mContext = context;
		mHandler = handler;
		
		mInflater = LayoutInflater.from(context);
		Resources res = mContext.getResources();
		mItemHeight = res.getDimensionPixelSize(R.dimen.aurora_upload_download_list_height);
		mProgressFormat = res.getString(R.string.aurora_string_progress_text_format);
		
		mCancelBtnRightMargin = res.getDimensionPixelSize(R.dimen.aurora_updown_cancel_btn_right_margin);
		mPauseResumBtnRightMargin = res.getDimensionPixelSize(R.dimen.aurora_updown_pause_resume_btn_right_margin);
		
		mCancelBtnWidth = res.getDimensionPixelSize(R.dimen.aurora_updown_cancel_btn_width_height);
		mCancelBtnHeight = res.getDimensionPixelSize(R.dimen.aurora_updown_cancel_btn_width_height);
		
		mPauseResumeBtnWidth = res.getDimensionPixelSize(R.dimen.aurora_updown_pause_resume_btn_width);
		mPauseResumeBtnHeight = res.getDimensionPixelSize(R.dimen.aurora_updown_pause_resume_btn_height);	
		
		mImageLoader = ImageLoader.getInstance(mContext);
		
		mControlBtnResumeTextColor = mContext.getResources().getColor(R.color.custom_action_bar_btn_pressed_color);
		
		mFakeTaskManager = FakeTaskManager.getInstance();
		mTaskManager = XCloudTaskListenerManager.getInstance(context);
		
		//mFakeProgressTimer.scheduleAtFixedRate(mFakeProgressTimerTask, 0, 1000);
		startFakeProgressTimer();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			return mFakeTaskManager.getFakeUploadBeans().getSize() + mTaskManager.getUploadBeans().size();
		} else if(mType == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			return mFakeTaskManager.getFakeDownloadBeans().getSize() + mTaskManager.getDownloadBeans().size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		//return mBeansList.get(position);
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	
    void sendMessage(int what, FileTaskStatusBean bean) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = bean;
        mHandler.sendMessage(msg);
    }
    
    /*
    private void clearList() {
    	if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
    		for(mFakeTaskManager.getFakeUploadBeans()) {
    			
    		}
    	}
    }
    */
    public void removeAllByType(int type) {
    	if(type != mType) return;
    	
    }
    
    private void showDeleteTaskConfirm() {
    	mDialog = new AuroraAlertDialog.Builder(mContext)
        .setTitle(R.string.aurora_upload_download_list_delete_task_confirm)
        .setPositiveButton(android.R.string.ok, mOnDeleteTaskConfirmListener)
        .setNegativeButton(R.string.cancel, mOnDeleteTaskCancelListener).create();
        mDialog.show();
    }
    
    private DialogInterface.OnClickListener mOnDeleteTaskConfirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            mDialog.dismiss();
            sendMessage(MSG_REMOVE, mBeanToDelete);
        }
    };
    
    private DialogInterface.OnClickListener mOnDeleteTaskCancelListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            mDialog.dismiss();
        }
    };
	
	private OnClickListener mClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getTag() instanceof FileTaskStatusBean) {
				FileTaskStatusBean bean = (FileTaskStatusBean)v.getTag();
				int state = bean.getStatusTaskCode();
				switch (state) {
		        case FileTaskStatusBean.STATE_TASK_DONE:
		        	mBeanToDelete = bean;
		        	showDeleteTaskConfirm();
		        	
		        	//sendMessage(MSG_REMOVE, bean);
		        	break;
		        	
		        case FileTaskStatusBean.STATE_TASK_PENDING:
		        	mBeanToDelete = bean;
		        	showDeleteTaskConfirm();
		        	//Log.i("SQF_LOG", "PENDING------------------------------------------------>SHOULD SWITCH_TO RESUME !!!!!!!!!!!!!!!!!!!!!!!");
		        	//sendMessage(MSG_REMOVE, bean);
		        	break;
		        case FileTaskStatusBean.STATE_TASK_CREATE:
		        case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
		        	//Log.i("SQF_LOG", "PENDING------------------------------------------------>SHOULD SWITCH_TO RESUME !!!!!!!!!!!!!!!!!!!!!!!");
		        	mBeanToDelete = bean;
		        	showDeleteTaskConfirm();
		        	//sendMessage(MSG_REMOVE, bean);
		        	break;
		        case FileTaskStatusBean.STATE_TASK_FAILED:
		        	sendMessage(MSG_RESTART, bean);
		        	if(mTaskManager.getUploadingBeans().size() >= AlbumClientProxy.DEFAULT_THREAD_COUNT) {
		        		//一切都是为了作假
		        		bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_PENDING);
		        		mTaskManager.updateList(bean, false);
		        	}
		        	break;
		        case FileTaskStatusBean.STATE_TASK_PAUSE: {
		        	//一切都是为了作假
		        	//update TaskInfo
		        	FakeProgressDb.TaskInfo info = new FakeProgressDb.TaskInfo();
		        	info.taskId = bean.getFileTaskId();
		        	info.currentSize = bean.getCurrentSize();
		        	info.totalSize = bean.getTotalSize();
		        	info.taskState = FileTaskStatusBean.STATE_TASK_PENDING;
		        	FakeProgressDb.saveTaskInfo(mContext, info);
		        	//update bean
		        	if(mTaskManager.getUploadingBeans().size() >= AlbumClientProxy.DEFAULT_THREAD_COUNT) {
		        		bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_PENDING);
		        	} else {
		        		bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_RUNNING);
		        	}
		        	mTaskManager.updateList(bean, false);
		        	//Log.i("SQF_LOG", "PAUSE------------------------------------------------>SWITCH_TO RESUME");
		        	sendMessage(MSG_RESUME, bean);
		        }
		        	break;
		        	
		        case FileTaskStatusBean.STATE_TASK_RUNNING: {
		        	
		        	//一切都是为了作假
		        	FakeProgressDb.TaskInfo info = new FakeProgressDb.TaskInfo();
		        	info.taskId = bean.getFileTaskId();
		        	info.currentSize = bean.getCurrentSize();
		        	info.totalSize = bean.getTotalSize();
		        	info.taskState = FileTaskStatusBean.STATE_TASK_PAUSE;
		        	FakeProgressDb.saveTaskInfo(mContext, info);
		        	
		        	//update bean
		        	bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_PAUSE);
		        	mTaskManager.updateList(bean, false);
		        	
		        	//Log.i("SQF_LOG", "RESUME------------------------------------------------>SWITCH_TO PAUSE");
		        	sendMessage(MSG_PAUSE, bean);
		        }
		        	break;
		        }
			}
		}
		
	};
	
	private void setStatusInfos(FileTaskStatusBean bean, TextView progressTextView, FakeProgressDb.TaskInfo info){
		//Log.i("SQF_LOG", " 0000000000000========= setStatusInfos ---- " + bean.toString());
		int statusTaskCode = bean.getStatusTaskCode();
		switch(statusTaskCode) {

		case FileTaskStatusBean.STATE_TASK_FAILED:
			
			//Log.i("SQF_LOG", " 0000000000000===================== aurora_upload_download_failed_task ---- " + bean.toString());
			
			progressTextView.setText(R.string.aurora_upload_download_failed_task);
			break;
		case FileTaskStatusBean.STATE_TASK_CREATE:
			progressTextView.setText(R.string.aurora_upload_download_creating_task);
			break;
		case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
			progressTextView.setText(R.string.aurora_upload_download_creating_failed_task);
			break;
		case FileTaskStatusBean.STATE_TASK_PENDING:
			progressTextView.setText(R.string.aurora_upload_download_waiting_task);
			break;
		case FileTaskStatusBean.STATE_TASK_PAUSE:
		case FileTaskStatusBean.STATE_TASK_RUNNING:
			if(statusTaskCode == FileTaskStatusBean.STATE_TASK_PENDING) {
				/*
				Log.i("SQF_LOG", "PENDING ====================================================" + bean.toString() + "\n");
				if(info != null) {
					Log.i("SQF_LOG", "PENDING ====================================================" + info.currentSize  + "\n" +info.totalSize);
				}
				*/
			}
			if(info != null) {
				String progressText = String.format(mProgressFormat, longFormatString(info.currentSize), longFormatString(info.totalSize));
				progressTextView.setText(progressText);
			} else if(bean.getCurrentSize() != -1 && bean.getTotalSize() != -1) {
				String progressText = String.format(mProgressFormat, longFormatString(bean.getCurrentSize()), longFormatString(bean.getTotalSize()));
				progressTextView.setText(progressText);
			} else {
				progressTextView.setText(R.string.aurora_upload_download_waiting_task);
			}
			break;
		case FileTaskStatusBean.STATE_TASK_DONE:

			//wenyongzhe 2015.8.225
			if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
				progressTextView.setText(R.string.aurora_upload_download_task_upload_completed);
			}else if(mType == FileTaskStatusBean.TYPE_TASK_DOWNLOAD){
				progressTextView.setText(R.string.aurora_upload_download_task_download_completed);
			} else{
				progressTextView.setText(R.string.aurora_upload_download_task_completed);
			}
			//wenyongzhe 2015.8.225 end

			break;
		default:
			break;
		}
	}
	
	private void showFadeInOutAnimation(final View v, final boolean show) {
		if(show) {
			if(v.getVisibility() == View.VISIBLE) return;
		} else {
			if(v.getVisibility() == View.INVISIBLE) return;
		}
		v.clearAnimation();
		Animation animation;
		animation = show ? AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in) : AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
		if(show) v.setVisibility(View.VISIBLE);
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				if(!show) {
					v.setVisibility(View.INVISIBLE);
				}
			}
		});
		v.startAnimation(animation);
	}
	
	//wenyongzhe
	private void setListItemStatus(AuroraRoundProgressBar btn, ProgressBar progressBar, int state) {
		//wenyongzhe 2015.9.23
//		setBtnRightWidthHeightAndMargin(btn, state);
        switch (state) {
        case FileTaskStatusBean.STATE_TASK_DONE:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_DONE");
        	
        	//wenyongzhe 2015.9.2 display Button start
//        	btn.setBackgroundResource(R.drawable.aurora_delete_upload_download);
//        	btn.setText("");
        	btn.setVisibility(View.INVISIBLE);
        	//wenyongzhe 2015.9.2 display Button end
        	
        	//btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, true);//
        	progressBar.setVisibility(View.INVISIBLE);//showFadeInOutAnimation(progressBar, false);//
        	break;
        case FileTaskStatusBean.STATE_TASK_PENDING:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_PENDING");
        	//btn.setText(R.string.aurora_upload_download_waiting_task);
        	
        	//wenyongzhe 2015.9.2 display Button start
//        	btn.setBackgroundResource(R.drawable.aurora_delete_upload_download);
//        	btn.setText("");
        	btn.setVisibility(View.INVISIBLE);
        	//wenyongzhe 2015.9.2 display Button end
        	
        	//btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, false);//
        	progressBar.setVisibility(View.VISIBLE);//showFadeInOutAnimation(progressBar, true);//
        	break;
        case FileTaskStatusBean.STATE_TASK_FAILED:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_TRANSACTION_FAILED");
        	//wenyongzhe 2015.9.23
        	btn.setBackgroundResource(R.drawable.aurora_continue_upload_download);
//        	btn.setText(R.string.aurora_upload_download_retry_task);
        	btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, true);//
        	//wenyongzhe
//        	progressBar.setVisibility(View.VISIBLE);//showFadeInOutAnimation(progressBar, true);//
        	progressBar.setVisibility(View.INVISIBLE);
        	break;
        case FileTaskStatusBean.STATE_TASK_PAUSE:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_TRANSACTION_PAUSE");
         	//wenyongzhe 2015.9.23
        	btn.setBackgroundResource(R.drawable.aurora_restart_upload_download);
//        	btn.setText(R.string.aurora_upload_download_continue_task);
//        	btn.setTextColor(mControlBtnResumeTextColor);
        	btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, true);//
        	//wenyongzhe
//        	progressBar.setVisibility(View.VISIBLE);//showFadeInOutAnimation(progressBar, true);//
        	progressBar.setVisibility(View.INVISIBLE);
        	break;
        case FileTaskStatusBean.STATE_TASK_RUNNING:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_TRANSACTION");
        	btn.setBackgroundResource(R.drawable.aurora_pause_upload_download);
//        	btn.setText(R.string.aurora_upload_download_pause_task);
//        	btn.setTextColor(Color.BLACK);
        	btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, true);//
        	//wenyongzhe
//        	progressBar.setVisibility(View.VISIBLE);//showFadeInOutAnimation(progressBar, true);//
        	progressBar.setVisibility(View.INVISIBLE);
        	break;
        case FileTaskStatusBean.STATE_TASK_CREATE:
        	//Log.i("SQF_LOG", "setListItemStatus STATE_TASK_CREATE");
        	
        	//wenyongzhe 2015.9.2 display Button start
//        	btn.setBackgroundResource(R.drawable.aurora_delete_upload_download);
//        	btn.setText("");
        	btn.setVisibility(View.INVISIBLE);
        	//wenyongzhe 2015.9.2 display Button end
        	
        	//btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, false);//
        	progressBar.setVisibility(View.INVISIBLE);//showFadeInOutAnimation(progressBar, false);//
        	break;
        case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
        	//Log.e("SQF_LOG", "setListItemStatus STATE_TASK_CREATE_FAILED");
        
        	//wenyongzhe 2015.9.2 display Button start
//        	btn.setBackgroundResource(R.drawable.aurora_delete_upload_download);
//        	btn.setText("");
        	btn.setVisibility(View.INVISIBLE);
        	//wenyongzhe 2015.9.2 display Button end
        	
        	//btn.setVisibility(View.VISIBLE);//showFadeInOutAnimation(btn, false);//
        	progressBar.setVisibility(View.INVISIBLE);//showFadeInOutAnimation(progressBar, false);//
        	break;
        default:
        	//Log.i("SQF_LOG", "setListItemStatus default");
        	break;
        }
	}
	
	private void setBtnRightMargin(AuroraRoundProgressBar btn, int rightMargin, int width, int height) {//wenyongzhe
		LayoutParams lp = (RelativeLayout.LayoutParams)btn.getLayoutParams();
		lp.setMargins(lp.leftMargin, lp.topMargin, rightMargin, lp.bottomMargin);
		lp.width = width;
		lp.height = height;
    	btn.setLayoutParams(lp);
	}
	
	private void setBtnRightWidthHeightAndMargin(AuroraRoundProgressBar btn, int state) {//wenyongzhe
		switch(state) {
		case FileTaskStatusBean.STATE_TASK_FAILED:
		case FileTaskStatusBean.STATE_TASK_RUNNING:
		case FileTaskStatusBean.STATE_TASK_PAUSE:
		case FileTaskStatusBean.STATE_TASK_CREATE_FAILED:
			setBtnRightMargin(btn, mPauseResumBtnRightMargin, mPauseResumeBtnWidth, mPauseResumeBtnHeight);
			break;
		case FileTaskStatusBean.STATE_TASK_PENDING:
		case FileTaskStatusBean.STATE_TASK_CREATE:
		case FileTaskStatusBean.STATE_TASK_DONE:
			setBtnRightMargin(btn, mCancelBtnRightMargin, mCancelBtnWidth, mCancelBtnHeight);
			break;
		default:
			setBtnRightMargin(btn, mPauseResumBtnRightMargin, mPauseResumeBtnWidth, mPauseResumeBtnHeight);
			break;
		}
	}
	
	private String keepTwoDigitsAfterPoint(float value) {
		//MyLog.i(TAG, "keepTwoDigitsAfterPoint float -----" + value + " " + (float)(Math.round(value*100))/100);
		DecimalFormat df = new DecimalFormat("###.##"); 
		return df.format(value);
		//return (float)(Math.round(value*100))/100;
	}
	
	private String longFormatString(long v) {
		float value = (float)v;
		String format = "";
		if(v < 1024) {
			format = mContext.getResources().getString(R.string.long_format_to_string_b);
			value = (float)v;
		} else if(v < 1024 * 1024) {
			format = mContext.getResources().getString(R.string.long_format_to_string_kb);
			value = (float)v / 1024;
		} else if(v < 1024 * 1024 * 1024) {
			format = mContext.getResources().getString(R.string.long_format_to_string_mb);
			value = (float)v / (1024 * 1024);
		} else {
			format = mContext.getResources().getString(R.string.long_format_to_string_gb);
			value = (float)v / (1024 * 1024 * 1024);
		} 
		return String.format(format, keepTwoDigitsAfterPoint(value));
	}
	
	private int MSG_SET_BITMAP = 1001;
	private Handler mSetBitmapHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what == MSG_SET_BITMAP) {
				SetBitmapHolder holder = (SetBitmapHolder)msg.obj;
				holder.thumbnailImageView.setImageBitmap(holder.bitmap);
				
				Object obj = holder.thumbnailImageView.getTag();
				if(!(obj instanceof FileTaskStatusBean)) return;
				FileTaskStatusBean bean = (FileTaskStatusBean)obj;
				if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {
					holder.thumbnailImageView.setHasMask(false);
				} else {
					holder.thumbnailImageView.setHasMask(true);
				}
				holder.thumbnailImageView.postInvalidate();
			}
		}
		
	};
	
	public class SetBitmapHolder {
		
		public MaskImageView thumbnailImageView;
		public Bitmap bitmap;
		
		public SetBitmapHolder(MaskImageView iv, Bitmap bmp) {
			thumbnailImageView = iv;
			bitmap = bmp;
		}
	};
	
	private FileTaskStatusBean getBeanByPosition(int pos) {
		try {
			if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
				if(pos >= mFakeTaskManager.getFakeUploadBeans().size()) {
					return mTaskManager.getUploadBeans().get(pos - mFakeTaskManager.getFakeUploadBeans().getSize());
				} else {
					return mFakeTaskManager.getFakeUploadBeans().getByPosition(pos);
				}
			} else if(mType == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
				if(pos >= mFakeTaskManager.getFakeDownloadBeans().size()) {
					return mTaskManager.getDownloadBeans().get(pos - mFakeTaskManager.getFakeDownloadBeans().getSize());
				} else {
					return mFakeTaskManager.getFakeDownloadBeans().getByPosition(pos);
				}
			}
		} catch(IndexOutOfBoundsException e) {
			
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView == null) {
			convertView = (View) mInflater.inflate(com.aurora.R.layout.aurora_slid_listview, null);
			RelativeLayout main = (RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front);
			RelativeLayout v = (RelativeLayout)mInflater.inflate(R.layout.upload_download_list_item, null);
			main.addView(v);
			ViewGroup.LayoutParams vl = v.getLayoutParams();
			if (null != vl) {
				vl.height = mItemHeight;
				v.setLayoutParams(vl);
			}
		}
		
		//wenyongzhe 2015.8.29 longclick start
		AuroraCheckBox mAuroraCheckBox =  (AuroraCheckBox) convertView.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		if (isOperationFile) {
			if(  isPlayAnima ){
				AuroraListView.auroraStartCheckBoxAppearingAnim((RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front)
						,  (CheckBox) convertView.findViewById(com.aurora.R.id.aurora_list_left_checkbox));
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible((RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front),
						(CheckBox) convertView.findViewById(com.aurora.R.id.aurora_list_left_checkbox), true);
			}
		} else {
			if (isPlayAnima) {
				AuroraListView.auroraStartCheckBoxDisappearingAnim((RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front)
						,  (CheckBox) convertView.findViewById(com.aurora.R.id.aurora_list_left_checkbox));
			} else {
				aurora.widget.AuroraListView.auroraSetCheckBoxVisible((RelativeLayout) convertView.findViewById(com.aurora.R.id.aurora_listview_front),
						(CheckBox) convertView.findViewById(com.aurora.R.id.aurora_list_left_checkbox), false);
			}
		}
//		mAuroraCheckBox.auroraSetChecked(true, true);

		ViewGroup.LayoutParams  lp = convertView.getLayoutParams(); 
		if(convertView.getHeight() >= mItemHeight ){
			mItemHeight = convertView.getHeight();
		}
		if(lp != null /*&& lp.height != mItemHeight*/)
		{
				lp.height = mItemHeight;	
				convertView.setLayoutParams(lp);
				LinearLayout  auroraChildContent = (LinearLayout)convertView.findViewById(com.aurora.R.id.content);
				auroraChildContent.setAlpha(255);
		}
		//wenyongzhe 2015.8.29 longclick end
		
		final MaskImageView thumbnailImageView = (MaskImageView)convertView.findViewById(R.id.thumbnail);
		//final ImageView thumbnailImageView = (ImageView)convertView.findViewById(R.id.thumbnail);
		TextView fileNameTextView = (TextView)convertView.findViewById(R.id.file_name);
		ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progress_bar);
		TextView progressTextView = (TextView)convertView.findViewById(R.id.progress_text);

		//wenyongzhe 2015.8.29 start
		AuroraRoundProgressBar controlBtn = (AuroraRoundProgressBar)convertView.findViewById(R.id.control_btn);
		controlBtn.setMax(100);
		//wenyongzhe 2015.8.29 end

		controlBtn.setOnClickListener(mClickListener);
		
		FakeProgressDb.TaskInfo info = null;
		FileTaskStatusBean bean = getBeanByPosition(position);
		//if(bean == null) return convertView;
		if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			switch(bean.getStatusTaskCode()) {
			case FileTaskStatusBean.STATE_TASK_DONE:
				FakeProgressDb.deleteTaskInfoByTaskId(mContext, bean.getFileTaskId());
				break;
			case FileTaskStatusBean.STATE_TASK_PENDING:
			case FileTaskStatusBean.STATE_TASK_PAUSE:
			case FileTaskStatusBean.STATE_TASK_RUNNING:
				info = FakeProgressDb.getTaskInfoByTaskId(mContext, bean.getFileTaskId());
				if(info != null) {
					bean.setStatusTaskCode(info.taskState);
				}
				break;
			default:
				break;
			}
		}

		//wenyongzhe 2015.8.29 OperationFile start
		controlBtn.setTag(bean);
		convertView.setTag(bean);
		if (isOperationFile) {
			if(bean.getIsCheck()){
				mAuroraCheckBox.setChecked(true);
			}else{
				mAuroraCheckBox.setChecked(false);
			}
		}
		//wenyongzhe 2015.8.29 OperationFile end
		
		//set file name
		if( ! TextUtils.isEmpty(bean.getFileName())) {
			fileNameTextView.setText(bean.getFileName());
		}
		/*
		Log.i("SQF_LOG", "bean getStatusType ---bean.getFileTaskId():" + bean.getFileTaskId() +"  bean.getStatusType() :" + bean.getStatusType() + 
							" bean.getCurrentSize():" + bean.getCurrentSize() + " bean.getTotalSize():" + bean.getTotalSize());
		*/
		//set Progress
		if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {
			progressBar.setVisibility(View.INVISIBLE);
		} else {
			progressBar.setVisibility(View.VISIBLE);
			if(info != null) {
				if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_PENDING ||
					bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_PAUSE ||
					bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_RUNNING) {
					int progress = (int)(info.currentSize * 100 / ((info.totalSize == 0) ? 1 : info.totalSize) );
					progressBar.setProgress(progress);
					//wenyongzhe
					controlBtn.setProgress(progress);
				}
			} else if(bean.getCurrentSize() != -1 && bean.getTotalSize() != -1) {
				int progress = bean.getTotalSize() == 0 ? 0 : (int)(bean.getCurrentSize() * 100 / bean.getTotalSize());
				progressBar.setProgress(progress);
				//wenyongzhe
				controlBtn.setProgress(progress);
				
			}
			
		}
		
		//thumbnailImageView.setImageResource(R.drawable.upload_download_list_default);
/*		if(bean.getStatusTaskCode() == FileTaskStatusBean.STATE_TASK_DONE) {
			thumbnailImageView.setHasMask(false);
		} else {
			thumbnailImageView.setHasMask(true);
		}
		thumbnailImageView.postInvalidate();*/
		
		// set current size / total size or status infos.
		setStatusInfos(bean, progressTextView, info);
		// pause, control, remove
		setListItemStatus(controlBtn, progressBar, bean.getStatusTaskCode());
		
		if(bean.getType() == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
			String md5 = bean.getSource();
			thumbnailImageView.setTag(md5);
			mImageLoader.displayThumbnail(mContext, bean.getSource(), md5, new ImageProcessingCallback() {
				@Override
				public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
					// TODO Auto-generated method stub
					if (((String) thumbnailImageView.getTag()).equals(tag)) {
						Bitmap bitmap = weak.get();
						if (bitmap == null || bitmap.isRecycled()) {
							return;
						}
						SetBitmapHolder holder = new SetBitmapHolder(thumbnailImageView, weak.get());
						Message msg = mSetBitmapHandler.obtainMessage(MSG_SET_BITMAP);
						msg.obj = holder;
						mSetBitmapHandler.sendMessage(msg);
						//thumbnailImageView.setImageBitmap(weak.get());
					}
				}
			});
			
		} else if(bean.getType() == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
			//Log.i("SQF_LOG", "bean.getSource(): " + bean.getSource() + " bean.getTarget():" + bean.getTarget() );
			String md5 = bean.getTarget() + "" + position;
			thumbnailImageView.setTag(md5);
			mImageLoader.displayImage(bean.getTarget(), md5, new ImageProcessingCallback() {
				@Override
				public void onImageProcessing(WeakReference<Bitmap> weak, String tag) {
					//Log.i("SQF_LOG", "tag----------: 1111 " + tag);
					if (((String) thumbnailImageView.getTag()).equals(tag)) {
						Bitmap bitmap = weak.get();
						if (bitmap == null || bitmap.isRecycled()) {
							return;
						}
						thumbnailImageView.setImageBitmap(weak.get());
					}
				}
			});
		}
		
/*		ViewGroup.LayoutParams vl = convertView.getLayoutParams();
		if (null != vl) {
			vl.height = mItemHeight + 1;// 1 is mListView.getDividerHeight(),
										// but currently getDividerHeight return
										// -1
			convertView.setLayoutParams(vl);
			convertView.findViewById(com.aurora.R.id.content).setAlpha(255);
		}*/
		
		return convertView;
	}

	@Override
	public void onNetworkStateChanged() {
		// TODO Auto-generated method stub
		
		boolean isWifi = NetworkUtil.checkNetwork(mContext);//wenyongzhe 2015.11.10
		ArrayList<FileTaskStatusBean> uploadingBeans = mTaskManager.getUploadingBeans();
		for(int i=0; i<uploadingBeans.size(); i++) {
			//Log.i("SQF_LOG", "---------------------------------------- onNetworkStateChanged ");
			FileTaskStatusBean bean = uploadingBeans.get(i);
			if( ! isWifi) {
				sendMessage(MSG_PAUSE, bean);
			} else {
				sendMessage(MSG_RESUME, bean);
			}
		}
		//sendMessage(XCloudTaskListenerManager.MSG_NOTIFY_UI, null);
	}

	//wenyongzhe 2015.8.29 longclick start
	private ArrayList<FileTaskStatusBean> deletList = new ArrayList<FileTaskStatusBean>();
	private Boolean isCheckAll = false;
	public void setCheckItem(int position, Boolean isCheck){
		if( isOperationFile ){
			FileTaskStatusBean bean = getBeanByPosition(position);
			if( isCheck){
				bean.setIsCheck(true);
				if(!deletList.contains(bean)){
					deletList.add(bean);
				}
			}else{
				bean.setIsCheck(false);
				if(deletList.contains(bean)){
					deletList.remove(bean);
				}
			}
		}
	}
	public void setCheckAll(){
		if( isOperationFile ){
			try {
				if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
					IndexedLinkedHashMap mUploadBeans = mFakeTaskManager.getFakeUploadBeans();
					for( int i=0; i<mUploadBeans.getSize(); i++) {
						mUploadBeans.getByPosition(i).setIsCheck(true);
						 if(!deletList.contains(mUploadBeans.getByPosition(i))){
								deletList.add(mUploadBeans.getByPosition(i));
							}
					}
					LinkedList<FileTaskStatusBean> mUploadTaskBeans = mTaskManager.getUploadBeans();
					for( int i=0; i<mUploadTaskBeans.size(); i++) {
						mUploadTaskBeans.get(i).setIsCheck(true);
						 if(!deletList.contains(mUploadTaskBeans.get(i))){
								deletList.add(mUploadTaskBeans.get(i));
							}
					}
				} else if(mType == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
					IndexedLinkedHashMap mDownloadBeans = mFakeTaskManager.getFakeDownloadBeans();
					for( int i=0; i<mDownloadBeans.getSize(); i++) {
						mDownloadBeans.getByPosition(i).setIsCheck(true);
						 if(!deletList.contains(mDownloadBeans.getByPosition(i))){
								deletList.add(mDownloadBeans.getByPosition(i));
							}
					}
					LinkedList<FileTaskStatusBean> mDownloadTaskBeans = mTaskManager.getDownloadBeans();
					for( int i=0; i<mDownloadTaskBeans.size(); i++) {
						mDownloadTaskBeans.get(i).setIsCheck(true);
						 if(!deletList.contains(mDownloadTaskBeans.get(i))){
								deletList.add(mDownloadTaskBeans.get(i));
							}
					}
				}
			setIsCheckAll(false);
			} catch(IndexOutOfBoundsException e) {
			}
		}
	}
	public void cancelAllSelect(){
			try {
				if(mType == FileTaskStatusBean.TYPE_TASK_UPLOAD) {
					IndexedLinkedHashMap mUploadBeans = mFakeTaskManager.getFakeUploadBeans();
					for( int i=0; i<mUploadBeans.getSize(); i++) {
						mUploadBeans.getByPosition(i).setIsCheck(false);
						deletList.clear();
					}
					LinkedList<FileTaskStatusBean> mUploadTaskBeans = mTaskManager.getUploadBeans();
					for( int i=0; i<mUploadTaskBeans.size(); i++) {
						mUploadTaskBeans.get(i).setIsCheck(false);
						deletList.clear();
					}
				} else if(mType == FileTaskStatusBean.TYPE_TASK_DOWNLOAD) {
					IndexedLinkedHashMap mDownloadBeans = mFakeTaskManager.getFakeDownloadBeans();
					for( int i=0; i<mDownloadBeans.getSize(); i++) {
						mDownloadBeans.getByPosition(i).setIsCheck(false);
						deletList.clear();
					}
					LinkedList<FileTaskStatusBean> mDownloadTaskBeans = mTaskManager.getDownloadBeans();
					for( int i=0; i<mDownloadTaskBeans.size(); i++) {
						mDownloadTaskBeans.get(i).setIsCheck(false);
						deletList.clear();
					}
				}
			setIsCheckAll(true);
			} catch(IndexOutOfBoundsException e) {
				
			}
	}
	public Boolean getCheckItem(int position){
		if( isOperationFile ){
			FileTaskStatusBean bean = getBeanByPosition(position);
			return bean.getIsCheck();
		}else
			return false;
	}
	public boolean isPlayAnima() {
		return isPlayAnima;
	}
	public void setPlayAnima(boolean isPlayAnima) {
		this.isPlayAnima = isPlayAnima;
	}
	public boolean isOperationFile() {
		return isOperationFile;
	}
	public void setOperationFile(boolean isOperationFile) {
		this.isOperationFile = isOperationFile;
	}
	public ArrayList<FileTaskStatusBean> getDeletList() {
		return deletList;
	}
	public Boolean getIsCheckAll() {
		return isCheckAll;
	}
	public void setIsCheckAll(Boolean isCheckAll) {
		this.isCheckAll = isCheckAll;
	}
	//wenyongzhe 2015.8.29 longclick end
	
	
}

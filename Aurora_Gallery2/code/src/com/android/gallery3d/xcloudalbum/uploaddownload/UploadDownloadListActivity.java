package com.android.gallery3d.xcloudalbum.uploaddownload;

import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import aurora.widget.AuroraAbsActionBar.Item;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.widget.AuroraListView.AuroraBackOnClickListener;
import aurora.widget.AuroraListView.AuroraDeleteItemListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog.Builder;

import com.android.gallery3d.R; 
import com.android.gallery3d.app.AlbumPage;
//import com.android.gallery3d.app.AlbumPage.NetworkStateReceiver;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.inter.IBackPressedListener;
import com.android.gallery3d.xcloudalbum.inter.IBaiduinterface;
import com.android.gallery3d.xcloudalbum.tools.cache.bean.BeanCacheManager;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.baidu.xcloud.account.AccountInfo;
import com.baidu.xcloud.pluginAlbum.AlbumClientProxy;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.IAlbumTaskListener;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean.TaskType;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraListView;
import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.DialogInterface.OnCancelListener;
import android.widget.AbsListView.OnScrollListener;

import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListAdapter;
import com.aurora.internal.R.color;
import com.aurora.utils.SystemUtils;

import aurora.app.AuroraAlertDialog;
 
public class UploadDownloadListActivity extends AuroraActivity implements OnClickListener, IBaiduinterface ,OnItemClickListener, OnItemLongClickListener{
	
	private enum TAB_TYPE {
		TAB_TYPE_UPLOAD,
		TAB_TYPE_DOWNLOAD,
	};
	
	public static final String JUMP_TO_DOWNLOAD_TAB_KEY = "JUMP_TO_DOWNLOAD_TAB_KEY";
	
	//wenyongzhe
	public static final int MSG_STOP_CHECKBOX_ANIM = 100;
	public static final int MSG_CLEAR_CURRENT_FAKE_LIST = 201;
	private AuroraActionBar actionBar;
	
	private XCloudTaskListenerManager mTaskManager;
	
	private TAB_TYPE mCurrentTabType = TAB_TYPE.TAB_TYPE_UPLOAD;
	private AuroraListView mAuroraListView;
	private TextView mEmptyView;
	private BaiduAlbumUtils mBaiduAlbumUtils;
	/*
	private Button mLoginBtn;
	private Button mGetListBtn;
	private Button mDownloadBtn;
	private Button mUploadBtn;
	private Button mMkDirBtn;
	*/
	private NetworkStateReceiver mNetworkStateReceiver = new NetworkStateReceiver();
	
	private Button mSwitchUploadBtn;
	private Button mSwitchDownloadBtn;
	private Button mClearListBtn;
	//private XCloudManager mXCloudManager;
	private AlbumClientProxy mAlbumClient;
    protected AccountHelper mAccountHelper; 
    
	//private LinkedList<FileTaskStatusBean> mUploadBeans = new LinkedList<FileTaskStatusBean>();
	//private LinkedList<FileTaskStatusBean> mDownloadBeans = new LinkedList<FileTaskStatusBean>();
    private Dialog mDialog;
    
	private UploadDownloadListAdapter mCurrentAdapter;
	private UploadDownloadListAdapter mUploadAdapter;
	private UploadDownloadListAdapter mDownloadAdapter;	
	private AccountInfo mAccountInfo;
	
	//wenyongzhe 2015.9.1 actionbar
	private TextView leftView, rightView;
	List<FileTaskStatusBean> list =   Collections.synchronizedList(new ArrayList<FileTaskStatusBean>());
	
	
    private int mPrevFirstVisibleItem = 0;
    private boolean mClearListBtnInAnimation = false;
	
    public interface NetworkStateChangeNotifier {
    	public void onNetworkStateChanged();
    }
	private FakeTaskManager mFakeTaskManager = FakeTaskManager.getInstance();
    public AccountHelper getAccountHelper() {
    	return mAccountHelper;
    }
    
	private void updateCurrentAdapter() {
		if(mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD) {
			mUploadAdapter.notifyDataSetChanged();
		} else if(mCurrentTabType == TAB_TYPE.TAB_TYPE_DOWNLOAD) {
			mDownloadAdapter.notifyDataSetChanged();
		}
		
		if(mCurrentAdapter.getCount() == 0) {
			showEmptyView(true);
		} else {
			showEmptyView(false);
		}
	}
	
	private void switchTab(TAB_TYPE type) {
		if(type == TAB_TYPE.TAB_TYPE_UPLOAD) {
			mSwitchUploadBtn.setSelected(true);
			mSwitchDownloadBtn.setSelected(false);
			mAuroraListView.setAdapter(mUploadAdapter);
			mCurrentTabType = TAB_TYPE.TAB_TYPE_UPLOAD;
			mCurrentAdapter = mUploadAdapter;
		} else if(type == TAB_TYPE.TAB_TYPE_DOWNLOAD) {
			mSwitchDownloadBtn.setSelected(true);
			mSwitchUploadBtn.setSelected(false);
			mAuroraListView.setAdapter(mDownloadAdapter);
			mCurrentTabType = TAB_TYPE.TAB_TYPE_DOWNLOAD;
			mCurrentAdapter = mDownloadAdapter;
		}
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.upload_download_list_activity);
        actionBar = getAuroraActionBar();
        setAuroraActionbarSplitLineVisibility(View.GONE);//wenyongzhe
        actionBar.changeAuroraActionbarType(AuroraActionBar.Type.Normal);
        actionBar.setTitle(R.string.aurora_upload_download_list_act_title);
        setAuroraBottomBarMenuCallBack(auroraMenuCallBack);//wenyongzhe setAuroraMenuCallBack mast befor initActionBottomBarMenu
        actionBar.initActionBottomBarMenu(R.menu.aurora_cloud_upload_down_list_menu, 1);
        actionBar.getCancelButton();
        setupViews();
        initData();
        registerNetworkStateReceiver();
        //FakeProgressDb.getAllSavedTaskInfo(this.getBaseContext());
        
      //weyongzhe add start 2015.8.25
  	SystemUtils.setStatusBarBackgroundTransparent(this);
  	setItemBottomBar(1,false);
  	//weyongzhe add end 2015.8.25
    }
    
    @Override
    protected void onDestroy() {
    	unregisterNetworkStateReceiver();
    	mAccountHelper.unregisterAccountContentResolver();
    	super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    /*
    private boolean listContainsTaskBean(LinkedList<FileTaskStatusBean> beans, FileTaskStatusBean bean) {
    	if(beans.contains(bean)) return true;
    	for(FileTaskStatusBean tmpBean : beans) {
    		if(tmpBean.getFileTaskId() == bean.getFileTaskId()) {
    			//Log.i("SQF_LOG", "listContainsTaskBean bean ----> id:" + tmpBean.getFileTaskId());
    			return true;
    		}
    	}
    	return false;
    }
    */
    
    private int temp = 0;
    
    private void initData() {
    	//mXCloudManager = new XCloudManager(this);
    	
    	//wenyongzhe new_ui
    	XCloudTaskListenerManager.getInstance(this).sendGetPhotoTaskListDelayed();
    	
    	mBaiduAlbumUtils = BaiduAlbumUtils.getInstance(this);
    	mTaskManager = XCloudTaskListenerManager.getInstance(this);
    	mTaskManager.setNotifyUiHandler(mHandler);

    	mAlbumClient = AlbumClientProxy.getInstance(mHandler);
       	mAccountHelper = new AccountHelper(this);
		mAccountHelper.registerAccountContentResolver();
		mAccountHelper.update();
		
		mAccountInfo = BaiduAlbumUtils.getInstance(this).getAccountInfo();
    	if(null == mAccountInfo) {
    		mAccountHelper.update();
    		BaiduAlbumUtils.getInstance(this).setBaiduinterface(this);
    		String token = mAccountHelper.user_id;
    		BaiduAlbumUtils.getInstance(this).loginBaidu(token, false);
    	} else {
    		//getPhotoTaskListDelayed();
    		//BaiduAlbumUtils.getInstance(this).getPhotoTaskList();
    	}
    	
    	mUploadAdapter = new UploadDownloadListAdapter(this, FileTaskStatusBean.TYPE_TASK_UPLOAD, mHandler);
    	mDownloadAdapter = new UploadDownloadListAdapter(this, FileTaskStatusBean.TYPE_TASK_DOWNLOAD, mHandler);
    	
    	Intent intent = getIntent();
    	if(intent.getBooleanExtra(JUMP_TO_DOWNLOAD_TAB_KEY, false)) {
    		switchTab(TAB_TYPE.TAB_TYPE_DOWNLOAD);
    	} else {
    		switchTab(TAB_TYPE.TAB_TYPE_UPLOAD);
    	}
    	
    	mAuroraListView.setOnScrollListener(mOnScrollListener);
    }
    
    /**
     * wenyongzhe
     * 2015.8.25
     */
    private AuroraBackOnClickListener mAuroraBackOnClickListener = new AuroraBackOnClickListener(){

		@Override
		public void auroraDragedSuccess(int arg0) {
			mCurrentAdapter.notifyListViewIsScrolling(false);
		}

		@Override
		public void auroraDragedUnSuccess(int arg0) {
			mCurrentAdapter.notifyListViewIsScrolling(false);
		}

		@Override
		public void auroraOnClick(int arg0) {
			dialog();
		}

		@Override
		public void auroraPrepareDraged(int arg0) {
				mCurrentAdapter.notifyListViewIsScrolling(true);
		}
    };
    /**
     * wenyongzhe
     * 2015.8.25
     */
     AuroraDeleteItemListener mAuroraDeleteItemListener = new AuroraDeleteItemListener(){

		@Override
		public void auroraDeleteItem(View view, int position) {
			FileTaskStatusBean bean = (FileTaskStatusBean)view.getTag();
//			mCurrentAdapter.deleteItem(view, position);
			mCurrentAdapter.sendMessage(UploadDownloadListAdapter.MSG_REMOVE, bean);
		}
     };
    private void dialog() {//delete one task
    		AuroraAlertDialog.Builder builder = new Builder(this);
    		builder.setTitle(R.string.photo_edit_cancel_tip_title);
    		builder.setMessage(R.string.aurora_upload_download_list_delete_task_confirm);
    		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
		    		dialog.dismiss();
		    		mAuroraListView.auroraDeleteSelectedItemAnim();
		    		}
	    		});
	
	    		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
	
	    		@Override
	    		public void onClick(DialogInterface dialog, int which) {
	    		dialog.dismiss();
	    		}
	    		});

    		builder.create().show();
    }
    /**
     * wenyongzhe
     * 2015.9.1
     */
    private void uploadDownDeleDialog() {
		AuroraAlertDialog.Builder builder = new Builder(this);
		String title;
		if(mCurrentAdapter.getDeletList().size() ==1){
			title = getResources().getString(R.string.aurora_upload_download_lists_delete_task_confirm_tip_one);
		}else{
			title = getResources().getString(R.string.aurora_upload_download_lists_delete_task_confirm_tip, mCurrentAdapter.getDeletList().size());
		}
		builder.setTitle(title);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
    		@Override
    		public void onClick(DialogInterface dialog, int which) {
	    		dialog.dismiss();
	    		deleSelectItem();
	    		}
		
    		});

    		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

    		@Override
    		public void onClick(DialogInterface dialog, int which) {
    		dialog.dismiss();
    		}
    		});

		builder.create().show();
    }
    /**
     * wenyongzhe
     * 2015.9.1
     */
    private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int action) {
			if (Utils.isFastDouble()) {
				return;
			}
			switch (action) {
			case R.id.cloud_operation_delete_select:
				uploadDownDeleDialog();
				break;
			default:
				break;
			}
		}

	};
   /**
     * wenyongzhe
     * 2015.9.1
     */
	private void deleSelectItem() {
		//wenyongzhe 2015.10.31 start
		mSwitchUploadBtn.setClickable( true );
		mSwitchDownloadBtn.setClickable( true );
		//wenyongzhe 2015.10.31 end
		mCurrentAdapter.setPlayAnima(true);
		mCurrentAdapter.setOperationFile(false);
		mCurrentAdapter.notifyDataSetChanged();
		mHandler.sendEmptyMessage(MSG_STOP_CHECKBOX_ANIM);
		actionBar.setShowBottomBarMenu(false);
		actionBar.showActionBarDashBoard();
		mHandler.sendEmptyMessage(UploadDownloadListAdapter.MSG_REMOVE_SELECT);
	}
    
    private OnScrollListener mOnScrollListener = new OnScrollListener() {
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub
			if(mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD) {
				if(scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL || 
					scrollState == OnScrollListener.SCROLL_STATE_FLING) {
					mCurrentAdapter.notifyListViewIsScrolling(true);
				} else if(scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					mCurrentAdapter.notifyListViewIsScrolling(false);
				}
			}
		}
		
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			// TODO Auto-generated method stub			
			
			//wenyongzhe 2015.9.2 屏蔽底部删除按钮
			if(true) return;
			//wenyongzhe 2015.9.2
			
			if(mClearListBtnInAnimation) return;
			//Log.i("SQF_LOG", "firstVisibleItem:" + firstVisibleItem + " mPrevFirstVisibleItem:" + mPrevFirstVisibleItem);
			if(firstVisibleItem < mPrevFirstVisibleItem) {
				mPrevFirstVisibleItem = firstVisibleItem;
				if(mClearListBtn.getVisibility() == View.GONE) {
					//Log.i("SQF_LOG", "mClearListBtn.getVisibility() == View.GONE");
					return;
				}
				Animation animation = AnimationUtils.loadAnimation(UploadDownloadListActivity.this, android.R.anim.fade_out);
				animation.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation arg0) {
						// TODO Auto-generated method stub
						mClearListBtnInAnimation = true;
					}
					
					@Override
					public void onAnimationRepeat(Animation arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation arg0) {
						// TODO Auto-generated method stub
						mClearListBtn.setVisibility(View.GONE);						
						mClearListBtnInAnimation = false;
					}
				});
				mClearListBtn.startAnimation(animation);
			} else if(firstVisibleItem > mPrevFirstVisibleItem) {
				mPrevFirstVisibleItem = firstVisibleItem;
				if(mClearListBtn.getVisibility() == View.VISIBLE) {
					//Log.i("SQF_LOG", "mClearListBtn.getVisibility() == View.VISIBLE");
					return;
				}
				Animation animation = AnimationUtils.loadAnimation(UploadDownloadListActivity.this, android.R.anim.fade_in);
				mClearListBtn.setVisibility(View.VISIBLE);
				animation.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation arg0) {
						// TODO Auto-generated method stub
						mClearListBtnInAnimation = true;
					}
					
					@Override
					public void onAnimationRepeat(Animation arg0) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation arg0) {
						// TODO Auto-generated method stub
						mClearListBtnInAnimation = false;
						
					}
				});
				mClearListBtn.startAnimation(animation);
			}
		}
	};

    /*
    private void setupTestViews() {
    	mLoginBtn = (Button)findViewById(R.id.login);
    	mGetListBtn = (Button)findViewById(R.id.get_list);
    	mDownloadBtn = (Button)findViewById(R.id.upload);
    	mUploadBtn = (Button)findViewById(R.id.download);
    	mMkDirBtn = (Button)findViewById(R.id.mkdir);
    	
    	mLoginBtn.setOnClickListener(this);
    	mGetListBtn.setOnClickListener(this);
    	mDownloadBtn.setOnClickListener(this);
    	mUploadBtn.setOnClickListener(this);
    	mMkDirBtn.setOnClickListener(this);
    }
    */
    
    private void showEmptyView(boolean show) {
    	if(show) {
	    	if(mEmptyView == null) {
	    		mEmptyView = new TextView(this);
	    	}
	    	mEmptyView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));  
	    	mEmptyView.setGravity(Gravity.CENTER);
	    	//mEmptyView.setText("This appears when the list is empty");
	    	mEmptyView.setText(R.string.aurora_upload_download_no_task);
	    	mEmptyView.setVisibility(View.GONE);
	    	((ViewGroup)mAuroraListView.getParent()).removeView(mEmptyView);  
	    	((ViewGroup)mAuroraListView.getParent()).addView(mEmptyView);  
	    	mAuroraListView.setEmptyView(mEmptyView);
    	} else {
    		((ViewGroup)mAuroraListView.getParent()).removeView(mEmptyView);  
    		mAuroraListView.setEmptyView(null);
    	}
    }
    
    private void setupViews() {
    	mAuroraListView = (AuroraListView)findViewById(R.id.list_view);
    	mSwitchUploadBtn = (Button)findViewById(R.id.upload_switch_btn);
    	mSwitchDownloadBtn = (Button)findViewById(R.id.download_switch_btn);
    	mClearListBtn = (Button)findViewById(R.id.clear_list);
    	mSwitchUploadBtn.setOnClickListener(this);
    	mSwitchDownloadBtn.setOnClickListener(this);
    	mClearListBtn.setOnClickListener(this);
    	//setupTestViews();//FOR TEST
    	
    	//wenyongzhe 2016.1.31 disable new_ui
//    	mAuroraListView.auroraSetNeedSlideDelete(true);
//    	mAuroraListView.auroraSetAuroraBackOnClickListener(mAuroraBackOnClickListener);
//    	mAuroraListView.auroraSetDeleteItemListener(mAuroraDeleteItemListener);
    	//wenyongzhe 2015.8.31
    	mAuroraListView.setOnItemLongClickListener(this);//wenyongzhe
    	mAuroraListView.setOnItemClickListener(this);//wenyongzhe
    	actionBarSetOnClickListener();
    	
    }
    
  
    
    private String getDeviceMsg() {
        String msg = "";
        msg = Build.MODEL;
        return msg;
    }

    private static final int TEST = 1121;
    
    private DialogInterface.OnClickListener mOnDeleteAllConfirmListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            mDialog.dismiss();
            if(mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD) {
            	mCurrentAdapter.killFakeProgressTimer();
            	mCurrentAdapter.setTotalClearNum();
            	mCurrentAdapter.resetClearedNum();
            	Log.i("SQF_LOG", "call process photot task list ......remove upload ");
            	mBaiduAlbumUtils.processPhotoTaskList(FileTaskStatusBean.PROCESS_TYPE_REMOVE, FileTaskStatusBean.TASK_TYPE_UPLOAD);
			} else if(mCurrentTabType == TAB_TYPE.TAB_TYPE_DOWNLOAD) {
				Log.i("SQF_LOG", "call process photot task list ......remove download ");
				mBaiduAlbumUtils.processPhotoTaskList(FileTaskStatusBean.PROCESS_TYPE_REMOVE, FileTaskStatusBean.TASK_TYPE_DOWNLOAD);
			}
        }
    };
    
    private DialogInterface.OnClickListener mOnDeleteAllCancelListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichButton) {
            mDialog.dismiss();
        }
    };

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		/*
		if(id == R.id.login) {
			mXCloudManager.login();
			//String token = PrefUtil.getString(this, AlbumPage.PREF_KEY_IUNI_ACCOUNT_TOKEN, "");
			//BaiduAlbumUtils.getInstance(this).loginBaidu(token, false);
			//mXCloudManager.explicitLogin(thirdToken)
		} else if (id == R.id.get_list) {
			//mXCloudManager.list(AlbumConfig.REMOTEPATH + "/" + getDeviceMsg());\
			mHandler.sendMessageDelayed(mHandler.obtainMessage(TEST), 200);
		} else if (id == R.id.upload) {
			mXCloudManager.upload();
		} else if (id == R.id.download) {
			mXCloudManager.download();
		} else if( id == R.id.mkdir) {
			mXCloudManager.mkPhothdir();
		}
		*/
		switch(id) {
		case R.id.upload_switch_btn:
			//wenyongzhe 2015.10.10
			if(!mCurrentAdapter.isOperationFile()){
				switchTab(TAB_TYPE.TAB_TYPE_UPLOAD);
			}
			break;
		case R.id.download_switch_btn:
			//wenyongzhe 2015.10.10
			if(!mCurrentAdapter.isOperationFile()){
				switchTab(TAB_TYPE.TAB_TYPE_DOWNLOAD);
			}
			break;
		case R.id.clear_list:
			//mBeanCacheManager.removeCache(BEAN_CACHE_KEY_UPLOAD);
			int strResId = -1;
			if(mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD) {
				strResId = R.string.aurora_clear_upload_list;
				mTaskManager.removeUploadCache();
			} else if(mCurrentTabType == TAB_TYPE.TAB_TYPE_DOWNLOAD) {
				strResId = R.string.aurora_clear_download_list;
				mTaskManager.removeDownloadCache();
			}
			//mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD ?  R.string.aurora_clear_upload_list : R.string.aurora_clear_download_list;
	        mDialog = new AuroraAlertDialog.Builder(this)
            .setTitle(strResId)
            .setPositiveButton(android.R.string.ok, mOnDeleteAllConfirmListener)
            .setNegativeButton(R.string.cancel, mOnDeleteAllCancelListener).create();
	        mDialog.show();
			break;
		}
	}
	
	
	
    private final static int MSG_DEFAULT = 0;
    final static int MSG_PROCESS_BEAN = MSG_DEFAULT + 1;
    
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            System.out.println(msg.what);
            switch (msg.what) {
            	case XCloudTaskListenerManager.MSG_NOTIFY_UI:
            		//Log.i("SQF_LOG", "UploadDownloadListActivity::handlerMessage --> MSG_NOTIFY_UI");
            		if(msg.arg1 == XCloudTaskListenerManager.ARG_CLEARING_LIST) {
            			mCurrentAdapter.increaseClearedNum();
            		}
            		updateCurrentAdapter();
            		break;
            	/*
            	case MSG_UPDATE_LIST:
            		FileTaskStatusBean b = (FileTaskStatusBean)msg.obj;
            		updateList(b);
            		updateCurrentAdapter();
            	break;
            	case MSG_UPDATE_BEAN_LIST:
            		List<FileTaskStatusBean> list = (List<FileTaskStatusBean>)msg.obj;
            		updateList(list);
            		updateCurrentAdapter();
            		break;
            	*/
            	case TEST:
            		//BaiduAlbumUtils.getInstance(UploadDownloadListActivity.this).getPhotoTaskList();
            	break;
            	
                case MSG_PROCESS_BEAN:
                    if (msg.obj != null) {
                        FileTaskStatusBean bean = (FileTaskStatusBean) msg.obj;
                        /*
                        Log.i(TAG,
                                "MSG_PROCESS_BEAN:======" + bean.getFileTaskId() + " currentSize:"
                                        + bean.getCurrentSize() + " totalSize:" + bean.getTotalSize() + " type:"
                                        + bean.getType() + " statusType:" + bean.getStatusType() + " errorCode:"
                                        + bean.getErrorCode() + " message:" + bean.getMessage());
                        refreshItem(bean);
                        */
                    }

                    break;

                case UploadDownloadListAdapter.MSG_PAUSE:
                    if (msg.obj != null) {
                        FileTaskStatusBean bean = (FileTaskStatusBean) msg.obj;
                        //Log.i("SQF_LOG", "pause:taskId:" + bean.getFileTaskId());
							/*
                        if(mTaskManager.getAlbumTaskListener() == null) {
                        	Log.i("SQF_LOG", "AlbumTaskListener() IS NULL, PAUSE XCloud Task: " + bean.toString() + " time:" + System.currentTimeMillis());
                        } else {
                        	Log.i("SQF_LOG", "AlbumTaskListener() NOT NULL, PAUSE XCloud Task: " + bean.toString() + " time:" + System.currentTimeMillis());
                        }
							*/
                        
                        //force set StatusTaskCode
                        //bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_PAUSE);
                        //mTaskManager.updateList(mTaskManager.findBeanInTwoList(bean));
                        
                        
                        updateCurrentAdapter();
                        mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_PAUSE.toUpperCase(),
                                bean.getFileTaskId(), mTaskManager.getAlbumTaskListener());
                        
                    }
                    break;
                case UploadDownloadListAdapter.MSG_RESUME:
                    if (msg.obj != null) {
                        FileTaskStatusBean bean = (FileTaskStatusBean) msg.obj;
                        //Log.i("SQF_LOG", "resume:taskId:" + bean.getFileTaskId());
                        
                        //force set StatusTaskCode
                        //bean.setStatusTaskCode(FileTaskStatusBean.STATE_TASK_RUNNING);
                        //mTaskManager.updateList(mTaskManager.findBeanInTwoList(bean));
                        updateCurrentAdapter();
                        mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_RESUME,
                                bean.getFileTaskId(), mTaskManager.getAlbumTaskListener());
                        
                        
                    }
                    break;
                case UploadDownloadListAdapter.MSG_RESTART:
                    if (msg.obj != null) {
                        FileTaskStatusBean bean = (FileTaskStatusBean) msg.obj;
                        //Log.i("SQF_LOG", "restart:taskId:" + bean.getFileTaskId());
                        mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_RESTART,
                                bean.getFileTaskId(),  mTaskManager.getAlbumTaskListener());
                    }
                    break;
                case UploadDownloadListAdapter.MSG_REMOVE:
                    if (msg.obj != null) {
                        FileTaskStatusBean bean = (FileTaskStatusBean) msg.obj;
                        //Log.i("SQF_LOG", "remove:taskId:" + bean.getFileTaskId());
                        mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_REMOVE, bean.getFileTaskId(), mTaskManager.getAlbumTaskListener());
                        mTaskManager.removeBeanFromTwoList(bean);
                        updateCurrentAdapter();
                    }
                    break;

		//wenyongzhe 2015.9  uploadDowloadList start
                case UploadDownloadListAdapter.MSG_REMOVE_SELECT:
                	list = mCurrentAdapter.getDeletList();
                    	for(FileTaskStatusBean mList : list){
                    		mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_REMOVE, mList.getFileTaskId(), mTaskManager.getAlbumTaskListener());
                    		mTaskManager.removeBeanFromTwoList(mList);
                    	}
                    	mCurrentAdapter.getDeletList().clear();
                        updateCurrentAdapter();
                    break;
                case MSG_STOP_CHECKBOX_ANIM:
                   mCurrentAdapter.setPlayAnima(false);
                    break;
                case MSG_CLEAR_CURRENT_FAKE_LIST:
                	int type = -1;
                	if(mCurrentTabType == TAB_TYPE.TAB_TYPE_UPLOAD) {
                		type=1;
                		mTaskManager.removeUploadCache();
                	}else if(mCurrentTabType == TAB_TYPE.TAB_TYPE_DOWNLOAD) {
                		type=2;
                		mTaskManager.removeDownloadCache();
                	}
                	//wenyongzhe 2016.1.31 start
                	LinkedList<FileTaskStatusBean> beanList;
                	if(type == 1){
                		beanList = mTaskManager.getUploadBeans();
                	}else{
                		beanList = mTaskManager.getDownloadBeans();
                	}
                	for(int j=0; j<beanList.size(); j++){
                		mAlbumClient.processPhotoTask(mAccountInfo, FileTaskStatusBean.PROCESS_TYPE_REMOVE, beanList.get(j).getFileTaskId(), mTaskManager.getAlbumTaskListener());
                	}
                	//wenyongzhe 2016.1.31 end
                	mTaskManager.removeAllFakeByType(type);
                	updateCurrentAdapter();
                     break;
                    
	      //wenyongzhe 2015.9  uploadDowloadList start

                    /*
                case BaiduAlbumMain.MSG_XLOUD_ENGINE_UPGREAD:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case BaiduAlbumMain.MSG_XLOUD_ACCOUNT_LOGOUT:
                    Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                    */
            }
        }
    };

	@Override
	public void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath,
			CommonFileInfo info) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void loginComplete(boolean success) {
		// TODO Auto-generated method stub
		//Log.i("SQF_LOG", "UploadDownloadListActivity::loginComplete success:" + success);
		if(success) {
			mAccountInfo = BaiduAlbumUtils.getInstance(this).getAccountInfo();
			//BaiduAlbumUtils.getInstance(this).getPhotoTaskList();
			//getPhotoTaskListDelayed();
		}
	}
	
    private void registerNetworkStateReceiver() {
    	IntentFilter intentFilter = new IntentFilter();
    	intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(mNetworkStateReceiver, intentFilter);        
    }
    
    private void unregisterNetworkStateReceiver() {
    	unregisterReceiver(mNetworkStateReceiver);
    }
	
    public class NetworkStateReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		mUploadAdapter.onNetworkStateChanged();
    		mDownloadAdapter.onNetworkStateChanged();
    	}  

    }

    //wenyognzhe 2015.8.31 start
	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		//TODO Auto-generated method stub
		if(mCurrentAdapter.isOperationFile()){
			return false;
		}
//		mCurrentAdapter.setPlayAnima(true);
//		mCurrentAdapter.setOperationFile(true);
//		mCurrentAdapter.notifyDataSetChanged();
//		mHandler.sendEmptyMessageDelayed(MSG_STOP_CHECKBOX_ANIM, 130);
//		actionBar.setShowBottomBarMenu(true);
//		actionBar.showActionBarDashBoard();
		
		mCurrentAdapter.setIsCheckAll(true);
		actionBarCancel(true);
		setItemBottomBar(1,false);

		onItemClick(arg0,arg1,position,arg3);//wenyongzhe 2015.10.30 BUG17057
		
		return false;
	}
	 //wenyognzhe 2015.8.31 end

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		//TODO Auto-generated method stub
		
		//wenyongzhe new_ui
				
		AuroraCheckBox mAuroraCheckBox =  (AuroraCheckBox) arg1.findViewById(com.aurora.R.id.aurora_list_left_checkbox);
		if(mAuroraCheckBox==null)	return;//wenyongzhe
		
		if(mCurrentAdapter.getCheckItem(position)){
			mAuroraCheckBox.auroraSetChecked(false, true);
			mCurrentAdapter.setCheckItem(position, false);
		}else{
			mAuroraCheckBox.auroraSetChecked(true, true);
			mCurrentAdapter.setCheckItem(position, true);
		}
		
		//wenyongzhe 2015.10.12 menu  buttom disable start
		if(mCurrentAdapter.getDeletList().size()>0){
			setItemBottomBar(1,true);
		}else{
			setItemBottomBar(1,false);
		}
		//wenyongzhe 2015.10.12 menu  buttom end
		
		//wenyongzhe 2015.11.3 actionbar rightButton start
		if(rightView == null){
			rightView = (TextView) getAuroraActionBar().getSelectRightButton();
		}
		if(mCurrentAdapter.getDeletList().size()<mCurrentAdapter.getCount()){
			rightView.setText(R.string.myselect_all);
		}else{
			rightView.setText(R.string.unmyselect_all);
		}
		//wenyongzhe 2015.11.3 actionbar rightButton end
	}

	/**
	 * wenyongzhe 2015.9.2
	 * 分类左右actionBar监听
	 */
	private void actionBarSetOnClickListener() {
		leftView = (TextView) getAuroraActionBar().getSelectLeftButton();
		rightView = (TextView) getAuroraActionBar().getSelectRightButton();
		rightView.setText(R.string.myselect_all);
		leftView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning()) {
					mCurrentAdapter.setIsCheckAll(true);
					actionBarCancel(false);
					mCurrentAdapter.cancelAllSelect();
				}
			}
		});
		rightView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!actionBarIsAnimRunning()) {
					if( getString(R.string.myselect_all).equals(rightView.getText()) ){//wenyongzhe 2015.11.3   if(mCurrentAdapter.getIsCheckAll()){
						rightView.setText(getResources().getString(R.string.unmyselect_all));
						mCurrentAdapter.setCheckAll();
					}else{
						rightView.setText(getResources().getString(R.string.myselect_all));
						mCurrentAdapter.cancelAllSelect();
					}
					mCurrentAdapter.notifyDataSetChanged();
				}
				
				//wenyongzhe 2015.10.12 menu  buttom disable start
				if(mCurrentAdapter.getDeletList().size()>0){
					setItemBottomBar(1,true);
				}else{
					setItemBottomBar(1,false);
				}
				//wenyongzhe 2015.10.12 menu  buttom end
				
			}
		});
	}
	//wenyognzhe 2015.9.1 
	protected void actionBarCancel(Boolean flag) {
		//wenyongzhe 2015.10.31 start
		mSwitchUploadBtn.setClickable( !flag );
		mSwitchDownloadBtn.setClickable( !flag );
		//wenyongzhe 2015.10.31 end
		mCurrentAdapter.setPlayAnima(true);
		mCurrentAdapter.setOperationFile(flag);
		mCurrentAdapter.notifyDataSetChanged();
		mHandler.sendEmptyMessageDelayed(MSG_STOP_CHECKBOX_ANIM, 130);
		actionBar.setShowBottomBarMenu(flag);
		actionBar.showActionBarDashBoard();
		if(mCurrentAdapter.getIsCheckAll()){
			rightView.setText(getResources().getString(R.string.myselect_all));
		}else{
			rightView.setText(getResources().getString(R.string.unmyselect_all));
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//wenyognzhe 2015.10.31 start
		if(actionBarIsAnimRunning()){
			return true;
		}
		//wenyognzhe 2015.10.31 end
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(mCurrentAdapter.isOperationFile()){
				//wenyongzhe 2015.10.31 start
				mSwitchUploadBtn.setClickable( true );
				mSwitchDownloadBtn.setClickable( true );
				//wenyongzhe 2015.10.31 end
				mCurrentAdapter.cancelAllSelect();//wenyongzhe 2015.10.10
				mCurrentAdapter.setPlayAnima(true);
				mCurrentAdapter.setOperationFile(false);
				mHandler.sendEmptyMessageDelayed(MSG_STOP_CHECKBOX_ANIM, 130);
				mCurrentAdapter.notifyDataSetChanged();
				mCurrentAdapter.setIsCheckAll(true);
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 判断编辑动画是否播放完成
	 * wenyognzhe 2015.9.1 
	 * @return
	 */
	public boolean actionBarIsAnimRunning() {
		if (getAuroraActionBar() == null) {// 快速点击图片和手机返回键
			return false;
		}
		if (getAuroraActionBar().auroraIsEntryEditModeAnimRunning()
				|| getAuroraActionBar().auroraIsExitEditModeAnimRunning()) {
			return true;
		}
		return false;
	}
	
	//wenyongzhe add 2015.10.12 diableButtom
	private void setItemBottomBar(int position, boolean use) {
		getAuroraActionBar().getAuroraActionBottomBarMenu()
				.setBottomMenuItemEnable(position, use);
	}

	
}

package com.android.gallery3d.viewpager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.app.Activity;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.view.ViewPager.OnPageChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraActionBarItem;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTabWidget;
import aurora.widget.AuroraViewPager;
import aurora.widget.AuroraActionBar.OnAuroraActionBarItemClickListener;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.local.BasicActivity;
import com.android.gallery3d.local.GalleryItemActivity;
import com.android.gallery3d.local.GalleryLocalActivity;
import com.android.gallery3d.local.tools.GalleryLocalUtils;
import com.android.gallery3d.local.tools.MediaFileOperationUtil;
import com.android.gallery3d.local.widget.MediaFileInfo;
import com.android.gallery3d.setting.SettingsActivity;
import com.android.gallery3d.ui.GLRoot;
import com.android.gallery3d.ui.GLRootView;
import com.android.gallery3d.ui.Log;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.Globals;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.inter.IBaiduTaskListener;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils.INotifyListener;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager.FileUploadTaskInfo;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudAutoUploadBroadcastReceiver;
import com.aurora.utils.SystemUtils;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

@SuppressWarnings("deprecation")
public class ViewpagerActivity extends AuroraActivity implements ViewPagerChileOperattionListener,INotifyListener {

	private static final String STATES_KEY = "android:states";
	private Context context = null;
	private LocalActivityManager mLocalActivityManager = null;
	private AuroraViewPager pager = null;
	private AuroraTabWidget mAuroraTabWidget;
	private static final String TAG = "ViewpagerActivity";
	private int currIndex = 0;// 当前页卡编号
	private String[] mlistTag = { "A", "B", "C" }; // activity标识
	private MyPagerAdapter myPagerAdapter;
	private AuroraOnPageChangeListener auroraOnPageChangeListener;
	private AuroraActionBar mAuroraActionBar;
	private INotifyListener mNotifyListener;
	private BaiduAlbumUtils baiduAlbumUtils;
	
	// private static final int CREATE_ALBUM = 10001;
	// private AuroraAlertDialog mDialog;
	// private AuroraEditText inputName;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = ViewpagerActivity.this;
		((GalleryAppImpl) getApplicationContext()).setmActivityContext(context);
		setAuroraContentView(R.layout.main_viewpager);
		mLocalActivityManager = new LocalActivityManager(this, false);
		Bundle states = savedInstanceState != null ? (Bundle) savedInstanceState.getBundle(STATES_KEY) : null;
		mLocalActivityManager.dispatchCreate(states);
		initAuroraActionBar();
		initPagerViewer();

		if (baiduAlbumUtils == null) {
			baiduAlbumUtils = BaiduAlbumUtils.getInstance(this);
		}
		baiduAlbumUtils.setINotifyListener(ViewpagerActivity.this);
	}

	/**
	 * 初始化PageViewer
	 */
	private ArrayList<View> list;

	private void initPagerViewer() {
		mAuroraTabWidget = (AuroraTabWidget) findViewById(R.id.auroratabwidget);
		mAuroraTabWidget.showCenterShadow();//wenyongzhe 2016.2.2 显示tab下面的横线
		pager = mAuroraTabWidget.getViewPager();
		list = new ArrayList<View>();
		Intent intent = new Intent(context, Gallery.class);
		list.add(getView("A", intent));
		// modify by JXH begin
		Intent intent2 = new Intent(context, GalleryLocalActivity.class);
		list.add(getView("B", intent2));
		Intent intent3 = new Intent(context, CloudActivity.class);
		list.add(getView("C", intent3));
		// modify by JXH end

		myPagerAdapter = new MyPagerAdapter(list);

		pager.setAdapter(myPagerAdapter);
		pager.setCurrentItem(0);
		pager.setOffscreenPageLimit(4);//paul add
		auroraOnPageChangeListener = new AuroraOnPageChangeListener();
		mAuroraTabWidget.setOnPageChangeListener(auroraOnPageChangeListener);
	}

	/**
	 * 通过activity获取视图
	 * @param id
	 * @param intent
	 * @return
	 */
	private View getView(String id, Intent intent) {
		return mLocalActivityManager.startActivity(id, intent).getDecorView();
	}

	/**
	 * 获取当前Activity
	 * @return
	 */
	public Activity getCurrentActivity() {
		return mLocalActivityManager.getActivity(mlistTag[currIndex]);
	}

	/**
	 * 页卡切换监听
	 */
	public class AuroraOnPageChangeListener implements aurora.view.ViewPager.OnPageChangeListener {

		@Override
		public void onPageSelected(int position) {
			// Animation animation = null;
			currIndex = position;
			loadCurrentActivity(position);
			
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
	}

	private void initAuroraActionBar() {
		mAuroraActionBar = getAuroraActionBar();
		((GalleryAppImpl) getApplicationContext()).setmAuroraActionBar(mAuroraActionBar);
		mAuroraActionBar.setTitle(R.string.app_name);

		if (!Globals.OVERSEA_VERSION) {
			getAuroraActionBar().setmOnActionBarBackItemListener(new OnAuroraActionBarBackItemClickListener() {
				@Override
				public void onAuroraActionBarBackItemClicked(int arg0) {
				}
			});//wenyongzhe2016.3.14
			
			View homeBackButton = mAuroraActionBar.getHomeButton();
			View homeTextView = mAuroraActionBar.getHomeTextView();
			mAuroraActionBar.setElevation(0f);
			mAuroraActionBar.setTitle(getResources().getString(R.string.aurora_album));
			mAuroraActionBar.getTitleView().setPadding(getResources().getDimensionPixelSize(com.aurora.internal.R.dimen.aurora_action_bar_margin_left), 0, 0, 0);
			mAuroraActionBar.getTitleView().setTextColor(getResources().getColor(R.color.aurora_actionbar_local_photos_color));
			homeBackButton.setVisibility(View.GONE);
			homeTextView.setVisibility(View.GONE);
			mAuroraActionBar.addItem(AuroraActionBarItem.Type.Set, R.id.aurora_action_bar_all_more);//wenyongzhe2016.1.29
			mAuroraActionBar.setOnAuroraActionBarListener(actionBarItemClickListener);
		}
		setAuroraActionbarSplitLineVisibility(View.GONE);//wenyongzhe

		return;
	}

	/**
	 * @param arg0
	 *            :页面位置
	 * @function:调用子Activity中的方法
	 */

	private void loadCurrentActivity(int position) {
		Activity currentActivity = mLocalActivityManager.getActivity(mlistTag[position]);
		mAuroraActionBar.removeItemByItemId(R.id.aurora_action_bar_all_add);
		switch (position) {
		case 0:
			if (currentActivity != null && currentActivity instanceof Gallery) {
				((Gallery) currentActivity).onPagerSelected();
			}
			break;
		case 1:
			if (currentActivity != null && currentActivity instanceof GalleryLocalActivity) {
				mAuroraActionBar.removeItemByItemId(R.id.aurora_action_bar_all_more);
				mAuroraActionBar.addItem(AuroraActionBarItem.Type.Add, R.id.aurora_action_bar_all_add);
				mAuroraActionBar.addItem(AuroraActionBarItem.Type.Set, R.id.aurora_action_bar_all_more);//wenyongzhe2016.1.29
				((GalleryLocalActivity) currentActivity).initAuroraActionBar();
			}
			break;
		case 2:
			if (currentActivity != null && currentActivity instanceof CloudActivity) {
				((CloudActivity) currentActivity).onPagerSelected();
			}
			break;
		}
	}

	/**
	 * 头标点击监听
	 */
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		@Override
		public void onClick(View v) {
			pager.setCurrentItem(index);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Bundle state = mLocalActivityManager.saveInstanceState();
		if (state != null) {
			outState.putBundle(STATES_KEY, state);
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mLocalActivityManager.dispatchResume();
		
		//wenyongzhe2016.2.16 start
		LinearLayout lIconView = (LinearLayout) mAuroraTabWidget.getmScrollIconLinearLayout();
		if (lIconView.getVisibility()==View.VISIBLE) {
			lIconView.setVisibility(View.VISIBLE);
		} else {
			lIconView.setVisibility(View.GONE);
		}
		//wenyongzhe2016.2.16 end
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		mLocalActivityManager.dispatchStop();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mLocalActivityManager.dispatchPause(isFinishing());// 传入true
	};

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLocalActivityManager.dispatchDestroy(isFinishing());
	}

	@Override
	public void onBackPressed() {
		// send the back event to the top sub-state
		// modify by JXH begin

		try {
			mLocalActivityManager.getActivity(mlistTag[currIndex]).onBackPressed();
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		super.onBackPressed();
		// modify by JXH end
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// modify by JXH begin

		try {
			return mLocalActivityManager.getActivity(mlistTag[currIndex]).onKeyDown(keyCode, event);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return super.onKeyDown(keyCode, event);
		// modify by JXH end
	}

	@Override
	public void subActivitySelectionModeChange(Boolean flag) {
		if (CloudActivity.IS_CLOUD_ITEM_FRAGMENT && !flag) {// 在clouditemfragment取消批量操作也不显示tab
			return;
		}
		LinearLayout lIconView = (LinearLayout) mAuroraTabWidget.getmScrollIconLinearLayout();
		pager.setCanScroll(!flag);
		
		 // wengyongzhe 2016.1.14 new_ui flash start
		if (getCurrentActivity() instanceof Gallery) {
			GLRoot mGLRoot = ((Gallery)getCurrentActivity()).getGLRoot();
			mGLRoot.setCanSetFrame(false);
		}
		// wengyongzhe 2016.1.14 new_ui flash end
		
		if (flag) {
			lIconView.setVisibility(View.GONE);
		} else {
			lIconView.setVisibility(View.VISIBLE);
		}
		
		 // wengyongzhe 2016.1.14 new_ui flash start
		if (getCurrentActivity() instanceof Gallery) {
			mHandler.obtainMessage(UPDATE_WINDOW_MESSAGE).sendToTarget();
		}
		// wengyongzhe 2016.1.14 new_ui flash end
	}

	// wengyongzhe 2016.1.14 new_ui flash bug
	private static final int UPDATE_WINDOW_MESSAGE = 100;
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case UPDATE_WINDOW_MESSAGE:
				Activity activity = getCurrentActivity();
				if (activity instanceof Gallery) {
					((Gallery)activity).getGLRoot().setCanSetFrame(true);
				}
				break;
			}
		}
	};

	private OnAuroraActionBarItemClickListener actionBarItemClickListener = new OnAuroraActionBarItemClickListener() {

		@Override
		public void onAuroraActionBarItemClicked(int itemId) {
			LogUtil.d(TAG, "-------itemId:" + itemId);
			if (itemId == R.id.aurora_action_bar_all_add) {
//				BasicActivity currentActivity = (BasicActivity) mLocalActivityManager.getActivity(mlistTag[currIndex]);
//				currentActivity.createNewAlbum();
				MediaFileOperationUtil operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(ViewpagerActivity.this);
				operationUtil.setDialogParams(newFilePath,false);
				operationUtil.createNewAlbum(ViewpagerActivity.this);
				
			}
			//wenyongzhe new_ui
			if (itemId == R.id.aurora_action_bar_all_more) {
				Intent mIntent = new Intent(context, SettingsActivity.class);
				context.startActivity(mIntent);
			}
			
			
		}

	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.d(TAG, "-----requestCode::" + requestCode + " resultCode::" + resultCode + " data::" + data);
		if(currIndex == 2){
			((CloudActivity)mLocalActivityManager.getActivity(mlistTag[currIndex])).onActivityResult(requestCode, resultCode, data);
		}else if(currIndex==1) {
			((GalleryLocalActivity)mLocalActivityManager.getActivity(mlistTag[currIndex])).onActivityResult(requestCode, resultCode, data);
		}
	}
	
	protected AuroraAlertDialog mDialog;
	protected AuroraEditText inputName;
	private String newFilePath;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == MediaFileOperationUtil.CREATE_ALBUM) {
			LogUtil.d(TAG, "-----------onCreateDialog");
			MediaFileOperationUtil operationUtil = MediaFileOperationUtil.getMediaFileOperationUtil(this);
			return operationUtil.createNewAlbumDialog();
		}
		return super.onCreateDialog(id);
	}



	//wenyongzhe 2016.2.17 start
	public void sendBroadcastScan() {
		Intent intent = new Intent();
		intent.setAction(CloudItemFragment.ACTION_DIR_SCAN);
		intent.setData(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory() + "/DCIM/cloud")));
		sendBroadcast(intent);
	}
	@Override
	public void baiduDownloadNotify(FileTaskStatusBean bean) {
		DownloadTaskListManager manager = ((GalleryAppImpl) getApplication()).getDownloadTaskListManager();
		manager.updateDownloadStatus(bean);
		int total = manager.getDownloadSize();
    	int complete = manager.getCompleteIndex();
    	if (total == complete || manager.getDownloadinfos().size()==0) {
    		sendBroadcastScan();
    	}
    	if(manager.getDownloadinfos().size()>0){
//    		uploadNotificationNotify(total,complete,manager.getDownloadinfos().get(0).get(0).hashCode(),false);
    		uploadNotificationNotify(total,complete,111,false);
    	}
    	else{
    		uploadNotificationNotify(total,complete,-1,false);
    	}
	}


	@Override
	public void baiduUploadNotify(FileTaskStatusBean bean) {
		UploadTaskListManager manager = ((GalleryAppImpl) getApplication()).getUploadTaskListManager();
		if (manager.getParcelSize() == 0) {
//			sendBroadcastScan();
		}
		manager.updateFileUploadTaskInfo(bean);
		int total = manager.howManyPhotosUnderCurrentParcel();
    	int complete = manager.completeNumberUnderCurrentParcel();
    	if(manager.getmUploadTaskList().size()>0){
    		int index = manager.getCurrentTaskParcelIndex();//wenyongzhe 2016.2.19
//    		uploadNotificationNotify(total,complete,manager.getmUploadTaskList().get(index).get(0).hashCode(), true);
    		uploadNotificationNotify(total,complete,5, true);
    	}
    	else{
    		uploadNotificationNotify(total,complete,-1,true);
    	}
	}
	
	private NotificationManager mNotifManager;
	private Notification.Builder builder;
	private Notification notification;
	private int mLastCmpIndex = -1;
	private int mHashcode = -1;//wenyongzhe 2016.2.19
	private void uploadNotificationNotify(int total, int complete,int hashCode, boolean isUpload) {   //wenyongzhe 2016.1.30 new_ui
		if(mLastCmpIndex == complete || context==null) return;
		if(hashCode==-1){
			hashCode = mHashcode;
		}else{
			mHashcode = hashCode;
		}
		if(builder == null){
			builder = new Notification.Builder(this);
		}
		if(mNotifManager == null){
			mNotifManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mLastCmpIndex = complete;
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
	
		Intent resultIntent = new Intent(this,UploadDownloadListActivity.class);
		PendingIntent resultPendingIntent = PendingIntent.getActivity(ViewpagerActivity.this, 0, resultIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(resultPendingIntent);
		
		if(total == complete ){ 
			mLastCmpIndex = -1;
			builder.setContentTitle(getResources().getString(R.string.aurora_downloaded));
		}else if(total > complete){
			builder.setContentTitle(getResources().getString(R.string.aurora_downloading));
		}
		builder.setContentText(complete+"/"+total);
//		if(complete==0){
			if(isUpload  ){
				builder.setSmallIcon(R.drawable.aurora_menu_upload_to_xcloud_normal);
				builder.setTicker(getResources().getString(R.string.aurora_cloud_notify_uploading));
			}else{
				builder.setSmallIcon(R.drawable.cloud_download_normal);
				builder.setTicker(getResources().getString(R.string.aurora_cloud_notify_dowloading));
			}
//		}
		notification = builder.build();
		mNotifManager.notify(hashCode, notification);
	}
	
}

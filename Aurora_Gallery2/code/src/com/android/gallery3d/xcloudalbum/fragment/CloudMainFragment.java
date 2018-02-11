package com.android.gallery3d.xcloudalbum.fragment;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import aurora.widget.AuroraActionBar;

import com.android.gallery3d.R;
import com.android.gallery3d.app.AlbumPage;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.util.MyLog;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.util.PrefUtil;
import com.android.gallery3d.xcloudalbum.CloudActivity;
import com.android.gallery3d.xcloudalbum.account.AccountHelper;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.DownloadTaskListManager;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.tools.cache.image.ImageLoader;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadDownloadListActivity;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager.FileUploadTaskInfo;
import com.android.gallery3d.xcloudalbum.uploaddownload.XCloudTaskListenerManager;
import com.android.gallery3d.xcloudalbum.widget.CloudMainAdapter;
import com.aurora.utils.SystemUtils;
import com.baidu.xcloud.pluginAlbum.AlbumConfig;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.AccountProxy;

public class CloudMainFragment extends BasicFragment{
	private static final String TAG = "CloudMainFragment";
	private CloudMainAdapter adapter;
	private View titleView;
	private AccountHelper accountHelper;
	private static final int REQUEST_ACCOUNT_LOGIN = 110;

	//wenyongzhe mark position
	private  static int gridviewPosition = 0,gridviewOffset =0;

	public CloudMainAdapter getAdapter() {
		return adapter;
	}

	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap = new ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>>();

	public ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> getConcurrentHashMap() {
		return concurrentHashMap;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.aurora_cloud_album, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		setGridView((GridView) getView().findViewById(R.id.cloud_album));
		//SQF ADDED ON 2015.6.16 BEGIN
		if(adapter == null) {
			adapter = new CloudMainAdapter(null, CloudMainFragment.this);
			getGridView().setAdapter(adapter);
		}
		//SQF ADDED ON 2015.6.16 END
		
		super.onActivityCreated(savedInstanceState);
		accountHelper = new AccountHelper(getCloudActivity());
		accountHelper.registerAccountContentResolver();

		//wenyongzhe modify 2015.9.10 modify fragment hide/show
		init();	
		//wenyongzhe 2015.10.16 disable screenshot
		setChange(2);

		showLoadingImage(true);
		accountHelper.update();
		
		if (!AccountHelper.mLoginStatus) {
			Uri uri = Uri.parse("openaccount://com.aurora.account.login");
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtra("type", 1);
			intent.setData(uri);
			startActivityForResult(intent, REQUEST_ACCOUNT_LOGIN);
		} else {
			loginBaiduAlbum();
		}
	
		//wenyognzhe 2015.8.28 设置GridView位置  start
		
		getGridView().post(new Runnable() { 
			@Override 
			public void run() {
				int heigth = getGridView().getHeight();
				int gridLine = getGridView().getCount();
				if(gridLine%2 == 1){
					gridLine++;
					gridLine=gridLine/2;
				}
				//Log.e(TAG, "onActivityCreated"+heigth+"---"+gridLine);
				if(gridLine !=0){
					if( (-gridviewOffset)  > (heigth/gridLine) ){
						gridviewPosition=gridviewPosition+2;
					}
				}
				getGridView().setSelection(gridviewPosition);
				//getGridView().smoothScrollToPositionFromTop(gridviewPosition, 200, 1);
				
			}
		});
		//wenyognzhe 2015.8.28 设置GridView位置 end

	}

	//wenyognzhe 2015.11.2 BUG 17059 StatusBar start
	@Override
	public void onPause() {
		//TODO Auto-generated method stub
		super.onPause();
		SystemUtils.switchStatusBarColorMode(SystemUtils.STATUS_BAR_MODE_WHITE, getCloudActivity());
	}
	//wenyognzhe 2015.11.2 BUG 17059 StatusBar  end

	private void loginBaiduAlbum() {
		//paul add 
		final AccountProxy proxy = AccountProxy.getInstance();
		if (getBaiduAlbumUtils().getAccountInfo() == null || proxy.hasLogout()) {// login
			String token = PrefUtil.getString(getActivity(),
					AlbumPage.PREF_KEY_IUNI_ACCOUNT_TOKEN, "");
			//LogUtil.d(TAG, "token::" + token);
			getBaiduAlbumUtils().loginBaidu(token, false);
		} else {
			if(getCloudActivity().isFragmentBackRefresh()){
				getCloudActivity().setFragmentBackRefresh(false);
				getBaiduAlbumUtils().getFileListFromBaidu(AlbumConfig.REMOTEPATH, true, null);
				return;
			}
			getBaiduAlbumUtils().getFileListDiffFromBaidu(
					AlbumConfig.REMOTEPATH, true, null);

		}
	}

	//wenyongzhe modify 2015.9.10 modify fragment hide/show
	private void init(){
		//wenyongzhe  2015.10.31 add baiduTaskStatus()
		getBaiduAlbumUtils().setBaiduTaskListener(this);
		
		getAuroraActionBar().changeAuroraActionbarType(
				AuroraActionBar.Type.Custom);
		getAuroraActionBar().setCustomView(R.layout.aurora_cloud_action_bar);
		titleView = getAuroraActionBar().getCustomView(R.id.cloud_actionbar);
		// LogUtil.d(TAG, "titleView::"+titleView);
		setLocalAlbum(titleView);
		startUploadDownloadActivity(titleView);
		LinearLayout parent = (LinearLayout) titleView.getParent().getParent();
		parent.setGravity(Gravity.CENTER_VERTICAL);
		getCloudActivity().setAuroraMenuCallBackListener();
		
		getCloudActivity().showHomeButtonView(false);
		getAuroraActionBar().initActionBottomBarMenu(
				R.menu.aurora_cloud_album_main_menu, 2);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		//TODO Auto-generated method stub
		super.onHiddenChanged(hidden);
		if(hidden == false){
			init();
			getBaiduAlbumUtils().getFileListDiffFromBaidu(AlbumConfig.REMOTEPATH, true, null);
		}
	}
	//wenyongzhe modify 2015.9.10 modify fragment hide/show

	private void setLocalAlbum(View view) {
		View localAlbum = view.findViewById(R.id.local_albums);
		localAlbum.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getCloudActivity().finish();
			}
		});
	}
	
	//wenyongzhe 2015.9.7 add button start
	private void startUploadDownloadActivity(View view) {
		View uploadDownloadView = view.findViewById(R.id.tv_upload_dowload);
		uploadDownloadView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getCloudActivity(), UploadDownloadListActivity.class);
				getCloudActivity().startActivity(intent);
			}
		});
	}
	//wenyongzhe 2015.9.7 add button end

	//wenyongzhe 2015.10.9 start
	@Override
	public void cancelOperation() {
		//TODO Auto-generated method stub
		super.cancelOperation();
		if(adapter != null) {
			adapter.notifyDataSetInvalidated();
		}
	}
	//wenyongzhe 2015.10.9 end
		
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.d(TAG, "requestCode::" + requestCode + " resultCode::"
				+ resultCode + " data::" + data);
		if (requestCode == REQUEST_ACCOUNT_LOGIN
				&& resultCode == Activity.RESULT_OK) {
			getCloudActivity().setToLogin();//paul add
			loginBaiduAlbum();
			mHandler.sendEmptyMessageDelayed(MSG_LOGIN_BAIDU_ALBUM, 20000);//wenyongzhe 2015.11.17 call loginBaiduAlbum()  again
		}else if(requestCode == REQUEST_ACCOUNT_LOGIN) {
			showLoadingImage(false);
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_album_login_cloud);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (accountHelper != null) {
			accountHelper.unregisterAccountContentResolver();
		}
		
		//wenyognzhe 2015.8.28 记录GridView位置  start
		  View c = getGridView().getChildAt(0);
		  if (c != null) {
			  gridviewOffset = c.getTop(); 
		    }
		  gridviewPosition =  getGridView().getFirstVisiblePosition();
		  //wenyognzhe 2015.8.28 记录GridView位置  end

	}
	
	//
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			//SQF ADDED BEGIN
			if(msg.what == MSG_REFRESH_UI) {
				if (size == concurrentHashMap.size()) {
					if (adapter == null) {
						adapter = new CloudMainAdapter(null, CloudMainFragment.this);
						getGridView().setAdapter(adapter);
					}
					adapter.setConcurrentHashMap(concurrentHashMap);
					getCloudActivity().setConcurrentHashMap(concurrentHashMap);
					adapter.notifyDataSetChanged();
					showLoadingImage(false);
					stopAnimationLoading();
				}
			} else if(msg.what == MSG_REFRESH_UI_2) {
				List<CommonFileInfo> temp = (List<CommonFileInfo>)msg.obj;
				if (adapter == null) {
					adapter = new CloudMainAdapter(temp, CloudMainFragment.this);
					getGridView().setAdapter(adapter);
				} else {
					adapter.setFileInfos(temp);
					adapter.notifyDataSetChanged();
					
					//wenyongzhe 2015.11.19 屏蔽自动进入新建目录，切换多语言bug
					//wenyongzhe 2015.9.17 跳转页面
//					if(isCreateAlbumComplete){
//						//wenyongzhe 2015.10.27 BUG 17018
//						if(adapter.getCount()<=2){
//							return;
//						}
//						try {
//							CommonFileInfo fileInfo = (CommonFileInfo) adapter.getItem(2);//wenyongzhe 2015.10.16 disable screenshot
//							getCloudActivity().setFileInfo(fileInfo);
//							getCloudActivity().setItemFileInfos(concurrentHashMap.get(fileInfo));
//							getCloudActivity().selectPage(CloudActivity.CLOUDITEM);
//						} catch (Exception e) {
//							// TODO: handle exception
//						}
//						isCreateAlbumComplete=false;
//					}
					//wenyongzhe 2015.9.17 跳转页面
					
				}
				//wenyongzhe 2015.11.17 call loginBaiduAlbum()  again start
			}else if(msg.what == MSG_LOGIN_BAIDU_ALBUM){
				if(mLoadingProgressBar.getVisibility() == View.VISIBLE){
					loginBaiduAlbum();
				}
			}
			//wenyongzhe 2015.11.17 call loginBaiduAlbum()  again end
			//SQF ADDED END
		}
		
	};
	
	@Override
	public void loginComplete(boolean success) {// login OK
		LogUtil.d(TAG, "loginComplete success::" + success);
		if (success) {
			XCloudTaskListenerManager.getInstance(getCloudActivity()).sendGetPhotoTaskListDelayed();//SQF ADDED ON 2015.6.1
			getBaiduAlbumUtils().getFileListDiffFromBaidu(
					AlbumConfig.REMOTEPATH, true, null);
		} else {
			getCloudActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					showLoadingImage(false);
					ToastUtils.showTast(getCloudActivity(),
							R.string.aurora_album_login_failed);
				}
			});
		}
	}

	private int size = 0;
	private CommonFileInfo album = null;
	private CommonFileInfo screenshot = null;
	private CommonFileInfo info = new CommonFileInfo();
	private static final int MSG_REFRESH_UI = 1200;//SQF ADDED ON 2015.6.16
	private static final int MSG_REFRESH_UI_2 = 1201;//SQF ADDED ON 2015.6.16
	private static final int MSG_LOGIN_BAIDU_ALBUM = 1202;	//wenyongzhe 2015.11.17 call loginBaiduAlbum()  again
	@Override
	public synchronized void baiduPhotoList(List<CommonFileInfo> list, boolean isDirPath,
			CommonFileInfo infos) {
//		LogUtil.d(TAG, "baiduPhotoList isVisible::"+isVisible()+" isDirPath::"+isDirPath);
		if (!isAdded()) {
			return;
		}
	
		if(isDirPath){
			List<CommonFileInfo> temp=null;
			concurrentHashMap.clear();
			if (list != null) {
				size = list.size();
				temp = new ArrayList<CommonFileInfo>(list);
				for (CommonFileInfo commonFileInfo : list) {
					if (commonFileInfo.isDir
							&& !TextUtils.isEmpty(commonFileInfo.path)) {
//						LogUtil.d(TAG,
//								"commonFileInfo::" + commonFileInfo.toString());
						 getBaiduAlbumUtils().getFileListFromBaidu(
						 commonFileInfo.path, false, commonFileInfo);
						if (commonFileInfo.path.equals(AlbumConfig.REMOTEPATH+getCloudActivity().getString(R.string.aurora_album)) )
							{
							temp.remove(commonFileInfo);
							album = commonFileInfo;
						}
						if (commonFileInfo.path.equals(AlbumConfig.REMOTEPATH+getCloudActivity().getString(R.string.aurora_system_screenshot)) )
						   {
							temp.remove(commonFileInfo);
							screenshot = commonFileInfo;
						}
					}
				}
				
				temp.add(0, info);
				if (album != null) {
					temp.add(1, album);
				} 
				
				//wenyongzhe 2015.10.16  disable screenshot start
//				if (screenshot != null) {
//					temp.add(2, screenshot);
//				}
				//wenyongzhe 2015.10.16  disable screenshot start
				
				setImageInfos(temp);
				getCloudActivity().setAlbumInfos(temp);
			}
			//SQF MODIFIED BEGIN
			//ORIGINAL:
//			if (adapter == null) {
//				adapter = new CloudMainAdapter(temp, CloudMainFragment.this);
//				getGridView().setAdapter(adapter);
//			} else {
//				adapter.setFileInfos(temp);
//				adapter.notifyDataSetChanged();
//			}
			//SQF MODIFIED TO 
			mHandler.obtainMessage(MSG_REFRESH_UI_2, temp).sendToTarget();//SQF MODIFIED TO
			//SQF MODIFIED END
			
			if (list == null) {
				showLoadingImage(false);
				if(!NetworkUtil.checkWifiNetwork(getCloudActivity())){
					setEmptyViewText(R.string.aurora_album_wifi_refash);
					showLoadingImagview();
				}
			}
			
			//wenyongzhe 2015.11.19 bug17169 multi language start
			if(album == null){
				 getOperationUtil().createAlbumFromBaidu( getActivity().getString(R.string.aurora_album));
			}
			//wenyongzhe 2015.11.19 bug17169 multi language end

		}else {
			if (list == null) {
				list = new ArrayList<CommonFileInfo>();
			}
			if (concurrentHashMap.contains(infos)) {
				concurrentHashMap.replace(infos, list);
			} else {
				concurrentHashMap.put(infos, list);
			}

			mHandler.obtainMessage(MSG_REFRESH_UI).sendToTarget();

//			LogUtil.d(TAG, "size::" + size + "  concurrentHashMap.size()::"
//					+ concurrentHashMap.size());
//			if (size == concurrentHashMap.size()) {
//				if (adapter == null) {
//					adapter = new CloudMainAdapter(null, CloudMainFragment.this);
//					getGridView().setAdapter(adapter);
//				}
//				adapter.setConcurrentHashMap(concurrentHashMap);
//				getCloudActivity().setConcurrentHashMap(concurrentHashMap);
//				adapter.notifyDataSetChanged();
//				showLoadingImage(false);
//				stopAnimationLoading();
//			}
		}
	}
	
	
	
	@Override
	public void reloadFileList() {
		super.reloadFileList();
		loginBaiduAlbum();
	}

	//wenyongzhe 2015.9.17 跳转页面
	private Boolean isCreateAlbumComplete = false;
	@Override
	public void createAlbumComplete(boolean success) {
		//TODO Auto-generated method stub
		super.createAlbumComplete(success);
		if(success){
			isCreateAlbumComplete = true;
		}
	}
	//wenyongzhe 2015.9.17 跳转页面
	
	@Override
	public void auroraOnItemClick(AdapterView<?> parent, View view,
			int position, long id) {
		super.auroraOnItemClick(parent, view, position, id);
		if (position == 0) {
			getOperationUtil().setMoveCreateAlbum(true);
			getOperationUtil().createAlbum(R.string.aurora_create_album,getCloudActivity());
			return;
		}
		if(size!=concurrentHashMap.size()){
			LogUtil.d(TAG, "size!=concurrentHashMap.size()::"+(size!=concurrentHashMap.size()));
			return;
		}
		CommonFileInfo fileInfo = (CommonFileInfo) adapter.getItem(position);
		getCloudActivity().setFileInfo(fileInfo);
		getCloudActivity().setItemFileInfos(concurrentHashMap.get(fileInfo));
		getCloudActivity().selectPage(CloudActivity.CLOUDITEM);

	}

	@Override
	public boolean auroraOnItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		if(isOperationFile()){
			return true;
		}
		if (position == 0) {
			return true;
		}
		if(album!=null&&screenshot!=null&&getImageInfos().size()==2){//wenyongzhe 2015.10.16 diable screenshot
			return true;
		}
		
		if(position>1){//wenyongzhe 2015.10.16 diable screenshot
			getSelectImages().add((CommonFileInfo) adapter.getItem(position));
		}
		setOperationFile(true);
		updateAuroraitemActionBarState();
		updateAuroraItemBottomBarState(false);
		showOrHideMenu(true);

		return true;
	}

	@Override
	public void onRightClick() {
		super.onRightClick();
		if (album != null) {
			getSelectImages().remove(album);
		}
		if (screenshot != null) {
			getSelectImages().remove(screenshot);
		}
		updateAuroraitemActionBarState();
	}

	@Override
	public void delComplete(boolean success) {
		super.delComplete(success);
		if (success) {
			cancelOperation();
			adapter.notifyDataSetChanged();
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_del_ok);
		} else {
			ToastUtils.showTast(getCloudActivity(), R.string.aurora_del_error);
		}
		showLoadingImage(false);

	}
	
	
	//wenyongzhe 2015.10.31 start
	@Override
	public void baiduTaskStatus(final FileTaskStatusBean bean) {
		if (!isAdded()) {
			return;
		}
		DownloadTaskListManager manager = ((GalleryAppImpl) getCloudActivity()
				.getApplication()).getDownloadTaskListManager();
		manager.updateDownloadStatus(bean);
		if (manager.getDownloadTaskSize() == 0) {
			downloadNotification();
			sendBroadcastScan();
		}
	}
	private void downloadNotification() {
		if (Utils.isRunningForeground(getCloudActivity())) {
			ToastUtils.showTast(getCloudActivity(),
					R.string.aurora_download_complete);
			return;
		}
		NotificationManager mNotifManager = (NotificationManager) getCloudActivity()
				.getSystemService(Context.NOTIFICATION_SERVICE);
		final Notification.Builder builder = new Notification.Builder(
				getCloudActivity());
		builder.setWhen(System.currentTimeMillis());
		builder.setAutoCancel(true);
		builder.setSmallIcon(android.R.drawable.stat_sys_download_done);
		builder.setContentTitle(getString(R.string.aurora_download_complete));
		Notification notification = builder.build();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		mNotifManager.notify(0, notification);
	}
	public void sendBroadcastScan() {
		Intent intent = new Intent();
		intent.setAction(CloudItemFragment.ACTION_DIR_SCAN);
		intent.setData(Uri.fromFile(new File(Environment
				.getExternalStorageDirectory() + "/DCIM/cloud")));
		getCloudActivity().sendBroadcast(intent);
	}
	//wenyongzhe 2015.10.31 end
		
	@Override
	public void baiduUploadTaskStatus(FileTaskStatusBean bean) {
		super.baiduUploadTaskStatus(bean);
		UploadTaskListManager manager = ((GalleryAppImpl) getCloudActivity()
				.getApplication()).getUploadTaskListManager();
		/**
		 * wenyongzhe 2015.11.4 Upload Toash bug
		manager.updateFileUploadTaskInfo(bean);
		int i = manager.findTaskParcelIndex(bean);
		int total = (i != UploadTaskListManager.NOT_FOUND) ? manager
				.howManyPhotosUnderParcel(i) : 0;
		int complete = manager.getPacelComplete(i);
		if (total == 0) {
			LogUtil.d(TAG, "total is 0");
		}
		int progress = complete * 100 / total;
		if (progress == 100) {
			getBaiduAlbumUtils().getFileListFromBaidu(AlbumConfig.REMOTEPATH, true, null);

		}
		 */
		// wenyongzhe 2015.11.4 Upload Toash bug
		if (manager.getParcelSize() == 0) {
			if (com.android.gallery3d.xcloudalbum.tools.Utils.isRunningForeground(getCloudActivity()) ) {
				getBaiduAlbumUtils().getFileListFromBaidu(AlbumConfig.REMOTEPATH, true, null);
				return;
			}
		}
			
	}
}

package com.android.gallery3d.xcloudalbum;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.PopupWindow.OnDismissListener;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraMenu;
import aurora.widget.AuroraActionBar.OnAuroraActionBarBackItemClickListener;
import aurora.widget.AuroraMenuBase.OnAuroraMenuItemClickListener;

import com.aurora.utils.SystemUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.Gallery;
import com.android.gallery3d.app.GalleryAppImpl;
import com.android.gallery3d.selectfragment.XcloudMoveFragment;
import com.android.gallery3d.selectfragment.XcloudMoveFragmentUtil;
import com.android.gallery3d.util.NetworkUtil;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudItemFragment;
import com.android.gallery3d.xcloudalbum.fragment.CloudMainFragment;
import com.android.gallery3d.xcloudalbum.fragment.BasicFragment.ISelectPageListener;
import com.android.gallery3d.xcloudalbum.inter.IBackPressedListener;
import com.android.gallery3d.xcloudalbum.tools.BaiduAlbumUtils;
import com.android.gallery3d.xcloudalbum.tools.LocalPopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.LogUtil;
import com.android.gallery3d.xcloudalbum.tools.OperationUtil;
import com.android.gallery3d.xcloudalbum.tools.PopupWindowUtil;
import com.android.gallery3d.xcloudalbum.tools.ToastUtils;
import com.android.gallery3d.xcloudalbum.tools.Utils;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager;
import com.android.gallery3d.xcloudalbum.uploaddownload.UploadTaskListManager.FileUploadTaskInfo;
import com.android.gallery3d.xcloudalbum.widget.CloudSelectPopupWindow;
import com.android.gallery3d.xcloudalbum.widget.ProgressPopupWindow;
import com.baidu.xcloud.pluginAlbum.bean.CommonFileInfo;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;
import com.baidu.xcloud.pluginAlbum.AccountProxy;

public class CloudActivity extends AuroraActivity implements
		ISelectPageListener {

	private static final String TAG = "CloudActivity";
	private Fragment currentFragment;
	private int currentPage;
	private OperationUtil operationUtil;

	//wenyongzhe start
	private int currentfragementFlag = 0;
//	private PopupWindowUtil popupWindowUtil;
	private XcloudMoveFragmentUtil mFragmentUtil;
	//wenyongzhe end

	private boolean mNeedFinish = false;//paul add

	//wenyongzhe start
//	public PopupWindowUtil getPopupWindowUtil() {
//		return popupWindowUtil;
//	}
	public XcloudMoveFragmentUtil getmFragmentUtil() {
		return mFragmentUtil;
	}
	public int getCurrentfragementFlag() {
		return currentfragementFlag;
	}
	public void setCurrentfragmentFlag(int currentfragementFlag) {
		this.currentfragementFlag = currentfragementFlag;
	}
	//wenyongzhe end

	/**
	 * item title data
	 */
	private CommonFileInfo fileInfo;

	public CommonFileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(CommonFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	/**
	 * item icon data
	 */
	private ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap;

	public ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> getConcurrentHashMap() {
		return concurrentHashMap;
	}

	public void setConcurrentHashMap(
			ConcurrentHashMap<CommonFileInfo, List<CommonFileInfo>> concurrentHashMap) {
		this.concurrentHashMap = concurrentHashMap;
	}

	/**
	 * album data
	 */
	private List<CommonFileInfo> albumInfos;

	public List<CommonFileInfo> getAlbumInfos() {
		return albumInfos;
	}

	public void setAlbumInfos(List<CommonFileInfo> albumInfos) {
		this.albumInfos = albumInfos;
	}

	/**
	 * item data
	 */
	private List<CommonFileInfo> itemFileInfos;

	public List<CommonFileInfo> getItemFileInfos() {
		return itemFileInfos;
	}

	public void setItemFileInfos(List<CommonFileInfo> itemFileInfos) {
		this.itemFileInfos = itemFileInfos;
	}
	
	private boolean fragmentBackRefresh;
	

	public boolean isFragmentBackRefresh() {
		return fragmentBackRefresh;
	}

	public void setFragmentBackRefresh(boolean fragmentBackRefresh) {
		this.fragmentBackRefresh = fragmentBackRefresh;
	}

	public OperationUtil getOperationUtil() {
		return operationUtil;
	}

	public static final int CLOUDMAIN = 11;
	public static final int CLOUDITEM = 12;
	//wenyongzhe
	public static final int XCLOUDMOVEPHOTOFRAGEMENT = 13;

	public Fragment getCurrentFragment() {
		return currentFragment;
	}

	public void setCurrentFragment(Fragment currentFragment, int page) {
		this.currentFragment = currentFragment;
		this.currentPage = page;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Utils.showNetSpeed(CloudActivity.this);
			handler.sendEmptyMessageDelayed(0, 200);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//wenyongzhe2015.9.30 start
//		setAuroraContentView(R.layout.aurora_xclould_main,
//				AuroraActionBar.Type.Normal, false);
		setAuroraPicContentView(R.layout.aurora_xclould_main);
		//wenyongzhe2015.9.30 end
		
		getAuroraActionBar().setmOnActionBarBackItemListener(
				actionBarBackItemClickListener);
		operationUtil = new OperationUtil(this);

		//wenyongzhe2015.9.30 start
//		popupWindowUtil = PopupWindowUtil.getInstance(this);
//		popupWindowUtil.setActivity(CloudActivity.this);
		mFragmentUtil = XcloudMoveFragmentUtil.getInstance(this);
		mFragmentUtil.setActivity(CloudActivity.this);
		//wenyongzhe2015.9.30 end

		localPopupWindowUtil = new LocalPopupWindowUtil(this);
		selectPage(CLOUDMAIN);
		LogUtil.d(TAG, "density::::"
				+ getResources().getDisplayMetrics().density+" CloudActivity:::"+CloudActivity.this);
		// handler.sendEmptyMessage(1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//wenyongzhe 2015.11.5 animation disable click
		if(this.getAuroraActionBar().auroraIsEntryEditModeAnimRunning() || this.getAuroraActionBar().auroraIsExitEditModeAnimRunning()){
			return true;
		}
				
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LogUtil.d(TAG, "KEYCODE_BACK");
			onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * actionBar返回监听
	 */
	private OnAuroraActionBarBackItemClickListener actionBarBackItemClickListener = new OnAuroraActionBarBackItemClickListener() {

		@Override
		public void onAuroraActionBarBackItemClicked(int item) {
			if (item == -1) {
				onBackPressed();
			}
		}
	};

	@Override
	public void onBackPressed() {
		IBackPressedListener backPressedListener = (IBackPressedListener) currentFragment;
		if (backPressedListener != null && !backPressedListener.onBack()) {
			//wenyongzhe start
			if(currentFragment instanceof CloudMainFragment){
				finish();
			}
			//wenyongzhe end

			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		LogUtil.d(TAG, "onDestroy");
		//paul add start
		BaiduAlbumUtils baiduAlbumUtils = ((BasicFragment) getCurrentFragment())
				.getBaiduAlbumUtils();
		if(null != baiduAlbumUtils){
			baiduAlbumUtils.clearAfterDelete(this);
		}
		//paul add end

		//wenyongzhe
		//dismissSelectPopupWindow();
		super.onDestroy();
	}

	@Override
	public void selectPage(int page) {
		showFragment(page);
	}

	
	//wenyongzhe modify 2015.9.10 start
	private void showFragment(int page) {
		Fragment fragment = null;
		
		FragmentManager manager = getFragmentManager();
	    FragmentTransaction transaction = manager.beginTransaction();
	    CloudMainFragment mainFragment = (CloudMainFragment) manager.findFragmentByTag("CLOUDMAIN");  
	    Fragment itemFragment = manager.findFragmentByTag("CLOUDITEM"); 
	    
		switch (page) {
		case CLOUDITEM:
			setCurrentfragmentFlag(CLOUDITEM);
			fragment = new CloudItemFragment();
			transaction.add(R.id.container,fragment, "CLOUDITEM");
			transaction.hide(mainFragment);// 隐藏当前的fragment，add下一个到Activity中  
			transaction.commit();
			break;
		case CLOUDMAIN:
			setCurrentfragmentFlag(CLOUDMAIN);
			 if (mainFragment == null) {    // 先判断是否被add过  
				    fragment = new CloudMainFragment();
			        transaction.add(R.id.container, fragment, "CLOUDMAIN");
			        transaction.commit(); 
			    } else {  
			    	fragment = mainFragment;
			    	if(itemFragment !=null){
			    		transaction.remove(itemFragment);
			    	}
			        transaction.show(mainFragment).commit(); // 隐藏当前的fragment，显示下一个  
			    }  
			break;
		default:
			break;
		}
		setCurrentFragment(fragment, page);
//		FragmentManager fragmentManager = getFragmentManager();
//		Fragment old = fragmentManager.findFragmentById(R.id.container);
//		if (old != null) {
//			old.onDetach();
//		}
//		FragmentTransaction transaction = fragmentManager.beginTransaction();	
//		transaction.replace(R.id.container, fragment);
//		transaction.commitAllowingStateLoss();
		operationUtil.setOperationComplete((BasicFragment) fragment);

	}
	//wenyongzhe modify 2015.9.10 end

	public void setAuroraMenuCallBackListener() {
		setAuroraMenuCallBack(auroraMenuCallBack);
	}

	private OnAuroraMenuItemClickListener auroraMenuCallBack = new OnAuroraMenuItemClickListener() {

		@Override
		public void auroraMenuItemClick(int action) {
			if (Utils.isFastDouble()) {
				return;
			}
			switch (action) {
			case R.id.cloud_operation_delete:
				operationUtil.deleteAlbumOrPhoto();
				break;
			case R.id.cloud_operation_rename:
				operationUtil.renameAlbumName();
				break;
			case R.id.cloud_operation_downlaod:
				//wenyongzhe 2015.9.17 wifi start
				if (!NetworkUtil.checkWifiNetwork(CloudActivity.this)) {
					showDialog(mOnDownloadConfirmListener);
				}else{
					operationUtil.addDownloadTask();
					((BasicFragment) getCurrentFragment()).cancelOperation();
				}
				//wenyongzhe 2015.9.17 wifi end
				break;
			case R.id.cloud_operation_move:
				int size = ((BasicFragment) getCurrentFragment())
						.getSelectImages().size();
				String title = getString(R.string.aurora_move_picture);
				title = String.format(title, size);
				//wenyongzhe 2015.9.17 
				showXcloudMoveToFragment(title);
				break;

			default:
				break;
			}
		}

	};

	//wenyongzhe 2015.9.17 wifi start
	private Dialog mDialog;
	private void showDialog(DialogInterface.OnClickListener mOnClickListener) {
		mDialog = new AuroraAlertDialog.Builder(this)
				.setTitle(R.string.photo_edit_cancel_tip_title)
				.setMessage(R.string.aurora_cloud_download_dialog_message)
				.setPositiveButton(android.R.string.ok,
						mOnClickListener)
				.setNegativeButton(R.string.cancel, mOnDownloadCancelListener)
				.create();
		mDialog.show();
	}
	private DialogInterface.OnClickListener mOnDownloadConfirmListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			mDialog.dismiss();
			operationUtil.addDownloadTask();
			((BasicFragment) getCurrentFragment()).cancelOperation();
		}
	};
	private DialogInterface.OnClickListener mOnDownloadCancelListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			mDialog.dismiss();
		}
	};
	private DialogInterface.OnClickListener mOnUploadConfirmListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			mDialog.dismiss();
			Intent intent = new Intent(CloudActivity.this,
					Gallery.class);
			intent.putExtra(CloudItemFragment.FROM_XCLOUD_MULTLI_SELECTION, true);
			startActivityForResult(intent, CloudItemFragment.CLOUDREQUESTCODE);
		}
	};
  //wenyongzhe 2015.9.17 wifi end
    
	public void showHomeButtonView(boolean show) {

		if (show) {
			getAuroraActionBar().getHomeButton().setVisibility(View.VISIBLE);
		} else {
			getAuroraActionBar().getHomeButton().setVisibility(View.GONE);
		}
	}

	/**
	 * 设置auroraActionBar title
	 * 
	 * @param title
	 */
	public void setAuroraActionBarTitle(String title) {
		if (getAuroraActionBar() != null) {
			getAuroraActionBar().setTitle(title);
		}
	}

	public boolean isShowPopupWindow() {
		//wenyongzhe
		return mFragmentUtil.isShowPopupWindow();
	}

	private View rootView;

	public View getRootView() {
		return rootView;
	}

	public void setRootView(View rootView) {
		this.rootView = rootView;
	}

	public void showXcloudMoveToFragment(String title) {//wenyongzhe modify 2015.9.10
		List<CommonFileInfo> toAlbumInfo = new ArrayList<CommonFileInfo>(
				getAlbumInfos());
		toAlbumInfo.remove(fileInfo);

		//wenyongzhe modify 2015.9.10 popo->fragment start
//		popupWindowUtil.setActivity(CloudActivity.this);
//		popupWindowUtil.showSelectPopupWindow(title, getRootView(), toAlbumInfo);
		mFragmentUtil.setActivity(CloudActivity.this);
		mFragmentUtil.showSelectPopupWindow(title, getRootView(), toAlbumInfo);
		//wenyongzhe modify 2015.9.10 popo->fragment end
	}

	public void dismissSelectPopupWindow() {
		mFragmentUtil.dismissSelectPopupWindow();//wenyongzhe modify 2015.9.10  popo->fragment
	}

	public void setAuroraMenuVisibility(boolean visibility) {
		mFragmentUtil.setAuroraMenuVisibility(visibility);//wenyongzhe modify 2015.9.10 popo->fragment
	}
	private LocalPopupWindowUtil localPopupWindowUtil;
	

	public void showUploadProgress() {
		//localPopupWindowUtil = LocalPopupWindowUtil.getInstance(this);
		if (!localPopupWindowUtil.isShowProgressPopupWindow()) {
			localPopupWindowUtil.showProgressPopupWindow(
					getRootView());
		}
	}

	public void dismissUploadProgress() {
		if (localPopupWindowUtil!=null&&localPopupWindowUtil.isShowProgressPopupWindow()) {
			localPopupWindowUtil.dismissProgressPopupWindow();
		}
	}

	public void updateUploadProgress(FileTaskStatusBean bean) {
		
		LogUtil.d(TAG, "bean:::"+bean);
		UploadTaskListManager manager = ((GalleryAppImpl)getApplication()).getUploadTaskListManager();
    	LocalPopupWindowUtil util = localPopupWindowUtil;
    	FileUploadTaskInfo fileInfo = manager.getCurrentBeanInfo();
    	int i = manager.getCurrentTaskParcelIndex();
    	int total = manager.howManyPhotosUnderCurrentParcel();
    	int complete = manager.completeNumberUnderCurrentParcel();
    	int progress = total == 0 ?  0 : (complete * 100 / total);
    	if(fileInfo != null) {
    		util.getProgressPopupWindow().displayIconImage(fileInfo.uploadInfo);
    	}
    	if(total == 0) {
    		util.getProgressPopupWindow().setLoadStatusText("");
    	} else {
    		util.getProgressPopupWindow().setLoadStatusText(complete + "/" + total);
    	}
	    //util.getProgressPopupWindow().setLoadNumText((i+1) + "");
	    util.getProgressPopupWindow().setLoadProgressBar(progress);
	    util.getProgressPopupWindow().setTaskTitle(R.string.aurora_album_upload_status);
	    //add by JXH 2015-5-14 jump type begin
	    util.getProgressPopupWindow().setJumpDownLoadTab(false);
	    //add by JXH 2015-5-14 jump type end
	    if(manager.getParcelSize() == 0) {
	    	dismissUploadProgress();
	    	BaiduAlbumUtils baiduAlbumUtils = ((BasicFragment) getCurrentFragment())
					.getBaiduAlbumUtils();
			baiduAlbumUtils.getHttpCacheManager().removeCache(getFileInfo().path);
			baiduAlbumUtils.getFileListFromBaidu(getFileInfo().path, false, getFileInfo());
	    }
	    

	}
	
	@Override
	public void onStart() {//wenyongzhe
		registerNetworkReceiver();
		super.onStart();
	}

	@Override
	public void onStop() {//wenyongzhe
		unRegisterNetworkReceiver();
		super.onStop();
	}

	private NetworkReceiver networkReceiver;
	
	public void registerNetworkReceiver(){
		if(networkReceiver==null){
			networkReceiver = new NetworkReceiver();
		}
		IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(networkReceiver, intentFilter);
	}
	
	public void unRegisterNetworkReceiver(){
		if(networkReceiver==null){
			return;
		}
		unregisterReceiver(networkReceiver);
	}
	
	private class NetworkReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				((BasicFragment)getCurrentFragment()).updateAuroraItemBottomBarState(true);
				if(getCurrentFragment() instanceof CloudItemFragment && !getCurrentFragment().isHidden()){//wenyongzhe Bug17026  2015.10.29
					//wenyongzhe
//					((CloudItemFragment)getCurrentFragment()).changeActionBarLayout();
					changeActionBarLayout();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	//paul add start
	public void setToLogin() {
		mNeedFinish = false;
	}
	
    @Override
    public void onResume() {//wenyongzhe
        super.onResume();
		final AccountProxy proxy = AccountProxy.getInstance();
		if(null != proxy && proxy.hasLogout() && mNeedFinish){
			Log.w(TAG, "onResume :: finish");
			finish();
		}
		if(!mNeedFinish){
			mNeedFinish = true;
		}
    }
	//paul add end
    
    //wenyongzhe 2015.9.14
    public void changeActionBarLayout() {
    	if(getFileInfo()==null ){//wenyongzhe 2015.11.2 BUG 17069 NullPointerException
    		return;
    	}
    	setAuroraActionBarTitle(Utils.getPathNameFromPath(getFileInfo().path));
    	getAuroraActionBar().changeItemLayout(R.layout.aurora_album_add,
				R.id.aurora_album_add_liner);
		ImageButton imageButton = (ImageButton) getAuroraActionBar()
				.getRootView().findViewById(R.id.aurora_album_add);
		if (imageButton != null) {
			//wenyongzhe 2015.9.29
			if (!NetworkUtil.checkNetwork(this)) {
			//if (!NetworkUtil.checkWifiNetwork(this)) {
				imageButton.setClickable(false);
				return;
			}
			imageButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {//移动到新建相册
					if (getCurrentfragementFlag() == CLOUDITEM){
						// 本地相册
						
						//wenyongzhe 2015.10.9 mobile data  start
						if (NetworkUtil.checkNetwork(CloudActivity.this) && !NetworkUtil.checkWifiNetwork(CloudActivity.this)) {
							showDialog(mOnUploadConfirmListener);
							return;
						}
						//wenyongzhe 2015.10.9 mobile data  end
						
						Intent intent = new Intent(CloudActivity.this,
								Gallery.class);
						intent.putExtra(CloudItemFragment.FROM_XCLOUD_MULTLI_SELECTION, true);
						startActivityForResult(intent, CloudItemFragment.CLOUDREQUESTCODE);
					}else 	if (getCurrentfragementFlag() == XCLOUDMOVEPHOTOFRAGEMENT){
						operationUtil.setMoveCreateAlbum(true);
						operationUtil.setMoveCheckBox(true);//wenyongzh  true:move   false:copy
						operationUtil.createAlbum(R.string.aurora_move_new_album,CloudActivity.this);
					}
				}
			});
		}
	}
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		LogUtil.d(TAG, "requestCode::" + requestCode + " resultCode::"
				+ resultCode + " data::" + data);
		
		//wenyongzhe 2015.10.9 mobile data start
		if (!NetworkUtil.checkNetwork(this)) {
			ToastUtils.showTast(this, R.string.aurora_network_not_available);
			return;
		}
		//wenyongzhe 2015.10.9 mobile data  end
		
		if (requestCode == CloudItemFragment.CLOUDREQUESTCODE && resultCode == Activity.RESULT_OK) {
			ArrayList<String> temps = ((GalleryAppImpl) getApplication()).getSelectedFilesForXCloud();
			ArrayList<String> paths = new ArrayList<String>(temps);
			temps.clear();
			getOperationUtil().uploadToAlbum(paths,getFileInfo());
		}
	}
    
}

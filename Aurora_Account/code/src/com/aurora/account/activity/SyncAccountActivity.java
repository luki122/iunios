package com.aurora.account.activity;

import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraAlertDialog;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraListView;
import aurora.widget.AuroraSwitch;

import com.aurora.account.R;
import com.aurora.account.adapter.SyncListAdapter;
import com.aurora.account.bean.AppConfigInfo;
import com.aurora.account.events.EventsType;
import com.aurora.account.receiver.BatteryReceiver;
import com.aurora.account.service.ExtraFileUpService;
import com.aurora.account.service.ExtraFileUpService.SyncStatus;
import com.aurora.account.service.ModuleDataWorker;
import com.aurora.account.totalCount.TotalCount;
import com.aurora.account.upload.UploadUpdateListener;
import com.aurora.account.util.BitmapUtil;
import com.aurora.account.util.CommonUtil;
import com.aurora.account.util.FileLog;
import com.aurora.account.util.Globals;
import com.aurora.account.util.Log;
import com.aurora.account.util.SystemUtils;
import com.aurora.account.util.TimeUtils;
import com.aurora.account.util.ToastUtil;
import com.aurora.account.widget.SyncProgressView;
import com.aurora.datauiapi.data.AccountManager;
import com.aurora.datauiapi.data.bean.UserLoginObject;
import com.aurora.datauiapi.data.implement.DataResponse;

/**
 * 同步界面
 * 
 * @author jason
 * @date 2014-9-30 下午3:40:44
 */

public class SyncAccountActivity extends BaseObserverActivity implements
		OnClickListener {

	public static final String EXTRA_KEY_LOGIN_STATUS = "loginStatus";
	public static final int LOGIN_STATUS_FIRST_LOGIN = 1; // 第一次登录
	public static final int LOGIN_STATUS_NORMAL = 2; // 注销后不换帐号再登录
	public static final int LOGIN_STATUS_CHANGE_ACCOUNT_FIRST_LOGIN = 3; // 切换帐号后的第一次登录
	public static final int LOGIN_STATUS_RELOGIN = 4; // 同一个帐号注销后再次登录

	public static final String EXTRA_KEY_DIS_WARN = "disWarn";
	private static final int REQ_CODE_SEE_DETAIL = 1;
	
	public static final String EXTRA_KEY_NEED_SET_EXIT_ANIMATION = "needSetExitAnimation";
	private boolean mNeedExitAnim = false;

	private static final int AURORA_SETTING = 1;
	private AuroraActionBar mActionBar;

	private AuroraListView mListView;
	private View sync_personal;
	private ImageView account_sync_icon;
	private ImageView mIconIv;
	private TextView mNickNameTv;
//	private LinearLayout header
	private RelativeLayout rl_mypage;

	private SyncListAdapter adapter = null;
	private long clicktime = 0;
	//是否显示提醒联系人同步
	private boolean isDisWarn = false;
	private AccountManager mAccountManager;
	
	private BatteryReceiver mBatteryReceiver = new BatteryReceiver(new BatteryReceiver.OnBatteryChangedCallback() {
        @Override
        public void onBatteryChanged(BatteryReceiver receiver, int batteryScale, int batteryLevel) {
            Log.d(TAG, "Jim, onBatteryChanged enter, mPendingSync: " + mPendingSync);
            if (mPendingSync) {
                mPendingSync = false;
                
                if (receiver.checkBattery()) {
                    dotheSyncThing();
                } else {
                    Log.e(TAG, "Jim, onBatteryChanged, battery is low, do not sync.");
                }
            }
        }
    });
	
	private boolean mPendingSync = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setAuroraContentView(R.layout.account_sync_activity,
				AuroraActionBar.Type.Empty, false);

		if (handleLogout(getIntent())) {
		    return;
		}
		
		int login_status = getIntent().getIntExtra(EXTRA_KEY_LOGIN_STATUS,LOGIN_STATUS_NORMAL);
		mNeedExitAnim = getIntent().getBooleanExtra(EXTRA_KEY_NEED_SET_EXIT_ANIMATION, false);
		isDisWarn = getIntent().getBooleanExtra(EXTRA_KEY_DIS_WARN,false);
		setSwitch(login_status);
		initViews();
		initActionBar();
		setListeners();
		setAdapter();
		initData();

		mAccountManager = new AccountManager(this);
		loadUserInfo();
		
		mBatteryReceiver.registerReceiver(this);
		
		/*if (TEST) {
			setAuroraMenuListener();
			initAuroraMenu();
		}*/
		if((login_status == LOGIN_STATUS_FIRST_LOGIN) ||
		        (login_status == LOGIN_STATUS_CHANGE_ACCOUNT_FIRST_LOGIN) ||
		        (login_status == LOGIN_STATUS_RELOGIN))
		{
			new Thread()
			{

				@Override
				public void run() {
					// 推迟500毫秒开始同步（防止页面未初始化完成开始同步导致图标闪动）
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					if(SystemUtils.getCurrentNetStatus() == 0)
					{
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								ToastUtil.longToast(R.string.network_not_available);
							}
						});
						
					}
					else if (!mPref.isWifiSyncOnly() || (mPref.isWifiSyncOnly() && (SystemUtils.getCurrentNetStatus() == 1)))
					{
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
							    if (!mBatteryReceiver.canCheckBattery()) {
					                Log.d(TAG, "Jim, Battery info is not available now, wait...");
					                mPendingSync = true;
					                return;
					            }
					            
					            if (!mBatteryReceiver.checkBattery()) {
					                Log.e(TAG, "Jim, battery is low, do not sync.");
					                return;
					            }
					            
								dotheSyncThing();
							}
						});
					}
				}
				
			}.start();
		}
	}

	@Override
    protected void onDestroy() {
	    mBatteryReceiver.unregisterReceiver(this);
	    
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
      

        if (intent != null) {
        	setIntent(intent);
        	isDisWarn = getIntent().getBooleanExtra(EXTRA_KEY_DIS_WARN,false);
        	if(isDisWarn)
        	{
        		adapter.setStatus(isDisWarn);
        		adapter.notifyDataSetChanged();
        	}
        	else
        	 handleLogout(intent);
        }
    
    }
	
	private boolean handleLogout(Intent intent) {
	    int command = intent.getIntExtra(Globals.EXTRA_COMMAND, Globals.COMMAND_UNKNOW);
        if (command == Globals.COMMAND_LOGOUT) {
            finish();
            startActivity(LoginActivity.class);
            return true;
        }
        
        return false;
	}

    private void setSwitch(int login_status)
	{
		if(login_status == LOGIN_STATUS_CHANGE_ACCOUNT_FIRST_LOGIN)
		{
			List<AppConfigInfo> info = SystemUtils.getAppConfigInfo(SyncAccountActivity.this);

			for (int j = 0; j < info.size(); j++) 
			{
				SystemUtils.updateAppSwitch(SyncAccountActivity.this,info.get(j).getApp_packagename(), true);
			}
	
		}
	
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		ExtraFileUpService.registerUpdateListener(updateListener);
		
		if (updateListener != null) {
			updateListener.downloadProgressUpdate();
		}
	}

	@Override
	protected void onPause() {
		mListView.auroraOnPause();
		if(isDisWarn)
		{
			isDisWarn = false;
			adapter.setStatus(isDisWarn);
			adapter.notifyDataSetChanged();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mListView.auroraOnResume();
		
		super.onResume();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		
		if (updateListener != null) {
			updateListener.downloadProgressUpdate();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		ExtraFileUpService.unRegisterUpdateListener(updateListener);
	}

	@Override
    public void finish() {
        super.finish();
        if (mNeedExitAnim) {
            overridePendingTransition(
                    com.aurora.R.anim.aurora_activity_close_enter,
                    com.aurora.R.anim.aurora_activity_close_exit);
        }
    }
	
	private void initActionBar() {
		mActionBar = getAuroraActionBar();
		mActionBar.addItem(R.layout.actionbar_setting, AURORA_SETTING);
		FrameLayout settingBtn = (FrameLayout) mActionBar
				.findViewById(R.id.setting_layout);
		settingBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Intent i = new Intent(SyncAccountActivity.this, SyncSettingActivity.class);
				startActivity(i);
			}
		});
	}
	
	private UploadUpdateListener updateListener = new UploadUpdateListener() {
		@Override
		public void downloadProgressUpdate() {
			Log.i(TAG, "SyncAccountActivity downloadProgressUpdate()");
			
			/*
			 * if (adapter != null) { // adapter.notifyDataSetChanged();
			 * adapter.updateView(); }
			 */
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					
					int headerCount = mListView.getHeaderViewsCount();
					int firstVisiblePosition = mListView.getFirstVisiblePosition();
					int offset = headerCount - firstVisiblePosition;
					boolean containerHeader = false;
					if (headerCount > 0) {
						if (firstVisiblePosition < headerCount) {
							containerHeader = true;
						}
					}
					int count = mListView.getChildCount();
					
					for (int i = 0; i < count; i++) {
						int position = 0;
						if (containerHeader) {
							if (i < offset) {
								continue;
							}
							position = i - offset;
						} else {
							position = i + firstVisiblePosition - headerCount;
						}
						
						List<AppConfigInfo> apps = SystemUtils
								.getAppConfigInfo(SyncAccountActivity.this);
						
						if (position >= apps.size()) {
							continue;
						}
						
						View v = mListView.getChildAt(i);
						SyncProgressView spv_app = (SyncProgressView) v.findViewById(R.id.spv_app);
						TextView tv_sync_time = (TextView) v.findViewById(R.id.tv_sync_time);
						ImageView iv_icon = (ImageView) v.findViewById(R.id.iv_icon);
						AuroraSwitch as_switch = (AuroraSwitch) v.findViewById(R.id.wlan_switch);

						int module_index = ExtraFileUpService.getModule_index();

						AppConfigInfo appConfigInfo = apps.get(position);
						
						// 是否应用自身完成同步
						if (!appConfigInfo.isApp_syncself()) {
							
							spv_app.setVisibility(View.VISIBLE);
							iv_icon.setVisibility(View.GONE);
							
							if (module_index == position
									&& appConfigInfo.isSync()
									&& ExtraFileUpService.getSyncStatus() != SyncStatus.SYNC_STATUS_DONE
									&& ExtraFileUpService.getSyncStatus() != SyncStatus.SYNC_STATUS_NOT_STARTED) { // 当前同步模块
								
								// int status = spv_app.getStatus();
								// if (status == SyncProgressView.STATUS_NORMAL) {
								spv_app.startProgress();
								// }
								
								int currentIndex = 0;
								int totalIndex = 0;
								if (ExtraFileUpService.getSync_type() == 0) {
									currentIndex = ExtraFileUpService
											.getM_currentIndex();
								} else {
									currentIndex = ExtraFileUpService
											.getM_currentIndex()
											+ ExtraFileUpService
											.getM_uptotalcount();
								}
								totalIndex = ExtraFileUpService
										.getM_downtotalcount()
										+ ExtraFileUpService.getM_uptotalcount();
								int progress = 0;
								if (totalIndex != 0) {
									progress = currentIndex * 100 / totalIndex;
								}
								Log.i(TAG, "zhangwei the progress="+progress+" the currentIndex="+currentIndex+ " totalIndex ="+totalIndex);
								spv_app.setProgressAnim(progress);
								
							} else { // 非当前同步模块
								if (appConfigInfo.isSync()) {
									
									int status = spv_app.getStatus();
									if (status == SyncProgressView.STATUS_PROGRESS) {
										spv_app.endProgress();
									} else {
										spv_app.setStatus(SyncProgressView.STATUS_NORMAL);
									}
									
								} else {
									spv_app.setStatus(SyncProgressView.STATUS_NORMAL);
								}
								// int status = spv_app.getStatus();
								// if (status == SyncProgressView.STATUS_PROGRESS) {
//								spv_app.endProgress();
								// }
							}
							
						} else {
							
							spv_app.setVisibility(View.GONE);
							iv_icon.setVisibility(View.VISIBLE);
							
						}
						
						int strId = R.string.account_sync_time;
						if (appConfigInfo.isApp_syncself()) {
							strId = R.string.account_backup_time;
						}
						
						String time = "";
						long timeL = appConfigInfo.getSyncTime();
						if (timeL == 0) {
							time = getString(strId,
									TimeUtils.getDistanceTime(String.valueOf(timeL)));
						} else {
							time = getString(strId, TimeUtils
									.getDistanceTime(TimeUtils.getDateTimeFromLong(timeL)));
						}
						tv_sync_time.setText(time);
						
					}
					
					// 刷新同步按钮
					if (ExtraFileUpService.getSyncStatus() != SyncStatus.SYNC_STATUS_SYNCING) {
						if (account_sync_icon != null) {
							account_sync_icon.clearAnimation();
						}
					} else {
					    if (account_sync_icon != null) {
					    	account_sync_icon.clearAnimation();
					        account_sync_icon.startAnimation(BitmapUtil.createRotateAnimation(false));
					    }
					}
				}
			});

		}
	};

	@Override
	protected String getActionBarTitle() {
		return getString(R.string.app_name);
	}

	private void initViews() {
		mListView = (AuroraListView) findViewById(R.id.lv_app);
		sync_personal = (View) findViewById(R.id.sync_personal);
		
		mListView.auroraEnableOverScroll(false);

		account_sync_icon = (ImageView) findViewById(R.id.account_sync_icon);
		mIconIv = (ImageView) findViewById(R.id.account_icon_iv);
		mNickNameTv = (TextView) findViewById(R.id.nick_name_tv);
		// expandViewTouchDelegate();
		SystemUtils.expandViewTouchDelegate(account_sync_icon, 40, 40, 40, 40);
		
		mNickNameTv.setAlpha(Float.parseFloat(getResources().getString(R.string.account_sync_name_text_alpha)));
		
//		mListView.setOnItemClickListener(new OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View arg1, int position,
//					long arg3) {
//				
//				// 当有相册时
//				List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(SyncAccountActivity.this);
//				int p = position - mListView.getHeaderViewsCount();
//				if (p >= 0 && apps.get(p).getApp_packagename().equals(Globals.GALLERY_PACKAGE_NAME)) {
//					Intent i = new Intent(SyncAccountActivity.this, PhotoBackupActivity.class);
//					startActivity(i);
//				}
//			}
//		});
		
		rl_mypage = (RelativeLayout) findViewById(R.id.rl_mypage);
	}

	private void initData() {
		setUserNick();
		Bitmap icon = IconLoader.loadLocalCacheIcon();
		if (icon != null) {
			mIconIv.setImageBitmap(icon);
		}
	}

	private void setUserNick() {
		String nick = mPref.getUserNick();
		if (TextUtils.isEmpty(nick)) {
			nick = mPref.getUserPhone();
			if (TextUtils.isEmpty(nick)) {
				nick = mPref.getUserEmail();
			}
		}
		mNickNameTv.setText(nick);
	}

	private static class IconLoader extends PhotoLoader<SyncAccountActivity> {
		public IconLoader(SyncAccountActivity target) {
			super(target);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			SyncAccountActivity target = mTarget.get();
			if (target != null) {
				if (mErrorMsgResId != -1) {
					ToastUtil.shortToast(mErrorMsgResId);
					return;
				}

				if (result != null) {
					target.mIconIv.setImageBitmap(result);
				}
			}
		}
	}

	public static void loadLatestPhoto(SyncAccountActivity target) {
		new IconLoader(target).loadLatestPhoto();
	}

	/**
	 * 从服务器查询最新的账户信息
	 */
	private void loadUserInfo() {
		// showProgressDialog(getString(R.string.account_info_get_user_info));
		mAccountManager.getUserInfo(new DataResponse<UserLoginObject>() {
			public void run() {
				// dismissProgressDialog();
				if (value != null) {
					Log.i(TAG, "Jim, code: " + value.getCode());
					if (value.getCode() == UserLoginObject.CODE_SUCCESS) {
						loadLatestPhoto(SyncAccountActivity.this);
					} else {
						Log.d(TAG, "Jim, error: " + value.getDesc());
						ToastUtil
								.shortToast(R.string.account_sync_error_failed_to_get_info);
					}
				}
			}
		}, this, mPref.getUserID(), mPref.getUserKey());
	}

	/**
	 * @Title: setAdapter
	 * @param
	 * @return void
	 * @throws
	 */
	private void setAdapter() {

		List<AppConfigInfo> apps = SystemUtils.getAppConfigInfo(this);

		adapter = new SyncListAdapter(this, apps,isDisWarn);

		mListView.setAdapter(adapter);

	}
	
	private void startSync() {
	    ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_SYNCING);
        
        // 显示通知栏同步动画
        CommonUtil.showSyncNotification(SyncAccountActivity.this);
        FileLog.i(TAG, "  call showSyncNotification in dotheSyncThing()");
        
        account_sync_icon.clearAnimation();
        account_sync_icon.startAnimation(BitmapUtil
                .createRotateAnimation(false));
        if ((ExtraFileUpService.getCurrent_status() == ExtraFileUpService.OPERATION_PAUSE_DOWNLOAD)
                || (ExtraFileUpService.getCurrent_status() == ExtraFileUpService.OPERATION_PAUSE_UPLOAD)) {
            CommonUtil.clearResumeSyncAlarm(); // 手动恢复了暂停，清除之前设置的恢复同步的闹铃服务
            ExtraFileUpService.continueOperation(SyncAccountActivity.this);
        } else {
            ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_SYNCING);
            ModuleDataWorker worker = new ModuleDataWorker(SyncAccountActivity.this, 0);
            new Thread(worker).start();
        }
	}

	
	private void dotheSyncThing()
	{
		if(!SystemUtils.isSyncExits(this))
			return;
		if (!ExtraFileUpService.isSyncing()) {		    
		    if(mPref.isWifiSyncOnly() &&(SystemUtils.getCurrentNetStatus() != 1)) {
                new AuroraAlertDialog.Builder(SyncAccountActivity.this)
                .setTitle(R.string.dialog_prompt)
                .setMessage(R.string.account_sync_promit_confirm)
                .setPositiveButton(R.string.dialog_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                startSync();
                            }
                        }).setNegativeButton(R.string.dialog_cancel, null)
                .create().show();
            } else {
                startSync();
            }		    
		} else {
			// ExtraFileUpService.setSyncStatus(SyncStatus.SYNC_STATUS_PAUSED);
			account_sync_icon.clearAnimation();
			ExtraFileUpService.pauseOperation(SyncAccountActivity.this,0);
			CommonUtil.setResumeSyncAlarm(); // 设置恢复同步的闹铃服务
		}

	}
	
	private void setListeners() {
		rl_mypage.setOnClickListener(this);
		
		sync_personal.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SyncAccountActivity.this,
						AccountInfoActivity.class);
				startActivityForResult(intent, REQ_CODE_SEE_DETAIL);
			}
		});

		account_sync_icon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				long currenttime = System.currentTimeMillis();
				if(currenttime - clicktime < 2000)
				{
					return;
				}
				else
				{
					clicktime = currenttime;
				}
				//add by zw 02-13 计算点击同步按钮的次数
				new TotalCount(SyncAccountActivity.this, "280", "001", 1).CountData();
				//end by zw
				if(SystemUtils.getCurrentNetStatus() == 0)
				{
					ToastUtil.longToast(R.string.network_not_available);
				} else {
					dotheSyncThing();
				}
				
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_mypage:
			//Toast.makeText(SyncAccountActivity.this, "打开黄页", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent();
			intent.setAction("yulore.personnal");
			startActivity(intent);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQ_CODE_SEE_DETAIL:
			if (resultCode == RESULT_OK && data != null) {
			    handleLogout(data);
			}
			break;
		default:
			super.onActivityResult(requestCode, resultCode, data);
			break;
		}
	}
	
	@Override
	protected boolean shouldHandleFocusChanged() {
	    return false;
	}

	@Override
	protected void onChange(String eventType) {
		if (EventsType.EVENT_UPDATE_NICKNAME.equals(eventType)) {
			setUserNick();
		} else if (EventsType.EVENT_UPDATE_ACCOUNT_ICON.equals(eventType)) {
			loadLatestPhoto(SyncAccountActivity.this);
		}
	}

	@Override
	protected String[] getObserverEventType() {
		return new String[] { EventsType.EVENT_UPDATE_NICKNAME,
				EventsType.EVENT_UPDATE_ACCOUNT_ICON };
	}

	// TODO: 删掉测试代码
	// ***************** 测试代码 *****************
	/*private static final boolean TEST = true;
	public static String AUTO_SYNC_TIME = null;

	private void setAuroraMenuListener() {
		setAuroraMenuCallBack(new OnAuroraMenuItemClickListener() {
			@Override
			public void auroraMenuItemClick(int menuItemId) {
				switch (menuItemId) {
				case R.id.set_auto_sync_time:
					setAutoSyncTime();
					break;
				}
			}
		});
	}

	private void setAutoSyncTime() {
		View view = LayoutInflater.from(this).inflate(
				R.layout.set_auto_sync_time, null);
		final AuroraEditText editText = (AuroraEditText) view
				.findViewById(R.id.set_auto_sync_time_et);
		new AuroraAlertDialog.Builder(this).setTitle("手动设置自动同步时间")
				.setView(view)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String strAutoSyncTime = editText.getText().toString()
								.trim();
						if (!TextUtils.isEmpty(strAutoSyncTime)
								&& strAutoSyncTime.matches("\\d{2}:\\d{2}")) {
							AUTO_SYNC_TIME = strAutoSyncTime;
						} else {
							AUTO_SYNC_TIME = null;
						}
						CommonUtil.setAutoSyncAlarm();
					}
				}).setNegativeButton("取消", null).create().show();
	}

	private void initAuroraMenu() {
		setAuroraMenuItems(R.menu.sync_account_set_auto_sync_time_menu);
	}*/
	// ***************** 测试代码 *****************
	// ====================加大按钮点击区域start====================//
	
	// 20150821 back键按home处理
//	@Override  
//    public boolean onKeyDown(int keyCode, KeyEvent event) {  
//        if (keyCode == KeyEvent.KEYCODE_BACK) {  
//            moveTaskToBack(false);  
//            return true;  
//        }  
//        return super.onKeyDown(keyCode, event);  
//    }  

}
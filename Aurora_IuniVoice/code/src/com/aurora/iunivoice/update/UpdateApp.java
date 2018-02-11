package com.aurora.iunivoice.update;

import java.io.File;
import java.util.List;

import android.app.ActivityManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.datauiapi.data.bean.AppUpgradeInfo;
import com.aurora.datauiapi.data.bean.AppUpgradeObject;
import com.aurora.datauiapi.data.implement.DataResponse;
import com.aurora.iunivoice.IuniVoiceApp;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.activity.AppActivity;
import com.aurora.iunivoice.activity.MainActivity;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.FilePathUtil;
import com.aurora.iunivoice.utils.SystemUtils;
import com.aurora.iunivoice.utils.TimeUtils;
import com.aurora.iunivoice.utils.ToastUtil;
import com.aurora.iunivoice.widget.app.AuroraAlertDialog;
import com.aurora.iunivoice.widget.app.AuroraProgressDialog;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.HttpHandler;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

/**
 * @author lmjssjj
 */
public class UpdateApp {
	private int mForceUpFlag;
	private AuroraProgressDialog mProgressDialog;

//	private Stirng
	
	private static final int STATE_DOWNLOADING = 1;
	private static final int STATE_CHECKING = 2;
	private static final int STATE_NONE = 0;
	private static int mCurrentState = STATE_NONE;
	
	private static boolean NETWORK_ERROR = true;

	private boolean isReg = false;
	private static UpdateApp instance = null;

	private UpdateApp() {}

	public static UpdateApp getInstance() {
		if (instance == null) {
			instance = new UpdateApp();
		}
		return instance;
	}

	/**
	 * 检查更新
	 */
	public void checkUpdateApp(final Context context,
			final boolean checkUpdate, IuniVoiceManager manager) {

		if (!SystemUtils.hasNetwork()) {
			ToastUtil.shortToast(R.string.network_not_available);
			return;
		}
		if (mCurrentState == STATE_CHECKING) {
			ToastUtil.shortToast(R.string.update_checking);
			return;
		}
		if (mCurrentState == STATE_DOWNLOADING&&checkUpdate) {
			ToastUtil.shortToast(R.string.updating);
			return;
		}
		mCurrentState = STATE_CHECKING;

		final SharedPreferences sp = context.getSharedPreferences("version",
				Context.MODE_APPEND);
		// 最后检查版本更新成功的时间
		String lastCheckVersionTime = sp
				.getString("lastcheckversiontime", "-1");
		// 当前的时间
		String currentTime = TimeUtils.getStringDateShort();
		if (!checkUpdate) {
			if (currentTime.equals(lastCheckVersionTime)) {
				
//				mCurrentState = STATE_NONE;
//				return;
				
			}
		} else {
			mCheckDialog = DialogUtil.getLoadingDialog(context, context
					.getResources().getString(R.string.update_checking));
			mCheckDialog.setCanceledOnTouchOutside(false);
			mCheckDialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					mCurrentState = STATE_NONE;
				}
			});
			mCheckDialog.show();
		}
		// 当前应用的版本
		final int currentVersion = SystemUtils.getVersionCode(context,
				context.getPackageName());
		// 获取网络的版本信息
		manager.checkVersion(new DataResponse<AppUpgradeObject>() {
		

			@Override
			public void run() {
				//mCurrentState = STATE_CHECKING;
				NETWORK_ERROR = false;
				if (value != null) {
					// 保存访问成功的时间
					sp.edit()
							.putString("lastcheckversiontime",
									TimeUtils.getStringDateShort()).commit();
					List<AppUpgradeInfo> upgradeAppList = value
							.getUpgradeApps();
					AppUpgradeInfo appInfo = null;
					if (upgradeAppList != null && upgradeAppList.size() > 0) {
						appInfo = upgradeAppList.get(0);
					}
					if (checkUpdate) {
						if (mCheckDialog != null && mCheckDialog.isShowing()) {
							mCheckDialog.dismiss();
						}
					}
					if (appInfo == null) {
						if (checkUpdate) {
							ToastUtil.shortToast(R.string.toast_no_new_version);
						}
						mCurrentState = STATE_NONE;

						return;
					}
					final AppUpgradeInfo info = appInfo;
					mForceUpFlag = info.getForceUpFlag();
					// 有新版本
					if (appInfo.getVersionCode() > currentVersion) {

						StringBuilder sb = new StringBuilder();
						sb.append("\n");
						sb.append(context.getResources().getString(
								R.string.new_version));
						sb.append(info.getVersionName());
						sb.append("V");
						sb.append("\n");
						sb.append(info.getDescription());

						AuroraAlertDialog alertDialog = (AuroraAlertDialog) DialogUtil
								.getAlertDialog(context, sb.toString(),
										R.string.cancel, R.string.update,
										new DialogUtil.IAlertDialogClick() {

											@Override
											public void sureClick() {
												downloadDialog(context,
														info.getDownloadURL(),
														info.getTitle());
											}

											@Override
											public void cancelClick() {
												mCurrentState = STATE_NONE;
											//	 是否强制更新
												 if (info.getForceUpFlag()==1) {
													 IuniVoiceApp.getInstance().exit();
												 }
											}
										});
						alertDialog.setTitle(R.string.dialog_update_title);
						alertDialog.setCanceledOnTouchOutside(false);
						if(info.getForceUpFlag()==1){
							alertDialog.setCancelable(false);
						}
						alertDialog.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								mCurrentState = STATE_NONE;
							}
						});
						alertDialog.show();
					}
				} else {
					mCurrentState = STATE_NONE;
					if(mCheckDialog!=null&&mCheckDialog.isShowing()){
						mCheckDialog.dismiss();
					}
				}

			}
		}, context);
		
		//mCurrentState = STATE_NONE;
		if(NETWORK_ERROR){
			mCurrentState = STATE_NONE;
			if(mCheckDialog!=null&&mCheckDialog.isShowing()){
				mCheckDialog.dismiss();
			}
		}

	}

	/**
	 * 下载对话框
	 * 
	 * @param context
	 */
	private void downloadDialog(Context context, String url, String appname) {

		download(context, url, appname);

		mProgressDialog = (AuroraProgressDialog) DialogUtil.getProgressDialog(
				context, context.getResources().getString(R.string.updating),
				"",
				context.getResources().getString(R.string.background_udpdate),
				new DialogUtil.IAlertDialogItemClick() {

					@Override
					public void onClick(int which) {
						mProgressDialog.dismiss();
					}
				},mForceUpFlag==0);
		// mProgressDialog = new AuroraProgressDialog(context);
		// mProgressDialog.setTitle("应用升级");
		// mProgressDialog.setMessage("正在更新...");
		// mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// mProgressDialog.setMax(100);
		// // auroraProgressDialog.setProgress(value);
		// mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "后台更新",
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// }
		// });
		mProgressDialog.show();
	}

	/**
	 * 多线程的下载器
	 * 
	 * @param downloadurl
	 */
	public void download(final Context context, String downloadurl,
			String filename) {

		if (TextUtils.isEmpty(filename)) {
			filename = System.currentTimeMillis() + "";
		}
		// 获取应用存放位置
		final String filePath = FilePathUtil.getAPKFilePath(context) + filename
				+ ".apk";

		HttpUtils http = new HttpUtils();
		register(context);
		HttpHandler<File> download = http.download(downloadurl, filePath,
				new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						cancleDownloadingNotify(context);
						unRegister(context);
						mCurrentState = STATE_NONE;
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");
						intent.setDataAndType(Uri.fromFile(new File(filePath)),
								"application/vnd.android.package-archive");
						context.startActivity(intent);
					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						mCurrentState = STATE_NONE;
						
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog.dismiss();
						}
						unRegister(context);
						Toast.makeText(context, R.string.update_error, 0)
								.show();
						System.out.println(arg1);
						arg0.printStackTrace();
					}

					@Override
					public void onLoading(long total, long current,
							boolean isUploading) {
						super.onLoading(total, current, isUploading);
						mCurrentState = STATE_DOWNLOADING;
						//register(context);
						sendDownloadingNotify(context, total, current);
						if (mProgressDialog != null
								&& mProgressDialog.isShowing()) {
							mProgressDialog
									.setProgress((int) (current * 100 / total));
						}
					}
				});
	}

	private Notification notification;

	/**
	 * 发送正在下载的广播
	 * 
	 */
	private void sendDownloadingNotify(Context context, long total, long current) {

		int progress = (int) ((current * 1.0f) / total * 100);

		if (notification == null) {
			notification = new Notification();
			notification.icon = android.R.drawable.stat_sys_download;
			notification.contentView = new RemoteViews(
					context.getPackageName(), R.layout.notification_download_2);
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			Intent intent  = new Intent(context,MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			notification.contentIntent = pendingIntent;
		}
		notification.contentView.setTextViewText(R.id.title, context
				.getResources().getString(R.string.notification_title));
		notification.contentView.setTextViewText(R.id.progress, progress + "%");
		notification.contentView.setProgressBar(R.id.progress_bar, 100,
				progress, false);
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
	}

	/**
	 * 取消正在下载广播
	 * 
	 */
	private void cancleDownloadingNotify(Context context) {
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
	}

	public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	private NetWorkReceiver netWorkReceiver;

	private Dialog mCheckDialog;

	public void register(Context context) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(CONNECTIVITY_CHANGE_ACTION);
		if (netWorkReceiver == null) {
			netWorkReceiver = new NetWorkReceiver();
			context.registerReceiver(netWorkReceiver, filter);
			isReg = true;
		}
	}

	public synchronized void unRegister(Context context) {
		if (netWorkReceiver != null && isReg) {
			context.unregisterReceiver(netWorkReceiver);
			isReg = false;
			netWorkReceiver = null;
		}
	}

	private class NetWorkReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (SystemUtils.hasNetwork()) {

			} else {
				cancleDownloadingNotify(context);
			}
		}
	}
}

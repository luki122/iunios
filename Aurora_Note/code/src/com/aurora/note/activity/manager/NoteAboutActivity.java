package com.aurora.note.activity.manager;

import java.util.List;

import com.aurora.note.R;
/*import com.aurora.note.data.Command;
import com.aurora.note.data.DataResponse;
import com.aurora.note.data.ManagerThread;
import com.aurora.note.data.NoteManager;
import com.aurora.note.data.bean.UpgradeAppInfo;
import com.aurora.note.data.bean.UpgradeAppsObject;
import com.aurora.note.data.interf.INotifiableController;
import com.aurora.note.data.interf.INotifiableManager;
import com.aurora.note.download.ProgressUtil;
import com.aurora.note.model.DownloadData;*/
import com.aurora.note.util.Globals;
import com.aurora.note.util.SystemUtils;
import com.aurora.note.util.TimeUtils;
import com.aurora.note.util.ToastUtil;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
// import aurora.lib.app.AuroraProgressDialog;
import aurora.widget.AuroraActionBar;

public class NoteAboutActivity extends AuroraActivity implements View.OnClickListener/*, INotifiableController*/ {

	public static final int NETWORK_ERROR = 501;

	private Context mContext;
	// private NoteManager mManager;
	// private ManagerThread mThread;
	private AuroraAlertDialog mDialog;
	// private AuroraProgressDialog progressDialog;
	// private DownloadData mDownloadData = new DownloadData();

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case NETWORK_ERROR:
				/*if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}*/
				ToastUtil.shortToast(R.string.toast_network_error);
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = this;
		/*mManager = new NoteManager();
		mThread = new ManagerThread(mManager);
		mThread.init(this)*/;

		setAuroraContentView(R.layout.note_about_activity, AuroraActionBar.Type.Normal);
		getAuroraActionBar().setTitle(R.string.note_about);

		initView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		/*mThread.quit();
		mManager.setController(null);*/
	}

	private void initView() {
		TextView noteVersionView = (TextView) findViewById(R.id.note_version);
		noteVersionView.setText(getString(R.string.app_name) + " V" + SystemUtils.getVersionName(this, getPackageName()));
		TextView checkUpdateView = (TextView) findViewById(R.id.check_update);
		checkUpdateView.setOnClickListener(this);
		TextView knowMoreView = (TextView) findViewById(R.id.know_more);
		knowMoreView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.check_update:
			// doCheckUpdate();
			break;
		case R.id.know_more:
			gotoKonwMore();
			break;
		}
	}

	private void gotoKonwMore() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse("http://www.iunios.com"));
		startActivity(intent);
	}

	/*private void doCheckUpdate() {
		if (!SystemUtils.isNetworkConnected()) {
			ToastUtil.shortToast(R.string.toast_no_network);
			return;
		}

		progressDialog = AuroraProgressDialog.show(mContext, null, getResources().getString(R.string.note_check_doing), true, false);
		progressDialog.show();

		mManager.checkVersion(new DataResponse<UpgradeAppsObject>() {
			@Override
			public void run() {
				if (value != null) {
					if (progressDialog != null && progressDialog.isShowing()) {
						progressDialog.dismiss();
					}

					SharedPreferences sp = mContext.getSharedPreferences(Globals.SHARE_PREF_NAME, Context.MODE_PRIVATE);
					sp.edit().putString(Globals.PREF_CHECK_VERSION_TIME, TimeUtils.getStringDateShort());
					sp.edit().commit();

					List<UpgradeAppInfo> upgradeAppList = value.getUpgradeApps();
					UpgradeAppInfo appInfo = null;
					if (upgradeAppList != null && upgradeAppList.size() > 0) {
						appInfo = upgradeAppList.get(0);
					}
					if (appInfo == null) {
						ToastUtil.shortToast(R.string.toast_no_new_version);
						return;
					}

					setDownloadData(appInfo);

					sp.edit().putInt(Globals.PREF_RETURN_VERSION_CODE, mDownloadData.getVersionCode());
					sp.edit().commit();

					if (mDialog != null && mDialog.isShowing()) {
						return;
					}

					View customView = getLayoutInflater().inflate(R.layout.note_update_view, null);
					TextView noticeView = (TextView) customView.findViewById(R.id.update_notice);
					noticeView.setText(getString(R.string.note_hava_new_version) + " V" + appInfo.getVersionName()
							+ "\n" + appInfo.getDescription());

					mDialog = new AuroraAlertDialog.Builder(mContext)
						.setTitle(R.string.dialog_update_title)
						.setTitleDividerVisible(true)
						.setView(customView, 0, 0, 0, 0)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(R.string.dialog_update_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								ProgressUtil progressUtil = new ProgressUtil();
								progressUtil.updateProgress(mContext, mDownloadData);
							}
						}).show();
				}
			}
		}, this);
	}

	private void setDownloadData(UpgradeAppInfo appInfo) {
		mDownloadData.setApkId(appInfo.getId());
		mDownloadData.setApkDownloadPath(appInfo.getDownloadURL());
		mDownloadData.setApkLogoPath(appInfo.getIcons().getPx256());
		mDownloadData.setApkName(appInfo.getTitle());
		mDownloadData.setPackageName(appInfo.getPackageName());
		mDownloadData.setVersionCode(appInfo.getVersionCode());
		mDownloadData.setVersionName(appInfo.getVersionName());
	}

	@Override
	public void onWrongConnectionState(int state, INotifiableManager manager, Command<?> source) {
		mHandler.sendEmptyMessage(NETWORK_ERROR);
	}

	@Override
	public void onError(int code, String message, INotifiableManager manager) {
		mHandler.sendEmptyMessage(NETWORK_ERROR);
	}

	@Override
	public void onMessage(String message) {
		
	}

	@Override
	public void runOnUI(DataResponse<?> response) {
		mHandler.post(response);
	}*/

}
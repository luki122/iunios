package com.aurora.iunivoice.activity;

import java.util.Collection;
import java.util.Iterator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.aurora.datauiapi.data.IuniVoiceManager;
import com.aurora.iunivoice.R;
import com.aurora.iunivoice.service.SystemPushMsgService;
import com.aurora.iunivoice.update.UpdateApp;
import com.aurora.iunivoice.utils.AccountHelper;
import com.aurora.iunivoice.utils.AccountPreferencesUtil;
import com.aurora.iunivoice.utils.AccountUtil;
import com.aurora.iunivoice.utils.BooleanPreferencesUtil;
import com.aurora.iunivoice.utils.DataCleanManager;
import com.aurora.iunivoice.utils.DialogUtil;
import com.aurora.iunivoice.utils.DialogUtil.IAlertDialogClick;
import com.aurora.iunivoice.utils.Globals;
import com.aurora.iunivoice.utils.Util;
import com.aurora.iunivoice.widget.AuroraSwitch;
import com.nostra13.universalimageloader.cache.memory.MemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;

public class MineSettingActivity extends BaseActivity implements
		OnClickListener {

	private AuroraSwitch wifi_net_switch, notification_switch;
	private TextView tv_cache_size;
	private TextView btn_check_update, btn_about_bbs, btn_exit;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public static final String IS_SYSTEM_MSG_PUSH_KEY = "isSystemMsgPushKey";
	public static final String IS_AUTO_SHOW_PIC = "isAutoShowPic";
	public static final String IUNIVOICE_SETTING_NAME = "iuniVoiceSetting";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		setupViews();
		initExitBtn();
		getCachSize();
	}

	@Override
	public void setupViews() {
		// TODO Auto-generated method stub
		setTitleRes(R.string.mine_setting_text);

		wifi_net_switch = (AuroraSwitch) findViewById(R.id.wifi_net_switch);
		notification_switch = (AuroraSwitch) findViewById(R.id.notification_switch);
		tv_cache_size = (TextView) findViewById(R.id.tv_cache_size);
		btn_check_update = (TextView) findViewById(R.id.btn_check_update);
		btn_about_bbs = (TextView) findViewById(R.id.btn_about_bbs);
		btn_exit = (TextView) findViewById(R.id.btn_exit);
		findViewById(R.id.rl_clear_cache).setOnClickListener(this);
		notification_switch.setChecked(getSettingValue(IS_SYSTEM_MSG_PUSH_KEY));
		wifi_net_switch.setChecked(getSettingValue(IS_AUTO_SHOW_PIC));
		wifi_net_switch.setOnClickListener(this);
		notification_switch.setOnClickListener(this);
		btn_check_update.setOnClickListener(this);
		btn_about_bbs.setOnClickListener(this);
		btn_exit.setOnClickListener(this);
		wifi_net_switch.setOnCheckedChangeListener(onCheckedChangeListener);
		notification_switch.setOnCheckedChangeListener(onCheckedChangeListener);
		tv_cache_size.setText(getCachSize());
	}

	private boolean getSettingValue(String name) {
		SharedPreferences sp = getSharedPreferences(IUNIVOICE_SETTING_NAME,
				Context.MODE_PRIVATE);
		return sp.getBoolean(name, true);
	}

	private void putSettingValue(String name, boolean value) {
		SharedPreferences sp = getSharedPreferences(IUNIVOICE_SETTING_NAME,
				Context.MODE_PRIVATE);
		sp.edit().putBoolean(name, value).commit();
	}

	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
			// TODO Auto-generated method stub
			if (arg0.getId() == R.id.wifi_net_switch) {
				putSettingValue(IS_AUTO_SHOW_PIC, arg0.isChecked());
			} else {
				putSettingValue(IS_SYSTEM_MSG_PUSH_KEY, arg0.isChecked());
				if(arg0.isChecked())
				{
					startService(Util.createExplicitFromImplicitIntent(MineSettingActivity.this,new Intent(SystemPushMsgService.SYSTEM_PUSHMSG_SERVICE_ACTION)));
				}else{
					stopService(Util.createExplicitFromImplicitIntent(MineSettingActivity.this,new Intent(SystemPushMsgService.SYSTEM_PUSHMSG_SERVICE_ACTION)));
				}
			}
		}
	};

	private String getCachSize() {
		try {
			long size = DataCleanManager.getFolderSize(ImageLoader
					.getInstance().getDiscCache().getDirectory())
					+ DataCleanManager.getFolderSize(ImageLoader.getInstance()
							.getDiskCache().getDirectory());
			MemoryCache cache = ImageLoader.getInstance().getMemoryCache();
			Collection<String> keys = cache.keys();
			Iterator<String> kt = keys.iterator();
			while (kt.hasNext()) {
				size += (cache.get(kt.next()).getByteCount());
			}
			return DataCleanManager.getFormatSize(size);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "0M";
		}
	}

	private void clearCache() {
		MemoryCache cache = ImageLoader.getInstance().getMemoryCache();
		cache.clear();
		DataCleanManager.deleteFolderFile(ImageLoader.getInstance()
				.getDiscCache().getDirectory().getAbsolutePath(), false);
		DataCleanManager.deleteFolderFile(ImageLoader.getInstance()
				.getDiskCache().getDirectory().getAbsolutePath(), false);
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.btn_check_update:
			UpdateApp.getInstance().checkUpdateApp(this, true,
					new IuniVoiceManager(this));
			UpdateApp.getInstance().register(this);
			break;
		case R.id.btn_about_bbs:
			Intent intent = new Intent(this, AboutIuniActivity.class);
			startActivity(intent);
			break;
		case R.id.btn_exit:

			DialogUtil.getAlertDialog(MineSettingActivity.this,
					getString(R.string.mine_setting_logout_message),
					R.string.dialog_cancel, R.string.dialog_confirm,
					new IAlertDialogClick() {

						@Override
						public void sureClick() {
							SharedPreferences sp = getSharedPreferences(
									"config", MODE_PRIVATE);
							sp.edit().putBoolean("isLoginFromApp", false)
									.commit();
							com.aurora.iunivoice.utils.Log.v("lmjssjj",
									"isLoginFromApp:" + false);

							postLogout();
							initExitBtn();
							finish();
						}

						@Override
						public void cancelClick() {

						}

					}).show();

			break;
		case R.id.rl_clear_cache:
			new Handler().postDelayed(clearCacheRunable, 1000);
			tv_cache_size.setText(R.string.clear_caching);
			break;
		default:
			break;
		}
	}

	private static final int CLEAR_CACHE_SUCCESS = 0x121;
	private static final int SHOW_ZERO_SIZE = 0x1214;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case CLEAR_CACHE_SUCCESS:
				tv_cache_size.setText(R.string.clear_cache_success);
				break;
			case SHOW_ZERO_SIZE:
				tv_cache_size.setText(getCachSize());
				break;
			default:
				break;
			}
		};
	};

	private Runnable clearCacheRunable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			clearCache();
			handler.sendEmptyMessage(CLEAR_CACHE_SUCCESS);
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					handler.sendEmptyMessage(SHOW_ZERO_SIZE);
				}
			}, 1000);
		}
	};

	private void initExitBtn() {
		if (AccountUtil.getInstance().isIuniOS()) {
			SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
			boolean b = sp.getBoolean("isLoginFromApp", false);
			btn_exit.setVisibility(b ? View.VISIBLE : View.GONE);
		} else {
			btn_exit.setVisibility(AccountHelper.mLoginStatus ? View.VISIBLE
					: View.GONE);
		}
	}

	private void postLogout() {
		AccountPreferencesUtil mPref = AccountPreferencesUtil.getInstance(this);
		;
		mPref.clear();
		Intent b = new Intent(Globals.LOCAL_LOGIN_ACTION);
		AccountHelper.logout();
		b.putExtra(Globals.LOCAL_LOGIN_RESULT, Globals.LOCAL_LOGOUT_SUCCESS);
		sendBroadcast(b);
		BooleanPreferencesUtil.getInstance(getApplicationContext()).setLogin(
				false);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UpdateApp.getInstance().unRegister(this);
	}
}

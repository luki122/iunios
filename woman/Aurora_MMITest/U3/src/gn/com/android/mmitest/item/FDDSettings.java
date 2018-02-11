package gn.com.android.mmitest.item;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Switch;
import android.os.SystemProperties;
import gn.com.android.mmitest.R;
import aurora.widget.AuroraSwitch;
import aurora.preference.AuroraPreferenceActivity;
import aurora.preference.AuroraPreference;
import aurora.preference.AuroraSwitchPreference;
import aurora.preference.AuroraPreference.OnPreferenceChangeListener;

import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;

public class FDDSettings extends AuroraPreferenceActivity implements
		OnPreferenceChangeListener, QcRilHookCallback {

	private final String FDD_SWITCH_KEY = "fdd_setting_key";
	private AuroraSwitchPreference mFDDSwitch;
	QcNvItems nvItems = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Log.v("FDDSettings", "------onCreate----");
		nvItems = new QcNvItems(this, this);

		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		if ("true".equals(SystemProperties.get("persist.radio.dispatchAllKey"))) {
			Log.v("FDDSettings", "------is true----");
			SystemProperties.set("persist.radio.dispatchAllKey", "false");
		}

		addPreferencesFromResource(R.xml.fdd_settings);
		mFDDSwitch = (AuroraSwitchPreference) findPreference(FDD_SWITCH_KEY);
		mFDDSwitch.setOnPreferenceChangeListener(this);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.v("FDDSettings", "------onDestroy----");
		System.exit(0);
	}

	@Override
	public boolean onPreferenceChange(AuroraPreference preference,
			Object newValue) {
		// TODO Auto-generated method stub
		boolean checked = ((Boolean) newValue).booleanValue();

		// 3: 打开lte fdd，0：关闭lte fdd
		try {
			if (checked) {
				Log.v("FDDSettings", "--onPreferenceChange----open fdd------");
				nvItems.setOpenMarketFlag((byte) 3);
			} else {
				Log.v("FDDSettings", "--onPreferenceChange----close fdd-----");
				nvItems.setOpenMarketFlag((byte) 0);
			}

			// 设置完后重启手机
			Intent i = new Intent(Intent.ACTION_REBOOT);
			i.putExtra("nowait", 1);
			i.putExtra("interval", 1);
			i.putExtra("window", 0);
			sendBroadcast(i);
		} catch (IOException e) {
			e.printStackTrace();
			Log.v("FDDSettings", "------onPreferenceChange----IOException");
		}

		return true;
	}

	/*
	 * 注:第一次执行getOpenMarketFlag时，由于没有存值,会报异常
	 */
	public void onQcRilHookReady() {
		boolean mIsChecked = false;
		byte result;
		try {
			result = nvItems.getOpenMarketFlag();
			Log.v("FDDSettings", "------onQcRilHookReady--result--" + result);
			if (result == 3) {
				mIsChecked = true;
			} else if (result == 0) {
				mIsChecked = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			mIsChecked = false;
			Log.v("FDDSettings", "------onQcRilHookReady--IOException--");
		}

		mFDDSwitch.setChecked(mIsChecked);
	}

}
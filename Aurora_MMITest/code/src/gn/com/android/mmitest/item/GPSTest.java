package gn.com.android.mmitest.item;

import gn.com.android.mmitest.R;
import gn.com.android.mmitest.TestUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class GPSTest extends Activity implements LocationListener, Listener,
		OnClickListener {
	public static final String FACTORY_GPS = "gps";

	private TextView mContentTv, mTimerTv, mInfoTv, mSatellitesTv;

	LocationManager mLocationMgr;

	LocationListener mListener;

	Timer mTimer;
	private Button mRightBtn, mWrongBtn, mRestartBtn;
	int mTimeCount;
	private String TAG = "GPSTest";
	Resources mRs;
	GpsSatellite mSatellite;
	private static final int TIME_LENGTH = 1000, EVENT_RESPONSE_SN_WRITE = 1,
			EVENT_RESPONSE_SN_READ = 2;
	private Handler mUiHandler;
	boolean mIsPass;
	private String mOldSn = null;
	private boolean mIsRightTime;

	private class TimerCountTask extends TimerTask {
		@Override
		public void run() {
			runOnUiThread(new Runnable() {
				public void run() {
					// show time count
					mTimerTv.setText(mTimeCount + "");
				}
			});
			mTimeCount++;
		}
	}

	/**
	 * start timer
	 */
	private void startTimer() {
		mTimeCount = 0;
		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerCountTask(), 0, TIME_LENGTH);
	}

	/**
	 * cancel timer
	 */
	private void cancelTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		mTimeCount = 0;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		//lp.dispatchAllKey = 1;
		getWindow().setAttributes(lp);

		setContentView(R.layout.gps_test);
		mRs = getResources();
		// mUiHandler = new Handler() {
		// public void handleMessage(Message msg) {
		// AsyncResult ar;
		// ar = (AsyncResult) msg.obj;
		//
		// switch (msg.what) {
		// case EVENT_RESPONSE_SN_WRITE: {
		// if (ar.exception == null) {
		// Log.d(TAG, "write success");
		// if (true == mIsPass) {
		// TestUtils.rightPress(TAG, GPSTest.this);
		// } else {
		// TestUtils.wrongPress(TAG, GPSTest.this);
		// }
		// } else {
		// Log.d(TAG, "write fail");
		// if (true == mIsPass) {
		// TestUtils.rightPress(TAG, GPSTest.this);
		// } else {
		// TestUtils.wrongPress(TAG, GPSTest.this);
		// }
		// }
		// break;
		// }
		// case EVENT_RESPONSE_SN_READ: {
		// if (ar.exception == null) {
		// mOldSn = (String) (ar.result);
		// Log.d(TAG, "read success sn = " + mOldSn);
		// } else {
		// Log.e(TAG, "read fail");
		// }
		// break;
		// }
		// }
		// }
		// };
		mContentTv = (TextView) findViewById(R.id.gps_state);
		mTimerTv = (TextView) findViewById(R.id.timer);
		mInfoTv = (TextView) findViewById(R.id.gps_info);
		mRightBtn = (Button) findViewById(R.id.right_btn);
		mWrongBtn = (Button) findViewById(R.id.wrong_btn);
		mRightBtn.setOnClickListener(this);
		mWrongBtn.setOnClickListener(this);
		mRestartBtn = (Button) findViewById(R.id.restart_btn);
		mRestartBtn.setOnClickListener(this);
		mSatellitesTv = (TextView) findViewById(R.id.satellites_info);
		mLocationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.e("lich", "---GPSTest----onCreate----");
	}

	@Override
	protected void onStart() {
		super.onStart();

		// PhoneFactory.getDefaultPhone().getMobileRevisionAndIMEI(5,
		// mUiHandler.obtainMessage(EVENT_RESPONSE_SN_READ));
		Log.e("lich", "---GPSTest----onStart----");
        mLocationMgr.addGpsStatusListener(this);
		if (true == mLocationMgr
				.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			mContentTv.setText(R.string.scanning_gps);
			startTimer();
		} else {
			mContentTv.setText(R.string.opening_gps);
		}
		mLocationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
				this);

		mInfoTv.setText(mRs.getString(R.string.longitude) + "\n"
				+ mRs.getString(R.string.latitude) + "\n"
				+ mRs.getString(R.string.altitude));
	}

	@Override
	public void onStop() {
		super.onStop();
		cancelTimer();
		mLocationMgr.removeUpdates(this);
		mLocationMgr.removeGpsStatusListener(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.e("lich", "onLocationChanged");
		if (null != location) {
			mInfoTv.setText(mRs.getString(R.string.longitude)
					+ location.getLongitude() + "\n"
					+ mRs.getString(R.string.latitude) + location.getLatitude()
					+ "\n" + mRs.getString(R.string.altitude)
					+ location.getAltitude());
			if (true == mIsRightTime) {
				mRightBtn.setEnabled(true);
			}
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		mContentTv.setText(R.string.scanning_gps);
		startTimer();
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		Settings.Secure.setLocationProviderEnabled(getContentResolver(),
				LocationManager.GPS_PROVIDER, true);
	}

	@Override
	public void onGpsStatusChanged(int event) {
		// TODO Auto-generated method stub
		switch (event) {
		case GpsStatus.GPS_EVENT_STARTED: {
			Log.e(TAG, "onGpsStatusChanged");
		}
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS: {
			Log.e(TAG, "GPS_EVENT_SATELLITE_STATUS");
			if (null != mLocationMgr) {
				Log.e(TAG, "GPS_EVENT_SATELLITE_STATUS---mLocationMgr is not null--");
				GpsStatus status = mLocationMgr.getGpsStatus(null);
				Iterator<GpsSatellite> iterator = status.getSatellites()
						.iterator();
				StringBuilder sb = new StringBuilder();
				int count = 0;
				// get satellite count
				while (iterator.hasNext()) {
					count++;
					mSatellite = iterator.next();
					sb.append(mRs.getString(R.string.satellite_snc, count)
							+ mSatellite.getSnr() + "\n");
				}
				mSatellitesTv.setText(mRs.getString(R.string.satellite_count,
						count) + "\n" + sb.toString());
				if (count > 2) {
				    mIsRightTime = true;
				}
			}
		}
		}
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {

		case R.id.right_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			if (TestUtils.mIsAutoMode) {
				SharedPreferences.Editor editor = TestUtils
						.getSNSharedPreferencesEdit(this);
				editor.putString(TestUtils.factoryFlag.get(FACTORY_GPS), "P");
				editor.commit();
			}
			TestUtils.rightPress(TAG, this);
			break;
		}

		case R.id.wrong_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			if (TestUtils.mIsAutoMode) {
				SharedPreferences.Editor editor = TestUtils
						.getSNSharedPreferencesEdit(this);
				editor.putString(TestUtils.factoryFlag.get(FACTORY_GPS), "F");
				editor.commit();
			}
			TestUtils.wrongPress(TAG, this);
			break;
		}

		case R.id.restart_btn: {
			mRightBtn.setEnabled(false);
			mWrongBtn.setEnabled(false);
			mRestartBtn.setEnabled(false);
			if (TestUtils.mAppContext == null) {
			    TestUtils.setAppContext(this);
			}
			TestUtils.restart(this, TAG);
			break;
		}
		}

	}

	// @Override
	// public void onClick(View v) {
	// // TODO Auto-generated method stub
	// switch (v.getId()) {
	// case R.id.right_btn: {
	// mIsPass = true;
	// if (null != mOldSn) {
	// String newSN = TestUtils.getNewSN(54, 'P', mOldSn);
	// if (!newSN.equals(mOldSn)) {
	// String AttachedAT[] = {
	// "AT+EGMR=1,5," + "\"" + newSN + "\"", ""
	// };
	// PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(AttachedAT,
	// mUiHandler.obtainMessage(EVENT_RESPONSE_SN_WRITE));
	// } else {
	// TestUtils.rightPress(TAG, GPSTest.this);
	// }
	// } else {
	// TestUtils.rightPress(TAG, GPSTest.this);
	// }
	// TestUtils.rightPress(TAG, GPSTest.this);
	// break;
	// }
	//
	// case R.id.wrong_btn: {
	// mIsPass = false;
	// if (null != mOldSn) {
	// String newSN = TestUtils.getNewSN(54, 'F', mOldSn);
	// if (!newSN.equals(mOldSn)) {
	// String AttachedAT[] = {
	// "AT+EGMR=1,5," + "\"" + newSN + "\"", ""
	// };
	// PhoneFactory.getDefaultPhone().invokeOemRilRequestStrings(AttachedAT,
	// mUiHandler.obtainMessage(EVENT_RESPONSE_SN_WRITE));
	// } else {
	// TestUtils.wrongPress(TAG, GPSTest.this);
	// }
	// } else {
	// TestUtils.wrongPress(TAG, GPSTest.this);
	// }
	// TestUtils.wrongPress(TAG, GPSTest.this);
	// break;
	// }
	// }
	// }
}

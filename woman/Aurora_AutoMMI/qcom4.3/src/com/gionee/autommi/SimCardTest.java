package com.gionee.autommi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.telephony.MSimTelephonyManager;

public class SimCardTest extends BaseActivity {
	public static final String TAG = "SimCardTest";
	private MSimTelephonyManager phoneManagerEx;
	private TelephonyManager tm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		phoneManagerEx = (MSimTelephonyManager) this.getSystemService(Context.MSIM_TELEPHONY_SERVICE);
		String info = null;
		if (null != phoneManagerEx) {
			int sim1State = phoneManagerEx.getSimState(0);
			int sim2State = phoneManagerEx.getSimState(1);
			info = "sim1:" + sim1State + "|smi2:" + sim2State;
			Log.d(TAG, info);
		} else {
			tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
			int simState = tm.getSimState();
		    info = "sim:" + simState;
		}
		((AutoMMI) getApplication()).recordResult(TAG, info, "2");
		Toast.makeText(this, info, Toast.LENGTH_LONG).show();
		/* SIM_STATE_ABSENT          1
		 * SIM_STATE_NETWORK_LOCKED  4
		 * SIM_STATE_PIN_REQUIRED    2
		 * SIM_STATE_PUK_REQUIRED    3
		 * SIM_STATE_READY           5
		 * SIM_STATE_UNKNOWN         0
		 */
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		this.finish();
	}
}

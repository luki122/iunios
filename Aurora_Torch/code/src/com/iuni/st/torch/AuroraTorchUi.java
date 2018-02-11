/*
 * this is activity use for test AuroraTorch app
 */
package com.iuni.st.torch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class AuroraTorchUi extends Activity {

	private static final String TAG = "TorchUi";
	private boolean torchStateFlag = false;
	Button torchControl;
	private Handler mHandler = new Handler();

	private Runnable controlTorchRunnable = new Runnable() {

		@Override
		public void run() {
			Intent intent = new Intent(AuroraTorchUi.this, AuroraTorchService.class);
			if (torchStateFlag) {
				/*sendBroadcast(new Intent(
						"iuni.intent.andtion.ACTION_CHANGE_TORCH_STATE")
						.putExtra(AuroraTorchView.EXTRA_TORCH_STATE,
								AuroraTorchView.TORCH_STATE_OFF));*/
				
				//start service to change torch state
				intent.putExtra(AuroraTorchView.EXTRA_TORCH_STATE, 0);
				torchStateFlag = false;

			} else {
				/*sendBroadcast(new Intent(
						"iuni.intent.andtion.ACTION_CHANGE_TORCH_STATE")
						.putExtra(AuroraTorchView.EXTRA_TORCH_STATE,
								AuroraTorchView.TORCH_STATE_ON));*/
				intent.putExtra(AuroraTorchView.EXTRA_TORCH_STATE, 1);
				torchStateFlag = true;
			}
			AuroraTorchUi.this.startService(intent);
			updateUI();
		}
	};

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ("iuni.intent.andtion.ACTION_CHANGE_TORCH_STATE"
					.equals(action)) {

				int state = intent.getIntExtra(
						AuroraTorchView.EXTRA_TORCH_STATE,
						AuroraTorchView.TORCH_STATE_OFF);
				AuroraDebugUtils.torchLogd(TAG, "receive action: "
						+ action);
				AuroraDebugUtils.torchLogd(TAG, "state: " + state);
				if (state == -1) {
					torchStateFlag = false;
					updateUI();
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.torchui_layout);
		torchControl = (Button) findViewById(R.id.control_torch);
		torchControl.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mHandler.removeCallbacks(controlTorchRunnable);
				//mHandler.postDelayed(controlTorchRunnable, 500);//avoid user toggle torch to fast,but is has problem that delay .5s as user first use
				mHandler.post(controlTorchRunnable);
			}
		});
		registerReceiver(mReceiver, new IntentFilter("iuni.intent.andtion.ACTION_CHANGE_TORCH_STATE"));
	}

	private void updateUI() {
		torchControl.setText(torchStateFlag ? "TorchOn" : "TorchOff");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(mReceiver);
	}

}

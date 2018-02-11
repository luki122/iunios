package com.iuni.st.torch;

import android.content.Context;
import android.content.Intent;

public class AuroraTorchHelper {

	private static final String TAG = "AuroraTorchHelper";
	private Context mContext;
	
	public AuroraTorchHelper() {
		super();
	}

	public AuroraTorchHelper(Context mContext) {
		super();
		this.mContext = mContext;
	}

	public void controlTorch(boolean turnOn) {
		final Intent serviceIntent = new Intent(mContext, AuroraTorchService.class);
		int state = turnOn ? AuroraTorchView.TORCH_STATE_ON : AuroraTorchView.TORCH_STATE_OFF;
		
		AuroraDebugUtils.torchLogd(TAG, "state: " + state + "  mContext: " + mContext);
		
		//start service to change torch state
		serviceIntent.putExtra(AuroraTorchView.EXTRA_TORCH_STATE, state);
		mContext.startService(serviceIntent);
	}
	
}

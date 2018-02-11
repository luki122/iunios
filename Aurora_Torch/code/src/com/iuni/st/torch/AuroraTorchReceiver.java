package com.iuni.st.torch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

//originaly i use this receiver to revice other app 's command
//as u3's broadcast receiver has problem 
//use java Reflection to change torch state
//but it still in use,you can send broadcast to control torch state
public class AuroraTorchReceiver extends BroadcastReceiver {
	
	private final String TAG = "TorchReceiver";
	public static final String ACTION_CHANGE_TORCH_STATE = "iuni.intent.action.ACTION_CHANGE_TORCH_STATE";
	private static final String ACTION_TOGGLE_DEBUG_STATE = "iuni.intent.action.ACTION_TOGGLE_DEBUG_STATE";
	private static final String ACTION_TEST_ACTIVITY = "iuni.intent.action.ACTION_TEST";

	@Override
	public void onReceive(Context context, Intent intent) {
		
		final String action = intent.getAction();
		final Intent serviceIntent = new Intent(context, AuroraTorchService.class);
		
		if (ACTION_CHANGE_TORCH_STATE.equals(action)) {
			
			int state = intent.getIntExtra(AuroraTorchView.EXTRA_TORCH_STATE, AuroraTorchView.TORCH_STATE_OFF);
			AuroraDebugUtils.torchLogd(TAG, "receive action: " + action);
			AuroraDebugUtils.torchLogd(TAG, "state: " + state);
			
			//start service to change torch state
			serviceIntent.putExtra(AuroraTorchView.EXTRA_TORCH_STATE, state);
			context.startService(serviceIntent);
		} else if(ACTION_TOGGLE_DEBUG_STATE.equals(action)){
			AuroraDebugUtils.DEBUGABLE = intent.getBooleanExtra("debugable", false);
		} else if(ACTION_TEST_ACTIVITY.equals(action)){
			android.util.Log.e("haha","come here");
			Intent i = new Intent();
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
			i.setClass(context, AuroraTorchUi.class);
			context.startActivity(i);
		}
	}
}

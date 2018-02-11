package com.android.systemui.recent;;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class AuroraTorchController {

	private Context mContext;
	private static final String TORCH_APP_CLASSNAME = "com.iuni.st.torch.AuroraTorchHelper";
	private static final String TORCH_APP_PACKAGENAME = "com.iuni.st.torch";

	// torch state
	public static final int TORCH_STATE_ON = 1;
	public static final int TORCH_STATE_OFF = 0;
	// processing state
	public static final int PROCESS_STATE_BUSY = 1;
	public static final int PROCESS_STATE_FREE = 0;

	// reflection object
	private Method controlTorchMethod;
	private Object torchObject;

	private int torchState = TORCH_STATE_OFF;
	private int processState = PROCESS_STATE_FREE;

	// handler avoid user change torch state too fase
	private Handler mHandler = new Handler();

	private boolean canChangeState = true;
	private static final int METHOD_DELAY_TIME = 300; // ms
	private static final int CHANGE_STATE_GAP_TIME = 300; // ms

	private final Runnable controlTorchRunnable = new Runnable() {
		@Override
		public void run() {
			controlTorchApp();
		}
	};

	private final Runnable resetControlableRunnable = new Runnable() {
		@Override
		public void run() {
			canChangeState = true;
		}
	};

	public void controlTorch() {
		if(canChangeState){
			mHandler.removeCallbacks(controlTorchRunnable);
			mHandler.post(controlTorchRunnable);
			canChangeState = false;
			mHandler.postDelayed(resetControlableRunnable, CHANGE_STATE_GAP_TIME);
		} else {
			mHandler.removeCallbacks(controlTorchRunnable);
			mHandler.removeCallbacks(resetControlableRunnable);
			mHandler.postDelayed(controlTorchRunnable, METHOD_DELAY_TIME);
			mHandler.postDelayed(resetControlableRunnable, CHANGE_STATE_GAP_TIME);
		}
	}

	public boolean isTorchOn() {
		return torchState == TORCH_STATE_ON ? true : false;
	}

	public boolean isProcessing() {
		return processState == PROCESS_STATE_BUSY ? true : false;
	}

	public void changeTorchState(boolean turnOn) {
		torchState = turnOn ? TORCH_STATE_ON : TORCH_STATE_OFF;
	}

	public AuroraTorchController(Context mContext) {
		super();
		this.mContext = mContext;
		getControlTorchAppMethod();
	}

	private void getControlTorchAppMethod() {
		if (mContext == null)
			return;
		try {
			Context context = mContext.createPackageContext(TORCH_APP_PACKAGENAME, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
			ClassLoader pluginLoader = context.getClassLoader();
			Class<?> pluginClass = pluginLoader.loadClass(TORCH_APP_CLASSNAME);
			Constructor ct = pluginClass.getConstructor(Context.class);
			torchObject = ct.newInstance(context);
			controlTorchMethod = pluginClass.getMethod("controlTorch", Boolean.TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void controlTorchApp() {
		try {
			boolean turnOn = (torchState == TORCH_STATE_ON) ? true : false;
			processState = PROCESS_STATE_BUSY;
			controlTorchMethod.invoke(torchObject, turnOn);
			processState = PROCESS_STATE_FREE;
			Log.e("hahaha", "Start control torch, State: " + turnOn);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}


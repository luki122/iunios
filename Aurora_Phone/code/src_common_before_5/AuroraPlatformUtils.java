package com.android.phone;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.content.Context;
import android.content.ComponentName;

public class AuroraPlatformUtils {
	public static void setMobileDataEnabled(boolean isChecked) {
      ConnectivityManager cm =
      (ConnectivityManager)PhoneGlobals.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
      			cm.setMobileDataEnabled(isChecked);
	}
	
	public static void dismissKeyguard() {
		try {
			ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public static void registerMediaButton(ComponentName name) {
        AudioManager am = (AudioManager) PhoneGlobals.getInstance().getSystemService(Context.AUDIO_SERVICE);
        am.registerMediaButtonEventReceiverForCalls(name);
	}

}

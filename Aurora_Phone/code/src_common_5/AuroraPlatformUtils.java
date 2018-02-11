package com.android.phone;
import android.media.AudioManager;
import android.telephony.SubscriptionManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.ComponentName;
import android.telephony.TelephonyManager;

public class AuroraPlatformUtils {
	public static void setMobileDataEnabled(boolean isChecked) {
		TelephonyManager.getDefault().setDataEnabled(isChecked);
	}
	
	
	public static void dismissKeyguard() {
		 KeyguardManager kgm = (KeyguardManager) PhoneGlobals.getInstance().getSystemService(Context.KEYGUARD_SERVICE);
//		 if (kgm != null) kgm.exitKeyguardSecurely(null);
		 KeyguardLock keyguardLock = kgm.newKeyguardLock("auroraPhone"); 
		 keyguardLock.disableKeyguard();	
		 kgm.exitKeyguardSecurely(null);
		 keyguardLock.reenableKeyguard();
	}
	
	public static void registerMediaButton(ComponentName name) {
        AudioManager am = (AudioManager) PhoneGlobals.getInstance().getSystemService(Context.AUDIO_SERVICE);
        am.registerMediaButtonEventReceiver(name);
	}

}

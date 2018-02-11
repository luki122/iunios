package gn.com.android.mmitest.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;

public class FDDReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.v("FDDSettings", "------onReceive----");
		 if (intent.getAction().equals(TelephonyIntents.SECRET_CODE_ACTION)) {
			 Log.v("FDDSettings", "------onReceive---1111-");
	            Intent i= new Intent(Intent.ACTION_MAIN);
	            i.setClass(context, FDDSettings.class);
	            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            context.startActivity(i);
	     }
	}
	
}


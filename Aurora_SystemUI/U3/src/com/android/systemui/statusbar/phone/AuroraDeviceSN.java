package com.android.systemui.statusbar.phone;

import android.os.Handler;
import android.util.Log;
import android.content.Context;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import java.io.IOException;
import com.qualcomm.qcnvitems.QcNvItems;
import com.qualcomm.qcrilhook.QcRilHookCallback;
import android.os.SystemProperties;

public class AuroraDeviceSN {
    
    private static final String TAG = "AuroraDeviceSN";
    private static QcNvItems mQcNvItems;

    public static final String DEFAULT_SN = "gionee0000000000";

    private static AuroraDeviceSN mInstance = new AuroraDeviceSN();


    public static AuroraDeviceSN getDefault() {
        return mInstance;
    }

    private static Runnable mRunnableClose = new Runnable() {
        @Override
        public void run() {
           if (mQcNvItems != null) {
               Log.d(TAG, "clear getSN factoryResult string ");
//               mQcNvItems.dispose();
           }
        }
    };

    private static QcRilHookCallback mQcrilHookCb = new QcRilHookCallback () {
        @Override
        public void onQcRilHookReady(){
            SystemProperties.set("persist.sys.aurora.device.sn", GnGetSN());   
           Log.v(TAG, "GnGetSN() = " + GnGetSN());
           if (mQcNvItems != null) {
               Log.d(TAG, "clear getSN factoryResult string ");
//               mQcNvItems.dispose();
           }
        }
    };

    private static String GnGetSN(){

	String factoryResult = DEFAULT_SN;
	try {
            factoryResult = mQcNvItems.getFactoryResult();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        } catch (Exception exc) {
			exc.printStackTrace();
		}
	Log.e(TAG, "getSN factoryResult :" + factoryResult);
        return factoryResult;
    }   

    public static void AuroraCreatQcNvItems(Context context) {
        String sn = SystemProperties.get("persist.sys.aurora.device.sn", DEFAULT_SN);
        Log.v(TAG, "mQcNvItems = " + mQcNvItems + " sn = " + sn);
        if (mQcNvItems == null && sn.equals(DEFAULT_SN)) {
	        mQcNvItems = new QcNvItems(context, mQcrilHookCb);
	    }
    } 
} 

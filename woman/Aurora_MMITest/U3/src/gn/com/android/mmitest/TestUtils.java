
package gn.com.android.mmitest;

import gn.com.android.mmitest.item.BluetoothTest;
import gn.com.android.mmitest.item.GPSTest;
import gn.com.android.mmitest.item.WIFITest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.lang.Thread;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.KeyEvent;
import android.os.SystemProperties;

public class TestUtils {
    public static WakeLock mWakeLock;

    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences mSNSharedPreferences;

    private static ArrayList<String> mItems;

    private static ArrayList<String> mItemKeys;

    private static ArrayList<String> mAutoItemKeys;

    private static ArrayList<String> mAutoItems;

    private static ArrayList<String> mKeyItems;
    private static ArrayList<String> mKeyItemKeys;
    public static boolean mIsAutoMode;

    public static Context mAppContext;
    
    public static int WRITE_TO_SN_COUNT = 4;
    private static SharedPreferences.Editor mEditor;
    public static int VOL_MINUS = 5;
    public static int VOL_MINUS_INCALL = 9;
    
    
    public static SharedPreferences.Editor mSNEditor;
    public static void setAppContext(Activity activity) {
        mAppContext = activity.getApplicationContext();
    }
    
    public static Map<String, String> factoryFlag= new HashMap<String, String>();
    static {
    	factoryFlag.put(WIFITest.FACTORY_WIFI, "15");
    	factoryFlag.put(BluetoothTest.FACTORY_BT, "18");
    	factoryFlag.put(GPSTest.FACTORY_GPS, "14");
    }
    
    public static void acquireWakeLock(Activity activity) {
        if (mWakeLock == null || false == mWakeLock.isHeld()) {
            PowerManager powerManager = (PowerManager) (activity.getApplicationContext()
                    .getSystemService(Context.POWER_SERVICE));
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Single Test");
        }
        if (false == mWakeLock.isHeld()) {
            mWakeLock.acquire();
        }
    }

    public static void releaseWakeLock() {
        if (null != mWakeLock && true == mWakeLock.isHeld()) {
            mWakeLock.release();
        }
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        if (null == mSharedPreferences) {
            mSharedPreferences = context.getSharedPreferences("gn_mmi_test",
                    Context.MODE_WORLD_WRITEABLE);
        }
        return mSharedPreferences;
    }

    public static SharedPreferences getSNSharedPreferences(Context context) {
        if (null == mSNSharedPreferences) {
            mSNSharedPreferences = context.getSharedPreferences("gn_mmi_sn",
                    Context.MODE_WORLD_WRITEABLE);
        }
        return mSNSharedPreferences;
    }

    public static SharedPreferences.Editor getSharedPreferencesEdit(Context context) {
        if (null == mEditor) {
            mSharedPreferences = getSharedPreferences(context);
            mEditor = mSharedPreferences.edit();
        }
        return mEditor;
    }
    
    public static SharedPreferences.Editor getSNSharedPreferencesEdit(Context context) {
        if (null == mSNEditor) {
            mSNSharedPreferences = getSNSharedPreferences(context);
            mSNEditor = mSNSharedPreferences.edit();
        }
        return mSNEditor;
    }
    
    public static ArrayList<String> getItemKeys(Context context) {
        if (null == mItemKeys) {
            mItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_keys)));
        }
        return mItemKeys;
    }

    public static ArrayList<String> getItems(Context context) {
        if (null == mItems) {
            mItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.single_test_items)));
        }
        return mItems;
    }

    public static ArrayList<String> getAutoItemKeys(Context context) {
        if (null == mAutoItemKeys) {
            mAutoItemKeys = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys)));
        }
        return mAutoItemKeys;
    }

    public static ArrayList<String> getAutoItems(Context context) {
        if (null == mAutoItems) {
            mAutoItems = new ArrayList(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items)));
        }
        return mAutoItems;
    }

    public static ArrayList<String> getKeyItems(Context context) {
        if (null == mKeyItems)
            configKeyTestArrays(context);
        
        return mKeyItems;
    }

    public static ArrayList<String> getKeyItemKeys(Context context) {
        if (null == mKeyItemKeys)
            configKeyTestArrays(context);
        
        return mKeyItemKeys;
    }
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin 
    public static void rightPress(String TAG, Activity activity) {
		activity.finish();
		if (mIsAutoMode_2) {
			
			processButtonPress_2(TAG, activity,true);
		}if (mIsAutoMode_3) {
			processButtonPress_3(TAG, activity,true);
		}
		else {
			processButtonPress(TAG, activity);
		}
	}

    public static void wrongPress(String TAG, Activity activity) {
        activity.finish();
        if (mIsAutoMode_2) {
        	processButtonPress_2(TAG,activity,false);
        }if (mIsAutoMode_3) {
        	processButtonPress_3(TAG,activity,false);
		}
        else {
        	processButtonPress1(TAG,activity);
        }
    }
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end 

    public static void processButtonPress(String TAG, Activity activity) {
        activity.finish();
        if (true == mIsAutoMode) {
            mAutoItemKeys = getAutoItemKeys(mAppContext);
            int index = mAutoItemKeys.indexOf(TAG);
            if (null == mEditor) {
                getSharedPreferencesEdit(mAppContext);
            }
            if (index == 0) {
                mEditor.clear();
            }
            mEditor.putInt(TestUtils.getAutoItems(mAppContext).get(index), 1);
            mEditor.commit();
            Log.d("mmi_TestUtils", TestUtils.getAutoItems(activity).get(index) + ":");
            if (index < mAutoItemKeys.size() - 1) {
                try {
                    Intent it = new Intent().setClass(
                            mAppContext,
                            Class.forName("gn.com.android.mmitest.item."
                                    + mAutoItemKeys.get(index + 1)));
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mAppContext.startActivity(it);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Intent it = new Intent(mAppContext, TestResult.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mAppContext.startActivity(it);
            }
        }
    }

    public static void processButtonPress1(String TAG, Activity activity) {
        activity.finish();
        if (true == mIsAutoMode) {
            mAutoItemKeys = getAutoItemKeys(mAppContext);
            int index = mAutoItemKeys.indexOf(TAG);
            if (null == mEditor) {
                getSharedPreferencesEdit(mAppContext);
            }
            if (index == 0) {
                mEditor.clear();
            }
            mEditor.putInt(TestUtils.getAutoItems(mAppContext).get(index), 0);
            mEditor.commit();
            if (index < mAutoItemKeys.size() - 1) {
                try {
                    Intent it = new Intent().setClass(
                            mAppContext,
                            Class.forName("gn.com.android.mmitest.item."
                                    + mAutoItemKeys.get(index + 1)));
                    it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mAppContext.startActivity(it);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                Intent it = new Intent(mAppContext, TestResult.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mAppContext.startActivity(it);
            }
        }
    }
    
    public static void restart(Activity activity, String TAG) {
        try {
            activity.finish();
            Intent it = new Intent(mAppContext, Class
                    .forName("gn.com.android.mmitest.item." + TAG));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mAppContext.startActivity(it);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void delayRestart(Activity activity, String TAG, int DELAY_TIME) {
        try {
            activity.finish();
            try {
                Thread.sleep(DELAY_TIME);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Intent it = new Intent(mAppContext, Class
                    .forName("gn.com.android.mmitest.item." + TAG));
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mAppContext.startActivity(it);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static String getNewSN(int position, Character value, String sn) {
        StringBuffer sb = new StringBuffer(sn);
        int length = sn.length();
        if (length < position - 1) {
            for (int i = 0; i < position - sn.length(); i++) {
                sb.append(" ");
            }
            sb.append(value);
        } else if (length == position - 1) {
            sb.append(value);
        } else {
            sb.setCharAt(position - 1, value);
        }
        return sb.toString();
    }
    
    public static void openBtAndWifi(Activity activity) {
        BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
        if (null != bAdapter && false == bAdapter.isEnabled()) {
            bAdapter.enable();
        }
        
        WifiManager wifiMgr = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (null != wifiMgr && false == wifiMgr.isWifiEnabled()) {
            wifiMgr.setWifiEnabled(true);
        }
    }
    
    // Gionee xiaolin 20120613 add for CR00624109 start
    public static int audioRecBufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
    
    public static int audioTrackBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT);
    
    public static AudioRecord aRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            audioRecBufferSize);
    
    public static AudioTrack aTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
            audioTrackBufferSize, AudioTrack.MODE_STREAM);
    // Gionee xiaolin 20120613 add for CR00624109 end
    
    
    
    private static ArrayList<String> mSingleItemKeys;
    private static ArrayList<String> mSingleItems;
    
    public static void configKeyTestArrays(Context context) {
        mKeyItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_items)));
        mKeyItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.key_test_keys)));
        Map<String, String> valueKeyMap = new HashMap<String, String>();
        Map<String, String> propDefMap = new HashMap<String, String>();
        Map<String, Integer> propToResMap = new HashMap<String, Integer>();
        
        if(mKeyItems.size() == mKeyItemKeys.size()) {
            int size = mKeyItemKeys.size();
            for(int i =0; i < size; i++ ) {
                valueKeyMap.put(mKeyItems.get(i), mKeyItemKeys.get(i));
            }
        } else {
            Log.d("TestUtils", "wrong!");
            return;
        }
        
        propDefMap.put("gn.mmi.keytest.menu", "yes");
        propDefMap.put("gn.mmi.keytest.app", "no");
        propDefMap.put("gn.mmi.keytest.search", "no");
        propDefMap.put("gn.mmi.keytest.camera", "no");

        String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
        String[] strName = deviceName.split("-");
        if(strName.length > 1){
           if(strName[1].equals("U3") || strName[1].equals("U3m")){
               propDefMap.put("gn.mmi.keytest.hall", "yes"); 
           }else{
               propDefMap.put("gn.mmi.keytest.hall", "no"); 
           }
        }else{
              propDefMap.put("gn.mmi.keytest.hall", "no"); 
        }

        
        propToResMap.put("gn.mmi.keytest.menu", R.string.menu_key);
        propToResMap.put("gn.mmi.keytest.app", R.string.app_key);
        propToResMap.put("gn.mmi.keytest.search", R.string.search_key);
        propToResMap.put("gn.mmi.keytest.camera", R.string.camera_key);
        
        
        for ( String prop : propDefMap.keySet()) {
            if (!"yes".equals(SystemProperties.get(prop, propDefMap.get(prop)))) {
                if ("gn.mmi.keytest.hall".equals(prop)) {
                  String hall_o_value = context.getResources().getString(R.string.hall_o_key);
                  String hall_c_value = context.getResources().getString(R.string.hall_c_key);
                  removeKeyItem(hall_o_value, valueKeyMap);
                  removeKeyItem(hall_c_value, valueKeyMap);
                } else {
                  String value = context.getResources().getString(propToResMap.get(prop));
                  removeKeyItem(value, valueKeyMap);
                }
            }
        }
    }
    
    private static void removeKeyItem(String value, Map<String, String> valueToKey) {
        mKeyItems.remove(value);
        mKeyItemKeys.remove(valueToKey.get(value));
    }  
    
    
    public static void configTestItemArrays(Context context) {
        
        mAutoItemKeys = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_keys)));
        mAutoItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.auto_test_items)));
        mSingleItemKeys = new ArrayList<String>(Arrays.asList(context.getResources()
                .getStringArray(R.array.single_test_keys)));
        mSingleItems = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                R.array.single_test_items)));
        
        Map<String, String> featureItem = new HashMap<String, String>();
        String[] featuresToCheck = new String[] {
            PackageManager.FEATURE_CAMERA_FRONT,
            PackageManager.FEATURE_SENSOR_ACCELEROMETER,
            PackageManager.FEATURE_SENSOR_COMPASS,
            PackageManager.FEATURE_SENSOR_LIGHT,
            PackageManager.FEATURE_SENSOR_GYROSCOPE
        };
        
        featureItem.put(PackageManager.FEATURE_CAMERA_FRONT,
                context.getResources().getString(R.string.front_camera));
        featureItem.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER,
                context.getResources().getString(R.string.acceleration));
        featureItem.put(PackageManager.FEATURE_SENSOR_COMPASS, 
                context.getResources().getString(R.string.magnetic_field));
        featureItem.put(PackageManager.FEATURE_SENSOR_LIGHT,
                context.getResources().getString(R.string.light_proximity));
        featureItem.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, 
                context.getResources().getString(R.string.gyroscope));

        PackageManager pm = context.getPackageManager();
        for (String feature : featuresToCheck) {
            if (!hasHardWareFeature(feature)) {
                removeTestItem(featureItem.get(feature));
            }
        }     
        
        if (!"yes".equals(SystemProperties.get("gn.mmi.mic2", "no"))) {
            String item  = context.getResources().getString(R.string.phone_loopback2);
            removeTestItem(item);
        }
        

       // if (!"yes".equals(SystemProperties.get("gn.mmi.receiver2", "no"))) {
            String receiver2  = context.getResources().getString(R.string.receiver2);
            removeTestItem(receiver2);
        //}
        if (!"yes".equals(SystemProperties.get("gn.mmi.otg", "yes"))) {
            String item  = context.getResources().getString(R.string.otg);
            removeTestItem(item);
        }
        if (!"yes".equals(SystemProperties.get("gn.mmi.pressuretest", "no"))) {
           String item1  = context.getResources().getString(R.string.pressure);
            removeTestItem(item1);
        }
        if (!"yes".equals(SystemProperties.get("gn.mmi.nfc", "no"))) {
            String item  = context.getResources().getString(R.string.nfc);
            removeTestItem(item);
        }
        
        String deviceName = SystemProperties.get("ro.gn.iuniznvernumber");
        Log.d("TestUtils", "-------deviceName----===="+deviceName);
        if(deviceName.contains("U3m") ||deviceName.contains("U3M")){
            String item  = context.getResources().getString(R.string.phone_loopback4);
            removeTestItem(item);

            String itemFM = context.getResources().getString(R.string.fm);
            removeTestItem(itemFM);
        }

        if (!"yes".equals(SystemProperties.get("gn.mmi.alsandpstest", "no"))) {
            String item  = context.getResources().getString(R.string.light_proximity);
            int index = mAutoItems.indexOf(item);
           if (-1 != index) {
             mAutoItems.remove(index);
             mAutoItemKeys.remove(index);
           }      
        }

    }
    
    private static boolean hasHardWareFeature(String feature) {
        
        Map<String, String> featureToSysProp = new HashMap<String, String>();
        featureToSysProp.put( PackageManager.FEATURE_CAMERA_FRONT, "gn.mmi.camera.front");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_ACCELEROMETER, "gn.mmi.sensor.acc");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_COMPASS, "gn.mmi.sensor.compass");
        featureToSysProp.put( PackageManager.FEATURE_SENSOR_LIGHT, "gn.mmi.sensor.light");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_PROXIMITY, "gn.mmi.sensor.prox");
        featureToSysProp.put(PackageManager.FEATURE_SENSOR_GYROSCOPE, "gn.mmi.sensor.gyro");
        if (PackageManager.FEATURE_SENSOR_GYROSCOPE.equals(feature)) {
            return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
        }
        
        if (PackageManager.FEATURE_SENSOR_LIGHT.equals(feature)) {
            return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
        }
        
        return "yes".equals(SystemProperties.get(featureToSysProp.get(feature), "yes"));
    }
   
    
    static private void removeTestItem(String item) {
        int index = mAutoItems.indexOf(item);
        if (-1 != index) {
            mAutoItems.remove(index);
            mAutoItemKeys.remove(index);
        }   
        
        index = mSingleItems.indexOf(item);
        if (-1 != index) {
            mSingleItems.remove(index);
            mSingleItemKeys.remove(index);
        }       
    }
    
    public static String[] getSingleTestItems(Context context) {
        if (null == mSingleItems)
            configTestItemArrays(context);
        return mSingleItems.toArray(new String[0]);
    }
    
    public static String[] getSingleTestKeys(Context context) {
        if (null == mSingleItemKeys)
            configTestItemArrays(context);
        return  mSingleItemKeys.toArray(new String[0]);        
    }
    
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin 
    public static boolean mIsAutoMode_2 = false;
    private static void processButtonPress_2(String TAG, Activity activity, boolean success) {

        int index = getAutoItemKeys_2(activity).indexOf(TAG);
        Log.d("mmi_TestUtils", mAutoItemKeys_2.toString());
        int result = success ? 1 : 0;
        Log.d("mmi_TestUtils", "result" + result);
        Log.d("mmi_TestUtils", "index" + index);
        if (null == mEditor) {
            getSharedPreferencesEdit(activity);
        }
        if (index == 0) {
            mEditor.clear();
        }
        mEditor.putInt(TestUtils.getAutoItems_2(activity).get(index), result);
        mEditor.commit();
        
        Log.d("mmi_TestUtils", TestUtils.getAutoItems_2(activity).get(index) + ":" + result);
        if (index < mAutoItemKeys_2.size() - 1) {
            try {
                Intent it = new Intent().setClass(
                        activity,
                        Class.forName("gn.com.android.mmitest.item."
                                + mAutoItemKeys_2.get(index + 1)));
                activity.startActivity(it);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Intent it = new Intent(activity, TestResult.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(it);
        }
    
    }
    private static ArrayList<String> mAutoItemKeys_2;
    private static ArrayList<String> mAutoItems_2;
    public static ArrayList<String> getAutoItemKeys_2(Context context) {
        if (null == mAutoItemKeys_2) {
            mAutoItemKeys_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys_2)));
        }
        return mAutoItemKeys_2;
    }

    public static ArrayList<String> getAutoItems_2(Context context) {
        if (null == mAutoItems_2) {
            mAutoItems_2 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items_2)));
        }
        return mAutoItems_2;
    }
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end 
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 begin 
    public static boolean mIsAutoMode_3 = false;
    private static void processButtonPress_3(String TAG, Activity activity, boolean success) {

        int index = getAutoItemKeys_3(activity).indexOf(TAG);
        Log.d("mmi_TestUtils", mAutoItemKeys_3.toString());
        int result = success ? 1 : 0;
        Log.d("mmi_TestUtils", "result" + result);
        Log.d("mmi_TestUtils", "index" + index);
        if (null == mEditor) {
            getSharedPreferencesEdit(activity);
        }
        if (index == 0) {
            mEditor.clear();
        }
        mEditor.putInt(TestUtils.getAutoItems_3(activity).get(index), result);
        mEditor.commit();
        
        Log.d("mmi_TestUtils", TestUtils.getAutoItems_3(activity).get(index) + ":" + result);
        if (index < mAutoItemKeys_3.size() - 1) {
            try {
                Intent it = new Intent().setClass(
                        activity,
                        Class.forName("gn.com.android.mmitest.item."
                                + mAutoItemKeys_3.get(index + 1)));
                activity.startActivity(it);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Intent it = new Intent(activity, TestResult.class);
            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(it);
        }
    
    }
    private static ArrayList<String> mAutoItemKeys_3;
    private static ArrayList<String> mAutoItems_3;
    public static ArrayList<String> getAutoItemKeys_3(Context context) {
        if (null == mAutoItemKeys_3) {
            mAutoItemKeys_3 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_keys_3)));
        }
        return mAutoItemKeys_3;
    }

    public static ArrayList<String> getAutoItems_3(Context context) {
        if (null == mAutoItems_3) {
            mAutoItems_3 = new ArrayList<String>(Arrays.asList(context.getResources().getStringArray(
                    R.array.auto_test_items_3)));
        }
        return mAutoItems_3;
    }
  //Gionee <bug> <zhangxiaowei> <2013-10-13> add for CR00907005 end 
}

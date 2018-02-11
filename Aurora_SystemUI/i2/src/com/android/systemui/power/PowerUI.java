/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.power;

import aurora.app.AuroraAlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Slog;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.media.NotificationPlayer;

import com.android.systemui.Xlog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.util.Arrays;

//Gionee fengjianyi 2012-12-28 add for CR00751916 start
import android.os.SystemProperties;
//Gionee fengjianyi 2012-12-28 add for CR00751916 end

//Gionee <zenggz><2013-4-20> add for CR00794507 begin
import android.os.PowerManager;
//Gionee <zenggz><2013-4-20> add for CR00794507 end

public class PowerUI extends SystemUI {
    static final String TAG = "PowerUI";

    static final boolean DEBUG = false;

    // Gionee fengjianyi 2012-12-28 add for CR00751916 start
    private static final boolean GN_WIDGET_SUPPORT = false; //SystemProperties.get("ro.gn.widget.support").equals("yes");
    // Gionee fengjianyi 2012-12-28 add for CR00751916 end
    public static final String EXTRA_USB_HEALTH = "usb_health";
    /// M: Support "Low Battery Sound". @{
    Handler mHandler = new BatteryHandler();
    /// M: Support "Low Battery Sound". @}

    int mBatteryLevel = 100;
    int mBatteryStatus = BatteryManager.BATTERY_STATUS_UNKNOWN;
    int mPlugType = 0;
    int mInvalidCharger = 0;

    int mLowBatteryAlertCloseLevel;
    int[] mLowBatteryReminderLevels = new int[2];

    AuroraAlertDialog mInvalidChargerDialog;
    AuroraAlertDialog mLowBatteryDialog;
    TextView mBatteryLevelTextView;
 // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog begin
    AuroraAlertDialog mOverVoltageDialog;
    AuroraAlertDialog mOverHeatDialog;
    AuroraAlertDialog mColdDialog;
    
 // Aurora <tongyh> <2014-06-19> update battery's unusual toast begin
//    int mUsbHealth = BatteryManager.BATTERY_HEALTH_GOOD;
    // Aurora <tongyh> <2014-06-19> update battery's unusual toast end
    int mHeat = BatteryManager.BATTERY_HEALTH_GOOD;

    
 // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog end
    
    public void start() {
		//temp modify
        mLowBatteryAlertCloseLevel = 20;//config_lowBatteryCloseWarningLevel
        mLowBatteryReminderLevels[0] = 15;//config_lowBatteryWarningLevel
        mLowBatteryReminderLevels[1] = 4;//config_criticalBatteryWarningLevel

        // Register for Intent broadcasts for...
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        /// M: Support show battery level when configuration changed. @{
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        /// M: Support show battery level when configuration changed. @}
        /// M: Hide low battery dialog when PowerOffAlarm ring. @{
        filter.addAction("android.intent.action.normal.boot");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        /// M: Hide low battery dialog when PowerOffAlarm ring. @}
        mContext.registerReceiver(mIntentReceiver, filter, null, mHandler);
    }

    /**
     * Buckets the battery level.
     *
     * The code in this function is a little weird because I couldn't comprehend
     * the bucket going up when the battery level was going down. --joeo
     *
     * 1 means that the battery is "ok"
     * 0 means that the battery is between "ok" and what we should warn about.
     * less than 0 means that the battery is low
     */
    private int findBatteryLevelBucket(int level) {
        if (level >= mLowBatteryAlertCloseLevel) {
            return 1;
        }
        /// M: Excluded level 0 for LowBatteryReminder. 
        if (level > mLowBatteryReminderLevels[0]) {
            return 0;
        }
        final int N = mLowBatteryReminderLevels.length;
        for (int i=N-1; i>=0; i--) {
            if (level <= mLowBatteryReminderLevels[i]) {
                return -1-i;
            }
        }
        throw new RuntimeException("not possible!");
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "action = " + action);
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                /// M: Hide low battery dialog when PowerOffAlarm ring. @{
                String bootReason = SystemProperties.get("sys.boot.reason");
                boolean ret = (bootReason != null && bootReason.equals("1")) ? true : false;
                Xlog.d(TAG, "Intent start() ret = " + ret + " mHideLowBDialog= " + mHideLowBDialog);
                if (ret && mHideLowBDialog) {
                    return;
                }
                /// M: Hide low battery dialog when PowerOffAlarm ring. @}
                final int oldBatteryLevel = mBatteryLevel;
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100);
                Xlog.d(TAG, "oldBatteryLevel = " + oldBatteryLevel + "mBatteryLevel = " + mBatteryLevel);
                final int oldBatteryStatus = mBatteryStatus;
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
                        BatteryManager.BATTERY_STATUS_UNKNOWN);
                final int oldPlugType = mPlugType;
                mPlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 1);
                final int oldInvalidCharger = mInvalidCharger;
                mInvalidCharger = intent.getIntExtra(BatteryManager.EXTRA_INVALID_CHARGER, 0);
                
             // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog begin
                
             // Aurora <tongyh> <2014-06-19> update battery's unusual toast begin
//                final int oldUsbHealth = mUsbHealth;
//                mUsbHealth = intent.getIntExtra(EXTRA_USB_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD);
             // Aurora <tongyh> <2014-06-19> update battery's unusual toast end
                final int oldHeat = mHeat;
                mHeat= intent.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD);
                Slog.d(TAG,"gndb:mHeat ="+mHeat);
             // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog end
                
                final boolean plugged = mPlugType != 0;
                final boolean oldPlugged = oldPlugType != 0;

                /// M: Support "Low Battery Sound". @{
                if (mInBatteryLow && mMediaPlayerInUse) {
                    if (plugged) {
                        if (mNP != null) {
                            mNP.stop();
                        }
                    }
                }
                /// M: Support "Low Battery Sound". @}
                int oldBucket = findBatteryLevelBucket(oldBatteryLevel);
                int bucket = findBatteryLevelBucket(mBatteryLevel);

                if (DEBUG) {
                    Slog.d(TAG, "buckets   ....." + mLowBatteryAlertCloseLevel
                            + " .. " + mLowBatteryReminderLevels[0]
                            + " .. " + mLowBatteryReminderLevels[1]);
                    Slog.d(TAG, "level          " + oldBatteryLevel + " --> " + mBatteryLevel);
                    Slog.d(TAG, "status         " + oldBatteryStatus + " --> " + mBatteryStatus);
                    Slog.d(TAG, "plugType       " + oldPlugType + " --> " + mPlugType);
                    Slog.d(TAG, "invalidCharger " + oldInvalidCharger + " --> " + mInvalidCharger);
                    Slog.d(TAG, "bucket         " + oldBucket + " --> " + bucket);
                    Slog.d(TAG, "plugged        " + oldPlugged + " --> " + plugged);
                }

                if (oldInvalidCharger == 0 && mInvalidCharger != 0) {
                    Slog.d(TAG, "showing invalid charger warning");
                    showInvalidChargerDialog();
                    return;
                } else if (oldInvalidCharger != 0 && mInvalidCharger == 0) {
                    Xlog.d(TAG, "dismissInvalidChargerDialog");
                    dismissInvalidChargerDialog();
                } else if (mInvalidChargerDialog != null) {
                    // if invalid charger is showing, don't show low battery
                    return;
                }
                
             // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog begin
                //over voltage   dian ya guo gao
             // Aurora <tongyh> <2014-06-19> update battery's unusual toast begin
                /*if(oldUsbHealth != BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE 
                    && mUsbHealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){*/
                if(oldHeat != BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE && mHeat == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){
                	// Aurora <tongyh> <2014-06-19> update battery's unusual toast end
                	 Slog.d(TAG, "BATTERY_HEALTH_OVER_VOLTAGE");
                    showOverVoltageDialog();
                    return;
                }
                // Aurora <tongyh> <2014-06-19> update battery's unusual toast begin
                /*else if(oldUsbHealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE 
                    && mUsbHealth != BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){*/
                else if(oldHeat == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE && mHeat!= BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE){
                // Aurora <tongyh> <2014-06-19> update battery's unusual toast end
                	Slog.d(TAG, "BATTERY_HEALTH_OVER_VOLTAGE----dismiss");
                    dismissOverVoltageDialog();
                }else if (mOverVoltageDialog!= null) {
                    // if over voltage is showing, don't show low battery
                	Slog.d(TAG, "BATTERY_HEALTH_OVER_VOLTAGE----return");
                    return;
                }
                
                //over heat  guo re
                if(oldHeat != BatteryManager.BATTERY_HEALTH_OVERHEAT &&
                    mHeat== BatteryManager.BATTERY_HEALTH_OVERHEAT){
                	Slog.d(TAG, "BATTERY_HEALTH_OVERHEAT");
                    showOverHeatDialog();
                    return;
                }else if(oldHeat == BatteryManager.BATTERY_HEALTH_OVERHEAT &&
                    mHeat!= BatteryManager.BATTERY_HEALTH_OVERHEAT){
                	Slog.d(TAG, "BATTERY_HEALTH_OVERHEAT---dismiss");
                    dismissOverHeatDialog();
                }else if (mOverHeatDialog!= null) {
                    // if over heat is showing, don't show low battery
                	Slog.d(TAG, "BATTERY_HEALTH_OVERHEAT---return");
                    return;
                }
                
                //cold
                if(oldHeat != BatteryManager.BATTERY_HEALTH_COLD &&
                		mHeat == BatteryManager.BATTERY_HEALTH_COLD){
                	Slog.d(TAG, "BATTERY_HEALTH_COLD");
                	if(mPlugType != 0){
                		showColdDialog();
                	}
                        return;
                    }else if(oldHeat == BatteryManager.BATTERY_HEALTH_COLD &&
                    		mHeat!= BatteryManager.BATTERY_HEALTH_COLD){
                    	Slog.d(TAG, "BATTERY_HEALTH_COLD---dismiss");
                        dismissColdDialog();
                    }else if (mColdDialog!= null) {
                        // if over heat is showing, don't show low battery
                    	Slog.d(TAG, "BATTERY_HEALTH_COLD---return");
                        return;
                    }
             // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog end


                if (!plugged
                        && (bucket < oldBucket || oldPlugged)
                        && mBatteryStatus != BatteryManager.BATTERY_STATUS_UNKNOWN
                        && bucket < 0) {
                    showLowBatteryWarning();

                    // only play SFX when the dialog comes up or the bucket changes
                    if (bucket != oldBucket || oldPlugged) {
                        Xlog.d(TAG, "playLowBatterySound1");
                        playLowBatterySound();
                    }
                } else if (plugged || (bucket > oldBucket && bucket > 0)) {
                    Xlog.d(TAG, "dismissLowBatteryWarning");
                    dismissLowBatteryWarning();
                    /// M: Support "Low Battery Sound". @{
                    mNP.stop();
                    /// M: Support "Low Battery Sound". @}
                } else if (mBatteryLevelTextView != null) {
                    showLowBatteryWarning();
                }
                /// M: Support "Low Battery Sound". @{
            } else if (action.equals(Intent.ACTION_BATTERY_LOW)) {
                mInBatteryLow = true;
            } else if (action.equals(Intent.ACTION_BATTERY_OKAY) || action.equals(Intent.ACTION_POWER_CONNECTED)) {
                mInBatteryLow = false;
            /// M: Support "Low Battery Sound". @}
            /// M: Support show battery level when configuration changed. @{
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                if (mLowBatteryDialog != null && mLowBatteryDialog.isShowing()) {
                	// Aurora <tongyh> <2013-11-11> Adjust low battery dialog begin
//                    CharSequence levelText = mContext.getString(
//                            R.string.battery_low_percent_format, mBatteryLevel);
                    CharSequence levelText = mContext.getString(
                            R.string.battery_low_content, mBatteryLevel);
                 // Aurora <tongyh> <2013-11-11> Adjust low battery dialog end
                    if (mBatteryLevelTextView != null) {
                        mBatteryLevelTextView.setText(levelText);
                    } else {
                        View v = View.inflate(mContext, R.layout.battery_low, null);
                        mBatteryLevelTextView = (TextView) v.findViewById(R.id.level_percent);
                        mBatteryLevelTextView.setText(levelText);
                    }
                }
            /// M: Support show battery level when configuration changed. @}
            /// M: Hide low battery dialog when PowerOffAlarm ring. @{
            } else if (action.equals("android.intent.action.normal.boot")) {
                Xlog.d(TAG, "Intent android.intent.action.normal.boot mHideLowBDialog = " + mHideLowBDialog);
                mHideLowBDialog = false;
            } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
                Xlog.d(TAG, "Intent android.intent.action.ACTION_SHUTDOWN_IPO mHideLowBDialog = " + mHideLowBDialog);
                mHideLowBDialog = true;
                /// M: Support show low battery dialog in IPO boot.
                mBatteryLevel = 100;
            /// M: Hide low battery dialog when PowerOffAlarm ring. @}
            } else {
                Slog.w(TAG, "unknown intent: " + intent);
            }
        }
    };

    void dismissLowBatteryWarning() {
        if (mLowBatteryDialog != null) {
            Slog.i(TAG, "closing low battery warning: level=" + mBatteryLevel);
            mLowBatteryDialog.dismiss();
        }
    }

    void showLowBatteryWarning() {
        Slog.i(TAG,
                ((mBatteryLevelTextView == null) ? "showing" : "updating")
                + " low battery warning: level=" + mBatteryLevel
                + " [" + findBatteryLevelBucket(mBatteryLevel) + "]");
     // Aurora <tongyh> <2013-11-11> Adjust low battery dialog begin
//        CharSequence levelText = mContext.getString(
//                R.string.battery_low_percent_format, mBatteryLevel);
        CharSequence levelText = mContext.getString(
                R.string.battery_low_content, mBatteryLevel);
     // Aurora <tongyh> <2013-11-11> Adjust low battery dialog end
        if (mBatteryLevelTextView != null) {
            mBatteryLevelTextView.setText(levelText);
        } else {
            View v = View.inflate(mContext, R.layout.battery_low, null);
            mBatteryLevelTextView = (TextView)v.findViewById(R.id.level_percent);

            mBatteryLevelTextView.setText(levelText);

            // Gionee fengjianyi 2012-12-24 modify for CR00751916 start
            /*
            AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setCancelable(true);
                b.setTitle(R.string.battery_low_title);
                b.setView(v);
                b.setIconAttribute(android.R.attr.alertDialogIcon);
                b.setPositiveButton(android.R.string.ok, null);
            */
            AuroraAlertDialog.Builder b;
            if (GN_WIDGET_SUPPORT) {
            	b = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
            } else {
            	b = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
            	b.setCancelable(true);
            	// Aurora <tongyh> <2013-11-11> Adjust low battery dialog begin           	
//            	b.setIconAttribute(android.R.attr.alertDialogIcon);
            	// Aurora <tongyh> <2013-11-11> Adjust low battery dialog end     	
            }
            b.setTitle(R.string.battery_low_title);
            b.setView(v);
            b.setPositiveButton(android.R.string.ok, null);
            // Gionee fengjianyi 2012-12-24 modify for CR00751916 end

            final Intent intent = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                    | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    | Intent.FLAG_ACTIVITY_NO_HISTORY);
         // Aurora <tongyh> <2013-11-11> Adjust low battery dialog begin           
//            if (intent.resolveActivity(mContext.getPackageManager()) != null) {
//                b.setNegativeButton(R.string.battery_low_why,
//                        new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        mContext.startActivityAsUser(intent, UserHandle.CURRENT);
//                        dismissLowBatteryWarning();
//                    }
//                });
//            }
         // Aurora <tongyh> <2013-11-11> Adjust low battery dialog end  

            AuroraAlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mLowBatteryDialog = null;
                        mBatteryLevelTextView = null;
                    }
                });
            d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            // Gionee fengjianyi 2013-01-07 add for CR00751916 start
            if (GN_WIDGET_SUPPORT) {
                d.setCanceledOnTouchOutside(false);
            }
            // Gionee fengjianyi 2013-01-07 add for CR00751916 end
            d.getWindow().getAttributes().privateFlags |=
                    WindowManager.LayoutParams.PRIVATE_FLAG_SHOW_FOR_ALL_USERS;
            d.show();
            mLowBatteryDialog = d;
        }
    }

    void playLowBatterySound() {
        if (DEBUG) {
            Slog.i(TAG, "playing low battery sound. WOMP-WOMP!");
        }

        /// M: Support "Low Battery Sound". @{
        Xlog.d(TAG, "playLowBatterySound");
        Message msg = Message.obtain();
        msg.what = EVENT_LOW_BATTERY_WARN_SOUND;
        mHandler.sendMessage(msg);
        /// M: Support "Low Battery Sound". @}

        final ContentResolver cr = mContext.getContentResolver();
        if (Settings.Global.getInt(cr, Settings.Global.POWER_SOUNDS_ENABLED, 1) == 1) {
            final String soundPath = Settings.Global.getString(cr,
                    Settings.Global.LOW_BATTERY_SOUND);
            if (soundPath != null) {
                final Uri soundUri = Uri.parse("file://" + soundPath);
                if (soundUri != null) {
                    final Ringtone sfx = RingtoneManager.getRingtone(mContext, soundUri);
                    /// M: If no NotificationPlayer,Use Ringtone.
                    if (sfx != null && mNP == null) {
                        sfx.setStreamType(AudioManager.STREAM_SYSTEM);
                        sfx.play();
                    }
                }
            }
        }
    }

    void dismissInvalidChargerDialog() {
        if (mInvalidChargerDialog != null) {
            mInvalidChargerDialog.dismiss();
        }
    }

    void showInvalidChargerDialog() {
        Slog.d(TAG, "showing invalid charger dialog");

        dismissLowBatteryWarning();

        // Gionee fengjianyi 2012-12-24 modify for CR00751916 start
        /*
        AlertDialog.Builder b = new AlertDialog.Builder(mContext);
            b.setCancelable(true);
            b.setMessage(R.string.invalid_charger);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            b.setPositiveButton(android.R.string.ok, null);
        */
        AuroraAlertDialog.Builder b;
        if (GN_WIDGET_SUPPORT) {
        	b = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        } else {
        	b = new AuroraAlertDialog.Builder(mContext, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN);
        	b.setCancelable(true);
        	b.setIconAttribute(android.R.attr.alertDialogIcon);
        }
        b.setMessage(R.string.invalid_charger);
        b.setPositiveButton(android.R.string.ok, null);
        // Gionee fengjianyi 2012-12-24 modify for CR00751916 end

        AuroraAlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mInvalidChargerDialog = null;
                        mBatteryLevelTextView = null;
                    }
                });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        // Gionee fengjianyi 2013-01-07 add for CR00751916 start
        if (GN_WIDGET_SUPPORT) {
            d.setCanceledOnTouchOutside(false);
        }
        // Gionee fengjianyi 2013-01-07 add for CR00751916 end
        d.show();
        mInvalidChargerDialog = d;
    }
    
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("mLowBatteryAlertCloseLevel=");
        pw.println(mLowBatteryAlertCloseLevel);
        pw.print("mLowBatteryReminderLevels=");
        pw.println(Arrays.toString(mLowBatteryReminderLevels));
        pw.print("mInvalidChargerDialog=");
        pw.println(mInvalidChargerDialog == null ? "null" : mInvalidChargerDialog.toString());
        pw.print("mLowBatteryDialog=");
        pw.println(mLowBatteryDialog == null ? "null" : mLowBatteryDialog.toString());
        pw.print("mBatteryLevel=");
        pw.println(Integer.toString(mBatteryLevel));
        pw.print("mBatteryStatus=");
        pw.println(Integer.toString(mBatteryStatus));
        pw.print("mPlugType=");
        pw.println(Integer.toString(mPlugType));
        pw.print("mInvalidCharger=");
        pw.println(Integer.toString(mInvalidCharger));
        pw.print("bucket: ");
        pw.println(Integer.toString(findBatteryLevelBucket(mBatteryLevel)));
    }

    /// M: Hide low battery dialog when PowerOffAlarm ring.
    private boolean mHideLowBDialog = true;
    /// M: Support "Low Battery Sound". @{
    private static final int EVENT_LOW_BATTERY_WARN_SOUND = 10;
    private ToneGenerator mToneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
    private NotificationPlayer mNP = new NotificationPlayer("StatusBarPolicy");
    private static final String SOUNDDIRECTORY = "/system/media/audio/ui/";
    private boolean mInBatteryLow = false;
    private boolean mMediaPlayerInUse = false;

    private class BatteryHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case EVENT_LOW_BATTERY_WARN_SOUND:
                final AudioManager audioManager = (AudioManager) mContext
                        .getSystemService(Context.AUDIO_SERVICE);
                String path = findTestFile("battery");
                if (mNP == null) {
                    mNP = new NotificationPlayer("StatusBarPolicy");
                }
                if (path != null) {
                    mMediaPlayerInUse = true;
                    String totolPath = SOUNDDIRECTORY + path;
                    File soundFile = new File(totolPath);
                    if (audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM) != 0) {
                        Xlog.d(TAG, "handleMessage, soundFile=" + soundFile);
                        mNP.play(mContext, Uri.fromFile(soundFile), false, AudioManager.STREAM_SYSTEM);
                    }
                } else {
                    mMediaPlayerInUse = false;
                    if (mToneGenerator != null) {
                        mToneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP);
                    }
                }
                break;
            default:
                break;
            }
        }
    }

    private String findTestFile(final String name) {
        Xlog.d(TAG, "findTestFile, name=" + name);
        File directory = new File("/system/media/audio/ui/");
        File[] list = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.startsWith(name);
            }
        });
        if (list.length > 0) {
            File file = list[0];
            for (int i = 1; i < list.length; i++) {
                if (list[i].lastModified() < file.lastModified()) {
                    file = list[i];
                }
            }
            return file.getName();
        }
        Xlog.d(TAG, "return = null");
        return null;
    }
    /// M: Support "Low Battery Sound". @}
    
 // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog begin
    void dismissOverVoltageDialog() {
        if (mOverVoltageDialog!= null) {
            mOverVoltageDialog.dismiss();
        }
    }
    void showOverVoltageDialog() {
        if(mOverVoltageDialog != null){
            return;
        }
        Slog.d(TAG, "showing overVolt health dialog");

        dismissLowBatteryWarning();

        AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);
            b.setCancelable(true);
            b.setMessage(R.string.zzzzz_gn_charging_over_voltage);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            b.setPositiveButton(android.R.string.ok, null);

            AuroraAlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mOverVoltageDialog= null;
                        mBatteryLevelTextView = null;
                    }
                });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
        mOverVoltageDialog= d;
    }
    
     void dismissOverHeatDialog() {
        if (mOverHeatDialog!= null) {
            mOverHeatDialog.dismiss();
        }
    }
    void showOverHeatDialog() {
        if(mOverHeatDialog != null){
            return;
        }
        Slog.d(TAG, "showing overVolt health dialog");

        dismissLowBatteryWarning();
        int titleId = 0;
        int messageId = 0;
        if(mPlugType != 0){
            titleId = R.string.zzzzz_gn_warn_title;
            messageId = R.string.zzzzz_gn_charging_over_heat;
        }else{
            titleId = R.string.zzzzz_gn_warn_title_unplug;
            messageId = R.string.zzzzz_gn_charging_over_heat_unplug;
        }
        
        AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);
            b.setCancelable(true);
            b.setTitle(titleId);
            b.setMessage(messageId);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            b.setPositiveButton(android.R.string.ok, null);

            AuroraAlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        mOverHeatDialog= null;
                        mBatteryLevelTextView = null;
                    }
                });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
        mOverHeatDialog= d;
    }
    
    
    void showColdDialog(){
    	if(mColdDialog != null){
            return;
        }
        Slog.d(TAG, "showing cold health dialog");

        dismissLowBatteryWarning();
        int titleId = 0;
        int messageId = 0;
        if(mPlugType != 0){
            titleId = R.string.zzzzz_gn_warn_title;
            messageId = R.string.zzzzz_gn_charging_cold;
        }else{
            titleId = R.string.zzzzz_gn_warn_title_unplug;
            messageId = R.string.zzzzz_gn_charging_cold_unplug;
        }
        
        AuroraAlertDialog.Builder b = new AuroraAlertDialog.Builder(mContext);
            b.setCancelable(true);
            b.setTitle(titleId);
            b.setMessage(messageId);
            b.setIconAttribute(android.R.attr.alertDialogIcon);
            b.setPositiveButton(android.R.string.ok, null);

            AuroraAlertDialog d = b.create();
            d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                    	mColdDialog= null;
                        mBatteryLevelTextView = null;
                    }
                });

        d.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        d.show();
        mColdDialog= d;
    }
    
    void dismissColdDialog(){
    	if (mColdDialog!= null) {
    		mColdDialog.dismiss();
        }
    }
    
    
 // Aurora <tongyh> <2014-03-23> add  OverVoltageDialog and OverHeatDialog end
}


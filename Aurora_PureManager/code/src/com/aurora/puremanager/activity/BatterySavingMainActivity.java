package com.aurora.puremanager.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.BatteryStats;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.aurora.puremanager.R;
import com.aurora.puremanager.service.SmartPowerService;
import com.aurora.puremanager.utils.CommonUtils;
import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.utils.PowerTimeUtil;
import com.aurora.puremanager.utils.TimeUtils;

import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;

public class BatterySavingMainActivity extends AuroraActivity implements View.OnClickListener {

    private final String TAG = "BatterySavingActivity";
    private Context mContext;
    private ImageView batteryDynamicIcon;
    private ImageView topHint;
    private ImageView usbIcon;
    private TextView percentTv;
    private TextView stateTv;
    private TextView longStandTv;
    private int percent = 0;
    private static int BATTERY_ICON_TOTAL_HEIGHT = 118;
    private IBatteryStats mBatteryInfo;
    private AuroraSwitch mSwitch;
    private USBBroadcastReceiver usbReceiver = null;
    private BatteryReceiver batteryReceiver = null;
    private boolean INIT_ENV = false;
    private TextView standbyTv;
    private TextView dialTv;
    private TextView videoTv;
    private TextView musicTv;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setAuroraContentView(R.layout.activity_bettery_saving, AuroraActionBar.Type.Normal);
        initActionBar();

        initViews();
        registerReceiver();
        initData();
        setListener();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    private void initData() {
        mBatteryInfo = IBatteryStats.Stub.asInterface(ServiceManager.getService(BatteryStats.SERVICE_NAME));
        SharedPreferences sp = getSharedPreferences(SmartPowerService.SETTINGS_VALUE, MODE_PRIVATE);
        if (sp.getInt(SmartPowerService.SMART_STATE, SmartPowerService.STATE_NORMAL)
                == SmartPowerService.STATE_NORMAL) {
            mSwitch.setChecked(false);
        } else {
            mSwitch.setChecked(true);
        }
    }

    private void registerReceiver() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        usbFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        usbReceiver = new USBBroadcastReceiver();
        usbFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(usbReceiver, usbFilter);

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, batteryFilter);
    }

    private void unregisterReceiver() {
        unregisterReceiver(batteryReceiver);
        unregisterReceiver(usbReceiver);
    }

    private void initViews() {
        batteryDynamicIcon = (ImageView) findViewById(R.id.battery_icon_dynamic);
        percentTv = (TextView) findViewById(R.id.percent_tv);
        usbIcon = (ImageView) findViewById(R.id.usb_icon);
        topHint = (ImageView) findViewById(R.id.battery_usb_hint_icon);
        stateTv = (TextView) findViewById(R.id.hint_text);
        mSwitch = (AuroraSwitch) findViewById(R.id.mode_switch);
        longStandTv = (TextView) findViewById(R.id.long_stand_tv);
        standbyTv = (TextView) findViewById(R.id.standby_time_tv);
        dialTv = (TextView) findViewById(R.id.dial_time_tv);
        videoTv = (TextView) findViewById(R.id.video_time_tv);
        musicTv = (TextView) findViewById(R.id.music_time_tv);
    }

    private void setListener() {
        findViewById(R.id.consume_ll).setOnClickListener(this);
        findViewById(R.id.long_stand_ll).setOnClickListener(this);
        findViewById(R.id.mode_switch_ll).setOnClickListener(this);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                mSwitch.setClickable(false);
                Intent intent = new Intent(mContext, SmartPowerService.class);
                if (isChecked) {
                    intent.putExtra(SmartPowerService.SMART_STATE, true);
                } else {
                    intent.putExtra(SmartPowerService.SMART_STATE, false);
                }
                startServiceAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
                registerReceiver(broadcastReceiver, new IntentFilter(SmartPowerService.ACTION));
                freshUsageTime();
            }
        });
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwitch.setClickable(true);
            unregisterReceiver(this);
        }
    };

    public void initActionBar() {
        setAuroraActionbarSplitLineVisibility(View.GONE);
        AuroraActionBar bar = getAuroraActionBar();
        bar.setBackgroundResource(R.color.power_green);
        bar.setTitle(R.string.power_manager);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.consume_ll:
                Intent consumeIntent = new Intent(mContext, BatteryConsumeActivity.class);
                startActivity(consumeIntent);
                break;
            case R.id.long_stand_ll:
                Intent saveIntent = new Intent(mContext, SuperStandbyAcitivity.class);
                saveIntent.putExtra(SuperStandbyAcitivity.LEVEL_KEY, percent);
                startActivity(saveIntent);
                break;
            case R.id.mode_switch_ll:
                mSwitch.setChecked(!mSwitch.isChecked());
                break;
            default:
                break;
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                percent = (level * 100) / scale;

                ViewGroup.LayoutParams lp = batteryDynamicIcon.getLayoutParams();
                lp.height = CommonUtils.dip2px(mContext, (percent * BATTERY_ICON_TOTAL_HEIGHT) / 100);
                batteryDynamicIcon.setLayoutParams(lp);
                percentTv.setText(percent + "");
                longStandTv.setText(PowerTimeUtil.getTimeStrInSuperMode(percent));

                freshUsageTime();

                if (!INIT_ENV) {
                    initUsbState(intent);
                }
            }
        }
    }

    private void freshUsageTime() {
        List<String> timeList = PowerTimeUtil.getMiscStandbyTime(percent, mSwitch.isChecked());
        if (timeList != null && timeList.size() == 4) {
            standbyTv.setText(timeList.get(0));
            dialTv.setText(timeList.get(1));
            videoTv.setText(timeList.get(2));
            musicTv.setText(timeList.get(3));
        }
        if (mSwitch.isChecked()) {
            stateTv.setText(getString(R.string.smart_open_hint));
        } else {
            stateTv.setText(getString(R.string.smart_close_hint));
        }
    }

    @Override
    protected void onPause() {
        INIT_ENV = false;
        super.onPause();
    }

    private void initUsbState(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) ||
                (status == BatteryManager.BATTERY_STATUS_FULL);

        if (isCharging) {
            usbIcon.setVisibility(View.VISIBLE);
            if (percent == 100) {
                topHint.setVisibility(View.GONE);
                //stateTv.setText(R.string.is_full);
            } else {
                topHint.setVisibility(View.VISIBLE);
                //String formattedTime = getChargeTimeStr();
                //stateTv.setText(formattedTime);
            }
        } else {
            usbIcon.setVisibility(View.INVISIBLE);
            topHint.setVisibility(View.GONE);
            //String formattedTime = getIdleTimeStr();
            //stateTv.setText(formattedTime);
        }
        INIT_ENV = true;
    }

    public class USBBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED)) {
                usbIcon.setVisibility(View.VISIBLE);
                if (percent == 100) {
                    topHint.setVisibility(View.GONE);
                    //stateTv.setText(R.string.is_full);
                } else {
                    topHint.setVisibility(View.VISIBLE);
                    //String formattedTime = getChargeTimeStr();
                    //stateTv.setText(formattedTime);
                }
            } else {
                usbIcon.setVisibility(View.INVISIBLE);
                topHint.setVisibility(View.GONE);
                //String formattedTime = getIdleTimeStr();
                //stateTv.setText(formattedTime);
            }
        }
    }

    private String getChargeTimeStr() {
        long timeFromSystem = 0;
        String formattedTime;
        try {
            timeFromSystem = mBatteryInfo.computeChargeTimeRemaining();
            Log.e(TAG, "getChargeTimeStr = " + timeFromSystem);
        } catch (RemoteException e) {
            e.printStackTrace();
            timeFromSystem = 0;
        }
        if (timeFromSystem <= 0) {
            formattedTime = getString(R.string.is_charging);
            return formattedTime;
        } else {
            formattedTime = Formatter.formatShortElapsedTime(mContext, timeFromSystem);
            return "充电还需:" + formattedTime;
        }
    }

    private String getIdleTimeStr() {
        long timeFromSystem = 0;
        String formattedTime;
        try {
            timeFromSystem = mBatteryInfo.computeBatteryTimeRemaining();
        } catch (RemoteException e) {
            e.printStackTrace();
            timeFromSystem = 0;
        }
        if (timeFromSystem <= 0) {
            formattedTime = getString(R.string.can_ongoing);
            return "可待机:" + formattedTime;
        } else {
            //formattedTime = Formatter.formatShortElapsedTime(mContext, timeFromSystem);
            formattedTime = TimeUtils.MillsToHHMM(timeFromSystem);
            return "可待机:" + formattedTime;
        }
    }
}

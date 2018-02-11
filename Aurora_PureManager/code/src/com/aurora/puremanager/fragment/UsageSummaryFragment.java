package com.aurora.puremanager.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.BatteryStats;
import android.os.BatteryStats.Uid;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.telephony.SignalStrength;
import android.text.format.Formatter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.internal.app.IBatteryStats;
import com.android.internal.os.BatteryStatsHelper;
import com.android.internal.os.BatteryStatsImpl;
import com.android.internal.os.PowerProfile;
import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.PowerUsageDetailActivity;
import com.aurora.puremanager.utils.BatteryInfoUtils;
import com.aurora.puremanager.utils.DrainType;
import com.aurora.puremanager.utils.HelperUtils;
import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.utils.TimeUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static android.os.BatteryStats.NETWORK_MOBILE_RX_DATA;
import static android.os.BatteryStats.NETWORK_MOBILE_TX_DATA;
import static android.os.BatteryStats.NETWORK_WIFI_RX_DATA;
import static android.os.BatteryStats.NETWORK_WIFI_TX_DATA;

public class UsageSummaryFragment extends Fragment implements OnItemClickListener, Runnable {
    private static final boolean DEBUG = false;
    private static final String TAG = "UsageSummaryFragment";

    private BatteryStatsImpl sStatsXfer;
    IBatteryStats mBatteryInfo;
    BatteryStatsImpl mStats;
    private List<BatterySipper> mHardwareList = new ArrayList<BatterySipper>();
    private List<BatterySipper> mSoftwareList = new ArrayList<BatterySipper>();
    private List<BatterySipper> mWifiSippers = new ArrayList<BatterySipper>();
    private List<BatterySipper> mBluetoothSippers = new ArrayList<BatterySipper>();
    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;
    private static final int MAX_ITEMS_TO_LIST = 10;
    // private long mStatsPeriod = 0;
    // private double mMaxPower = 1;
    private double mSoftTotalPower;
    private double mHardTotalPower;
    private double mTotalPower;
    private double mWifiPower;
    private double mBluetoothPower;

    //private double mGpsPower;
    //private double mOtherSensorsPower;
    private long mAppMobileActive;
    private PowerProfile mPowerProfile;
    private long mAppWifiRunning;
    private ArrayList<BatterySipper> mRequestQueue = new ArrayList<BatterySipper>();
    private Thread mRequestThread;
    private boolean mAbort;
    private ListView mListView;
    private SummaryAppListAdapter mAdapter;
    long mRawRealtime;
    long mTypeBatteryRealtime;
    private BatteryStatsHelper mStatsHelper;
    private UserManager mUm;
    private List<com.android.internal.os.BatterySipper> usageList;
    public static Drawable iconForDetail = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mStats = sStatsXfer;
        }

        mBatteryInfo = BatteryInfoUtils.getBatteryInfo();

        mPowerProfile = new PowerProfile(getActivity());
        mAdapter = new SummaryAppListAdapter(getActivity());
        mStatsHelper.create(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.powersaver_usage_summary_fragment, container, false);
        mListView = (ListView) view.findViewById(R.id.usage_summary_app_list);
        mListView.setItemsCanFocus(true);
        mListView.setAdapter(mAdapter);

        LinearLayout header = (LinearLayout) getLayoutInflater(null).inflate(R.layout.list_header_ly, null);
        mListView.addHeaderView(header);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mStatsHelper.clearStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        mAbort = false;
        mStatsHelper.clearStats();
        refreshStats();
    }

    @Override
    public void onPause() {
        super.onPause();
        synchronized (mRequestQueue) {
            mAbort = true;
        }
        mHandler.removeMessages(MSG_UPDATE_NAME_ICON);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mStatsHelper = new BatteryStatsHelper(activity, true);
        mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSoftwareList.clear();
        mHardwareList.clear();
        mWifiSippers.clear();
        mBluetoothSippers.clear();
        mSoftwareList = null;
        mHardwareList = null;
        mWifiSippers = null;

        if (getActivity().isChangingConfigurations()) {
            sStatsXfer = mStats;
        }
    }

    private void refreshStats() {
        if (mStats == null) {
            load();
        }
        //mMaxPower = 0;
        mSoftTotalPower = 0;
        mHardTotalPower = 0;
        mTotalPower = 0;
        mWifiPower = 0;
        mBluetoothPower = 0;
        mAppMobileActive = 0;
        // mGpsPower = 0;
        mAppWifiRunning = 0;
        mHardwareList.clear();
        mSoftwareList.clear();
        mWifiSippers.clear();
        mBluetoothSippers.clear();
        mRawRealtime = SystemClock.elapsedRealtime() * 1000;

        mTypeBatteryRealtime = mStats.computeBatteryRealtime(mRawRealtime, mStatsType);

        processAppUsage();
        processMiscUsage();

        mTotalPower = mHardTotalPower + mSoftTotalPower;

        // 设置软硬件百分比
        Bundle bundle = getArguments();
        int arg = bundle.getInt("fragment_key");
        int softPercent = (int) Math.round((mSoftTotalPower * 100) / mTotalPower);
        if (arg == 0) {
            mListView.setOnItemClickListener(this);
            StringBuilder timeStr = new StringBuilder().
                    append("已使用").
                    append(TimeUtils.MillsToHHMM(SystemClock.uptimeMillis()))//.
                    /*append("，耗电占比").
                    append(softPercent).
                    append("%")*/;
            ((TextView) ((mListView.getAdapter().getView(0, null, mListView))
                    .findViewById(R.id.list_head_text)))
                    .setText(timeStr.toString());
        } else {
            mListView.setOnItemClickListener(this);
            StringBuilder timeStr = new StringBuilder().
                    append("已使用").
                    append(TimeUtils.MillsToHHMM(SystemClock.uptimeMillis()))//.
                    /*append("，耗电占比").
                    append(100 - softPercent).
                    append("%")*/;
            ((TextView) ((mListView.getAdapter().getView(0, null, mListView))
                    .findViewById(R.id.list_head_text)))
                    .setText(timeStr.toString());
        }

        synchronized (mRequestQueue) {
            if (!mRequestQueue.isEmpty()) {
                if (mRequestThread == null) {
                    mRequestThread = new Thread(UsageSummaryFragment.this, "BatteryUsage Icon Loader");
                    mRequestThread.setPriority(Thread.MIN_PRIORITY);
                    mRequestThread.start();
                }
                mRequestQueue.notify();
            }
        }

        if (arg == 0) {
            refreshList(mSoftwareList);
        } else {
            refreshList(mHardwareList);
        }
    }

    private void processAppUsage() {
        SensorManager sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        final int which = mStatsType;
        final int speedSteps = mPowerProfile.getNumSpeedSteps();
        final double[] powerCpuNormal = new double[speedSteps];
        final long[] cpuSpeedStepTimes = new long[speedSteps];
        for (int p = 0; p < speedSteps; p++) {
            powerCpuNormal[p] = mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_ACTIVE, p);
        }
        final double mobilePowerPerPacket = getMobilePowerPerPacket();
        final double mobilePowerPerMs = getMobilePowerPerMs();
        final double wifiPowerPerPacket = getWifiPowerPerPacket();
        long appWakelockTimeUs = 0;
        BatterySipper osApp = null;
        //mStatsPeriod = mTypeBatteryRealtime;
        SparseArray<? extends Uid> uidStats = mStats.getUidStats();
        final int NU = uidStats.size();
        for (int iu = 0; iu < NU; iu++) {
            Uid u = uidStats.valueAt(iu);
            double p; // in mAs
            double power = 0; // in mAs
            double highestDrain = 0;
            String packageWithHighestDrain = null;
            Map<String, ? extends Uid.Proc> processStats = u.getProcessStats();
            long cpuTime = 0;
            long cpuFgTime = 0;
            long wakelockTime = 0;
            long gpsTime = 0;
            if (processStats.size() > 0) {
                // Process CPU time
                for (Map.Entry<String, ? extends Uid.Proc> ent : processStats.entrySet()) {
                    Uid.Proc ps = ent.getValue();
                    final long userTime = ps.getUserTime(which);
                    final long systemTime = ps.getSystemTime(which);
                    final long foregroundTime = ps.getForegroundTime(which);
                    cpuFgTime += foregroundTime * 10; // convert to millis
                    final long tmpCpuTime = (userTime + systemTime) * 10; // convert
                    // to
                    // millis
                    int totalTimeAtSpeeds = 0;
                    // Get the total first
                    for (int step = 0; step < speedSteps; step++) {
                        cpuSpeedStepTimes[step] = ps.getTimeAtCpuSpeedStep(step, which);
                        totalTimeAtSpeeds += cpuSpeedStepTimes[step];
                    }
                    if (totalTimeAtSpeeds == 0)
                        totalTimeAtSpeeds = 1;
                    // Then compute the ratio of time spent at each speed
                    double processPower = 0;
                    for (int step = 0; step < speedSteps; step++) {
                        double ratio = (double) cpuSpeedStepTimes[step] / totalTimeAtSpeeds;
                        if (DEBUG && ratio != 0)
                            Log.d(TAG, "UID " + u.getUid() + ": CPU step #" + step + " ratio="
                                    + makemAh(ratio) + " power="
                                    + makemAh(ratio * tmpCpuTime * powerCpuNormal[step] / (60 * 60 * 1000)));
                        processPower += ratio * tmpCpuTime * powerCpuNormal[step];
                    }
                    cpuTime += tmpCpuTime;
                    if (DEBUG && processPower != 0) {
                        Log.d(TAG,
                                String.format("process %s, cpu power=%s", ent.getKey(), makemAh(processPower
                                        / (60 * 60 * 1000))));
                    }
                    power += processPower;
                    if (packageWithHighestDrain == null || packageWithHighestDrain.startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    } else if (highestDrain < processPower && !ent.getKey().startsWith("*")) {
                        highestDrain = processPower;
                        packageWithHighestDrain = ent.getKey();
                    }
                }
            }
            if (cpuFgTime > cpuTime) {
                if (DEBUG && cpuFgTime > cpuTime + 10000) {
                    Log.d(TAG, "WARNING! Cputime is more than 10 seconds behind Foreground time");
                }
                cpuTime = cpuFgTime; // Statistics may not have been gathered
                // yet.
            }
            power /= (60 * 60 * 1000);

            // Process wake lock usage
            Map<String, ? extends Uid.Wakelock> wakelockStats = u.getWakelockStats();
            for (Map.Entry<String, ? extends Uid.Wakelock> wakelockEntry : wakelockStats
                    .entrySet()) {
                Uid.Wakelock wakelock = wakelockEntry.getValue();
                // Only care about partial wake locks since full wake locks
                // are canceled when the user turns the screen off.
                BatteryStats.Timer timer = wakelock.getWakeTime(BatteryStats.WAKE_TYPE_PARTIAL);
                if (timer != null) {
                    wakelockTime += timer.getTotalTimeLocked(mRawRealtime, which);
                }
            }
            appWakelockTimeUs += wakelockTime;
            wakelockTime /= 1000; // convert to millis

            // Add cost of holding a wake lock
            p = (wakelockTime * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_AWAKE))
                    / (60 * 60 * 1000);
            if (DEBUG && p != 0)
                Log.d(TAG, "UID " + u.getUid() + ": wake " + wakelockTime + " power=" + makemAh(p));
            power += p;

            // Add cost of mobile traffic
            final long mobileRx = u.getNetworkActivityPackets(NETWORK_MOBILE_RX_DATA, mStatsType);
            final long mobileTx = u.getNetworkActivityPackets(NETWORK_MOBILE_TX_DATA, mStatsType);
            final long mobileRxB = u.getNetworkActivityBytes(NETWORK_MOBILE_RX_DATA, mStatsType);
            final long mobileTxB = u.getNetworkActivityBytes(NETWORK_MOBILE_TX_DATA, mStatsType);
            final long mobileActive = u.getMobileRadioActiveTime(mStatsType);
            if (mobileActive > 0) {
                // We are tracking when the radio is up, so can use the active
                // time to
                // determine power use.
                mAppMobileActive += mobileActive;
                p = (mobilePowerPerMs * mobileActive) / 1000;
            } else {
                // We are not tracking when the radio is up, so must approximate
                // power use
                // based on the number of packets.
                p = (mobileRx + mobileTx) * mobilePowerPerPacket;
            }
            if (DEBUG && p != 0)
                Log.d(TAG, "UID " + u.getUid() + ": mobile packets " + (mobileRx + mobileTx)
                        + " active time " + mobileActive + " power=" + makemAh(p));
            power += p;

            // Add cost of wifi traffic
            final long wifiRx = u.getNetworkActivityPackets(NETWORK_WIFI_RX_DATA, mStatsType);
            final long wifiTx = u.getNetworkActivityPackets(NETWORK_WIFI_TX_DATA, mStatsType);
            final long wifiRxB = u.getNetworkActivityBytes(NETWORK_WIFI_RX_DATA, mStatsType);
            final long wifiTxB = u.getNetworkActivityBytes(NETWORK_WIFI_TX_DATA, mStatsType);
            p = (wifiRx + wifiTx) * wifiPowerPerPacket;
            if (DEBUG && p != 0)
                Log.d(TAG, "UID " + u.getUid() + ": wifi packets " + (mobileRx + mobileTx) + " power="
                        + makemAh(p));
            power += p;

            // Add cost of keeping WIFI running.
            long wifiRunningTimeMs = u.getWifiRunningTime(mRawRealtime, which) / 1000;
            mAppWifiRunning += wifiRunningTimeMs;
            p = (wifiRunningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON))
                    / (60 * 60 * 1000);
            if (DEBUG && p != 0)
                Log.d(TAG, "UID " + u.getUid() + ": wifi running " + wifiRunningTimeMs + " power="
                        + makemAh(p));
            power += p;

            // Add cost of WIFI scans
            long wifiScanTimeMs = u.getWifiScanTime(mRawRealtime, which) / 1000;
            p = (wifiScanTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_SCAN))
                    / (60 * 60 * 1000);
            if (DEBUG)
                Log.d(TAG, "UID " + u.getUid() + ": wifi scan " + wifiScanTimeMs + " power=" + makemAh(p));
            power += p;
            for (int bin = 0; bin < Uid.NUM_WIFI_BATCHED_SCAN_BINS; bin++) {
                long batchScanTimeMs = u.getWifiBatchedScanTime(bin, mRawRealtime, which) / 1000;
                p = ((batchScanTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_BATCHED_SCAN,
                        bin))) / (60 * 60 * 1000);
                if (DEBUG && p != 0)
                    Log.d(TAG, "UID " + u.getUid() + ": wifi batched scan # " + bin + " time="
                            + batchScanTimeMs + " power=" + makemAh(p));
                power += p;
            }

            // Process Sensor usage
            SparseArray<? extends Uid.Sensor> sensorStats = u.getSensorStats();
            int NSE = sensorStats.size();
            for (int ise = 0; ise < NSE; ise++) {
                Uid.Sensor sensor = sensorStats.valueAt(ise);
                int sensorHandle = sensorStats.keyAt(ise);
                BatteryStats.Timer timer = sensor.getSensorTime();
                long sensorTime = timer.getTotalTimeLocked(mRawRealtime, which) / 1000;
                double multiplier = 0;
                switch (sensorHandle) {
                    case Uid.Sensor.GPS:
                        multiplier = mPowerProfile.getAveragePower(PowerProfile.POWER_GPS_ON);
                        gpsTime = sensorTime;
                        break;
                    default:
                        List<Sensor> sensorList = sensorManager
                                .getSensorList(Sensor.TYPE_ALL);
                        for (Sensor s : sensorList) {
                            if (s.getHandle() == sensorHandle) {
                                multiplier = s.getPower();
                                break;
                            }
                        }
                }
                p = (multiplier * sensorTime) / (60 * 60 * 1000);
                if (DEBUG && p != 0)
                    Log.d(TAG, "UID " + u.getUid() + ": sensor #" + sensorHandle + " time=" + sensorTime
                            + " power=" + makemAh(p));
                power += p;
            }


            final int userId = UserHandle.getUserId(u.getUid());
            if (power != 0 && u.getUid() != 0) {
                //Gionee <yangxinruo> <2015-5-29> modify for CR01528508 begin
                //BatterySipper app = new BatterySipper(getActivity(), mRequestQueue, mHandler,
                //        packageWithHighestDrain, DrainType.APP, 0, u, new double[] {power});
                String appLabel = "";
                if (packageWithHighestDrain == null || packageWithHighestDrain.isEmpty() || packageWithHighestDrain.startsWith("*"))
                    appLabel = getResources().getString(R.string.power_ranking_noname);
                else
                    appLabel = packageWithHighestDrain;
                BatterySipper app = new BatterySipper(getActivity(), mRequestQueue, mHandler,
                        appLabel, DrainType.APP, 0, u, new double[]{power});
                //Gionee <yangxinruo> <2015-5-29> modify for CR01528508 end
                app.cpuTime = cpuTime;
                app.gpsTime = gpsTime;
                app.wifiRunningTime = wifiRunningTimeMs;
                app.cpuFgTime = cpuFgTime;
                app.wakeLockTime = wakelockTime;
                app.mobileRxPackets = mobileRx;
                app.mobileTxPackets = mobileTx;
                app.mobileActive = mobileActive / 1000;
                app.mobileActiveCount = u.getMobileRadioActiveCount(mStatsType);
                app.wifiRxPackets = wifiRx;
                app.wifiTxPackets = wifiTx;
                app.mobileRxBytes = mobileRxB;
                app.mobileTxBytes = mobileTxB;
                app.wifiRxBytes = wifiRxB;
                app.wifiTxBytes = wifiTxB;
                app.packageWithHighestDrain = packageWithHighestDrain;

                if (u.getUid() == Process.WIFI_UID) {
                    mWifiSippers.add(app);
                    mWifiPower += power;
                } else if (u.getUid() == Process.BLUETOOTH_UID) {
                    mBluetoothSippers.add(app);
                    mBluetoothPower += power;
                }
                mSoftwareList.add(app);
            }

            if (u.getUid() == Process.WIFI_UID) {
                mWifiPower += power;
            } else if (u.getUid() == Process.BLUETOOTH_UID) {
                mBluetoothPower += power;
            }

            if (u.getUid() != 0) {
                mSoftTotalPower += power;
            }
        }
    }


    private void aggregateSippers(BatterySipper bs, List<BatterySipper> from, String tag) {
        for (int i = 0; i < from.size(); i++) {
            BatterySipper wbs = from.get(i);
            if (DEBUG)
                Log.d(TAG, tag + " adding sipper " + wbs + ": cpu=" + wbs.cpuTime);
            bs.cpuTime += wbs.cpuTime;
            bs.gpsTime += wbs.gpsTime;
            bs.wifiRunningTime += wbs.wifiRunningTime;
            bs.cpuFgTime += wbs.cpuFgTime;
            bs.wakeLockTime += wbs.wakeLockTime;
            bs.mobileRxPackets += wbs.mobileRxPackets;
            bs.mobileTxPackets += wbs.mobileTxPackets;
            bs.mobileActive += wbs.mobileActive;
            bs.mobileActiveCount += wbs.mobileActiveCount;
            bs.wifiRxPackets += wbs.wifiRxPackets;
            bs.wifiTxPackets += wbs.wifiTxPackets;
            bs.mobileRxBytes += wbs.mobileRxBytes;
            bs.mobileTxBytes += wbs.mobileTxBytes;
            bs.wifiRxBytes += wbs.wifiRxBytes;
            bs.wifiTxBytes += wbs.wifiTxBytes;
        }
        bs.computeMobilemspp();
    }

    /*
     * private void addFlashlightUsage() { long flashlightOnTimeMs =
     * mStats.getFlashlightOnTime(mRawRealtime, mStatsType) / 1000; double
     * flashlightPower = flashlightOnTimeMs
     * mPowerProfile.getAveragePower(PowerProfile.POWER_FLASHLIGHT) /
     * (60*60*1000); if (flashlightPower != 0) {
     * addEntry(BatterySipper.DrainType.FLASHLIGHT, flashlightOnTimeMs,
     * flashlightPower); } }
     */

    /*
     * private void addUserUsage() { for (int i=0; i<mUserSippers.size(); i++) {
     * final int userId = mUserSippers.keyAt(i); final List<BatterySipper>
     * sippers = mUserSippers.valueAt(i); Double userPower =
     * mUserPower.get(userId); double power = (userPower != null) ? userPower :
     * 0.0; BatterySipper bs = addEntry(BatterySipper.DrainType.USER, 0, power);
     * bs.userId = userId; aggregateSippers(bs, sippers, "User"); } }
     */

    /**
     * Return estimated power (in mAs) of sending or receiving a packet with the mobile radio.
     */
    private double getMobilePowerPerPacket() {
        final long MOBILE_BPS = 200000; // TODO: Extract average bit rates from
        // system
        final double MOBILE_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;

        final long mobileRx = mStats.getNetworkActivityPackets(NETWORK_MOBILE_RX_DATA, mStatsType);
        final long mobileTx = mStats.getNetworkActivityPackets(NETWORK_MOBILE_TX_DATA, mStatsType);
        final long mobileData = mobileRx + mobileTx;

        final long radioDataUptimeMs = mStats.getMobileRadioActiveTime(mRawRealtime, mStatsType) / 1000;
        final double mobilePps = (mobileData != 0 && radioDataUptimeMs != 0) ? (mobileData / (double) radioDataUptimeMs)
                : (((double) MOBILE_BPS) / 8 / 2048);

        return (MOBILE_POWER / mobilePps) / (60 * 60);
    }

    /**
     * Return estimated power (in mAs) of keeping the radio up
     */
    private double getMobilePowerPerMs() {
        return mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / (60 * 60 * 1000);
    }

    /**
     * Return estimated power (in mAs) of sending a byte with the Wi-Fi radio.
     */
    private double getWifiPowerPerPacket() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
        // system
        final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
        return (WIFI_POWER / (((double) WIFI_BPS) / 8 / 2048)) / (60 * 60);
    }

    private void processMiscUsage() {
        addPhoneUsage();
        addScreenUsage();

        // addFlashlightUsage();
        addWiFiUsage();
        addBluetoothUsage();

        // Not including cellular idle power
        addIdleUsage();
        addRadioUsage();

        final List<UserHandle> profiles = mUm.getUserProfiles();
        mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);
        usageList = mStatsHelper.getUsageList();
    }

    // 语音通话
    private void addPhoneUsage() {
        long phoneOnTimeMs = mStats.getPhoneOnTime(mRawRealtime, mStatsType) / 1000;
        double phoneOnPower = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) * phoneOnTimeMs
                / (60 * 60 * 1000);
        if (phoneOnPower != 0) {
            addEntry(getActivity().getString(R.string.power_phone), DrainType.PHONE,
                    phoneOnTimeMs, R.drawable.ic_voice_calls, phoneOnPower);
        }
    }

    // 屏幕
    private void addScreenUsage() {
        double power = 0;
        long screenOnTimeMs = mStats.getScreenOnTime(mRawRealtime, mStatsType) / 1000;
        power += screenOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_ON);
        final double screenFullPower = mPowerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
        for (int i = 0; i < BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS; i++) {
            double screenBinPower = screenFullPower * (i + 0.5f) / BatteryStats.NUM_SCREEN_BRIGHTNESS_BINS;
            long brightnessTime = mStats.getScreenBrightnessTime(i, mRawRealtime, mStatsType) / 1000;
            double p = screenBinPower * brightnessTime;
            if (DEBUG && p != 0) {
                Log.d(TAG, "Screen bin #" + i + ": time=" + brightnessTime + " power="
                        + makemAh(p / (60 * 60 * 1000)));
            }
            power += p;
        }
        power /= (60 * 60 * 1000); // To hours
        if (power != 0) {
            addEntry(getActivity().getString(R.string.power_screen), DrainType.SCREEN, screenOnTimeMs,
                    R.drawable.ic_display_for_power, power);
        }
    }

    // 信号待机
    private void addRadioUsage() {
        Log.e(TAG, "addRadioUsage start");
        double power = 0;
        final int BINS = SignalStrength.NUM_SIGNAL_STRENGTH_BINS;
        long signalTimeMs = 0;
        long noCoverageTimeMs = 0;
        for (int i = 0; i < BINS; i++) {
            long strengthTimeMs = mStats.getPhoneSignalStrengthTime(i, mRawRealtime, mStatsType) / 1000;
            double p = (strengthTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ON, i))
                    / (60 * 60 * 1000);
            if (DEBUG && p != 0) {
                Log.d(TAG, "Cell strength #" + i + ": time=" + strengthTimeMs + " power=" + makemAh(p));
            }
            power += p;
            signalTimeMs += strengthTimeMs;
            if (i == 0) {
                noCoverageTimeMs = strengthTimeMs;
            }
        }
        long scanningTimeMs = mStats.getPhoneSignalScanningTime(mRawRealtime, mStatsType) / 1000;
        double p = (scanningTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_SCANNING))
                / (60 * 60 * 1000);
        if (DEBUG && p != 0) {
            Log.d(TAG, "Cell radio scanning: time=" + scanningTimeMs + " power=" + makemAh(p));
        }
        power += p;
        long radioActiveTimeUs = mStats.getMobileRadioActiveTime(mRawRealtime, mStatsType);
        long remainingActiveTime = (radioActiveTimeUs - mAppMobileActive) / 1000;
        if (remainingActiveTime > 0) {
            power += getMobilePowerPerMs() * remainingActiveTime;
        }
        if (power != 0) {
            Log.e(TAG, "addEntry 信号待机");
            BatterySipper bs = addEntry(getActivity().getString(R.string.power_cell), DrainType.CELL,
                    signalTimeMs, R.drawable.ic_cell_standby, power);
            /*if (signalTimeMs != 0) {
                bs.noCoveragePercent = noCoverageTimeMs * 100.0 / signalTimeMs;
            }*/
            bs.mobileActive = remainingActiveTime;
            bs.mobileActiveCount = mStats.getMobileRadioActiveUnknownCount(mStatsType);
        }
        Log.e(TAG, "power = " + power);
        Log.e(TAG, "addRadioUsage end");
    }

    // WLAN
    private void addWiFiUsage() {
        long onTimeMs = mStats.getWifiOnTime(mRawRealtime, mStatsType) / 1000;
        long runningTimeMs = mStats.getGlobalWifiRunningTime(mRawRealtime, mStatsType) / 1000;
        if (DEBUG)
            Log.d(TAG, "WIFI runningTime=" + runningTimeMs + " app runningTime=" + mAppWifiRunning);
        runningTimeMs -= mAppWifiRunning;
        if (runningTimeMs < 0)
            runningTimeMs = 0;
        double wifiPower = (onTimeMs * 0 /* TODO */
                * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON) + runningTimeMs
                * mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ON))
                / (60 * 60 * 1000);
        if (DEBUG && wifiPower != 0) {
            Log.d(TAG, "Wifi: time=" + runningTimeMs + " power=" + makemAh(wifiPower));
        }
        if ((wifiPower + mWifiPower) != 0) {
            BatterySipper bs = addEntry(getActivity().getString(R.string.power_wifi), DrainType.WIFI,
                    runningTimeMs, R.drawable.icon_use_wlan, wifiPower + mWifiPower);
            aggregateSippers(bs, mWifiSippers, "WIFI");
        }
    }

    // 手机待机
    private void addIdleUsage() {
        Log.e(TAG, "addIdleUsage start");
        long idleTimeMs = (mTypeBatteryRealtime - mStats.getScreenOnTime(mRawRealtime, mStatsType)) / 1000;
        double idlePower = (idleTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_CPU_IDLE))
                / (60 * 60 * 1000);
        if (DEBUG && idlePower != 0) {
            Log.d(TAG, "Idle: time=" + idleTimeMs + " power=" + makemAh(idlePower));
        }
        if (idlePower != 0) {
            addEntry(getActivity().getString(R.string.power_idle), DrainType.IDLE, idleTimeMs,
                    R.drawable.ic_phone_idle, idlePower);
        }
        Log.e(TAG, "idlePower = " + idlePower);
        Log.e(TAG, "addIdleUsage end");
    }

    // 蓝牙
    private void addBluetoothUsage() {
        Log.e(TAG, "addBluetoothUsage start");
        long btOnTimeMs = mStats.getBluetoothOnTime(mRawRealtime, mStatsType) / 1000;
        double btPower = btOnTimeMs * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_ON)
                / (60 * 60 * 1000);
        if (DEBUG && btPower != 0) {
            Log.d(TAG, "Bluetooth: time=" + btOnTimeMs + " power=" + makemAh(btPower));
        }
        int btPingCount = mStats.getBluetoothPingCount();
        double pingPower = (btPingCount * mPowerProfile.getAveragePower(PowerProfile.POWER_BLUETOOTH_AT_CMD))
                / (60 * 60 * 1000);
        if (DEBUG && pingPower != 0) {
            Log.d(TAG, "Bluetooth ping: count=" + btPingCount + " power=" + makemAh(pingPower));
        }
        btPower += pingPower;
        if ((btPower + mBluetoothPower) != 0) {
            BatterySipper bs = addEntry(getActivity().getString(R.string.power_bluetooth),
                    DrainType.BLUETOOTH, btOnTimeMs, R.drawable.icon_use_bluetooth, btPower + mBluetoothPower);
            aggregateSippers(bs, mBluetoothSippers, "Bluetooth");
        }
        Log.e(TAG, "btPower = " + btPower);
        Log.e(TAG, "addBluetoothUsage end");
    }

    private BatterySipper addEntry(String label, DrainType drainType, long time, int iconId, double power) {
        Log.e(TAG, "addEntry label: " + label);
        mHardTotalPower += power;
        BatterySipper bs = new BatterySipper(getActivity(), mRequestQueue, mHandler, label, drainType,
                iconId, null, new double[]{power});
        //bs.usageTime = time;
        bs.iconId = iconId;
        mHardwareList.add(bs);
        return bs;
    }

    private void refreshList(List<BatterySipper> list) {
        mAdapter.setItemData(list);
    }

    private void load() {
        try {
            byte[] data = mBatteryInfo.getStatistics();
            Parcel parcel = Parcel.obtain();
            parcel.unmarshall(data, 0, data.length);
            parcel.setDataPosition(0);
            mStats = BatteryStatsImpl.CREATOR.createFromParcel(parcel);
            mStats.distributeWorkLocked(BatteryStats.STATS_SINCE_CHARGED);
        } catch (RemoteException e) {
        }
    }

    public void run() {
        while (true) {
            BatterySipper bs;
            synchronized (mRequestQueue) {
                if (mRequestQueue.isEmpty() || mAbort) {
                    mRequestThread = null;
                    return;
                }
                bs = mRequestQueue.remove(0);
            }
            bs.getNameIcon();
        }
    }

    static final int MSG_UPDATE_NAME_ICON = 1;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_UPDATE_NAME_ICON:
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> paramAdapterView, View paramView, int paramInt, long paramLong) {
        if (mAdapter == null || paramInt == 0) {
            return;
        }
        BatterySipper sipper = mAdapter.getItem(paramInt - 1);
        if (sipper == null) {
            return;
        }
        Log.e(TAG, "onItemClick" + "sipper.drainType = " + sipper.drainType);

        String hardwareItem = null;
        int iconId = 0;
        com.android.internal.os.BatterySipper.DrainType type = null;
        switch (sipper.drainType) {
            case APP:
            case USER:
                Intent intent = new Intent(getActivity(), PowerUsageDetailActivity.class);
                Bundle bundle = new Bundle();

                //cpu time
                bundle.putString(getString(R.string.cpu_total_time),
                        TimeUtils.MillsToHHMMSS(sipper.cpuTime));
                //cpu time fg
                if (sipper.cpuFgTime > 0) {
                    bundle.putString(getString(R.string.cpu_foreground),
                            TimeUtils.MillsToHHMMSS(sipper.cpuFgTime));
                }
                //保持唤醒
                if (sipper.wakeLockTime > 0) {
                    bundle.putString(getString(R.string.wakeup_time),
                            TimeUtils.MillsToHHMMSS(sipper.wakeLockTime));
                }
                if (sipper.gpsTime > 0) {
                    //gps
                    bundle.putString(getString(R.string.gps_time),
                            TimeUtils.MillsToHHMMSS(sipper.gpsTime));
                }
                if (sipper.wifiRxBytes > 1024) {
                    //wifi 接收数据
                    bundle.putString(getString(R.string.wlan_received),
                            Formatter.formatFileSize(getActivity(), sipper.wifiRxBytes));
                }
                if (sipper.wifiTxBytes > 1024) {
                    //wifi 发送数据
                    bundle.putString(getString(R.string.wlan_send),
                            Formatter.formatFileSize(getActivity(), sipper.wifiTxBytes));

                }
                if (sipper.mobileRxBytes > 1024) {
                    //grs 接收数据
                    bundle.putString(getString(R.string.ems_received),
                            Formatter.formatFileSize(getActivity(), sipper.mobileRxBytes));

                }
                if (sipper.mobileTxBytes > 1024) {
                    //grs 发送数据
                    bundle.putString(getString(R.string.ems_send),
                            Formatter.formatFileSize(getActivity(), sipper.mobileTxBytes));
                }
                //耗电量

                String packageName = sipper.defaultPackageName;
                if (packageName == null || "android".equals(packageName)) {
                    Log.e(TAG, "package = null, " + sipper.name);
                    bundle.putString(PowerUsageDetailActivity.SOFTWARE_NAME, sipper.name);
                    intent.putExtra(PowerUsageDetailActivity.DETAIL_MODE, PowerUsageDetailActivity.SYSTEM_APP_DETAIL);
                } else {
                    Log.e(TAG, "package = " + sipper.defaultPackageName);
                    intent.putExtra(PowerUsageDetailActivity.PACKAGE_NAME, packageName);
                    intent.putExtra(PowerUsageDetailActivity.DETAIL_MODE, PowerUsageDetailActivity.USER_APP_DETAIL);
                }
                iconForDetail = sipper.icon;
                intent.putExtras(bundle);
                startActivity(intent);
                return;
            case SCREEN:
                hardwareItem = getString(R.string.power_screen);
                iconId = R.drawable.ic_display_for_power;
                type = com.android.internal.os.BatterySipper.DrainType.SCREEN;
                break;
            case PHONE:
                hardwareItem = getString(R.string.power_phone);
                iconId = R.drawable.ic_phone_idle;
                type = com.android.internal.os.BatterySipper.DrainType.PHONE;
                break;
            case CELL:
                hardwareItem = getString(R.string.power_cell);
                iconId = R.drawable.ic_cell_standby;
                type = com.android.internal.os.BatterySipper.DrainType.CELL;
                break;
            case IDLE:
                type = com.android.internal.os.BatterySipper.DrainType.IDLE;
                hardwareItem = getString(R.string.power_idle);
                iconId = R.drawable.ic_phone_idle;
                break;
            case WIFI:
                type = com.android.internal.os.BatterySipper.DrainType.WIFI;
                hardwareItem = getString(R.string.power_wifi);
                iconId = R.drawable.icon_use_wlan;
                break;
            case BLUETOOTH:
                type = com.android.internal.os.BatterySipper.DrainType.BLUETOOTH;
                hardwareItem = getString(R.string.power_bluetooth);
                iconId = R.drawable.icon_use_bluetooth;
                break;
            default:
                break;
        }
        Log.e(TAG, "other usage drainType = " + sipper.drainType + " type " + type);

        Intent intent = new Intent(getActivity(), PowerUsageDetailActivity.class);
        intent.putExtra(PowerUsageDetailActivity.HARDWARE_RUNNING_TIME, TimeUtils.MillsToHHMMSS(getHardwareRunningTime(type)));
        intent.putExtra(PowerUsageDetailActivity.HAREWARE_NAME, hardwareItem);
        intent.putExtra(PowerUsageDetailActivity.HAREWARE_ICON, iconId);
        intent.putExtra(PowerUsageDetailActivity.DETAIL_MODE, PowerUsageDetailActivity.HAREDWARE_DETAIL);
        getActivity().startActivity(intent);
    }

    private long getHardwareRunningTime(com.android.internal.os.BatterySipper.DrainType drainType) {
        Log.e(TAG, "getHardwareRunningTime " + "usageList.size() = " + usageList.size());
        if (drainType == null) {
            Log.e(TAG, "getHardwareRunningTime " + "type = null");
            return 0;
        }
        for (com.android.internal.os.BatterySipper hardSipper : usageList) {
            if (hardSipper.drainType == drainType) {
                Log.e(TAG, "getHardwareRunningTime drainType = " + drainType + " usageTime " + hardSipper.usageTime);
                return hardSipper.usageTime;
            }
        }
        Log.e(TAG, "getHardwareRunningTime " + "type = " + drainType + " time = 0");
        return 0;
    }

    public class SummaryAppListAdapter extends BaseAdapter {
        private Context mContext;
        //private BatterySipper mInfo;
        private int mProgress;
        //private CharSequence mTitleName;
        private CharSequence mProgressText;
        //private int mIndicationVisibility;
        private List<BatterySipper> mListItemData = new ArrayList<BatterySipper>();

        public SummaryAppListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            if (mListItemData != null) {
                return mListItemData.size();
            }
            return 0;
        }

        @Override
        public BatterySipper getItem(int paramInt) {
            if (mListItemData == null) {
                return null;
            }
            return mListItemData.get(paramInt);
        }

        @Override
        public long getItemId(int paramInt) {
            return 0;
        }

        private void setPercent(double percentOfMax, double percentOfTotal) {
            mProgress = (int) Math.ceil(percentOfMax);
            mProgressText = mContext.getResources().getString(R.string.percentage, percentOfTotal);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = convertView;
            final ViewCache viewCache;

            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.powersaver_app_percentage_item, parent,
                        false);

                viewCache = new ViewCache();
                view.setTag(viewCache);
            } else {
                viewCache = (ViewCache) view.getTag();
            }

            BatterySipper sipper = mListItemData.get(position);

            setPercent(sipper.percent, sipper.percent);

            viewCache.icon = (ImageView) view.findViewById(android.R.id.icon);
            viewCache.icon.setImageDrawable(sipper.getIcon());
            String pkgLable = sipper.name;
            try {
                pkgLable = HelperUtils.loadLabel(mContext,
                        HelperUtils.getApplicationInfo(mContext, sipper.defaultPackageName));
            } catch (Exception e) {
                pkgLable = sipper.name;
            }
            viewCache.text = (TextView) view.findViewById(R.id.title);
            viewCache.text.setText(pkgLable);
            // Gionee <liuyb> <2014-09-20> modify for CR01386162 end

            viewCache.progress = (ProgressBar) view.findViewById(R.id.progress);
            viewCache.progress.setProgress(mProgress);

            viewCache.text1 = (TextView) view.findViewById(R.id.summary);
            viewCache.text1.setText(mProgressText);

            // viewCache.indiImage = (ImageView) view.findViewById(R.id.indi);
            // viewCache.indiImage.setVisibility(View.VISIBLE);
            /*
             * if (mTabFragment.isLeftButton()) { String pn =
             * sipper.defaultPackageName; if (pn == null ||
             * pn.equals("android")) {
             * viewCache.indiImage.setVisibility(View.INVISIBLE); } }
             */
            return view;
        }

        public void setItemData(List<BatterySipper> list) {
            mListItemData.clear();
            double totalPower = 0;
            if (list == mHardwareList) {
                totalPower = mHardTotalPower;
            } else {
                totalPower = mSoftTotalPower;
            }
            if (totalPower == 0) {
                notifyDataSetChanged();
                return;
            }
            Collections.sort(list, mComparator);
            for (BatterySipper sipper : list) {
                final double percentOfTotal = ((sipper.getSortValue() / mTotalPower) * 100);
                if (percentOfTotal < 0.1f) {
                    continue;
                }

                sipper.percent = percentOfTotal;
                mListItemData.add(sipper);

                if (mListItemData.size() > MAX_ITEMS_TO_LIST) {
                    break;
                }
            }

            Collections.sort(mListItemData, mComparator);

            notifyDataSetChanged();
        }
    }

    private Comparator<BatterySipper> mComparator = new Comparator<BatterySipper>() {
        @Override
        public int compare(BatterySipper paramT1, BatterySipper paramT2) {
            BigDecimal d1 = new BigDecimal(Double.toString(paramT1.getSortValue()));
            BigDecimal d2 = new BigDecimal(Double.toString(paramT2.getSortValue()));
            double d3 = d1.subtract(d2).doubleValue();
            if (d3 > 0.0000d) {
                return -1;
            } else if (d3 < 0.0000d) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    // Gionee <xuhz> <2014-03-24> add for android 4.4 begin
    public static final int NETWORK_MOBILE_RX_BYTES = 0;
    public static final int NETWORK_MOBILE_TX_BYTES = 1;
    public static final int NETWORK_WIFI_RX_BYTES = 2;
    public static final int NETWORK_WIFI_TX_BYTES = 3;

    /**
     * Return estimated power (in mAs) of sending a byte with the mobile radio.
     */
    /*
     * private double getMobilePowerPerByte() { final long MOBILE_BPS = 200000;
     * // TODO: Extract average bit rates from system final double MOBILE_POWER
     * = mPowerProfile.getAveragePower(PowerProfile.POWER_RADIO_ACTIVE) / 3600;
     *
     * final long mobileRx = BatteryInfoUtils.getNetworkActivityCount(mStats,
     * NETWORK_MOBILE_RX_BYTES, mStatsType); final long mobileTx =
     * BatteryInfoUtils.getNetworkActivityCount(mStats, NETWORK_MOBILE_TX_BYTES,
     * mStatsType); final long mobileData = mobileRx + mobileTx;
     *
     * final long radioDataUptimeMs = mStats.getRadioDataUptime() / 1000; final
     * long mobileBps = radioDataUptimeMs != 0 ? mobileData * 8 * 1000 /
     * radioDataUptimeMs : MOBILE_BPS;
     *
     * return MOBILE_POWER / (mobileBps / 8); }
     */

    /**
     * Return estimated power (in mAs) of sending a byte with the Wi-Fi radio.
     */
    private double getWifiPowerPerByte() {
        final long WIFI_BPS = 1000000; // TODO: Extract average bit rates from
        // system
        final double WIFI_POWER = mPowerProfile.getAveragePower(PowerProfile.POWER_WIFI_ACTIVE) / 3600;
        return WIFI_POWER / (WIFI_BPS / 8);
    }

    /*
     * private long getDataTrafficPower(Uid u) { long dataPower = 0;
     *
     * if (Build.VERSION.SDK_INT >= 19) { double mobilePowerPerByte =
     * getMobilePowerPerByte(); double wifiPowerPerByte = getWifiPowerPerByte();
     *
     * final long mobileRx = BatteryInfoUtils.getNetworkActivityCount(u,
     * NETWORK_MOBILE_RX_BYTES, mStatsType); final long mobileTx =
     * BatteryInfoUtils.getNetworkActivityCount(u, NETWORK_MOBILE_TX_BYTES,
     * mStatsType); dataPower += (mobileRx + mobileTx) * mobilePowerPerByte;
     *
     * // Add cost of wifi traffic final long wifiRx =
     * BatteryInfoUtils.getNetworkActivityCount(u, NETWORK_WIFI_RX_BYTES,
     * mStatsType); final long wifiTx =
     * BatteryInfoUtils.getNetworkActivityCount(u, NETWORK_WIFI_TX_BYTES,
     * mStatsType); dataPower += (wifiRx + wifiTx) * wifiPowerPerByte; } else {
     * long tcpBytesReceived = BatteryInfoUtils.getTcpBytesReceived(u,
     * mStatsType); long tcpBytesSent = BatteryInfoUtils.getTcpBytesSent(u,
     * mStatsType); final double averageCostPerByte = getAverageDataCost();
     *
     * dataPower += (tcpBytesReceived+tcpBytesSent) * averageCostPerByte; }
     * return dataPower; }
     */
    // Gionee <xuhz> <2014-03-24> add for android 4.4 end

    public static String makemAh(double power) {
        if (power < .00001)
            return String.format("%.8f", power);
        else if (power < .0001)
            return String.format("%.7f", power);
        else if (power < .001)
            return String.format("%.6f", power);
        else if (power < .01)
            return String.format("%.5f", power);
        else if (power < .1)
            return String.format("%.4f", power);
        else if (power < 1)
            return String.format("%.3f", power);
        else if (power < 10)
            return String.format("%.2f", power);
        else if (power < 100)
            return String.format("%.1f", power);
        else
            return String.format("%.0f", power);
    }
}
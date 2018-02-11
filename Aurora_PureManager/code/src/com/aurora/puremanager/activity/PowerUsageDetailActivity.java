package com.aurora.puremanager.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.fragment.UsageSummaryFragment;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.utils.ApkUtils;
import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.view.AppDetailInfoView;
import com.aurora.puremanager.view.InfoDialog;

import java.util.ArrayList;
import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

/**
 * Created by joy on 1/16/16.
 */
public class PowerUsageDetailActivity extends AuroraActivity implements View.OnClickListener, Observer {

    private final String TAG = "PowerUsageDetail";
    public static final String PACKAGE_NAME = "package_name";
    public static final String HAREWARE_NAME = "hareware_name";
    public static final String SOFTWARE_NAME = "software_name";
    public static final String HAREWARE_ICON = "hareware_icon";
    public static final String HARDWARE_RUNNING_TIME = "hardware_running_time";
    private static final int MAX_LINE = 5;
    private AppInfo curAppInfo = null;
    private String pkgName = "";
    private LinearLayout firstListLy, secondListLy;
    private Bundle mBundle;
    public static final int USER_APP_DETAIL = 0;
    public static final int SYSTEM_APP_DETAIL = 1;
    public static final int HAREDWARE_DETAIL = 2;
    public static final String DETAIL_MODE = "detail_mode";

    private Button mForceStopButton;
    private Button uninstallBtn;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.power_usage_detal_layout, AuroraActionBar.Type.Normal);
        initActionBar();

        ((TextView) findViewById(R.id.list_head_text)).setText(R.string.detail_usage_summery);
        firstListLy = (LinearLayout) findViewById(R.id.first_list);
        initData();
    }

    private void initData() {
        mBundle = getIntent().getExtras();

        int mode = mBundle.getInt(DETAIL_MODE);
        Log.e(TAG, "mode = " + mode);
        mBundle.remove(DETAIL_MODE);
        if (mode == HAREDWARE_DETAIL) {
            ((ImageView) findViewById(R.id.appIcon)).setImageResource(mBundle.getInt(HAREWARE_ICON));
            ((TextView) findViewById(R.id.appName)).setText(mBundle.getString(HAREWARE_NAME));
            (findViewById(R.id.version)).setVisibility(View.GONE);
            (findViewById(R.id.btnLayout)).setVisibility(View.GONE);

            LinearLayout ll = (LinearLayout) getLayoutInflater().
                    inflate(R.layout.battery_usage_item, null);
            ((TextView) ll.findViewById(R.id.item_name)).setText(getString(R.string.running_time));
            ((TextView) ll.findViewById(R.id.item_value)).setText(mBundle.getString(HARDWARE_RUNNING_TIME));
            firstListLy.addView(ll);
        } else {
            mForceStopButton = (Button) findViewById(R.id.mForceStopButton);
            uninstallBtn = (Button) findViewById(R.id.uninstallBtn);
            if (mode == SYSTEM_APP_DETAIL) {
                String name = mBundle.getString(PowerUsageDetailActivity.SOFTWARE_NAME);
                mBundle.remove(PowerUsageDetailActivity.SOFTWARE_NAME);
                ((ImageView) findViewById(R.id.appIcon)).setImageDrawable(UsageSummaryFragment.iconForDetail);
                ((TextView) findViewById(R.id.appName)).setText(name);
                (findViewById(R.id.version)).setVisibility(View.GONE);
                (findViewById(R.id.btnLayout)).setVisibility(View.GONE);
            } else {
                pkgName = mBundle.getString(PACKAGE_NAME);
                mBundle.remove(PACKAGE_NAME);
                receiveData();
                ((AppDetailInfoView) findViewById(R.id.app_detailinfo_layout)).setCurAppInfo(curAppInfo);
                if (curAppInfo.getIsUserApp()) {
                    ConfigModel.getInstance(this).getAppInfoModel().attach(this);
                    mForceStopButton.setOnClickListener(this);
                    uninstallBtn.setOnClickListener(this);
                } else {
                    uninstallBtn.setEnabled(false);
                    mForceStopButton.setEnabled(false);
                }
            }
            initViews();
            checkForceStop();
        }
    }

    private void receiveData() {
        Log.e(TAG, "receiveData pkgName = " + pkgName);
        curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(pkgName);

        if (curAppInfo == null || !curAppInfo.getIsInstalled()) {
            InfoDialog.showToast(this, getString(R.string.app_not_install));
            finish();
            return;
        }
    }

    private void initViews() {
        int totalLine = mBundle.size();
        LayoutInflater inflater = getLayoutInflater();
        int firstLine = totalLine > MAX_LINE ? MAX_LINE : totalLine;
        List<String> keyList = new ArrayList<String>(mBundle.keySet());
        String key;
        for (int index = 0; index < firstLine; index++) {
            key = keyList.get(index);
            LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.battery_usage_item, null);
            ((TextView) ll.findViewById(R.id.item_name)).setText(key);
            ((TextView) ll.findViewById(R.id.item_value)).setText(mBundle.getString(key));
            firstListLy.addView(ll);
        }
        if (totalLine > MAX_LINE) {
            secondListLy = (LinearLayout) findViewById(R.id.second_list);
            secondListLy.setVisibility(View.VISIBLE);
            int secendLine = totalLine - MAX_LINE;
            for (int index = 0; index < secendLine; index++) {
                key = keyList.get(index + MAX_LINE);
                LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.battery_usage_item, null);
                ((TextView) ll.findViewById(R.id.item_name)).setText(key);
                ((TextView) ll.findViewById(R.id.item_value)).setText(mBundle.getString(key));
                secondListLy.addView(ll);
            }
        }
    }

    public void initActionBar() {
        AuroraActionBar bar = getAuroraActionBar();
        bar.setTitle(R.string.power_consumption_detail);
    }

    @Override
    public void onClick(View v) {
        if (curAppInfo == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.mForceStopButton:
                InfoDialog.showDialog(this,
                        R.string.force_stop_dlg_title,
                        android.R.attr.alertDialogIcon,
                        R.string.force_stop_dlg_text,
                        R.string.sure,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                forceStopPackage(curAppInfo.getPackageName());
                            }
                        },
                        R.string.cancel,
                        null,
                        null);
                break;
            case R.id.uninstallBtn:
                if (curAppInfo.getIsUserApp()) {
                    uninstallFunc();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 强行停止一个应用
     *
     * @param pkgName
     */
    private void forceStopPackage(String pkgName) {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.forceStopPackage(pkgName);
        checkForceStop();
    }

    private void checkForceStop() {
        if (curAppInfo == null) {
            return;
        }
        DevicePolicyManager mDpm;
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ApplicationInfo applicationInfo = ApkUtils.getApplicationInfo(this, curAppInfo.getPackageName());
        if (applicationInfo == null) {
            return;
        }
        if (mDpm.packageHasActiveAdmins(curAppInfo.getPackageName())) {
            updateForceStopButton(false);
        } else if ((applicationInfo.flags & ApplicationInfo.FLAG_STOPPED) == 0) {
            updateForceStopButton(true);
        } else {
            Intent intent = new Intent(Intent.ACTION_QUERY_PACKAGE_RESTART,
                    Uri.fromParts("package", curAppInfo.getPackageName(), null));
            intent.putExtra(Intent.EXTRA_PACKAGES, new String[]{curAppInfo.getPackageName()});
            intent.putExtra(Intent.EXTRA_UID, applicationInfo.uid);
            intent.putExtra(Intent.EXTRA_USER_HANDLE, UserHandle.getUserId(applicationInfo.uid));
            sendOrderedBroadcast(intent, null, mCheckKillProcessesReceiver, null,
                    Activity.RESULT_CANCELED, null, null);
        }
    }

    private final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateForceStopButton(getResultCode() != Activity.RESULT_CANCELED);
        }
    };

    private void updateForceStopButton(boolean enabled) {
        if (mForceStopButton != null) {
            if (curAppInfo.getIsUserApp()) {
                mForceStopButton.setEnabled(enabled);
            }
        }
    }

    /**
     * 卸载应用
     */
    private void uninstallFunc() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.DELETE");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setData(Uri.parse("package:" + curAppInfo.getPackageName()));
        startActivity(intent);
    }

    @Override
    public void updateOfInit(Subject subject) {

    }

    @Override
    public void updateOfInStall(Subject subject, String pkgName) {

    }

    @Override
    public void updateOfCoverInStall(Subject subject, String pkgName) {

    }

    @Override
    public void updateOfUnInstall(Subject subject, String pkgName) {
        if(pkgName != null &&
                curAppInfo != null &&
                pkgName.equals(curAppInfo.getPackageName())){
            finish();
        }
    }

    @Override
    public void updateOfRecomPermsChange(Subject subject) {

    }

    @Override
    public void updateOfExternalAppAvailable(Subject subject, List<String> pkgList) {

    }

    @Override
    public void updateOfExternalAppUnAvailable(Subject subject, List<String> pkgList) {

    }
}
package com.aurora.puremanager.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.aurora.puremanager.R;
import com.aurora.puremanager.data.AppInfo;
import com.aurora.puremanager.data.AutoStartData;
import com.aurora.puremanager.interfaces.Observer;
import com.aurora.puremanager.interfaces.Subject;
import com.aurora.puremanager.model.AutoSleepModel;
import com.aurora.puremanager.model.AutoStartModel;
import com.aurora.puremanager.model.ConfigModel;
import com.aurora.puremanager.model.StopWakeUpModel;
import com.aurora.puremanager.provider.open.AutoStartAppProvider;
import com.aurora.puremanager.provider.open.StopWakeAppProvider;
import com.aurora.puremanager.utils.mConfig;
import com.aurora.puremanager.view.AppDetailInfoView;
import com.aurora.puremanager.view.InfoDialog;

import java.util.List;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

/**
 * 纯净后台设置详情页-禁止唤醒,自动休眠,应用自启
 */
public class PureBackgroundDetailActivity extends AuroraActivity implements
        CompoundButton.OnCheckedChangeListener, View.OnClickListener, Observer {
    private final static String TAG = "PureBackground";

    private View mAutoStartLayout;
    private View mStopWakeLayout;
    private View mAutoSleepLayout;
    private Switch mStopAutoStartSwitch;
    private Switch mStopWakeSwitch;
    private Switch mAutoSleepSwitch;
    private TextView tvAutoStartInfo;
    private TextView tvStopWakeInfo;
    private TextView tvAutoSleepInfo;

    private AppInfo curAppInfo = null;

    private boolean stopAutoStartOriginalStatus;
    private boolean stopWakeOriginalStatus;
    private boolean autoSleepOriginalStatus;
    private boolean stopAutoStartCheckStatus;
    private boolean stopWakeCheckStatus;
    private boolean autoSleepCheckStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mConfig.isNative) {
            setContentView(R.layout.activity_pure_background_detail);
        } else {
            setAuroraContentView(R.layout.activity_pure_background_detail, AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.app_setting);
            getAuroraActionBar().setBackgroundResource(R.color.pure_background_title_color);
        }

        receiveData();
        initViews();
        initData();
    }

    private void receiveData() {
        if (getIntent() != null && getIntent().getData() != null) {
            String packageName = getIntent().getData().getSchemeSpecificPart();
            curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(packageName);
        }

        if (curAppInfo == null || !curAppInfo.getIsInstalled()) {
            InfoDialog.showToast(this, getString(R.string.app_not_install));
            finish();
            return;
        }
    }

    private void initViews() {
        ((AppDetailInfoView) findViewById(R.id.app_detail_layout)).setCurAppInfo(curAppInfo);

        mStopWakeLayout = findViewById(R.id.stop_wake_item);
        tvStopWakeInfo = (TextView) findViewById(R.id.tv_stop_wake_info);
        mStopWakeSwitch = (Switch) findViewById(R.id.stop_wake_switch);
        mStopWakeLayout.setClickable(true);
        mStopWakeLayout.setOnClickListener(this);
        mStopWakeSwitch.setOnCheckedChangeListener(this);

        mAutoSleepLayout = findViewById(R.id.auto_sleep_item);
        tvAutoSleepInfo = (TextView) findViewById(R.id.tv_auto_sleep_info);
        mAutoSleepSwitch = (Switch) findViewById(R.id.auto_sleep_switch);
        mAutoSleepLayout.setClickable(true);
        mAutoSleepLayout.setOnClickListener(this);
        mAutoSleepSwitch.setOnCheckedChangeListener(this);

        //判断是否可以自启,无自启行为则不显示
        mAutoStartLayout = findViewById(R.id.auto_start_item);
        if (curAppInfo.getIsUserApp()) {
            AutoStartData autoStartData = AutoStartModel.getInstance(this).
                    getAutoStartData(curAppInfo.getPackageName());
            if (autoStartData != null) {
                tvAutoStartInfo = (TextView) findViewById(R.id.tv_auto_start_info);
                mStopAutoStartSwitch = (Switch) findViewById(R.id.auto_start_switch);
                mAutoStartLayout.setVisibility(View.VISIBLE);
                mAutoSleepLayout.setClickable(true);
                mAutoStartLayout.setOnClickListener(this);
                mStopAutoStartSwitch.setOnCheckedChangeListener(this);

                //与禁止唤醒,自动休眠相反,禁止自启数据库中保存的为允许自启动
                if (AutoStartAppProvider.isInDB(this, curAppInfo.getPackageName())) {
                    stopAutoStartOriginalStatus = false;
                    mStopAutoStartSwitch.setChecked(false);
                    tvAutoStartInfo.setText(R.string.auto_start_detail);
                } else {
                    stopAutoStartOriginalStatus = true;
                    mStopAutoStartSwitch.setChecked(true);
                    tvAutoStartInfo.setText(R.string.close_auto_start_detail);
                }
            } else {
                stopAutoStartOriginalStatus = false;
                mAutoStartLayout.setVisibility(View.GONE);
            }
        } else {
            mAutoStartLayout.setVisibility(View.GONE);
        }
    }

    private void initData() {
        if (StopWakeAppProvider.isInDB(this, curAppInfo.getPackageName())) {
            stopWakeOriginalStatus = true;
            mStopWakeSwitch.setChecked(true);
            tvStopWakeInfo.setText(R.string.open_stop_wake_detail);
        } else {
            stopWakeOriginalStatus = false;
            mStopWakeSwitch.setChecked(false);
            tvStopWakeInfo.setText(R.string.stop_wake_detail);
        }

        if (AutoSleepModel.getInstance(this).isInAutoSleepMode(curAppInfo.getPackageName())) {
            autoSleepOriginalStatus = true;
            mAutoSleepSwitch.setChecked(true);
            tvAutoSleepInfo.setText(R.string.open_auto_sleep_detail);
        } else {
            autoSleepOriginalStatus = false;
            mAutoSleepSwitch.setChecked(false);
            tvAutoSleepInfo.setText(R.string.auto_sleep_detail);
        }

        stopAutoStartCheckStatus = stopAutoStartOriginalStatus;
        stopWakeCheckStatus = stopWakeOriginalStatus;
        autoSleepCheckStatus  = autoSleepOriginalStatus;

        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
    }

    public void onBackPressed() {
        if ((stopAutoStartCheckStatus == stopAutoStartOriginalStatus)
                && (stopWakeCheckStatus == stopWakeOriginalStatus)
                && (autoSleepCheckStatus == autoSleepOriginalStatus)) {
            setResult(RESULT_CANCELED);
        } else {
            Intent intent = new Intent();
            intent.putExtra("StopAutoStart", stopAutoStartCheckStatus);
            intent.putExtra("StopWake", stopWakeCheckStatus);
            intent.putExtra("AutoSleep", autoSleepCheckStatus);
            setResult(RESULT_OK, intent);
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        ConfigModel.getInstance(this).getAppInfoModel().detach(this);
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (curAppInfo == null) {
            return;
        }
        switch (buttonView.getId()) {
            case R.id.auto_start_switch:
                stopAutoStartCheckStatus = isChecked;
                AutoStartModel.getInstance(this).
                        tryChangeAutoStartState(curAppInfo.getPackageName(), !stopAutoStartCheckStatus);
                if (isChecked == false) {
                    tvAutoStartInfo.setText(R.string.auto_start_detail);
                } else {
                    tvAutoStartInfo.setText(R.string.close_auto_start_detail);
                }
                break;
            case R.id.stop_wake_switch:
                stopWakeCheckStatus = isChecked;
                StopWakeUpModel.getInstance(this).
                        tryChangeStopWakeState(curAppInfo.getPackageName(), stopWakeCheckStatus);
                if (isChecked == false) {
                    tvStopWakeInfo.setText(R.string.stop_wake_detail);
                } else {
                    tvStopWakeInfo.setText(R.string.open_stop_wake_detail);
                }
                break;
            case R.id.auto_sleep_switch:
                autoSleepCheckStatus = isChecked;
                AutoSleepModel.getInstance(this).
                        tryChangeAutoSleepState(curAppInfo.getPackageName(), autoSleepCheckStatus);
                if (isChecked == false) {
                    tvAutoSleepInfo.setText(R.string.auto_sleep_detail);
                } else {
                    tvAutoSleepInfo.setText(R.string.open_auto_sleep_detail);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.auto_start_item:
                stopAutoStartCheckStatus = !stopAutoStartCheckStatus;
                mStopAutoStartSwitch.setChecked(stopAutoStartCheckStatus);
                break;
            case R.id.stop_wake_item:
                stopWakeCheckStatus = !stopWakeCheckStatus;
                mStopWakeSwitch.setChecked(stopWakeCheckStatus);
                break;
            case R.id.auto_sleep_item:
                autoSleepCheckStatus = !autoSleepCheckStatus;
                mAutoSleepSwitch.setChecked(autoSleepCheckStatus);
                break;
        }
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
        InfoDialog.showToast(this, getString(R.string.app_not_install));
        finish();
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
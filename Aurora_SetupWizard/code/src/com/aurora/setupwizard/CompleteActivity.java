package com.aurora.setupwizard;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.setupwizard.navigationbar.SetupWizardNavBar;

import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class CompleteActivity extends AuroraActivity implements SetupWizardNavBar.NavigationBarListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setAuroraContentView(R.layout.setup_complete, AuroraActionBar.Type.Empty);
        getAuroraActionBar().setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        com.aurora.utils.SystemUtils.switchStatusBarColorMode(
                com.aurora.utils.SystemUtils.STATUS_BAR_MODE_BLACK, this);
    }

    private void complete() {
        // Add a persistent setting to allow other apps to know the device has been provisioned.
        Settings.Global.putInt(getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.USER_SETUP_COMPLETE, 1);

        // remove this activity from the package manager.
        PackageManager pm = getPackageManager();
        ComponentName name = new ComponentName(this, LanguageSetupWizard.class);
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);

        enableStatusBar();
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        am.forceStopPackage(getPackageName());
    }

    /*private void disableSetupWizards(Intent intent) {
        final PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }*/

    public void enableStatusBar() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }

    @Override
    public void onNavigationBarCreated(SetupWizardNavBar bar) {
        bar.getNextButton().setText(R.string.finish);
    }

    @Override
    public void onNavigateBack() {
        onBackPressed();
    }

    @Override
    public void onNavigateNext() {
        complete();
    }
}

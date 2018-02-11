package com.aurora.puremanager.activity;

import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import com.aurora.puremanager.R;
import com.aurora.puremanager.service.PowerManagerService;
import com.aurora.puremanager.utils.Consts;
import com.aurora.puremanager.utils.Log;
import com.aurora.puremanager.utils.StateController;

import java.lang.reflect.Field;

import aurora.app.AuroraActivity;

public class WaitingActivity extends AuroraActivity {

    private static final String TAG = "WaitingActivity";
    private static final int INTO_SUPER_POWER_SAVE_MODE = 0;
    private static final int EXIT_SUPER_POWER_SAVE_MODE = 1;
    private static final String LAST_MODE = "mode";
    private int mPreviousMode;
    private Context mContext;
    private static final boolean DEBUG = true;
    private View mView;
    private static final int TIME_OUT = 5 * 60 * 1000;
    private static final int TIME_OUT_CHECK_SCREENOFF_QUIT = 7 * 1000;
    private static final int EVENT_ID = 1;
    private static final int EVENT_CHECK_SCREENOFF_QUIT = 2;
    private static final int EVENT_HOME = 3;
    private static String FOOL_PROOF = "fool_proof";
    private int mFlag = -1;
    private static final int EVENT_INIT_CHECK_PROCESS_FLOW = 21;
    private static final int EVENT_GET_FOOL_PROOF = 22;
    private static final int EVENT_TIMEOUT_CHECK_PROCESS_FLOW = 23;
    private static final int EVENT_RESTORE_NONE_MODE = 24;
    private Messenger mService = null;
    boolean mIsBound;
    private BroadcastReceiver mModeChangeDoneReceiver;
    private boolean mIsFinished = false;

    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EVENT_HOME:
                    enterSaveLauncher();
                case EVENT_ID:
                    break;
                case EVENT_RESTORE_NONE_MODE:
                    break;
                case EVENT_CHECK_SCREENOFF_QUIT:
                    if (mService != null) {
                        try {
                            mMessenger.send(Message.obtain(null, EVENT_GET_FOOL_PROOF));
                        } catch (RemoteException e) {
                        }
                    }
                    break;
                case EVENT_INIT_CHECK_PROCESS_FLOW:
                    Log.e(TAG, "mHandler:EVENT_INIT_CHECK_PROCESS_FLOW");
                    Intent servIntent = new Intent(mContext, PowerManagerService.class);
                    servIntent.setAction(Consts.MODE_CHANGE_INTENT);
                    servIntent.putExtra("from", from);
                    servIntent.putExtra("to", to);
                    mContext.startService(servIntent);
                    break;
                case EVENT_GET_FOOL_PROOF:
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    home.addCategory(Intent.CATEGORY_HOME);
                    mContext.startActivity(home);
                    WaitingActivity.this.finish();
                    break;
                default:
                    break;
            }
        }
    };

    final Messenger mMessenger = new Messenger(mHandler);
    private int from;
    private int to;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate--->");
        mContext = this;

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.powersaver_waiting_view);
        initView();
        initDate();
        mIsBound = true;
        selectSuperModeInOrOut();
        mModeChangeDoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Log.e(TAG, "mModeChangeDoneReceiver$onReceive");
                String action = intent.getAction();
                //Log.e(TAG, "aciton = " + action);
                if (action.equals(StateController.EVENT_FORCESTOP_PKG)) {
                    String pkgName = intent.getStringExtra(StateController.EVENT_FORCESTOP_KEY);
                    //Log.e(TAG, "EVENT_FORCESTOP_PKG " + pkgName);
                    //manager.forceStopPackage(pkgName);

                    try {
                        //Log.d(TAG, " freezeApp PkgName ---> " + pkgName);
                        //mContext.getPackageManager().setApplicationEnabledSetting(pkgName,
                        //       PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                        //manager.forceStopPackage(pkgName);
                        getPackageManager().setApplicationEnabledSetting(pkgName,
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                    return;
                }
                if (mIsFinished) {
                    return;
                }
                mContext.unregisterReceiver(mModeChangeDoneReceiver);
                final Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                home.addCategory(Intent.CATEGORY_HOME);

                if (action.equals(StateController.EVENT_EXIT_SUPERMODE_FINISH)) {
                    mIsFinished = true;
                }
                if (action.equals(StateController.EVENT_START_SUPERMODE_FINISH)) {
                    if (mContext.getPackageManager().resolveActivity(home, 0) == null) {
                        Log.e(TAG, "mModeChangeDoneReceiver$onReceive " + "home is nul");
                        return;
                    }
                }
                enterSaveLauncher();
            }
        };
        IntentFilter modeChangeDoneReceiver = new IntentFilter();
        modeChangeDoneReceiver.addAction(StateController.EVENT_EXIT_SUPERMODE_FINISH);
        modeChangeDoneReceiver.addAction(StateController.EVENT_START_SUPERMODE_FINISH);
        modeChangeDoneReceiver.addAction(StateController.EVENT_FORCESTOP_PKG);
        modeChangeDoneReceiver.setPriority(Integer.MAX_VALUE);
        mContext.registerReceiver(mModeChangeDoneReceiver, modeChangeDoneReceiver);
    }

    private void enterSaveLauncher() {
        Log.e(TAG, "enterSaveLauncher");
        Intent home = new Intent(Intent.ACTION_MAIN);

        //home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        home.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);

        home.addCategory(Intent.CATEGORY_HOME);

        mContext.startActivity(home);
        WaitingActivity.this.finish();
    }

    @Override
    protected void onPause() {
        if (mHandler.hasMessages(EVENT_ID)) {
            mHandler.removeMessages(EVENT_ID);
        }
        /*if (to == Consts.NONE_MODE) {
            Settings.System.putString(getContentResolver(), Consts.POWER_MODE_KEY, Consts.POWER_MODE_NORMAL);
        } else if (to == Consts.SUPER_MODE) {
            Settings.System.putString(getContentResolver(), Consts.POWER_MODE_KEY, Consts.POWER_MODE_SUPER);
        }*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        mHandler.sendEmptyMessageDelayed(EVENT_ID, TIME_OUT);
        enableStatusBar(false);
        super.onResume();
    }

    public void enableStatusBar(boolean enable) {
        StatusBarManager sm = (StatusBarManager) mContext.getSystemService(Context.STATUS_BAR_SERVICE);
        if (enable) {
            sm.disable(StatusBarManager.DISABLE_NONE);
            Intent intent = new Intent();
            intent.setAction(Consts.ENABLE_HANDLER);
            sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        } else {
            sm.disable(StatusBarManager.DISABLE_EXPAND);
            Intent intent = new Intent();
            intent.setAction(Consts.DISABLE_HANDLER);
            sendBroadcastAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        }
    }

    private void initView() {
        Log.e(TAG, "initView");
        //dispatchAllKey();
        //ImageView mImageViewFilling = (ImageView) findViewById(R.id.wait_anim);
        //((AnimationDrawable) mImageViewFilling.getBackground()).start();
    }

    private void dispatchAllKey() {
        try {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            Field field = lp.getClass().getDeclaredField("isHomekeyDispatched");
            field.setAccessible(true);
            field.set(lp, 1);
            getWindow().setAttributes(lp);
        } catch (Exception e) {
        }
    }

    private void initDate() {
        mContext = this.getApplicationContext();
    }

    private void selectSuperModeInOrOut() {
        from = 0;
        to = 0;
        mFlag = getIntent().getIntExtra("power_flag", -1);
        if (mFlag == INTO_SUPER_POWER_SAVE_MODE) {
            from = getIntent().getIntExtra("from", 0);
            to = Consts.SUPER_MODE;
        } else if (mFlag == EXIT_SUPER_POWER_SAVE_MODE) {
            from = Consts.SUPER_MODE;
            to = Consts.NONE_MODE;
        }

        try {
            mMessenger.send(Message.obtain(null, EVENT_INIT_CHECK_PROCESS_FLOW));
        } catch (RemoteException e) {
        }
    }

    @Override
    protected void onDestroy() {
        if (to == Consts.NONE_MODE) {
            enableStatusBar(true);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
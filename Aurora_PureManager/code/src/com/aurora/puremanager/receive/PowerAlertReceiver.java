package com.aurora.puremanager.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.WindowManager;

import com.aurora.puremanager.R;
import com.aurora.puremanager.activity.WaitingActivity;
import com.aurora.puremanager.utils.Consts;
import com.aurora.puremanager.utils.Log;

import aurora.app.AuroraAlertDialog;

/**
 * Created by joy on 1/21/16.
 */
public class PowerAlertReceiver extends BroadcastReceiver {

    private final String TAG = "PowerAlertReceiver";
    private final String POWER_LEVEL = "low_power_level";

    @Override
    public void onReceive(Context context, Intent intent) {
        /*int level = intent.getIntExtra(POWER_LEVEL, 0);
        Log.e(TAG, "PowerAlertReceiver onReceive " + level);
        if (level == 0) {
            return;
        } else {
            if (level == 15 || level == 10) {
                createDialog(context, level);
            }
        }*/

        String mode = Settings.System.getString(context.getContentResolver(), Consts.POWER_MODE_KEY);
        if ("super".equals(mode)) {
            Log.e(TAG, "already in super mode");
        } else {
            intoSuperSaveMode(context);
        }
    }

    private void dissmissDialog() {
        if (mPermDialog != null && mPermDialog.isShowing()) {
            mPermDialog.dismiss();
        }
    }

    private AuroraAlertDialog mPermDialog;

    private void createDialog(final Context context, int level) {
        mPermDialog = new AuroraAlertDialog.Builder(context, AuroraAlertDialog.THEME_AMIGO_FULLSCREEN)
                .create();
        mPermDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        mPermDialog.setMessage(context.getString(R.string.power_warning, level));

        DialogInterface.OnClickListener dialogClickLsn = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case AuroraAlertDialog.BUTTON_POSITIVE:
                        intoSuperSaveMode(context);
                        break;
                    case AuroraAlertDialog.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };

        mPermDialog.setButton(AuroraAlertDialog.BUTTON_POSITIVE, context.getString(R.string.switch_string),
                dialogClickLsn);
        mPermDialog.setButton(AuroraAlertDialog.BUTTON_NEGATIVE, context.getString(R.string.cancel),
                dialogClickLsn);
        mPermDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {

            }
        });

        mPermDialog.show();
    }

    private void intoSuperSaveMode(Context context) {
        Intent intent = new Intent(context, WaitingActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("power_flag", 0);
        bundle.putInt("from", Consts.NONE_MODE);
        intent.putExtras(bundle);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

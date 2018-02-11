//Gionee <jianghuan> <2013-09-29> add for CR00975553 begin
package com.aurora.puremanager.traffic;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Window;
import aurora.app.AuroraActivity;

import com.aurora.puremanager.R;

public class TrafficPopWindows extends AuroraActivity {

    private Context mContext;
    private int mActivatedSimIndex;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent intent = this.getIntent();
        mActivatedSimIndex = intent.getIntExtra("sim_activatedindex", 0);
        popDialog();
    }

    private void popDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext)
//                .setTitle(mContext.getString(R.string.mobile_data_stop_title))
                .setMessage(mContext.getString(R.string.mobile_data_stop_content))
                .setPositiveButton(mContext.getString(R.string.action_stop),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                SIMInfoWrapper.getDefault(mContext).setGprsEnable("setMobileDataEnabled",
                                        false);
                                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                        .putInt(TrafficPreference.getSimFlowlinkFlag(mActivatedSimIndex), 1)
                                        .commit();

                                TrafficPopWindows.this.finish();
                                // MobileTemplate.setMobileDataEnabled(mContext,
                                // false);
                            }
                        })
                .setNegativeButton(mContext.getString(R.string.action_restart),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                TrafficPopWindows.this.finish();
                                PreferenceManager.getDefaultSharedPreferences(mContext).edit()
                                        .putInt(TrafficPreference.getSimFlowlinkFlag(mActivatedSimIndex), 2)
                                        .commit();
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        // TODO Auto-generated method stub
                                        // TrafficProcessorService.processIntent(mContext,
                                        // false);
                                    }
                                }).start();

                            }
                        }).create();

        alertDialog.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                    dialog.dismiss();
                    TrafficPopWindows.this.finish();
                }
                return false;
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
// Gionee <jianghuan> <2013-09-29> add for CR00975553 end
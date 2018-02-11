/*
 *
 * Copyright (C) 2012 gionee Inc
 *
 * Author: fangbin
 *
 * Description:
 *
 * history
 * name                              date                                      description
 *
 */
package com.gionee.mms.popup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import aurora.widget.AuroraButton;

import com.android.mms.MmsApp;
import com.android.mms.R;
import aurora.app.AuroraActivity;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.transaction.WapPushMessagingNotification;
import com.gionee.mms.popup.PopUpView.showSimSelectViewListener;
import com.gionee.mms.ui.SimSelectView;
//gionee <gaoj> <2013-07-02> add for CR00832502 begin
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
//gionee <gaoj> <2013-07-02> add for CR00832502 end

public class PopUpMsgActivity extends AuroraActivity implements MessageStatusListener{
    private PopUpView mPopUpView = null;
    private int mCurrentWindowWidth = -1;
    // gionee zhouyj 2013-04-02 add for CR00792038 start
    public static PopUpMsgActivity sPopUpMsgActivity = null;
    // gionee zhouyj 2013-04-02 add for CR00792038 end

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        registerBroadcastReceiver();
        PopUpUtils.mPopUpShowing = true;
        setContentView(R.layout.gn_popup_main);
        //gionee <gaoj> <2013-07-02> add for CR00832502 begin
        if (MmsApp.mGnNewPopSupport) {
            LayoutParams params = getWindow().getAttributes();
            params.width = LayoutParams.MATCH_PARENT;
        }
        //gionee <gaoj> <2013-07-02> add for CR00832502 end
        mCurrentWindowWidth = getWindowWidth();
        initViews();
        if (null != getIntent() && null != getIntent().getExtras()) {
            updateContent(getIntent().getExtras());
        }
        // gionee zhouyj 2013-04-02 add for CR00792038 start
        sPopUpMsgActivity = this;
        // gionee zhouyj 2013-04-02 add for CR00792038 end
    }

    public void initViews() {
        mPopUpView = (PopUpView) findViewById(R.id.popUpView);
        mPopUpView.setmCurrentWindowWidth(mCurrentWindowWidth);
        mPopUpView.setOnFinishCallBackListener(new PopUpView.OnFinishCallBackListener() {

            @Override
            public void onFinishCallBack() {
                // TODO Auto-generated method stub
                PopUpMsgActivity.this.finish();
            }
        });
        //gionee gaoj 2012-12-24 added for CR00745515 start
        mPopUpView.setshowSimSelectViewListener(new showSimSelectViewListener() {
            
            @Override
            public void showSimSelectView() {
                // TODO Auto-generated method stub
                AuroraButton mResponseBtn = (AuroraButton) mPopUpView.findViewById(R.id.responseBtn);
                int[] location = new  int[2] ;
                mResponseBtn.getLocationOnScreen(location);
                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                int x = 0;
                int y = location[1] - dm.heightPixels/2 -145;
                Intent viewIntent = new Intent(PopUpMsgActivity.this, SimSelectView.class);
                viewIntent.putExtra("x", x);
                viewIntent.putExtra("y", y);
                viewIntent.putExtra("isPop", true);
                startActivity(viewIntent);
            }
        });
        //gionee gaoj 2012-12-24 added for CR00745515 end
    }

    public void updateContent(Bundle bundle) {
        if (null != bundle) {
            mPopUpView.addInfoNode(bundle);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        if (null != intent && null != intent.getExtras()) {
            updateContent(intent.getExtras());
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        //gionee gaoj 2012-12-24 added for CR00745515 start
        unregisterReceiver(mSimSelectReceiver);
        //gionee gaoj 2012-12-24 added for CR00745515 end
        unregisterReceiver(mMsgReceiver);
        mPopUpView.setVisibility(View.GONE);
        PopUpUtils.mPopUpShowing = false;
        mPopUpView.destroy();
        mPopUpView = null;
        // gionee zhouyj 2013-04-02 add for CR00792038 start
        sPopUpMsgActivity = null;
        // gionee zhouyj 2013-04-02 add for CR00792038 end
    }

    private int getWindowWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    @Override
    public void onProtocolChanged(boolean mms, boolean needToast) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAttachmentChanged() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPreMessageSent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMessageSent() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onMaxPendingMessagesReached() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onAttachmentError(int error) {
        // TODO Auto-generated method stub

    }

    private BroadcastReceiver mMsgReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(PopUpUtils.MSG_INFO_RECEIVER_ACTION)) {
                if (null != intent && null != intent.getExtras()) {
                    updateContent(intent.getExtras());
                }
            }
        }
    };

    //gionee gaoj 2012-12-24 added for CR00745515 start
    private BroadcastReceiver mSimSelectReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(SIM_INFO_RECEIVER_ACTION)) {
                if (null != intent && null != intent.getExtras()) {
                    int simid = intent.getIntExtra("simid", -1);
                    if (simid != -1) {
                        mPopUpView.sendMsg(simid);
                    }
                }
            }
        }
    };

    public static final String SIM_INFO_RECEIVER_ACTION = "android.intent.action.PopUpMsgActivity.SimSelectReceiver";
    //gionee gaoj 2012-12-24 added for CR00745515 end

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PopUpUtils.MSG_INFO_RECEIVER_ACTION);
        registerReceiver(mMsgReceiver, filter);
        //gionee gaoj 2012-12-24 added for CR00745515 start
        IntentFilter simfilter = new IntentFilter();
        simfilter.addAction(SIM_INFO_RECEIVER_ACTION);
        registerReceiver(mSimSelectReceiver, simfilter);
        //gionee gaoj 2012-12-24 added for CR00745515 end
    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        if (null != mPopUpView) {
            // Gionee fangbin 20120517 removed for CR00596563 start
            //mPopUpView.resetView();
            /// Gionee fangbin 20120517 removed for CR00596563 start
        }
    }
    
    // gionee zhouyj 2012-11-14 add for CR00729441 start 
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if (MmsApp.mIsSafeModeSupport) {
            finish();
        }
        super.onResume();
    }
    // gionee zhouyj 2012-11-14 add for CR00729441 end 

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        // Gionee fangbin 20120625 removed for CR00627746 start
        /*
        MessagingNotification.blockingUpdateNewMessageIndicator(this, false, false);
        WapPushMessagingNotification.nonBlockingUpdateNewMessageIndicator(this, false);
        */
        // Gionee fangbin 20120625 removed for CR00627746 end
    }

    @Override
    public void onPreMmsSent() {
        // TODO Auto-generated method stub
        
    }
}

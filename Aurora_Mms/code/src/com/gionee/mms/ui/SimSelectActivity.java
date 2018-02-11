package com.gionee.mms.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.mms.MmsApp;
import com.android.mms.R;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageUtils;
import android.os.ServiceManager;
import gionee.provider.GnTelephony.SIMInfo;
import com.android.internal.telephony.ITelephony;
import com.aurora.featureoption.FeatureOption;
import com.gionee.internal.telephony.GnTelephonyManagerEx;
import com.gionee.internal.telephony.GnPhone;

import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SimSelectActivity extends AuroraActivity{
    private List<SIMInfo> mSimInfoList;
    private int mSelectedSimId;
    private int mSimCount;
    private int mAssociatedSimId;
    private AuroraAlertDialog mSIMSelectDialog;
    
    private static final String TAG = "SimSelectActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
         if (MmsApp.mDarkTheme) {
            setTheme(android.R.style.Theme_Holo_Dialog);
        }
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        init();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
    
    private void getSimInfoList() {
        if (MmsApp.mGnMultiSimMessage) {
            mSimInfoList = SIMInfo.getInsertedSIMList(this);
            mSimCount = mSimInfoList.isEmpty()? 0: mSimInfoList.size();
        } else { // single SIM
            ITelephony phone = ITelephony.Stub.asInterface(ServiceManager.checkService("phone"));
            if (phone != null) {
                try {
                    mSimCount = phone.hasIccCard() ? 1 : 0;
                } catch (RemoteException e) {
                    Log.e(MmsApp.TXN_TAG, "check sim insert status failed");
                    mSimCount = 0;
                }
            }
        }
    }
    
    private void init() {
        List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
        getSimInfoList();
        for (int i = 0; i < mSimCount; i++) {
            SIMInfo simInfo = mSimInfoList.get(i);
            HashMap<String, Object> entry = new HashMap<String, Object>();

            entry.put("simIcon", simInfo.mSimBackgroundRes);
            int state = MessageUtils.getSimStatus(i, mSimInfoList, GnTelephonyManagerEx.getDefault());
            entry.put("simStatus", MessageUtils.getSimStatusResource(state));
            String simNumber = "";
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                switch(simInfo.mDispalyNumberFormat) {
                    //case android.provider.Telephony.SimInfo.DISPLAY_NUMBER_DEFAULT:
                    // Aurora xuyong 2013-11-15 modified for S4 adapt start
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_FIRST:
                    // Aurora xuyong 2013-11-15 modified for S4 adapt end
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(0, 4);
                        break;
                    // Aurora xuyong 2013-11-15 modified for S4 adapt start
                    case gionee.provider.GnTelephony.SimInfo.DISPLAY_NUMBER_LAST:
                    // Aurora xuyong 2013-11-15 modified for S4 adapt end
                        if(simInfo.mNumber.length() <= 4)
                            simNumber = simInfo.mNumber;
                        else
                            simNumber = simInfo.mNumber.substring(simInfo.mNumber.length() - 4);
                        break;
                    case 0://android.provider.Telephony.SimInfo.DISPLAY_NUMBER_NONE:
                        simNumber = "";
                        break;
                }
            }
            if (!TextUtils.isEmpty(simNumber)) {
                entry.put("simNumberShort",simNumber);
            } else {
                entry.put("simNumberShort", "");
            }

            entry.put("simName", simInfo.mDisplayName);
            if (!TextUtils.isEmpty(simInfo.mNumber)) {
                entry.put("simNumber", simInfo.mNumber);
            } else {
                entry.put("simNumber", "");
            }
            if (mAssociatedSimId == (int) simInfo.mSimId) {
                // if this SIM is contact SIM, set "Suggested"
                entry.put("suggested", getString(R.string.suggested));
            } else {
                entry.put("suggested", "");// not suggested
            }
            if(MessageUtils.mUnicomCustom || MessageUtils.mShowDigitalSlot) {
                if((int) simInfo.mSlot == 0) {
                    entry.put("sim3g", getString(R.string.gn_sim_slot_1));
                } else if((int) simInfo.mSlot == 1) {
                    entry.put("sim3g", getString(R.string.gn_sim_slot_2));
                } else {
                    entry.put("sim3g", "");
                }
            } else if(MessageUtils.mShowSlot) {
                if((int) simInfo.mSlot == 0) {
                    entry.put("sim3g", getString(R.string.gn_sim_slot_a));
                } else if((int) simInfo.mSlot == 1) {
                    entry.put("sim3g", getString(R.string.gn_sim_slot_b));
                } else {
                    entry.put("sim3g", "");
                }
            }
            else {
                entry.put("sim3g", "");
            }
            entries.add(entry);
        }

        final SimpleAdapter a = new SimpleAdapter(
                this,
                entries,
                R.layout.gn_sim_selector,
                new String[] {"simIcon", "simStatus", "simNumberShort", "simName", "simNumber", "suggested", "sim3g"},
                new int[] {R.id.sim_icon, R.id.sim_status, R.id.sim_number_short, 
                        R.id.sim_name, R.id.sim_number, R.id.sim_suggested, R.id.sim3g});
        SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                    TextView nameTextView = (TextView) view.findViewById(R.id.sim_name);
                    if (nameTextView != null) {
                        if (MmsApp.mDarkTheme) 
                            nameTextView.setTextColor(Color.WHITE);
                    }
                    TextView numTextView = (TextView) view.findViewById(R.id.sim_number);
                    if (numTextView != null) {
                        if(MmsApp.mDarkTheme)
                            numTextView.setTextColor(Color.GRAY);
                        else 
                            numTextView.setTextColor(R.color.gn_color_gray);
                    }
                if (view instanceof ImageView) {
                    if (view.getId() == R.id.sim_icon) {
                        ImageView simicon = (ImageView) view.findViewById(R.id.sim_icon);
                        simicon.setBackgroundResource((Integer) data);
                    } else if (view.getId() == R.id.sim_status) {
                        ImageView simstatus = (ImageView)view.findViewById(R.id.sim_status);
                        if ((Integer)data != GnPhone.SIM_INDICATOR_UNKNOWN
                                && (Integer)data != GnPhone.SIM_INDICATOR_NORMAL) {
                            simstatus.setVisibility(View.VISIBLE);
                            simstatus.setImageResource((Integer)data);
                        } else {
                            simstatus.setVisibility(View.GONE);
                        }
                    }
                    return true;
                }
                if(view instanceof TextView) {
                    if (view.getId() == R.id.sim_number) {
                        TextView simNumber = (TextView)view.findViewById(R.id.sim_number);
                        if(!TextUtils.isEmpty((String)data)) {
                            simNumber.setText((String)data);
                            simNumber.setVisibility(View.VISIBLE);
                        } else {
                            simNumber.setText("");
                            simNumber.setVisibility(View.GONE);
                        }
                        return true;
                    }
                }
                return false;
            }
        };
        a.setViewBinder(viewBinder);
        AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this)
            .setTitle(getString(R.string.sim_selected_dialog_title))
            .setCancelable(true)
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    // TODO Auto-generated method stub
                    setResult(RESULT_CANCELED);
                    dialog.dismiss();
                    finish();
                }
            })
            .setAdapter(a, new DialogInterface.OnClickListener() {
                @SuppressWarnings("unchecked")
                public final void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent("SIM_SELECT");
                    i.putExtra("slot", which);
                    setResult(RESULT_OK, i);
                    dialog.dismiss();
                    finish();
                }});
        mSIMSelectDialog = builder.create();
        mSIMSelectDialog.show();
        mSIMSelectDialog.setCanceledOnTouchOutside(false);
    }

}

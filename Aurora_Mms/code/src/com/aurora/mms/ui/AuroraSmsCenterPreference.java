package com.aurora.mms.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import aurora.preference.AuroraEditTextPreference;

import com.android.mms.R;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.MessageUtils;

import gionee.provider.GnTelephony.SIMInfo;

public class AuroraSmsCenterPreference extends AuroraEditTextPreference{
    
    private int sDrawbaleId;
    private String sOpratorName;
    private String sSmsCenterNumber;
    
    
    private Context mContext;
    
    private TextView mOperatorView;
    private TextView mSmsCenterNumberView;
    
    private ImageView mSimThumbImage;
    
    public AuroraSmsCenterPreference(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.preferenceStyle);
        mContext = context;
    }
    
    @Override
    public View onCreateView(ViewGroup parent) {
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.aurora_sms_center_layout, null);

        return layout;
    }
    
    @Override
    protected void onBindView(View view) {
        if (view != null) {
            mOperatorView = (TextView)view.findViewById(R.id.aurora_operator_name);
            mOperatorView.setText(sOpratorName);
            mSmsCenterNumberView = (TextView)view.findViewById(R.id.aurora_sms_center_number);
            mSmsCenterNumberView.setText(sSmsCenterNumber);
            mSimThumbImage = (ImageView)view.findViewById(R.id.aurora_sms_center_thumbnail);
            mSimThumbImage.setImageResource(sDrawbaleId);
        }
    }
    
    public void setOperator(String operatorName) {
        if (operatorName.endsWith("00") || operatorName.endsWith("02") || operatorName.endsWith("07")) {
            sOpratorName = mContext.getResources().getString(R.string.aurora_operator_cm);
        } else if (operatorName.endsWith("01")) {
            sOpratorName = mContext.getResources().getString(R.string.aurora_operator_cu);
        } else if (operatorName.endsWith("03")) {
            sOpratorName = mContext.getResources().getString(R.string.aurora_operator_ct);
        }
    }
    
    public void setSmsCenterNumber(String setSmsCenterNumvber) {
        sSmsCenterNumber = setSmsCenterNumvber;
    }
    
    public void setSimThumbNail(int slot) {
        int simId = -1;
        switch (slot) {
            case 0:
                SIMInfo info1 = SIMInfo.getSIMInfoBySlot(mContext
                            , 0);
                if (info1 != null) {
                    simId = (int)(info1.mSimId);
                }
                break;
            case 1:
                SIMInfo info2 = SIMInfo.getSIMInfoBySlot(mContext
                            , 1);
                if (info2 != null) {
                    simId = (int)(info2.mSimId);
                }
                break;
        }
        sDrawbaleId = MessageUtils.getSimBigIcon(mContext, simId);
    }

    public String getSmsCenterNumber() {
        if (mSmsCenterNumberView != null) {
            return (String) mSmsCenterNumberView.getText();
        } 
        return null;
    }
    
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        getEditText().setText(sSmsCenterNumber);
    }


}

package com.android.contacts.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.contacts.ContactsApplication;
import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
//GIONEE:liuying 2012-7-5 modify for CR00637517 start
import gionee.provider.GnTelephony.SIMInfo;
import java.util.List;
//GIONEE:liuying 2012-7-5 modify for CR00637517 end

public class AuroraDialpadAdditionalButtonsV2 extends FrameLayout {
    //Gionee:huangzy 20130325 add for CR00788980 start
	int mMinimumHeight = 0;
    //Gionee:huangzy 20130325 add for CR00788980 end
	
    private View mSeeContactsView;
    private View mDialView;
    private View mVideoDialView;    
    private View mShowMenuView;
    private View mShowDialpadView;

    //GIONEE:liuying 2012-7-5 modify for CR00637517 start
    private List<SIMInfo> mSimInfoList = null;
    private int mSimCount = 0;
    //GIONEE:liuying 2012-7-5 modify for CR00637517 end
    public AuroraDialpadAdditionalButtonsV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        if (ContactsApplication.sGnGeminiDialSupport){
        	mSimInfoList = SIMInfo.getInsertedSIMList(getContext());
        	mSimCount = mSimInfoList.isEmpty() ? 0 : mSimInfoList.size();
    	}
        
		if (ContactsApplication.sGnGeminiDialSupport && mSimCount>1) {
			inflate(getContext(), R.layout.gn_dialpad_additional_buttons_v2_sim1sim2, this);
		} else {
			inflate(getContext(), R.layout.aurora_dialpad_additional_buttons_v2, this);
		}
    }

    @Override
    protected void onFinishInflate() {
        // TODO Auto-generated method stub
        super.onFinishInflate();

        mSeeContactsView = findViewById(R.id.contacts);
        mDialView  = findViewById(R.id.dialButton);
        mShowMenuView =  findViewById(R.id.overflow_menu);
        mShowDialpadView = findViewById(R.id.showDialpadButton);
        mVideoDialView = findViewById(R.id.videoDialButton);
    }
    
    public void showShowDialpadView(boolean show) {
    	mShowDialpadView.setVisibility(show ? View.VISIBLE : View.GONE);
    	mDialView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    public void showVideoDialView(boolean show) {
    	mVideoDialView.setVisibility(show ? View.VISIBLE : View.GONE);
    	mShowMenuView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
    	
    	//Gionee:huangzy 20130325 add for CR00788980 start
    	View line = findViewById(R.id.additional_buttons);
    	if (null != line) {
    		int height = getHeight();
    		if (height > mMinimumHeight ) {
    			mMinimumHeight = height;
    			line.setMinimumHeight(height);
    		}	
    	}
    	//Gionee:huangzy 20130325 add for CR00788980 end
    }
}

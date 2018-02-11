package com.android.contacts.util;

import com.android.contacts.ContactsApplication;

import android.os.Handler;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

public abstract class EditTextSwitchAdder implements Runnable {
	private String[] mPrints;
    private int mPrinting;
    private boolean mIsFirstLongClick = true;
    private AuroraEditText mAddTo;
    private Handler mHandler;
    private long mDelayMillis = 500;
    
    public EditTextSwitchAdder(AuroraEditText addTo, Handler handler, String[] prints4Switch) {
    	mAddTo = addTo;
    	mHandler = handler;
    	mPrints = prints4Switch;
    	mPrinting = mPrints.length + 1;
    }
    
    public void reset() {
    	mPrinting = mPrints.length + 1;
    	mIsFirstLongClick = true;        	
    }
    
	@Override
	public void run() {
		if (isGoingOn()) {
			mPrinting = (++mPrinting)%mPrints.length;
			Editable ea = mAddTo.getEditableText();
			
			int start = mAddTo.getSelectionStart();
			int end = mAddTo.getSelectionEnd();
			
			if (!mIsFirstLongClick && start > 0) {
				start -= 1;
			}
					
			ea.replace(start, end, mPrints[mPrinting]);
            mAddTo.invalidate();
			
			mHandler.postDelayed(this, mDelayMillis);
			mIsFirstLongClick = false;
		} else {
			mHandler.removeCallbacks(this);
		}
	}
	
	public abstract boolean isGoingOn();

	public void setDelayMillis(long delayMillis) {
		this.mDelayMillis = delayMillis;
	}
}
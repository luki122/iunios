package com.android.contacts.util;

import android.os.Handler;
import android.text.Editable;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

//Gionee:huangzy 20120710 add for CR00614809 start
public abstract class EditTextControler implements Runnable {
    protected AuroraEditText mEditText;
    protected Handler mHandler;
    protected long mDelayMillis = 500;
    protected boolean mIsFirstLongClick = true;
    
    public EditTextControler(AuroraEditText editText, Handler hander) {
        mEditText = editText;
        mHandler = hander;
    }
    
    public void reset() {}
    
    public abstract boolean isGoingOn();
    
    public void setDelayMillis(long delayMillis) {
        this.mDelayMillis = delayMillis;
    }
    
    //=========================================================
    
    public static abstract class EditTextSwitchAdder extends EditTextControler {
        private String[] mPrints;
        private int mPrinting;
        
        public EditTextSwitchAdder(AuroraEditText addTo, Handler handler, String[] prints4Switch) {
            super(addTo, handler);
            mPrints = prints4Switch;
            mPrinting = mPrints.length + 1;
            mDelayMillis = 500;
        }
        
        public void reset() {
            mPrinting = mPrints.length + 1;
            mIsFirstLongClick = true;           
        }
        
        @Override
        public void run() {
            if (isGoingOn()) {
                mPrinting = (++mPrinting)%mPrints.length;
                Editable ea = mEditText.getEditableText();
                
                int start = mEditText.getSelectionStart();
                int end = mEditText.getSelectionEnd();
                
                if (!mIsFirstLongClick && start > 0) {
                    start -= 1;
                }
                        
                ea.replace(start, end, mPrints[mPrinting]);
                mEditText.invalidate();
                
                mHandler.postDelayed(this, mDelayMillis);
                mIsFirstLongClick = false;
            } else {
                mHandler.removeCallbacks(this);
            }
        }
    }
    
//    public static abstract class EditTextDeleter extends EditTextControler {
//
//        public EditTextDeleter(AuroraEditText editText, Handler hander) {
//            super(editText, hander);
//            mDelayMillis = 100;
//        }
//        
//        @Override
//        public void run() {
//            if (isGoingOn()) {
//                Editable ea = mEditText.getEditableText();
//                
//                int start = mEditText.getSelectionStart();
//                
//                if (start > 0) {
//                    ea.replace(start-1, start, "");
//                    mEditText.invalidate();
//                    mHandler.postDelayed(this, mDelayMillis);
//                    return;
//                }
//            }
//            
//            mHandler.removeCallbacks(this);
//        }
//        
//    }
}
//Gionee:huangzy 20120710 add for CR00614809 end
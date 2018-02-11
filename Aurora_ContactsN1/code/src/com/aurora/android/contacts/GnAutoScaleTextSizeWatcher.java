package com.aurora.android.contacts;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

//Gionee:huangzy 20130325 add for CR00788980
public class GnAutoScaleTextSizeWatcher implements TextWatcher {

    private static final String TAG = "AutoScaleTextSizeWatcher";

     
    private Context mContext;
     
    protected AuroraEditText mTarget;

     
    private final int MAX_TEXT_SIZE;
     
    private final int MIN_TEXT_SIZE;
     
    private final int DELTA_TEXT_SIZE;
     
    private final int MAX_DELTA_TRY;
    
     
    private int mCurrentTextSize;
     
    private volatile int mPreTextLength;

    private Paint mPaint;

    public GnAutoScaleTextSizeWatcher(AuroraEditText editText,
    		int minTextSize, int maxTextSize, int deltaTextSize) {

    	MAX_TEXT_SIZE = maxTextSize;
        MIN_TEXT_SIZE = minTextSize;
        DELTA_TEXT_SIZE = deltaTextSize;
        if (null == editText || maxTextSize < minTextSize || minTextSize < 0 ||
        		deltaTextSize < 0 || deltaTextSize > maxTextSize - minTextSize) {
        	throw new IllegalArgumentException();
        }
        MAX_DELTA_TRY = (maxTextSize - minTextSize)/deltaTextSize + 2;
        
        mContext = editText.getContext();
        mTarget = editText;
        mPreTextLength = editText.length();
        
        mPaint = new Paint();
        mPaint.set(editText.getPaint());
        
        mCurrentTextSize = MAX_TEXT_SIZE;
        mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, mCurrentTextSize);
    }

    protected void autoScaleTextSize() {
    	final int currentLen = mTarget.length(); 
    	if (currentLen == mPreTextLength) {
    		return;
    	}
    	
    	final String digits = mTarget.getText().toString();
        final int digitsWidth = mTarget.getWidth();
    	if(digits.length() == 0 || digitsWidth <= 0) {
            mCurrentTextSize = MAX_TEXT_SIZE;
            mPreTextLength = 0;
            mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, MAX_TEXT_SIZE);
            return;
        }
    	
    	final boolean deleted = mPreTextLength > currentLen;
    	mPreTextLength = currentLen;
    	
    	if (deleted && mCurrentTextSize == MAX_TEXT_SIZE) {
    		return;
    	}

        final int max = MAX_TEXT_SIZE;
        final int min = MIN_TEXT_SIZE;
        final int delta = DELTA_TEXT_SIZE;
        final Paint paint = mPaint;

        int curTextSize = mCurrentTextSize;
        paint.setTextSize(curTextSize);
        int inputWidth = (int)paint.measureText(digits);

        if(deleted) {
        	if (curTextSize < max) {
        		int trytimes = 0;
        		while (trytimes < MAX_DELTA_TRY) {
        			trytimes++;
        			int largerSize = curTextSize + delta;
	        		largerSize = largerSize > max ? max : largerSize;
	        		paint.setTextSize(largerSize);
	        		int largerLen = (int)paint.measureText(digits);
	        		if (largerLen < digitsWidth) {
	        			curTextSize = largerSize;
	        			if (curTextSize == max) {
	        				break;
	        			}
	        		} else {
	        			break;
	        		}
        		}
        	}
        } else {
        	if (curTextSize > min && inputWidth > digitsWidth) {
        		while (inputWidth > digitsWidth) {
                	if (curTextSize - delta < min) {
                		curTextSize = min;
                		break;
                	}
                	
                    curTextSize -= delta;
                    paint.setTextSize(curTextSize);
                    inputWidth = (int)paint.measureText(digits);
                }	
        	}
        }

        if (curTextSize != mCurrentTextSize) {
        	mCurrentTextSize = curTextSize;
            mTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, curTextSize);	
        }
    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		autoScaleTextSize();
	}
}

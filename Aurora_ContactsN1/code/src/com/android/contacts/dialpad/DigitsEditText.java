/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.contacts.dialpad;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Rect;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import aurora.widget.AuroraEditText;

/**
 * EditText which suppresses IME show up.
 */
public class DigitsEditText extends AuroraEditText {
    //Gionee:huangzy 20130325 add for CR00788980 start
	int mMinimumHeight = 0;
    //Gionee:huangzy 20130325 add for CR00788980 end
	
    public DigitsEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setInputType(getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        //mtk81281 add for cr 111694 and 111344 start
        // aurora <wangth> <2013-10-31> remove for aurora ui begin
        //setEnableShowSoftInput(false);
        // aurora <wangth> <2013-10-31> remove for aurora ui end
        //mtk81281 add for cr 111694 and 111344 end
        
        // aurora <wangth> <2013-10-31> add for aurora ui begin
        try {
            Class<AuroraEditText> cls = AuroraEditText.class;
            //Class cls = Class.forName("AuroraEditText");
            Method setShowSoftInputOnFocus = cls.getMethod(
                    "setShowSoftInputOnFocus", boolean.class);
            setShowSoftInputOnFocus.invoke(this, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // aurora <wangth> <2013-10-31> add for aurora ui end
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        final InputMethodManager imm = ((InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null && imm.isActive(this)) {
            imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean ret = super.onTouchEvent(event);
        // Must be done after super.onTouchEvent()
        final InputMethodManager imm = ((InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE));
        if (imm != null && imm.isActive(this)) {
            imm.hideSoftInputFromWindow(getApplicationWindowToken(), 0);
        }
        
        return ret;
    }

    @Override
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            // Since we're replacing the text every time we add or remove a
            // character, only read the difference. (issue 5337550)
            final int added = event.getAddedCount();
            final int removed = event.getRemovedCount();
            final int length = event.getBeforeText().length();
            if (added > removed) {
                event.setRemovedCount(0);
                event.setAddedCount(1);
                event.setFromIndex(length);
            } else if (removed > added) {
                event.setRemovedCount(1);
                event.setAddedCount(0);
                event.setFromIndex(length - 1);
            } else {
                return;
            }
        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            // The parent EditText class lets tts read "edit box" when this View has a focus, which
            // confuses users on app launch (issue 5275935).
            return;
        }
        super.sendAccessibilityEventUnchecked(event);
    }
    
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
    		int bottom) {
    	super.onLayout(changed, left, top, right, bottom);
    	
    	//Gionee:huangzy 20130325 add for CR00788980 start
    	int height = getHeight();
		if (height > mMinimumHeight) {
			mMinimumHeight = height;
			setMinimumHeight(height);	
		}
    	//Gionee:huangzy 20130325 add for CR00788980 end
    }
}
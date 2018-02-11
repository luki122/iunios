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

package com.android.calculator2;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class CalculatorViewPager extends ViewPager {
	private boolean isScrollable = false;
	
    public CalculatorViewPager(Context context) {
        super(context);
    }

    public CalculatorViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * ViewPager inherits ViewGroup's default behavior of delayed clicks
     * on its children, but in order to make the calc buttons more responsive
     * we disable that here.
     */
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    // Aurora liugj 2014-01-16 modified for aurora's new feature start
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            return super.onTouchEvent(ev);
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isScrollable == false) {
            return false;
        } else {
            return super.onInterceptTouchEvent(ev);
        }

    }

    public boolean isScrollable() {
        return isScrollable;
    }

    public void setScrollable(boolean isScrollable) {
        this.isScrollable = isScrollable;
    }
    // Aurora liugj 2014-01-16 modified for aurora's new feature end
}

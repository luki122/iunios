package com.android.systemui.recent;

import android.view.View;

interface AuroraPage {
    public int getPageChildCount();
    public View getChildOnPageAt(int i);
    public void removeAllViewsOnPage();
    public void removeViewOnPageAt(int i);
    public int indexOfChildOnPage(View v);
    public View getChildOnPageAtPoint(float x,float y);
}

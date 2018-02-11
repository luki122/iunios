package com.aurora.mms.util;
// Aurora xuyong 2014-11-07 created for bug #9526
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class AuroraMessageBodyView extends TextView {

    public AuroraMessageBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
     // do nothing
    }
    
    @Override
    public void scrollTo(int x, int y) {
        // do nothing
    }
}

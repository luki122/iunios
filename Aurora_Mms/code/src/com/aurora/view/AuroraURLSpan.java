package com.aurora.view;
// Aurora xuyong 2014-09-15 created for aurora's new feature
import com.aurora.mms.ui.ClickContent;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.View;

public class AuroraURLSpan extends URLSpan{
    // Aurora xuyong 2015-02-04 modified for bug #11531 start
    private Handler mHandler;
    // Aurora xuyong 2015-02-04 modified for bug #11531 start
    private int mState = 0;
    
    private boolean mPressed = false;
    
    private static int mPressedLinkColorId = 0x0;
    private static int mNormalLinkColorId = 0x0;
    
    public AuroraURLSpan(String url) {
        super(url);
    }
    
    public AuroraURLSpan(Parcel src) {
        super(src);
    }
    // Aurora xuyong 2015-02-04 modified for bug #11531 start
    public void setHandler(Handler handler) {
    // Aurora xuyong 2015-02-04 modified for bug #11531 end
        mHandler = handler;
    }
    
    public static void setPressedLinkedColor(int resId) {
        mPressedLinkColorId = resId;
    }
    
    public static void setNormalLinkedColor(int resId) {
        mNormalLinkColorId = resId;
    }
    
    public void setPressed(boolean state) {
        mPressed = state;
    }
    
    @Override
    public void onClick(View widget) {
        Uri uri = Uri.parse(getURL());
        // Aurora xuyong 2014-09-16 modified for bug #8331 start
        sendOptMessage(uri.toString());
        // Aurora xuyong 2014-09-16 modified for bug #8331 end
    }
    // Aurora xuyong 2014-09-16 modified for bug #8331 start
    private void sendOptMessage(String url) {
    // Aurora xuyong 2014-09-16 modified for bug #8331 end
        Message msg = mHandler.obtainMessage(AuroraMultiLinkAdapter.LINK_CLICK);
        ClickContent cc = new ClickContent();
        cc.setValue(url);
        msg.obj = cc;
        msg.sendToTarget();
    }
    
    @Override
    public void updateDrawState(android.text.TextPaint ds) {
        if (mPressed) {
            ds.setColor(mPressedLinkColorId);
        } else {
            ds.setColor(mNormalLinkColorId);
        }
        ds.setUnderlineText(false);
    }
}

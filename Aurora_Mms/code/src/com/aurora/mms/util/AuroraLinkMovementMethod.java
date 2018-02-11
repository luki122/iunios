package com.aurora.mms.util;

//Aurora xuyong 2014-09-27 added for aurora's new feature created
import com.aurora.view.AuroraURLSpan;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

public class AuroraLinkMovementMethod extends LinkMovementMethod{
    private static final int CLICK = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    
    private static AuroraLinkMovementMethod mInstance;
    
    public static AuroraLinkMovementMethod getInstance() {
        if (mInstance == null) {
            mInstance = new AuroraLinkMovementMethod();
        }
        return mInstance;
    }
    
    private AuroraURLSpan mLink = null;
    
    @Override
    protected boolean up(TextView widget, Spannable buffer) {
        boolean result = super.up(widget, buffer);
        changePressed(widget, buffer);
        return result;
    }
        
    @Override
    protected boolean down(TextView widget, Spannable buffer) {
        boolean result = super.down(widget, buffer);
        changePressed(widget, buffer);
        return result;
    }

    @Override
    protected boolean left(TextView widget, Spannable buffer) {
        boolean result = super.left(widget, buffer);
        changePressed(widget, buffer);
        return result;
    }

    @Override
    protected boolean right(TextView widget, Spannable buffer) {
        boolean result = super.right(widget, buffer);
        changePressed(widget, buffer);
        return result;
    }
    
    private void changePressed(TextView widget, Spannable buffer) {
        int start = widget.getSelectionStart();
        int end = widget.getSelectionEnd();
        
        AuroraURLSpan[] link = buffer.getSpans(start, end, AuroraURLSpan.class);
        if (link.length > 0) {
            if (mLink != null) {
                if (mLink != link[0]) {
                    mLink.setPressed(false);
                    link[0].setPressed(true);
                    widget.invalidate();
                    mLink = link[0];
                }
            } else {
                link[0].setPressed(true);
                widget.invalidate();
                mLink = link[0];
            }
        } else {
            if (mLink != null) {
                mLink.setPressed(false);
                widget.invalidate();
                mLink = null;
            }
        }
    }

    @Override
    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();

        if (action == MotionEvent.ACTION_UP ||
            action == MotionEvent.ACTION_DOWN ||
            action == MotionEvent.ACTION_MOVE) {
            
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            AuroraURLSpan[] link = buffer.getSpans(off, off, AuroraURLSpan.class);

            if (action == MotionEvent.ACTION_UP) {
                if (link.length > 0) {
                    link[0].setPressed(false);
                    widget.invalidate();
                    mLink = null;
                    link[0].onClick(widget);
                } else {
                    if (mLink != null) {
                        mLink.setPressed(false);
                        widget.invalidate();
                        mLink = null;
                    }
                    Selection.removeSelection(buffer);
                }
            } else if (action == MotionEvent.ACTION_DOWN) {
                if (link.length > 0) {
                    link[0].setPressed(true);
                    widget.invalidate();
                    mLink = link[0];
                     Selection.setSelection(buffer,
                             buffer.getSpanStart(link[0]),
                             buffer.getSpanEnd(link[0]));
                } else {
                    if (mLink != null) {
                        mLink.setPressed(false);
                        widget.invalidate();
                        mLink = null;
                    }
                    Selection.removeSelection(buffer);
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (link.length > 0) {
                    if (mLink != null) {
                        if (mLink != link[0]) {
                            mLink.setPressed(false);
                            link[0].setPressed(true);
                            widget.invalidate();
                            mLink = link[0];
                        }
                    } else {
                        link[0].setPressed(true);
                        widget.invalidate();
                        mLink = link[0];
                    }
                } else {
                    if (mLink != null) {
                        mLink.setPressed(false);
                        widget.invalidate();
                        mLink = null;
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            if (mLink != null) {
                mLink.setPressed(false);
                widget.invalidate();
                mLink = null;
            }
        }
        return AuroraTouch.onTouchEvent(widget, buffer, event);//super.onTouchEvent(widget, buffer, event);
    }
    
}

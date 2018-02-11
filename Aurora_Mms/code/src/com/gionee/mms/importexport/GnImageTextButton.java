package com.gionee.mms.importexport;

import com.android.mms.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GnImageTextButton extends LinearLayout {

    public GnImageTextButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public GnImageTextButton(Context context) {
        this(context, null);
    }
    
    public GnImageTextButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, R.attr.buttonStyle);

        TypedArray a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ImageView,
                    0, 0);
        Drawable mDrawable = a.getDrawable(com.android.internal.R.styleable.ImageView_src);
        a.recycle();
        
        a =
            context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TextView,
                    0, 0);
        CharSequence mText = a.getText(com.android.internal.R.styleable.TextView_text);
        a.recycle();

        inflate(context, R.layout.gn_imagetext_button_layout, this);
        final ImageView imageView = (ImageView)findViewById(R.id.gnDrawable); 
        if (imageView != null) {
            imageView.setImageDrawable(mDrawable);
        }
        final TextView textView = (TextView)findViewById(R.id.gnText);
        if (textView != null){
            textView.setText(mText);
        }
        // gionee zhouyj 2012-05-28 add for CR00607938 start
//        setBackgroundResource(R.drawable.gn_btn_press_common);
        // gionee zhouyj 2012-05-28 add for CR00607938 end
    }

    public void setImageResource(int resId) {
        final ImageView imageView = (ImageView)findViewById(R.id.gnDrawable); 
        if (imageView != null) {
            imageView.setImageResource(resId);
        }
    }

    public void setImageTextEnabled(boolean isEnable) {
        final TextView textView = (TextView)findViewById(R.id.gnText);
        if (textView != null){
            textView.setEnabled(isEnable);
            textView.setTextSize(12);
        }
    }

    public void setTextColor(int colorid) {
        final TextView textView = (TextView)findViewById(R.id.gnText);
        if (textView != null){
            textView.setTextColor(colorid);
        }
    }
}


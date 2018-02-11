package com.android.keyguard.view;

import java.util.Calendar;

import com.android.keyguard.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ColorView extends ImageView {

    private boolean mIsDate = false;

    public ColorView(Context context) {
        this(context, null);
    }
    
    public ColorView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }
    
    public ColorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mIsDate) {
            setImageResource(R.drawable.color_view);
            int date = getDate();
            setImageLevel(date);
        } else {
            setImageResource(R.color.default_colors);
        }
    }
    
    
    private int getDate(){
        int date = 1;
        Calendar c = Calendar.getInstance();
        date = c.get(Calendar.DAY_OF_MONTH);
        return date;
    }
}

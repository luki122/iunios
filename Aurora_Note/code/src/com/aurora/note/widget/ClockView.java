
package com.aurora.note.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.aurora.note.R;

public class ClockView extends LinearLayout {
    private final SparseArray<Drawable> mDigtalNumberMap = new SparseArray<Drawable>();

//    private ImageView mHourColon;
    private ImageView mHourFirst;
    private ImageView mHourSecond;
    private int mHourFirstValue;
    private int mHourSecondValue;
//    private boolean mIsMoreThanHour;

//    private ImageView mMillSecondFirst;
//    private int mMillSecondFirstValue;
//    private ImageView mMillSecondPoint;
//    private ImageView mMillSecondSecond;
//    private int mMillSecondSecondValue;

    private ImageView mMinuteFirst;
    private int mMinuteFirstValue;
    private ImageView mMinuteSecond;
    private int mMinuteSecondValue;

    private ImageView mSecondFirst;
    private int mSecondFirstValue;
    private ImageView mSecondSecond;
    private int mSecondSecondValue;

    public ClockView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDigtalNumberMap(context);
        init(context);
    }

    private void init(Context context) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.clock_view_layout, this, true);

        mHourSecond = ((ImageView) findViewById(R.id.hour_second));
        mHourFirst = ((ImageView) findViewById(R.id.hour_first));
        mMinuteSecond = ((ImageView) findViewById(R.id.minute_second));
        mMinuteFirst = ((ImageView) findViewById(R.id.minute_first));
        mSecondSecond = ((ImageView) findViewById(R.id.second_second));
        mSecondFirst = ((ImageView) findViewById(R.id.second_first));
//        mMillSecondSecond = ((ImageView) findViewById(R.id.mill_second));
//        mMillSecondFirst = ((ImageView) findViewById(R.id.mill_first));
//        mHourColon = ((ImageView) findViewById(R.id.hour_colon));
//        mMillSecondPoint = ((ImageView) findViewById(R.id.mill_second_point));
    }

    private void initDigtalNumberMap(Context context) {
        Resources res = context.getResources();
        Drawable digtalNumber = res.getDrawable(R.drawable.record_0);
        mDigtalNumberMap.put(0, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_1);
        mDigtalNumberMap.put(1, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_2);
        mDigtalNumberMap.put(2, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_3);
        mDigtalNumberMap.put(3, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_4);
        mDigtalNumberMap.put(4, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_5);
        mDigtalNumberMap.put(5, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_6);
        mDigtalNumberMap.put(6, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_7);
        mDigtalNumberMap.put(7, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_8);
        mDigtalNumberMap.put(8, digtalNumber);
        digtalNumber = res.getDrawable(R.drawable.record_9);
        mDigtalNumberMap.put(9, digtalNumber);
    }

    private int[] parseElapsedTime(long elapsedTime) {
        int[] result = new int[8];
        if (elapsedTime > 0L) {
            int i = (int) (elapsedTime % 1000L);
            result[7] = (i / 10 % 10);
            result[6] = (i / 100);
            
            i = (int) (elapsedTime / 1000L % 60L);
            result[5] = (i % 10);
            result[4] = (i / 10);
            
            i = (int) (elapsedTime / 60000L % 60L);
            result[3] = (i % 10);
            result[2] = (i / 10);
            
            i = (int) (elapsedTime / 3600000L);
            result[1] = (i % 10);
            result[0] = (i / 10);
        }
        return result;
    }

    public void reset() {
        timeChanged(0L);
    }

    public void timeChanged(long elapsed) {
        int[] elapsedParsed = parseElapsedTime(elapsed);

//        if (elapsedParsed[0] > 0 || elapsedParsed[1] > 0) {
//            mIsMoreThanHour = true;
//        } else {
//            mIsMoreThanHour = false;    
//        }

//        if (!mIsMoreThanHour) {
//            mMillSecondFirst.setVisibility(View.VISIBLE);
//            mMillSecondSecond.setVisibility(View.VISIBLE);
//            mMillSecondPoint.setVisibility(View.VISIBLE);
//            
//            if (mMillSecondFirstValue != elapsedParsed[7]) {
//                mMillSecondFirst.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[7]));
//                mMillSecondFirstValue = elapsedParsed[7];
//            }
//            if (mMillSecondSecondValue != elapsedParsed[6]) {
//                mMillSecondSecond.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[6]));
//                mMillSecondSecondValue = elapsedParsed[6];
//            }
//        } else {
//            mMillSecondFirst.setVisibility(View.GONE);
//            mMillSecondSecond.setVisibility(View.GONE);
//            mMillSecondPoint.setVisibility(View.GONE);
//        }

        if (mSecondFirstValue != elapsedParsed[5]) {
            mSecondFirst.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[5]));
            mSecondFirstValue = elapsedParsed[5];
        }

        if (mSecondSecondValue != elapsedParsed[4]) {
            mSecondSecond.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[4]));
            mSecondSecondValue = elapsedParsed[4];
        }

        if (mMinuteFirstValue != elapsedParsed[3]) {
            mMinuteFirst.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[3]));
            mMinuteFirstValue = elapsedParsed[3];
        }

        if (mMinuteSecondValue != elapsedParsed[2]) {
            mMinuteSecond.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[2]));
            mMinuteSecondValue = elapsedParsed[2];
        }

//        if (!mIsMoreThanHour) {
//            mHourFirst.setVisibility(View.GONE);
//            mHourColon.setVisibility(View.GONE);
//        } else {
//            mHourFirst.setVisibility(View.VISIBLE);
//            mHourSecond.setVisibility(View.VISIBLE);
//            mHourColon.setVisibility(View.VISIBLE);
            
            if (mHourFirstValue != elapsedParsed[1]) {
                mHourFirst.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[1]));
                mHourFirstValue = elapsedParsed[1];
            }
            
            if (mHourSecondValue != elapsedParsed[0]) {
                mHourSecond.setImageDrawable(mDigtalNumberMap.get(elapsedParsed[0]));
                mHourSecondValue = elapsedParsed[0];
            }
//        }
    }
}

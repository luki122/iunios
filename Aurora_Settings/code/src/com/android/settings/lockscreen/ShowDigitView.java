package com.android.settings.lockscreen;


import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.android.settings.R;

public class ShowDigitView extends LinearLayout {

    private final static int STATE_EMPTY = 1;
    private final static int STATE_FULL = 2;
    private final static int STATE_ERROR = 3;
    private final static int STATE_UNKONW = -1;

    private ImageView dot_img_1 = null;
    private ImageView dot_img_2 = null;
    private ImageView dot_img_3 = null;
    private ImageView dot_img_4 = null;

    public ShowDigitView(Context context) {
        this(context, null);
    }

    public ShowDigitView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public ShowDigitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        dot_img_1 = ( ImageView ) findViewById(R.id.digit_dot_1);
        dot_img_2 = ( ImageView ) findViewById(R.id.digit_dot_2);
        dot_img_3 = ( ImageView ) findViewById(R.id.digit_dot_3);
        dot_img_4 = ( ImageView ) findViewById(R.id.digit_dot_4);
        setGravity(Gravity.CENTER);
    }

    public void onTextChange(int count) {
        switch (count) {
            case 0:
                setImageSate(dot_img_1, STATE_EMPTY);
                setImageSate(dot_img_2, STATE_EMPTY);
                setImageSate(dot_img_3, STATE_EMPTY);
                setImageSate(dot_img_4, STATE_EMPTY);
                break;
            case 1:
                setImageSate(dot_img_1, STATE_FULL);
                setImageSate(dot_img_2, STATE_EMPTY);
                setImageSate(dot_img_3, STATE_EMPTY);
                setImageSate(dot_img_4, STATE_EMPTY);
                break;
            case 2:
                setImageSate(dot_img_1, STATE_FULL);
                setImageSate(dot_img_2, STATE_FULL);
                setImageSate(dot_img_3, STATE_EMPTY);
                setImageSate(dot_img_4, STATE_EMPTY);
                break;
            case 3:
                setImageSate(dot_img_1, STATE_FULL);
                setImageSate(dot_img_2, STATE_FULL);
                setImageSate(dot_img_3, STATE_FULL);
                setImageSate(dot_img_4, STATE_EMPTY);
                break;
            case 4:
                setImageSate(dot_img_1, STATE_FULL);
                setImageSate(dot_img_2, STATE_FULL);
                setImageSate(dot_img_3, STATE_FULL);
                setImageSate(dot_img_4, STATE_FULL);
                break;
            case 5:
                setImageSate(dot_img_1, STATE_ERROR);
                setImageSate(dot_img_2, STATE_ERROR);
                setImageSate(dot_img_3, STATE_ERROR);
                setImageSate(dot_img_4, STATE_ERROR);
                break;

            default:
                setImageSate(dot_img_1, STATE_EMPTY);
                setImageSate(dot_img_2, STATE_EMPTY);
                setImageSate(dot_img_3, STATE_EMPTY);
                setImageSate(dot_img_4, STATE_EMPTY);
                break;
        }
    }

    public void setDigitState(int count) {
        ImageView digitImageView = ( ImageView ) getChildAt(0);
        if (count > 0) {
            digitImageView = ( ImageView ) getChildAt(count - 1);
        }
        setImageSate(digitImageView, STATE_FULL);
    }

    public void setImageSate(ImageView img, int state) {
        switch (state) {
            case STATE_EMPTY:
                img.setImageResource(R.drawable.digit_password_dot_empty);
                break;

            case STATE_FULL:
                img.setImageResource(R.drawable.digit_password_dot_full);
                break;

            case STATE_ERROR:
                img.setImageResource(R.drawable.digit_password_dot_error);
                break;

            default:
                img.setImageResource(R.drawable.digit_password_dot_empty);
                break;
        }
    }

}

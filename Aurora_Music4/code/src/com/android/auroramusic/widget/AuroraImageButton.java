package com.android.auroramusic.widget;



import android.R.integer;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.music.R;



public class AuroraImageButton extends RelativeLayout{

	private static final String TAG = "AuroraImageButton";
	private Context mContext;
	private boolean mChecked;
	//private ImageButton mButton = null;
	private ImageView mButton = null;
	private TextView mTextView = null;
	
	
	
	public AuroraImageButton(Context context) {
		//this(context, null, 0);
		super(context);
		init(context);
	}

	public AuroraImageButton(Context context, AttributeSet attrs) {
		//this(context, attrs, 0);
		super(context, attrs);
		init(context);
	}

	public AuroraImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		init(context);
	}
	
	private void init(Context context) {
		
		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.aurora_imagebutton, this);
		mButton = (ImageView)findViewById(R.id.my_bt_img);
		mTextView = (TextView)findViewById(R.id.my_bt_text);
		
		this.setClickable(true);
		this.setFocusable(true);
		
		return;
	}

	public void setImgResource(int resId) {
		if (mButton != null) {
			mButton.setImageResource(resId);
		}
	}
	
	public void setText(int resid) {
		if (mTextView != null) {
			mTextView.setText(resid);
		}
    }
	
	public void setText(String text) {
		if (mTextView != null) {
			mTextView.setText(text);
		}
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	
}

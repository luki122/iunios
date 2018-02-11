package com.aurora.thememanager.widget;

import com.aurora.thememanager.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class DownloadButton extends FrameLayout{
	
	
	public static final int STATUS_NORMAL = 0;
	
	public static final int STATUS_DOWNLOADING = 1;
	
	public static final int STATUS_PAUSE = 2;
	
	public static final int STATE_UPDATE = 3;
	
	public static final int STATUS_DOWNLOAD_SUCCESS = 4;

	public static final int STATUS_WAIT_APPLAY = 5;

	public static final int STATUS_WAIT_DOWNLOAD = 6;

	public static final int STATUS_PROGRESSING_DOWNLOAD = 7;
	
	private int mCurrentState = STATUS_NORMAL;

	private OnStateChangeListener mStateListener;
	
	
	
	public interface OnStateChangeListener{
		
		public void onStateChange(int state);
		
	}
	
	
	public DownloadButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init(){
		setState(STATUS_NORMAL);
		changeStateView(mCurrentState);
	}
	
	public int getState(){
		return mCurrentState;
	}
	
	public void setState(int state){
	
		if(mCurrentState != state){
			mCurrentState = state;
			
			if(mStateListener != null){
				mStateListener.onStateChange(mCurrentState);
			}
			changeStateView(state);
		}
		
	}
	
	
	public void setOnStateChangeListener(OnStateChangeListener listener){
		this.mStateListener = listener;
	}
	
	public  void changeStateView(int state) {
		Log.d("state", "state:"+state);
		switch (state) {
		case STATUS_NORMAL:
			break;
		case STATUS_DOWNLOADING:
			break;
		case STATUS_PAUSE:
			break;
		case STATE_UPDATE:
			break;
		case STATUS_DOWNLOAD_SUCCESS:
			break;
		case STATUS_WAIT_APPLAY:
//			setText("应用");
			break;

		default:
			break;
		}
	}

	public void setProgress(int progress) {
		// TODO Auto-generated method stub
//		setText(progress + getText().toString());
	}
	
	
	public void setText(int resId){
		
	}
	public void setText(CharSequence text){
		
	}
	
	
	
}

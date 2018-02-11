package com.aurora.thememanager.widget;

import com.aurora.thememanager.R;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

public class DownloadHorizontalButton extends ImageButton{
	
	
	public static final int STATE_NORMAL = 0;
	
	public static final int STATE_DOWNLOADING = 1;
	
	public static final int STATE_PAUSE = 2;
	
	public static final int STATE_UPDATE = 3;
	
	public static final int STATE_DOWNLOAD_SUCCESS = 4;
	
	private int mCurrentState = STATE_NORMAL;

	private OnStateChangeListener mStateListener;
	
	public interface OnStateChangeListener{
		
		public void onStateChange(int state);
		
	}
	
	
	public DownloadHorizontalButton(Context context, AttributeSet attrs,
			int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadHorizontalButton(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadHorizontalButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}

	public DownloadHorizontalButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init();
	}

	private void init(){
		setState(STATE_NORMAL);
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
	
	private void changeStateView(int state) {
		Log.d("state", "state:"+state);
		switch (state) {
		case STATE_NORMAL:
			setImageResource(R.drawable.btn_download_normal);
			break;
		case STATE_DOWNLOADING:
			setImageResource(R.drawable.btn_download_downloading);
			break;
		case STATE_PAUSE:
			setImageResource(R.drawable.btn_download_pause);
			break;
		case STATE_UPDATE:
			setImageResource(R.drawable.btn_download_update);
			break;
		case STATE_DOWNLOAD_SUCCESS:
			setImageResource(R.drawable.btn_download_success);
			break;

		default:
			break;
		}
	}
	
	
	
	
	
	
	
}

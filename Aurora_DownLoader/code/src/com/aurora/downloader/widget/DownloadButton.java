package com.aurora.downloader.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.aurora.downloader.R;
import com.aurora.downloader.util.AuroraLog;
import com.aurora.downloader.util.DownloadStateUtil.Status;
import com.aurora.downloader.util.Util;

public class DownloadButton extends ImageView {

	private static final String TAG_LOG="DownloadButton";
	private static  String TAG = "";
	private Status mStatus;
	private DownloadButtonListener mListener;
	private long mResultCode;

	public interface DownloadButtonListener {

		public void onClick(DownloadButton view, Status status, long resultCode);
	}

	public DownloadButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	public DownloadButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public DownloadButton(Context context) {
		super(context);
		initialize();
	}

	private void initialize() {
		super.setOnClickListener(mOnClickListener);
	}

	public void setDownloadButtonListener(DownloadButtonListener listener,
			long resultCode) {
		mListener = listener;
		mResultCode = resultCode;
		TAG= TAG_LOG+"["+resultCode+"]";
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (Util.getSDPath(getContext()) == null) {// 储存器不可用
				Toast.makeText(getContext(), R.string.enable_sd_card,
						Toast.LENGTH_LONG).show();
				return;
			}
			switch (mStatus) {
			case RUNNING:
				setDownloadStatus(Status.PAUSED);
				break;
			case PAUSED:
				setDownloadStatus(Status.RUNNING);
				break;
			case WAITING:
				setDownloadStatus(Status.PAUSED);
				break;
			case FAILED:
				setDownloadStatus(Status.RUNNING);
				break;

			default:
				break;
			}
			if (mListener != null) {
				mListener.onClick(DownloadButton.this, mStatus, mResultCode);
			}
		}
	};

	public void setDownloadStatus(Status status) {
		AuroraLog.log(TAG, "setButtonStatus: " + status);
		if (mStatus == status) {
			return;
		}
		mStatus = status;
		switch (mStatus) {
		case WAITING: {
			setBackgroundResource(R.drawable.aurora_download_stop);
			setVisibility(View.VISIBLE);
			break;
		}
		case RUNNING: {
			setBackgroundResource(R.drawable.aurora_download_start);
			setVisibility(View.VISIBLE);
			break;
		}
		case PAUSED: {
			setBackgroundResource(R.drawable.aurora_download_stop);
			setVisibility(View.VISIBLE);
			break;
		}
		case FAILED: {
			setBackgroundResource(R.drawable.aurora_download_stop);
			setVisibility(View.VISIBLE);
			break;
		}
		case SUCESS: {
			setVisibility(View.INVISIBLE);
			break;
		}
		}
	}

}

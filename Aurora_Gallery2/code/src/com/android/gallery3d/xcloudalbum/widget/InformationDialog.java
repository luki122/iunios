package com.android.gallery3d.xcloudalbum.widget;

import com.android.gallery3d.xcloudalbum.widget.TextInputDialog.OnFinishListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import aurora.widget.AuroraTextView;

import com.android.gallery3d.R;

public class InformationDialog extends aurora.app.AuroraAlertDialog {
	private static final String TAG = "InformationDialog";
	private OnFinishListener mListener;
	private String mTitle;
	private String dialogMsg;
	private boolean isDir;

	public InformationDialog(Context context,String title,String msg,boolean isDir,OnFinishListener mListener) {
		super(context);
		this.mTitle = title;
		this.dialogMsg = msg;
		this.mListener = mListener;
		this.isDir =isDir;
	}
	
	public InformationDialog(Context context,int titleId,String msg,boolean isDir,OnFinishListener mListener) {
		super(context);
		this.mTitle = context.getString(titleId);
		this.dialogMsg = msg;
		this.mListener = mListener;
		this.isDir = isDir;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View mView = LayoutInflater.from(getContext()).inflate(
				R.layout.information_dialog, null);
		AuroraTextView	dialogText =(AuroraTextView)mView.findViewById(R.id.dialog_text);
		dialogText.setText(dialogMsg);
		setTitle(mTitle);
		setCanceledOnTouchOutside(false);
		setView(mView);
		setButton(BUTTON_POSITIVE, getContext().getString(android.R.string.ok),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						if (which == BUTTON_POSITIVE) {

							if (mListener.onFinish(isDir, null,false)) {
								dismiss();
							}
						}
					}
				});
		setButton(BUTTON_NEGATIVE,
				getContext().getString(android.R.string.cancel),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (which == BUTTON_NEGATIVE) {
							mListener.onFinish(isDir, null,true);
							dialog.dismiss();
						}
					}
				});
		super.onCreate(savedInstanceState);
	}

}

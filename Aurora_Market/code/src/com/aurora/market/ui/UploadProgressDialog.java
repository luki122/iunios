package com.aurora.market.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import com.aurora.market.R;



public class UploadProgressDialog extends Dialog {
	private Context context;
	private static UploadProgressDialog dialog;
	
	public UploadProgressDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	public UploadProgressDialog(Context context, int theme){
		super(context, theme);
	}
	
	public static UploadProgressDialog createDialog(Context context){
		dialog = new UploadProgressDialog(context,R.style.upload_progressDialog);
		dialog.setContentView(R.layout.upload_progressdialog_layout);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		dialog.getWindow().getAttributes().dimAmount = 0;
		return dialog;
	}
	

	
	public void onWindowFocusChanged(boolean hasFocus){
		if(dialog == null){
			return;
		}
		
		ImageView imageView = (ImageView)dialog.findViewById(R.id.loadingImageView);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
		ad.start();
	}
	
	
	public UploadProgressDialog setMessage(String strMessage){
		TextView tvMsg = (TextView)dialog.findViewById(R.id.loadingTextView);
		if(tvMsg != null){
			tvMsg.setText(strMessage);
		}
		return dialog;
	}
}

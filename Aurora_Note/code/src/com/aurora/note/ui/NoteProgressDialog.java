package com.aurora.note.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.aurora.note.R;

public class NoteProgressDialog extends Dialog {

	private NoteProgressDialog(Context context) {
		super(context);
	}

	private NoteProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public static NoteProgressDialog createDialog(Context context) {
        return createDialog(context, true);
    }

	public static NoteProgressDialog createDialog(Context context, boolean isTransparent){
	    NoteProgressDialog dialog = new NoteProgressDialog(context, R.style.upload_progressDialog);
		dialog.setContentView(R.layout.note_progress_dialog_layout);
		dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
		if (isTransparent) {
		    dialog.getWindow().getAttributes().dimAmount = 0;
		}
		return dialog;
	}

	public void onWindowFocusChanged(boolean hasFocus) {		
		/*ImageView imageView = (ImageView) findViewById(R.id.loadingImageView);
		AnimationDrawable ad = (AnimationDrawable)imageView.getBackground();
		ad.start();*/
	}

	public NoteProgressDialog setMessage(String strMessage){
		TextView tvMsg = (TextView) findViewById(R.id.loadingTextView);
		if(tvMsg != null){
		    if (tvMsg.getVisibility() != View.VISIBLE) {
		        tvMsg.setVisibility(View.VISIBLE);
		    }
			tvMsg.setText(strMessage);
		}
		
		return this;
	}

}

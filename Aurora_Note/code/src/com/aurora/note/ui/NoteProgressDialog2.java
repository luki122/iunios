package com.aurora.note.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import aurora.widget.AuroraLoadingView;

import com.aurora.note.R;

public class NoteProgressDialog2 extends Dialog {

	private NoteProgressDialog2(Context context) {
		super(context);
	}

	private NoteProgressDialog2(Context context, int theme) {
		super(context, theme);
	}

	public static NoteProgressDialog2 createDialog(Context context) {
        return createDialog(context, true);
    }

	public static NoteProgressDialog2 createDialog(Context context, boolean isTransparent) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.note_progress_dialog_layout_2, null);
        AuroraLoadingView loadingView = (AuroraLoadingView) contentView.findViewById(R.id.loading_view);
        loadingView.show();

        NoteProgressDialog2 dialog = new NoteProgressDialog2(context, R.style.upload_progressDialog);
        dialog.setContentView(contentView);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        if (isTransparent) {
            dialog.getWindow().getAttributes().dimAmount = 0;
        }

		return dialog;
	}

	public NoteProgressDialog2 setMessage(String message){
		TextView loadingText = (TextView) findViewById(R.id.loading_textview);
		if(loadingText != null){
		    if (loadingText.getVisibility() != View.VISIBLE) {
		    	loadingText.setVisibility(View.VISIBLE);
		    }
		    loadingText.setText(message);
		}

		return this;
	}

}
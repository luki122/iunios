/**
 * Vulcan created this file in 2014年10月10日 下午5:26:26 .
 */
package com.privacymanage.activity;


import com.privacymanage.model.AccountModel.EmailSendingCallback;
import com.privacymanage.utils.StringUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import aurora.widget.AuroraEditText;
import aurora.widget.AuroraTextView;

/**
 * Vulcan created GetbackPassword in 2014年10月10日 .
 * 
 */
public class GetbackPassword extends FounderPage{
	
	private ProgressDialog pdSendingEmail = null;

	/**
	 * 
	 */
	public GetbackPassword() {
		//this.mLayoutResId = ResIdMan.LAYOUT_FILE_GETBACK_PASSWORD;
	}
	
	private void setErrorInfoText(int resid) {
		AuroraTextView atvErrorMsg = (AuroraTextView)findViewById(ResIdMan.TEXTVIEW_ERROR_MSG);
		atvErrorMsg.setText(resid);
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月11日 上午11:26:22 .
	 * @param s
	 */
	@SuppressWarnings("unused")
	private void setDebugInfoText(String s) {
		AuroraTextView atvErrorMsg = (AuroraTextView)findViewById(ResIdMan.TEXTVIEW_DEBUG_MSG);
		atvErrorMsg.setText(s);
	}
	
	/* (non-Javadoc)
	 * @see aurora.app.AuroraActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setAuroraContentView(ResIdMan.LAYOUT_FILE_GETBACK_PASSWORD);
		this.getAuroraActionBar().setTitle(ResIdMan.STRING_GET_BACK_PASSWORD);
		
		getAuroraActionBar().addItem(ResIdMan.LAYOUT_FILE_NEXT_STEP,0);
		
		if (!UnderlyingWrapper.networkIsAvailable()) {
			Toast toast = Toast.makeText(this, ResIdMan.STRING_NET_BAD, Toast.LENGTH_LONG);
			toast.show();
		}
		
		final AuroraEditText aetEmail = (AuroraEditText) findViewById(ResIdMan.EDITTEXT_EMAIL_ADDR);
		aetEmail.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				setErrorInfoText(ResIdMan.STRING_NULL);
				aetEmail.setSelected(false);
			}
		});
		
		TextView textViewNextStep = (TextView) findViewById(ResIdMan.TEXTVIEW_NEXT_STEP);
		textViewNextStep.setText(ResIdMan.STRING_CONFIRM);
		textViewNextStep.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String email = aetEmail.getText().toString();
				if(StringUtils.emailIsInvalid(email)) {
					setErrorInfoText(ResIdMan.STRING_EMAIL_ADDRESS_NOT_COMPLETE);
					aetEmail.setSelected(true);
					return;
				}
				else if(!UnderlyingWrapper.emailIsRegistered(email)) {
					setErrorInfoText(ResIdMan.STRING_EMAIL_ADDRESS_NOT_EXIST);
					aetEmail.setSelected(true);
					return;
				}
				else if(!UnderlyingWrapper.networkIsAvailable()) {
					setErrorInfoText(ResIdMan.STRING_NET_BAD);
					aetEmail.setSelected(true);
					return;
				}
				
				showDialogSendProgress(GetbackPassword.this);
				
				setErrorInfoText(ResIdMan.STRING_NULL);
				
				UnderlyingWrapper.sendGetbackPasswordEmail(email,new EmailSendingCallback() {
					@Override
					public void onNotifyEmailSendingResult(final int error) {
						
						
						GetbackPassword.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								GetbackPassword.this.onNotifyEmailSendingResult(error);
							}
							
						});

					}
				});
				

				
			}
			
		});
		
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 下午2:10:27 .
	 */
	protected void showDialogSendProgress(Context context) {
		pdSendingEmail = new ProgressDialog(context);
		//View view = getLayoutInflater().inflate(ResIdMan.LAYOUT_FILE_EMAIL_SEND_PROGRESS, null);
		pdSendingEmail.requestWindowFeature(Window.FEATURE_NO_TITLE);
		pdSendingEmail.setMessage(context.getString(ResIdMan.STRING_WAIT_SENDING));
		pdSendingEmail.setCanceledOnTouchOutside(false);
		pdSendingEmail.setCancelable(false);
		pdSendingEmail.show();
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月14日 下午6:17:59 .
	 * @param context
	 */
	protected void hideDialogSendProgress(Context context) {
		pdSendingEmail.cancel();
		pdSendingEmail = null;
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月14日 下午6:23:56 .
	 * @param error
	 */
	protected void onNotifyEmailSendingResult(int error) {
		Log.d("vprivacy","onNotifyEmailSendingResult = " + error);
		
		//anyway, cancel the dialog
		hideDialogSendProgress(GetbackPassword.this);
		
		if(error == EmailSendingCallback.SUCCESS) {
			Toast toast = Toast.makeText(this, ResIdMan.STRING_INFO_SEND_SUCCESS, Toast.LENGTH_LONG);
			toast.show();
			finish();
		}
		else {
			Toast toast = Toast.makeText(this, ResIdMan.STRING_INFO_SEND_FAIL, Toast.LENGTH_LONG);
			toast.show();
		}
		return;
	}
	
}

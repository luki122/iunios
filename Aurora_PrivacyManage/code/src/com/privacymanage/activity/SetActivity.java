package com.privacymanage.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraCheckBox;
import aurora.app.AuroraActivity;
import aurora.app.AuroraAlertDialog;
import com.aurora.privacymanage.R;
import com.privacymanage.data.AccountData;
import com.privacymanage.interfaces.AccountObserver;
import com.privacymanage.model.AccountModel;

public class SetActivity extends AuroraActivity implements OnClickListener,
                                                           AccountObserver{
    private TextView EmailText;
    private boolean deletePrivacyContent =false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.activity_set,AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.set);
        initView();  
        AccountModel.getInstance().attach(this);
    }
    
    private void initView(){
    	findViewById(R.id.passwordProtectLayout).setOnClickListener(this);
    	findViewById(R.id.ChangePasswordLayout).setOnClickListener(this);
    	findViewById(R.id.callMsgLayout).setOnClickListener(this); 	
    	findViewById(R.id.deletePrivacySpaceBtn).setOnClickListener(this); 	
    	EmailText = (TextView)findViewById(R.id.EmailText);
    }
    
	@Override
	protected void onResume() {
		updateEmailText();
		super.onResume();
	}
     
    private void updateEmailText(){
    	if(EmailText != null){
    		EmailText.setText(""+AccountModel.getInstance().getCurAccount().getEMail());
    	}   	
    }

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch(v.getId()){
		case R.id.passwordProtectLayout:
			startActivityChangeEmail();
			break;
		case R.id.ChangePasswordLayout:
			startActivityChangePassword();
			break;
		case R.id.callMsgLayout:
			intent = new Intent(this, PrivacyRingMsgActivity.class);
			startActivity(intent);
			break;
		case R.id.deletePrivacySpaceBtn:		
			deletePrivacySpace();
			break;
		}		
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 上午9:52:35 .
	 */
	protected void startActivityChangeEmail() {
		Intent intentChangeEmail = new Intent(this, ChangeEmail.class);
		startActivity(intentChangeEmail);
		return;
	}
	
	/**
	 * 
	 * Vulcan created this method in 2014年10月13日 上午10:57:48 .
	 */
	protected void startActivityChangePassword() {
		Intent intentChangePassword = new Intent(this, ChangePassword.class);
		startActivity(intentChangePassword);
		return;
	}
	
	/**
	 * 删除隐私空间
	 */
	private void deletePrivacySpace(){
		deletePrivacyContent =false;
		View layout = getLayoutInflater().inflate(R.layout.dialog_content_of_delete_privacy_space,null);	
		AuroraCheckBox checkBox = (AuroraCheckBox)layout.findViewById(R.id.checkBox);
		final TextView hintText = (TextView)layout.findViewById(R.id.hintText);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				@Override
				public void onCheckedChanged(
						CompoundButton buttonView, boolean isChecked) {
					deletePrivacyContent = isChecked;
					if(isChecked){
						hintText.setVisibility(View.GONE);
					}else{
						hintText.setVisibility(View.VISIBLE);
					}
				}});
		checkBox.setChecked(true);
		
		AuroraAlertDialog.Builder builder = new AuroraAlertDialog.Builder(this);
		builder.setView(layout);		
		builder.setTitle(R.string.delete_privacy_space);
		builder.setMessage(R.string.sure_delete_privacy_space);		
		builder.setPositiveButton(R.string.sure,new DialogInterface.OnClickListener() {
	          public void onClick(DialogInterface dialog, int which) {
	        	  AccountModel.getInstance().deleteAccount(deletePrivacyContent);
	        	  startActivity(new Intent(SetActivity.this,PageWelcome.class));
	            }});
		builder.setNegativeButton(R.string.cancel,null);		
		try{
			builder.create().show();
		}catch(Exception e){
			e.printStackTrace();
		} 
	}

	@Override
	protected void onDestroy() {
		AccountModel.getInstance().detach(this);
		super.onDestroy();
	}

	@Override
	public void switchAccount(AccountData accountData) {
		if(accountData != null && 
				accountData.getAccountId() == AccountData.NOM_ACCOUNT){
			finish();
		}		
	}

	@Override
	public void deleteAccount(AccountData accountData, boolean delete) {
		finish();	
	}

}

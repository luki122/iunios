package com.privacymanage.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import aurora.widget.AuroraActionBar;
import aurora.widget.AuroraSwitch;
import aurora.app.AuroraActivity;
import com.aurora.privacymanage.R;
import com.privacymanage.data.AccountConfigData;
import com.privacymanage.data.AccountData;
import com.privacymanage.interfaces.AccountObserver;
import com.privacymanage.model.AccountModel;
import com.privacymanage.provider.ConfigProvider;
import com.privacymanage.view.InfoDialog;
import com.privacymanage.view.InfoDialog.EditDialogPosiBtnCallBack;

public class PrivacyRingMsgActivity extends AuroraActivity implements OnClickListener,
                                                               OnCheckedChangeListener,
                                                               AccountObserver{
    private TextView notifyText;
    private AuroraSwitch notifySwitch;
    private AccountConfigData accountConfigData;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAuroraContentView(R.layout.activity_privacy_ring_msg,AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.privacy_call_msg);
        accountConfigData = AccountModel.getInstance().getAccountConfigData();
        initView();  
        AccountModel.getInstance().attach(this);
    }
    
    private void initView(){      	
    	findViewById(R.id.privacyRingLayout).setOnClickListener(this);
    	findViewById(R.id.msgNotifyWordLayout).setOnClickListener(this);
    	notifyText = (TextView)findViewById(R.id.notifyText);
    	updateNotifyText();
    	
    	notifySwitch = (AuroraSwitch)findViewById(R.id.notifySwitch);
    	notifySwitch.setOnCheckedChangeListener(this);
    	updateNotifySwitch();
    }
    
    private void updateNotifyText(){
    	if(notifyText != null && accountConfigData != null){
    		notifyText.setText(""+accountConfigData.getMsgNotifyHintStr());
    	}   	
    }
    
    private void updateNotifySwitch(){
    	if(notifySwitch != null && accountConfigData != null){
    		notifySwitch.setChecked(accountConfigData.getMsgNotifySwitch());    	
    	}   	
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.privacyRingLayout:
			try{
				Intent intent = new Intent();
				intent.setComponent(new ComponentName("com.android.contacts", 
						"com.android.contacts.activities.AuroraPrivateSettingListActivity"));		
				startActivity(intent);
			}catch(Exception e){
				Toast.makeText(this, R.string.this_function_is_unavailable, Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.msgNotifyWordLayout:
			String hintText="";
			if(accountConfigData != null){
				hintText = accountConfigData.getMsgNotifyHintStr();
			}
			InfoDialog.showCustomEditDialog(
					this,
					R.string.notify_word,
					hintText,
					R.string.sure,
					new EditDialogPosiBtnCallBack(){
						@Override
						public void onPositiBtn(String editStr) {
							if(editStr == null || editStr.length() == 0){
								Toast.makeText(PrivacyRingMsgActivity.this, 
										R.string.must_enter_word, 
										Toast.LENGTH_SHORT).show();
							}else if(editStr.length() > 20){
								Toast.makeText(PrivacyRingMsgActivity.this, 
										R.string.can_not_export_20_word, 
										Toast.LENGTH_SHORT).show();
							}else{
								if(accountConfigData != null){
									accountConfigData.setMsgNotifyHintStr(editStr);
								}								
								updateNotifyText();
								saveConfigData();								
							}
						}
					},
					R.string.cancel,
					20
			    );
			break;
		}		
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(accountConfigData == null){
			return ;
		}
		if(isChecked != accountConfigData.getMsgNotifySwitch()){
			accountConfigData.setMsgNotifySwitch(isChecked);
			saveConfigData();	
		}	
	}
	
	private void saveConfigData(){
		ConfigProvider.insertOrUpdateDate(this, accountConfigData,
				AccountModel.getInstance().getCurAccount().getAccountId());
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
		// TODO Auto-generated method stub
		finish();
	}
}

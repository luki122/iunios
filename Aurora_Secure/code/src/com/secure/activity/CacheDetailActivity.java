package com.secure.activity;

import java.util.List;

import com.aurora.secure.R;
import com.privacymanage.data.AidlAccountData;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.Constants;
import com.secure.data.PrivacyAppData;
import com.secure.interfaces.Observer;
import com.secure.interfaces.PrivacyAppObserver;
import com.secure.interfaces.PrivacyAppSubject;
import com.secure.interfaces.Subject;
import com.secure.model.AuroraPrivacyManageModel;
import com.secure.model.ConfigModel;
import com.secure.totalCount.TotalCount;
import com.secure.utils.StringUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.view.AppDetailInfoView;
import com.secure.view.AppSizeView;
import com.secure.view.InfoDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class CacheDetailActivity extends AuroraActivity implements OnClickListener,
                                                                   Observer,
                                                                   PrivacyAppObserver{	
	private final int REQUEST_CODE_OF_UNINSTALL =1;
	private final int REQUEST_CODE_OF_APP_DETAIL = 2;
    
	private AppInfo curAppInfo = null;
	private AppSizeView appSizeView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(mConfig.isNative){
        	setContentView(R.layout.cache_detail_activity);
        }else{
        	setAuroraContentView(R.layout.cache_detail_activity,
            		AuroraActionBar.Type.Normal);
            getAuroraActionBar().setTitle(R.string.cache_detail);
        }
        receiveData();
        if(curAppInfo == null || !curAppInfo.getIsInstalled()){
        	InfoDialog.showToast(this, getString(R.string.app_not_install));
        	finish();
        	return ;
        }
        ConfigModel.getInstance(this).getAppInfoModel().attach(this);
        AuroraPrivacyManageModel.getInstance(this).attach(this);
        initView();          
    }

	private void receiveData() {
		if(getIntent() != null && getIntent().getData() != null){
			String packageName = getIntent().getData().getSchemeSpecificPart();
			curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().findAppInfo(packageName);
		}	
	} 
    
    private void initView(){
    	findViewById(R.id.appDetailButton).setOnClickListener(this);
    	Button uninstallBtn = (Button)findViewById(R.id.uninstallBtn);
    	if(curAppInfo.getIsUserApp()){
    		uninstallBtn.setEnabled(true);
    		uninstallBtn.setOnClickListener(this);	
    	}else{
    		uninstallBtn.setEnabled(false);
    	}
    	
    	((AppDetailInfoView)findViewById(R.id.appDetailInfoLayout)).setCurAppInfo(curAppInfo);
    	  	
    	appSizeView = (AppSizeView)findViewById(R.id.appSizeView);
    	appSizeView.setCurAppInfo(curAppInfo.getPackageName());
    	appSizeView.justShowCacheLayout();
    }
 
    
	@Override
	public void onClick(View v) {
		Intent intent;
		switch(v.getId()){
		case R.id.appDetailButton:
			new TotalCount(this, "23", 1).CountData();
			intent = new Intent(this,AppDetailActivity.class);
			intent.setData(Uri.fromParts(Constants.SCHEME, curAppInfo.getPackageName(), null));
			startActivityForResult(intent, REQUEST_CODE_OF_APP_DETAIL);
			break;
		case R.id.uninstallBtn:
			new TotalCount(this, "24", 1).CountData();
			intent = new Intent();
			intent.setAction("android.intent.action.DELETE");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setData(Uri.fromParts(Constants.SCHEME, curAppInfo.getPackageName(), null));
			startActivityForResult(intent, REQUEST_CODE_OF_UNINSTALL);
			break;
		}		
	}

	@Override
	protected void onDestroy() {
		AuroraPrivacyManageModel.getInstance(this).detach(this);
		ConfigModel.getInstance(this).getAppInfoModel().detach(this);
		releaseObject();
		super.onDestroy();
	}
	
    /**
     * 释放不需要用的对象所占用的堆内存
     */
	private void releaseObject(){
	} 
     
     @Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		switch(requestCode){
 		case REQUEST_CODE_OF_APP_DETAIL:
 			if(curAppInfo != null){
 				if(!ApkUtils.isAppInstalled(this, curAppInfo.getPackageName())){
 					finish();
 				}else{
 					if(appSizeView != null){
 						appSizeView.updatePacakgeSize();
 					}					
 				}
 			}
 			break;
 		}
 	}

	public void updateOfInit(Subject subject) {
		// TODO Auto-generated method stub	
	}

	public void updateOfInStall(Subject subject, String pkgName) {
		// TODO Auto-generated method stub		
	}

	public void updateOfCoverInStall(Subject subject, String pkgName) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			if(appSizeView != null){
				appSizeView.updatePacakgeSize();
			}
		}		
	}

	public void updateOfUnInstall(Subject subject, String pkgName) {
		if(pkgName != null && 
				curAppInfo != null && 
				pkgName.equals(curAppInfo.getPackageName())){
			finish();
		}		
	}

	public void updateOfRecomPermsChange(Subject subject) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void updateOfExternalAppAvailable(Subject subject,List<String> pkgList) {
		if(curAppInfo == null || pkgList == null){
    		return ;
    	}			
		for(int i=0;i<pkgList.size();i++){
			if(pkgList.get(i).equals(curAppInfo.getPackageName())){
				curAppInfo = ConfigModel.getInstance(this).getAppInfoModel().
						findAppInfo(curAppInfo.getPackageName(),curAppInfo.getIsUserApp());
				break;
			}
		}		
	}
	
	@Override
	public void updateOfExternalAppUnAvailable(Subject subject,List<String> pkgList) {
		// TODO Auto-generated method stub					
	}

	@Override
	public void updateOfPrivacyAppInit(PrivacyAppSubject subject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppAdd(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAppDelete(PrivacyAppSubject subject,
			List<PrivacyAppData> PrivacyAppList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateOfPrivacyAccountSwitch(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}			
	}
	
	@Override
	public void updateOfDeletePrivacyAccount(PrivacyAppSubject subject,
			AidlAccountData accountData) {
		if(curAppInfo != null && 
			null == ConfigModel.getInstance(this).getAppInfoModel().
			   findAppInfo(curAppInfo.getPackageName())){
			finish();
		}					
	}
}

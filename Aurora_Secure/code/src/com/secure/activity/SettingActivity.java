package com.secure.activity;

import java.lang.reflect.Method;
import com.aurora.secure.R;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.AutoStartData;
import com.secure.data.MainActivityItemData;
import com.secure.interfaces.LBEServiceBindSubject;
import com.secure.interfaces.PermissionObserver;
import com.secure.interfaces.Subject;
import com.secure.model.ConfigModel;
import com.secure.model.LBEmodel;
import com.secure.model.LBEmodel.BindServiceCallback;
import com.secure.utils.ActivityUtils;
import com.secure.utils.DisableChanger;
import com.secure.utils.StringUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.utils.ActivityUtils.LoadCallback;
import com.secure.view.AppDetailInfoView;
import com.secure.view.AppSizeView;
import com.secure.view.InfoDialog;
import com.secure.view.MyProgressDialog;

import aurora.widget.AuroraSwitch;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class SettingActivity extends AuroraActivity implements OnCheckedChangeListener{	
	private AuroraSwitch monitorSwitch;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);      
        setAuroraContentView(R.layout.set_activity,
        		AuroraActionBar.Type.Normal);
        getAuroraActionBar().setTitle(R.string.setting);
        
        monitorSwitch = (AuroraSwitch)findViewById(R.id.monitorSwitch);
        monitorSwitch.setOnCheckedChangeListener(this);
        updateSwitchState();
    }
    
	@Override
	protected void onRestart() {
		super.onRestart();
	}
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		final ApplicationInfo applicationInfo = ApkUtils.getApplicationInfo(this,
				mConfig.LBE_PERMISSION_MANAGE_PKG);
		if(isChecked){
			openOrCloseLBEService(true);
		}else{
			InfoDialog.showDialog(this, 
					R.string.permission_monitor_disable_dlg_title,
					android.R.attr.alertDialogIcon,
					R.string.permission_monitor_disable_dlg_text,
					R.string.sure,
					new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                    	openOrCloseLBEService(false);
	                    }
	                }, 
	                R.string.cancel, 
	                new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int which) {
	                    	if(monitorSwitch != null){
								monitorSwitch.setChecked(true);
							}	
	                    }
	                },
					null,
					new OnCancelListener(){
						@Override
						public void onCancel(DialogInterface dialog) {
							if(monitorSwitch != null){
								monitorSwitch.setChecked(true);
							}						
					}});
		}
	}
	
	/**
	 * 打开或关闭LBE服务
	 * @param isOpen
	 */
	private void openOrCloseLBEService(final boolean isOpen){
		if(isOpen == ApkUtils.isLBEServiceConnect()){
			return ;
		}
		
		if(LBEmodel.getInstance(SettingActivity.this).isBindLBEService()){		
			if(isOpen){
				LBEmodel.getInstance(SettingActivity.this).openLBEFunc();				
			}else{
				LBEmodel.getInstance(SettingActivity.this).closeLBEFunc();
			}
			updateAllAppsPermission(isOpen);
		}else{
			LBEmodel.getInstance(SettingActivity.this).bindLBEService(new BindServiceCallback(){
				@Override
				public void callback(boolean result) {
					if(isOpen){
						LBEmodel.getInstance(SettingActivity.this).openLBEFunc();
					}else{
						LBEmodel.getInstance(SettingActivity.this).closeLBEFunc();
					}
					updateAllAppsPermission(isOpen);
				}}
			);
		}
	}
	
	private void updateAllAppsPermission(final boolean isOpen){
		final MyProgressDialog dialog = new MyProgressDialog();
		if(isOpen){
			dialog.show(this, 
					getResources().getString(R.string.open_permission_monitor),
					getResources().getString(R.string.wait_for_open_permission_monitor));
		}
	
	    final Handler handler = new Handler() {
		   @Override
		   public void handleMessage(Message msg) {
			   if(dialog != null){
				   dialog.close();
			   }		   
		   }
		};
		
		new Thread() {
			@Override
			public void run() {
				if(isOpen){
					//开启权限监控，需要重新加载所有应用的权限，比较耗时，所以弹等待对话框
					ApkUtils.updateAllAppsPermission(SettingActivity.this);
					handler.sendEmptyMessage(0);
				}			
				if(LBEServiceBindSubject.getInstanceOfNotCreate() != null){
					LBEServiceBindSubject.getInstanceOfNotCreate().
					   notifyObserversOfBindStateChangeFunc(isOpen);
				}
			}
		}.start();
	}
		
	private void updateSwitchState(){
		if(ApkUtils.isLBEServiceConnect()){
			monitorSwitch.setChecked(true); 
		}else{
			monitorSwitch.setChecked(false);
		}
	}	
}

package com.secure.receive;

import java.util.ArrayList;

import com.lbe.security.service.sdkhelper.SDKConstants;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.data.PermissionInfo;
import com.secure.interfaces.PermissionSubject;
import com.secure.model.ConfigModel;
import com.secure.model.LBEmodel;
import com.secure.provider.AppInfosProvider;
import com.secure.provider.PermissionProvider;
import com.secure.utils.ApkUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PermissionChangeReciver extends BroadcastReceiver {

	public static final String PERMISSION_CHANGE_ACTION = "com.lbe.security.action_package_permission_change";
	
	private static final String PACKAGE_NAME_KEY = "com.lbe.security.extra_package_name";
	
	private static final String PERMISSION_ID_KEY = "com.lbe.security.extra_permission_id";
	
	private static final String PERMISSION_STATE_KEY = "com.lbe.security.extra_permission_action";
	
	public static final int ACTION_ACCEPT = 3;
	public static final int ACTION_PROMPT = 2;
	public static final int ACTION_REJECT = 1;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		String packageName = arg1.getStringExtra(PACKAGE_NAME_KEY);
		int permissionId = arg1.getIntExtra(PERMISSION_ID_KEY, -1);
		int permissionState = arg1.getIntExtra(PERMISSION_STATE_KEY, -1);
		Log.e("jadon3", "PERMISSION_CHANGE_ACTION  packageName = "
				+ packageName + "   state = " + permissionState + "    id = "
				+ permissionId);
		try {
			AppInfo appInfo = ConfigModel.getInstance(arg0).getAppInfoModel()
					.getThirdPartyAppsInfo().getAppInfoByName(packageName);
			if (appInfo == null) {
				return;
			}

			PermissionInfo info = new PermissionInfo();
			info.permId = permissionId;

			switch (permissionState) {
			case ACTION_ACCEPT:
				info.setCurState(SDKConstants.ACTION_ACCEPT);
				ApkUtils.openApkAppointPermission(arg0, appInfo, info);
				break;
			case ACTION_PROMPT:
				info.setCurState(SDKConstants.ACTION_PROMPT);
				ApkUtils.promptApkAppointPermission(arg0, appInfo, info);
				break;
			case ACTION_REJECT:
				info.setCurState(SDKConstants.ACTION_REJECT);
				ApkUtils.closeApkAppointPermission(arg0, appInfo, info);
				break;
			}
			updateAppPermissionState(appInfo, info);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void updateAppPermissionState(AppInfo appInfo,PermissionInfo permissionInfo){
		
		ArrayList<PermissionInfo> infos = appInfo.getPermission();
		for(int i = 0;i < infos.size();i++)
		{
			if(infos.get(i).permId == permissionInfo.permId)
			{
				infos.get(i).setCurState(permissionInfo.getCurState());
			}
		}
	}

}

package com.secure.view;

import com.aurora.secure.R;
import com.secure.data.AppInfo;
import com.secure.data.AppsInfo;
import com.secure.model.ConfigModel;
import com.secure.utils.StringUtils;
import com.secure.utils.TimeUtils;
import com.secure.utils.ApkUtils;
import com.secure.utils.Utils;
import com.secure.utils.mConfig;
import com.secure.view.InfoDialog;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDataObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import aurora.app.AuroraActivity;
import aurora.widget.AuroraActionBar;

public class AppDetailInfoView extends FrameLayout{	
	
	private AppInfo curAppInfo = null;
	
	public AppDetailInfoView(Context context) {
		super(context); 
		initView();
	}
	
	public AppDetailInfoView(Context context, AttributeSet attrs) {
	    super(context, attrs); 
	    initView();
	}
	
	private void initView(){    	 
	    LayoutInflater inflater = LayoutInflater.from(getContext());
	    inflater.inflate(R.layout.app_info_layout, this, true);	    	
    }
    
    /**
     * 设置当前的apk信息
     * @param curAppInfo
     */
    public void setCurAppInfo(AppInfo curAppInfo){
    	this.curAppInfo = curAppInfo;
    	if(curAppInfo == null){
    		throw new IllegalArgumentException("ERROR_OF_curAppInfo_NULL_IN_AppSizeView");
    	}
    	updateView(); 	
    }
    
    private void updateView(){    	 
    	ImageView appIcon = (ImageView)findViewById(R.id.appIcon);
    	TextView appName = (TextView)findViewById(R.id.appName);
    	TextView version = (TextView)findViewById(R.id.version);    	
    	appIcon.setImageDrawable(ApkUtils.getApkIcon(getContext(),curAppInfo.getPackageName()));
    	appName.setText(ApkUtils.getApkName(getContext(),curAppInfo.getApplicationInfo()));
    	
    	String versionStr = ApkUtils.getApkVersion(getContext(), curAppInfo.getPackageName());
    	if(versionStr == null || versionStr.equals("") || versionStr.equals("null")){
    		versionStr = "1.0";
		}
    	version.setText(getResources().getString(R.string.version)+
    			getResources().getString(R.string.colon)+
    			versionStr);
    }
    
}

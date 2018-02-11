package com.aurora.thememanager.utils;

import com.aurora.utils.Utils2Icon;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aurora.thememanager.R;
public class IconUtils {
	
	private Context mContext;
	
	private Utils2Icon mUtils;
	
	private LayoutInflater mInflater;
	
	private boolean mSmall = false;
	
	public IconUtils(Context context){
		this(context,false);
	}
	public IconUtils(Context context,boolean samllIcon){
		mContext = context;
		mUtils = Utils2Icon.getInstance(mContext);
		mInflater = LayoutInflater.from(mContext);
		mSmall = samllIcon;
	}
	

	public void setupWallPaperPreviewBottomIcons(LinearLayout bottomParent){
		int size = ThemeConfig.WALLPAPER_PREVIEW_BOTTOM_ICONS.length;
		for(int i = 0;i< size;i++){
			String pkg = ThemeConfig.WALLPAPER_PREVIEW_BOTTOM_ICONS[i];
			Drawable iconDrawable = mUtils.getIconDrawable(pkg, Utils2Icon.INTER_SHADOW);
			if(iconDrawable != null){
				View iconView = mInflater.inflate(mSmall?R.layout.wallpaper_icon_item_bottom_small:R.layout.wallpaper_icon_item_bottom, null);
				ImageView icon = (ImageView)iconView.findViewById(R.id.icon_bottom);
				icon.setImageDrawable(iconDrawable);
				
				bottomParent.addView(iconView,createParams());
			}
		}
	}
	
	private LinearLayout.LayoutParams createParams(){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.weight = 1;
		return params;
	}
	
	public void setupWallpaperPreviewTopIcons(LinearLayout topParent){
		int size = ThemeConfig.WALLPAPER_PREVIEW_TOP_ICONS.length;
		for(int i = 0;i< size;i++){
			String pkg = ThemeConfig.WALLPAPER_PREVIEW_TOP_ICONS[i];
			Drawable iconDrawable = mUtils.getIconDrawable(pkg, Utils2Icon.INTER_SHADOW);
			if(iconDrawable != null){
				View iconView = mInflater.inflate(mSmall?R.layout.wallpaper_icon_item_top_small:R.layout.wallpaper_icon_item_top, null);
				ImageView icon = (ImageView)iconView.findViewById(R.id.icon_img);
				icon.setImageDrawable(iconDrawable);
				TextView iconName = (TextView)iconView.findViewById(R.id.icon_name);
				String appName = getAppNameByPkgName(pkg);
				iconName.setText(appName);
				topParent.addView(iconView,createParams());
			}
		}
		
	}
	
	private String getAppNameByPkgName(String pkgName){
		PackageManager pm = mContext.getPackageManager();
		try {
			ApplicationInfo appInfo = pm.getApplicationInfo(pkgName, PackageManager.GET_META_DATA);
			if(appInfo != null){
				CharSequence appName = pm.getApplicationLabel(appInfo);
				return appName.toString();
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return "";
	}
	
	

}

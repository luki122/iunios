/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.aurora.launcher;

import java.util.ArrayList;

import com.aurora.util.HanziToPinyin;
import com.aurora.util.Utils;
import com.aurora.util.HanziToPinyin.Token;

import android.appwidget.AppWidgetHostView;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * Represents a launchable icon on the workspaces and in folders.
 */
public class ShortcutInfo extends ItemInfo {

    /**
     * The intent used to start the application.
     */
    Intent intent;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    boolean customIcon;

    /**
     * Indicates whether we're using the default fallback icon instead of something from the
     * app.
     */
    boolean usingFallbackIcon;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

    /**
     * The application icon.
     */
    private Bitmap mIcon;

    ShortcutInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }
    
    String fullPinyin = "#";
    String simplePinyin = "#";
    String startLetter;
    
    public String getFirstSpell(String spellStr){
    	char letter = '#';
		String str = spellStr.toUpperCase();
		if(str.length() > 0){
			letter = str.charAt(0);
			if(!(letter>='A' && letter<='Z')){
				letter = '#';
			}
		} 
		return String.valueOf(letter);
    }
    
    public void constructSpell(){
    	// addAllAppsIntoDB调用getShortcutInfo得到数据
    	// 调用addItemToContentValues来构建保存到数据库的数据，然后会调用到onAddToDatabase对数据进行赋值
    	// 其实favorites不会保存到数据库，因此需要在getShortcutInfo/*2620*/行再保存一次
		String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
        String[] pinyinArray = Utils.getFullPinYin(titleStr);
        if(pinyinArray != null) {
	        fullPinyin = pinyinArray[0];
	        simplePinyin = pinyinArray[1];
	        startLetter = getFirstSpell(simplePinyin);
	        //Log.e("HJJ", ">>>>fullPinyin:" + fullPinyin + ",simplePinyin:" + simplePinyin + ",startLetter:" + startLetter);
        }
    }
    
    public void constructSpell(Context context){
		final ContentValues values = new ContentValues();
		String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
        String[] pinyinArray = Utils.getFullPinYin(titleStr);
        if(pinyinArray != null) {
	        fullPinyin = pinyinArray[0];
	        simplePinyin = pinyinArray[1];
	        startLetter = getFirstSpell(simplePinyin);
			values.put(LauncherSettings.BaseLauncherColumns.FULL_PINYIN, fullPinyin);
	        values.put(LauncherSettings.BaseLauncherColumns.SIMPLE_PINYIN, simplePinyin);
	        LauncherModel.updatePinyinInDatabase(context, this, values);
	        //Log.e("HJJ", ">>>>title:" + titleStr + ",full:" + fullPinyin + ",simple:" + simplePinyin);
        }
    }
    
    public ShortcutInfo(ShortcutInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        mIcon = info.mIcon; // TODO: should make a copy here.  maybe we don't need this ctor at all
        customIcon = info.customIcon;
    }

    /** TODO: Remove this.  It's only called by ApplicationInfo.makeShortcut. */
    public ShortcutInfo(ApplicationInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        customIcon = false;
    }

    public void setIcon(Bitmap b) {
        mIcon = b;
    }

    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
            updateIcon(iconCache);
        }
        return mIcon;
    }

    /** Returns the package name that the shortcut's intent will resolve to, or an empty string if
     *  none exists. */
    public String getPackageName() {
        return super.getPackageName(intent);
    }
    
    /**
     * get class name from intent
     * it maybe return null.
     * @return
     */
    public String getClassName() {
    	if(intent == null) {
    		return null;
    	}
    	if(intent.getComponent() == null) {
    		return null;
    	}
    	
    	return intent.getComponent().getClassName();
    }

    //Vulcan created this method for dynamic icon in 2014-6-10
    public Intent getIntent() {
    	return this.intent;
    }
    
    public String getTitie(){
    	return String.valueOf(this.title);
    }

    public void updateIcon(IconCache iconCache) {
        mIcon = iconCache.getIcon(intent);
        usingFallbackIcon = iconCache.isDefaultIcon(mIcon);
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }

    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);

        String titleStr = title != null ? title.toString().replace(' ', ' ').trim() : null;
        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);
        values.put(LauncherSettings.BaseLauncherColumns.FULL_PINYIN, fullPinyin);
        values.put(LauncherSettings.BaseLauncherColumns.SIMPLE_PINYIN, simplePinyin);

        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);
        
        if(uri != null) {
        	if(uri.contains("sourceBounds=")) {
        		LauncherApplication.logVulcan.print("onAddToDatabase: find out sourceBounds, intent = " + uri);
        		LauncherApplication.logVulcan.print("onAddToDatabase: stack = " + LogWriter.StackToString(new Throwable()));
        	}
        }

        if (customIcon) {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP);
            writeBitmap(values, mIcon);
        } else {
            if (!usingFallbackIcon) {
                //writeBitmap(values, mIcon);
            }
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE);
            if (iconResource != null) {
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE,
                        iconResource.packageName);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE,
                        iconResource.resourceName);
            }
        }
        
        //Log.d("vulcan-db",LogWriter.StackToString(new Throwable()));
        
        

		//iconcat, vulcan create this statement in 2014-7-10
        values.put(LauncherSettings.Favorites.FIRST_INSTALL_TIME, firstInstallTime);
            
        Log.d("vulcan-db",String.format("onAddToDatabase: writing firstInstallTime = %d, time is %s,app is %s",
        		firstInstallTime,
        		LogWriter.LongTimeToFormatTime(firstInstallTime),
        		this.title));
    }

    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + "intent=" + intent + "id=" + this.id
                + " type=" + this.itemType + " container=" + this.container + " screen=" + screen
                + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX + " spanY=" + spanY
                + " dropPos=" + dropPos + ")";
    }

    public static void dumpShortcutInfoList(String tag, String label,
            ArrayList<ShortcutInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (ShortcutInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + " icon=" + info.mIcon
                    + " customIcon=" + info.customIcon);
        }
    }

    // Aurora <jialf> <2013-09-19> add for install/unstall apps begin
    int flags = 0;
    
    boolean newFlag;
    // Aurora <jialf> <2013-09-19> add for install/unstall apps end
    
    boolean deleteFlag;
    
    //iconcat, vulcan created this field in 2014-7-10
    long firstInstallTime;
    
    //Hazel start to add for classify application 
    public String appType;

	public String cateName;
	
	public int clsid;
	
	int appWidgetId = NO_ID;
	AppWidgetHostView hostView = null;
	int enableWidgets = 0;
	int widgetIndex = 0;
}


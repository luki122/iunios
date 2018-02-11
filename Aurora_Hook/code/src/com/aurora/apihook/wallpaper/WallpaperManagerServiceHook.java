package com.aurora.apihook.wallpaper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.android.internal.util.JournaledFile;
import android.util.SparseArray;
import android.util.Xml;
import com.aurora.apihook.ClassHelper;
import com.aurora.apihook.Hook;
import com.aurora.apihook.XC_MethodHook.MethodHookParam;


public class WallpaperManagerServiceHook implements Hook {
	public final static String TAG = "WallpaperManagerServiceHook";
	private static final boolean DEBUG = false;

	public WallpaperManagerServiceHook() {

	}

	public void before_loadSettingsLocked(MethodHookParam param) {
		Log.i(TAG,"before_loadSettingsLocked invoked!");
		Object args = param.args;
		int userId = -1;
		if(args!=null && param.args.length>0){
			userId =((Integer)(param.args[0])).intValue();
		}
		JournaledFile journal = (JournaledFile)(ClassHelper.callMethod(param.thisObject, "makeJournaledFile", userId));
        FileInputStream stream = null;
        File file = journal.chooseForRead();
        if (!file.exists()) {
            // This should only happen one time, when upgrading from a legacy system
            ClassHelper.callMethod(param.thisObject, "migrateFromOld");
        }
        SparseArray<Object> mWallpaperMap =  (SparseArray<Object>)(ClassHelper.getObjectField(param.thisObject,"mWallpaperMap"));
        Object wallpaper = ClassHelper.callMethod(mWallpaperMap, "get", userId);
        if (wallpaper == null) {
        	try{
        		Class<?> clz = ClassHelper.findClass("com.android.server.WallpaperManagerService.WallpaperData",null);//new WallpaperData(userId);
        		if(clz != null){
        			
        			Constructor<?> constructor = clz.getDeclaredConstructor(int.class);
        			if(constructor != null){
        				constructor.setAccessible(true);
        				wallpaper = constructor.newInstance(userId);
                		mWallpaperMap.put(userId, wallpaper);
        			}
            		
        		}
        		
        	}catch(Exception e){
        		Log.i(TAG,"before_loadSettingsLocked handle IllegalAccessException : ",e);
        	}
        }
        boolean success = false;
        try {
            stream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);

            int type;
            do {
                type = parser.next();
                if (type == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if ("wp".equals(tag)) {
                    	ClassHelper.setIntField(wallpaper, "width", Integer.parseInt(parser.getAttributeValue(null, "width")));
                    	ClassHelper.setIntField(wallpaper, "height", Integer.parseInt(parser.getAttributeValue(null, "height")));
                    	ClassHelper.setObjectField(wallpaper, "name", parser.getAttributeValue(null, "name"));
                        String comp = parser.getAttributeValue(null, "component"); 
                        ComponentName cp = (comp!=null)?ComponentName.unflattenFromString(comp):null;
                        ClassHelper.setObjectField(wallpaper, "nextWallpaperComponent", cp);
                        if (cp == null
                                || "android".equals(cp.getPackageName())) {
                        	ComponentName cpn =(ComponentName)( ClassHelper.getStaticObjectField((param.thisObject).getClass(), "IMAGE_WALLPAPER"));
                        	ClassHelper.setObjectField(wallpaper, "nextWallpaperComponent", cpn);
                        }
                    }
                }
            } while (type != XmlPullParser.END_DOCUMENT);
            success = true;
        } catch (FileNotFoundException e) {
        	Log.w(TAG, "no current wallpaper -- first boot?");
        } catch (NullPointerException e) {
        	Log.w(TAG, "failed parsing " + file + " " + e);
        } catch (NumberFormatException e) {
        	Log.w(TAG, "failed parsing " + file + " " + e);
        } catch (XmlPullParserException e) {
        	Log.w(TAG, "failed parsing " + file + " " + e);
        } catch (IOException e) {
        	Log.w(TAG, "failed parsing " + file + " " + e);
        } catch (IndexOutOfBoundsException e) {
        	Log.w(TAG, "failed parsing " + file + " " + e);
        }
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            // Ignore
        }

        if (!success) {
        	ClassHelper.setIntField(wallpaper, "width", -1);
        	ClassHelper.setIntField(wallpaper, "height", -1);
        	ClassHelper.setObjectField(wallpaper, "name", "");
        }
        
        // We always want to have some reasonable width hint.
        Context context = (Context)(ClassHelper.getObjectField(param.thisObject, "mContext"));
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        int baseSize = d.getMaximumSizeDimension();
        Log.i(TAG, "wallpaper.width = " + ClassHelper.getIntField(wallpaper, "width")+"  : baseSize = "+baseSize);
        if (ClassHelper.getIntField(wallpaper, "width") == -1) {
            //wallpaper.width = d.getWidth() * 2;
            //wallpaper.width = d.getWidth() ;
        	ClassHelper.setIntField(wallpaper, "width", d.getWidth());
        }
        if (ClassHelper.getIntField(wallpaper, "height") == -1) {
        	ClassHelper.setIntField(wallpaper, "height", d.getHeight());
        }
        param.setResult(null);
}
}

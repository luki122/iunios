package com.secure.utils;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import com.secure.imageloader.ImageCallback;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
/**
 * 异步加载apk的图标
 * @author Administrator
 */
public class AsyncApkIconLoader {
	private static AsyncApkIconLoader instance;
	private HashMap<String, SoftReference<Drawable>> imageCache;
	private Context context = null;

	public static synchronized AsyncApkIconLoader getInstance(Context context) {
		if (instance == null) {
			instance = new AsyncApkIconLoader(context);
		}
		return instance;
	}
	
	private AsyncApkIconLoader(Context context) {
		imageCache = new HashMap<String, SoftReference<Drawable>>();
		this.context = context;
	}
	
	public Drawable loadDrawable(final String packageName,
			final Object viewTag,
			final ImageCallback imageCallback) {

		if (imageCache.containsKey(packageName)) {
			SoftReference<Drawable> softReference = imageCache.get(packageName);
			if (softReference != null) {
				Drawable drawable = softReference.get();
				if (drawable != null) {
					return drawable;
				}
			}
		}
		
		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				Drawable icon =(Drawable)message.obj;
				if(icon == null || imageCache == null){
					return ;
				}
				imageCache.put(packageName, new SoftReference<Drawable>(icon));
				if(imageCallback != null){
					imageCallback.imageLoaded(icon, viewTag);	
				}  
			}
		};
		
		new Thread() {
			@Override
			public void run() {
				Drawable drawable = ApkUtils.getApkIcon(context, packageName);
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return null;
	}
	
	public static void releaseObject(){
		if(instance != null){
			if(instance.imageCache != null){
				instance.imageCache.clear();	
			}
			if(mConfig.SET_NULL_OF_CONTEXT){
				instance.context = null;
			}			
			instance = null;
		}
	}
}

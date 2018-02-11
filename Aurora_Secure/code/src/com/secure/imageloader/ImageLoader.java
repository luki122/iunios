package com.secure.imageloader;

import java.lang.ref.SoftReference;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;

public class ImageLoader {
	public static final String TAG = ImageLoader.class.getSimpleName();
	private static final String ERROR_INIT_CONFIG_WITH_NULL = "ImageLoader configuration can not be initialized with null";

	private ImageLoaderConfiguration configuration;
	private ImageLoaderEngine engine;
	private UIHandler mUIhandler;
	
	private volatile static ImageLoader instance;	

	public static ImageLoader getInstance(Context context) {
		if (instance == null) {
			synchronized (ImageLoader.class) {
				if (instance == null) {
					instance = new ImageLoader(context);
				}
			}
		}
		return instance;
	}
	
	protected ImageLoader(Context context){
		mUIhandler = new UIHandler(Looper.getMainLooper());
		init(context);
	}
	
	private void init(Context context){
		if(context == null){
			throw new IllegalArgumentException(ERROR_INIT_CONFIG_WITH_NULL);
		}
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context.getApplicationContext()).build();
		if (this.configuration == null) {
			engine = new ImageLoaderEngine(config);
			this.configuration = config;
		} 
	}
	
	/**
	 * @param pkgName
	 * @param viewTag
	 * @param imageCallback 
	 * @throws IllegalStateException    if {@link #init(ImageLoaderConfiguration)} method wasn't called before
	 * @throws IllegalArgumentException if passed <b>imageView</b> is null
	 * @return
	 */
	public Drawable displayImage(View view,
			Object imgUrl,
			Object viewTag,
			ImageCallback imageCallback){
		if(view == null || 
				imgUrl == null ||
				viewTag == null || 
				imageCallback == null){
			return null;
		}
		Object memoryCacheKey = imgUrl;
		Drawable drawable = null;
		SoftReference<Drawable> softReference = configuration.memoryCache.get(memoryCacheKey);
		if (softReference != null) {
			drawable = softReference.get();
		}
		if (drawable != null) {
            return drawable;
		} else {	
			ImageLoadingInfo imageLoadingInfo = new ImageLoadingInfo(view,
					viewTag,
					imgUrl,
					memoryCacheKey,
					imageCallback,
					mUIhandler,
					engine.getLockForUri(imgUrl));
			
			LoadImageTask displayTask = null;
			displayTask = configuration.getTaskFromUnAvailMap(view.hashCode());
			if(displayTask != null && 
					displayTask.updateInfoIfIsWait(imageLoadingInfo)){
                return null;
			}
			
			displayTask = configuration.getTaskFromAvailList();
			if(displayTask != null &&
					displayTask.updateInfoIfIsFinish(imageLoadingInfo)){	
				configuration.removeTaskFromAvailList(displayTask);
				configuration.addTaskToUnAvailMap(view.hashCode(), displayTask);
				engine.submit(displayTask);
				return null;
			}
					
			displayTask = new LoadImageTask(engine, imageLoadingInfo);
			configuration.addTaskToUnAvailMap(view.hashCode(), displayTask);
			engine.submit(displayTask);
		}
		return null;
	}
	
    final class UIHandler extends Handler{		
 		public UIHandler(Looper looper){
            super(looper);
        }
 		@Override
 	    public void handleMessage(Message msg) {  
 			ImageLoadingResult result = (ImageLoadingResult)msg.obj;
 			if(result == null){
 				return ;
 			}
 			result.imageCallback.imageLoaded(result.drawable, result.viewTag);
 	    }
 	}

	/**
	 * Cancels all running and scheduled display image tasks.<br />
	 * ImageLoader still can be used after calling this method.
	 */
	public void stop() {
		engine.stop();
	}
	
	public static void releaseObject(){
		if(instance != null){
			instance.stop();
			if(instance.configuration != null){
				instance.configuration.releaseObject();
			}
			instance = null;
		}
	}
}

package com.secure.imageloader;

import java.lang.ref.SoftReference;
import java.util.concurrent.locks.ReentrantLock;
import com.secure.utils.ApkUtils;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.view.View;

final class LoadImageTask implements Runnable {
	private final ImageLoaderConfiguration configuration;
	private ImageLoadingInfo imageLoadingInfo;
	private final Object locked = new Object();
	private RunState runState;
	
	enum RunState {
		WAITTING,
		RUNNING,
		FINISH
	}

	public LoadImageTask(ImageLoaderEngine engine, 
			ImageLoadingInfo imageLoadingInfo) {
		this.imageLoadingInfo = imageLoadingInfo;
		configuration = engine.configuration;	
		runState = RunState.WAITTING;
	}
	
	private void setRunningFlag(){
		synchronized(locked){
			runState = RunState.RUNNING;
		}	
	}
	
	private void setFinishFlag(){
        synchronized(locked){
        	runState = RunState.FINISH;
		}	
		configuration.removeTaskFromUnAvailMap(imageLoadingInfo.view.hashCode());
		configuration.addTaskToAvailList(this);
	}

	@Override
	public void run() {
		ImageLoadingInfo tmpImageLoadingInfo = null;
		while(tmpImageLoadingInfo == null || tmpImageLoadingInfo !=imageLoadingInfo){
			tmpImageLoadingInfo = imageLoadingInfo;
			try {
				Thread.sleep(150);//这里延时是关键，如果这里不延时加载，U3在快速滑动时会卡死
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		setRunningFlag();					
		ReentrantLock loadFromUriLock = imageLoadingInfo.loadFromUriLock;
		loadFromUriLock.lock();
		Drawable drawable = null;
		try {
			if ((imageLoadingInfo.imgUrl) instanceof String) {
				drawable = ApkUtils.getApkIcon(configuration.context, 
						(String)imageLoadingInfo.imgUrl);			
			}else if((imageLoadingInfo.imgUrl) instanceof ResolveInfo){
				drawable = ApkUtils.getApkIcon(configuration.context, 
						(ResolveInfo)imageLoadingInfo.imgUrl);	
			}		
			drawable = reduceDrawableIfNeed(drawable,imageLoadingInfo.view);
			if(drawable != null){
				configuration.memoryCache.put(imageLoadingInfo.memoryCacheKey,
						new SoftReference<Drawable>(drawable));
			}
		} finally {
			loadFromUriLock.unlock();
		}
		if (checkTaskIsInterrupted()){
			setFinishFlag();	
			return;
		}
		
	    if(drawable != null){
			Message msg = imageLoadingInfo.handler.obtainMessage();
			msg.obj = new ImageLoadingResult(imageLoadingInfo.viewTag,
					imageLoadingInfo.imageCallback,
					drawable);
			imageLoadingInfo.handler.sendMessage(msg);	
	    }
		setFinishFlag();			
	}
	
	private Drawable reduceDrawableIfNeed(Drawable drawable,View view){
		if(drawable == null || view == null){
			return drawable;
		}
		int widthOfDrawable = drawable.getIntrinsicWidth();
		int heightOfDrawable = drawable.getIntrinsicHeight();
		int widthOfView = view.getWidth();
		int heightOfView = view.getHeight();
		if(widthOfView == 0 || heightOfView == 0){
			return drawable;
		}
		
		if(widthOfDrawable>widthOfView*1.3 && 
				heightOfDrawable>heightOfView*1.3){	
			float widthScale = (float) widthOfDrawable / widthOfView;
			float heightScale = (float) heightOfDrawable / heightOfView;
			float scale = widthScale<heightScale?widthScale:heightScale;
			int bitMapWidth = (int)(widthOfDrawable/scale);
			int bitMapHeight = (int)(heightOfDrawable/scale);
			if(bitMapWidth < widthOfView || 
					bitMapHeight < heightOfView ){
				return drawable;
			}
			
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			Bitmap finalBitmap = Bitmap.createScaledBitmap(bitmap,
					bitMapWidth,bitMapHeight, false);		
			drawable = new BitmapDrawable(finalBitmap);
			return drawable;						
		}else{
			return drawable;
		}		
	}
	
	/**
	 * 如果当前Runnable未执行，还处于wait状态，则可以更新imageLoadingInfo
	 * @param view
	 * @param pkgName
	 * @param viewTag
	 * @param imageCallback
	 * @return
	 */
	public boolean updateInfoIfIsWait(ImageLoadingInfo imageLoadingInfo){
		synchronized(locked){
			if(runState == RunState.WAITTING){
				this.imageLoadingInfo = imageLoadingInfo;
				return true;
			}
			return false;		
		}
	}
	
	/**
	 * 如果当前Runnable处于Finish状态，则可以更新imageLoadingInfo
	 * @param view
	 * @param pkgName
	 * @param viewTag
	 * @param imageCallback
	 * @return
	 */
	public boolean updateInfoIfIsFinish(ImageLoadingInfo imageLoadingInfo){
		synchronized(locked){
			if(runState == RunState.FINISH){
				runState = RunState.WAITTING;
				this.imageLoadingInfo = imageLoadingInfo;
				return true;
			}
			return false;		
		}
	}

	/** Check whether the current task was interrupted */
	private boolean checkTaskIsInterrupted() {
		boolean interrupted = Thread.interrupted();
		return interrupted;
	}
}

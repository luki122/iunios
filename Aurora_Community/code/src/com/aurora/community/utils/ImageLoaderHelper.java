package com.aurora.community.utils;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ImageLoaderHelper {

	private static ImageLoader imageLoader = ImageLoader.getInstance();
	
	public static ImageLoader getImageLoader(){
		return imageLoader;
	}
	public static boolean checkImageLoader(){
		return imageLoader.isInited();
	}
	
	public static void disPlay(String uri, ImageAware imageAware,Drawable defaultDrawable){
		Log.e("linp", "################uri="+uri);
		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(defaultDrawable)
		.showImageForEmptyUri(defaultDrawable)
		.showImageOnFail(defaultDrawable)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new SimpleBitmapDisplayer())
		.build();

		imageLoader.displayImage(uri, imageAware, options);
		
	}
	
	public static void loadImage(String url,ImageLoadingListener listener){
		imageLoader.loadImage(url, listener);
	}
	
	public static void disPlay(String uri, ImageView img,Drawable defaultDrawable){

		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(defaultDrawable)
		.showImageForEmptyUri(defaultDrawable)
		.showImageOnFail(defaultDrawable)
		.cacheInMemory(true)
		.cacheOnDisc(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.displayer(new FadeInBitmapDisplayer(500))
		.build();
		imageLoader.displayImage(uri, img, options);
	//	imageLoader.displayImage(uri, img);
	
	
	}
	
	public static File getDirectory(){
		return imageLoader.getDiskCache().getDirectory();
	}
	
	public static void clear(){
		imageLoader.clearMemoryCache();		
		imageLoader.clearDiscCache();
	}
	
	public static void resume(){
		imageLoader.resume();
	}
	/**
	 * 暂停加载
	 */
	public static void pause(){
		imageLoader.pause();
	}
	/**
	 * 停止加载
	 */
	public static void stop(){
		imageLoader.stop();
	}
	/**
	 * 销毁加载
	 */
	public static void destroy() {
		imageLoader.destroy();
	}
}

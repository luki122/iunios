package com.aurora.change.utils;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import com.aurora.change.R;
import com.aurora.change.activities.WallpaperLocalActivity;
import com.aurora.change.data.DataOperation;
import com.aurora.change.data.DbControl;
import com.aurora.change.model.NextDayPictureInfo;
import com.aurora.change.model.PictureInfo;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ImageLoaderHelper {
	private Context mContext;
	private Handler mHandler;
//	private int maxWidth;
//	private int maxHeight;
	
	public ImageLoaderHelper(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
	}
	
	public void setHandle(Handler handler) {
		if (mHandler == null) {
			mHandler = handler;
		}
	}
	
	public void setupDefaultConfiguration() {
		//创建默认的ImageLoader配置参数
		ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(mContext);
        
		//Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(configuration);
	}
	
	public void setupCustomizedConfiguration(int maxWidth, int maxHeight, String filePath) {
		File mFile = new File(filePath);
		
		ImageLoaderConfiguration configuration = new ImageLoaderConfiguration
		.Builder(mContext)
		.memoryCacheExtraOptions(maxWidth, maxHeight)	// max width, max height 即保存的每个缓存文件的最大长宽
//	    .diskCacheExtraOptions(maxWidth, maxHeight, CompressFormat.JPEG, 75, null)	// Can slow ImageLoader, use it carefully (Better don't use it)
	    																			//设置缓存的详细信息，最好不要设置这个
		.threadPoolSize(3)	//线程池内加载的数量 default
		.threadPriority(Thread.NORM_PRIORITY - 1)	//default
		.denyCacheImageMultipleSizesInMemory()
//		.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))	// You can pass your own memory cache implementation/你可以通过自己的内存缓存实现
//		.memoryCacheSize(2 * 1024 * 1024)
		.memoryCache(new LruMemoryCache(10 * 1024 * 1024))	//10M
        .memoryCacheSize(10 * 1024 * 1024)
        .memoryCacheSizePercentage(13)	// default
		.diskCache(new UnlimitedDiscCache(mFile))	//自定义缓存路径
		.diskCacheSize(100 * 1024 * 1024)	//100M
		.diskCacheFileCount(100)	//缓存的文件数量
//	    .discCacheFileNameGenerator(new Md5FileNameGenerator())//将保存的时候的URI名称用MD5 加密
		.tasksProcessingOrder(QueueProcessingType.FIFO)		
		.defaultDisplayImageOptions(DisplayImageOptions.createSimple())
		.imageDownloader(new BaseImageDownloader(mContext, 5 * 1000, 30 * 1000))	// connectTimeout (5 s), readTimeout (30 s)超时时间
		.writeDebugLogs()	// Remove for release app
		.build();	//开始构建
		
		//Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(configuration);
	}
	
	public static DisplayImageOptions setupDisplayImageOption() {
		DisplayImageOptions options = new DisplayImageOptions.Builder()
//		.showImageOnLoading(R.drawable.ic_stub)	// resource or drawable
//		.showImageForEmptyUri(R.drawable.wallpaper_load_failed) // resource or drawable
		.showImageOnFail(R.drawable.wallpaper_load_failed) // resource or drawable
		.resetViewBeforeLoading(true)	//设置图片在下载前是否重置，复位 
//		.delayBeforeLoading(int delayInMillis)
		.cacheInMemory(true) // default
		.cacheOnDisk(true) // default
//		.preProcessor(BitmapProcessor preProcessor)
//		.postProcessor(...)
//		.extraForDownloader(...)
//		.considerExifParams(false) // default
		.imageScaleType(ImageScaleType.EXACTLY) // default
		.bitmapConfig(Bitmap.Config.ARGB_8888) // default
//		.decodingOptions(android.graphics.BitmapFactory.Options decodingOptions)//设置图片的解码配置
//		.displayer(new SimpleBitmapDisplayer()) // default
		.displayer(new FadeInBitmapDisplayer(100))//是否图片加载好后渐入的动画时间
//		.handler(new Handler()) // default
		.build();
		
		return options;
	}
	
	public void updatePreviewPicture(NextDayPictureInfo pictureInfo, String uri, String operation, ImageView imageView, 
																	DisplayImageOptions options, ProgressBar progressBar) {		
		ImageLoader.getInstance().displayImage(uri, imageView, options, 
								new UpdatePictureListener(pictureInfo, operation, progressBar), new UpdateProgressListener());
	}
	
	class UpdatePictureListener implements ImageLoadingListener {
		private NextDayPictureInfo pictureInfo;
		private String operationType;
		private ProgressBar mProgressBar;
				
		public UpdatePictureListener(NextDayPictureInfo info, String type, ProgressBar progressBar) {
			// TODO Auto-generated constructor stub
			pictureInfo = info;
			operationType = type;
			mProgressBar = progressBar;
		}
		
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onLoadingStarted");
			/*if (mProgressBar != null) {
				mProgressBar.setVisibility(View.VISIBLE);
			}*/
		}
		
		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onLoadingFailed----imageUri = "+imageUri+" failReason = "+failReason);
			view.setBackgroundResource(R.drawable.wallpaper_load_server_error);
			mProgressBar.setVisibility(View.GONE);
		}
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			// TODO Auto-generated method stub
//			Log.d("Wallpaper_DEBUG", "--------LoadingListener--------onLoadingComplete----isSave = "+toSave+" isShow = "+toShow);
			Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onLoadingComplete----operationType = "+operationType);
			
//			String initPath = Consts.NEXTDAY_WALLPAPER_PATH + "NextDay" + File.separator + pictureInfo.getPictureTime() + ".jpg";
	    	String originalPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + ".jpg";
	    	String previewPath = Consts.NEXTDAY_WALLPAPER_SAVED + pictureInfo.getPictureTime() + "_comment" + ".jpg";
			
			if (mProgressBar != null) {
				mProgressBar.setVisibility(View.GONE);
			}
			
			if (Consts.NEXTDAY_PICTURE_LOADTYPE_NONE.equals(operationType)) {
				//do nothing
				
			} else {
				if (loadedImage == null) return;
				
				if (Consts.NEXTDAY_PICTURE_LOADTYPE_DOWNLOAD.equals(operationType) || Consts.NEXTDAY_PICTURE_LOADTYPE_PICTURE.equals(operationType)) {
					Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onLoadingComplete----loadedImage.getByteCount() = "+loadedImage.getByteCount());
					if (loadedImage.getByteCount() > 10000) {
						FileHelper.writeImage(loadedImage, originalPath, 100);
					}
				}
				
				if (FileHelper.fileIsExist(previewPath)) return;
				
				Bitmap targetBitmap = null;
				if (pictureInfo != null) {
					targetBitmap = WallpaperConfigUtil.nextdayPictureCompose(mContext, Consts.NEXTDAY_OPERATION_SET, pictureInfo, loadedImage, targetBitmap);
					
				} else {
					targetBitmap = loadedImage;
				}
				
				FileHelper.writeImage(targetBitmap, previewPath, 100);
				if (targetBitmap != null) {
					targetBitmap.recycle();
				}
			}
		}
		
		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onLoadingCancelled");
		}
	}
	
	class UpdateProgressListener implements ImageLoadingProgressListener {
		@Override
		public void onProgressUpdate(String imageUri, View view, int current, int total) {
			// TODO Auto-generated method stub
//			Log.d("Wallpaper_DEBUG", "updatePreviewPicture--------onProgressUpdate---current = "+current+" total = "+total);
		}
	}
	
	public void LoadAndProcessPicture(NextDayPictureInfo pictureInfo, String uri, String operation, String type, String path, ProgressBar progressBar) {
		ImageLoader.getInstance().loadImage(uri, new LoadListener(pictureInfo, operation, type, path, progressBar));
	}
	
	class LoadListener implements ImageLoadingListener {
		private NextDayPictureInfo pictureInfo;
		private String operationType;
		private String loadingType;
		private String filePath;
		private ProgressBar mProgressBar;
				
		public LoadListener(NextDayPictureInfo info, String operation, String type, String path, ProgressBar progressBar) {
			// TODO Auto-generated constructor stub
			pictureInfo = info;
			operationType = operation;
			loadingType = type;
			filePath = path;
			mProgressBar = progressBar;
		}
		
		@Override
		public void onLoadingStarted(String imageUri, View view) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadAndProcessPicture--------onLoadingStarted");
			/*if (mProgressBar != null) {
				mProgressBar.setVisibility(View.VISIBLE);
			}*/
		}
		
		@Override
		public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadAndProcessPicture--------onLoadingFailed----imageUri = "+imageUri+" failReason = "+failReason);
//			mProgressBar.setVisibility(View.GONE);
		}
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadAndProcessPicture--------onLoadingComplete----operationType = "+operationType);
			
	    	WallpaperConfigUtil.pictureProcessForNextDay(mContext, mHandler, pictureInfo, operationType, filePath, loadedImage);
		}
		
		@Override
		public void onLoadingCancelled(String imageUri, View view) {
			// TODO Auto-generated method stub
			Log.d("Wallpaper_DEBUG", "LoadAndProcessPicture--------onLoadingCancelled");
		}
	}	
	
}

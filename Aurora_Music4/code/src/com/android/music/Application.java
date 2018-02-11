package com.android.music;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import android.os.storage.StorageManager;

import com.android.auroramusic.model.XiaMiSdkUtils;
import com.android.auroramusic.ui.AuroraMediaPlayHome;
import com.android.auroramusic.util.AuroraListItem;
import com.android.auroramusic.util.AuroraMusicUtil;
import com.android.auroramusic.util.Globals;
import com.android.auroramusic.util.LogUtil;
import com.android.auroramusic.util.ThreadPoolExecutorUtils;
import com.android.auroramusic.util.AuroraMusicUtil.SDCardInfo;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.xiami.music.model.RadioCategory;
import com.xiami.music.model.RadioInfo;
import com.xiami.sdk.entities.OnlineSong;

import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.StorageVolume;
import android.widget.Toast;

public class Application extends android.app.Application {
	protected static final String TAG = "Application";
	// private static String cacheDir;
	public static List<RadioCategory> mRadioCategories = null;
	public static int mRadioPosition = -1;
	public static int mRadiotype = -1;
	private StorageManager mStorageManager;

	/**
	 * 全局保存可用存储路径
	 */
	private List<String> storagePath = new ArrayList<String>();

	public List<String> getStoragePath() {
		return storagePath;
	}
	

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.d(TAG, "onCreate Application");
		
		initImageLoader(getApplicationContext());
		XiaMiSdkUtils.enableLog(getApplicationContext(), true);// 0.1.1
		AuroraMusicUtil.setMusicProp();
		AuroraMusicUtil.getMountedStorage(this);// lory add 2014.9.2
		setStorage(1);
		long sdFreeSzie=0;
		for (int i = 0; i < storagePath.size(); i++) {
			SDCardInfo phoneInfo = AuroraMusicUtil.getSDCardInfo(true, storagePath.get(i));
			if(phoneInfo!=null){
				sdFreeSzie+=phoneInfo.free;
			}
		}
		LogUtil.d(TAG, "-------------sdFreeSzie:"+sdFreeSzie);
		if(sdFreeSzie<=Globals.LOW_MEMORY){
			Toast.makeText(this, getResources().getString(R.string.low_memory), Toast.LENGTH_LONG).show();
		}

	}
	
	
	
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
		LogUtil.e(TAG, "---onLowMemory"+activityManager.getMemoryClass());
	}


	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		LogUtil.e(TAG, "---onTrimMemory:"+level);
	}


	/**
	 * 设置使用存储器
	 * @param size
	 */
	public void setStorage(int size){
		boolean  isHaveSd = isHaveSdStorage();
		final SDCardInfo phoneInfo = AuroraMusicUtil.getSDCardInfo(true, storagePath.get(0));
		if (isHaveSd&& MusicUtils.getIntPref(getApplicationContext(), "storage_select", 0) != 0) {
			final SDCardInfo cardInfo = AuroraMusicUtil.getSDCardInfo(true, storagePath.get(1));
			if (cardInfo!=null&&size*Globals.AURORA_LOW_MEMORY > cardInfo.free) {// SD卡满，选择手机存储
				Globals.initPath(storagePath.get(0));
				MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
			} else {
				Globals.initPath(storagePath.get(1));
				MusicUtils.setIntPref(getApplicationContext(), "storage_select", 1);
			}
		} else {
			if(isHaveSd){
				if(phoneInfo!=null&&size*Globals.AURORA_LOW_MEMORY > phoneInfo.free){
					Globals.initPath(storagePath.get(1));
					MusicUtils.setIntPref(getApplicationContext(), "storage_select", 1);
				}else {
					Globals.initPath(storagePath.get(0));
					MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
				}
			}else {
				Globals.initPath(storagePath.get(0));
				MusicUtils.setIntPref(getApplicationContext(), "storage_select", 0);
			}
			
		}
		LogUtil.d(TAG, "-------------Globals.storagePath:" + Globals.storagePath + " ------:" + Globals.mSavePath);
	}

	private static void initImageLoader(Context context) {
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).threadPriority(Thread.NORM_PRIORITY - 2).threadPoolSize(3).memoryCache(new WeakMemoryCache())
				.memoryCacheSize(6 * 1024 * 1024).diskCacheFileNameGenerator(new Md5FileNameGenerator()).diskCacheSize(40 * 1024 * 1024).diskCacheFileCount(300)
				.tasksProcessingOrder(QueueProcessingType.LIFO).build();

		ImageLoader.getInstance().init(config);
	}

	/**
	 * 判断是否有SD卡
	 * @return
	 */
	public boolean isHaveSdStorage() {
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
		StorageVolume[] storageVolume = mStorageManager.getVolumeList();

		storagePath.clear();
		for (int i = 0; i < storageVolume.length; i++) {
			String temp = storageVolume[i].getPath();
			try {
				if (Globals.mTestMode||!temp.contains("usb")) {
					if (AuroraMusicUtil.sdIsMounted(temp)) {
						LogUtil.d(TAG, "----us " + temp);
						storagePath.add(temp);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "sd info " + e.getMessage());
			}
		}
		return storagePath.size() > 1;
	}

	public static void setRadio(int type, int posion) {
		LogUtil.d(TAG, "setRadio:" + type + " posion:" + posion);
		mRadioPosition = posion;
		mRadiotype = type;
	}

	public static List<RadioInfo> getRadioInfoList() {
		if (mRadioCategories != null && mRadiotype >= 0) {
			return mRadioCategories.get(mRadiotype).getRadios();
		}
		return null;
	}

	public static boolean isRadioType() {
		LogUtil.d(TAG, "isRadioType:" + Application.mRadiotype);
		boolean isRadioType = false;
		if (Application.mRadiotype >= 0) {
			isRadioType = true;
		} else {
			isRadioType = false;
		}
		return isRadioType;
	}

	private SerchRadioRunnable mRadioRunnable;

	public void startPlayRadio(int posion, Handler handler, Context context) {
		ThreadPoolExecutor executor = ThreadPoolExecutorUtils.getThreadPoolExecutor().getExecutor();
		if (mRadioRunnable != null) {
			mRadioRunnable.cancelSearch();
			executor.remove(mRadioRunnable);
		}
		mRadioRunnable = new SerchRadioRunnable(posion, handler, context);
		executor.submit(mRadioRunnable);
	}

	private class SerchRadioRunnable implements Runnable {

		private boolean iscancel = false;
		private int postion;
		private Handler mHandler;
		private SoftReference<Context> activity;

		public SerchRadioRunnable(int pos, Handler handler, Context context) {
			postion = pos;
			mHandler = handler;
			activity = new SoftReference<Context>(context);
		}

		private void cancelSearch() {
			iscancel = true;
		}

		@Override
		public void run() {
			final Context context = activity.get();
			LogUtil.d(TAG, "SerchRadioRunnable context 1:" + context);
			if (Application.getRadioInfoList() == null || context == null) {
				return;
			}
			LogUtil.d(TAG, "SerchRadioRunnable postion 2:" + postion);
			RadioInfo info = Application.getRadioInfoList().get(postion);
			List<OnlineSong> arg1 = XiaMiSdkUtils.fetchRadioDetailSync(context, info.getType(), info.getRadioId());
			if (iscancel || context == null) {
				LogUtil.d(TAG, "------iscancel:" + iscancel + " context:" + context);
				return;
			}
			if (arg1 != null) {
				final ArrayList<AuroraListItem> arrayList = new ArrayList<AuroraListItem>();
				for (int i = 0; i < arg1.size(); i++) {
					OnlineSong music = (OnlineSong) arg1.get(i);
					String pic = music.getImageUrl();
					AuroraListItem songItem = new AuroraListItem(music.getSongId(), music.getSongName(), music.getListenFile(), music.getAlbumName(), music.getAlbumId(), music.getArtistName(), 1,
							pic, music.getLyric(), null, -1,!AuroraMusicUtil.isNoPermission(music));
					songItem.setArtistId(music.getArtistId());
					arrayList.add(songItem);
				}
				if (iscancel || context == null) {
					return;
				}
				LogUtil.d(TAG, "start playlist arrayList:" + arrayList.size() + " context:" + context);
				if (arrayList.size() == 0) {
					return;
				}
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						LogUtil.d(TAG, "Application.setRadio::" + Application.mRadiotype);
						Application.setRadio(Application.mRadiotype, postion);
						MusicUtils.playRadioAll(context, arrayList, 0, 0, true);
					}
				});
			}
		}
	}
}

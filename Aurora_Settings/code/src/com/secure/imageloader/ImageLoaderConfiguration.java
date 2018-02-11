package com.secure.imageloader;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;
import android.content.Context;
import android.graphics.drawable.Drawable;

public final class ImageLoaderConfiguration {
	final Context context;
	final Executor taskExecutor;
	final boolean customExecutor;
	final int threadPoolSize;
	final int threadPriority;
	final HashMap<Object, SoftReference<Drawable>> memoryCache;
	private final HashMap<Integer,LoadImageTask> unAvailTaskMap = new HashMap<Integer,LoadImageTask>();
	private final ArrayList<LoadImageTask> availTaskList = new ArrayList<LoadImageTask>();	

	private ImageLoaderConfiguration(final Builder builder) {
		context = builder.context;
		taskExecutor = builder.taskExecutor;
		threadPoolSize = builder.threadPoolSize;
		threadPriority = builder.threadPriority;
		customExecutor = builder.customExecutor;
		memoryCache = new HashMap<Object, SoftReference<Drawable>>();
	}
	
	public void addTaskToUnAvailMap(Integer key,LoadImageTask value){
		synchronized (unAvailTaskMap) {
			unAvailTaskMap.put(key,value);
		}
	}
	
	public LoadImageTask getTaskFromUnAvailMap(Integer key){
		synchronized (unAvailTaskMap) {
			return unAvailTaskMap.get(key);
		}
	}
	
	public void removeTaskFromUnAvailMap(Integer key){
		synchronized (unAvailTaskMap) {
			unAvailTaskMap.remove(key);
		}
	}
		
	public void addTaskToAvailList(LoadImageTask loadImageTask){
		synchronized (availTaskList) {
			availTaskList.add(loadImageTask);
		}
	}
	
	public LoadImageTask getTaskFromAvailList(){
		synchronized (availTaskList) {
			if(availTaskList.size()>0){
				return availTaskList.get(0);
			}
			return null;
		}
	}
	
	public void removeTaskFromAvailList(LoadImageTask loadImageTask){
		synchronized (availTaskList) {
			availTaskList.add(loadImageTask);
		}
	}
	
	public void releaseObject(){
		if(memoryCache != null){
			memoryCache.clear();
		}
		unAvailTaskMap.clear();
		availTaskList.clear();
	}

	public static ImageLoaderConfiguration createDefault(Context context) {
		return new Builder(context).build();
	}

	public static class Builder {
		/** {@value} */
		public static final int DEFAULT_THREAD_POOL_SIZE = 2;//3;
		/** {@value} */
		public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

		private Context context;
		private Executor taskExecutor = null;
		private boolean customExecutor = false;
		private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
		private int threadPriority = DEFAULT_THREAD_PRIORITY;

		public Builder(Context context) {
			this.context = context.getApplicationContext();
		}

		public Builder taskExecutor(Executor executor) {
			this.taskExecutor = executor;
			return this;
		}

		public Builder threadPoolSize(int threadPoolSize) {
			this.threadPoolSize = threadPoolSize;
			return this;
		}

		public Builder threadPriority(int threadPriority) {
			if (threadPriority < Thread.MIN_PRIORITY) {
				this.threadPriority = Thread.MIN_PRIORITY;
			} else {
				if (threadPriority > Thread.MAX_PRIORITY) {
					threadPriority = Thread.MAX_PRIORITY;
				} else {
					this.threadPriority = threadPriority;
				}
			}
			return this;
		}

		/** Builds configured {@link ImageLoaderConfiguration} object */
		public ImageLoaderConfiguration build() {
			initEmptyFiledsWithDefaultValues();
			return new ImageLoaderConfiguration(this);
		}

		private void initEmptyFiledsWithDefaultValues() {
			if (taskExecutor == null) {
				taskExecutor = DefaultConfigurationFactory.createExecutor(
						threadPoolSize, 
						threadPriority);
			} else {
				customExecutor = true;
			}
		}
	}
}

package com.android.gallery3d.local.tools;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorUtils {
	private ThreadPoolExecutor executor;
	
	private static ThreadPoolExecutorUtils threadPoolExecutorUtils;
	
	
	public static ThreadPoolExecutorUtils getThreadPoolUtil(){
		if(threadPoolExecutorUtils==null){
			threadPoolExecutorUtils = new ThreadPoolExecutorUtils();
		}
		return threadPoolExecutorUtils;
	}
	
	public ThreadPoolExecutorUtils() {
		int processors = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(processors, 20, 2,
				TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(8),  new PriorityThreadFactory(),
				new ThreadPoolExecutor.DiscardOldestPolicy());
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	

}

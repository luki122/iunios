package com.aurora.tools;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.aurora.lazyloader.PriorityThreadFactory;

public class ThreadPoolExecutorUtils {
	private ThreadPoolExecutor executor;
	
	private static ThreadPoolExecutorUtils threadPoolExecutorUtils;
	
	
	public static ThreadPoolExecutorUtils getPrivacyUtils(){
		if(threadPoolExecutorUtils==null){
			threadPoolExecutorUtils = new ThreadPoolExecutorUtils();
		}
		return threadPoolExecutorUtils;
	}
	
	public ThreadPoolExecutorUtils() {
		int processors = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(0, processors, 60L,
				TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(15),  new PriorityThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	

}

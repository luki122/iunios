package com.aurora.reject.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CountManage {

	private static Object sync = new Object();
	public static ThreadPoolExecutor threadPool = null;
	private static int corePoolSize = 1;
	private static BlockingQueue<Runnable> workQueue;
	private static RejectedExecutionHandler handler;

	public static ThreadPoolExecutor getThreadPoolExecutor() {
		synchronized (sync) {
			if (threadPool == null) {
				workQueue = new LinkedBlockingQueue<Runnable>();
				handler = new ThreadPoolExecutor.DiscardOldestPolicy();
				threadPool = new ThreadPoolExecutor(corePoolSize, corePoolSize, 1,
						TimeUnit.SECONDS, workQueue, handler);
			}
		}
		return threadPool;
	}

	public static void setCorePoolSize(int size) {
		corePoolSize = size;
		if (threadPool != null) {
			threadPool.setCorePoolSize(corePoolSize);
		}
	}

}

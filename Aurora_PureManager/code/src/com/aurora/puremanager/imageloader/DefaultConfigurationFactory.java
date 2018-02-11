package com.aurora.puremanager.imageloader;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultConfigurationFactory {
	public static Executor createExecutor(
			int threadPoolSize, 
			int threadPriority) {
		BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>() ;
		return new ThreadPoolExecutor(threadPoolSize, 
				threadPoolSize, 
				0, 
				TimeUnit.MILLISECONDS, 
				taskQueue, 
				createThreadFactory(threadPriority));
	}

	private static ThreadFactory createThreadFactory(int threadPriority) {
		return new DefaultThreadFactory(threadPriority);
	}

	private static class DefaultThreadFactory implements ThreadFactory {
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority) {
			this.threadPriority = threadPriority;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon()) t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}
}

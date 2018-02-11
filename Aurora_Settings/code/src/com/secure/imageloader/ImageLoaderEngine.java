package com.secure.imageloader;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReentrantLock;

class ImageLoaderEngine {
	final ImageLoaderConfiguration configuration;
	private Executor taskExecutor;	
	private final Map<Object, ReentrantLock> uriLocks = new WeakHashMap<Object, ReentrantLock>();

	ImageLoaderEngine(ImageLoaderConfiguration configuration) {
		this.configuration = configuration;
		taskExecutor = configuration.taskExecutor;
	}

	/** Submits task to execution pool */
	void submit(final LoadImageTask task) {	
		initExecutorsIfNeed();
		taskExecutor.execute(task);
	}

	private void initExecutorsIfNeed() {
		if (!configuration.customExecutor && ((ExecutorService) taskExecutor).isShutdown()) {
			taskExecutor = createTaskExecutor();
		}		
	}

	private Executor createTaskExecutor() {
		return DefaultConfigurationFactory.createExecutor(configuration.threadPoolSize, 
				configuration.threadPriority);
	}

	/** Stops engine, cancels all running and scheduled display image tasks. Clears internal data. */
	void stop() {
		if (!configuration.customExecutor) {
			((ExecutorService) taskExecutor).shutdownNow();
		}
		uriLocks.clear();
	}
	
	ReentrantLock getLockForUri(Object uri) {
		ReentrantLock lock = uriLocks.get(uri);
		if (lock == null) {
			lock = new ReentrantLock();
			uriLocks.put(uri, lock);
		}
		return lock;
	}
}

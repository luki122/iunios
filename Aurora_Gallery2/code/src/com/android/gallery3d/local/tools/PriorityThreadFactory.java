package com.android.gallery3d.local.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityThreadFactory implements ThreadFactory {
	static final AtomicInteger poolNumber = new AtomicInteger(1);  
	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(r);
		t.setName("local_thread_pool_"+poolNumber.getAndIncrement());
		t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

}

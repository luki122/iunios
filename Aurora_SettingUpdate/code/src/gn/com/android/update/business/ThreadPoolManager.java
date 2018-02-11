package gn.com.android.update.business;

import gn.com.android.update.business.job.Job;
import gn.com.android.update.utils.LogUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadPoolManager {
    private static final String TAG = "ThreadPoolManager";

    private Map<Integer, Future<?>> mTaskMap = new HashMap<Integer, Future<?>>();
    private Map<Integer, Job> mJobMap = new HashMap<Integer, Job>();
    private static final int THREADPOOL_SIZE = 5;

    private volatile static ThreadPoolManager sInstance = null;

    private ExecutorService mExecutorService;

    private ThreadPoolManager() {
        LogUtils.logd(TAG, "ThreadPoolManager");
        mExecutorService = Executors.newFixedThreadPool(THREADPOOL_SIZE);
    }

    public static ThreadPoolManager getInstance() {
        if (sInstance == null) {
            synchronized (ThreadPoolManager.class) {
                if (sInstance == null) {
                    sInstance = new ThreadPoolManager();
                }
            }
        }
        return sInstance;
    }

    public void submitTask(Job job) {

        Integer hashCode = job.hashCode();
        LogUtils.logd(TAG, "submitTask() hashCode = " + hashCode);
        Future<?> future = mExecutorService.submit(job);
        synchronized (mTaskMap) {
            mTaskMap.put(hashCode, future);
        }

        synchronized (mJobMap) {
            mJobMap.put(hashCode, job);
        }
    }

    public void removeCompleteTask(Job job) {
        int hashCode = job.hashCode();
        LogUtils.logd(TAG, "removeTask() hashCode = " + hashCode);
        synchronized (mTaskMap) {
            mTaskMap.remove(hashCode);
        }
        synchronized (mJobMap) {
            mJobMap.remove(hashCode);
        }
    }

    public void stopTask(Job job) {
        int hashCode = job.hashCode();
        LogUtils.logd(TAG, "stopTask() hashCode = " + hashCode);

        synchronized (mTaskMap) {
            if (mTaskMap.containsKey(hashCode)) {
                boolean result = mTaskMap.get(hashCode).cancel(false);
                LogUtils.logd(TAG, "stopTask() result = " + result);
                mTaskMap.remove(hashCode);
            } else {
                LogUtils.loge(TAG, "stopTask() task already removed");
            }
        }

        synchronized (mJobMap) {
            if (mJobMap.containsKey(hashCode)) {
                mJobMap.get(hashCode).cancel();
                mJobMap.remove(hashCode);
            } else {
                LogUtils.loge(TAG, "stopTask() task already removed");
            }
        }
    }

    public void removeAllTask() {
        synchronized (mTaskMap) {
            Collection<Future<?>> futures = mTaskMap.values();
            for (Future<?> future : futures) {
                future.cancel(false);
            }
            mTaskMap.clear();
        }
        synchronized (mJobMap) {
            Collection<Job> jobs = mJobMap.values();
            for (Job job : jobs) {
                job.cancel();
            }
            mJobMap.clear();
        }
    }

    public void stop() {
//        mExecutorService.shutdown();
//        mExecutorService = null;
//        sInstance = null;
    }

    public boolean isStop() {
        if (mExecutorService == null) {
            return true;
        }
        return mExecutorService.isShutdown();
    }

}

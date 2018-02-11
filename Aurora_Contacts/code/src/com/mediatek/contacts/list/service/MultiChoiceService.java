
package com.mediatek.contacts.list.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import com.android.contacts.ContactsApplication;
import com.android.contacts.vcard.ProcessorBase;

import android.accounts.Account;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * The class responsible for handling all of multiple choice requests. This
 * Service creates one MultiChoiceRequest object (as Runnable) per request and
 * push it to {@link ExecutorService} with single thread executor. The executor
 * handles each request one by one, and notifies users when needed.
 */
public class MultiChoiceService extends Service {

    private final static String TAG = MultiChoiceService.class.getSimpleName();

    public final static boolean DEBUG = true;

    // Should be single thread, as we don't want to simultaneously handle import
    // and export
    // requests.
    private final ExecutorService mExecutorService = ContactsApplication.getInstance().singleTaskService;

    // Stores all unfinished import/export jobs which will be executed by
    // mExecutorService.
    // Key is jobId.
    private static final Map<Integer, ProcessorBase> mRunningJobMap = new HashMap<Integer, ProcessorBase>();

    public static final int TYPE_COPY = 1;
    public static final int TYPE_DELETE = 2;

    private static int mCurrentJobId = 0;

    private MyBinder mBinder;

    public class MyBinder extends Binder {
        public MultiChoiceService getService() {
            return MultiChoiceService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new MyBinder();
        if (DEBUG)
            Log.d(TAG, "Multi-choice Service is being created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int id) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Tries to call {@link ExecutorService#execute(Runnable)} toward a given
     * processor.
     * 
     * @return true when successful.
     */
    private synchronized boolean tryExecute(ProcessorBase processor) {
        try {
            if (DEBUG) {
                Log.d(TAG, "Executor service status: shutdown: " + mExecutorService.isShutdown()
                        + ", terminated: " + mExecutorService.isTerminated());
            }
            mExecutorService.execute(processor);
            mRunningJobMap.put(mCurrentJobId, processor);
            return true;
        } catch (RejectedExecutionException e) {
            Log.w(TAG, "Failed to excetute a job.", e);
            return false;
        }
    }

    public synchronized void handleDeleteRequest(List<MultiChoiceRequest> requests,
            MultiChoiceHandlerListener listener) {

        mCurrentJobId++;
        if (tryExecute(new DeleteProcessor(this, listener, requests, mCurrentJobId))) {
            if (listener != null) {
                listener.onProcessed(TYPE_DELETE, mCurrentJobId, 0, -1, requests
                        .get(0).mContactName);
            }
        }
    }

    public synchronized void handleCopyRequest(List<MultiChoiceRequest> requests,
            MultiChoiceHandlerListener listener, final Account sourceAccount,
            final Account destinationAccount) {

        mCurrentJobId++;
        if (tryExecute(new CopyProcessor(this, listener, requests, mCurrentJobId, sourceAccount,
                destinationAccount))) {
            if (listener != null) {
                listener.onProcessed(TYPE_COPY, mCurrentJobId, 0, -1,
                        requests.get(0).mContactName);
            }
        }
    }

    public synchronized void handleCancelRequest(MultiChoiceCancelRequest request) {
        final int jobId = request.jobId;
        if (DEBUG)
            Log.d(TAG, String.format("Received cancel request. (id: %d)", jobId));
        final ProcessorBase processor = mRunningJobMap.remove(jobId);

        if (processor != null) {
            processor.cancel(true);
        } else {
            Log.w(TAG, String.format("Tried to remove unknown job (id: %d)", jobId));
        }
        stopServiceIfAppropriate();
    }

    /**
     * Checks job list and call {@link #stopSelf()} when there's no job and no
     * scanner connection is remaining. A new job (import/export) cannot be
     * submitted any more after this call.
     */
    private synchronized void stopServiceIfAppropriate() {
        if (mRunningJobMap.size() > 0) {
            for (final Map.Entry<Integer, ProcessorBase> entry : mRunningJobMap.entrySet()) {
                final int jobId = entry.getKey();
                final ProcessorBase processor = entry.getValue();
                if (processor.isDone()) {
                    mRunningJobMap.remove(jobId);
                } else {
                    Log.i(TAG, String.format("Found unfinished job (id: %d)", jobId));
                    return;
                }
            }
        }

        Log.i(TAG, "No unfinished job. Stop this service.");
        //mExecutorService.shutdown();
        stopSelf();
    }

    public synchronized void handleFinishNotification(int jobId, boolean successful) {
        if (DEBUG) {
            Log.d(TAG, String.format("Received handle finish notification (id: %d). "
                    + "Result: %b", jobId, (successful ? "success" : "failure")));
        }
        if (mRunningJobMap.remove(jobId) == null) {
            Log.w(TAG, String.format("Tried to remove unknown job (id: %d)", jobId));
        }
        stopServiceIfAppropriate();
    }

    public static synchronized boolean isProcessing(int requestType) {
        if (mRunningJobMap.size() <= 0) {
            return false;
        }

        if (mRunningJobMap.size() > 0) {
            for (final Map.Entry<Integer, ProcessorBase> entry : mRunningJobMap.entrySet()) {
                final ProcessorBase processor = entry.getValue();
                if (processor.getType() == requestType) {
                    return true;
                }
            }
        }

        return false;
    }
}

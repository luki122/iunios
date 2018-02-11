package gn.com.android.update.business.job;

import gn.com.android.update.business.IBaseCallback;
import gn.com.android.update.business.ThreadPoolManager;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.MSG;
import gn.com.android.update.utils.Util;
import android.os.Handler;
import android.os.Message;

public abstract class Job implements Runnable {
    private String mTag = "job";
    protected State mStatus = State.STATUS_IDLE;
    protected int mErrorCode = NO_ERROR;
    protected int mProgress = 0;
    private JobCompleteListener mListener = null;
    protected Object mResultObject;
    protected static final int NO_ERROR = 0;
    protected boolean mCanceled = false;
    private static InnerHandler sHandler = new InnerHandler();
    public static final int JOB_COMPLETE_STATE_SUCCESSFULE = 1;
    public static final int JOB_COMPLETE_STATE_ERROE = 2;
    public static final int JOB_COMPLETE_STATE_CANCELED = 3;
    private JobEventListener mEventListener = null;

    private class JobEventHolder {
        public Job mJob;
        public JobEvent mJobEvent;

        public JobEventHolder(Job job, JobEvent jobEvent) {
            mJob = job;
            mJobEvent = jobEvent;
        }
    }

    public static class JobEvent {
        public JobEventType mEventType;
        public int mEventContent;

        public JobEvent(JobEventType eventType, int eventContent) {
            mEventType = eventType;
            mEventContent = eventContent;
        }
    }

    public static enum JobEventType {
        EVENT_ERROE, EVENT_UPDATE_PROGRESS, EVENT_DOWNLOAD_BEGIN, EVENT_DOWNLOAD_END;
    }

    public interface JobEventListener {
        void onJobEvent(JobEvent jobEvent);
    }

    private static class InnerHandler extends Handler {

        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG.NOTIFY_JOB_COMPLETE:
                    Job job = (Job) (msg.obj);
                    LogUtils.logd("InnerHandler", "handleMessage NOTIFY_JOB_COMPLETE " + job.getTag());
                    job.onResult();
                    break;
                case MSG.NOTIFY_JOB_EVENT:
                    JobEventHolder holder = (JobEventHolder) msg.obj;
                    holder.mJob.onEvent(holder.mJobEvent);
                    break;
                default:
                    break;
            }
        }
    }

    protected static enum State {
        STATUS_IDLE, STATUS_RUNNING, STATUS_ERROR, STATUS_COMPLETE, STATUS_CANCELED;
    }

    public interface JobCompleteListener {
        void onComplete(Job job, int state, int errorCode, Object resultObject);
    }

    public Job() {
    }

    public Job(String tag) {
        mTag = tag;
    }

    public void run() {
        synchronized (this) {
            mCanceled = false;
            mStatus = State.STATUS_RUNNING;
        }
        try {
            runTask();

            setStatus();

            notifyManager();

            complete();

        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        mCanceled = true;
        cancelTask();
        logd("cancel");
    }

    private void setStatus() {
        synchronized (this) {
            if (mCanceled) {
                mStatus = State.STATUS_CANCELED;

            } else {
                if (mErrorCode != NO_ERROR) {
                    mStatus = State.STATUS_ERROR;
                } else {
                    mStatus = State.STATUS_COMPLETE;
                }
            }
            logd("setStatus() mStatus = " + mStatus);
        }
    }

    public abstract void runTask();

    public abstract void registerCallback(IBaseCallback callback);

    public abstract void unRegisterCallback();

    public abstract void onResult();

    public void registerJobCompleteListener(JobCompleteListener jobCompleteListener) {
        mListener = jobCompleteListener;
    }

    public void unregisterJobCompleteListener() {
        logd("unregisterListener");
        mListener = null;
    }

    public void cancelTask() {
    }

    protected void complete() {
        ThreadPoolManager.getInstance().removeCompleteTask(this);
        Message message = sHandler.obtainMessage(MSG.NOTIFY_JOB_COMPLETE);
        message.obj = this;
        message.sendToTarget();
    }

    protected void notifyManager() {
        if (mListener == null) {
            LogUtils.logd(mTag, "notifyManager  mListener is null ");
            return;
        }

        switch (mStatus) {
            case STATUS_ERROR:
                mListener.onComplete(this, JOB_COMPLETE_STATE_ERROE, mErrorCode, null);
                break;
            case STATUS_COMPLETE:
                mListener.onComplete(this, JOB_COMPLETE_STATE_SUCCESSFULE, NO_ERROR, mResultObject);

                break;
            case STATUS_CANCELED:
                mListener.onComplete(this, JOB_COMPLETE_STATE_CANCELED, NO_ERROR, null);
                break;
            default:
                break;
        }
    }

    protected void logd(String msg) {
        LogUtils.logd(mTag, "thread id = " + Util.getThreadId() + "-" + msg);
    }

    protected void loge(String msg) {
        LogUtils.loge(mTag, "thread id = " + Util.getThreadId() + "-" + msg);
    }

    protected void sendEvent(JobEvent event) {
        if (mCanceled) {
            loge("sendEvent already cancel");
            return;
        }
        Message message = sHandler.obtainMessage(MSG.NOTIFY_JOB_EVENT);
        message.obj = new JobEventHolder(this, event);
        message.sendToTarget();
    }

    protected void onEvent(JobEvent event) {
        if (mCanceled) {
            loge("onEvent already canceled");
            return;
        }
        if (mEventListener != null) {
            mEventListener.onJobEvent(event);
        }

    }

    public void registerJobEventListener(JobEventListener jobEventListener) {
        if (jobEventListener == null) {
            return;
        }
        mEventListener = jobEventListener;
    }

    public void unregisterJobEventListener() {
        mEventListener = null;
    }

    public String getTag() {
        return mTag;
    }
}

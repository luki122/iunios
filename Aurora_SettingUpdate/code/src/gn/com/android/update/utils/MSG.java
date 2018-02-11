package gn.com.android.update.utils;

public final class MSG {

    public static final int BASE_ID = 0;

    private static final int NOTIFY_BASE = BASE_ID + 200;
    public static final int NOTIFY_JOB_PROGRESS = NOTIFY_BASE + 1;
    public static final int NOTIFY_JOB_COMPLETE = NOTIFY_BASE + 2;
    public static final int NOTIFY_JOB_EVENT = NOTIFY_BASE + 3;
    public static final int NOTIFY_ACTIVITY_CREATE = NOTIFY_BASE + 4;
    public static final int NOTIFY_LOW_DOWNLOAD_SPEED = NOTIFY_BASE + 5;

    private static final int REQ_BASE = BASE_ID + 300;
    public static final int REQ_START_WORK = REQ_BASE + 2;

}

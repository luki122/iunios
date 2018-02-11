package gn.com.android.update.business;

import gn.com.android.update.utils.Util;

public class Config {
    public static final String UNDER_LINE_STRING = "_";
    public static final String SETTING_STRING = "setting";

    public static final int INC_VERSION = 1;
    public static final int ERROR_PUSH_ID = -1;

    public static final int DOWNLOAD_NOTIFICATION_ID = 10001;
    public static final int VERSION_NOTIFICATION_ID = 10002;

    public static final long DEFAULT_NEXT_DOWNLOAD_JOB_DELAYED_TIME = 4000;

    public static final boolean GEMINI_SUPPORT = Util.isGeminiSupport();

    public static final boolean IS_MTK_PLATFORM = Util.isMtkPlatform();

    public static final int DEFAULT_MAX_PROGRESS = 100;
    public static final long ERROR_DOWNLOAD_ID = -1;
    public static final int MIN_STORAGE_SPACE = 10 * 1024 * 1024;

    public static final boolean PUSH_SUPPORT = true;

    public static final int AUTO_DOWNLOAD_BEGIN_MIN_BATTERY_LEVEl = 60;
    public static final int AUTO_DOWNLOAD_CONNITUE_NEED_MIN_BATTERY_LEVEl = 20;

    public static final long RESUME_INITIAL_STATE_DEFAULT_CYCLE = 24 * 60 * 60 * 1000;

    public static final String DATE_FORMAT_BY_DAY = "yyyy.MM.dd";
    public static final int CHARGE = 15;

}

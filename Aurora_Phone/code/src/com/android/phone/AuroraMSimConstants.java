package com.android.phone;


public class AuroraMSimConstants {

    public static final int DEFAULT_SUBSCRIPTION = 0;

    public static final int INVALID_SUBSCRIPTION = -1;

    public static final int RIL_CARD_MAX_APPS    = 8;

    public static final int DEFAULT_CARD_INDEX   = 0;

    public static final int MAX_PHONE_COUNT_SINGLE_SIM = 1;

    public static final int MAX_PHONE_COUNT_DUAL_SIM = 2;

    public static final int MAX_PHONE_COUNT_TRI_SIM = 3;

    public static final String SUBSCRIPTION_KEY  = "subscription";

    public static final int SUB1 = 0;
    public static final int SUB2 = 1;
    public static final int SUB3 = 2;

    public static final int EVENT_SUBSCRIPTION_ACTIVATED   = 500;
    public static final int EVENT_SUBSCRIPTION_DEACTIVATED = 501;

    public enum CardUnavailableReason {
        REASON_CARD_REMOVED,
        REASON_RADIO_UNAVAILABLE,
        REASON_SIM_REFRESH_RESET
    };
}

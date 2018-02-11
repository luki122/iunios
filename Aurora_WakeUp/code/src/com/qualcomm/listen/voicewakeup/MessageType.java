/*
 * Copyright (c) 2013 Qualcomm Technologies, Inc.  All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.qualcomm.listen.voicewakeup;

public final class MessageType {
	// Service-client management
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;

    // New values
    public static final int MSG_LMCGETINSTANCE_FAILED = 0;
    public static final int MSG_REGISTER_SOUNDMODEL = 10;
    public static final int MSG_DEREGISTER_SOUNDMODEL = 11;
    public static final int MSG_EXTEND_SOUNDMODEL = 12;
    public static final int MSG_DETECT_SUCCEEDED = 13;
    public static final int MSG_DETECT_FAILED = 14;
    public static final int MSG_VERIFY_RECORDING = 15;
    public static final int MSG_RECORDING_RESULT = 16;
    public static final int MSG_LISTEN_RUNNING = 17;
    public static final int MSG_LISTEN_STOPPED = 18;
    public static final int MSG_CLOSE_VWUSESSION = 19;

    // For the SettingsActivity
    public static final int MSG_ENABLE = 20;
    public static final int MSG_DISABLE = 21;
    public static final int MSG_LISTEN_GET_PARAM = 22;
    public static final int MSG_LISTEN_SET_PARAM = 23;
    public static final int MSG_LISTEN_ENABLED = 24;
    public static final int MSG_LISTEN_DISABLED = 25;
    public static final int MSG_VOICEWAKEUP_GET_PARAM = 26;
    public static final int MSG_VOICEWAKEUP_SET_PARAM = 27;
    public static final int MSG_VOICEWAKEUP_ENABLED = 28;
    public static final int MSG_VOICEWAKEUP_DISABLED = 29;

    // Testing
    public static final int MSG_TEST_ONE = 101;
    public static final int MSG_TEST_TWO = 102;

}

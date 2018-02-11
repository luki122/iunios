package com.android.phone;
import android.telephony.DisconnectCause;
import com.android.internal.telephony.Connection;

public class AuroraDisconnectCause {
	public int mCause = DisconnectCause.NOT_DISCONNECTED;
	public AuroraDisconnectCause(Connection c) {
		mCause = c.getDisconnectCause();
	} 
	
	
	public static int  NOT_DISCONNECTED = DisconnectCause.NOT_DISCONNECTED;
	public static int  INCOMING_MISSED = DisconnectCause.INCOMING_MISSED;
	public static int  NORMAL = DisconnectCause.NORMAL;
	public static int  LOCAL = DisconnectCause.LOCAL;
	public static int  BUSY = DisconnectCause.BUSY;
	public static int  CONGESTION = DisconnectCause.CONGESTION;
	public static int  MMI = DisconnectCause.MMI;
	public static int  INVALID_NUMBER = DisconnectCause.INVALID_NUMBER;
	public static int  NUMBER_UNREACHABLE = DisconnectCause.NUMBER_UNREACHABLE;
	public static int  SERVER_UNREACHABLE = DisconnectCause.SERVER_UNREACHABLE;
	public static int  INVALID_CREDENTIALS = DisconnectCause.INVALID_CREDENTIALS;
	public static int  OUT_OF_NETWORK = DisconnectCause.OUT_OF_NETWORK;
	public static int  SERVER_ERROR = DisconnectCause.SERVER_ERROR;
	public static int  TIMED_OUT = DisconnectCause.TIMED_OUT;
	public static int  LOST_SIGNAL = DisconnectCause.LOST_SIGNAL;
	public static int  LIMIT_EXCEEDED = DisconnectCause.LIMIT_EXCEEDED;
	public static int  INCOMING_REJECTED = DisconnectCause.INCOMING_REJECTED;
	public static int  POWER_OFF = DisconnectCause.POWER_OFF;
	public static int  OUT_OF_SERVICE = DisconnectCause.OUT_OF_SERVICE;
	public static int  ICC_ERROR = DisconnectCause.ICC_ERROR;
	public static int  CALL_BARRED = DisconnectCause.CALL_BARRED;
	public static int  FDN_BLOCKED = DisconnectCause.FDN_BLOCKED;
	public static int  CS_RESTRICTED = DisconnectCause.CS_RESTRICTED;
	public static int  CS_RESTRICTED_NORMAL = DisconnectCause.CS_RESTRICTED_NORMAL;
	public static int  CS_RESTRICTED_EMERGENCY = DisconnectCause.CS_RESTRICTED_EMERGENCY;
	public static int  UNOBTAINABLE_NUMBER = DisconnectCause.UNOBTAINABLE_NUMBER;
//	public static int  DIAL_MODIFIED_TO_USSD = DisconnectCause.DIAL_MODIFIED_TO_USSD;
//	public static int  DIAL_MODIFIED_TO_SS = DisconnectCause.DIAL_MODIFIED_TO_SS;
//	public static int  DIAL_MODIFIED_TO_DIAL = DisconnectCause.DIAL_MODIFIED_TO_DIAL;
	public static int  CDMA_LOCKED_UNTIL_POWER_CYCLE = DisconnectCause.CDMA_LOCKED_UNTIL_POWER_CYCLE;
	public static int  CDMA_DROP = DisconnectCause.CDMA_DROP;
	public static int  CDMA_INTERCEPT = DisconnectCause.CDMA_INTERCEPT;
	public static int  CDMA_REORDER = DisconnectCause.CDMA_REORDER;
	public static int  CDMA_SO_REJECT = DisconnectCause.CDMA_SO_REJECT;
	public static int  CDMA_RETRY_ORDER = DisconnectCause.CDMA_RETRY_ORDER;
	public static int  CDMA_ACCESS_FAILURE = DisconnectCause.CDMA_ACCESS_FAILURE;
	public static int  CDMA_PREEMPTED = DisconnectCause.CDMA_PREEMPTED;
	public static int  CDMA_NOT_EMERGENCY = DisconnectCause.CDMA_NOT_EMERGENCY;
	public static int  CDMA_ACCESS_BLOCKED = DisconnectCause.CDMA_ACCESS_BLOCKED;
//	public static int  EMERGENCY_TEMP_FAILURE = DisconnectCause.EMERGENCY_TEMP_FAILURE;
	
//	public static int  EMERGENCY_PERM_FAILURE = DisconnectCause.EMERGENCY_PERM_FAILURE;
	public static int  ERROR_UNSPECIFIED = DisconnectCause.ERROR_UNSPECIFIED;
//	public static int  SRVCC_CALL_DROP = DisconnectCause.SRVCC_CALL_DROP;
//	public static int  ANSWERED_ELSEWHERE = DisconnectCause.ANSWERED_ELSEWHERE;
//	public static int  CALL_FAIL_MISC = DisconnectCause.CALL_FAIL_MISC;

}
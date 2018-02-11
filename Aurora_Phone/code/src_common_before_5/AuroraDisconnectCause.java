package com.android.phone;
import com.android.internal.telephony.Connection.DisconnectCause;
import com.android.internal.telephony.Connection;

public class AuroraDisconnectCause {
	public DisconnectCause mCause = DisconnectCause.NOT_DISCONNECTED;
	public AuroraDisconnectCause(Connection c) {
		mCause = c.getDisconnectCause();
	} 	
	
	public String toString() {
		return mCause.toString();
	}
	
	public static DisconnectCause  NOT_DISCONNECTED = DisconnectCause.NOT_DISCONNECTED;
	public static DisconnectCause  INCOMING_MISSED = DisconnectCause.INCOMING_MISSED;
	public static DisconnectCause  NORMAL = DisconnectCause.NORMAL;
	public static DisconnectCause  LOCAL = DisconnectCause.LOCAL;
	public static DisconnectCause  BUSY = DisconnectCause.BUSY;
	public static DisconnectCause  CONGESTION = DisconnectCause.CONGESTION;
	public static DisconnectCause  MMI = DisconnectCause.MMI;
	public static DisconnectCause  INVALID_NUMBER = DisconnectCause.INVALID_NUMBER;
	public static DisconnectCause  NUMBER_UNREACHABLE = DisconnectCause.NUMBER_UNREACHABLE;
	public static DisconnectCause  SERVER_UNREACHABLE = DisconnectCause.SERVER_UNREACHABLE;
	public static DisconnectCause  INVALID_CREDENTIALS = DisconnectCause.INVALID_CREDENTIALS;
	public static DisconnectCause  OUT_OF_NETWORK = DisconnectCause.OUT_OF_NETWORK;
	public static DisconnectCause  SERVER_ERROR = DisconnectCause.SERVER_ERROR;
	public static DisconnectCause  TIMED_OUT = DisconnectCause.TIMED_OUT;
	public static DisconnectCause  LOST_SIGNAL = DisconnectCause.LOST_SIGNAL;
	public static DisconnectCause  LIMIT_EXCEEDED = DisconnectCause.LIMIT_EXCEEDED;
	public static DisconnectCause  INCOMING_REJECTED = DisconnectCause.INCOMING_REJECTED;
	public static DisconnectCause  POWER_OFF = DisconnectCause.POWER_OFF;
	public static DisconnectCause  OUT_OF_SERVICE = DisconnectCause.OUT_OF_SERVICE;
	public static DisconnectCause  ICC_ERROR = DisconnectCause.ICC_ERROR;
	public static DisconnectCause  CALL_BARRED = DisconnectCause.CALL_BARRED;
	public static DisconnectCause  FDN_BLOCKED = DisconnectCause.FDN_BLOCKED;
	public static DisconnectCause  CS_RESTRICTED = DisconnectCause.CS_RESTRICTED;
	public static DisconnectCause  CS_RESTRICTED_NORMAL = DisconnectCause.CS_RESTRICTED_NORMAL;
	public static DisconnectCause  CS_RESTRICTED_EMERGENCY = DisconnectCause.CS_RESTRICTED_EMERGENCY;
	public static DisconnectCause  UNOBTAINABLE_NUMBER = DisconnectCause.UNOBTAINABLE_NUMBER;
//	public static DisconnectCause  DIAL_MODIFIED_TO_USSD = DisconnectCause.DIAL_MODIFIED_TO_USSD;
//	public static DisconnectCause  DIAL_MODIFIED_TO_SS = DisconnectCause.DIAL_MODIFIED_TO_SS;
//	public static DisconnectCause  DIAL_MODIFIED_TO_DIAL = DisconnectCause.DIAL_MODIFIED_TO_DIAL;
	public static DisconnectCause  CDMA_LOCKED_UNTIL_POWER_CYCLE = DisconnectCause.CDMA_LOCKED_UNTIL_POWER_CYCLE;
	public static DisconnectCause  CDMA_DROP = DisconnectCause.CDMA_DROP;
	public static DisconnectCause  CDMA_INTERCEPT = DisconnectCause.CDMA_INTERCEPT;
	public static DisconnectCause  CDMA_REORDER = DisconnectCause.CDMA_REORDER;
	public static DisconnectCause  CDMA_SO_REJECT = DisconnectCause.CDMA_SO_REJECT;
	public static DisconnectCause  CDMA_RETRY_ORDER = DisconnectCause.CDMA_RETRY_ORDER;
	public static DisconnectCause  CDMA_ACCESS_FAILURE = DisconnectCause.CDMA_ACCESS_FAILURE;
	public static DisconnectCause  CDMA_PREEMPTED = DisconnectCause.CDMA_PREEMPTED;
	public static DisconnectCause  CDMA_NOT_EMERGENCY = DisconnectCause.CDMA_NOT_EMERGENCY;
	public static DisconnectCause  CDMA_ACCESS_BLOCKED = DisconnectCause.CDMA_ACCESS_BLOCKED;
//	public static DisconnectCause  EMERGENCY_TEMP_FAILURE = DisconnectCause.EMERGENCY_TEMP_FAILURE;
	
//	public static DisconnectCause  EMERGENCY_PERM_FAILURE = DisconnectCause.EMERGENCY_PERM_FAILURE;
	public static DisconnectCause  ERROR_UNSPECIFIED = DisconnectCause.ERROR_UNSPECIFIED;
//	public static DisconnectCause  SRVCC_CALL_DROP = DisconnectCause.SRVCC_CALL_DROP;
//	public static DisconnectCause  ANSWERED_ELSEWHERE = DisconnectCause.ANSWERED_ELSEWHERE;
//	public static DisconnectCause  CALL_FAIL_MISC = DisconnectCause.CALL_FAIL_MISC;

}
package com.android.phone;

import android.content.Context;
import android.util.Log;
import aurora.preference.*;
import aurora.app.*;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneConstants;

public class AuroraNetworkUtils {
	private static final String LOG_TAG = "AuroraNetworkUtils";

	private static final int preferredNetworkMode = Phone.PREFERRED_NT_MODE;
	public static final int NT_MODE_WCDMA_PREF = Constants.NETWORK_MODE_WCDMA_PREF;
	public static final int NT_MODE_GSM_ONLY = Constants.NETWORK_MODE_GSM_ONLY;
	public static final int NT_MODE_WCDMA_ONLY = Constants.NETWORK_MODE_WCDMA_ONLY;
	public static final int NT_MODE_GSM_UMTS = Constants.NETWORK_MODE_GSM_UMTS;

	public static final int NT_MODE_CDMA = Constants.NETWORK_MODE_CDMA;

	public static final int NT_MODE_CDMA_NO_EVDO = Constants.NETWORK_MODE_CDMA_NO_EVDO;
	public static final int NT_MODE_EVDO_NO_CDMA = Constants.NETWORK_MODE_EVDO_NO_CDMA;
	public static final int NT_MODE_GLOBAL = Constants.NETWORK_MODE_GLOBAL;

	public static final int NT_MODE_LTE_CDMA_AND_EVDO = Constants.NETWORK_MODE_LTE_CDMA_EVDO;
	public static final int NT_MODE_LTE_GSM_WCDMA = Constants.NETWORK_MODE_LTE_GSM_WCDMA;
	public static final int NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA = Constants.NETWORK_MODE_LTE_CMDA_EVDO_GSM_WCDMA;
	public static final int NT_MODE_LTE_ONLY = Constants.NETWORK_MODE_LTE_ONLY;
	public static final int NT_MODE_LTE_WCDMA = Constants.NETWORK_MODE_LTE_WCDMA;
	public static final int PREFERRED_NT_MODE = Constants.PREFERRED_NETWORK_MODE;

	public static final int NT_MODE_TD_SCDMA_ONLY = Constants.NETWORK_MODE_TD_SCDMA_ONLY;
	public static final int NT_MODE_TD_SCDMA_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_WCDMA;
	public static final int NT_MODE_TD_SCDMA_LTE = Constants.NETWORK_MODE_TD_SCDMA_LTE;
	public static final int NT_MODE_TD_SCDMA_GSM = Constants.NETWORK_MODE_TD_SCDMA_GSM;
	public static final int NT_MODE_TD_SCDMA_GSM_LTE = Constants.NETWORK_MODE_TD_SCDMA_GSM_LTE;
	public static final int NT_MODE_TD_SCDMA_GSM_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA;
	public static final int NT_MODE_TD_SCDMA_WCDMA_LTE = Constants.NETWORK_MODE_TD_SCDMA_WCDMA_LTE;
	public static final int NT_MODE_TD_SCDMA_GSM_WCDMA_LTE = Constants.NETWORK_MODE_TD_SCDMA_GSM_WCDMA_LTE;
	public static final int NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA;
	public static final int NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA = Constants.NETWORK_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA;

	public static int getPreferredNetworkMode(int sub) {

		int nwMode;

		nwMode = android.provider.Settings.Global.getInt(
				PhoneGlobals.getInstance().getContentResolver(),
				android.provider.Settings.Global.PREFERRED_NETWORK_MODE
						+ AuroraSubUtils.getSubIdbySlot(
								PhoneGlobals.getInstance(), sub),
				preferredNetworkMode);

		return nwMode;
	}
	

	public static int convertNetworkMode(int nwMode) {
		if(nwMode == 3) {
			return 0;
		} else {
			return nwMode;
		}	
	}
	

	public static void setPreferredNetworkMode(int nwMode, int sub) {

		android.provider.Settings.Global.putInt(
				PhoneGlobals.getInstance().getContentResolver(),
				android.provider.Settings.Global.PREFERRED_NETWORK_MODE
						+ AuroraSubUtils.getSubIdbySlot(
								PhoneGlobals.getInstance(), sub), nwMode);
	}

	public static int getPreferredNetworkStringId(int sub) {
		int NetworkMode = getPreferredNetworkMode(sub);
		switch (NetworkMode) {
		case 0:
		case 3:
			return R.string.aurora_preferred_network_mode_choices_3p;
		case 1:
			return R.string.aurora_preferred_network_mode_choices_2;
		case 2:
			return R.string.aurora_preferred_network_mode_choices_3;
		case 9:
			return R.string.aurora_preferred_network_mode_choices_4p;
		default:
			return R.string.aurora_preferred_network_mode_choices_4p;

		}
	}
	
	public static int getCdmaPreferredNetworkStringId(int mode) {
	
		switch (mode) {
		case 48:
			return R.string.aurora_preferred_network_mode_choices_3p;
		case 112:
			return R.string.aurora_preferred_network_mode_choices_4p;
		default:
			return R.string.aurora_preferred_network_mode_choices_4p;

		}
	}

	public static void UpdatePreferredNetworkModeSummary(AuroraPreference ap,
			int NetworkMode) {

		ap.setSummary("");
		
		
//		if (PhoneUtils.isMtk()) {
//			switch (NetworkMode) {
//			case NT_MODE_GSM_UMTS:
//				ap.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
//				break;
//			case NT_MODE_WCDMA_ONLY:
//				ap.setSummary(R.string.aurora_preferred_network_mode_choices_3);
//				break;
//			case NT_MODE_GSM_ONLY:
//				ap.setSummary(R.string.aurora_preferred_network_mode_choices_2);
//				break;
//			default:
//				ap.setSummary(R.string.aurora_preferred_network_mode_choices_3p);
//			}
//			return;
//		}
//
//		switch (NetworkMode) {
//		case NT_MODE_WCDMA_PREF:
//			ap.setSummary(R.string.preferred_network_mode_wcdma_perf_summary);
//			break;
//		case NT_MODE_GSM_ONLY:
//			ap.setSummary(R.string.preferred_network_mode_gsm_only_summary);
//			break;
//		case NT_MODE_WCDMA_ONLY:
//			ap.setSummary(R.string.preferred_network_mode_wcdma_only_summary);
//			break;
//		case NT_MODE_GSM_UMTS:
//			ap.setSummary(R.string.preferred_network_mode_gsm_wcdma_summary);
//			break;
//		case NT_MODE_CDMA:
////			switch (mPhone.getLteOnCdmaMode()) {
////			case PhoneConstants.LTE_ON_CDMA_TRUE:
////				ap.setSummary(R.string.preferred_network_mode_cdma_summary);
////				break;
////			case PhoneConstants.LTE_ON_CDMA_FALSE:
////			default:
//				ap.setSummary(R.string.preferred_network_mode_cdma_evdo_summary);
//				break;
////			}
////			break;
//		case NT_MODE_CDMA_NO_EVDO:
//			ap.setSummary(R.string.preferred_network_mode_cdma_only_summary);
//			break;
//		case NT_MODE_EVDO_NO_CDMA:
//			ap.setSummary(R.string.preferred_network_mode_evdo_only_summary);
//			break;
//		case NT_MODE_LTE_ONLY:
//			ap.setSummary(R.string.preferred_network_mode_lte_summary);
//			break;
//		case NT_MODE_LTE_GSM_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_lte_gsm_wcdma_summary);
//			break;
//		case NT_MODE_LTE_CDMA_AND_EVDO:
//			ap.setSummary(R.string.preferred_network_mode_lte_cdma_evdo_summary);
//			break;
//		case NT_MODE_LTE_CMDA_EVDO_GSM_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_global_summary);
//			break;
//		case NT_MODE_GLOBAL:
//			ap.setSummary(R.string.preferred_network_mode_cdma_evdo_gsm_wcdma_summary);
//			break;
//		case NT_MODE_LTE_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_lte_wcdma_summary);
//			break;
//		case NT_MODE_TD_SCDMA_ONLY:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_only_summary);
//			break;
//		case NT_MODE_TD_SCDMA_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_wcdma_summary);
//			break;
//		case NT_MODE_TD_SCDMA_LTE:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_lte_summary);
//			break;
//		case NT_MODE_TD_SCDMA_GSM:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_gsm_summary);
//			break;
//		case NT_MODE_TD_SCDMA_GSM_LTE:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_gsm_lte_summary);
//			break;
//		case NT_MODE_TD_SCDMA_GSM_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_gsm_wcdma_summary);
//			break;
//		case NT_MODE_TD_SCDMA_WCDMA_LTE:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_wcdma_lte_summary);
//			break;
//		case NT_MODE_TD_SCDMA_GSM_WCDMA_LTE:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_gsm_wcdma_lte_summary);
//			break;
//		case NT_MODE_TD_SCDMA_CDMA_EVDO_GSM_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_cdma_evdo_gsm_wcdma_summary);
//			break;
//		case NT_MODE_TD_SCDMA_LTE_CDMA_EVDO_GSM_WCDMA:
//			ap.setSummary(R.string.preferred_network_mode_td_scdma_lte_cdma_evdo_gsm_wcdma_summary);
//			break;
//		default:
//			ap.setSummary(R.string.preferred_network_mode_global_summary);
//		}
	}

}
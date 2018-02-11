package com.android.phone;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.util.Log;
import com.android.phone.AuroraTelephony.SIMInfo;
import com.android.phone.AuroraTelephony.SimInfo;

import com.android.internal.telephony.Connection;

public class SimIconUtils {
	private static final String LOG_TAG = "SimIconUtils";

	public static int getSmallSimIcon(int slot) {
		int result = getSimIconBySlot(slot);

		return result;
	}


	public static int getSimIconNotification(int simId) {
		int result = -1;
		int slot = 0;

		slot = AuroraSubUtils.getSlotBySubId(PhoneGlobals.getInstance(), simId);
		if (slot == 0) {
			result = R.drawable.sim_noti_1;
		} else {
			result = R.drawable.sim_noti_2;
		}

		return result;
	}

	public static void setColorForSIM(int resId, int slot) {
		// int[] colors = new int[8];
		SIMInfo simInfo = SIMInfo.getSIMInfoBySlot(PhoneGlobals.getInstance(),
				slot);
		if (simInfo != null) {
			ContentValues valueColor1 = new ContentValues(1);
			valueColor1.put(SimInfo.COLOR, resId);
			PhoneGlobals
					.getInstance()
					.getContentResolver()
					.update(ContentUris.withAppendedId(SimInfo.CONTENT_URI,
							simInfo.mSimId), valueColor1, null, null);
			Log.v(LOG_TAG, "setColorForNewSIM SimInfo simColorRes is " + resId);
		}
	}

	public static int getNewCardSimColor(int slot) {
		Context ctx = PhoneGlobals.getInstance();
		int insertCount = SIMInfo.getInsertedSIMCount(ctx);
		if (insertCount > 1) {
			SIMInfo otherSimInfo = SIMInfo.getSIMInfoBySlot(ctx,
					AuroraPhoneUtils.getOtherSlot(slot));
			if (otherSimInfo.mColor == AuroraMSimConstants.SUB1) {
				return AuroraMSimConstants.SUB2;
			} else {
				return AuroraMSimConstants.SUB1;
			}
		} else {
			return slot;
		}
	}

	private static int getSimIconBySlot(int slot) {
		if (slot == 0) {
			return R.drawable.smallsim1;
		} else {
			return R.drawable.smallsim2;
		}
	}

	public static int getIncomingSimIcon(int slot) {
		if (slot > 0) {
			return R.drawable.incoming_sim_2;
		} else {
			return R.drawable.incoming_sim_1;
		}
	}

}
package com.gionee.framework;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

public class QCOMSIMInfo implements ISIMInfo {

    public long mSimId = 0L;
    public String mICCId;
    public static int mSimContactPhotoRes = 0;

    public String mDisplayName = "";
    public String mNumber = "";
    public int mDispalyNumberFormat = 1;
    public int mColor;
    public int mDataRoaming = 1;
    public int mSlot = 0;
    public int mSimBackgroundRes = 0;
    public int mWapPush = -1;

    public QCOMSIMInfo() {
    }

    /**
     * 
     * @param ctx
     * @return the array list of Current SIM Info
     */
    public List<SIMInfo> getInsertedSIMList(Context ctx) {
        ArrayList<SIMInfo> simList = new ArrayList<SIMInfo>();
        return simList;
    }

    /**
     * 
     * @param ctx
     * @return array list of all the SIM Info include what were used before
     */
    public List<SIMInfo> getAllSIMList(Context ctx) {
        ArrayList<SIMInfo> simList = new ArrayList<SIMInfo>();

        return simList;
    }

    /**
     * 
     * @param ctx
     * @param SIMId
     *            the unique SIM id
     * @return SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoById(Context ctx, long SIMId) {
        // int slotId = (int) SIMId - 1;

        return new SIMInfo();
    }

    /**
     * 
     * @param ctx
     * @param SIMName
     *            the Name of the SIM Card
     * @return SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoByName(Context ctx, String SIMName) {
        if (SIMName == null)
            return null;

        return null;
    }

    /**
     * @param ctx
     * @param cardSlot
     * @return The SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoBySlot(Context ctx, int cardSlot) {

        return new SIMInfo();

    }

    /**
     * @param ctx
     * @param iccid
     * @return The SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoByICCId(Context ctx, String iccid) {
        if (iccid == null)
            return null;
        Cursor cursor = null;//
        // ctx.getContentResolver().query(SimInfo.CONTENT_URI,
        // null, SimInfo.ICC_ID + "=?", new String[]{iccid}, null);
        // try {
        // if (cursor != null) {
        // if (cursor.moveToFirst()) {
        // return SIMInfo.fromCursor(cursor);
        // }
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        // return null;
        return new SIMInfo();
    }

    /**
     * @param ctx
     * @param SIMId
     * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
     */
    public int getSlotById(Context ctx, long SimId) {
        // if (SimId <= 0 ) return SimInfo.SLOT_NONE;
        // Cursor cursor =
        // ctx.getContentResolver().query(ContentUris.withAppendedId(SimInfo.CONTENT_URI,
        // SIMId),
        // new String[]{SimInfo.SLOT}, null, null, null);
        // try {
        // if (cursor != null) {
        // if (cursor.moveToFirst()) {
        // return cursor.getInt(0);
        // }
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        int slotId = (int) SimId - 1;
        return slotId;
    }

    /**
     * @param ctx
     * @param SIMName
     * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
     */
    public int getSlotByName(Context ctx, String SIMName) {
        // if (SIMName == null) return SimInfo.SLOT_NONE;
        // Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI,
        // new String[]{SimInfo.SLOT}, SimInfo.DISPLAY_NAME + "=?", new
        // String[]{SIMName}, null);
        // try {
        // if (cursor != null) {
        // if (cursor.moveToFirst()) {
        // return cursor.getInt(0);
        // }
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        // return SimInfo.SLOT_NONE;
        return 0;
    }

    /**
     * @param ctx
     * @return current SIM Count
     */
    public int getInsertedSIMCount(Context ctx) {
        // Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI,
        // null, SimInfo.SLOT + "!=" + SimInfo.SLOT_NONE, null, null);
        // try {
        // if (cursor != null) {
        // return cursor.getCount();
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        // return 0;
        int count = 0;
        // MSimTelephonyManager tm = MSimTelephonyManager.getDefault();
        // for (int i = 0; i < tm.getPhoneCount(); i++) {
        // // Gionee <fengjianyi><2013-03-21> modify for CR00773021 start
        // if (tm.hasIccCard(i) /*&& tm.getSimState(i) !=
        // TelephonyManager.SIM_STATE_DEACTIVATED*/) {
        // count++;
        // }
        // // Gionee <fengjianyi><2013-03-21> modify for CR00773021 end
        // }
        return count;

    }

    /**
     * @param ctx
     * @return the count of all the SIM Card include what was used before
     */
    public int getAllSIMCount(Context ctx) {
        // Cursor cursor = ctx.getContentResolver().query(SimInfo.CONTENT_URI,
        // null, null, null, null);
        // try {
        // if (cursor != null) {
        // return cursor.getCount();
        // }
        // } finally {
        // if (cursor != null) {
        // cursor.close();
        // }
        // }
        return 1;
    }

    public int getDefaultDataSubId() {
        return 0;
    }

}

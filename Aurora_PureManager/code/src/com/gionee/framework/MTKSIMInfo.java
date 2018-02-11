package com.gionee.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;

public class MTKSIMInfo implements ISIMInfo {

    public static final String CLASS_NAME = "android.telephony.SubscriptionManager";
    public static final String SUBINFO_CLASS_NAME = "android.telephony.SubInfoRecord";

    private static Class<?> mClass = FrameworkUtility.createClass(CLASS_NAME);
    private static Class<?> mSubinfoClass = FrameworkUtility.createClass(SUBINFO_CLASS_NAME);

    public MTKSIMInfo() {
    }

    /**
     * 
     * @param ctx
     * @return the array list of Current SIM Info
     */

    public List<SIMInfo> getInsertedSIMList(Context ctx) {
        if (mClass == null) {
            return null;
        }

        Object retObj = FrameworkUtility.invokeStaticMethod(mClass, "getActiveSubInfoList");
        if (retObj != null) {
            List<?> retObjs = (List<?>) retObj;
            List<SIMInfo> records = new ArrayList<SIMInfo>();
            for (Object obj : retObjs) {
                long subId = (Long) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "subId");
                int slotId = (Integer) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "slotId");
                String name = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "displayName");
                String num = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "number");
                String iccId = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "iccId");
                int color = (Integer) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "color");

                SIMInfo record = new SIMInfo();
                record.mSimId = subId;
                record.mSlot = slotId;
                record.mDisplayName = name;
                record.mNumber = num;
                record.mICCId = iccId;
                records.add(record);
            }

            Collections.sort(records, new Comparator<SIMInfo>() {
                @Override
                public int compare(SIMInfo a, SIMInfo b) {
                    if (a.mSlot < b.mSlot) {
                        return -1;
                    } else if (a.mSlot > b.mSlot) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            return records;
        }

        return null;
    }

    /**
     * 
     * @param ctx
     * @return array list of all the SIM Info include what were used before
     */
    public List<SIMInfo> getAllSIMList(Context ctx) {
        if (mClass == null) {
            return null;
        }

        Object retObj = FrameworkUtility.invokeStaticMethod(mClass, "getAllSubInfoList");
        if (retObj != null) {
            List<?> retObjs = (List<?>) retObj;
            List<SIMInfo> records = new ArrayList<SIMInfo>();
            for (Object obj : retObjs) {
                long subId = (Long) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "subId");
                int slotId = (Integer) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "slotId");
                String name = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "displayName");
                String num = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "number");
                String iccId = (String) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "iccId");
                int color = (Integer) FrameworkUtility.getFieldValue(mSubinfoClass, obj, "color");

                SIMInfo record = new SIMInfo();
                record.mSimId = subId;
                record.mSlot = slotId;
                record.mDisplayName = name;
                record.mNumber = num;
                record.mICCId = iccId;
                records.add(record);
            }

            Collections.sort(records, new Comparator<SIMInfo>() {
                @Override
                public int compare(SIMInfo a, SIMInfo b) {
                    if (a.mSlot < b.mSlot) {
                        return -1;
                    } else if (a.mSlot > b.mSlot) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            return records;
        }

        return null;
    }

    /**
     * 
     * @param ctx
     * @param SIMId
     *            the unique SIM id
     * @return SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoById(Context ctx, long SIMId) {
        if (mClass == null) {
            return null;
        }
        Object object = FrameworkUtility.invokeStaticMethod(mClass, "getSubInfoForSubscriber",
                new Class[] {long.class}, new Object[] {SIMId});
        if (object != null) {
            long subId = (Long) FrameworkUtility.getFieldValue(mSubinfoClass, object, "subId");
            int slotId = (Integer) FrameworkUtility.getFieldValue(mSubinfoClass, object, "slotId");
            SIMInfo record = new SIMInfo();
            record.mSimId = subId;
            record.mSlot = slotId;
            return record;
        }
        return null;
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
        List<SIMInfo> SubInfoRecords = getInsertedSIMList(ctx);
        for (SIMInfo record : SubInfoRecords) {
            if (record.mSlot == cardSlot) {
                return record;
            }
        }
        return null;
    }

    /**
     * @param ctx
     * @param iccid
     * @return The SIM-Info, maybe null
     */
    public SIMInfo getSIMInfoByICCId(Context ctx, String iccid) {
        List<SIMInfo> SubInfoRecords = getInsertedSIMList(ctx);
        for (SIMInfo record : SubInfoRecords) {
            if (record.mICCId.equals(iccid)) {
                return record;
            }
        }
        return null;
    }

    /**
     * @param ctx
     * @param SIMId
     * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
     */
    public int getSlotById(Context ctx, long SimId) {
        int slotId = (int) SimId - 1;
        return slotId;
    }

    /**
     * @param ctx
     * @param SIMName
     * @return the slot of the SIM Card, -1 indicate that the SIM card is missing
     */
    public int getSlotByName(Context ctx, String SIMName) {
        return 0;
    }

    /**
     * @param ctx
     * @return current SIM Count
     */
    public int getInsertedSIMCount(Context ctx) {
        if (mClass == null) {
            return -1;
        }

        Object retObj = FrameworkUtility.invokeStaticMethod(mClass, "getActiveSubInfoCount");
        if (retObj != null) {
            return (Integer) retObj;
        }

        return 0;
    }

    /**
     * @param ctx
     * @return the count of all the SIM Card include what was used before
     */
    public int getAllSIMCount(Context ctx) {
        if (mClass == null) {
            return -1;
        }

        Object retObj = FrameworkUtility.invokeStaticMethod(mClass, "getAllSubInfoCount");
        if (retObj != null) {
            return (Integer) retObj;
        }

        return 0;
    }
    
    public int getDefaultDataSubId() {
        if (mClass == null) {
            return -1;
        }
        Object subId = FrameworkUtility.invokeStaticMethod(mClass, "getDefaultDataSubId");
        
        if (subId != null) {
            return ((Long) subId).intValue();
        }

        return 0;
    }

}

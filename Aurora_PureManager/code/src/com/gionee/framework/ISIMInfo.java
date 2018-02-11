package com.gionee.framework;

import java.util.List;

import android.content.Context;

public interface ISIMInfo {

    public List<SIMInfo> getInsertedSIMList(Context ctx);

    public List<SIMInfo> getAllSIMList(Context ctx);

    public SIMInfo getSIMInfoById(Context ctx, long SIMId);

    public SIMInfo getSIMInfoByName(Context ctx, String SIMName);

    public SIMInfo getSIMInfoBySlot(Context ctx, int cardSlot);

    public SIMInfo getSIMInfoByICCId(Context ctx, String iccid);

    public int getSlotById(Context ctx, long SimId);

    public int getSlotByName(Context ctx, String SIMName);

    public int getInsertedSIMCount(Context ctx);

    public int getAllSIMCount(Context ctx);
    
    public int getDefaultDataSubId();
}

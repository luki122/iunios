package com.android.mms.model;
// Aurora xuyong 2013-12-11 created for aurora's new feature
import java.util.ArrayList;
import gionee.provider.GnTelephony.Sms;

public class GroupItemInfoModel {
    
    private int mNums;
    private int mPositions;
    private ArrayList<Long> mIds = new ArrayList<Long>();
    private ArrayList<Integer> mStatus = new ArrayList<Integer>();
    private ArrayList<String> mAddress = new ArrayList<String>();
    
    public int getNums() {
        return mNums;
    }
    
    public int getPositions() {
        return mPositions;
    }
    
    public ArrayList<Long> getIds() {
        return mIds;
    }
    
    public ArrayList<Integer> getStatus() {
        return mStatus;
    }
    
    public ArrayList<String> getAddress() {
        return mAddress;
    }
    
    public void setNums(int nums) {
        mNums = nums;
    }
    
    public void setPositions(int pos) {
        mPositions = pos;
    }
    
    public void addIds(long id) {
        if (mIds != null) {
            mIds.add(id);
        }
    }
        
    public void addStatus(int errorTypes) {
        if (mStatus != null) {
            mStatus.add(errorTypes);
        }
    }
    
    public void addAddress(String ads) {
        if (mAddress != null) {
            mAddress.add(ads);
        }
    }

}

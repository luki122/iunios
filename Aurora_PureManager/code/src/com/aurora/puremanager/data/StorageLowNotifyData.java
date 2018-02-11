package com.aurora.puremanager.data;

/**
 * 存储空间不足，提示用户
 */
public class StorageLowNotifyData {
    private boolean isAlreadyLow = false;
    private int alreadyNotifyTimes = 0;
    private long lastNotifyTime = 0;
    
    /**
     * 存储空间是否已经不足
     * @param isAlreadyLow
     */
    public void setIsAlreadyLow(boolean isAlreadyLow){
    	this.isAlreadyLow = isAlreadyLow;
    }
    
    /**
     * 已经提示用户的次数
     * @param alreadyNotifyTimes
     */
    public void setAlreadyNotifyTimes(int alreadyNotifyTimes){
    	this.alreadyNotifyTimes = alreadyNotifyTimes;
    }
    
    /**
     * 上次提示用户的时间
     * @param lastNotifyTime
     */
    public void setLastNotifyTime(long lastNotifyTime){
    	this.lastNotifyTime = lastNotifyTime;
    }
    
    /**
     * 存储空间是否已经不足
     * @param isAlreadyLow
     */
    public boolean getIsAlreadyLow(){
    	return this.isAlreadyLow;
    }
    
    /**
     * 已经提示用户的次数
     * @param alreadyNotifyTimes
     */
    public int getAlreadyNotifyTimes(){
    	return this.alreadyNotifyTimes;
    }
    
    /**
     * 上次提示用户的时间
     * @param lastNotifyTime
     */
    public long getLastNotifyTime(){
    	return this.lastNotifyTime;
    }
}

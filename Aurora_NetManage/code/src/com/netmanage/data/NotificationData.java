package com.netmanage.data;

/**
 * 提示用户记录数据
 */
public class NotificationData {
    private int alreadyNotifyTimes = 0;
    private DateData lastNotifyTime;
  
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
    public void setLastNotifyTime(DateData lastNotifyTime){
    	this.lastNotifyTime = lastNotifyTime;
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
    public DateData getLastNotifyTime(){
    	return this.lastNotifyTime;
    }
}

package com.aurora.commemoration.model;

/**
 * Created by joy on 4/27/15.
 */
public class RememberDayInfo {
    private int id; // 纪念日id
    private String title; // 纪念日标题
    private String day; // 纪念日日期
    private Long reminderData; // 提醒数据
    private String picPath; // 背景图路径
    private int scheduleFlag; // 日程标记 0 false 1 true
    private String createTime; // 创建时间
    private boolean futureFlag; // 过去、还有flag true past false future
    private long millTime; //创建时间 毫秒

    public void setId(int id) {
        this.id = id;
    }

    public void setMillTime(long millTime) {
        this.millTime = millTime;
    }

    public void setFutureFlag(boolean futureFlag) {
        this.futureFlag = futureFlag;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDay(String day) {
        this.day = day;
    }


    public void setPicPath(String picPath) {
        this.picPath = picPath;
    }

    public void setScheduleFlag(int scheduleFlag) {
        this.scheduleFlag = scheduleFlag;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDay() {
        return day;
    }

  
    public Long getReminderData() {
		return reminderData;
	}

	public void setReminderData(Long reminderData) {
		this.reminderData = reminderData;
	}

	public String getPicPath() {
        return picPath;
    }

    public int getScheduleFlag() {
        return scheduleFlag;
    }

    public boolean getFutureFlag() {
        return futureFlag;
    }

    public String getCreateTime() {
        return createTime;
    }

    public long getMillTime() {
        return millTime;
    }


}

package com.aurora.note.bean;

import java.util.ArrayList;

/**
 * @author JimXia
 * 2014-7-25 下午2:01:04
 */
public class RecorderInfo {
    // 对应序列ID
    private int id;
    private String path;
    private String name;
    private ArrayList<MarkInfo> marks;
    private long createTime;
    private long duration;
    private int sampleRate;
    
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ArrayList<MarkInfo> getMarks() {
        return marks;
    }
    public String getMarkInfo() {
        if (marks == null) {
            marks = new ArrayList<MarkInfo>();
        }
        return marks.toString();
    }
    public void setMarks(ArrayList<MarkInfo> marks) {
        this.marks = marks;
    }
    public long getCreateTime() {
        return createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public int getSampleRate() {
        return sampleRate;
    }
    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }
}

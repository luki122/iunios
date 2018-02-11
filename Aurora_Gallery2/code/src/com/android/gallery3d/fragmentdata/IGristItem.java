package com.android.gallery3d.fragmentdata;

public interface IGristItem {
    public long getId();
    public String getTitle(int type);
    public long getTime();
    public int getType();
    public String getUri();
    public String getFilePath();
    public int getRotation();
}
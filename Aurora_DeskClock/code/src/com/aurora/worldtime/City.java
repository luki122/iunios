package com.aurora.worldtime;

import android.hardware.Camera.Size;

public class City {
    private String id;
    private String name;
    private String gmt;
    private int offset;

    public City() {        
    }

    public City(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGmt() {
        return gmt;
    }

    public void setGmt(String gmt) {
        this.gmt = gmt;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof City && id != null
                && id.equals(((City) o).getId())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return 1;
    }

}

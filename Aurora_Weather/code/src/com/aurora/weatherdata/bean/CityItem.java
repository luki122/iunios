package com.aurora.weatherdata.bean;

/**
 * Created by joy on 10/20/14....
 */

public class CityItem {
    private int id;             //int
    private String cityName;
    private String upCity;
    private String province;
    private String pinyin;
    private String pinyinSn;
    private boolean isCity;

    public void setCityFlag(boolean isCity) {
        this.isCity = isCity;
    }

    public boolean getCityFlag() {
        return isCity;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public void setUpCity(String upCity) {
        this.upCity = upCity;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public void setPinyinSn(String pinyinSn) {
        this.pinyinSn = pinyinSn;
    }

    public int getId() {
        return id;
    }

    public String getCityName() {
        return cityName;
    }

    public String getUpCity() {
        return upCity;
    }

    public String getProvince() {
        return province;
    }

    public String getPinyin() {
        return pinyin;
    }

    public String getPinyinSn() {
        return pinyinSn;
    }
}

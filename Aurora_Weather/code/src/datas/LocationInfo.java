package datas;

/**
 * Created by joy on 11/4/14.
 */
public class LocationInfo {
    private String province;
    private String city;
    private String district;
    private String getTime;

    public String getGetTime() {
        return getTime;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public void setGetTime(String getTime) {
        this.getTime = getTime;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }
}

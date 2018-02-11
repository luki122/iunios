
package com.aurora.ota.reporter;

import gn.com.android.update.business.OtaReceiver;
import gn.com.android.update.utils.LogUtils;
import gn.com.android.update.utils.Util;

import java.security.GeneralSecurityException;
import java.util.List;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.aurora.ota.database.RepoterManager;
import com.aurora.ota.location.LocationInfo;
import com.aurora.ota.reporter.appnumber.AppInfo;
import com.aurora.ota.reporter.appnumber.PakageInfoProvider;

public class ReporterItem implements Parcelable {

    private int id;
    private String apkVersion = "";
    private String appName = "";
    private String imei = "";
    private String mobileModel = "";
    private String mobileNumber = "";
    private String registerUserId = "";
    private String shutdownTime = "";
    private String startupTime = "";
    private int status;
    private int reported;
    private int appNum;
    
    private String creatItemTime = "";
    private String duration = "";

	private String mChanel = "";
    private String mLocation="";
    
    private String mPhoneWidth;
    private String mPhoneHeight;
    
    private String mPhoneNumber;
    
    private String city="";
    private String province="";
//    apkVersion":"apk20130903","appName":"玩机圈APP","channelName":"应用汇"," +
//    		""createTime":null,"id":null,"imei":"ubXyyGU5Ywg8Go4fJ77VPw==","
//    		locationCountry":"中国","locationProvince":"深圳","locationCity":"福田区","
//    		locationTime":1396928664762,"mobileModel":"HTC","mobileNumber":"
//            4WwAjkZA3mXip36ddmhFGw==","modelHeight":1920,"modelWidth":1080,"networkIp":"14.197.239.255

    private final long reporterTimeLimit = 24*60*60*1000;
    public ReporterItem(){}
    public static final Parcelable.Creator<ReporterItem> CREATOR = new Parcelable.Creator<ReporterItem>()
    {
        public ReporterItem createFromParcel(Parcel in)
        {
            return new ReporterItem(in);
        }

        public ReporterItem[] newArray(int size)
        {
            return new ReporterItem[size];
        }
    };

    private ReporterItem(Parcel in)
    {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in)
    {
        id = in.readInt();
        apkVersion = in.readString();
        appName = in.readString();
        imei = in.readString();

        mobileModel = in.readString();
        mobileNumber = in.readString();
        registerUserId = in.readString();

        shutdownTime = in.readString();
        startupTime = in.readString();

        status = in.readInt();
        reported = in.readInt();
        mLocation = in.readString();
        appNum = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flag)
    {
        //out.writeInt(id);
        out.writeString(apkVersion);
        out.writeString(appName);
        out.writeString(imei);

        out.writeString(mobileModel);
        out.writeString(mobileNumber);
        out.writeString(registerUserId);

        out.writeString(shutdownTime);
        out.writeString(startupTime);

        out.writeInt(status);
        out.writeInt(reported);
        out.writeString(mLocation);
        out.writeInt(appNum);
    }

    
    
    
    public void setLocation(String location){
        this.mLocation = location;
    }
    
    public String getLocation(){
        return mLocation;
    }
    
    public String getmChanel() {
        return mChanel;
    }

    public void setmChanel(String mChanel) {
        this.mChanel = mChanel;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    public String getCreatItemTime() {
		return creatItemTime;
	}

	public void setCreatItemTime(String creatItemTime) {
		this.creatItemTime = creatItemTime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMobileModel() {
        return mobileModel;
    }

    public void setMobileModel(String mobileModel) {
        this.mobileModel = mobileModel;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getRegisterUserId() {
        return registerUserId;
    }

    public void setRegisterUserId(String registerUserId) {
        this.registerUserId = registerUserId;
    }

    public String getShutdownTime() {
        return shutdownTime;
    }

    public void setShutdownTime(String shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    public String getStartupTime() {
        return startupTime;
    }

    public void setStartupTime(String startupTime) {
        this.startupTime = startupTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getReported() {
        return reported;
    }

    public void setReported(int reported) {
        this.reported = reported;
    }
    
    public int getAppNum() {
		return appNum;
	}

	public void setAppNum(int appNum) {
		this.appNum = appNum;
	}

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "id :"+id+" apkVersion"+apkVersion+"  appName"+
                appName+" status " +status+" startupTime"+startupTime+" reported:"+reported+" imei:"+imei+"appNum:"+appNum + "duration :" + duration;
    }
   
   
    
    public boolean equalsItem(ReporterItem other) {
//        // TODO Auto-generated method stub
        return apkVersion.equals(other.getApkVersion())&&
        appName.equals(other.getAppName())&&
        imei.equals(other.getImei())&&
        mobileModel.equals(other.getMobileModel())&&
        mobileNumber.equals(other.getMobileNumber())
        &&registerUserId.equals(other.getRegisterUserId())&&inOneday(other);
    }
    
    private boolean inOneday(ReporterItem other){
        long result = Math.abs(Long.parseLong(startupTime)-Long.parseLong(other.getStartupTime()+""));
        return result<reporterTimeLimit;
        
    }
    
    
    
    
    
    
    

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getPhoneWidth() {
        return mPhoneWidth;
    }

    public void setPhoneWidth(String mPhoneWidth) {
        this.mPhoneWidth = mPhoneWidth;
    }

    public String getPhoneHeight() {
        return mPhoneHeight;
    }

    public void setPhoneHeight(String mPhoneHeight) {
        this.mPhoneHeight = mPhoneHeight;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }
    

    public static ReporterItem createItem(Context context){
        RepoterManager mManager = RepoterManager.getInstance(context);
        int phoneWidth = 1080;
        int phoneHeight = 1920;
        String province = "";
        String city = "";
        String country = "";
        LocationInfo location = mManager.getHistoryLocation();
        if(location != null){
            province = location.getProvince()+"";
            city = location.getCity()+"";
            country = location.getCoutry();
        }
        int[] phoneSize = mManager.getPhoneSize();
        if(phoneSize != null){
            phoneWidth = phoneSize[0];
            phoneHeight = phoneSize[1];
        }
        ReporterItem item = new ReporterItem();
        long currentTime = System.currentTimeMillis();
        String version = Util.getInternalVersion();
        String imei = "";
        try {
            imei = APKinfoUtil.getUniqueID(context);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //得到安装应用的个数
        int i = 0;
       List<AppInfo> appInfos =  new PakageInfoProvider(context).getAppInfo();
    	for (AppInfo appInfo : appInfos) {
			if (appInfo.getIsUserApp()) {
				i++;
			} 
		}
        
        item = new ReporterItem();
        item.setAppNum(i);
        item.setApkVersion(version + "");
        item.setmChanel(APKinfoUtil.getChanel(context));
        item.setAppName(APKinfoUtil.getMobilModel() + "");
        item.setMobileModel(APKinfoUtil.getMobilModel() + "");
        item.setImei(imei);
        item.setMobileNumber(" ");
        item.setStartupTime(OtaReceiver.startupTime + "");
        item.setShutdownTime(OtaReceiver.shutdownTime + "");
        item.setCreatItemTime(System.currentTimeMillis()+ "");
        LogUtils.log("1122", "setDuration:  "  + RepoterManager.getInstance(context).getUsageTime());
        item.setDuration(RepoterManager.getInstance(context).getUsageTime() + "");
        LogUtils.log("1122", "after setDuration:  "  + item.getDuration());
        item.setReported(0);
        item.setPhoneHeight(phoneHeight+"");
        item.setPhoneWidth(phoneWidth+"");
       
        try {
            Log.e("luofu", "cellphone:"+APKinfoUtil.getPhoneNumber(context));
            item.setMobileNumber(APKinfoUtil.getPhoneNumber(context));
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            item.setPhoneNumber("");
        }
        item.setLocation(province+Constants.SPLITE+city);

        return item;
    }

}

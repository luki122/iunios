package com.adblock.data;


import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import com.secure.data.MyArrayList;

/**
 * 记录广告提供商的信息
 */
public class AdProviderData {
    private String providerName;
    private String url;
    private String adClassCommName;
    private MyArrayList<AdClassData> adClassList = null;
    private MyArrayList<String> adPermList = null;
    private boolean isRiskOfPerm = false;//广告运营商申请的权限是否有风险。
    private boolean isHaveNotifyAd;//是否含有通知栏广告
    private boolean isHaveViewAd;//是否含有视图广告
    
    public void setProviderName(String providerName){
    	this.providerName = providerName;
    }
    
    @Override
    public String toString() {
        return "AdProviderData [providerName=" + providerName + ", url=" + url
                + ", adClassCommName=" + adClassCommName + ", adClassList=" + adClassList
                + ", adPermList=" + adPermList + ", isRiskOfPerm=" + isRiskOfPerm
                + ", isHaveNotifyAd=" + isHaveNotifyAd + ", isHaveViewAd=" + isHaveViewAd + "]";
    }

    public String getProviderName(){
    	return this.providerName;
    }
    
    public void setUrl(String url){
    	this.url = url;
    }
    
    public String getUrl(){
    	return this.url;
    }
    
    public void setAdClassCommName(String adClassCommName){
    	this.adClassCommName = adClassCommName;
    }
    
    public String getAdClassCommName(){
    	return this.adClassCommName;
    }
    
    public void addAdClassData(AdClassData adClassData){
    	if(adClassList == null){
    		adClassList = new MyArrayList<AdClassData>();
    	}
    	
    	adClassList.add(adClassData);
    }
      
    public MyArrayList<AdClassData> getAdClassList(){
    	return adClassList;
    }
    
    public void addAdPermStr(PackageManager packageManager, String permName){
    	if(adPermList == null){
    		adPermList = new MyArrayList<String>();
    	}
    	
    	if(permName != null){
    		adPermList.add(permName);
    		if(packageManager != null && !getIsRiskOfPerm()){
    			try{
    				PermissionInfo permissionInfo=packageManager.
        					getPermissionInfo(permName, PackageManager.GET_META_DATA);
    				if(permissionInfo != null && 
    					permissionInfo.protectionLevel != PermissionInfo.PROTECTION_NORMAL){
    					setIsRiskOfPerm(true);
    				}
    			}catch(Exception e){
    				e.printStackTrace();
    			}  			
    		}
    	}   	
    }
      
    public MyArrayList<String> getAdPermList(){
    	return adPermList;
    }
    
    /**
     * 广告运营商申请的权限是否有风险
     * @return
     */
    public boolean getIsRiskOfPerm(){
    	return this.isRiskOfPerm;
    }
    
    /**
     * 广告运营商申请的权限是否有风险
     * @param isRiskOfPerm
     */
    private void setIsRiskOfPerm(boolean isRiskOfPerm){
    	this.isRiskOfPerm = isRiskOfPerm;
    }
    
    /**
     * 是否含有通知栏广告
     * @return
     */
    public void setIsHaveNotifyAd(boolean isHaveNotifyAd){
    	this.isHaveNotifyAd = isHaveNotifyAd;
    }
    
    /**
     * 是否含有视图广告
     * @return
     */
    public void setIsHaveViewAd(boolean isHaveViewAd){
    	this.isHaveViewAd = isHaveViewAd;
    }
    
    /**
     * 是否含有通知栏广告
     * @return
     */
    public boolean getIsHaveNotifyAd(){
    	return this.isHaveNotifyAd;
    }
    
    /**
     * 是否含有视图广告
     * @return
     */
    public boolean getIsHaveViewAd(){
    	return this.isHaveViewAd;
    }
    
    /**
     * 判断当前类名是不是广告类
     * @param adClassName
     * @return
     */
    public boolean isAdClass(String className){
    	if(adClassList == null || className == null){
    		return false;
    	}
    	
    	AdClassData adClassData = null;
		for(int i=0;i<adClassList.size();i++){
			adClassData = adClassList.get(i);
			if(adClassData == null){
				continue;
			}
			if(className.equals(adClassData.getName())){
				return true;
			}  		
		}
		return false;
    }
}

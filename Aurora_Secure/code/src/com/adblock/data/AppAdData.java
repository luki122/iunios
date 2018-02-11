package com.adblock.data;

import java.util.ArrayList;
import com.secure.data.MyArrayList;
import com.secure.utils.StringUtils;

/**
 * 记录app中的广告插件的信息
 */
public class AppAdData {
    private String pkgName;
    private int versionCode;
    private MyArrayList<AdProviderData> adProviderList = null;
    
    public void setPkgName(String pkgName){
    	this.pkgName = pkgName;
    }
       
    public String getPkgName(){
    	return this.pkgName;
    }
    
    public void addAdProviderData(AdProviderData adProviderData){
    	synchronized (this) {
    		if(adProviderList == null){
        		adProviderList = new MyArrayList<AdProviderData>();
        	}
        	adProviderList.add(adProviderData);
		}   	
    }
    
    public MyArrayList<AdProviderData> getAdProviderList(){
    	synchronized (this) {
    		return this.adProviderList;
    	}   	
    }
  
	public int getVersionCode(){
		return this.versionCode;
	}
	
	public void setVersionCode(int versionCode){
		this.versionCode = versionCode;
	}
    
    public AidlAdData createAidlAdData(){  			
		ArrayList<String> adClassNameList = new ArrayList<String>();
		
		synchronized (this) {
			int sizeAdProvider = adProviderList==null?0:adProviderList.size();
			for(int i=0;i<sizeAdProvider;i++){
				AdProviderData adProviderData = adProviderList.get(i);
				if(adProviderData == null){
					continue;
				}
				
				if(!StringUtils.isEmpty(adProviderData.getAdClassCommName())){
					adClassNameList.add(adProviderData.getAdClassCommName());
				}else{
					MyArrayList<AdClassData> adClassList = adProviderData.getAdClassList();
					int sizeClass = adClassList==null?0:adClassList.size();
					for(int j=0;j<sizeClass;j++){
						AdClassData adClassData = adClassList.get(j);
						if(adClassData != null){
							adClassNameList.add(adClassData.getName());
						}								
					}
				}
			}
		}
				
		AidlAdData aidlAdData = new AidlAdData();
		aidlAdData.setPkgName(pkgName);	
		aidlAdData.setAdClassLis(adClassNameList);
		return aidlAdData;
    }
    
    /**
     * 获取广告sdk申请的权限是否有风险
     * @return
     */
    public boolean getIsAdProviderPermRisk(){
    	if(adProviderList == null){
    		return false;
    	}
    	for(int i=0;i<adProviderList.size();i++){
    		AdProviderData adProviderData = adProviderList.get(i);
    		if(adProviderData == null){
    			continue;
    		}
    		if(adProviderData.getIsRiskOfPerm()){
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * 是否包含某个广告提供商
     * @param providerName
     * @return
     */
    public boolean isHaveAdProvider(String providerName){
    	if(adProviderList == null || providerName == null){
    		return false;
    	}
    	for(int i=0;i<adProviderList.size();i++){
    		AdProviderData adProviderData = adProviderList.get(i);
    		if(adProviderData == null){
    			continue;
    		}
    		if(providerName.equals(adProviderData.getProviderName())){
    			return true;
    		}
    	}
    	return false;
    }
}

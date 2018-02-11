package com.secure.data;

public class PrivacyAppData extends BaseData{

	private long accountId;
	private String pkgName;
	
	public PrivacyAppData() {
		super(PrivacyAppData.class.getName());
	}
	
	public void setAccountId(long accountId){
		this.accountId = accountId;
	}
	
	public void setPkgName(String pkgName){
		this.pkgName = pkgName;
	}
	
	public long getAccountId(){
		return this.accountId;
	}
	
	public String getPkgName(){
		return this.pkgName;
	}

}

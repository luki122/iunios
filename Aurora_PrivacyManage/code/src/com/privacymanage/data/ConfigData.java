package com.privacymanage.data;

public class ConfigData extends BaseData{
	
	public ConfigData() {
		super(ConfigData.class.getName());
	}

	/**
	 * 最后被使用的隐私账户id
	 */
	private long lastAccountId = 0;
	
    /**
     * 最后被使用的隐私账户id
     * @param lastAccountId
     */
	public void setLastAccountId(long lastAccountId){
		this.lastAccountId = lastAccountId;
	}
	
	/**
	 * 最后被使用的隐私账户id
	 * @return
	 */
	public long getLastAccountId(){
		return this.lastAccountId;
	}
}

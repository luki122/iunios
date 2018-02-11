package com.privacymanage.data;

public class ModuleInfoData extends BaseData{
	private String pkgName;
	private String className;
	private int itemNum;
	private boolean needShow;//是否要显示在主控制界面
		
	public ModuleInfoData() {
		super(ModuleInfoData.class.getName());
	}
    
    public void setPkgName(String pkgName){
    	this.pkgName = pkgName;
    }
    
    public String getPkgName(){
    	return this.pkgName;
    }
    
    public void setClassName(String className){
    	this.className = className;
    }
    
    public String getClassName(){
    	return this.className;
    }
    
    public void setItemNum(int itemNum){
    	this.itemNum = itemNum;
    }
    
    public int getItemNum(){
    	return this.itemNum;
    }
	
	/**
	 * 是否要显示在主控制界面
	 * @param needShow
	 */
	public void setNeedShow(boolean needShow){
		this.needShow = needShow;
	}
	
	/**
	 * 是否要显示在主控制界面
	 * @return
	 */
	public boolean getNeedShow(){
		return this.needShow;
	}
}

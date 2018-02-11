package com.aurora.puremanager.data;

import android.content.ComponentName;
import android.view.View;

public class MainActivityItemData extends BaseData{
	
	private int iconRes;
	private String itemName = "";
	private String hintStrFront = "";
	private String hintStrTail = "";
	private String hintStrTail2 = "";
	private ComponentName componentName;
	private int vilibileFlag = View.VISIBLE;//显示属性
	
	public MainActivityItemData() {
		super("MainActivityItemData");
	}
	
	public void setIconRes(int iconRes){
    	this.iconRes = iconRes;
    }
    
	public void setItemName(String itemName){
    	this.itemName = itemName;
    }
	
	public void setHintStrFront(String hintStrFront){
		this.hintStrFront = hintStrFront;
	}
    
	public void setHintStrTail(String hintStrTail){
		this.hintStrTail = hintStrTail;
	}
	
	public void setHintStrTail2(String hintStrTail2){
		this.hintStrTail2 = hintStrTail2;
	}
	
	public void setComponentName(ComponentName componentName){
		this.componentName = componentName;
	}
		
	public int getIconRes( ){
    	return this.iconRes;
    }
    
	public String getItemName( ){
		return this.itemName;
    }
    
	public String getHintStrFront( ){
		return this.hintStrFront;
	}
	
	public String getHintStrTail( ){
		return this.hintStrTail;
	}
	
	public String getHintStrTail2( ){
		return this.hintStrTail2;
	}
	
	public ComponentName getComponentName( ){
		return this.componentName;
	}
	
	/**
	 * 显示属性
	 * @param vilibileFlag 取值为：View.VISIBLE View.INVISIBLE View.GONE
	 */
	public void setVilibileFlag(int vilibileFlag){
		this.vilibileFlag = vilibileFlag;
	}
	
	/**
	 * 显示属性
	 * @return 取值为：View.VISIBLE View.INVISIBLE View.GONE
	 */
	public int getVilibileFlag(){
		return this.vilibileFlag ;
	}
}

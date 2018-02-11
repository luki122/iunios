package com.netmanage.data;

import com.lbe.security.service.sdkhelper.SDKConstants;

/**
 * 权限类，记录权限属性
 */
public class PermissionInfo {
	/**
	 * 权限图标
	 */
	public int icon;
	
	/**
	 * 权限id
	 */
	public int permId;
	
	/**
	 * 权限名
	 */
    public String name;
    
    /**
     * 权限描述
     */
    public String desc;
    
    /**
     * 设置当前权限的状态
     * @param curState  
     * 1.SDKConstants.ACTION_ACCEPT;
     * 2.SDKConstants.ACTION_PROMPT;
     * 3.SDKConstants.ACTION_PROMPT;
     * 4.SDKConstants.ACTION_DEFAULT;
     */
    private int curState = SDKConstants.ACTION_ACCEPT;
    
    /**
     * 设置当前权限的状态
     * @param curState  
     * 1.SDKConstants.ACTION_ACCEPT;
     * 2.SDKConstants.ACTION_PROMPT;
     * 3.SDKConstants.ACTION_PROMPT;
     * 4.SDKConstants.ACTION_DEFAULT;
     */
    public void setCurState(int curState){
    	this.curState = curState; 	
    }
    
    /**
     * 判断当前权限是否打开
     * @return
     */
    public boolean getIsOpen(){
    	boolean result ;
    	if(curState == SDKConstants.ACTION_REJECT){
			result = false;
		}else{
			result = true;
		}
    	return result;
    }
}

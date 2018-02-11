package com.secure.data;

import android.util.Log;

import com.aurora.secure.R;
import com.lbe.security.service.sdkhelper.SDKConstants;
import com.secure.utils.mConfig;

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
    
    public int getCurState(){
    	return this.curState;
    }
    
    public int getStateTextRes(){
    	int res = 0;
    	if(curState == SDKConstants.ACTION_ACCEPT)
    	{
    		return R.string.permission_accept;
    	}else if(curState == SDKConstants.ACTION_REJECT)
    	{
    		return R.string.permission_reject;
    	}else if(curState == SDKConstants.ACTION_PROMPT)
    	{
    		return R.string.permission_prompt;
    	}
    	
    	return res;
    }
    
    
    /**
     * 判断当前权限是否打开
     * @return
     */
    public boolean getIsOpen(){
    	return curState == SDKConstants.ACTION_ACCEPT;
//    	boolean result ;
//    	if(curState == SDKConstants.ACTION_REJECT){
//			result = false;
//		}else{
//			result = true;
//		}
//    	return result;
    }
    
    public void updateObject(PermissionInfo permissionInfo){
    	if(permissionInfo != null){
    		icon = permissionInfo.icon;
    		permId = permissionInfo.permId;
    		curState = permissionInfo.getCurState();
    	}
    }
}

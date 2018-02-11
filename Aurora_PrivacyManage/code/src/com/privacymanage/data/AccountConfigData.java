package com.privacymanage.data;

public class AccountConfigData {
	private boolean msgNotifySwitch;
	private String msgNotifyHintStr;
    
    public void setMsgNotifySwitch(boolean msgNotifySwitch){
    	this.msgNotifySwitch = msgNotifySwitch;
    }
    
    public boolean getMsgNotifySwitch(){
    	return this.msgNotifySwitch;
    }
    
    public void setMsgNotifyHintStr(String msgNotifyHintStr){
    	this.msgNotifyHintStr = msgNotifyHintStr;
    }
    
    public String getMsgNotifyHintStr(){
    	return this.msgNotifyHintStr;
    }
    
    public void copy(AccountConfigData accountConfigData){
    	if(accountConfigData != null){
    		setMsgNotifySwitch(accountConfigData.getMsgNotifySwitch());
    		setMsgNotifyHintStr(accountConfigData.getMsgNotifyHintStr());
    	}
    }
}

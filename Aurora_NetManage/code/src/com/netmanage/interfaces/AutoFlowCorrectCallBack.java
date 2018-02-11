package com.netmanage.interfaces;

import com.netmanage.data.AutoCorrectInfo;

/**
 * 流量自动校正的回调
 */
public interface AutoFlowCorrectCallBack {
	public enum STEP{
		BEGIN,
		SEND_MSG,
		END
	}
	
	/**
	 * 流量自动校正的回调
	 * @param autoCorrectInfo
	 */
	public void autoFlowCorrectCallBack(AutoCorrectInfo autoCorrectInfo);

}

package com.netmanage.data;

import com.netmanage.interfaces.AutoFlowCorrectCallBack.STEP;

/**
 * 流量校正过程中，记录状态信息的数据
 * @author chengrq
 *
 */
public class AutoCorrectInfo {
	public boolean isSucess;
	public STEP step;
	public String hintMsg;
	public AutoCorrectInfo(boolean isSucess, STEP step,
			String hintMsg){
		this.isSucess = isSucess;
		this.step = step;
		this.hintMsg = hintMsg;
	}
}

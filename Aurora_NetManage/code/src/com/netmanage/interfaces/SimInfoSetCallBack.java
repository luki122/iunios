package com.netmanage.interfaces;

import com.netmanage.view.SimInfoSetDlg;

/**
 * sim信息设置后的回调
 */
public interface SimInfoSetCallBack {
	/**
	 * sim信息设置后的回调
	 * @param isCancel 是否取消了选择
	 * @param type 具体选择项目
	 */
	public void result(boolean isCancel,SimInfoSetDlg.SetSimInfoTypeEnum type);

}

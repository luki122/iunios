package com.secure.interfaces;

public interface LBEServiceBindObserver {
	/**
	 * LBE绑定状态的改变
	 * @param isConnection true：连接，false：断开
	 */
	public void LBEServiceBindStateChange(boolean isConnection);
	
	
}

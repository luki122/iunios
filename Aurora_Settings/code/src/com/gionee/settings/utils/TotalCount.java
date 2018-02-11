package com.gionee.settings.utils;

import android.content.Context;
import java.util.concurrent.ThreadPoolExecutor;

public class TotalCount implements Runnable {
	
	public static final String TAG = "TotalCount";
	

	private Context m_context;
	private String m_module = "";
	private String m_action = "";
	private int value = 0;

	
	/**
	 * 构建文件下载器
	 * 
	 * @param module_id 模块id
	 * @param action_id 动作id
	 * @param value 值
	 */
	public TotalCount(Context context,String module_id,String action_id,int value) {
		this.m_context = context;
		this.m_module = module_id;
		this.m_action = action_id;
		this.value = value;
	}
	
	@Override
	public void run() {
		startCount();
	}

	/**
	 * 开始计数
	 * 
	 */
	private void startCount() {
		DataCount.updataData(m_context, m_module, m_action, value);
	}
	

	
	//============对外控制方法开始=============//

	/**
	 * 统计队列
	 * 
	 */
	public void CountData() {
		ThreadPoolExecutor threadPool = CountManage.getThreadPoolExecutor();
		threadPool.execute(this);
	}
	

}

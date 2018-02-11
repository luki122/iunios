package com.baidu.xcloud.pluginAlbum;
import com.baidu.xcloud.pluginAlbum.bean.FileTaskStatusBean;

/**
 * 任务回调接口定义
 * 
 */
interface IAlbumTaskCallback {
	/**
	 * 任务消息(任务的状态等,具体参见FileTaskStatusBean的定义)
	 * 
	 */
	 void onGetTaskStatus(in FileTaskStatusBean fileTaskStatusBean);

    /**
	 * 设置更新进度的间隔时间。 默认间隔为2000ms, 允许的最小时间间隔是100ms。
	 * 
	 */
	 long progressInterval();
	  
	 /**
	  *任务列表
	  * 
	  */
	 void onGetTaskListFinished(in List<FileTaskStatusBean> fileTaskStatusBeanList);
	 /**
	  * xcloud 框架错误
	  */
	 void onXcloudError(in int errorCode);
}

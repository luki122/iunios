package com.aurora.fileObserver;
/**
 * 监听文件夹 增加 修改 删除 
 * @author jiangxh
 * @CreateTime 2014年5月17日 上午10:05:50
 * @Description com.aurora.fileObserver AuroraFileObserverListener.java
 */
public interface AuroraFileObserverListener {
	public void onFileCreated(String path);
	public void onFileDeleted(String path);
	public void onFileModified(String path);
	public void onFileRenamed(String path);
}
